#!/usr/bin/env python3
"""Verify offline audio integrity, coverage and human review of Mandarin tones.

--inspect: report status, exit zero on valid partial pack. No cloud access.
--require-complete: fail unless ALL planned recordings exist.
--release: require complete, fully approved audio and local licensing confirmation.
"""
import argparse,csv,hashlib,json,sys
from pathlib import Path
from azure_audio import OUT,plan,filename,duration_and_codec,manifest_path

p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--inspect',action='store_true')
p.add_argument('--require-complete',action='store_true')
p.add_argument('--release',action='store_true')
a=p.parse_args()
planned=plan(); names={j['file']:j for j in planned}
if not manifest_path().exists():
    print(f'Offline audio not generated. {len(planned)} recordings planned. Android TTS fallback remains available.')
    if a.require_complete or a.release:sys.exit(1)
    sys.exit(0)
manifest=json.loads(manifest_path().read_text(encoding='utf-8'))
assert manifest['schema']==1 and manifest['synthetic'] is True
records=manifest['records']
seen=set();problems=[]
for r in records:
    name=r['file']
    if name in seen:problems.append('duplicate: '+name)
    seen.add(name)
    if name not in names:problems.append('stale content: '+name);continue
    if r['text']!=names[name]['text'] or r['role']!=names[name]['role']:
        problems.append('incorrect text / speaker: '+name)
    f=OUT/name
    if not f.is_file():problems.append('missing '+name);continue
    if hashlib.sha256(f.read_bytes()).hexdigest()!=r['sha256']:
        problems.append('sha256 mismatch: '+name)
    try: duration_and_codec(f)
    except Exception:problems.append('bad audio: '+name)
missing=set(names)-seen
review=OUT/'audio_review.csv'
reviewed={}
if review.is_file():
    with review.open(encoding='utf-8',newline='') as fh:
        for row in csv.DictReader(fh): reviewed[row['file']]=row
pending=[]
for r in records:
    x=reviewed.get(r['file'],{})
    if x.get('review_status')!='approved' or x.get('sha256')!=r['sha256']:
        pending.append(r['file'])
print(f'Audio: {len(seen)}/{len(names)} recordings · {len(missing)} missing · {len(pending)} pending human review')
print(f'  Female A: {sum(1 for x in records if x["role"]=="a")}; Male B: {sum(1 for x in records if x["role"]=="b")}')
if missing:print('  Examples of missing audio: '+', '.join(sorted(missing)[:4]))
if problems:print('  ERRORS: '+'; '.join(problems[:10]),file=sys.stderr)
if problems:sys.exit(1)
if (a.require_complete or a.release) and missing:sys.exit('Audio pack is incomplete.')
if a.release:
    if pending:sys.exit('Human review is incomplete. Listen and approve every clip in audio_review.csv.')
    accepted=OUT/'REDISTRIBUTION_RIGHTS_CONFIRMED.txt'
    if not accepted.is_file() or 'reviewed-and-confirmed' not in accepted.read_text():
        sys.exit('Licensing approval missing. Review Azure terms then create audio/REDISTRIBUTION_RIGHTS_CONFIRMED.txt with reviewed-and-confirmed and date.')
    print('Offline audio passed the publishing gate; this does not replace legal review.')
