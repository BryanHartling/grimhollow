// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
/** Fixed painted composition; logical UI dimensions remain independent of the texture. */
public class TitleBackground extends Component {
    public static float SCROLL_SPEED = 15f;
    public static void reset() {}
    public TitleBackground(int width, int height) {
        this.width=width; this.height=height;
        Image image=new Image("interfaces/title_grimhollow.png");
        float scale=Math.max(width/image.width, height/image.height);
        image.scale.set(scale); image.x=(width-image.width())/2; image.y=(height-image.height())/2;
        add(image);
    }
}
