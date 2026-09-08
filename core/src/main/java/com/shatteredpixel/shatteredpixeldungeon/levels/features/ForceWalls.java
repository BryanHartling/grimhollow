// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.features;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import java.util.HashMap;
/** Transparent, temporary terrain; originals restore on timeout, exit and load. */
public class ForceWalls extends Buff {
    private int depth,branch;
    private static final HashMap<Integer,Image> visuals=new HashMap<>();
    public static boolean line(int center,int turns){
        Level l=Dungeon.level;int w=l.width(),hero=Dungeon.hero.pos;
        int dx=Integer.signum(center%w-hero%w),dy=Integer.signum(center/w-hero/w);if(dx==0&&dy==0)dx=1;
        int offset=-dy+dx*w;boolean made=false;
        for(int step=-1;step<=1;step++){int cell=center+step*offset;
            if(!l.insideMap(cell)||l.distance(center,cell)>1||!l.passable[cell]||Actor.findChar(cell)!=null||l.heaps.get(cell)!=null||l.traps.get(cell)!=null||l.map[cell]==Terrain.ENTRANCE||l.map[cell]==Terrain.EXIT)continue;
            l.forceOriginal.put(cell,l.map[cell]);l.forceTurns.put(cell,turns);Level.set(cell,Terrain.FORCE_WALL);GameScene.updateMap(cell);made=true;
            if(com.watabou.noosa.Game.scene() instanceof GameScene){Image image=new Image("effects/force_wall.png");image.logicalSize(16,16);image.alpha(.75f);image.point(DungeonTilemap.tileToWorld(cell));GameScene.effect(image);visuals.put(cell,image);}
        }
        if(made){ForceWalls buff=Buff.affect(Dungeon.hero,ForceWalls.class);buff.depth=Dungeon.depth;buff.branch=Dungeon.branch;Dungeon.observe();}return made;
    }
    private static void removeVisual(int cell){Image image=visuals.remove(cell);if(image!=null)image.killAndErase();}
    public static void clear(Level level){if(level==null)return;for(int cell:level.forceOriginal.keyArray()){Level.set(cell,level.forceOriginal.get(cell),level);removeVisual(cell);}level.forceOriginal.clear();level.forceTurns.clear();}
    public static boolean heroPasses(int cell){return Dungeon.level.insideMap(cell)&&Dungeon.level.map[cell]==Terrain.FORCE_WALL&&Dungeon.hero.pointsInTalent(Talent.PERMEABLE)>=3;}
    public static boolean repulse(Char mob,int cell){
        if(mob.alignment!=Char.Alignment.ENEMY||!Dungeon.level.insideMap(cell)||Dungeon.level.map[cell]!=Terrain.FORCE_WALL)return false;
        int p=Dungeon.hero.pointsInTalent(Talent.REPULSE);if(p>0)Buff.prolong(mob,Paralysis.class,p);return true;
    }
    @Override public boolean act(){Level l=Dungeon.level;if(depth!=Dungeon.depth||branch!=Dungeon.branch||l.forceOriginal.keyArray().length==0){detach();return true;}
        boolean changed=false;for(int cell:l.forceTurns.keyArray()){int remaining=l.forceTurns.get(cell)-1;if(remaining<=0){Level.set(cell,l.forceOriginal.get(cell));l.forceOriginal.remove(cell);l.forceTurns.remove(cell);removeVisual(cell);GameScene.updateMap(cell);changed=true;}else l.forceTurns.put(cell,remaining);}
        if(changed)Dungeon.observe();spend(TICK);return true;
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("depth",depth);b.put("branch",branch);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);depth=b.getInt("depth");branch=b.getInt("branch");}
}
