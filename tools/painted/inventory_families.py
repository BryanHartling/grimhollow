"""Complete item families from authored objects; aliases and coatings remain explicit."""
import json
import numpy as np
from PIL import Image, ImageEnhance, ImageOps
from inventory import HERE, panels, icon

def colorize(image,color,mask=None):
    pixels=np.array(image).copy();rgb=pixels[:,:,:3].astype(float)
    light=(rgb@[.2126,.7152,.0722])/255
    tint=np.array(color,dtype=float)/255
    painted=np.clip((.18+.92*light[:,:,None])*tint*255,0,255)
    if mask is None:mask=pixels[:,:,3]>0
    pixels[:,:,:3][mask]=(rgb*.15+painted*.85)[mask].astype('uint8')
    return Image.fromarray(pixels)

def bottle(image,color):
    pixels=np.array(image);r,g,b=[pixels[:,:,i].astype(float) for i in range(3)]
    # Repaint the purple glass and crystal, preserving the authored bronze fittings.
    return colorize(image,color,(b>g*1.15)&(b>r*.75))

def extend(replacements,semantics):
    config=json.loads((HERE/'items-continuation.json').read_text())
    for sheet,names in config['sheets'].items():
        assert len(names)==16,sheet
        for i,name in enumerate(names):
            if name is not None:replacements[name]=icon(panels(sheet)[i])
    colors=[(180,45,58),(203,126,42),(225,195,61),(74,163,84),(58,178,176),(67,149,215),
            (100,85,188),(191,70,159),(134,95,57),(69,69,78),(185,204,216),(235,224,191)]
    color_names=['CRIMSON','AMBER','GOLDEN','JADE','TURQUOISE','AZURE','INDIGO','MAGENTA','BISTRE','CHARCOAL','SILVER','IVORY']
    for name,color in zip(color_names,colors):
        replacements['EXOTIC_'+name]=bottle(replacements['ELIXIR_ARCANE'],color)
    runes=['KAUNAN','SOWILO','LAGUZ','YNGVI','GYFU','RAIDO','ISAZ','MANNAZ','NAUDIZ','BERKANAN','ODAL','TIWAZ']
    for rune in runes:
        source=replacements['SCROLL_'+rune];pixels=np.array(source)
        r,g,b=[pixels[:,:,i].astype(float) for i in range(3)]
        replacements['EXOTIC_'+rune]=colorize(source,(83,151,220),(r>g*1.5)&(r>b*1.4))
    darts=['ROT','INCENDIARY','ADRENALINE','HEALING','CHILLING','SHOCKING','POISON','CLEANSING','PARALYTIC','HOLY','DISPLACING','BLINDING']
    coatings=[(169,45,59),(239,105,38),(192,148,81),(91,186,99),(124,211,237),(147,133,233),
              (91,144,47),(221,131,211),(213,166,56),(249,229,151),(81,184,184),(106,109,122)]
    yy,xx=np.mgrid[:64,:64]
    for name,color in zip(darts,coatings):
        source=replacements['DART'];pixels=np.array(source)
        mask=(xx<35)&(yy>31)&(pixels[:,:,3]>0)
        replacements[name+'_DART']=colorize(source,color,mask)
    for name,color in [('AQUA',(48,170,185)),('TOXIC',(110,177,45)),('ICY',(146,216,242))]:
        replacements['ELIXIR_'+name]=bottle(replacements['ELIXIR_ARCANE'],color)
    replacements['OVERPRICED']=ImageEnhance.Brightness(replacements['SUPPLY_RATION']).enhance(1.2)
    for name,color in zip(['SEWER','PRISON','CAVES','CITY','HALLS'],[(169,179,110),(154,167,185),(153,142,108),(203,168,87),(184,97,98)]):
        replacements[name+'_PAGE']=colorize(replacements['GUIDE_PAGE'],color)
    holders={'WEAPON':'WORN_SHORTSWORD','ARMOR':'ARMOR_PLATE','MISSILE':'DART','WAND':'WAND_MAGIC_MISSILE',
             'RING':'RING_AMETHYST','ARTIFACT':'ARTIFACT_HOURGLASS','TRINKET':'RAT_SKULL','FOOD':'RATION',
             'BOMB':'BOMB','POTION':'POTION_CRIMSON','SEED':'SEED_SUNGRASS','SCROLL':'SCROLL_KAUNAN',
             'STONE':'STONE_AUGMENTATION','ELIXIR':'ELIXIR_ARCANE','SPELL':'MAGIC_INFUSE','MOB':'RAT_SKULL','DOCUMENT':'GUIDE_PAGE'}
    for name,source in holders.items():
        image=replacements[source]
        gray=ImageOps.grayscale(image).convert('RGBA');gray.putalpha(image.getchannel('A'))
        replacements[name+'_HOLDER']=gray
    from status import symbols
    replacements['SOMETHING']=icon(symbols()[87])
    # Every public item ID is accounted for, including the DART/DARTS alias.
    by_index={semantics['items'][name]['artIndex']:art for name,art in replacements.items()}
    missing=[]
    for name,entry in semantics['items'].items():
        if name not in replacements:
            if entry['artIndex'] in by_index:replacements[name]=by_index[entry['artIndex']]
            else:missing.append(name)
    assert not missing,('Unpainted named inventory cells',missing)
