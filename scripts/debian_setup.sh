#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
for tool in java curl unzip sha256sum python3; do
  if ! command -v "$tool" >/dev/null; then
    echo "Missing $tool. On Debian run: sudo apt update && sudo apt install -y default-jdk curl unzip python3 ca-certificates" >&2
    exit 1
  fi
done
if [[ "$(uname -m)" != 'x86_64' ]]; then
  echo "This automatic SDK installer supports Debian x86_64. Found $(uname -m). Please configure a compatible Android SDK manually." >&2
  exit 1
fi
JAVA_MAJOR="$(java -version 2>&1 | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' | head -1)"
if [[ -z "$JAVA_MAJOR" || "$JAVA_MAJOR" -lt 17 || "$JAVA_MAJOR" -gt 24 ]]; then
  echo "Need JDK 17–24 for this pinned Gradle release. Found: $(java -version 2>&1 | head -1)" >&2
  exit 1
fi
# Keep SDK files in a user-owned location rather than assuming ~/Android is writable.
# Either ANDROID_HOME or ANDROID_SDK_ROOT may point to an existing Android SDK.
export ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/.local/share/hskcompanion/android-sdk}}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
if ! mkdir -p "$ANDROID_HOME/cmdline-tools"; then
  echo "Could not create Android SDK folder: $ANDROID_HOME" >&2
  echo 'Choose a writable directory, for example:' >&2
  echo '  unset ANDROID_HOME ANDROID_SDK_ROOT; bash run_all.sh' >&2
  exit 1
fi
if [[ ! -w "$ANDROID_HOME/cmdline-tools" ]]; then
  echo "Android SDK directory is not writable by user $USER: $ANDROID_HOME" >&2
  echo 'Run without sudo and select a user-owned SDK path.' >&2
  exit 1
fi
SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
if [[ ! -x "$SDKMANAGER" ]]; then
  ZIPNAME='commandlinetools-linux-15859902_latest.zip'
  SHA='4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583'
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  echo 'Downloading Android SDK command-line tools (~182 MB)...'
  curl --fail --location --retry 3 --connect-timeout 20 \
    "https://dl.google.com/android/repository/$ZIPNAME" --output "$TMP/$ZIPNAME"
  printf '%s  %s\n' "$SHA" "$TMP/$ZIPNAME" | sha256sum -c
  unzip -q "$TMP/$ZIPNAME" -d "$TMP/extracted"
  mv "$TMP/extracted/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
fi
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
echo "Installing Android SDK into $ANDROID_HOME ..."
# SDK licences may make yes exit with SIGPIPE; handle without hiding actual SDK errors.
set +o pipefail
yes | "$SDKMANAGER" --sdk_root="$ANDROID_HOME" --licenses >/dev/null
STATUS=${PIPESTATUS[1]}
set -o pipefail
(( STATUS == 0 )) || { echo 'Android SDK licenses could not be accepted' >&2; exit 1; }
"$SDKMANAGER" --sdk_root="$ANDROID_HOME" 'platforms;android-36' 'build-tools;36.0.0' 'platform-tools'
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > "$ROOT/local.properties"
echo 'Android SDK ready.'
