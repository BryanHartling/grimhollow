// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.graphics.GL20;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.WantedPoster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.Image;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

/** The same immutable personal notice can be read in inventory or the run's deeds archive. */
public class WndHeroWantedPoster extends Window {
    private final Component content=new Component();
    private ScrollPane paper;
    private int paperWidth,paperHeight;
    private RenderedTextBlock heading,title,subclass,flavorTitle,flavor,reward;
    private Image portrait,seal;
    private ColorBlock signatureLine;
    public WndHeroWantedPoster(HeroClass hero,String name,int bounty){
        this(hero,HeroSubClass.NONE,name,bounty);
    }
    public WndHeroWantedPoster(HeroClass hero,HeroSubClass specialization,String name,int bounty){
        final String subjectName=name==null||name.trim().isEmpty()?hero.title():name;
        int w=Math.min(210,(int)PixelScene.uiCamera.width-28),h=Math.min(405,(int)PixelScene.uiCamera.height-36);
        Image background=new Image("interfaces/bounty_parchment.png");
        background.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);background.logicalSize(w,h);add(background);
        heading=ink(Messages.get(Cole.class,"poster_heading"),26,w,10);
        title=ink(subjectName,14,w,heading.bottom()+4);
        portrait=new Image(hero.splashArt());
        portrait.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
        float portraitWidth=Math.min(144,w-48);
        portrait.logicalSize(portraitWidth,portraitWidth*portrait.height/portrait.width);
        portrait.x=(w-portrait.width())/2;portrait.y=title.bottom()+6;content.add(portrait);
        float bottom=portrait.y+portrait.height()+8;
        if(specialization!=null && specialization!=HeroSubClass.NONE){
            subclass=ink(specialization.title(),12,w,bottom);bottom=subclass.bottom()+7;
        }
        flavorTitle=ink(WantedPoster.headline(hero),11,w,bottom);
        flavor=ink(WantedPoster.flavor(hero),9,w,flavorTitle.bottom()+5);
        reward=ink(Messages.get(Cole.class,"poster_bounty",bounty),14,w,flavor.bottom()+11);
        RenderedTextBlock signature=ink(Messages.get(Cole.class,"hero_poster_signature"),8,w,reward.bottom()+10);
        signatureLine=new ColorBlock(92,.5f,0x805A3D2C);
        signatureLine.x=(w-signatureLine.width())/2;signatureLine.y=signature.bottom()+4;content.add(signatureLine);
        seal=new Image("interfaces/bounty_poster_seals.png");
        seal.frame(new TextureFilm(seal.texture,256,256).get(2));seal.logicalSize(80,80);
        seal.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);seal.x=(w-80)/2;seal.y=signatureLine.y+5;content.add(seal);
        content.setSize(w,seal.y+seal.height()+10);
        ArtworkButton enlarge=new ArtworkButton(()->portrait,()->subjectName);enlarge.setRect(portrait.x,portrait.y,portrait.width(),portrait.height());content.add(enlarge);
        paperWidth=w;paperHeight=h-31;paper=new ScrollPane(content);add(paper);
        RedButton close=new RedButton(Messages.get(com.shatteredpixel.shatteredpixeldungeon.RunDeeds.class,"close")){
            @Override protected void onClick(){hide();}
        };close.setRect(9,h-27,w-18,21);add(close);resize(w,h);
    }
    private RenderedTextBlock ink(String text,int size,int width,float y){
        int scale=Math.max(1,Math.round(PixelScene.defaultZoom*DeviceCompat.getRealPixelScaleX()));
        RenderedTextBlock block=new RenderedTextBlock(size*scale){
            @Override protected RenderedText createWord(String word,int pixels){return new RenderedText(word,pixels,false);}
            @Override protected float spaceWidth(){return size*.28f;}
            @Override protected float wordOverlap(){return 0;}
        };
        block.zoom(1f/scale);block.setHightlighting(false);block.hardlight(0x302218);
        block.text(text.replace("_",""),width-38);block.align(RenderedTextBlock.CENTER_ALIGN);
        block.setPos((width-block.width())/2,y);PixelScene.align(block);content.add(block);return block;
    }
    @Override public void resize(int w,int h){super.resize(w,h);alignPaper();}
    @Override public void offset(int x,int y){super.offset(x,y);alignPaper();}
    private void alignPaper(){if(paper!=null)paper.setRect(0,0,paperWidth,paperHeight);}
}
