#!/usr/bin/env python3
"""Static regression check for v0.9 information architecture and compatibility."""
from pathlib import Path
import re
src = Path('app/src/main/java/com/example/hskcompanion/MainActivity.java').read_text()
build = Path('app/build.gradle').read_text()
store = Path('app/src/main/java/com/example/hskcompanion/ProgressStore.java').read_text()
assert 'versionCode 18' in build and "versionName '1.2.1'" in build
assert "applicationId 'com.example.hskcompanion'" in build
assert 'hsk_progress_v1' in store
assert '2021 proficiency list' not in src and '2021 proficiency-list' not in src
assert 'page.equals("learn_hub")' in src and 'page.equals("practice_hub")' in src
assert 'String[] keys = {"home", "learn_hub", "practice_hub", "stats", "settings"};' in src
assert 'private String sectionForPage(' in src
assert 'case "path_unit": page = "path";' in src
assert 'case "dialogue_detail": page = "dialogues";' in src
home = src.split('    private void home() {',1)[1].split('    private void learnHub() {',1)[0]
assert 'beginLesson()' in home and 'beginReviewOnly()' in home
assert 'optionTile(' not in home, 'Home contains duplicated navigation tiles'
assert 'The 2021 proficiency list' not in home
learn = src.split('    private void learnHub() {',1)[1].split('    private void practiceHub() {',1)[0]
practice = src.split('    private void practiceHub() {',1)[1].split('    private void statNumber(',1)[0]
assert all(item in learn for item in ('"path"','"words"','"grammar"','"dialogues"'))
assert all(item in practice for item in ('"cards"','"quiz"','"tones"','"mock"'))
assert 'Button hsk1=button("HSK 1",level==1,true)' in src
assert 'Suggested next focus:' in src
print('NavigationSelfTest PASS: distinct five tabs, sensible nested back, clean Today, exam focus, preserved data')
