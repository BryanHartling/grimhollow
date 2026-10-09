// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Phylactery;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.watabou.utils.*;
/** Persistent per-floor talent state. No regenerating resource. */
public class Necromancy extends Buff {
    { revivePersists = true; }
    private final java.util.HashSet<Integer> wardFloors=new java.util.HashSet<>();
    private final java.util.HashMap<Integer,Integer> nourishmentSpent=new java.util.HashMap<>();
    public static int points(Talent talent) { return Dungeon.hero == null ? 0 : Dungeon.hero.pointsInTalent(talent); }
    public static void heal(int amount) { if (Dungeon.hero != null && Dungeon.hero.isAlive()) Dungeon.hero.heal(Math.max(0,amount)); }
    public static void onFood() { for (NecroSkeleton m : NecroSkeleton.minions()) m.HP=Math.min(m.HT,m.HP+Math.round(m.HT*.25f*points(Talent.BONE_MEAL))); }
    public static void onHit(Char enemy) {
        int p=points(Talent.NECROTIC_TOUCH);
        if (p>0 && !(Dungeon.hero.belongings.attackingWeapon() instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon)) {
            Buff.affect(enemy,NecroticTouch.class).refresh(p,Dungeon.hero.lvl); Buff.prolong(enemy,HeroDamage.class,p+1);
        }
    }
    public static class HeroDamage extends FlavourBuff {}
    /** A fixed, refreshed wound, separate from escalating wand Corrosion. */
    public static class NecroticTouch extends Corrosion {
        private int remaining,damage;
        {type=buffType.NEGATIVE;announced=true;}
        public void refresh(int turns,int level){remaining=Math.max(remaining,turns);damage=Math.max(1,level);if(target!=null)target.needsIncomingDOTUpdate=true;}
        @Override public boolean act(){
            if(!target.isAlive()||remaining<=0){detach();return true;}
            target.damage(damage,this);remaining--;target.needsIncomingDOTUpdate=true;
            if(remaining<=0||!target.isAlive())detach();else spend(TICK);
            return true;
        }
        @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.POISON;}
        @Override public void tintIcon(com.watabou.noosa.Image icon){icon.hardlight(0xA6C883);}
        @Override public String iconTextDisplay(){return Integer.toString(damage);}
        @Override public String desc(){return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(this,"desc",damage,remaining);}
        @Override public int totalIncomingDMG(){return damage*remaining;}
        @Override public void detach(){if(target!=null)target.needsIncomingDOTUpdate=true;super.detach();}
        @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("remaining",remaining);b.put("damage",damage);}
        @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);remaining=b.getInt("remaining");damage=b.getInt("damage");}
    }
    public static void markDamage(Char target,Object cause) {
        if (Dungeon.hero != null && Dungeon.hero.heroClass==HeroClass.NECROMANCER && target.alignment==Char.Alignment.ENEMY
            && (cause==Dungeon.hero || cause instanceof NecroSkeleton || cause instanceof Weapon || cause instanceof Wand || cause instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll || cause instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion || cause instanceof com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob || cause instanceof Buff)) Buff.prolong(target,HeroDamage.class,5);
    }
    public static void onDeath(Mob target,Object cause) {
        if (Dungeon.level==null || target.alignment!=Char.Alignment.ENEMY) return;
        Dungeon.level.corpses.put(target.pos,target.HT);
        if (Dungeon.hero==null || Dungeon.hero.heroClass!=HeroClass.NECROMANCER || !Dungeon.hero.isAlive()) return;
        NecroCurse curse=NecroCurse.find(target);
        if (cause==Dungeon.hero || cause instanceof NecroSkeleton || target.buff(HeroDamage.class)!=null || curse!=null) {
            Phylactery item=Dungeon.hero.belongings.getItem(Phylactery.class);
            if (item!=null && item.charges()>=item.cap()) {
                Necromancy passive=Dungeon.hero.buff(Necromancy.class);
                if (passive!=null) passive.sustain(target);
            }
            if (item!=null) item.gainCharge(1+(Random.Float()<.25f*points(Talent.GRAVE_HARVEST)?1:0));
            if (curse!=null) {
                heal(Math.min(15,points(Talent.DARK_PACT)*curse.remaining));
                int p=points(Talent.CURSED_GROUND);
                if (p>0) for (Mob other : Dungeon.level.mobs.toArray(new Mob[0]))
                    if (other!=target && other.alignment==Char.Alignment.ENEMY && Dungeon.level.adjacent(other.pos,target.pos))
                        NecroCurse.apply(other,curse.kind,Math.round(curse.remaining*(.25f+.25f*p)));
            }
        }
    }
    private void sustain(Mob enemy) {
        // Keep NECROTIC_SIPHON as the serialized talent key so invested ranks survive.
        int p=points(Talent.NECROTIC_SIPHON);
        Hunger hunger=target.buff(Hunger.class);
        if (p<=0 || enemy.EXP<=0 || enemy instanceof NecroSkeleton
                || enemy instanceof Necromancer.NecroSkeleton || enemy.properties().contains(Char.Property.BOSS_MINION)
                || hunger==null || hunger.hunger()==0) return;
        int floor=Dungeon.depth+100*Dungeon.branch;
        int spent=nourishmentSpent.containsKey(floor)?nourishmentSpent.get(floor):0;
        int amount=Math.min(10*p,60*p-spent);
        if (amount<=0) return;
        // Plain hunger restoration: no HP healing, Well Fed extension or food hooks.
        hunger.satisfy(amount);
        nourishmentSpent.put(floor,spent+amount);
    }
    public void ward() {
        Hero h=(Hero)target;
        if (h.isAlive() && h.HP < h.HT*.3f && points(Talent.WARD_OF_BONE)>0 && !wardFloors.contains(Dungeon.depth+100*Dungeon.branch)) {
            wardFloors.add(Dungeon.depth+100*Dungeon.branch);
            Buff.affect(h,Barkskin.class).setForDuration(points(Talent.WARD_OF_BONE)==1?h.lvl/2:h.lvl,20);
        }
    }
    @Override public boolean act() { ward(); spend(TICK); return true; }
    @Override public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        int[] wards=new int[wardFloors.size()];int n=0;for(int floor:wardFloors)wards[n++]=floor;b.put("ward_floors",wards);
        int[] floors=new int[nourishmentSpent.size()],amounts=new int[floors.length];n=0;
        for(java.util.Map.Entry<Integer,Integer> entry:nourishmentSpent.entrySet()){floors[n]=entry.getKey();amounts[n++]=entry.getValue();}
        b.put("nourishment_floors",floors);b.put("nourishment_spent",amounts);
    }
    @Override public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);wardFloors.clear();for(int floor:b.getIntArray("ward_floors"))wardFloors.add(floor);
        nourishmentSpent.clear();
        if(b.contains("nourishment_floors")&&b.contains("nourishment_spent")){
            int[] floors=b.getIntArray("nourishment_floors"),amounts=b.getIntArray("nourishment_spent");
            for(int n=0;n<Math.min(floors.length,amounts.length);n++)nourishmentSpent.put(floors[n],Math.max(0,amounts[n]));
        }
    }
}
