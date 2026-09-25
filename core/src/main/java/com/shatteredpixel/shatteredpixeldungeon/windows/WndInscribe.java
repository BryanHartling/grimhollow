// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;
import java.util.*;

/** The run's permanent inscription library, clipped and scrollable on small screens. */
public class WndInscribe extends Window {
    private ScrollPane pane;
    private int listWidth,listHeight,listTop;
    public WndInscribe(Hero hero,SigilBrush brush,Item item){
        int width=(int)Math.min(PixelScene.landscape()?190:170,PixelScene.uiCamera.width-24);
        int height=(int)Math.min(220,PixelScene.uiCamera.height-40);
        resize(width,height);
        IconTitle title=new IconTitle(new ItemSprite(item),Messages.get(SigilBrush.class,"inscribe"));
        title.setRect(0,0,width,0);add(title);
        RenderedTextBlock hint=PixelScene.renderTextBlock(Messages.get(SigilBrush.class,"known"),6);
        hint.maxWidth(width-4);hint.setPos(2,title.bottom()+4);add(hint);
        List<Class<?>> choices=new ArrayList<>(EnchanterMagic.state().choices(item));
        choices.sort(Comparator.comparing(WndInscribe::name));
        Component content=new Component();float y=0;
        for(Class<?> choice:choices){
            RedButton button=new RedButton(name(choice),7){
                @Override protected void onClick(){
                    apply(hero,brush,item,choice);
                }
            };
            button.multiline=true;button.setRect(0,y,width-26,24);content.add(button);
            IconButton info=new IconButton(Icons.get(Icons.INFO)){
                @Override protected void onClick(){showDescription(item,choice);}
            };
            info.setRect(width-26,y,22,24);content.add(info);y+=26;
        }
        if(choices.isEmpty()){
            RenderedTextBlock empty=PixelScene.renderTextBlock(Messages.get(SigilBrush.class,"no_known"),7);
            empty.maxWidth(width-4);content.add(empty);y=empty.height();
        }
        listWidth=width-4;listTop=(int)hint.bottom()+5;listHeight=height-listTop;
        content.setSize(listWidth,Math.max(listHeight,y));
        // ScrollPane owns the pointer gesture so drags cannot activate a row.
        // Its completed click arrives in content coordinates, including scroll.
        pane=new ScrollPane(content){
            @Override public void onClick(float x,float y){
                int row=(int)(y/26);
                if(x<0 || x>=listWidth || y<0 || y%26>=24 || row>=choices.size())return;
                Class<?> choice=choices.get(row);
                if(x<listWidth-22)apply(hero,brush,item,choice);
                else showDescription(item,choice);
            }
        };
        add(pane);pane.setRect(2,listTop,listWidth,listHeight);
    }
    private void apply(Hero hero,SigilBrush brush,Item item,Class<?> choice){
        if(brush.cast(hero,"inscribe",hero.pos,item,choice))hide();
        else GLog.w(Messages.get(SigilBrush.class,"inscribe_unavailable"));
    }
    private void showDescription(Item item,Class<?> choice){
        GameScene.show(new WndTitledMessage(new ItemSprite(item),name(choice),description(choice)));
    }
    public static String name(Class<?> type){Object e=Reflection.newInstance(type);return e instanceof Weapon.Enchantment?((Weapon.Enchantment)e).name():((Armor.Glyph)e).name();}
    public static String description(Class<?> type){Object e=Reflection.newInstance(type);return e instanceof Weapon.Enchantment?((Weapon.Enchantment)e).desc():((Armor.Glyph)e).desc();}
    @Override public void offset(int x,int y){super.offset(x,y);if(pane!=null)pane.setRect(2,listTop,listWidth,listHeight);}
}
