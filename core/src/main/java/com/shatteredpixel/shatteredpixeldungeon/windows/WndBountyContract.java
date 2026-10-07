// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.ui.Component;

/** A scrollable paper contract with a fixed acceptance button. */
public class WndBountyContract extends Window {
    private ScrollPane paper;
    private int paperWidth,paperHeight;
    public WndBountyContract(BountyBoard.Contract c) {
        int w=Math.min(180,(int)PixelScene.uiCamera.width-28);
        int h=Math.min(265,(int)PixelScene.uiCamera.height-40);
        Image background=new Image("interfaces/bounty_parchment.png");
        background.logicalSize(w,h);add(background);
        Component content=new Component();
        RenderedTextBlock heading=PixelScene.renderTextBlock(Messages.get(Cole.class,"poster_heading"),10);
        heading.hardlight(0x3F2115);heading.maxWidth(w-28);
        heading.setPos((w-heading.width())/2,9);content.add(heading);
        Image target=poster(c);target.logicalSize(70,70);
        target.x=(w-target.width())/2;target.y=heading.bottom()+4;content.add(target);
        RenderedTextBlock body=PixelScene.renderTextBlock(7);
        body.setHightlighting(false);body.hardlight(0x30241B);body.text((c.title()+"\n\n"+text(c)).replace("_",""),w-30);
        body.setPos(15,target.y+target.height()+5);content.add(body);
        Image seal=new Image("interfaces/bounty_seals.png");
        seal.frame(new TextureFilm(seal.texture,64,64).get(Math.min(2,c.index)));
        seal.logicalSize(24,24);seal.x=(w-seal.width())/2;seal.y=body.bottom()+5;content.add(seal);
        if(c.complete){
            Image stamp=Icons.get(Icons.CHECKED);stamp.logicalSize(13,13);
            stamp.x=seal.x+seal.width()+3;stamp.y=seal.y+6;stamp.angle=-12;content.add(stamp);
        }
        content.setSize(w,seal.y+seal.height()+10);
        ArtworkButton enlarge=new ArtworkButton(()->target,()->c.title());
        enlarge.setRect(target.x,target.y,target.width(),target.height());content.add(enlarge);
        paperWidth=w;paperHeight=h-29;paper=new ScrollPane(content);add(paper);
        RedButton action=new RedButton(Messages.get(Cole.class,c.complete?"return":"accept")){
            @Override protected void onClick(){hide();if(c.complete)BountyBoard.returnClaim(c.index);else BountyBoard.accept(c.index);}
        };
        action.enable(!BountyBoard.betrayed && !c.accepted || c.index<3 && c.complete && !c.returned && com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(c.index));
        action.setRect(9,h-26,w-18,20);add(action);resize(w,h);
    }
    @Override public void resize(int w,int h){super.resize(w,h);alignPaper();}
    @Override public void offset(int x,int y){super.offset(x,y);alignPaper();}
    private void alignPaper(){if(paper!=null)paper.setRect(0,0,paperWidth,paperHeight);}
    public static Image poster(BountyBoard.Contract c){
        Image image=new Image("interfaces/bounty_posters.png");
        image.frame(new TextureFilm(image.texture,256,256).get(c.species));image.logicalSize(28,28);return image;
    }
    public static String text(BountyBoard.Contract c) {
        String text=Messages.get(Cole.class,"poster",c.alias(),Messages.get(Cole.class,c.index==3?"boss_flavor":"flavor_"+(c.index==2?"warden":c.species)),c.floor-5,
                c.index==2?Messages.get(Cole.class,"coat_prize"):Messages.get(Cole.class,"gold_prize",c.payment));
        if(c.index<2)text+="\n\n"+Messages.get(Cole.class,"urgent")+"\n"+Messages.get(Cole.class,"swift");
        text+="\n\n"+Messages.get(Cole.class,c.returned?"returned":c.complete?"complete":c.accepted?"accepted":"offered");
        return text;
    }
}
