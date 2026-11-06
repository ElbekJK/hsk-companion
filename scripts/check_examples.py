#!/usr/bin/env python3
"""Validate authored Chinese examples and reconcile them with either word pack.

HSK Companion ships a curated 293-entry starter list and, on Debian, can fetch
NihaoLearn's 297-entry classic HSK 1–2 list. These lists are NOT supersets:
12 curated targets are absent from the 297-entry source, and 16 imported words
have no curated examples. That is a bounded content difference, not corruption.

We do NOT delete or relabel authored examples to make a word list appear complete.
When a target is absent, the Android example repository simply cannot find it
for an active vocabulary word. The mismatches are printed for editorial review.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/assets'
STARTER_SIZE = 293
IMPORTED_SIZE = 297
MIN_MATCHED_IMPORTED = 275
MAX_ORPHANED_IMPORTED = 20
MAX_UNEXAMPLED_IMPORTED = 20


def validate(vocab_path=ROOT / 'vocabulary.json', examples_path=ROOT / 'examples.json', *, verbose=True):
    words = json.loads(Path(vocab_path).read_text(encoding='utf-8'))
    examples = json.loads(Path(examples_path).read_text(encoding='utf-8'))
    if not isinstance(words, list) or len(words) not in (STARTER_SIZE, IMPORTED_SIZE):
        raise ValueError('unexpected vocabulary size; expected curated 293 or imported 297')
    vocab = {w['hanzi'] for w in words}
    if len(vocab) != len(words):
        raise ValueError('duplicated Chinese vocabulary entries')
    if not isinstance(examples, list) or len(examples) < 400:
        raise ValueError('sentence examples are missing or incomplete')
    seen = set()
    targets = set()
    for index, item in enumerate(examples):
        if not isinstance(item, dict) or set(item) != {'target', 'chinese', 'pinyin', 'english'}:
            raise ValueError(f'incorrect fields at example {index}')
        if not all(isinstance(v, str) and v.strip() for v in item.values()):
            raise ValueError(f'blank field at example {index}')
        if item['target'] not in item['chinese']:
            raise ValueError(f'example {index} does not contain its target')
        key = (item['target'], item['chinese'])
        if key in seen:
            raise ValueError(f'duplicate sentence at example {index}')
        seen.add(key)
        targets.add(item['target'])
    if len(targets) != STARTER_SIZE:
        raise ValueError(f'expected {STARTER_SIZE} distinct authored example targets, got {len(targets)}')

    orphaned = targets - vocab  # authored sentences not active in imported vocabulary
    missing = vocab - targets    # active words needing authored examples
    matched = vocab & targets
    if len(words) == STARTER_SIZE:
        if orphaned or missing:
            raise ValueError('curated starter pack must match all authored example targets')
    else:
        if (len(matched) < MIN_MATCHED_IMPORTED or
                len(orphaned) > MAX_ORPHANED_IMPORTED or
                len(missing) > MAX_UNEXAMPLED_IMPORTED):
            raise ValueError(
                'imported vocabulary differs too much from authored examples: '
                f'{len(matched)} matched, {len(orphaned)} orphaned, {len(missing)} missing. '
                'Keep the starter pack and review the imported source.'
            )
    if verbose:
        print(f'Validated {len(examples)} original sentence examples: '
              f'{len(matched)}/{len(vocab)} active words covered; '
              f'{len(orphaned)} archived-only example targets, '
              f'{len(missing)} active words without examples.')
        if orphaned:
            print('CONTENT NOTICE: These curated example targets are not in the imported list '
                  '(examples retained, not used for active word exercises): '
                  + ', '.join(sorted(orphaned)))
        if missing:
            print('CONTENT NOTICE: These imported words still need curated examples '
                  '(word-level exercises remain available): ' + ', '.join(sorted(missing)))
    return {'missing': missing, 'orphaned': orphaned, 'matched': matched}


if __name__ == '__main__':
    validate()
