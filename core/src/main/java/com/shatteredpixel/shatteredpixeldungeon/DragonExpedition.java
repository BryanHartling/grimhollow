// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.ExpeditionMap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** One expedition per save, in a branch separate from the Imp's Vault. */
public final class DragonExpedition {
    private DragonExpedition() {}
    public static final int BRANCH = 2, CHASM = 17, CAVERN = 18, HOARD = 19;
    // Enable only after the connected encounter and its return route are complete.
    public static final boolean AVAILABLE = false;
    public static int hunterDepth, hunterPos = -1, returnCell = -1;
    public static java.util.ArrayList<Item> fallenItems = new java.util.ArrayList<>();
    public static ExpeditionDragon dragon;
    public static int dragonFloor = CHASM;
    public static boolean victoryPending;
    public static boolean accepted, entered, dragonSlain, spiderSlain, rewardsCreated;

    public static void reset() {
        Random.pushGenerator(Dungeon.seed ^ 0x445241474F4EL);
        hunterDepth = Random.IntRange(16, 19);
        Random.popGenerator();
        hunterPos = returnCell = -1;
        fallenItems.clear();
        dragon = null; dragonFloor = CHASM; victoryPending = false;
        accepted = entered = dragonSlain = spiderSlain = rewardsCreated = false;
    }

    public static void store(Bundle parent) {
        Bundle b = new Bundle();
        b.put("hunter_depth", hunterDepth); b.put("hunter_pos", hunterPos);
        b.put("return_cell", returnCell); b.put("accepted", accepted); b.put("entered", entered);
        b.put("dragon_slain", dragonSlain); b.put("spider_slain", spiderSlain);
        b.put("rewards_created", rewardsCreated);
        b.put("fallen_items", fallenItems);
        b.put("dragon", dragon); b.put("dragon_floor", dragonFloor); b.put("victory_pending", victoryPending);
        parent.put("dragon_expedition", b);
    }

    public static void restore(Bundle parent) {
        reset();
        if (!parent.contains("dragon_expedition")) return;
        Bundle b = parent.getBundle("dragon_expedition");
        hunterDepth = Math.max(16, Math.min(19, b.getInt("hunter_depth")));
        hunterPos = b.getInt("hunter_pos"); returnCell = b.getInt("return_cell");
        accepted = b.getBoolean("accepted"); entered = b.getBoolean("entered");
        dragonSlain = b.getBoolean("dragon_slain"); spiderSlain = b.getBoolean("spider_slain");
        rewardsCreated = b.getBoolean("rewards_created");
        dragon = (ExpeditionDragon) b.get("dragon"); dragonFloor = b.getInt("dragon_floor");
        victoryPending = b.getBoolean("victory_pending");
        for (com.watabou.utils.Bundlable item : b.getCollection("fallen_items")) fallenItems.add((Item) item);
    }

    /** Quest snapshot is authoritative; stale floor files cannot duplicate or heal the dragon. */
    public static void arriveDragon(Level level) {
        if (Dungeon.branch != BRANCH || Dungeon.depth != CHASM && Dungeon.depth != HOARD) return;
        for (Mob mob : level.mobs.toArray(new Mob[0])) if (mob instanceof ExpeditionDragon) level.mobs.remove(mob);
        if (dragonSlain) return;
        if (dragon == null) {
            dragon = new ExpeditionDragon();
            dragon.pos = level.randomRespawnCell(dragon);
            dragonFloor = Dungeon.depth;
        } else if (dragonFloor != Dungeon.depth || !level.insideMap(dragon.pos)) {
            dragon.pos = level.randomRespawnCell(dragon);
            dragon.pending = ExpeditionDragon.Attack.NONE;
            dragonFloor = Dungeon.depth;
        }
        if (dragon.pos < 0) dragon.pos = level.entrance() + 1;
        dragon.timeToNow();
        level.mobs.add(dragon);
    }

    public static void spawnHunter(Level level) {
        if (!AVAILABLE || Dungeon.branch != 0 || Dungeon.depth != hunterDepth) return;
        int cell = level.randomRespawnCell(null);
        if (cell < 0) return;
        TreasureHunter hunter = new TreasureHunter();
        hunter.pos = hunterPos = cell;
        level.mobs.add(hunter);
    }

    /** Exchange precisely one ordinary healing potion; repeating the conversation gives no rewards. */
    public static boolean accept(Hero hero) {
        if (accepted || Dungeon.branch != 0 || Dungeon.depth != hunterDepth
                || hunterPos < 0 || Dungeon.level.distance(hero.pos, hunterPos) > 1) return false;
        PotionOfHealing potion = hero.belongings.getItem(PotionOfHealing.class);
        if (potion == null || potion.getClass() != PotionOfHealing.class) return false;
        potion.detach(hero.belongings.backpack);
        accepted = true;
        give(hero, new ExpeditionMap());
        give(hero, new ElixirOfFeatherFall().identify());
        return true;
    }

    private static void give(Hero hero, Item item) {
        if (!item.collect(hero.belongings.backpack)) Dungeon.level.drop(item, hero.pos);
    }

    public static boolean canEnter(Hero hero) {
        return AVAILABLE && accepted && hero.belongings.getItem(ExpeditionMap.class) != null
                && Dungeon.branch == 0 && Dungeon.depth == hunterDepth && !Dungeon.level.locked
                && hunterPos >= 0 && Dungeon.level.distance(hero.pos, hunterPos) <= 2;
    }

    public static void enter(Hero hero) {
        if (!canEnter(hero)) return;
        returnCell = hero.pos;
        entered = true;
        travel(CHASM, BRANCH, -1);
    }

    public static void travel(int depth, int branch, int cell) {
        Level.beforeTransition();
        InterlevelScene.returnDepth = depth;
        InterlevelScene.returnBranch = branch;
        InterlevelScene.returnPos = cell;
        InterlevelScene.mode = InterlevelScene.Mode.RETURN;
        Game.switchScene(InterlevelScene.class);
    }
}
