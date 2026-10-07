// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.GL20;
import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.ui.Component;

/** A saved quest clock, not a timed buff. Each notice opens its own contract. */
public class BountyClockIndicator extends Component {
    private final StyledButton[] rows = new StyledButton[2];
    private final RenderedTextBlock heading;

    public BountyClockIndicator() {
        heading = PixelScene.renderTextBlock(6);
        heading.text(Messages.get(Cole.class, "urgent"));
        heading.hardlight(0xE5BC79);
        add(heading);
        for (int i = 0; i < rows.length; i++) {
            final int index = i;
            rows[i] = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "", 6) {
                @Override protected void onClick() {
                    BountyBoard.Contract c = BountyBoard.contracts[index];
                    if (c != null && c.urgencyRunning()) GameScene.show(new WndBountyContract(c));
                }
                @Override protected String hoverText() {
                    BountyBoard.Contract c = BountyBoard.contracts[index];
                    return c == null ? null : c.alias() + "\n" + BountyBoard.urgency(c);
                }
            };
            Image seal = new Image("interfaces/bounty_poster_seals.png");
            seal.frame(new TextureFilm(seal.texture, 256, 256).get(i));
            seal.texture.filter(GL20.GL_LINEAR, GL20.GL_LINEAR);
            seal.logicalSize(12, 12);
            rows[i].icon(seal);
            rows[i].visible = false;
            add(rows[i]);
        }
        width = 88;
        visible = false;
    }

    @Override public void update() {
        super.update();
        boolean changed = false, any = false;
        for (int i = 0; i < rows.length; i++) {
            BountyBoard.Contract c = BountyBoard.contracts[i];
            boolean shown = BountyBoard.present && c != null && c.urgencyRunning();
            changed |= rows[i].visible != shown;
            rows[i].visible = rows[i].active = shown;
            if (!shown) continue;
            any = true;
            String rarity = Messages.get(Cole.class, "poster_rarity_" + i);
            String label = c.urgencyExpired() ? Messages.get(Cole.class, "clock_hud_expired", rarity)
                    : Messages.get(Cole.class, c.turnsRemaining() == 1 ? "clock_hud_one" : "clock_hud", rarity, c.turnsRemaining());
            if (!label.equals(rows[i].text())) rows[i].text(label);
            rows[i].textColor(c.urgencyExpired() ? 0xD48B82 : c.turnsRemaining() <= 50 ? 0xFFE09A : 0xECE0CB);
        }
        visible = any;
        if (changed) layout();
    }

    @Override protected void layout() {
        heading.setPos(x + width - heading.width(), y);
        PixelScene.align(heading);
        float next = heading.bottom() + 2;
        for (StyledButton row : rows) if (row.visible) {
            row.setRect(x, next, width, 18);
            next = row.bottom() + 1;
        }
        height = next - y;
    }
}
