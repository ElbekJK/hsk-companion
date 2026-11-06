#!/usr/bin/env python3
"""One-time, offline import of v1.1 Azure recordings into a separate v1.2 checkout.

Wait for the v1.1 generator to finish. We copy only the verified generated MP3s,
its review and provenance files, and its downloaded vocabulary. No secrets,
keystores, APKs, or environment files are copied.
"""
import argparse, hashlib, json, os, re, shutil, subprocess, sys, tempfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
DST=ROOT/'app/src/main/assets'
PATTERN=re.compile(r'^[ab]/[0-9a-f]{24}\.mp3$')

def filehash(p):
    h=hashlib.sha256()
    with p.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
    return h.hexdigest()

def main():
    cli=argparse.ArgumentParser(description=__doc__)
    cli.add_argument('source',help='v1.1 HSKCompanion project directory, e.g. ~/hsk-companion-v110/HSKCompanion')
    cli.add_argument('--dry-run',action='store_true',help='Show readiness and planned transfer without copying anything')
    args=cli.parse_args()
    source=Path(args.source).expanduser().resolve()
    audio=source/'app/src/main/assets/audio'
    manifest_file=audio/'audio_manifest.json'
    if source==ROOT:raise SystemExit('Source must be a DIFFERENT v1.1 project folder.')
    if not manifest_file.is_file():raise SystemExit('No source audio_manifest.json found. Finish generation in v1.1 first.')
    manifest=json.loads(manifest_file.read_text(encoding='utf8'))
    if manifest.get('schema')!=1 or not manifest.get('synthetic') or not isinstance(manifest.get('records'),list):
        raise SystemExit('Unexpected source audio manifest format.')
    total=manifest.get('planned',0)
    records=manifest['records']
    if not isinstance(total,int) or total<1 or len(records)!=total:
        raise SystemExit(f'Audio incomplete: {len(records)}/{total} recorded. Let v1.1 finish first; do not interrupt it.')
    vocab=source/'app/src/main/assets/vocabulary.json'
    if not vocab.is_file():raise SystemExit('v1.1 vocabulary.json missing.')
    words=json.loads(vocab.read_text(encoding='utf8'))
    if not isinstance(words,list) or len(words)<290 or len(words)>320:
        raise SystemExit('Unexpected vocabulary content; refusing to overwrite v1.2 assets.')
    ids=[(str(w.get('standard','2.0')),int(w['level']),w['hanzi']) for w in words]
    if len(ids)!=len(set(ids)):raise SystemExit('Duplicate words in source vocabulary.')
    paths=set()
    checked=[]
    for row in records:
        rel=row.get('file','')
        if not isinstance(rel,str) or not PATTERN.fullmatch(rel) or rel in paths:
            raise SystemExit('Invalid or duplicate relative audio file path in source manifest.')
        paths.add(rel)
        file=audio/rel
        if not file.is_file() or filehash(file)!=row.get('sha256'):
            raise SystemExit(f'Missing, changed or corrupted MP3 in v1.1 source: {rel}')
        checked.append((file,rel))
    for name in ('audio_review.csv',):
        if not (audio/name).is_file():raise SystemExit(f'Missing source review file: {name}')
    print(f'v1.1 source ready: {len(records)}/{total} verified recordings; {len(words)} vocabulary entries.')
    print('Import includes the matching vocabulary so lesson/audio keys remain consistent.')
    if args.dry_run:
        print('DRY RUN — no files changed. Rerun without --dry-run when ready.')
        return 0
    dest_audio=DST/'audio'
    dest_audio.mkdir(parents=True,exist_ok=True)
    copied=0;skipped=0
    for source_file,rel in checked:
        dst=dest_audio/rel
        dst.parent.mkdir(parents=True,exist_ok=True)
        if dst.is_file():
            if filehash(dst)!=filehash(source_file):
                raise SystemExit(f'Conflicting destination recording (not overwritten): {rel}')
            skipped+=1;continue
        with tempfile.NamedTemporaryFile(prefix='.incoming-',suffix='.mp3',dir=dst.parent,delete=False) as f:
            temp=Path(f.name)
        try:
            shutil.copyfile(source_file,temp)
            if filehash(temp)!=filehash(source_file):raise SystemExit('Failed transfer integrity check for '+rel)
            os.replace(temp,dst)
        finally:
            temp.unlink(missing_ok=True)
        copied+=1
    # Copy provenance files and matching vocabulary only after every MP3 is validated.
    for filename in ['audio_manifest.json','audio_review.csv','REDISTRIBUTION_RIGHTS_CONFIRMED.txt']:
        src=audio/filename
        if src.is_file():shutil.copy2(src,dest_audio/filename)
    shutil.copy2(vocab,DST/'vocabulary.json')
    print(f'Imported {copied} recordings; {skipped} already present; kept review and licensing documentation.')
    print('Checking current v1.2 curriculum and audio names (without re-decoding 758 MP3 files)...')
    subprocess.run([sys.executable,'scripts/audit_vocab.py'],cwd=ROOT,check=True)
    sys.path.insert(0,str(ROOT/'scripts'))
    from azure_audio import plan
    expected={j['file']:(j['role'],j['text']) for j in plan()}
    if paths!=set(expected):
        raise SystemExit(f'Audio-library mismatch: source {len(paths)} files vs v1.2 plan {len(expected)}. Check imported vocabulary and curriculum.')
    for row in records:
        if expected[row['file']]!=(row['role'],row['text']):
            raise SystemExit('Voice or text mismatch in source manifest: '+row['file'])
    print(f'Audio import complete: {len(expected)} correct voice/text keys, SHA-256 verified. No Azure calls made.')
    print('For a thorough (slower) MP3 format/duration audit run: python3 scripts/audit_audio.py --require-complete')
    print('NOTE: Audio still requires human listening review and redistribution-rights confirmation before publication.')
    return 0

if __name__=='__main__':sys.exit(main())
