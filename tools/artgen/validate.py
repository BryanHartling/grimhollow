"""SPDX-License-Identifier: GPL-3.0-or-later. Validate generated pixels and report missing inventory.
Default is the complete deliverable gate. --generated-only diagnoses the implemented subset.
"""
import argparse
import json
import sys
import numpy as np
import colorsys
from PIL import Image,ImageFilter
from build import ROOT, SPEC_DIR, COLORS, paint, locked

def character_checks(spec,pixels,errors,regional):
    fw,fh=spec['frame'];cols=spec['dimensions'][0]//fw
    if (fw,fh) not in ((48,60),(96,96)):errors.append(spec['output']+': not a native character frame')
    for i,entry in enumerate(spec['poses']):
        frame=pixels[i//cols*fh:(i//cols+1)*fh,i%cols*fw:(i%cols+1)*fw]
        active=frame[:,:,3]>0
        if not active.any():errors.append(f'{spec["output"]}: missing frame {i}')
        opaque=frame[:,:,3]==255
        eroded=np.array(Image.fromarray(np.pad(opaque.astype('uint8')*255,2)).filter(ImageFilter.MinFilter(5)))[2:-2,2:-2]>0
        if (opaque&~eroded&np.any(frame[:,:,:3]!=[14,13,12],axis=2)).any():
            errors.append(f'{spec["output"]}: frame {i} lacks an intact two-pixel outline')
        if entry['pose']=='idle' and active.any():
            y=np.where(active)[0];occupancy=(y.max()-y.min()+1)/fh
            if not .60<=occupancy<=.85:errors.append(f'{spec["output"]}: idle {i} occupancy {occupancy:.3f} outside .60-.85')
    for form in spec['forms']:
        for name,ids in form['animations'].items():
            if len(set(ids))>1:
                frames={pixels[i//cols*fh:(i//cols+1)*fh,i%cols*fw:(i%cols+1)*fw].tobytes() for i in set(ids)}
                if len(frames)<2:errors.append(f'{spec["output"]}: {form["name"]} {name} has no animated change')
        indices=set(form['animations'].get('idle',[form['idle']]))
        for i in indices:
            frame=Image.fromarray(pixels[i//cols*fh:(i//cols+1)*fh,i%cols*fw:(i%cols+1)*fw])
            frame.thumbnail((16,16),Image.Resampling.LANCZOS)
            tiny=np.array(frame);rgb=tiny[:,:,:3][tiny[:,:,3]>=128]
            hist=np.histogram([colorsys.rgb_to_hsv(*color)[0] for color in rgb/255],bins=16,range=(0,1))[0].astype(float)
            hist/=max(1,hist.sum())
            for region in form['regions']:regional.setdefault(region,[]).append((form['species'],form['name'],i,hist))

def readability(regional,errors):
    total=0;minimum=2.;bad={}
    for region,entries in regional.items():
        for j,(species,name,index,hist) in enumerate(entries):
            for other,oname,oi,ohist in entries[j+1:]:
                if species==other:continue
                distance=float(np.abs(hist-ohist).sum());total+=1;minimum=min(minimum,distance)
                if distance<.25:
                    key=(region,name,oname);bad[key]=min(bad.get(key,2),distance)
    for (region,name,other),value in bad.items():errors.append(f'TEST 30 {region}: {name}/{other} hue L1={value:.4f} below .25')
    if not regional:errors.append('TEST 30: native regional character coverage missing')
    print(f'TEST 30: {len(regional)} regions; idle pairs={total}; minimum hue L1={minimum:.4f}; failing species pairs={len(bad)}')

def lab(rgb):
    rgb = rgb/255.0
    linear = np.where(rgb<=.04045,rgb/12.92,((rgb+.055)/1.055)**2.4)
    xyz = linear @ np.array([[.4124564,.3575761,.1804375],[.2126729,.7151522,.0721750],[.0193339,.1191920,.9503041]]).T
    xyz /= [.95047,1,1.08883]
    f = np.where(xyz>(6/29)**3,np.cbrt(xyz),xyz/(3*(6/29)**2)+4/29)
    return np.stack([116*f[:,1]-16,500*(f[:,0]-f[:,1]),200*(f[:,1]-f[:,2])],axis=1)

def item_checks(spec,pixels,errors):
    import items,item_catalog
    entries=items.catalog()['items'];compiled={e['index'] for e in entries}
    java=item_catalog.constants(item_catalog.SOURCE.read_text().split('public static class Icons')[0])
    if compiled!=set(java.values()):errors.append('Item catalog does not cover the current Java indices')
    for entry in entries:
        index=entry['index'];x=index%16*32;y=index//16*32;frame=pixels[y:y+32,x:x+32]
        if frame.shape!=(32,32,4):errors.append(f'Item {entry["name"]}: atlas index outside sheet');continue
        opaque=frame[:,:,3]>0
        if not opaque.any():errors.append(f'Item {entry["name"]}: empty frame');continue
        yy,xx=np.where(opaque)
        if abs((xx.min()+xx.max())/2-15.5)>1 or abs((yy.min()+yy.max())/2-15.5)>1:
            errors.append(f'Item {entry["name"]}: object is not centred')
        eroded=np.array(Image.fromarray(np.pad(opaque.astype('uint8')*255,2)).filter(ImageFilter.MinFilter(5)))[2:-2,2:-2]>0
        if (opaque&~eroded&np.any(frame[:,:,:3]!=[14,13,12],axis=2)).any():errors.append(f'Item {entry["name"]}: two-pixel outline damaged')
        allowed={tuple(c) for c in items.tones(entry)}|{(14,13,12)}
        if any(tuple(c) not in allowed for c in np.unique(frame[:,:,:3][opaque],axis=0)):
            errors.append(f'Item {entry["name"]}: more than three source material colors')
    print(f'Item style and frame coverage: public indices={len(java)} unique props={len(entries)}')

def frame_checks(spec,pixels,errors):
    if spec.get('native_glyphs'):
        import glyphs
        entries=glyphs.entries(spec['native_glyphs']);fw=fh=32;cols=16
        for name,index in entries.items():
            patch=pixels[index//cols*fh:(index//cols+1)*fh,index%cols*fw:(index%cols+1)*fw]
            if patch.shape!=(fh,fw,4) or not patch[:,:,3].any():errors.append(f'{spec["output"]}: missing {name} glyph {index}')
        print(f'{spec["native_glyphs"]} frame coverage: {len(entries)}')
    for index in spec.get('required_frames',[]):
        fw,fh=spec['frame'];cols=spec['dimensions'][0]//fw
        patch=pixels[index//cols*fh:(index//cols+1)*fh,index%cols*fw:(index%cols+1)*fw]
        if patch.shape!=(fh,fw,4) or not patch[:,:,3].any():errors.append(f'{spec["output"]}: referenced frame {index} empty')

def validate(generated_only=False):
    errors=[]; outputs=set(); count=0;regional={};native=0
    # Finite tints/shades used by this painter; CIE76 tolerance remains the spec's 12.
    candidates=np.concatenate([COLORS*s for s in np.linspace(0,1,101)]+[COLORS+(255-COLORS)*s for s in np.linspace(0,1,101)])
    palette_lab=lab(candidates)
    for source in sorted(SPEC_DIR.glob('*.json')):
        spec=json.loads(source.read_text()); path=ROOT/spec['output']; outputs.add(path.resolve()); count+=1
        if not path.exists(): errors.append(f'{spec["output"]}: missing'); continue
        image=Image.open(path).convert('RGBA')
        if max(image.size)>4096: errors.append(f'{spec["output"]}: atlas exceeds 4096')
        if list(image.size)!=spec['dimensions']: errors.append(f'{spec["output"]}: wrong dimensions')
        pixels=np.array(image); visible=pixels[:,:,3]>0
        if not visible.any(): errors.append(f'{spec["output"]}: empty asset'); continue
        if spec.get('native_character'):
            character_checks(spec,pixels,errors,regional);native+=1
        if spec.get('rendered_items'):item_checks(spec,pixels,errors)
        frame_checks(spec,pixels,errors)
        if spec.get('rendered_character'):
            fw,fh=spec['frame']
            for row in range(spec.get('tiers',1)):
                for index,pose in enumerate(spec['layout']):
                    frame=pixels[row*fh:(row+1)*fh,index*fw:(index+1)*fw];active=frame[:,:,3]>0
                    if not active.any():errors.append(f'{spec["output"]}: empty rendered tier {row} frame {index}')
                    if pose.startswith('idle') and active.any():
                        y=np.where(active)[0];occupancy=(y.max()-y.min()+1)/fh
                        if not .60<=occupancy<=.85:errors.append(f'{spec["output"]}: idle occupancy {occupancy:.3f} outside .60-.85')
        unique=np.unique(pixels[:,:,:3][visible],axis=0)
        for chunk in np.array_split(lab(unique),max(1,len(unique)//128)):
            delta=np.sqrt(((chunk[:,None,:]-palette_lab[None,:,:])**2).sum(axis=2)).min(axis=1)
            if np.any(delta>12): errors.append(f'{spec["output"]}: palette deltaE exceeds 12'); break
        if spec['kind']=='tile':
            for y in range(0,image.height,64):
                for x in range(0,image.width,64):
                    tile=pixels[y:y+64,x:x+64]; alpha=tile[:,:,3]>0
                    if alpha.any() and (tile[:,:,:3][alpha] @ np.array([.2126,.7152,.0722])/255).mean()>.45:
                        errors.append(f'{spec["output"]}: tile ({x//64},{y//64}) luminance exceeds .45')
                    if alpha.any() and (tile[:,:,:3][alpha] @ np.array([.2126,.7152,.0722])/255).std()<.08:
                        errors.append(f'{spec["output"]}: tile ({x//64},{y//64}) luminance std below .08')
        # Construction check also prevents untracked text/logo/watermark additions.
        # Locked human replacements need separate review; they are never automatically certified.
        if locked(path): errors.append(f'{spec["output"]}: locked human override requires style review')
        elif path.suffix=='.png' and image.tobytes()!=paint(spec).tobytes(): errors.append(f'{spec["output"]}: differs from source painting')
    if native:readability(regional,errors)
    if not generated_only:
        inventory=[]
        for directory in ['environment','sprites','effects']:
            inventory.extend((ROOT/'core/src/main/assets'/directory).rglob('*.png'))
        missing=[p for p in inventory if p.resolve() not in outputs]
        if missing: errors.append(f'Incomplete art inventory: {len(missing)} existing sheets lack pipeline specs (including {", ".join(p.name for p in missing[:8])}).')
        for hero in ['necromancer','enchanter','psychic']:
            if not (ROOT/'core/src/main/assets/sprites'/f'hero_{hero}.png').exists(): errors.append(f'Missing hero_{hero}.png; character outline, occupancy and animation tests cannot pass.')
        item_specs=[json.loads(p.read_text()) for p in SPEC_DIR.glob('*.json') if json.loads(p.read_text()).get('rendered_items')]
        if len(item_specs)!=1:errors.append('Full art requires one validated native item atlas')
        if native!=78:errors.append(f'Native character atlas coverage changed: {native}, expected 78; audit new sprites')
    for error in errors: print('FAIL:',error)
    print(f'Validated {count} generated specifications; {len(errors)} failures.' + (' Subset diagnostic only.' if generated_only else ''))
    return bool(errors)

if __name__=='__main__':
    parser=argparse.ArgumentParser(); parser.add_argument('--generated-only',action='store_true');parser.add_argument('--rerender',action='store_true');parser.add_argument('--rebuild',action='store_true')
    parser.add_argument('--asset',help='Scope a fresh Blender reproducibility run, preserving approved classes')
    args=parser.parse_args();render_fail=False
    if args.rebuild:
        import hashlib
        from build import build
        paths=[ROOT/json.loads(p.read_text())['output'] for p in SPEC_DIR.glob('*.json')]
        before={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in paths};build()
        changes=[p for p,sha in before.items() if hashlib.sha256(p.read_bytes()).hexdigest()!=sha]
        render_fail=bool(changes);print(f'TEST 28: {len(paths)} rebuilt files; byte differences={len(changes)}; Blender not invoked')
    if args.rerender:
        from rendered import CACHE,phash
        from build import render,build
        paths=[p for name in args.asset.split(',') for p in (CACHE/name).rglob('*.png')] if args.asset else list(CACHE.rglob('*.png'))
        # A tolerance check must not replace the reviewed cache with a different render.
        original={p:p.read_bytes() for p in paths}
        before={p:phash(p) for p in paths};render(args.asset);build()
        changed=[(str(p.relative_to(CACHE)),int(np.count_nonzero(bits!=phash(p)))) for p,bits in before.items()]
        bad=[(name,bits) for name,bits in changed if bits>2];render_fail=render_fail or bool(bad) or not before
        for name,bits in bad:print('FAIL: render pHash',name,bits,'bits')
        print(f'TEST 29: {len(changed)} cached frames; max pHash distance={max((v for _,v in changed),default=-1)}; failures={len(bad)}')
        for path,data in original.items():path.write_bytes(data)
        import rendered
        rendered.tile.cache_clear();rendered.liquid_frame.cache_clear();rendered.region_tile.cache_clear();build()
    sys.exit(validate(args.generated_only) or render_fail)
