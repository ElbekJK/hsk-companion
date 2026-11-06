#!/usr/bin/env python3
from collections import Counter
from pathlib import Path
import json

path = Path(__file__).resolve().parents[1] / 'app/src/main/assets/grammar.json'
items = json.loads(path.read_text(encoding='utf-8'))
assert len(items) >= 24, f'Expected at least 24 original grammar exercises, got {len(items)}'
assert Counter(i['level'] for i in items) == {1: 12, 2: 12}
assert len(set(i['id'] for i in items)) == len(items)
for item in items:
    for field in ('title','pattern','explain','hanzi','pinyin','translation','question','feedback'):
        assert isinstance(item.get(field),str) and item[field].strip(), (item['id'],field)
    assert len(item['choices']) == 4, item['id']
    assert len(set(item['choices'])) == 4, item['id']
    assert isinstance(item['answer'],int) and 0 <= item['answer'] < 4, item['id']
print('Grammar content OK:',len(items),'original lessons with unique choices.')
