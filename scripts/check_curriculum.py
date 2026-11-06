#!/usr/bin/env python3
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'app/src/main/assets'
units=json.loads((root/'curriculum.json').read_text())
grammar={v['id']:v for v in json.loads((root/'grammar.json').read_text())}
dialogues={v['id']:v for v in json.loads((root/'dialogues.json').read_text())}
assert len(units)==10
assert len(set(u['id'] for u in units))==10
assert [u['level'] for u in units]==[1]*5+[2]*5
covered_g,covered_d=set(),set()
for unit in units:
    assert unit['title'].strip() and unit['goal'].strip()
    assert len(unit['grammar'])>=2 and len(unit['dialogues'])>=2
    for gid in unit['grammar']:
        assert gid in grammar and grammar[gid]['level']<=unit['level'],(unit['id'],gid)
        covered_g.add(gid)
    for did in unit['dialogues']:
        assert did in dialogues and dialogues[did]['level']<=unit['level'],(unit['id'],did)
        covered_d.add(did)
assert set(grammar)<=covered_g, ('unmapped grammar',sorted(set(grammar)-covered_g))
assert set(dialogues)<=covered_d, ('unmapped dialogues',sorted(set(dialogues)-covered_d))
print(f'Curriculum OK: {len(units)} ordered units mapping all {len(grammar)} grammar rules and {len(dialogues)} dialogues.')
