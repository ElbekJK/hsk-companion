#!/usr/bin/env python3
import json
import sys
from collections import Counter
from pathlib import Path

file = Path(__file__).resolve().parents[1] / 'app/src/main/assets/vocabulary.json'
items = json.loads(file.read_text(encoding='utf-8'))
assert isinstance(items, list) and items, 'vocabulary must be a nonempty JSON array'
keys = []
for i, item in enumerate(items):
    assert set(item) == {'version', 'level', 'hanzi', 'pinyin', 'english'}, f'bad fields at {i}'
    assert item['version'] in {'2.0'} and item['level'] in {1, 2}
    assert all(isinstance(item[k], str) and item[k].strip() for k in ('hanzi', 'pinyin', 'english'))
    keys.append((item['version'], item['level'], item['hanzi']))
assert len(set(keys)) == len(keys), 'duplicate vocabulary IDs'
counts = Counter((x['version'], x['level']) for x in items)
print('Validated', len(items), 'words:', dict(sorted(counts.items())))
if '--full' in sys.argv:
    assert counts == {('2.0', 1): 150, ('2.0', 2): 147}, 'incomplete full packs'
