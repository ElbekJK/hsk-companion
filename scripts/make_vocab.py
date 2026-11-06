#!/usr/bin/env python3
"""Build an Android asset from NihaoLearn's redistributable TSV vocabulary."""
import csv
import json
import sys
from pathlib import Path

EXPECTED = {('2.0', 1): 150, ('2.0', 2): 147}


def compile_data(source: Path):
    words = []
    seen = set()
    for (version, level), expected in EXPECTED.items():
        path = source / f'hsk-{version}-level-{level}.tsv'
        with path.open(encoding='utf-8-sig', newline='') as stream:
            rows = list(csv.reader(stream, delimiter='\t'))
        if len(rows) != expected:
            raise ValueError(f'{path.name}: expected {expected} rows, got {len(rows)}; source format may have changed')
        for index, row in enumerate(rows, 1):
            if len(row) != 4:
                raise ValueError(f'{path.name}:{index}: expected four TSV columns (word,pinyin,part_of_speech,definition), got {len(row)}')
            hanzi, pinyin, _pos, english = [value.strip() for value in row]
            if not hanzi or not pinyin or not english:
                raise ValueError(f'{path.name}:{index}: missing mandatory field')
            key = (version, level, hanzi)
            if key in seen:
                raise ValueError(f'duplicate entry: {key}')
            seen.add(key)
            words.append({'version': version, 'level': level, 'hanzi': hanzi,
                          'pinyin': pinyin, 'english': english})
    return words


def main():
    if len(sys.argv) != 3:
        raise SystemExit('Usage: make_vocab.py <download-dir> <output-json>')
    source = Path(sys.argv[1])
    target = Path(sys.argv[2])
    rows = compile_data(source)
    target.parent.mkdir(parents=True, exist_ok=True)
    temp = target.with_suffix('.json.tmp')
    temp.write_text(json.dumps(rows, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    # Validate the replacement *before* switching Android's active vocabulary.
    # This prevents a downloaded-but-incompatible word list from breaking builds.
    try:
        from check_examples import validate
        examples = target.parent / 'examples.json'
        if examples.exists():
            validate(vocab_path=temp, examples_path=examples)
        temp.replace(target)
    finally:
        if temp.exists():
            temp.unlink()
    print(f'Created {target} with {len(rows)} source-list words (classic HSK 1 and 2; not an official 300-entry audit).')

if __name__ == '__main__':
    main()
