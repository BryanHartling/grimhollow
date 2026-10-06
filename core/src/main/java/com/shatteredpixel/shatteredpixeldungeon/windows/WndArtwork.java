// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;

/** Read-only art inspection, layered over its originating description window. */
public class WndArtwork extends Window {
    private static JsonValue previews;
    private Image artwork;
    private SmartTexture ownedTexture;
    private Pixmap previewKey;

    public WndArtwork(Image source, String title) {
        int w = Math.max(80, Math.min(320, (int)PixelScene.uiCamera.width - 24));
        int h = Math.max(80, Math.min(330, (int)PixelScene.uiCamera.height - 30));
        RenderedTextBlock heading = PixelScene.renderTextBlock(title, 9);
        heading.hardlight(TITLE_COLOR);
        heading.maxWidth(w - 8);
        heading.setPos((w - heading.width()) / 2, 2);
        add(heading);

        String file = previewFile(source);
        if (file != null) {
            // Only the open preview occupies GPU memory. Do not retain a gallery
            // of large images in the shared gameplay texture cache on tablets.
            previewKey = new Pixmap(Gdx.files.internal(file));
            ownedTexture = TextureCache.get(previewKey);
            ownedTexture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
            artwork = new Image(ownedTexture);
        } else {
            // Construct independently: Image.copy shares its scale vector.
            // Opening a preview must never resize or retint the source sprite.
            artwork = new Image(source.texture);
        }
        artwork.flipHorizontal = source.flipHorizontal;
        artwork.flipVertical = source.flipVertical;
        artwork.frame(file != null ? new RectF(0, 0, 1, 1) : source.frame());
        artwork.rm = source.rm; artwork.gm = source.gm; artwork.bm = source.bm;
        artwork.ra = source.ra; artwork.ga = source.ga; artwork.ba = source.ba;
        float top = heading.bottom() + 8;
        float space = Math.max(20, h - top - 29);
        artwork.scale.set(Math.min((w - 16) / artwork.width, space / artwork.height));
        artwork.x = (w - artwork.width()) / 2;
        artwork.y = top + (space - artwork.height()) / 2;
        add(artwork);

        RedButton close = new RedButton(Messages.get(this, "close")) {
            @Override protected void onClick() { hide(); }
        };
        close.setRect(0, h - 21, w, 20);
        add(close);
        resize(w, h);
    }

    private static String previewFile(Image source) {
        Object key = TextureCache.sourceOf(source.texture);
        if (!(key instanceof String)) return null;
        if (previews == null) previews = new JsonReader().parse(Gdx.files.internal("artwork-previews.json"));
        JsonValue entries = previews.get((String)key);
        if (entries == null) return null;
        RectF frame = source.frame();
        float l = frame.left * source.texture.width, t = frame.top * source.texture.height;
        float r = frame.right * source.texture.width, b = frame.bottom * source.texture.height;
        for (JsonValue entry = entries.child; entry != null; entry = entry.next) {
            int[] rect = entry.get("rect").asIntArray();
            if (l >= rect[0] - .1f && t >= rect[1] - .1f && r <= rect[2] + .1f && b <= rect[3] + .1f) {
                return entry.getString("file");
            }
        }
        return null;
    }

    @Override public void destroy() {
        super.destroy();
        if (ownedTexture != null) {
            TextureCache.remove(previewKey);
            ownedTexture = null;
            previewKey = null;
        }
    }
}
