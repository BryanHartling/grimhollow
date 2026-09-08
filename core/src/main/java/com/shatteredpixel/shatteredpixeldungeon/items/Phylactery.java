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
    public enum Spell { RAISE_SKELETON, WITHER, RAISE_WRAITH, RAISE_GHOUL, RAISE_REVENANT, AMPLIFY, DECREPIFY, IRON_MAIDEN, LOWER_RESISTANCE }
    {image=ItemSpriteSheet.PHYLACTERY;unique=true;bones=false;charge=1;chargeCap=3;levelCap=10;defaultAction=AC_CAST;}
    private float spentExperience;
    private final java.util.HashSet<Integer> visitedFloors=new java.util.HashSet<>();
    public int cap(){return 3+(level()>=2?1:0)+(level()>=5?1:0)+(level()>=8?1:0)+(Dungeon.hero!=null&&Dungeon.hero.subClass==HeroSubClass.DEATHSPEAKER?1:0);}
    public float minionBonus(){return 1+.05f*level();}
    public void arrive(){if(visitedFloors.add(Dungeon.depth+100*Dungeon.branch)&&charge==0)gainCharge(1);}
    @Override public void charge(Hero hero,float amount) {} // Resource is kill-driven; never an artifact/wand regeneration target.
    @Override public Item upgrade(){return this;}
    @Override public void transferUpgrade(int level) {}
    public int cost(Spell spell){int n=spell==Spell.RAISE_REVENANT?4:spell==Spell.RAISE_GHOUL?3:spell==Spell.RAISE_WRAITH?2:1;return Math.max(1,n-(level()>=10&&spell.name().startsWith("RAISE_")?1:0));}
    private void spendCharges(int amount){
        charge-=amount;
        spentExperience+=amount*(1+.25f*Math.max(0,Necromancy.points(Talent.GRAVE_WISDOM)-1));
        while(level()<10&&spentExperience>=10+5*level()){spentExperience-=10+5*level();super.upgrade();}
        chargeCap=cap();
        for(NecroSkeleton m:NecroSkeleton.minions())m.refreshStats();
    }
    public int charges(){return charge;}
    @Override protected ArtifactBuff passiveBuff(){return new Keeper();}
    public class Keeper extends ArtifactBuff {
        @Override public boolean act(){chargeCap=cap();charge=Math.min(charge,chargeCap);spend(TICK);return true;}
    }
    public void gainCharge(int amount){chargeCap=cap();charge=Math.min(chargeCap,Math.max(0,charge+amount));Item.updateQuickslot();}
    @Override public boolean isUpgradable(){return false;}
    @Override public boolean isIdentified(){return true;}
    @Override public String status(){return charge+"/"+cap();}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("spent_experience",spentExperience);b.put("visited_floors",visitedFloors.stream().mapToInt(Integer::intValue).toArray());}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);level(b.getInt("level"));charge=b.getInt("charge");spentExperience=b.getFloat("spent_experience");visitedFloors.clear();for(int floor:b.getIntArray("visited_floors"))visitedFloors.add(floor);chargeCap=cap();}
    @Override public ArrayList<String> actions(Hero hero){ArrayList<String> a=super.actions(hero);if(isEquipped(hero))a.add(AC_CAST);return a;}
    public ArrayList<Spell> spells(Hero hero){
        ArrayList<Spell> spells=new ArrayList<>();spells.add(Spell.RAISE_SKELETON);spells.add(Spell.WITHER);
        if(level()>=1)spells.add(Spell.RAISE_WRAITH);
        if(level()>=3)spells.add(Spell.RAISE_GHOUL);
        if(level()>=6&&hero.subClass==HeroSubClass.DEATHSPEAKER)spells.add(Spell.RAISE_REVENANT);
        if(hero.subClass==HeroSubClass.HEXWEAVER){spells.add(Spell.AMPLIFY);spells.add(Spell.DECREPIFY);spells.add(Spell.IRON_MAIDEN);spells.add(Spell.LOWER_RESISTANCE);}
        return spells;
    }
    @Override public void execute(Hero hero,String action){super.execute(hero,action);if(action.equals(AC_CAST)&&isEquipped(hero))GameScene.show(new SpellMenu(hero));}
    /** Resolve through the same path for targeting, quickslots and headless class acceptance. */
    public boolean cast(Hero hero,Spell spell,Integer cell){
        int cost=cost(spell);
        if(!spells(hero).contains(spell)||!isEquipped(hero)||hero.buff(MagicImmune.class)!=null||charge<cost){GLog.w(Messages.get(this,"no_charge"));return false;}
        if(spell.name().startsWith("RAISE_")){
            int pos=-1;
            for(int offset:PathFinder.NEIGHBOURS8){int p=hero.pos+offset;if(Dungeon.level.insideMap(p)&&Dungeon.level.passable[p]&&Actor.findChar(p)==null){pos=p;break;}}
            if(pos<0||NecroSkeleton.raise(pos,spell==Spell.RAISE_REVENANT?3:spell==Spell.RAISE_GHOUL?2:spell==Spell.RAISE_WRAITH?1:0,false)==null){GLog.w(Messages.get(this,"no_room"));return false;}
        }else{
            if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell])return false;
            Char target=Actor.findChar(cell);if(target==null||target.alignment!=Char.Alignment.ENEMY)return false;
            NecroCurse.Kind kind=NecroCurse.Kind.valueOf(spell.name());
            int base=spell==Spell.WITHER?10:spell==Spell.DECREPIFY?6:12;
            int duration=Math.round(base*(1+.25f*Necromancy.points(Talent.LINGERING_HEX)));
            if(NecroCurse.apply(target,kind,duration)==null)return false;
        }
        spendCharges(cost);Item.updateQuickslot();
        if(hero.sprite!=null&&hero.sprite.parent!=null)hero.sprite.operate(hero.pos);
        hero.spendAndNext(Actor.TICK);return true;
    }
    private class SpellMenu extends Window {
        SpellMenu(Hero hero){
            resize(220,170);ArrayList<Spell> options=spells(hero);options.removeIf(s->s.name().startsWith("RAISE_")&&s!=Spell.RAISE_SKELETON);
            for(int i=0;i<options.size();i++){
                final Spell spell=options.get(i);double angle=-Math.PI/2+2*Math.PI*i/options.size();
                RedButton button=new RedButton(Messages.get(Phylactery.class,spell.name()),6){
                    @Override protected void onClick(){hide();
                        if(spell.name().startsWith("RAISE_")){
                            ArrayList<Spell> tiers=spells(hero);tiers.removeIf(s->!s.name().startsWith("RAISE_")||cost(s)>charges());
                            String[] labels=new String[tiers.size()];for(int j=0;j<labels.length;j++)labels[j]=Messages.get(Phylactery.class,tiers.get(j).name())+" ("+cost(tiers.get(j))+")";
                            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions(Messages.get(Phylactery.class,"raise_dead"),Messages.get(Phylactery.class,"tiers"),labels){@Override protected void onSelect(int i){cast(hero,tiers.get(i),hero.pos);}});
                        }
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
