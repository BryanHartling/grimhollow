// SPDX-License-Identifier: GPL-3.0-or-later
// Derived from the Pixel Dungeon / Shattered Pixel Dungeon key display.
// Copyright (C) 2012-2015 Oleg Dolya; Copyright (C) 2014-2026 Evan Debenham.
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.*;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import java.util.ArrayList;

/** Collected keys on this floor, grouped without discarding larger counts. */
public class KeyDisplay extends Component {
    private static final Class<?>[] TYPES={WornKey.class,CrystalKey.class,GoldenKey.class,IronKey.class};
    private static final int[] IMAGES={ItemSpriteSheet.WORN_KEY,ItemSpriteSheet.CRYSTAL_KEY,ItemSpriteSheet.GOLDEN_KEY,ItemSpriteSheet.IRON_KEY};
    private final ArrayList<Image> icons=new ArrayList<>();
    private final ArrayList<RenderedTextBlock> labels=new ArrayList<>();
    private NinePatch bg;
    private int totalKeys;

    public KeyDisplay(){updateKeys();}
    @Override protected void createChildren(){bg=Chrome.get(Chrome.Type.GREY_BUTTON);add(bg);}
    public int keyCount(){return totalKeys;}
    public void invalidateLayout(){layout();}

    public void updateKeys(){
        for(Image icon:icons){remove(icon);icon.destroy();}icons.clear();
        for(RenderedTextBlock label:labels){remove(label);label.destroy();}labels.clear();
        int[] counts=new int[TYPES.length];boolean missed=false;
        for(Notes.KeyRecord rec:Notes.getRecords(Notes.KeyRecord.class)){
            if(rec.depth()<Dungeon.depth)missed=true;
            else if(rec.depth()==Dungeon.depth&&Dungeon.branch==0)
                for(int i=0;i<TYPES.length;i++)if(rec.type()==TYPES[i])counts[i]+=rec.quantity();
        }
        totalKeys=missed?1:0;
        if(missed)addKey(ItemSpriteSheet.IRON_KEY,"?",true);
        for(int i=0;i<counts.length;i++)if(counts[i]>0){totalKeys+=counts[i];addKey(IMAGES[i],Integer.toString(counts[i]),false);}
        visible=totalKeys>0;height=12;width=4;
        for(int i=0;i<icons.size();i++)width+=9+labels.get(i).width()+3;
        layout();
    }
    private void addKey(int image,String count,boolean missed){
        ItemSprite icon=new ItemSprite(image);float scale=9/Math.max(icon.width(),icon.height());icon.scale.scale(scale);
        if(missed)icon.hardlight(0x82786D);
        icons.add(icon);add(icon);
        RenderedTextBlock label=PixelScene.renderTextBlock(count,6);label.hardlight(missed?0xB8AA96:0xFFF0CB);
        labels.add(label);add(label);
    }
    @Override protected void layout(){
        bg.x=x;bg.y=y;bg.size(width,height);
        float left=x+2;
        for(int i=0;i<icons.size();i++){
            Image icon=icons.get(i);RenderedTextBlock label=labels.get(i);
            icon.x=left+(9-icon.width())/2;icon.y=y+(height-icon.height())/2;PixelScene.align(icon);
            label.setPos(left+10,y+(height-label.height())/2);PixelScene.align(label);
            left+=9+label.width()+3;
        }
    }
}
