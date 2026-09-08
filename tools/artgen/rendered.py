"""Palette/outline/atlas post-process for the committed Blender render cache."""
import json
from functools import lru_cache
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFilter

ROOT=Path(__file__).resolve().parents[2];CACHE=Path(__file__).parent/'render_cache'
HEX=['1A1816','3B3733','6B645C','2E241A','4A3B2A','3C2E1F','5A4630','2C2F33','565B62','C9BFA8','EFE7D2','5E0D12','9E1B24','3F6A1F','7BB33B','2E6F7A','6FD3E0','8A4B12','E0982F','8A7331','E4C76A','4A2C6E','9D6BD1']
COLORS=np.array([tuple(bytes.fromhex(c)) for c in HEX],dtype=float)
TONES=np.rint(np.concatenate([COLORS*s for s in [.18,.40,.70,1]]+[COLORS+(255-COLORS)*.16])).astype(np.uint8)

def quantize(image,tile=False):
    pixels=np.array(image.convert('RGBA'));active=pixels[:,:,3]>100;rgb=pixels[:,:,:3].astype(float)
    if tile and active.any():
        lum=rgb@np.array([.2126,.7152,.0722])/255
        lo,hi=np.percentile(lum[active],[8,92]);target=np.clip((lum-lo)/max(.02,hi-lo),0,1)
        # Four hard value bands retain relief under the runtime ambient overlay.
        target=np.array([.055,.18,.32,.46])[np.minimum(3,(target*4).astype(int))]
        shade=np.minimum(1,target/np.maximum(lum,.001));tint=np.clip((target-lum)/np.maximum(1-lum,.001),0,1)
        rgb=np.where((target<lum)[:,:,None],rgb*shade[:,:,None],rgb+(255-rgb)*tint[:,:,None])
        palette=np.rint(np.concatenate([COLORS*s for s in np.linspace(0,1,21)]+[COLORS+(255-COLORS)*s for s in np.linspace(0,1,21)])).astype(np.uint8)
    else:palette=TONES
    flat=rgb.reshape(-1,3);result=np.empty_like(flat,dtype=np.uint8)
    for start in range(0,len(flat),2048):
        chunk=flat[start:start+2048];dist=((chunk[:,None,:]-palette[None,:,:])**2*np.array([.2126,.7152,.0722])).sum(axis=2)
        result[start:start+len(chunk)]=palette[dist.argmin(axis=1)]
    pixels[:,:,:3]=result.reshape(rgb.shape);pixels[:,:,3]=active.astype(np.uint8)*255
    return Image.fromarray(pixels)

