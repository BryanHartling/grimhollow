// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BloodmarkedBrand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** One authoritative quest record, independent of floor files and physical claims. */
public final class BountyBoard {
    private BountyBoard() {}
    // Components remain isolated from ordinary runs until the full quest is integrated.
    public static boolean AVAILABLE = false;
    public static boolean present, shopClosed;
    public static final int OFFICE_DEPTH = 7;
    public static int officeCell = -1;
    public static Item[] stock = new Item[5];
    public static int[] prices = new int[5];
    public static Contract[] contracts = new Contract[4];
    public static int bossChoice = -1, heroBounty;
    public static boolean bossDefeated, betrayed, departed;
    private static boolean dialogueOpen;

    public static class Contract implements com.watabou.utils.Bundlable {
        public int index, species, floor, traits, payment;
        public boolean accepted, spawned, complete, returned, paid, issued, clockStarted, bonusEarned;
        public float elapsed;
        public int deadline = 500, bonusPercent = 20;
        public Mob target;
        public boolean coatIssued;
        public Contract() {}
        public String alias() { return index == 3 ? preview().name() : Messages.get(Cole.class, "alias_" + (index == 2 ? "warden" : species)); }
        public String title() { return index == 3 ? Messages.get(Cole.class,"boss_title",alias()) : Messages.get(Cole.class, "title_" + (index == 2 ? "warden" : species)); }
        public int amount() { return payment + (bonusEarned ? payment * bonusPercent / 100 : 0); }
        public Mob preview() {
            Mob mob;
            switch (species) {
                case 1: mob = new Thief(); break;
                case 2: mob = new Guard(); break;
                case 3: mob = new DM100(); break;
                case 4: mob = new Necromancer(); break;
                case 5: mob = new Tengu(); break;
                case 6: mob = new Chainwarden(); break;
                default: mob = new Skeleton();
            }
            return mob;
        }
        @Override public void storeInBundle(Bundle b) {
            b.put("index", index); b.put("species", species); b.put("floor", floor);
            b.put("traits", traits); b.put("payment", payment); b.put("accepted", accepted);
            b.put("spawned", spawned); b.put("complete", complete); b.put("returned", returned);
            b.put("paid", paid); b.put("issued", issued); b.put("clock_started", clockStarted);
            b.put("elapsed", elapsed); b.put("bonus_earned", bonusEarned);
            b.put("deadline", deadline); b.put("bonus_percent", bonusPercent);
            b.put("target", target);
            b.put("coat_issued", coatIssued);
        }
        @Override public void restoreFromBundle(Bundle b) {
            index = b.getInt("index"); species = b.getInt("species"); floor = b.getInt("floor");
            traits = b.getInt("traits"); payment = b.getInt("payment"); accepted = b.getBoolean("accepted");
            spawned = b.getBoolean("spawned"); complete = b.getBoolean("complete"); returned = b.getBoolean("returned");
            paid = b.getBoolean("paid"); issued = b.getBoolean("issued"); clockStarted = b.getBoolean("clock_started");
            elapsed = b.getFloat("elapsed"); bonusEarned = b.getBoolean("bonus_earned");
            deadline = b.contains("deadline") ? b.getInt("deadline") : 500;
            bonusPercent = b.contains("bonus_percent") ? b.getInt("bonus_percent") : 20;
            target = (Mob)b.get("target");
            coatIssued = b.getBoolean("coat_issued");
        }
    }

    public static void reset() {
        present = AVAILABLE;
        shopClosed = false; officeCell = -1;
        stock = new Item[5]; prices = new int[5];
        contracts = new Contract[4];
        bossChoice=-1; heroBounty=0; bossDefeated=betrayed=departed=dialogueOpen=false;
    }

