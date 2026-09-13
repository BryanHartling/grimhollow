// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.*;
import com.watabou.noosa.*;
import com.watabou.utils.Point;
import java.util.*;

/** Tests 43/45 in the existing desktop renderer, with normal hero movement and no terrain edits. */
final class RecoveryChecks {
    private final int region;
    private final String folder;
    private final ArrayList<Integer> route = new ArrayList<>();
    private final Map<String,Integer> remembered = new TreeMap<>();
    private final ArrayList<String> evidence = new ArrayList<>();
    private int[] bounds;
    private int index, waiting, lastCell, lastDirection, turns, doors, screenshots, fogPixels, blackPixels;
    private boolean started, pending;
    private int beforeDoor;

    RecoveryChecks(int region) {
        this.region=region;
        folder="verification/recovery/"+new String[]{"sewers","prison","caves","city","halls"}[region]+"/";
        for(String kind:new String[]{"floor","water","grass","door","chasm"})remembered.put(kind,0);
    }

    static void titleControls() {
        String[] fields={"btnPlay","btnRankings","btnJournal","btnSettings","btnCredits"};
        for(String name:fields)if(field(Game.scene(),name)==null)throw new AssertionError("Missing title control "+name);
        int controls=0;
        for(Gizmo child:members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)controls++;
        if(controls!=5)throw new AssertionError("Title must contain exactly five main controls, found "+controls);
    }

