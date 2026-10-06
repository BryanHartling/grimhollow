// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

/** Runs inside the existing real-generator smoke harness. */
final class BountyScenario {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError("Bounty Board: " + message);
    }
    static void run() {
        boolean enabled = BountyBoard.AVAILABLE;
        try {
            BountyBoard.AVAILABLE = true;
            BountyBoard.restore(new Bundle());
            check(!BountyBoard.present && BountyBoard.officeCell == -1, "old-save absent default");
            for (int seed = 0; seed < 10; seed++) {
                Dungeon.init(); Dungeon.seed = seed; Dungeon.depth = 7; Dungeon.branch = 0;
                Level level = Dungeon.newLevel(); Dungeon.switchLevel(level, -1);
                Cole cole = null; int slots = 0;
                for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : level.mobs)
                    if (mob instanceof Cole) { check(cole == null, "duplicate office actor"); cole = (Cole)mob; }
                check(cole != null, "missing office");
                java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
                boolean[] reached = new boolean[level.length()]; reached[level.entrance()] = true; queue.add(level.entrance());
                while (!queue.isEmpty()) {
                    int cell = queue.remove();
                    for (int offset : new int[]{-1, 1, -level.width(), level.width()}) {
                        int next = cell+offset;
                        if (level.insideMap(next) && level.passable[next] && !reached[next]) { reached[next]=true; queue.add(next); }
                    }
                }
                check(reached[cole.pos] && reached[level.exit()], "office or route unreachable");
                Heap bought = null;
                for (Heap heap : level.heaps.valueList()) if (BountyBoard.owns(heap)) {
                    slots++; check(heap.type == Heap.Type.FOR_SALE && heap.size() == 1, "invalid slot");
                    check(heap.salePrice() == Shopkeeper.sellPrice(heap.peek())*2, "double-standard quote");
                    if (heap.coleSlot == 0) { check(heap.salePrice() == 200, "ration price"); bought = heap; }
                }
                check(slots == 5 && BountyBoard.prices[4] == 600, "fixed five slots and Brand bundle");
                int[] quotes = BountyBoard.prices.clone();
                BountyBoard.takeStock(bought); bought.pickUp();
                Bundle saved = new Bundle(); BountyBoard.store(saved); BountyBoard.restore(saved);
                BountyBoard.planShop();
                check(BountyBoard.stock[0] == null && java.util.Arrays.equals(quotes, BountyBoard.prices), "stock/quote reroll");
                Heap unrelated = level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.food.Food(), level.exit());
                unrelated.type = Heap.Type.FOR_SALE;
                BountyBoard.failedTheft();
                check(level.mobs.contains(cole) && level.heaps.get(unrelated.pos) == unrelated, "vendor isolation");
                BountyBoard.store(saved); BountyBoard.restore(saved);
                check(BountyBoard.shopClosed && BountyBoard.stock[1] != null, "shop closure persistence");
                for (Heap heap : level.heaps.valueList()) check(!BountyBoard.owns(heap), "closed stock still available");
                Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
                check(BountyBoard.shopClosed && BountyBoard.stock[0] == null, "disk-save receipts");
            }
        } catch (java.io.IOException error) { throw new AssertionError(error); }
        finally { BountyBoard.AVAILABLE = enabled; BountyBoard.reset(); }
    }
}
