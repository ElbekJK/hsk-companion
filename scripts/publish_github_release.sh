#!/usr/bin/env bash
# Publish already SIGNED APK to GitHub Releases (never publish a debug APK).
set -Eeuo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
MODE="${1:---dry-run}"
[[ "$MODE" == '--dry-run' || "$MODE" == '--publish' ]] || { echo 'Usage: bash scripts/publish_github_release.sh [--dry-run|--publish]' >&2; exit 2; }
command -v gh >/dev/null || { echo 'Install gh first: sudo apt install gh' >&2; exit 1; }
gh auth status >/dev/null || { echo 'Run gh auth login first' >&2; exit 1; }
APK="${HSK_RELEASE_APK:-$HOME/Downloads/HSK_Companion_v1.2.1_release.apk}"
[[ -f "$APK" ]] || { echo "Signed APK missing: $APK. Run bash scripts/prepare_public_apk.sh first" >&2; exit 1; }
[[ "$(basename "$APK")" != *debug* && "$(basename "$APK")" != *unsigned* ]] || { echo 'Refusing to upload a debug/unsigned APK' >&2; exit 1; }
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" && -f local.properties ]]; then SDK="$(sed -n 's/^sdk.dir=//p' local.properties | head -1)"; fi
[[ -n "$SDK" ]] || SDK="$HOME/.local/share/hskcompanion/android-sdk"
TOOLS="$SDK/build-tools/36.0.0"
"$TOOLS/apksigner" verify --verbose "$APK" || { echo 'Invalid APK signature.' >&2; exit 1; }
python3 scripts/check_public_source.py
if [[ -n "$(find app/src/main/assets/audio -type f -name '*.mp3' -print -quit 2>/dev/null || true)" ]]; then
  bash scripts/check_publishable_audio.sh
fi
REPO="$(gh repo view --json nameWithOwner --jq '.nameWithOwner')"
[[ -n "$REPO" ]] || { echo 'Cannot resolve GitHub repo; run publish_github.sh first' >&2; exit 1; }
SUM="$HOME/Downloads/HSK_Companion_v1.2.1_SHA256SUMS.txt"
(cd "$(dirname "$APK")" && sha256sum "$(basename "$APK")") > "$SUM"
NOTES="$(mktemp)"
trap 'rm -f "$NOTES"' EXIT
cat RELEASE_NOTES_GITHUB.md > "$NOTES"
if [[ -n "$(find app/src/main/assets/audio -type f -name '*.mp3' -print -quit 2>/dev/null || true)" ]]; then
  printf '\n**Audio:** Complete Azure Mandarin pack included, subject to licensing and pronunciation reviews documented by the maintainer.\n' >> "$NOTES"
else
  printf '\n**Audio:** Azure pre-recorded speech NOT included in this build; Android Mandarin TTS is used as fallback.\n' >> "$NOTES"
fi
printf '\nGitHub repo: %s\nAPK: %s\nTag: v1.2.1-beta\n' "$REPO" "$APK"
if [[ "$MODE" != '--publish' ]]; then
  echo 'DRY RUN only. To publish the prerelease run: bash scripts/publish_github_release.sh --publish'
  exit 0
fi
if gh release view v1.2.1-beta --repo "$REPO" >/dev/null 2>&1; then
  echo 'Release tag v1.2.1-beta already exists; refusing overwrite.' >&2;exit 1
fi
gh release create v1.2.1-beta "$APK" "$SUM" --repo "$REPO" \
  --title 'HSK Companion v1.2.1 beta' --notes-file "$NOTES" --prerelease
printf 'Published release: '
gh release view v1.2.1-beta --repo "$REPO" --json url --jq .url
