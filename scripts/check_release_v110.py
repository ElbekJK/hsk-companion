#!/usr/bin/env python3
"""Source invariants: bundled Azure audio, no credentials in app, preserved data."""
from pathlib import Path
import re,json,subprocess,sys
root=Path(__file__).resolve().parents[1]
build=(root/'app/build.gradle').read_text()
activity=(root/'app/src/main/java/com/example/hskcompanion/MainActivity.java').read_text()
player=(root/'app/src/main/java/com/example/hskcompanion/OfflineAudioPlayer.java').read_text()
assert 'versionCode 18' in build and "versionName '1.2.1'" in build
assert "applicationId 'com.example.hskcompanion'" in build
assert "noCompress.add('mp3')" in build
assert 'new OfflineAudioPlayer(this)' in activity
assert 'offlineAudio.playDialogue(d.a,d.b' in activity
assert 'offlineAudio.play("a",chinese)' in activity
assert 'progress = new ProgressStore(this)' in activity
assert 'hasBundledAudio()' in activity and 'Offline Azure voices' in activity
assert '▶ Preview female A' in activity and '▶ Preview male B' in activity
assert 'tts.speak(chinese' in activity # fallback still present
assert 'audio/' in player and 'MessageDigest.getInstance("SHA-256")' in player
assert not re.search(r'AZURE_SPEECH_(?:KEY|REGION)|api\.cognitive\.microsoft',activity+'\n'+player)
assert 'hsk_progress_v1' in (root/'app/src/main/java/com/example/hskcompanion/ProgressStore.java').read_text()
assert (root/'scripts/azure_audio.py').exists() and (root/'scripts/audit_audio.py').exists()
print('OfflineAudioReleaseSelfTest PASS: new version, unchanged data, local player with Android TTS fallback, voice previews, no Azure credentials in app')
