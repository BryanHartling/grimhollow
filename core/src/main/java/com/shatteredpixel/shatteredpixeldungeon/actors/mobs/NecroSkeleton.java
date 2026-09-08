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
    public int summonerLevel=1,remaining=30,grace=0;
    public boolean sacrificed;
    private boolean inheritedBless,inheritedHaste,inheritedBark;
    { spriteClass=NecroSkeletonSprite.class; EXP=0; maxLvl=0; properties.add(Property.UNDEAD); }
    public static ArrayList<NecroSkeleton> minions() {
        ArrayList<NecroSkeleton> result=new ArrayList<>();
        if(Dungeon.level!=null)for(Mob m:Dungeon.level.mobs)if(m instanceof NecroSkeleton && m.isAlive())result.add((NecroSkeleton)m);
        return result;
    }
    public static int cap() {return (Dungeon.hero.lvl<7?1:Dungeon.hero.lvl<21?2:3)+(Necromancy.points(Talent.BONE_LEGION)>0?1:0)+(Dungeon.hero.subClass==HeroSubClass.DEATHSPEAKER?1:0);}
    public static int counted() {int n=0;for(NecroSkeleton m:minions())if(m.grace<=0)n+=m.slots();return n;}
    public void configure(int level) {summonerLevel=level;HT=HP=Math.round(baseHealth()*growth()*(1+.1f*Necromancy.points(Talent.STURDY_BONES)));viewDistance=8+2*Necromancy.points(Talent.CORPSE_SENSE);}
    protected int baseHealth() {return 15+4*summonerLevel;}
    public int slots(){return 1;}
    protected float growth(){com.shatteredpixel.shatteredpixeldungeon.items.Phylactery p=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Phylactery.class);return p==null?1:p.minionBonus();}
    public static NecroSkeleton raise(int pos,boolean ghoul,boolean second){return raise(pos,ghoul?2:0,second);}
    public static NecroSkeleton raise(int pos,int tier,boolean second) {
        if(!Dungeon.level.insideMap(pos) || !Dungeon.level.passable[pos] || Actor.findChar(pos)!=null) return null;
        int slots=tier==3?2:1,total=minions().stream().mapToInt(NecroSkeleton::slots).sum();
        if(second ? total>=cap()+1 : counted()+slots>cap() || total+slots>cap()+1) return null;
        if(tier==3&&minions().stream().anyMatch(m->m instanceof NecroRevenant))return null;
        NecroSkeleton m=tier==3?new NecroRevenant():tier==2?new NecroGhoul():tier==1?new NecroWraith():new NecroSkeleton();m.configure(Dungeon.hero.lvl);m.pos=pos;m.grace=second?5:0;
        if(com.watabou.noosa.Game.scene() instanceof GameScene)GameScene.add(m);else{Dungeon.level.mobs.add(m);Actor.add(m);}
        Buff.affect(m,Lifetime.class);
        return m;
    }
    @Override public int damageRoll() {return Math.round(Random.NormalIntRange(2+summonerLevel/2,5+summonerLevel)*growth())+Necromancy.points(Talent.DEATHSPEAKERS_COMMAND);}
    @Override public int drRoll() {return super.drRoll()+summonerLevel/3+Necromancy.points(Talent.STURDY_BONES);}
    private float speech() {return 1+.05f*Necromancy.points(Talent.GRAVE_SPEECH)*Math.max(0,minions().size()-1);}
    @Override public int attackSkill(Char target) {return Math.round((10+summonerLevel)*speech())+(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Phylactery.class)!=null&&Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Phylactery.class).isEquipped(Dungeon.hero)&&Dungeon.level.distance(pos,Dungeon.hero.pos)<=2?1:0);}
    @Override public int defenseSkill(Char enemy) {return Math.round((5+summonerLevel)*speech());}
    public void refreshStats(){
        summonerLevel=Dungeon.hero.lvl;
        int next=Math.round(baseHealth()*growth()*(1+.1f*Necromancy.points(Talent.STURDY_BONES)));
        if(next!=HT){HP=Math.min(next,Math.max(1,HP+next-HT));HT=next;}
        viewDistance=8+2*Necromancy.points(Talent.CORPSE_SENSE);
    }
    private int directedTarget=-1;
    @Override public void targetChar(Char ch){super.targetChar(ch);directedTarget=ch.id();}
    @Override public void followHero(){super.followHero();directedTarget=-1;}
    @Override public void defendPos(int cell){super.defendPos(cell);directedTarget=-1;}
    @Override protected Char chooseEnemy(){
        Char directed=(Char)Actor.findById(directedTarget);
        if(directed!=null&&directed.isAlive()&&directed.alignment==Alignment.ENEMY)return directed;
        directedTarget=-1;
        Char closest=null;int distance=Integer.MAX_VALUE;
        for(Mob mob:Dungeon.level.mobs)if(mob!=this&&mob.isAlive()&&mob.alignment==Alignment.ENEMY
                &&fieldOfView[mob.pos]&&mob.invisible<=0&&!mob.isInvulnerable(getClass())){
            int next=Dungeon.level.distance(Dungeon.hero.pos,mob.pos);
            if(next<distance||(next==distance&&closest!=null&&mob.pos<closest.pos)){closest=mob;distance=next;}
        }
        return closest;
    }
    @Override public boolean act() {
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
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("directed_target",directedTarget);b.put("summoner_level",summonerLevel);b.put("remaining",remaining);b.put("grace",grace);b.put("inherit",new boolean[]{inheritedBless,inheritedHaste,inheritedBark});}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);directedTarget=b.contains("directed_target")?b.getInt("directed_target"):-1;summonerLevel=b.getInt("summoner_level");remaining=b.getInt("remaining");grace=b.getInt("grace");boolean[] a=b.getBooleanArray("inherit");if(a.length==3){inheritedBless=a[0];inheritedHaste=a[1];inheritedBark=a[2];}}
    public static class Lifetime extends Buff {
        @Override public boolean act(){
            NecroSkeleton m=(NecroSkeleton)target;
            if(--m.remaining<=0 || (m.grace>0 && --m.grace==0 && counted()>cap())) {m.sacrificed=true;m.die(this);detach();return true;}
            spend(TICK);return true;
        }
    }
}
