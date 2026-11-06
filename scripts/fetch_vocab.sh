#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CACHE="$ROOT/.cache/vocab"
DEST="$ROOT/app/src/main/assets/vocabulary.json"
mkdir -p "$CACHE"
command -v curl >/dev/null || { echo 'curl is required' >&2; exit 1; }
command -v python3 >/dev/null || { echo 'python3 is required' >&2; exit 1; }

# --require fails if the full lists cannot be downloaded. Without it,
# preserve the bundled sample dataset when offline.
REQUIRE=0
[[ "${1:-}" == '--require' ]] && REQUIRE=1
URL_ROOT='https://nihaolearn.com/downloads'
status=0
for standard in 2.0; do
  for level in 1 2; do
    name="hsk-${standard}-level-${level}.tsv"
    if [[ ! -s "$CACHE/$name" ]]; then
      echo "Fetching ${name}..."
      if ! curl --fail --location --silent --show-error --retry 2 --connect-timeout 12 --max-time 90 \
          "$URL_ROOT/$name" --output "$CACHE/$name.tmp"; then
        echo "Could not retrieve $name" >&2
        status=1
        rm -f "$CACHE/$name.tmp"
        break 2
      fi
      mv "$CACHE/$name.tmp" "$CACHE/$name"
    fi
  done
done
if (( status != 0 )); then
  if (( REQUIRE )); then exit 1; fi
  echo 'Keeping the bundled sample vocabulary. Re-run scripts/fetch_vocab.sh when online.' >&2
  exit 0
fi
if ! python3 "$ROOT/scripts/make_vocab.py" "$CACHE" "$DEST"; then
  echo "Vocabulary import failed; leaving the existing offline dataset unchanged." >&2
  if (( REQUIRE )); then exit 1; fi
fi
