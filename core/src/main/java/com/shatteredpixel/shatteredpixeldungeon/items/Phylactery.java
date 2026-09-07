// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.*;
import java.util.ArrayList;
public class Phylactery extends Artifact {
    public static final String AC_CAST="CAST";
    public enum Spell { RAISE_SKELETON, WITHER, RAISE_GHOUL, AMPLIFY, DECREPIFY, IRON_MAIDEN, LOWER_RESISTANCE }
    {image=ItemSpriteSheet.PHYLACTERY;unique=true;bones=false;charge=0;chargeCap=3;levelCap=0;defaultAction=AC_CAST;}
    public int cap(){return (Dungeon.hero!=null && Dungeon.hero.subClass==HeroSubClass.DEATHSPEAKER?5:3)+(Necromancy.points(Talent.GRAVE_WISDOM)>=2?1:0);}
    public int charges(){return charge;}
    @Override protected ArtifactBuff passiveBuff(){return new Keeper();}
    public class Keeper extends ArtifactBuff {
        @Override public boolean act(){chargeCap=cap();charge=Math.min(charge,chargeCap);spend(TICK);return true;}
    }
    public void gainCharge(int amount){chargeCap=cap();charge=Math.min(chargeCap,Math.max(0,charge+amount));Item.updateQuickslot();}
    @Override public boolean isUpgradable(){return false;}
    @Override public boolean isIdentified(){return true;}
    @Override public String status(){return charge+"/"+cap();}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);charge=b.getInt("charge");}
    @Override public ArrayList<String> actions(Hero hero){ArrayList<String> a=super.actions(hero);if(isEquipped(hero))a.add(AC_CAST);return a;}
    public ArrayList<Spell> spells(Hero hero){
        ArrayList<Spell> spells=new ArrayList<>();spells.add(Spell.RAISE_SKELETON);spells.add(Spell.WITHER);
        if(hero.subClass==HeroSubClass.DEATHSPEAKER)spells.add(Spell.RAISE_GHOUL);
        if(hero.subClass==HeroSubClass.HEXWEAVER){spells.add(Spell.AMPLIFY);spells.add(Spell.DECREPIFY);spells.add(Spell.IRON_MAIDEN);spells.add(Spell.LOWER_RESISTANCE);}
        return spells;
    }
    @Override public void execute(Hero hero,String action){super.execute(hero,action);if(action.equals(AC_CAST)&&isEquipped(hero))GameScene.show(new SpellMenu(hero));}
    /** Resolve through the same path for targeting, quickslots and headless class acceptance. */
    public boolean cast(Hero hero,Spell spell,Integer cell){
        int cost=spell==Spell.RAISE_GHOUL?2:1;
        if(!spells(hero).contains(spell)||!isEquipped(hero)||hero.buff(MagicImmune.class)!=null||charge<cost){GLog.w(Messages.get(this,"no_charge"));return false;}
        if(spell==Spell.RAISE_SKELETON||spell==Spell.RAISE_GHOUL){
            int pos=-1;
            for(int offset:PathFinder.NEIGHBOURS8){int p=hero.pos+offset;if(Dungeon.level.insideMap(p)&&Dungeon.level.passable[p]&&Actor.findChar(p)==null){pos=p;break;}}
            if(pos<0||NecroSkeleton.raise(pos,spell==Spell.RAISE_GHOUL,false)==null){GLog.w(Messages.get(this,"no_room"));return false;}
        }else{
            if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell])return false;
            Char target=Actor.findChar(cell);if(target==null||target.alignment!=Char.Alignment.ENEMY)return false;
            NecroCurse.Kind kind=NecroCurse.Kind.valueOf(spell.name());
            int base=spell==Spell.WITHER?10:spell==Spell.DECREPIFY?6:12;
            int duration=Math.round(base*(1+.25f*Necromancy.points(Talent.LINGERING_HEX)));
            if(NecroCurse.apply(target,kind,duration)==null)return false;
        }
        charge-=cost;Item.updateQuickslot();
        if(hero.sprite!=null&&hero.sprite.parent!=null)hero.sprite.operate(hero.pos);
        hero.spendAndNext(Actor.TICK);return true;
    }
    private class SpellMenu extends Window {
        SpellMenu(Hero hero){
            resize(220,170);ArrayList<Spell> options=spells(hero);
            for(int i=0;i<options.size();i++){
                final Spell spell=options.get(i);double angle=-Math.PI/2+2*Math.PI*i/options.size();
                RedButton button=new RedButton(Messages.get(Phylactery.class,spell.name()),6){
                    @Override protected void onClick(){hide();
                        if(spell==Spell.RAISE_SKELETON||spell==Spell.RAISE_GHOUL)cast(hero,spell,hero.pos);
                        else GameScene.selectCell(new CellSelector.Listener(){
                            public void onSelect(Integer cell){cast(hero,spell,cell);}
                            public String prompt(){return Messages.get(Phylactery.class,"prompt");}
                        });
                    }
                };button.setRect(74+(float)Math.cos(angle)*72,71+(float)Math.sin(angle)*62,72,28);add(button);
            }
        }
    }
}
