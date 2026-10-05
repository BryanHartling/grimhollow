// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.watabou.utils.Bundle;
public final class DoubloonScenario {
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
