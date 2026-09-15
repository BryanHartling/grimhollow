// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.GL20;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;

/** Four texture pixels per existing UI unit; hit areas and layout dimensions do not change. */
public final class PaintedInterface {
    private PaintedInterface() {}
    private static final int SCALE = 4;
    private static final String GLYPHS = "interfaces/painted_glyphs.png";
    private static final String[] GLYPH_NAMES = {
            "ENTER", "EXIT", "BACKPACK_LRG", "JOURNAL", "RANKINGS", "BADGES", "PREFS", "MAGNIFY",
            "WAIT", "ALCHEMY", "STATS", "TALENT", "CATALOG", "BUFFS", "COIN_SML", "ENERGY_SML",
            "DISPLAY_LAND", "DISPLAY_PORT", "AUDIO", "LANGS", "CONTROLLER", "KEYBOARD", "DATA", "INFO",
            "WARNING", "CLOSE", "PLUS", "CHECKED", "UNCHECKED", "LEFTARROW", "RIGHTARROW", "REPEAT"
    };

    public static Image glyph(String name, float width, float height) {
        for (int i=0; i<GLYPH_NAMES.length; i++) if (GLYPH_NAMES[i].equals(name)) {
            Image image = new Image(GLYPHS);
            image.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
            image.frame(i%8*64, i/8*64, 64, 64);
            image.logicalSize(width, height);
            return image;
        }
        return null;
    }

    public static Image replace(Icons type, Image original) {
        String name=type.name();
        if (type==Icons.DISPLAY) name=PixelScene.landscape()?"DISPLAY_LAND":"DISPLAY_PORT";
        if (type==Icons.BACKPACK) name="BACKPACK_LRG";
        if (type==Icons.GOLD) name="COIN_SML";
        if (type==Icons.NEWS || type==Icons.CHANGES) name="JOURNAL";
        Image replacement;
        int bag=type==Icons.SEED_POUCH?ItemSpriteSheet.POUCH:type==Icons.SCROLL_HOLDER?ItemSpriteSheet.HOLDER:
                type==Icons.WAND_HOLSTER?ItemSpriteSheet.HOLSTER:type==Icons.POTION_BANDOLIER?ItemSpriteSheet.BANDOLIER:-1;
        if(bag>=0) {
            replacement=new Image(Assets.Sprites.ITEMS);
            replacement.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
            replacement.frame(ItemSpriteSheet.film.get(bag));
            replacement.logicalSize(original.width,original.height);
        } else replacement=glyph(name, original.width, original.height);
        if (replacement==null) return original;
        replacement.scale.set(original.scale);
        original.destroy();
        return replacement;
    }

    public static Image image(String texture, int x, int y, int width, int height) {
        Image image=new Image(texture);
        image.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
        frame(image,x,y,width,height);
        return image;
    }

    public static void frame(Image image,int x,int y,int width,int height) {
        image.frame(x*SCALE,y*SCALE,width*SCALE,height*SCALE);
        image.logicalSize(width,height);
    }

    public static Patch patch(String texture,int x,int y,int w,int h,int margin) {
        return patch(texture,x,y,w,h,margin,margin,margin,margin);
    }
    public static Patch patch(String texture,int x,int y,int w,int h,int l,int t,int r,int b) {
        return new Patch(texture,x,y,w,h,l,t,r,b);
    }
    public static Patch slot() { return patch(Assets.Interfaces.CHROME,0,64,28,28,2); }

    public static final class Patch extends NinePatch {
        private final int sourceWidth, sourceHeight;
        private Patch(String asset,int x,int y,int w,int h,int l,int t,int r,int b) {
            super(asset,x*SCALE,y*SCALE,w*SCALE,h*SCALE,l*SCALE,t*SCALE,r*SCALE,b*SCALE);
            sourceWidth=w; sourceHeight=h;
            marginLeft=l; marginTop=t; marginRight=r; marginBottom=b;
            nWidth=width=w; nHeight=height=h;
            texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
            updateVertices();
        }
        public void source(int x,int y) {
            outterF=texture.uvRect(x*SCALE,y*SCALE,(x+sourceWidth)*SCALE,(y+sourceHeight)*SCALE);
            innerF=texture.uvRect((x+marginLeft)*SCALE,(y+marginTop)*SCALE,
                    (x+sourceWidth-marginRight)*SCALE,(y+sourceHeight-marginBottom)*SCALE);
            updateVertices();
        }
    }
}
