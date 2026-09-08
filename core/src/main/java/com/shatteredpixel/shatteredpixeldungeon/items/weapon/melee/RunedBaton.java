// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.*;
import java.util.ArrayList;
public class RunedBaton extends MeleeWeapon {
    public static final Class<?>[] FLOOR_ENCHANTS={Blazing.class,Shocking.class,Chilling.class,Kinetic.class,Lucky.class,Blooming.class};
    public Weapon.Enchantment floorEnchant;
    {tier=1;image=ItemSpriteSheet.RUNED_BATON;unique=true;bones=false;roll();}
    public void roll(){floorEnchant=(Weapon.Enchantment)Reflection.newInstance(Random.element(FLOOR_ENCHANTS));}
    @Override public int min(int lvl){return 2+lvl;}
    @Override public int max(int lvl){return 6+2*lvl;}
    @Override public ArrayList<String> actions(Hero hero){ArrayList<String>a=super.actions(hero);a.remove(AC_DROP);a.remove(AC_THROW);return a;}
    @Override public void doDrop(Hero hero){}
    @Override public int proc(Char a,Char d,int damage){damage=super.proc(a,d,damage);if(a.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune.class)==null&&d.isAlive())damage=EnchanterMagic.weaponProc(floorEnchant,this,a,d,damage,.5f);return damage;}
    @Override public String info(){return super.info()+"\n\n"+Messages.get(this,"floor",floorEnchant.name());}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("floor_enchant",floorEnchant);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);floorEnchant=(Weapon.Enchantment)b.get("floor_enchant");if(floorEnchant==null)roll();}
}