def sewers_quantize(image,params,kind):
    """Four value bands preserve material hue without bleaching bright slabs.

    Candidates derive from the locked palette/material nodes, and each must
    pass the existing CIE76 tolerance. Emissive torch pixels retain source values.
    """
    from validate import lab
    pixels=np.array(image.convert('RGBA'));active=pixels[:,:,3]>100
    rgb=pixels[:,:,:3].astype(float);lum=rgb@np.array([.2126,.7152,.0722])/255
    if not active.any():raise ValueError('Empty Sewers render')
    flame_zone=np.indices(lum.shape)[0]<31
    emissive=flame_zone&((rgb[:,:,0]>rgb[:,:,1]*1.1)|(lum>.87))&active
    exposure_mask=active&~emissive if kind=='wall_torch' and params.get('separate_emission_curve') else active
    lo,hi=np.percentile(lum[exposure_mask],[3,97])
    value=(lum if params.get('absolute_tones') else np.clip((lum-lo)/max(.02,hi-lo),0,1))**params['tone_gamma']
    targets=(np.interp(value,params['tone_positions'],params['tone_curve']) if params.get('smooth_tones')
             else np.array(params['tone_curve'])[np.searchsorted(params['tone_breaks'],value)])
    rgb=np.clip(rgb*targets[:,:,None]/np.maximum(lum[:,:,None],.002),0,255)
    original=pixels[:,:,:3].astype(float)
    if kind=='wall_torch':rgb=np.where(emissive[:,:,None],original,rgb)
    flame_edge=params.get('flame_edge','782D17')
    swatches=np.concatenate([COLORS,np.array([tuple(bytes.fromhex(params[k])) for k in ('stone_color','moss_color','water_color')]),np.array([tuple(bytes.fromhex(flame_edge))])])
    candidates=[]
    for level in (np.linspace(0,.9,65) if params.get('smooth_tones') else params['tone_curve']):
        candidates.extend(np.clip(swatches*level/np.maximum(.001,swatches@np.array([.2126,.7152,.0722])/255)[:,None],0,255))
    if kind=='wall_torch':candidates.extend([tuple(bytes.fromhex(c)) for c in (flame_edge,'E0982F','E4C76A','EFE7D2')])
    palette=np.unique(np.rint(candidates).astype(np.uint8),axis=0)
    allowed=np.concatenate([COLORS*s for s in np.linspace(0,1,101)]+[COLORS+(255-COLORS)*s for s in np.linspace(0,1,101)])
    distances=((lab(palette)[:,None,:]-lab(allowed)[None,:,:])**2).sum(axis=2)
    palette=palette[distances.min(axis=1)<=12**2]
    flat=rgb.reshape(-1,3);result=np.empty_like(flat,dtype=np.uint8)
    for start in range(0,len(flat),2048):
        chunk=flat[start:start+2048]
        distance=((chunk[:,None,:]-palette[None,:,:])**2*np.array([.2126,.7152,.0722])).sum(axis=2)
        result[start:start+len(chunk)]=palette[distance.argmin(axis=1)]
    pixels[:,:,:3]=result.reshape(rgb.shape);pixels[:,:,3]=active.astype(np.uint8)*255
    if kind=='wall_torch':
        flame_palette=np.array([tuple(bytes.fromhex(c)) for c in (flame_edge,'E0982F','E4C76A','EFE7D2')])
        flat=original[emissive]
        if len(flat):pixels[:,:,:3][emissive]=flame_palette[((flat[:,None,:]-flame_palette[None,:,:])**2).sum(axis=2).argmin(axis=1)]
    output=Image.fromarray(pixels)
    if kind in ('decor','wall_torch'):
        outline=Image.new('RGBA',output.size,'#0E0D0C')
        outline.putalpha(output.getchannel('A').filter(ImageFilter.MaxFilter(5)))
        outline.alpha_composite(output);output=outline
    return output


