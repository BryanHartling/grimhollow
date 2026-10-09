// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;
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
    private static int weaponTier(Item item) {
        return item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon
                ?((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon)item).tier
                :((com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon)item).tier;
    }
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
        check(level.viewDistance == 3 && level.addRespawner() instanceof DragonCavernLevel.CavernSpawner && level.heaps.size == 48, "dark cavern, dedicated timer and finite supplies");
        check(level.map[DragonCavernLevel.CENTER]==Terrain.ENTRANCE,"cavern climb must depict stairs up");
        int food = 0, torches = 0, gear=0, rings=0, bones=0, remains=0;
        for(Heap heap:level.heaps.valueList()){if(heap.type==Heap.Type.SKELETON)bones++;if(heap.type==Heap.Type.REMAINS)remains++;}
        for (Heap heap : level.heaps.valueList()) for (Item item : heap.items) {
            if (item instanceof Food) food += item.quantity(); if (item instanceof Torch) torches += item.quantity();
            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon || item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor){
                gear++;check(item.trueLevel()==0,"common remains have upgrades");
                if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)
                    check(weaponTier(item)<=3,"remains weapon tier");
                else check(((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item).tier<=3,"remains armor tier");
            }
            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring)rings++;
        }
        check(food == 3 && torches == 4, "guaranteed supplies");
        check(gear==33&&rings==2&&bones==24&&remains==24,"equipment majority, occasional rings and mixed remains");
        int pillars = 0;
        for (int y = 1; y < level.height()-1; y++) for (int x = 1; x < level.width()-1; x++) {
            int c = y * level.width() + x;
            if (x<level.width()-2&&y<level.height()-2&&level.map[c] == Terrain.WALL && level.map[c-1] != Terrain.WALL
                    && level.map[c-level.width()] != Terrain.WALL&&level.map[c+2]!=Terrain.WALL&&level.map[c+2*level.width()]!=Terrain.WALL) {
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
        Actor timer = level.addRespawner(); float remaining = timer.cooldown();
        check(remaining >= 60 && remaining <= 100 && timer == level.addRespawner()
                && timer.cooldown() == remaining, "duplicate timer / visit resets replenishment delay");
        int survivors = level.mobs.size() - 1;
        Dungeon.hero.lvl = 30; boss.HP = 0; boss.die(Dungeon.hero);
        check(DragonExpedition.spiderSlain && level.mobs.size() == survivors, "broodmother death clears surviving spiders");
        java.util.Arrays.fill(level.heroFOV, true);
        cavernTick(timer);
        check(level.mobs.size() == survivors && timer.cooldown() >= 60 && timer.cooldown() <= 100,
                "spawn inside sight / blocked attempt retries every turn");
        java.util.Arrays.fill(level.heroFOV, false);
        cavernTick(timer);
        Broodmother replacement = null;
        for (Mob mob : level.mobs) if (mob instanceof Broodmother) replacement = (Broodmother) mob;
        check(replacement != null && replacement.replenished && replacement.EXP == 0
                && replacement.createLoot() == null && replacement.canHatch(), "replacement mother missing / reward farming / cannot hatch after first victory");
        check(level.distance(replacement.pos, Dungeon.hero.pos) >= 8 && level.distance(replacement.pos, DragonCavernLevel.CENTER) >= 14
                && level.openSpace[replacement.pos], "replacement spawns beside hero / unsafe landing / cramped large mob");
        Bundle replacementBundle = new Bundle(); replacementBundle.put("mother", replacement);
        Broodmother restoredMother = (Broodmother) replacementBundle.get("mother");
        check(restoredMother.replenished && restoredMother.EXP == 0 && restoredMother.createLoot() == null, "replacement mother rewards return after load");
        replacement.hatchCell = level.randomRespawnCell(null);
        check(replacement.hatch(), "replacement cannot hatch after original victory");
        cavernTick(timer);
        CavernSpinner returning = null;
        for (Mob mob : level.mobs) if (mob instanceof CavernSpinner && ((CavernSpinner) mob).replenished) returning = (CavernSpinner) mob;
        check(returning != null && returning.EXP == 0 && !returning.hatchling && returning.createLoot() == null,
                "timed scavenger missing / farms rewards / wrongly counts as summoned brood");
        Bundle returningBundle = new Bundle(); returningBundle.put("spider", returning);
        CavernSpinner restoredSpider = (CavernSpinner) returningBundle.get("spider");
        check(restoredSpider.replenished && restoredSpider.EXP == 0 && restoredSpider.createLoot() == null, "returning spider rewards reset on load");
        int capped = level.mobs.size();
        java.util.Set<Float> delays = new java.util.HashSet<>();
        for (int attempt = 0; attempt < 20; attempt++) {
            cavernTick(timer); delays.add(timer.cooldown());
            check(level.mobs.size() == capped && timer.cooldown() >= 60 && timer.cooldown() <= 100,
                    "spider cap / duplicate mother / timer bounds");
        }
        check(delays.size() > 1, "replenishment timer is not randomized");
        com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth wealth = new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth();
        wealth.level(10); Dungeon.hero.belongings.ring = wealth; wealth.activate(Dungeon.hero);
        int heapCount = level.heaps.size, itemCount = 0;
        for (Heap heap : level.heaps.valueList()) itemCount += heap.items.size();
        for (int roll = 0; roll < 100; roll++) { replacement.rollToDropLoot(); returning.rollToDropLoot(); restoredSpider.rollToDropLoot(); }
        int afterItems = 0; for (Heap heap : level.heaps.valueList()) afterItems += heap.items.size();
        check(level.heaps.size == heapCount && afterItems == itemCount, "renewable ordinary / Wealth / Lucky loot farming");
        int experience = Dungeon.hero.exp;
        replacement.sprite = replacement.sprite(); replacement.HP = 0; replacement.die(Dungeon.hero);
        check(Dungeon.hero.exp == experience && DragonExpedition.spiderSlain && level.mobs.size() == capped - 1,
                "replacement boss grants XP / resets victory / clears spiders");
        int retained = level.mobs.size(); float savedDelay = timer.cooldown();
        InterlevelScene.curTransition = level.getTransition(null);
        java.lang.reflect.Method descend = InterlevelScene.class.getDeclaredMethod("descend"); descend.setAccessible(true);
        descend.invoke(new InterlevelScene());
        check(Dungeon.level instanceof DragonChasmLevel && Dungeon.hero.pos == DragonChasmLevel.centerCell(), "return climb destination");
        InterlevelScene.curTransition = Dungeon.level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
        ascend.invoke(new InterlevelScene());
        check(Dungeon.level.mobs.size() == retained && Dungeon.level.heaps.size == 48, "reentry clears surviving enemies / regenerates supplies");
        level = (DragonCavernLevel) Dungeon.level;
        timer = level.addRespawner();
        check(timer.cooldown() == savedDelay && timer == level.addRespawner(), "saved timer resets across floor transitions");
        Bundle legacy = new Bundle(); level.storeInBundle(legacy); legacy.remove("cavern_spawner");
        DragonCavernLevel oldSave = new DragonCavernLevel(); oldSave.restoreFromBundle(legacy);
        check(oldSave.addRespawner() != null && oldSave.mobs.size() == retained, "old cavern save loses mobs / fails timer migration");
        Actor.remove(oldSave.addRespawner());
        check(Dungeon.level.map[DragonCavernLevel.CENTER]==Terrain.ENTRANCE,"loaded cavern stairs up");
        long originalSeed=Dungeon.seed;
        for(int seed=0;seed<32;seed++) {
            Dungeon.init();Dungeon.seed=seed;Dungeon.branch=DragonExpedition.BRANCH;Dungeon.depth=DragonExpedition.CAVERN;
            level=(DragonCavernLevel)Dungeon.newLevel();
            Buff.affect(Dungeon.hero,Chasm.Falling.class);
            Dungeon.switchLevel(level,level.fallCell(false));
            Broodmother mother=null;for(Mob mob:level.mobs)if(mob instanceof Broodmother)mother=(Broodmother)mob;
            check(mother!=null&&mother.state!=mother.HUNTING,"fall immediately alerts broodmother");
            check(level.distance(mother.pos,DragonCavernLevel.CENTER)>=14,"broodmother too near central landing");
            for(int trial=0;trial<20;trial++)check(level.distance(level.fallCell(false),mother.pos)>=10,"fall lands beside broodmother");
            boolean[] reached=new boolean[level.length()];java.util.ArrayDeque<Integer> queue=new java.util.ArrayDeque<>();
            reached[DragonCavernLevel.CENTER]=true;queue.add(DragonCavernLevel.CENTER);
            while(!queue.isEmpty()){int cell=queue.remove();for(int d:new int[]{-1,1,-level.width(),level.width()}){
                int n=cell+d;if(level.insideMap(n)&&level.passable[n]&&!reached[n]){reached[n]=true;queue.add(n);}
            }}
            int walkable=0;java.util.HashSet<Integer> edges=new java.util.HashSet<>();
            for(int cell=0;cell<level.length();cell++)if(level.passable[cell]){check(reached[cell],"disconnected natural cavern");walkable++;}
            for(int y=2;y<level.height()-2;y++)for(int x=1;x<level.width()-1;x++)if(level.passable[y*level.width()+x]){edges.add(x);break;}
            check(walkable>650&&walkable<1200&&edges.size()>=7,"cavern lacks open interior / natural contour");
            for(Mob mob:level.mobs)if(mob instanceof CavernSpinner)
                check(level.distance(mob.pos,DragonCavernLevel.CENTER)<=10,"standard spiders not on approach");
            check(level.map[1*level.width()+1]==Terrain.WALL,"square room corner still open");
            Buff.detach(Dungeon.hero,Chasm.Falling.class);
        }
        Dungeon.seed=originalSeed;
        System.out.println("TEST 59 cavern PASS: natural outlines/falls, finite mixed supplies, surviving spiders, randomized 60-100-turn pressure, hidden arrivals, replacement brood, caps, zero renewable XP/loot with Wealth +10, open climb, saved timer and old-save migration");
    }
    private static void cavernTick(Actor timer) throws Exception {
        java.lang.reflect.Method spend = Actor.class.getDeclaredMethod("spend", float.class); spend.setAccessible(true);
        spend.invoke(timer, -timer.cooldown());
        java.lang.reflect.Method act = DragonCavernLevel.CavernSpawner.class.getDeclaredMethod("act"); act.setAccessible(true);
        check((Boolean) act.invoke(timer), "cavern timer stalls actor loop");
    }
    private static void dragon() throws Exception {
        Dungeon.init(); Dungeon.branch = DragonExpedition.BRANCH; Dungeon.depth = DragonExpedition.CHASM;
        Dungeon.switchLevel(Dungeon.newLevel(), -1);
        ExpeditionDragon dragon = DragonExpedition.dragon;
        int center = DragonChasmLevel.centerCell(), width = Dungeon.level.width();
        for (int y = -8; y <= 8; y++) for (int x = -8; x <= 8; x++) Level.set(center + y*width + x, Terrain.EMPTY_SP);
        dragon.pos = center; dragon.sprite = dragon.sprite(); dragon.sprite.visible = false;
        Dungeon.hero.pos = center + 5; Dungeon.hero.sprite = new HeroSprite(); Dungeon.hero.sprite.visible = false;
        Dungeon.hero.HP = Dungeon.hero.HT = 200; Dungeon.hero.lvl = 24;
        java.util.Arrays.fill(Dungeon.level.heroFOV, true); dragon.aggro(Dungeon.hero);
        int hp = Dungeon.hero.HP;
        check(dragon.prepare(ExpeditionDragon.Attack.BREATH, Dungeon.hero.pos), "breath preparation");
        check(Dungeon.hero.HP == hp, "windup deals damage");
        java.util.Set<Integer> cone = dragon.attackCells(ExpeditionDragon.Attack.BREATH, Dungeon.hero.pos);
        check(cone.contains(Dungeon.hero.pos) && !cone.contains(center + 8), "breath range/cone");
        Level.set(center+3, Terrain.WALL);
        check(!dragon.attackCells(ExpeditionDragon.Attack.BREATH, Dungeon.hero.pos).contains(center+5), "breath passes through wall");
        Level.set(center+3, Terrain.EMPTY_SP);
        Dungeon.hero.pos = center - 5; dragon.release();
        check(Dungeon.hero.HP == hp, "breath tracks hero after warning");
        check(!dragon.prepare(ExpeditionDragon.Attack.BREATH, center+5), "immediate repeat breath");
        java.lang.reflect.Method spend = ExpeditionDragon.class.getDeclaredMethod("spend", float.class); spend.setAccessible(true);
        spend.invoke(dragon, 2f); check(!dragon.prepare(ExpeditionDragon.Attack.BREATH, center+5), "breath before three turns");
        spend.invoke(dragon, 1f); check(dragon.prepare(ExpeditionDragon.Attack.BREATH, center+5), "breath after three turns");
        Dungeon.hero.pos = center + 5; dragon.release(); check(Dungeon.hero.HP < hp, "breath misses target in warned cone");
        Buff.detach(Dungeon.hero, Burning.class); Dungeon.level.blobs.clear();
        Dungeon.hero.pos = center+1; hp = Dungeon.hero.HP;
        check(dragon.prepare(ExpeditionDragon.Attack.WINGBEAT, Dungeon.hero.pos), "wingbeat prep");
        dragon.release(); check(Dungeon.hero.pos == center+3 && Dungeon.hero.HP < hp, "wingbeat damage and two-cell push");
        int old = dragon.HP; dragon.damage(31, ExpeditionScenario.class); int wounded = dragon.HP;
        check(wounded < old && dragon.heal(999) == 0, "dragon healed");
        DragonExpedition.arriveDragon(Dungeon.level);
        check(Dungeon.level.mobs.stream().filter(m -> m instanceof ExpeditionDragon).count() == 1, "duplicate dragon");
        Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
        Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
        check(DragonExpedition.dragon.HP == wounded && DragonExpedition.dragon.breathDelay == dragon.breathDelay, "dragon HP/cooldown reset on save");
        InterlevelScene.curTransition = Dungeon.level.getTransition(LevelTransition.Type.REGULAR_ENTRANCE);
        java.lang.reflect.Method ascend = InterlevelScene.class.getDeclaredMethod("ascend"); ascend.setAccessible(true); ascend.invoke(new InterlevelScene());
        check(Dungeon.level.mobs.stream().noneMatch(m -> m instanceof ExpeditionDragon), "dragon follows into lower cavern");
        Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
        Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
        InterlevelScene.curTransition = Dungeon.level.getTransition(null);
        java.lang.reflect.Method descend = InterlevelScene.class.getDeclaredMethod("descend"); descend.setAccessible(true); descend.invoke(new InterlevelScene());
        check(DragonExpedition.dragon.HP == wounded && Dungeon.level.mobs.contains(DragonExpedition.dragon), "dragon healed/disappeared after cavern round trip");
        Dungeon.hero.sprite = new HeroSprite(); DragonExpedition.dragon.sprite = DragonExpedition.dragon.sprite();
        DragonExpedition.dragon.HP = 0; DragonExpedition.dragon.die(Dungeon.hero);
        check(DragonExpedition.dragonSlain && DragonExpedition.victoryPending, "victory not recorded");
        DragonExpedition.arriveDragon(Dungeon.level);
        check(Dungeon.level.mobs.stream().noneMatch(m -> m instanceof ExpeditionDragon), "dead dragon respawns");
        System.out.println("TEST 59 dragon PASS: warned fixed cone, range/occlusion, three-turn cooldown, wingbeat distance, no healing, single persistent boss, cavern round trip and death");
    }
    private static void returnRoute() throws Exception {
        java.lang.reflect.Method route = InterlevelScene.class.getDeclaredMethod("returnTo");
        route.setAccessible(true); route.invoke(new InterlevelScene());
    }
    private static void hoard() throws Exception {
        Dungeon.init(); Dungeon.depth = DragonExpedition.hunterDepth; Dungeon.switchLevel(Dungeon.newLevel(), -1);
        DragonExpedition.hunterPos = Dungeon.hero.pos; DragonExpedition.returnCell = Dungeon.hero.pos;
        new PotionOfHealing().collect(); check(DragonExpedition.accept(Dungeon.hero), "quest exchange for full route");
        int town = Dungeon.depth, returnCell = Dungeon.hero.pos;
        DragonExpedition.travel(DragonExpedition.CHASM, DragonExpedition.BRANCH, -1); returnRoute();
        check(Dungeon.branch == 2 && Dungeon.level instanceof DragonChasmLevel, "expedition entry route");
        ExpeditionDragon dragon = DragonExpedition.dragon; dragon.sprite = dragon.sprite(); dragon.damage(41, ExpeditionScenario.class);
        int wounded = dragon.HP;
        InterlevelScene.curTransition = Dungeon.level.getTransition(LevelTransition.Type.REGULAR_EXIT);
        java.lang.reflect.Method descend = InterlevelScene.class.getDeclaredMethod("descend"); descend.setAccessible(true);
        descend.invoke(new InterlevelScene());
        check(Dungeon.level instanceof DragonHoardLevel && DragonExpedition.dragon.HP == wounded
                && Dungeon.level.mobs.stream().filter(m -> m instanceof ExpeditionDragon).count() == 1, "hoard relocation clones or heals dragon");
        DragonHoardLevel level = (DragonHoardLevel) Dungeon.level;
        check(level.map[DragonHoardLevel.RETURN]==Terrain.ENTRANCE,"hoard return must depict stairs up");
        check(level.heaps.size == 0 && !DragonExpedition.rewardsCreated, "treasure exists before dragon dies");
        check(level.activateTransition(Dungeon.hero, level.getTransition(LevelTransition.Type.REGULAR_EXIT)), "living dragon blocks permitted escape");
        returnRoute();
        check(Dungeon.branch == 0 && Dungeon.depth == town && Dungeon.hero.pos == returnCell, "escape loses original City location");
        DragonExpedition.travel(DragonExpedition.CHASM, DragonExpedition.BRANCH, -1); returnRoute();
        check(DragonExpedition.dragon.HP == wounded, "reentry heals boss");
        NecroSkeleton ally = new NecroSkeleton(); ally.pos = Dungeon.level.randomRespawnCell(ally);
        Dungeon.level.mobs.add(ally); Actor.add(ally); ally.sprite = ally.sprite();
        Dungeon.hero.sprite = new HeroSprite(); Dungeon.hero.lvl = 30;
        dragon = DragonExpedition.dragon; dragon.sprite = dragon.sprite(); dragon.HP = 0; dragon.die(Dungeon.hero);
        DragonVictoryPassage passage = Dungeon.hero.buff(DragonVictoryPassage.class);
        check(passage != null, "no immediate victory passage"); passage.act(); returnRoute();
        check(Dungeon.level instanceof DragonHoardLevel && Dungeon.hero.pos == DragonHoardLevel.VICTORY_ARRIVAL
                && !DragonExpedition.victoryPending && Dungeon.hero.buff(DragonVictoryPassage.class) == null, "victory placement or repeated transport");
        check(Dungeon.level.mobs.stream().anyMatch(m -> m instanceof NecroSkeleton)
                && Dungeon.level.mobs.stream().noneMatch(m -> m instanceof ExpeditionDragon), "victory loses ally or retains dragon");
        check(DragonExpedition.rewardsCreated && Dungeon.level.heaps.size >= 4, "victory reward missing");
        for (Heap heap : Dungeon.level.heaps.valueList()) for (Item item : heap.items) {
            if (item instanceof Gold) check(item.quantity() >= 2000 && item.quantity() <= 3000, "hoard gold bounds");
            else if (!(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)
                    && !(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket))
                check(item.trueLevel() >= 3 && item.trueLevel() <= 4 && item.isIdentified() && !item.cursed, "exceptional gear quality");
        }
        Dungeon.level.heaps.clear(); Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
        Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
        ((DragonHoardLevel) Dungeon.level).unlockHoard(); check(Dungeon.level.heaps.size == 0, "collected hoard regenerates on save/load");
        Dungeon.level.activateTransition(Dungeon.hero, Dungeon.level.getTransition(LevelTransition.Type.REGULAR_EXIT)); returnRoute();
        DragonExpedition.travel(DragonExpedition.HOARD, DragonExpedition.BRANCH, DragonHoardLevel.ARRIVAL); returnRoute();
        check(Dungeon.level.heaps.size == 0, "return visit duplicates treasure");
        new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth().collect();
        new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap().collect();
        check(com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth.mimicChanceMultiplier() == 1.5f
                && com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ParchmentScrap.enchantChanceMultiplier() == 2f,
                "two carried trinkets not simultaneously active");
        for (int i = 0; i < 30; i++) {
            com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket bonus = DragonHoardLevel.bonusTrinket();
            check(bonus != null && Dungeon.hero.belongings.getItem(bonus.getClass()) == null, "bonus trinket duplicates carried type");
        }
        System.out.println("TEST 59 hoard PASS: living-dragon retreat, original return cell, wounded reentry, safe victory/ally transfer, sealed treasure, one-time rewards, two active trinkets");
    }
    private static void tuning() throws Exception {
        Dungeon.init(); Playtest.enable();
        BalanceTuning.set(EXPEDITION_CHANCE, 0); check(!DragonExpedition.offerAvailable(), "disabled offer");
        BalanceTuning.set(EXPEDITION_CHANCE, 100); check(DragonExpedition.offerAvailable(), "guaranteed offer");
        BalanceTuning.set(EXPEDITION_CHANCE, 50);
        Random.pushGenerator(991); long expected=Random.Long(); Random.popGenerator();
        Random.pushGenerator(991); boolean offer=DragonExpedition.offerAvailable();
        check(offer==DragonExpedition.offerAvailable() && Random.Long()==expected,"offer tuning perturbs main RNG"); Random.popGenerator();
        BalanceTuning.set(DRAGON_HEALTH,777); BalanceTuning.set(DRAGON_DAMAGE,0); BalanceTuning.set(DRAGON_BREATH_COOLDOWN,7);
        BalanceTuning.set(DRAGON_KNOCKBACK,0); BalanceTuning.set(BROOD_HEALTH,333); BalanceTuning.set(BROOD_DAMAGE,0);
        BalanceTuning.set(CAVERN_SIGHT,5); BalanceTuning.set(SPIDERS_INITIAL,0); BalanceTuning.set(BROOD_LIVE,0);
        BalanceTuning.set(CAVERN_RATIONS,1); BalanceTuning.set(CAVERN_TORCHES,2);
        Dungeon.branch=2; Dungeon.depth=DragonExpedition.CAVERN;
        DragonCavernLevel level=(DragonCavernLevel)Dungeon.newLevel(); Dungeon.switchLevel(level,DragonCavernLevel.CENTER);
        check(level.viewDistance==5 && level.mobs.size()==1,"configured cavern sight/population");
        Broodmother brood=(Broodmother)level.mobs.iterator().next();
        check(brood.HP==333 && brood.damageRoll()==0 && !brood.canHatch(),"configured brood stats/cap");
        BalanceTuning.set(CAVERN_SPAWN_INTERVAL,0);
        check(!level.replenishBrood() && level.mobs.size()==1,"disabled cavern replenishment spawns creatures");
        BalanceTuning.set(CAVERN_SPAWN_INTERVAL,40);
        for(int sample=0;sample<20;sample++)check(level.respawnCooldown()>=30 && level.respawnCooldown()<=50,"configured cavern timer bounds");
        int food=0,torches=0;
        for(Heap heap:level.heaps.valueList())for(Item item:heap.items){if(item instanceof Food)food++;if(item instanceof Torch)torches++;}
        check(food==1 && torches==2 && level.heaps.size==48,"configured guaranteed supplies");
        BalanceTuning.set(CAVERN_REMAINS,36);BalanceTuning.set(CAVERN_GEAR_WEIGHT,100);
        BalanceTuning.set(CAVERN_RING_WEIGHT,0);BalanceTuning.set(CAVERN_GOLD_WEIGHT,0);BalanceTuning.set(CAVERN_CONSUMABLE_WEIGHT,0);
        BalanceTuning.set(CAVERN_MAX_TIER,1);BalanceTuning.set(CAVERN_UPGRADES,2);
        DragonCavernLevel configured=(DragonCavernLevel)Dungeon.newLevel();
        Dungeon.switchLevel(configured,DragonCavernLevel.CENTER);
        check(configured.heaps.size==36,"configured remains count");
        for(Heap heap:configured.heaps.valueList())for(Item item:heap.items)if(!(item instanceof Food)&&!(item instanceof Torch)){
            check(item.trueLevel()==2,"configured remains upgrade");
            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)
                check(weaponTier(item)==1,"configured weapon tier");
            else check(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor
                    &&((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item).tier==1,"configured gear-only mix/tier");
        }
        boolean rejected=false;try{BalanceTuning.set(CAVERN_GEAR_WEIGHT,0);}catch(IllegalArgumentException zeroWeights){rejected=true;}
        check(rejected,"all-zero cavern weights accepted");
        BalanceTuning.set(CAVERN_CONSUMABLE_WEIGHT,100);BalanceTuning.set(CAVERN_GEAR_WEIGHT,0);
        configured=(DragonCavernLevel)Dungeon.newLevel();
        Dungeon.switchLevel(configured,DragonCavernLevel.CENTER);
        for(Heap heap:configured.heaps.valueList())for(Item item:heap.items)check(item instanceof Food||item instanceof Torch
                ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion
                ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll
                ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone
                ||item instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed,"configured consumable-only mix");
        ExpeditionDragon dragon=new ExpeditionDragon();dragon.sprite=dragon.sprite(); dragon.pos=Dungeon.hero.pos+2;
        check(dragon.HP==777 && dragon.damageRoll()==0,"configured dragon stats");
        check(dragon.prepare(ExpeditionDragon.Attack.BREATH,dragon.pos+1),"configured breath prepare");dragon.release();
        check(dragon.breathDelay==7,"configured cooldown");
        BalanceTuning.set(HOARD_GOLD,0);BalanceTuning.set(HOARD_EQUIPMENT,1);BalanceTuning.set(HOARD_UPGRADES,5);
        BalanceTuning.set(HOARD_ARTIFACT,0);BalanceTuning.set(HOARD_TRINKET,100);
        DragonExpedition.dragonSlain=true;Dungeon.depth=DragonExpedition.HOARD;
        Dungeon.switchLevel(Dungeon.newLevel(),DragonHoardLevel.ARRIVAL);
        int equipment=0,trinkets=0;
        for(Heap heap:Dungeon.level.heaps.valueList())for(Item item:heap.items){
            check(!(item instanceof Gold) && !(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact),"disabled bonus/gold");
            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket)trinkets++;
            else {equipment++;check(item.trueLevel()>=5 && item.trueLevel()<=6,"configured reward level");}
        }
        check(equipment==1 && trinkets==1,"configured reward count/trinket chance");
        Dungeon.saveAll();Dungeon.loadGame(GamesInProgress.curSlot);Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot),Dungeon.hero.pos);
        check(BalanceTuning.get(DRAGON_HEALTH)==777 && BalanceTuning.get(HOARD_TRINKET)==100,"expedition tuning lost on disk");
        BalanceTuning.reset();check(new ExpeditionDragon().HT==480 && BalanceTuning.get(CAVERN_RATIONS)==3,"expedition reset");
        Dungeon.init();Dungeon.switchLevel(Dungeon.newLevel(),-1);Playtest.enable();
        for(int depth:new int[]{DragonExpedition.CHASM,DragonExpedition.CAVERN,DragonExpedition.HOARD}){
            Playtest.travel(depth,DragonExpedition.BRANCH);
            check(Dungeon.branch==2 && Dungeon.depth==depth && Dungeon.level.passable[Dungeon.hero.pos] && DragonExpedition.returnCell>=0,"playtest expedition destination");
        }
        Dungeon.level.activateTransition(Dungeon.hero,Dungeon.level.getTransition(LevelTransition.Type.REGULAR_EXIT));returnRoute();
        check(Dungeon.branch==0 && Dungeon.depth==DragonExpedition.hunterDepth && Dungeon.level.passable[Dungeon.hero.pos],"playtest expedition retreat");
        System.out.println("TEST 59 tuning PASS: isolated offer RNG, custom boss stats/cooldown, cavern population/sight/supplies, brood cap, one-time reward count/quality/chances, disk persistence/reset");
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
        Mob hunter=null;
        for(Mob mob:Dungeon.level.mobs)if(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter){check(hunter==null,"duplicate hunter");hunter=mob;}
        check(hunter!=null && hunter.pos==DragonExpedition.hunterPos,"campaign hunter missing");
        int approach=-1;
        for(int d:new int[]{-1,1,-Dungeon.level.width(),Dungeon.level.width()}){
            int cell=hunter.pos+d;
            if(Dungeon.level.passable[cell] && Dungeon.level.findMob(cell)==null){approach=cell;break;}
        }
        check(approach>=0,"hunter cannot be approached");Dungeon.hero.pos=approach;
        check(!DragonExpedition.canEnter(Dungeon.hero),"entry without exchange");
        for (PotionOfHealing p : Dungeon.hero.belongings.getAllItems(PotionOfHealing.class)) p.detachAll(Dungeon.hero.belongings.backpack);
        check(!DragonExpedition.accept(Dungeon.hero), "free map without healing potion");
        new PotionOfHealing().quantity(2).collect();
        check(DragonExpedition.accept(Dungeon.hero), "healing exchange failed");
        check(Dungeon.hero.belongings.getItem(PotionOfHealing.class).quantity() == 1, "exchange consumes exactly one potion");
        check(Dungeon.hero.belongings.getItem(ExpeditionMap.class) != null
                && Dungeon.hero.belongings.getItem(ElixirOfFeatherFall.class) != null, "missing quest supplies");
        check(!DragonExpedition.accept(Dungeon.hero), "repeat reward");
        check(DragonExpedition.canEnter(Dungeon.hero),"earned map cannot open at hunter");
        int savedReturn=Dungeon.hero.pos;DragonExpedition.enter(Dungeon.hero);returnRoute();
        check(Dungeon.branch==2 && Dungeon.level instanceof DragonChasmLevel && !DragonExpedition.canEnter(Dungeon.hero),"actual map entry/one-way gate");
        DragonExpedition.travel(chosen,0,savedReturn);returnRoute();
        DragonExpedition.spiderSlain = true;
        Dungeon.saveAll(); Dungeon.loadGame(GamesInProgress.curSlot);
        Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot), Dungeon.hero.pos);
        check(DragonExpedition.accepted && DragonExpedition.entered && DragonExpedition.spiderSlain
                && DragonExpedition.returnCell == Dungeon.hero.pos, "actual save/load loses quest state");
        check(new ExpeditionMap().unique && !new ExpeditionMap().isUpgradable(), "map must be protected quest item");
        DragonExpedition.restore(new Bundle());
        check(!DragonExpedition.accepted && !DragonExpedition.entered && DragonExpedition.returnCell == -1, "old save/new run inherits quest");
        check(DragonExpedition.BRANCH != 1, "expedition aliases Vault branch");
        maze(); cavern(); dragon(); hoard(); tuning();
        System.out.println("TEST 59 foundation PASS: seeded placement, healing exchange, unique rewards, protected map, disk save/load, legacy defaults");
    }
}
