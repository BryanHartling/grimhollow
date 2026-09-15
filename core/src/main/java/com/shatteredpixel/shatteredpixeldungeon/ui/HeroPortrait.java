// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.GL20;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.watabou.noosa.Image;

/** A face cropped from that class's full painting, independent of world armor sprites. */
public class HeroPortrait extends Image {
    public HeroPortrait(HeroClass hero, float size) {
        super("interfaces/painted_portraits.png");
        int index=hero.ordinal();
        frame(index%3*128,index/3*128,128,128);
        logicalSize(size,size);
        texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
    }
}
