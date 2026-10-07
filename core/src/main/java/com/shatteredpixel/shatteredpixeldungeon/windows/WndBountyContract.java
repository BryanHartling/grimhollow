// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.graphics.GL20;
import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

/** A wanted notice in centered ink, with the contract action outside its scroll area. */
public class WndBountyContract extends Window {
    private ScrollPane paper;
    private int paperWidth, paperHeight;
    private Component content;
    private RenderedTextBlock heading, quarry, flavorTitle, flavor, location, reward, urgency, signature;
    private Image target, seal;

    public WndBountyContract(BountyBoard.Contract c) {
        int w = Math.min(210, (int) PixelScene.uiCamera.width - 28);
        int h = Math.min(405, (int) PixelScene.uiCamera.height - 36);
        Image background = new Image("interfaces/bounty_parchment.png");
        background.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
        background.logicalSize(w, h);
        add(background);
        content = new Component();

        heading = ink(Messages.get(Cole.class, "poster_heading"), 26, w, 10);
        quarry = ink(c.alias(), 11, w, heading.bottom() + 4);
        target = poster(c);
        float portraitSize = Math.min(112, w - 48);
        target.logicalSize(portraitSize, portraitSize);
        target.x = (w - target.width()) / 2;
        target.y = quarry.bottom() + 3;
        PixelScene.align(target);
        content.add(target);

        flavorTitle = ink(c.title(), 10, w, target.y + target.height() + 4);
        flavor = ink(flavor(c), 8, w, flavorTitle.bottom() + 4);
        location = ink(Messages.get(Cole.class, "poster_location", c.floor - 5), 8, w, flavor.bottom() + 8);
        reward = ink(c.index == 2 ? Messages.get(Cole.class, "coat_prize")
                : Messages.get(Cole.class, "poster_bounty", c.payment), 11, w, location.bottom() + 8);
        float bottom = reward.bottom();
        if (c.index < 2) {
            urgency = ink(Messages.get(Cole.class, "urgent"), 9, w, bottom + 8);
            RenderedTextBlock swift = ink(Messages.get(Cole.class, "swift"), 8, w, urgency.bottom() + 3);
            bottom = swift.bottom();
        }
        signature = ink(Messages.get(Cole.class, c.returned ? "returned" : c.complete ? "complete"
                : c.accepted ? "accepted" : "offered"), 8, w, bottom + 9);
        ColorBlock signatureLine = new ColorBlock(92, .5f, 0x805A3D2C);
        signatureLine.x = (w - signatureLine.width()) / 2;
        signatureLine.y = signature.bottom() + 4;
        content.add(signatureLine);

        seal = new Image("interfaces/bounty_poster_seals.png");
        seal.frame(new TextureFilm(seal.texture, 256, 256).get(Math.min(2, c.index)));
        seal.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
        seal.logicalSize(80, 80);
        seal.x = (w - seal.width()) / 2;
        seal.y = signatureLine.y + 4;
        PixelScene.align(seal);
        content.add(seal);
        int rarity = Math.min(2, c.index);
        ink(Messages.get(Cole.class, "poster_rarity_" + rarity), rarity == 2 ? 5 : 6, w, seal.y + 46.5f);
        if (c.complete) {
            Image stamp = Icons.get(Icons.CHECKED);
            stamp.logicalSize(15, 15);
            stamp.x = seal.x + seal.width() + 2;
            stamp.y = seal.y + 22;
            stamp.angle = -12;
            content.add(stamp);
        }
        content.setSize(w, seal.y + seal.height() + 10);
        ArtworkButton enlarge = new ArtworkButton(() -> target, () -> c.alias());
        enlarge.setRect(target.x, target.y, target.width(), target.height());
        content.add(enlarge);

        paperWidth = w;
        paperHeight = h - 31;
        paper = new ScrollPane(content);
        add(paper);
        RedButton action = new RedButton(Messages.get(Cole.class, c.complete ? "return" : "accept")) {
            @Override protected void onClick() {
                hide();
                if (c.complete) BountyBoard.returnClaim(c.index);
                else BountyBoard.accept(c.index);
            }
        };
        action.enable(!BountyBoard.betrayed && !c.accepted || c.index < 3 && c.complete && !c.returned
                && com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(c.index));
        action.setRect(9, h - 27, w - 18, 21);
        add(action);
        resize(w, h);
    }

    private RenderedTextBlock ink(String text, int size, int width, float y) {
        int scale = Math.round(PixelScene.defaultZoom * DeviceCompat.getRealPixelScaleX());
        RenderedTextBlock block = new RenderedTextBlock(size * scale) {
            @Override protected RenderedText createWord(String word, int pixels) {
                return new RenderedText(word, pixels, false);
            }
            @Override protected float spaceWidth() { return size * .28f; }
            @Override protected float wordOverlap() { return 0; }
        };
        block.zoom(1f / scale);
        block.setHightlighting(false);
        block.hardlight(0x302218);
        block.text(text.replace("_", ""), width - 38);
        block.align(RenderedTextBlock.CENTER_ALIGN);
        block.setPos((width - block.width()) / 2, y);
        PixelScene.align(block);
        content.add(block);
        return block;
    }

    @Override public void resize(int w, int h) { super.resize(w, h); alignPaper(); }
    @Override public void offset(int x, int y) { super.offset(x, y); alignPaper(); }
    private void alignPaper() { if (paper != null) paper.setRect(0, 0, paperWidth, paperHeight); }

    public static Image poster(BountyBoard.Contract c) {
        Image image = new Image("interfaces/bounty_posters.png");
        image.frame(new TextureFilm(image.texture, 512, 512).get(c.index == 2 ? 7 : c.species));
        image.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
        image.logicalSize(28, 28);
        return image;
    }

    private static String flavor(BountyBoard.Contract c) {
        return Messages.get(Cole.class, c.index == 3 ? "boss_flavor"
                : "flavor_" + (c.index == 2 ? "warden" : c.species));
    }

    public static String text(BountyBoard.Contract c) {
        String text = Messages.get(Cole.class, "poster", c.alias(), flavor(c), c.floor - 5,
                c.index == 2 ? Messages.get(Cole.class, "coat_prize") : Messages.get(Cole.class, "gold_prize", c.payment));
        if (c.index < 2) text += "\n\n" + Messages.get(Cole.class, "urgent") + "\n" + Messages.get(Cole.class, "swift");
        text += "\n\n" + Messages.get(Cole.class, c.returned ? "returned" : c.complete ? "complete" : c.accepted ? "accepted" : "offered");
        return text;
    }
}
