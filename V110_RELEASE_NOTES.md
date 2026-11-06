# HSK Companion v1.1.0 — Natural offline speech pipeline

- Azure prebuilt neural TTS batch generation for **female A (Xiaoxiao)** and **male B (Yunxi)**.
- One-click preview generation: six MP3 recordings; complete generation covers words, sentences, grammar examples and two-speaker dialogue lines.
- Offline MP3 playback before Android TTS fallback; no Azure network calls on Android.
- In-app female/male voice previews, usable with the built APK after audio generation.
- Deterministic, content-addressed audio files with integrity manifests, codec/duration checks and a human-review CSV.
- Public-release quality/licensing gate; no unverified audio included in the source ZIP.
- HSK 1–2 curriculum, five-tab UX, adaptive System/Light/Dark mode, reminder settings, and local-progress store unchanged.
- Android app versionCode 16; app ID retained for updates.

**Important:** This ZIP has no Microsoft-generated audio. Without your Azure Speech key/region, the app functions as v1.0 using Android TTS. Follow `AZURE_AUDIO_GUIDE.md` to generate and bundle real Azure voice recordings on Debian.
