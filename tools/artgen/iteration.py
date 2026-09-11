"""Stage 5.7 parameter iteration and its specified JSON/PNG review deliverables.

Measurements are pixel statistics, not similarity to a photograph. Targets and
weights are transcribed from references.md and are never adjusted by the loop.
Board numeric scores retain the fixed sample with the runtime light equation.
Structural targets use unlit tiles; the global and six room gates use actual
OpenGL screenshot pixels and camera metadata. No gameplay is simulated here.
"""
import argparse
import hashlib
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from build import ROOT, COLORS
from rendered import tile, region_tile, CACHE
from validate import lab

CLASSES=('floor','wall','water','door','decor','wall_torch')
OUT=ROOT/'verification/iteration'
PARAMS=ROOT/'tools/artgen/blender/params'
LUMA=np.array([.2126,.7152,.0722])
REGION='sewers'
REGIONS={'sewers':dict(ambient=[.50,.55,.45],mean=.14), 'prison':dict(ambient=[.50,.50,.58],mean=.12),
 'caves':dict(ambient=[.60,.45,.35],mean=.15), 'city':dict(ambient=[.50,.55,.62],mean=.16),
 'halls':dict(ambient=[.40,.40,.52],mean=.09)}

def select_region(region):
    global REGION,OUT,PARAMS,REFERENCES,tile
    REGION=region
    if region=='sewers':return
    OUT=ROOT/'verification/iteration'/region;PARAMS=ROOT/'tools/artgen/blender/params'/region
    tile=lambda kind,variant=0:region_tile(region,kind,variant)
    REFERENCES={'floor':[(7,1),(21,.7)],'wall':[(7,1),(12,.5)],'door':[(10,.9),(12,.5)],
                'decor':[(10,.9),(11,.5),(12,.5)],'wall_torch':[(7,1),(9,.4)]}
    if region=='caves':REFERENCES={'floor':[(13,1),(21,.7)],'wall':[(13,1),(15,.6)],'door':[(13,1)],'decor':[(13,1),(14,.7)],'wall_torch':[(14,.7),(13,.6)]}
    elif region=='city':REFERENCES={'floor':[(16,1),(17,.7),(21,.7)],'wall':[(16,1),(17,.7)],'door':[(17,1),(16,.7)],'decor':[(16,1),(17,.7)],'wall_torch':[(17,1)]}
    elif region=='halls':REFERENCES={'floor':[(18,1),(21,.7)],'wall':[(18,1)],'door':[(18,1),(7,.6)],'decor':[(18,1),(7,.6)],'wall_torch':[(18,1),(7,.6)]}

def dominant_hues(h,s,rgb):
    if REGION=='sewers':return (h>=15)&(h<=125)
    chroma=np.linalg.norm(lab(rgb)[:,1:],axis=1)
    low,high={'prison':(25,65),'caves':(15,55),'city':(200,230),'halls':(200,240)}[REGION]
    return (chroma<(10 if REGION=='halls' else 8))|((h>=low)&(h<=high))
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
    light=np.broadcast_to(np.array(REGIONS[REGION]['ambient']),raw.shape).copy()
    for x,y,radius,color in [(3.5,3.5,8,[.45,.31,.12]),(3.5,.5,4 if REGION=='city' else 3,[.38,.24,.08])]:
        strength=np.maximum(0,1-np.hypot(xx-x,yy-y)/radius)**2
        light+=strength[:,:,None]*color
    if REGION=='halls':
        for y,line in enumerate(layout):
            for x,c in enumerate(line):
                if c=='a':
                    strength=np.maximum(0,1-np.hypot(xx-x-.5,yy-y-.5)/1.25)**2
                    light+=strength[:,:,None]*[.38,.25,.05]
    lit=raw*np.minimum(1,light)
    if REGION=='halls':
        for y,line in enumerate(layout):
            for x,c in enumerate(line):
                if c=='a':lit[y*64:(y+1)*64,x*64:(x+1)*64]=raw[y*64:(y+1)*64,x*64:(x+1)*64]
    return lit,raw,light