    static void linkHandlers() {
        com.badlogic.gdx.Net previous=Gdx.net;ArrayList<String> opened=new ArrayList<>();
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero=Dungeon.hero;
        Gdx.net=(com.badlogic.gdx.Net)java.lang.reflect.Proxy.newProxyInstance(com.badlogic.gdx.Net.class.getClassLoader(),
                new Class<?>[]{com.badlogic.gdx.Net.class},(proxy,method,args)->{
                    if(method.getName().equals("openURI")){opened.add((String)args[0]);return true;}
                    throw new AssertionError("Unexpected scene network request "+method.getName());
                });
        try {
            com.shatteredpixel.shatteredpixeldungeon.scenes.AboutScene credits=new com.shatteredpixel.shatteredpixeldungeon.scenes.AboutScene();
            credits.create();int links=0;
            for(Gizmo child:members(credits))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RedButton) {
                java.lang.reflect.Method click=child.getClass().getDeclaredMethod("onClick");click.setAccessible(true);click.invoke(child);links++;
            }
            if(links!=4||opened.size()!=4)throw new AssertionError("Credits link inventory changed");
            for(String url:opened)if(!com.watabou.utils.RepositoryUris.allowed(url))throw new AssertionError("External credits link "+url);
            if(Game.platform.openURI("https://example.org/")||opened.size()!=4)throw new AssertionError("Platform navigation guard bypassed");
            float elapsed=Game.elapsed;Game.elapsed=2;credits.update();Game.elapsed=elapsed;Camera.updateAll();
            Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);credits.draw();Pixmap shot=screen();
            PixmapIO.writePNG(Gdx.files.local("verification/recovery/credits.png"),shot,-1,true);shot.dispose();credits.destroy();
            com.shatteredpixel.shatteredpixeldungeon.scenes.JournalScene journal=new com.shatteredpixel.shatteredpixeldungeon.scenes.JournalScene();
            journal.create();journal.destroy();
            if(opened.size()!=4)throw new AssertionError("Journal navigates externally");
            if(com.shatteredpixel.shatteredpixeldungeon.services.news.News.service!=null
                    ||com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates.service!=null)
                throw new AssertionError("A network feed service is active");
            System.out.println("TEST 46 runtime: title controls=5 credits handlers=4 repository URLs=4 external opens=0 scene fetches=0 failures=0");
        } catch(Exception e){throw new AssertionError(e);}
        finally {Gdx.net=previous;Dungeon.hero=hero;}
    }

    void prepare() {
        SPDSettings.zoom(0);
        for(long seed=417;seed<929;seed++) {
            Dungeon.seed=seed; Dungeon.init(); Dungeon.depth=region*5+1;
            RegularLevel level=(RegularLevel)Dungeon.newLevel();
            int w=level.width();
            for(Room room:level.rooms()) {
                if(room.width()<5||room.width()>11||room.height()<5||room.height()>11)continue;
                Set<String> kinds=new TreeSet<>();int start=-1;
                for(int y=room.top;y<=room.bottom;y++)for(int x=room.left;x<=room.right;x++) {
                    int c=x+y*w; kinds.add(kind(level.map[c]));
                    if(x>room.left&&x<room.right&&y>room.top&&y<room.bottom&&level.map[c]==Terrain.EMPTY
                            && !level.traps.containsKey(c)&&level.findMob(c)==null)start=c;
                }
                kinds.remove("other");
                if(kinds.size()<4||start<0)continue;
                ArrayList<Integer> path=route(level,start);
                if(path==null)continue;
                for(Mob mob:level.mobs)Actor.remove(mob);
                level.mobs.clear();level.heaps.clear();
                Dungeon.switchLevel(level,start);Dungeon.hero.HT=Dungeon.hero.HP=200;
                Dungeon.hero.live(); Dungeon.observe();
                bounds=new int[]{room.left,room.top,room.right,room.bottom};route.addAll(path);
                lastCell=start;
                System.out.println("RECOVERY generated region="+region+" seed="+seed+" room="+Arrays.toString(bounds)
                        +" fixed-path steps="+route.size()+" terrainEdits=0");
                return;
            }
        }
        throw new AssertionError("No generated recovery room/path for region "+region);
    }

    private static boolean walkable(Level l,int c) {
        if(!l.insideMap(c)||l.traps.containsKey(c)||l.plants.containsKey(c)||l.pit[c])return false;
        int t=l.map[c];
        return t==Terrain.EMPTY||t==Terrain.EMPTY_SP||t==Terrain.EMPTY_DECO||t==Terrain.WATER||t==Terrain.DOOR||t==Terrain.OPEN_DOOR;
    }

    private static int[] parents(Level l,int start) {
        int[] parent=new int[l.length()];Arrays.fill(parent,-1);parent[start]=start;
        ArrayDeque<Integer> q=new ArrayDeque<>();q.add(start);
        while(!q.isEmpty()) {
            int c=q.remove();
            for(int n:new int[]{c-l.width(),c+1,c+l.width(),c-1})
                if(n>=0&&n<parent.length&&parent[n]<0&&walkable(l,n)){parent[n]=c;q.add(n);}
        }
        return parent;
    }

    private static ArrayList<Integer> route(Level l,int start) {
        int[] reachable=parents(l,start);ArrayList<Integer> targets=new ArrayList<>();
        for(String type:new String[]{"water","grass","door","chasm"}) {
            int target=-1;
            for(int c=0;c<l.length()&&target<0;c++)if(kind(l.map[c]).equals(type)) {
                if(type.equals("door")&&reachable[c]>=0)target=c;
                else for(int n:new int[]{c-l.width(),c+1,c+l.width(),c-1})
                    if(n>=0&&n<l.length()&&reachable[n]>=0&&walkable(l,n)){target=n;break;}
            }
            if(target<0)return null;targets.add(target);
        }
        int far=start,best=0;
        for(int c=0;c<reachable.length;c++)if(reachable[c]>=0) {
            int distance=Math.abs(c%l.width()-start%l.width())+Math.abs(c/l.width()-start/l.width());
            if(distance>best){best=distance;far=c;}
        }
        targets.add(far);targets.add(start);
        ArrayList<Integer> route=new ArrayList<>();int from=start;
        for(int target:targets) {
            int[] parent=parents(l,from);if(parent[target]<0)return null;
            ArrayList<Integer> segment=new ArrayList<>();
            for(int c=target;c!=from;c=parent[c])segment.add(c);
            Collections.reverse(segment);route.addAll(segment);from=target;
        }
        return route.size()>20&&route.size()<360?route:null;
    }

    static String kind(int t) {
        if(t==Terrain.EMPTY||t==Terrain.EMPTY_SP||t==Terrain.ENTRANCE||t==Terrain.ENTRANCE_SP
                ||t==Terrain.EXIT||t==Terrain.SECRET_TRAP)return "floor";
        if(t==Terrain.WATER)return "water";
        if(t==Terrain.GRASS||t==Terrain.HIGH_GRASS||t==Terrain.FURROWED_GRASS)return "grass";
        if(DungeonTileSheet.doorTile(t))return "door";
        if(t==Terrain.CHASM)return "chasm";
        if(t==Terrain.WALL||t==Terrain.SECRET_DOOR)return "wall";
        if(t==Terrain.EMPTY_DECO||t==Terrain.WALL_DECO||t==Terrain.REGION_DECO||t==Terrain.REGION_DECO_ALT
                ||t==Terrain.STATUE||t==Terrain.STATUE_SP||t==Terrain.ALCHEMY||t==Terrain.BARRICADE||t==Terrain.BOOKSHELF
                ||t==Terrain.EMPTY_WELL||t==Terrain.WELL||t==Terrain.PEDESTAL||t==Terrain.MINE_CRYSTAL||t==Terrain.MINE_BOULDER
                ||t==Terrain.CUSTOM_DECO||t==Terrain.CUSTOM_DECO_EMPTY||t==Terrain.CUSTOM_DECO_WTR)return "decor";
        if(t==Terrain.TRAP||t==Terrain.INACTIVE_TRAP)return "trap";
        if(t==Terrain.EMBERS)return "embers";
        return "terrain-"+t;
    }

    boolean tick() {
        if(!(Game.scene() instanceof GameScene))return false;
        if(!started) {
            if(++waiting<50)return false;
            room();started=true;waiting=0;return false;
        }
        if(!Dungeon.hero.isAlive())throw new AssertionError("Recovery walking hero died");
        if(!Dungeon.hero.ready||Dungeon.hero.sprite.isMoving) {waiting=0;return false;}
        if(++waiting<12)return false;
        waiting=0;
        if(pending) {
            int target=route.get(index);
            if(Dungeon.hero.pos!=target)throw new AssertionError("Hero did not walk to "+target+" from "+lastCell);
            int direction=target-lastCell;boolean turned=lastDirection!=0&&direction!=lastDirection;
            if(turned)turns++;
            boolean opened=beforeDoor==Terrain.DOOR&&Dungeon.level.map[target]==Terrain.OPEN_DOOR;
            if(opened)doors++;
            lastDirection=direction;lastCell=target;index++;pending=false;
            checkRemembered();
            if(screenshots<16&&(index==1||index==route.size()||opened||turned&&screenshots<8||index%24==0))
                snapshot(opened?"open-door":turned?"turn":"walk");
        }
        if(index>=route.size()) {
            for(Map.Entry<String,Integer> count:remembered.entrySet())
                if(count.getValue()==0)throw new AssertionError("No remembered "+count.getKey()+" coverage in region "+region);
            if(turns==0||doors==0||fogPixels==0||blackPixels==0)throw new AssertionError("Incomplete walking/fog evidence");
            String result="{\"region\":"+region+",\"seed\":"+Dungeon.seed+",\"path\":"+route
                    +",\"steps\":"+index+",\"turns\":"+turns+",\"openedDoors\":"+doors
                    +",\"fogPixels\":"+fogPixels+",\"blackPixels\":"+blackPixels+",\"failures\":0,\"screenshots\":["
                    +String.join(",",evidence)+"]}";
            Gdx.files.local(folder+"walk.json").writeString(result,false,"UTF-8");
            System.out.println("TEST 43: region="+region+" steps="+index+" remembered="+remembered
                    +" fogPixels="+fogPixels+" unseenBlackPixels="+blackPixels+" turns="+turns+" doors="+doors+" failures=0");
            return true;
        }
        // Remove actors from this diagnostic fixture only; terrain and normal input remain untouched.
        for(Mob mob:Dungeon.level.mobs.toArray(new Mob[0])){Actor.remove(mob);if(mob.sprite!=null)mob.sprite.killAndErase();}
        Dungeon.level.mobs.clear();
        int target=route.get(index);beforeDoor=Dungeon.level.map[target];
        if(!Dungeon.hero.handle(target))throw new AssertionError("Hero rejected adjacent move");
        Dungeon.hero.next();pending=true;
        return false;
    }

    private static Object field(Object object,String name) {
        try {
            for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass()) {
                try {java.lang.reflect.Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(object);}
                catch(NoSuchFieldException ignored) { }
            }
            throw new NoSuchFieldException(name);
        }
        catch(Exception e){throw new AssertionError(e);}
    }

    @SuppressWarnings("unchecked")
    private static List<Gizmo> members(Group group) {return (List<Gizmo>)field(group,"members");}

    private void checkRemembered() {
        GameScene scene=(GameScene)Game.scene();Level l=Dungeon.level;int w=l.width();
        DungeonTerrainTilemap terrain=(DungeonTerrainTilemap)field(scene,"tiles");
        DungeonWallsTilemap walls=(DungeonWallsTilemap)field(scene,"walls");
        WallBlockingTilemap blocking=(WallBlockingTilemap)field(scene,"wallBlocking");
        for(int c=0;c<l.length();c++)if(l.visited[c]&&!l.heroFOV[c]&&remembered.containsKey(kind(l.map[c]))) {
            String type=kind(l.map[c]);Image actual=terrain.image(c%w,c/w);
            if(walls.image(c%w,c/w)!=null||blocking.image(c%w,c/w)!=null)
                throw new AssertionError("Remembered "+type+" repainted by a wall at "+c);
            int expected=-1,t=l.map[c];
            if(type.equals("floor"))expected=DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.directVisuals.get(t),c);
            else if(type.equals("water"))expected=DungeonTileSheet.stitchWaterTile(l.map[c-w],l.map[c+1],l.map[c+w],l.map[c-1]);
            else if(t==Terrain.GRASS)expected=DungeonTileSheet.getVisualWithAlts(DungeonTileSheet.GRASS,c);
            else if(type.equals("door"))expected=DungeonTileSheet.getRaisedDoorTile(t,l.map[c-w]);
            else if(type.equals("chasm"))expected=DungeonTileSheet.stitchChasmTile(l.map[c-w]);
            if(expected>=0&&expected!=DungeonTileSheet.WATER) {
                if(actual==null)throw new AssertionError("Missing remembered terrain "+type);
                com.watabou.noosa.TextureFilm film=new com.watabou.noosa.TextureFilm(actual.texture,64,64);
                com.watabou.utils.RectF a=actual.frame(),b=film.get(expected);
                if(a.left!=b.left||a.right!=b.right||a.top!=b.top||a.bottom!=b.bottom)
                    throw new AssertionError("Wrong remembered terrain frame "+type+" at "+c);
            }
            remembered.put(type,remembered.get(type)+1);
        }
        if(index%12!=0&&index!=route.size())return;
        FogOfWar fog=(FogOfWar)field(scene,"fog");
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);scene.draw();Pixmap dim=screen();
        ((Gizmo)fog).visible=false;Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);scene.draw();Pixmap raw=screen();((Gizmo)fog).visible=true;
        for(int c=0;c<l.length();c++)if(!l.heroFOV[c]&&!DungeonTileSheet.wallStitcheable(l.map[c])) {
            Point p=Camera.main.cameraToScreen(c%w*16+8,c/w*16+8);
            int x=p.x,y=dim.getHeight()-1-p.y;
            if(x<20||x>=dim.getWidth()-20||y<40||y>=dim.getHeight()-100)continue;
            int mask=fog.texture.bitmap.getPixel(c%w*2+1,c/w*2+1),alpha=mask&255;
            int a=dim.getPixel(x,y),b=raw.getPixel(x,y);
            if(!l.visited[c]&&!l.mapped[c]&&alpha==255) {
                if((a>>>8)!=0)throw new AssertionError("Never-seen cell not black at "+c);
                blackPixels++;
            } else if(l.visited[c]&&alpha>0&&alpha<255&&remembered.containsKey(kind(l.map[c]))) {
                for(int shift:new int[]{24,16,8}) {
                    int expected=Math.round(((b>>>shift)&255)*(1-alpha/255f)+((mask>>>shift)&255)*alpha/255f);
                    if(Math.abs(((a>>>shift)&255)-expected)>3)throw new AssertionError("Remembered fog changes terrain pixels at "+c);
                }
                fogPixels++;
            }
        }
        dim.dispose();raw.dispose();Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);scene.draw();
    }

    private static Pixmap screen(){return Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());}
    private void save(String name) {Pixmap shot=screen();PixmapIO.writePNG(Gdx.files.local(folder+name),shot,-1,true);shot.dispose();}
    private void snapshot(String event) {
        String name=String.format(java.util.Locale.ROOT,"walk-%02d-%s.png",screenshots++,event);save(name);
        evidence.add("{\"file\":\""+name+"\",\"cell\":"+Dungeon.hero.pos+",\"step\":"+index+",\"event\":\""+event+"\"}");
    }
    private void room() {
        save("lit.png");Level l=Dungeon.level;int w=l.width();StringBuilder cells=new StringBuilder();
        for(int y=bounds[1];y<=bounds[3];y++)for(int x=bounds[0];x<=bounds[2];x++) {
            int c=x+y*w;if(!l.heroFOV[c])continue;
            Point a=Camera.main.cameraToScreen(x*16,y*16),b=Camera.main.cameraToScreen((x+1)*16,(y+1)*16);
            if(cells.length()>0)cells.append(',');
            cells.append("{\"cell\":").append(c).append(",\"kind\":\"").append(kind(l.map[c]))
                    .append("\",\"hero\":").append(c==Dungeon.hero.pos).append(",\"box\":[")
                    .append(a.x).append(',').append(a.y).append(',').append(b.x).append(',').append(b.y).append("]}");
        }
        Gdx.files.local(folder+"room.json").writeString("{\"seed\":"+Dungeon.seed+",\"region\":"+region
                +",\"lighting\":"+SPDSettings.dynamicLighting()+",\"bounds\":"+Arrays.toString(bounds)+",\"cells\":["+cells+"]}",false,"UTF-8");
        snapshot("start");
    }
}
