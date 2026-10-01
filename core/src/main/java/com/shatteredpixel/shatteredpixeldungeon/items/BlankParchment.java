// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.*;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndScribe;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Scribing uses the run's scroll identities and existing alchemical energy. */
public class BlankParchment extends Item {
    public static final String AC_SCRIBE="SCRIBE";
    public static final float SCRIBE_TIME=3f;
    { image=ItemSpriteSheet.BLANK_PARCHMENT; stackable=true; defaultAction=AC_SCRIBE; }
    @Override public boolean isIdentified(){return true;}
    @Override public boolean isUpgradable(){return false;}
    @Override public int value(){return 0;}
    @Override public ArrayList<String> actions(Hero hero){
        ArrayList<String> actions=super.actions(hero);
        if(hero.heroClass==HeroClass.ENCHANTER)actions.add(AC_SCRIBE);
        return actions;
    }
    @Override public void execute(Hero hero,String action){
        super.execute(hero,action);
        if(action.equals(AC_SCRIBE)&&hero.heroClass==HeroClass.ENCHANTER)GameScene.show(new WndScribe(hero));
    }
    public static List<Class<? extends Scroll>> recipes(){
        List<Class<? extends Scroll>> result=new ArrayList<>();
        for(Class<?> type:Generator.Category.SCROLL.classes)
            if(type!=ScrollOfUpgrade.class && Scroll.getKnown().contains(type))result.add(type.asSubclass(Scroll.class));
        result.sort(Comparator.comparing(type->Reflection.newInstance(type).name()));
        return result;
    }
    public static int cost(Class<? extends Scroll> type){return type==ScrollOfTransmutation.class?20:12;}
    public static boolean canScribe(Hero hero,Class<? extends Scroll> type){
        SigilBrush brush=hero.belongings.getItem(SigilBrush.class);
        return hero==Dungeon.hero && hero.heroClass==HeroClass.ENCHANTER && hero.isAlive()
                && recipes().contains(type) && hero.belongings.getItem(BlankParchment.class)!=null
                && brush!=null && brush.ready(hero,1) && Dungeon.energy>=cost(type);
    }
    public static boolean scribe(Hero hero,Class<? extends Scroll> type){
        if(!canScribe(hero,type))return false;
        Scroll output=Reflection.newInstance(type);
        if(output==null)return false;
        hero.belongings.getItem(BlankParchment.class).detach(hero.belongings.backpack);
        Dungeon.energy-=cost(type);
        hero.belongings.getItem(SigilBrush.class).gainCharge(-1);
        output.identify();
        if(!output.collect(hero.belongings.backpack))Dungeon.level.drop(output,hero.pos);
        GLog.p(Messages.get(BlankParchment.class,"written",output.name()));
        EnhancedEffects.burst(hero.pos,EnhancedEffects.Style.INSCRIPTION,16,.6f);
        // Like eating: one committed action occupies three turns, with no repeat queue.
        hero.busy();hero.spend(SCRIBE_TIME);
        if(hero.sprite!=null)hero.sprite.operate(hero.pos);else hero.next();
        return true;
    }
    public static void recycle(Hero hero,int quantity){
        if(quantity<=0)return;
        BlankParchment parchment=new BlankParchment();parchment.quantity(quantity);
        if(!parchment.collect(hero.belongings.backpack))Dungeon.level.drop(parchment,hero.pos);
        GLog.i(Messages.get(BlankParchment.class,"reclaimed",quantity));
    }
    /** One early sheet, then a seeded 75% opportunity per later region; no RNG disruption. */
    public static boolean spawnsOn(int depth,int branch,HeroClass heroClass,long seed){
        if(branch!=0||heroClass!=HeroClass.ENCHANTER||depth<2||depth>24||depth%5==0)return false;
        if(depth<=5)return depth==2;
        int region=(depth-1)/5;
        Random.pushGenerator(seed+0x534352494245L+region);
        try{return Random.Float()<.75f && depth==region*5+1+Random.Int(4);}
        finally{Random.popGenerator();}
    }
}
