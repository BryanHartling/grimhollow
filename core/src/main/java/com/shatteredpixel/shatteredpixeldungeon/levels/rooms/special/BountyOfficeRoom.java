// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;

/** Added alongside, rather than drawn from, the normal special-room budget. */
public class BountyOfficeRoom extends SpecialRoom {
    @Override public int minWidth() { return 7; }
    @Override public int minHeight() { return 7; }
    @Override public int maxWidth() { return 9; }
    @Override public int maxHeight() { return 9; }
    @Override public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);
        for (Door d : connected.values()) d.set(Door.Type.REGULAR);
        BountyBoard.planShop();
        Cole cole = new Cole(); cole.pos = BountyBoard.officeCell = level.pointToCell(center());
        level.mobs.add(cole);
        int index = 0;
        for (int y = top+1; y < bottom && index < 5; y++) for (int x = left+1; x < right && index < 5; x++) {
            int cell = x + y * level.width();
            if (cell == cole.pos || Math.abs(cell % level.width() - entrance().x)
                    + Math.abs(cell / level.width() - entrance().y) <= 1) continue;
            if (!BountyBoard.shopClosed && BountyBoard.stock[index] != null) {
                Heap heap = level.drop(BountyBoard.stock[index], cell);
                heap.type = Heap.Type.FOR_SALE; heap.coleSlot = index;
            }
            index++;
        }
    }
}
