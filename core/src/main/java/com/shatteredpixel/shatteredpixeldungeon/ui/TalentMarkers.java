// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.GL20;
import com.watabou.noosa.Image;

/** Shared, smoothly sampled bronze trim; no per-marker GPU texture. */
public final class TalentMarkers {
    private TalentMarkers() {}
    public static final String TEXTURE="interfaces/painted_talents.png";
    public static Image image(int cell,float size){
        Image result=new Image(TEXTURE);
        result.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
        frame(result,cell,size);
        return result;
    }
    public static void frame(Image image,int cell,float size){
        image.frame(image.texture.uvRect(cell*64,0,(cell+1)*64,64));
        image.logicalSize(size,size);
    }
}
