// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Image;

/** The same known identity emblem used by inventory, examination and artwork. */
public final class ItemIdentityIcon extends Image {
    public ItemIdentityIcon(int index, float size) {
        super(Assets.Sprites.ITEM_ICONS);
        frame(ItemSpriteSheet.Icons.film.get(index));
        logicalSize(size, size);
        texture.filter(com.badlogic.gdx.graphics.GL20.GL_LINEAR, com.badlogic.gdx.graphics.GL20.GL_LINEAR);
    }
    public static int index(Item item) {
        return item != null && item.icon >= 0 && (item.isIdentified()
                || item instanceof Ring && ((Ring)item).isKnown()) ? item.icon : -1;
    }
}
