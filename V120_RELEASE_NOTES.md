# HSK Companion v1.2.0 — Blue & Warm Accent

## Visual redesign
- Replaced the green identity with the accessible blue / warm orange **Focused Learning** palette.
- Added independently tuned light/dark colors for reading surfaces and buttons. System/Light/Dark settings remain.
- Redesigned the launcher icon as an original speech bubble with blue and orange Mandarin tone contour strokes, available as native Android vector, adaptive icon, and monochrome themed icon.
- Orange is used deliberately for the main **Start mission** call to action; other actions, navigation and lessons use blue. Success remains teal and errors remain red, paired with text.
- Added regression checks for color contrast, icon XML, Android identity and audio-preserving upgrade.

## Audio continuity
- This ZIP deliberately ships WITHOUT generated audio or Azure credentials.
- **Let the Azure generator finish in your existing v1.1 folder first.** No changes to that running process are required.
- After v1.1 completes, run `python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion` from the v1.2 project.
- This copies verified MP3s, their manifest/review records and the matching downloaded vocabulary. The audio filenames and source text keys are unchanged, so there is **no need to regenerate 758 Azure clips**.
- A dry-run check is provided. All files remain private locally and will be packaged into the APK when you run the build.
- Public redistribution of the audio needs licensing confirmation and listening review; the transfer check is not itself an approval.

## Compatibility
- VersionCode 17; versionName 1.2.0; `applicationId` remains `com.example.hskcompanion`.
- Existing local progress namespace remains `hsk_progress_v1`. Do not uninstall the old app to update. Upgrades require the same debug/release signing certificate; backup is available in Settings.
- Still native Java, no videos, no cloud calls from the Android app, and Android TTS fallback remains for missing offline files.
- The full APK build and on-device visual review should be performed on Debian / OnePlus 13.
