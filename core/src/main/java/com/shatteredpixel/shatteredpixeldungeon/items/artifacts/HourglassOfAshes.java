// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import java.util.ArrayList;
/** The refund is owned by the hero, so swapping or transmuting the artifact cannot reset it. */
public class HourglassOfAshes extends Artifact {
    public static final String AC_REWIND="REWIND";
    { image=ItemSpriteSheet.HOURGLASS_ASHES; levelCap=10; chargeCap=10; charge=0; defaultAction=AC_REWIND; }
    public int charges(){return charge;}
    public float turnsPerCharge(){return 30-level()*2;}
    public boolean rewind(Hero hero){
        if(!isEquipped(hero)||cursed||hero.buff(MagicImmune.class)!=null||charge<1||!hero.canRefundAshes())return false;
        charge--;
        if(level()>0)for(Mob mob:Dungeon.level.mobs)if(mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.heroFOV[mob.pos]){
            for(Buff buff:mob.buffs())if(buff.type==Buff.buffType.POSITIVE&&buff.age()<=level())buff.detach();
        }
        hero.refundAshes(); Item.updateQuickslot(); return true;
    }
    @Override public ArrayList<String> actions(Hero hero){
        ArrayList<String> result=super.actions(hero);
        if(isEquipped(hero)&&!cursed&&charge>0&&hero.canRefundAshes())result.add(AC_REWIND);
        return result;
    }
    @Override public void execute(Hero hero,String action){
        super.execute(hero,action);
        if(action.equals(AC_REWIND)&&!rewind(hero))GLog.w(Messages.get(this,"unavailable"));
    }
    @Override protected ArtifactBuff passiveBuff(){return new AshKeeper();}
    public class AshKeeper extends ArtifactBuff {
        @Override public boolean act(){
            if(!cursed&&target.buff(MagicImmune.class)==null&&charge<chargeCap){
                partialCharge+=1f/turnsPerCharge();
                if(partialCharge>=.99999f){charge++;partialCharge=Math.max(0,partialCharge-1);Item.updateQuickslot();}
            }
            spend(TICK);return true;
        }
        public void absorb(int damage){
            if(cursed||target.buff(MagicImmune.class)!=null||damage<=0)return;
            exp+=damage;
            while(level()<levelCap&&exp>=20+10*level()){exp-=20+10*level();HourglassOfAshes.this.upgrade();}
            Item.updateQuickslot();
        }
    }
}
