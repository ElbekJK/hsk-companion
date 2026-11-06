#!/usr/bin/env python3
"""Generate reproducible pre-recorded HSK audio via Azure Speech REST on Debian.

Keys are read ONLY from the environment. They are never written to the app,
manifest, logs, or ZIP. Use --dry-run without credentials to inspect the plan.
"""
import argparse, csv, hashlib, html, json, os, re, subprocess, sys, tempfile, time
from pathlib import Path
from urllib import request, error

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
OUT=ASSETS/'audio'
VOICE={'a':'zh-CN-XiaoxiaoNeural','b':'zh-CN-YunxiNeural'}
PREVIEW='你好，很高兴认识你。'
OUTPUT_FORMAT='audio-24khz-48kbitrate-mono-mp3'
GENERATOR_VERSION='v1.2.0'

def digest(speaker,text):
    return hashlib.sha256((speaker+'\n'+text).encode('utf-8')).hexdigest()[:24]

def filename(speaker,text):
    return f'{speaker}/{digest(speaker,text)}.mp3'

def plan():
    jobs={}
    def add(speaker,text,category):
        assert speaker in VOICE
        text=text.strip()
        if not text: return
        key=(speaker,text)
        if key not in jobs: jobs[key]={'role':speaker,'voice':VOICE[speaker],
                 'text':text,'file':filename(speaker,text),'categories':[]}
        if category not in jobs[key]['categories']: jobs[key]['categories'].append(category)
    for s in ('a','b'): add(s,PREVIEW,'voice_preview')
    for row in json.loads((ASSETS/'vocabulary.json').read_text()):
        if row.get('standard','2.0')=='2.0': add('a',row['hanzi'],'vocabulary')
    for row in json.loads((ASSETS/'examples.json').read_text()): add('a',row['chinese'],'example')
    for row in json.loads((ASSETS/'grammar.json').read_text()): add('a',row['hanzi'],'grammar')
    for row in json.loads((ASSETS/'dialogues.json').read_text()):
        add('a',row['a'],'dialogue_a')
        add('b',row['b'],'dialogue_b')
    return sorted(jobs.values(),key=lambda j:(j['role'],j['file']))

def ssml(job):
    # Do not substitute polyphonic Hanzi automatically: review each audio file.
    return ("<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='zh-CN'>"
            f"<voice name='{job['voice']}'>"
            f"<prosody rate='0%'>{html.escape(job['text'],quote=True)}</prosody>"
            '</voice></speak>').encode('utf-8')

def synthesize(job,region,key):
    url=f'https://{region}.tts.speech.microsoft.com/cognitiveservices/v1'
    data=ssml(job)
    req=request.Request(url,data=data,method='POST',headers={
        'Ocp-Apim-Subscription-Key':key,'Content-Type':'application/ssml+xml',
        'X-Microsoft-OutputFormat':OUTPUT_FORMAT,
        'User-Agent':'HSKCompanion-DebianAudioBuilder/1.1'})
    for attempt in range(4):
        try:
            with request.urlopen(req,timeout=35) as response:
                body=response.read()
                if not body.startswith(b'ID3') and not (len(body)>2 and body[0]==0xff and (body[1]&0xe0)==0xe0):
                    raise RuntimeError('Azure response is not recognizable MP3 audio')
                return body
        except error.HTTPError as ex:
            if ex.code in (401,403):
                raise RuntimeError(f'Azure authorization failed (HTTP {ex.code}). Check key, resource, region and billing.') from None
            if ex.code not in (429,500,502,503,504) or attempt==3:
                raise RuntimeError(f'Azure Speech request failed (HTTP {ex.code}); no secret or response body printed.') from None
            time.sleep(2**attempt)
        except error.URLError:
            if attempt==3: raise RuntimeError('Cannot reach Azure Speech endpoint. Check network and region.') from None
            time.sleep(2**attempt)
    raise RuntimeError('Speech generation failed')

def duration_and_codec(path):
    try:
        p=subprocess.run(['ffprobe','-v','error','-select_streams','a:0',
            '-show_entries','stream=codec_name:format=duration','-of','json',str(path)],
            capture_output=True,text=True,check=True)
        obj=json.loads(p.stdout)
        dur=float(obj.get('format',{}).get('duration',0))
        streams=obj.get('streams',[])
        codec=streams[0].get('codec_name','') if streams else ''
        if codec!='mp3' or not (0.20<=dur<=40):
            raise ValueError('invalid codec/duration')
        return round(dur,3)
    except (subprocess.CalledProcessError,ValueError,IndexError,KeyError) as exc:
        raise RuntimeError('Audio validation failed (ffprobe codec/duration).') from exc

def manifest_path(): return OUT/'audio_manifest.json'

