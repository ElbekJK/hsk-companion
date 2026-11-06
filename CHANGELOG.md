## v1.2.1 — optional donations and GitHub beta

- Maintains the blue/orange UI, dark mode, phone progress, and offline Mandarin audio keys.
- Adds optional external funding link under Settings; disabled until configured by the maintainer.
- Adds GitHub funding configuration, source preflight, privacy policy, release signing, and GitHub publication scripts.
- Keeps Azure audio and signing material out of the public source repository.
- Increments versionCode to 18 for future Android upgrades.

## v1.2.0 — Blue and warm orange visual refresh

- Brand redesigned for light and dark mode, with legible high-contrast blue + orange colors.
- Original speech-bubble/tone-contour vector and adaptive Android launcher icon.
- Added safe offline import of finished v1.1 Azure audio, preserving review records and matching vocabulary without regenerating clips.
- Unchanged package, progress data and Azure audio keys.

# Changelog

## v0.9.0
- Refocus Today, Learn, Practice, Progress and Settings into five distinct sections with nested navigation.
- Remove the deprecated-list explanation from user-facing Home and Settings.
- Improve button tap targets, reading hierarchy, learning-unit continuation and exam section feedback.
- Preserve package ID, original progress preferences, two-speaker TTS, curriculum content, and existing Debian workflows.


## v0.8.0
- Add two-speaker Mandarin TTS with independent voice A/B selection, preview, safe sequential playback, and answer-unlock gating after the second line.
- Add ten structured learning units, 18 original dialogues, eight grammar lessons, and 67 more context sentences.
- Preserve notification options, classic vocabulary mode, app ID, and user progress.

## v0.7.2 — Imported vocabulary mismatch / build fix (2026-11-05)
- Verified that the online 297-entry list is not a superset of the 293-entry curated starter: 12 authored targets are absent and 16 online terms lack examples.
- Reconciles both datasets without dropping good examples; logs uncovered terms while failing on invalid sentences or excessive source drift.
- Validates downloaded lists before replacing the active asset, preserving an existing usable vocabulary after a failed import.
- Tests for the exact 12/16 mismatch, normal 293/297 variants, bad imports and malformed examples.
- Same package ID/progress files; versionCode 12, versionName 0.7.2.

## 0.7.1 — Build validation fix
- Fixed example coverage check after 297-word downloaded HSK vocabulary import.
- Added visible missing-example report and regression test.
- Kept unchanged app ID, stored progress, and learning features.

## v0.7.0 — Context, tones, and gentle reminders (2026-11-05)

- 346 curated/handwritten sentence examples and multi-example daily rotation; original examples preserved.
- Removed 2021 proficiency-list option, classic 2.0 track remains with future official syllabus import deferred.
- Independent reading/listening/production skill stages; legacy mastery maps only to reading; existing preferences and Android package unchanged.
- Mandarin tone-pair MCQ lab based on words with different tones; listening missions now lock choices until audio plays, and no longer use shadow self-ratings as listening assessment.
- Optional strict tone-number/diacritic pinyin grading; starter mode remains forgiving.
- Optional inexact local review reminders with user-selected daytime hours, no notification when no review is due.
- versionCode 10, versionName 0.7.0; no video, no accounts, no ads.

# HSK Companion change log

## v0.6.0 — Learning-first, minimal UI (2026-11-05)

- Review-only session for due vocabulary; backlogged sessions automatically reduce new-word load.
- Prevent same-day repetitions from moving a word several mastery stages; old SharedPreferences data is preserved.
- Mistake-aware selection prioritizes words that have been hard to remember.
- Ten original two-person mini-dialogues with context-specific comprehension and optional Android TTS/reading.
- Home curriculum selector for classic HSK 2.0 and the 2021 proficiency vocabulary set; explicitly distinguishes it from the 2025 HSK 3.0 exam specification.
- Simplified UI colors and practice labels; updated progress figures for dialogue practice.
- Java pedagogical tests, dialogue data validation, no-video and TTS regressions.
- No accounts, ads, videos, external players, or new Android permissions. Same Android ID, signing approach, progress store. versionCode 9.


