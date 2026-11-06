# Release v0.8.0 — Structured Chinese Learning / Two-Speaker Voice

## Delivered

- v0.7.2 core preserved: no video, classic HSK1/2, adaptive review and per-skill mastery, tone MCQs and optional strict pinyin, locally scheduled opt-in notifications, no cloud backend.
- 28 original two-turn dialogue/listening MCQs (18 more than v0.7.2). Their transcript is hidden until reading mode is selected or the learner answers.
- Sequential dialogue playback using Android TTS callbacks: choose voice A, finish A, select voice B, finish B, then unlock the question. Playback interruption and error never unlock the audio-only prompt.
- TTS speaker A/B choice and preview in settings; persist voice names with **new keys in the existing** `hsk_progress_v1` preferences. Names/genders cannot be assumed: Android doesn't standardize a gender field for Voice. Fallback to a single voice when necessary; no pitch shifting that would interfere with Mandarin tone recognition.
- 24 original grammar lessons, +8 over v0.7.2; 413 authored example sentences, +67 over v0.7.2 (all curated 293 starter words covered). Some are context-rich, not strict exam-level phrases.
- 10 thematic learning-path units, linking every grammar rule and conversation, progress carried over from prior releases.
- v0.8 code 13. Existing app ID and progress-storage path preserved. No uninstall required provided debug signing key remains the same.

## Android and testing

- Offline source validation, 293/297 importer reconciliation, Java tests, voice-sequencing fake-engine tests and Android-stub compilation passed.
- Full real SDK `:app:assembleDebug` and real OnePlus 13 tests **not run in the packaging environment**.
- Voice availability and acoustic gender cannot be guaranteed by the app; selection is dependent on installed device TTS engine. Network voices may need internet.
- An interrupted dialogue is not scored as completed listening; user can choose reading fallback.

## Known limitations

1. `297` downloaded vocabulary entries versus conventional classic HSK 1–2 300 cumulative target, with 16 imported entries missing dedicated examples. The curated 293-entry offline pack remains complete within its own source list but is also not a verified full official exam list.
2. Context sentences, grammar and audio require a native speaker review before public distribution; TTS output is not human speech.
3. Scores represent practice, not validated HSK readiness; no human-speaker listening recordings or automated pronunciation scoring.
4. Android voice gender isn't a supported metadata property; manually preview and select voices for distinct character sounds.
5. Google Play submission requires independent release, privacy, licensing and current target-SDK audits.
