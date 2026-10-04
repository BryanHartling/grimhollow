// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

/** Upper blades belong behind actors; only RaisedTerrainTilemap covers their feet. */
public class GrassOverhangTilemap extends DungeonTilemap {
    public GrassOverhangTilemap() {
        super(Dungeon.level.tilesTex());
        map(Dungeon.level.map, Dungeon.level.width());
    }
    @Override protected int getTileVisual(int pos, int tile, boolean flat) {
        int below=pos+mapWidth;
        if(flat || below>=map.length || DungeonWallsTilemap.skipCells.contains(pos))return -1;
        if(!Dungeon.level.heroFOV[below] && !Dungeon.level.visited[below] && !Dungeon.level.mapped[below])return -1;
        if(DungeonTileSheet.wallStitcheable(tile))return -1;
        if(map[below]==Terrain.HIGH_GRASS)return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.HIGH_GRASS_OVERHANG,below);
        if(map[below]==Terrain.FURROWED_GRASS)return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.FURROWED_OVERHANG,below);
        return -1;
    }
}
