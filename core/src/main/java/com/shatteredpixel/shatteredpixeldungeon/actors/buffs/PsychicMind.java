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
    private final HashSet<Integer> floors=new HashSet<>(),precognitionFloors=new HashSet<>();
    public static int points(Talent t){return Dungeon.hero==null?0:Dungeon.hero.pointsInTalent(t);}
    public static PsychicMind state(){return Dungeon.hero==null?null:Dungeon.hero.buff(PsychicMind.class);}
    public static int force(Hero h){return h.heroClass==HeroClass.PSYCHIC?h.lvl/5:0;}
    public static int thrownDamage(Hero h,int damage){damage+=force(h);if(h.buff(MeldedMind.class)!=null&&h.hasTalent(Talent.KINETIC_SURGE))damage=Math.round(damage*(h.pointsInTalent(Talent.KINETIC_SURGE)==3?1.5f:1.25f));return damage;}
    public static boolean calm(){if(Dungeon.level==null)return true;for(Mob mob:Dungeon.level.mobs)if(mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.heroFOV[mob.pos]&&mob.invisible<=0)return false;return true;}
    public void arrive(){int floor=Dungeon.depth+100*Dungeon.branch;if(!floors.add(floor))return;FocusCrystal crystal=Dungeon.hero.belongings.getItem(FocusCrystal.class);if(crystal!=null)crystal.gainCharge(points(Talent.KINETIC_RESERVE));if(Dungeon.hero.subClass==HeroSubClass.SEER&&points(Talent.TREASURE_SENSE)>0)reveal(true,points(Talent.TREASURE_SENSE)>=3,false,false);}
    public boolean dodge(int damage,Object source){
        Hero h=(Hero)target;int floor=Dungeon.depth+100*Dungeon.branch;
        if(!(source instanceof Hunger)&&damage>0&&points(Talent.PRECOGNITION)>0&&h.HP+h.shielding()-damage<h.HT*.25f&&!precognitionFloors.contains(floor)){precognitionFloors.add(floor);return true;}return false;
    }
    @Override public boolean act(){
        Hero h=(Hero)target;
        if(h.subClass==HeroSubClass.SEER){Buff.affect(h,SeerSight.class);for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.distance(h.pos,cell)<=3&&(Dungeon.level.secret[cell]||Dungeon.level.traps.get(cell)!=null))Dungeon.level.discover(cell);}
        else Buff.detach(h,SeerSight.class);
        if(points(Talent.TRAP_SENSE)>0)for(Trap trap:Dungeon.level.traps.valueList())if(Dungeon.level.heroFOV[trap.pos]&&trap.canBeSearched)Dungeon.level.discover(trap.pos);
        for(Mob mob:Dungeon.level.mobs){Amok amok=mob.buff(Amok.class);if(amok!=null&&amok.dominated)amok.share(h);}
        spend(TICK);return true;
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
        if(cause instanceof Mob){Amok amok=((Mob)cause).buff(Amok.class);if(amok!=null&&amok.dominated&&crystal!=null&&Random.Float()<points(Talent.HARVEST_THOUGHT)/3f)crystal.gainCharge(1);}
        if(cause==h||mob.buff(PsychicDamage.class)!=null){MindVision sight=h.buff(MindVision.class);if(sight!=null)Buff.affect(h,MindVision.class,points(Talent.LINGERING_SIGHT));}
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("floors",floors.stream().mapToInt(Integer::intValue).toArray());b.put("precognition_floors",precognitionFloors.stream().mapToInt(Integer::intValue).toArray());}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);for(int i:b.getIntArray("floors"))floors.add(i);for(int i:b.getIntArray("precognition_floors"))precognitionFloors.add(i);}
}
