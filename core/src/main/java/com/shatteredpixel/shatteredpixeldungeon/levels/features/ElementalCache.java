// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

/** A persistent elemental seal; discovery and opening are deliberately separate. */
public class ElementalCache implements Bundlable {
    public enum Kind { FIRE, WATER, LIGHTNING }
    public int door, mechanism, keyCost;
    public Kind kind;
    public boolean opened;

    public static ElementalCache atDoor(Level level, int cell) {
        if (level != null) for (ElementalCache cache : level.elementalCaches) if (cache.door == cell) return cache;
        return null;
    }
    public static ElementalCache atMechanism(Level level, int cell) {
        if (level != null) for (ElementalCache cache : level.elementalCaches) if (cache.mechanism == cell) return cache;
        return null;
    }
    public static boolean sealed(Level level, int cell) {
        ElementalCache cache = atDoor(level, cell);
        return cache != null && !cache.opened;
    }
    public static float searchChance(Level level, int cell, float ordinary, boolean foresight) {
        return !foresight && sealed(level, cell) ? ordinary * .5f : ordinary;
    }
    public static boolean activate(Level level, int cell, Kind effect) {
        ElementalCache cache = atMechanism(level, cell);
        if (cache == null || cache.opened || cache.kind != effect) return false;
        cache.open(level);
        return true;
    }
    public void open(Level level) {
        if (opened) return;
        opened = true;
        Level.set(door, Terrain.DOOR, level);
        level.mapped[door] = true;
        if (level == Dungeon.level) {
            GameScene.updateMap(door);
            for (CustomTilemap tile : level.customTiles) if (tile instanceof MechanismTile) tile.updateKnowledge();
            if (Dungeon.hero != null) {
                Dungeon.observe();
                Dungeon.hero.interrupt();
                GLog.p(Messages.get(ElementalCache.class, "opened"));
            }
        }
    }
    public static boolean pour(Hero hero, Waterskin skin, int cell) {
        Level level = Dungeon.level;
        ElementalCache cache = atMechanism(level, cell);
        if (hero != Dungeon.hero || cache == null || cache.opened || cache.kind != Kind.WATER
                || !skin.isFull() || !hero.belongings.contains(skin) || !level.adjacent(hero.pos, cell)
                || !level.heroFOV[cell]) return false;
        skin.empty();
        cache.open(level);
        hero.busy();
        hero.spend(1f);
        if (hero.sprite != null) hero.sprite.operate(cell); else hero.next();
        return true;
    }
    /** Decoration is finalized after regional painters so the visible clue survives grass/water. */
    public static void finishPlacement(Level level) {
        java.util.HashSet<Integer> occupied=new java.util.HashSet<>();
        for (ElementalCache cache : level.elementalCaches) {
            // Two leaf entrances can face the same corridor cell. Keep their clues distinct.
            if(occupied.contains(cache.mechanism))cache.mechanism=freeClueCell(level,cache.mechanism,occupied);
            occupied.add(cache.mechanism);
            Level.set(cache.door, Terrain.SECRET_DOOR, level);
            Level.set(cache.mechanism, Terrain.EMPTY_DECO, level);
            level.traps.remove(cache.mechanism);
            level.plants.remove(cache.mechanism);
            MechanismTile tile = new MechanismTile();
            tile.pos(cache.mechanism, level);
            level.customTiles.add(tile);
        }
    }
    private static int freeClueCell(Level level,int start,java.util.Set<Integer> occupied) {
        java.util.ArrayDeque<Integer> queue=new java.util.ArrayDeque<>();boolean[] seen=new boolean[level.length()];
        queue.add(start);seen[start]=true;
        while(!queue.isEmpty()) {
            int cell=queue.remove();
            if(!occupied.contains(cell)&&atDoor(level,cell)==null)return cell;
            for(int offset:com.watabou.utils.PathFinder.NEIGHBOURS8) {
                int next=cell+offset;
                if(level.insideMap(next)&&!seen[next]&&level.passable[next]&&atDoor(level,next)==null){seen[next]=true;queue.add(next);}
            }
        }
        throw new IllegalStateException("Elemental treasury has no accessible clue cell");
    }
    public String doorDescription() { return Messages.get(ElementalCache.class, "seal_desc", keyCost); }
    @Override public void storeInBundle(Bundle b) {
        b.put("door", door); b.put("mechanism", mechanism); b.put("kind", kind); b.put("opened", opened); b.put("key_cost", keyCost);
    }
    @Override public void restoreFromBundle(Bundle b) {
        door=b.getInt("door"); mechanism=b.getInt("mechanism"); kind=b.getEnum("kind",Kind.class); opened=b.getBoolean("opened"); keyCost=b.getInt("key_cost");
    }

    public static class MechanismTile extends CustomTilemap {
        { texture = "environment/custom_tiles/elemental_cache.png"; }
        private int lastFrame=-1;
        private boolean lastKnown;
        private ElementalCache cache() { return atMechanism(Dungeon.level, tileX + tileY * Dungeon.level.width()); }
        @Override protected boolean knownSource(int cell) {
            int source=tileX+tileY*Dungeon.level.width();
            return Dungeon.level.heroFOV[source] || Dungeon.level.visited[source] || Dungeon.level.mapped[source];
        }
        @Override public Tilemap create() { super.create(); lastFrame=-1; updateKnowledge(); return vis; }
        @Override public void updateKnowledge() {
            if (vis != null) {
                ElementalCache c=cache();int frame=c==null?0:c.kind.ordinal()+(c.opened?4:0);boolean known=knownSource(0);
                if(frame!=lastFrame)vis.map(new int[]{frame},1);else if(known!=lastKnown)vis.updateMap();
                lastFrame=frame;lastKnown=known;
            }
        }
        @Override public boolean allowWater(int x,int y) { return false; }
        @Override public String name(int x,int y) { ElementalCache c=cache();return c==null?null:Messages.get(ElementalCache.class,c.kind.name().toLowerCase(java.util.Locale.ROOT)); }
        @Override public String desc(int x,int y) {
            ElementalCache c=cache();return c==null?null:Messages.get(ElementalCache.class,c.opened?"spent":c.kind.name().toLowerCase(java.util.Locale.ROOT)+"_desc");
        }
    }
}
