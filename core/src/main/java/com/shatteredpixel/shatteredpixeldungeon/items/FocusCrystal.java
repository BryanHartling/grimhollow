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
import java.util.Arrays;
public class FocusCrystal extends ClassSpellItem {
    {image=ItemSpriteSheet.FOCUS_CRYSTAL;}
    @Override public int cap(){return super.cap()+(PsychicMind.points(Talent.FOCUSED_MIND)>=2?1:0);}
    @Override protected float regeneration(){return PsychicMind.calm()?1+.15f*PsychicMind.points(Talent.DEEP_FOCUS):1;}
    @Override public String[] spells(Hero h){return h.subClass==HeroSubClass.PUPPETEER?new String[]{"grasp","glimpse","dominate","suggestion"}:h.subClass==HeroSubClass.SEER?new String[]{"grasp","glimpse","hurl"}:new String[]{"grasp","glimpse"};}
    @Override protected void select(Hero h,String spell){
        if(spell.equals("glimpse")){cast(h,spell,h.pos,null);return;}
        GameScene.selectCell(new CellSelector.Listener(){public void onSelect(Integer cell){if(cell==null)return;if(spell.equals("hurl"))GameScene.selectCell(new CellSelector.Listener(){public void onSelect(Integer direction){cast(h,spell,cell,direction);}public String prompt(){return Messages.get(FocusCrystal.class,"direction");}});else cast(h,spell,cell,null);}public String prompt(){return Messages.get(FocusCrystal.class,"target");}});
    }
    public boolean cast(Hero h,String spell,Integer cell,Integer direction){
        int cost=spell.equals("dominate")?2:1;if(!Arrays.asList(spells(h)).contains(spell)||!ready(h,cost))return false;
        if(spell.equals("glimpse")){Buff.prolong(h,MindVision.class,5);Dungeon.observe();finish(h,cost);return true;}
        if(cell==null||!Dungeon.level.insideMap(cell)||!Dungeon.level.heroFOV[cell])return false;
        Char enemy=Actor.findChar(cell);
        if(spell.equals("grasp")){
            int range=h.viewDistance+2*Math.max(0,h.pointsInTalent(Talent.FAR_REACH)-1);
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
            if(spell.equals("dominate")){Amok amok=Buff.prolong(enemy,Amok.class,Char.hasProp(enemy,Char.Property.BOSS)?5:15);if(amok==null)return false;amok.dominated=true;amok.share(h);}
            else if(spell.equals("suggestion")){Terror terror=Buff.prolong(enemy,Terror.class,10);if(terror==null)return false;terror.object=h.id();}
            else if(spell.equals("hurl")){
                boolean boss=Char.hasProp(enemy,Char.Property.BOSS);if(direction==null||!Dungeon.level.insideMap(direction)||direction==cell||enemy.rooted||Char.hasProp(enemy,Char.Property.IMMOVABLE)||(boss&&!h.hasTalent(Talent.HEAVY_HAND)))return false;
                int width=Dungeon.level.width(),dx=Integer.signum(direction%width-cell%width),dy=Integer.signum(direction/width-cell/width);
                Ballistica ray=new Ballistica(cell,cell+dx+dy*width,Ballistica.MAGIC_BOLT);
                int distance=boss?1:3+(h.hasTalent(Talent.HEAVY_HAND)?1:0);
                if(ray.dist<distance&&Dungeon.level.solid[ray.path.get(Math.min(ray.path.size()-1,ray.dist+1))])Buff.prolong(enemy,Paralysis.class,2);
                WandOfBlastWave.throwChar(enemy,ray,distance,false,false,this);
            }else return false;
            Buff.prolong(enemy,PsychicMind.PsychicDamage.class,20);
        }
        finish(h,cost);return true;
    }
}
