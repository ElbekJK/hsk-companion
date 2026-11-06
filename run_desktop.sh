#!/usr/bin/env bash
# Build and launch HSK Companion in an already configured Android emulator.
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"
if (( EUID == 0 )); then echo "Run this as your normal user, not with sudo." >&2; exit 1; fi
export ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/.local/share/hskcompanion/android-sdk}}"
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
if ! command -v adb >/dev/null || ! command -v emulator >/dev/null; then
  echo "Android emulator not installed under $ANDROID_HOME" >&2
  echo 'Follow the emulator setup instructions in README.md first.' >&2
  exit 1
fi
adb start-server >/dev/null
if ! adb devices | grep -qE '^emulator-[0-9]+[[:space:]]+device'; then
  if ! emulator -list-avds | grep -qx 'HSK_Test'; then
    echo 'No HSK_Test virtual phone. Create it using avdmanager (see README.md).' >&2
    exit 1
  fi
  mkdir -p "$HOME/.cache/hskcompanion"
  echo 'Starting your HSK_Test virtual Android phone...'
  nohup emulator -avd HSK_Test > "$HOME/.cache/hskcompanion/emulator.log" 2>&1 < /dev/null &
  adb wait-for-device
fi
# Keep installation pointed at the emulator if another phone is connected.
export ANDROID_SERIAL="$(adb devices | awk '/^emulator-[0-9]+[[:space:]]+device$/ {print $1; exit}')"
if [[ -z "$ANDROID_SERIAL" ]]; then echo 'No ready emulator found by adb.' >&2; exit 1; fi
# Confirm boot is complete before installing or launching.
booted=0
for _ in $(seq 1 90); do
  if [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == '1' ]]; then
    booted=1
    break
  fi
  sleep 2
done
if (( ! booted )); then
  echo 'Emulator did not finish booting. Inspect ~/.cache/hskcompanion/emulator.log.' >&2
  exit 1
fi
bash "$ROOT/run_all.sh" --install "$@"
