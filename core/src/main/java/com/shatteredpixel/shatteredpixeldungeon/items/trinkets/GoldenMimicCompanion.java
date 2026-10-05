// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import java.util.*;

/** The transformed harness owns recovery and inherited fortune, not a reserved artifact slot. */
public class GoldenMimicCompanion extends Trinket {
    public int coinLevel,hatchlingLevel;
    public float inheritedGold,inheritedQuality,inheritedSearch;
    private int recovery,knockedOutFloor=-1,lastFloor=-1;
    private boolean resting;
    private final HashSet<Integer> charmed=new HashSet<>();
    {image=ItemSpriteSheet.HATCHLING_MIMIC;level(3);bones=false;}
    public static GoldenMimicCompanion carried(){return Dungeon.hero==null?null:Dungeon.hero.belongings.getItem(GoldenMimicCompanion.class);}
    @Override protected int upgradeEnergyCost(){return 0;}
    @Override public Item upgrade(){return this;}
    @Override public String statsDesc(){return Messages.get(this,resting?"resting":"awake");}
    public boolean resting(){return resting;}
    public GoldenMimicAlly ally(){for(Mob m:Dungeon.level.mobs)if(m instanceof GoldenMimicAlly)return (GoldenMimicAlly)m;return null;}
    public void knockOut(){resting=true;recovery=BalanceTuning.get(BalanceTuning.Key.COMPANION_RECOVERY);knockedOutFloor=floor();lastFloor=floor();}
    private static int floor(){return Dungeon.depth+100*Dungeon.branch;}
    public void arrive(){
        if(resting && lastFloor!=floor() && floor()!=knockedOutFloor && recovery<=0)resting=false;
        lastFloor=floor();ensureAlly();
    }
    public void tick(){if(recovery>0)recovery--;ensureAlly();}
    public void ensureAlly(){
        if(!resting && ally()==null){int cell=HatchlingMimic.closestSpawn(Dungeon.hero.pos);if(cell>=0){GoldenMimicAlly m=new GoldenMimicAlly();m.pos=cell;m.refreshStrength(true);GameScene.add(m);}}
    }
    @Override public boolean collect(Bag bag){boolean success=super.collect(bag);if(success&&Dungeon.hero!=null)Buff.affect(Dungeon.hero,Bond.class);return success;}
    public boolean charm(Mimic m){
        if(m.getClass()!=Mimic.class||charmed.contains(floor()))return false;
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff.affectAndLoot(m,Dungeon.hero,HatchlingMimic.Kinship.class);
        if(m.buff(HatchlingMimic.Kinship.class)==null)return false;charmed.add(floor());m.kinship(true);return true;
    }
    public static class Bond extends Buff {
        {revivePersists=true;}
        @Override public boolean act(){GoldenMimicCompanion item=carried();if(item==null){detach();return true;}item.tick();spend(TICK);return true;}
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("coin_level",coinLevel);b.put("hatchling_level",hatchlingLevel);b.put("gold",inheritedGold);b.put("quality",inheritedQuality);b.put("search",inheritedSearch);b.put("recovery",recovery);b.put("ko_floor",knockedOutFloor);b.put("last_floor",lastFloor);b.put("resting",resting);b.put("charmed",charmed.stream().mapToInt(Integer::intValue).toArray());}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);level(3);coinLevel=b.getInt("coin_level");hatchlingLevel=b.getInt("hatchling_level");inheritedGold=b.getFloat("gold");inheritedQuality=b.getFloat("quality");inheritedSearch=b.getFloat("search");recovery=b.getInt("recovery");knockedOutFloor=b.getInt("ko_floor");lastFloor=b.getInt("last_floor");resting=b.getBoolean("resting");charmed.clear();for(int n:b.getIntArray("charmed"))charmed.add(n);}
}
