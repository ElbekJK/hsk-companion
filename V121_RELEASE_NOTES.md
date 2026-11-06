# HSK Companion v1.2.1 — GitHub Beta & Voluntary Support

- Adds an optional external donation link to **Settings**. No account, paywall, tracking, or payment processing SDK; hidden until a real HTTPS support page is configured.
- Adds `scripts/configure_donations.py` to configure the in-app link and GitHub `.github/FUNDING.yml` consistently.
- Adds GitHub repository publishing (dry run by default), GitHub release publishing, and release-APK signing scripts. Publishing only happens after an explicit `--publish` command on the maintainer's Debian machine.
- Adds public source audit, a GitHub Actions source-check workflow, privacy policy and public beta release notes.
- Keeps the v1.2 blue/orange UI, Android package ID, learning progress, and Azure MP3 file mappings unchanged. Audio is still generated in v1.1 and imported without re-synthesis.
- Increments Android versionCode 17 → 18, versionName 1.2.0 → 1.2.1.
- The ZIP does not contain a signing key, GitHub credentials, actual donation page, or Azure recordings.
- A GitHub release containing Azure audio is blocked until the pack passes the existing complete/human-reviewed/licensed gate; a fallback-TTS public beta is allowed.
