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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.HashSet;

public class DungeonWallsTilemap extends DungeonTilemap {

    @Override protected com.watabou.noosa.NoosaScript script() {
        return LightingOverlay.walls();
    }

	public static HashSet<Integer> skipCells = new HashSet<>();

	public DungeonWallsTilemap(){
		super(Dungeon.level.tilesTex().equals("environment/tiles_sewers.png")
				? "environment/walls_sewers.png" : Dungeon.level.tilesTex());
		skipCells.clear();
		map( Dungeon.level.map, Dungeon.level.width() );
	}

	@Override
	protected int getTileVisual(int pos, int tile, boolean flat){

		if (flat) return -1;

		// An overhang must not disclose an unseen wall, prop or door below a
		// visible tile. The fog quad masks the destination, not the source.
        boolean belowKnown = pos + mapWidth < size && known(pos + mapWidth);
        if (!belowKnown && (tile == Terrain.LOCKED_EXIT || tile == Terrain.UNLOCKED_EXIT)
                && !skipCells.contains(pos)) return DungeonTileSheet.EXIT_UNDERHANG;
        if (!DungeonTileSheet.wallStitcheable(tile) && !Dungeon.level.heroFOV[pos]
                && (Dungeon.level.visited[pos] || Dungeon.level.mapped[pos])
                && !(belowKnown && DungeonTileSheet.wallStitcheable(knownTerrain(pos+mapWidth)))) return -1;


		if (DungeonTileSheet.wallStitcheable(tile)) {
			if (pos + mapWidth < size && !DungeonTileSheet.wallStitcheable(knownTerrain(pos + mapWidth))){

				if (!belowKnown) {
					// The exit underhang belongs to this cell, not its unknown neighbor.
					return (tile == Terrain.LOCKED_EXIT || tile == Terrain.UNLOCKED_EXIT)
							&& !skipCells.contains(pos) ? DungeonTileSheet.EXIT_UNDERHANG : -1;
				}
				if (knownTerrain(pos + mapWidth) == Terrain.DOOR){
					return DungeonTileSheet.DOOR_SIDEWAYS;
				} else if (knownTerrain(pos + mapWidth) == Terrain.LOCKED_DOOR) {
					return DungeonTileSheet.DOOR_SIDEWAYS_LOCKED;
				} else if (knownTerrain(pos + mapWidth) == Terrain.HERO_LKD_DR){
					return DungeonTileSheet.DOOR_SIDEWAYS_LOCKED;
				} else if (knownTerrain(pos + mapWidth) == Terrain.CRYSTAL_DOOR){
					return DungeonTileSheet.DOOR_SIDEWAYS_CRYSTAL;
				} else if (knownTerrain(pos + mapWidth) == Terrain.OPEN_DOOR){
					return DungeonTileSheet.NULL_TILE;
				}

			} else {
				return DungeonTileSheet.stitchInternalWallTile(
						tile,
						(pos+1) % mapWidth != 0 ?                           knownTerrain(pos + 1) : -1,
						(pos+1) % mapWidth != 0 && pos + mapWidth < size ?  knownTerrain(pos + 1 + mapWidth) : -1,
						pos + mapWidth < size ?                             knownTerrain(pos + mapWidth) : -1,
						pos % mapWidth != 0 && pos + mapWidth < size ?      knownTerrain(pos - 1 + mapWidth) : -1,
						pos % mapWidth != 0 ?                               knownTerrain(pos - 1) : -1
				);
			}

		}

		if (skipCells.contains(pos)){
			return -1;
		}
		if (knownTerrain(pos) == Terrain.LOCKED_EXIT || knownTerrain(pos) == Terrain.UNLOCKED_EXIT){
			return DungeonTileSheet.EXIT_UNDERHANG;
		} else if (!belowKnown) {
			return -1;
		} else if (pos + mapWidth < size && DungeonTileSheet.wallStitcheable(knownTerrain(pos+mapWidth))) {

			return DungeonTileSheet.stitchWallOverhangTile(
					tile,
					(pos+1) % mapWidth != 0 ?   knownTerrain(pos + 1 + mapWidth) : -1,
												knownTerrain(pos + mapWidth),
					pos % mapWidth != 0 ?       knownTerrain(pos - 1 + mapWidth) : -1
			);

		} else if (Dungeon.level.insideMap(pos) && knownTerrain(pos+mapWidth) == Terrain.DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG;
		} else if (Dungeon.level.insideMap(pos) && knownTerrain(pos+mapWidth) == Terrain.LOCKED_DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG;
		} else if (Dungeon.level.insideMap(pos) && knownTerrain(pos+mapWidth) == Terrain.HERO_LKD_DR ) {
			return DungeonTileSheet.DOOR_OVERHANG;
		} else if (Dungeon.level.insideMap(pos) && knownTerrain(pos+mapWidth) == Terrain.OPEN_DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG_OPEN;
		} else if (Dungeon.level.insideMap(pos) && knownTerrain(pos+mapWidth) == Terrain.CRYSTAL_DOOR ) {
			return DungeonTileSheet.DOOR_OVERHANG_CRYSTAL;
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.STATUE){
			return DungeonTileSheet.STATUE_OVERHANG;
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.STATUE_SP){
			return DungeonTileSheet.STATUE_SP_OVERHANG;
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.REGION_DECO){
			return DungeonTileSheet.REGION_DECO_OVERHANG;
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.REGION_DECO_ALT){
			return DungeonTileSheet.REGION_DECO_ALT_OVERHANG;
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.MINE_CRYSTAL){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.MINE_CRYSTAL_OVERHANG_BLUE, pos + mapWidth);
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.MINE_BOULDER){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.MINE_BOULDER_OVERHANG, pos + mapWidth);
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.ALCHEMY){
			return DungeonTileSheet.ALCHEMY_POT_OVERHANG;
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.BARRICADE){
			return -1; // BarricadeLayer draws the complete boards inside their own cell.
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.HIGH_GRASS){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.HIGH_GRASS_OVERHANG, pos + mapWidth);
		} else if (pos + mapWidth < size && knownTerrain(pos+mapWidth) == Terrain.FURROWED_GRASS){
			return DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.FURROWED_OVERHANG, pos + mapWidth);
		}

		return -1;
	}

	@Override
	public boolean overlapsPoint( float x, float y ) {
		return true;
	}
    private int knownTerrain(int cell){
        return cell>=0&&cell<size&&known(cell)?map[cell]:Terrain.WALL;
    }
	private boolean known(int cell){
		return Dungeon.level.heroFOV[cell] || Dungeon.level.visited[cell] || Dungeon.level.mapped[cell];
	}

	@Override
	public boolean overlapsScreenPoint( int x, int y ) {
		return true;
	}
	
}
