// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.watabou.utils.*;
import java.util.ArrayList;
public class NecroSkeleton extends DirectableAlly {
    public int summonerLevel=1,remaining=40,grace=0;
    public boolean sacrificed;
    private boolean inheritedBless,inheritedHaste,inheritedBark;
    { spriteClass=NecroSkeletonSprite.class; EXP=0; maxLvl=0; properties.add(Property.UNDEAD); }
    public static ArrayList<NecroSkeleton> minions() {
        ArrayList<NecroSkeleton> result=new ArrayList<>();
        if(Dungeon.level!=null)for(Mob m:Dungeon.level.mobs)if(m instanceof NecroSkeleton && m.isAlive())result.add((NecroSkeleton)m);
        return result;
    }
    public static int cap() {return 2+(Necromancy.points(Talent.BONE_LEGION)>0?1:0);}
    public static int counted() {int n=0;for(NecroSkeleton m:minions())if(m.grace<=0)n++;return n;}
    public void configure(int level) {summonerLevel=level;HT=HP=Math.round(baseHealth()*(1+.1f*Necromancy.points(Talent.STURDY_BONES)));viewDistance=8+2*Necromancy.points(Talent.CORPSE_SENSE);}
    protected int baseHealth() {return 15+4*summonerLevel;}
    public static NecroSkeleton raise(int pos,boolean ghoul,boolean second) {
        if(!Dungeon.level.insideMap(pos) || !Dungeon.level.passable[pos] || Actor.findChar(pos)!=null) return null;
        if(second ? minions().size()>=cap()+1 : counted()>=cap() || minions().size()>=cap()+1) return null;
        NecroSkeleton m=ghoul?new NecroGhoul():new NecroSkeleton();m.configure(Dungeon.hero.lvl);m.pos=pos;m.grace=second?5:0;
        if(com.watabou.noosa.Game.scene() instanceof GameScene)GameScene.add(m);else{Dungeon.level.mobs.add(m);Actor.add(m);}
        Buff.affect(m,Lifetime.class);
        return m;
    }
    @Override public int damageRoll() {return Random.NormalIntRange(2+summonerLevel/2,5+summonerLevel)+Necromancy.points(Talent.DEATHSPEAKERS_COMMAND);}
    @Override public int drRoll() {return super.drRoll()+summonerLevel/3+Necromancy.points(Talent.STURDY_BONES);}
    private float speech() {return 1+.05f*Necromancy.points(Talent.GRAVE_SPEECH)*Math.max(0,minions().size()-1);}
    @Override public int attackSkill(Char target) {return Math.round((10+summonerLevel)*speech());}
    @Override public int defenseSkill(Char enemy) {return Math.round((5+summonerLevel)*speech());}
    public void refreshStats(){
        summonerLevel=Dungeon.hero.lvl;
        int next=Math.round(baseHealth()*(1+.1f*Necromancy.points(Talent.STURDY_BONES)));
        if(next!=HT){HP=Math.min(next,Math.max(1,HP+next-HT));HT=next;}
        viewDistance=8+2*Necromancy.points(Talent.CORPSE_SENSE);
    }
    @Override protected boolean act() {
        refreshStats();
        viewDistance=8+2*Necromancy.points(Talent.CORPSE_SENSE);
        if(Dungeon.hero.subClass==HeroSubClass.DEATHSPEAKER) {
            if(Dungeon.hero.buff(Bless.class)!=null){inheritedBless=buff(Bless.class)==null||inheritedBless;Buff.prolong(this,Bless.class,2);}else if(inheritedBless){Buff.detach(this,Bless.class);inheritedBless=false;}
            if(Dungeon.hero.buff(Haste.class)!=null){inheritedHaste=buff(Haste.class)==null||inheritedHaste;Buff.prolong(this,Haste.class,2);}else if(inheritedHaste){Buff.detach(this,Haste.class);inheritedHaste=false;}
            if(Dungeon.hero.buff(Barkskin.class)!=null){inheritedBark=buff(Barkskin.class)==null||inheritedBark;Buff.affect(this,Barkskin.class).setForDuration(Dungeon.hero.buff(Barkskin.class).level(),2);}else if(inheritedBark){Buff.detach(this,Barkskin.class);inheritedBark=false;}
        }
        return super.act();
    }
    @Override public void die(Object cause) {
        int cell=pos; boolean reborn=!sacrificed && Random.Float()<.15f*Necromancy.points(Talent.SECOND_GRAVE);
        super.die(cause);if(reborn)raise(cell,false,true);
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("summoner_level",summonerLevel);b.put("remaining",remaining);b.put("grace",grace);b.put("inherit",new boolean[]{inheritedBless,inheritedHaste,inheritedBark});}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);summonerLevel=b.getInt("summoner_level");remaining=b.getInt("remaining");grace=b.getInt("grace");boolean[] a=b.getBooleanArray("inherit");if(a.length==3){inheritedBless=a[0];inheritedHaste=a[1];inheritedBark=a[2];}}
    public static class Lifetime extends Buff {
        @Override public boolean act(){
            NecroSkeleton m=(NecroSkeleton)target;
            if(--m.remaining<=0 || (m.grace>0 && --m.grace==0 && counted()>cap())) {m.sacrificed=true;m.die(this);detach();return true;}
            spend(TICK);return true;
        }
    }
}
