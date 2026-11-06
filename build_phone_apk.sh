#!/usr/bin/env bash
# Build an APK for transfer to a physical phone, without starting an emulator.
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"
if (( EUID == 0 )); then echo "Run without sudo." >&2; exit 1; fi
bash run_all.sh "$@"
SOURCE="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
DEST="$HOME/Downloads/HSK_Companion_v1.2.1.apk"
mkdir -p "$HOME/Downloads"
cp "$SOURCE" "$DEST"
printf '\nReady to transfer to your phone: %s\n' "$DEST"
