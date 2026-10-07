"""Named quarry skins, fitted to their species' unchanged animation rectangles."""
from functools import lru_cache
from PIL import Image
from monsters import CONTRACT, HERE, blank_atlas, rectangle, pose, pose_choice, split_poses

WANTED = [('jack','skeleton'),('nails','thief'),('voss','guard'),
          ('widowmaker','dm100'),('bone_clerk','necromancer'),('morcant','guard')]


def spec(base):
    return next(s for s in CONTRACT['monsters'] if s['name']==base)


@lru_cache(None)
def source_poses(name):
    source=Image.open(HERE/f'sources/wanted/{name}.png').convert('RGBA')
    art=split_poses(source,name,2,2)
    assert len(art)==4 and all(im.getbbox() for im in art),('Missing quarry pose',name)
    return art


@lru_cache(None)
def outputs():
    result={}
    for name,base in WANTED:
        contract=spec(base);art=source_poses(name);den=8
        atlas=Image.new('RGBA',blank_atlas(contract.get('atlas',base)).size)
        size=tuple(v*den for v in contract['frame'])
        used={pose_choice(mode,step,base) for mode in ('idle','move','attack','defeated')
              for step,_ in enumerate(contract.get(mode,[]))}
        scale=min((size[0]-2*den)/max(art[i].width for i in used),
                  (size[1]-2*den)/max(art[i].height for i in used))
        for mode in ('idle','move','attack','defeated'):
            frames=contract.get(mode,[])
            for step,index in enumerate(frames):
                atlas.paste(pose(art,size,scale,mode,step,len(frames),base,den),
                            rectangle(atlas,contract['frame'],index,den))
        result[f'sprites/wanted_{name}.png']=atlas
    return result


def metadata():
    result={}
    for name,base in WANTED:
        contract=spec(base);size=blank_atlas(contract.get('atlas',base)).size
        canvas=Image.new('RGBA',size)
        indices={i for mode in ('idle','move','attack','defeated') for i in contract.get(mode,[])}
        result[f'sprites/wanted_{name}.png']={'size':size,'logical_size':[v//8 for v in size],
            'painted_rects':[list(rectangle(canvas,contract['frame'],i,8)) for i in sorted(indices)]}
    return result
