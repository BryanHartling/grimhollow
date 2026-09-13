// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.GL20;
import com.watabou.noosa.Game;
import com.watabou.noosa.Halo;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

/** Presentation-only motion. Never consumes the dungeon's random generator. */
public class TitleBackground extends Component {
    public static float SCROLL_SPEED = 15f;
    public static void reset() {}
    private final Image painting;
    private final Image[] mist = new Image[2];
    private final Halo[] braziers = new Halo[2];
    private final Halo[] embers = new Halo[24];
    private float time;

    public TitleBackground(int width, int height) {
        this.width=width; this.height=height;
        painting=new Image("interfaces/title_grimhollow.png");
        painting.texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
        painting.scale.set(Math.max(width/painting.width,height/painting.height)*1.025f);
        add(painting);
        for(int i=0;i<2;i++) {
            braziers[i]=new Halo(width*.075f,0xFF9D4D,.22f);
            add(braziers[i]);
            mist[i]=new Image("interfaces/title_mist.png");
            mist[i].texture.filter(GL20.GL_LINEAR,GL20.GL_LINEAR);
            mist[i].logicalSize(width*1.25f,height*.3f);
            mist[i].flipHorizontal=i==1;
            add(mist[i]);
        }
        for(int i=0;i<embers.length;i++) {
            embers[i]=new Halo(.35f+(i%3)*.12f,0xFFD49B,.5f);
            add(embers[i]);
        }
        animate();
    }

    @Override public void update() {
        super.update();
        time+=Game.elapsed;
        animate();
    }

    private void animate() {
        painting.x=(width-painting.width())/2+(float)Math.sin(time*.12f)*width*.006f;
        painting.y=(height-painting.height())/2+(float)Math.cos(time*.09f)*height*.003f;
        for(int i=0;i<2;i++) {
            float bx=painting.x+painting.width()*(i==0?.108f:.893f);
            float by=painting.y+painting.height()*.713f;
            braziers[i].point(bx,by);
            braziers[i].alpha(.2f+.035f*(float)Math.sin(time*9.1f+i*2)+.025f*(float)Math.sin(time*16.3f+i));
            mist[i].x=-width*.15f+(float)Math.sin(time*.065f+i*2)*width*.13f;
            mist[i].y=height*(.64f+i*.14f)+(float)Math.sin(time*.09f+i)*height*.02f;
            mist[i].alpha(.055f+.02f*(float)Math.sin(time*.17f+i*2));
        }
        for(int i=0;i<embers.length;i++) {
            float progress=(time*(.09f+(i%4)*.009f)+i*.618034f)%1f;
            float bx=painting.x+painting.width()*(i%2==0?.108f:.893f);
            float by=painting.y+painting.height()*.713f;
            embers[i].point(bx+(float)Math.sin(i*2.7f+progress*5)*width*.01f,by-progress*height*.19f);
            embers[i].alpha((float)Math.sin(progress*Math.PI)*.75f);
        }
    }
}
