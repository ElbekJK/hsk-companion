#!/usr/bin/env bash
# Small, verified Gradle bootstrap for a terminal-only Debian setup.
# The official Gradle distribution is downloaded once into the local user cache.
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSION='8.13'
SHA='20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78'
CACHE="${XDG_CACHE_HOME:-$HOME/.cache}/hskcompanion"
BIN="$CACHE/gradle-$VERSION/bin/gradle"
if [[ ! -x "$BIN" ]]; then
  command -v curl >/dev/null || { echo 'Install curl first' >&2; exit 1; }
  command -v unzip >/dev/null || { echo 'Install unzip first' >&2; exit 1; }
  mkdir -p "$CACHE"
  ZIP="$CACHE/gradle-$VERSION-bin.zip"
  if [[ ! -f "$ZIP" ]] || ! printf '%s  %s\n' "$SHA" "$ZIP" | sha256sum -c --status; then
    echo 'Downloading Gradle 8.13 (verified SHA-256)...'
    curl --fail --location --retry 3 --connect-timeout 20 \
      'https://services.gradle.org/distributions/gradle-8.13-bin.zip' --output "$ZIP.tmp"
    mv "$ZIP.tmp" "$ZIP"
  fi
  printf '%s  %s\n' "$SHA" "$ZIP" | sha256sum -c
  unzip -q -o "$ZIP" -d "$CACHE"
fi
cd "$ROOT"
exec "$BIN" "$@"
