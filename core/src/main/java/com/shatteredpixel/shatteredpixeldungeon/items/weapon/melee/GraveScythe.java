// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
public class GraveScythe extends BoneScythe {
    {tier=5;image=com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.GRAVE_SCYTHE;}
    @Override public int min(int level){return 10+level;}
    @Override public int max(int level){return 36+(tier+1)*level;}
    @Override protected float sweep(){return 1f;}
    @Override public int proc(com.shatteredpixel.shatteredpixeldungeon.actors.Char a,com.shatteredpixel.shatteredpixeldungeon.actors.Char d,int damage){
        if(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NecroCurse.find(d)!=null||d.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex.class)!=null)damage=Math.round(damage*1.1f);
        return super.proc(a,d,damage);
    }
}
