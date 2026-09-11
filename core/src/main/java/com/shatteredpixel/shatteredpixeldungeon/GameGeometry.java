// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

/** Texture pixels and logical world units are deliberately independent of UI scaling. */
public final class GameGeometry {
    private GameGeometry() {}
    public static final int TILE_SIZE = 64;
    public static final int HERO_FRAME_W = 48, HERO_FRAME_H = 60;
    public static final int ITEM_ICON = 32;
    public static final int WORLD_TILE_SIZE = 16;
    public static final int LEGACY_HERO_FRAME_W = 12, LEGACY_HERO_FRAME_H = 15;
    public static final int LEGACY_ITEM_ICON = 16;
    public static final int LIGHT_SAMPLES_PER_TILE = TILE_SIZE / 4;
    public static final int FOG_SAMPLES_PER_TILE = 2;
    public static final int MAX_ATLAS_SIZE = 4096;

    public static int tileFrame(String texture) {
        return texture.equals(Assets.Environment.TILES_SEWERS)
                || texture.equals("environment/walls_sewers.png")
                || texture.equals("environment/tiles_prison.png")
                || texture.equals("environment/tiles_caves.png")
                || texture.equals("environment/tiles_city.png")
                || texture.equals("environment/tiles_halls.png") ? TILE_SIZE : WORLD_TILE_SIZE;
    }
    public static final int LOGICAL_TILE = WORLD_TILE_SIZE;
    public static final int TEX_TILE = TILE_SIZE, TEX_HERO_W = HERO_FRAME_W, TEX_HERO_H = HERO_FRAME_H;
    public static final int TEX_ITEM = ITEM_ICON, LOGICAL_ITEM = 8;
    public static final int HERO_DENSITY = HERO_FRAME_W / LEGACY_HERO_FRAME_W;
    public static final int ITEM_DENSITY = TEX_ITEM / LEGACY_ITEM_ICON;
    private static final String[] RENDERED_CHARACTERS={"sprites/rat.png","sprites/crab.png","sprites/minion_skeleton.png","sprites/minion_ghoul.png"};
    public static int characterDensity(Object texture) {
        for(String path:RENDERED_CHARACTERS)if(path.equals(texture)||texture==com.watabou.gltextures.TextureCache.get(path))return 4;
        if (texture instanceof String) return ((String)texture).startsWith("sprites/hero_") ? HERO_DENSITY : 1;
        // Converted hero atlases retain upstream's eight armor rows and padding.
        if (texture instanceof com.watabou.gltextures.SmartTexture) {
            com.watabou.gltextures.SmartTexture t = (com.watabou.gltextures.SmartTexture)texture;
            return t.width == 256*HERO_DENSITY && t.height == 128*HERO_DENSITY ? HERO_DENSITY : 1;
        }
        return 1;
    }
    // Alpha occupancy is measured once per atlas rectangle, not on every rendered frame.
    private static final java.util.WeakHashMap<com.watabou.gltextures.SmartTexture,java.util.Map<String,Integer>> heights = new java.util.WeakHashMap<>();
    public static int opaqueHeight(com.watabou.gltextures.SmartTexture tx, com.watabou.utils.RectF frame) {
        int x=Math.round(frame.left*tx.width), y=Math.round(frame.top*tx.height);
        int w=Math.round(frame.width()*tx.width), h=Math.round(frame.height()*tx.height);
        String key=x+":"+y+":"+w+":"+h;
        java.util.Map<String,Integer> cache=heights.computeIfAbsent(tx,t->new java.util.HashMap<>());
        if(cache.containsKey(key))return cache.get(key);
        int top=h,bottom=-1;
        for(int j=0;j<h;j++)for(int i=0;i<w;i++)if((tx.bitmap.getPixel(x+i,y+j)&255)!=0){top=Math.min(top,j);bottom=Math.max(bottom,j);}
        int result=Math.max(0,bottom-top+1);cache.put(key,result);return result;
    }
    public static void fit(com.watabou.noosa.Image image, com.watabou.utils.RectF reference, float visibleHeight) {
        int opaque=opaqueHeight(image.texture,reference);
        float factor=opaque==0?1:visibleHeight/opaque;
        image.logicalSize(image.frame().width()*image.texture.width*factor,image.frame().height()*image.texture.height*factor);
    }
    public static void fitBox(com.watabou.noosa.Image image, float w, float h) {
        com.watabou.utils.RectF f=image.frame(); int x=Math.round(f.left*image.texture.width),y=Math.round(f.top*image.texture.height);
        int fw=Math.round(f.width()*image.texture.width),fh=Math.round(f.height()*image.texture.height),left=fw,right=-1,top=fh,bottom=-1;
        for(int j=0;j<fh;j++)for(int i=0;i<fw;i++)if((image.texture.bitmap.getPixel(x+i,y+j)&255)!=0){left=Math.min(left,i);right=Math.max(right,i);top=Math.min(top,j);bottom=Math.max(bottom,j);}
        if(right<left)return;
        float scale=Math.min(w/(right-left+1),h/(bottom-top+1));image.logicalSize(fw*scale,fh*scale);
    }
    public static com.watabou.noosa.Image portrait(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass hero,int tier) {
        if(hero==null||tier<0||tier>7)throw new IllegalArgumentException("Invalid saved hero portrait");
        com.watabou.noosa.Image image=heroImage(hero.spritesheet(),0,15*tier,12,15);
        if(opaqueHeight(image.texture,image.frame())==0)throw new IllegalArgumentException("Missing hero portrait");
        fit(image,image.frame(),14.5f);return image;
    }
    public static com.watabou.noosa.TextureFilm characterFilm(Object texture, int width, int height) {
        int density = characterDensity(texture);
        return new com.watabou.noosa.TextureFilm(texture, width*density, height*density);
    }
    public static com.watabou.noosa.Image heroImage(Object texture) {
        return new com.watabou.noosa.Image(texture) {
            @Override public void frame(com.watabou.utils.RectF frame) {
                super.frame(frame); width /= HERO_DENSITY; height /= HERO_DENSITY; updateVertices();
            }
        };
    }
    public static com.watabou.noosa.Image heroImage(Object texture, int x, int y, int w, int h) {
        com.watabou.noosa.Image image = heroImage(texture);
        image.frame(x*HERO_DENSITY, y*HERO_DENSITY, w*HERO_DENSITY, h*HERO_DENSITY);
        return image;
    }
}
