// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Image;
import com.watabou.utils.*;

/** Inspectable scenery, not an item, heap, creature or additional loot roll. */
public class FreshRemains implements Bundlable {
    public int pos;
    public boolean seen;
    private Class<? extends Mob> victim;
    private Class<? extends CharSprite> spriteClass;
    public FreshRemains() {}
    public FreshRemains(Mob mob) { pos=mob.pos; victim=mob.getClass(); spriteClass=mob.spriteClass; }
    public String name() { return Messages.get(this,"name",Messages.get(victim,"name")); }
    public String description() {
        return Messages.get(this,"desc",Messages.get(victim,"name"))
                +(Bestiary.encounterCount(LurkingHorror.class)>0?"\n\n"+Messages.get(this,"recognize"):"");
    }
    public Image image() {
        CharSprite sprite=Reflection.newInstance(spriteClass);
        Image image=new Image(sprite);
        image.frame(sprite.remainsFrame());
        RectF bounds=GameGeometry.opaqueBounds(image.texture,image.frame());
        int x=Math.round(image.frame().left*image.texture.width),y=Math.round(image.frame().top*image.texture.height);
        image.frame(image.texture.uvRect(x+bounds.left,y+bounds.top,x+bounds.right,y+bounds.bottom));
        GameGeometry.fitBox(image,14,10);
        image.hardlight(.72f,.58f,.55f);
        sprite.destroy();
        return image;
    }
    @Override public void storeInBundle(Bundle b) { b.put("pos",pos); b.put("seen",seen); b.put("victim",victim); b.put("sprite",spriteClass); }
    @SuppressWarnings("unchecked")
    @Override public void restoreFromBundle(Bundle b) {
        pos=b.getInt("pos"); seen=b.getBoolean("seen"); victim=b.getClass("victim"); spriteClass=b.getClass("sprite");
    }
}
