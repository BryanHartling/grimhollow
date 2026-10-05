// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WaywardChart;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;

/** The exhausted cloth remains after the actual loot heap is emptied. */
public class WaywardMoundTile extends CustomTilemap {
    public int cacheID;
    {texture="environment/custom_tiles/wayward_cache.png";}
    @Override protected boolean knownSource(int cell){
        int pos=tileX+tileY*Dungeon.level.width();
        return Dungeon.level.visited[pos] || Dungeon.level.mapped[pos] || Dungeon.level.heroFOV[pos];
    }
    @Override public Tilemap create(){Tilemap result=super.create();updateKnowledge();return result;}
    @Override public void updateKnowledge(){
        if(vis==null)return;
        WaywardJourney.Cache c=WaywardJourney.cache(cacheID);
        // Live piles belong to the ordinary heap layer; only exhausted scenery lives here.
        vis.map(new int[]{c!=null && c.remaining==0?2:-1},1);vis.updateMap();
    }
    @Override public String name(int x,int y){return Messages.get(WaywardChart.class,"cache_name");}
    @Override public String desc(int x,int y){return Messages.get(WaywardChart.class,"cache_empty");}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("cache_id",cacheID);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);cacheID=b.getInt("cache_id");}
}
