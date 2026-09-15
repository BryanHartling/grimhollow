// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

/** Texture pixels and logical world units are deliberately independent of UI scaling. */
public final class GameGeometry {
    private GameGeometry() {}
    public static final int TILE_SIZE = 64;
    public static final int HERO_FRAME_W = 48, HERO_FRAME_H = 60;
    public static final int ITEM_ICON = 64;
    public static final int WORLD_TILE_SIZE = 16;
    public static final int LEGACY_HERO_FRAME_W = 12, LEGACY_HERO_FRAME_H = 15;
    public static final int LEGACY_ITEM_ICON = 16;
    public static final int LIGHT_SAMPLES_PER_TILE = WORLD_TILE_SIZE;
    // Visibility is a cell mask, independent of the resolution of the artwork.
    public static final int FOG_SAMPLES_PER_TILE = 1;
    public static final int MAX_ATLAS_SIZE = 4096;

    public static int tileFrame(String texture) {
        return texture.equals(Assets.Environment.TILES_SEWERS)
                || texture.equals("environment/walls_sewers.png")
                || texture.equals("environment/tiles_prison.png")
                || texture.equals("environment/tiles_caves.png")
                || texture.equals("environment/tiles_city.png")
                || texture.equals("environment/tiles_halls.png")
                || texture.startsWith("environment/custom_tiles/")
                || texture.equals(Assets.Environment.TERRAIN_FEATURES)
                || texture.equals(Assets.Environment.RAISED_TERRAIN)
                || texture.equals(Assets.Environment.TILES_CAVES_CRYSTAL)
                || texture.equals(Assets.Environment.TILES_CAVES_GNOLL) ? TILE_SIZE : WORLD_TILE_SIZE;
    }
    public static final int LOGICAL_TILE = WORLD_TILE_SIZE;
    public static final int TEX_TILE = TILE_SIZE, TEX_HERO_W = HERO_FRAME_W, TEX_HERO_H = HERO_FRAME_H;
    public static final int TEX_ITEM = ITEM_ICON, LOGICAL_ITEM = 8;
    public static final int HERO_DENSITY = HERO_FRAME_W / LEGACY_HERO_FRAME_W;
    public static final int ITEM_DENSITY = TEX_ITEM / LEGACY_ITEM_ICON;
    // Recovery manifest identifies whole upstream sheets enlarged exactly four times.
    private static java.util.Map<String,int[]> characterLayouts;
    private static final java.util.WeakHashMap<Object,int[]> textureLayouts=new java.util.WeakHashMap<>();
    private static int[] characterLayout(Object texture) {
        if(characterLayouts==null){
            characterLayouts=new java.util.LinkedHashMap<>();
            com.badlogic.gdx.utils.JsonValue json=new com.badlogic.gdx.utils.JsonReader().parse(
                    com.badlogic.gdx.Gdx.files.internal("sprites/character-layouts.json"));
            for(com.badlogic.gdx.utils.JsonValue entry:json)characterLayouts.put(entry.name,entry.asIntArray());
        }
        if(texture instanceof String)return characterLayouts.get(texture);
        if(textureLayouts.containsKey(texture))return textureLayouts.get(texture);
        for(java.util.Map.Entry<String,int[]> entry:characterLayouts.entrySet()){
            if(com.watabou.gltextures.TextureCache.contains(entry.getKey())
                    && com.watabou.gltextures.TextureCache.get(entry.getKey())==texture){
                textureLayouts.put(texture,entry.getValue());return entry.getValue();
            }
        }
        return null;
    }
    public static int characterDensity(Object texture) {
        return characterLayout(texture)==null?1:HERO_DENSITY;
    }
    // Alpha occupancy is measured once per atlas rectangle, not on every rendered frame.
    private static final java.util.WeakHashMap<com.watabou.gltextures.SmartTexture,java.util.Map<String,com.watabou.utils.RectF>> bounds = new java.util.WeakHashMap<>();
    public static com.watabou.utils.RectF opaqueBounds(com.watabou.gltextures.SmartTexture tx, com.watabou.utils.RectF frame) {
        int x=Math.round(frame.left*tx.width), y=Math.round(frame.top*tx.height);
        int w=Math.round(frame.width()*tx.width), h=Math.round(frame.height()*tx.height);
        String key=x+":"+y+":"+w+":"+h;
        java.util.Map<String,com.watabou.utils.RectF> cache=bounds.computeIfAbsent(tx,t->new java.util.HashMap<>());
        if(cache.containsKey(key))return new com.watabou.utils.RectF(cache.get(key));
        int left=w,right=-1,top=h,bottom=-1;
        for(int j=0;j<h;j++)for(int i=0;i<w;i++)if((tx.bitmap.getPixel(x+i,y+j)&255)!=0){
            left=Math.min(left,i);right=Math.max(right,i);top=Math.min(top,j);bottom=Math.max(bottom,j);
        }
        com.watabou.utils.RectF result=bottom<top?new com.watabou.utils.RectF():new com.watabou.utils.RectF(left,top,right+1,bottom+1);
        cache.put(key,result);return new com.watabou.utils.RectF(result);
    }
    public static int opaqueHeight(com.watabou.gltextures.SmartTexture tx, com.watabou.utils.RectF frame) {
        return Math.round(opaqueBounds(tx,frame).height());
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
        int density=characterDensity(texture);
        filterPaintedCharacter(texture);
        return new com.watabou.noosa.TextureFilm(texture, width*density, height*density);
    }
    private static java.util.Set<String> paintedCharacters;
    private static final java.util.WeakHashMap<com.watabou.gltextures.SmartTexture,Boolean> filteredCharacters = new java.util.WeakHashMap<>();
    public static void filterPaintedCharacter(Object source) {
        com.watabou.gltextures.SmartTexture texture = com.watabou.gltextures.TextureCache.get(source);
        if(filteredCharacters.containsKey(texture))return;
        if(paintedCharacters==null){
            paintedCharacters=new java.util.HashSet<>();
            com.badlogic.gdx.utils.JsonValue assets=new com.badlogic.gdx.utils.JsonReader().parse(
                    com.badlogic.gdx.Gdx.files.internal("painted-assets.json")).get("assets");
            for(com.badlogic.gdx.utils.JsonValue entry:assets)
                if(entry.name.startsWith("sprites/"))paintedCharacters.add(entry.name);
        }
        for(String path:paintedCharacters){
            if(com.watabou.gltextures.TextureCache.contains(path)
                    && com.watabou.gltextures.TextureCache.get(path)==texture){
                texture.filter(com.badlogic.gdx.graphics.GL20.GL_LINEAR,com.badlogic.gdx.graphics.GL20.GL_LINEAR);
                break;
            }
        }
        filteredCharacters.put(texture,Boolean.TRUE);
    }
    public static com.watabou.noosa.Image heroImage(Object texture) {
        com.watabou.noosa.Image image = new com.watabou.noosa.Image(texture) {
            @Override public void frame(com.watabou.utils.RectF frame) {
                super.frame(frame); width /= HERO_DENSITY; height /= HERO_DENSITY; updateVertices();
            }
        };
        image.texture.filter(com.badlogic.gdx.graphics.GL20.GL_LINEAR, com.badlogic.gdx.graphics.GL20.GL_LINEAR);
        return image;
    }
    public static com.watabou.noosa.Image heroImage(Object texture, int x, int y, int w, int h) {
        com.watabou.noosa.Image image = heroImage(texture);
        image.frame(x*HERO_DENSITY, y*HERO_DENSITY, w*HERO_DENSITY, h*HERO_DENSITY);
        return image;
    }
}
