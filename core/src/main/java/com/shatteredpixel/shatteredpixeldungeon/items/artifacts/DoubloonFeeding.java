// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.*;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;

public final class DoubloonFeeding {
    public static void confirm(Hero hero,HatchlingMimic hatchling,FickleDoubloon coin){
        GameScene.show(new WndOptions(new ItemSprite(coin),coin.name(),Messages.get(FickleDoubloon.class,coin.cursed&&coin.cursedKnown?"feed_cursed":"feed_warning"),Messages.get(FickleDoubloon.class,"feed_accept"),Messages.get(FickleDoubloon.class,"feed_cancel")){
            @Override protected void onSelect(int n){if(n==0)hatchling.feedChosen(hero,coin);}
        });
    }
    public static boolean consume(Hero hero,HatchlingMimic hatchling,FickleDoubloon coin){
        int cell=HatchlingMimic.closestSpawn(hero.pos);if(cell<0)return false;
        if(FickleDoubloon.luckRunning(hero))return false;
        boolean heads=!coin.cursed&&Random.Float()<coin.headsChance();
        GoldenMimicCompanion companion=new GoldenMimicCompanion();
        companion.coinLevel=coin.level();companion.hatchlingLevel=hatchling.level();
        companion.inheritedGold=coin.goldBonus()/2;
        companion.inheritedQuality=coin.level()>=2?coin.plunder()/100f:0;
        companion.inheritedSearch=coin.level()>=3?coin.plunder()/200f:0;
        coin.stolen(hero);coin.settle();hatchling.detachAll(hero.belongings.backpack);
        if(heads){
            companion.collect(hero.belongings.backpack);companion.arrive();
            GLog.p(Messages.get(FickleDoubloon.class,"companion"));
        }else{
            Mimic hostile=Mimic.spawnAt(cell,HatchlingMimic.mimicType(Dungeon.depth));
            hostile.HP=hostile.HT=Math.round(hostile.HT*1.5f);hostile.forfeitedCoin=coin;
            hostile.items.removeIf(i->i instanceof FickleDoubloon||i instanceof HatchlingMimic);
            hostile.items.add(HatchlingMimic.wealthReward(FickleDoubloon.class));
            HatchlingMimic.awaken(hostile);GameScene.add(hostile);
            Buff.affect(hero,FickleDoubloon.Luck.class).start(false,Math.max(1,coin.level()),false);
            GLog.n(Messages.get(FickleDoubloon.class,"betrayal"));
        }
        Item.updateQuickslot();return true;
    }
    public static void meal(HatchlingMimic.Tier tier){
        FickleDoubloon c=FickleDoubloon.active();if(c!=null&&tier.ordinal()>=HatchlingMimic.Tier.STANDARD.ordinal())c.charge(Dungeon.hero,1);
    }
    public static void ordinaryArtifact(){
        FickleDoubloon c=FickleDoubloon.active();if(c!=null)Buff.affect(Dungeon.hero,FickleDoubloon.Luck.class).start(false,Math.max(1,c.level()),false);
    }
}
