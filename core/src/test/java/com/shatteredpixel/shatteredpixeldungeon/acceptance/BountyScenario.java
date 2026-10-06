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
            items();
            boss();
            crews();
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
            int gold=Dungeon.gold;check(BountyBoard.beginBetrayal(),"betrayal missing");
            check(Dungeon.gold-gold==1500&&!com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(3),"boss cash / paper");
            int snapshot=BountyBoard.heroBounty;
            BountyBoard.beginBetrayal();check(Dungeon.gold-gold==1500,"boss duplicate payout");
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
        System.out.println("BOUNTY COMPONENT 4 PASS: both actual boss choices, explicit unlock/acceptance, once-only payment/snapshot, departure/reload, no retroactive arc and all nine dialogues");
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
            c.members[0].HP-=5;c.members[0].ammunition=2;c.members[0].healed=true;
            Bundle save=new Bundle();BountyBoard.store(save);BountyBoard.restore(save);BountyBoard.arrive(Dungeon.level);
            c=BountyBoard.crews[region];int hp=c.members[0].HP;
            Dungeon.level.mobs.remove(c.members[0]);BountyBoard.arrive(Dungeon.level);
            check(c.members[0].HP==hp&&c.members[0].ammunition==2&&c.members[0].healed,"crew rebuild replenishes");
            c.members[c.members.length-1].alignment=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ALLY;
            for(int i=0;i<c.members.length;i++){
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.BountyHunter hunter=c.members[i];
                Heap loot=Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.food.Food(),hunter.pos);
                loot.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(loot);loot.sprite.link(loot);
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
}
