// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** One authoritative quest record, independent of floor files and physical claims. */
public final class BountyBoard {
    private BountyBoard() {}
    // New runs receive the quest; absent records in older saves remain absent.
    public static boolean AVAILABLE = true;
    public static boolean present, shopClosed;
    public static final int OFFICE_DEPTH = 7;
    public static int officeCell = -1;
    public static Item[] stock = new Item[5];
    public static int[] prices = new int[5];
    public static Contract[] contracts = new Contract[4];
    public static int bossChoice = -1, heroBounty;
    public static boolean bossDefeated, betrayed, departed;
    private static boolean dialogueOpen;
    public static Crew[] crews=new Crew[3];
    public static int hallsCell=-1, meetingDepth=-1;
    public static Cole encounter;
    public static boolean resolved, hallsIntroduced, combatRequested, responsePending;
    public static int outcome, deepestMain=1;
    private static final java.util.ArrayList<RunDeeds.Notice> deathReceipts = new java.util.ArrayList<>();
    public static Item[] settlementTwo=new Item[2], settlementThree=new Item[3];
    public static int[] settlementPrices=new int[2];
    public static class Crew implements com.watabou.utils.Bundlable {
        public int index, floor;
        public boolean spawned, complete, issued;
        public BountyHunter[] members;
        public boolean[] dead;
        public Crew(){}
        @Override public void storeInBundle(Bundle b){
            b.put("index",index);b.put("floor",floor);b.put("spawned",spawned);b.put("complete",complete);b.put("issued",issued);
            b.put("dead",dead);b.put("count",members.length);for(int i=0;i<members.length;i++)b.put("member_"+i,members[i]);
        }
        @Override public void restoreFromBundle(Bundle b){
            index=b.getInt("index");floor=b.getInt("floor");spawned=b.getBoolean("spawned");complete=b.getBoolean("complete");issued=b.getBoolean("issued");
            members=new BountyHunter[b.getInt("count")];dead=b.getBooleanArray("dead");
            if(dead.length!=members.length)dead=new boolean[members.length];
            for(int i=0;i<members.length;i++)members[i]=(BountyHunter)b.get("member_"+i);
        }
    }

