// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.*;
import com.watabou.utils.PointF;
import java.util.HashMap;
import java.util.HashSet;

/** Entity silhouettes above fog: no FOV, visited, mapped, heap or terrain mutation. */
public class HorrorSenseLayer extends Group {
    private final HashMap<Integer,Image> markers=new HashMap<>();
    private static int warningCell=-1;
    private static volatile float arrivalRemaining;
    private Image warning;
    private Notice notice;
    public HorrorSenseLayer() { warningCell=-1; arrivalRemaining=0; }
    public static void warn(int cell) { warningCell=cell; }
    public static void arrival() { arrivalRemaining=6; }
    public boolean ambushNoticeVisible(){return notice!=null && notice.visible && notice.ambush;}
    public static Image silhouette() {
        Image image=new Image("sprites/lurking_horror.png");
        image.frame(0,0,256,256); GameGeometry.fitBox(image,18,16);
        return image;
    }
    @Override public void update() {
        HashSet<Integer> used=new HashSet<>();
        boolean ambush=false;
        for(Mob mob:Dungeon.level.mobs.toArray(new Mob[0])) if(mob instanceof LurkingHorror) {
            LurkingHorror h=(LurkingHorror)mob;
            if(h.isAlive() && h.phase()==LurkingHorror.Phase.WARNING){ambush=true;warningCell=h.pos;}
            if(h.isAlive() && h.sensed() && !Dungeon.level.heroFOV[h.pos]) {
                used.add(h.id()); Image image=markers.get(h.id());
                if(image==null) { image=silhouette(); markers.put(h.id(),image); add(image); }
                PointF p=DungeonTilemap.tileCenterToWorld(h.pos);
                image.x=p.x-image.width()/2; image.y=p.y-image.height()/2;
                image.hardlight(.60f,.67f,.8f); image.alpha(.85f);
            }
        }
        for(Integer id:new HashSet<>(markers.keySet())) if(!used.contains(id)) {
            Image image=markers.remove(id); remove(image); image.destroy();
        }
        if(ambush && warningCell>=0) {
            if(warning==null) { warning=new Image("effects/readability.png"); warning.frame(0,0,64,64); warning.logicalSize(24,24); warning.hardlight(1f,.7f,.3f); add(warning); }
            PointF p=DungeonTilemap.tileCenterToWorld(warningCell);
            PointF hero=DungeonTilemap.tileCenterToWorld(Dungeon.hero.pos);
            float dx=p.x-hero.x, dy=p.y-hero.y;
            Camera camera=Camera.main;
            float halfW=Math.max(1,camera.width/2f-16), halfH=Math.max(1,camera.height/2f-16);
            float ratio=1/Math.max(Math.abs(dx)/halfW,Math.abs(dy)/halfH);
            warning.x=camera.scroll.x+camera.width/2f+dx*ratio-12;
            warning.y=camera.scroll.y+camera.height/2f+dy*ratio-12;
            warning.alpha(.8f); warning.visible=true;
        } else if(warning!=null) warning.visible=false;
        if(ambush || arrivalRemaining>0){
            if(notice==null){notice=new Notice();add(notice);}
            notice.show(ambush);notice.visible=true;
        }else if(notice!=null)notice.visible=false;
        arrivalRemaining=Math.max(0,arrivalRemaining-Game.elapsed);
        super.update();
    }
    /** No pointer area or modal pause: the fresh-action response barrier remains in AI. */
    private static final class Notice extends com.watabou.noosa.ui.Component {
        final NinePatch panel;final RenderedTextBlock title,text;boolean ambush;
        Notice(){
            camera=PixelScene.uiCamera;
            panel=Chrome.get(Chrome.Type.TOAST);add(panel);
            title=PixelScene.renderTextBlock(8);title.align(RenderedTextBlock.CENTER_ALIGN);add(title);
            text=PixelScene.renderTextBlock(6);text.align(RenderedTextBlock.CENTER_ALIGN);add(text);
        }
        void show(boolean attack){
            if(width==0 || ambush!=attack){
                ambush=attack;
                title.text(Messages.get(LurkingHorror.class,attack?"warning_title":"omen_title"));
                text.text(Messages.get(LurkingHorror.class,attack?"warning_hud":"omen"));
                title.hardlight(attack?0xFFCE86:0xE6CDA4);text.hardlight(0xF7E9D1);
            }
            setRect((camera.width-Math.min(190,camera.width-14))/2,Math.max(32,camera.height*.13f),Math.min(190,camera.width-14),0);
        }
        @Override protected void layout(){
            title.maxWidth((int)width-12);title.setPos(x+(width-title.width())/2,y+6);
            text.maxWidth((int)width-12);text.setPos(x+(width-text.width())/2,title.bottom()+3);
            height=text.bottom()-y+6;panel.x=x;panel.y=y;panel.size(width,height);
        }
    }
}