def audit_plan(jobs):
    count=sum(len(j['text']) for j in jobs)
    units=sum(sum(2 if '\u4e00'<=c<='\u9fff' else 1 for c in j['text']) for j in jobs)
    roles={s:sum(1 for j in jobs if j['role']==s) for s in VOICE}
    print(f'Audio plan: {len(jobs)} distinct clips · {roles} · {count} raw characters / ~{units} CJK-adjusted billable units')
    print(f'Free Azure F0 quota (published): 500,000 neural TTS characters/month; check your resource and usage.')

def write_manifest(jobs):
    records=[]
    for j in jobs:
        path=OUT/j['file']
        if not path.exists():continue
        records.append({**j,'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),
             'bytes':path.stat().st_size,'duration_sec':duration_and_codec(path)})
    manifest={'schema':1,'generator':GENERATOR_VERSION,'source':'Azure AI Speech neural prebuilt voices',
       'synthetic':True,'disclaimer':'Review licensing and every audio clip before public redistribution.',
       'output_format':OUTPUT_FORMAT,'voices':VOICE,'planned':len(jobs),'generated':len(records),'records':records}
    OUT.mkdir(exist_ok=True,parents=True)
    tmp=manifest_path().with_suffix('.json.tmp')
    tmp.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    tmp.replace(manifest_path())
    return manifest

def write_review(manifest):
    review=OUT/'audio_review.csv'
    prev={}
    if review.exists():
        with review.open(encoding='utf-8',newline='') as f:
            for r in csv.DictReader(f):prev[r['file']]=r
    cols=['file','role','voice','text','sha256','review_status','notes']
    with review.open('w',encoding='utf-8',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=cols);writer.writeheader()
        for record in manifest['records']:
            old=prev.get(record['file'],{})
            can_retain=(old.get('sha256')==record['sha256'] and old.get('review_status')=='approved')
            writer.writerow({k:({'review_status':'approved' if can_retain else 'pending',
                'notes':old.get('notes','') if can_retain else ''}.get(k,record.get(k,''))) for k in cols})


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--preview',action='store_true',help='Generate a small preview: both voices + first two dialogues')
    p.add_argument('--all',action='store_true',help='Generate every word, sentence, grammar model and dialogue line')
    p.add_argument('--dry-run',action='store_true',help='Print plan (requires no Azure credentials)')
    p.add_argument('--limit',type=int,default=0,help='Cap API requests; useful for controlled testing')
    args=p.parse_args()
    jobs=plan()
    if args.preview:
        diag=json.loads((ASSETS/'dialogues.json').read_text())[:2]
        sample={(s,PREVIEW) for s in VOICE}
        sample.update({(s,d['a' if s=='a' else 'b']) for d in diag for s in VOICE})
        jobs=[j for j in jobs if (j['role'],j['text']) in sample]
    audit_plan(jobs)
    if args.dry_run or not (args.preview or args.all):
        if not args.dry_run:print('Choose --preview, --all or --dry-run. No API request made.')
        return 0
    region=os.environ.get('AZURE_SPEECH_REGION','').strip().lower()
    key=os.environ.get('AZURE_SPEECH_KEY','').strip()
    if not re.fullmatch(r'[a-z0-9-]{3,32}',region) or not key:
        print('Set AZURE_SPEECH_REGION and AZURE_SPEECH_KEY in your terminal; never put credentials in source files.',file=sys.stderr)
        return 2
    if not shutil_which('ffprobe'):
        print('ffprobe is required: sudo apt install ffmpeg',file=sys.stderr);return 2
    OUT.mkdir(exist_ok=True,parents=True)
    new=0
    for idx,job in enumerate(jobs,1):
        dest=OUT/job['file']
        if dest.exists():
            try:duration_and_codec(dest);continue
            except RuntimeError: dest.unlink()
        if args.limit and new>=args.limit:break
        dest.parent.mkdir(exist_ok=True,parents=True)
        print(f'[{idx}/{len(jobs)}] {job["role"]} {job["file"]}: {job["text"][:35]}')
        body=synthesize(job,region,key)
        with tempfile.NamedTemporaryFile(dir=dest.parent,suffix='.mp3',delete=False) as tmp:
            temp=Path(tmp.name);tmp.write(body)
        try:
            duration_and_codec(temp)
            temp.replace(dest)
        finally:
            temp.unlink(missing_ok=True)
        new+=1
        time.sleep(0.15)
    # Include all existing recordings that still belong to current full content plan,
    # even when --preview selects only a subset.
    all_jobs=plan()
    manifest=write_manifest(all_jobs)
    write_review(manifest)
    print(f'Generated {new} new recordings; {manifest["generated"]}/{manifest["planned"]} in offline package.')
    print('Next: python3 scripts/audit_audio.py --inspect; review audio_review.csv; then build APK.')
    return 0

def shutil_which(cmd):
    from shutil import which
    return which(cmd)

if __name__=='__main__':sys.exit(main())
