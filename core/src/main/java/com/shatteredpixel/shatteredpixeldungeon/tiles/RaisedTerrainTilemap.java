/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.HashSet;

public class RaisedTerrainTilemap extends DungeonTilemap {
    private final boolean behindActors;
    private final HashSet<Integer> previous = new HashSet<>();
    private static final HashSet<Integer> behindCells = new HashSet<>();
    private static final HashSet<Integer> next = new HashSet<>();
    public static HashSet<Integer> skipCells = new HashSet<>();

    public RaisedTerrainTilemap() { this(false); }
    public RaisedTerrainTilemap(boolean behindActors) {
        super(Assets.Environment.RAISED_TERRAIN);
        this.behindActors=behindActors;
        if(behindActors)behindCells.clear();
        skipCells.clear();
        map(Dungeon.level.map, Dungeon.level.width());
    }
    @Override public void draw() {
        if(behindActors)partitionActors();
        // Only rebuild cells crossing an upper silhouette. At rest both
        // batches remain cached; both passes reuse the same grass texture.
        for(int cell:previous)if(!behindCells.contains(cell))updateMapCell(cell);
        for(int cell:behindCells)if(!previous.contains(cell))updateMapCell(cell);
        previous.clear();previous.addAll(behindCells);
        super.draw();
    }
    private static void partitionActors() {
        next.clear();
        if(Dungeon.hero!=null)protectUpperBody(Dungeon.hero.sprite);
        // Actors may spawn/despawn on their worker thread; use its synchronized
        // snapshot rather than iterating the level's mutable mob collection.
        for(com.shatteredpixel.shatteredpixeldungeon.actors.Char actor:com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars())protectUpperBody(actor.sprite);
        behindCells.clear();behindCells.addAll(next);
    }
    private static void protectUpperBody(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite) {
        if(sprite==null||!sprite.visible||sprite.am<=0)return;
        float top=sprite.y,bottom=top+sprite.height()*.65f;
        int width=Dungeon.level.width(),height=Dungeon.level.height();
        int x0=Math.max(0,(int)Math.floor(sprite.x/SIZE)),x1=Math.min(width-1,(int)Math.floor((sprite.x+sprite.width()-.001f)/SIZE));
        int y0=Math.max(0,(int)Math.floor(top/SIZE)),y1=Math.min(height-1,(int)Math.floor((bottom-.001f)/SIZE));
        for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++) {
            int cell=y*width+x,tile=Dungeon.level.map[cell];
            // The foreground painting has pixels only in its lower half.
            if((tile==Terrain.HIGH_GRASS||tile==Terrain.FURROWED_GRASS)&&y*SIZE+SIZE/2f<bottom&&(y+1)*SIZE>top)next.add(cell);
        }
    }
    @Override protected int getTileVisual(int pos,int tile,boolean flat) {
        if(flat||skipCells.contains(pos)||behindActors!=behindCells.contains(pos))return -1;
        int regionOffset=(Dungeon.depth-1)/5*4;
        if(tile==Terrain.HIGH_GRASS) {
            return regionOffset+(DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.RAISED_HIGH_GRASS,pos)==DungeonTileSheet.RAISED_HIGH_GRASS_ALT?2:0);
        } else if(tile==Terrain.FURROWED_GRASS) {
            return regionOffset+1+(DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.RAISED_FURROWED_GRASS,pos)==DungeonTileSheet.RAISED_FURROWED_ALT?2:0);
        }
        return -1;
    }
}
