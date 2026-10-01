// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.BlankParchment;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;
import java.util.List;

public class WndScribe extends Window {
    private ScrollPane pane;
    private int listWidth,listHeight,listTop;
    public WndScribe(Hero hero){
        int width=(int)Math.min(PixelScene.landscape()?210:170,PixelScene.uiCamera.width-24);
        int height=(int)Math.min(220,PixelScene.uiCamera.height-40);resize(width,height);
        IconTitle title=new IconTitle(new ItemSprite(new BlankParchment()),Messages.get(BlankParchment.class,"ac_scribe"));
        title.setRect(0,0,width,0);add(title);
        RenderedTextBlock hint=PixelScene.renderTextBlock(Messages.get(BlankParchment.class,"choose",Dungeon.energy),6);
        hint.maxWidth(width-4);hint.setPos(2,title.bottom()+4);add(hint);
        List<Class<? extends Scroll>> choices=BlankParchment.recipes();
        Component content=new Component();float y=0;
        for(Class<? extends Scroll> choice:choices){
            Scroll output=Reflection.newInstance(choice);
            RedButton button=new RedButton(output.name()+" — "+BlankParchment.cost(choice),6){
                @Override protected void onClick(){apply(hero,choice);}
            };
            button.multiline=true;button.icon(new ItemSprite(output));button.setRect(0,y,width-26,28);
            button.enable(BlankParchment.canScribe(hero,choice));content.add(button);
            IconButton info=new IconButton(Icons.INFO.get()){
                @Override protected void onClick(){GameScene.show(new WndInfoItem(Reflection.newInstance(choice)));}
            };
            info.setRect(width-26,y,22,28);content.add(info);y+=30;
        }
        if(choices.isEmpty()){
            RenderedTextBlock empty=PixelScene.renderTextBlock(Messages.get(BlankParchment.class,"empty"),7);
            empty.maxWidth(width-4);content.add(empty);y=empty.height();
        }
        listWidth=width-4;listTop=(int)hint.bottom()+5;listHeight=height-listTop;
        content.setSize(listWidth,Math.max(listHeight,y));
        pane=new ScrollPane(content){
            @Override public void onClick(float x,float y){
                int row=(int)(y/30);
                if(x<0||x>=listWidth||y<0||y%30>=28||row>=choices.size())return;
                Class<? extends Scroll> choice=choices.get(row);
                if(x<listWidth-22)apply(hero,choice);
                else GameScene.show(new WndInfoItem(Reflection.newInstance(choice)));
            }
        };
        add(pane);pane.setRect(2,listTop,listWidth,listHeight);
    }
    private void apply(Hero hero,Class<? extends Scroll> choice){
        if(BlankParchment.scribe(hero,choice))hide();else GLog.w(Messages.get(BlankParchment.class,"unavailable"));
    }
    @Override public void offset(int x,int y){super.offset(x,y);if(pane!=null)pane.setRect(2,listTop,listWidth,listHeight);}
}
