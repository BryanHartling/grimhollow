// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;

/** Named quarry paintings inherit every animation and callback from their species. */
public final class WantedSprites {
    private WantedSprites() {}

    public static Class<? extends CharSprite> typeFor(Mob mob) {
        if (mob.bountyContract < 0 || mob.bountyContract >= 3) return mob.spriteClass;
        if (mob instanceof Guard) return mob.bountyContract == 2 ? Morcant.class : Voss.class;
        if (mob instanceof Skeleton) return Jack.class;
        if (mob instanceof Thief) return Nails.class;
        if (mob instanceof DM100) return Widowmaker.class;
        if (mob instanceof Necromancer) return BoneClerk.class;
        return mob.spriteClass;
    }

    public static class Jack extends SkeletonSprite {
        @Override protected String textureFile(){ return "sprites/wanted_jack.png"; }
    }
    public static class Nails extends ThiefSprite {
        @Override protected String textureFile(){ return "sprites/wanted_nails.png"; }
    }
    public static class Voss extends GuardSprite {
        @Override protected String textureFile(){ return "sprites/wanted_voss.png"; }
    }
    public static class Widowmaker extends DM100Sprite {
        @Override protected String textureFile(){ return "sprites/wanted_widowmaker.png"; }
    }
    public static class BoneClerk extends NecromancerSprite {
        @Override protected String textureFile(){ return "sprites/wanted_bone_clerk.png"; }
    }
    public static class Morcant extends GuardSprite {
        @Override protected String textureFile(){ return "sprites/wanted_morcant.png"; }
    }
}
