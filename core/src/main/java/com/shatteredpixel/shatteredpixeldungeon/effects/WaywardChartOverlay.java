// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.*;
import java.util.Arrays;

/** A separate faded-ink veil and treasure X; neither can reveal terrain or unseen creatures. */
public class WaywardChartOverlay extends Group {
    private final Tilemap veil;
    private int revision=-1;
    public WaywardChartOverlay(){
        veil=new Tilemap("effects/wayward_veil.png",new TextureFilm("effects/wayward_veil.png",64,64)){
            {cellSize(GameGeometry.WORLD_TILE_SIZE,GameGeometry.WORLD_TILE_SIZE);}
            @Override protected boolean needsRender(int pos){
                return super.needsRender(pos) && !Dungeon.level.heroFOV[pos]
                        && (Dungeon.level.visited[pos] || Dungeon.level.mapped[pos]) && !WaywardJourney.protectedCell(pos);
            }
            @Override protected NoosaScript script(){return NoosaScriptNoLighting.get();}
        };
        add(veil);refresh();
    }
    private void refresh(){
        for(Gizmo child:members.toArray(new Gizmo[0]))if(child!=null && child!=veil)child.killAndErase();
        int[] cells=new int[Dungeon.level.length()];Arrays.fill(cells,-1);
        boolean[] mask=WaywardJourney.veilCells();for(int i=0;i<mask.length;i++)if(mask[i])cells[i]=0;
        veil.map(cells,Dungeon.level.width());veil.updateMap();
        for(WaywardJourney.Cache c:WaywardJourney.caches())if(c.depth==Dungeon.depth && c.branch==Dungeon.branch && c.remaining>0){
            ItemSprite marker=new ItemSprite(ItemSpriteSheet.WAYWARD_MARKER){
                @Override public void update(){visible=!Dungeon.level.heroFOV[c.pos];}
                @Override protected NoosaScript script(){return NoosaScriptNoLighting.get();}
            };
            marker.logicalSize(12,12);marker.x=(c.pos%Dungeon.level.width())*16+2;marker.y=(c.pos/Dungeon.level.width())*16+2;
            add(marker);
        }
        revision=WaywardJourney.revision;
    }
    @Override public void update(){
        // Observe advances the revision when a live memory needs a new visibility mask.
        // Ordinary movement without a forgotten room must not rebuild a whole tilemap.
        if(revision!=WaywardJourney.revision)refresh();
        super.update();
    }
}
