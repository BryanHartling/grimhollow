// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroWantedPoster;
import com.watabou.utils.Bundle;
import java.util.ArrayList;

/** A personal keepsake of Cole's betrayal, independent of later hunter Warrants. */
public class WantedPoster extends Item {
    public static final String READ="READ";
    public HeroClass subject=HeroClass.WARRIOR;
    public HeroSubClass subjectSubclass=HeroSubClass.NONE;
    public String subjectName="";
    public int bounty;
    public WantedPoster(){unique=true;image=ItemSpriteSheet.WARRANT;defaultAction=READ;bones=false;}
    public WantedPoster(HeroClass subject,String name,int bounty){this(subject,HeroSubClass.NONE,name,bounty);}
    public WantedPoster(HeroClass subject,HeroSubClass subclass,String name,int bounty){this();this.subject=subject;subjectSubclass=subclass==null?HeroSubClass.NONE:subclass;subjectName=name;this.bounty=bounty;}
    public String displayName(){return subjectName==null||subjectName.trim().isEmpty()?subject.title():subjectName;}
    public static String headline(HeroClass subject){return Messages.get(Cole.class,"hero_poster_title_"+subject.name());}
    public static String flavor(HeroClass subject){return Messages.get(Cole.class,"hero_poster_flavor_"+subject.name());}
    public static HeroSubClass legacySubclass(HeroClass subject,String name){
        if(subject!=null)for(HeroSubClass subclass:subject.subClasses())if(subclass.title().equals(name))return subclass;
        return HeroSubClass.NONE;
    }
    @Override public boolean isUpgradable(){return false;}
    @Override public boolean isIdentified(){return true;}
    @Override public int value(){return 0;}
    @Override public String info(){return Messages.get(this,"desc")+"\n\n"+displayName()
            +(subjectSubclass==HeroSubClass.NONE?"":"\n"+subjectSubclass.title())+"\n\n"+headline(subject)+"\n"+flavor(subject)
            +"\n\n"+Messages.get(Cole.class,"poster_bounty",bounty);}
    @Override public ArrayList<String> actions(Hero hero){ArrayList<String> actions=super.actions(hero);actions.remove(AC_THROW);actions.add(READ);return actions;}
    @Override public void execute(Hero hero,String action){
        super.execute(hero,action);
        if(READ.equals(action))GameScene.show(new WndHeroWantedPoster(subject,subjectSubclass,displayName(),bounty));
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("subject",subject);b.put("subject_subclass",subjectSubclass);b.put("subject_name",subjectName);b.put("posted_bounty",bounty);}
    @Override public void restoreFromBundle(Bundle b){
        super.restoreFromBundle(b);subject=b.getEnum("subject",HeroClass.class);subjectName=b.getString("subject_name");bounty=b.getInt("posted_bounty");
        subjectSubclass=b.contains("subject_subclass")?b.getEnum("subject_subclass",HeroSubClass.class):legacySubclass(subject,subjectName);
        if(!b.contains("subject_subclass") && subjectSubclass!=HeroSubClass.NONE)subjectName=subject.title();
    }
}
