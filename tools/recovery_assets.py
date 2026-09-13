#!/usr/bin/env python3
"""Restore historical pixels for recovery; no art generation or artgen imports.

Only Git extraction, integer nearest-neighbour scaling, rectangular copying and
fixed palette substitution are permitted. --check compares every derived pixel
with the original Git objects, including transparent pixels and frame layout.
"""
from pathlib import Path
from io import BytesIO
from functools import lru_cache
import argparse
import hashlib
import json
import subprocess

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'core/src/main/assets'
MANIFEST = ASSETS / 'recovery-assets.json'
OLD = 'v0.3.2-fixup2'
UPSTREAM = 'v4.0.0'
APPROVED = 'v1.0.0-content-complete'
PREFIX = 'core/src/main/assets/'


@lru_cache(None)
def blob(ref, path):
    return subprocess.check_output(['git', 'show', ref + ':' + PREFIX + path], cwd=ROOT)


def files(ref, directory):
    return [p[len(PREFIX):] for p in subprocess.check_output(
        ['git', 'ls-tree', '-r', '--name-only', ref, PREFIX + directory], cwd=ROOT,
        text=True).splitlines() if p.endswith(('.png', '.jpg'))]


def open_image(ref, path):
    return Image.open(BytesIO(blob(ref, path))).convert('RGBA')


def swapped(image, palette):
    # One fixed input RGB always maps to one output RGB; alpha and geometry stay exact.
    palette = {tuple(bytes.fromhex(k)): tuple(bytes.fromhex(v)) for k, v in palette.items()}
    out = image.copy()
    out.putdata([palette.get(pixel[:3], pixel[:3]) + (pixel[3],) for pixel in image.getdata()])
    return out


def reconstruct(entry):
    if entry.get('raw'):
        return blob(entry['ref'], entry['source'])
    image = open_image(entry['ref'], entry['source'])
    if 'canvas_from' in entry:
        base = open_image(entry['canvas_from']['ref'], entry['canvas_from']['source'])
        base.paste(image, (0, 0)); image = base
    image = swapped(image, entry.get('palette', {}))
    scale = entry.get('scale', 1)
    if scale != 1:
        image = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
    for patch in entry.get('patches', []):
        source = open_image(patch['ref'], patch['source'])
        for rect in patch['rectangles']:
            image.paste(source.crop(tuple(rect)), tuple(rect[:2]))
    buffer = BytesIO(); image.save(buffer, format='PNG', optimize=False)
    return buffer.getvalue()


def entry(ref, source, scale=1, **kwargs):
    return dict(ref=ref, source=source, source_blob=subprocess.check_output(
        ['git', 'rev-parse', ref + ':' + PREFIX + source], cwd=ROOT, text=True).strip(),
        scale=scale, **kwargs)


