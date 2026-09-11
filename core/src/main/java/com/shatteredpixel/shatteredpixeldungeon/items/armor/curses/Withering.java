// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.items.armor.curses;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Bundle;
public class Withering extends Armor.Glyph {
    private int lost,lastDepth=-1,lastBranch;
    public static int penalty(Hero hero){Armor armor=hero.belongings.armor();return armor!=null&&!armor.boneConstruction&&armor.glyph instanceof Withering?((Withering)armor.glyph).lost:0;}
    public static void arrive(Hero hero){Armor armor=hero.belongings.armor();if(armor!=null&&armor.glyph instanceof Withering){Withering curse=(Withering)armor.glyph;
        if(curse.lastDepth>=0&&Dungeon.branch==curse.lastBranch&&Dungeon.depth>curse.lastDepth)curse.lost+=Dungeon.depth-curse.lastDepth;
        curse.lastDepth=Dungeon.depth;curse.lastBranch=Dungeon.branch;}hero.updateHT(false);}
    public void removed(){lost=0;lastDepth=-1;}
    @Override public int proc(Armor armor,Char a,Char d,int damage){return damage;}
    @Override public boolean curse(){return true;}
    @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0x000000);}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("lost",lost);b.put("last_depth",lastDepth);b.put("last_branch",lastBranch);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);lost=b.getInt("lost");lastDepth=b.contains("last_depth")?b.getInt("last_depth"):-1;lastBranch=b.getInt("last_branch");}
}
