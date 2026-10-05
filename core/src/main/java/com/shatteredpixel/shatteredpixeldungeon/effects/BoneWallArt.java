// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.graphics.GL20;
import com.watabou.noosa.Image;

/** Four stable painted arrangements, shared by the wand, prison and inspection. */
public final class BoneWallArt {
    private BoneWallArt() {}
    public static final String TEXTURE="environment/painted_bone_wall.png";
    public static Image image(int cell){
        Image image=new Image(TEXTURE);frame(image,cell);return image;
    }
    public static void frame(Image image,int cell){
        image.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
        int variant=EnhancedEffects.phase(cell)%4,x=variant%2*128,y=variant/2*128;
        image.frame(image.texture.uvRect(x,y,x+128,y+128));image.logicalSize(16,20);
    }
}
