// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;

/** Save-local playtest overrides. Defaults deliberately retain the original RNG calls. */
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
        POTION(3), SCROLL(3), SEED(3), STONE(3), GOLD(3);

        public final int group, baseline, min, max;
        Key(int group) { this(group,100,0,1000); }
        Key(int group,int baseline,int min,int max) {
            this.group=group;this.baseline=baseline;this.min=min;this.max=max;
        }
        public String id() { return name().toLowerCase(Locale.ROOT); }
        public String display(int value) { return value+(this==TIER_SHIFT?"":"%"); }
    }
    private static final EnumMap<Key,Integer> values=new EnumMap<>(Key.class);
    public static int get(Key key) { return Playtest.enabled()?values.getOrDefault(key,key.baseline):key.baseline; }
    public static float multiplier(Key key) { return get(key)/100f; }
    public static void set(Key key,int value) {
        Playtest.require();
        if(value<key.min || value>key.max)throw new IllegalArgumentException("Value outside tuning range.");
        if(key.group==3 && value==0) {
            boolean any=false;
            for(Key other:Key.values())if(other.group==3 && other!=key && get(other)>0)any=true;
            if(!any)throw new IllegalArgumentException("Keep at least one item category above zero.");
        }
        if(value==key.baseline)values.remove(key);else values.put(key,value);
    }
    public static void reset() { values.clear(); }
    public static int changedCount() { return Playtest.enabled()?values.size():0; }
    public static void store(Bundle bundle) {
        Bundle tuning=new Bundle();
        for(Key key:values.keySet())tuning.put(key.id(),values.get(key));
        bundle.put("balance_tuning",tuning);
    }
    public static void restore(Bundle bundle) {
        reset();
        if(!Playtest.enabled() || !bundle.contains("balance_tuning"))return;
        Bundle tuning=bundle.getBundle("balance_tuning");
        for(Key key:Key.values())if(tuning.contains(key.id())) {
            int value=Math.max(key.min,Math.min(key.max,tuning.getInt(key.id())));
            if(value!=key.baseline)values.put(key,value);
        }
        boolean any=false;
        for(Key key:Key.values())if(key.group==3 && get(key)>0)any=true;
        if(!any)for(Key key:Key.values())if(key.group==3)values.remove(key);
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