    public static class Contract implements com.watabou.utils.Bundlable {
        public int index, species, floor, traits, payment;
        public boolean accepted, spawned, complete, returned, paid, issued, clockStarted, bonusEarned;
        public float elapsed;
        public int deadline = 500, bonusPercent = 20;
        public Mob target;
        public boolean coatIssued;
        private boolean legacyLegendaryCash;
        public Contract() {}
        public String alias() { return index == 3 ? preview().name() : Messages.get(Cole.class, "alias_" + (index == 2 ? "warden" : species)); }
        public String title() { return index == 3 ? Messages.get(Cole.class,"boss_title",alias()) : Messages.get(Cole.class, "title_" + (index == 2 ? "warden" : species)); }
        public int amount() { return payment + (bonusEarned ? payment * bonusPercent / 100 : 0); }
        public boolean urgencyRunning() { return index < 2 && accepted && clockStarted && !complete && !paid; }
        public boolean urgencyExpired() { return elapsed > deadline; }
        public int turnsRemaining() { return Math.max(0, (int)Math.ceil(deadline - elapsed)); }
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
            // The notice preview and spawned quarry use the same appearance.
            if (index < 3) mob.bountyContract = index;
            return mob;
        }
        @Override public void storeInBundle(Bundle b) {
            b.put("index", index); b.put("species", species); b.put("floor", floor);
            b.put("traits", traits); b.put("payment", payment); b.put("accepted", accepted);
            b.put("spawned", spawned); b.put("complete", complete); b.put("returned", returned);
            b.put("paid", paid); b.put("issued", issued); b.put("clock_started", clockStarted);
            b.put("elapsed", elapsed); b.put("bonus_earned", bonusEarned);
            b.put("deadline", deadline); b.put("bonus_percent", bonusPercent);
            b.put("legendary_cash", true);
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
            // Older unpaid Legendary Warrants had only the dropped coat as their reward.
            // Preserve settled claims and deliberately zero-priced new plans.
            legacyLegendaryCash = index == 2 && !paid && payment == 0 && !b.contains("legendary_cash");
            if (legacyLegendaryCash)
                payment = BalanceTuning.get(BalanceTuning.Key.BOUNTY_RARE_PAY);
            target = (Mob)b.get("target");
            coatIssued = b.getBoolean("coat_issued");
        }
    }

    public static void reset() {
        deathReceipts.clear();
        present = AVAILABLE;
        shopClosed = false; officeCell = -1;
        stock = new Item[5]; prices = new int[5];
        contracts = new Contract[4];
        bossChoice=-1; heroBounty=0; bossDefeated=betrayed=departed=dialogueOpen=false;
        crews=new Crew[3];hallsCell=meetingDepth=-1;encounter=null;
        resolved=hallsIntroduced=combatRequested=responsePending=false;
        outcome=0;deepestMain=1;settlementTwo=new Item[2];settlementThree=new Item[3];settlementPrices=new int[2];
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
                c.payment = BalanceTuning.get(i == 0 ? BalanceTuning.Key.BOUNTY_COMMON_PAY : BalanceTuning.Key.BOUNTY_RARE_PAY);
                c.deadline=BalanceTuning.get(BalanceTuning.Key.BOUNTY_DEADLINE);c.bonusPercent=BalanceTuning.get(BalanceTuning.Key.BOUNTY_BONUS);
            }
        } finally { Random.popGenerator(); }
    }
    public static boolean accept(int index) {
        if (!present || betrayed || index < 0 || index >= 4 || index==3 && (!bossUnlocked() || bossDefeated)) return false;
        planContracts(); Contract c = contracts[index];
        if(index==3){ planBoss(); c=contracts[3]; }
        if (c.accepted) return false;
        c.accepted = true;
        com.shatteredpixel.shatteredpixeldungeon.journal.Notes.addBounty(index,OFFICE_DEPTH);
        if (!c.issued) { c.issued = true; give(new Warrant(index)); }
        arrive(Dungeon.level);
        return true;
    }
    public static void give(Item item) {
        if (!item.collect(Dungeon.hero.belongings.backpack)) Dungeon.level.drop(item, Dungeon.hero.pos);
    }
    public static void arrive(Level level) {
        if (!present || Dungeon.branch != 0) return;
        if(Dungeon.depth==OFFICE_DEPTH)com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.BountyOfficeRoom.refreshPresentation(level);
        deepestMain=Math.max(deepestMain,Math.min(25,Dungeon.depth));
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
                    mob.wantedDamage = BalanceTuning.multiplier(new BalanceTuning.Key[]{BalanceTuning.Key.WANTED_COMMON_DAMAGE,BalanceTuning.Key.WANTED_RARE_DAMAGE,BalanceTuning.Key.WANTED_LEGEND_DAMAGE}[c.index]);
                    mob.wantedMovement = (c.traits & 2) != 0 ? BalanceTuning.multiplier(BalanceTuning.Key.WANTED_MOVEMENT) : 1f;
                    mob.wantedArmor = (c.traits & 1) != 0 ? BalanceTuning.get(BalanceTuning.Key.WANTED_ARMOR) : 0;
                    mob.HT = mob.HP = Math.round(mob.HT * BalanceTuning.multiplier(new BalanceTuning.Key[]{BalanceTuning.Key.WANTED_COMMON_HP,BalanceTuning.Key.WANTED_RARE_HP,BalanceTuning.Key.WANTED_LEGEND_HP}[c.index]));
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
        if(betrayed && (Dungeon.depth==OFFICE_DEPTH||Dungeon.depth==22)) for(Mob mob:level.mobs.toArray(new Mob[0]))
            if(mob instanceof Cole){
                if(!resolved&&Dungeon.depth==meetingDepth&&encounter!=null&&encounter.id()==mob.id())encounter=(Cole)mob;
                else removeCole((Cole)mob);
            }
        if(Dungeon.depth==10 && contracts[3]!=null && contracts[3].complete && !departed) ensureDeparture();
        if(betrayed){arriveCrews(level);arriveMeeting(level);}
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
        if(turns>0&&responsePending)responsePending=false;
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
        if (!c.paid) { c.paid = true; pay(c.amount(), c, true); }
        Warrant.retireContract(index);
        return true;
    }
    public static String urgency(Contract c) {
        if (c.index >= 2 || !c.accepted) return "";
        if (c.complete) return Messages.get(Cole.class, c.bonusEarned ? "clock_earned" : "clock_missed");
        if (c.paid) return "";
        if (!c.clockStarted) return Messages.get(Cole.class, "clock_pending");
        return c.urgencyExpired() ? Messages.get(Cole.class, "clock_expired")
                : Messages.get(Cole.class, c.turnsRemaining() == 1 ? "clock_remaining_one" : "clock_remaining", c.turnsRemaining());
    }
    /** Quest payments retain their quoted amount and announce the actual award once. */
    private static void pay(int amount, Contract claim, boolean receipt) {
        if (amount <= 0) return;
        Hero hero = Dungeon.hero;
        int awarded = new Gold(amount).sale().award(hero);
        RunDeeds.paid(claim, awarded);
        String message = Messages.get(Cole.class, "payment", awarded);
        if (claim != null) {
            message += "\n\n" + Messages.get(Cole.class, "payment_claim", claim.alias());
            if (claim.bonusEarned)
                message += "\n" + Messages.get(Cole.class, "payment_bonus", claim.amount() - claim.payment);
        }
        GLog.h(message.replace("\n\n", " ").replace("\n", " "));
        // Text, particles and audio belong to the render thread, never the actor thread.
        if (Game.scene() instanceof GameScene) {
            GameScene scene = (GameScene) Game.scene();
            String body = message;
            Game.runOnRenderThread(() -> {
                if (Game.scene() != scene || Dungeon.hero != hero) return;
                if (hero.sprite != null) {
                    hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, "+" + awarded, FloatingText.GOLD);
                    hero.sprite.emitter().burst(Speck.factory(Speck.COIN), 8);
                }
                Sample.INSTANCE.play(Assets.Sounds.GOLD);
                if (receipt) GameScene.show(new WndTitledMessage(new ItemSprite(new Gold(awarded)),
                        Messages.get(Cole.class, "payment_title", awarded), body));
            });
        }
    }
    /** One saved roll shared by the offer and the actual boss, with the original configured probability. */
    public static void planBoss() {
        if(bossChoice<0){
            Random.pushGenerator(Dungeon.seed ^ 0x424F554E5459424FL);
            try {bossChoice=BalanceTuning.roll(BalanceTuning.Key.CHAINWARDEN,10,3)?1:0;}
            finally{Random.popGenerator();}
        }
        if(contracts[3]==null){Contract c=contracts[3]=new Contract();c.index=3;c.species=5+bossChoice;c.floor=10;c.payment=BalanceTuning.get(BalanceTuning.Key.BOUNTY_BOSS_PAY);}
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
            c.returned=true; if(!c.paid){c.paid=true;pay(c.payment,c,false);}
            Warrant.retireContract(3);shopClosed=true;betrayed=true;
            long value=Dungeon.gold;java.util.Set<Item> counted=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            for(Item item:Dungeon.hero.belongings)if(counted.add(item))value+=Math.max(0,item.value());
            heroBounty=(int)Math.min(Integer.MAX_VALUE,value);
            RunDeeds.wantedPoster();
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.addBounty(4,10);
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
        if(present&&betrayed&&!resolved&&Dungeon.branch==0&&Dungeon.depth==22&&encounter!=null
                &&!hallsIntroduced&&Dungeon.level.mobs.contains(encounter)&&Dungeon.level.heroFOV[encounter.pos]
                &&com.watabou.noosa.Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene){
            hallsIntroduced=true;Dungeon.hero.interrupt();Dungeon.hero.lastAction=null;Dungeon.hero.resting=false;
            com.watabou.noosa.Game.runOnRenderThread(()->showMeeting(encounter,true));
        }
        if(!present||Dungeon.branch!=0||Dungeon.depth!=10||departed||dialogueOpen||contracts[3]==null||!contracts[3].complete)return;
        if(!(com.watabou.noosa.Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene))return;
        dialogueOpen=true;Dungeon.hero.interrupt();Dungeon.hero.lastAction=null;Dungeon.hero.resting=false;
        com.watabou.noosa.Game.runOnRenderThread(()->{
            Cole cole=ensureDeparture();if(cole==null){dialogueOpen=false;return;}
            beginBetrayal();
            com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyBetrayal(cole));
        });
    }
    private static void planCrews(){
        for(int i=0;i<3;i++)if(crews[i]==null){
            Crew c=crews[i]=new Crew();c.index=i;c.floor=new int[]{13,18,22}[i];
            c.members=new BountyHunter[i==0?2:3];c.dead=new boolean[c.members.length];
        }
    }
    private static void arriveCrews(Level level){
        planCrews();
        for(Crew c:crews)if(Dungeon.depth==c.floor){
            for(Mob mob:level.mobs.toArray(new Mob[0]))if(mob instanceof BountyHunter&&((BountyHunter)mob).crew==c.index){
                int member=((BountyHunter)mob).member;
                if(member<0||member>=c.members.length||c.dead[member]){level.mobs.remove(mob);Actor.remove(mob);if(mob.sprite!=null)mob.sprite.killAndErase();}
                else c.members[member]=(BountyHunter)mob;
            }
            if(resolved){for(BountyHunter m:c.members)if(m!=null)m.endPursuit();continue;}
            if(c.complete)continue;
            java.util.ArrayList<Integer> cells=crewCells(level,c.members.length);
            if(cells==null)continue;
            for(int i=0;i<c.members.length;i++)if(!c.dead[i]){
                BountyHunter mob=c.members[i];if(mob!=null&&level.mobs.contains(mob))continue;
                if(mob==null){mob=new BountyHunter();mob.configure(c.floor,c.index==0&&i==1?2:i);mob.crew=c.index;mob.member=i;c.members[i]=mob;}
                mob.pos=cells.remove(Random.Int(cells.size()));mob.timeToNow();level.mobs.add(mob);
                if(com.watabou.noosa.Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene)
                    com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(mob);
            }
            c.spawned=true;
        }
    }
    private static java.util.ArrayList<Integer> crewCells(Level level,int count){
        if(!(level instanceof RegularLevel))return null;
        java.util.LinkedHashMap<StandardRoom,java.util.ArrayList<Integer>> rooms=new java.util.LinkedHashMap<>();
        com.watabou.utils.PathFinder.buildDistanceMap(level.entrance(),level.passable);
        for(int cell=0;cell<level.length();cell++)if(level.passable[cell]&&!level.pit[cell]&&level.findMob(cell)==null
                &&cell!=Dungeon.hero.pos&&cell!=level.exit()&&cell!=level.entrance()&&!level.heroFOV[cell]
                &&level.distance(cell,Dungeon.hero.pos)>5&&level.distance(cell,level.entrance())>5
                &&level.traps.get(cell)==null&&level.heaps.get(cell)==null&&com.watabou.utils.PathFinder.distance[cell]!=Integer.MAX_VALUE
                &&((RegularLevel)level).room(cell) instanceof StandardRoom)
            rooms.computeIfAbsent((StandardRoom)((RegularLevel)level).room(cell),r->new java.util.ArrayList<>()).add(cell);
        java.util.ArrayList<java.util.ArrayList<Integer>> choices=new java.util.ArrayList<>();
        for(java.util.ArrayList<Integer> cells:rooms.values())if(cells.size()>=count)choices.add(cells);
        return choices.isEmpty()?null:Random.element(choices);
    }
    public static void hunterDied(BountyHunter mob){
        if(!present||!betrayed||resolved||mob.crew<0||mob.crew>=3||Dungeon.branch!=0)return;
        Crew c=crews[mob.crew];int member=mob.member;
        if(c==null||c.floor!=Dungeon.depth||member<0||member>=c.members.length||c.dead[member]
                ||c.members[member]==null||c.members[member].id()!=mob.id())return;
        c.dead[member]=true;c.members[member]=null;
        for(boolean dead:c.dead)if(!dead)return;
        c.complete=true;
        if(!c.issued){c.issued=true;Dungeon.level.drop(Warrant.hunter(c.index),Dungeon.level.insideMap(mob.pos)?mob.pos:Dungeon.hero.pos);}
    }
    public static int hunterClaims(){
        java.util.HashSet<Integer> ids=new java.util.HashSet<>();
        for(Item item:Dungeon.hero.belongings)if(item instanceof Warrant&&((Warrant)item).hunter)
            for(int id:((Warrant)item).claims)if(id>=0&&id<3&&crews[id]!=null&&crews[id].complete)ids.add(id);
        return ids.size();
    }
    public static boolean earnedDebt(){
        for(int i=0;i<3;i++)if(contracts[i]!=null&&contracts[i].accepted&&contracts[i].complete&&!contracts[i].paid)return true;
        return false;
    }
    public static int payEarned(){
        int total=0;for(int i=0;i<3;i++)if(contracts[i]!=null){Contract c=contracts[i];
            if(c.accepted&&c.complete&&!c.paid){total+=c.amount();c.paid=c.returned=true;RunDeeds.paid(c,c.amount());Warrant.retireContract(i);}
        }
        pay(total,null,false);return total;
    }
    public static boolean arrangeOffice(){
        if(!present||!betrayed||resolved||Dungeon.branch!=0||Dungeon.depth!=OFFICE_DEPTH||officeCell<0
                ||hunterClaims()<1&&!earnedDebt())return false;
        if(encounter==null)encounter=new Cole();
        if(Dungeon.level.mobs.contains(encounter))return true;
        meetingDepth=OFFICE_DEPTH;placeEncounter(Dungeon.level,officeCell);return Dungeon.level.mobs.contains(encounter);
    }
    private static void arriveMeeting(Level level){
        if(resolved)return;
        if(Dungeon.depth==22&&meetingDepth<0&&hallsCell>=0){meetingDepth=22;if(encounter==null)encounter=new Cole();}
        if(Dungeon.depth==meetingDepth&&encounter!=null&&!level.mobs.contains(encounter))placeEncounter(level,encounter.combatConfigured?encounter.pos:meetingDepth==22?hallsCell:officeCell);
    }
    private static void placeEncounter(Level level,int origin){
        if(origin<0||origin>=level.length())return;int best=-1,distance=Integer.MAX_VALUE;
        for(int cell=0;cell<level.length();cell++)if(level.passable[cell]&&!level.pit[cell]&&level.findMob(cell)==null
                &&cell!=Dungeon.hero.pos&&cell!=level.entrance()&&cell!=level.exit()&&level.heaps.get(cell)==null&&level.traps.get(cell)==null){
            int d=level.distance(cell,origin);if(d<distance){best=cell;distance=d;}
        }
        if(best<0)return;encounter.pos=best;encounter.timeToNow();level.mobs.add(encounter);
        if(com.watabou.noosa.Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene)
            com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(encounter);
    }
    public static void requestCombat(){combatRequested=responsePending=true;if(encounter!=null)encounter.startCombat();Dungeon.hero.interrupt();Dungeon.hero.lastAction=null;}
    public static void showMeeting(Cole cole,boolean halls){
        int paid=payEarned();int claims=hunterClaims();
        if(claims>=2)planSettlements();
        String text=(paid>0?Messages.get(Cole.class,"debt_paid")+"\n"+Messages.get(Cole.class,"payment",paid)+"\n\n":"")+Messages.get(Cole.class,halls&&claims==0?"hostile":"meeting");
        java.util.ArrayList<String> choices=new java.util.ArrayList<>();
        choices.add(Messages.get(Cole.class,claims==0&&halls?"continue":"confront"));
        if(claims>=2)choices.add(Messages.get(Cole.class,"settlement"));
        choices.add(Messages.get(Cole.class,"leave"));
        com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions(
                cole.sprite(),cole.name(),text,choices.toArray(new String[0])){
            @Override protected void onSelect(int index){if(index==0)requestCombat();else if(index==1&&claims>=2)showSettlement(claims);}
            @Override public void hide(){super.hide();if(halls&&claims==0)requestCombat();}
        });
    }
    public static void showBoard(){
        com.watabou.noosa.Game.runOnRenderThread(()->{
            if(!betrayed){for(Mob m:Dungeon.level.mobs)if(m instanceof Cole){((Cole)m).interact(Dungeon.hero);return;}}
            planContracts();java.util.ArrayList<String> options=new java.util.ArrayList<>();
            options.add(Messages.get(Cole.class,"arrange"));for(int i=0;i<3;i++)options.add(contracts[i].title());options.add(Messages.get(Cole.class,"wanted_title",Dungeon.hero.name()));
            com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions(
                    Messages.get(Cole.class,"board"),Messages.get(Cole.class,"note"),options.toArray(new String[0])){
                @Override protected boolean enabled(int index){return index>0||!resolved&&(hunterClaims()>0||earnedDebt());}
                @Override protected void onSelect(int index){if(index==0&&arrangeOffice())showMeeting(encounter,false);
                    else if(index>0&&index<4)com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract(contracts[index-1]));
                    else if(index==4)com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage(
                        new com.shatteredpixel.shatteredpixeldungeon.ui.HeroPortrait(Dungeon.hero.heroClass,24),Messages.get(Cole.class,"wanted_title",Dungeon.hero.name()),
                        Messages.get(Cole.class,"hero_poster",Dungeon.hero.name(),Dungeon.hero.heroClass.title(),heroBounty)));
                }
            });
        });
    }
    /** Death pays accepted unfinished cash claims without completing their targets. */
    public static int releaseCashOnDeath(){
        deathReceipts.clear();
        int total=0;for(Contract c:contracts)if(c!=null&&c.accepted&&!c.paid&&c.payment>0){
            deathReceipts.add(RunDeeds.receipt(c,c.amount()));
            total+=c.amount();c.paid=c.returned=true;Warrant.retireContract(c.index);
        }
        return total;
    }
    public static Gold deathGold(int amount) { return new Gold(amount).sale().bountyReceipts(deathReceipts); }
    private static Weapon weaponPrize(Generator.Category category,int level){
        Weapon item=(Weapon)Generator.randomUsingDefaults(category);item.level(level);item.cursed=false;
        item.enchant(Weapon.Enchantment.random());item.identify();return item;
    }
    public static Item qualityPrize(int tier,int level){
        return weaponPrize(tier==5?Generator.Category.WEP_T5:Generator.Category.WEP_T4,level);
    }
    private static Item ringPrize(int level){
        Item item=Generator.randomUsingDefaults(Generator.Category.RING);item.level(level);item.cursed=false;return item.identify();
    }
    public static void planSettlements(){
        if(settlementTwo[0]!=null)return;
        Random.pushGenerator(Dungeon.seed ^ 0x434F4C455052495AL);
        try{
            settlementTwo[0]=qualityPrize(4,2);settlementTwo[1]=ringPrize(2);
            settlementThree[0]=qualityPrize(5,3);settlementThree[1]=ringPrize(3);
            com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor armor=Random.Int(2)==0?
                    new com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor():new com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor();
            armor.level(3);armor.cursed=false;armor.inscribe(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.random());settlementThree[2]=armor.identify();
            int quoteDepth=Math.min(21,(Math.max(1,deepestMain)-1)/5*5+1);
            for(int i=0;i<2;i++)settlementPrices[i]=Math.round(standardPrice(settlementTwo[i],quoteDepth)*BalanceTuning.multiplier(BalanceTuning.Key.COLE_PRICE)/2f);
        }finally{Random.popGenerator();}
    }
    public static boolean settle(int count,int choice){
        if(!present||!betrayed||resolved||count<2||count>3||hunterClaims()<count)return false;
        planSettlements();Item[] items=count==3?settlementThree:settlementTwo;
        if(choice<0||choice>=items.length)return false;int price=count==3?0:settlementPrices[choice];
        if(Dungeon.gold<price)return false;Dungeon.gold-=price;Item prize=items[choice];
        finishOutcome(count==3?3:2);give(prize);
        if(encounter!=null&&Dungeon.level.mobs.contains(encounter))removeCole(encounter);
        return true;
    }
    public static void showSettlement(int count){
        planSettlements();Item[] items=count>=3?settlementThree:settlementTwo;
        String[] labels=new String[items.length];for(int i=0;i<items.length;i++)labels[i]=Messages.titleCase(items[i].title())+"\n"+(count>=3?Messages.get(Cole.class,"free"):settlementPrices[i]+" "+Messages.get(Cole.class,"gold"));
        com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions(
                Messages.get(Cole.class,"settlement"),Messages.get(Cole.class,"choose_prize"),labels){
            @Override protected void layoutBody(float pos,String message,String... options){
                int width= com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.landscape()?180:160;
                width=Math.min(width,(int)com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.width-32);
                com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock body=com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.renderTextBlock(6);
                body.text(message,width);body.setPos(0,pos);add(body);pos=body.bottom()+4;
                for(int i=0;i<options.length;i++){
                    final int index=i;
                    com.shatteredpixel.shatteredpixeldungeon.ui.RedButton button=new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton(options[i],6){
                        @Override protected void onClick(){hide();onSelect(index);}
                    };
                    button.multiline=true;button.setRect(0,pos,width-24,28);button.enable(enabled(i));add(button);
                    com.shatteredpixel.shatteredpixeldungeon.ui.IconButton info=new com.shatteredpixel.shatteredpixeldungeon.ui.IconButton(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.INFO)){
                        @Override protected void onClick(){onInfo(index);}
                    };
                    info.setRect(width-24,pos,24,28);add(info);pos+=30;
                }
                resize(width,(int)pos-2);
            }
            @Override protected boolean hasInfo(int i){return true;}
            @Override protected void onInfo(int i){com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(items[i]));}
            @Override protected boolean enabled(int i){return count>=3||Dungeon.gold>=settlementPrices[i];}
            @Override protected void onSelect(int i){settle(Math.min(3,count),i);}
        });
    }
    public static boolean finishOutcome(int result){
        if(!present||!betrayed||outcome!=0||result<1||result>4)return false;outcome=result;resolved=true;
        for(Crew c:crews)if(c!=null)for(BountyHunter m:c.members)if(m!=null)m.endPursuit();
        if(Dungeon.level!=null)for(Mob m:Dungeon.level.mobs)if(m instanceof BountyHunter&&((BountyHunter)m).coleReinforcement)((BountyHunter)m).endPursuit();
        Badges.validateBounty(result);return true;
    }
    public static int coleDefeated(Cole cole){
        if(!betrayed||resolved||encounter==null||encounter.id()!=cole.id())return 0;
        int claims=releaseCashOnDeath();finishOutcome(1);return claims;
    }
    public static void onEscape(){
        if(present&&betrayed&&!resolved&&Statistics.amuletObtained)finishOutcome(4);
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
            for (int i = 0; i < 5; i++) prices[i] = Math.round(standardPrice(stock[i], OFFICE_DEPTH) * BalanceTuning.multiplier(BalanceTuning.Key.COLE_PRICE));
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
        b.put("halls",hallsCell);b.put("meeting_depth",meetingDepth);b.put("encounter",encounter);b.put("resolved",resolved);
        b.put("halls_introduced",hallsIntroduced);b.put("combat_requested",combatRequested);b.put("response_pending",responsePending);
        b.put("outcome",outcome);b.put("deepest_main",deepestMain);
        for(int i=0;i<3;i++)b.put("settlement_three_"+i,settlementThree[i]);
        for(int i=0;i<2;i++){b.put("settlement_two_"+i,settlementTwo[i]);b.put("settlement_price_"+i,settlementPrices[i]);}
        for(int i=0;i<3;i++)b.put("crew_"+i,crews[i]);
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
        hallsCell=b.contains("halls")?b.getInt("halls"):-1;meetingDepth=b.contains("meeting_depth")?b.getInt("meeting_depth"):-1;
        encounter=(Cole)b.get("encounter");resolved=b.getBoolean("resolved");hallsIntroduced=b.getBoolean("halls_introduced");
        combatRequested=b.getBoolean("combat_requested");responsePending=b.getBoolean("response_pending");
        outcome=b.getInt("outcome");deepestMain=b.contains("deepest_main")?b.getInt("deepest_main"):Math.max(1,Statistics.deepestFloor);
        for(int i=0;i<3;i++)settlementThree[i]=(Item)b.get("settlement_three_"+i);
        for(int i=0;i<2;i++){settlementTwo[i]=(Item)b.get("settlement_two_"+i);settlementPrices[i]=b.getInt("settlement_price_"+i);}
        for(int i=0;i<3;i++)crews[i]=(Crew)b.get("crew_"+i);
        officeCell = b.contains("office") ? b.getInt("office") : -1;
        for (int i = 0; i < 4; i++) contracts[i] = (Contract)b.get("contract_" + i);
        if (contracts[2] != null && contracts[2].legacyLegendaryCash && contracts[1] != null)
            contracts[2].payment = contracts[1].payment;
        for (int i = 0; i < 5; i++) { stock[i] = (Item)b.get("stock_" + i); prices[i] = b.getInt("price_" + i); }
    }
}
