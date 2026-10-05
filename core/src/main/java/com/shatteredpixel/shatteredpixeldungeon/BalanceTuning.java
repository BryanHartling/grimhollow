// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;

/** Device-wide playtest balance profile. Defaults retain the original RNG calls. */
public final class BalanceTuning {
    private BalanceTuning() {}
    public enum Key {
        DENSITY(0,100,0,200), RESPAWN(0,100,0,400), RARE_MOBS(0,100,0,1000),
        CURSEBOUND(1,10,0,100), CURSE_HEALTH(1,30,0,200), CURSE_LOOT(1,100,0,100),
        HEXCASTER(1,10,0,100), CHAINWARDEN(1,30,0,100),
        FLOOR_LOOT(2,100,0,300), MOB_LOOT(2,100,0,500), TIER_SHIFT(2,0,-2,2),
        UPGRADES(2,100,0,400), CURSED_GEAR(2,100,0,400), ENCHANTED_GEAR(2,100,0,1000),
        RARE_ENCHANT(2,100,0,1000), BONE_ARMOR(2,50,0,100),
        WEAPON(3), ARMOR(3), MISSILE(3), WAND(3), RING(3), ARTIFACT(3),
        POTION(3), SCROLL(3), SEED(3), STONE(3), GOLD(3),
        EXPEDITION_CHANCE(4,100,0,100), DRAGON_HEALTH(4,480,120,1500), DRAGON_DAMAGE(4,100,0,300),
        DRAGON_BREATH_COOLDOWN(4,3,3,12), DRAGON_KNOCKBACK(4,2,0,5),
        CAVERN_SIGHT(5,3,2,8), EXPEDITION_FALL_DAMAGE(5,15,0,50),
        BROOD_HEALTH(5,240,60,900), BROOD_DAMAGE(5,100,0,300), BROOD_POISON(5,6,0,20),
        SPIDERS_INITIAL(5,6,0,12), BROOD_LIVE(5,3,0,6), BROOD_TOTAL(5,6,0,18),
        SPIDERS_CAP(5,9,0,18), BROOD_INTERVAL(5,5,2,12),
        CAVERN_RATIONS(6,3,0,8), CAVERN_TORCHES(6,4,0,12), HOARD_GOLD(6,2500,0,10000),
        HOARD_EQUIPMENT(6,3,0,6), HOARD_UPGRADES(6,3,0,10), HOARD_ARTIFACT(6,25,0,100), HOARD_TRINKET(6,25,0,100),
        CAVERN_REMAINS(7,48,20,100), CAVERN_GEAR_WEIGHT(7,80,0,100), CAVERN_RING_WEIGHT(7,5,0,100),
        CAVERN_GOLD_WEIGHT(7,10,0,100), CAVERN_CONSUMABLE_WEIGHT(7,5,0,100),
        CAVERN_MAX_TIER(7,3,1,5), CAVERN_UPGRADES(7,0,0,3),
        HORROR_CHANCE(8,50,0,100), HORROR_DAMAGE(8,100,0,200), HORROR_EVASION(8,100,25,200),
        HORROR_FLIGHT(8,100,25,200), HORROR_HEALING(8,25,0,25),
        CACHE_CHANCE(9,100,0,300), CACHE_FIRE_CHANCE(9,6,0,100), CACHE_WATER_CHANCE(9,5,0,100), CACHE_LIGHTNING_CHANCE(9,4,0,100),
        CACHE_LOOT(9,100,0,300), CACHE_FIRE_KEY(9,4,1,8), CACHE_WATER_KEY(9,5,1,8), CACHE_LIGHTNING_KEY(9,6,1,8),
        COIN_HEADS(10,50,0,70), COIN_ZERO_EXPIRY(10,2,1,10), COIN_CHARGE_TURNS(10,40,10,200),
        COIN_GOLD_CHARGE(10,100,25,500), COIN_GOLD(10,100,0,300), COIN_QUALITY(10,100,0,300),
        COIN_WEIGHTED(10,100,0,300), COIN_THEFT_RESIST(10,50,0,100), COIN_ROOM(10,10,0,100),
        COIN_CACHE(10,10,0,100), COIN_SHOP_ROLLS(10,2,1,3),
        COMPANION_HEALTH(11,100,25,300), COMPANION_DAMAGE(11,100,25,300), COMPANION_STEAL(11,30,0,100),
        COMPANION_GOLD(11,100,0,300), COMPANION_ROOT(11,2,0,5), COMPANION_ROOT_COOLDOWN(11,8,3,30),
        COMPANION_RECOVERY(11,100,25,400), COMPANION_COLLECT(11,5,0,8),
        CHART_CHANCE_0(12,10,0,100), CHART_CHANCE_1(12,15,0,100), CHART_CHANCE_2(12,20,0,100), CHART_CHANCE_3(12,25,0,100),
        CHART_GOLD(12,100,25,300), CHART_ITEMS_0(12,2,2,8), CHART_ITEMS_1(12,3,2,8), CHART_ITEMS_2(12,4,2,8), CHART_ITEMS_3(12,5,2,8),
        CHART_QUALITY_ROLLS(12,2,1,4), CHART_MEMORY_CELLS(12,36,6,100);

