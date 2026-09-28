// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.ExpeditionMap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

final class ExpeditionScenario {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError("59: " + message); }
    static void run() throws Exception {
        Dungeon.init();
        int chosen = DragonExpedition.hunterDepth;
        check(chosen >= 16 && chosen <= 19, "hunter placement outside City");
        Random.pushGenerator(109); long expected = Random.Long(); Random.popGenerator();
        Random.pushGenerator(109); DragonExpedition.reset();
        check(Random.Long() == expected && DragonExpedition.hunterDepth == chosen, "quest seed perturbs main RNG");
        Random.popGenerator();
        Dungeon.depth = chosen; Dungeon.switchLevel(Dungeon.newLevel(), -1);
        DragonExpedition.hunterPos = Dungeon.hero.pos;
        for (PotionOfHealing p : Dungeon.hero.belongings.getAllItems(PotionOfHealing.class)) p.detachAll(Dungeon.hero.belongings.backpack);
        check(!DragonExpedition.accept(Dungeon.hero), "free map without healing potion");
        new PotionOfHealing().quantity(2).collect();
        check(DragonExpedition.accept(Dungeon.hero), "healing exchange failed");
        check(Dungeon.hero.belongings.getItem(PotionOfHealing.class).quantity() == 1, "exchange consumes exactly one potion");
        check(Dungeon.hero.belongings.getItem(ExpeditionMap.class) != null
                && Dungeon.hero.belongings.getItem(ElixirOfFeatherFall.class) != null, "missing quest supplies");
        check(!DragonExpedition.accept(Dungeon.hero), "repeat reward");
        DragonExpedition.entered = true; DragonExpedition.returnCell = Dungeon.hero.pos;
        DragonExpedition.spiderSlain = true;
        Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
        Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
        check(DragonExpedition.accepted && DragonExpedition.entered && DragonExpedition.spiderSlain
                && DragonExpedition.returnCell == Dungeon.hero.pos, "actual save/load loses quest state");
        check(new ExpeditionMap().unique && !new ExpeditionMap().isUpgradable(), "map must be protected quest item");
        DragonExpedition.restore(new Bundle());
        check(!DragonExpedition.accepted && !DragonExpedition.entered && DragonExpedition.returnCell == -1, "old save/new run inherits quest");
        check(DragonExpedition.BRANCH != 1, "expedition aliases Vault branch");
        System.out.println("TEST 59 foundation PASS: seeded placement, healing exchange, unique rewards, protected map, disk save/load, legacy defaults");
    }
}
