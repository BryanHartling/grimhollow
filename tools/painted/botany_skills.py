"""Pack authored botanical cutouts and new-class skill illustrations without generation."""
import hashlib
import json
from PIL import Image, ImageOps
from pack import HERE, put

DATA=json.loads((HERE/'botany-skills.json').read_text(encoding='utf-8'))

def cutouts(name,columns,rows,count,bounds):
    sheet=Image.open(HERE/'sources/botany-skills'/f'{name}.png').convert('RGBA')
    assert sheet.getchannel('A').getextrema()[0]==0, (name,'needs real alpha')
    result=[]
    for i in range(count):
        x,y=i%columns,i//columns
        part=sheet.crop((round(x*sheet.width/columns),round(y*sheet.height/rows),
                         round((x+1)*sheet.width/columns),round((y+1)*sheet.height/rows)))
        box=part.getchannel('A').point(lambda a:255 if a>=16 else 0).getbbox()
        assert box is not None,(name,i,'empty authored cell')
        result.append(ImageOps.contain(part.crop(box),bounds,Image.Resampling.LANCZOS))
    return result

def plants(features):
    for i,part in enumerate(cutouts('plants',4,4,len(DATA['plants']),(56,58))):
        tile=Image.new('RGBA',(64,64))
        tile.alpha_composite(part,((64-part.width)//2,61-part.height))
        put(features,7*16+i,tile)

def outputs():
    atlas=Image.new('RGBA',(1024,512));hashes=set();count=0
    for group,(name,skills) in enumerate(DATA['skills'].items()):
        for i,part in enumerate(cutouts(name,6,7,len(skills),(58,58))):
            tile=Image.new('RGBA',(64,64))
            tile.alpha_composite(part,((64-part.width)//2,(64-part.height)//2))
            digest=hashlib.sha256(tile.tobytes()).hexdigest()
            assert digest not in hashes,(name,skills[i]['key'],'duplicate skill')
            hashes.add(digest);put(atlas,group*42+i,tile);count+=1
    assert count==113
    return {'interfaces/painted_skills.png':atlas}