def globals_for(images):
    lit,raw,light=scene(images);h,s,v=hsv(lit*255)
    dominant=(h>=15)&(h<=125)
    # Neutral metal/black pixels do not constitute an additional coloured accent.
    accent_mask=(~dominant)&(s>=.10)&(v>.025)
    bins=np.bincount((h[accent_mask]//30).astype(int),minlength=12)/h.size
    present=bins>0
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
    if REGION!='sewers':return region_scores(kind,metrics,p,seam,images)
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


def region_scores(kind,m,p,seam,images):
    if REGION!='prison':return later_region_scores(kind,m,p,seam,images)
    room,_,_=scene(images);room_h,room_s,room_v=hsv(room*255)
    a=luminance(images[kind]);h,s,v=hsv(np.asarray(images[kind])[:,:,:3].astype(float));active=np.asarray(images[kind])[:,:,3]>0
    metal=images['decor'] if kind=='wall' else images[kind]
    metal_rgb=np.asarray(metal);metal_lum=luminance(metal);mh,ms,mv=hsv(metal_rgb[:,:,:3].astype(float))
    iron=(metal_rgb[:,:,3]>0)&(metal_lum>=.04)&(ms<.22)
    if kind=='door':
        material=Image.open(CACHE/REGION/'door_0_metal.png').crop((64,64,128,128)).convert('L')
        iron=(np.asarray(material)>127)&(ms<.22)
    rust=active&(h>=10)&(h<=35)&(s>=.22)
    iron_mean=float(metal_lum[iron].mean()) if iron.any() else 0
    stone=measurements(images['wall']);light=images['wall_torch'];l=np.asarray(light);fl=luminance(light)
    flame=(l[:,:,3]>0)&(fl>.60)
    rows_by_number={
        7:([target('stone hue',m['hue'],30,50),target('stone saturation',m['saturation'],high=.15)] if kind in ('floor','wall') else [])+
          ([target('light source frame fraction',float(flame.mean()),high=.05)] if kind=='wall_torch' else []),
        9:[target('room gold accent fraction',float(((room_h>=20)&(room_h<=65)&(room_s>.3)&(room_v>.3)).mean()),high=.08)],
        10:[target('iron luminance',iron_mean,.08,.20)]+([target('rust hue',float(np.median(h[rust])),15,30)] if rust.any() else []),
        11:[],12:[target('iron/stone luminance gap',abs(stone['mean']-iron_mean),.15)],
        21:[target('2x2 seam pass',int(seam['pass_']),1)]}
    rows=[]
    for number,weight in REFERENCES[kind]:
        targets=rows_by_number[number]
        # Qualitative-only rows inform vision and are not assigned invented scores.
        if targets:rows.append(dict(reference=number,weight=weight,targets=targets,score=float(np.mean([t['score'] for t in targets]))))
    return rows,float(sum(r['weight']*r['score'] for r in rows)/sum(r['weight'] for r in rows))


def later_region_scores(kind,m,p,seam,images):
    room,_,_=scene(images);rh,rs,rv=hsv(room*255);a=luminance(images[kind])
    rgb=np.asarray(images[kind])[:,:,:3];h,s,v=hsv(rgb.astype(float));active=np.asarray(images[kind])[:,:,3]>0
    timber=active&(h>=15)&(h<=55)&(s>.20)
    rows={21:[target('2x2 seam pass',int(seam['pass_']),1)]}
    if REGION=='caves':
        rows[13]=[target('rock saturation',m['saturation'],high=.12)] if kind in ('floor','wall') else [
            target('timber hue',float(np.median(h[timber])) if timber.any() else 0,20,35),
            target('timber saturation',float(np.median(s[timber])) if timber.any() else 0,.30,.50)]
        rows[14]=[target('highlight peak',float(a.max()),.70),target('highlight frame fraction',float((a>=.70).mean()),high=.04)]
        rows[15]=[target('lower-third depth',float(a[43:].mean()),high=.06)]
    elif REGION=='city':
        rows[16]=[target('pattern repeats per edge',p.get('pattern_repeats',2),2)] if kind=='floor' else []
        # The runtime ambient is a source colour; ground warmth is measured in the fixed lit room.
        ambient=np.array(REGIONS[REGION]['ambient']);ah,_,_=hsv(ambient*255)
        warm=(rh>=15)&(rh<=65)&(rs>.10)
        rows[17]=[target('ambient hue',float(ah),200,230),target('warm ground hue',float(np.median(rh[warm])) if warm.any() else 0,30,45)]
        rows[18]=[target('dark pixels in room',float((room@LUMA<.08).mean()),.80)]
    else:
        # Bone props occupy a small part of the room, not 100% of their isolated crop.
        rows[18]=[target('dark stone mean',m['mean'],.06,.12)] if kind in ('floor','wall') else [target('lit room mean',float((room@LUMA).mean()),.06,.12)]
        rows[7]=[target('bone room fraction',float(((room@LUMA>=.55)&(rs<.30)).mean()),high=.10)]
    result=[]
    for number,weight in REFERENCES[kind]:
        targets=rows[number]
        if targets:result.append(dict(reference=number,weight=weight,targets=targets,score=float(np.mean([t['score'] for t in targets]))))
    return result,float(sum(r['weight']*r['score'] for r in result)/sum(r['weight'] for r in result))


def bits(image):
    gray=np.array(image.convert('L').resize((32,32),Image.Resampling.LANCZOS),dtype=float)
    n=np.arange(32);k=np.arange(8)[:,None];basis=np.cos(np.pi*(2*n+1)*k/64)
    spectrum=basis@gray@basis.T
    return spectrum>np.median(spectrum.flatten()[1:])


def luminance(image):
    return np.asarray(image.convert('RGB'),dtype=float)@LUMA/255


def edge_features(image):
    a=luminance(image.filter(ImageFilter.GaussianBlur(.7)))
    gy,gx=np.gradient(a);weight=np.hypot(gx,gy)
    # Edge direction, not gradient direction. Nine fixed 20-degree bins.
    angle=(np.arctan2(gy,gx)+np.pi/2)%np.pi
    histogram=np.histogram(angle,bins=np.linspace(0,np.pi,10),weights=weight)[0]
    histogram=histogram/max(1e-9,histogram.sum())
    horizontal=float(weight[(angle<=np.pi/8)|(angle>=7*np.pi/8)].sum()/max(1e-9,weight.sum()))
    raw=luminance(image);lap=-4*raw+np.roll(raw,1,0)+np.roll(raw,-1,0)+np.roll(raw,1,1)+np.roll(raw,-1,1)
    return dict(orientation_histogram=histogram.tolist(),peak=float(histogram.max()),horizontal=horizontal,laplacian_variance=float(lap.var()))


def stone_components(image):
    a=luminance(image);lo,hi=np.percentile(a,[5,80]);threshold=lo+.45*(hi-lo)
    mask=a>threshold;h,w=mask.shape;visited=np.zeros_like(mask);parts=[]
    # Nearest sampling can join separate slabs by a one-pixel neck. Separate
    # eroded cores, then grow them back within the original stone pixels.
    core=mask.copy()
    for dy,dx in ((1,0),(-1,0),(0,1),(0,-1)):core&=np.roll(np.roll(mask,dy,0),dx,1)
    for y,x in zip(*np.where(core)):
        if visited[y,x]:continue
        points=[];stack=[(y,x)];visited[y,x]=True
        while stack:
            yy,xx=stack.pop();points.append((yy,xx))
            for dy,dx in ((1,0),(-1,0),(0,1),(0,-1)):
                ny,nx=(yy+dy)%h,(xx+dx)%w
                if core[ny,nx] and not visited[ny,nx]:visited[ny,nx]=True;stack.append((ny,nx))
        if len(points)>=h*w*.01:parts.append(np.array(points))
    from collections import deque
    labels=np.zeros((h,w),dtype=int);queue=deque()
    for label,part in enumerate(parts,1):
        for y,x in part:labels[y,x]=label;queue.append((y,x))
    while queue:
        y,x=queue.popleft()
        for dy,dx in ((1,0),(-1,0),(0,1),(0,-1)):
            ny,nx=(y+dy)%h,(x+dx)%w
            if mask[ny,nx] and not labels[ny,nx]:labels[ny,nx]=labels[y,x];queue.append((ny,nx))
    parts=[np.argwhere(labels==label) for label in range(1,len(parts)+1)]
    areas=[len(p)/(h*w) for p in parts]
    def span(values,n):
        v=np.unique(values);gaps=np.diff(np.r_[v,v[0]+n]);return float((n-gaps.max()+1)/n)
    spans=[max(span(p[:,0],h),span(p[:,1],w)) for p in parts]
    # Joint width measured away from junctions: median distance to nearest stone.
    distance=np.zeros_like(a);remaining=~mask;front=mask.copy()
    for depth in range(1,10):
        front=front|np.roll(front,1,0)|np.roll(front,-1,0)|np.roll(front,1,1)|np.roll(front,-1,1)
        added=remaining&front;distance[added]=depth;remaining[added]=False
    return dict(areas=areas,median_area=float(np.median(areas)) if areas else 0,
                area_ratio=max(areas)/min(areas) if areas else 0,largest_span=max(spans,default=0),
                mortar_width=float(2*np.median(distance[~mask])/w) if (~mask).any() else 0,
                mortar_contrast=float(a[mask].mean()-a[~mask].mean()) if mask.any() and (~mask).any() else 0,
                stone_count=len(parts))


def structural(kind,image,images,p):
    m=measurements(image);e=edge_features(image);a=luminance(image);targets=[];details={}
    if kind=='floor' and REGION=='caves':
        targets=[target('earth/rubble edge orientation peak',e['peak'],high=.30),target('dry specular fraction',m['highlights_075'],high=.01)]
    elif kind=='floor' and REGION=='city':
        details=stone_components(image)
        targets=[target('pattern repeats per edge',p['pattern_repeats'],2),target('visible mineral wear',float(a.std()),.08)]
    elif kind=='floor':
        details=stone_components(image)
        targets=[target('stones across tile edge',p['count'],2,3),target('largest stone span',details['largest_span'],.40),
                 target('stone area size ratio',details['area_ratio'],1.5 if REGION=='prison' else 2),target('mortar width / edge',details['mortar_width'],high=.0625 if REGION=='halls' else .06),
                 target('mortar luminance separation',details['mortar_contrast'],.07 if REGION=='halls' else .15,.25),target('orientation histogram peak',e['peak'],high=.30)]
    elif kind=='wall' and REGION=='caves':
        targets=[target('fracture orientation peak',e['peak'],high=.35),target('wall / floor pHash bits',int(np.count_nonzero(bits(image)!=bits(images['floor']))),12)]
    elif kind=='wall':
        body=float(a[13:45].mean());top=float(a[:13].mean());damp=float(a[45:].mean())
        details=dict(top=top,body=body,damp=damp)
        targets=[target('horizontal edge energy',e['horizontal'],.55),target('top band minus body',top-body,.15),
                 target('body minus damp band',body-damp,.025 if REGION=='halls' else .10),target('wall / floor pHash bits',int(np.count_nonzero(bits(image)!=bits(images['floor']))),12)]
    elif kind=='water':
        edge=np.ones(a.shape,dtype=bool);edge[8:-8,8:-8]=False
        depth=float(a[edge].mean()-a[24:40,24:40].mean())
        floor_energy=edge_features(images['floor'])['laplacian_variance']
        details=dict(depth=depth,floor_laplacian_variance=floor_energy)
        targets=[target('high frequency / floor',e['laplacian_variance']/max(1e-9,floor_energy),high=.25),target('edge minus centre depth',depth,.08),
                 target('specular >= .70 fraction',float((a>=.70).mean()),.01,.04),target('water hue',m['hue'],80,110),
                 target('water saturation',m['saturation'],.20,.35),target('water mean',m['mean'],high=.12),
                 target('water / grass pHash bits',int(np.count_nonzero(bits(image)!=bits(tile('grass')))),16)]
    else:
        baseline=json.loads((OUT/kind/'round-00.json').read_text())['parameters']
        keys=('seed','count','relief','displacement','wall_height','mortar_width','chip','bevel','decor_density','water_depth','light_angle','light_strength')
        same=all(p.get(k)==baseline.get(k) for k in keys)
        targets=[target('approved shape parameters unchanged',int(same),1)]
    if REGION=='prison' and kind in ('floor','wall'):
        targets.append(target('dry specular fraction',float((a>=.70).mean()),high=.01))
    return dict(targets=targets,details=details,edges=e,passing=all(t['pass_'] for t in targets),score=float(np.mean([t['score'] for t in targets])))


def room_gate(annotate=False):
    metadata=json.loads((OUT/'room.json').read_text());image=Image.open(OUT/'sewers-ingame.png').convert('RGB')
    terrain=Image.open(OUT/'sewers-terrain.png').convert('RGB');a=luminance(image);base=luminance(terrain)
    cells=metadata['cells'];samples={};visible=np.zeros(a.shape,dtype=bool)
    for cell in cells:
        x0,y0,x1,y1=cell['box'];x0=max(0,x0);y0=max(0,y0);x1=min(a.shape[1],x1);y1=min(a.shape[0],y1)
        if x0>=x1 or y0>=y1:continue
        cell['box']=[x0,y0,x1,y1];visible[y0:y1,x0:x1]=True
        crop=terrain.crop((x0,y0,x1,y1));value=float(luminance(crop).mean())
        h,s,v=hsv(np.array(crop,dtype=float));chromatic=(s>.1)&(v>.02)
        hue=float(np.degrees(np.angle(np.mean(np.exp(1j*np.radians(h[chromatic]))))))%360 if chromatic.any() else 0
        samples[(cell['x'],cell['y'])]=dict(cell=cell,image=crop,mean=value,hue=hue)
    walls=[s for s in samples.values() if s['cell']['kind']=='wall'];floors=[s for s in samples.values() if s['cell']['kind']=='floor']
    if not walls or not floors:raise ValueError('Room lacks visible floor or wall samples')
    wall_mean=float(np.mean([s['mean'] for s in walls]));floor_mean=float(np.mean([s['mean'] for s in floors]))
    # Representative cells selected by closeness to their class median, never by best contrast.
    wall=min(walls,key=lambda s:abs(s['mean']-np.median([x['mean'] for x in walls])))
    floor=min(floors,key=lambda s:abs(s['mean']-np.median([x['mean'] for x in floors])))
    phash=int(np.count_nonzero(bits(wall['image'])!=bits(floor['image'])))
    areas=[stone_components(s['image'])['median_area'] for s in floors]
    adjacency=[]
    for (x,y),s in samples.items():
        if s['cell']['kind']!='water':continue
        for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)):
            other=samples.get((x+dx,y+dy))
            if other and other['cell']['kind']=='floor':
                delta=abs(s['hue']-other['hue']);adjacency.append((abs(s['mean']-other['mean']),min(delta,360-delta)))
    if not adjacency:raise ValueError('No adjacent visible water and floor')
    coords=set(samples);outer={c for c in coords if any((c[0]+dx,c[1]+dy) not in coords for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)))}
    inner={c for c in coords-outer if any((c[0]+dx,c[1]+dy) in outer for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)))}
    outer_mean=float(np.mean([samples[c]['mean'] for c in outer]));inner_mean=float(np.mean([samples[c]['mean'] for c in inner]))
    readability=[]
    for subject in metadata['subjects']:
        x0,y0,x1,y1=subject['box'];difference=np.abs(a[y0:y1,x0:x1]-base[y0:y1,x0:x1])
        contrast=float(np.percentile(difference,95));readability.append(dict(name=subject['name'],contrast=contrast,pass_=contrast>=.20))
    groups=[dict(name='Wall/floor distinctness',measurements=[target('mean luminance difference',abs(wall_mean-floor_mean),.02 if REGION=='halls' else .10),target('representative pHash bits',phash,12)]),
            dict(name='Feature scale at 1x',measurements=[target('median flagstone fraction',float(np.median(areas)),.12)]),
            dict(name='Adjacent water/floor contrast',measurements=[target('median luminance difference',float(np.median([x[0] for x in adjacency])),.04 if REGION=='halls' else .12),target('median hue difference',float(np.median([x[1] for x in adjacency])),30)]),
            dict(name='No boundary halo',measurements=[target('outer minus inner mean',outer_mean-inner_mean,high=0)],outer_mean=outer_mean,inner_mean=inner_mean),
            dict(name='Visible-cell value range',measurements=[target('visible mean',float(a[visible].mean()),.06 if REGION=='halls' else .12,.12 if REGION=='halls' else .20),target('visible std',float(a[visible].std()),.09 if REGION=='halls' else .12)]),
            dict(name='Hero / rat / item readability',measurements=[target(x['name']+' bbox contrast',x['contrast'],.20) for x in readability])]
    for g in groups:g['passing']=all(m['pass_'] for m in g['measurements'])
    terrain_rgb=np.array(terrain,dtype=float)[visible];hue,sat,val=hsv(terrain_rgb);dominant=dominant_hues(hue,sat,terrain_rgb)
    accent=(~dominant)&(sat>=.10)&(val>.025)
    bins=np.bincount((hue[accent]//30).astype(int),minlength=12)/max(1,len(hue))
    # Sub-per-mille quantization fringes are not a visible colour family. Keep
    # every pixel in the independent 70% dominant-family coverage measurement.
    present=bins>0
    families=[]
    for start in range(12):
        if present[start] and not present[(start-1)%12]:
            family=0.;index=start
            while present[index]:
                family+=bins[index];index=(index+1)%12
            families.append(family)
    if present.all():families=[float(bins.sum())]
    accents=int(sum(f>=.001 for f in families))
    mean=float(a[visible].mean());desired=REGIONS[REGION]['mean'];globals_=dict(region_mean_after_lighting=mean,region_mean_pass=desired-.03<=mean<=desired+.03,
        dominant_green_brown_fraction=float(dominant.mean()),hue_budget_pass=float(dominant.mean())>=.70 and accents<=1,accent_families=accents,accent_bins=bins.tolist(),
        readability='Existing mobs unchanged; sprite-pair histogram test 30 remains pending',composition='Actual OpenGL screenshot; all heroFOV visible cells',ambient=REGIONS[REGION]['ambient'])
    result=dict(passing=all(g['passing'] for g in groups),groups=groups,global_targets=globals_,
                screenshot_sha256=hashlib.sha256((OUT/'sewers-ingame.png').read_bytes()).hexdigest(),
                terrain_sha256=hashlib.sha256((OUT/'sewers-terrain.png').read_bytes()).hexdigest(),
                sample_count=len(samples),seed=metadata['seed'],lighting=metadata['lighting'],zoom=metadata['zoom'],
                method='Full visible-cell mask; class-median representative pHashes; all water/floor adjacencies; 4-neighbour rings; p95 paired-background absolute bbox luminance contrast')
    if annotate:
        canvas=Image.new('RGB',(image.width,image.height+220),'#141311');canvas.paste(image,(0,0));draw=ImageDraw.Draw(canvas)
        font=review_font(16)
        for i,g in enumerate(groups):
            line=f'{i+1}. {g["name"]}: '+', '.join(f'{m["target"]}={m["value"]:.3f}' for m in g['measurements'])+(' PASS' if g['passing'] else ' FAIL')
            draw.text((16,image.height+12+i*32),line,font=font,fill='#C9BFA8' if g['passing'] else '#E0982F')
        canvas.save(OUT/'sewers-gate.png');(OUT/'room-gate.json').write_text(json.dumps(result,indent=2)+'\n')
    return result


def review_font(size):
    try:return ImageFont.truetype('C:/Windows/Fonts/consola.ttf',size)
    except OSError:return ImageFont.load_default(size=size)


def reference(kind):
    number=max(REFERENCES[kind],key=lambda r:r[1])[0]
    path=next(p for p in sorted((ROOT/'references').glob(f'ref-{number:02}-*')) if p.suffix.lower() in ('.jpg','.png') and not any(s in p.name for s in ('normal','roughness','displacement')))
    image=Image.open(path).convert('RGBA');box=(0,0,*image.size)
    if kind=='wall_torch':
        rgba=np.array(image);mask=(rgba[:,:,:3].max(axis=2)>35)&(rgba[:,:,3]>0)
        yy,xx=np.where(mask);box=(int(xx.min()),int(yy.min()),int(xx.max()+1),int(yy.max()+1))
        image=image.crop(box)
    elif kind=='decor':image=image.crop(image.getbbox());box=(0,0,*image.size)
    return image,dict(number=number,file=path.relative_to(ROOT).as_posix(),subject_box=box,sha256=hashlib.sha256(path.read_bytes()).hexdigest())


def rounds(kind):
    return [json.loads(p.read_text()) for p in sorted((OUT/kind).glob('round-*.json')) if p.stem!='round-00']


def best(kind):
    values=rounds(kind);passing=[r for r in values if r['passing']]
    return max(passing or values,key=lambda r:(r['composite'],r['round'])) if values else None


def summary(preview=False,kinds=CLASSES):
    canvas=Image.new('RGB',(1152,64+len(kinds)*384),'#141311');draw=ImageDraw.Draw(canvas);font=review_font(16)
    for x,label in zip((16,304,592,880),('REFERENCE SUBJECT','PROCEDURAL 4x','APPROVED PASS / R0','CURRENT' if preview else 'BEST PASSING 4x')):draw.text((x,16),label,font=font,fill='#C9BFA8')
    for row,kind in enumerate(kinds):
        y=64+row*384;r=best(kind);ref,info=reference(kind)
        images=[ref,Image.open(OUT/kind/'procedural.png').convert('RGBA'),Image.open(OUT/kind/'round-00.png').convert('RGBA'),tile(kind) if preview or not r else Image.open(OUT/kind/f'round-{r["round"]:02}.png').convert('RGBA')]
        draw.text((16,y),f'{kind} / ref {info["number"]:02}',font=font,fill='#EFE7D2')
        for col,im in enumerate(images):
            im=im.copy()
            if col==0:im.thumbnail((256,256),Image.Resampling.LANCZOS)
            else:im=im.resize((256,256),Image.Resampling.NEAREST)
            canvas.paste(im,(16+col*288+(256-im.width)//2,y+28+(256-im.height)//2),im)
        values=rounds(kind);first=next((v['round'] for v in values if v['vision']['score']>.7),None)
        if r:
            draw.text((16,y+290),f'vision first >0.7: {first if first else "not reached"}',font=font,fill='#C9BFA8')
            draw.text((592,y+290),f'r{r["round"]:02} composite={r["composite"]:.3f} '+('PASS' if r['passing'] else 'FAIL'),font=font,fill='#C9BFA8')
            for key,color in [('composite','#E0982F')]:
                points=[(24+(v['round']-1)*34,y+362-v[key]*45) for v in values]
                draw.line((24,y+362-.90*45,466,y+362-.90*45),fill='#6B645C')
                if len(points)>1:draw.line(points,fill=color,width=2)
                for x,yy in points:draw.ellipse((x-2,yy-2,x+2,yy+2),fill='#E4C76A')
    canvas.save(ROOT/'.local/iteration-review.png' if preview else OUT/'summary.png')


def record(number,kinds,vision_path):
    if not 1<=number<=14:raise ValueError('Calibrated rounds must be 1-14')
    judgments=json.loads(Path(vision_path).read_text());images={k:tile(k) for k in CLASSES};room=room_gate(True)
    import io,contextlib
    from validate import validate
    output=io.StringIO()
    with contextlib.redirect_stdout(output):atlas_failed=validate(True)
    atlas=dict(passing=not atlas_failed,output=output.getvalue().strip().splitlines())
    room_folder=OUT/'rooms';room_folder.mkdir(exist_ok=True)
    for suffix,source in [('.png','sewers-ingame.png'),('-terrain.png','sewers-terrain.png'),('.json','room.json')]:
        (room_folder/f'round-{number:02}{suffix}').write_bytes((OUT/source).read_bytes())
    for kind in kinds:
        folder=OUT/kind;path=folder/f'round-{number:02}.json'
        if path.exists():raise ValueError('Round already recorded: '+str(path))
        vision=judgments[kind]
        if not 0<=vision['score']<=1 or not vision['reason'].strip():raise ValueError('Vision score and one-line reason required')
        if vision.get('wrong_kind') and vision['score']>.3:raise ValueError('Wrong-kind vision score must be <= .3')
        p=json.loads((PARAMS/f'{kind}.json').read_text());variants=[]
        for variant in range(3):
            im=tile(kind,variant);m=measurements(im);seam=seams(im,kind in ('decor','wall_torch'));structure=structural(kind,im,images,p)
            valid=im.size==(64,64) and m['opaque_pixels']>0 and m['palette_max_delta']<=12 and m['std']>=.08 and m['mean']<=.45
            variants.append(dict(variant=variant,metrics=m,seam=seam,structural=structure,validator_pass=bool(valid)))
        rows,numeric=scores(kind,variants[0]['metrics'],p,variants[0]['seam'],images)
        structure_score=float(np.mean([v['structural']['score'] for v in variants]));score=.30*numeric+.30*structure_score+.40*vision['score']
        g=room['global_targets'];passed=all(v['validator_pass'] and v['seam']['pass_'] and v['structural']['passing'] for v in variants) and room['passing'] and g['region_mean_pass'] and g['hue_budget_pass'] and atlas['passing']
        _,ref=reference(kind)
        data=dict(asset_class=kind,round=number,parameters=p,references=rows,reference_subject=ref,vision=vision,
                  weights=dict(numeric=.30,structural=.30,vision=.40),numeric=numeric,structural_score=structure_score,composite=score,
                  variants=variants,room=room,atlas_validator=atlas,global_targets=g,passing=bool(passed),converged=bool(passed and score>=.90 and number>=6),
                  cache_sha256={f'{"tiles" if REGION=="sewers" else REGION}/{kind}_{i}.png':hashlib.sha256((CACHE/('tiles' if REGION=='sewers' else REGION)/f'{kind}_{i}.png').read_bytes()).hexdigest() for i in range(3)})
        path.write_text(json.dumps(data,indent=2)+'\n');images[kind].save(folder/f'round-{number:02}.png')
        retained=ROOT/'.local/iteration-cache'/REGION/kind/f'round-{number:02}';retained.mkdir(parents=True,exist_ok=True)
        for cached_kind in ([kind,'door_open'] if kind=='door' else [kind]):
            for variant in range(3):
                filename=f'{cached_kind}_{variant}.png';(retained/filename).write_bytes((CACHE/('tiles' if REGION=='sewers' else REGION)/filename).read_bytes())
                mask=CACHE/REGION/f'{cached_kind}_{variant}_metal.png'
                if mask.exists():(retained/mask.name).write_bytes(mask.read_bytes())
        critique=f'Vision {vision["score"]:.2f}: {vision["reason"]}\n'+vision.get('gap','Material/value refinement follows the measured failures.')+'\n'
        if len(critique.strip().splitlines())>3:raise ValueError('Critique must fit three lines')
        (folder/f'round-{number:02}.md').write_text(critique)
        print(f'{kind} r{number:02}: composite={score:.3f} vision={vision["score"]:.2f} passing={passed}; structural failures='+str([t['target'] for v in variants for t in v['structural']['targets'] if not t['pass_']]))
    summary(kinds=[k for k in CLASSES if REGION=='sewers' or k!='water'])


def check(room_only=False):
    failures=[];room=room_gate(True);images={k:tile(k) for k in CLASSES}
    for g in room['groups']:
        print(g['name']+': '+', '.join(f'{m["target"]}={m["value"]:.4f}' for m in g['measurements'])+(' PASS' if g['passing'] else ' FAIL'))
        if not g['passing']:failures.append(g['name'])
    print(f'TEST {40 if REGION=="sewers" else 42}: region={REGION} measurements=6 failures={len(failures)}')
    if room_only:return bool(failures)
    class_failures=[]
    for kind in (k for k in CLASSES if k!='water'):
        values=rounds(kind);chosen=best(kind)
        if len(values)<6:class_failures.append(kind+': fewer than six rounds')
        if not chosen or not chosen['passing']:class_failures.append(kind+': no round passes all required gates')
        p=json.loads((PARAMS/f'{kind}.json').read_text())
        if chosen:
            if p!=chosen['parameters']:class_failures.append(kind+': current parameters differ from selected round')
            if tile(kind).tobytes()!=Image.open(OUT/kind/f'round-{chosen["round"]:02}.png').convert('RGBA').tobytes():class_failures.append(kind+': selected pixels differ')
            if chosen['composite']<.90 and len(values)<14:class_failures.append(kind+': stopped below .90 before round 14')
        for rendered_kind in ([kind,'door_open'] if kind=='door' else [kind]):
            for v in range(3):
                im=tile(rendered_kind,v);m=measurements(im)
                if im.size!=(64,64) or not m['opaque_pixels'] or m['palette_max_delta']>12 or m['std']<.08 or m['mean']>.45 or not seams(im,kind in ('decor','wall_torch'))['pass_'] or not structural(kind,im,images,p)['passing']:class_failures.append(f'{rendered_kind}/{v}: current validation, structure or seams fail')
        for r in values:
            note=OUT/kind/f'round-{r["round"]:02}.md'
            if not note.exists() or not 1<=len(note.read_text().strip().splitlines())<=3:class_failures.append(kind+': missing/overlong critique')
            if r['vision'].get('wrong_kind') and r['vision']['score']>.3:class_failures.append(kind+': wrong-kind score exceeds .3')
    if not room['global_targets']['region_mean_pass'] or not room['global_targets']['hue_budget_pass']:class_failures.append('current screenshot fails board global targets')
    locks=json.loads((ROOT/'tools/artgen/blender/locks.json').read_text())
    for name,digest in locks['sha256'].items():
        path=ROOT/name;raw=path.read_text().encode() if path.suffix=='.json' else path.read_bytes()
        if hashlib.sha256(raw).hexdigest()!=digest:class_failures.append('approved asset changed: '+name)
    for f in class_failures:print('FAIL:',f)
    print(f'TEST 39: region={REGION} classes=5 failures={len(class_failures)}; liquids use test 41')
    return bool(failures or class_failures)


def liquid_hash(x,y):
    value=((x*0x1f123bb5)^(y*0x5f356495))&0xffffffff
    value^=value>>16;value=(value*0x45d9f3b)&0xffffffff;return value^(value>>16)


def liquid_phase(x,y):return (x&1)|((y&1)<<1)|((liquid_hash(x,y)&1)<<2)


def liquid_metrics():
    from rendered import liquid_frame
    result={};failures=[]
    for kind in ('sewage','water','lava'):
        frames=[];hashes=[]
        for v in range(4):
            for f in range(8):
                im=liquid_frame(kind,v,f);a=luminance(im);m=measurements(im)
                mask=a>=.70;visited=np.zeros(mask.shape,dtype=bool);streaks=[]
                for y,x in zip(*np.where(mask)):
                    if visited[y,x]:continue
                    stack=[(y,x)];visited[y,x]=True;points=[]
                    while stack:
                        yy,xx=stack.pop();points.append((yy,xx))
                        for dy in (-1,0,1):
                            for dx in (-1,0,1):
                                ny,nx=yy+dy,xx+dx
                                if 0<=ny<64 and 0<=nx<64 and mask[ny,nx] and not visited[ny,nx]:visited[ny,nx]=True;stack.append((ny,nx))
                    if len(points)>=2:
                        eigen=np.linalg.eigvalsh(np.cov(np.array(points).T));aspect=float(np.sqrt((eigen[-1]+.1)/(eigen[0]+.1)))
                        streaks.append(dict(pixels=len(points),aspect=aspect))
                edge=np.ones(a.shape,dtype=bool);edge[8:-8,8:-8]=False
                center=np.zeros(a.shape,dtype=bool);center[24:40,24:40]=True
                base=a<.25;depth=float(a[edge&base].mean()-a[center&base].mean())
                targets=[target('mean',m['mean'],.10,.18),target('specular fraction',float(mask.mean()),.01,.04),
                    target('open streak count',len(streaks),3),target('streak aspect',min((s['aspect'] for s in streaks),default=0),3),
                    target('depth',depth,.02,.04),target('palette delta',m['palette_max_delta'],high=12),target('tile std',m['std'],.08)]
                low,high={'sewage':(80,110),'water':(190,215),'lava':(10,25)}[kind]
                targets.append(target('hue',m['hue'],low,high))
                if kind=='sewage':targets.append(target('saturation',m['saturation'],.20,.35))
                bad=[t['target'] for t in targets if not t['pass_']]
                if bad:failures.append(f'{kind}/{v}/{f}: '+', '.join(bad))
                frames.append(dict(variant=v,frame=f,targets=targets,metrics=m,streaks=streaks))
        for f in range(8):
            for v in range(4):
                for w in range(v+1,4):hashes.append(int(np.count_nonzero(bits(liquid_frame(kind,v,f))!=bits(liquid_frame(kind,w,f)))))
        correlations=[]
        for tick in range(8):
            for origin in (0,7,19):
                grid=np.zeros((192,192))
                for y in range(3):
                    for x in range(3):
                        xx,yy=x+origin,y+origin;v=(liquid_hash(xx,yy)>>8)&3;f=(tick+liquid_phase(xx,yy))&7
                        grid[y*64:y*64+64,x*64:x*64+64]=luminance(liquid_frame(kind,v,f))
                correlations.extend([float(np.corrcoef(grid[:,:-64].ravel(),grid[:,64:].ravel())[0,1]),float(np.corrcoef(grid[:-64,:].ravel(),grid[64:,:].ravel())[0,1])])
        if min(hashes)<10:failures.append(kind+': variant pHash below 10')
        if max(correlations)>.35:failures.append(kind+': one-tile autocorrelation above .35')
        result[kind]=dict(frames=frames,min_variant_phash=min(hashes),max_one_tile_autocorrelation=max(correlations))
    return result,failures


def liquid_review(number=None,vision_path=None,preview=False):
    from rendered import liquid_frame
    folder=OUT/'liquids';folder.mkdir(exist_ok=True)
    if preview or number:
        canvas=Image.new('RGB',(1024,848),'#141311');draw=ImageDraw.Draw(canvas)
        for row,kind in enumerate(('sewage','water','lava')):
            draw.text((8,row*280+4),kind+' / four variants at frame 0; frames 0-7 below',font=review_font(16),fill='#C9BFA8')
            for v in range(4):canvas.paste(liquid_frame(kind,v,0).resize((160,160),Image.Resampling.NEAREST),(v*256,row*280+30))
            for f in range(8):canvas.paste(liquid_frame(kind,0,f),(f*128,row*280+206))
        canvas.save(ROOT/'.local/liquid-review.png' if preview else folder/f'round-{number:02}.png')
    if preview:return False
    metrics,failures=liquid_metrics()
    if number:
        vision=json.loads(Path(vision_path).read_text())
        note=f'Vision {vision["score"]:.2f}: {vision["reason"]}\nDoes this read as liquid? {vision["reads_as_liquid"]}. Is any feature visibly repeating? {vision["feature_repeats"]}.\n'
        (folder/f'round-{number:02}.md').write_text(note)
        if not vision['reads_as_liquid'] or vision['feature_repeats']:failures.append('hard room vision gate')
        record=dict(round=number,vision=vision,metrics=metrics,failures=failures,passing=not failures,
            screenshot_sha256=hashlib.sha256((OUT/'sewers-ingame.png').read_bytes()).hexdigest(),
            room_sha256={name:hashlib.sha256((ROOT/name).read_bytes()).hexdigest() for name in vision.get('rooms',[])},
            cache_sha256={p.relative_to(CACHE).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (CACHE/'liquids').rglob('*.png')},
            autocorrelation_method='Actual runtime 3x3 variant/phase composition at eight times and three origins; identical copies necessarily correlate 1 and are not the runtime tiling.')
        (folder/f'round-{number:02}.json').write_text(json.dumps(record,indent=2)+'\n')
    else:
        paths=sorted(folder.glob('round-*.json'))
        if not paths:failures.append('missing room vision critique')
        else:
            record=json.loads(paths[-1].read_text());vision=record['vision']
            if not vision['reads_as_liquid'] or vision['feature_repeats']:failures.append('hard room vision gate')
            for name,digest in record.get('room_sha256',{}).items():
                if hashlib.sha256((ROOT/name).read_bytes()).hexdigest()!=digest:failures.append('unreviewed liquid room '+name)
            for name,digest in record['cache_sha256'].items():
                if hashlib.sha256((CACHE/name).read_bytes()).hexdigest()!=digest:failures.append('unreviewed liquid cache '+name)
    for kind,m in metrics.items():print(f'{kind}: min variant pHash={m["min_variant_phash"]}, max one-tile autocorrelation={m["max_one_tile_autocorrelation"]:.3f}')
    for failure in failures:print('FAIL:',failure)
    print(f'TEST 41: liquid frames=96 failures={len(failures)}')
    return bool(failures)


if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--round',type=int);parser.add_argument('--class',dest='kinds',choices=CLASSES,nargs='+')
    parser.add_argument('--vision');parser.add_argument('--summary',action='store_true');parser.add_argument('--preview',action='store_true');parser.add_argument('--check',action='store_true');parser.add_argument('--check-room',action='store_true')
    parser.add_argument('--liquids',action='store_true');parser.add_argument('--region',choices=list(REGIONS),default='sewers');args=parser.parse_args()
    select_region(args.region)
    if args.liquids:raise SystemExit(liquid_review(args.round,args.vision,args.preview))
    kinds=args.kinds or [k for k in CLASSES if REGION=='sewers' or k!='water']
    if args.preview:summary(True,kinds)
    if args.round:record(args.round,kinds,args.vision)
    if args.summary:summary(kinds=kinds)
    if args.check or args.check_room:raise SystemExit(check(args.check_room))


