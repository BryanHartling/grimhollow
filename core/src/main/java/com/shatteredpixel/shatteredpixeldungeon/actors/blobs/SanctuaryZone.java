// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.watabou.utils.*;
public class SanctuaryZone extends Blob {
    private int center,radius,remaining,turn,lastHero;
    public static boolean contains(int cell){SanctuaryZone zone=Dungeon.level==null?null:(SanctuaryZone)Dungeon.level.blobs.get(SanctuaryZone.class);return zone!=null&&zone.cur!=null&&zone.cur[cell]>0;}
    public static SanctuaryZone place(int cell,int radius,int turns){SanctuaryZone zone=Blob.seed(cell,turns,SanctuaryZone.class);zone.center=cell;zone.lastHero=Dungeon.hero.pos;zone.radius=radius;zone.remaining=turns;
        for(int i=0;i<Dungeon.level.length();i++)if(Dungeon.level.distance(cell,i)<=radius&&Dungeon.level.passable[i])zone.seed(Dungeon.level,i,turns);
        if(com.watabou.noosa.Game.scene() instanceof GameScene)GameScene.add(zone);return zone;}
    @Override protected void evolve(){
        int mobile=Dungeon.hero.pointsInTalent(Talent.MOBILE);
        if(lastHero!=Dungeon.hero.pos){lastHero=Dungeon.hero.pos;if(Random.Float()<(mobile==3?1:mobile*.25f))center=lastHero;}
        java.util.Arrays.fill(off,0);area.setEmpty();turn++;remaining--;
        for(int cell=0;cell<cur.length;cell++)if(Dungeon.level.distance(center,cell)<=radius&&Dungeon.level.passable[cell]){
            off[cell]=Math.max(0,remaining);volume+=off[cell];area.union(cell%Dungeon.level.width(),cell/Dungeon.level.width());
            Char ch=Actor.findChar(cell);if(ch==null)continue;
            if(ch.alignment==Char.Alignment.ALLY){if(turn%2==0)ch.heal(1);}
            else if(ch.alignment==Char.Alignment.ENEMY){Buff.prolong(ch,Slow.class,2);Buff.prolong(ch,Hex.class,2);int p=Dungeon.hero.pointsInTalent(Talent.CONSECRATED);if(p>0)Buff.affect(ch,Corrosion.class).set(2,p);Buff.prolong(ch,EnchanterMagic.EnchanterDamage.class,3);}
        }
    }
    @Override public void use(BlobEmitter emitter){super.use(emitter);emitter.pour(Speck.factory(Speck.LIGHT),.2f);}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("center",center);b.put("radius",radius);b.put("remaining",remaining);b.put("turn",turn);b.put("last_hero",lastHero);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);center=b.getInt("center");radius=b.getInt("radius");remaining=b.getInt("remaining");turn=b.getInt("turn");lastHero=b.getInt("last_hero");}
}
