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
}
