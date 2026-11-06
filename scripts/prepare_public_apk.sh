#!/usr/bin/env bash
# Build a SIGNED, reproducible-source release APK for direct GitHub distribution.
# Keystore never leaves ~/.config/hskcompanion. All lessons remain free.
set -Eeuo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
ROOT="$PWD"
[[ ${EUID} -ne 0 ]] || { echo 'Do not build as root' >&2; exit 1; }
KEYSTORE="${HSK_RELEASE_KEYSTORE:-$HOME/.config/hskcompanion/release-key.jks}"
ALIAS="${HSK_RELEASE_ALIAS:-hskcompanion}"
DEST="${HSK_RELEASE_APK:-$HOME/Downloads/HSK_Companion_v1.2.1_release.apk}"
if [[ ! -f "$KEYSTORE" ]]; then
  echo "No signing key at $KEYSTORE" >&2
  echo 'Create one once (and back it up securely) using:' >&2
  echo '  mkdir -p ~/.config/hskcompanion && chmod 700 ~/.config/hskcompanion' >&2
  echo '  keytool -genkeypair -v -keystore ~/.config/hskcompanion/release-key.jks -alias hskcompanion -keyalg RSA -keysize 3072 -validity 10000' >&2
  exit 1
fi
python3 scripts/check_public_source.py
bash scripts/test_project.sh
# Never distribute unreviewed or unlicensed Azure recordings inside a public APK.
if [[ -n "$(find app/src/main/assets/audio -type f -name '*.mp3' -print -quit 2>/dev/null || true)" ]]; then
  echo 'Bundled Azure recordings detected: enforcing complete library, human review, and redistribution confirmation.'
  bash scripts/check_publishable_audio.sh
else
  echo 'No Azure MP3s bundled; Android TTS remains available. Release notes must disclose this.'
fi
./gradlew :app:assembleRelease --no-daemon
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" && -f local.properties ]]; then
  SDK="$(sed -n 's/^sdk.dir=//p' local.properties | head -1)"
fi
[[ -n "$SDK" ]] || SDK="$HOME/.local/share/hskcompanion/android-sdk"
TOOLS="$SDK/build-tools/36.0.0"
[[ -x "$TOOLS/zipalign" && -x "$TOOLS/apksigner" ]] || { echo "Missing SDK build tools at $TOOLS" >&2; exit 1; }
UNSIGNED="app/build/outputs/apk/release/app-release-unsigned.apk"
[[ -f "$UNSIGNED" ]] || { echo "Release APK not found: $UNSIGNED" >&2; exit 1; }
mkdir -p "$(dirname "$DEST")"
ALIGNED="$(mktemp --suffix=.apk)"
trap 'rm -f "$ALIGNED"' EXIT
"$TOOLS/zipalign" -f -p 4 "$UNSIGNED" "$ALIGNED"
"$TOOLS/apksigner" sign --ks "$KEYSTORE" --ks-key-alias "$ALIAS" --out "$DEST" "$ALIGNED"
"$TOOLS/apksigner" verify --verbose --print-certs "$DEST"
sha256sum "$DEST"
echo "Signed release APK ready: $DEST"
echo 'Keep this keystore permanently; a different signing certificate cannot update an installed release.'
