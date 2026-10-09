// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroPortrait;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.RunDeeds;
public class WndBountyBetrayal extends WndTitledMessage {
    public WndBountyBetrayal(Cole cole){
        super(new HeroPortrait(Dungeon.hero.heroClass,24),Messages.get(Cole.class,"wanted_title",Dungeon.hero.name()),
                Messages.get(Cole.class,"betray_"+Dungeon.hero.heroClass.name())+"\n\n"+Messages.get(Cole.class,"hands_bounty"));
        BountyBoard.holdExitConversation();
        RedButton done=new RedButton(Messages.get(Cole.class,"view_bounty")){
            @Override protected void onClick(){
                hide();BountyBoard.issueWantedPoster();RunDeeds record=RunDeeds.capture();
                GameScene.show(new WndHeroWantedPoster(record.wantedClass,record.wantedName,record.wantedBounty){
                    @Override public void hide(){super.hide();BountyBoard.finishDeparture();}
                });
            }
        };done.setRect(0,height+2,width,20);add(done);resize(width,height+22);
    }
    @Override protected float targetHeight(){return Math.min(super.targetHeight(),PixelScene.uiCamera.height-74);}
    @Override public void hide(){super.hide();BountyBoard.closeExitConversation();}
}
