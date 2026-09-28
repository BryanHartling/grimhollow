// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.ExpeditionMap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;

final class ExpeditionScenario {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError("59: " + message); }
    private static void maze() throws Exception {
        java.util.Set<Integer> exits = new java.util.HashSet<>();
        long originalSeed = Dungeon.seed;
        for (int seed = 0; seed < 64; seed++) {
            Dungeon.seed = seed; Dungeon.branch = DragonExpedition.BRANCH; Dungeon.depth = DragonExpedition.CHASM;
            com.shatteredpixel.shatteredpixeldungeon.levels.DragonChasmLevel level =
                    (com.shatteredpixel.shatteredpixeldungeon.levels.DragonChasmLevel) Dungeon.newLevel();
            Dungeon.switchLevel(level, -1);
            check(Dungeon.hero.pos == level.centerCell(), "central arrival");
            check(!Dungeon.interfloorTeleportAllowed(), "portable escape from expedition");
            check(level.addRespawner() == null && level.heaps.size == 0, "main dungeon supplies leak into branch");
            java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
            boolean[] reached = new boolean[level.length()];
            reached[level.entrance()] = true; queue.add(level.entrance());
            while (!queue.isEmpty()) {
                int c = queue.remove();
                for (int d : new int[]{-1, 1, -level.width(), level.width()}) {
                    int n = c + d;
                    if (level.insideMap(n) && level.passable[n] && !reached[n]) { reached[n] = true; queue.add(n); }
                }
            }
            int walkable = 0, pits = 0;
            for (int c = 0; c < level.length(); c++) {
                if (level.passable[c]) { walkable++; check(reached[c], "disconnected platform " + seed + ":" + c); check(!level.flamable[c], "platform can burn away"); }
                if (level.pit[c]) pits++;
            }
            check(pits > walkable && reached[level.exit()] && level.loopCount >= 1 && level.deadEnds >= 4, "maze lacks chasm/loops/dead ends");
            exits.add(level.exitIndex);
            Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
            Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
            check(java.util.Arrays.equals(level.map, Dungeon.level.map) && Dungeon.level.exit() == level.exit(), "maze rerolled after save/load");
        }
        check(exits.size() == 8, "all eight exit placements not exercised");
        Dungeon.seed = originalSeed;
        System.out.println("TEST 59 maze PASS: 64 seeds, 8 exit positions, all platforms reachable, loops/dead ends, nonflammable, disk persistence");
    }
    private static void cavern() throws Exception {
        Dungeon.init(); Dungeon.branch = DragonExpedition.BRANCH; Dungeon.depth = DragonExpedition.CHASM;
        Dungeon.switchLevel(Dungeon.newLevel(), -1);
        Dungeon.dropToChasm(new Torch());
        Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
        Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
        check(DragonExpedition.fallenItems.size() == 1 && Dungeon.droppedItems.get(18) == null, "fallen items leak into main dungeon or disappear on save");
        InterlevelScene.curTransition = Dungeon.level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
        java.lang.reflect.Method ascend = InterlevelScene.class.getDeclaredMethod("ascend"); ascend.setAccessible(true);
        int hp = Dungeon.hero.HP; ascend.invoke(new InterlevelScene());
        check(Dungeon.level instanceof DragonCavernLevel && Dungeon.hero.HP == hp && Dungeon.hero.buff(Cripple.class) == null, "voluntary descent harms hero");
        DragonCavernLevel level = (DragonCavernLevel) Dungeon.level;
        check(level.viewDistance == 3 && level.addRespawner() == null && level.heaps.size == 40, "dark finite cavern");
        int food = 0, torches = 0;
        for (Heap heap : level.heaps.valueList()) for (Item item : heap.items) {
            if (item instanceof Food) food += item.quantity(); if (item instanceof Torch) torches += item.quantity();
        }
        check(food == 3 && torches == 4, "guaranteed supplies");
        int pillars = 0;
        for (int y = 1; y < level.height()-1; y++) for (int x = 1; x < level.width()-1; x++) {
            int c = y * level.width() + x;
            if (level.map[c] == Terrain.WALL && level.map[c-1] != Terrain.WALL && level.map[c-level.width()] != Terrain.WALL) {
                check(level.map[c+1] == Terrain.WALL && level.map[c+level.width()] == Terrain.WALL
                        && level.map[c+level.width()+1] == Terrain.WALL, "pillar not 2x2"); pillars++;
            }
        }
        check(pillars >= 15, "not enough pillars");
        Broodmother boss = null;
        for (Mob mob : level.mobs) { mob.sprite = mob.sprite(); if (mob instanceof Broodmother) boss = (Broodmother) mob; }
        // Supply the visual-only emitter normally owned by GameScene in this headless fixture.
        Dungeon.hero.sprite = new HeroSprite() {
            @Override public com.watabou.noosa.particles.Emitter emitter() { return new com.watabou.noosa.particles.Emitter(); }
        };
        check(boss != null && boss.state == boss.HUNTING && level.mobs.size() == 7, "arrival fails to alert boss / initial count");
        check(!level.activateTransition(Dungeon.hero, level.getTransition(null)), "climb bypasses living boss");
        Dungeon.hero.HT = Dungeon.hero.HP = 100;
        com.watabou.noosa.Camera.main = new com.watabou.noosa.Camera(0,0,320,240,1);
        Chasm.heroLand();
        check(Dungeon.hero.HP == 85 && Dungeon.hero.buff(Cripple.class) != null
                && Dungeon.hero.buff(Bleeding.class) == null, "special fall damage/cripple/bleeding");
        Buff.detach(Dungeon.hero, Cripple.class);
        Buff.affect(Dungeon.hero, ElixirOfFeatherFall.FeatherBuff.class, 50f);
        Chasm.heroLand();
        check(Dungeon.hero.HP == 85 && Dungeon.hero.buff(Cripple.class) == null, "feather fall protection");
        for (int i = 0; i < 3; i++) {
            boss.hatchCell = level.randomRespawnCell(null); check(boss.hatch(), "valid hatch rejected");
        }
        check(!boss.canHatch(), "live brood cap");
        for (int wave = 0; wave < 3; wave++) {
            for (Mob mob : level.mobs.toArray(new Mob[0])) if (mob instanceof CavernSpinner && ((CavernSpinner) mob).hatchling) {
                check(mob.EXP == 0 && mob.lootChance() == 0, "hatchling farming");
                mob.alignment = Char.Alignment.NEUTRAL; mob.destroy();
            }
            boss.hatchCell = level.randomRespawnCell(null); check(boss.hatch(), "later hatch rejected");
        }
        check(boss.hatched == 6 && !boss.canHatch(), "finite lifetime brood budget");
        Bundle b = new Bundle(); b.put("boss", boss); Broodmother copy = (Broodmother) b.get("boss");
        check(copy.hatched == 6 && !copy.canHatch(), "brood budget reset on load");
        Dungeon.hero.lvl = 30; boss.HP = 0; boss.die(Dungeon.hero);
        check(DragonExpedition.spiderSlain && level.mobs.isEmpty(), "cavern not safe after victory");
        InterlevelScene.curTransition = level.getTransition(null);
        java.lang.reflect.Method descend = InterlevelScene.class.getDeclaredMethod("descend"); descend.setAccessible(true);
        descend.invoke(new InterlevelScene());
        check(Dungeon.level instanceof DragonChasmLevel && Dungeon.hero.pos == DragonChasmLevel.centerCell(), "return climb destination");
        InterlevelScene.curTransition = Dungeon.level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
        ascend.invoke(new InterlevelScene());
        check(Dungeon.level.mobs.isEmpty() && Dungeon.level.heaps.size == 40, "reentry regenerates enemies/supplies");
        System.out.println("TEST 59 cavern PASS: pillars, guaranteed finite supplies, descent/climb, fall/Feather Fall, brood caps and persistence, cleared floor remains safe");
    }
    static void run() throws Exception {
        Dungeon.init();
        int chosen = DragonExpedition.hunterDepth;
        check(chosen >= 16 && chosen <= 19, "hunter placement outside City");
        Random.pushGenerator(109); long expected = Random.Long(); Random.popGenerator();
        Random.pushGenerator(109); DragonExpedition.reset();
        check(Random.Long() == expected && DragonExpedition.hunterDepth == chosen, "quest seed perturbs main RNG");
        Random.popGenerator();
        Dungeon.depth = chosen; Dungeon.switchLevel(Dungeon.newLevel(), -1);
        DragonExpedition.hunterPos = Dungeon.hero.pos;
        for (PotionOfHealing p : Dungeon.hero.belongings.getAllItems(PotionOfHealing.class)) p.detachAll(Dungeon.hero.belongings.backpack);
        check(!DragonExpedition.accept(Dungeon.hero), "free map without healing potion");
        new PotionOfHealing().quantity(2).collect();
        check(DragonExpedition.accept(Dungeon.hero), "healing exchange failed");
        check(Dungeon.hero.belongings.getItem(PotionOfHealing.class).quantity() == 1, "exchange consumes exactly one potion");
        check(Dungeon.hero.belongings.getItem(ExpeditionMap.class) != null
                && Dungeon.hero.belongings.getItem(ElixirOfFeatherFall.class) != null, "missing quest supplies");
        check(!DragonExpedition.accept(Dungeon.hero), "repeat reward");
        DragonExpedition.entered = true; DragonExpedition.returnCell = Dungeon.hero.pos;
        DragonExpedition.spiderSlain = true;
        Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
        Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
        check(DragonExpedition.accepted && DragonExpedition.entered && DragonExpedition.spiderSlain
                && DragonExpedition.returnCell == Dungeon.hero.pos, "actual save/load loses quest state");
        check(new ExpeditionMap().unique && !new ExpeditionMap().isUpgradable(), "map must be protected quest item");
        DragonExpedition.restore(new Bundle());
        check(!DragonExpedition.accepted && !DragonExpedition.entered && DragonExpedition.returnCell == -1, "old save/new run inherits quest");
        check(DragonExpedition.BRANCH != 1, "expedition aliases Vault branch");
        maze(); cavern();
        System.out.println("TEST 59 foundation PASS: seeded placement, healing exchange, unique rewards, protected map, disk save/load, legacy defaults");
    }
}
