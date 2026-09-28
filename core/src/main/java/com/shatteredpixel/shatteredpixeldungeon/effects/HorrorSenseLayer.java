// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GameGeometry;
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
    private static float warningRemaining;
    private Image warning;
    public HorrorSenseLayer() { warningCell=-1; warningRemaining=0; }
    public static void warn(int cell) { warningCell=cell; warningRemaining=.8f; }
    public static Image silhouette() {
        Image image=new Image("sprites/lurking_horror.png");
        image.frame(0,0,256,256); GameGeometry.fitBox(image,18,16);
        return image;
    }
    @Override public void update() {
        HashSet<Integer> used=new HashSet<>();
        for(Mob mob:Dungeon.level.mobs.toArray(new Mob[0])) if(mob instanceof LurkingHorror) {
            LurkingHorror h=(LurkingHorror)mob;
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
        if(warningRemaining>0 && warningCell>=0) {
            if(warning==null) { warning=new Image("effects/readability.png"); warning.frame(0,0,64,64); warning.logicalSize(12,12); warning.hardlight(.65f,.7f,.85f); add(warning); }
            PointF p=DungeonTilemap.tileCenterToWorld(warningCell);
            PointF hero=DungeonTilemap.tileCenterToWorld(Dungeon.hero.pos);
            float dx=p.x-hero.x, dy=p.y-hero.y;
            Camera camera=Camera.main;
            float halfW=Math.max(1,camera.width/2f-12), halfH=Math.max(1,camera.height/2f-12);
            float ratio=1/Math.max(Math.abs(dx)/halfW,Math.abs(dy)/halfH);
            warning.x=camera.scroll.x+camera.width/2f+dx*ratio-6;
            warning.y=camera.scroll.y+camera.height/2f+dy*ratio-6;
            warning.alpha(Math.min(.7f,warningRemaining)); warning.visible=true;
            warningRemaining-=Game.elapsed;
        } else if(warning!=null) warning.visible=false;
        super.update();
    }
}
