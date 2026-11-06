#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
if (( EUID==0 )); then echo 'Run as your normal account, not sudo.' >&2;exit 1;fi
python3 scripts/azure_audio.py "$@"
