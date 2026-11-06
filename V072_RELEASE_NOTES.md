# HSK Companion v0.7.2 — fix after the 297-word online refresh

## Reproduced failure

On Debian, the 297-entry NihaoLearn import succeeded, but `scripts/check_examples.py`
then stopped on 12 authored targets not present in the downloaded source:
为什么, 宾馆, 对, 往, 男, 等, 说, 踢足球, 过, 铅笔, 面条, 饭店.

This was the second incompatible validator assumption. In the imported 297-entry
set, **281** words are covered by the curated example bank, **16** imported words
have no authored examples, and **12** example targets are absent from the list.
There are still **346 original examples** in the project. No authored content is
silently removed, and Android only uses examples whose targets match active words.

## Changes

1. `check_examples.py` accepts **bounded, explicitly reported** differences between
   the 293-entry curated pack and the external 297-entry pack. It still fails for
   missing or malformed example data, excessive vocabulary drift, and duplicate
   vocabulary items. The offline 293-entry pack must match all 293 authored targets.
2. `make_vocab.py` validates a candidate dataset *before* replacing the working
   asset. If the import fails, the prior asset stays intact.
3. `test_example_coverage.py` reproduces 293/293, 293/297, the observed 12/16
   mismatch, an excessive-change rejection, and malformed sentence rejection.
4. `test_vocab_import.py` verifies the 297-entry TSV path and ensures a rejected
   import cannot overwrite working vocabulary.
5. Android `versionCode=12`, `versionName=0.7.2`; `build_phone_apk.sh` exports
   `~/Downloads/HSK_Companion_v0.7.2.apk`.

## Notes

The NihaoLearn source publishes 150+147=297 entries rather than the conventional
150+150=300 classic HSK 1–2 vocabulary count. Our remaining task is to reconcile
and audit a canonical exam vocabulary list and write/review sentences for missing
words. The current build should not be marketed as a completely verified official
HSK list. See `NOTICE_DATA.md` for original source and glossary licenses.

## Build and user progress

```bash
mkdir -p ~/hsk-companion-v072
unzip -q ~/Downloads/HSKCompanion_Android_Debian_v0.7.2.zip -d ~/hsk-companion-v072
cd ~/hsk-companion-v072/HSKCompanion
bash scripts/test_project.sh
bash run_desktop.sh
bash build_phone_apk.sh
```

Never use `sudo` for these build commands. Install over the previous app, without
uninstalling it. Same `com.example.hskcompanion` package ID and the same progress
store (`hsk_progress_v1`); the APK signing key must also be identical for Android
to accept an update. No videos or new permission changes in this release.

Verification here: Python data and import regression, Java logic tests, Android
source compile against local API stubs, XML tests and ZIP validation. Full Android
Gradle assembly and real-device checks must run in the user's Debian environment.
