// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.noosa.Game;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.particles.Emitter;

/** Batched animated liquid beneath the existing terrain/bank masks. */
public class LiquidTilemap extends Tilemap {
    private float time;
    private int tick=-1;
    public LiquidTilemap() {
        super(Dungeon.level.waterTex(),new TextureFilm(Dungeon.level.waterTex(),64,64));
        cellSize(16,16);
        map(new int[Dungeon.level.length()],Dungeon.level.width());
        refresh(0);
    }
    public static int hash(int x,int y) {
        int value=x*0x1f123bb5 ^ y*0x5f356495;
        value^=value>>>16;value*=0x45d9f3b;value^=value>>>16;
        return value;
    }
    /** Coordinate parity prevents even diagonal neighbours from synchronizing. */
    public static int phase(int x,int y) { return (x&1)|((y&1)<<1)|((hash(x,y)&1)<<2); }
    public static int variant(int x,int y) { return (hash(x,y)>>>8)&3; }
    public static int frame(int x,int y,int tick) { return variant(x,y)*8+((tick+phase(x,y))&7); }
    private void refresh(int next) {
        tick=next;
        for(int cell=0;cell<data.length;cell++)data[cell]=frame(cell%mapWidth,cell/mapWidth,tick);
        updateMap();
    }
    @Override public void update() {
        super.update();
        if(!Emitter.freezeEmitters)time+=Game.elapsed;
        int next=(int)(time*8)&7;
        if(next!=tick)refresh(next);
    }
    @Override protected NoosaScript script(){return NoosaScriptNoLighting.get();}
}