    public static void planContracts() {
        if (!present || contracts[0] != null) return;
        Random.pushGenerator(Dungeon.seed ^ 0x434F4E5452414354L);
        try {
            for (int i = 0; i < 3; i++) {
                Contract c = contracts[i] = new Contract(); c.index = i;
                c.floor = Random.IntRange(7, 9);
                c.species = i == 0 ? Random.Int(2) : i == 1 ? Random.IntRange(2, 4) : 2;
                c.traits = i == 0 ? 0 : i == 1 ? 1 << Random.Int(2) : 3;
                c.payment = i == 0 ? 600 : i == 1 ? 1200 : 0;
            }
        } finally { Random.popGenerator(); }
    }
    public static boolean accept(int index) {
        if (!present || betrayed || index < 0 || index >= 4 || index==3 && (!bossUnlocked() || bossDefeated)) return false;
        planContracts(); Contract c = contracts[index];
        if(index==3){ planBoss(); c=contracts[3]; }
        if (c.accepted) return false;
        c.accepted = true;
        if (!c.issued) { c.issued = true; give(new Warrant(index)); }
        arrive(Dungeon.level);
        return true;
    }
    public static void give(Item item) {
        if (!item.collect(Dungeon.hero.belongings.backpack)) Dungeon.level.drop(item, Dungeon.hero.pos);
    }
    public static void arrive(Level level) {
        if (!present || Dungeon.branch != 0) return;
        planContracts();
        if(level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel && contracts[3]!=null && !contracts[3].complete)
            contracts[3].target=((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)level).bountyBoss();
        for (Contract c : contracts) if (c != null && c.index<3 && c.accepted && !c.complete) {
            if (Dungeon.depth == c.floor) {
                c.clockStarted = true;
                boolean exists = false;
                for (Mob mob : level.mobs) if (mob.bountyContract == c.index) { c.target = mob; exists = true; break; }
                if (!exists && (!c.spawned || c.target != null)) {
                    Mob mob = c.spawned ? c.target : c.preview(); int cell = spawnCell(level, mob);
                    if (cell < 0) continue;
                    if (!c.spawned) {
                    mob.bountyContract = c.index;
                    mob.wantedDamage = c.index == 0 ? 1.10f : c.index == 1 ? 1.15f : 1.25f;
                    mob.wantedMovement = (c.traits & 2) != 0 ? 1.10f : 1f;
                    mob.wantedArmor = (c.traits & 1) != 0 ? 1 : 0;
                    mob.HT = mob.HP = Math.round(mob.HT * (c.index == 0 ? 1.25f : c.index == 1 ? 1.60f : 2.20f));
                    }
                    mob.pos = cell; mob.timeToNow(); level.mobs.add(mob);
                    if (com.watabou.noosa.Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene)
                        com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(mob);
                    c.spawned = true; c.target = mob;
                }
            }
        }
        if (Dungeon.depth == OFFICE_DEPTH && shopClosed)
            for (Heap heap : level.heaps.valueList().toArray(new Heap[0])) if (owns(heap)) heap.destroy();
        if(betrayed && Dungeon.depth==OFFICE_DEPTH) for(Mob mob:level.mobs.toArray(new Mob[0]))
            if(mob instanceof Cole) removeCole((Cole)mob);
        if(Dungeon.depth==10 && contracts[3]!=null && contracts[3].complete && !departed) ensureDeparture();
    }
    /** Exhaustive candidates prevent an unlucky placement roll from losing a contract. */
    public static int spawnCell(Level level, Mob mob) {
        if (!(level instanceof RegularLevel)) return -1;
        com.watabou.utils.PathFinder.buildDistanceMap(level.entrance(), level.passable);
        java.util.ArrayList<Integer> candidates = new java.util.ArrayList<>();
        for (int cell = 0; cell < level.length(); cell++) {
            if (level.passable[cell] && !level.solid[cell] && level.findMob(cell) == null
                    && cell != Dungeon.hero.pos && cell != level.exit() && cell != level.entrance()
                    && level.distance(cell, Dungeon.hero.pos) > 5 && level.distance(cell, level.entrance()) > 5
                    && !level.heroFOV[cell] && level.traps.get(cell) == null && level.heaps.get(cell) == null
                    && ((RegularLevel)level).room(cell) instanceof StandardRoom
                    && com.watabou.utils.PathFinder.distance[cell] != Integer.MAX_VALUE
                    && (!com.shatteredpixel.shatteredpixeldungeon.actors.Char.hasProp(mob,
                            com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.LARGE) || level.openSpace[cell]))
                candidates.add(cell);
        }
        return candidates.isEmpty() ? -1 : Random.element(candidates);
    }
    public static void onHeroSpent(float turns) {
        if (!present) return;
        for (Contract c : contracts) if (c != null && c.clockStarted && c.index < 2 && !c.complete && !c.paid)
            c.elapsed += turns;
        // Retry a pending placement when occupied cells become free.
        if (Dungeon.level != null) arrive(Dungeon.level);
    }
    public static void targetDied(Mob mob) {
        int index = mob.bountyContract;
        if(present && index==3 && Dungeon.depth==10 && Dungeon.branch==0 && mob instanceof Tengu) {
            bossDefeated=true; Contract c=contracts[3];
            if(c!=null && c.accepted && !c.complete && c.target!=null && c.target.id()==mob.id()) {
                c.complete=true; c.target=null;
                Dungeon.hero.interrupt(); Dungeon.hero.lastAction=null;
            }
            return;
        }
        if (!present || index < 0 || index >= 3 || Dungeon.branch != 0) return;
        Contract c = contracts[index];
        if (c == null || !c.accepted || !c.spawned || c.complete || c.floor != Dungeon.depth) return;
        if (c.target == null || c.target.id() != mob.id()) return;
        c.complete = true;
        c.target = null;
        if (index == 2 && !c.coatIssued) {
            c.coatIssued = true;
            Item coat = new com.shatteredpixel.shatteredpixeldungeon.items.armor.WardensCoat().level(2);
            int cell = Dungeon.level.insideMap(mob.pos) ? mob.pos : Dungeon.hero.pos;
            Dungeon.level.drop(coat, cell);
        }
        c.bonusEarned = !c.paid && index < 2 && c.clockStarted && c.elapsed <= c.deadline;
    }
    public static boolean returnClaim(int index) {
        if (!present || index < 0 || index >= 3 || contracts[index] == null) return false;
        Contract c = contracts[index];
        if (!c.complete || c.returned || !Warrant.ownedContract(index)) return false;
        c.returned = true;
        if (!c.paid) { c.paid = true; if (c.amount() > 0) new Gold(c.amount()).sale().award(Dungeon.hero); }
        Warrant.retireContract(index);
        return true;
    }
    /** One saved roll shared by the offer and the actual boss, with the original configured probability. */
    public static void planBoss() {
        if(bossChoice<0){
            Random.pushGenerator(Dungeon.seed ^ 0x424F554E5459424FL);
            try {bossChoice=BalanceTuning.roll(BalanceTuning.Key.CHAINWARDEN,10,3)?1:0;}
            finally{Random.popGenerator();}
        }
        if(contracts[3]==null){Contract c=contracts[3]=new Contract();c.index=3;c.species=5+bossChoice;c.floor=10;c.payment=1500;}
    }
    public static Tengu createBoss() {
        if(!present)return BalanceTuning.roll(BalanceTuning.Key.CHAINWARDEN,10,3)?new Chainwarden():new Tengu();
        planBoss(); Tengu boss=bossChoice==1?new Chainwarden():new Tengu();
        boss.bountyContract=3;contracts[3].target=boss;return boss;
    }
    private static Cole ensureDeparture() {
        for(Mob m:Dungeon.level.mobs)if(m instanceof Cole)return (Cole)m;
        int origin=Dungeon.level.exit(); int best=-1,distance=Integer.MAX_VALUE;
        for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.passable[cell]&&!Dungeon.level.pit[cell]
                &&Dungeon.level.findMob(cell)==null&&cell!=Dungeon.hero.pos&&cell!=origin&&cell!=Dungeon.level.entrance()
                &&Dungeon.level.traps.get(cell)==null&&Dungeon.level.heaps.get(cell)==null){
            int d=Dungeon.level.distance(cell,origin);if(d<distance){best=cell;distance=d;}
        }
        if(best<0)return null;
        Cole cole=new Cole();cole.pos=best;Dungeon.level.mobs.add(cole);
        if(com.watabou.noosa.Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene)
            com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(cole);
        return cole;
    }
    public static boolean beginBetrayal() {
        Contract c=contracts[3];if(!present||c==null||!c.accepted||!c.complete||departed)return false;
        if(!betrayed){
            c.returned=true; if(!c.paid){c.paid=true;new Gold(c.payment).sale().award(Dungeon.hero);}
            Warrant.retireContract(3);shopClosed=true;betrayed=true;
            long value=Dungeon.gold;java.util.Set<Item> counted=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            for(Item item:Dungeon.hero.belongings)if(counted.add(item))value+=Math.max(0,item.value());
            heroBounty=(int)Math.min(Integer.MAX_VALUE,value);
        }
        return true;
    }
    public static void finishDeparture() {
        departed=true;dialogueOpen=false;
        for(Mob mob:Dungeon.level.mobs.toArray(new Mob[0]))if(mob instanceof Cole)removeCole((Cole)mob);
    }
    private static void removeCole(Cole cole) {
        Dungeon.level.mobs.remove(cole); Actor.remove(cole);
        if(cole.sprite!=null){cole.sprite.killAndErase();cole.sprite=null;}
    }
    public static void onHeroReady() {
        if(!present||Dungeon.branch!=0||Dungeon.depth!=10||departed||dialogueOpen||contracts[3]==null||!contracts[3].complete)return;
        if(!(com.watabou.noosa.Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene))return;
        dialogueOpen=true;Dungeon.hero.interrupt();Dungeon.hero.lastAction=null;Dungeon.hero.resting=false;
        com.watabou.noosa.Game.runOnRenderThread(()->{
            Cole cole=ensureDeparture();if(cole==null){dialogueOpen=false;return;}
            beginBetrayal();
            com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyBetrayal(cole));
        });
    }
    public static boolean bossUnlocked() {
        int count = 0; for (int i = 0; i < 3; i++) if (contracts[i] != null && contracts[i].returned) count++;
        return count >= 2;
    }

    /** Seeding this plan cannot consume generation rolls from unrelated rooms. */
    public static void planShop() {
        if (!present || prices[0] != 0) return;
        Random.pushGenerator(Dungeon.seed ^ 0x434F4C4553484F50L);
        try {
            stock[0] = new Food();
            stock[1] = new PotionOfHealing();
            stock[2] = Random.Int(2) == 0 ? new PotionOfHaste() : new PotionOfInvisibility();
            Weapon weapon = (Weapon) Generator.randomUsingDefaults(Random.Int(3) == 0
                    ? Generator.Category.MIS_T3 : Generator.Category.WEP_T3);
            weapon.level(Random.IntRange(1, 2)); weapon.cursed = false;
            weapon.enchant(Weapon.Enchantment.random());
            stock[3] = weapon.identify();
            stock[4] = new BloodmarkedBrand().quantity(2);
            for (int i = 0; i < 5; i++) prices[i] = standardPrice(stock[i], OFFICE_DEPTH) * 2;
        } finally { Random.popGenerator(); }
    }

    public static int standardPrice(Item item, int depth) {
        return item.value() * 5 * (depth / 5 + 1);
    }
    public static boolean owns(Heap heap) {
        return present && Dungeon.branch == 0 && Dungeon.depth == OFFICE_DEPTH
                && heap.coleSlot >= 0 && heap.coleSlot < stock.length;
    }
    public static boolean canTrade(Heap heap) {
        return !owns(heap) || !shopClosed && stock[heap.coleSlot] != null;
    }
    public static void takeStock(Heap heap) {
        if (owns(heap)) stock[heap.coleSlot] = null;
    }
    public static void failedTheft() {
        shopClosed = true;
        for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs)
            if (mob instanceof Cole) mob.yell(Messages.get(Cole.class, "thief"));
        for (Heap heap : Dungeon.level.heaps.valueList().toArray(new Heap[0]))
            if (owns(heap)) heap.destroy();
    }
    public static void store(Bundle quests) {
        Bundle b = new Bundle();
        b.put("present", present); b.put("closed", shopClosed); b.put("office", officeCell);
        b.put("boss_choice",bossChoice);b.put("boss_defeated",bossDefeated);b.put("betrayed",betrayed);b.put("departed",departed);b.put("hero_bounty",heroBounty);
        for (int i = 0; i < 4; i++) b.put("contract_" + i, contracts[i]);
        for (int i = 0; i < 5; i++) { b.put("stock_" + i, stock[i]); b.put("price_" + i, prices[i]); }
        quests.put("bounty_board", b);
    }
    public static void restore(Bundle quests) {
        reset(); present = false; // Old saves never acquire retroactive room plans.
        if (!quests.contains("bounty_board")) return;
        Bundle b = quests.getBundle("bounty_board");
        present = b.getBoolean("present"); shopClosed = b.getBoolean("closed");
        bossChoice=b.contains("boss_choice")?b.getInt("boss_choice"):-1;bossDefeated=b.getBoolean("boss_defeated");
        betrayed=b.getBoolean("betrayed");departed=b.getBoolean("departed");heroBounty=b.getInt("hero_bounty");
        officeCell = b.contains("office") ? b.getInt("office") : -1;
        for (int i = 0; i < 4; i++) contracts[i] = (Contract)b.get("contract_" + i);
        for (int i = 0; i < 5; i++) { stock[i] = (Item)b.get("stock_" + i); prices[i] = b.getInt("price_" + i); }
    }
}
