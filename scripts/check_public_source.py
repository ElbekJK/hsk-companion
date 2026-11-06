#!/usr/bin/env python3
"""Fail closed on common private files, unsafe funding URL, and accidental audio commits.

Run after git add in scripts/publish_github.sh; can also run before git init.
This is a helper, NOT a guarantee that the repository contains no secrets.
"""
import re,subprocess,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

PROHIBITED=[re.compile(x,re.I) for x in (
    r'(^|/)(?:\.env(?:\..+)?|local\.properties|key\.properties|id_rsa|id_ed25519)$',
    r'\.(?:jks|keystore|pem|p12|pfx|key|apk|aab|pyc)$',
    r'(^|/)(?:audio|__pycache__|\.gradle|build)/',
)]

def tracked():
    p=subprocess.run(['git','ls-files','--cached','-z'],cwd=ROOT,capture_output=True)
    if p.returncode==0 and p.stdout:
        return [ROOT/x.decode('utf8') for x in p.stdout.split(b'\0') if x]
    return [p for p in ROOT.rglob('*') if p.is_file() and '.git' not in p.parts and not any(re.search(x,p.as_posix()) for x in (r'/build/',r'/__pycache__/',r'/audio/'))]

problems=[]
for path in tracked():
    rel=path.relative_to(ROOT).as_posix()
    if any(p.search(rel) for p in PROHIBITED):problems.append('private/generated file staged: '+rel)
    if rel.startswith('app/src/main/assets/audio/'):
        problems.append('generated audio must be excluded from source: '+rel)
    if not path.is_file():continue
    if path.stat().st_size>5_000_000:problems.append('large source file: '+rel)
    if path.suffix.lower() in ('.py','.sh','.java','.gradle','.yml','.yaml','.md','.txt','.xml','.properties','.json'):
        s=path.read_text(encoding='utf8',errors='replace')
        # Known key headers and environment variable NAMES are harmless; literal
        # high-entropy assignments embedded in source are not.
        for m in re.finditer(r'(?i)(?:api[_-]?key|client[_-]?secret|access[_-]?token|password)\s*[:=]\s*["\']?([a-zA-Z0-9/+_=.-]{32,})',s):
            if not m.group(1).startswith(('http','https','YOUR_','EXAMPLE_')):
                problems.append('possible embedded secret in '+rel)
                break
from urllib.parse import urlsplit
fund=ROOT/'app/src/main/assets/donation_url.txt'
if fund.is_file():
    url=fund.read_text(encoding='utf8').strip()
    if url:
        try:
            u=urlsplit(url)
            if u.scheme!='https' or not u.hostname or u.username or u.password or u.port is not None:
                problems.append('invalid public donation URL')
        except ValueError:problems.append('invalid public donation URL')
if not (ROOT/'LICENSE').is_file() or not (ROOT/'NOTICE_DATA.md').is_file():
    problems.append('source license/attribution documents missing')
if problems:
    print('PUBLIC SOURCE PREFLIGHT FAILED:',file=sys.stderr)
    for p in problems:print(' -',p,file=sys.stderr)
    sys.exit(1)
print(f'Public-source preflight passed: {len(tracked())} examined files. Review personally before publishing.')
