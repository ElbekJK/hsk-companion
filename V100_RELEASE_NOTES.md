# HSK Companion v1.0.0 — Release candidate for device testing

## Shipped
- Refined warm paper / evergreen / sage palette, restrained bottom-navigation selection and tab accessibility labels. **System / Light / Dark** appearance in Settings with fully separate foreground, background, feedback and surface colors, plus native dark text-input/dialog styles.
- Original app icon design with legacy and adaptive icon resources, including a monochrome themed layer.
- Export and restore learning history through Android's document picker. Backups contain the existing `hsk_progress_v1` data with typed fields and a versioned JSON envelope. Import is validated before any change and explicitly confirmed by the user. No storage permission is requested. Exported backups are **unencrypted**; users should protect the JSON file.
- Updated compile/target SDK 36 and added top/bottom system-bar safe-area handling on Android 15+.
- Retained the stable five-page navigation, all lessons, HSK 1–2 content, independent skill review histories, optional notifications and two-speaker TTS.
- Retained existing app ID and local preference name and increased Android version code to 15 for in-place updates when signing key matches.

## Not yet verified or release-ready
- A full Android Gradle build and physical OnePlus 13 Android 16 test are not performed in this environment. Compile stubs and XML validation are not a substitute.
- Native-speaker editorial review, word-list reconciliation, Play Store policy declarations, privacy-policy URL, permanent package ID, production signing, and public branding remain open.
- This version is a **beta/release candidate**, not a Play Store submission and not a validated official HSK score predictor.

## Regression steps
1. Build and update over v0.9 using same debug keystore. Confirm known review counts and voice preferences remain.
2. Settings -> Your learning data -> Export backup. Save JSON in local Documents.
3. Make a harmless change (e.g. daily mission length), then restore JSON; confirm it returns to original. Also try an invalid JSON file and cancel restore. Keep a separate copy before testing destructive replacement.
4. Inspect status bar, bottom navigation and document pickers on the OnePlus 13 (including dark mode / large font / gesture navigation).
5. Switch between System, Light and Dark in Settings. Verify Home, tabs, Practice MCQs, grammar feedback, dialogs and the document picker are legible with both light/dark phone settings.
6. Confirm dialogues still switch available Mandarin voices and exam audio MCQs remain gated until audio.
