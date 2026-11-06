#!/usr/bin/env bash
# Upload source code from your Debian terminal, using YOUR authenticated GitHub CLI.
# Default: dry-run. Explicit --publish required for any remote changes.
set -Eeuo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
MODE="${1:---dry-run}"
[[ "$MODE" == '--dry-run' || "$MODE" == '--publish' ]] || { echo 'Usage: bash scripts/publish_github.sh [--dry-run|--publish]' >&2; exit 2; }
command -v gh >/dev/null || { echo 'Install gh first: sudo apt install gh' >&2; exit 1; }
command -v git >/dev/null || { echo 'Install git first: sudo apt install git' >&2; exit 1; }
gh auth status >/dev/null || { echo 'Run gh auth login first' >&2; exit 1; }
python3 scripts/check_public_source.py
[[ -d .git ]] || git init -b main
# Stage respecting .gitignore, then independently inspect staged paths.
git add -A
python3 scripts/check_public_source.py
printf '\nAbout to publish this SOURCE tree. Staged summary:\n'
git diff --cached --stat
printf '\nDonation URL is public, if configured. Generated Azure audio is NOT committed.\n'
printf 'Review: git diff --cached ; then inspect notices/privacy and licenses.\n'
if [[ "$MODE" != '--publish' ]]; then
  echo 'DRY RUN only. To create/push a public GitHub repo run: bash scripts/publish_github.sh --publish'
  exit 0
fi
if ! git diff --cached --quiet; then
  git commit -m 'HSK Companion v1.2.1: optional donations and public beta distribution'
fi
if ! git remote get-url origin >/dev/null 2>&1; then
  echo 'Creating a NEW PUBLIC repository under your authenticated GitHub account.'
  gh repo create hsk-companion --public --source=. --remote=origin --push
else
  echo "Using existing origin: $(git remote get-url origin)"
  git push -u origin "$(git branch --show-current)"
fi
echo 'GitHub source upload completed. Confirm the repository is public and Sponsorships are enabled.'
