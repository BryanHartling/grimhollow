// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.watabou.utils.Bundle;
public final class DoubloonScenario {
    private static void world(FickleDoubloon c) throws Exception {
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger a=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger(),b=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger();
        a.cursed=true;a.level(3);check(DoubloonLoot.compare(b,a)>0,"curse-first quality");a.cursed=false;check(DoubloonLoot.compare(a,b)>0,"permanent upgrade quality");
        int premium=0,artifacts=0;
        com.watabou.utils.Random.pushGenerator(24002);
        for(int n=0;n<10000;n++){int roll=DoubloonLoot.shopRoll();if(roll<3)premium++;if(roll==2)artifacts++;}
        com.watabou.utils.Random.popGenerator();
        check(premium>4900&&premium<5300&&artifacts>1700&&artifacts<2100,"double shop rarity distribution");
        c.cursed=true;check(FickleDoubloon.active()==null,"curse suppresses economic bonuses");c.cursed=false;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief alerted=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief();
        Dungeon.hero.invisible=1;alerted.alertForCoin();check(alerted.state==alerted.WANDERING,"invisible bearer wakes thieves without a location");
        Dungeon.hero.invisible=0;alerted.alertForCoin();
        java.lang.reflect.Field target=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob.class.getDeclaredField("target");target.setAccessible(true);
        check(alerted.state==alerted.HUNTING&&target.getInt(alerted)==Dungeon.hero.pos,"coin pursuers resume after invisibility");
        java.lang.reflect.Method steal=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief.class.getDeclaredMethod("steal",com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.class);steal.setAccessible(true);
        BalanceTuning.set(BalanceTuning.Key.COIN_THEFT_RESIST,100);
        check(!(Boolean)steal.invoke(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief(),Dungeon.hero),"equipped resistance");
        Buff.affect(Dungeon.hero,FickleDoubloon.Luck.class).start(true,3,false);
        BalanceTuning.set(BalanceTuning.Key.COIN_THEFT_RESIST,0);
        for(int n=0;n<30;n++){steal.invoke(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief(),Dungeon.hero);check(Dungeon.hero.belongings.artifact==c,"active luck protects coin");}
        Buff.detach(Dungeon.hero,FickleDoubloon.Luck.class);
        check((Boolean)steal.invoke(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief(),Dungeon.hero)&&FickleDoubloon.equipped()==null,"forced theft preserves item, removes equipped passive");
        Dungeon.hero.belongings.artifact=c;BalanceTuning.reset();
        Dungeon.gold=2500;com.shatteredpixel.shatteredpixeldungeon.items.Gold sale=new com.shatteredpixel.shatteredpixeldungeon.items.Gold(100).sale();check(sale.award(Dungeon.hero)==100,"sales unmultiplied");
        check(new com.shatteredpixel.shatteredpixeldungeon.items.Gold(100).award(Dungeon.hero)==150,"earned gold Plunder bonus");
        int normal=0,boost=0;
        for(int seed=0;seed<1000;seed++)for(int region=0;region<5;region++)for(com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.Kind k:com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.Kind.values()){
            if(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.plannedDepth(seed,region,k)>=0)normal++;
            if(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.plannedDepth(seed,region,k,10)>=0)boost++;
        }
        check(boost>normal+1200&&boost<normal+1800,"cache regional percentage-point boost");
        com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.reset();
        Dungeon.depth=2;
        com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.addRooms(new java.util.ArrayList<>());
        Bundle plan=new Bundle();com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.storeRun(plan);
        Dungeon.hero.belongings.artifact=null;
        com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.addRooms(new java.util.ArrayList<>());
        Bundle unchanged=new Bundle();com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.storeRun(unchanged);
        check(java.util.Arrays.equals(plan.getIntArray("elemental_coin_plans"),unchanged.getIntArray("elemental_coin_plans")),"removing coin cannot reroll region plans");
        com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.restoreRun(plan);
        for(com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.Kind k:com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.Kind.values()){
            com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache cache=new com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache();cache.kind=k;Dungeon.level.elementalCaches.add(cache);
        }
        com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.record(Dungeon.level);
        java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room> rooms=new java.util.ArrayList<>();
        for(int floor=2;floor<25;floor++){Dungeon.depth=floor;com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretElementalRoom.addRooms(rooms);}
        check(rooms.isEmpty(),"encountered caches never repeat even with bonus");Dungeon.level.elementalCaches.clear();Dungeon.hero.belongings.artifact=c;
    }
    public static void check(boolean ok,String text){if(!ok)throw new AssertionError("Doubloon: "+text);}
    public static void run() throws Exception {
        BalanceTuning.reset();Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();
        Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
        FickleDoubloon c=new FickleDoubloon();Dungeon.hero.belongings.artifact=c;Dungeon.hero.belongings.misc=null;
        check(c.name().equals("Fickle Doubloon")&&!c.info().contains(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.NO_TEXT_FOUND),"localized item name and description");
        c.identify();c.playtestLevel(0);
        check(c.charges()==1&&c.flips()==0,"level-zero playtest editing");
        check(c.image==com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.FICKLE_HEADS&&!FickleDoubloon.luckRunning(Dungeon.hero),"initial face grants no active luck");
        check(c.flip(Dungeon.hero)&&c.flips()==1&&c.charges()==0,"flip cost and experience");
        check(!c.flip(Dungeon.hero)&&!c.doUnequip(Dungeon.hero,true,false),"active wager lock");
        FickleDoubloon.Luck luck=Dungeon.hero.buff(FickleDoubloon.Luck.class);
        check(luck.use()&&!luck.use(),"level-zero single roll");luck.detach();
        luck=Buff.affect(Dungeon.hero,FickleDoubloon.Luck.class);luck.start(true,2,false);
        Bundle clock=new Bundle();luck.storeInBundle(clock);float expiry=clock.getFloat("expires");luck.fixTime(1);
        Bundle adjusted=new Bundle();luck.storeInBundle(adjusted);check(adjusted.getFloat("expires")==expiry-1,"saved actor-clock normalization preserves luck expiry");luck.detach();
        c.cursed=true;c.playtestRecharge();check(c.flip(Dungeon.hero)&&!Dungeon.hero.buff(FickleDoubloon.Luck.class).favor&&c.image==com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.FICKLE_TAILS,"Black Spot is rigged and displays Tails");
        Buff.detach(Dungeon.hero,FickleDoubloon.Luck.class);c.cursed=false;
        for(int i=c.flips();i<66;i++){c.playtestRecharge();check(c.flip(Dungeon.hero),"flip sequence");Buff.detach(Dungeon.hero,FickleDoubloon.Luck.class);}
        check(c.level()==10&&c.flips()==66,"cumulative flip curve");
        Dungeon.gold=2500;check(c.plunder()==5&&Math.abs(c.qualityChance()-.2f)<.001f,"weighted Plunder");
        world(c);
        c.playtestLevel(8);Dungeon.hero.HT=100;Dungeon.hero.HP=19;Dungeon.depth=3;
        luck=Buff.affect(Dungeon.hero,FickleDoubloon.Luck.class);luck.start(false,3,false);
        FickleDoubloon.damaged(Dungeon.hero,20);check(!luck.favor,"blocked last hand");luck.detach();
        FickleDoubloon.damaged(Dungeon.hero,20);check(Dungeon.hero.buff(FickleDoubloon.Luck.class).favor,"deferred low-health last hand");
        Buff.detach(Dungeon.hero,FickleDoubloon.Luck.class);FickleDoubloon.damaged(Dungeon.hero,20);
        check(!FickleDoubloon.luckRunning(Dungeon.hero),"once per distinct floor");
        Bundle b=new Bundle();b.put("coin",c);c=(FickleDoubloon)b.get("coin");Dungeon.hero.belongings.artifact=c;
        FickleDoubloon.damaged(Dungeon.hero,20);check(!FickleDoubloon.luckRunning(Dungeon.hero),"last-hand floor persists");
        check(FickleDoubloon.choose(2,8,true)==8&&FickleDoubloon.choose(2,8,false)==2,"sample selection");
        companion();
        BalanceTuning.reset();Playtest.reset();
        System.out.println("PASS Doubloon core: curve, cost, single roll, curse, Plunder, deferred last hand, persistence");
    }
    private static void companion() throws Exception {
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();
        Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
        FickleDoubloon c=new FickleDoubloon();c.playtestLevel(10);Dungeon.hero.belongings.artifact=c;Dungeon.hero.belongings.misc=null;
        com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic hatchling=new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic();hatchling.level(3);hatchling.collect(Dungeon.hero.belongings.backpack);
        Dungeon.gold=2500;BalanceTuning.set(BalanceTuning.Key.COIN_HEADS,70);
        check(DoubloonFeeding.consume(Dungeon.hero,hatchling,c),"Heads transformation");
        com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion harness=com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.carried();
        check(harness!=null&&Dungeon.hero.belongings.artifact==null&&com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.carried()==null,"sacrifices without artifact-slot loss");
        check(Math.abs(harness.inheritedGold-.3f)<.001&&Math.abs(harness.inheritedQuality-.05f)<.001,"inherited snapshot not duplicated");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimicAlly ally=harness.ally();check(ally!=null&&ally.isDirectableAlly(),"protector commands");
        check(harness.name().equals("Golden Mimic Companion")&&harness.info().contains("Doubloon and hatchling are gone")&&!ally.description().contains(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.NO_TEXT_FOUND),"localized companion item and creature");
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:new java.util.ArrayList<>(Dungeon.level.mobs))if(m!=ally){m.destroy();}
        com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(ally.pos,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);
        Dungeon.level.heroFOV[ally.pos]=true;
        com.shatteredpixel.shatteredpixeldungeon.items.Heap heap=Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.Gold(100),ally.pos);
        com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing kept=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing();heap.drop(kept);heap.type=com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.HEAP;
        int collected=Dungeon.gold;ally.act();check(Dungeon.gold==collected+130&&heap.items.contains(kept)&&heap.items.stream().noneMatch(i->i instanceof com.shatteredpixel.shatteredpixeldungeon.items.Gold),"guardian gathers visible loose gold and preserves other loot");
        BalanceTuning.set(BalanceTuning.Key.COMPANION_STEAL,100);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
        int gold=Dungeon.gold;check(ally.pilfer(rat)&&!ally.pilfer(rat)&&Dungeon.gold>gold,"one stealing success per enemy");
        rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();rat.generatedRespawn=true;check(!ally.pilfer(rat),"respawn farming exclusion");
        BalanceTuning.set(BalanceTuning.Key.COMPANION_STEAL,0);
        rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();ally.attackProc(rat,4);check(rat.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots.class)!=null,"protector clamping bite");
        rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();rat.properties().add(com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS);ally.attackProc(rat,4);check(rat.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots.class)==null,"boss root exclusion");
        rat.generatedRespawn=true;
        Bundle saved=new Bundle();saved.put("rat",rat);check(((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat)saved.get("rat")).generatedRespawn,"respawn flag persists");
        check(harness.actions(Dungeon.hero).contains(com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.AC_DIRECT),"harness exposes Direct action");
        int guard=ally.pos;ally.directTocell(guard);
        java.lang.reflect.Field defending=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob.class.getDeclaredField("defendingPos");defending.setAccessible(true);
        check(defending.getInt(ally)==guard,"Golden Mimic accepts a guard command");
        ally.directTocell(Dungeon.hero.pos);check(defending.getInt(ally)==-1,"Golden Mimic accepts Follow command");
        ally.die(DoubloonScenario.class);check(harness.resting(),"incapacitation retains harness");
        Dungeon.depth=2;harness.arrive();check(harness.resting(),"immediate stair transition cannot revive");
        for(int n=0;n<100;n++)harness.tick();harness.arrive();check(harness.resting(),"timer alone on same floor cannot revive");
        Dungeon.depth=3;harness.arrive();check(!harness.resting()&&harness.ally()!=null,"different-floor recovery");
        saved=new Bundle();saved.put("harness",harness);com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion copy=(com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion)saved.get("harness");
        check(copy.coinLevel==10&&copy.hatchlingLevel==3&&copy.inheritedGold==harness.inheritedGold,"companion snapshot persistence");
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
        c=new FickleDoubloon();c.playtestLevel(10);c.cursed=true;Dungeon.hero.belongings.artifact=c;Dungeon.hero.belongings.misc=null;
        hatchling=new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic();hatchling.collect(Dungeon.hero.belongings.backpack);
        check(DoubloonFeeding.consume(Dungeon.hero,hatchling,c)&&com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.carried()==null,"Black Spot cannot create a friendly companion");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic hostile=null;
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic&&((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic)m).forfeitedCoin!=null)hostile=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic)m;
        check(hostile!=null&&hostile.forfeitedCoin==c&&c.level()==10&&c.charges()==0&&FickleDoubloon.luckRunning(Dungeon.hero),"Tails stake, preserved levels, zero charges and waning fortune");
        hostile.rollToDropLoot();check(hostile.forfeitedCoin==null&&Dungeon.level.heaps.get(hostile.pos).items.contains(c),"kill before escape recovers coin");
        hostile.forfeitedCoin=c;hostile.HP=hostile.HT/2;hostile.pos=Dungeon.level.exit();
        java.lang.reflect.Method act=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic.class.getDeclaredMethod("act");act.setAccessible(true);act.invoke(hostile);
        check(hostile.forfeitedCoin==null&&!Dungeon.level.mobs.contains(hostile),"escape permanently removes coin carrier");
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();Playtest.god(false);
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero psychic=Dungeon.hero;
        psychic.heroClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.PSYCHIC;
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(psychic);
        psychic.talents.get(1).put(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.PRECOGNITION,1);
        Buff.affect(psychic,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PsychicMind.class);
        c=new FickleDoubloon();c.playtestLevel(10);psychic.belongings.artifact=c;
        psychic.HT=100;psychic.HP=25;
        check(!c.actions(psychic).contains("FEED_HATCHLING"),"coin does not advertise the hidden feeding interaction");
        psychic.damage(5,new Object());check(psychic.HP==25&&!FickleDoubloon.luckRunning(psychic),"Precognition prevention leaves Dead Man's Hand unspent");
        psychic.damage(5,new Object());check(psychic.HP==20&&FickleDoubloon.luckRunning(psychic),"actual low-health damage triggers the preserved hand");
        System.out.println("PASS Doubloon companion: sacrifices, inherited fortune, pilfer cap, recovery, persistence, rigged transformation, coin recovery");
    }
}
