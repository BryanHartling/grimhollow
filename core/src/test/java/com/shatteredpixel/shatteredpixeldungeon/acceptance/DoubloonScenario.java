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
    }
    public static void check(boolean ok,String text){if(!ok)throw new AssertionError("Doubloon: "+text);}
    public static void run() throws Exception {
        BalanceTuning.reset();Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();
        Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
        FickleDoubloon c=new FickleDoubloon();Dungeon.hero.belongings.artifact=c;Dungeon.hero.belongings.misc=null;
        c.identify();c.playtestLevel(0);
        check(c.flip(Dungeon.hero)&&c.flips()==1&&c.charges()==0,"flip cost and experience");
        check(!c.flip(Dungeon.hero)&&!c.doUnequip(Dungeon.hero,true,false),"active wager lock");
        FickleDoubloon.Luck luck=Dungeon.hero.buff(FickleDoubloon.Luck.class);
        check(luck.use()&&!luck.use(),"level-zero single roll");luck.detach();
        c.cursed=true;c.playtestRecharge();check(c.flip(Dungeon.hero)&&!Dungeon.hero.buff(FickleDoubloon.Luck.class).favor,"Black Spot is rigged");
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
        BalanceTuning.reset();Playtest.reset();
        System.out.println("PASS Doubloon core: curve, cost, single roll, curse, Plunder, deferred last hand, persistence");
    }
}
