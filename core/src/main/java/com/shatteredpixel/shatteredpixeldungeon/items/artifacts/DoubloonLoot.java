// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
/** Only ordinary random floor loot and enemy equipment call this, not authored rewards. */
public final class DoubloonLoot {
    public static Item improve(Item first){
        float chance=FickleDoubloon.qualityBonus();
        if(chance<=0||first==null||first.unique||!(first instanceof Weapon||first instanceof Armor||first instanceof Wand||first instanceof Ring)
                ||Random.Float()>=chance)return first;
        // A second roll of this subtype preserves every source-specific tier restriction,
        // and consumes neither a unique artifact nor a finite generator deck entry.
        Item second=Reflection.newInstance(first.getClass()).random();
        return compare(second,first)>0?second:first;
    }
    public static int compare(Item a,Item b){
        int result=Boolean.compare(!a.cursed,!b.cursed);
        if(result==0)result=Integer.compare(a.trueLevel(),b.trueLevel());
        if(result==0)result=Boolean.compare(good(a),good(b));
        if(result==0)result=Integer.compare(tier(a),tier(b));
        return result;
    }
    private static boolean good(Item i){return i instanceof Weapon?((Weapon)i).hasGoodEnchant():i instanceof Armor&&((Armor)i).hasGoodGlyph();}
    private static int tier(Item i){return i instanceof MeleeWeapon?((MeleeWeapon)i).tier:i instanceof MissileWeapon?((MissileWeapon)i).tier:i instanceof Armor?((Armor)i).tier:0;}
    public static int shopRoll(){
        FickleDoubloon c=FickleDoubloon.active();int best=Random.Int(10);
        if(c!=null&&c.level()>=10)for(int n=1;n<BalanceTuning.get(BalanceTuning.Key.COIN_SHOP_ROLLS);n++){
            int next=Random.Int(10);if(shopGrade(next)>shopGrade(best))best=next;
        }
        return best;
    }
    private static int shopGrade(int n){return n==2?2:n<2?1:0;}
    public static boolean roomRoll(){FickleDoubloon c=FickleDoubloon.active();return c!=null&&c.level()>=10&&Dungeon.branch==0&&Dungeon.depth>1&&Dungeon.depth<25&&Dungeon.depth%5!=0&&Random.Int(100)<BalanceTuning.get(BalanceTuning.Key.COIN_ROOM);}
}
