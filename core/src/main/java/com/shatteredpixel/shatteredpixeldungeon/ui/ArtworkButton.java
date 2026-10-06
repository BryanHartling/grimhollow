// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndArtwork;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import java.util.function.Supplier;

/** A transparent touch target over examination artwork; never performs a game action. */
public class ArtworkButton extends Button {
    private final Supplier<Image> artwork;
    private final Supplier<String> title;
    private Image lens;

    public ArtworkButton(Supplier<Image> artwork, Supplier<String> title) {
        this.artwork = artwork;
        this.title = title;
        lens = Icons.get(Icons.MAGNIFY);
        lens.scale.set(8f / Math.max(lens.width, lens.height));
        add(lens);
    }

    @Override protected void layout() {
        super.layout();
        if (lens != null) {
            lens.x = right() - lens.width();
            lens.y = bottom() - lens.height();
            PixelScene.align(lens);
        }
    }

    @Override protected void onPointerDown() { lens.brightness(1.4f); }
    @Override protected void onPointerUp() { lens.resetColor(); }
    @Override protected void onClick() {
        Image image = artwork.get();
        if (image != null && image.texture != null && image.width > 0 && image.height > 0) {
            Game.scene().addToFront(new WndArtwork(image, title.get()));
        }
    }
    @Override protected String hoverText() { return Messages.get(WndArtwork.class, "enlarge"); }
}
