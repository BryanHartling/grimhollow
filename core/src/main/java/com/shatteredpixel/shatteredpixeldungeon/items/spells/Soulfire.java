// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.spells;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
public class Soulfire extends TargetedSpell {
    {image=ItemSpriteSheet.SOULFIRE;usesTargeting=true;}
    public void ignite(int center,Hero hero){
        for(int offset:PathFinder.NEIGHBOURS9){int cell=center+offset;if(!Dungeon.level.insideMap(cell)||Dungeon.level.solid[cell]||Dungeon.level.distance(center,cell)>1)continue;
            GameScene.add(Blob.seed(cell,5,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Soulfire.class));
            Char target=Actor.findChar(cell);if(target!=null){Terror terror=Buff.prolong(target,Terror.class,4);if(terror!=null)terror.object=hero.id();}
        }
    }
    @Override protected void affectTarget(Ballistica bolt,Hero hero){ignite(bolt.collisionPos,hero);onSpellused();}
    @Override public int value(){return 60*quantity;}
    @Override public int energyVal(){return 6*quantity;}
    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
        {inputs=new Class[]{com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame.class,com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror.class};inQuantity=new int[]{1,1};cost=6;output=Soulfire.class;outQuantity=1;}
    }
}
