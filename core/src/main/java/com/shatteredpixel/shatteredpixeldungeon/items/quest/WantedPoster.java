// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
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
    public String subjectName="";
    public int bounty;
    public WantedPoster(){unique=true;image=ItemSpriteSheet.WARRANT;defaultAction=READ;bones=false;}
    public WantedPoster(HeroClass subject,String name,int bounty){this();this.subject=subject;subjectName=name;this.bounty=bounty;}
    @Override public boolean isUpgradable(){return false;}
    @Override public boolean isIdentified(){return true;}
    @Override public int value(){return 0;}
    @Override public String info(){return Messages.get(this,"desc")+"\n\n"+Messages.get(Cole.class,"hero_poster",subjectName,subject.title(),bounty);}
    @Override public ArrayList<String> actions(Hero hero){ArrayList<String> actions=super.actions(hero);actions.remove(AC_THROW);actions.add(READ);return actions;}
    @Override public void execute(Hero hero,String action){
        super.execute(hero,action);
        if(READ.equals(action))GameScene.show(new WndHeroWantedPoster(subject,subjectName,bounty));
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("subject",subject);b.put("subject_name",subjectName);b.put("posted_bounty",bounty);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);subject=b.getEnum("subject",HeroClass.class);subjectName=b.getString("subject_name");bounty=b.getInt("posted_bounty");}
}
