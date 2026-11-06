#!/usr/bin/env bash
# A gate, not a license grant. Must pass before distributing the Azure assets publicly.
set -Eeuo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
python3 scripts/audit_audio.py --release
