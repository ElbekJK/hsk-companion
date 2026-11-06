#!/usr/bin/env python3
"""v1.2 non-regression checks: branding, saved progress, and reuse of generated audio."""
from pathlib import Path
import re, xml.etree.ElementTree as ET
r=Path(__file__).resolve().parents[1]
b=(r/'app/build.gradle').read_text()
a=(r/'app/src/main/java/com/example/hskcompanion/MainActivity.java').read_text()
assert "versionCode 18" in b and "versionName '1.2.1'" in b
assert "applicationId 'com.example.hskcompanion'" in b
assert 'hsk_progress_v1' in (r/'app/src/main/java/com/example/hskcompanion/ProgressStore.java').read_text()
assert 'OfflineAudioPlayer' in a and 'offlineAudio.playDialogue(d.a,d.b' in a
assert 'ACCENT_INK' in a and 'accentButton(' in a and 'HERO_BG' in a
assert 'Set<String>' not in b
icon=(r/'app/src/main/res/drawable/app_icon_foreground.xml').read_text()
assert '#F4A261' in icon and '#2F5DA8' in icon and 'strokeWidth' in icon
assert 'EAF3ED' not in a and '#1F5B4C' not in a
assert (r/'scripts/import_audio_from_v110.py').is_file()
assert (r/'V120_RELEASE_NOTES.md').is_file()
for file in (r/'app/src/main/res/mipmap-anydpi/ic_launcher.xml',r/'app/src/main/res/drawable/app_icon_foreground.xml',r/'app/src/main/res/drawable/app_icon_monochrome.xml'):
    ET.parse(file)
print('BrandReleaseSelfTest PASS: new native icon, blue/orange palette, unchanged progress and audio mappings')
