"""Offline journal/navigation export from the committed painted source atlas."""
import json
from PIL import Image, ImageOps, ImageEnhance
from pack import HERE
from interface_art import panels, patch
from inventory import gutters, icon
import numpy as np


def outputs(items):
    sheet=Image.open(HERE/'sources/sprint/journal.png').convert('RGBA')
    alpha=np.asarray(sheet.getchannel('A'));assert alpha.min()==0
    xs,ys=gutters(alpha,0),gutters(alpha,1)
    cells=[icon(sheet.crop((xs[x],ys[y],xs[x+1],ys[y+1]))) for y in range(4) for x in range(4)]
    atlas=Image.new('RGBA',(256,256))
    for i,cell in enumerate(cells):atlas.paste(cell,(i%4*64,i//4*64))
    from playtest_polish import journal_consumables
    atlas.paste(journal_consumables(items),(0,128))
    materials=panels()
    buttons=Image.new('RGBA',(256,64))
    buttons.paste(patch(materials[0],(52,44),(6,6,6,6)),(8,8))
    menu=patch(materials[0],(48,44),(6,6,6,6))
    menu.alpha_composite(cells[1].resize((36,32),Image.Resampling.LANCZOS),(6,6))
    buttons.paste(menu,(68,8))
    book=ImageOps.contain(cells[0],(44,24),Image.Resampling.LANCZOS)
    buttons.alpha_composite(book,(124+(44-book.width)//2,(24-book.height)//2))
    # The caller's item atlas is already built; use stable public key indices.
    contract=json.loads((HERE.parents[1]/'desktop/src/test/resources/item-semantics.json').read_text())
    for i,name in enumerate(('IRON_KEY','WORN_KEY','CRYSTAL_KEY','GOLDEN_KEY','IRON_KEY')):
        index=contract['items'][name]['artIndex'];x,y=index%16*64,index//16*64
        key=items.crop((x,y,x+64,y+64));box=key.getchannel('A').getbbox();assert box
        key=key.crop(box)
        if i==0:key=ImageEnhance.Color(key).enhance(0);key=ImageEnhance.Brightness(key).enhance(.35)
        for top,height in ((0,28),(32,16)):
            part=ImageOps.contain(key,(12,height),Image.Resampling.LANCZOS)
            buttons.alpha_composite(part,(172+i*12+(12-part.width)//2,top+(height-part.height)//2))
    menu_bg=Image.new('RGBA',(128,128))
    menu_bg.paste(patch(materials[0],(124,84),(8,8,8,8)),(4,0))
    menu_bg.paste(patch(materials[0],(24,32),(12,0,8,0)),(4,88))
    landmarks=Image.open(HERE/'sources/sprint/journal-landmarks.png').convert('RGBA')
    alpha=np.asarray(landmarks.getchannel('A'));assert alpha.min()==0
    xs,ys=gutters(alpha,0),gutters(alpha,1)
    notes=Image.new('RGBA',(256,256))
    for y in range(4):
        for x in range(4):
            notes.paste(icon(landmarks.crop((xs[x],ys[y],xs[x+1],ys[y+1]))),(x*64,y*64))
    from playtest_presentation import well_icon
    notes.paste(well_icon(),(64,128))
    return {'interfaces/painted_journal.png':atlas,'interfaces/painted_landmarks.png':notes,
            'interfaces/menu_button.png':buttons,'interfaces/menu_pane.png':menu_bg}
