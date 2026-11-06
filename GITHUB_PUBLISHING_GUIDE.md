# HSK Companion — Free GitHub Distribution & Optional Donations

This repository is **not published automatically**. The scripts run on your local Debian computer through your own GitHub CLI authorization; never place Azure keys, personal passwords or keystores in the repository.

## A. Configure optional support (only after you have a real page)

Choose a provider, create your account, and confirm you are eligible to receive payments. Options include GitHub Sponsors (requires approved sponsor account), Ko-fi or Buy Me a Coffee. The app does NOT collect payments itself.

```bash
cd ~/hsk-companion-v121/HSKCompanion
python3 scripts/configure_donations.py 'https://ko-fi.com/YOUR_ACTUAL_PAGE'
# or: python3 scripts/configure_donations.py 'https://github.com/sponsors/YOUR_GITHUB_HANDLE'
# remove link: python3 scripts/configure_donations.py --disable
```

Replace the sample with your REAL account URL. The script writes the public URL to `app/src/main/assets/donation_url.txt` and `.github/FUNDING.yml`. Android Settings shows the link only if configured. On GitHub, enable **Repository Settings → Features → Sponsorships** for the Sponsor button.

No provider username or payment URL has been fabricated; this distribution has the donation button disabled until configured.

## B. Your existing Azure synthesis job

Keep v1.1 running while v1.2.1 is extracted in a separate directory. When the full batch finishes:

```bash
python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion --dry-run
python3 scripts/import_audio_from_v110.py ~/hsk-companion-v110/HSKCompanion
python3 scripts/audit_audio.py --require-complete
```

This copies files without creating more Azure requests. **Do not distribute Azure MP3s in a public APK until you have verified redistribution rights and reviewed the Mandarin recordings.** `bash scripts/check_publishable_audio.sh` enforces the local gate; it is not a substitute for legal advice. You may release an APK without the Azure pack and rely on Android TTS in the meantime.

## C. Test, sign, and inspect the APK

```bash
bash scripts/test_project.sh
mkdir -p ~/.config/hskcompanion && chmod 700 ~/.config/hskcompanion
keytool -genkeypair -v -keystore ~/.config/hskcompanion/release-key.jks \
  -alias hskcompanion -keyalg RSA -keysize 3072 -validity 10000
# Run keytool ONLY ONCE. Keep an encrypted backup of this key and its password.
bash scripts/prepare_public_apk.sh
```

The signing script builds `~/Downloads/HSK_Companion_v1.2.1_release.apk` and verifies its signature. **Never publish `app-debug.apk`.** Do not commit `*.jks`, `.env`, `local.properties` or the Azure audio folder. Existing debug-signed builds can't be updated by release-signed APKs without uninstalling; export/import your progress first. Later public versions MUST use the same signing certificate.

## D. Create a public repository from Debian

```bash
sudo apt update && sudo apt install -y git gh
cd ~/hsk-companion-v121/HSKCompanion
gh auth login
bash scripts/publish_github.sh --dry-run
# inspect the changes: git diff --cached
bash scripts/publish_github.sh --publish
```

The last command will create `hsk-companion` on the GitHub account authenticated by `gh`, or push to your existing origin if configured. **The --publish flag sends source files to a public repository.** Check `NOTICE_DATA.md`, `PRIVACY_POLICY.md`, source licensing and funding page before executing.

## E. Publish a prerelease containing your signed APK

```bash
bash scripts/publish_github_release.sh --dry-run
bash scripts/publish_github_release.sh --publish
```

It creates a *v1.2.1-beta* release with the signed APK, a SHA256 checksum file and a visible note about whether Azure recordings were included. This does not automatically enable downloads or auto-updates inside the app; users manually install APK updates from GitHub Releases.

## F. Recommended review before public release

- Verify the logo, dark and light mode, Settings support link, and spoken Mandarin on a real Android phone.
- Check all content attribution/license obligations; this repository's MIT license does not override third-party licenses.
- Verify audio rights and pronunciation or publish a build without the Azure audio pack.
- Confirm no credentials, local keystores, logs, or private JSON backups are in staged files.
- Confirm a stable release-signing key has been securely backed up, and the package name is suitable long-term. Current ID is `com.example.hskcompanion`.
- Consider future package-name selection BEFORE broad distribution because changing it breaks seamless upgrades.
- If you later distribute the app on Google Play, review its *current* payments and external-link policies separately.

Sources: https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/displaying-a-sponsor-button-in-your-repository ; https://cli.github.com/manual/gh_release_create ; https://developer.android.com/tools/apksigner
