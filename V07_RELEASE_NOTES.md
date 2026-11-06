# HSK Companion v0.7.0 — learning corrections

- Classic HSK vocabulary only. 2021 proficiency-standard switching removed. Official newer exam data must be verified/licensed before being added.
- New corpus: 346 original short sentence examples; rotation stable within local day. Not all are restricted to HSK 1–2 vocabulary; examples are supplementary, not exam-syllabus-verified.
- Listening checkpoints are audio-first multiple-choice questions. No new self-rated listening questions.
- Dedicated 2-way HSK word tone listening lab; device TTS pronunciation quality depends on installed voice.
- Optional tone-sensitive pinyin grading; supports accented or numbered tones. No automatic spoken pronunciation scoring.
- Independent reading/listening/production stages stored alongside (not in place of) existing legacy word stages. Old progress retained; old general mastery is imported only as reading mastery.
- Optional Android inexact daily review reminders (off by default); permission requested only after opt-in. The phone may delay notifications under Doze or manufacturer restrictions. Uses no cloud service.
- Existing app ID `com.example.hskcompanion`, existing prefs `hsk_progress_v1`, higher versionCode 10. To keep saved progress, update with the SAME signing certificate; uninstalling clears data.
- No video or browser integration.
- Debug APK build must be completed on Debian using `bash build_phone_apk.sh`.
