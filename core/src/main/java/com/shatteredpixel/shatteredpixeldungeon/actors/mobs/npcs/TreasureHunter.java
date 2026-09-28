// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WandmakerSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;

public class TreasureHunter extends NPC {
    { spriteClass = WandmakerSprite.class; properties.add(Property.IMMOVABLE); }
    @Override public int defenseSkill(Char enemy) { return INFINITE_EVASION; }
    @Override public void damage(int damage, Object source) {}
    @Override public boolean add(Buff buff) { return false; }
    @Override public boolean reset() { return true; }
    @Override public boolean interact(Char ch) {
        if (ch != Dungeon.hero) return true;
        Game.runOnRenderThread(() -> {
            if (DragonExpedition.accepted) {
                GameScene.show(new WndQuest(this, Messages.get(this, "healed")));
            } else {
                GameScene.show(new WndOptions(sprite(), name(), Messages.get(this, "request"),
                        Messages.get(this, "give"), Messages.get(this, "leave")) {
                    @Override protected void onSelect(int index) {
                        if (index != 0) return;
                        boolean healed = DragonExpedition.accept(Dungeon.hero);
                        GameScene.show(new WndQuest(TreasureHunter.this,
                                Messages.get(TreasureHunter.this, healed ? "healed" : "no_potion")));
                        if (healed) Dungeon.hero.spendAndNext(1f);
                    }
                });
            }
        });
        return true;
    }
}
