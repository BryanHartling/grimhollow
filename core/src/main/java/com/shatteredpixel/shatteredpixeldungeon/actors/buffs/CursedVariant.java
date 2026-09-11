// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
/** Separate spawn identity from the dispellable curse, and never roll again on reload. */
public class CursedVariant extends Buff {
    public NecroCurse.Kind kind;
    private boolean lootDropped;
    public static boolean eligible(Mob mob){
        return mob instanceof Rat||mob instanceof Gnoll||mob instanceof Crab||mob instanceof Skeleton||mob instanceof Thief||mob instanceof Bat||mob instanceof Brute||mob instanceof Shaman||mob instanceof Monk||mob instanceof Warlock||mob instanceof Golem||mob instanceof Succubus||mob instanceof Eye||mob instanceof Scorpio;
    }
    public static void roll(Mob mob){if(eligible(mob)&&!(mob instanceof Hexcaster)&&Random.Int(10)==0)apply(mob,NecroCurse.Kind.values()[Random.Int(NecroCurse.Kind.values().length)]);}
    public static void apply(Mob mob,NecroCurse.Kind kind){
        if(mob.buff(CursedVariant.class)!=null)return;
        CursedVariant variant=new CursedVariant();variant.kind=kind;variant.attachTo(mob);
        mob.HT=Math.round(mob.HT*1.3f);mob.HP=Math.round(mob.HP*1.3f);
        NecroCurse curse=NecroCurse.apply(mob,kind,5);if(curse!=null)curse.permanent=true;
    }
    public void onHit(Char victim){if(victim==Dungeon.hero)NecroCurse.apply(victim,kind,5);}
    public void drop(){
        if(!lootDropped){lootDropped=true;com.shatteredpixel.shatteredpixeldungeon.items.Heap heap=Dungeon.level.drop(Generator.randomUsingDefaults(),target.pos);if(heap.sprite!=null)heap.sprite.drop();}
    }
    @Override public void fx(boolean on){if(on)target.sprite.aura(0x7BB33B,6);else target.sprite.clearAura();}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("curse_kind",kind);b.put("loot_dropped",lootDropped);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);kind=b.getEnum("curse_kind",NecroCurse.Kind.class);lootDropped=b.getBoolean("loot_dropped");}
}