## v0.5.0 — Grammar, analytics, unofficial exam-length practice (2026-11-05)

- Kept v0.4.2's no-video, offline, native Java and OnePlus-friendly Android TTS behavior.
- Added 16 original beginner grammar lessons with example sentences, pinyin, original explanations, audio replay, and retrieval questions after the explanation is hidden. Grammar completion stays in the existing progress store.
- Added timed **classic HSK-format-count** vocabulary practice: 20+20 questions at HSK 1, 35+25 at HSK 2. This is an UNOFFICIAL, simplified word-level assessment, not a full HSK replica or score forecast.
- Results include listening/reading breakdown and optionally revealed missed vocabulary. This practice does not modify spaced repetition scheduling.
- Added per-skill objective practice accuracy and day-by-day seven-day progress; no external collection or accounts.
- Added test-only Android framework stubs for source Java compilation and automated grammar/assessment/data regression checks.
- Added Play Store release checklist and editable privacy-policy draft; the debug APK intentionally remains **not** Play-ready (target API 34; Play requires API 36 for new standard Android submissions).
- Raised Android versionCode to 8; maintained existing package ID and SharedPreferences keys for same-signing-key in-place upgrades.

# HSK Companion change log

## v0.4.2 — Android Mandarin TTS fix (2026-11-05)

- Added Android 11+ `<queries>` declaration for `android.intent.action.TTS_SERVICE`, as Android documentation requires for apps using text-to-speech.
- Added TTS initialization and Mandarin-language diagnostics in the Settings screen.
- Added a **Retry voice detection** button after installing or switching voice engines.
- Refreshes voice-status information when asynchronous TTS initialization completes.
- Added `build_phone_apk.sh`: builds and exports a version-named APK to `~/Downloads/`.
- Expanded source tests to enforce the TTS manifest declaration.
- Remains the no-video app. Uses Android system speech voices; no new permissions, cloud service, or bundled audio data.
- Raised versionCode to 7 (same app ID). A same-key APK update preserves saved progress.
- The fix is code-reviewed and source-tested, but needs a physical-device retest; actual Mandarin voice availability depends on the phone's installed engine and language data.

## v0.4.1 — Classic/no-video restoration (2026-11-05)

- Restored all app screens and learning engine from the proven v0.2.1 source release.
- Removed all external clip links, embedded player screens, video exams, scripts/assets and any video downloads.
- Retained vocabulary, guided missions, adaptive spaced repetition, flashcards, listening via Android system TTS, pinyin typing, reading quizzes, and progress history.
- Raised Android `versionCode` from 5 (v0.4) to 6 to support installing over v0.4 without a downgrade error.
- Added `run_desktop.sh` to build/install/launch on the existing `HSK_Test` emulator on Debian.
- No data reset or uninstall should be necessary for a same-machine debug-signed update.

# Changelog

## v0.2.1 (2026-11-05)
- Fix Android compile error: `Button.setTextAllCaps(false)` replaced with supported `Button.setAllCaps(false)` (API 14+).
- Fix Debian SDK setup failing on a root-owned/unwritable `~/Android` directory: use `~/.local/share/hskcompanion/android-sdk` by default.
- Refuse root/sudo execution and explain how to recover previously root-owned checkout files.
- Add targeted static regression check for the invalid Android method call.
- Retain the same package ID and learning/progress data model; bump versionCode to 3.
- Android APK compilation remains to be verified on a machine with Android SDK access.

## v0.2.0 (2026-11-05)
- New adaptive, bounded daily mission engine.
- Native contextual sentences with pronunciation and cloze questions.
- Typed pinyin recall, self-rated retrieval, listening and pronunciation shadowing.
- Prioritize due words correctly: previously unseen words no longer count as overdue.
- Migration of v0.1 saved words and stage/due schedules.
- Optional quick/deep study plan; real learning milestones and past-seven-day activity count.
- Missed-answer retesting inside lessons; no punitive streak mechanics.
- Honest labels for 2021 vocabulary versus the 2025 HSK exam syllabus.
- Verified Java logic, JSON data, and project shell scripts. APK build is run on Debian.
