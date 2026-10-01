// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.GL20;
import com.watabou.noosa.Image;

/** Painted navigation and journal categories, at the existing logical UI size. */
public final class JournalIcons {
    public static final int JOURNAL=0, MENU=1, GUIDE=2, NOTES=3, ALCHEMY=4, BESTIARY=5,
            LORE=6, EQUIPMENT=7, CONSUMABLES=8, SEEDS=9, STONES=10, ELIXIRS=11,
            SPELLS=12, FOOD=13, BOMBS=14, MISSILES=15;
    private JournalIcons() {}
    public static Image get(int index) {
        Image icon = new Image("interfaces/painted_journal.png");
        icon.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
        icon.frame(index%4*64, index/4*64, 64, 64);
        icon.logicalSize(16, 16);
        return icon;
    }
}
