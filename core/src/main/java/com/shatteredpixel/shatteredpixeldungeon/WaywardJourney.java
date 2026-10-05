// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DoubloonLoot;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WaywardChart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.entrance.EntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.exit.ExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.WaywardMoundTile;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.*;
import java.util.*;
import com.watabou.utils.Random;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

/** Run-owned promises and memory. Never changes map, FOV, visited, mapped or pathfinding. */
public final class WaywardJourney {
    private WaywardJourney() {}
    private static boolean[] entered = new boolean[27], rewarded = new boolean[5];
    private static int[] debt = new int[5];
    private static boolean hoardRewarded;
    private static final ArrayList<Memory> memories = new ArrayList<>();
    private static final ArrayList<Cache> caches = new ArrayList<>();
    public static volatile int revision;

    public static synchronized void reset() {
        entered = new boolean[27]; rewarded = new boolean[5]; debt = new int[5];
        hoardRewarded = false; memories.clear(); caches.clear(); revision++;
    }
    public static boolean eligible() {
        return Dungeon.branch == 0 && Dungeon.depth > 0 && Dungeon.depth < 25
                && Dungeon.depth % 5 != 0 && Dungeon.level instanceof RegularLevel;
    }
    private static boolean mainFloor(){return Dungeon.branch==0 && Dungeon.depth>0 && Dungeon.depth<=25 && Dungeon.level!=null;}
    public static int region(int depth) { return Math.max(0, Math.min(4, (depth-1)/5)); }
    public static boolean regionRewarded(int region) { return rewarded[region]; }
    public static boolean floorEntered(int depth) { return entered[depth]; }
    public static synchronized List<Cache> caches() { return new ArrayList<>(caches); }
    public static synchronized List<Memory> memories() { return new ArrayList<>(memories); }
    public static synchronized Cache cache(int id) { for (Cache c:caches) if(c.id==id)return c; return null; }

