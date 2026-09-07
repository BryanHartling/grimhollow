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
                || texture.equals("environment/walls_sewers.png") ? TILE_SIZE : WORLD_TILE_SIZE;
    }
    public static final int LOGICAL_TILE = WORLD_TILE_SIZE;
    public static final int TEX_TILE = TILE_SIZE, TEX_HERO_W = HERO_FRAME_W, TEX_HERO_H = HERO_FRAME_H;
    public static final int TEX_ITEM = ITEM_ICON, LOGICAL_ITEM = 8;
    public static final int HERO_DENSITY = HERO_FRAME_W / LEGACY_HERO_FRAME_W;
    public static final int ITEM_DENSITY = TEX_ITEM / LEGACY_ITEM_ICON;
    public static int characterDensity(Object texture) {
        if (texture instanceof String) return ((String)texture).startsWith("sprites/hero_") ? HERO_DENSITY : 1;
        // Converted hero atlases retain upstream's eight armor rows and padding.
        if (texture instanceof com.watabou.gltextures.SmartTexture) {
            com.watabou.gltextures.SmartTexture t = (com.watabou.gltextures.SmartTexture)texture;
            return t.width == 256*HERO_DENSITY && t.height == 128*HERO_DENSITY ? HERO_DENSITY : 1;
        }
        return 1;
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