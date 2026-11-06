#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"
if (( EUID == 0 )); then
  cat >&2 <<'MSG'
Do not run this Android build script with sudo.
Running as root creates SDK and build files owned by root, which then block
normal user builds. From the extracted project directory, run:
  bash run_all.sh
If an earlier sudo invocation made the old project root-owned, either extract
this release into a NEW folder, or repair that folder's ownership first.
MSG
  exit 1
fi
if [[ ! -w "$ROOT" || ! -w "$ROOT/app/src/main/assets" || ( -e "$ROOT/local.properties" && ! -w "$ROOT/local.properties" ) ]]; then
  cat >&2 <<MSG
This project contains files or directories that your account cannot write.
If this is an old checkout previously built with sudo, repair it using:
  sudo chown -R "$(id -un):$(id -gn)" "$ROOT"
Or extract this ZIP into a new directory in your home folder.
MSG
  exit 1
fi
INSTALL=0
FETCH=1
for arg in "$@"; do
  case "$arg" in
    --install) INSTALL=1 ;;
    --offline) FETCH=0 ;;
    *) echo "Unknown option: $arg. Try --install or --offline." >&2; exit 2 ;;
  esac
done
if (( FETCH )); then
  echo '1) Refreshing the HSK 1–2 word lists if internet access is available...'
  bash scripts/fetch_vocab.sh
else
  echo '1) Using bundled vocabulary (offline mode).'
fi
bash scripts/test_project.sh
printf '\n2) Setting up Android SDK...\n'
bash scripts/debian_setup.sh
printf '\n3) Building a debug APK...\n'
./gradlew :app:assembleDebug --no-daemon
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$APK" ]] || { echo 'APK not found after build' >&2; exit 1; }
printf '\nBuild successful! APK: %s\n' "$APK"
if (( INSTALL )); then
  if ! command -v adb >/dev/null; then
    export PATH="${ANDROID_HOME:-$HOME/Android/Sdk}/platform-tools:$PATH"
  fi
  adb devices
  adb install -r "$APK"
  adb shell am start -n com.example.hskcompanion/.MainActivity
else
  echo 'To install on a phone with USB debugging enabled: bash run_all.sh --install'
fi
