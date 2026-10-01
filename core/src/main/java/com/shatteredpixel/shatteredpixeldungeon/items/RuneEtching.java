// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
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
    public Armor.Glyph floorGlyph;
    private boolean armorMode;
    {unique=true;bones=false;image=ItemSpriteSheet.RUNE_ETCHING;identify();roll();}
    public void roll(){
        floorEnchant=(Weapon.Enchantment)Reflection.newInstance(Random.element(FLOOR_ENCHANTS));
        floorGlyph=Armor.Glyph.randomCommon();
    }
    public Class<?> activeEffect(){return armorMode?floorGlyph.getClass():floorEnchant.getClass();}
    @Override public ArrayList<String> actions(Hero h){return new ArrayList<>();}
    @Override public void doDrop(Hero h){}
    @Override public boolean isUpgradable(){return false;}
    public static RuneEtching find(Hero hero){
        for(Item item:hero.belongings)if(attached(item)!=null)return attached(item);
        return hero.belongings.getItem(RuneEtching.class);
    }
    public static boolean etch(Hero hero){
        return etch(hero,hero.belongings.weapon);
    }
    public static RuneEtching attached(Item item){
        return item instanceof Weapon?((Weapon)item).runeEtching:item instanceof Armor?((Armor)item).runeEtching:null;
    }
    public static boolean canEtch(Hero hero,Item item){
        return item!=null && (item instanceof MeleeWeapon || item instanceof Armor)
                && item.isEquipped(hero) && attached(item)==null && find(hero)!=null;
    }
    public static boolean etch(Hero hero,Item next){
        if(!canEtch(hero,next))return false;
        RuneEtching rune=find(hero);
        // Learn the old effect before changing the carrier; transfers never reroll.
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic magic=hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic.class);
        if(magic!=null)magic.choices(rune.armorMode);
        for(Item item:hero.belongings)if(attached(item)==rune)detach(item);
        rune.detachAll(hero.belongings.backpack);
        rune.armorMode=next instanceof Armor;
        if(next instanceof Armor)((Armor)next).runeEtching=rune;else ((Weapon)next).runeEtching=rune;
        next.level(next.trueLevel()+rune.level());
        if(magic!=null)magic.choices(rune.armorMode);
        Item.updateQuickslot();return true;
    }
    private static RuneEtching detach(Item carrier){
        RuneEtching rune=attached(carrier);
        if(carrier instanceof Weapon)((Weapon)carrier).runeEtching=null;else ((Armor)carrier).runeEtching=null;
        if(rune!=null)carrier.level(carrier.trueLevel()-rune.level());return rune;
    }
    public static void recover(Item carrier){
        if(equipping||attached(carrier)==null||Dungeon.hero==null)return;
        RuneEtching rune=detach(carrier);
        // This mandatory attachment must survive a full pack when its carrier is lost.
        if(!rune.collect())Dungeon.hero.belongings.backpack.items.add(rune);
    }
    @Override public String info(){return super.info()+"\n\n"+floorEnchant.name()+": "+floorEnchant.desc()+"\n\n"+floorGlyph.name()+": "+floorGlyph.desc();}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("floor_enchant",floorEnchant);b.put("floor_glyph",floorGlyph);b.put("armor_mode",armorMode);}
    @Override public void restoreFromBundle(Bundle b){
        super.restoreFromBundle(b);
        floorEnchant=(Weapon.Enchantment)b.get("floor_enchant");
        floorGlyph=(Armor.Glyph)b.get("floor_glyph");armorMode=b.getBoolean("armor_mode");
        if(floorEnchant==null)floorEnchant=(Weapon.Enchantment)Reflection.newInstance(Random.element(FLOOR_ENCHANTS));
        if(floorGlyph==null)floorGlyph=Armor.Glyph.randomCommon();
    }
}
