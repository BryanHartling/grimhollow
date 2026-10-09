// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.BalanceTuning;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CavernSpinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;
import com.watabou.utils.PathFinder;
import com.watabou.utils.BArray;
import java.util.ArrayList;

public class DragonCavernLevel extends ExpeditionLevel {
    public static final int SIZE = 43, CENTER = 21 * SIZE + 21;
    private CavernSpawner cavernSpawner;
    { viewDistance = BalanceTuning.get(CAVERN_SIGHT); }
    @Override protected boolean build() {
        setSize(SIZE, SIZE);
        double a=Random.Float()*Math.PI*2, b=Random.Float()*Math.PI*2, c=Random.Float()*Math.PI*2;
        // A star-shaped contour stays open and connected; varying lobes make natural rock edges.
        for (int y = 2; y < SIZE - 2; y++) for (int x = 2; x < SIZE - 2; x++) {
            double dx=x-21, dy=y-21, angle=Math.atan2(dy,dx);
            double radius=18+1.5*Math.sin(3*angle+a)+Math.sin(5*angle+b)+.7*Math.sin(9*angle+c);
            if(Math.hypot(dx,dy)<=radius)map[y*SIZE+x]=Terrain.EMPTY;
        }
        int pillars = 0;
        for (int attempts = 0; pillars < 30 && attempts < 2000; attempts++) {
            int x = Random.IntRange(3, SIZE - 5), y = Random.IntRange(3, SIZE - 5);
            if (Math.abs(x - 21) <= 6 && Math.abs(y - 21) <= 6) continue;
            boolean clear = true;
            for (int dy = -1; dy <= 2; dy++) for (int dx = -1; dx <= 2; dx++)
                if (map[(y + dy) * SIZE + x + dx] != Terrain.EMPTY) clear = false;
            if (!clear) continue;
            for (int dy = 0; dy < 2; dy++) for (int dx = 0; dx < 2; dx++) map[(y + dy) * SIZE + x + dx] = Terrain.WALL;
            pillars++;
        }
        map[CENTER] = Terrain.ENTRANCE;
        transitions.add(new LevelTransition(this, CENTER, LevelTransition.Type.REGULAR_EXIT,
                DragonExpedition.CHASM, DragonExpedition.BRANCH, LevelTransition.Type.REGULAR_ENTRANCE));
        return true;
    }
    @Override protected void createMobs() {
        if (DragonExpedition.spiderSlain) return;
        ArrayList<Integer> lairs=new ArrayList<>();
        for(int cell=0;cell<length();cell++)if(passable[cell]&&openSpace[cell]&&distance(cell,CENTER)>=14)lairs.add(cell);
        Broodmother boss = new Broodmother(); boss.pos = Random.element(lairs); mobs.add(boss);
        ArrayList<Integer> approaches=new ArrayList<>();
        for(int cell=0;cell<length();cell++)if(passable[cell]&&distance(cell,CENTER)>=5
                &&distance(cell,CENTER)<=10&&distance(cell,boss.pos)>=7)approaches.add(cell);
        for (int i = 0; i < Math.min(BalanceTuning.get(SPIDERS_INITIAL), BalanceTuning.get(SPIDERS_CAP)); i++) {
            CavernSpinner spider = new CavernSpinner();
            spider.pos = approaches.isEmpty()?randomRespawnCell(spider):approaches.remove(Random.Int(approaches.size()));
            if (spider.pos >= 0) mobs.add(spider);
        }
    }
    @Override protected void createItems() {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int i = 0; i < length(); i++) if (passable[i] && distance(i, CENTER) > 4 && findMob(i) == null) cells.add(i);
        int food = BalanceTuning.get(CAVERN_RATIONS), torches = BalanceTuning.get(CAVERN_TORCHES);
        int total=Math.max(BalanceTuning.get(CAVERN_REMAINS),food+torches);
        // Apportion finite remains by weight, so a small random streak cannot turn the cavern into a seed cache.
        float[] quotas=new float[BalanceTuning.CAVERN_LOOT_WEIGHTS.length], fractions=new float[quotas.length];
        int weight=0, assigned=0;
        for(BalanceTuning.Key key:BalanceTuning.CAVERN_LOOT_WEIGHTS)weight+=BalanceTuning.get(key);
        for(int j=0;j<quotas.length;j++) {
            float exact=(total-food-torches)*BalanceTuning.get(BalanceTuning.CAVERN_LOOT_WEIGHTS[j])/(float)weight;
            quotas[j]=(int)exact;fractions[j]=exact-quotas[j];assigned+=(int)quotas[j];
        }
        while(assigned++<total-food-torches) {
            int best=0;for(int j=1;j<fractions.length;j++)if(fractions[j]>fractions[best])best=j;
            quotas[best]++;fractions[best]=-1;
        }
        for (int i = 0; i < total && !cells.isEmpty(); i++) {
            int cell = cells.remove(Random.Int(cells.size()));
            Item item;
            if (i < food) item = new Food();
            else if (i < food + torches) item = new Torch();
            else {
                int category=Random.chances(quotas);quotas[category]--;
                int attempts=0;
                do {item=remainsItem(category,i-food-torches);}while(Challenges.isItemBlocked(item)&&++attempts<30);
                if(Challenges.isItemBlocked(item))item=new Gold(Random.IntRange(15,40));
            }
            drop(item, cell).type = i%2==0?Heap.Type.SKELETON:Heap.Type.REMAINS;
        }
    }
    private Item remainsItem(int category,int index) {
        Item item;
        if(category==0) {
            int maxTier=BalanceTuning.get(CAVERN_MAX_TIER);
            int tier=Random.Int(5)==0?1:Random.IntRange(Math.max(1,maxTier-1),maxTier);
            switch(index%3) {
                case 0:item=Generator.randomUsingDefaults(Generator.wepTiers[tier-1]);break;
                case 1:item=((Armor)Reflection.newInstance(Generator.Category.ARMOR.classes[tier-1])).random();break;
                default:item=Generator.randomUsingDefaults(Generator.misTiers[tier-1]);break;
            }
            item.level(BalanceTuning.get(CAVERN_UPGRADES));
        } else if(category==1) {
            item=Generator.randomUsingDefaults(Generator.Category.RING);item.level(BalanceTuning.get(CAVERN_UPGRADES));
        } else if(category==2)item=new Gold(Random.IntRange(15,40));
        else item=Generator.randomUsingDefaults(new Generator.Category[]{Generator.Category.SEED,
                Generator.Category.STONE,Generator.Category.POTION,Generator.Category.SCROLL}[Random.Int(4)]);
        return item;
    }
    @Override public boolean activateTransition(Hero hero, LevelTransition transition) {
        if (!DragonExpedition.spiderSlain) { GLog.w(Messages.get(this, "blocked")); return false; }
        return super.activateTransition(hero, transition);
    }
    @Override public int fallCell(boolean intoPit) {
        ArrayList<Integer> cells=new ArrayList<>();
        for(int cell=0;cell<length();cell++)if(passable[cell]&&distance(cell,CENTER)<=4&&cell!=CENTER&&findMob(cell)==null) {
            boolean safe=true;
            for(Mob mob:mobs)if(mob instanceof Broodmother&&distance(cell,mob.pos)<10)safe=false;
            if(safe)cells.add(cell);
        }
        // The central clearing also guarantees a safe landing on a previously visited cavern.
        if(!cells.isEmpty())return Random.element(cells);
        int best=-1, separation=-1;
        for(int cell=0;cell<length();cell++)if(passable[cell]&&findMob(cell)==null&&cell!=CENTER) {
            int nearest=length();for(Mob mob:mobs)if(mob instanceof Broodmother)nearest=Math.min(nearest,distance(cell,mob.pos));
            if(nearest>separation){best=cell;separation=nearest;}
        }
        return best;
    }
    public void arrive() {
        // Deliberately climbing down still alerts her. Falling does not bypass the outer spiders.
        if(Dungeon.hero.pos==CENTER&&Dungeon.hero.buff(Chasm.Falling.class)==null)
            for (Mob mob : mobs) if (mob instanceof Broodmother) ((Broodmother) mob).alertArrival();
    }
    @Override public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);map[CENTER]=Terrain.ENTRANCE;
        cavernSpawner = (CavernSpawner) bundle.get("cavern_spawner");
    }
    @Override public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle); bundle.put("cavern_spawner", cavernSpawner);
    }
    @Override public Actor addRespawner() {
        if (cavernSpawner == null) {
            cavernSpawner = new CavernSpawner();
            Actor.addDelayed(cavernSpawner, respawnCooldown());
        } else {
            // Reuse the saved remaining time. Visits and loads do not reroll the delay.
            Actor.add(cavernSpawner);
        }
        return cavernSpawner;
    }
    @Override public float respawnCooldown() {
        int interval = BalanceTuning.get(CAVERN_SPAWN_INTERVAL);
        return interval == 0 ? 80 : Random.IntRange(Math.max(1, interval * 3 / 4), Math.max(1, interval * 5 / 4));
    }
    public boolean replenishBrood() {
        if (BalanceTuning.get(CAVERN_SPAWN_INTERVAL) == 0 || !Dungeon.hero.isAlive()) return false;
        int spiders = 0;
        boolean motherAlive = false;
        for (Mob mob : mobs) if (mob.isAlive()) {
            if (mob instanceof Broodmother) motherAlive = true;
            if (mob instanceof CavernSpinner) spiders++;
        }
        Mob arrival;
        if (!motherAlive) {
            Broodmother mother = new Broodmother(); mother.setReplenished(); arrival = mother;
        } else {
            if (spiders >= BalanceTuning.get(SPIDERS_CAP)) return false;
            CavernSpinner spider = new CavernSpinner(); spider.setReplenished(); arrival = spider;
        }
        PathFinder.buildDistanceMap(Dungeon.hero.pos, BArray.or(passable, avoid, null));
        ArrayList<Integer> cells = new ArrayList<>();
        for (int cell = 0; cell < length(); cell++) {
            if (insideMap(cell) && passable[cell] && !pit[cell] && !heroFOV[cell]
                    && distance(cell, Dungeon.hero.pos) >= 8 && findMob(cell) == null
                    && Actor.findChar(cell) == null && cell != CENTER
                    && PathFinder.distance[cell] != Integer.MAX_VALUE
                    && (!(arrival instanceof Broodmother) || openSpace[cell] && distance(cell, CENTER) >= 14)) cells.add(cell);
        }
        if (cells.isEmpty()) return false;
        arrival.pos = Random.element(cells);
        // Set the patrol destination before GameScene creates the sprite. beckon()
        // can call notice(), which requires a sprite that is not attached yet.
        if (arrival instanceof Broodmother) ((Broodmother) arrival).patrolToward(Dungeon.hero.pos);
        else ((CavernSpinner) arrival).patrolToward(Dungeon.hero.pos);
        com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(arrival, 1f);
        return true;
    }
    public static class CavernSpawner extends Actor {
        { actPriority = BUFF_PRIO; }
        @Override protected boolean act() {
            if (!(Dungeon.level instanceof DragonCavernLevel)) { Actor.remove(this); return true; }
            DragonCavernLevel cavern = (DragonCavernLevel) Dungeon.level;
            cavern.replenishBrood();
            // Even blocked/capped attempts wait for another full, randomized interval.
            spend(cavern.respawnCooldown());
            return true;
        }
    }
}
