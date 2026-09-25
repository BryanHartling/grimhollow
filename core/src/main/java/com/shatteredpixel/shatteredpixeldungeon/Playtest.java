// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;
import java.io.IOException;
import java.util.*;

/** Explicit, save-local testing tools. No action is available to an ordinary run. */
public final class Playtest {
    private Playtest() {}
    private static boolean enabled, god;
    public static boolean enabled() { return enabled; }
    public static boolean god() { return enabled && god; }
    public static void reset() { enabled = god = false; }
    public static void enable() { enabled = true; }
    public static void require() {
        if (!enabled || Dungeon.hero == null || Dungeon.level == null)
            throw new IllegalStateException("Enable Playtest mode in this save first.");
    }
    public static void store(Bundle b) { b.put("playtest", enabled); b.put("playtest_god", god); }
    public static void restore(Bundle b) { enabled=b.getBoolean("playtest"); god=enabled && b.getBoolean("playtest_god"); }
    public static void god(boolean value) { require(); god=value; if(value)Dungeon.hero.HP=Dungeon.hero.HT; }

    public static void restoreHero() {
        require();
        Hero h=Dungeon.hero;
        for(Buff buff:h.buffs()) if(buff.type==Buff.buffType.NEGATIVE)buff.detach();
        h.HP=h.HT;
        Buff.affect(h,Hunger.class).satisfy(Hunger.STARVING);
        h.interrupt();
        BuffIndicator.refreshHero();
    }
    public static void recharge() {
        require();
        for(Item item:Dungeon.hero.belongings)recharge(item);
        Item.updateQuickslot();
    }
    public static void recharge(Item item) {
        require();
        if(item instanceof Artifact)((Artifact)item).playtestRecharge();
        if(item instanceof Wand)((Wand)item).curCharges=((Wand)item).maxCharges;
        if(item instanceof MagesStaff)((MagesStaff)item).gainCharge(100);
        if(item instanceof ClassArmor)((ClassArmor)item).charge=100;
    }
    public static int maxItemLevel(Item item) {
        if(item instanceof Artifact)return ((Artifact)item).playtestLevelCap()>0?10:0;
        if(item instanceof Trinket)return 3;
        return item.isUpgradable()?20:0;
    }
    public static void itemLevel(Item item,int level) {
        require();
        if(level<0 || level>maxItemLevel(item))throw new IllegalArgumentException("Level outside this item's range.");
        if(item instanceof Artifact)((Artifact)item).playtestLevel(level);
        else item.level(level);
        recharge(item);
        Dungeon.hero.updateHT(false);
        Item.updateQuickslot();
    }
    public static Item create(Class<? extends Item> type,int quantity,int level,boolean identified,boolean cursed) {
        require();
        if(!PlaytestCatalog.items().contains(type))throw new IllegalArgumentException("Item is not in the playtest catalog.");
        if(quantity<1 || quantity>100)throw new IllegalArgumentException("Quantity must be 1-100.");
        Item item=Reflection.newInstance(type);
        if(item==null)throw new IllegalArgumentException("Unable to create this item.");
        itemLevel(item,level);
        if(item.stackable)item.quantity(quantity);
        if(item instanceof Key)((Key)item).depth=Dungeon.depth;
        if(identified)item.identify();
        item.cursed=cursed;
        item.cursedKnown=identified;
        return item;
    }
    /** Uses real inventory rules; overflow goes to the hero's feet, never disappears. */
    public static boolean give(Item item) {
        require();
        if(item instanceof Gold){Dungeon.gold+=item.quantity();return true;}
        if(item instanceof EnergyCrystal){Dungeon.energy+=item.quantity();return true;}
        if(item instanceof Key){Notes.add((Key)item);GameScene.updateKeyDisplay();return true;}
        if(item.collect(Dungeon.hero.belongings.backpack))return true;
        Dungeon.level.drop(item,Dungeon.hero.pos);
        return false;
    }
    public static void enchant(Item item,Class<?> effect) {
        require();
        if(item instanceof Weapon && (effect==null || Weapon.Enchantment.class.isAssignableFrom(effect)))
            ((Weapon)item).enchant(effect==null?null:(Weapon.Enchantment)Reflection.newInstance(effect));
        else if(item instanceof Armor && (effect==null || Armor.Glyph.class.isAssignableFrom(effect)))
            ((Armor)item).inscribe(effect==null?null:(Armor.Glyph)Reflection.newInstance(effect));
        else throw new IllegalArgumentException("Choose a weapon enchantment or an armor glyph.");
        item.identify();
        EnchanterMagic.learn(item);
        Item.updateQuickslot();
    }
    public static void learnInscriptions() {
        require();
        if(EnchanterMagic.state()==null)throw new IllegalStateException("Change to Enchanter first.");
        for(Class<?> type:PlaytestCatalog.enchantments(false)){
            Weapon weapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RunedBaton();
            weapon.enchant((Weapon.Enchantment)Reflection.newInstance(type)); weapon.cursedKnown=true;
            EnchanterMagic.learn(weapon);
        }
        for(Class<?> type:PlaytestCatalog.enchantments(true)){
            Armor armor=new ClothArmor();armor.inscribe((Armor.Glyph)Reflection.newInstance(type));armor.cursedKnown=true;
            EnchanterMagic.learn(armor);
        }
    }
    public static void heroLevel(int level) {
        require();
        if(level<1 || level>Hero.MAX_LEVEL)throw new IllegalArgumentException("Hero level must be 1-30.");
        Dungeon.hero.playtestLevel(level);
    }
    public static void strength(int value) {
        require();
        if(value<1 || value>50)throw new IllegalArgumentException("Strength must be 1-50.");
        Dungeon.hero.STR=value;
    }
    public static void resetTalents() {
        require();
        Hero h=Dungeon.hero;
        h.talents.clear();h.metamorphedTalents.clear();
        Talent.initClassTalents(h);Talent.initSubclassTalents(h);Talent.initArmorTalents(h);
        h.updateHT(false);
    }
    public static void maximizeTalents() {
        require();
        for(LinkedHashMap<Talent,Integer> tier:Dungeon.hero.talents)
            for(Talent talent:new ArrayList<>(tier.keySet()))
                while(Dungeon.hero.pointsInTalent(talent)<talent.maxPoints())Dungeon.hero.upgradeTalent(talent);
        Dungeon.hero.updateHT(false);
    }
    public static void subclass(HeroSubClass value) {
        require();
        if(value!=HeroSubClass.NONE && !Arrays.asList(Dungeon.hero.heroClass.subClasses()).contains(value))
            throw new IllegalArgumentException("Subclass does not belong to this hero.");
        Dungeon.hero.subClass=value;resetTalents();
        Buff.detach(Dungeon.hero,Preparation.class);
        if(value==HeroSubClass.ASSASSIN && Dungeon.hero.invisible>0)Buff.affect(Dungeon.hero,Preparation.class);
    }
    public static void armorAbility(ArmorAbility ability) {
        require();
        if(Arrays.stream(Dungeon.hero.heroClass.armorAbilities()).noneMatch(a->a.getClass()==ability.getClass()))
            throw new IllegalArgumentException("Ability does not belong to this hero.");
        Hero h=Dungeon.hero;
        h.armorAbility=ability;resetTalents();
        if(!(h.belongings.armor instanceof ClassArmor)){
            Armor base=h.belongings.armor==null?new PlateArmor():h.belongings.armor;
            h.belongings.armor=ClassArmor.upgrade(h,base);
        }
        ((ClassArmor)h.belongings.armor).charge=100;
    }
    /** Same hero/save/level/backpack; fresh class kit, talents and temporary state. */
    public static void heroClass(HeroClass value) {
        require();
        Hero h=Dungeon.hero;
        ArrayList<Item> gear=new ArrayList<>();
        Collections.addAll(gear,h.belongings.weapon,h.belongings.armor,h.belongings.artifact,
                h.belongings.misc,h.belongings.ring,h.belongings.secondWep);
        for(Mob mob:new ArrayList<>(Dungeon.level.mobs))if(mob.alignment==Char.Alignment.ALLY){
            mob.destroy();if(mob.sprite!=null)mob.sprite.killAndErase();
        }
        Mob.clearHeldAllies();
        for(Buff buff:h.buffs())buff.detach();
        h.belongings.weapon=h.belongings.secondWep=h.belongings.thrownWeapon=h.belongings.abilityWeapon=null;
        h.belongings.armor=null;h.belongings.artifact=null;h.belongings.misc=null;h.belongings.ring=null;
        h.subClass=HeroSubClass.NONE;h.armorAbility=null;
        h.talents.clear();h.metamorphedTalents.clear();
        Dungeon.quickslot.reset();
        h.belongings.consolidateBags();
        h.live();value.initHero(h);
        for(Item item:gear)if(item!=null)give(item);
        h.updateHT(false);h.HP=h.HT;
        h.curAction=h.lastAction=null;
    }
    public static void reveal() {
        require();
        for(int cell=0;cell<Dungeon.level.length();cell++){
            Dungeon.level.mapped[cell]=true;
            if(Terrain.flags[Dungeon.level.map[cell]]!=0 && Dungeon.level.secret[cell])Dungeon.level.discover(cell);
        }
        GameScene.updateMap();Dungeon.observe();GameScene.updateFog();
    }
    public static void teleport(int cell) {
        require();
        if(!Dungeon.level.insideMap(cell) || !Dungeon.level.passable[cell] || Actor.findChar(cell)!=null)
            throw new IllegalArgumentException("Choose an empty walkable cell.");
        Hero h=Dungeon.hero;h.interrupt();h.pos=cell;
        if(h.sprite!=null){h.sprite.place(cell);h.sprite.turnTo(cell,cell);}
        Dungeon.observe();GameScene.updateFog();
    }
    public static void spawnMob(Class<? extends Mob> type,int cell) {
        require();
        if(!PlaytestCatalog.mobs().contains(type))throw new IllegalArgumentException("Choose a catalog creature.");
        if(!Dungeon.level.insideMap(cell) || !Dungeon.level.passable[cell] || Actor.findChar(cell)!=null)
            throw new IllegalArgumentException("Choose an empty walkable cell.");
        Mob mob=Reflection.newInstance(type);
        if(Char.hasProp(mob,Char.Property.LARGE) && !Dungeon.level.openSpace[cell])
            throw new IllegalArgumentException("This creature needs an open area.");
        mob.pos=cell;
        if(com.watabou.noosa.Game.scene() instanceof GameScene)GameScene.add(mob);
        else {Dungeon.level.mobs.add(mob);Actor.add(mob);}
        Dungeon.observe();
    }
    /** Called in the existing interlevel loading thread, also exercised by headless tests. */
    public static void rebuildFloor() {
        require();Level.beforeTransition();Mob.holdAllies(Dungeon.level);
        Dungeon.switchLevel(Dungeon.newLevel(),-1);
    }
    public static void travel(int depth,int branch) throws IOException {
        require();
        if(depth<1 || depth>26 || branch<0 || branch>1 || (branch==1 && !(depth>=11&&depth<=14 || depth>=16&&depth<=19)))
            throw new IllegalArgumentException("Choose a dungeon floor or a supported quest branch.");
        Level.beforeTransition();
        Mob.holdAllies(Dungeon.level);
        Dungeon.saveAll();
        // Generate skipped main floors in order: shops, limited drops and quest placement depend on this.
        int generateThrough=branch==1?(depth<=14?14:19):depth;
        for(int d=1;d<=generateThrough;d++)if(!Dungeon.levelHasBeenGenerated(d,0)){
            Dungeon.depth=d;Dungeon.branch=0;Dungeon.level=Dungeon.newLevel();
            Dungeon.saveLevel(GamesInProgress.curSlot);
        }
        Dungeon.depth=depth;Dungeon.branch=branch;
        Level level=Dungeon.levelHasBeenGenerated(depth,branch)?Dungeon.loadLevel(GamesInProgress.curSlot):Dungeon.newLevel();
        Dungeon.switchLevel(level,-1);
    }
}
