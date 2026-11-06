#!/usr/bin/env python3
"""Test atomic TSV import and 293/297 authored-example list reconciliation."""
import csv
import json
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EXAMPLES = ROOT / 'app/src/main/assets/examples.json'
ORPHANED = set('为什么 宾馆 对 往 男 等 说 踢足球 过 铅笔 面条 饭店'.split())

examples = json.loads(EXAMPLES.read_text(encoding='utf-8'))
targets = sorted({item['target'] for item in examples})
assert len(targets) == 293 and ORPHANED <= set(targets)
fixture_hanzi = [t for t in targets if t not in ORPHANED] + [f'新样本{i}' for i in range(16)]
assert len(fixture_hanzi) == 297

with tempfile.TemporaryDirectory() as temp:
    root = Path(temp)
    asset_dir = root / 'assets'
    asset_dir.mkdir()
    asset_examples = asset_dir / 'examples.json'
    asset_examples.write_bytes(EXAMPLES.read_bytes())
    dest = asset_dir / 'vocabulary.json'
    original = b'["working vocabulary asset"]\n'
    dest.write_bytes(original)
    cache = root / 'cache'
    cache.mkdir()

    def write_tsv(words):
        for level, segment in ((1, words[:150]), (2, words[150:])):
            with (cache / f'hsk-2.0-level-{level}.tsv').open('w', encoding='utf-8', newline='') as out:
                writer = csv.writer(out, delimiter='\t')
                for hanzi in segment:
                    writer.writerow([hanzi, 'cè shì', 'n', 'fixture definition'])

    def run():
        return subprocess.run(
            [sys.executable, str(ROOT / 'scripts/make_vocab.py'), str(cache), str(dest)],
            text=True, capture_output=True, check=False)

    write_tsv(fixture_hanzi)
    proc = run()
    assert proc.returncode == 0, proc.stderr
    imported = json.loads(dest.read_text(encoding='utf-8'))
    assert [item['hanzi'] for item in imported] == fixture_hanzi
    last_good = dest.read_bytes()

    # Same 297 rows but 30 additional mismatches: a bad import cannot replace
    # already working vocabulary, or strand a stale temporary asset.
    broken = [f'坏样本{i}' for i in range(30)] + fixture_hanzi[30:]
    assert len(broken) == 297
    write_tsv(broken)
    proc = run()
    assert proc.returncode != 0
    assert 'differs too much' in proc.stderr
    assert dest.read_bytes() == last_good
    assert not dest.with_suffix('.json.tmp').exists()

print('VocabularyImportSelfTest PASS: bounded 297-word import and atomic failure protection')
