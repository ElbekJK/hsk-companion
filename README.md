# HSK Companion v1.2.1 — Optional Donations & GitHub Beta

## Download the Android app — free beta

**[⬇ Download HSK Companion v1.2.1 APK](https://github.com/ElbekJK/hsk-companion/releases/download/v1.2.1-beta/HSK_Companion_v1.2.1_release.apk)**

[View the release notes and SHA-256 checksum](https://github.com/ElbekJK/hsk-companion/releases/tag/v1.2.1-beta) · [See all releases](https://github.com/ElbekJK/hsk-companion/releases)

On your Android phone, download the APK and approve **Install unknown apps** for your browser or file manager when prompted. This beta uses Android's installed Mandarin TTS; the Azure-generated offline voice library is not included yet. **The app is free, and donations are optional.**

The **full app remains free**. Voluntary support can be offered through an external HTTPS donation page, configured by the maintainer. No account, checkout, SDK, or monetized learning feature is added. The donation button in **Settings** is absent until a real URL is configured.

To publish source and a signed beta APK through GitHub Releases, follow **[GITHUB_PUBLISHING_GUIDE.md](GITHUB_PUBLISHING_GUIDE.md)** or use the provided dry-run and publish scripts. This source ZIP includes neither your Azure speech clips nor any API keys or signing credentials.

**Your ongoing v1.1 Azure generation is unaffected.** After it completes, run `python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion` from this directory. Keep audio private until the pronunciation and redistribution checks are completed.

---

> **Azure audio is not bundled in the source ZIP:** use `AZURE_AUDIO_GUIDE.md` to generate the licensed offline female/male MP3s on Debian before building the APK. No Azure account or key is needed by end users after packaging. Existing Android TTS fallback remains.

# HSK Companion v1.2.1 — Focused Learning redesign + Offline Azure Voices

**Native, offline-first Android app in Java for classic HSK 1–2.** No videos, login, cloud backend, ads or paid APIs. Android's installed Mandarin TTS engine provides word and conversation audio; available voices vary by phone.


## v1.2 branding and audio migration (READ BEFORE BUILDING)

This version uses a new **blue + orange** light/dark brand and original speech/tone logo. See `BRAND_DIRECTION.md` and `V120_RELEASE_NOTES.md`. The current Android app name and package ID are unchanged.

**If your v1.1 Azure batch is running, do not interrupt it.** Let it complete all 758 files in `~/hsk-companion-v110/HSKCompanion`. The v1.2 source ZIP intentionally does not contain those private generated recordings. When the v1.1 job is finished, run this **from the new v1.2 project directory**:

```bash
python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion --dry-run
python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion
python3 scripts/audit_audio.py --require-complete
bash scripts/test_project.sh
bash build_phone_apk.sh --offline
```

The import reads the generated audio manifest, verifies every MP3's SHA-256, copies the matching v1.1 vocabulary, preserves the audio review CSV and licensing record, and checks full coverage. It makes **no new Azure requests**, and it does not copy credentials. The bundled lesson content and audio filename hashing are the same. Copying audio does not certify pronunciation or redistribution rights.

For initial UI testing before the audio is complete, run `bash run_desktop.sh --offline` in v1.2; any missing audio still uses Android TTS. The built APK has only the recordings present at build time.

## The v1.0 milestone

This milestone refines branding, adds a user-selectable System / Light / Dark theme and accessibility labels, adds explicit local JSON backup/restore, and targets Android 16 (API 36). It retains the v0.9 five-part navigation and every learning module. See `BRAND_DIRECTION.md` and `V100_RELEASE_NOTES.md`.

## Core learning screens

Five focused destinations replace a crowded homepage:

- **Today:** one primary guided mission, optional reviews due, compact progress pulse.
- **Learn:** the ten structured units, 24 original grammar lessons, 28 two-speaker dialogues and a vocabulary library.
- **Practice:** flashcards, listening/reading MCQs, tone lab and exam-length vocabulary practice.
- **Progress:** learning history, skill-specific recall and unofficial assessment results.
- **Settings:** change between classic HSK 1 / 2, choose dialogue speaker voices, configure TTS, difficulty and gentle reminders.

The app retains the classic HSK 1–2 learning track. It does not claim official alignment with revised HSK examinations.

## Debian: emulator workflow

Download `HSKCompanion_Android_Debian_v1.2.1.zip`, then:

```bash
mkdir -p ~/hsk-companion-v121
unzip -q ~/Downloads/HSKCompanion_Android_Debian_v1.2.1.zip -d ~/hsk-companion-v121
cd ~/hsk-companion-v121/HSKCompanion
bash scripts/test_project.sh
bash run_desktop.sh
```

If the `HSK_Test` emulator is already installed, `run_desktop.sh` builds, updates and launches the app. Use your regular user, **not sudo**. Java 17+, Python 3, curl, unzip and zip are prerequisites; the scripts install or reuse Android SDK and Gradle in user-owned directories.

For the OnePlus 13 APK (no emulator required):

```bash
bash build_phone_apk.sh
```

The debug APK will be copied to `~/Downloads/HSK_Companion_v1.2.1.apk`.

## Upgrade safety

The Android package ID stays `com.example.hskcompanion`, and the existing `hsk_progress_v1` preferences storage remains unchanged. The app's `versionCode` is 18. **Install over your previous copy with the same Android debug signing key** to retain progress. Back up `~/.android/debug.keystore`. If Android reports a signature mismatch, do not uninstall the old app solely to make installation succeed: that would erase its locally stored learning progress.

## Local backup and restore

Use **Settings → Your learning data → Export progress backup** to write a JSON file with Android's document picker. **Restore from backup** validates the file and asks for confirmation before replacing the current local history. Backup files are unencrypted; store them privately. For first-time users, the export includes the default selected level even before any preferences were changed. Neither action needs Android storage permissions.

## Learning content and release limitations

The offline starter pack contains 293 curated words, with 413 original example sentences, 24 grammar lessons, 28 short dialogues, and 10 learning units. The online refresh attempts to fetch a 297-entry list (150 HSK 1 and 147 HSK 2); this list differs from the conventional 300-word cumulative target and does not exactly overlap with the curated examples. Tests report this issue. Native-speaker editorial review of content, typography/size testing on a physical device and a full Android SDK build remain required before a Play Store release.

Listening practice uses Mandarin TTS and audio-first multiple-choice questions; it is **not** a valid HSK proficiency score. The mock exam uses classic length and counts but simpler vocabulary question types than official exams.

## Project structure

`app/src/main/java` contains the Android activity, lesson engine, progress preferences and TTS. `app/src/main/assets` contains content JSON. `scripts` contains import, integrity checks and build prerequisites. `tests` contains pure Java self-tests and Android API compilation stubs. `UX_DESIGN_NOTES.md` documents navigation decisions. `NOTICE_DATA.md` contains vocabulary source and license attribution.
