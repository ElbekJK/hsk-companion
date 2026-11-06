#!/usr/bin/env python3
"""Regression tests for curated 293 and downloaded 297 different-word lists."""
import json
import tempfile
from pathlib import Path
from check_examples import validate, ROOT

examples = json.loads((ROOT / 'examples.json').read_text(encoding='utf-8'))
targets = sorted({ex['target'] for ex in examples})
assert len(targets) == 293, 'curated bank must contain 293 distinct targets'

# Actual character targets absent in the NihaoLearn download, as observed in
# a real Debian run. This fixture reproduces the overlap (281) and gap (16).
observed_orphaned = set('为什么 宾馆 对 往 男 等 说 踢足球 过 铅笔 面条 饭店'.split())
assert observed_orphaned <= set(targets)

def word(hanzi):
    return {'version': '2.0', 'level': 1, 'hanzi': hanzi,
            'pinyin': 'cè shì', 'english': 'test fixture'}

starter = [word(t) for t in targets]
with tempfile.TemporaryDirectory() as directory:
    path = Path(directory) / 'vocabulary.json'
    def write(words):
        path.write_text(json.dumps(words, ensure_ascii=False), encoding='utf-8')

    write(starter)
    report = validate(vocab_path=path, verbose=False)
    assert not report['missing'] and not report['orphaned']

    extra4 = [word(f'测试新词{i}') for i in range(4)]
    write(starter + extra4)
    report = validate(vocab_path=path, verbose=False)
    assert report['missing'] == {w['hanzi'] for w in extra4}
    assert not report['orphaned']

    # The real download replaces twelve authored targets with sixteen others.
    added16 = [word(f'测试词语{i}') for i in range(16)]
    imported = [w for w in starter if w['hanzi'] not in observed_orphaned] + added16
    assert len(imported) == 297
    write(imported)
    report = validate(vocab_path=path, verbose=False)
    assert report['orphaned'] == observed_orphaned
    assert report['missing'] == {w['hanzi'] for w in added16}
    assert len(report['matched']) == 281

    # A silently substituted list with excessive churn must fail.
    replaced30 = [word(f'测试替换{i}') for i in range(30)]
    bad = [w for w in starter if w['hanzi'] not in set(targets[:26])] + replaced30
    assert len(bad) == 297
    write(bad)
    try:
        validate(vocab_path=path, verbose=False)
    except ValueError as e:
        assert 'differs too much' in str(e)
    else:
        raise AssertionError('unexpectedly accepted a substantially changed vocabulary source')

    # A broken Chinese sentence must still halt the build.
    bad_examples = list(examples)
    bad_examples[0] = dict(examples[0], chinese='这句不含目标')
    ex_path = Path(directory) / 'examples.json'
    ex_path.write_text(json.dumps(bad_examples, ensure_ascii=False), encoding='utf-8')
    write(starter)
    try:
        validate(vocab_path=path, examples_path=ex_path, verbose=False)
    except ValueError as e:
        assert 'does not contain its target' in str(e)
    else:
        raise AssertionError('accepted a malformed example')

print('ExampleCoverageSelfTest PASS: starter 293, 297 with four new, real 12/16 drift, and corruption guards')
