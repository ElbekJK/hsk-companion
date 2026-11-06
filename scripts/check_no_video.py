#!/usr/bin/env python3
"""Guard against accidental reintroduction of external or embedded video."""
from pathlib import Path
root = Path(__file__).resolve().parent.parent
java = root / 'app/src/main/java'
assets = root / 'app/src/main/assets'
for p in java.rglob('*.java'):
    text = p.read_text(encoding='utf-8')
    for marker in ('VideoView', 'VideoScene', 'VideoSceneRepository', 'youtube.com', 'youtu.be'):
        assert marker not in text, f'Unexpected video integration: {marker} in {p}'
for p in assets.rglob('*'):
    assert p.suffix.lower() not in ('.mp4','.m4v','.webm','.mkv') and p.name not in ('scenes.json','videos.json'), p
print('No-video regression check passed.')
