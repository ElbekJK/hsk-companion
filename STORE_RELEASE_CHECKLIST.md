# Google Play release checklist — HSK Companion v1.0.0

**Status: development beta, not ready to upload to Play Console.**

1. **Accuracy and exam validity.** Manually review every word, pinyin, English gloss and grammar example with qualified Mandarin speakers. Decide whether to support classic HSK 2.0, the revised HSK 3.0 syllabus (officially launches worldwide December 13, 2026), or both. Do not label word-level practice as an official mock examination.
2. **Permissions and data.** Audit Android privacy behavior and Android TTS provider behavior. Host and finalize a privacy policy. Complete the Google Play Data safety form based on actual release behavior and any externally sourced speech engine data handling.
3. **Production identity.** Choose the public app title, publisher name, a permanent unique package ID, privacy-policy URL and support email. The current ID is `com.example.hskcompanion` and **is not appropriate for production branding**. Changing it creates a separate app from debug installs unless an explicit migration mechanism is built.
4. **API requirements.** The current prototype now targets and compiles against SDK **36**, but must still pass Android 16 behavior/edge-to-edge tests for new standard mobile app submissions as of August 31, 2026. Test updated layouts/insets on Android 16 before release, especially navigation bars and input fields. Do not simply raise the number without testing.
5. **Signing and delivery.** Make a secure production upload key and build a **signed AAB**, not the debug APK. Keep private signing keys out of the ZIP/repository. Configure Play App Signing.
6. **QA.** Test upgrade in place (same debug key) on OnePlus 13, test fresh install, rotate mid-assessment, background during a timed test, disabled Mandarin voice, audio language selection, all grammar answers, offline mode, and data retention.
7. **Store materials.** Prepare app icon, screenshots, short/long description, content rating, target audience, copyright/licenses and lawful use of the word list/gloss data. Avoid any claim of affiliation with HSK authorities.
8. **Beta and release.** Set up internal testing first. Certain new personal Play Console accounts must satisfy a 12-testers/14-day closed-testing requirement before production access; verify your account's current requirements.

Official references: https://support.google.com/googleplay/android-developer/answer/11926878 and https://www.chinesetest.cn/HSK .
