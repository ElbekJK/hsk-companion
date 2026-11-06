# v0.9.0 — Clear navigation and exam feedback

This is an incremental update to v0.8.0, not a rebuild or a new app identity.

## What changed

- Five top-level destinations: **Today**, **Learn**, **Practice**, **Progress**, **Settings**.
- Today has a single primary mission action, optional due-review action, and a compact learning pulse. It no longer duplicates every feature as a menu.
- Learn organizes the 10-unit curriculum, grammar, vocabulary reference, and two-speaker dialogues. Its Continue button opens the next unfinished unit.
- Practice is the exclusive launch area for flashcards, reading/listening MCQs, tone discrimination, and exam-length practice.
- Progress has its own space for skill-specific retention and assessment history.
- Settings now contains the HSK 1/2 level selector, TTS voice controls, study length, and review reminders. The top header has only an HSK level shortcut.
- The previous paragraph mentioning the withdrawn proficiency-list option has been **removed from the Android Home and Settings UI**.
- Exam-length result screen now suggests listening or reading MCQs based on which section had the lower accuracy. This is NOT a validated HSK score prediction.
- More consistent native padding and 48dp minimum button targets, with active-state tabs and a compact header.
- Nested Back behavior returns to the relevant tab or parent list, rather than always jumping to Home.

## Preservation / limitations

- `applicationId` stays `com.example.hskcompanion`.
- `ProgressStore` still uses the existing `hsk_progress_v1` Android SharedPreferences file and does not clear it.
- `versionCode` is now 14, `versionName` is `0.9.0`.
- Install as an update with the **same signing certificate**; do not uninstall if you wish to keep saved progress.
- Existing 413 contextual sentences, 24 original grammar lessons, 28 original dialogues, 10 curriculum units, offline TTS and reminders remain.
- No videos, account login, analytics SDK, payment, or advertising was added.
- The classic list imported from the internet still needs the separate vocabulary/coverage reconciliation described in NOTICE_DATA.md before publication.
- Native Mandarin content remains pending human editorial validation.

## Tests

Run `bash scripts/test_project.sh` before `bash run_desktop.sh`. The project checks navigation invariants, previous vocabulary importer mismatch regressions, no-video policy, content data, API-stub Java compilation and unit tests. **A full Gradle APK build and device-specific visual testing still need to be performed on Debian/OnePlus.**
