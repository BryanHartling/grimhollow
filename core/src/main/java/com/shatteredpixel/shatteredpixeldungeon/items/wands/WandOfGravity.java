// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.wands;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
public class WandOfGravity extends WandOfBlastWave {
    {image=ItemSpriteSheet.WAND_GRAVITY;}
    @Override public void onZap(Ballistica bolt){Char target=Actor.findChar(bolt.collisionPos);if(target!=null&&target.alignment==Char.Alignment.ENEMY){wandProc(target,chargesPerCast());pull(target,Dungeon.hero.pos,2+Math.max(0,buffedLvl())/3,this);}}
    public static void pull(Char target,int toward,int distance,Object cause){
        if(target.rooted||Char.hasProp(target,Char.Property.IMMOVABLE)||target.pos==toward)return;
        Ballistica path=new Ballistica(target.pos,toward,Ballistica.STOP_TARGET|Ballistica.STOP_SOLID);
        int power=Char.hasProp(target,Char.Property.BOSS)?(distance+1)/2:distance;
        for(int i=1;i<=Math.min(power,path.dist);i++){Char other=Actor.findChar(path.path.get(i));if(other!=null&&other!=target){Buff.prolong(target,Vertigo.class,3);Buff.prolong(other,Vertigo.class,3);path=new Ballistica(target.pos,path.path.get(i),Ballistica.STOP_TARGET|Ballistica.STOP_SOLID);break;}}
        WandOfBlastWave.throwChar(target,path,distance,false,false,cause);
    }
    @Override public int min(int level){return 0;}
    @Override public int max(int level){return 0;}
    @Override public String statsDesc(){return Messages.get(this,"stats_desc",2+Math.max(0,buffedLvl())/3);}
    @Override public String upgradeStat1(int level){return Integer.toString(2+Math.max(0,level)/3);}
}
