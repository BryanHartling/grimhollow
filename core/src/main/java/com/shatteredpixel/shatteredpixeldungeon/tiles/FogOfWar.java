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

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.utils.Rect;

import java.util.ArrayList;

public class FogOfWar extends Image {

	//first index is visibility type, second is brightness level
	private static final int FOG_COLORS[][] = new int[][]{{
			//visible
			0x00000000, //-1 brightness
			0x00000000, //0  brightness
			0x00000000, //1  brightness
			}, {
			//visited
			0xCC000000,
			0x99000000,
			0x55000000
			}, {
			//mapped
			0xCC112244,
			0x99193366,
			0x55224488
			}, {
			//invisible
			0xFF000000,
			0xFF000000,
			0xFF000000
			}};

	private static final int VISIBLE    =   0;
	private static final int VISITED    =   1;
	private static final int MAPPED     =   2;
	private static final int INVISIBLE  =   3;

	private int mapWidth;
	private int mapHeight;
	
	private int pWidth;
	private int pHeight;
	
	private int width2;
	private int height2;

	private volatile ArrayList<Rect> toUpdate;
	private volatile ArrayList<Rect> updating;

	private static final int PIX_PER_TILE = GameGeometry.FOG_SAMPLES_PER_TILE;
	private final String cacheKey;

	public FogOfWar( int mapWidth, int mapHeight ) {

		super();

		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;

		pWidth = mapWidth * PIX_PER_TILE;
		pHeight = mapHeight * PIX_PER_TILE;

		width2 = 1;
		while (width2 < pWidth) {
			width2 <<= 1;
		}

		height2 = 1;
		while (height2 < pHeight) {
			height2 <<= 1;
		}

		float size = (float)GameGeometry.WORLD_TILE_SIZE / PIX_PER_TILE;

		cacheKey = "FogOfWar" + width2 + "x" + height2;
		texture(TextureCache.create(cacheKey, width2, height2));
		// Linear filtering blends clear and opaque cells across visibility edges.
		texture.filter(Texture.NEAREST, Texture.NEAREST);

		//sets contents to all black
		texture.bitmap.setColor( 0x000000FF );
		texture.bitmap.fill();

		texture.bind();

		scale.set( size, size );

		toUpdate = new ArrayList<>();
		toUpdate.add(new Rect(0, 0, mapWidth, mapHeight));
	}

	public synchronized void updateFog(){
		toUpdate.clear();
		toUpdate.add(new Rect(0, 0, mapWidth, mapHeight));
	}
	
	public synchronized void updateFog(Rect update){
		for (Rect r : toUpdate.toArray(new Rect[0])){
			if (!r.intersect(update).isEmpty()){
				toUpdate.remove(r);
				toUpdate.add(r.union(update));
				return;
			}
		}
		toUpdate.add(update);
	}

	public synchronized void updateFog( int cell, int radius ){
		Rect update = new Rect(
				(cell % mapWidth) - radius,
				(cell / mapWidth) - radius,
				(cell % mapWidth) - radius + 1 + 2*radius,
				(cell / mapWidth) - radius + 1 + 2*radius);
		update.left = Math.max(0, update.left);
		update.top = Math.max(0, update.top);
		update.right = Math.min(mapWidth, update.right);
		update.bottom = Math.min(mapHeight, update.bottom);
		if (update.isEmpty()) return;
		updateFog( update );
	}

	public synchronized void updateFogArea(int x, int y, int w, int h){
		updateFog(new Rect(x, y, x + w, y + h));
	}

	private synchronized void moveToUpdating(){
		updating = toUpdate;
		toUpdate = new ArrayList<>();
	}

	private boolean[] visible;
	private boolean[] visited;
	private boolean[] mapped;
	private int brightness;
	
	private void updateTexture( boolean[] visible, boolean[] visited, boolean[] mapped ) {
		this.visible = visible;
		this.visited = visited;
		this.mapped = mapped;
		this.brightness = SPDSettings.brightness() + 1;

		moveToUpdating();
		
        Pixmap fog = texture.bitmap;
        fog.setBlending(Pixmap.Blending.None);
        for (Rect update : updating) {
            for (int y=Math.max(0,update.top); y<Math.min(mapHeight,update.bottom); y++) {
                for (int x=Math.max(0,update.left); x<Math.min(mapWidth,update.right); x++) {
                    int cell=x+y*mapWidth;
                    // FOV is authoritative for visible cells, including walls/doors.
                    // Wall overhang occlusion belongs to WallBlockingTilemap; it must
                    // not paint a different cell's visibility into this cell's mask.
                    fillCell(fog,x,y,FOG_COLORS[getCellFog(cell)][brightness]);
                }
            }
        }
        texture.bitmap(fog);
    }

	private int getCellFog( int cell ){

		if (visible[cell]) {
			return VISIBLE;
		} else if (visited[cell]) {
			return VISITED;
		} else if (mapped[cell] ) {
			return MAPPED;
		} else {
			return INVISIBLE;
		}
	}
	
	private void fillCell( Pixmap fog, int x, int y, int color){
		fog.setColor((color << 8) | (color >>> 24));
		fog.fillRectangle(x * PIX_PER_TILE, y*PIX_PER_TILE, PIX_PER_TILE, PIX_PER_TILE);
	}

	@Override
	protected NoosaScript script() {
		return NoosaScriptNoLighting.get();
	}

	@Override
	public void draw() {

		if (!toUpdate.isEmpty()){
			updateTexture(Dungeon.level.heroFOV, Dungeon.level.visited, Dungeon.level.mapped);
		}

		super.draw();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		if (texture != null){
			TextureCache.remove(cacheKey);
		}
	}
}
