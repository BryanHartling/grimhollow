"""Pack committed bounty paintings; no generation service needed by CI."""
from pathlib import Path
from PIL import Image
from inventory import icon
HERE = Path(__file__).resolve().parent


def items(cell_size=64):
    result = {}
    for name, file in [('BLOODMARKED_BRAND', 'brand'), ('WARDENS_COAT', 'coat')]:
        art = Image.open(HERE / 'sources/bounty' / (file + '.png')).convert('RGBA')
        assert art.getchannel('A').getextrema()[0] == 0, (name, 'genuine transparency required')
        result[name] = icon(art, cell_size)
    return result
