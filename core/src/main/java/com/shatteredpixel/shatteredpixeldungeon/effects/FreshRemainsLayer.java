// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.FreshRemains;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.*;
import com.watabou.utils.PointF;
import java.util.HashMap;

/** Below normal fog and loot; no markers or extra information outside normal exploration. */
public class FreshRemainsLayer extends Group {
    private final HashMap<Integer,Image> images=new HashMap<>();
    @Override public void update() {
        for(Integer cell:new java.util.HashSet<>(images.keySet())) if(Dungeon.level.freshRemains.get(cell)==null) {
            Image old=images.remove(cell);remove(old);old.destroy();
        }
        for(FreshRemains remains:Dungeon.level.freshRemains.valueList()) {
            Image image=images.get(remains.pos);
            if(image==null) {
                image=remains.image(); images.put(remains.pos,image); add(image);
                PointF p=DungeonTilemap.tileCenterToWorld(remains.pos);
                image.x=p.x-image.width()/2; image.y=p.y-image.height()/2;
            }
            image.visible=remains.seen;
        }
        super.update();
    }
}
