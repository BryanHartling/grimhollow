"""Named painted identification emblems, rebuilt from the existing status paintings."""
from pathlib import Path
import hashlib, json
import numpy as np
from PIL import Image
from functools import lru_cache
HERE=Path(__file__).resolve().parent

@lru_cache(None)
def build():
    from inventory import base_file, SEMANTICS
    from status import symbols
    art=symbols(); contract=json.loads(base_file(SEMANTICS))['icons']
    mapping=json.loads((HERE/'identification.json').read_text())['symbols']
    assert set(mapping)==set(contract), 'Every named identification icon needs an explicit source'
    atlas=Image.new('RGBA',(512,256))
    for name,source in mapping.items():
        im=art[source].copy();im.thumbnail((29,29),Image.Resampling.LANCZOS)
        pixels=np.array(im);pixels[pixels[:,:,3]<8]=0
        # Gold rings, warm ivory scrolls, pale turquoise potions remain legible at HUD scale.
        tint=(1,.89,.65) if name.startswith('RING') else (.79,.94,1) if name.startswith('POTION') else (1,.97,.88)
        pixels[:,:,:3]=np.rint(pixels[:,:,:3]*np.array(tint)).astype(np.uint8)
        cell=Image.new('RGBA',(32,32));cell.alpha_composite(Image.fromarray(pixels),((32-im.width)//2,(32-im.height)//2))
        index=contract[name]['artIndex'];atlas.paste(cell,(index%16*32,index//16*32))
        contract[name]['rgbaSha256']=hashlib.sha256(cell.tobytes()).hexdigest()
        contract[name]['sourceStatus']=source
    return atlas,contract

def outputs():return {'sprites/item_icons.png':build()[0]}
