// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;

/** Accepting the payment opens the separate betrayal conversation. Back leaves it unclaimed. */
public class WndBountyPayment extends WndTitledMessage {
    public WndBountyPayment(Cole cole){
        super(cole.sprite(),cole.name(),Messages.get(Cole.class,"boss_paid",BountyBoard.contracts[3].payment));
        RedButton accept=new RedButton(Messages.get(Cole.class,"accept_bounty")){
            @Override protected void onClick(){
                hide();
                if(BountyBoard.beginBetrayal())GameScene.show(new WndBountyBetrayal(cole));
            }
        };
        accept.setRect(0,height+2,width,20);add(accept);resize(width,height+22);
    }
    @Override protected float targetHeight(){return Math.min(super.targetHeight(),PixelScene.uiCamera.height-74);}
    @Override public void hide(){super.hide();BountyBoard.closeExitConversation();}
}
