"""Original native-resolution character silhouettes, GPL-3.0-or-later.

The museum armour and draped figure studies on reference board D inform the
large planes and proportions. No upstream sprite pixels enter this painter.
Animation indices remain the game's existing indices; poses are vector geometry.
"""
import math
from functools import lru_cache
from PIL import Image, ImageDraw, ImageFilter

OUTLINE=(14,13,12,255)


def shade(color, value=1):
    rgb=tuple(bytes.fromhex(color))
    return tuple(round(c*value if value<=1 else c+(255-c)*(value-1)) for c in rgb)+(255,)


class Figure:
    def __init__(self, size, pose, phase, primary, accent):
        self.image=Image.new('RGBA',size)
        self.draw=ImageDraw.Draw(self.image)
        self.sx=size[0]/48;self.sy=size[1]/60
        self.pose=pose;self.phase=phase
        # Cosine gives distinct endpoints for the many two-frame upstream loops.
        self.bob=.5*(math.cos(phase*math.tau)-1) if pose=='idle' else .8*math.cos(phase*math.tau) if pose in ('run','special') else phase if pose in ('zap','operate','read') else 0
        self.lean=1.2*phase if pose=='attack' else 0
        self.fall=max(0,phase)*.65 if pose=='die' else 0
        self.dark=shade(primary,.40);self.mid=shade(primary,.70)
        self.light=shade(primary);self.glint=shade(primary,1.18)
        self.accent=shade(accent);self.adark=shade(accent,.52);self.amid=shade(accent,.78)

    def point(self,p):
        x,y=p
        x+=self.lean*(52-y)/44
        if self.fall:
            x-=(y-28)*self.fall*.12
            y=50-(50-y)*(1-self.fall)
        else:y+=self.bob
        return (round(x*self.sx),round(y*self.sy))

    def poly(self,points,color):
        self.draw.polygon([self.point(p) for p in points],fill=color)

    def line(self,points,color,width=1):
        self.draw.line([self.point(p) for p in points],fill=color,width=max(1,round(width*self.sx)),joint='curve')

    def ellipse(self,box,color):
        x0,y0=self.point(box[:2]);x1,y1=self.point(box[2:])
        self.draw.ellipse((min(x0,x1),min(y0,y1),max(x0,x1),max(y0,y1)),fill=color)

    def finish(self):
        mask=self.image.getchannel('A')
        # Two native pixels on every exposed edge, including holes in the figure.
        outline=Image.new('RGBA',self.image.size,OUTLINE)
        outline.putalpha(mask.filter(ImageFilter.MaxFilter(5)))
        outline.alpha_composite(self.image)
        shadow=Image.new('RGBA',self.image.size)
        d=ImageDraw.Draw(shadow)
        d.ellipse((round(10*self.sx),round(49*self.sy),round(39*self.sx),round(52*self.sy)),fill=(14,13,12,45))
        shadow=shadow.filter(ImageFilter.GaussianBlur(.65))
        shadow.alpha_composite(outline)
        return shadow


def weapon(f,kind,tier):
    swing=6*math.sin(f.phase*math.pi/2) if f.pose=='attack' else 3*f.phase if f.pose=='zap' else 0
    x=39-swing
    if kind in ('staff','crook','brush','scythe'):
        f.line([(x,12),(x-3,49)],f.dark,4)
        f.line([(x-1,12),(x-4,48)],f.amid,2)
        if kind=='scythe':
            f.poly([(x-1,12),(x-8,9),(x-17,11),(x-23,18),(x-13,14),(x,16)],f.accent)
            f.line([(x-19,15),(x-11,11),(x-3,12)],f.glint)
        elif kind=='brush':
            f.poly([(x-3,17),(x-5,10),(x-1,7),(x+2,11),(x+1,18)],f.accent)
        elif kind=='crook':
            f.line([(x,18),(x+3,11),(x,8),(x-5,9),(x-6,14)],f.accent,3)
        else:
            f.poly([(x-5,12),(x,7),(x+4,12),(x,18)],f.accent)
            f.line([(x-2,12),(x,10)],f.glint,2)
    elif kind=='bow':
        f.line([(39,13),(42,23),(43,33),(39,46)],f.amid,3)
        f.line([(40,13),(39,30),(40,46)],f.accent)
        f.line([(30,30),(42,27)],f.light)
    elif kind in ('sword','rapier','axe','dagger','mace','hammer'):
        tip=15 if kind!='dagger' else 27
        f.line([(x-2,44),(x+1,tip)],f.dark,4)
        if kind in ('axe','hammer'):
            f.poly([(x-7,tip),(x+5,tip),(x+6,tip+8),(x-6,tip+8)],f.amid)
            f.line([(x-7,tip),(x+5,tip)],f.accent,2)
        elif kind=='mace':
            f.poly([(x,tip-3),(x+5,tip+2),(x+3,tip+8),(x-3,tip+8),(x-5,tip+2)],f.accent)
        else:
            f.poly([(x-3,35),(x,tip),(x+3,35)],f.amid)
            f.line([(x,tip),(x-1,33)],f.accent,2 if kind=='sword' else 1)
            f.line([(x-6,36),(x+4,37)],f.accent,2)


