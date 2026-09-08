// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunedBaton;
import com.watabou.utils.*;
import java.util.*;
import com.watabou.utils.Random;
/** Run knowledge, item-bound temporary effects, and Enchanter talent hooks. */
public class EnchanterMagic extends Buff {
    private final Set<String> known=new TreeSet<>(),floorKnown=new TreeSet<>();
    private final Set<Integer> floors=new HashSet<>();
    private int lastPos=-1,stationary,lastFloor=-1;
    private static final ThreadLocal<Float> strength=ThreadLocal.withInitial(()->1f);
    public static int points(Talent t){return Dungeon.hero==null?0:Dungeon.hero.pointsInTalent(t);}
    public static EnchanterMagic state(){return Dungeon.hero==null?null:Dungeon.hero.buff(EnchanterMagic.class);}
    public static void learn(Item item){
        EnchanterMagic state=state();if(state==null)return;
        if(item instanceof Weapon){Weapon.Enchantment e=((Weapon)item).enchantment;if(e!=null&&!e.curse()&&item.cursedKnown)state.known.add(e.getClass().getName());}
        if(item instanceof Armor){Armor.Glyph g=((Armor)item).glyph;if(g!=null&&!g.curse()&&item.cursedKnown)state.known.add(g.getClass().getName());}
    }
    public java.util.List<Class<?>> choices(boolean armor){
        Set<String> all=new TreeSet<>(known);all.addAll(floorKnown);
        for(Class<?> type:Statistics.itemTypesDiscovered)if(Weapon.Enchantment.class.isAssignableFrom(type)||Armor.Glyph.class.isAssignableFrom(type))all.add(type.getName());
        Item weapon=Dungeon.hero.belongings.weapon;
        if(weapon instanceof RunedBaton)all.add(((RunedBaton)weapon).floorEnchant.getClass().getName());
        java.util.List<Class<?>> list=new ArrayList<>();
        for(String name:all)try{Class<?> type=Class.forName(name);if((armor?Armor.Glyph.class:Weapon.Enchantment.class).isAssignableFrom(type))list.add(type);}catch(ClassNotFoundException ignored){}
        return list;
    }
    public void arrive(){
        int floor=Dungeon.depth+100*Dungeon.branch;if(lastFloor==floor)return;
        boolean descending=lastFloor!=-1&&floor>lastFloor;lastFloor=floor;lastPos=Dungeon.hero.pos;stationary=0;
        Hero h=Dungeon.hero;SigilBrush brush=h.belongings.getItem(SigilBrush.class);
        if(descending&&brush!=null)brush.gainCharge(points(Talent.FIELD_REPAIR));
        if(h.belongings.weapon instanceof RunedBaton)((RunedBaton)h.belongings.weapon).roll();
        floorKnown.clear();while(floorKnown.size()<Math.max(0,points(Talent.DEEP_KNOWLEDGE)-1))floorKnown.add(Weapon.Enchantment.random().getClass().getName());
        if(floors.add(floor)&&descending&&h.subClass==HeroSubClass.ARTIFICER&&Random.Float()<.25f*points(Talent.LASTING_WORK)){
            for(Item item:new Item[]{h.belongings.weapon,h.belongings.armor})if(item!=null){
                if(item instanceof Weapon&&((Weapon)item).inscribed!=null){((Weapon)item).enchant(((Weapon)item).inscribed);((Weapon)item).inscribed=null;item.inscriptionTurns=0;learn(item);break;}
                if(item instanceof Armor&&((Armor)item).inscribed!=null){((Armor)item).inscribe(((Armor)item).inscribed);((Armor)item).inscribed=null;item.inscriptionTurns=0;learn(item);break;}
                if(item.reinforceTurns>0){item.upgrade();item.reinforceTurns=0;item.reinforceFlat=0;break;}
            }
        }
    }
    @Override public boolean act(){
        Hero h=(Hero)target;
        if(lastPos==h.pos)stationary++;else{lastPos=h.pos;stationary=0;}
        if(stationary>=3&&points(Talent.WARDING_SIGILS)>0)Buff.affect(h,Barkskin.class).setForDuration(h.lvl*points(Talent.WARDING_SIGILS),5);
        Set<Item> items=new HashSet<>();for(Item item:h.belongings)items.add(item);
        if(Dungeon.level!=null)for(Heap heap:Dungeon.level.heaps.valueList())items.addAll(heap.items);
        for(Item item:items){
            if(item.reinforceTurns>0&&--item.reinforceTurns==0)item.reinforceFlat=0;
            if(item.inscriptionTurns>0&&--item.inscriptionTurns==0){
                int p=points(Talent.OVERLOAD);
                if(p>0&&item.isEquipped(h))for(Mob mob:Dungeon.level.mobs.toArray(new Mob[0]))if(mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.adjacent(h.pos,mob.pos)){
                    if(item instanceof Weapon&&((Weapon)item).inscribed!=null)weaponProc(((Weapon)item).inscribed,(Weapon)item,h,mob,0,p*.5f);
                    if(item instanceof Armor&&((Armor)item).inscribed!=null)glyphProc(((Armor)item).inscribed,(Armor)item,mob,h,0,p*.5f);
                }
                if(item instanceof Weapon)((Weapon)item).inscribed=null;
                if(item instanceof Armor)((Armor)item).inscribed=null;
            }
        }
        spend(TICK);return true;
    }
    public static void consume(Hero h){SigilBrush brush=h.belongings.getItem(SigilBrush.class);if(brush!=null)brush.advance(5*h.pointsInTalent(Talent.KEEN_STUDY));}
    public static void collect(Item item){if(state()==null)return;if(Random.Float()<.5f*points(Talent.ATTUNEMENT))item.cursedKnown=true;learn(item);}
    public static int armorRoll(Char ch){if(ch.buff(FracturedArmor.class)!=null||ch.buff(Unmade.class)!=null)return 0;int dr=ch.drRoll();return ch.buff(DegradedGear.class)!=null?Math.round(dr*.7f):dr;}
    public static void counterweight(){if(Dungeon.hero.subClass==HeroSubClass.SCRIVENER&&points(Talent.COUNTERWEIGHT)>0)Buff.affect(Dungeon.hero,Barkskin.class).setForDuration(Dungeon.hero.lvl/2,points(Talent.COUNTERWEIGHT));}
    public static float procChance(Char ch,float chance){float boost=1+.25f*points(Talent.AMPLIFIED);return ch.buff(Overcharged.class)!=null?Math.max(1f,chance/boost)*boost:chance;}
    public static float procStrength(Char ch){return strength.get()*(ch.buff(Overcharged.class)!=null?1+.25f*points(Talent.AMPLIFIED):1);}
    public static float permanent(Item item){return item.inscriptionTurns>0?1+.1f*points(Talent.RESONANCE):1;}
    public static int weaponProc(Weapon.Enchantment enchant,Weapon w,Char a,Char d,int damage,float power){
        if(a.buff(Unmade.class)!=null)return damage;
        float previous=strength.get();strength.set(previous*power);
        try{if(a==Dungeon.hero&&state()!=null)Buff.prolong(d,EnchanterDamage.class,20);return enchant.proc(w,a,d,damage);}finally{strength.set(previous);}
    }
    public static int glyphProc(Armor.Glyph glyph,Armor armor,Char a,Char d,int damage,float power){float previous=strength.get();strength.set(previous*power);try{return glyph.proc(armor,a,d,damage);}finally{strength.set(previous);}}
    public static class EnchanterDamage extends FlavourBuff {}
    public static void onDeath(Mob mob,Object cause){
        if(state()==null||mob.alignment!=Char.Alignment.ENEMY||(cause!=Dungeon.hero&&mob.buff(EnchanterDamage.class)==null))return;
        SigilBrush brush=Dungeon.hero.belongings.getItem(SigilBrush.class);if(brush==null)return;
        if(Dungeon.hero.buff(Overcharged.class)!=null&&Random.Float()<points(Talent.FEEDBACK)/3f)brush.gainCharge(1);
        if(mob.buff(Unmade.class)!=null)brush.gainCharge(points(Talent.SALVAGE));
    }
    public static int strip(Char enemy){int count=0;for(Buff buff:enemy.buffs())if(!buff.revivePersists){buff.detach();count++;}return count;}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("known",known.toArray(new String[0]));b.put("floor_known",floorKnown.toArray(new String[0]));b.put("floors",floors.stream().mapToInt(Integer::intValue).toArray());b.put("last_floor",lastFloor);b.put("last_pos",lastPos);b.put("stationary",stationary);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);Collections.addAll(known,b.getStringArray("known"));Collections.addAll(floorKnown,b.getStringArray("floor_known"));for(int n:b.getIntArray("floors"))floors.add(n);lastFloor=b.getInt("last_floor");lastPos=b.getInt("last_pos");stationary=b.getInt("stationary");}
}
