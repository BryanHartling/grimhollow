// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

/** Text-only scroll viewport with visible controls, separate from item actions. */
public class DescriptionPane extends ScrollPane {
    private IconButton up,down;
    public DescriptionPane(RenderedTextBlock text,int width){super(body(text,width));}
    private static Component body(RenderedTextBlock text,int width){
        text.maxWidth(width-12);text.setPos(0,0);
        Component body=new Component();body.add(text);
        // Scissor rounding and glyph descenders must not clip the last baseline.
        body.setSize(width,(float)Math.ceil(text.height())+4);
        return body;
    }
    @Override protected void createChildren(){
        super.createChildren();
        up=arrow(-90,-1);add(up);
        down=arrow(90,1);add(down);
        thumb.hardlight(0xDBB675);thumb.am=.9f;
    }
    private IconButton arrow(float angle,int direction){
        Image image=Icons.ARROW.get();image.logicalSize(8,8);image.originToCenter();image.angle=angle;
        return new IconButton(image){
            @Override protected void onClick(){scrollTo(0,content.camera.scroll.y+direction*Math.max(12,DescriptionPane.this.height-12));}
            @Override protected String hoverText(){return Messages.get(DescriptionPane.class,direction<0?"up":"down");}
        };
    }
    @Override protected void layout(){
        super.layout();
        controller.width=Math.max(1,width-12);
        up.setRect(right()-11,y,10,10);down.setRect(right()-11,bottom()-10,10,10);
        trim();
    }
    @Override public void scrollTo(float x,float y){super.scrollTo(x,y);trim();}
    @Override public synchronized void update(){super.update();trim();}
    private void trim(){
        boolean more=content.height()>height+.1f;
        up.visible=down.visible=thumb.visible=more;
        up.enable(more&&content.camera.scroll.y>.1f);
        down.enable(more&&content.camera.scroll.y+height<content.height()-.1f);
        if(more){
            float track=Math.max(1,height-24),size=Math.max(4,track*height/content.height());
            thumb.scale.set(2,size);thumb.x=right()-7;
            thumb.y=y+12+(track-size)*content.camera.scroll.y/(content.height()-height);
        }
    }
}
