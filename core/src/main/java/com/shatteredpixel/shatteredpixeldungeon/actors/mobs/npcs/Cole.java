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
            if(BountyBoard.betrayed){BountyBoard.showMeeting(Cole.this,Dungeon.depth==22);return;}
            BountyBoard.planContracts();
            java.util.ArrayList<String> options=new java.util.ArrayList<>();
            for(int i=0;i<3;i++)options.add(BountyBoard.contracts[i].title());
            if(BountyBoard.bossUnlocked()&&!BountyBoard.bossDefeated){BountyBoard.planBoss();options.add(BountyBoard.contracts[3].title());}
            GameScene.show(new WndOptions(sprite(), name(),
                    Messages.get(this, "greet_" + Dungeon.hero.heroClass.name())
                            + (BountyBoard.shopClosed ? "\n\n" + Messages.get(this, "closed") : ""),
                    options.toArray(new String[0])) {
                @Override protected void onSelect(int index) { GameScene.show(new WndBountyContract(BountyBoard.contracts[index])); }
            });
        });
        return true;
    }
}
