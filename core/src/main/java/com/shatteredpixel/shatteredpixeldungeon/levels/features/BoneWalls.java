// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.features;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.*;
/** Terrain ownership lives on the level. Saving records originals; loading always restores them. */
public class BoneWalls extends Buff {
    private int depth,branch,keys;
    public static boolean raise(int cell,int turns){
        Level l=Dungeon.level;if(!l.insideMap(cell)||!l.passable[cell]||Actor.findChar(cell)!=null||l.heaps.get(cell)!=null||l.traps.get(cell)!=null||l.map[cell]==Terrain.ENTRANCE||l.map[cell]==Terrain.EXIT)return false;
        l.boneOriginal.put(cell,l.map[cell]);l.boneTurns.put(cell,turns);Level.set(cell,Terrain.BONE_WALL);GameScene.updateMap(cell);
        BoneWalls buff=Buff.affect(Dungeon.hero,BoneWalls.class);buff.depth=Dungeon.depth;buff.branch=Dungeon.branch;Dungeon.observe();return true;
    }
    public static boolean prison(int center,int turns,int keys){
        Level l=Dungeon.level;boolean made=false;
        for(int offset:PathFinder.NEIGHBOURS8){int cell=center+offset;
            if(!l.insideMap(cell)||!l.passable[cell]||Actor.findChar(cell)!=null||l.heaps.get(cell)!=null||l.traps.get(cell)!=null||l.map[cell]==Terrain.ENTRANCE||l.map[cell]==Terrain.EXIT)continue;
            l.boneOriginal.put(cell,l.map[cell]);l.boneTurns.put(cell,turns);Level.set(cell,Terrain.BONE_WALL);GameScene.updateMap(cell);made=true;
        }
        if(made){BoneWalls buff=Buff.affect(Dungeon.hero,BoneWalls.class);buff.depth=Dungeon.depth;buff.branch=Dungeon.branch;buff.keys=keys;Dungeon.observe();}
        return made;
    }
    public static void clear(Level l){
        if(l==null)return;
        for(int cell:l.boneOriginal.keyArray())Level.set(cell,l.boneOriginal.get(cell),l);
        l.boneOriginal.clear();l.boneTurns.clear();
    }
    public static boolean unlock(int cell){
        BoneWalls b=Dungeon.hero.buff(BoneWalls.class);Level l=Dungeon.level;
        if(b==null||b.keys<=0||!l.insideMap(cell)||l.map[cell]!=Terrain.BONE_WALL||!l.adjacent(Dungeon.hero.pos,cell))return false;
        b.keys--;Level.set(cell,l.boneOriginal.get(cell));l.boneOriginal.remove(cell);l.boneTurns.remove(cell);GameScene.updateMap(cell);Dungeon.observe();return true;
    }
    @Override public boolean act(){
        Level l=Dungeon.level;
        if(depth!=Dungeon.depth||branch!=Dungeon.branch||l.boneOriginal.keyArray().length==0){detach();return true;}
        int damage=2*Necromancy.points(Talent.JAGGED);
        if(damage>0)for(Mob m:l.mobs.toArray(new Mob[0]))if(m.alignment==Char.Alignment.ENEMY){
            for(int cell:l.boneOriginal.keyArray())if(l.adjacent(cell,m.pos)){Buff.prolong(m,Necromancy.HeroDamage.class,2);m.damage(damage,this);break;}
        }
        boolean changed=false;
        for(int cell:l.boneTurns.keyArray()){int left=l.boneTurns.get(cell)-1;if(left<=0){Level.set(cell,l.boneOriginal.get(cell));l.boneOriginal.remove(cell);l.boneTurns.remove(cell);GameScene.updateMap(cell);changed=true;}else l.boneTurns.put(cell,left);}
        if(changed)Dungeon.observe();spend(TICK);return true;
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("depth",depth);b.put("branch",branch);b.put("keys",keys);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);depth=b.getInt("depth");branch=b.getInt("branch");keys=b.getInt("keys");}
}
