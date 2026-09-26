// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.watabou.noosa.Group;

/** A light second pass keeps visible loot recognizable beneath dense clouds. */
public class HeapReadability extends Group {
    @Override public void draw() {
        for (Heap heap : Dungeon.level.heaps.valueList()) {
            if (heap.hidden || !Dungeon.level.heroFOV[heap.pos]
                    || heap.sprite == null || !heap.sprite.visible) continue;
            boolean covered = false;
            for (Blob blob : Dungeon.level.blobs.values()) {
                if (blob.cur != null && blob.cur[heap.pos] > 0 && EnhancedEffects.replaces(blob)) {
                    covered = true;
                    break;
                }
            }
            if (covered) {
                float alpha = heap.sprite.am;
                heap.sprite.am = alpha * .75f;
                heap.sprite.draw();
                heap.sprite.am = alpha;
            }
        }
    }
}
