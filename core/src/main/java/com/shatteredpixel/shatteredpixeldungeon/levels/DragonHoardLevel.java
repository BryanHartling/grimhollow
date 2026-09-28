// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.BalanceTuning;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class DragonHoardLevel extends ExpeditionLevel {
    @Override public String tilesTex() { return "environment/tiles_expedition_hoard.png"; }
    public static final int WIDTH = 25, HEIGHT = 23;
    public static final int ARRIVAL = 19 * WIDTH + 4, RETURN = 19 * WIDTH + 20, TREASURE = 6 * WIDTH + 12;
    public static final int VICTORY_ARRIVAL = TREASURE + 3 * WIDTH;
    { viewDistance = 8; }
    @Override protected boolean build() {
        setSize(WIDTH, HEIGHT);
        for (int y = 2; y < HEIGHT - 2; y++) for (int x = 2; x < WIDTH - 2; x++) map[y*WIDTH+x] = Terrain.EMPTY;
        for (int y = 4; y <= 8; y++) for (int x = 9; x <= 15; x++) map[y*WIDTH+x] = Terrain.PEDESTAL;
        map[ARRIVAL] = Terrain.ENTRANCE; map[RETURN] = Terrain.EXIT;
        transitions.add(new LevelTransition(this, ARRIVAL, LevelTransition.Type.REGULAR_ENTRANCE,
                DragonExpedition.CHASM, DragonExpedition.BRANCH, LevelTransition.Type.REGULAR_EXIT));
        transitions.add(new LevelTransition(this, RETURN, LevelTransition.Type.REGULAR_EXIT,
                DragonExpedition.hunterDepth, 0, LevelTransition.Type.REGULAR_ENTRANCE));
        customTiles.add(new com.shatteredpixel.shatteredpixeldungeon.tiles.ExpeditionHoardTiles());
        return true;
    }
    @Override protected void createItems() { unlockHoard(); }

    /** There are no real treasure items to burn, steal, pull or collect before victory. */
    public void unlockHoard() {
        if (!DragonExpedition.dragonSlain) return;
        Fire fire = (Fire) blobs.get(Fire.class); if (fire != null) fire.fullyClear();
        if (DragonExpedition.rewardsCreated) return;
        ArrayList<Item> rewards = new ArrayList<>();
        int gold = BalanceTuning.get(HOARD_GOLD);
        if (gold > 0) rewards.add(new Gold(Random.IntRange(Math.round(gold * .8f), Math.round(gold * 1.2f))));
        for (int i = 0; i < BalanceTuning.get(HOARD_EQUIPMENT); i++) {
            Item item;
            int attempts = 0;
            do {
                switch (Random.Int(4)) {
                    default: case 0: item = Generator.randomWeapon(4, true); break;
                    case 1: item = Generator.randomArmor(4); break;
                    case 2: item = Generator.randomUsingDefaults(Generator.Category.RING); break;
                    case 3: item = Generator.randomUsingDefaults(Generator.Category.WAND); break;
                }
            } while (Challenges.isItemBlocked(item) && ++attempts < 100);
            if (Challenges.isItemBlocked(item)) { rewards.add(new Gold(500)); continue; }
            int upgrade = BalanceTuning.get(HOARD_UPGRADES);
            item.level(Random.IntRange(upgrade, Math.min(10, upgrade + 1))); item.cursed = false;
            if (item instanceof Weapon) ((Weapon) item).enchant();
            if (item instanceof Armor) ((Armor) item).inscribe();
            rewards.add(item.identify());
        }
        if (BalanceTuning.roll(HOARD_ARTIFACT, 4, 1)) {
            Artifact artifact = (Artifact) Generator.randomArtifact();
            if (artifact != null && !Challenges.isItemBlocked(artifact)) {
                artifact.cursed = false; artifact.transferUpgrade(5); rewards.add(artifact.identify());
            }
        }
        if (BalanceTuning.roll(HOARD_TRINKET, 4, 1)) {
            Trinket trinket = bonusTrinket(); if (trinket != null) rewards.add(trinket);
        }
        for (int i = 0; i < rewards.size(); i++) drop(rewards.get(i), TREASURE + (i % 3) - 1 + (i / 3) * WIDTH);
        DragonExpedition.rewardsCreated = true;
    }

    public static Trinket bonusTrinket() {
        for (int i = 0; i < 100; i++) {
            Item item = Generator.random(Generator.Category.TRINKET);
            if (item instanceof Trinket && Dungeon.hero.belongings.getItem(item.getClass()) == null
                    && !Challenges.isItemBlocked(item)) return (Trinket) item;
        }
        return null;
    }
    @Override public boolean activateTransition(Hero hero, LevelTransition transition) {
        if (transition.type == LevelTransition.Type.REGULAR_EXIT) {
            // This is the one permitted retreat, even while the dragon is alive.
            DragonExpedition.travel(DragonExpedition.hunterDepth, 0, DragonExpedition.returnCell);
            return true;
        }
        return super.activateTransition(hero, transition);
    }
    @Override public String tileName(int tile) {
        return tile == Terrain.PEDESTAL ? Messages.get(this, DragonExpedition.dragonSlain ? "treasure_open_name" : "treasure_name") : super.tileName(tile);
    }
    @Override public String tileDesc(int tile) {
        return tile == Terrain.PEDESTAL ? Messages.get(this, DragonExpedition.dragonSlain ? "unsealed" : "sealed") : super.tileDesc(tile);
    }
}
