// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.*;
import com.watabou.noosa.ui.Component;
import java.util.List;

public class WndMessageHistory extends Window {
    public WndMessageHistory(){
        int w=Math.min(220,(int)PixelScene.uiCamera.width-30);
        int h=Math.min(190,(int)PixelScene.uiCamera.height-36);
        resize(w,h);
        IconTitle title=new IconTitle(Icons.JOURNAL.get(),Messages.get(this,"title"));
        title.setRect(0,0,w,0);add(title);
        Component content=new Component();float y=0;
        List<String> history=MessageHistory.snapshot();
        if(history.isEmpty())history.add(Messages.get(this,"empty"));
        for(String message:history){
            int color=CharSprite.DEFAULT;
            if(message.startsWith(GLog.POSITIVE)){color=CharSprite.POSITIVE;message=message.substring(3);}
            else if(message.startsWith(GLog.NEGATIVE)){color=CharSprite.NEGATIVE;message=message.substring(3);}
            else if(message.startsWith(GLog.WARNING)){color=CharSprite.WARNING;message=message.substring(3);}
            else if(message.startsWith(GLog.HIGHLIGHT)){color=CharSprite.NEUTRAL;message=message.substring(3);}
            RenderedTextBlock row=PixelScene.renderTextBlock(message,6);
            row.setHightlighting(false);row.maxWidth(w-3);row.hardlight(color);row.setPos(0,y);
            content.add(row);y=row.bottom()+4;
        }
        content.setSize(w,y);
        ScrollPane pane=new ScrollPane(content);add(pane);
        pane.setRect(0,title.bottom()+4,w,h-title.bottom()-4);
        pane.scrollTo(0,Float.MAX_VALUE);
    }
}
