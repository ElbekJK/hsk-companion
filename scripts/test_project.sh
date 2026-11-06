#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
python3 scripts/check_data.py
python3 scripts/audit_vocab.py
python3 scripts/check_examples.py
python3 scripts/test_example_coverage.py
python3 scripts/test_vocab_import.py
python3 scripts/check_grammar.py
python3 scripts/check_dialogues.py
python3 scripts/check_curriculum.py
python3 scripts/check_navigation.py
python3 scripts/check_release_v100.py
python3 scripts/check_release_v110.py
python3 scripts/check_release_v120.py
python3 scripts/check_release_v121.py
python3 scripts/check_public_source.py
python3 scripts/audit_audio.py --inspect
python3 scripts/test_theme.py
python3 scripts/check_no_video.py
# Upgrade-safety invariants: keep the same package and local progress store.
python3 - <<'PY_UPGRADE'
from pathlib import Path
build=Path('app/build.gradle').read_text()
store=Path('app/src/main/java/com/example/hskcompanion/ProgressStore.java').read_text()
assert "applicationId 'com.example.hskcompanion'" in build
assert "versionCode 18" in build
assert 'hsk_progress_v1' in store
print('Same-package data-preservation checks passed.')
PY_UPGRADE
# Android 11+ requires a manifest visibility declaration to discover TTS engines.
python3 - <<'PY_TTS_MANIFEST'
import xml.etree.ElementTree as ET
root = ET.parse('app/src/main/AndroidManifest.xml').getroot()
ns = '{http://schemas.android.com/apk/res/android}'
intents = root.findall('queries/intent/action')
assert any(a.get(ns + 'name') == 'android.intent.action.TTS_SERVICE' for a in intents), 'Missing Android TTS_SERVICE manifest declaration'
print('TTS manifest visibility check passed.')
PY_TTS_MANIFEST
# Catch the exact invalid Android-only API call that broke the v0.2 APK build.
if grep -R -n -F '.setTextAllCaps(' app/src/main/java; then
  echo 'Android Button/TextView has setAllCaps(), not setTextAllCaps().' >&2
  exit 1
fi
if command -v javac >/dev/null && command -v java >/dev/null; then
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  javac -encoding UTF-8 -d "$TMP" \
      app/src/main/java/com/example/hskcompanion/{Word,QuizEngine,Exercise,SessionPlanner,ReviewScheduler,ReviewPolicy,ReviewFocus,PinyinCheck,ToneTrainer,SentenceExample,ExamBlueprint,GrammarRule}.java \
      tests/QuizEngineSelfTest.java tests/PedagogySelfTest.java tests/ExamBlueprintSelfTest.java tests/ReviewPolicySelfTest.java tests/ToneTrainerSelfTest.java
  java -ea -cp "$TMP" QuizEngineSelfTest
  java -ea -cp "$TMP" PedagogySelfTest
  java -ea -cp "$TMP" ReviewPolicySelfTest
  java -ea -cp "$TMP" ToneTrainerSelfTest
  java -ea -cp "$TMP" ExamBlueprintSelfTest
  # Compile all Android sources against minimal test-only API stubs. This catches
  # Java syntax/type errors but does not replace a real Android SDK build.
  javac -encoding UTF-8 -d "$TMP/android" \
    $(find tests/android-stubs -name '*.java') \
    $(find app/src/main/java -name '*.java') \
    tests/DialogueVoiceSelfTest.java tests/OfflineAudioKeySelfTest.java tests/DonationLinksSelfTest.java
  java -ea -cp "$TMP/android" DialogueVoiceSelfTest
  java -ea -cp "$TMP/android" OfflineAudioKeySelfTest
  java -ea -cp "$TMP/android" DonationLinksSelfTest
  echo 'Android source compile against test API stubs passed.' 
else
  echo 'Install JDK 17 or later to run Java unit tests.' >&2; exit 1
fi
python3 - <<'PY'
from pathlib import Path
import xml.etree.ElementTree as ET
for xml in Path('app/src/main').rglob('*.xml'):
    ET.parse(xml)
    print('XML OK:', xml)
PY
for sh in run_all.sh run_desktop.sh build_phone_apk.sh gradlew scripts/*.sh; do bash -n "$sh"; done
echo 'All offline source/data/unit/shell checks passed.'
