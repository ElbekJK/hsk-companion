#!/usr/bin/env python3
"""Color, legibility and theme-mode regression checks for HSK Companion."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET
text = Path('app/src/main/java/com/example/hskcompanion/MainActivity.java').read_text()
colors = {}
for name, dark, light in re.findall(r'\b([A-Z_]+)\s*=\s*Color\.parseColor\(dark\s*\?\s*"(#[0-9A-Fa-f]{6})"\s*:\s*"(#[0-9A-Fa-f]{6})"\s*\);',text):
    colors[name] = {'dark':dark, 'light':light}
expected = {'BG','INK','MUTED','PRIMARY','PALE','SUCCESS_BG','WHITE','BORDER','RED','GREEN','ERROR_BG','WARNING_BG','INFO_BG','HERO_NOTE','HERO_BG','HERO_INK','ACCENT','ACCENT_INK'}
assert expected.issubset(colors), f'Missing semantic color tokens: {expected-set(colors)}'
def lum(hex_color):
    vals=[int(hex_color[i:i+2],16)/255 for i in (1,3,5)]
    rs=[v/12.92 if v<=0.04045 else ((v+0.055)/1.055)**2.4 for v in vals]
    return 0.2126*rs[0]+0.7152*rs[1]+0.0722*rs[2]
def contrast(a,b):
    a,b=sorted((lum(a),lum(b)))
    return (b+.05)/(a+.05)
for mode in ('light','dark'):
    for a,b in [('INK','BG'),('INK','WHITE'),('MUTED','WHITE'),('MUTED','PALE'),
                ('PRIMARY','WHITE'),('WHITE','PRIMARY'),('INK','SUCCESS_BG'),
                ('RED','ERROR_BG'),('GREEN','SUCCESS_BG'),('HERO_NOTE','HERO_BG'),('HERO_INK','HERO_BG'),('ACCENT_INK','ACCENT')]:
        ratio=contrast(colors[a][mode],colors[b][mode])
        assert ratio >= 4.5, f'{mode} {a}/{b} fails 4.5:1 (got {ratio:.2f})'
    print(f'{mode.capitalize()} palette: contrast checks PASS')
root = ET.parse('app/src/main/res/values/styles.xml').getroot()
styles={item.attrib['name'] for item in root.findall('style')}
assert {'AppTheme','AppThemeDark'} <= styles
assert 'ui_theme_mode' in text and 'Configuration.UI_MODE_NIGHT_MASK' in text
assert 'for (String value : new String[]{"system", "light", "dark"})' in text
assert 'recreate();' in text and 'setTheme(R.style.AppThemeDark)' in text
print('ThemeSelfTest PASS: System/Light/Dark, native styles, semantic palettes')

assert 'LinearLayout hero = card(HERO_BG)' in text
assert 'Button study=accentButton(' in text
assert 'HERO_INK' in text
print('BrandColorSelfTest PASS: blue reading palette, restrained orange mission CTA')
