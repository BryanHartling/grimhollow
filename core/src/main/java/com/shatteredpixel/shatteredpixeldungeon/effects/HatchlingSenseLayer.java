// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.ItemSense;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Group;
import java.util.HashMap;
import java.util.HashSet;

/** Draw only sensed objects after fog. Never mutate FOV, mapped, visited or heap.seen. */
public class HatchlingSenseLayer extends Group {
    private final HashMap<Integer,ItemSprite> markers = new HashMap<>();
    private final HashMap<Integer,Integer> images = new HashMap<>();
    @Override public void update() {
        ItemSense sense = Dungeon.hero.buff(ItemSense.class);
        HashSet<Integer> used = new HashSet<>();
        if (sense != null) {
            for (Heap heap : Dungeon.level.heaps.valueList())
                if (!heap.isEmpty() && sense.senses(heap.pos) && !Dungeon.level.heroFOV[heap.pos])
                    mark(heap.pos,heapImage(heap),used);
            if (sense.revealsMimics()) for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0]))
                if (mob instanceof Mimic && mob.isAlive() && sense.senses(mob.pos))
                    mark(mob.pos,ItemSpriteSheet.HATCHLING_MIMIC,used);
        }
        for (Integer cell : new HashSet<>(markers.keySet())) if (!used.contains(cell)) {
            ItemSprite sprite=markers.remove(cell); remove(sprite); sprite.destroy(); images.remove(cell);
        }
        super.update();
    }
    private static int heapImage(Heap heap) {
        switch (heap.type) {
            case CHEST: return ItemSpriteSheet.CHEST;
            case LOCKED_CHEST: return ItemSpriteSheet.LOCKED_CHEST;
            case CRYSTAL_CHEST: return ItemSpriteSheet.CRYSTAL_CHEST;
            case TOMB: return ItemSpriteSheet.TOMB;
            case SKELETON: return ItemSpriteSheet.BONES;
            case REMAINS: return ItemSpriteSheet.REMAINS;
            default: return heap.peek().image;
        }
    }
    private void mark(int cell, int image, HashSet<Integer> used) {
        used.add(cell);
        ItemSprite sprite=markers.get(cell);
        if (sprite==null) { sprite=new ItemSprite(image); markers.put(cell,sprite); add(sprite); }
        if (!images.containsKey(cell) || images.get(cell)!=image) { sprite.view(image,null); images.put(cell,image); }
        sprite.place(cell); sprite.alpha(.85f);
    }
}