        public final int group, baseline, min, max;
        Key(int group) { this(group,100,0,1000); }
        Key(int group,int baseline,int min,int max) {
            this.group=group;this.baseline=baseline;this.min=min;this.max=max;
        }
        public String id() { return name().toLowerCase(Locale.ROOT); }
        public String display(int value) { boolean percentage = this != TIER_SHIFT && (group < 4 || this == EXPEDITION_CHANCE
                    || this == DRAGON_DAMAGE || this == BROOD_DAMAGE || this == EXPEDITION_FALL_DAMAGE
                    || group==9 && (this==CACHE_CHANCE || this==CACHE_LOOT || name().endsWith("_CHANCE")) || this == HOARD_ARTIFACT || this == HOARD_TRINKET || group==8
                    || group==10 && this!=COIN_ZERO_EXPIRY && this!=COIN_CHARGE_TURNS && this!=COIN_GOLD_CHARGE && this!=COIN_SHOP_ROLLS
                    || group==11 && (this==COMPANION_HEALTH || this==COMPANION_DAMAGE || this==COMPANION_STEAL || this==COMPANION_GOLD));
            percentage |= group==12 && (name().startsWith("CHART_CHANCE") || this==CHART_GOLD);
            return value+(percentage?"%":""); }
    }
    private static final EnumMap<Key,Integer> values=new EnumMap<>(Key.class);
    public static final Key[] CAVERN_LOOT_WEIGHTS={Key.CAVERN_GEAR_WEIGHT,Key.CAVERN_RING_WEIGHT,
            Key.CAVERN_GOLD_WEIGHT,Key.CAVERN_CONSUMABLE_WEIGHT};
    public static final Key[] CACHE_BASE_CHANCES={Key.CACHE_FIRE_CHANCE,Key.CACHE_WATER_CHANCE,Key.CACHE_LIGHTNING_CHANCE};
    private static final String PROFILE="balance_profile_v1";
    public static int configured(Key key) { return values.getOrDefault(key,key.baseline); }
    public static int get(Key key) { return Playtest.enabled()?configured(key):key.baseline; }
    public static float multiplier(Key key) { return get(key)/100f; }
    public static void set(Key key,int value) {
        Playtest.require();
        setShared(key,value);
    }
    /** Edit the device profile without loading or modifying a saved hero. */
    public static void setShared(Key key,int value) {
        if(value<key.min || value>key.max)throw new IllegalArgumentException("Value outside tuning range.");
        if(key.group==3 && value==0) {
            boolean any=false;
            for(Key other:Key.values())if(other.group==3 && other!=key && configured(other)>0)any=true;
            if(!any)throw new IllegalArgumentException("Keep at least one item category above zero.");
        }
        if(java.util.Arrays.asList(CAVERN_LOOT_WEIGHTS).contains(key) && value==0) {
            boolean any=false;
            for(Key other:CAVERN_LOOT_WEIGHTS)if(other!=key && configured(other)>0)any=true;
            if(!any)throw new IllegalArgumentException("Keep at least one cavern loot weight above zero.");
        }
        if(value==key.baseline)values.remove(key);else values.put(key,value);
        persist();
    }
    public static void reset() { values.clear(); persist(); }
    public static int changedCount() { return values.size(); }
    private static void persist() {
        StringBuilder profile=new StringBuilder();
        for(Key key:values.keySet())profile.append(key.id()).append('=').append(values.get(key)).append(';');
        // An empty profile is authoritative: old saves must not undo a reset.
        SPDSettings.put(PROFILE,profile.toString());
    }
    public static void loadShared() {
        values.clear();
        for(String entry:SPDSettings.getString(PROFILE,"").split(";")) {
            String[] pair=entry.split("=");
            if(pair.length!=2)continue;
            try {
                Key key=Key.valueOf(pair[0].toUpperCase(Locale.ROOT));
                int value=Math.max(key.min,Math.min(key.max,Integer.parseInt(pair[1])));
                if(value!=key.baseline)values.put(key,value);
            } catch(IllegalArgumentException ignored) { /* Ignore removed keys or damaged entries. */ }
        }
        sanitize();
    }
    public static void store(Bundle bundle) {
        Bundle tuning=new Bundle();
        for(Key key:values.keySet())tuning.put(key.id(),values.get(key));
        bundle.put("balance_tuning",tuning);
    }
    public static void restore(Bundle bundle) {
        if(SPDSettings.contains(PROFILE)) { loadShared(); return; }
        values.clear();
        if(!Playtest.enabled() || !bundle.contains("balance_tuning"))return;
        Bundle tuning=bundle.getBundle("balance_tuning");
        for(Key key:Key.values())if(tuning.contains(key.id())) {
            int value=Math.max(key.min,Math.min(key.max,tuning.getInt(key.id())));
            if(value!=key.baseline)values.put(key,value);
        }
        sanitize();
        // Adopt the first legacy customized save once, until the user edits or resets it.
        if(!values.isEmpty())persist();
    }
    private static void sanitize() {
        boolean any=false;
        for(Key key:Key.values())if(key.group==3 && values.getOrDefault(key,key.baseline)>0)any=true;
        if(!any)for(Key key:Key.values())if(key.group==3)values.remove(key);
        any=false;
        for(Key key:CAVERN_LOOT_WEIGHTS)if(values.getOrDefault(key,key.baseline)>0)any=true;
        if(!any)for(Key key:CAVERN_LOOT_WEIGHTS)values.remove(key);
    }
    public static int count(Key key,int count) { return Math.round(count*multiplier(key)); }
    public static boolean roll(Key key,int denominator,int successes) {
        return get(key)==key.baseline ? Random.Int(denominator)<successes : Random.Int(100)<get(key);
    }
    public static boolean upgradeRoll(int denominator) {
        return get(Key.UPGRADES)==100 ? Random.Int(denominator)==0 : Random.Float()<multiplier(Key.UPGRADES)/denominator;
    }
    public static int tier(int rolled) { return Math.max(0,Math.min(4,rolled+get(Key.TIER_SHIFT))); }
    public static float[] enchantRarity(float[] original) {
        if(get(Key.RARE_ENCHANT)==100)return original;
        float[] adjusted=original.clone();adjusted[2]*=multiplier(Key.RARE_ENCHANT);return adjusted;
    }
    public static boolean customItemMix() {
        for(Key key:Key.values())if(key.group==3 && get(key)!=100)return true;
        return false;
    }
    /** Custom category draws are independent; item sub-decks and artifact uniqueness stay native. */
    public static Generator.Category itemCategory() {
        LinkedHashMap<Generator.Category,Float> weights=new LinkedHashMap<>();
        for(Key key:Key.values())if(key.group==3) {
            Generator.Category category=Generator.Category.valueOf(key.name());
            weights.put(category,(category.firstProb+category.secondProb)*multiplier(key));
        }
        return Random.chances(weights);
    }
}