def humanoid(f,features,tier):
    robe='robe' in features; skeletal='skeleton' in features
    broad=2 if any(x in features for x in ('brute','guard','golem')) else 0
    shoulder=min(3,tier//2)+broad
    walk=3*math.cos(f.phase*math.tau) if f.pose=='run' else 0
    # Long legs and a compact head keep the figure weighty rather than chibi.
    f.poly([(15,35),(23,35),(21+walk,48),(13+walk,49),(13+walk,46)],f.dark)
    f.poly([(25,35),(32,34),(35-walk,48),(27-walk,49),(25,45)],f.mid)
    f.line([(17,37),(16+walk,46)],f.light,3)
    f.line([(28,37),(30-walk,46)],f.light,3)
    f.poly([(13+walk,47),(21+walk,47),(22+walk,50),(11+walk,50)],f.mid)
    f.poly([(28-walk,47),(35-walk,47),(38-walk,50),(27-walk,50)],f.dark)
    if robe:
        f.poly([(15,22),(31,21),(35,35),(37,48),(30,50),(22,48),(11,50),(14,35)],f.mid)
        f.poly([(16,26),(22,26),(21,43),(18,48),(12,49)],f.light)
        f.poly([(26,26),(30,25),(34,46),(29,48)],f.dark)
        f.poly([(23,28),(26,32),(27,48),(22,46)],f.amid)
        f.line([(16,31),(14,46)],f.glint)
    # Cape, shoulder yoke and a torso with three broad material planes.
    if any(x in features for x in ('cape','hood','robe','cloak')):
        f.poly([(12-shoulder,23),(18,19),(31,19),(37+shoulder,25),(39,45),(33,47),(29,33),(16,35),(10,46)],f.dark)
    f.poly([(14-shoulder,22),(20,19),(29,19),(34+shoulder,24),(32,36),(15,36)],f.mid)
    f.poly([(15,23),(23,22),(24,33),(17,35),(14,28)],f.light)
    f.poly([(25,22),(31,24),(32,34),(24,33)],f.dark)
    f.line([(16,23),(22,22),(29,23)],f.glint,2)
    f.poly([(13-shoulder,23),(18,21),(17,29),(11-shoulder,31),(9-shoulder,27)],f.light)
    f.poly([(31,21),(36+shoulder,24),(38+shoulder,28),(32,30)],f.dark)
    arm=3*math.cos(f.phase*math.tau) if f.pose=='run' else 0
    f.poly([(11,29),(16,28),(18+arm,37),(14+arm,40),(9,34)],f.mid)
    f.poly([(33,28),(37,28),(40-arm,35),(37-arm,39),(33,35)],f.light)
    f.ellipse((13+arm,35,19+arm,41),f.accent)
    f.ellipse((34-arm,34,40-arm,40),f.amid)
    f.line([(15,35),(31,35)],f.adark,3)
    f.poly([(21,34),(25,34),(25,37),(21,37)],f.accent)
    if tier>=2:
        f.poly([(14-shoulder,23),(18,21),(21,24),(16,28),(10-shoulder,28)],f.amid)
        f.line([(13-shoulder,23),(18,22),(20,24)],f.accent,2)
        if tier>=4:
            f.poly([(24,23),(30,24),(31,30),(25,34),(22,29)],f.amid)
            f.line([(25,24),(25,30)],f.accent,2)
        for y in range(38,45,3):f.line([(15,y),(20,y-1)],f.amid,2)
    if skeletal:
        f.line([(23,23),(23,35)],f.accent,3)
        for y in (25,29,32):f.line([(17,y),(22,y+1),(29,y-1)],f.accent,2)
        for x in (16,30):f.line([(x,39),(x+1,46)],f.accent,2)
    elif 'tabard' in features or 'cleric' in features:
        f.poly([(21,24),(27,24),(28,42),(24,45),(20,42)],f.amid)
        if 'cleric' in features:
            f.line([(24,26),(24,35)],f.accent,2);f.line([(21,29),(27,29)],f.accent,2)
    # A hood is a pointed arch with a deep face recess, not a round cap.
    if any(x in features for x in ('hood','robe','cloak')):
        f.poly([(13,21),(14,13),(18,8),(25,6),(31,10),(34,18),(32,24),(24,26),(17,24)],f.mid)
        f.poly([(14,20),(16,13),(21,9),(26,8),(23,12),(19,15),(18,21)],f.light)
        f.poly([(20,13),(25,11),(30,15),(30,21),(24,24),(19,21)],OUTLINE)
        f.poly([(21,15),(26,14),(28,18),(25,21),(22,20)],f.adark)
        f.line([(21,16),(23,16)],f.accent)
        f.line([(27,16),(28,16)],f.accent)
        f.line([(17,22),(23,24),(31,21)],f.amid,2)
    elif 'helm' in features or tier>=5 and 'hat' not in features:
        f.poly([(16,19),(16,12),(21,8),(28,9),(31,14),(30,21),(24,24),(19,22)],f.mid)
        f.poly([(17,12),(22,9),(24,11),(22,19),(18,19)],f.light)
        f.line([(17,16),(29,16)],OUTLINE,2)
        f.line([(24,10),(24,22)],f.accent)
        f.poly([(15,11),(15,7),(18,12)],f.accent)
    else:
        f.poly([(18,11),(24,8),(29,11),(30,19),(25,24),(20,22),(17,17)],f.amid)
        f.poly([(18,12),(22,10),(25,11),(22,19),(19,18)],f.accent)
        f.line([(19,16),(21,16)],OUTLINE)
        f.line([(26,16),(28,16)],OUTLINE)
        if 'gnoll' in features:
            f.poly([(17,13),(14,7),(20,10)],f.light)
            f.poly([(27,10),(32,8),(30,16)],f.dark)
            f.poly([(21,18),(30,19),(26,23),(21,22)],f.light)
            f.ellipse((27,19,29,20),OUTLINE)
        elif 'undead' in features or skeletal:
            f.ellipse((18,14,22,18),OUTLINE);f.ellipse((25,14,29,18),OUTLINE)
            f.line([(21,22),(27,22)],OUTLINE,2)
        else:f.poly([(17,12),(19,8),(25,7),(29,11),(29,13),(24,10),(19,14)],f.dark)
    if 'hat' in features:
        f.poly([(13,13),(18,9),(30,10),(35,15),(27,17),(16,16)],f.dark)
        f.line([(15,14),(24,15),(33,14)],f.light,2)
        f.poly([(27,12),(29,7),(34,6),(31,10)],f.accent)
    if 'chain' in features:
        for y in range(24,42,4):f.ellipse((9,y,14,y+6),f.accent);f.ellipse((10,y+1,13,y+4),f.dark)
    if 'crown' in features:
        f.poly([(17,11),(16,6),(21,9),(24,5),(27,9),(32,6),(30,13)],f.accent)
    if 'cleric' in features:
        f.poly([(18,14),(18,9),(23,6),(29,9),(30,15)],f.accent)
        f.line([(24,8),(24,13)],f.adark,2)
    if 'psychic' in features:
        f.ellipse((22,11,26,15),f.accent)
        f.line([(12,25),(21,28),(33,23)],f.accent,2)
    if 'wings' in features:
        for side in (-1,1):
            f.poly([(24+side*9,23),(24+side*19,16),(24+side*17,31),(24+side*12,29)],f.dark)
    if f.pose=='read':
        y=29+f.phase
        f.poly([(15,y),(23,y+2),(32,y),(33,y+9),(24,y+12),(15,y+9)],f.accent)
        f.line([(24,y+3),(24,y+10)],f.adark,2)
        f.line([(17,y+3),(21,y+4)],f.amid)
        f.line([(27,y+4),(30,y+3)],f.amid)
    elif 'book' in features:
        f.poly([(7,32),(16,29),(20,35),(12,39)],f.amid)
        f.line([(8,32),(12,37),(18,34)],f.accent,2)
    for gear in ('staff','crook','brush','scythe','bow','sword','rapier','axe','dagger','mace','hammer'):
        if gear in features:weapon(f,gear,tier)


def beast(f,kind,features):
    walk=3*math.cos(f.phase*math.tau) if f.pose=='run' else 0
    if kind=='swarm':
        for j,(x,y) in enumerate(((15,17),(30,20),(21,29),(35,36),(12,39),(24,46))):
            drift=round(2*math.sin(f.phase*math.tau+j))
            f.ellipse((x-6,y-4+drift,x,y+1+drift),f.light)
            f.ellipse((x+1,y-4-drift,x+6,y+1-drift),f.light)
            f.poly([(x-2,y-1),(x+2,y-2),(x+3,y+5),(x,y+7),(x-2,y+4)],f.amid)
            f.ellipse((x-2,y-3,x+2,y),f.accent)
    elif kind in ('rat','sheep'):
        f.line([(13,38),(6,39),(5,30),(9,26)],f.adark,3)
        f.poly([(11,30),(16,21),(28,20),(36,27),(39,37),(30,44),(16,42),(10,37)],f.mid)
        f.poly([(13,29),(18,23),(27,23),(31,29),(24,32),(13,35)],f.light)
        f.poly([(12,34),(26,32),(34,37),(30,43),(18,41)],f.dark)
        for x,dy in ((14,walk),(28,-walk)):
            f.poly([(x,38),(x+5,37),(x+7,46+dy/3),(x-2,46+dy/3)],f.adark)
            f.line([(x,45+dy/3),(x+5,45+dy/3)],f.accent,2)
        f.poly([(26,29),(32,27),(38,31),(42,38),(36,43),(30,40)],f.light)
        f.poly([(29,29),(28,22),(32,23),(34,30)],f.amid)
        f.poly([(35,30),(36,24),(40,28),(39,33)],f.accent)
        f.ellipse((35,34,37,36),OUTLINE);f.ellipse((40,38,43,40),f.adark)
        f.line([(38,39),(42,40)],f.accent)
        if kind=='sheep':
            f.ellipse((18,17,29,28),f.accent)
            for x,y in ((13,27),(20,23),(27,24),(14,34),(22,31)):f.ellipse((x-3,y-3,x+6,y+6),f.accent)
        if 'crown' in features:f.poly([(27,26),(25,19),(30,22),(32,17),(35,23),(40,20),(38,29)],f.accent)
        if 'plague' in features:
            for x,y in ((17,29),(22,36),(30,31)):f.ellipse((x,y,x+3,y+3),f.accent)
    elif kind in ('crab','spider','scorpio'):
        pairs=4 if kind=='spider' else 3
        for j in range(pairs):
            y=25+j*6
            for side in (-1,1):
                f.line([(24+side*10,y),(24+side*18,y-6+walk),(24+side*19,y+5)],f.mid,3)
                f.line([(24+side*10,y-1),(24+side*17,y-6+walk)],f.light)
        f.poly([(14,24),(20,20),(30,22),(36,28),(34,38),(26,43),(16,39),(11,32)],f.dark)
        f.poly([(14,25),(22,22),(30,25),(32,32),(25,35),(15,32)],f.light)
        f.poly([(15,33),(26,35),(32,32),(31,39),(24,41)],f.mid)
        f.line([(17,25),(23,24),(29,27)],f.glint,2)
        for x in (18,29):f.ellipse((x,34,x+3,37),f.accent)
        if kind=='crab':
            for x in (7,36):
                f.poly([(x,28),(x-3,20),(x,13),(x+2,21),(x+7,17),(x+8,24),(x+4,29)],f.amid)
                f.line([(x-1,20),(x,15)],f.accent,2)
        elif kind=='scorpio':
            f.line([(24,25),(31,19),(34,11),(27,8),(22,13)],f.amid,4)
            f.poly([(19,14),(23,9),(26,15)],f.accent)
        else:
            f.ellipse((15,16,32,30),f.mid)
            f.poly([(19,18),(25,17),(29,24),(22,25)],f.amid)
            f.line([(18,36),(21,43),(24,36),(27,43),(30,36)],f.accent,2)
    elif kind in ('bat','bird','bee'):
        flap=5*math.cos(f.phase*math.tau)
        for side in (-1,1):
            f.poly([(24+side*4,28),(24+side*13,18+flap),(24+side*20,15+flap),(24+side*17,34),(24+side*11,30),(24+side*8,38)],f.amid)
            f.line([(24+side*5,28),(24+side*14,23+flap),(24+side*19,17+flap)],f.accent,2)
            f.line([(24+side*9,26),(24+side*12,31)],f.adark,2)
        f.poly([(19,22),(24,19),(30,24),(30,38),(25,44),(19,37)],f.mid)
        f.poly([(20,24),(24,22),(25,39),(21,36)],f.light)
        f.poly([(19,24),(18,14),(22,18),(28,18),(31,14),(30,27)],f.dark)
        f.ellipse((20,23,22,25),f.accent);f.ellipse((27,23,29,25),f.accent)
        if kind=='bird':f.poly([(26,25),(35,29),(27,31)],f.accent)
        if kind=='bee':
            for y in (29,34,39):f.line([(20,y),(28,y)],f.accent,3)
    elif kind=='snake':
        f.line([(10,42),(7,36),(14,30),(30,34),(36,41),(30,45),(18,43),(17,33),(23,22),(31,18)],f.dark,9)
        f.line([(10,41),(10,36),(16,34),(31,37),(32,41),(24,42),(20,38),(24,28)],f.light,4)
        f.line([(13,36),(23,39),(29,40)],f.amid,3)
        f.poly([(23,25),(21,17),(26,12),(34,15),(37,22),(32,28)],f.mid)
        f.poly([(23,17),(27,14),(34,17),(32,20),(25,20)],f.light)
        f.ellipse((28,18,30,20),f.accent)
        f.line([(33,25),(40,27)],f.accent)
    elif kind=='fish':
        f.poly([(10,30),(5,22),(6,39),(15,35)],f.amid)
        f.poly([(10,29),(20,20),(31,22),(41,30),(38,38),(28,43),(16,37)],f.mid)
        f.poly([(14,29),(22,23),(31,24),(36,29),(24,31)],f.light)
        f.poly([(18,24),(22,13),(29,23)],f.amid)
        f.poly([(22,38),(28,48),(32,39)],f.dark)
        f.ellipse((31,27,36,32),OUTLINE);f.ellipse((32,28,34,30),f.accent)
        f.line([(34,36),(38,33),(40,35)],f.accent,2)


def other(f,kind,features,tier):
    if kind=='slime' and f.pose=='run':f.bob=1.5*math.sin(f.phase*math.tau)
    if kind in ('slime','egg','eye','elemental','ghost'):
        points=[(9,43),(11,31),(16,20),(24,15),(32,18),(37,29),(39,44),(33,49),(25,47),(17,50)]
        if kind=='ghost':points=[(13,48),(14,22),(18,11),(27,9),(33,17),(35,33),(39,48),(31,44),(26,50),(21,44)]
        if kind=='elemental':points=[(14,48),(17,40),(10,31),(15,24),(13,14),(20,21),(25,8),(27,19),(36,14),(33,27),(40,33),(30,43),(34,49)]
        f.poly(points,f.dark)
        f.poly([(13,39),(15,27),(23,20),(30,23),(28,36),(21,43)],f.mid)
        f.poly([(16,29),(21,22),(27,23),(24,28),(19,34)],f.light)
        f.line([(19,24),(23,22),(26,23)],f.glint,2)
        if kind=='elemental':
            f.poly([(18,40),(15,30),(20,32),(24,18),(28,31),(32,29),(29,41),(23,46)],f.amid)
            f.poly([(22,36),(24,27),(27,36),(24,42)],f.accent)
        if 'gash' in features:
            f.poly([(16,22),(24,25),(31,21),(30,33),(25,42),(19,34)],f.amid)
            f.line([(18,24),(25,30),(23,37)],f.accent,3)
        if kind=='eye':
            f.poly([(9,30),(19,24),(29,24),(39,31),(29,39),(19,38)],f.accent)
            f.ellipse((21,25,30,38),f.adark);f.ellipse((24,25,26,37),OUTLINE)
            for side in (-1,1):f.line([(24+side*10,35),(24+side*15,43),(24+side*10,48)],f.mid,3)
        elif kind=='egg':
            f.line([(25,20),(21,29),(29,35),(24,43)],f.amid,3)
            f.ellipse((19,32,27,38),f.accent)
        else:
            f.line([(18,30),(21,31)],f.accent,2);f.line([(28,30),(31,29)],f.accent,2)
            f.poly([(21,38),(29,38),(25,42)],f.adark)
            if 'toxic' in features:f.poly([(11,37),(17,34),(18,42),(13,44)],f.amid)
    elif kind in ('golem','machine'):
        for x in (12,29):
            f.poly([(x,33),(x+8,33),(x+9,46),(x+11,49),(x-2,49)],f.dark)
            f.poly([(x+1,34),(x+6,34),(x+5,44),(x,45)],f.mid)
        f.poly([(9,18),(17,13),(31,14),(39,22),(35,38),(15,40),(10,33)],f.dark)
        f.poly([(11,20),(18,16),(27,17),(27,32),(17,35),(12,30)],f.light)
        f.poly([(29,17),(36,23),(33,34),(28,32)],f.mid)
        for side in (-1,1):
            f.poly([(24+side*12,21),(24+side*18,24),(24+side*20,39),(24+side*14,40)],f.mid)
            f.line([(24+side*17,25),(24+side*17,35)],f.light,3)
        f.poly([(16,15),(18,8),(28,8),(32,16),(28,23),(19,22)],f.amid)
        f.line([(19,15),(29,15)],OUTLINE,3)
        f.ellipse((22,14,26,16),f.accent)
        for y in (25,29,33):f.line([(17,y),(24,y)],f.dark,2)
        if kind=='machine':
            f.poly([(6,32),(11,28),(14,35),(11,43),(6,41)],f.amid)
            f.line([(7,34),(11,33),(10,39)],f.accent,2)
        if 'crawler' in features:
            f.poly([(8,37),(35,37),(41,43),(39,50),(7,50),(5,44)],f.dark)
            f.poly([(9,39),(35,39),(38,44),(36,47),(9,47),(8,44)],f.amid)
            for x in (12,20,28,35):f.line([(x,40),(x-1,46)],f.accent,2)
            f.poly([(32,26),(39,27),(43,32),(38,36),(31,32)],f.amid)
    elif kind=='mimic':
        gape=3+round(5*abs(math.sin(f.phase*math.pi))) if f.pose=='attack' else 3
        f.poly([(9,26),(17,19),(37,23),(39,43),(29,49),(9,44)],f.dark)
        f.poly([(10,27),(28,30),(29,47),(10,43)],f.mid)
        f.poly([(29,30),(37,25),(37,42),(30,46)],f.adark)
        f.poly([(9,25),(11,19),(18,15),(36,19),(39,25),(28,30)],f.light)
        f.poly([(11,31),(28,33),(38,28),(37,32+gape),(28,36+gape),(11,34+gape)],OUTLINE)
        for x in (13,19,25):f.poly([(x,32),(x+3,33),(x+2,36+gape)],f.accent)
        f.line([(13,23),(32,26)],f.glint,2)
        f.line([(16,25),(16,43)],f.amid,3);f.line([(31,27),(32,43)],f.amid,3)
        f.ellipse((17,20,20,22),f.accent);f.ellipse((28,21,31,23),f.accent)
    elif kind in ('plant','fungus','tower','mirror','door'):
        f.poly([(13,47),(18,43),(18,25),(16,17),(24,11),(31,17),(30,40),(37,47),(34,50),(14,50)],f.dark)
        f.poly([(19,25),(23,20),(25,43),(19,46)],f.mid)
        f.line([(20,25),(21,42)],f.light,3)
        if kind in ('plant','fungus'):
            for side,y in ((-1,30),(1,36),(-1,43)):
                f.poly([(24,y),(24+side*15,y-12),(24+side*16,y-5),(24+side*9,y+2)],f.mid)
                f.line([(24,y),(24+side*13,y-9)],f.light,2)
            f.poly([(10,25),(14,15),(22,9),(29,10),(38,20),(37,26)],f.amid)
            f.poly([(13,22),(17,16),(23,12),(26,13),(20,19)],f.accent)
            f.line([(11,26),(24,29),(36,26)],f.adark,3)
        else:
            f.poly([(12,44),(12,17),(18,9),(29,9),(35,18),(35,44)],f.mid)
            f.poly([(17,39),(17,20),(22,14),(28,15),(30,21),(30,39)],f.adark)
            f.line([(13,40),(13,18),(19,11),(28,11)],f.light,2)
            if kind=='mirror':
                f.poly([(19,20),(24,15),(29,22),(29,36),(18,38)],f.accent)
                f.line([(20,22),(27,29)],f.amid,2)
            elif kind=='door':
                for x in (18,23,29):f.line([(x,18),(x,40)],f.accent,2)
            else:
                f.poly([(17,25),(24,17),(31,25),(24,33)],f.accent)
                f.ellipse((22,23,26,27),OUTLINE)
                if tier>0:f.line([(10,15),(7,24),(12,33)],f.amid,3)
    elif kind=='fist':
        f.poly([(16,47),(14,35),(10,28),(12,18),(18,18),(20,12),(26,12),(27,17),(33,16),(37,22),(36,33),(29,39),(31,48)],f.mid)
        f.poly([(12,24),(13,20),(17,20),(20,30),(17,35)],f.light)
        f.poly([(21,15),(25,15),(28,31),(21,33)],f.light)
        f.line([(13,28),(18,30),(23,29),(28,30),(34,26)],f.dark,2)
        f.line([(17,39),(28,39)],f.amid,3)
        f.poly([(18,34),(26,33),(29,38),(27,46),(20,46)],f.amid)
        f.line([(20,36),(25,36),(24,44)],f.accent,2)
    elif kind=='log':
        f.poly([(12,17),(25,12),(36,20),(37,44),(25,50),(12,42)],f.mid)
        f.ellipse((12,10,35,25),f.accent);f.ellipse((16,13,30,22),f.adark)
        for x in (17,23,29):f.line([(x,25),(x+2,43)],f.dark,2)
    else:raise ValueError('Unknown character silhouette '+kind)


@lru_cache(maxsize=6000)
def native_frame(size,kind,features,primary,accent,tier,pose,phase):
    f=Figure(size,pose,phase,primary,accent)
    if kind=='humanoid':humanoid(f,features,tier)
    elif kind in ('rat','sheep','crab','spider','scorpio','bat','bird','bee','swarm','snake','fish'):beast(f,kind,features)
    else:other(f,kind,features,tier)
    return f.finish()


def paint(spec):
    canvas=Image.new('RGBA',spec['dimensions']);fw,fh=spec['frame'];columns=spec['dimensions'][0]//fw
    for index,entry in enumerate(spec['poses']):
        form=spec['forms'][entry['form']]
        frame=native_frame((fw,fh),form['kind'],tuple(form.get('features',[])),form['primary'],form['accent'],entry.get('tier',0),entry['pose'],entry['phase'])
        canvas.alpha_composite(frame,(index%columns*fw,index//columns*fh))
    return canvas


def gallery(specs):
    """Native frames beside their actual 16-pixel previews, for human art review."""
    entries=[]
    for spec in sorted(specs,key=lambda s:('hero_' not in s['output'],s['output'])):
        for form in spec['forms']:
            pose=spec['poses'][form['idle']]
            frame=native_frame(tuple(spec['frame']),form['kind'],tuple(form['features']),form['primary'],form['accent'],pose['tier'],'idle',pose['phase'])
            name=spec['output'].split('/')[-1][:-4]
            label=name.replace('hero_','')+' '+str(pose['tier'] if name.startswith('hero_') else form['idle'])
            entries.append((label,frame))
    width=12*112;out=Image.new('RGB',(width,36+math.ceil(len(entries)/12)*128),'#242421');d=ImageDraw.Draw(out)
    d.text((8,10),'GRIMHOLLOW | Native character silhouettes and 16 px readability previews',fill='#EFE7D2')
    for i,(label,frame) in enumerate(entries):
        x=i%12*112;y=36+i//12*128
        out.paste(frame,(x+(112-frame.width)//2,y),frame)
        mini=frame.copy();mini.thumbnail((16,16),Image.Resampling.LANCZOS)
        out.paste(mini,(x+48,y+98),mini)
        d.text((x+3,y+114),label[:18],fill='#C9BFA8')
    return out