def restore_world(entries):
    old = set(files(OLD, 'environment'))
    upstream = set(files(UPSTREAM, 'environment'))
    for path in sorted(old | upstream):
        ref = OLD if path in old else UPSTREAM
        scale = 1
        if '/tiles_' in path or '/walls_' in path:
            scale = 1024 // open_image(ref, path).width
        elif '/custom_tiles/' in path or path.endswith(('terrain_features.png', 'raised_terrain.png')):
            scale = 4
        e = entry(ref, path, scale)
        if path.endswith('terrain_features.png'):
            # v4 added lower rows; preserve their upstream content and the old top rows.
            e['canvas_from'] = dict(ref=UPSTREAM, source=path)
        if path.endswith(('tiles_sewers.png', 'walls_sewers.png')):
            indices = [49, 53, 84, 85, 86, 87, 100, 101, 102, 103,
                       56, 57, 58, 59, 60, 61, 112, 113, 114, 115, 116,
                       224, 225, 226, 227, 228, 229]
            e['patches'] = [dict(ref=APPROVED, source=path, rectangles=[
                [i % 16 * 64, i // 16 * 64, (i % 16 + 1) * 64, (i // 16 + 1) * 64]
                for i in indices])]
        entries[path] = e


def recolor_palette(ref, source, hue):
    # Select only saturated cloth colours, leaving faces, bone and outlines intact.
    # This deterministic palette swap never changes a pixel position or alpha.
    import colorsys
    palette = {}
    for r, g, b, a in sorted(set(open_image(ref, source).getdata())):
        h, s, v = colorsys.rgb_to_hsv(r/255, g/255, b/255)
        if a and s > .35 and v > .12 and not (.015 < h < .13 and r > g > b):
            rgb = tuple(round(c * 255) for c in colorsys.hsv_to_rgb(hue, s, v))
            palette[bytes((r,g,b)).hex()] = bytes(rgb).hex()
    return palette


def restore_characters(entries):
    upstream = set(files(UPSTREAM, 'sprites'))
    excluded = {'items.png', 'item_icons.png', 'avatars.png', 'amulet.png'}
    aliases = {
        'hero_necromancer.png': ('mage.png', .28),
        'hero_enchanter.png': ('mage.png', .58),
        'hero_psychic.png': ('cleric.png', .76),
        'minion_skeleton.png': ('skeleton.png', .28),
        'minion_ghoul.png': ('ghoul.png', .28),
        'hexcaster.png': ('warlock.png', .76),
        'chainwarden.png': ('tengu.png', .58),
        'demon.png': ('ripper.png', None),
        'red_sentry.png': ('sentry.png', None),
    }
    layouts = {}
    for path in sorted(set(files(APPROVED, 'sprites')) | upstream):
        name = Path(path).name
        if name in excluded: continue
        source, hue = aliases.get(name, (name.removeprefix('hero_'), None))
        source = 'sprites/' + source
        if source not in upstream:
            raise ValueError('No upstream source for ' + path)
        e = entry(UPSTREAM, source, 4, character=True,
                  palette=recolor_palette(UPSTREAM, source, hue) if hue is not None else {})
        entries[path] = e
        # Values document source dimensions; frame rectangles come from upstream code.
        layouts[path] = list(open_image(UPSTREAM, source).size)
    for hero in ('warrior','mage','rogue','huntress','duelist','cleric'):
        path = f'splashes/{hero}.jpg'
        entries[path] = entry(UPSTREAM, path, raw=True)
    (ASSETS/'sprites/character-layouts.json').write_text(json.dumps(layouts,indent=2)+'\n',encoding='utf-8')
    # The rejected generated splashes are no longer packaged, even as unused files.
    for hero in ('warrior','mage','rogue','huntress','duelist','cleric','necromancer','enchanter','psychic'):
        p=ASSETS/f'splashes/{hero}.png'
        if p.exists(): p.unlink()


def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--world',action='store_true')
    parser.add_argument('--characters',action='store_true'); parser.add_argument('--check',action='store_true')
    args=parser.parse_args()
    entries=json.loads(MANIFEST.read_text(encoding='utf-8')) if MANIFEST.exists() else {}
    if args.world: restore_world(entries)
    if args.characters: restore_characters(entries)
    failures=[]; characters=0
    for path,e in sorted(entries.items()):
        expected=reconstruct(e); p=ASSETS/path
        if args.check:
            if not p.exists() or p.read_bytes()!=expected: failures.append(path)
        else:
            p.parent.mkdir(parents=True,exist_ok=True); p.write_bytes(expected)
        characters+=bool(e.get('character'))
    if not args.check: MANIFEST.write_text(json.dumps(entries,indent=2)+'\n',encoding='utf-8')
    if args.check:
        for path in files(APPROVED,'sprites'):
            if Path(path).name not in {'items.png','item_icons.png','avatars.png','amulet.png'} and path not in entries:
                failures.append('Unaccounted character '+path)
        for name in ('warrior','mage','rogue','huntress','duelist','cleric','necromancer','enchanter','psychic'):
            if (ASSETS/f'splashes/{name}.png').exists():failures.append('Rejected splash still packaged '+name)
    print(f'TEST 44: upstream-derived character sheets={characters}; restored assets={len(entries)}; failures={len(failures)}')
    for failure in failures: print('FAIL:',failure)
    return bool(failures)


if __name__=='__main__': raise SystemExit(main())
