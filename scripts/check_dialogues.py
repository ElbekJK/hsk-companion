#!/usr/bin/env python3
"""Validate original, attributed-in-app fictional pedagogical micro-dialogues."""
import json
from pathlib import Path
entries=json.loads(Path('app/src/main/assets/dialogues.json').read_text(encoding='utf-8'))
assert len(entries)>=28
ids=set()
for d in entries:
    assert d['id'] not in ids
    ids.add(d['id'])
    assert d['level'] in (1,2)
    assert len(d['choices'])==4 and len(set(d['choices']))==4
    assert isinstance(d['answer'], int) and 0<=d['answer']<4
    for key in ('title','a','b','pa','pb','ea','eb','question','feedback'):
        assert isinstance(d[key],str) and d[key].strip(), (d['id'],key)
    assert any('\u4e00'<=x<='\u9fff' for x in d['a']+d['b'])
print(f'Validated {len(entries)} original dialogues with answer keys and level tags.')
