// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;
import java.util.Arrays;

/** Decorative furnishings leave the room's floor flags and movement unchanged. */
public class BountyOfficeTiles extends CustomTilemap {
    private int[] data;
    public BountyOfficeTiles() { texture="environment/custom_tiles/wardens_office.png"; }
    public BountyOfficeTiles(int x,int y,int w,int h) {
        this();setRect(x,y,w,h);data=new int[w*h];Arrays.fill(data,-1);
    }
    public void put(int x,int y,int frame) {
        if(x>=tileX && y>=tileY && x<tileX+tileW && y<tileY+tileH)
            data[x-tileX+(y-tileY)*tileW]=frame;
    }
    @Override public Tilemap create() { super.create();vis.map(data,tileW);return vis; }
    @Override public void storeInBundle(Bundle b) { super.storeInBundle(b);b.put("office_art",data); }
    @Override public void restoreFromBundle(Bundle b) { super.restoreFromBundle(b);data=b.getIntArray("office_art"); }
}
