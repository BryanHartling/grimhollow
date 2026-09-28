// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import java.util.ArrayList;

public class ExpeditionMap extends Item {
    public static final String OPEN = "OPEN";
    { image = ItemSpriteSheet.EXPEDITION_MAP; unique = true; defaultAction = OPEN; }
    @Override public boolean isUpgradable() { return false; }
    @Override public boolean isIdentified() { return true; }
    @Override public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero); actions.add(OPEN); return actions;
    }
    @Override public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (!OPEN.equals(action)) return;
        if (!DragonExpedition.canEnter(hero)) { GLog.w(Messages.get(this, "at_hunter")); return; }
        GameScene.show(new WndOptions(name(), Messages.get(this, "warning"),
                Messages.get(this, "enter"), Messages.get(this, "stay")) {
            @Override protected void onSelect(int index) { if (index == 0) DragonExpedition.enter(hero); }
        });
    }
}
