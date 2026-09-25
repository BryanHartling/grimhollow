// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.Game;

/** Local, scrollable evidence. Sharing is always an explicit player action. */
public class WndSupportPrompt extends WndTitledMessage {
    public WndSupportPrompt(){this(ShatteredPixelDungeon.lastIssue());}
    public WndSupportPrompt(String issue){
        super(Icons.INFO.get(),"Report an issue",details(issue));
        final String report=details(issue);
        float y=height+3;
        RedButton copy=new RedButton("Copy report",7){
            @Override protected void onClick(){
                try{Gdx.app.getClipboard().setContents(report);text("Copied");}
                catch(RuntimeException unavailable){text("Use screenshot");}
            }
        };add(copy);copy.setRect(0,y,width/2f-1,20);
        RedButton close=new RedButton("Close",7){@Override protected void onClick(){hide();}};
        add(close);close.setRect(width/2f+1,y,width/2f-1,20);
        RedButton github=new RedButton("Open GitHub",7){@Override protected void onClick(){ShatteredPixelDungeon.platform.openURI("https://github.com/bryanhartling/grimhollow/issues");}};
        add(github);github.setRect(0,y+22,width,20);resize(width,(int)y+42);
    }
    public static String details(String issue){
        StringBuilder text=new StringBuilder("Grimhollow ").append(Game.version).append(" (build ").append(Game.versionCode).append(")");
        if(Gdx.app!=null)text.append("\nPlatform: ").append(Gdx.app.getType());
        if(Gdx.graphics!=null)text.append("\nDisplay: ").append(Gdx.graphics.getWidth()).append(" x ").append(Gdx.graphics.getHeight()).append("; FPS: ").append(Gdx.graphics.getFramesPerSecond());
        if(Dungeon.hero!=null)text.append("\nHero: ").append(Dungeon.hero.heroClass).append(" level ").append(Dungeon.hero.lvl)
                .append("\nFloor: ").append(Dungeon.depth).append("; branch: ").append(Dungeon.branch).append("; cell: ").append(Dungeon.hero.pos)
                .append("\nSeed: ").append(Dungeon.seed).append("; Playtest: ").append(Playtest.enabled());
        text.append("\n\n").append(issue==null?"No error was captured in this session. Describe what happened when sharing this report.":issue);
        return text.toString();
    }
    @Override protected boolean useHighlighting(){return false;}
    @Override protected float targetHeight(){return Math.min(220,PixelScene.uiCamera.height-90);}
}
