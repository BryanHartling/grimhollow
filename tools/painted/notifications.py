"""Reuse committed painted symbols for notifications, preserving logical sizes and IDs."""
import re
import json
from PIL import Image, ImageOps
from pack import ROOT, cell, fit
from status import symbols
from interface_art import glyphs


def outputs(built):
    art, icons = symbols(), glyphs()
    # The quest row's pickaxe uses the same named slot as the inventory packer.
    from inventory import base_file, SEMANTICS
    pickaxe = cell(built['sprites/items.png'], json.loads(base_file(SEMANTICS))['items']['PICKAXE']['artIndex'])
    atlas = Image.new('RGBA', (18*56, 5*64))
    mapping = {0: art[45], 1: art[45], 2: art[72], 3: pickaxe,
               5: art[5], 6: art[2], 7: art[34], 8: art[15],
               9: built['effects/painted_particles.png'].crop((64,128,128,192)),
               10: art[26], 11: art[3], 12: art[48], 13: art[3], 14: art[8],
               15: art[28], 16: art[36], 17: art[59], 18: art[44], 19: art[76],
               20: icons['TALENT'], 21: icons['STATS'], 23: icons['COIN_SML'],
               24: icons['ENERGY_SML'], 83: art[86]}
    reasons = [45, 20, 37, 47, 70, 27, 63, 63, 64, 42, 27, 51]
    misses = [45, 20, 37, 47, 70, 27, 63, 63, 61, 63, 51]
    for i, symbol in enumerate(reasons):
        mapping[36+i] = mapping[54+i] = art[symbol]
    for i, symbol in enumerate(misses):
        mapping[72+i] = art[symbol]
    java = (ROOT/'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/effects/FloatingText.java').read_text()
    required = {int(n) for n in re.findall(r'public static int \w+\s*=\s*(\d+);', java)}
    assert required <= mapping.keys(), ('unmapped notification', required-mapping.keys())
    for index, source in mapping.items():
        frame = Image.new('RGBA', (56, 64))
        part = fit(source, 48, 56)
        frame.alpha_composite(part, ((56-part.width)//2, (64-part.height)//2))
        # Separate hit, armor-piercing hit, and miss reasons without pixel glyphs.
        marker = 'RIGHTARROW' if 54 <= index < 66 or index == 1 else 'CLOSE' if 72 <= index <= 82 else None
        if marker:
            mark = ImageOps.contain(icons[marker], (22, 22), Image.Resampling.LANCZOS)
            frame.alpha_composite(mark, (56-mark.width, 64-mark.height))
        atlas.alpha_composite(frame, (index % 18*56, index//18*64))
    spells = Image.new('RGBA', (512, 64))
    for i, source in enumerate([built['effects/painted_food.png'], icons['JOURNAL'], art[34],
                                art[40], art[52], art[41], art[0], art[25]]):
        part = fit(source, 60, 60)
        spells.alpha_composite(part, (i*64+(64-part.width)//2, (64-part.height)//2))
    return {'effects/painted_notifications.png': atlas, 'effects/painted_spells.png': spells}
