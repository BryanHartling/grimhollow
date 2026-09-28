// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.watabou.utils.Bundle;

/** Only broodmother-spawned spiders suppress loot and experience. */
public class CavernSpinner extends Spinner {
    public boolean hatchling;
    public void setHatchling() { hatchling = true; EXP = 0; lootChance = 0; }
    @Override public void storeInBundle(Bundle b) { super.storeInBundle(b); b.put("hatchling", hatchling); }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b); if (b.getBoolean("hatchling")) setHatchling();
    }
}
