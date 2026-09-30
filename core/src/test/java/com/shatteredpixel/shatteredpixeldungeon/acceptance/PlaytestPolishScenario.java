// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.RatKingRoom;
import com.shatteredpixel.shatteredpixeldungeon.utils.*;
import com.watabou.utils.*;

final class PlaytestPolishScenario {
    private static void check(boolean ok,String why){if(!ok)throw new AssertionError("61: "+why);}
    static void startingKit(){
        for(Item item:Dungeon.hero.belongings)check(item.isIdentified(),"unidentified starter "+Dungeon.hero.heroClass+" "+item.getClass());
        if(Dungeon.hero.heroClass==HeroClass.ENCHANTER)
            check(Dungeon.hero.belongings.getItem(Dart.class).quantity()==3,"three starting steel-tipped darts");
    }
    static void run(){
        GamesInProgress.selectedClass=HeroClass.ENCHANTER;
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);SmokeRun.clearArena();
        Hero h=Dungeon.hero;SigilBrush brush=h.belongings.getItem(SigilBrush.class);
        check(brush.doUnequip(h,true),"unequip Brush into backpack");
        float cadence=Math.max(20,40-2*h.lvl);
        for(int rank=0;rank<=2;rank++){
            h.talents.get(1).put(Talent.DUAL_INSCRIPTION,rank);
            // Full cap clears fractional charge before each independent case.
            brush.gainCharge(100);brush.advance(1);brush.gainCharge(-100);
            float rate=rank==0?0:rank==1?.5f:.75f;
            check(brush.carriedRechargeRate(h)==rate,"carried rate at rank "+rank);
            int turns=rank==0?100:(int)Math.ceil(cadence/rate)+1;
            for(int i=0;i<turns;i++)EnchanterMagic.state().act();
            check(brush.charges()==(rank==0?0:1),"actual inventory recharge at rank "+rank);
            check(brush.actions(h).contains("CAST")== (rank>0),"inventory action permission");
            if(rank>0){
                h.talents.get(0).put(Talent.FIELD_REPAIR,1);
                check(brush.cast(h,"defensive_sigil",h.pos,null,null),"actual cast from inventory");
                Buff.detach(h,DefensiveSigil.class);
            }
        }
        brush.gainCharge(100);brush.advance(1);brush.gainCharge(-100);
        Buff.affect(h,MagicImmune.class);
        for(int i=0;i<100;i++)brush.rechargeCarried(h);
        check(brush.charges()==0&&!brush.ready(h,0),"inventory magic immunity");Buff.detach(h,MagicImmune.class);
        h.belongings.backpack.items.remove(brush);h.belongings.artifact=brush;
        for(int i=0;i<100;i++)brush.rechargeCarried(h);
        check(brush.charges()==0,"equipped Brush does not double charge");
        Bundle talents=new Bundle();Talent.storeTalentsInBundle(talents,h);Talent.restoreTalentsFromBundle(talents,h);
        check(h.pointsInTalent(Talent.DUAL_INSCRIPTION)==2,"renamed talent retains saved ranks");
        Dagger item=new Dagger();item.inscriptionTurns=20;
        for(int rank=0;rank<=2;rank++){
            h.talents.get(1).put(Talent.RESONANCE,rank);
            check(Math.abs(EnchanterMagic.permanent(item)-(rank==0?1:rank==1?1.2:1.3))<.0001,"Resonance rank "+rank);
        }
        for(int rank=1;rank<=2;rank++){
            h.talents.get(1).put(Talent.ATTUNEMENT,rank);
            int successes=0;
            for(int seed=0;seed<100;seed++){
                Random.pushGenerator(seed);boolean expected=Random.Float()<(rank==1?.2f:.3f);Random.popGenerator();
                item=new Dagger();Random.pushGenerator(seed);EnchanterMagic.collect(item);Random.popGenerator();
                check(item.enchanterAppraised&&item.isIdentified()==expected,"Appraisal exact roll "+rank+"/"+seed);
                if(expected)successes++;
                Item saved=item.duplicate();
                for(int i=0;i<20;i++)EnchanterMagic.collect(saved);
                check(saved.isIdentified()==expected&&saved.enchanterAppraised,"pickup/save/load cannot reroll");
            }
            check(successes>0&&successes<100,"Appraisal has both success and failure");
        }
        Food food=new Food();EnchanterMagic.collect(food);check(!food.enchanterAppraised,"Appraisal restricted to gear");
        ThrowingKnife tested=new ThrowingKnife();tested.enchanterAppraised=true;tested.quantity(3);
        check(tested.split(1).enchanterAppraised,"split keeps attempted flag");
        ThrowingKnife untested=(ThrowingKnife)tested.duplicate();untested.enchanterAppraised=false;untested.merge(tested);check(untested.enchanterAppraised,"merge keeps attempted flag");

