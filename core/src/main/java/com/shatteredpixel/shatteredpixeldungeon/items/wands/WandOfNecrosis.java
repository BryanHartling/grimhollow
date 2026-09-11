// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.wands;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import java.util.*;
public class WandOfNecrosis extends WandOfCorrosion {
    {image=ItemSpriteSheet.WAND_NECROSIS;}
    @Override public void onZap(Ballistica bolt){
        Char next=Actor.findChar(bolt.collisionPos);Set<Char> struck=new HashSet<>();
        for(int hop=0;next!=null&&hop<=1+Math.max(0,buffedLvl())/2;hop++){
            if(next.alignment!=Char.Alignment.ENEMY)break;
            struck.add(next);wandProc(next,chargesPerCast());
            Corrosion corrosion=Buff.affect(next,Corrosion.class);if(corrosion!=null)corrosion.set(2,Math.max(1,2+buffedLvl()-hop),getClass());
            com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.burst(next.pos,com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.Style.NECROTIC,16,.6f);
            Char previous=next;next=null;
            for(Char candidate:Actor.chars())if(candidate.alignment==Char.Alignment.ENEMY&&!struck.contains(candidate)&&Dungeon.level.adjacent(previous.pos,candidate.pos)
                    &&(next==null||candidate.pos<next.pos))next=candidate;
        }
        if(struck.isEmpty())Dungeon.level.pressCell(bolt.collisionPos);
    }
    @Override public String statsDesc(){return Messages.get(this,"stats_desc",2+buffedLvl(),1+Math.max(0,buffedLvl())/2);}
}
