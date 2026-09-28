// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.BalanceTuning;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CavernSpinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class DragonCavernLevel extends ExpeditionLevel {
    public static final int SIZE = 43, CENTER = 21 * SIZE + 21;
    { viewDistance = BalanceTuning.get(CAVERN_SIGHT); }
    @Override protected boolean build() {
        setSize(SIZE, SIZE);
        for (int y = 1; y < SIZE - 1; y++) for (int x = 1; x < SIZE - 1; x++) map[y * SIZE + x] = Terrain.EMPTY;
        int pillars = 0;
        for (int attempts = 0; pillars < 30 && attempts < 600; attempts++) {
            int x = Random.IntRange(3, SIZE - 5), y = Random.IntRange(3, SIZE - 5);
            if (Math.abs(x - 21) <= 6 && Math.abs(y - 21) <= 6) continue;
            boolean clear = true;
            for (int dy = -2; dy <= 3; dy++) for (int dx = -2; dx <= 3; dx++)
                if (map[(y + dy) * SIZE + x + dx] != Terrain.EMPTY) clear = false;
            if (!clear) continue;
            for (int dy = 0; dy < 2; dy++) for (int dx = 0; dx < 2; dx++) map[(y + dy) * SIZE + x + dx] = Terrain.WALL;
            pillars++;
        }
        map[CENTER] = Terrain.EXIT;
        transitions.add(new LevelTransition(this, CENTER, LevelTransition.Type.REGULAR_EXIT,
                DragonExpedition.CHASM, DragonExpedition.BRANCH, LevelTransition.Type.REGULAR_ENTRANCE));
        return true;
    }
    @Override protected void createMobs() {
        if (DragonExpedition.spiderSlain) return;
        Broodmother boss = new Broodmother(); boss.pos = CENTER - 4 * SIZE; mobs.add(boss);
        for (int i = 0; i < Math.min(BalanceTuning.get(SPIDERS_INITIAL), BalanceTuning.get(SPIDERS_CAP)); i++) {
            CavernSpinner spider = new CavernSpinner(); spider.pos = randomRespawnCell(spider);
            if (spider.pos >= 0) mobs.add(spider);
        }
    }
    @Override protected void createItems() {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int i = 0; i < length(); i++) if (passable[i] && distance(i, CENTER) > 4 && findMob(i) == null) cells.add(i);
        int food = BalanceTuning.get(CAVERN_RATIONS), torches = BalanceTuning.get(CAVERN_TORCHES);
        for (int i = 0; i < 40 && !cells.isEmpty(); i++) {
            int cell = cells.remove(Random.Int(cells.size()));
            Item item;
            if (i < food) item = new Food();
            else if (i < food + torches) item = new Torch();
            else if (i % 3 == 0) item = new Gold(Random.IntRange(15, 40));
            else item = Generator.randomUsingDefaults(new Generator.Category[]{Generator.Category.SEED,
                    Generator.Category.STONE, Generator.Category.POTION, Generator.Category.SCROLL}[Random.Int(4)]);
            drop(item, cell).type = Heap.Type.SKELETON;
        }
    }
    @Override public boolean activateTransition(Hero hero, LevelTransition transition) {
        if (!DragonExpedition.spiderSlain) { GLog.w(Messages.get(this, "blocked")); return false; }
        return super.activateTransition(hero, transition);
    }
    @Override public int fallCell(boolean intoPit) { return randomRespawnCell(Dungeon.hero); }
    public void arrive() {
        if (DragonExpedition.spiderSlain) { clearBrood(); return; }
        for (Mob mob : mobs) if (mob instanceof Broodmother) ((Broodmother) mob).alertArrival();
    }
    public void clearBrood() {
        for (Mob mob : mobs.toArray(new Mob[0])) if (mob instanceof CavernSpinner) {
            mob.alignment = com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL;
            mob.destroy(); if (mob.sprite != null) mob.sprite.die();
        }
        Web web = (Web) blobs.get(Web.class);
        if (web != null) web.fullyClear();
    }
}
