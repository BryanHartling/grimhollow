// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.wands;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
public class WandOfBone extends WandOfMagicMissile {
    {image=ItemSpriteSheet.WAND_BONE;collisionProperties=Ballistica.STOP_TARGET|Ballistica.STOP_SOLID;}
    @Override public void onZap(Ballistica bolt){com.shatteredpixel.shatteredpixeldungeon.levels.features.BoneWalls.raise(bolt.collisionPos,5+buffedLvl());}
    @Override public int min(int level){return 0;}
    @Override public int max(int level){return 0;}
    @Override public String statsDesc(){return Messages.get(this,"stats_desc",5+buffedLvl());}
    @Override public String upgradeStat1(int level){return Integer.toString(5+level);}
}
