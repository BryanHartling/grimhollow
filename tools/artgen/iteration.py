"""Stage 5.6 parameter iteration and its specified JSON/PNG review deliverables.

Measurements are pixel statistics, not similarity to a photograph. Targets and
weights are transcribed from references.md and are never adjusted by the loop.
Room-wide targets use one fixed composition with the runtime light equation;
surface targets use visible, unlit pixels. No game mechanics are simulated here.
"""
import argparse
import hashlib
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from build import ROOT, COLORS
from rendered import tile, CACHE
from validate import lab

CLASSES=('floor','wall','water','door','decor','wall_torch')
OUT=ROOT/'verification/iteration'
PARAMS=ROOT/'tools/artgen/blender/params'
LUMA=np.array([.2126,.7152,.0722])
REFERENCES={
 'floor':[(4,1),(19,1),(20,.8),(21,.7)],
 'wall':[(4,1),(1,.9),(3,.4),(5,.6),(21,.7)],
 'water':[(4,1),(6,.7),(20,.8),(21,.7)],
 'door':[(2,.5),(1,.9),(21,.7)],
 'decor':[(2,.5),(20,.8),(21,.7)],
 'wall_torch':[(22,1),(24,.7),(5,.8)]}
# Sources with only qualitative targets (23, 36, supplied CC0 torch studies)
# inform critiques; they do not receive invented numeric scores.


def hsv(rgb):
    rgb=rgb/255;maximum=rgb.max(axis=-1);minimum=rgb.min(axis=-1);delta=maximum-minimum
    hue=np.zeros_like(maximum);red,green,blue=np.moveaxis(rgb,-1,0)
    for mask,value in [(maximum==red,(green-blue)/np.maximum(delta,1e-8)%6),
                       ((maximum==green)&(maximum!=red),(blue-red)/np.maximum(delta,1e-8)+2),
                       ((maximum==blue)&(maximum!=red)&(maximum!=green),(red-green)/np.maximum(delta,1e-8)+4)]:
        hue=np.where(mask,value*60,hue)
    return hue,np.divide(delta,maximum,out=np.zeros_like(delta),where=maximum>0),maximum


def measurements(image):
    rgba=np.array(image);rgb=rgba[:,:,:3].astype(float);active=rgba[:,:,3]>0
    visible=rgb[active];lum=visible@LUMA/255;h,s,v=hsv(visible)
    unique=np.unique(visible,axis=0)
    allowed=np.concatenate([COLORS*a for a in np.linspace(0,1,101)]+[COLORS+(255-COLORS)*a for a in np.linspace(0,1,101)])
    delta=np.sqrt(((lab(unique)[:,None,:]-lab(allowed)[None,:,:])**2).sum(axis=2)).min(axis=1)
    chromatic=(s>.10)&(lum>.03)&(lum<.75)
    hue=float(np.degrees(np.angle(np.mean(np.exp(1j*np.radians(h[chromatic]))))))%360 if chromatic.any() else 0
    return dict(mean=float(lum.mean()),std=float(lum.std()),dark_fraction=float((lum<.15).mean()),
                hue=hue,saturation=float(np.median(s[chromatic])) if chromatic.any() else 0,
                highlights_075=float((lum>=.75).mean()),core_090=float((lum>=.90).mean()),
                mortar_contrast=float(np.percentile(lum,85)-np.percentile(lum,15)),
                palette_max_delta=float(delta.max()),opaque_pixels=int(active.sum()))


def seams(image,prop=False):
    a=np.array(image).astype(float)/255
    # Premultiplied RGBA catches both colour jumps and alpha silhouettes at a join.
    values=np.concatenate([a[:,:,:3]*a[:,:,3,None],a[:,:,3,None]],axis=2)
    dx=np.abs(np.diff(values,axis=1)).mean(axis=2).mean(axis=0)
    dy=np.abs(np.diff(values,axis=0)).mean(axis=2).mean(axis=1)
    wrap_x=float(np.abs(values[:,0]-values[:,-1]).mean())
    wrap_y=float(np.abs(values[0]-values[-1]).mean())
    # A boundary must not be a stronger full-edge feature than the interior's
    # 95th percentile. The one-byte allowance is quantization, not a seam blur.
    limit_x=float(np.percentile(dx,95)+1/255);limit_y=float(np.percentile(dy,95)+1/255)
    if prop:
        passed=bool(not a[0,:,3].any() and not a[-1,:,3].any() and not a[:,0,3].any() and not a[:,-1,3].any())
    else:passed=bool(wrap_x<=limit_x and wrap_y<=limit_y)
    return dict(pass_=passed,wrap_x=wrap_x,wrap_y=wrap_y,interior_x_limit=limit_x,interior_y_limit=limit_y,
                method='2x2 wrap; mean boundary gradient <= interior edge p95 + 1/255; props require transparent perimeter')


