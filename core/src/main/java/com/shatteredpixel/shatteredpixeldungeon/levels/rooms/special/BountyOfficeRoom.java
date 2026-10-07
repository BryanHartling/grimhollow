// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.BountyBoard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;

/** Added alongside, rather than drawn from, the normal special-room budget. */
public class BountyOfficeRoom extends SpecialRoom {
    @Override public int minWidth() { return 7; }
    @Override public int minHeight() { return 7; }
    @Override public int maxWidth() { return 9; }
    @Override public int maxHeight() { return 9; }
    @Override public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY_SP);
        for (Door d : connected.values()) d.set(Door.Type.REGULAR);
        BountyBoard.planShop();
        Cole cole = new Cole(); cole.pos = BountyBoard.officeCell = level.pointToCell(center());
        level.mobs.add(cole);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BountyNotice board=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BountyNotice();
        level.mobs.add(board);
        decorate(level,cole.pos,board,true);
    }
    /** Upgrade an existing office's presentation without recreating stock or receipts. */
    public static void refreshPresentation(Level level){
        if(!(level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)
                || level.customTiles.stream().anyMatch(t->t instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles))return;
        for(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room room:((com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)level).rooms()){
            if(!(room instanceof BountyOfficeRoom))continue;
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BountyNotice board=null;
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:level.mobs)
                if(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BountyNotice)board=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BountyNotice)mob;
            ((BountyOfficeRoom)room).decorate(level,BountyBoard.officeCell,board,false);
            return;
        }
    }
    private void decorate(Level level,int officeCell,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.BountyNotice board,boolean newStock){
        int cx=officeCell%level.width(),cy=officeCell/level.width();
        if(board!=null){
        board.pos=cx+(top+1)*level.width();
        if(Math.abs(cx-entrance().x)+Math.abs(top+1-entrance().y)<=1)board.pos++;
        if(board.sprite!=null)board.sprite.place(board.pos);
        }
        com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles art=
                new com.shatteredpixel.shatteredpixeldungeon.tiles.BountyOfficeTiles(left,top,width(),height());
        for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)art.put(cx+dx,cy+dy,(dy+1)*3+dx+1);
        art.put(cx-1,top+2,16);art.put(cx,top+2,17);
        art.put(left+1,top+1,20);art.put(right-1,top+1,19);art.put(right-2,top+1,18);
        java.util.ArrayList<Heap> oldDisplays=new java.util.ArrayList<>();
        if(!newStock)for(Heap heap:level.heaps.valueList())if(BountyBoard.owns(heap))oldDisplays.add(heap);
        java.util.ArrayList<Integer> candidates=new java.util.ArrayList<>(),placed=new java.util.ArrayList<>();
        for(int y=top+3;y<bottom;y++)for(int x=left+1;x<right;x++){
            int cell=x+y*level.width();
            if(cell==officeCell || x==cx && y<bottom-1 || Math.abs(x-entrance().x)+Math.abs(y-entrance().y)<=1)continue;
            Heap existing=level.heaps.get(cell);
            if(!newStock && (existing!=null&&!BountyBoard.owns(existing)
                    || com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.pos==cell))continue;
            candidates.add(cell);
        }
        boolean spaced=chooseDisplays(candidates,placed,level.width(),officeCell);
        if(!newStock && !spaced){
            // A player can fill the office with loot. Never overwrite their heaps.
            for(Heap heap:oldDisplays)art.put(heap.pos%level.width(),heap.pos/level.width(),21);
            level.customTiles.add(art);return;
        }
        if(!spaced)throw new IllegalStateException("Office needs five spaced display positions");
        for(Heap heap:oldDisplays)level.heaps.remove(heap.pos);
        for(int index=0;index<5;index++){
            int cell=placed.get(index);
            art.put(cell%level.width(),cell/level.width(),21);
            if(newStock && !BountyBoard.shopClosed && BountyBoard.stock[index] != null) {
                Heap heap = level.drop(BountyBoard.stock[index], cell);
                heap.type = Heap.Type.FOR_SALE; heap.coleSlot = index;
            }else if(!newStock){
                for(Heap heap:oldDisplays)if(heap.coleSlot==index){
                    heap.pos=cell;level.heaps.put(cell,heap);if(heap.sprite!=null)heap.sprite.place(cell);
                }
            }
        }
        level.customTiles.add(art);
    }
    private static boolean chooseDisplays(java.util.ArrayList<Integer> candidates,java.util.ArrayList<Integer> placed,int width,int officeCell){
        if(placed.size()==5)return true;
        java.util.ArrayList<Integer> ranked=new java.util.ArrayList<>(candidates);
        ranked.sort((a,b)->Integer.compare(separation(b,placed,width,officeCell),separation(a,placed,width,officeCell)));
        for(int candidate:ranked){
            if(placed.contains(candidate)||separation(candidate,placed,width,officeCell)<2)continue;
            placed.add(candidate);
            if(chooseDisplays(candidates,placed,width,officeCell))return true;
            placed.remove(placed.size()-1);
        }
        return false;
    }
    private static int separation(int cell,java.util.ArrayList<Integer> placed,int width,int officeCell){
        if(placed.isEmpty())return distance(cell,officeCell,width);
        int nearest=Integer.MAX_VALUE;for(int other:placed)nearest=Math.min(nearest,distance(cell,other,width));return nearest;
    }
    private static int distance(int a,int b,int width){return Math.abs(a%width-b%width)+Math.abs(a/width-b/width);}
}
