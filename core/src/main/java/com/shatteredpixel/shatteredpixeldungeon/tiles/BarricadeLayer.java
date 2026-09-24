// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

/** Uses the existing painted boards, oriented across the blocked passage. */
public class BarricadeLayer extends Group {
    private final Image[] boards = new Image[Dungeon.level.length()];
    public static boolean vertical(Level level, int cell) {
        int w = level.width();
        if (!level.insideMap(cell)) return false;
        return level.solid[cell-w] && level.solid[cell+w]
                && (!level.solid[cell-1] || !level.solid[cell+1]);
    }
    @Override public void update() {
        Level level = Dungeon.level;
        for (int cell = 0; cell < boards.length; cell++) {
            boolean shown = level.map[cell] == Terrain.BARRICADE
                    && (level.heroFOV[cell] || level.visited[cell] || level.mapped[cell]);
            Image image = boards[cell];
            if (shown) {
                if (image == null) {
                    Image source = TerrainFeaturesTilemap.tile(cell, Terrain.BARRICADE);
                    image = boards[cell] = new Image(source.texture) {
                        @Override protected com.watabou.noosa.NoosaScript script(){ return LightingOverlay.walls(); }
                    };
                    RectF uv = new RectF(source.frame());
                    RectF bounds = com.shatteredpixel.shatteredpixeldungeon.GameGeometry.opaqueBounds(source.texture,uv);
                    float left=uv.left+Math.max(0,bounds.left-.5f)/source.texture.width;
                    float top=uv.top+Math.max(0,bounds.top-.5f)/source.texture.height;
                    uv.set(left,top,left+bounds.width()/source.texture.width,top+bounds.height()/source.texture.height);
                    image.frame(uv); image.logicalSize(16,16*bounds.height()/bounds.width());
                    image.point(DungeonTilemap.tileToWorld(cell));
                    image.y+=(16-image.height)/2;
                    image.origin.set(image.width/2,image.height/2); add(image);
                    source.destroy();
                }
                image.angle = vertical(level,cell) ? 90 : 0;
            }
            if (image != null) image.visible = shown;
        }
    }
}
