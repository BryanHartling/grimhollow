// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
/** Permanent, limited-radius counterpart of Mind Vision; range is integrated in Level's existing sense pass. */
public class SeerSight extends Buff {
    {type=buffType.POSITIVE;}
    @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MIND_VISION;}
}
