// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import java.util.*;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

/** Generated floors, real save/load and native recipes; no parallel implementation. */
final class WaywardScenario {
    private static void check(boolean ok,String reason){if(!ok)throw new AssertionError("Wayward Chart: "+reason);}
    private static int tier(Item item){return item instanceof Armor?((Armor)item).tier:item instanceof MeleeWeapon?((MeleeWeapon)item).tier:item instanceof MissileWeapon?((MissileWeapon)item).tier:0;}
    static void run() throws Exception {
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();BalanceTuning.reset();
        WaywardChart chart=new WaywardChart();chart.identify();chart.collect();
        check(WaywardJourney.floorEntered(1),"entry recorded without carrying");
        BalanceTuning.set(CHART_CHANCE_0,100);WaywardJourney.arrive();
        check(WaywardJourney.caches().isEmpty(),"mid-floor acquisition cannot reroll entry");
        ArrayList<Item> ingredients=new ArrayList<>();ingredients.add(chart);
        Trinket.UpgradeTrinket recipe=new Trinket.UpgradeTrinket();
        for(int rank=0;rank<3;rank++){chart.level(rank);check(recipe.cost(ingredients)==10+5*rank,"native energy curve");}
        chart.level(0);
        for(int region=0;region<5;region++)for(int rank=0;rank<4;rank++){
            Dungeon.depth=1+region*5;
            ArrayList<Item> loot=WaywardJourney.rewards(rank,false);
            check(loot.size()==rank+3 && loot.get(0) instanceof Gold,"rank reward count");
            int gearTier=tier(loot.get(1));
            if(gearTier>0){
                java.lang.reflect.Field odds=Generator.class.getDeclaredField("floorSetTierProbs");odds.setAccessible(true);
                check(((float[][])odds.get(null))[region][gearTier-1]>0,"native depth tier restriction");
            }
            check(loot.get(2) instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion
                    || loot.get(2) instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll,"useful consumable");
            for(Item item:loot)check(!item.unique && !(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.keys.Key),"no unique quest/class rewards");
        }
        Dungeon.depth=1;
        Arrays.fill(Dungeon.level.visited,true);WaywardJourney.captureRooms();
        int savedMemories=WaywardJourney.memories().size();check(savedMemories>=2,"explored ordinary rooms captured");
        Level first=Dungeon.level;Dungeon.saveAll();
        Dungeon.depth=2;Level second=Dungeon.newLevel();Dungeon.switchLevel(second,-1);
        check(WaywardJourney.caches().size()==1 && WaywardJourney.regionRewarded(0),"guaranteed first entry, regional cap");
        WaywardJourney.Cache c=WaywardJourney.caches().get(0);Heap heap=second.heaps.get(c.pos);
        check(heap!=null && heap.waywardCache==c.id && c.total==heap.size(),"actual marked loot heap");
        PathFinder.buildDistanceMap(second.entrance(),second.passable);check(PathFinder.distance[c.pos]!=Integer.MAX_VALUE,"reachable placement");
        check(second.traps.get(c.pos)==null && second.findMob(c.pos)==null && second.getTransition(c.pos)==null,"safe placement");
        boolean[] visited=second.visited.clone(),mapped=second.mapped.clone(),fov=second.heroFOV.clone();int[] map=second.map.clone();
        check(!c.claimed,"discovery has no memory cost");
        WaywardJourney.collected(heap);heap.pickUp();
        check(c.claimed && c.state()==1,"first actual collection makes partially collected state");
        long losses=WaywardJourney.memories().stream().filter(m->m.forgotten && m.depth==1).count();check(losses==1,"memory loss reaches prior explored floor");
        WaywardJourney.collected(heap);check(WaywardJourney.memories().stream().filter(m->m.forgotten).count()==1,"one memory cost per cache");
        check(Arrays.equals(visited,second.visited) && Arrays.equals(mapped,second.mapped) && Arrays.equals(fov,second.heroFOV)
                && Arrays.equals(map,second.map),"no mutation of terrain or knowledge");
        int total=WaywardJourney.caches().size();WaywardJourney.arrive();WaywardJourney.mapping();
        check(WaywardJourney.caches().size()==total,"revisit and mapping cannot exceed regional cap");
        while(!heap.isEmpty())heap.pickUp();check(c.state()==2 && Notes.getRecords(Notes.ChartRecord.class).isEmpty(),"exhausted mound removes reminder");
        Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
        check(WaywardJourney.caches().size()==1 && WaywardJourney.caches().get(0).claimed
                && WaywardJourney.caches().get(0).state()==2 && WaywardJourney.regionRewarded(0),"real save/load keeps claims and limits");
        check(WaywardJourney.memories().stream().anyMatch(m->m.depth==1 && m.forgotten),"earlier-floor memory survives save/load");
        WaywardJourney.Memory forgotten=WaywardJourney.memories().stream().filter(m->m.forgotten).findFirst().orElseThrow();
        int safeArrival=-1;
        for(int i=0;i<first.length();i++)if(first.passable[i] && !forgotten.contains(i)
                && first.distance(i,forgotten.cells[0])>4){safeArrival=i;break;}
        check(safeArrival>=0,"arrival outside the forgotten room");
        Dungeon.depth=1;Dungeon.switchLevel(first,safeArrival);
        check(forgotten.forgotten,"unvisited room remains forgotten on return to its floor");
        Arrays.fill(first.heroFOV,false);boolean[] veil=WaywardJourney.veilCells();check(count(veil)>0,"known memory has distinct veil");
        first.heroFOV[forgotten.cells[0]]=true;check(!WaywardJourney.veilCells()[forgotten.cells[0]],"visible cell never veiled");
        int protectedCell=forgotten.cells[1];Level.set(protectedCell,Terrain.ENTRANCE);check(!WaywardJourney.veilCells()[protectedCell],"stairs protected");
        Dungeon.hero.pos=forgotten.cells[2];WaywardJourney.observe();check(!forgotten.forgotten,"room reentry restores memory");
        // Two simultaneous losses survive; no global/region cap or automatic replacement.
        List<WaywardJourney.Memory> ms=WaywardJourney.memories();ms.get(0).forgotten=true;ms.get(1).forgotten=true;
        Bundle state=new Bundle();WaywardJourney.store(state);WaywardJourney.restore(state);
        check(WaywardJourney.memories().stream().filter(m->m.forgotten).count()==2,"accumulated memory losses persist");
        WaywardJourney.mapping();check(WaywardJourney.memories().stream().noneMatch(m->m.forgotten),"mapping restores all losses on its floor");
        // A paid mapping succeeds even when the one entry roll failed, only once.
        Dungeon.init();Playtest.enable();BalanceTuning.reset();BalanceTuning.set(CHART_CHANCE_0,0);
        chart=new WaywardChart();chart.collect();Dungeon.switchLevel(Dungeon.newLevel(),-1);
        check(WaywardJourney.caches().isEmpty(),"zero entry odds");
        chart.detachAll(Dungeon.hero.belongings.backpack);WaywardJourney.arrive();check(WaywardJourney.caches().isEmpty(),"dropping never rerolls");chart.collect();
        WaywardJourney.mapping();check(WaywardJourney.caches().size()==1,"mapping paid discovery");
        WaywardJourney.mapping();check(WaywardJourney.caches().size()==1,"mapping paid discovery once");
        // Boss/sidequest exclusion; the separate pocket requires actual dragon victory.
        Dungeon.depth=5;check(!WaywardJourney.discover(true),"boss excluded");Dungeon.branch=1;Dungeon.depth=11;check(!WaywardJourney.discover(true),"sidequest excluded");
        Dungeon.branch=DragonExpedition.BRANCH;Dungeon.depth=DragonExpedition.HOARD;
        DragonExpedition.dragonSlain=false;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        check(!WaywardJourney.discoverHoard(),"living dragon gates bonus");
        DragonExpedition.dragonSlain=true;((DragonHoardLevel)Dungeon.level).unlockHoard();
        check(WaywardJourney.caches().size()==2 && WaywardJourney.caches().get(1).hoard,"independent hoard bonus");
        check(!WaywardJourney.discoverHoard(),"hoard bonus once");
        check(!WaywardJourney.regionRewarded(3),"hoard does not consume normal City promise");
        Dungeon.saveAll();Dungeon.loadGame(99);Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
        check(!WaywardJourney.discoverHoard() && WaywardJourney.caches().size()==2,"hoard bonus save/load");
        check(chart.info().contains("Magic Mapping") && !chart.info().contains(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.NO_TEXT_FOUND),"localized player hints");
        // Real generated placements in every region, including an actual scroll read.
        Dungeon.init();Playtest.enable();BalanceTuning.reset();
        BalanceTuning.set(CHART_CHANCE_0,0);chart=new WaywardChart();chart.collect();
        Dungeon.hero.sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
        for(int region=0;region<5;region++){
            Dungeon.depth=1+region*5;Dungeon.branch=0;Dungeon.switchLevel(Dungeon.newLevel(),-1);
            if(region==0){
                ScrollOfMagicMapping scroll=new ScrollOfMagicMapping();scroll.identify();scroll.collect();
                // The headless backend has no CellSelector; invoke the real read implementation.
                Arrays.fill(Dungeon.level.heroFOV,false);Dungeon.hero.sprite.visible=false;
                java.lang.reflect.Field user=Item.class.getDeclaredField("curUser");user.setAccessible(true);user.set(null,Dungeon.hero);
                scroll.doRead();
            }else WaywardJourney.mapping();
            check(WaywardJourney.regionRewarded(region),"generated regional cache "+region);
            WaywardJourney.Cache regional=WaywardJourney.caches().get(region);
            check(regional.depth==Dungeon.depth && Dungeon.level.heaps.get(regional.pos)!=null,"regional physical rewards "+region);
            PathFinder.buildDistanceMap(Dungeon.hero.pos,Dungeon.level.passable);
            check(PathFinder.distance[regional.pos]!=Integer.MAX_VALUE,"regional reachable reward "+region);
            WaywardJourney.arrive();WaywardJourney.mapping();
            check(WaywardJourney.caches().size()==region+1,"regional repeat cap "+region);
        }
        Dungeon.depth=5;Dungeon.switchLevel(Dungeon.newLevel(),-1);Dungeon.level.locked=false;
        Arrays.fill(Dungeon.level.visited,true);WaywardJourney.captureRooms();
        check(WaywardJourney.memories().stream().anyMatch(m->m.depth==5 && m.cells.length>=6),"cleared boss floor memory is eligible");
        Bundle old=new Bundle();WaywardJourney.restore(old);WaywardJourney.migrateVisited(Arrays.asList(1,6,1002));
        check(WaywardJourney.floorEntered(1) && WaywardJourney.floorEntered(6) && !WaywardJourney.floorEntered(2),"old main-floor migration excludes branches");
        BalanceTuning.reset();Playtest.reset();
        System.out.println("WAYWARD PASS: ranks, recipes, safe/depth-correct rewards, first entry, regional and hoard caps, paid mapping, claim cost, prior-floor/accumulated memory, protected visibility, actual save/load; failures=0");
    }
    private static int count(boolean[] cells){int n=0;for(boolean cell:cells)if(cell)n++;return n;}
}
