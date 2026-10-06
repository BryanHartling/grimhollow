// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShopkeeperSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract;
import com.watabou.noosa.Game;

public class Cole extends NPC {
    { spriteClass = ShopkeeperSprite.class; properties.add(Property.IMMOVABLE); }
    @Override public int defenseSkill(Char enemy) { return INFINITE_EVASION; }
    @Override public void damage(int damage, Object source) {}
    @Override public boolean add(Buff buff) { return false; }
    @Override public boolean reset() { return true; }
    @Override public boolean interact(Char ch) {
        if (ch == Dungeon.hero) Game.runOnRenderThread(() -> {
            BountyBoard.planContracts();
            GameScene.show(new WndOptions(sprite(), name(),
                    Messages.get(this, "greet_" + Dungeon.hero.heroClass.name())
                            + (BountyBoard.shopClosed ? "\n\n" + Messages.get(this, "closed") : ""),
                    BountyBoard.contracts[0].title(), BountyBoard.contracts[1].title(), BountyBoard.contracts[2].title()) {
                @Override protected void onSelect(int index) { GameScene.show(new WndBountyContract(BountyBoard.contracts[index])); }
            });
        });
        return true;
    }
}
