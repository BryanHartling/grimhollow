// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;
/** A fixed three-by-three soul flame; its own damage type bypasses ordinary fire immunity. */
public class Soulfire extends Blob implements com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.Doom {
    @Override protected void evolve(){
        volume=0;
        for(int cell=0;cell<cur.length;cell++){
            if(cur[cell]>0){Char ch=Actor.findChar(cell);if(ch!=null)ch.damage(Random.NormalIntRange(1,3+Dungeon.depth/4),this);
                if(Dungeon.level.heaps.get(cell)!=null)Dungeon.level.heaps.get(cell).burn();
                if(Dungeon.level.plants.get(cell)!=null)Dungeon.level.plants.get(cell).wither();
                if(cur[cell]==1)GameScene.scorchDecal(cell);
            }
            volume+=(off[cell]=Math.max(0,cur[cell]-1));
        }
    }
    @Override public void use(BlobEmitter emitter){super.use(emitter);emitter.pour(ElmoParticle.FACTORY,.03f);}
    @Override public String tileDesc(){return Messages.get(this,"desc");}
    @Override public void onDeath(){Dungeon.fail(getClass());}
}
