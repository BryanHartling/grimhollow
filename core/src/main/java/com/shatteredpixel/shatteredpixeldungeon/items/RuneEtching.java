// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.*;
import java.util.ArrayList;

/** A single movable rune, including at most one of its carrier's upgrades. */
public class RuneEtching extends Item {
    public static boolean equipping;
    public static final Class<?>[] FLOOR_ENCHANTS={Blazing.class,Shocking.class,Chilling.class,Kinetic.class,Lucky.class,Blooming.class};
    public Weapon.Enchantment floorEnchant;
    {unique=true;bones=false;image=ItemSpriteSheet.RUNE_ETCHING;identify();roll();}
    public void roll(){floorEnchant=(Weapon.Enchantment)Reflection.newInstance(Random.element(FLOOR_ENCHANTS));}
    @Override public ArrayList<String> actions(Hero h){return new ArrayList<>();}
    @Override public void doDrop(Hero h){}
    @Override public boolean isUpgradable(){return false;}
    public static RuneEtching find(Hero hero){
        for(Item item:hero.belongings)if(item instanceof Weapon&&((Weapon)item).runeEtching!=null)return ((Weapon)item).runeEtching;
        return hero.belongings.getItem(RuneEtching.class);
    }
    public static boolean etch(Hero hero){
        if(!(hero.belongings.weapon instanceof MeleeWeapon))return false;
        Weapon next=(Weapon)hero.belongings.weapon;RuneEtching rune=find(hero);
        if(rune==null||next.runeEtching==rune)return false;
        for(Item item:hero.belongings)if(item instanceof Weapon&&((Weapon)item).runeEtching==rune)detach((Weapon)item);
        rune.detachAll(hero.belongings.backpack);next.runeEtching=rune;next.level(next.level()+rune.level());Item.updateQuickslot();return true;
    }
    private static RuneEtching detach(Weapon weapon){
        RuneEtching rune=weapon.runeEtching;weapon.runeEtching=null;
        if(rune!=null)weapon.level(weapon.level()-rune.level());return rune;
    }
    public static void recover(Weapon weapon){
        if(equipping||weapon.runeEtching==null||Dungeon.hero==null)return;
        RuneEtching rune=detach(weapon);
        // This mandatory attachment must survive a full pack when its carrier is lost.
        if(!rune.collect())Dungeon.hero.belongings.backpack.items.add(rune);
    }
    @Override public String info(){return super.info()+"\n\n"+floorEnchant.name()+": "+floorEnchant.desc();}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("floor_enchant",floorEnchant);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);floorEnchant=(Weapon.Enchantment)b.get("floor_enchant");if(floorEnchant==null)roll();}
}
