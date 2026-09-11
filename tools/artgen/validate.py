"""SPDX-License-Identifier: GPL-3.0-or-later. Validate generated pixels and report missing inventory.
Default is the complete deliverable gate. --generated-only diagnoses the implemented subset.
"""
import argparse
import json
import sys
import numpy as np
from PIL import Image
from build import ROOT, SPEC_DIR, COLORS, paint, locked

def lab(rgb):
    rgb = rgb/255.0
    linear = np.where(rgb<=.04045,rgb/12.92,((rgb+.055)/1.055)**2.4)
    xyz = linear @ np.array([[.4124564,.3575761,.1804375],[.2126729,.7151522,.0721750],[.0193339,.1191920,.9503041]]).T
    xyz /= [.95047,1,1.08883]
    f = np.where(xyz>(6/29)**3,np.cbrt(xyz),xyz/(3*(6/29)**2)+4/29)
    return np.stack([116*f[:,1]-16,500*(f[:,0]-f[:,1]),200*(f[:,1]-f[:,2])],axis=1)

def validate(generated_only=False):
    errors=[]; outputs=set(); count=0
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
    if not generated_only:
        inventory=[]
        for directory in ['environment','sprites','effects']:
            inventory.extend((ROOT/'core/src/main/assets'/directory).rglob('*.png'))
        missing=[p for p in inventory if p.resolve() not in outputs]
        if missing: errors.append(f'Incomplete art inventory: {len(missing)} existing sheets lack pipeline specs (including {", ".join(p.name for p in missing[:8])}).')
        for hero in ['necromancer','enchanter','psychic']:
            if not (ROOT/'core/src/main/assets/sprites'/f'hero_{hero}.png').exists(): errors.append(f'Missing hero_{hero}.png; character outline, occupancy and animation tests cannot pass.')
        errors.append('Full character/item style and referenced-frame coverage gates are not implemented; tests 16 and 17 cannot be certified.')
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
