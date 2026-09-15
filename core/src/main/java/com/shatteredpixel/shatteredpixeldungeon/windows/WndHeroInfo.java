// SPDX-License-Identifier: GPL-3.0-or-later
// Based on Pixel Dungeon (C) 2012-2015 Oleg Dolya and
// Shattered Pixel Dungeon (C) 2014-2026 Evan Debenham.
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Read-only class handbook; progress badges never hide information about a choice. */
public class WndHeroInfo extends WndTabbed {
    private final ArrayList<ScrollPane> pages = new ArrayList<>();
    private final int pageWidth, pageHeight;
    public WndHeroInfo(HeroClass hero) {
        pageWidth=(int)Math.min(PixelScene.landscape()?210:180,PixelScene.uiCamera.width-24);
        int windowHeight=(int)Math.min(230,PixelScene.uiCamera.height-48);
        pageHeight=windowHeight-42;
        resize(pageWidth,windowHeight);
        HeroPortrait portrait=new HeroPortrait(hero,34);
        portrait.x=2;portrait.y=2;add(portrait);
        RenderedTextBlock title=PixelScene.renderTextBlock(Messages.titleCase(hero.title()),10);
        title.maxWidth(pageWidth-44);title.hardlight(TITLE_COLOR);title.setPos(42,4);add(title);
        RenderedTextBlock hint=PixelScene.renderTextBlock(Messages.get(this,"handbook"),6);
        hint.maxWidth(pageWidth-44);hint.setPos(42,title.bottom()+3);add(hint);
        Page profile=new Page();
        profile.text(hero.shortDesc());profile.text(hero.desc());
        if(!hero.isUnlocked())profile.text(Messages.get(HeroClass.class,hero.name()+"_unlock"));
        addPage("profile",profile);
        Page growth=new Page();growth.text(Messages.get(this,"growth_msg"));
        ArrayList<LinkedHashMap<Talent,Integer>> talents=new ArrayList<>();
        Talent.initClassTalents(hero,talents);
        growth.talents(talents.get(0),1);growth.talents(talents.get(1),2);addPage("growth",growth);
        Page paths=new Page();paths.text(Messages.get(this,"subclasses_msg"));
        for(HeroSubClass sub:hero.subClasses()){
            paths.choice(Messages.titleCase(sub.title()),()->Game.scene().addToFront(new WndInfoSubclass(hero,sub)));
            paths.text(sub.shortDesc());
            ArrayList<LinkedHashMap<Talent,Integer>> list=new ArrayList<>();
            Talent.initClassTalents(hero,list);Talent.initSubclassTalents(sub,list);
            paths.talents(list.get(2),3);
        }
        addPage("paths",paths);
        Page armor=new Page();armor.text(Messages.get(this,"abilities_msg"));
        for(ArmorAbility ability:hero.armorAbilities()){
            armor.choice(Messages.titleCase(ability.name()),()->Game.scene().addToFront(new WndInfoArmorAbility(hero,ability)));
            armor.text(ability.shortDesc());
            ArrayList<LinkedHashMap<Talent,Integer>> list=new ArrayList<>();
            Talent.initArmorTalents(ability,list);armor.talents(list.get(3),4);
        }
        addPage("armor",armor);layoutTabs();select(0);
    }
    private void addPage(String key,Page content){
        content.setSize(pageWidth-4,Math.max(pageHeight,content.cursor));
        final ScrollPane pane=new ScrollPane(content);
        pages.add(pane);add(pane);
        pane.setRect(2,42,pageWidth-4,pageHeight);
        pane.visible=pane.active=false;
        add(new LabeledTab(Messages.get(WndHeroInfo.class,key)){
            @Override protected void select(boolean value){
                super.select(value);pane.visible=pane.active=value;
            }
        });
    }
    @Override public void offset(int x,int y){
        super.offset(x,y);
        // A ScrollPane's independent camera must follow the moved window.
        if(pages!=null)for(ScrollPane pane:pages)pane.setRect(2,42,pageWidth-4,pageHeight);
    }
    private class Page extends Component{
        float cursor=2;
        void text(String value){
            RenderedTextBlock block=PixelScene.renderTextBlock(value,7);
            block.maxWidth(pageWidth-8);block.setPos(0,cursor);add(block);cursor=block.bottom()+9;
        }
        void choice(String label,Runnable action){
            RedButton button=new RedButton(label,7){
                @Override protected void onClick(){action.run();}
            };
            button.setRect(0,cursor,pageWidth-8,22);add(button);cursor=button.bottom()+5;
        }
        void talents(LinkedHashMap<Talent,Integer> values,int tier){
            if(values.isEmpty())return;
            TalentsPane.TalentTierPane pane=new TalentsPane.TalentTierPane(values,tier,TalentButton.Mode.INFO);
            pane.setRect(0,cursor,pageWidth-8,pane.height());add(pane);cursor=pane.bottom()+13;
        }
    }
}
