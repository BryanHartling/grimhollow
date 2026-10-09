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
                java.util.ArrayList<Heap> originalDisplays=new java.util.ArrayList<>();
                for(Heap heap:level.heaps.valueList())if(BountyBoard.owns(heap))originalDisplays.add(heap);
                level.customTiles.removeIf(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles);
                BountyBoard.arrive(level);BountyBoard.arrive(level);
                for(Heap heap:originalDisplays)check(level.heaps.get(heap.pos)==heap,"presentation migration preserves actual stock");
                check(level.customTiles.stream().filter(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles).count()==2,"two office layers; migration is idempotent");
                com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles furniture=(com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles)
                        level.customTiles.stream().filter(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles
                                && !(t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles.Rug)).findFirst().get();
                // Simulate an old mixed layer with part of its carpet overwritten by a display.
                level.customTiles.removeIf(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles.Rug);
                furniture.put(cole.pos%level.width()-1,cole.pos/level.width()-1,0);
                BountyBoard.arrive(level);BountyBoard.arrive(level);
                com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles.Rug rug=
                        (com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles.Rug)level.customTiles.get(0);
                Bundle rugSave=new Bundle();rug.storeInBundle(rugSave);
                check(java.util.Arrays.equals(rugSave.getIntArray("office_art"),new int[]{0,1,2,3,4,5,6,7,8}),"complete rug beneath furniture");
                Bundle furnitureSave=new Bundle();furniture.storeInBundle(furnitureSave);
                for(int frame:furnitureSave.getIntArray("office_art"))check(frame<0 || frame>8,"mixed carpet removed without furnishing fragments");
                for(Heap heap:originalDisplays)check(level.heaps.get(heap.pos)==heap,"rug repair preserves stock");
                check(level.customTiles.stream().filter(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles).count()==2,"rug repair idempotent");
                Bundle layers=new Bundle();layers.put("rug",rug);
                check(layers.get("rug") instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles.Rug,"rug save/load");
                java.util.ArrayList<Heap> displays=new java.util.ArrayList<>();
                for(Heap heap:level.heaps.valueList())if(BountyBoard.owns(heap))displays.add(heap);
                for(Heap a:displays){
                    check(reached[a.pos],"display reachable");
                    for(Heap b:displays)if(a!=b)check(Math.abs(a.pos%level.width()-b.pos%level.width())
                            +Math.abs(a.pos/level.width()-b.pos/level.width())>=2,"display spacing");
                }
                check(level.customTiles.stream().anyMatch(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles),"painted office furniture");
                com.watabou.utils.Bundle officeSave=new com.watabou.utils.Bundle();
                officeSave.put("furniture",level.customTiles.stream().filter(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles).findFirst().get());
                check(officeSave.get("furniture") instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles,"office art serialization");
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
            pendingTargets();
            legendaryCash();
            items();
            boss();
            crews();
            resolution();
            deeds();
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
        BountyBoard.contracts[0].floor=8;
        check(BountyBoard.accept(0)&&!BountyBoard.contracts[0].clockStarted
                && BountyBoard.urgency(BountyBoard.contracts[0]).contains("starts when"), "clock starts before arrival");
        BountyBoard.onHeroSpent(3);
        check(BountyBoard.contracts[0].elapsed==0,"waiting for target-floor arrival consumes bonus");
        BountyBoard.contracts[0].floor=7;BountyBoard.arrive(Dungeon.level);
        for (int i=1;i<3;i++) check(BountyBoard.accept(i) && !BountyBoard.accept(i), "acceptance duplication");
        check(!BountyBoard.accept(0),"first acceptance duplicated");
        int targets=0;
        for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs) if(mob.bountyContract>=0) {
            targets++; check(!Dungeon.level.heroFOV[mob.pos], "target appears in immediate sight");
            check(mob.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedVariant.class)==null, "double variant");
            mob.sprite=mob.sprite(); mob.sprite.link(mob);
        }
        check(targets==3 && com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(0), "accepted overlap / claim issuance");
        Dungeon.branch=1;BountyBoard.onHeroSpent(499.5f);Dungeon.branch=0;
        BountyBoard.Contract common=BountyBoard.contracts[0], rare=BountyBoard.contracts[1];
        check(common.urgencyRunning() && common.turnsRemaining()==1 && !common.urgencyExpired(), "fractional countdown rounding");
        check(new com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant(0).info().contains("1 turn remaining")
                && new com.shatteredpixel.shatteredpixeldungeon.journal.Notes.BountyRecord(0,7).desc().contains("1 turn remaining"), "Warrant/journal countdown missing");
        common.target.HP=0; common.target.die(Dungeon.hero);
        check(common.complete && common.bonusEarned && common.amount()==720, "real hero kill/earned timer");
        BountyBoard.onHeroSpent(1f);
        check(rare.turnsRemaining()==0 && rare.urgencyExpired()
                && BountyBoard.urgency(rare).contains("base bounty is still payable"), "expiry hides base claim");
        check(!common.urgencyRunning() && common.elapsed==499.5f
                && BountyBoard.urgency(common).contains("secured"), "kill does not lock the urgency bonus");
        rare.target.HP=0; rare.target.die(com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.class);
        check(rare.complete && !rare.bonusEarned && rare.amount()==1200, "environment kill/expired timer");
        Bundle saved=new Bundle(); BountyBoard.store(saved); BountyBoard.restore(saved);
        check(BountyBoard.contracts[0].title().equals(title)&&BountyBoard.contracts[0].alias().equals(alias),"seeded identity load");
        int gold=Dungeon.gold;
        String paidMessage=com.shatteredpixel.shatteredpixeldungeon.utils.GLog.HIGHLIGHT
                + com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(Cole.class,"payment",720);
        long notices=com.shatteredpixel.shatteredpixeldungeon.utils.MessageHistory.snapshot().stream().filter(m->m.startsWith(paidMessage)).count();
        check(BountyBoard.returnClaim(0)&&!BountyBoard.returnClaim(0),"payment duplicate");
        check(Dungeon.gold-gold==720&&!com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(0),"payment receipt / consumed paper");
        java.util.List<String> history=com.shatteredpixel.shatteredpixeldungeon.utils.MessageHistory.snapshot();
        check(history.stream().filter(m->m.startsWith(paidMessage)).count()==notices+1
                && history.stream().anyMatch(m->m.startsWith(paidMessage)&&m.contains(alias)&&m.contains("120 gold for swift completion")), "payment amount/bonus receipt missing or duplicated");
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
        // The headless backend has no GameScene emitter; retain the real Guard death animation.
        savedTarget.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.GuardSprite(){
            @Override public com.watabou.noosa.particles.Emitter emitter(){return new com.watabou.noosa.particles.Emitter();}
        }; savedTarget.sprite.link(savedTarget);
        savedTarget.HP=0; savedTarget.die(Dungeon.hero);
        int coats=0; for(Heap heap:Dungeon.level.heaps.valueList()) for(Item item:heap.items)
            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.WardensCoat) {
                coats++; check(item.trueLevel()==2,"found coat level");
            }
        check(coats==1&&legendary.coatIssued,"unique carried prize");
        BountyBoard.targetDied(savedTarget);
        int after=0; for(Heap heap:Dungeon.level.heaps.valueList()) for(Item item:heap.items)
            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.WardensCoat)after++;
        check(after==1,"duplicate corpse prize");
        check(legendary.payment==BountyBoard.contracts[1].payment && !legendary.bonusEarned
                && !com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract.text(legendary).contains("URGENT"), "Legendary cash must match Rare without urgency");
        int legendaryGold=Dungeon.gold;
        check(BountyBoard.returnClaim(2)&&!BountyBoard.returnClaim(2)&&Dungeon.gold-legendaryGold==1200, "Legendary coat/cash claim duplicated or missing");
    }
    private static void pendingTargets() throws java.io.IOException {
        Dungeon.init();Dungeon.seed=3848062306978L;Dungeon.depth=7;Dungeon.branch=0;
        Dungeon.switchLevel(Dungeon.newLevel(),-1);
        String plan=BountyBoard.contracts[1].alias()+" floor="+BountyBoard.contracts[1].floor;
        for(int i=0;i<3;i++)check(BountyBoard.accept(i),"reported-seed acceptance");
        for(int depth=7;depth<=9;depth++){
            if(depth>7){Dungeon.depth=depth;Dungeon.switchLevel(Dungeon.newLevel(),-1);}
            BountyBoard.arrive(Dungeon.level);
            for(BountyBoard.Contract c:BountyBoard.contracts)if(c!=null&&c.index<3&&c.floor==depth){
                check(c.target!=null&&Dungeon.level.mobs.contains(c.target)&&c.clockStarted,"reported-seed promised quarry/clock missing");
                check(com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract.text(c).contains("Dungeon floor "+depth),"poster promises different floor");
            }
            Dungeon.saveAll();Dungeon.loadGame(GamesInProgress.curSlot);
            Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot),Dungeon.hero.pos);
            for(BountyBoard.Contract c:BountyBoard.contracts)if(c!=null&&c.index<3&&c.floor==depth)
                check(c.target!=null&&Dungeon.level.mobs.contains(c.target)&&c.clockStarted,"reported-seed disk reload loses quarry/clock");
        }
        Dungeon.init();Dungeon.depth=7;Dungeon.branch=0;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        BountyBoard.Contract c=BountyBoard.contracts[1];c.floor=7;
        java.util.Arrays.fill(Dungeon.level.heroFOV,true);
        check(BountyBoard.accept(1)&&!c.spawned&&!c.clockStarted,"blocked placement started urgent countdown");
        BountyBoard.onHeroSpent(20);check(c.elapsed==0&&!c.clockStarted,"pending spawn consumed bonus time");
        // Reproduce an unfinished legacy record with a lost target reference.
        c.spawned=true;Bundle saved=new Bundle();BountyBoard.store(saved);BountyBoard.restore(saved);c=BountyBoard.contracts[1];
        java.util.Arrays.fill(Dungeon.level.heroFOV,false);BountyBoard.arrive(Dungeon.level);
        check(c.target!=null&&c.target.isAlive()&&Dungeon.level.mobs.contains(c.target)&&c.clockStarted&&c.elapsed==0,"orphaned contract cannot recover");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob quarry=c.target;
        int ht=quarry.HT;quarry.HP--;BountyBoard.onHeroSpent(3);
        BountyBoard.store(saved);BountyBoard.restore(saved);c=BountyBoard.contracts[1];BountyBoard.arrive(Dungeon.level);
        check(c.target==quarry&&c.target.HT==ht&&c.target.HP==ht-1&&c.elapsed==3,"rebinding rerolled stats, restored health or reset timer");
        check(Dungeon.level.mobs.stream().filter(m->m.bountyContract==1).count()==1,"repair duplicated quarry");
        c.complete=true;c.target=null;Dungeon.level.mobs.remove(quarry);BountyBoard.arrive(Dungeon.level);
        check(c.target==null&&Dungeon.level.mobs.stream().noneMatch(m->m.bountyContract==1),"completed quarry resurrected");
        System.out.println("BOUNTY RECOVERY PASS: seed=3848062306978 "+plan+"; all three promised floors, disk reload, pending placement clock, missing reference repair, single wounded actor and completed-target protection");
    }
    private static void legendaryCash(){
        int original=BalanceTuning.get(BalanceTuning.Key.BOUNTY_RARE_PAY);
        try{
            BalanceTuning.setShared(BalanceTuning.Key.BOUNTY_RARE_PAY,1800);
            Dungeon.init();Dungeon.depth=7;Dungeon.branch=0;Dungeon.switchLevel(Dungeon.newLevel(),-1);
            BountyBoard.Contract c=BountyBoard.contracts[2];
            check(c.payment==1800&&c.payment==BountyBoard.contracts[1].payment,"Legendary/Rare tuning diverged");
            BalanceTuning.setShared(BalanceTuning.Key.BOUNTY_RARE_PAY,900);BountyBoard.planContracts();
            Bundle saved=new Bundle();BountyBoard.store(saved);BountyBoard.restore(saved);c=BountyBoard.contracts[2];
            check(c.payment==1800,"saved Legendary quote was repriced");
            // Reproduce the previous release's on-disk record, with no new cash marker.
            Bundle legacyClaim=new Bundle();legacyClaim.put("__className",BountyBoard.Contract.class.getName());
            legacyClaim.put("index",2);legacyClaim.put("species",c.species);legacyClaim.put("floor",c.floor);legacyClaim.put("payment",0);
            Bundle legacyQuest=saved.getBundle("bounty_board");legacyQuest.put("contract_2",legacyClaim);saved.put("bounty_board",legacyQuest);
            BountyBoard.restore(Bundle.read(new java.io.ByteArrayInputStream(saved.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8))));
            c=BountyBoard.contracts[2];
            check(c.payment==1800&&c.payment==BountyBoard.contracts[1].payment,"old Legendary must inherit its run's saved Rare quote");
            c.accepted=c.complete=true;BountyBoard.give(new com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant(2));
            int gold=Dungeon.gold;
            check(BountyBoard.earnedDebt()&&BountyBoard.payEarned()==1800&&BountyBoard.payEarned()==0
                    &&Dungeon.gold-gold==1800&&!com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(2),"deferred Legendary payment/receipt");
            Bundle old=new Bundle();old.put("index",2);old.put("accepted",true);old.put("complete",true);old.put("payment",0);
            BountyBoard.Contract migrated=new BountyBoard.Contract();migrated.restoreFromBundle(old);
            check(migrated.payment==900&&!migrated.paid,"old unpaid Legendary missing new cash");
            old.put("paid",true);migrated.restoreFromBundle(old);check(migrated.payment==0&&migrated.paid,"old settled Legendary was reopened");
            BountyBoard.Contract zero=new BountyBoard.Contract();zero.index=2;zero.payment=0;zero.storeInBundle(saved);migrated.restoreFromBundle(saved);
            check(migrated.payment==0,"deliberately zero-priced new Legendary was migrated");
            BountyBoard.Contract clock=new BountyBoard.Contract();clock.elapsed=clock.deadline;
            check(!clock.urgencyExpired()&&clock.turnsRemaining()==0,"exact deadline boundary changed");
            System.out.println("BOUNTY RECEIPTS/CLOCK PASS: quoted cash and bonus once, fractional countdown/Warrant/journal, delayed start/continuous clock/kill lock; Legendary equals Rare, no urgency, deferred claim, tuning/save migration/settled exclusion");
        }catch(java.io.IOException error){throw new AssertionError(error);}
        finally{BalanceTuning.setShared(BalanceTuning.Key.BOUNTY_RARE_PAY,original);}
    }
    private static void items() {
        Dungeon.init(); Dungeon.depth=7; Dungeon.branch=0; Dungeon.switchLevel(Dungeon.newLevel(),-1);
        for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff b:Dungeon.hero.buffs())b.detach();
        com.shatteredpixel.shatteredpixeldungeon.items.armor.WardensCoat coat=new com.shatteredpixel.shatteredpixeldungeon.items.armor.WardensCoat();
        for(int lvl=2;lvl<=10;lvl+=2){coat.level(lvl);check(coat.DRMin(lvl)==lvl&&coat.DRMax(lvl)==4*lvl+4,"coat protection table "+lvl);}
        coat.level(2);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
        check(coat.guardFirstHit(rat,0)==0&&!rat.wardensGuardUsed,"fully blocked consumes guard");
        check(coat.guardFirstHit(rat,99)==80&&rat.wardensGuardUsed&&coat.guardFirstHit(rat,99)==99,"per-enemy ceil/one hit");
        Bundle record=new Bundle();record.put("rat",rat);rat=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat)record.get("rat");
        check(coat.guardFirstHit(rat,99)==99,"enemy guard reload");
        coat.curseInfusionBonus=true;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat fresh=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
        check(coat.level()>coat.trueLevel()&&coat.guardFirstHit(fresh,100)==80,"temporary levels raise first-hit percent");
        coat.curseInfusionBonus=false;for(int i=0;i<8;i++)coat.upgrade();
        Dungeon.hero.belongings.armor=coat;
        Dungeon.hero.HT=Dungeon.hero.HP=500;Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();Dungeon.hero.sprite.link(Dungeon.hero);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat enemy=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();enemy.pos=Dungeon.hero.pos;
        Dungeon.hero.damage(20,enemy);check(Dungeon.hero.HP==500&&enemy.wardensGuardUsed,"actual +10 direct absorption");
        Dungeon.hero.damage(20,enemy);check(Dungeon.hero.HP==480,"second hit protected");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100 caster=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100();
        com.shatteredpixel.shatteredpixeldungeon.actors.DirectAttack.apply(Dungeon.hero,20,new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100.LightningBolt(),caster);
        check(Dungeon.hero.HP==480&&caster.wardensGuardUsed,"spell owner first-hit protection");
        Dungeon.hero.damage(10,com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.class);check(Dungeon.hero.HP==470,"environmental damage protected");
        com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor converted=com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor.upgrade(Dungeon.hero,coat);
        check(converted.wardensCoat&&converted.DRMax(10)==44&&converted.guardFirstHit(enemy,20)==20,"crown transfers growth and receipts");
        record.put("armor",converted);converted=(com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor)record.get("armor");
        check(converted.wardensCoat&&converted.DRMin(10)==10,"class armor save");
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(enemy,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.class,8);
        check(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.accuracy(Dungeon.hero,enemy)==1.25f
                &&com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.armor(Dungeon.hero,enemy,9)==5,"Brand weapon accuracy/rolled DR");
        check(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.accuracy(fresh,enemy)==1f
                &&com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.armor(fresh,enemy,9)==9,"allies inherit Brand");
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(enemy,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.class,8);
        check(enemy.buffs(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.class).size()==1,"Brand stacks");
        enemy.pos=Dungeon.level.exit(); Dungeon.level.mobs.add(enemy); Dungeon.level.heroFOV[enemy.pos]=false;
        boolean[] mapped=Dungeon.level.mapped.clone(),visited=Dungeon.level.visited.clone(),fov=Dungeon.level.heroFOV.clone();
        new com.shatteredpixel.shatteredpixeldungeon.effects.HatchlingSenseLayer().update();
        check(java.util.Arrays.equals(mapped,Dungeon.level.mapped)&&java.util.Arrays.equals(visited,Dungeon.level.visited)
                &&java.util.Arrays.equals(fov,Dungeon.level.heroFOV),"tracking reveals terrain");
        enemy.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.class).act();
        check(enemy.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bloodmark.class)==null,"Brand expiration");
        System.out.println("BOUNTY COMPONENT 3 PASS: unique +2 coat, actual +10 upgrades, growth table, per-enemy receipts, direct spell attribution, environment exclusion, crown/save, Brand benefits/refresh/tracking");
    }
    private static void boss(){
        for(int choice=0;choice<2;choice++){
            Dungeon.init();Dungeon.depth=7;Dungeon.branch=0;Dungeon.switchLevel(Dungeon.newLevel(),-1);
            BountyBoard.planContracts();BountyBoard.bossChoice=choice;
            check(!BountyBoard.accept(3),"locked boss accepted");
            BountyBoard.contracts[0].returned=BountyBoard.contracts[1].returned=true;
            check(BountyBoard.accept(3)&&!BountyBoard.accept(3),"boss acceptance receipt");
            Dungeon.depth=10;Dungeon.switchLevel(Dungeon.newLevel(),-1);
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu boss=
                    ((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).bountyBoss();
            check((boss instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Chainwarden)==(choice==1),"poster/generator boss mismatch");
            check(BountyBoard.contracts[3].alias().equals(boss.name())&&BountyBoard.contracts[3].target==boss,"boss identity binding");
            boss.HP=0;BountyBoard.targetDied(boss);
            check(BountyBoard.contracts[3].complete&&!BountyBoard.contracts[3].paid,"death skips betrayal payment");
            int unpaidGold=Dungeon.gold;
            BountyBoard.arrive(Dungeon.level);BountyBoard.onHeroReady();BountyBoard.onHeroReady();
            check(!BountyBoard.betrayed&&!BountyBoard.departed&&Dungeon.gold==unpaidGold
                    &&com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(3)
                    &&!RunDeeds.capture().wanted,"boss death auto-settled the claim");
            Bundle waiting=new Bundle();BountyBoard.store(waiting);BountyBoard.restore(waiting);BountyBoard.arrive(Dungeon.level);
            check(BountyBoard.contracts[3].complete&&!BountyBoard.contracts[3].paid&&!BountyBoard.betrayed
                    &&!BountyBoard.departed&&Dungeon.gold==unpaidGold,"waiting claim lost on reload");
            int waitingCount=0;for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)if(m instanceof Cole){
                waitingCount++;check(m.pos!=Dungeon.level.exit()&&m.pos!=Dungeon.level.entrance()
                        &&m.pos!=Dungeon.hero.pos&&Dungeon.level.passable[m.pos],"waiting Cole blocks the stairs");
            }
            check(waitingCount==1,"waiting Cole missing or duplicated after reload");
            int gold=Dungeon.gold;check(BountyBoard.beginBetrayal(),"betrayal missing");
            check(Dungeon.gold-gold==1500&&!com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(3),"boss cash / paper");
            int snapshot=BountyBoard.heroBounty;
            BountyBoard.beginBetrayal();check(Dungeon.gold-gold==1500,"boss duplicate payout");
            check(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.quest.WantedPoster.class)==null,"poster delivered before handover");
            if(choice==1)while(Dungeon.hero.belongings.backpack.items.size()<Dungeon.hero.belongings.backpack.capacity())
                Dungeon.hero.belongings.backpack.items.add(new Item());
            com.shatteredpixel.shatteredpixeldungeon.items.quest.WantedPoster poster=BountyBoard.issueWantedPoster();
            check(poster!=null&&BountyBoard.issueWantedPoster()==null,"personal poster missing or duplicated");
            if(choice==1)check(Dungeon.level.heaps.get(Dungeon.hero.pos)!=null
                    &&Dungeon.level.heaps.get(Dungeon.hero.pos).items.contains(poster),"full pack loses personal poster");
            else check(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.quest.WantedPoster.class)==poster,"poster not kept in inventory");
            check(poster.bounty==snapshot&&poster.subject==Dungeon.hero.heroClass&&poster.subjectName.equals(Dungeon.hero.name())
                    &&poster.isIdentified()&&!poster.isUpgradable()&&poster.value()==0
                    &&com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.protectedItem(poster,Dungeon.hero),"poster snapshot/quest protection");
            Bundle savedPoster=new Bundle();savedPoster.put("poster",poster);
            com.shatteredpixel.shatteredpixeldungeon.items.quest.WantedPoster copy=(com.shatteredpixel.shatteredpixeldungeon.items.quest.WantedPoster)savedPoster.get("poster");
            check(copy.subject==poster.subject&&copy.subjectName.equals(poster.subjectName)&&copy.bounty==snapshot,"poster reload changes identity");
            Bundle handover=new Bundle();BountyBoard.store(handover);BountyBoard.restore(handover);
            check(BountyBoard.issueWantedPoster()==null&&Dungeon.gold-gold==1500,"handover reload creates extra poster/payment");
            check(!BountyBoard.accept(2),"post-betrayal new contract");
            BountyBoard.arrive(Dungeon.level);
            int count=0;for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)if(m instanceof Cole)count++;
            check(count==1,"missing/duplicate departure actor");
            BountyBoard.finishDeparture();
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)check(!(m instanceof Cole),"Cole remains at exit");
            Bundle saved=new Bundle();BountyBoard.store(saved);BountyBoard.restore(saved);BountyBoard.arrive(Dungeon.level);
            check(BountyBoard.departed&&BountyBoard.betrayed&&BountyBoard.heroBounty==snapshot&&!BountyBoard.beginBetrayal(),"betrayal reload");
            check(Dungeon.level.exit()>=0,"boss exit removed");
        }
        Dungeon.init();Dungeon.depth=10;Dungeon.branch=0;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu boss=((com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level).bountyBoss();
        BountyBoard.targetDied(boss);check(!BountyBoard.beginBetrayal()&&!BountyBoard.accept(3),"retroactive boss arc");
        for(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass hc:com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.values())
            check(!com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(Cole.class,"betray_"+hc.name()).contains("!!!"),"missing class betrayal");
        System.out.println("BOUNTY COMPONENT 4 PASS: both actual boss choices, explicit unlock/acceptance, unpaid stairs meeting/reload, once-only payment/snapshot, departure/reload, no retroactive arc and all nine dialogues");
    }
    private static void crews(){
        Dungeon.init();Dungeon.branch=0;Dungeon.depth=13;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        Dungeon.hero.lvl=30;Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();Dungeon.hero.sprite.link(Dungeon.hero);
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)
            check(!(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter),"pre-betrayal crew");
        BountyBoard.betrayed=BountyBoard.departed=true;BountyBoard.heroBounty=7000;BountyBoard.arrive(Dungeon.level);
        for(int region=0;region<3;region++){
            if(region>0){Dungeon.depth=new int[]{13,18,22}[region];Dungeon.switchLevel(Dungeon.newLevel(),-1);}
            BountyBoard.Crew c=BountyBoard.crews[region];int count=0;
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)
                if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter){count++;check(!Dungeon.level.heroFOV[m.pos],"crew in sight");}
            check(count==(region==0?2:3)&&c.spawned&&!c.complete,"crew quota");
            java.util.HashSet<Class<?>> appearances=new java.util.HashSet<>();
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter hunter:c.members){
                appearances.add(hunter.spriteClass);
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter other:c.members)
                    check(Dungeon.level.distance(hunter.pos,other.pos)<=4,"crew spawned scattered");
                Bundle appearanceSave=new Bundle();appearanceSave.put("hunter",hunter);
                check(((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter)appearanceSave.get("hunter")).spriteClass==hunter.spriteClass,"role skin lost on reload");
            }
            check(appearances.size()==c.members.length,"crew roles share a skin");
            // Isolate navigation from loot-producing grass/trap callbacks, which
            // require a live GameScene and are covered by the native room fixture.
            for(int cell=0;cell<Dungeon.level.length();cell++){
                if(Dungeon.level.traps.get(cell)!=null)Level.set(cell,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.INACTIVE_TRAP);
                if(Dungeon.level.map[cell]==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.HIGH_GRASS
                        ||Dungeon.level.map[cell]==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.FURROWED_GRASS)
                    Level.set(cell,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.GRASS);
            }
            Dungeon.level.traps.clear();Dungeon.level.plants.clear();
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter hunter:c.members){
                hunter.sprite=hunter.sprite();hunter.sprite.link(hunter);
                hunter.fieldOfView=new boolean[Dungeon.level.length()];
            }
            int moves=0;
            for(int turn=0;turn<40;turn++){
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter hunter:c.members){
                    boolean waits=false;
                    if(hunter==c.members[0])for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter other:c.members)
                        if(other!=hunter&&Dungeon.level.distance(hunter.pos,other.pos)>3)waits=true;
                    int before=hunter.pos;hunter.WANDERING.act(false,false);if(hunter.pos!=before)moves++;
                    check(Dungeon.level.passable[hunter.pos],"patrol entered blocked terrain");
                    if(waits)check(hunter.pos==before,"patrol leader abandoned a straggler");
                    if(hunter!=c.members[0]){
                        Bundle patrolSave=new Bundle();hunter.storeInBundle(patrolSave);
                        check(patrolSave.getInt("target")==c.members[0].pos,"companion wandered independently");
                    }
                }
            }
            check(moves>0,"crew patrol never moved");
            c.members[0].aggro(Dungeon.hero);
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter hunter:c.members)
                check(hunter.state==hunter.HUNTING&&hunter.isTargeting(Dungeon.hero),"crew did not share quarry");
            c.members[0].HP-=5;c.members[0].ammunition=2;c.members[0].healed=true;
            Bundle save=new Bundle();BountyBoard.store(save);BountyBoard.restore(save);BountyBoard.arrive(Dungeon.level);
            c=BountyBoard.crews[region];int hp=c.members[0].HP;
            Dungeon.level.mobs.remove(c.members[0]);BountyBoard.arrive(Dungeon.level);
            check(c.members[0].HP==hp&&c.members[0].ammunition==2&&c.members[0].healed,"crew rebuild replenishes");
            c.members[c.members.length-1].alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY;
            for(int i=0;i<c.members.length;i++){
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter hunter=c.members[i];
                Heap loot=Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.food.Food(),hunter.pos);
                loot.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(loot){
                    @Override public void drop(){} // Real heap contents, no water-ripple renderer in headless mode.
                };loot.sprite.link(loot);
                hunter.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.GuardSprite(){
                    @Override public com.watabou.noosa.particles.Emitter emitter(){return new com.watabou.noosa.particles.Emitter();}
                };hunter.sprite.link(hunter);hunter.HP=0;hunter.die(Dungeon.hero);
                check(c.complete==(i==c.members.length-1),"controlled living hunter completes crew");
            }
            int warrants=0;
            for(Heap heap:Dungeon.level.heaps.valueList())for(Item item:new java.util.ArrayList<>(heap.items))
                if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant){
                    warrants++;item.collect(Dungeon.hero.belongings.backpack);heap.items.remove(item);
                }
            check(warrants==1&&c.issued&&BountyBoard.hunterClaims()==region+1,"one crew claim");
            BountyBoard.arrive(Dungeon.level);
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)
                check(!(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter),"dead crew respawns");
        }
        check(BountyBoard.encounter!=null&&BountyBoard.meetingDepth==22&&BountyBoard.hallsCell>=0,"optional Halls meeting");
        BountyBoard.encounter.HP=1;Bundle save=new Bundle();BountyBoard.store(save);BountyBoard.restore(save);BountyBoard.arrive(Dungeon.level);
        check(BountyBoard.encounter.HP==1&&Dungeon.level.mobs.contains(BountyBoard.encounter),"Cole encounter reload identity");
        BountyBoard.requestCombat();check(BountyBoard.responsePending,"no hero response");BountyBoard.onHeroSpent(1);check(!BountyBoard.responsePending,"hero response not accepted");
        Dungeon.depth=7;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        check(BountyBoard.arrangeOffice()&&BountyBoard.meetingDepth==7,"office route");
        BountyBoard.planContracts();BountyBoard.Contract common=BountyBoard.contracts[0],rare=BountyBoard.contracts[1];
        common.accepted=common.complete=common.bonusEarned=true;rare.accepted=true;
        int gold=Dungeon.gold;check(BountyBoard.payEarned()==720&&Dungeon.gold-gold==720&&BountyBoard.payEarned()==0,"earned debt duplicate");
        check(BountyBoard.releaseCashOnDeath()==1200&&rare.paid&&!rare.complete&&BountyBoard.releaseCashOnDeath()==0,"unfinished death settlement");
        BountyBoard.resolved=true;Dungeon.depth=13;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)
            check(!(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter),"resolved crew spawn");
        System.out.println("BOUNTY COMPONENT 5 PASS: accepted-only 2/3/3 crews, real controlled/hostile deaths, one saved Warrant, member resources and Cole identity, both meetings, response and earned/unfinished payment receipts");
    }
    private static void leverage(int count){
        BountyBoard.betrayed=BountyBoard.departed=true;BountyBoard.arrive(Dungeon.level);
        for(int i=0;i<count;i++){BountyBoard.crews[i].complete=true;com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.hunter(i).collect(Dungeon.hero.belongings.backpack);}
    }

    private static void deeds() throws java.io.IOException {
        Dungeon.init(); Dungeon.depth=7; Dungeon.switchLevel(Dungeon.newLevel(),-1);
        check(RunDeeds.capture().deeds.isEmpty() && RunDeeds.capture().highestPaid()==null, "fresh run inherits deeds");
        for (int type=1; type<=3; type++) {
            Bundle quests=new Bundle(), wandmaker=new Bundle();
            wandmaker.put("spawned",true);wandmaker.put("given",true);wandmaker.put("type",type);
            wandmaker.put("wand1",new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave());
            wandmaker.put("wand2",new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth());
            quests.put("wandmaker",wandmaker);Wandmaker.Quest.restoreFromBundle(quests);
            RunDeeds.reset();
            Item ingredient=type==1?new com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust()
                    :type==2?new com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers():new com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry.Seed();
            RunDeeds.Deed found=type==1?RunDeeds.Deed.DUST_FOUND:type==2?RunDeeds.Deed.EMBERS_FOUND:RunDeeds.Deed.ROTBERRY_FOUND;
            RunDeeds.Deed returned=type==1?RunDeeds.Deed.DUST_RETURNED:type==2?RunDeeds.Deed.EMBERS_RETURNED:RunDeeds.Deed.ROTBERRY_RETURNED;
            RunDeeds.pickedUp(ingredient);
            check(RunDeeds.capture().deeds.contains(found)&&!RunDeeds.capture().deeds.contains(returned),"retrieval prematurely marks delivery");
            Wandmaker.Quest.complete();
            check(RunDeeds.capture().deeds.contains(returned),"Wandmaker variant completion absent "+type);
        }
        Dungeon.init();Dungeon.depth=7;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        RunDeeds.pickedUp(new com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust());
        Bundle completedQuests=new Bundle(), ghost=new Bundle(), troll=new Bundle(), imp=new Bundle();
        ghost.put("spawned",true);ghost.put("given",true);ghost.put("processed",true);completedQuests.put("sadGhost",ghost);
        Ghost.Quest.restoreFromBundle(completedQuests);
        troll.put("spawned",true);troll.put("given",true);troll.put("boss_beaten",true);completedQuests.put("blacksmith",troll);
        Blacksmith.Quest.restoreFromBundle(completedQuests);
        new com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe().collect(Dungeon.hero.belongings.backpack);
        new com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold().quantity(20).collect(Dungeon.hero.belongings.backpack);
        Blacksmith.Quest.complete();
        imp.put("spawned",true);imp.put("old_quest",false);imp.put("given",true);completedQuests.put("demon",imp);
        imp.put("reward_options",new java.util.ArrayList<Item>());
        Imp.Quest.restoreFromBundle(completedQuests);Imp.Quest.complete(4000);
        RunDeeds questHistory=RunDeeds.capture();
        check(questHistory.deeds.containsAll(java.util.EnumSet.of(RunDeeds.Deed.GHOST_QUARRY,RunDeeds.Deed.GHOST_REWARD,
                RunDeeds.Deed.TROLL_GOLD,RunDeeds.Deed.TROLL_BOSS,RunDeeds.Deed.TROLL_COMPLETED,RunDeeds.Deed.IMP_COMPLETED)),"ghost/troll/Imp quest history absent");
        BountyBoard.Contract c=BountyBoard.contracts[0];c.accepted=c.complete=c.bonusEarned=true;
        c.payment=600;c.bonusPercent=20;
        com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant w=new com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant(0);
        w.collect(Dungeon.hero.belongings.backpack);check(BountyBoard.returnClaim(0),"deeds actual payment fixture");
        BountyBoard.heroBounty=4321;BountyBoard.betrayed=true;RunDeeds.wantedPoster();
        DragonExpedition.entered=true;DragonExpedition.spiderSlain=true;
        check(!RunDeeds.capture().deeds.contains(RunDeeds.Deed.DRAGON_ESCAPED),"entering is recorded as surviving");
        RunDeeds.record(RunDeeds.Deed.DRAGON_ESCAPED);
        RunDeeds before=RunDeeds.capture();
        check(before.highestPaid().collected==720 && before.highestPaid().complete && before.wantedBounty==4321, "paid gold / poster snapshot");
        Dungeon.saveAll();Dungeon.loadGame(GamesInProgress.curSlot);
        check(RunDeeds.capture().highestPaid().collected==720 && RunDeeds.capture().deeds.contains(RunDeeds.Deed.DRAGON_ESCAPED),"deeds disk save/load");
        Rankings.Record first=new Rankings.Record();first.heroClass=Dungeon.hero.heroClass;first.date="2026-10-07";first.version="test";first.customSeed="";
        Rankings.INSTANCE.saveGameData(first);
        Bundle records=new Bundle();records.put("first",first);
        first=(Rankings.Record)records.get("first");
        String alias=first.deeds.highestPaid().alias;
        Dungeon.init();Dungeon.depth=7;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        BountyBoard.contracts[0].payment=99999;DragonExpedition.dragonSlain=true;
        RunDeeds.paid(BountyBoard.contracts[0],99999);
        Rankings.Record second=new Rankings.Record();Rankings.INSTANCE.saveGameData(second);
        check(first.deeds.highestPaid().collected==720 && first.deeds.highestPaid().alias.equals(alias)
                && !first.deeds.deeds.contains(RunDeeds.Deed.DRAGON_SLAIN) && second.deeds.deeds.contains(RunDeeds.Deed.DRAGON_SLAIN),"later run changes old history");
        BountyBoard.Contract readOnly=first.deeds.highestPaid().poster();
        readOnly.payment=1;readOnly.complete=false;
        check(first.deeds.highestPaid().payment==600 && first.deeds.highestPaid().complete,"poster mutates archived notice");
        Bundle old=new Bundle();old.put("class",first.heroClass);old.put("gameData",first.gameData);
        Rankings.Record legacy=new Rankings.Record();legacy.restoreFromBundle(old);
        check(legacy.deeds==null,"old rankings fabricate deeds from current quest state");
        RunDeeds.restore(new Bundle());check(RunDeeds.capture().partial,"legacy ongoing save loses history qualifier");
        // A claimed gold pile from Cole's death has not yet been collected: it is not a receipt.
        Dungeon.init();Dungeon.depth=7;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        BountyBoard.contracts[0].accepted=BountyBoard.contracts[0].complete=true;
        int released=BountyBoard.releaseCashOnDeath();
        check(RunDeeds.capture().highestPaid()==null,"uncollected death loot recorded as cash collected");
        Gold payout=BountyBoard.deathGold(released+100);
        Gold merged=new Gold(10);merged.merge(payout);
        Bundle dropped=new Bundle();dropped.put("gold",merged);
        Gold loaded=(Gold)dropped.get("gold");loaded.award(Dungeon.hero);
        check(RunDeeds.capture().highestPaid().collected==BountyBoard.contracts[0].amount(),"collected death claim loses its receipt when merged/saved");
        for(RunDeeds.Deed deed:RunDeeds.Deed.values())check(!RunDeeds.text(deed).equals(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.NO_TEXT_FOUND),"unlocalized deed "+deed);
        System.out.println("RANKED DEEDS PASS: all three Wandmaker variants, retrieval vs delivery, actual paid bounty, Wanted snapshot, dragon entry vs survival/kill, disk and rankings serialization, independent runs, read-only poster copies, honest old records and no uncollected gold claims");
    }
    private static void resolution(){
        Dungeon.init();Dungeon.depth=7;Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();leverage(2);
        BountyBoard.deepestMain=18;BountyBoard.planSettlements();
        Item first=BountyBoard.settlementTwo[0];int price=BountyBoard.settlementPrices[0];
        check(first instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon&&first.level()==2&&!first.cursed&&first.isIdentified(),"two-claim quality");
        check(price==BountyBoard.standardPrice(first,16),"deepest-region half-double quote");
        Bundle save=new Bundle();BountyBoard.store(save);BountyBoard.restore(save);BountyBoard.deepestMain=25;BountyBoard.planSettlements();
        check(price==BountyBoard.settlementPrices[0]&&first.getClass()==BountyBoard.settlementTwo[0].getClass(),"settlement reroll/reprice");
        Dungeon.gold=price-1;check(!BountyBoard.settle(2,0)&&!BountyBoard.resolved,"unaffordable settlement");
        Dungeon.gold=price+99;check(BountyBoard.settle(2,0)&&BountyBoard.outcome==2&&Dungeon.gold==99,"negotiation price/outcome");
        check(!BountyBoard.settle(2,1)&&!BountyBoard.finishOutcome(1),"duplicate prize/second outcome");
        for(BountyBoard.Crew crew:BountyBoard.crews)if(crew!=null)for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter m:crew.members)if(m!=null)check(m.alignment==com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL,"active pursuit after settlement");
        Dungeon.init();Dungeon.depth=7;Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();leverage(3);Dungeon.gold=0;
        BountyBoard.planSettlements();check(BountyBoard.settlementThree[0].level()==3&&BountyBoard.settlementThree[2].level()==3,"professional quality");
        check(BountyBoard.settle(3,2)&&BountyBoard.outcome==3&&Dungeon.gold==0,"professional free single reward");
        BountyBoard.onEscape();check(BountyBoard.outcome==3,"settlement also Wanted");
        Dungeon.init();Dungeon.depth=7;Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();
        BountyBoard.betrayed=true;Statistics.amuletObtained=false;BountyBoard.onEscape();check(BountyBoard.outcome==0,"Wanted without Amulet");
        Statistics.amuletObtained=true;check(BountyBoard.hunterClaims()==0,"zero-crew escape fixture");BountyBoard.onEscape();check(BountyBoard.outcome==4&&BountyBoard.resolved,"zero-crew Wanted escape");
        check(!Badges.filterReplacedBadges(false).contains(Badges.Badge.WANTED),"Playtest cosmetic badge eligible");
        combat();
        for(BalanceTuning.Key key:BalanceTuning.Key.values())if(key.group>=13){
            check(!com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(BalanceTuning.class,key.id()).equals(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.NO_TEXT_FOUND),"unlocalized tuning label");
            check(key.baseline>=key.min&&key.baseline<=key.max,"invalid default/range");
        }
        BalanceTuning.setShared(BalanceTuning.Key.COLE_BOLAS,3);Cole tuned=new Cole();tuned.startCombat();check(tuned.bolas.quantity()==3&&Playtest.unranked(),"shared combat tuning");
        BalanceTuning.setShared(BalanceTuning.Key.COLE_BOLAS,2);
        System.out.println("BOUNTY COMPONENT 6 PASS: seeded/priced single settlements, finite Cole combat/normal Bolas controls, saved supplies and debuffs, once-only accepted cash and personal loot, mutually exclusive outcomes, zero-crew escape and unranked tuning");
    }
    private static void combat(){
        try{
            Dungeon.init();Dungeon.depth=22;Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();
            Dungeon.hero.lvl=30;Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();Dungeon.hero.sprite.link(Dungeon.hero);
            BountyBoard.betrayed=BountyBoard.departed=true;BountyBoard.deepestMain=22;BountyBoard.arrive(Dungeon.level);
            Cole cole=BountyBoard.encounter;check(cole!=null,"combat encounter");BountyBoard.requestCombat();
            check(cole.HP==240&&cole.bolas.quantity()==2&&cole.alignment==com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY&&BountyBoard.responsePending,"combat budget/response");
            java.lang.reflect.Method act=Cole.class.getDeclaredMethod("act");act.setAccessible(true);
            check((Boolean)act.invoke(cole)&&cole.bolas.quantity()==2,"first response consumes ammo");BountyBoard.onHeroSpent(1);
            com.shatteredpixel.shatteredpixeldungeon.actors.Char target=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat(){@Override public int defenseSkill(com.shatteredpixel.shatteredpixeldungeon.actors.Char c){return 0;}};
            target.HT=target.HP=500;target.sprite=null;cole.sprite=null;
            int from=-1,to=-1;
            for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.insideMap(cell)&&Dungeon.level.passable[cell]&&cell%Dungeon.level.width()<Dungeon.level.width()-4){
                int end=cell+3;if(Dungeon.level.passable[end]&&new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(cell,end,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos==end){from=cell;to=end;break;}
            }
            check(from>=0,"clear ranged fixture");cole.pos=from;target.pos=to;
            java.lang.reflect.Method attack=Cole.class.getDeclaredMethod("doAttack",com.shatteredpixel.shatteredpixeldungeon.actors.Char.class);attack.setAccessible(true);
            int health=target.HP;attack.invoke(cole,target);check(cole.bolas.quantity()==1&&target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class)!=null&&health-target.HP<=9,"ordinary Bolas hit/damage/Cripple");
            target.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple.class).detach();attack.invoke(cole,target);check(cole.bolas.quantity()==0,"finite ammunition");
            cole.HP=83;cole.healed=cole.smoked=cole.repositioned=cole.reinforcementCalled=true;cole.bolts=4;
            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(cole,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness.class,4);
            Bundle saved=new Bundle();saved.put("cole",cole);Cole restored=(Cole)saved.get("cole");
            restored.startCombat();check(restored.HP==83&&restored.bolts==4&&restored.bolas.quantity()==0&&restored.healed&&restored.smoked&&restored.repositioned&&restored.reinforcementCalled&&restored.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness.class)!=null,"combat reload restores resources/status");
            BountyBoard.encounter=restored;Dungeon.level.mobs.remove(cole);Dungeon.level.mobs.add(restored);
            BountyBoard.Contract common=BountyBoard.contracts[0],rare=BountyBoard.contracts[1];common.accepted=common.complete=common.paid=true;rare.accepted=true;
            restored.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ColeSprite(){@Override public com.watabou.noosa.particles.Emitter emitter(){return new com.watabou.noosa.particles.Emitter();}};restored.sprite.link(restored);
            Heap corpse=Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.food.Food(),restored.pos);corpse.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(corpse);corpse.sprite.link(corpse);
            restored.HP=0;restored.die(Dungeon.hero);int cash=0,prizes=0;
            for(Item item:corpse.items){if(item instanceof Gold)cash+=item.quantity();if(item==restored.prize)prizes++;}
            check(cash==1800&&prizes==1&&BountyBoard.outcome==1&&rare.paid&&!rare.complete,"Cole real death fixed loot/unfinished claim");
            restored.rollToDropLoot();check(corpse.items.size()==3&&BountyBoard.releaseCashOnDeath()==0,"duplicate loot/claims");
            BountyBoard.store(saved);BountyBoard.restore(saved);check(BountyBoard.outcome==1&&BountyBoard.encounter.lootDropped,"death receipt reload");
        }catch(ReflectiveOperationException error){throw new AssertionError(error);}
    }
}
