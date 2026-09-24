// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import java.util.Arrays;
public class FocusCrystal extends ClassSpellItem {
    {image=ItemSpriteSheet.FOCUS_CRYSTAL;levelCap=10;}
    private int spentExperience;
    @Override protected void onPlaytestLevelSet(){spentExperience=0;}
    @Override public int cap(){return super.cap()+PsychicMind.points(Talent.FOCUSED_MIND);}
    @Override public int visiblyUpgraded(){return level();}
    @Override public Item upgrade(){return this;}
    @Override public void transferUpgrade(int level) {}
    @Override protected void onChargesSpent(int cost){
        spentExperience+=cost;
        while(level()<10&&spentExperience>=10+5*level()){
            spentExperience-=10+5*level();
            super.upgrade();
        }
        if(level()>=10)spentExperience=0;
    }
    public int spentExperience(){return spentExperience;}
    public int pushDistance(){return level()>=8?4:level()>=2?3:2;}
    public int graspRange(Hero hero){return (hero==null?8:hero.viewDistance)+(level()>=7?2:level()>=3?1:0)
            +farReachBonus(hero==null?0:hero.pointsInTalent(Talent.FAR_REACH));}
    public static int farReachBonus(int rank){return rank>=3?4:Math.max(0,rank);}
    public int glimpseDuration(){return level()>=10?9:level()>=5?7:5;}
    @Override public String desc(){
        return super.desc()+"\n\n"+Messages.get(this,"progress",level(),spentExperience,level()<10?10+5*level():0)
                +"\n\n"+Messages.get(this,"utility_stats",graspRange(Dungeon.hero),glimpseDuration())
                +"\n\n"+Messages.get(this,"push_stats",pushDistance());
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("spent_experience",spentExperience);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);level(Math.max(0,Math.min(10,b.getInt("level"))));spentExperience=b.getInt("spent_experience");chargeCap=cap();}
    @Override protected float regeneration(){return PsychicMind.calm()?1+.15f*PsychicMind.points(Talent.DEEP_FOCUS):1;}
    @Override public String[] spells(Hero h){return h.subClass==HeroSubClass.PUPPETEER?new String[]{"grasp","glimpse","push","dominate","suggestion"}:h.subClass==HeroSubClass.SEER?new String[]{"grasp","glimpse","push","hurl"}:new String[]{"grasp","glimpse","push"};}
    @Override protected void select(Hero h,String spell){
        if(spell.equals("glimpse")){cast(h,spell,h.pos,null);return;}
        GameScene.selectCell(new CellSelector.Listener(){public void onSelect(Integer cell){if(cell==null)return;if(spell.equals("hurl"))GameScene.selectCell(new CellSelector.Listener(){public void onSelect(Integer direction){cast(h,spell,cell,direction);}public String prompt(){return Messages.get(FocusCrystal.class,"direction");}});else cast(h,spell,cell,null);}public String prompt(){return Messages.get(FocusCrystal.class,"target");}});
    }
    public boolean cast(Hero h,String spell,Integer cell,Integer direction){
        int cost=spell.equals("dominate")?2:1;if(!Arrays.asList(spells(h)).contains(spell)||!ready(h,cost))return false;
        if(spell.equals("glimpse")){Buff.prolong(h,MindVision.class,glimpseDuration());Dungeon.observe();com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.burst(cell==null?h.pos:cell,com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.Style.PSYCHIC,16,.6f);finish(h,cost);return true;}
        if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell])return false;
        Char enemy=Actor.findChar(cell);
        if(spell.equals("grasp")){
            int range=graspRange(h);
            if(Dungeon.level.distance(h.pos,cell)>range)return false;
            Heap heap=Dungeon.level.heaps.get(cell);Trap trap=Dungeon.level.traps.get(cell);
            if(heap!=null&&heap.type==Heap.Type.HEAP&&!heap.isEmpty()){
                if(enemy!=null&&enemy.alignment==Char.Alignment.ENEMY&&h.hasTalent(Talent.WRENCH))Buff.prolong(enemy,Vertigo.class,2*h.pointsInTalent(Talent.WRENCH));
                float time=0;while(!heap.isEmpty()){Item item=heap.pickUp();if(item.doPickUp(h,h.pos))time+=item.pickupDelay();else Dungeon.level.drop(item,h.pos);}
                h.spend(-time);
            }else if(trap!=null&&trap.visible&&trap.active){trap.psychicPower=1+.25f*h.pointsInTalent(Talent.TRAP_SENSE);trap.trigger();Dungeon.level.traps.remove(cell);if(Dungeon.level.map[cell]==Terrain.TRAP||Dungeon.level.map[cell]==Terrain.INACTIVE_TRAP||Dungeon.level.map[cell]==Terrain.SECRET_TRAP)Level.set(cell,Terrain.EMPTY);GameScene.updateMap(cell);}
            else return false;
        }else{
            if(enemy==null||enemy.alignment!=Char.Alignment.ENEMY)return false;
            if(spell.equals("dominate")){
                if(Char.hasProp(enemy,Char.Property.BOSS))return false;
                if(level()>=6){
                    PsychicDomination control=new PsychicDomination().configure(h,level());
                    if(!control.attachTo(enemy))return false;
                    Buff.detach(enemy,Amok.class);
                    control.share(h);
                }else{
                    Amok amok=Buff.prolong(enemy,Amok.class,15);if(amok==null)return false;
                    amok.dominated=true;amok.share(h);
                }
                PsychicDomination.releasePermanent(h,enemy);
            }
            else if(spell.equals("suggestion")){Terror terror=Buff.prolong(enemy,Terror.class,10);if(terror==null)return false;terror.object=h.id();}
            else if(spell.equals("push")||spell.equals("hurl")){
                boolean hurl=spell.equals("hurl"),boss=Char.hasProp(enemy,Char.Property.BOSS);
                if((boss&&!hurl)||enemy.rooted||Char.hasProp(enemy,Char.Property.IMMOVABLE))return false;
                if(hurl&&(direction==null||!Dungeon.level.insideMap(direction)||direction==cell))return false;
                int width=Dungeon.level.width();
                int dx=hurl?Integer.signum(direction%width-cell%width):Integer.signum(cell%width-h.pos%width);
                int dy=hurl?Integer.signum(direction/width-cell/width):Integer.signum(cell/width-h.pos/width);
                if(dx==0&&dy==0)return false;
                Ballistica ray=new Ballistica(cell,cell+dx+dy*width,Ballistica.MAGIC_BOLT);
                int distance=boss?1:pushDistance()+(hurl?2:0),crystalLevel=level();
                WandOfBlastWave.throwChar(enemy,ray,distance,false,false,this,new WandOfBlastWave.LandingRules(crystalLevel>=6){
                    @Override public void collide(Char target,int moved){
                        if(crystalLevel<4)return;
                        int damage=Hero.heroDamageIntRange(2+h.lvl/2,4+h.lvl);
                        if(hurl)damage=Math.round(damage*(1+.25f*h.pointsInTalent(Talent.HEAVY_HAND)));
                        target.damage(damage,FocusCrystal.this);
                        if(target.isAlive()){
                            if(hurl)Buff.prolong(target,Paralysis.class,2);
                            else Buff.prolong(target,Vertigo.class,2);
                        }
                    }
                });
            }else return false;
            if(enemy.isAlive())Buff.prolong(enemy,PsychicMind.PsychicDamage.class,20);
        }
        com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.burst(cell==null?h.pos:cell,com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.Style.PSYCHIC,16,.6f);finish(h,cost);return true;
    }
}
