// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel;

/** Resolve victory before the next combat turn, through the normal saved ally-transfer route. */
public class DragonVictoryPassage extends Buff {
    { actPriority = VFX_PRIO; revivePersists = true; }
    @Override public boolean act() {
        detach();
        if (DragonExpedition.victoryPending && Dungeon.hero.isAlive())
            DragonExpedition.travel(DragonExpedition.HOARD, DragonExpedition.BRANCH, DragonHoardLevel.VICTORY_ARRIVAL);
        return true;
    }
}
