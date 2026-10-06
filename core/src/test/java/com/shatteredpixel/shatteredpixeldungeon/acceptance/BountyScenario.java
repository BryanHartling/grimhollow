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
            contracts();
            System.out.println("BOUNTY COMPONENTS 1-2 PASS: ten reachable offices, finite double-price stock, isolated closure, disk receipts; accepted-only saved targets, real deaths, timing, Warrants and once-only rewards");
        } catch (java.io.IOException error) { throw new AssertionError(error); }
        finally { BountyBoard.AVAILABLE = enabled; BountyBoard.reset(); }
    }
    private static void contracts() throws java.io.IOException {
        Dungeon.init(); Dungeon.depth=7; Dungeon.branch=0;
        Dungeon.switchLevel(Dungeon.newLevel(), -1);
        Dungeon.hero.lvl=30; Dungeon.hero.sprite = new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
        Dungeon.hero.sprite.link(Dungeon.hero);
        BountyBoard.planContracts();
        for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs)
            check(mob.bountyContract < 0, "unaccepted target spawned");
        for (int i=0;i<3;i++) BountyBoard.contracts[i].floor=7; // Deliberate same-floor overlap fixture.
        String title=BountyBoard.contracts[0].title(), alias=BountyBoard.contracts[0].alias();
        check(!BountyBoard.contracts[0].accepted && !BountyBoard.bossUnlocked(), "examination accepts / early boss");
        check(com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract.text(BountyBoard.contracts[0]).contains("URGENT BOUNTY")
                && !com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract.text(BountyBoard.contracts[0]).contains("500"), "implicit urgency");
        for (int i=0;i<3;i++) check(BountyBoard.accept(i) && !BountyBoard.accept(i), "acceptance duplication");
        int targets=0;
        for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs) if(mob.bountyContract>=0) {
            targets++; check(!Dungeon.level.heroFOV[mob.pos], "target appears in immediate sight");
            check(mob.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedVariant.class)==null, "double variant");
            mob.sprite=mob.sprite(); mob.sprite.link(mob);
        }
        check(targets==3 && com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(0), "accepted overlap / claim issuance");
        BountyBoard.onHeroSpent(499.5f);
        BountyBoard.Contract common=BountyBoard.contracts[0], rare=BountyBoard.contracts[1];
        common.target.HP=0; common.target.die(Dungeon.hero);
        check(common.complete && common.bonusEarned && common.amount()==720, "real hero kill/earned timer");
        BountyBoard.onHeroSpent(1f);
        rare.target.HP=0; rare.target.die(com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.class);
        check(rare.complete && !rare.bonusEarned && rare.amount()==1200, "environment kill/expired timer");
        Bundle saved=new Bundle(); BountyBoard.store(saved); BountyBoard.restore(saved);
        check(BountyBoard.contracts[0].title().equals(title)&&BountyBoard.contracts[0].alias().equals(alias),"seeded identity load");
        int gold=Dungeon.gold;
        check(BountyBoard.returnClaim(0)&&!BountyBoard.returnClaim(0),"payment duplicate");
        check(Dungeon.gold-gold==720&&!com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(0),"payment receipt / consumed paper");
        check(BountyBoard.returnClaim(1)&&BountyBoard.bossUnlocked(), "two cash contracts unlock boss");
        BountyBoard.store(saved); BountyBoard.restore(saved);
        check(!BountyBoard.returnClaim(0), "reload repayment");
        com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant one = new com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant();
        one.hunter=true; one.targetName="hero"; one.payment=7000; one.claims.add(0);
        com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant two = new com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant();
        two.hunter=true; two.targetName="hero"; two.payment=7000; two.claims.add(1);
        one.merge(two); check(one.quantity()==2&&two.quantity()==0, "hunter claim stacking");
        Item split=one.split(1); one.merge(split); check(one.quantity()==2&&one.claims.size()==2,"split/merge IDs");
        Bundle paper=new Bundle(); paper.put("paper",one);
        check(((com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant)paper.get("paper")).claims.size()==2,"saved stack IDs");
        check(com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.protectedItem(one,Dungeon.hero)&&one.value()==0,"protected quest paper");
        check(!one.isSimilar(new com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant(2)),"incompatible claims merge");
        BountyBoard.Contract legendary=BountyBoard.contracts[2];
        BountyBoard.arrive(Dungeon.level); // Bind the authoritative restored record to the loaded floor actor.
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob savedTarget=legendary.target;
        Dungeon.level.mobs.remove(savedTarget); BountyBoard.arrive(Dungeon.level);
        check(Dungeon.level.mobs.contains(savedTarget)&&legendary.spawned,"rebuild loses pending target");
    }
}