def scene(images):
    # Fixed seven-tile-wide sample, specified before the first score: 24 boundary
    # walls, 14 floors including a bridge, 8 water, one door, one rubble, one torch.
    layout=['wwwtwww','waafffw','waafffw','wfffffw','waadffw','waafffw','wwwowww']
    canvas=Image.new('RGBA',(448,448))
    mapping={'w':'wall','a':'water','f':'floor','o':'door','d':'decor','t':'wall_torch'}
    for y,line in enumerate(layout):
        for x,c in enumerate(line):
            kind=mapping[c];im=images[kind]
            if kind in ('decor','wall_torch'):
                im=images['floor' if kind=='decor' else 'wall'].copy();im.alpha_composite(images[kind])
            canvas.alpha_composite(im,(x*64,y*64))
    raw=np.array(canvas)[:,:,:3].astype(float)/255
    yy,xx=np.mgrid[:448,:448];xx=(xx//16+.5)/4;yy=(yy//16+.5)/4
    # Actual LightMap AMBIENT[0] and LightingOverlay hero/torch source values.
    light=np.broadcast_to(np.array([.50,.55,.45]),raw.shape).copy()
    for x,y,radius,color in [(3.5,3.5,8,[.45,.31,.12]),(3.5,.5,3,[.38,.24,.08])]:
        strength=np.maximum(0,1-np.hypot(xx-x,yy-y)/radius)**2
        light+=strength[:,:,None]*color
    lit=raw*np.minimum(1,light)
    return lit,raw,light


def globals_for(images):
    lit,raw,light=scene(images);h,s,v=hsv(lit*255)
    dominant=(h>=15)&(h<=125)
    # Neutral metal/black pixels do not constitute an additional coloured accent.
    accent_mask=(~dominant)&(s>=.10)&(v>.025)
    bins=np.bincount((h[accent_mask]//30).astype(int),minlength=12)/h.size
    present=bins>.01
    accents=int(sum(present[i] and not present[(i-1)%12] for i in range(12)))
    if present.all():accents=1
    mean=float((lit@LUMA).mean());coverage=float(dominant.mean())
    return dict(region_mean_after_lighting=mean,region_mean_pass=.11<=mean<=.17,
                dominant_green_brown_fraction=coverage,hue_budget_pass=coverage>=.70 and accents<=1,
                accent_families=accents,accent_bins=bins.tolist(),
                readability='N/A: no characters or mobs are iterated; full-game test 30 remains NOT RUN',
                composition='7x7 fixed terrain sample; actual ambient/hero/torch equation, no fog or UI',
                ambient=[.50,.55,.45])


def target(name,value,low=None,high=None):
    passed=bool((low is None or value>=low) and (high is None or value<=high))
    if passed:score=1.
    elif low is not None and value<low:score=max(0,value/low) if low else 0.
    else:score=max(0,1-(value-high)/max(abs(high),.01))
    return dict(target=name,value=float(value),minimum=low,maximum=high,pass_=passed,score=score)


def scores(kind,metrics,p,seam,images):
    lit,raw,light=scene(images);room_lum=lit@LUMA
    h,s,v=hsv(lit*255)
    # Lighting split is sampled close to and far from the existing wall source.
    near=h[0:128,128:320];far=h[320:448,:]
    near_h=float(np.median(near));far_h=float(np.median(far))
    water=measurements(images['water']);m=metrics
    flame=np.array(images['wall_torch']);fh,fs,fv=hsv(flame[:,:,:3].astype(float));fl=flame[:,:,:3]@LUMA/255
    # The flame is above the holder (centre y=32); exclude brown wood and collars.
    flame_zone=np.indices(fl.shape)[0]<31
    mask=(flame[:,:,3]>0)&(fh<65)&(fs>.25)&(fl>.12)&flame_zone
    core=(flame[:,:,3]>0)&(fl>=.9)
    # Include the neutral core in the flame denominator, excluding iron/wood.
    flame_area=max(1,int(mask.sum()+core.sum()))
    edge=mask&(fl<.3)
    edge_hue=float(np.median(fh[edge])) if edge.any() else 0
    all_rows={
      1:[target('room mean luminance',float(room_lum.mean()),.12,.20),target('room fraction below .15',float((room_lum<.15).mean()),.60),target('warm lit hue',near_h,30,40)],
      2:[target('room mean luminance',float(room_lum.mean()),.12,.20),target('room fraction below .15',float((room_lum<.15).mean()),.60),target('warm lit hue',near_h,30,40)],
      3:[target('room mean luminance',float(room_lum.mean()),.10,.18)],
      4:([target('constructed stones per tile edge',p['count'],2,3),target('surface p85-p15 mortar contrast',m['mortar_contrast'],.20)] if kind in ('floor','wall') else []) +
         ([target('water hue',water['hue'],80,110),target('water saturation',water['saturation'],.25,.40)] if kind=='water' else []),
      5:[target('near warm hue',near_h,25,40),target('far cool hue',far_h,200,230)],
      6:[target('water mean luminance',water['mean'],high=.12),target('water highlight fraction',water['highlights_075'],high=.03)],
      19:[target('constructed flagstones per tile edge',p['count'],2,3),target('highlight fraction',m['highlights_075'],high=.05),target('surface luminance std',m['std'],.10)],
      20:[],
      21:[target('2x2 seam pass',int(seam['pass_']),1)],
      22:[target('flame has >= .90 luminance core',int(core.any()),1),target('core fraction of flame',float(core.sum()/flame_area),high=.02),target('flame edge hue',edge_hue,10,20)],
      # The current runtime source has radius 3. Measure its residual at 2 tiles;
      # do not silently shorten it to make a presentation score pass.
      24:[target('wall light residual at two tiles',float(np.linalg.norm(np.array([.38,.24,.08])*(1-2/3)**2)),high=1/255)]}
    moss_rgba=np.array(images[kind if kind in ('floor','decor') else 'water']);mh,ms,mv=hsv(moss_rgba[:,:,:3].astype(float))
    moss=(mh>=60)&(mh<=125)&(ms>.15)&(moss_rgba[:,:,3]>0)
    moss_lum=moss_rgba[:,:,:3]@LUMA/255
    all_rows[20]=[target('moss/wet-edge hue',float(np.median(mh[moss])) if moss.any() else 0,75,100),
                  target('moss/wet-edge saturation',float(np.median(ms[moss])) if moss.any() else 0,.30,.50),
                  target('moss/wet-edge luminance std',float(moss_lum[moss].std()) if moss.any() else 0,.08)]
    rows=[]
    for number,weight in REFERENCES[kind]:
        targets=all_rows[number]
        rows.append(dict(reference=number,weight=weight,targets=targets,score=float(np.mean([t['score'] for t in targets]))))
    return rows,float(sum(r['weight']*r['score'] for r in rows)/sum(r['weight'] for r in rows))


def record(number,kinds):
    if not 1<=number<=12:raise ValueError('Rounds must be 1-12')
    images={k:tile(k,0) for k in CLASSES}
    global_metrics=globals_for(images)
    for kind in kinds:
        folder=OUT/kind;folder.mkdir(parents=True,exist_ok=True);stem=f'round-{number:02}'
        path=folder/(stem+'.json')
        if path.exists():raise ValueError('Recorded rounds are immutable: '+str(path))
        p=json.loads((PARAMS/(kind+'.json')).read_text())
        variants=[]
        for variant in range(3):
            im=tile(kind,variant);m=measurements(im);seam=seams(im,kind in ('decor','wall_torch'))
            valid=bool(im.size==(64,64) and m['opaque_pixels']>0 and m['palette_max_delta']<=12 and m['std']>=.08 and m['mean']<=.45)
            variants.append(dict(variant=variant,metrics=m,seam=seam,validator_pass=valid))
        rows,score=scores(kind,variants[0]['metrics'],p,variants[0]['seam'],images)
        passed=all(v['validator_pass'] and v['seam']['pass_'] for v in variants) and global_metrics['region_mean_pass'] and global_metrics['hue_budget_pass']
        hashes={f'tiles/{kind}_{i}.png':hashlib.sha256((CACHE/f'tiles/{kind}_{i}.png').read_bytes()).hexdigest() for i in range(3)}
        data=dict(asset_class=kind,round=number,parameters=p,references=rows,composite=score,global_targets=global_metrics,
                  variants=variants,passing=bool(passed),converged=bool(passed and score>=.85),cache_sha256=hashes,
                  reference_board_sha256=hashlib.sha256((ROOT/'references/references.md').read_bytes()).hexdigest())
        path.write_text(json.dumps(data,indent=2)+'\n')
        images[kind].save(folder/(stem+'.png'))
        print(f'{kind} round={number:02} composite={score:.3f} validator={all(v["validator_pass"] for v in variants)} seams={all(v["seam"]["pass_"] for v in variants)} globals={passed} converged={data["converged"]}')
    summary()
    summary(number,kinds)


def rounds(kind):
    return [json.loads(p.read_text()) for p in sorted((OUT/kind).glob('round-*.json'))]


def best(kind):
    values=rounds(kind);passing=[v for v in values if v['passing']]
    return max(passing or values,key=lambda x:(x['composite'],x['round'])) if values else None


def summary(number=None,kinds=CLASSES):
    canvas=Image.new('RGB',(1152,64+len(kinds)*352),'#141311');d=ImageDraw.Draw(canvas)
    try:font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',16)
    except OSError:font=ImageFont.load_default(size=16)
    for x,text in zip((16,304,592,880),('REFERENCE (fit)','PROCEDURAL 4x','POC ROUND 0 4x','BEST ROUND 4x')):d.text((x,16),text,font=font,fill='#C9BFA8')
    for row,kind in enumerate(kinds):
        y=64+row*352;chosen=best(kind)
        if number is not None:chosen=next((r for r in rounds(kind) if r['round']==number),chosen)
        ref=max(REFERENCES[kind],key=lambda t:t[1])[0]
        source=next(p for p in sorted((ROOT/'references').glob(f'ref-{ref:02}-*.jpg')) if not any(s in p.name for s in ('normal','roughness','displacement')))
        d.text((16,y),f'{kind} / ref {ref:02}',font=font,fill='#EFE7D2')
        paths=[source,OUT/kind/'procedural.png',OUT/kind/'round-00.png',OUT/kind/f'round-{chosen["round"]:02}.png' if chosen else None]
        for col,path in enumerate(paths):
            if path is None:continue
            im=Image.open(path).convert('RGBA')
            if col==0:im.thumbnail((256,256),Image.Resampling.LANCZOS)
            else:im=im.resize((256,256),Image.Resampling.NEAREST)
            canvas.paste(im,(16+col*288+(256-im.width)//2,y+28+(256-im.height)//2),im)
        values=rounds(kind)
        if values:
            label=f'r{chosen["round"]:02} score {chosen["composite"]:.3f}  '+(('VALID' if chosen['composite']>=.85 else 'VALID (<0.85)') if chosen['passing'] else 'FAILED ROUND')
            d.text((592,y+292),label,font=font,fill='#C9BFA8' if chosen['passing'] else '#E0982F')
            points=[(24+(r['round']-1)*44,y+336-r['composite']*36) for r in values]
            d.line((24,y+336-.85*36,508,y+336-.85*36),fill='#3B3733')
            if len(points)>1:d.line(points,fill='#E0982F',width=2)
            for x,yy in points:d.ellipse((x-3,yy-3,x+3,yy+3),fill='#E4C76A')
            d.text((16,y+285),'Score history (line = 0.85)',font=font,fill='#6B645C')
            if kind in ('decor','wall_torch'):d.text((592,y+315),'POC lacked a dedicated prop',font=font,fill='#6B645C')
    canvas.save(OUT/'summary.png' if number is None else ROOT/'.local/iteration-review.png')


def check():
    failures=[]
    images={k:tile(k) for k in CLASSES}
    current_global=globals_for(images)
    if not current_global['region_mean_pass'] or not current_global['hue_budget_pass']:
        failures.append('current shipped terrain composition fails global targets')
    for kind in CLASSES:
        chosen=best(kind)
        if not chosen or not chosen['passing']:failures.append(kind+': no passing round')
        if chosen:
            p=json.loads((PARAMS/(kind+'.json')).read_text())
            if p!=chosen['parameters']:failures.append(kind+': shipped parameters differ from best passing round')
            if tile(kind).tobytes()!=Image.open(OUT/kind/f'round-{chosen["round"]:02}.png').convert('RGBA').tobytes():failures.append(kind+': shipped pixels differ')
            if chosen['composite']<.85 and len(rounds(kind))<12:failures.append(kind+': stopped before convergence or round 12')
            for rendered_kind in ([kind,'door_open'] if kind=='door' else [kind]):
                for variant in range(3):
                    im=tile(rendered_kind,variant);m=measurements(im)
                    if im.size!=(64,64) or not m['opaque_pixels'] or m['palette_max_delta']>12 or m['std']<.08 or m['mean']>.45:
                        failures.append(f'{rendered_kind}/{variant}: current render fails validator')
                    if not seams(im,kind in ('decor','wall_torch'))['pass_']:failures.append(f'{rendered_kind}/{variant}: current seam failure')
            for r in rounds(kind):
                critique=OUT/kind/f'round-{r["round"]:02}.md'
                if not critique.exists() or not 1<=len(critique.read_text().strip().splitlines())<=3:failures.append(kind+': missing or overlong critique')
    for fail in failures:print('FAIL:',fail)
    print(f'Sewers lit composition: mean={current_global["region_mean_after_lighting"]:.4f}; dominant green-brown={current_global["dominant_green_brown_fraction"]:.4f}')
    print(f'TEST 39: classes={len(CLASSES)} failures={len(failures)}')
    return bool(failures)


if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--round',type=int);parser.add_argument('--class',dest='kind',choices=CLASSES);parser.add_argument('--summary',action='store_true');parser.add_argument('--check',action='store_true')
    args=parser.parse_args()
    if args.round:record(args.round,[args.kind] if args.kind else CLASSES)
    if args.summary:summary()
    if args.check:raise SystemExit(check())
