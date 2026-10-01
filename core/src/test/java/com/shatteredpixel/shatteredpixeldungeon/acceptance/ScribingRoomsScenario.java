// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem;
import com.watabou.utils.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Random;

/** Real gameplay regressions for Spellguard, scroll scribing and elemental caches. */
final class ScribingRoomsScenario {
    static void elementalRooms() throws Exception {
        BalanceTuning.reset();Dungeon.init();
        int[] distribution=new int[4];int total=0;
        for(int seed=0;seed<10000;seed++) {
            int count=0;
            for(ElementalCache.Kind kind:ElementalCache.Kind.values())for(int region=0;region<5;region++) {
                int depth=SecretElementalRoom.plannedDepth(seed,region,kind);
                if(depth!=-1){check(depth>1&&depth<25&&depth%5!=0&&((depth-1)/5)==region,"eligible scheduled depth");count++;break;}
            }
            distribution[count]++;total+=count;
        }
        check(total>15000&&total<18500&&distribution[0]>400&&distribution[3]>900,"independent once-per-type frequency");
        System.out.println("TEST 64 FREQUENCY: "+total+" rooms/10000 runs; 0/1/2/3 distribution="+java.util.Arrays.toString(distribution));
        int generated=0;
        for(int region=0;region<5;region++)for(ElementalCache.Kind kind:ElementalCache.Kind.values()) {
            GamesInProgress.selectedClass=HeroClass.ENCHANTER;Dungeon.init();Playtest.enable();BalanceTuning.reset();
            BalanceTuning.set(BalanceTuning.Key.CACHE_CHANCE,100);
            BalanceTuning.set(BalanceTuning.CACHE_BASE_CHANCES[kind.ordinal()],100);
            for(BalanceTuning.Key weight:BalanceTuning.CACHE_BASE_CHANCES)if(weight!=BalanceTuning.CACHE_BASE_CHANCES[kind.ordinal()])BalanceTuning.set(weight,0);
            Dungeon.seed=64000+region*10+kind.ordinal();
            Dungeon.depth=SecretElementalRoom.plannedDepth(Dungeon.seed,region,kind);
            Level level=Dungeon.newLevel();Dungeon.switchLevel(level,-1);
            check(level.elementalCaches.size()==1,"one generated elemental room in region "+region);
            ElementalCache cache=level.elementalCaches.get(0);generated++;
            check(cache.kind==kind&&cache.keyCost==4+kind.ordinal(),"independent variant and seal costs");
            check(level.map[cache.door]==Terrain.SECRET_DOOR&&!level.passable[cache.door]&&level.passable[cache.mechanism],"hidden sealed door and accessible clue");
            check(level.customTiles.stream().anyMatch(t->t instanceof ElementalCache.MechanismTile&&t.tileX+t.tileY*level.width()==cache.mechanism),"painted clue placed after region decoration");
            check(!SecretRoom.createRoom().getClass().equals(SecretElementalRoom.class),"ordinary secret pool is independent");
            Bundle queue=new Bundle();SecretRoom.storeRoomsInBundle(queue);SecretRoom.initForRun();SecretRoom.restoreRoomsFromBundle(queue);
            check(!(SecretRoom.createRoom() instanceof SecretElementalRoom),"ordinary secret queue survives save/load");
            check(SecretElementalRoom.encountered(kind),"type encounter survives save/load");
            int oldDepth=Dungeon.depth;
            for(int depth=2;depth<=24;depth++) {Dungeon.depth=depth;java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room> added=new java.util.ArrayList<>();SecretElementalRoom.addRooms(added);check(added.isEmpty(),"type never repeats later in this run");}
            Dungeon.depth=oldDepth;
            // Seal blocks no required route. Normal hidden doors can be discovered en route.
            boolean[] seen=new boolean[level.length()];java.util.ArrayDeque<Integer> todo=new java.util.ArrayDeque<>();
            todo.add(level.entrance());seen[level.entrance()]=true;
            while(!todo.isEmpty()) { int current=todo.remove(); for(int d:com.watabou.utils.PathFinder.NEIGHBOURS8) {
                int n=current+d;
                if(level.insideMap(n)&&!seen[n]&&n!=cache.door&&(level.passable[n]||level.map[n]==Terrain.SECRET_DOOR)){seen[n]=true;todo.add(n);}
                }
            }
            check(seen[level.exit()]&&seen[cache.mechanism],"exit and visible clue reachable without opening cache");
            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room room=((RegularLevel)level).rooms().stream().filter(r->r instanceof SecretElementalRoom).findFirst().get();
            int chests=0,gold=0;boolean goodGear=false;
            for(Heap heap:level.heaps.valueList())if(room.inside(level.cellToPoint(heap.pos))) {
                if(heap.type==Heap.Type.CHEST)chests++;
                for(Item item:heap.items){if(item instanceof Gold)gold++;
                    if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon || item instanceof Armor) {
                        int tier=item instanceof Armor?((Armor)item).tier:((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon)item).tier;
                        if(item.level()>=1&&!item.cursed&&tier<=Math.min(5,region+2))goodGear=true;
                    }
                }
            }
            check(chests==1+kind.ordinal()+region/2&&gold==2+kind.ordinal(),"scaled finite chests and gold");
            check(kind!=ElementalCache.Kind.LIGHTNING||goodGear,"lightning guaranteed depth-appropriate improved equipment");
            Bundle saved=new Bundle();saved.put("level",level);Level restored=(Level)saved.get("level");
            ElementalCache copy=restored.elementalCaches.get(0);
            check(copy.door==cache.door&&copy.mechanism==cache.mechanism&&copy.kind==kind&&!copy.opened&&copy.keyCost==cache.keyCost,"closed state persists");
            check(ElementalCache.searchChance(level,cache.door,1,false)==.5f&&ElementalCache.searchChance(level,cache.door,.1f,false)==.05f
                    &&ElementalCache.searchChance(level,cache.door,1,true)==1,"manual/passive search penalty and foresight exemption");
            level.discover(cache.door);check(level.map[cache.door]==Terrain.LOCKED_DOOR&&!cache.opened&&!level.passable[cache.door],"discovery reveals without opening");
            level.destroy(cache.door);check(level.map[cache.door]==Terrain.LOCKED_DOOR,"terrain destruction cannot bypass seal");
            for(ElementalCache.Kind wrong:ElementalCache.Kind.values())if(wrong!=kind)check(!ElementalCache.activate(level,cache.mechanism,wrong)&&!cache.opened,"wrong element rejected");
            if(kind==ElementalCache.Kind.FIRE)new Fire().seed(level,cache.mechanism,2);
            else if(kind==ElementalCache.Kind.WATER)check(level.setCellToWater(true,cache.mechanism),"real water creation");
            else new Electricity().seed(level,cache.mechanism,2);
            check(cache.opened&&level.map[cache.door]==Terrain.DOOR&&level.passable[cache.door],"matching gameplay effect opens");
            saved=new Bundle();saved.put("level",level);restored=(Level)saved.get("level");
            check(restored.elementalCaches.get(0).opened&&restored.map[cache.door]==Terrain.DOOR,"opened seal and contents survive load");
            int heaps=level.heaps.size;check(!ElementalCache.activate(level,cache.mechanism,kind)&&heaps==level.heaps.size,"repeat activation yields no new loot");
        }
        Dungeon.init();Playtest.enable();BalanceTuning.reset();
        for(BalanceTuning.Key key:BalanceTuning.CACHE_BASE_CHANCES)BalanceTuning.set(key,100);
        long sharedSeed=0;
        while(sharedSeed<10000) {
            int d=SecretElementalRoom.plannedDepth(sharedSeed,0,ElementalCache.Kind.FIRE);
            if(d==SecretElementalRoom.plannedDepth(sharedSeed,0,ElementalCache.Kind.WATER)&&d==SecretElementalRoom.plannedDepth(sharedSeed,0,ElementalCache.Kind.LIGHTNING))break;
            sharedSeed++;
        }
        check(sharedSeed<10000,"shared three-type floor seed found");Dungeon.seed=sharedSeed;Dungeon.depth=SecretElementalRoom.plannedDepth(sharedSeed,0,ElementalCache.Kind.FIRE);
        Level shared=Dungeon.newLevel();Dungeon.switchLevel(shared,-1);
        check(shared.elementalCaches.size()==3,"all three optional leaves fit on one floor");
        java.util.Set<Integer> mechanisms=new java.util.HashSet<>();
        for(ElementalCache c:shared.elementalCaches) {check(mechanisms.add(c.mechanism),"clues do not overlap");check(ElementalCache.activate(shared,c.mechanism,c.kind),"every simultaneous seal responds");}
        BalanceTuning.reset();Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();SmokeRun.clearArena();
        Hero hero=Dungeon.hero;Level level=Dungeon.level;int center=hero.pos;
        ElementalCache cache=new ElementalCache();cache.mechanism=center+1;cache.door=center+2;cache.kind=ElementalCache.Kind.WATER;cache.keyCost=5;level.elementalCaches.add(cache);
        Level.set(cache.door,Terrain.SECRET_DOOR);Waterskin skin=hero.belongings.getItem(Waterskin.class);skin.empty();float time=hero.cooldown();
        check(!ElementalCache.pour(hero,skin,cache.mechanism)&&hero.cooldown()==time,"partial skin spends nothing");
        skin.fill();check(!ElementalCache.pour(hero,skin,cache.mechanism+level.width()*2)&&skin.isFull(),"wrong or remote target retains full skin");
        check(ElementalCache.pour(hero,skin,cache.mechanism)&&!skin.isFull()&&hero.belongings.contains(skin)&&hero.cooldown()==time+1,"full skin consumes water and one turn only");
        SkeletonKey key=new SkeletonKey();hero.belongings.artifact=key;key.activate(hero);key.playtestLevel(10);
        java.lang.reflect.Field charge=Artifact.class.getDeclaredField("charge");charge.setAccessible(true);
        hero.pos=cache.mechanism;
        for(int cost=4;cost<=6;cost++) {
            cache.opened=false;cache.keyCost=cost;Level.set(cache.door,Terrain.SECRET_DOOR);level.mapped[cache.door]=true;
            key.playtestRecharge();check(!key.openElemental(hero,cache.door),"key cannot find concealed door");level.discover(cache.door);
            charge.setInt(key,cost-1);time=hero.cooldown();check(!key.openElemental(hero,cache.door)&&charge.getInt(key)==cost-1&&hero.cooldown()==time,"insufficient key charges atomic");
            charge.setInt(key,cost);key.cursed=true;check(!key.openElemental(hero,cache.door),"cursed key rejected");key.cursed=false;
            Buff.affect(hero,MagicImmune.class);check(!key.openElemental(hero,cache.door),"suppressed key rejected");Buff.detach(hero,MagicImmune.class);
            check(key.openElemental(hero,cache.door)&&charge.getInt(key)==0&&hero.cooldown()==time+1,"exact seal cost "+cost);
            check(hero.buff(SkeletonKey.KeyReplacementTracker.class)==null,"seal cannot consume or refund ordinary iron keys");
        }
        cache.opened=false;Level.set(cache.door,Terrain.SECRET_DOOR);level.cleanWalls();
        // Full mapping, prismatic and Talisman callbacks need real particle emitters; exercised by the native fixture.
        cache.kind=ElementalCache.Kind.LIGHTNING;
        new WandOfLightning().onZap(new Ballistica(center,cache.mechanism,Ballistica.STOP_TARGET));check(cache.opened,"direct lightning wand on empty conductor");
        BalanceTuning.reset();
        System.out.println("TEST 64 PASS: 15 generated elemental rooms/all five regions; route safety, once-per-type caps, save/load, discovery/search penalty, real elements, water cost, Skeleton Key 4/5/6, finite depth-scaled loot");
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError("Scribing/rooms: " + message);
    }

    static void scribing() {
        GamesInProgress.selectedClass=HeroClass.ENCHANTER;
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);SmokeRun.clearArena();
        Hero hero=Dungeon.hero;
        SigilBrush brush=hero.belongings.getItem(SigilBrush.class);
        BlankParchment paper=new BlankParchment();paper.quantity(10);paper.collect();
        Scroll.initLabels();
        com.shatteredpixel.shatteredpixeldungeon.journal.Catalog.setSeen(ScrollOfIdentify.class);
        check(BlankParchment.recipes().isEmpty(),"historical journal knowledge is not a recipe");
        Dungeon.energy=100;int charges=brush.charges();float time=hero.cooldown();
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"unknown recipe rejected");
        check(Dungeon.energy==100&&paper.quantity()==10&&brush.charges()==charges&&hero.cooldown()==time,"invalid selection spends nothing");
        new ScrollOfIdentify().identify();new ScrollOfTransmutation().identify();new ScrollOfUpgrade().identify();new ScrollOfEnchantment().identify();
        check(BlankParchment.recipes().contains(ScrollOfIdentify.class)&&BlankParchment.recipes().contains(ScrollOfTransmutation.class)
                &&!BlankParchment.recipes().contains(ScrollOfUpgrade.class)&&!BlankParchment.recipes().contains(ScrollOfEnchantment.class),"regular pool and forbidden upgrade/exotic recipes");
        check(BlankParchment.scribe(hero,ScrollOfIdentify.class),"known regular scroll written");
        check(Dungeon.energy==88&&paper.quantity()==9&&brush.charges()==charges-1&&hero.cooldown()==time+3,"one parchment, charge, 12 energy and exactly three turns");
        check(hero.belongings.getItem(ScrollOfIdentify.class).isIdentified(),"written output is known");
        time=hero.cooldown();check(BlankParchment.scribe(hero,ScrollOfTransmutation.class)&&Dungeon.energy==68&&hero.cooldown()==time+3,"Transmutation costs 20 energy and three turns");
        Dungeon.energy=11;time=hero.cooldown();charges=brush.charges();
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class)&&paper.quantity()==8&&brush.charges()==charges&&hero.cooldown()==time,"insufficient energy is atomic");
        Dungeon.energy=100;brush.gainCharge(-100);
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"empty Brush rejected");brush.gainCharge(10);
        Buff.affect(hero,MagicImmune.class);check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"suppressed Brush rejected");Buff.detach(hero,MagicImmune.class);
        hero.belongings.artifact=null;hero.belongings.backpack.items.add(brush);
        check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"unequipped Brush requires Wandering Brush");
        hero.talents.get(1).put(Talent.DUAL_INSCRIPTION,1);
        check(BlankParchment.scribe(hero,ScrollOfIdentify.class),"Wandering Brush permits carried Scribe");
        hero.heroClass=HeroClass.WARRIOR;check(!BlankParchment.scribe(hero,ScrollOfIdentify.class),"other classes cannot write");hero.heroClass=HeroClass.ENCHANTER;
        // Actual shared recycling path covers pot, Toolkit and Alchemize callers.
        ScrollOfIdentify scroll=new ScrollOfIdentify();scroll.quantity(4);hero.belongings.backpack.items.add(scroll);
        int before=paper.quantity(),energy=Dungeon.energy;
        WndEnergizeItem.energizeOne(scroll);
        check(scroll.quantity()==3&&paper.quantity()==before+1&&Dungeon.energy==energy+6,"recycle one returns one parchment and normal energy");
        WndEnergizeItem.energizeAll(scroll);
        check(!hero.belongings.contains(scroll)&&paper.quantity()==before+4&&Dungeon.energy==energy+24,"recycle stack returns one sheet per scroll");
        WndEnergizeItem.energizeAll(scroll);
        check(paper.quantity()==before+4&&Dungeon.energy==energy+24,"stale recycling selection cannot duplicate energy or parchment");
        ScrollOfEnchantment exotic=new ScrollOfEnchantment();hero.belongings.backpack.items.add(exotic);
        WndEnergizeItem.energizeAll(exotic);check(paper.quantity()==before+5,"exotic recycling also returns parchment");
        com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder holder=new com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder();
        check(holder.canHold(paper),"parchment fits scroll holder");
        check(((BlankParchment)paper.duplicate()).quantity()==paper.quantity(),"paper stack save/load");
        int opportunities=0;
        for(int seed=0;seed<100;seed++){
            int count=0;
            for(int depth=1;depth<=26;depth++){
                boolean spawn=BlankParchment.spawnsOn(depth,0,HeroClass.ENCHANTER,seed);
                check(!spawn||depth>=2&&depth<=24&&depth%5!=0,"no boss/endgame parchment");
                check(!BlankParchment.spawnsOn(depth,1,HeroClass.ENCHANTER,seed)&&!BlankParchment.spawnsOn(depth,0,HeroClass.WARRIOR,seed),"no other-class or branch parchment");
                if(spawn)count++;
            }
            check(count>=1&&count<=5&&BlankParchment.spawnsOn(2,0,HeroClass.ENCHANTER,seed),"early sheet and regional bounds");opportunities+=count;
        }
        check(opportunities>=340&&opportunities<=450,"seeded parchment average remains modest");
        System.out.println("TEST 63 PASS: Scribe knowledge, exclusions, atomic costs, three turns, carried Brush, recycling one/stack/exotic/stale, bag/save and seeded parchment opportunities="+opportunities+"/100 runs");
    }

    static void spellguard() {
        GamesInProgress.selectedClass = HeroClass.ENCHANTER;
        Dungeon.init(); Dungeon.switchLevel(Dungeon.newLevel(), -1); SmokeRun.clearArena();
        Hero hero = Dungeon.hero;
        for (Buff buff : hero.buffs()) buff.detach();
        ClothArmor armor = new ClothArmor(); hero.belongings.armor = armor;
        hero.HT = 10000;
        for (int rank = 0; rank <= 2; rank++) {
            hero.talents.get(1).put(Talent.SPELLGUARD, rank);
            armor.inscribed = new Obfuscation(); armor.inscriptionTurns = 10;
            hero.HP = 10000; hero.damage(100, new Warlock.DarkBolt());
            check(hero.HP == 9900 + 10*rank, "actual magical damage rank " + rank);
            hero.HP = 10000; hero.damage(100, new Rat());
            check(hero.HP == 9900, "physical damage unchanged");
        }
        armor.inscribe(new Obfuscation()); armor.inscribed = null;
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "permanent glyph alone does not qualify");
        armor.runeEtching = new RuneEtching();
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "rune alone does not qualify");
        armor.inscribed = new Obfuscation(); armor.inscriptionTurns = 0;
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "expired inscription does not qualify");
        armor.inscriptionTurns = 1;
        EnchanterMagic magic = Buff.affect(hero, EnchanterMagic.class); magic.act();
        check(armor.inscribed == null && armor.inscriptionTurns == 0, "real expiration clears inscription");
        armor.inscribed = new Obfuscation(); armor.inscriptionTurns = 10;
        hero.belongings.armor = null;
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "unworn inscription does not qualify");
        hero.belongings.armor = armor; Buff.affect(hero, MagicImmune.class);
        check(EnchanterMagic.spellguardMultiplier(hero, new Warlock.DarkBolt()) == 1, "magic suppression");
        Buff.detach(hero, MagicImmune.class);
        Bundle legacy = new Bundle(), tier = new Bundle(); tier.put("OVERLOAD", 2); legacy.put("talents_tier_2", tier);
        Talent.restoreTalentsFromBundle(legacy, hero);
        check(hero.pointsInTalent(Talent.SPELLGUARD) == 2, "old Overload ranks migrate");
        Bundle saved = new Bundle(); Talent.storeTalentsInBundle(saved, hero); Talent.restoreTalentsFromBundle(saved, hero);
        check(hero.pointsInTalent(Talent.SPELLGUARD) == 2, "new Spellguard ranks persist");
        System.out.println("TEST 62 PASS: Spellguard damage ranks, physical damage, expiry, equipment, glyph/rune exclusion, suppression and old/new save ranks");
    }
}
