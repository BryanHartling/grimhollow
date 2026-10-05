// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.*;
import java.util.function.IntSupplier;

/** The coin changes samples, never repeats attacks or their side effects. */
public class FickleDoubloon extends Artifact {
    public static final String AC_FLIP="FLIP";
    private static final int[] FLIPS={3,6,10,15,21,28,36,45,55,66};
    private final HashSet<Integer> lastHands=new HashSet<>();
    private int face;
    { levelCap=10; chargeCap=1; image=ItemSpriteSheet.GOLD; defaultAction=AC_FLIP; }
    public static FickleDoubloon equipped(){
        Hero h=Dungeon.hero;
        if(h==null)return null;
        return h.belongings.artifact instanceof FickleDoubloon ? (FickleDoubloon)h.belongings.artifact
                : h.belongings.misc instanceof FickleDoubloon ? (FickleDoubloon)h.belongings.misc : null;
    }
    public static FickleDoubloon active(){
        FickleDoubloon c=equipped();
        return c!=null && !c.cursed && Dungeon.hero.buff(MagicImmune.class)==null ? c:null;
    }
    public static boolean luckRunning(Hero h){return h.buff(Luck.class)!=null;}
    public void settle(){charge=0;partialCharge=0;}
    public static float goldFindBonus(){
        FickleDoubloon c=active();float result=c==null?0:c.goldBonus();
        com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion pet=com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.carried();
        return result+(pet!=null&&!pet.resting()?pet.inheritedGold:0);
    }
    public static float qualityBonus(){FickleDoubloon c=active();com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion p=com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.carried();return (c==null?0:c.qualityChance())+(p!=null&&!p.resting()?p.inheritedQuality:0);}
    public static float searchFindBonus(boolean trap){FickleDoubloon c=active();com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion p=com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.carried();return (c==null?0:c.searchBonus(trap))+(p!=null&&!p.resting()&&p.coinLevel>=(trap?4:3)?p.inheritedSearch:0);}
    public static FickleDoubloon carried(){return Dungeon.hero==null?null:Dungeon.hero.belongings.getItem(FickleDoubloon.class);}
    public void stolen(Hero h){
        if(h.belongings.artifact==this)h.belongings.artifact=null;
        if(h.belongings.misc==this)h.belongings.misc=null;
        if(passiveBuff!=null)passiveBuff.detach();
        detachAll(h.belongings.backpack);updateQuickslot();
    }
    @Override public String info(){return super.info()+(cursed&&cursedKnown?"\n\n"+Messages.get(this,"black_spot"):"");}
    public int charges(){return charge;}
    public int flips(){return exp;}
    public int face(){return face;}
    public int plunder(){return Math.min(5,Math.max(0,Dungeon.gold)/500);}
    public float headsChance(){return Math.min(1,(BalanceTuning.get(BalanceTuning.Key.COIN_HEADS)+3*level())/100f);}
    public float goldBonus(){return (new int[]{5,8,8,12,12,16,16,20,20,25,25}[Math.min(10,level())]+5*plunder()
            +(HatchlingMimic.carried()!=null?10:0))*BalanceTuning.multiplier(BalanceTuning.Key.COIN_GOLD)/100f;}
    public float qualityChance(){return level()<2?0:(level()+2*plunder())*BalanceTuning.multiplier(BalanceTuning.Key.COIN_QUALITY)/100f;}
    public float searchBonus(boolean trap){return level()<(trap?4:3)?0:(level()+plunder())/100f;}
    @Override public ArrayList<String> actions(Hero h){
        ArrayList<String> a=super.actions(h);if(isEquipped(h)){a.add(AC_FLIP);if(HatchlingMimic.carried()!=null)a.add("FEED_HATCHLING");}return a;
    }
    @Override public void execute(Hero h,String action){super.execute(h,action);if(AC_FLIP.equals(action))flip(h);if("FEED_HATCHLING".equals(action)){HatchlingMimic pet=HatchlingMimic.carried();if(pet!=null&&pet.canFeed(h,this))DoubloonFeeding.confirm(h,pet,this);else GLog.w(Messages.get(this,"unavailable"));}}
    public boolean flip(Hero h){
        if(!isEquipped(h)||charge<1||luckRunning(h)||h.buff(MagicImmune.class)!=null){GLog.w(Messages.get(this,"unavailable"));return false;}
        int duration=level();
        boolean favor=!cursed && Random.Float()<headsChance();
        charge--;face=favor?1:2;
        Buff.affect(h,Luck.class).start(favor,duration,false);
        exp++;
        while(level()<10 && exp>=FLIPS[level()]){super.upgrade();GLog.p(Messages.get(this,"levelup"));}
        chargeCap=level()+1;updateQuickslot();
        GLog.w(Messages.get(this,favor?"heads":"tails"));
        h.spendAndNext(1);return true;
    }
    @Override public Item upgrade(){return this;}
    @Override public void transferUpgrade(int amount){}
    @Override public void resetForTrinity(int visibleLevel){}
    @Override protected void onPlaytestLevelSet(){chargeCap=level()+1;}
    @Override public boolean doUnequip(Hero h,boolean collect,boolean single){
        if(luckRunning(h)){GLog.n(Messages.get(this,"bound"));return false;}return super.doUnequip(h,collect,single);
    }
    @Override public boolean doEquip(Hero h){
        if(h.belongings.artifact!=null || h.belongings.misc instanceof Artifact){GLog.w(Messages.get(this,"exclusive"));return false;}
        return super.doEquip(h);
    }
    @Override public String status(){return isIdentified()?charge+"/"+(level()+1):null;}
    @Override protected ArtifactBuff passiveBuff(){return new CoinKeeper();}
    public void earnedGold(int amount){
        if(amount>0)addCharge(amount/(float)BalanceTuning.get(BalanceTuning.Key.COIN_GOLD_CHARGE));
    }
    private void addCharge(float amount){
        chargeCap=level()+1;
        if(charge>=chargeCap){partialCharge=0;return;}
        partialCharge+=amount;
        while(partialCharge>=1 && charge<chargeCap){partialCharge--;charge++;}
        if(charge>=chargeCap)partialCharge=0;
        updateQuickslot();
    }
    @Override public void charge(Hero h,float amount){addCharge(amount);}
    public class CoinKeeper extends ArtifactBuff {
        @Override public boolean act(){
            if(target.isAlive() && target.buff(MagicImmune.class)==null)
                addCharge(RingOfEnergy.artifactChargeMultiplier(target)/BalanceTuning.get(BalanceTuning.Key.COIN_CHARGE_TURNS));
            spend(TICK);return true;
        }
    }
    public static float choose(float first,float second,boolean favor){return favor?Math.max(first,second):Math.min(first,second);}
    public static float roll(Hero h,float maximum){
        float first=Random.Float(maximum);
        Luck luck=h.buff(Luck.class);
        return maximum>0 && luck!=null && luck.use() ? choose(first,Random.Float(maximum),luck.favor):first;
    }
    public static int damageRoll(int min,int max,IntSupplier ordinary){
        int first=damageSample(min,max,ordinary);
        Luck luck=Dungeon.hero==null?null:Dungeon.hero.buff(Luck.class);
        return min<max && luck!=null && luck.use() ? (int)choose(first,damageSample(min,max,ordinary),luck.favor):first;
    }
    private static int damageSample(int min,int max,IntSupplier ordinary){
        FickleDoubloon c=active();
        if(min<max && c!=null && c.level()>=5 && Random.Float()<c.level()*BalanceTuning.multiplier(BalanceTuning.Key.COIN_WEIGHTED)/100f)return max;
        return ordinary.getAsInt();
    }
    /** A blocked low-health opportunity never consumes the floor's last hand. */
    public static void damaged(Hero h,int previousHP){
        FickleDoubloon c=active();int floor=Dungeon.depth+100*Dungeon.branch;
        if(c!=null && c.level()>=8 && h.isAlive() && previousHP*4<=h.HT && h.HP<previousHP
                && !luckRunning(h) && !c.lastHands.contains(floor)){
            c.lastHands.add(floor);Buff.affect(h,Luck.class).start(true,3,true);GLog.p(Messages.get(c,"last_hand"));
        }
    }
    public static class Luck extends Buff {
        public boolean favor;
        private boolean single,used,lastHand;
        private float expires;
        {type=buffType.POSITIVE;actPriority=HERO_PRIO+1;revivePersists=true;}
        public void start(boolean favor,int level,boolean lastHand){
            this.favor=favor;this.lastHand=lastHand;single=level==0;used=false;
            // Manual flips spend one action before the useful luck window begins.
            expires=Actor.now()+(lastHand?0:1)+(single?BalanceTuning.get(BalanceTuning.Key.COIN_ZERO_EXPIRY):level);
            type=favor?buffType.POSITIVE:buffType.NEGATIVE;
        }
        public boolean use(){if(used||Actor.now()>=expires)return false;if(single)used=true;return true;}
        @Override public boolean act(){if(used||Actor.now()>=expires)detach();spend(TICK);return true;}
        @Override public void fixTime(float decrement){super.fixTime(decrement);expires-=decrement;}
        @Override public int icon(){return BuffIndicator.BLESS;}
        @Override public void tintIcon(com.watabou.noosa.Image icon){icon.hardlight(favor?0xE0982F:0xA52B35);}
        @Override public String name(){return Messages.get(FickleDoubloon.class,lastHand?"last_hand_name":favor?"favor":"wane");}
        @Override public String desc(){return Messages.get(FickleDoubloon.class,favor?"favor_desc":"wane_desc");}
        @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("favor",favor);b.put("single",single);b.put("used",used);b.put("last_hand",lastHand);b.put("expires",expires);}
        @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);favor=b.getBoolean("favor");single=b.getBoolean("single");used=b.getBoolean("used");lastHand=b.getBoolean("last_hand");expires=b.getFloat("expires");type=favor?buffType.POSITIVE:buffType.NEGATIVE;}
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("coin_face",face);b.put("last_hands",lastHands.stream().mapToInt(Integer::intValue).toArray());}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);level(Math.max(0,Math.min(10,b.getInt("level"))));chargeCap=level()+1;charge=Math.min(chargeCap,b.getInt("charge"));face=b.getInt("coin_face");lastHands.clear();for(int n:b.getIntArray("last_hands"))lastHands.add(n);}
}
