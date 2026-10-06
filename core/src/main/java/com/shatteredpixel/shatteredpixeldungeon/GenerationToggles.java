// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.watabou.utils.Random;
import java.util.*;

/** Device-wide eligibility for random pools. Never deletes inventory or alters fixed quest rewards. */
public final class GenerationToggles {
    private static final String PROFILE="generation_exclusions_v1";
    private static final Set<String> excluded=new HashSet<>();
    private GenerationToggles(){}
    public static boolean allowed(Class<?> type){
        for(Class<?> parent=type;parent!=null;parent=parent.getSuperclass())
            if(excluded.contains(parent.getName()))return false;
        return true;
    }
    public static int changedCount(){return excluded.size();}
    public static void load(){
        excluded.clear();
        String profile=SPDSettings.getString(PROFILE,"");
        if(!profile.isEmpty())Collections.addAll(excluded,profile.split(";"));
        // A damaged or obsolete profile must never leave a typed generation pool empty.
        try{validate();}catch(IllegalArgumentException invalid){excluded.clear();persist();}
    }
    private static void persist(){SPDSettings.put(PROFILE,String.join(";",new TreeSet<>(excluded)));}
    public static void reset(){Playtest.customBalance();excluded.clear();persist();}
    public static void toggle(Class<?> type){
        String name=type.getName();boolean disabled=excluded.remove(name);
        if(!disabled)excluded.add(name);
        try{validate();}catch(IllegalArgumentException invalid){if(disabled)excluded.add(name);else excluded.remove(name);throw invalid;}
        persist();if(Dungeon.hero!=null)Playtest.customBalance();
    }
    public static List<Class<? extends Item>> itemTypes(){
        Set<Class<? extends Item>> result=new LinkedHashSet<>();
        for(Generator.Category category:Generator.Category.values())for(Class<?> type:category.classes)
            if(Item.class.isAssignableFrom(type))result.add(type.asSubclass(Item.class));
        result.addAll(ExoticPotion.regToExo.values());result.addAll(ExoticScroll.regToExo.values());
        result.add(com.shatteredpixel.shatteredpixeldungeon.items.armor.BoneArmor.class);
        List<Class<? extends Item>> sorted=new ArrayList<>(result);sorted.sort(Comparator.comparing(Class::getSimpleName));return sorted;
    }
    public static float[] filter(float[] weights,Class<?>[] types){
        if(excluded.isEmpty())return weights;
        float[] result=weights.clone();
        for(int i=0;i<result.length;i++)if(!allowed(types[i]))result[i]=0;
        return result;
    }
    public static int pick(float[] weights,Class<?>[] types){return Random.chances(filter(weights,types));}
    private static void validate(){
        for(Generator.Category category:Generator.Category.values()){
            if(category==Generator.Category.ARTIFACT || category.classes.length==0)continue;
            float[] weights=category.defaultProbs==null?category.probs:category.defaultProbs;
            boolean any=false;
            for(int i=0;i<weights.length;i++)if(allowed(category.classes[i])
                    && (weights[i]>0 || category.defaultProbs2!=null && category.defaultProbs2[i]>0))any=true;
            if(!any)throw new IllegalArgumentException("Keep at least one choice in the "+category.name().toLowerCase(Locale.ROOT)+" pool.");
        }
        if(excluded.isEmpty())return;
        Random.pushGenerator(0);
        try{
            for(int depth=1;depth<=24;depth++)if(!Dungeon.bossLevel(depth)){
                boolean any=false;for(Class<? extends Mob> mob:MobSpawner.standardMobRotation(depth))if(allowed(mob))any=true;
                if(!any)throw new IllegalArgumentException("Keep at least one normal enemy for floor "+depth+".");
            }
        }finally{Random.popGenerator();}
    }
}
