// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;
import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
/** A one-door side room; never the required path to the Halls exit. */
public class ColeMeetingRoom extends SpecialRoom {
    @Override public int minWidth(){return 6;}
    @Override public int minHeight(){return 6;}
    @Override public int maxWidth(){return 8;}
    @Override public int maxHeight(){return 8;}
    @Override public void paint(Level level){
        Painter.fill(level,this,Terrain.WALL);Painter.fill(level,this,1,Terrain.EMPTY_SP);
        for(Door d:connected.values())d.set(Door.Type.REGULAR);
        BountyBoard.hallsCell=level.pointToCell(center());
    }
}
