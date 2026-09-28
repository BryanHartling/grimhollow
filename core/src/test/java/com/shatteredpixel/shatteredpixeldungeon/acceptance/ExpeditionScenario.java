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
    private static void maze() throws Exception {
        java.util.Set<Integer> exits = new java.util.HashSet<>();
        long originalSeed = Dungeon.seed;
        for (int seed = 0; seed < 64; seed++) {
            Dungeon.seed = seed; Dungeon.branch = DragonExpedition.BRANCH; Dungeon.depth = DragonExpedition.CHASM;
            com.shatteredpixel.shatteredpixeldungeon.levels.DragonChasmLevel level =
                    (com.shatteredpixel.shatteredpixeldungeon.levels.DragonChasmLevel) Dungeon.newLevel();
            Dungeon.switchLevel(level, -1);
            check(Dungeon.hero.pos == level.centerCell(), "central arrival");
            check(!Dungeon.interfloorTeleportAllowed(), "portable escape from expedition");
            check(level.addRespawner() == null && level.heaps.size == 0, "main dungeon supplies leak into branch");
            java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
            boolean[] reached = new boolean[level.length()];
            reached[level.entrance()] = true; queue.add(level.entrance());
            while (!queue.isEmpty()) {
                int c = queue.remove();
                for (int d : new int[]{-1, 1, -level.width(), level.width()}) {
                    int n = c + d;
                    if (level.insideMap(n) && level.passable[n] && !reached[n]) { reached[n] = true; queue.add(n); }
                }
            }
            int walkable = 0, pits = 0;
            for (int c = 0; c < level.length(); c++) {
                if (level.passable[c]) { walkable++; check(reached[c], "disconnected platform " + seed + ":" + c); check(!level.flamable[c], "platform can burn away"); }
                if (level.pit[c]) pits++;
            }
            check(pits > walkable && reached[level.exit()] && level.loopCount >= 1 && level.deadEnds >= 4, "maze lacks chasm/loops/dead ends");
            exits.add(level.exitIndex);
            Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
            Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
            check(java.util.Arrays.equals(level.map, Dungeon.level.map) && Dungeon.level.exit() == level.exit(), "maze rerolled after save/load");
        }
        check(exits.size() == 8, "all eight exit placements not exercised");
        Dungeon.seed = originalSeed;
        System.out.println("TEST 59 maze PASS: 64 seeds, 8 exit positions, all platforms reachable, loops/dead ends, nonflammable, disk persistence");
    }
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
        maze();
        System.out.println("TEST 59 foundation PASS: seeded placement, healing exchange, unique rewards, protected map, disk save/load, legacy defaults");
    }
}
