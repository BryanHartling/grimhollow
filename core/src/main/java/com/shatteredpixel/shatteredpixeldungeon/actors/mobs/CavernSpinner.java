// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.watabou.utils.Bundle;

/** The initial scavengers are finite rewards; all replenished spiders are threats only. */
public class CavernSpinner extends Spinner {
    public boolean hatchling, replenished;
    public void setHatchling() { hatchling = true; EXP = 0; lootChance = 0; }
    public void setReplenished() { replenished = true; generatedRespawn = true; EXP = 0; lootChance = 0; }
    public void patrolToward(int cell) { state = WANDERING; target = cell; }
    @Override public com.shatteredpixel.shatteredpixeldungeon.items.Item createLoot() {
        return hatchling || replenished ? null : super.createLoot();
    }
    @Override public void rollToDropLoot() {
        // Suppress Wealth/Lucky bonus drops too, not just the ordinary meat roll.
        if (!hatchling && !replenished) super.rollToDropLoot();
    }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b); b.put("hatchling", hatchling); b.put("replenished", replenished);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b); if (b.getBoolean("hatchling")) setHatchling();
        if (b.getBoolean("replenished")) setReplenished();
    }
}
