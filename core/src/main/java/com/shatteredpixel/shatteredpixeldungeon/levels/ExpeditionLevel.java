// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Random;
import java.util.ArrayList;

/** Finite side-quest floors never use the main dungeon's respawn or supply schedules. */
public abstract class ExpeditionLevel extends Level {
    @Override public String tilesTex() { return Assets.Environment.TILES_CAVES; }
    @Override public String waterTex() { return Assets.Environment.WATER_CAVES; }
    @Override public Mob createMob() { return null; }
    @Override public Actor addRespawner() { return null; }
    @Override protected void createMobs() {}
    @Override protected void createItems() {}
    @Override public int randomRespawnCell(Char ch) {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int i = 0; i < length(); i++) {
            if (insideMap(i) && passable[i] && !pit[i] && findMob(i) == null
                    && (Dungeon.hero == null || Dungeon.hero.pos != i) && getTransition(i) == null) cells.add(i);
        }
        return cells.isEmpty() ? -1 : Random.element(cells);
    }
}