    public static synchronized void arrive() {
        if(eligible()) {
            boolean first = !entered[Dungeon.depth];
            entered[Dungeon.depth] = true; // Carrying/acquiring/upgrading later never creates an entry reroll.
            if(first) discover(false);
        } else if(Dungeon.level instanceof DragonHoardLevel && DragonExpedition.dragonSlain) discoverHoard();
    }
    public static int chance(int rank) {
        return BalanceTuning.get(new BalanceTuning.Key[]{CHART_CHANCE_0,CHART_CHANCE_1,CHART_CHANCE_2,CHART_CHANCE_3}[Math.min(3,Math.max(0,rank))]);
    }
    public static synchronized boolean discover(boolean paidMapping) {
        int rank=WaywardChart.rank();
        if(rank<0 || !eligible() || rewarded[region(Dungeon.depth)])return false;
        Random.pushGenerator(Dungeon.seed ^ 0x57415957415244L ^ Dungeon.depth);
        try {
            if(!paidMapping && Random.Int(100)>=chance(rank))return false;
            int cell=placement(false); if(cell<0)return false;
            spawn(cell,rank,false); rewarded[region(Dungeon.depth)]=true; return true;
        } finally { Random.popGenerator(); }
    }
    public static synchronized boolean discoverHoard() {
        int rank=WaywardChart.rank();
        if(rank<0 || hoardRewarded || !DragonExpedition.dragonSlain || Dungeon.branch!=DragonExpedition.BRANCH
                || !(Dungeon.level instanceof DragonHoardLevel))return false;
        Random.pushGenerator(Dungeon.seed ^ 0x574159484F415244L);
        try { int cell=placement(true); if(cell<0)return false;
            spawn(cell,rank,true); hoardRewarded=true;return true;
        } finally {Random.popGenerator();}
    }
    private static int placement(boolean hoard) {
        Level level=Dungeon.level;
        // Known doors are traversable; secret/locked doors are not a valid route to a promise.
        boolean[] route=level.passable.clone();
        for(int i=0;i<route.length;i++)if(level.map[i]==Terrain.SECRET_DOOR || level.map[i]==Terrain.LOCKED_DOOR)route[i]=false;
        PathFinder.buildDistanceMap(Dungeon.hero.pos,route);
        ArrayList<Integer> cells=new ArrayList<>(), distant=new ArrayList<>();
        for(int i=0;i<level.length();i++){
            if(level.map[i]!=Terrain.EMPTY && level.map[i]!=Terrain.EMPTY_SP && level.map[i]!=Terrain.EMPTY_DECO)continue;
            if(!level.insideMap(i) || PathFinder.distance[i]==Integer.MAX_VALUE || level.heaps.get(i)!=null
                    || level.traps.get(i)!=null || level.plants.get(i)!=null || level.findMob(i)!=null
                    || level.getTransition(i)!=null || i==Dungeon.hero.pos)continue;
            if(!hoard){Room room=((RegularLevel)level).room(i);if(!ordinary(room))continue;}
            boolean scenery=false;
            for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap tile:level.customTiles)
                if(i%level.width()>=tile.tileX && i%level.width()<tile.tileX+tile.tileW
                        && i/level.width()>=tile.tileY && i/level.width()<tile.tileY+tile.tileH) {scenery=true;break;}
            if(scenery)continue;
            cells.add(i); if(!level.heroFOV[i])distant.add(i);
        }
        if(!distant.isEmpty())cells=distant;
        return cells.isEmpty()?-1:cells.get(Random.Int(cells.size()));
    }
    private static boolean ordinary(Room room) {
        return room instanceof StandardRoom && !(room instanceof EntranceRoom) && !(room instanceof ExitRoom);
    }
    public static ArrayList<Item> rewards(int rank, boolean hoard) {
        ArrayList<Item> result=new ArrayList<>();
        int gold=Math.round((60+25*Dungeon.depth)*(1+.25f*rank)*BalanceTuning.multiplier(CHART_GOLD)*(hoard?2:1));
        result.add(new Gold(Math.max(1,gold)));
        int count=hoard?2:BalanceTuning.get(new BalanceTuning.Key[]{CHART_ITEMS_0,CHART_ITEMS_1,CHART_ITEMS_2,CHART_ITEMS_3}[rank]);
        for(int n=0;n<count;n++){
            Item item;
            // At least one equipment and one useful consumable; no unique deck/quest/class items.
            if(n==0 || n>1 && Random.Int(2)==0){
                int tier=Math.min(4,region(hoard?DragonExpedition.hunterDepth:Dungeon.depth));
                int type=Random.Int(4);
                item=type==0?Generator.randomWeapon(tier,true):type==1?Generator.randomArmor(tier)
                        :Generator.randomUsingDefaults(type==2?Generator.Category.RING:Generator.Category.WAND);
                if(rank>=2 || hoard){
                    for(int roll=1;roll<BalanceTuning.get(CHART_QUALITY_ROLLS);roll++){
                        Item other=Reflection.newInstance(item.getClass()).random();
                        if(DoubloonLoot.compare(other,item)>0)item=other;
                    }
                }
                if(hoard){item.cursed=false;item.level(Math.max(item.trueLevel(),1+rank));}
            } else {
                Class<?>[] useful={com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class,
                        com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility.class,
                        com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision.class,
                        com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify.class,
                        com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse.class,
                        com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping.class};
                item=(Item)Reflection.newInstance(useful[Random.Int(useful.length)]);
            }
            if(Challenges.isItemBlocked(item))item=new Gold(40+10*Dungeon.depth);
            result.add(item);
        }
        return result;
    }
    private static void spawn(int cell,int rank,boolean hoard) {
        Cache c=new Cache();c.id=caches.size()+1;c.depth=Dungeon.depth;c.branch=Dungeon.branch;c.pos=cell;
        c.region=region(hoard?DragonExpedition.hunterDepth:Dungeon.depth);c.hoard=hoard;
        ArrayList<Item> items=rewards(rank,hoard); c.total=c.remaining=items.size();caches.add(c);
        for(Item item:items)Dungeon.level.drop(item,cell);
        Heap heap=Dungeon.level.heaps.get(cell);heap.waywardCache=c.id;heap.type=Heap.Type.HEAP;
        c.total=c.remaining=heap.size(); // Native stacking may merge identical consumable draws.
        if(heap.sprite!=null)heap.sprite.view(heap).place(cell);
        WaywardMoundTile tile=new WaywardMoundTile();tile.cacheID=c.id;tile.pos(cell);Dungeon.level.customTiles.add(tile);
        if(Game.scene() instanceof GameScene){
            Level owner=Dungeon.level;
            Game.runOnRenderThread(()->{if(Dungeon.level==owner && Game.scene() instanceof GameScene)GameScene.add(tile,false);});
        }
        Notes.addChart(c.id,hoard?DragonExpedition.hunterDepth:c.depth);
        revision++;
        GLog.w(Messages.get(WaywardChart.class,hoard?"hoard_found":"found"));
    }
    /** Call only after an actual hero/hero-owned pickup succeeds, not on opening or failed pickup. */
    public static synchronized void collected(Heap heap) {
        Cache c=cache(heap.waywardCache);if(c==null || c.claimed)return;
        c.claimed=true;debt[c.region]++;revision++;
        captureRooms();assignDebt(c.region);
    }
    public static synchronized void changed(Heap heap) {
        Cache c=cache(heap.waywardCache);if(c==null)return;
        c.remaining=heap.size();if(c.remaining==0)Notes.removeChart(c.id);
        revision++;
    }
    public static synchronized void mapping() {
        boolean restored=false;
        if(Dungeon.branch==0)for(Memory m:memories)if(m.depth==Dungeon.depth && m.forgotten){m.forgotten=false;restored=true;}
        if(restored){revision++;GLog.w(Messages.get(WaywardChart.class,"restored"));}
        // This is a paid exception to the entry-only roll, not another random attempt.
        discover(true);
    }
    /** Snapshot only rooms whose interior terrain has really been explored; keep it bounded. */
    public static synchronized void captureRooms() {
        if(!mainFloor())return;
        if(!(Dungeon.level instanceof RegularLevel)){captureArena();return;}
        RegularLevel level=(RegularLevel)Dungeon.level;
        for(Room room:level.rooms())if(ordinary(room)){
            int anchor=room.left+room.top*level.width();Memory m=null;
            for(Memory previous:memories)if(previous.depth==Dungeon.depth && previous.anchor==anchor){m=previous;break;}
            if(m!=null && m.forgotten)continue;
            ArrayList<Integer> cells=new ArrayList<>();
            for(int y=room.top+1;y<room.bottom;y++)for(int x=room.left+1;x<room.right;x++){
                int cell=x+y*level.width();
                if(level.visited[cell] && !protectedCell(cell))cells.add(cell);
            }
            if(cells.size()<6)continue;
            if(m==null){m=new Memory();m.depth=Dungeon.depth;m.anchor=anchor;m.left=room.left;m.right=room.right;
                m.top=room.top;m.bottom=room.bottom;m.width=level.width();memories.add(m);}
            int limit=Math.min(cells.size(),BalanceTuning.get(CHART_MEMORY_CELLS));
            m.cells=new int[limit];for(int i=0;i<limit;i++)m.cells[i]=cells.get(i);
        }
    }
    /** Cleared boss floors also have explored memories, even without generated Room objects. */
    private static void captureArena(){
        Level level=Dungeon.level;if(level.locked)return;
        Memory previous=null;
        for(Memory m:memories)if(m.depth==Dungeon.depth && m.anchor==-1){previous=m;break;}
        if(previous!=null && previous.forgotten)return;
        boolean[] scanned=new boolean[level.length()];ArrayList<Integer> largest=new ArrayList<>();
        for(int start=0;start<scanned.length;start++)if(!scanned[start] && level.insideMap(start)
                && level.visited[start] && level.passable[start] && !protectedCell(start)){
            ArrayList<Integer> part=new ArrayList<>();ArrayDeque<Integer> queue=new ArrayDeque<>();queue.add(start);scanned[start]=true;
            while(!queue.isEmpty()){
                int cell=queue.remove();part.add(cell);
                for(int delta:new int[]{-1,1,-level.width(),level.width()}){
                    int next=cell+delta;
                    if(level.insideMap(next) && !scanned[next] && level.visited[next] && level.passable[next] && !protectedCell(next)){
                        scanned[next]=true;queue.add(next);
                    }
                }
            }
            if(part.size()>largest.size())largest=part;
        }
        if(largest.size()<6)return;
        Memory m=previous==null?new Memory():previous;m.depth=Dungeon.depth;m.anchor=-1;m.width=level.width();
        int limit=Math.min(largest.size(),BalanceTuning.get(CHART_MEMORY_CELLS));m.cells=new int[limit];
        m.left=level.width();m.top=level.height();m.right=m.bottom=0;
        for(int i=0;i<limit;i++){
            int cell=largest.get(i);m.cells[i]=cell;
            m.left=Math.min(m.left,cell%level.width()-1);m.right=Math.max(m.right,cell%level.width()+1);
            m.top=Math.min(m.top,cell/level.width()-1);m.bottom=Math.max(m.bottom,cell/level.width()+1);
        }
        if(previous==null)memories.add(m);
    }
    private static void assignDebt(int region) {
        while(debt[region]>0){
            ArrayList<Memory> eligible=new ArrayList<>();
            for(Memory m:memories)if(region(m.depth)==region && !m.forgotten
                    && !(Dungeon.branch==0 && m.depth==Dungeon.depth && m.contains(Dungeon.hero.pos)))eligible.add(m);
            if(eligible.isEmpty())return;
            Memory m=eligible.get(Random.Int(eligible.size()));m.forgotten=true;debt[region]--;revision++;
            GLog.w(Messages.get(WaywardChart.class,"forgotten",m.depth));
        }
    }
    public static synchronized void observe() {
        if(Dungeon.hero==null || Dungeon.level==null)return;
        for(Cache c:caches)if(c.depth==Dungeon.depth && c.branch==Dungeon.branch && c.remaining>0){
            Heap heap=Dungeon.level.heaps.get(c.pos);
            int now=heap!=null && heap.waywardCache==c.id?heap.size():0;
            if(c.remaining!=now){c.remaining=now;revision++;if(now==0)Notes.removeChart(c.id);}
        }
        boolean restored=false;
        if(Dungeon.branch==0)for(Memory m:memories)if(m.forgotten && m.depth==Dungeon.depth && m.contains(Dungeon.hero.pos)){
            m.forgotten=false;restored=true;
        }
        if(restored){revision++;GLog.w(Messages.get(WaywardChart.class,"restored"));}
        else if(Dungeon.branch==0 && memories.stream().anyMatch(m->m.forgotten && m.depth==Dungeon.depth))revision++;
        if(mainFloor() && debt[region(Dungeon.depth)]>0){captureRooms();assignDebt(region(Dungeon.depth));}
    }
    public static synchronized boolean protectedCell(int cell) {
        Level l=Dungeon.level;
        if(l.getTransition(cell)!=null || l.map[cell]==Terrain.ENTRANCE || l.map[cell]==Terrain.EXIT
                || l.map[cell]==Terrain.DOOR || l.map[cell]==Terrain.OPEN_DOOR || l.map[cell]==Terrain.LOCKED_DOOR
                || l.map[cell]==Terrain.ALCHEMY || l.map[cell]==Terrain.WELL || l.map[cell]==Terrain.PEDESTAL)return true;
        if(l.traps.get(cell)!=null && l.traps.get(cell).visible)return true;
        for(com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache c:l.elementalCaches)
            if(cell==c.door || cell==c.mechanism)return true;
        for(Cache c:caches)if(c.depth==Dungeon.depth && c.branch==Dungeon.branch && cell==c.pos)return true;
        return false;
    }
    public static synchronized boolean[] veilCells() {
        Level l=Dungeon.level;boolean[] mask=new boolean[l.length()];if(Dungeon.branch!=0)return mask;
        for(Memory m:memories)if(m.forgotten && m.depth==Dungeon.depth && m.width==l.width())
            for(int cell:m.cells)if(cell>=0 && cell<mask.length && (l.visited[cell] || l.mapped[cell])
                    && !l.heroFOV[cell] && l.distance(Dungeon.hero.pos,cell)>2 && !protectedCell(cell))mask[cell]=true;
        return mask;
    }
    public static synchronized void store(Bundle parent) {
        captureRooms();Bundle b=new Bundle();b.put("entered",entered);b.put("rewarded",rewarded);b.put("debt",debt);
        b.put("hoard",hoardRewarded);b.put("memories",memories);b.put("caches",caches);parent.put("wayward_journey",b);
    }
    public static synchronized void restore(Bundle parent) {
        reset();if(!parent.contains("wayward_journey"))return;Bundle b=parent.getBundle("wayward_journey");
        entered=b.getBooleanArray("entered");rewarded=b.getBooleanArray("rewarded");debt=b.getIntArray("debt");
        hoardRewarded=b.getBoolean("hoard");
        for(Bundlable m:b.getCollection("memories"))memories.add((Memory)m);
        for(Bundlable c:b.getCollection("caches"))caches.add((Cache)c);
    }
    /** Existing saves never get entry rerolls on floors generated before the Chart existed. */
    public static synchronized void migrateVisited(Collection<Integer> generated) {
        for(int key:generated)if(key>0 && key<entered.length)entered[key]=true;
    }
    public static class Cache implements Bundlable {
        public int id,depth,branch,pos,region,total,remaining;public boolean claimed,hoard;
        public int state(){return remaining<=0?2:remaining<total?1:0;}
        @Override public void storeInBundle(Bundle b){b.put("id",id);b.put("depth",depth);b.put("branch",branch);b.put("pos",pos);
            b.put("region",region);b.put("total",total);b.put("remaining",remaining);b.put("claimed",claimed);b.put("hoard",hoard);}
        @Override public void restoreFromBundle(Bundle b){id=b.getInt("id");depth=b.getInt("depth");branch=b.getInt("branch");pos=b.getInt("pos");
            region=b.getInt("region");total=b.getInt("total");remaining=b.getInt("remaining");claimed=b.getBoolean("claimed");hoard=b.getBoolean("hoard");}
    }
    public static class Memory implements Bundlable {
        public int depth,anchor,left,right,top,bottom,width;public int[] cells=new int[0];public boolean forgotten;
        public boolean contains(int cell){return cell%width>left && cell%width<right && cell/width>top && cell/width<bottom;}
        @Override public void storeInBundle(Bundle b){b.put("depth",depth);b.put("anchor",anchor);b.put("left",left);b.put("right",right);
            b.put("top",top);b.put("bottom",bottom);b.put("width",width);b.put("cells",cells);b.put("forgotten",forgotten);}
        @Override public void restoreFromBundle(Bundle b){depth=b.getInt("depth");anchor=b.getInt("anchor");left=b.getInt("left");right=b.getInt("right");
            top=b.getInt("top");bottom=b.getInt("bottom");width=b.getInt("width");cells=b.getIntArray("cells");forgotten=b.getBoolean("forgotten");}
    }
}
