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
/** Persistent once-per-floor and once-per-turn talent state. No regenerating resource. */
public class Necromancy extends Buff {
    { revivePersists = true; }
    private final java.util.HashSet<Integer> wardFloors=new java.util.HashSet<>();
    private float siphonTurn=-100;
    public static int points(Talent talent) { return Dungeon.hero == null ? 0 : Dungeon.hero.pointsInTalent(talent); }
    public static void heal(int amount) { if (Dungeon.hero != null && Dungeon.hero.isAlive()) Dungeon.hero.HP=Math.min(Dungeon.hero.HT,Dungeon.hero.HP+Math.max(0,amount)); }
    public static void onFood() { for (NecroSkeleton m : NecroSkeleton.minions()) m.HP=Math.min(m.HT,m.HP+Math.round(m.HT*.25f*points(Talent.BONE_MEAL))); }
    public static void onHit(Char enemy) {
        int p=points(Talent.NECROTIC_TOUCH);
        if (p>0 && !(Dungeon.hero.belongings.attackingWeapon() instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon)) {
            Buff.affect(enemy,Corrosion.class).set(p,1); Buff.prolong(enemy,HeroDamage.class,p+1);
        }
    }
    public static class HeroDamage extends FlavourBuff {}
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
    public void siphon(Char enemy) {
        int p=points(Talent.NECROTIC_SIPHON);
        if (p>0 && enemy.isAlive() && com.shatteredpixel.shatteredpixeldungeon.Statistics.duration+Actor.now()>=siphonTurn+1) {
            siphonTurn=com.shatteredpixel.shatteredpixeldungeon.Statistics.duration+Actor.now(); int amount=Math.min(p,enemy.HP); heal(amount); enemy.damage(amount,this);
        }
    }
    public void ward() {
        Hero h=(Hero)target;
        if (h.isAlive() && h.HP < h.HT*.3f && points(Talent.WARD_OF_BONE)>0 && !wardFloors.contains(Dungeon.depth+100*Dungeon.branch)) {
            wardFloors.add(Dungeon.depth+100*Dungeon.branch);
            Buff.affect(h,Barkskin.class).setForDuration(points(Talent.WARD_OF_BONE)==1?h.lvl/2:h.lvl,20);
        }
    }
    @Override public boolean act() { ward(); spend(TICK); return true; }
    @Override public void storeInBundle(Bundle b) { super.storeInBundle(b);b.put("ward_floors",wardFloors.stream().mapToInt(Integer::intValue).toArray());b.put("siphon_turn",siphonTurn); }
    @Override public void restoreFromBundle(Bundle b) { super.restoreFromBundle(b);wardFloors.clear();for(int floor:b.getIntArray("ward_floors"))wardFloors.add(floor);siphonTurn=b.getFloat("siphon_turn"); }
}