def frame(path,size):
    image=Image.open(CACHE/path).convert('RGBA');box=image.getbbox()
    if not box:raise ValueError('Empty Blender frame: '+str(path))
    image=image.crop(box);w,h=size;ratio=min((w-6)/image.width,int(h*.78)/image.height)
    image=image.resize((max(1,round(image.width*ratio)),max(1,round(image.height*ratio))),Image.Resampling.LANCZOS)
    image=quantize(image);canvas=Image.new('RGBA',size);canvas.alpha_composite(image,((w-image.width)//2,h-3-image.height))
    outline=Image.new('RGBA',size,'#0E0D0C');outline.putalpha(canvas.getchannel('A').filter(ImageFilter.MaxFilter(5)));outline.alpha_composite(canvas);return outline

@lru_cache(maxsize=32)
def tile(kind,variant=0):
    # Blender renders a 3x3 neighborhood; only its center is used in the atlas.
    source=Image.open(CACHE/f'tiles/{kind}_{variant}.png').crop((64,64,128,128))
    params=Path(__file__).parent/'blender/params'/f'{"door" if kind=="door_open" else kind}.json'
    if params.exists():return sewers_quantize(source,json.loads(params.read_text()),kind)
    return quantize(source,True)

def character(spec,base=None):
    # Shared upstream atlases also contain variants outside the rendered POC.
    # Keep their generated source frames until those variants reach stage 6.
    output=base.copy() if base is not None else Image.new('RGBA',spec['dimensions']);fw,fh=spec['frame'];kind=spec['rendered_character']
    for row in range(spec.get('tiers',1)):
        for index,pose in enumerate(spec['layout']):
            output.paste(frame(f'{kind}/{row if kind=="necromancer" else 0}/{pose}.png',(fw,fh)),(index*fw,row*fh))
    return output

def apply_tiles(spec,base):
    output=base.copy()
    for index,kind in spec['rendered_tiles'].items():
        index=int(index);x=index%16*64;y=index//16*64;original=base.crop((x,y,x+64,y+64))
        variant=(index//6)%3 if kind in ('floor','decor') and index<16 else index%3
        if kind=='decor':
            material=tile('floor',variant).copy();material.alpha_composite(tile('decor',variant))
        elif kind=='wall_torch':
            material=tile('wall',variant).copy();material.alpha_composite(tile('wall_torch',variant))
        else:material=tile(kind,variant).copy()
        if kind=='floor' and 33<=index<=47:
            params=json.loads((Path(__file__).parent/'blender/params/floor.json').read_text())
            if 'bank_color' in params:
                # Wet bank components retain the floor geometry but use the
                # waterline's darker material/value range, not dry floor albedo.
                a=np.array(material);lum=a[:,:,:3]@np.array([.2126,.7152,.0722])/255
                color=np.array(tuple(bytes.fromhex(params['bank_color'])),dtype=float)
                a[:,:,:3]=np.clip(lum[:,:,None]*color/(color@np.array([.2126,.7152,.0722])/255),0,255)
                bank=dict(params,stone_color=params['bank_color'],tone_curve=params['bank_tone_curve'])
                material=sewers_quantize(Image.fromarray(a),bank,'floor')
        bounds=original.getbbox()
        if kind in ('floor','wall') and bounds and bounds!=(0,0,64,64):
            # Upstream half-height walls and water-bank strips need the full
            # material relief fitted into their logical component, like doors.
            left,top,right,bottom=bounds
            component=material.resize((right-left,bottom-top),Image.Resampling.NEAREST)
            material=Image.new('RGBA',(64,64));material.paste(component,(left,top))
        if kind.startswith('door') and bounds:
            left,top,right,bottom=bounds
            # Upright partial door components use a compact projection of the
            # whole gate. Cropping its dark centre discarded jamb/strap contrast.
            # Horizontal overhangs continue to sample their original strip.
            if bottom-top>right-left:
                component=material.resize((right-left,bottom-top),Image.Resampling.NEAREST)
                material=Image.new('RGBA',(64,64));material.paste(component,(left,top))
        material.putalpha(original.getchannel('A'));output.paste(material,(x,y))
    return output

def comparison(specs,procedural):
    pairs=[];terrain=next(s for s in specs if s['output'].endswith('tiles_sewers.png'));before=procedural(terrain)
    for label,index,kind in [('Floor',0,'floor'),('Wall',48,'wall'),('Water',32,'water'),('Grass',2,'grass'),('Door',56,'door')]:
        if kind=='water':
            water=next(s for s in specs if s['output'].endswith('water0.png'));old=procedural(water)
        else:old=before.crop((index%16*64,index//16*64,index%16*64+64,index//16*64+64))
        pairs.append((label,old,tile(kind,index%3)))
    for name in ['rat','crab','necromancer','skeleton','ghoul']:
        spec=next(s for s in specs if s.get('rendered_character')==name);old=procedural(spec);new=character(spec,old);fw,fh=spec['frame']
        for tier in range(spec.get('tiers',1)):
            box=(0,tier*fh,fw,(tier+1)*fh);pairs.append((name.title()+(' armor '+str(tier) if name=='necromancer' else ''),old.crop(box),new.crop(box)))
    sheet=Image.new('RGB',(576,32+len(pairs)*284),'#141311');d=ImageDraw.Draw(sheet);d.text((16,9),'PROCEDURAL',fill='#C9BFA8');d.text((304,9),'BLENDER RENDERED',fill='#C9BFA8')
    for row,(label,old,new) in enumerate(pairs):
        y=32+row*284;d.text((16,y),label,fill='#C9BFA8')
        for x,im in [(16,old),(304,new)]:
            canvas=Image.new('RGBA',(64,64));im=im.copy();im.thumbnail((64,64));canvas.alpha_composite(im,((64-im.width)//2,(64-im.height)//2));sheet.paste(canvas.resize((256,256),Image.Resampling.NEAREST),(x,y+20),canvas.resize((256,256),Image.Resampling.NEAREST))
    sheet.save(ROOT/'verification/render-poc.png',compress_level=9)

def phash(path):
    image=np.array(Image.open(path).convert('L').resize((32,32),Image.Resampling.LANCZOS),dtype=float)
    n=np.arange(32);k=np.arange(8)[:,None];basis=np.cos(np.pi*(2*n+1)*k/64)
    spectrum=basis@image@basis.T;bits=spectrum>np.median(spectrum.flatten()[1:]);return bits
