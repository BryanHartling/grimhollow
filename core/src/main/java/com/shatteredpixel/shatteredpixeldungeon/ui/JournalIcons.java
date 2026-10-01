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
    /** Notes and floor feelings share one painted source in lists and popups. */
    public static Image landmark(Icons type) {
        int index;
        switch (type) {
            case STAIRS: index=0; break;
            case STAIRS_CHASM: index=1; break;
            case STAIRS_WATER: index=2; break;
            case STAIRS_GRASS: index=3; break;
            case STAIRS_DARK: index=4; break;
            case STAIRS_LARGE: index=5; break;
            case STAIRS_TRAPS: index=6; break;
            case STAIRS_SECRETS: index=7; break;
            case GRASS: index=8; break;
            case DISTANT_WELL: index=9; break;
            case WELL_HEALTH: index=10; break;
            case WELL_AWARENESS: index=11; break;
            case SACRIFICE_ALTAR: index=12; break;
            case ALCHEMY: index=13; break;
            case BACKPACK_LRG: index=14; break;
            case SCROLL_COLOR: case SCROLL_GREY: index=15; break;
            default: return null;
        }
        Image icon=new Image("interfaces/painted_landmarks.png");
        icon.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
        icon.frame(index%4*64,index/4*64,64,64);
        icon.logicalSize(16,16);
        return icon;
    }
    public static Image treasury(com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.Kind kind) {
        Image icon=new Image("environment/custom_tiles/elemental_cache.png");
        icon.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
        icon.frame(kind.ordinal()*64,0,64,64);
        icon.logicalSize(16,16);
        return icon;
    }
    public static Image get(int index) {
        Image icon = new Image("interfaces/painted_journal.png");
        icon.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
        icon.frame(index%4*64, index/4*64, 64, 64);
        icon.logicalSize(16, 16);
        return icon;
    }
}
