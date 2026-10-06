// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.*;
import com.watabou.utils.Random;
import java.util.ArrayList;

/** One independent seed-derived opportunity per region, with a saved run-wide cap. */
public final class HorrorHunts {
    private HorrorHunts() {}
    public static int selectedFloor(long seed,int region) {
        if(region<0 || region>4)return -1;
        Random.pushGenerator(seed ^ (0x4852524f52L + 7919L*region));
        try {
            if(Random.Int(100)>=BalanceTuning.get(BalanceTuning.Key.HORROR_CHANCE))return -1;
            return region==0 ? 2+Random.Int(3) : region*5+1+Random.Int(4);
        } finally { Random.popGenerator(); }
    }
    public static void populate(Level level) {
        int region=(Dungeon.depth-1)/5;
        if(!GenerationToggles.allowed(LurkingHorror.class) || Dungeon.branch!=0 || BalanceTuning.get(BalanceTuning.Key.DENSITY)==0
                || !(level instanceof RegularLevel) || Dungeon.depth<2 || Dungeon.depth>24
                || Dungeon.bossLevel() || selectedFloor(Dungeon.seed,region)!=Dungeon.depth
                || (Dungeon.LimitedDrops.HORROR_REGIONS.count & (1<<region))!=0) return;
        RegularLevel regular=(RegularLevel)level;
        ArrayList<Integer> candidates=new ArrayList<>();
        for(Room room:regular.rooms()) {
            if(!(room instanceof StandardRoom) || room.inside(level.cellToPoint(level.entrance()))
                    || room.inside(level.cellToPoint(level.exit())))continue;
            boolean occupied=false;
            for(Mob mob:level.mobs) if(room.inside(level.cellToPoint(mob.pos))) { occupied=true; break; }
            if(occupied)continue;
            for(int y=room.top+1;y<room.bottom;y++) for(int x=room.left+1;x<room.right;x++) {
                int cell=x+y*level.width();
                if(level.passable[cell] && !level.pit[cell] && !level.water[cell] && level.traps.get(cell)==null
                        && level.heaps.get(cell)==null && level.distance(cell,level.entrance())>=8) candidates.add(cell);
            }
        }
        if(candidates.isEmpty())return;
        LurkingHorror horror=new LurkingHorror(); horror.pos=Random.element(candidates);
        level.mobs.add(horror); Dungeon.LimitedDrops.HORROR_REGIONS.count|=1<<region;
    }
}
