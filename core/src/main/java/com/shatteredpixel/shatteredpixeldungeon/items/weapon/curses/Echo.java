// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
public class Echo extends Weapon.Enchantment implements com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.Doom {
    @Override public void onDeath(){com.shatteredpixel.shatteredpixeldungeon.Dungeon.fail(this);}
    @Override public int proc(Weapon w,Char a,Char d,int damage){arm(a,d,.25f,getClass());return damage;}
    public static void arm(Char attacker,Char defender,float fraction,Class<? extends Weapon.Enchantment> cause){
        Recoil recoil=com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(defender,Recoil.class,1);
        if(recoil!=null){recoil.attacker=attacker.id();recoil.fraction=fraction;recoil.cause=cause;}
    }
    public static class Recoil extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {
        private int attacker;private float fraction;private Class<? extends Weapon.Enchantment> cause;
        public void reflect(Char source,int lostHP){
            if(source.id()==attacker&&source.isAlive()&&lostHP>0)source.damage(Math.round(lostHP*fraction),com.watabou.utils.Reflection.newInstance(cause));
            detach();
        }
        @Override public void storeInBundle(com.watabou.utils.Bundle b){super.storeInBundle(b);b.put("attacker",attacker);b.put("fraction",fraction);b.put("cause",cause);}
        @Override public void restoreFromBundle(com.watabou.utils.Bundle b){super.restoreFromBundle(b);attacker=b.getInt("attacker");fraction=b.getFloat("fraction");cause=b.getClass("cause");}
    }
    @Override public boolean curse(){return true;}
    @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0x000000);}
}
