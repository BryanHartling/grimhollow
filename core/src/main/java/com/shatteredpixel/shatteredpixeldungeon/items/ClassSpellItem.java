// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
/** Shared charge cadence and radial UI for the Brush and Crystal specified by the class kits. */
public abstract class ClassSpellItem extends Artifact {
    {unique=true;bones=false;charge=chargeCap=2;defaultAction="CAST";levelCap=0;}
    public int charges(){return charge;}
    public int cap(){int n=Dungeon.hero.lvl;return 2+(n>=7?1:0)+(n>=13?1:0)+(n>=20?1:0);}
    public void gainCharge(int n){chargeCap=cap();charge=Math.min(cap(),Math.max(0,charge+n));Item.updateQuickslot();}
    public void advance(float turns){chargeCap=cap();if(charge<cap()){partialCharge+=turns/Math.max(20,40-2*Dungeon.hero.lvl);while(partialCharge>=1&&charge<cap()){charge++;partialCharge--;}}else partialCharge=0;Item.updateQuickslot();}
    protected float regeneration(){return 1;}
    public boolean ready(Hero hero,int cost){return isEquipped(hero)&&hero.buff(MagicImmune.class)==null&&charge>=cost;}
    protected void finish(Hero hero,int cost){charge-=cost;Item.updateQuickslot();hero.spendAndNext(1);}
    @Override public boolean isIdentified(){return true;}
    @Override public int visiblyUpgraded(){return 0;}
    @Override public String status(){return charge+"/"+cap();}
    @Override protected ArtifactBuff passiveBuff(){return new Charger();}
    public class Charger extends ArtifactBuff {@Override public boolean act(){advance(regeneration());spend(TICK);return true;}}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);charge=b.getInt("charge");}
    @Override public void doDrop(Hero hero){}
    @Override public boolean doUnequip(Hero hero,boolean collect,boolean single){return false;}
    @Override public ArrayList<String> actions(Hero hero){ArrayList<String> a=super.actions(hero);a.remove(AC_DROP);a.remove(AC_THROW);a.remove(AC_UNEQUIP);if(isEquipped(hero))a.add("CAST");return a;}
    protected abstract String[] spells(Hero hero);
    protected abstract void select(Hero hero,String spell);
    @Override public void execute(Hero hero,String action){super.execute(hero,action);if(action.equals("CAST")&&isEquipped(hero))GameScene.show(new Window(){
        {resize(220,170);String[] choices=spells(hero);for(int i=0;i<choices.length;i++){final String spell=choices[i];double angle=-Math.PI/2+2*Math.PI*i/choices.length;
            RedButton button=new RedButton(Messages.get(ClassSpellItem.this,spell),6){@Override protected void onClick(){hide();select(hero,spell);}};
            button.setRect(74+(float)Math.cos(angle)*72,71+(float)Math.sin(angle)*62,72,28);add(button);}}
    });}
}
