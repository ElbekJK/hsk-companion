#!/usr/bin/env python3
"""Guardrails for v1.0 beta: identity and backup functionality are not lost."""
from pathlib import Path
import xml.etree.ElementTree as ET
build=Path('app/build.gradle').read_text()
activity=Path('app/src/main/java/com/example/hskcompanion/MainActivity.java').read_text()
progress=Path('app/src/main/java/com/example/hskcompanion/ProgressStore.java').read_text()
backup=Path('app/src/main/java/com/example/hskcompanion/ProgressBackup.java').read_text()
manifest=ET.parse('app/src/main/AndroidManifest.xml').getroot()
ns='{http://schemas.android.com/apk/res/android}'
assert 'compileSdk 36' in build and 'targetSdk 36' in build
assert 'versionCode 18' in build and "versionName '1.2.1'" in build
assert "applicationId 'com.example.hskcompanion'" in build
assert 'hsk_progress_v1' in progress and 'SharedPreferences rawPreferences()' in progress
assert 'ACTION_CREATE_DOCUMENT' in activity and 'ACTION_OPEN_DOCUMENT' in activity
assert 'new AlertDialog.Builder(this)' in activity and 'ProgressBackup.restore' in activity
assert 'MAX_BYTES = 2 * 1024 * 1024' in backup
assert 'snapshot.put("level", 1)' in backup
assert 'if (!edit.commit())' in backup
assert 'root.setOnApplyWindowInsetsListener' in activity
assert 'ui_theme_mode' in activity and 'setPalette(useDark)' in activity
assert 'Configuration.UI_MODE_NIGHT_MASK' in activity and 'recreate();' in activity
assert 'R.style.AppThemeDark' in activity
assert Path('app/src/main/res/values/styles.xml').read_text().count('<style name=') == 2
assert 'The 2021 proficiency list' not in activity
assert manifest.find('application').get(ns+'icon')=='@mipmap/ic_launcher'
assert Path('app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml').exists()
assert Path('BRAND_DIRECTION.md').exists() and Path('V100_RELEASE_NOTES.md').exists()
print('ReleaseCandidateSelfTest PASS: SDK36, unchanged identity, backup/restore, adaptive icon, dark/light/system themes, insets, version 18')
