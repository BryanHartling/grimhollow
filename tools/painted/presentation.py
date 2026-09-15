"""Compile authored class paintings, matching portraits, trap plates and launchers.

No generation runs here. Crop coordinates and the original trap index/color
contract are fixed; Windows, Android and the game use the same source pixels.
"""
from io import BytesIO
import hashlib
import numpy as np
from PIL import Image, ImageOps
from pack import HERE, ROOT, put
from actors import HEROES

SIZE = (1600, 900)
PORTRAITS = 'interfaces/painted_portraits.png'
CROPS = {
    'warrior': (.37, 0, .65, .498), 'mage': (.385, 0, .665, .498),
    'rogue': (.40, 0, .68, .498), 'huntress': (.39, 0, .67, .498),
    'duelist': (.375, 0, .655, .498), 'cleric': (.38, 0, .66, .498),
    'necromancer': (.38, 0, .66, .498), 'enchanter': (.375, 0, .655, .498),
    'psychic': (.38, 0, .66, .498),
}


def source(name):
    return Image.open(HERE/'sources/presentation'/f'{name}.png').convert('RGBA')


def outputs():
    result = {}
    atlas = Image.new('RGBA', (384, 384))
    for index, name in enumerate(HEROES):
        painting = ImageOps.fit(source(name), SIZE, Image.Resampling.LANCZOS)
        result[f'splashes/painted_{name}.png'] = painting
        crop = CROPS[name]
        face = painting.crop(tuple(round(v*SIZE[i%2]) for i,v in enumerate(crop)))
        face = ImageOps.fit(face, (128,128), Image.Resampling.LANCZOS)
        atlas.paste(face, (index%3*128, index//3*128))
    result[PORTRAITS] = atlas
    assert len({hashlib.sha256(result[f'splashes/painted_{h}.png'].tobytes()).hexdigest() for h in HEROES}) == 9
    return result


def traps(features):
    sheet = source('traps')
    assert sheet.getchannel('A').getextrema()[0] == 0, 'Trap source needs real alpha'
    # Existing RED..BLACK colors. Tint only the authored cyan insets; retain
    # neutral forged metal and each shape even when a plate is disarmed.
    palette = [(255,60,43),(255,140,28),(255,224,47),(67,224,64),
               (44,206,223),(184,100,249),(246,239,220),(133,143,150),(24,27,31)]
    for shape in range(7):
        col,row=shape%4,shape//4
        part=sheet.crop((round(col*sheet.width/4),round(row*sheet.height/2),
                         round((col+1)*sheet.width/4),round((row+1)*sheet.height/2)))
        box=part.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox()
        part=ImageOps.contain(part.crop(box),(56,52),Image.Resampling.LANCZOS)
        base=np.asarray(part).copy()
        rgb=base[:,:,:3].astype(float)
        mask=np.clip((np.minimum(rgb[:,:,1],rgb[:,:,2])-rgb[:,:,0]-12)/65,0,1)[:,:,None]
        assert mask.max() > .9, ('Missing trap inset',shape)
        for color, target in enumerate(palette):
            light=np.maximum(rgb[:,:,1],rgb[:,:,2])[:,:,None]/255
            painted=base.copy()
            painted[:,:,:3]=np.clip(rgb*(1-mask)+np.array(target)*light*mask,0,255).astype('uint8')
            painted[painted[:,:,3]<8]=0
            tile=Image.new('RGBA',(64,64))
            tile.alpha_composite(Image.fromarray(painted),((64-part.width)//2,(64-part.height)//2))
            put(features,shape*16+color,tile)


def launcher_files():
    """Return exact bytes for platform resources, including ICO subimages."""
    art=source('launcher')
    assert art.getchannel('A').getextrema()[0] == 0, 'Launcher needs real alpha'
    art=art.crop(art.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox())
    files={}
    def icon(size, occupied, bg=(0,0,0,0)):
        out=Image.new('RGBA',(size,size),bg)
        part=ImageOps.contain(art,(round(size*occupied),round(size*occupied)),Image.Resampling.LANCZOS)
        out.alpha_composite(part,((size-part.width)//2,(size-part.height)//2))
        return out
    def png(path,im):
        buffer=BytesIO();im.save(buffer,format='PNG');files[path]=buffer.getvalue()
    for size in (16,32,48,64,128,256):
        png(f'desktop/src/main/assets/icons/icon_{size}.png',icon(size,.92))
    buffer=BytesIO();icon(256,.92).save(buffer,format='ICO',sizes=[(s,s) for s in (16,24,32,48,64,128,256)])
    files['desktop/src/main/assets/icons/windows.ico']=buffer.getvalue()
    for density,factor in [('ldpi',.75),('mdpi',1),('hdpi',1.5),('xhdpi',2),('xxhdpi',3),('xxxhdpi',4)]:
        legacy=icon(round(48*factor),.88,(16,18,20,255))
        foreground=icon(round(108*factor),.59) # Entire art inside Android's 66dp safe zone.
        background=Image.new('RGBA',foreground.size,(16,18,20,255))
        # Themed Android icons retain the arch silhouette and the central flame.
        pixels=np.asarray(foreground)
        bright=np.maximum.reduce([pixels[:,:,0],pixels[:,:,1],pixels[:,:,2]])
        mono=Image.new('RGBA',foreground.size,(255,255,255,0))
        mono.putalpha(Image.fromarray(np.where(bright>90,pixels[:,:,3],0).astype('uint8')))
        for variant in ('main','debug'):
            prefix=f'android/src/{variant}/res/mipmap-{density}/ic_launcher'
            for suffix,im in [('',legacy),('_foreground',foreground),('_background',background),('_monochrome',mono)]:
                png(prefix+suffix+'.png',im)
    return files


def pack_launchers(check, failures):
    files=launcher_files()
    for path,data in files.items():
        target=ROOT/path
        if check:
            if not target.exists(): failures.append(path);continue
            if path.endswith('.ico'):
                actual=Image.open(target);expected=Image.open(BytesIO(data))
                if actual.ico.sizes()!=expected.ico.sizes() or any(actual.ico.getimage(s).tobytes()!=expected.ico.getimage(s).tobytes() for s in expected.ico.sizes()):failures.append(path)
            else:
                actual=Image.open(target).convert('RGBA');expected=Image.open(BytesIO(data)).convert('RGBA')
                if actual.size!=expected.size or actual.tobytes()!=expected.tobytes():failures.append(path)
        else:
            target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
    print(f'PAINTED launcher resources={len(files)}')
