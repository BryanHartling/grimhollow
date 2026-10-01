// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.BalanceTuning;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Bundle;
import java.util.ArrayList;

/** Independent optional leaf rooms, each elemental kind encountered at most once per run. */
public class SecretElementalRoom extends SecretRoom {
    private static int encountered;
    private ElementalCache.Kind selectedKind=ElementalCache.Kind.FIRE;
    public SecretElementalRoom() {}
    public SecretElementalRoom(ElementalCache.Kind kind) { selectedKind=kind; }
    public static void reset() { encountered=0; }
    public static void storeRun(Bundle b) { b.put("elemental_types_seen",encountered); }
    public static void restoreRun(Bundle b) { encountered=b.getInt("elemental_types_seen"); }
    public static boolean encountered(ElementalCache.Kind kind) { return (encountered&(1<<kind.ordinal()))!=0; }
    public static int plannedDepth(long seed,int region,ElementalCache.Kind kind) {
        if(region<0||region>4)return -1;
        Random.pushGenerator(seed+0x454C454D454E54L+region*7919L+kind.ordinal()*104729L);
        try {
            float chance=Math.min(100,BalanceTuning.get(BalanceTuning.CACHE_BASE_CHANCES[kind.ordinal()])
                    *(region+1)*BalanceTuning.multiplier(BalanceTuning.Key.CACHE_CHANCE));
            if(Random.Float()*100>=chance)return -1;
            return region==0?2+Random.Int(3):region*5+1+Random.Int(4);
        } finally { Random.popGenerator(); }
    }
    public static void addRooms(ArrayList<com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room> rooms) {
        if(Dungeon.branch!=0||Dungeon.depth<2||Dungeon.depth>24||Dungeon.depth%5==0)return;
        for(ElementalCache.Kind kind:ElementalCache.Kind.values())
            if(!encountered(kind)&&plannedDepth(Dungeon.seed,(Dungeon.depth-1)/5,kind)==Dungeon.depth)rooms.add(new SecretElementalRoom(kind));
    }
    public static void record(Level level) {
        if(Dungeon.branch==0)for(ElementalCache cache:level.elementalCaches)encountered|=1<<cache.kind.ordinal();
    }
    @Override public void storeInBundle(Bundle b) { super.storeInBundle(b);b.put("element",selectedKind); }
    @Override public void restoreFromBundle(Bundle b) { super.restoreFromBundle(b);selectedKind=b.getEnum("element",ElementalCache.Kind.class); }
    @Override public int minWidth() { return 6; }
    @Override public int minHeight() { return 6; }
    @Override public int maxWidth() { return 8; }
    @Override public int maxHeight() { return 8; }
    @Override public void paint(Level level) {
        Painter.fill(level,this,Terrain.WALL);
        Painter.fill(level,this,1,Terrain.EMPTY_SP);
        Door entry=entrance(); entry.set(Door.Type.HIDDEN);
        ElementalCache cache=new ElementalCache();
        cache.door=level.pointToCell(entry);
        Point clue=new Point(entry);
        if (entry.x==left) clue.x--; else if(entry.x==right) clue.x++; else if(entry.y==top) clue.y--; else clue.y++;
        cache.mechanism=level.pointToCell(clue);
        cache.kind=selectedKind;
        cache.keyCost=BalanceTuning.get(new BalanceTuning.Key[]{BalanceTuning.Key.CACHE_FIRE_KEY,BalanceTuning.Key.CACHE_WATER_KEY,BalanceTuning.Key.CACHE_LIGHTNING_KEY}[cache.kind.ordinal()]);
        level.elementalCaches.add(cache);
        ArrayList<Integer> spots=new ArrayList<>();
        for(int y=top+2;y<bottom;y++)for(int x=left+2;x<right;x++)spots.add(x+y*level.width());
        Random.shuffle(spots);
        float scale=BalanceTuning.multiplier(BalanceTuning.Key.CACHE_LOOT);
        int quality=cache.kind.ordinal();
        int region=Math.min(4,(Dungeon.depth-1)/5);
        if(scale<=0)return;
        int piles=Math.min(spots.size()-1,Math.round((2+quality)*scale));
        for(int i=0;i<piles;i++)level.drop(new Gold().random(),spots.remove(0));
        int chests=Math.min(spots.size(),Math.max(1,Math.round((1+quality+region/2)*scale)));
        for(int i=0;i<chests;i++) {
            Item reward;
            if(i==0 && (quality==2 || region+quality>=2)) {
                reward=Random.Int(2)==0?Generator.randomWeapon(region):Generator.randomArmor(region);
                // A guaranteed useful +1 roll, without importing next-region equipment tiers.
                int ceiling=Math.min(5,region+2);
                if(reward instanceof MeleeWeapon && ((MeleeWeapon)reward).tier>ceiling
                    || reward instanceof Armor && ((Armor)reward).tier>ceiling) {
                    reward=Generator.random(Generator.wepTiers[ceiling-1]);
                }
                reward.cursed=false; reward.level(Math.max((quality==2?1:0)+region/3,reward.level()));
                if(reward instanceof MeleeWeapon && ((MeleeWeapon)reward).enchantment!=null && ((MeleeWeapon)reward).enchantment.curse()) ((MeleeWeapon)reward).enchant(null);
                if(reward instanceof Armor && ((Armor)reward).glyph!=null && ((Armor)reward).glyph.curse()) ((Armor)reward).inscribe(null);
            } else if(quality==2 && i==1 && Random.Int(10)==0) reward=Generator.random(Generator.Category.ARTIFACT);
            else reward=Generator.random(Random.Int(2)==0?Generator.Category.POTION:Generator.Category.SCROLL);
            if(reward==null)reward=new Gold().random();
            Heap heap=level.drop(reward,spots.remove(0));
            if(heap!=null)heap.type=Heap.Type.CHEST;
        }
    }
}