        h.belongings.backpack.items.clear();HatchlingMimic pet=new HatchlingMimic();pet.collect();
        PotionOfHealing chosen=new PotionOfHealing();chosen.quantity(3);h.belongings.backpack.items.add(chosen);
        ThrowingKnife priority=new ThrowingKnife();priority.quantity(3);h.belongings.backpack.items.add(priority);
        while(!pet.warned())pet.tick(h);
        float time=h.cooldown();long debt=pet.goldDemand();
        check(pet.feedChosen(h,chosen)&&chosen.quantity()==2&&h.belongings.contains(priority),"explicit meal overrides automatic priority; one potion");
        check(h.cooldown()==time+1&&pet.remaining()==pet.interval()&&!pet.warned(),"manual feeding spends one turn, resets pending hunger");
        pet.onHeroReady();pet.tick(h);
        check(h.belongings.contains(priority)&&chosen.quantity()==2,"no second meal after manual warning response");
        check(!pet.feedChosen(h,priority),"digestion blocks consecutive rewards");
        while(!pet.hungry())pet.tick(h);
        check(pet.feedChosen(h,priority)&&!h.belongings.contains(priority)&&pet.goldDemand()==debt,"whole thrown stack, persistent gold demand");
        time=h.cooldown();check(!pet.feedChosen(h,new Food())&&!pet.feedChosen(h,h.belongings.weapon())&&h.cooldown()==time,"invalid/protected selections spend nothing");
        HatchlingMimic saved=(HatchlingMimic)pet.duplicate();check(saved.remaining()==pet.interval()&&!saved.warned(),"manual hunger reset persists");
        while(!pet.warned())pet.tick(h);pet.onHeroReady();pet.tick(h);
        check(chosen.quantity()==1,"neglected pet still eats automatically");

        MessageHistory.clear();for(int i=0;i<503;i++)GLog.w("history "+i);
        check(MessageHistory.snapshot().size()==500&&MessageHistory.snapshot().get(0).endsWith("history 3"),"bounded log retains newest 500");
        Bundle log=new Bundle();MessageHistory.store(log);MessageHistory.clear();MessageHistory.restore(log);
        check(MessageHistory.snapshot().get(499).equals(GLog.WARNING+"history 502"),"history order/color survives save");
        MessageHistory.restore(new Bundle());check(MessageHistory.snapshot().isEmpty(),"legacy save/new run does not inherit another log");

        int crown=h.pos-Dungeon.level.width(),source=h.pos;
        class Crown extends RatKingRoom.StatueOverhang { boolean known(){return knownSource(0);} }
        Crown overhang=new Crown();overhang.pos(crown);
        Dungeon.level.map[source]=Terrain.CUSTOM_DECO;
        Dungeon.level.heroFOV[crown]=true;Dungeon.level.visited[crown]=true;
        Dungeon.level.heroFOV[source]=Dungeon.level.visited[source]=Dungeon.level.mapped[source]=false;
        check(!overhang.known(),"seeing wall above an unseen statue does not reveal its crown");
        com.watabou.noosa.Tilemap visual=overhang.create();
        check(visual.image(0,0)==null,"actual overhang renderer suppresses hidden crown");
        Dungeon.level.heroFOV[source]=true;overhang.updateKnowledge();
        check(overhang.known()&&visual.image(0,0)!=null,"actual crown render appears when statue is seen");
        Dungeon.level.heroFOV[source]=false;Dungeon.level.visited[source]=true;check(overhang.known(),"known statue remains remembered");
        System.out.println("TEST 61 PASS: starting kits, carried Brush rates/casting, Appraisal once per item, Resonance, manual/automatic feeding, saved log history, crown source visibility");
    }
}
