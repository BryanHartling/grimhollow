// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.utils.*;
import java.util.HashSet;
public class PsychicMind extends Buff {
    { revivePersists = true; }
    private final HashSet<Integer> floors=new HashSet<>();
    private final java.util.HashMap<Integer,Integer> precognitionUses=new java.util.HashMap<>();
    public static int points(Talent t){return Dungeon.hero==null?0:Dungeon.hero.pointsInTalent(t);}
    public static PsychicMind state(){return Dungeon.hero==null?null:Dungeon.hero.buff(PsychicMind.class);}
    public static int force(Hero h){
        if(h==null||h.heroClass!=HeroClass.PSYCHIC)return 0;
        return 1+(h.lvl>=6?1:0)+(h.lvl>=12?1:0)+(h.lvl>=18?1:0)+(h.lvl>=24?1:0);
    }
    public static int effectiveUpgrade(Hero h,int actual){return force(h)>0?Math.max(actual,force(h)):actual;}
    public static int thrownDamage(Hero h,int damage){if(h.buff(MeldedMind.class)!=null&&h.hasTalent(Talent.KINETIC_SURGE))damage=Math.round(damage*(1+.125f*h.pointsInTalent(Talent.KINETIC_SURGE)));return damage;}
    public static boolean calm(){if(Dungeon.level==null)return true;for(Mob mob:Dungeon.level.mobs)if(mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.heroFOV[mob.pos]&&mob.invisible<=0)return false;return true;}
    public void arrive(){int floor=Dungeon.depth+100*Dungeon.branch;if(!floors.add(floor))return;FocusCrystal crystal=Dungeon.hero.belongings.getItem(FocusCrystal.class);if(crystal!=null)crystal.gainCharge(points(Talent.KINETIC_RESERVE));if(Dungeon.hero.subClass==HeroSubClass.SEER&&points(Talent.TREASURE_SENSE)>0)reveal(true,points(Talent.TREASURE_SENSE)>=3,false,false);if(Dungeon.hero.subClass==HeroSubClass.SEER&&points(Talent.TREASURE_SENSE)>=2)revealDoors(Integer.MAX_VALUE);}
    public boolean dodge(int damage,Object source){
        Hero h=(Hero)target;int floor=Dungeon.depth+100*Dungeon.branch;
        if(!(source instanceof Hunger)&&damage>0&&points(Talent.PRECOGNITION)>0&&h.HP+h.shielding()-damage<h.HT*.25f&&precognitionUses.getOrDefault(floor,0)<points(Talent.PRECOGNITION)){precognitionUses.put(floor,precognitionUses.getOrDefault(floor,0)+1);return true;}return false;
    }
    @Override public boolean act(){
        Hero h=(Hero)target;
        if(h.subClass==HeroSubClass.SEER){Buff.affect(h,SeerSight.class);for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.distance(h.pos,cell)<=3&&(Dungeon.level.secret[cell]||Dungeon.level.traps.get(cell)!=null))Dungeon.level.discover(cell);}
        else Buff.detach(h,SeerSight.class);
        if(points(Talent.TRAP_SENSE)>0)for(Trap trap:Dungeon.level.traps.valueList())if(Dungeon.level.heroFOV[trap.pos]&&trap.canBeSearched)Dungeon.level.discover(trap.pos);
        for(Mob mob:Dungeon.level.mobs){Amok amok=mob.buff(Amok.class);if(amok!=null&&amok.dominated)amok.share(h);PsychicDomination control=mob.buff(PsychicDomination.class);if(control!=null)control.share(h);}
        spend(TICK);return true;
    }
    public static void revealDoors(int radius){
        for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.map[cell]==Terrain.SECRET_DOOR && Dungeon.level.distance(Dungeon.hero.pos,cell)<=radius)Dungeon.level.discover(cell);
        Dungeon.observe();com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap();
    }
    public static void reveal(boolean items,boolean traps,boolean secrets,boolean map){
        Level level=Dungeon.level;
        if(items)for(Heap heap:level.heaps.valueList()){heap.seen=true;for(int n:PathFinder.NEIGHBOURS9)if(level.insideMap(heap.pos+n))level.mapped[heap.pos+n]=true;}
        if(traps)for(Trap trap:level.traps.valueList())level.discover(trap.pos);
        if(secrets)for(int cell=0;cell<level.length();cell++)if(level.secret[cell])level.discover(cell);
        if(map)java.util.Arrays.fill(level.mapped,true);
        Dungeon.observe();com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateFog();
    }
    public static class PsychicDamage extends FlavourBuff {}
    public static void onDeath(Mob mob,Object cause){
        if(state()==null||mob.alignment!=Char.Alignment.ENEMY)return;
        Hero h=Dungeon.hero;FocusCrystal crystal=h.belongings.getItem(FocusCrystal.class);
        if(cause instanceof Mob){Amok amok=((Mob)cause).buff(Amok.class);PsychicDomination control=((Mob)cause).buff(PsychicDomination.class);if(((amok!=null&&amok.dominated)||(control!=null&&control.ownedBy(h)))&&crystal!=null&&Random.Float()<points(Talent.HARVEST_THOUGHT)/3f)crystal.gainCharge(1);}
        if(cause==h||mob.buff(PsychicDamage.class)!=null){MindVision sight=h.buff(MindVision.class);if(sight!=null)Buff.affect(h,MindVision.class,points(Talent.LINGERING_SIGHT));}
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("floors",floors.stream().mapToInt(Integer::intValue).toArray());int[] keys=precognitionUses.keySet().stream().mapToInt(Integer::intValue).toArray();b.put("precognition_floors",keys);b.put("precognition_uses",java.util.Arrays.stream(keys).map(k->precognitionUses.get(k)).toArray());}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);for(int i:b.getIntArray("floors"))floors.add(i);precognitionUses.clear();int[] keys=b.getIntArray("precognition_floors"),counts=b.getIntArray("precognition_uses");for(int i=0;i<keys.length;i++)precognitionUses.put(keys[i],i<counts.length?counts[i]:1);}
}
