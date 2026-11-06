#!/usr/bin/env python3
"""Configure a real public HTTPS donation link in Settings and GitHub Sponsors.

No provider account or payment processing is bundled with the app.
Example: python3 scripts/configure_donations.py https://ko-fi.com/YOUR_PAGE
"""
from pathlib import Path
from urllib.parse import urlsplit
import argparse, json, sys
ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'app/src/main/assets/donation_url.txt'
FUNDING=ROOT/'.github/FUNDING.yml'

def valid(url):
    if len(url)>500 or any(ord(x)<33 for x in url):return False
    try:
        v=urlsplit(url)
        return v.scheme=='https' and bool(v.hostname) and v.username is None and v.password is None and v.port is None and len(v.hostname)>3 and '.' in v.hostname
    except ValueError:return False

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('url',nargs='?',help='Your actual public payment/donation URL, beginning with https://')
    p.add_argument('--disable',action='store_true',help='Remove link from the app and GitHub funding file')
    args=p.parse_args()
    if args.disable:
        ASSET.write_text('',encoding='utf8')
        FUNDING.unlink(missing_ok=True)
        print('Donation links disabled. Nothing paid is required to use the app.')
        return
    if not args.url or not valid(args.url):p.error('Please supply a real HTTPS donation page, with no embedded credentials or spaces.')
    ASSET.write_text(args.url+'\n',encoding='utf8')
    FUNDING.parent.mkdir(parents=True,exist_ok=True)
    FUNDING.write_text('# Voluntary, external support link. All app functions remain free.\ncustom: '+json.dumps(args.url,ensure_ascii=False)+'\n',encoding='utf8')
    print('Optional donation link configured in Settings and .github/FUNDING.yml:',args.url)
    print('Only publish when you have verified this page belongs to you and accepts donations.')
    print('GitHub sponsor button must be enabled under repository Settings → Sponsorships.')

if __name__=='__main__':main()
