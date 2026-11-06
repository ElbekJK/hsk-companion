# HSK Companion v0.7.1 — Android build unblocker

This is a small corrective release based on v0.7.0. **No learning data is reset.**

## Fixed

- v0.7.0 stopped during `run_desktop.sh` after successfully importing the 297-word complete classic HSK dataset. `check_examples.py` mistakenly demanded all 297 imported words have curated examples, although the bank contains examples for the original 293 starter words.
- The validator now verifies every curated example target is in the vocabulary, requires coverage of at least 293 words, and prints the four imported words without examples as a **visible content notice**. It still stops if more than four terms lack examples, instead of hiding larger data changes.
- Added `scripts/test_example_coverage.py` with starter, four-extra, and five-extra fixtures to prevent the same regression.
- Increased `versionCode` from 10 to 11 and `versionName` to 0.7.1. Updated Debian APK output filename.

## Known limitation

The four entries added by the complete downloaded vocabulary do not yet have authored sentence examples. Word-level learning still works for these entries. The exact missing terms are printed during each build, so they can be editorially addressed after their dictionary entries have been confirmed.

## Update and build

Run `bash run_desktop.sh` on Debian for emulator deployment, or `bash build_phone_apk.sh` for a transferable APK. Install **over** your existing app without uninstalling it, to retain on-device learning progress. The Android package remains `com.example.hskcompanion` and the same preference store is used. Update signature must match the previously installed version.
