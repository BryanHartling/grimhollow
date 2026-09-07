// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;

/** Raised skeletons collapse quietly, without the hostile skeleton's explosive death effect. */
public class NecroSkeletonSprite extends SkeletonSprite {
    @Override public void die() {
        sleeping=false;
        processStateRemoval(State.PARALYSED);
        play(die);
        hideEmo();
        if(health!=null)health.killAndErase();
    }
}
