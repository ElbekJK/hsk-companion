#!/usr/bin/env python3
"""Conservative vocabulary consistency audit; not a substitute for bilingual proofing."""
from collections import Counter, defaultdict
from pathlib import Path
import json
import sys
source=Path(__file__).resolve().parents[1]/'app/src/main/assets/vocabulary.json'
words=json.loads(source.read_text(encoding='utf-8'))
grouped=defaultdict(list)
for row in words:
    grouped[(row['version'],row['level'])].append(row)
    for field in ('hanzi','pinyin','english'):
        assert row[field].strip()==row[field], (row,field)
flags=[]
for group,items in sorted(grouped.items()):
    by_meaning=defaultdict(list)
    by_reading=defaultdict(list)
    for w in items:
        by_meaning[w['english'].casefold()].append(w['hanzi'])
        by_reading[w['pinyin'].casefold()].append(w['hanzi'])
    for meaning,hanzis in by_meaning.items():
        if len(hanzis)>1:
            flags.append(f'{group}: same English gloss {meaning!r} across {", ".join(hanzis)}')
    print(f'{group}: {len(items)} entries; {sum(len(h)>1 for h in by_meaning.values())} repeated English glosses')
print(f'Vocabulary consistency check complete; {len(flags)} items for possible human review.')
if '--report' in sys.argv:
    target=Path(__file__).resolve().parents[1]/'VOCAB_REVIEW_REPORT.txt'
    target.write_text('Automatically flagged POSSIBLE gloss collisions; not verified errors.\n'
                      'Bilingual/manual accuracy checking is still required.\n\n'
                      + '\n'.join(flags[:350])+'\n',encoding='utf-8')
    print('Wrote',target)
