#!/usr/bin/env python3
"""Regression checks for donation and safe/public GitHub publishing workflows."""
from pathlib import Path
from urllib.parse import urlsplit
import importlib.util,re
ROOT=Path(__file__).resolve().parents[1]
activity=(ROOT/'app/src/main/java/com/example/hskcompanion/MainActivity.java').read_text()
build=(ROOT/'app/build.gradle').read_text()
assert "versionCode 18" in build and "versionName '1.2.1'" in build
assert "applicationId 'com.example.hskcompanion'" in build
assert 'DonationLinks.configuredUrl(this)' in activity
assert 'if (!donationLink.isEmpty())' in activity
assert 'new Intent(Intent.ACTION_VIEW, Uri.parse(donationLink))' in activity
assert '❤ Optional donation (opens browser)' in activity
assert 'hsk_progress_v1' in (ROOT/'app/src/main/java/com/example/hskcompanion/ProgressStore.java').read_text()
assert (ROOT/'scripts/prepare_public_apk.sh').is_file()
assert (ROOT/'scripts/publish_github.sh').is_file()
assert (ROOT/'scripts/publish_github_release.sh').is_file()
assert (ROOT/'PRIVACY_POLICY.md').is_file()
assert (ROOT/'.github/workflows/source-checks.yml').is_file()
ignore=(ROOT/'.gitignore').read_text()
for key in ('*.jks','app/src/main/assets/audio/','*.apk','local.properties'):
    assert key in ignore
spec=importlib.util.spec_from_file_location('donations',ROOT/'scripts/configure_donations.py')
module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
for url in ('https://ko-fi.com/example','https://github.com/sponsors/example'):
    assert module.valid(url)
for bad in ('http://ko-fi.com/example','https://evil.com/path with spaces','https://name:pwd@example.com/path','https://bad.example:8080/a','abc'):
    assert not module.valid(bad)
assert 'gh repo create hsk-companion --public' in (ROOT/'scripts/publish_github.sh').read_text()
assert '--publish' in (ROOT/'scripts/publish_github_release.sh').read_text()
print('PublicDistributionSelfTest PASS: optional HTTPS support, GitHub dry-run guards, privacy/secret exclusions')
