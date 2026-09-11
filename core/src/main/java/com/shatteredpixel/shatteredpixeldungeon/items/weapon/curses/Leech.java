// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.watabou.utils.*;
public class Leech extends Weapon.Enchantment {
    @Override public int proc(Weapon w,Char a,Char d,int damage){
        float chance=com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic.procChance(a,.3f*procChanceMultiplier(a));
        if(Random.Float()<chance){Recovery recovery=Buff.prolong(d,Recovery.class,1);if(recovery!=null)recovery.attacker=a.id();}
        return damage;
    }
    @Override public boolean curse(){return true;}
    @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0x000000);}
    public static class Recovery extends FlavourBuff {
        int attacker;
        public void recover(Char source,int lostHP){if(source.id()==attacker){if(target.isAlive())target.heal(Math.round(Math.max(0,lostHP)*.2f));detach();}}
        @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("attacker",attacker);}
        @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);attacker=b.getInt("attacker");}
    }
}
