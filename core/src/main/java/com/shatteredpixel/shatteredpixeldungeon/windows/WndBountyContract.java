// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;

public class WndBountyContract extends WndTitledMessage {
    public WndBountyContract(BountyBoard.Contract c) {
        super(c.preview().sprite(), c.title(), text(c));
        RedButton action = new RedButton(Messages.get(Cole.class, c.complete ? "return" : "accept")) {
            @Override protected void onClick() {
                hide(); if (c.complete) BountyBoard.returnClaim(c.index); else BountyBoard.accept(c.index);
            }
        };
        action.enable(!BountyBoard.betrayed && !c.accepted || c.index<3 && c.complete && !c.returned && com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(c.index));
        action.setRect(0, height+2, width, 20); add(action); resize(width, height+22);
    }
    @Override protected float targetHeight() { return Math.min(super.targetHeight(), PixelScene.uiCamera.height-74); }
    public static String text(BountyBoard.Contract c) {
        String text = Messages.get(Cole.class, "poster", c.alias(), Messages.get(Cole.class, c.index==3 ? "boss_flavor" : "flavor_"+(c.index == 2 ? "warden" : c.species)), c.floor-5,
                c.index == 2 ? Messages.get(Cole.class, "coat_prize") : Messages.get(Cole.class, "gold_prize", c.payment));
        if (c.index < 2) text += "\n\n_"+Messages.get(Cole.class, "urgent")+"_\n"+Messages.get(Cole.class, "swift");
        text += "\n\n"+Messages.get(Cole.class, c.returned ? "returned" : c.complete ? "complete" : c.accepted ? "accepted" : "offered");
        return text;
    }
}
