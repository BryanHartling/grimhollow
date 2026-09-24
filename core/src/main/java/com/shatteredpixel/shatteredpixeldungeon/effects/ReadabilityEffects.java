// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.*;
import com.watabou.utils.RectF;

/** Authored detail sprites. All dimensions below are logical world units. */
public final class ReadabilityEffects {
    public static final String ATLAS="effects/readability.png";
    public static final int LOCK=5, MIST=6, DROP=7;
    private static com.watabou.gltextures.SmartTexture filtered;
    public static void frame(Image image,int index,float width,float height){
        com.watabou.gltextures.SmartTexture texture=com.watabou.gltextures.TextureCache.get(ATLAS);
        if(texture!=filtered){texture.filter(Texture.LINEAR,Texture.LINEAR);filtered=texture;}
        if(image.texture!=texture)image.texture(texture);
        image.frame(new RectF((index*64+.5f)/512f,.5f/64f,
                ((index+1)*64-.5f)/512f,63.5f/64f));
        image.logicalSize(width,height);
    }

    /** Draw after wall caps, but before fog; opening the door removes its lock. */
    public static class Locks extends Group {
        private final Image[] locks=new Image[Dungeon.level.length()];
        @Override public void update(){
            int width=Dungeon.level.width();
            for(int cell=0;cell<locks.length;cell++){
                int tile=Dungeon.level.map[cell];
                boolean shown=(tile==Terrain.LOCKED_DOOR || tile==Terrain.HERO_LKD_DR || tile==Terrain.CRYSTAL_DOOR)
                        && (Dungeon.level.heroFOV[cell] || Dungeon.level.visited[cell] || Dungeon.level.mapped[cell]);
                if(shown && locks[cell]==null){
                    Image lock=locks[cell]=new Image(ATLAS);frame(lock,LOCK,6,7);
                    add(lock);
                }
                if(shown){
                    // Sideways leaves live in the wall overlay one cell north of
                    // their walkable threshold, unlike front-facing door leaves.
                    boolean side=cell>=width && com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.wallStitcheable(Dungeon.level.map[cell-width]);
                    locks[cell].x=cell%width*16+5;locks[cell].y=cell/width*16+(side?-8:3);
                }
                if(locks[cell]!=null)locks[cell].visible=shown;
            }
        }
    }
}
