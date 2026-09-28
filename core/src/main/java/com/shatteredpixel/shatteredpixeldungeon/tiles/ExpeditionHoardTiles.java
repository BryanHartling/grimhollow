// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.DragonExpedition;
import com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Tilemap;

/** Scenery is visible before victory, but cannot be collected, burned or pulled. */
public class ExpeditionHoardTiles extends CustomTilemap {
    { texture = "environment/custom_tiles/expedition_hoard.png"; }
    public ExpeditionHoardTiles() { setRect(9, 5, 7, 4); }
    @Override public Tilemap create() {
        Tilemap map = super.create(); map.map(mapSimpleImage(0, 0, 128), tileW); return map;
    }
    @Override public String name(int x, int y) {
        return Messages.get(DragonHoardLevel.class, DragonExpedition.dragonSlain ? "treasure_open_name" : "treasure_name");
    }
    @Override public String desc(int x, int y) {
        return Messages.get(DragonHoardLevel.class, DragonExpedition.dragonSlain ? "unsealed" : "sealed");
    }
}
