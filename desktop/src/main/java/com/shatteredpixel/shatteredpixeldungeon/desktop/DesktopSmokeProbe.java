// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Group;
import com.watabou.noosa.Camera;
import com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/** Opt-in launch diagnostic: renders real OpenGL frames, writes evidence, exits. */
final class DesktopSmokeProbe extends ShatteredPixelDungeon {
    private final boolean sewers;
    private final boolean vault=Boolean.getBoolean("grimhollow.vault");
    private com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultFinalRoom vaultArena;
    private com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultBossElemental vaultBoss;
    private int frames;
    private boolean originalLighting;
    private int originalZoom;
    private int[] reviewBounds;
    private final int reviewRegion=Integer.getInteger("grimhollow.region",0);
    private final RecoveryChecks recovery=Boolean.getBoolean("grimhollow.recovery")?new RecoveryChecks(reviewRegion):null;
    private String reviewPath(String name) {
        String[] regions={"sewers","prison","caves","city","halls"};
        return "verification/iteration/"+(reviewRegion==0?"":regions[reviewRegion]+"/")+name;
    }
    private com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat reviewRat;
    private com.shatteredpixel.shatteredpixeldungeon.items.Heap reviewItem;
    DesktopSmokeProbe(boolean sewers) {
        super(new DesktopPlatformSupport());
        this.sewers=sewers;
        sceneClass=TitleScene.class;
    }
    @Override public void create() {
        super.create();
        originalLighting=SPDSettings.dynamicLighting();
        originalZoom=SPDSettings.zoom();
    }
    private void capture(String name) {
        Pixmap screenshot=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        String output=System.getProperty("grimhollow.smokeDir", ".local/acceptance")+"/"+name+".png";
        PixmapIO.writePNG(Gdx.files.absolute(output),screenshot,-1,true);
        screenshot.dispose();
        System.out.println("RENDERED="+Game.scene().getClass().getSimpleName()+" SCREENSHOT="+output);
    }
    @Override public void render() {
        super.render();
        frames++;
        if(recovery!=null&&frames>180) {
            if(recovery.tick()) {
                if(Boolean.getBoolean("grimhollow.geometryTests"))geometryTests();
                if(Boolean.getBoolean("grimhollow.effectsTests"))effectsTests();
                SPDSettings.dynamicLighting(originalLighting);
                SPDSettings.zoom(originalZoom);
                Gdx.app.exit();
            }
            return;
        }
        if (vault && sewers) vaultFrames();
        if (frames==180) {
            if (!(Game.scene() instanceof TitleScene)) throw new AssertionError("Title scene did not launch");
            capture("title");
            if(recovery!=null) {
                RecoveryChecks.titleControls();
                // Journal deliberately clears run item-identification state on entry;
                // exercise its menu flow before creating the diagnostic dungeon.
                if(reviewRegion==0)RecoveryChecks.linkHandlers();
            }
            if (!sewers) { Gdx.app.exit(); return; }
            GamesInProgress.selectedClass=(vault||Boolean.getBoolean("grimhollow.renderPoc"))?HeroClass.NECROMANCER:HeroClass.WARRIOR;
            GamesInProgress.curSlot=99;
            if(recovery!=null) recovery.prepare();
            else if(vault) vaultRoom();
            else if(Boolean.getBoolean("grimhollow.iteration")) iterationRoom();
            else {
                Dungeon.seed=417;
                Dungeon.init();
                Dungeon.switchLevel(Dungeon.newLevel(),-1);
                if(Boolean.getBoolean("grimhollow.renderPoc"))pocRoom();
            }
            InterlevelScene.mode=InterlevelScene.Mode.DESCEND;
            SPDSettings.dynamicLighting(true);
            switchNoFade(GameScene.class);
        } else if (sewers && frames==360) {
            if (!(Game.scene() instanceof GameScene)) throw new AssertionError("Sewer scene did not launch");
            capture(vault?"vault-lighting-on":"sewers-lighting-on");
            if(Boolean.getBoolean("grimhollow.iteration")) {
                if (!SPDSettings.dynamicLighting() || com.watabou.noosa.Camera.main.zoom != com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.defaultZoom)
                    throw new AssertionError("Iteration review requires lighting on and default zoom");
                Pixmap shot=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
                PixmapIO.writePNG(Gdx.files.absolute(reviewPath("sewers-ingame.png")),shot,-1,true);shot.dispose();
                roomMetadata();
                Dungeon.hero.sprite.visible=false;reviewRat.sprite.visible=false;reviewItem.sprite.visible=false;
                com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator targetBar=com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator.instance;
                boolean targetBarVisible=targetBar!=null&&targetBar.visible;
                if(targetBar!=null)targetBar.visible=false;
                // Draw the exact same frame without subjects; ItemSprite.update would
                // otherwise restore visibility before a later-frame background capture.
                Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);Game.scene().draw();
                Pixmap ground=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
                PixmapIO.writePNG(Gdx.files.absolute(reviewPath("sewers-terrain.png")),ground,-1,true);ground.dispose();
                Dungeon.hero.sprite.visible=true;reviewRat.sprite.visible=true;reviewItem.sprite.visible=true;
                if(targetBar!=null)targetBar.visible=targetBarVisible;
                System.out.println("ITERATION SCREENSHOT: lighting=true defaultZoom="+com.watabou.noosa.Camera.main.zoom);
            }
            if(Boolean.getBoolean("grimhollow.renderPoc")){Pixmap shot=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());PixmapIO.writePNG(Gdx.files.absolute("verification/render-poc-ingame.png"),shot,-1,true);shot.dispose();}
            SPDSettings.dynamicLighting(false);
        } else if (sewers && frames==420) {
            capture(vault?"vault-lighting-off":"sewers-lighting-off");
            SPDSettings.dynamicLighting(originalLighting);
            SPDSettings.zoom(originalZoom);
            if (Boolean.getBoolean("grimhollow.geometryTests")) geometryTests();
            if(Boolean.getBoolean("grimhollow.effectsTests"))effectsTests();
            if (Boolean.getBoolean("grimhollow.iteration")) liquidTests();
            System.out.println("PASS: "+(vault?"Vault":"Sewer")+" scene renders with dynamic lighting on and off.");
            Gdx.app.exit();
        }
    }

    /** Reuse the real renderer for v4 arena spawning, three forms and door-unlock behavior. */
    private void vaultRoom() {
        SPDSettings.zoom(0);
        Dungeon.seed=417;Dungeon.init();Dungeon.depth=19;
        Dungeon.switchLevel(Dungeon.newLevel(),-1);
        Dungeon.hero.lvl=20;Dungeon.hero.HT=Dungeon.hero.HP=200;
        Dungeon.hero.live();
        com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal escape=new com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal();
        escape.storeHeroBelongings(Dungeon.hero);escape.collect();
        Dungeon.hero.belongings.armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor();
        Dungeon.branch=1;
        com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel level=(com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel)Dungeon.newLevel();
        vaultArena=(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultFinalRoom)level.room(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultFinalRoom.class);
        if(vaultArena==null)throw new AssertionError("Vault arena missing");
        com.watabou.utils.Bundle data=new com.watabou.utils.Bundle();vaultArena.storeInBundle(data);
        int dx=data.getInt("locked_door_x"),dy=data.getInt("locked_door_y");
        com.watabou.utils.Point center=vaultArena.center();
        int heroCell=dx+Integer.signum(center.x-dx)*2+(dy+Integer.signum(center.y-dy)*2)*level.width();
        if(!level.passable[heroCell])throw new AssertionError("Vault arena entry is blocked");
        Dungeon.switchLevel(level,heroCell);Dungeon.observe();
    }

    private void vaultFrames() {
        if(frames==240) {
            vaultArena.processHeroStep(Dungeon.hero);
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs)
                if(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultBossElemental)
                    vaultBoss=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultBossElemental)mob;
            if(vaultBoss==null||!Dungeon.level.locked)throw new AssertionError("Vault boss did not seal its arena");
            vaultBoss.setElementalForm(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultBossElemental.ElementalForm.FIRE);
            Dungeon.observe();
        } else if(frames==275) capture("vault-fire");
        else if(frames==280) vaultBoss.setElementalForm(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultBossElemental.ElementalForm.FROST);
        else if(frames==315) capture("vault-frost");
        else if(frames==320) vaultBoss.setElementalForm(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultBossElemental.ElementalForm.SHOCK);
        else if(frames==355) capture("vault-shock");
        else if(frames==400) {
            vaultBoss.die(Dungeon.hero);
            if(Dungeon.level.locked)throw new AssertionError("Vault boss death did not unseal arena");
            com.watabou.utils.Bundle data=new com.watabou.utils.Bundle();vaultArena.storeInBundle(data);
            int door=data.getInt("locked_door_x")+data.getInt("locked_door_y")*Dungeon.level.width();
            if(Dungeon.level.map[door]!=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR)throw new AssertionError("Vault treasure door did not unlock");
            System.out.println("PASS V4 rendering: arena trigger, FIRE/FROST/SHOCK, scripted boss death and treasure door unlock (not a played fight)");
        }
    }

    private void liquidTests() {
        int checked=0;
        for(int y=-16;y<16;y++)for(int x=-16;x<16;x++) {
            int phase=com.shatteredpixel.shatteredpixeldungeon.tiles.LiquidTilemap.phase(x,y);
            if(phase<0||phase>7)throw new AssertionError("Liquid phase outside loop");
            for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)if(dx!=0||dy!=0)
                if(phase==com.shatteredpixel.shatteredpixeldungeon.tiles.LiquidTilemap.phase(x+dx,y+dy))throw new AssertionError("Adjacent liquids synchronize");
            for(int tick=0;tick<8;tick++) {
                int frame=com.shatteredpixel.shatteredpixeldungeon.tiles.LiquidTilemap.frame(x,y,tick);
                if(frame<0||frame>=32||frame%8!=((phase+tick)&7))throw new AssertionError("Wrong liquid animation frame");
                checked++;
            }
        }
        System.out.println("TEST 41 runtime: phase/frame checks="+checked+" adjacent synchronization failures=0");
    }

    /** Reuse the existing screenshot runner; select an unmodified upstream bridge room. */
    private void iterationRoom() {
        SPDSettings.zoom(0);
        for (long seed=417;seed<929;seed++) {
            Dungeon.seed=seed;Dungeon.init();Dungeon.depth=reviewRegion*5+1;
            com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel level=(com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)Dungeon.newLevel();
            int w=level.width();
            for(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room room:level.rooms()) {
                if((reviewRegion==0 && !(room instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.WaterBridgeRoom)) || room.width()>12 || room.height()>11)continue;
                int water=0,door=0,decor=0,torch=0,torchCell=-1,bridge=-1,openBoundary=0;float nearest=Float.MAX_VALUE;
                for(int y=room.top;y<=room.bottom;y++)for(int x=room.left;x<=room.right;x++) {
                    int c=x+y*w,t=level.map[c];
                    if ((x==room.left || x==room.right || y==room.top || y==room.bottom) &&
                            !com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.wallStitcheable(t) &&
                            !com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.doorTile(t))openBoundary++;
                    if(t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WATER)water++;
                    if(t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR)door++;
                    if(t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY_DECO)decor++;
                    if(t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL_DECO){torch++;torchCell=c;}
                    if(x>room.left&&x<room.right&&y>room.top&&y<room.bottom&&level.passable[c]&&!level.water[c]&&level.findMob(c)==null &&
                            (reviewRegion!=0 || (level.water[c-1]&&level.water[c+1])||(level.water[c-w]&&level.water[c+w]))) {
                        float distance=Math.abs(x-(room.left+room.right)/2f)+Math.abs(y-(room.top+room.bottom)/2f);
                        if(distance<nearest){bridge=c;nearest=distance;}
                    }
                }
                if(water>=4&&door>0&&(decor>0&&torch==1)&&bridge>=0) {
                    // Merged room outlines can include an open corridor into unrelated special rooms.
                    if(reviewRegion>=2 && openBoundary>0)continue;
                    if(reviewRegion>=2 && (torchCell/w!=room.top || torchCell%w<=room.left || torchCell%w>=room.right || torchCell+w>=level.length() ||
                            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.wallStitcheable(level.map[torchCell+w]) ||
                            com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.doorTile(level.map[torchCell+w])))continue;
                    java.util.ArrayList<Integer> floor=new java.util.ArrayList<>();
                    for(int y=room.top+1;y<room.bottom;y++)for(int x=room.left+1;x<room.right;x++) {
                        int c=x+y*w;
                        if(level.map[c]==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY && !level.traps.containsKey(c))floor.add(c);
                    }
                    floor.sort(java.util.Comparator.comparingDouble(c->Math.abs(c%w-(room.left+room.right)/2f)+Math.abs(c/w-(room.top+room.bottom)/2f)));
                    if(floor.size()<3)continue;
                    int heroCell=floor.get(0);
                    if(reviewRegion>=2 && Math.hypot(heroCell%w-torchCell%w,heroCell/w-torchCell/w)>6)continue;
                    for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:level.mobs)
                        com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);
                    level.mobs.clear();level.heaps.clear();
                    reviewBounds=new int[]{room.left,room.top,room.right,room.bottom};
                    reviewRat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();reviewRat.pos=floor.get(1);reviewRat.state=reviewRat.PASSIVE;level.mobs.add(reviewRat);
                    reviewItem=level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing(),floor.get(2));
                    Dungeon.switchLevel(level,heroCell);Dungeon.observe();
                    System.out.println("ITERATION ROOM: seed="+seed+" bounds="+room.left+","+room.top+","+room.right+","+room.bottom+" water="+water+" doors="+door+" rubble="+decor+" wallTorch="+torch+" torchCell="+torchCell+" bridgeCell="+bridge+" heroCell="+heroCell+" terrainEdits=0");
                    return;
                }
            }
        }
        throw new AssertionError("No generated Sewer bridge room satisfies the review scene");
    }

    private String screenBox(float x,float y,float width,float height) {
        com.watabou.noosa.Camera c=com.watabou.noosa.Camera.main;
        com.watabou.utils.Point a=c.cameraToScreen(x,y),b=c.cameraToScreen(x+width,y+height);
        float sx=(float)Gdx.graphics.getBackBufferWidth()/Gdx.graphics.getWidth(),sy=(float)Gdx.graphics.getBackBufferHeight()/Gdx.graphics.getHeight();
        return "["+Math.round(a.x*sx)+","+Math.round(a.y*sy)+","+Math.round(b.x*sx)+","+Math.round(b.y*sy)+"]";
    }

    /** Pixel masks derive from the actual camera/terrain; no synthetic room is scored. */
    private void roomMetadata() {
        com.shatteredpixel.shatteredpixeldungeon.levels.Level l=Dungeon.level;int w=l.width();
        StringBuilder json=new StringBuilder("{\"seed\":"+Dungeon.seed+",\"lighting\":true,\"zoom\":"+com.watabou.noosa.Camera.main.zoom+",\"bounds\":"+java.util.Arrays.toString(reviewBounds)+",\"cells\":[");
        boolean first=true;
        for(int c=0;c<l.length();c++)if(l.heroFOV[c]) {
            int t=l.map[c];String kind="other";
            if(t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY)kind="floor";
            else if(t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WATER)kind="water";
            else if(t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL||t==com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL_DECO)kind="wall";
            if(!first)json.append(',');first=false;
            json.append("{\"cell\":").append(c).append(",\"x\":").append(c%w).append(",\"y\":").append(c/w).append(",\"kind\":\"").append(kind).append("\",\"box\":").append(screenBox(c%w*16,c/w*16,16,16)).append('}');
        }
        json.append("],\"subjects\":[");
        com.watabou.noosa.Visual[] sprites={Dungeon.hero.sprite,reviewRat.sprite,reviewItem.sprite};
        String[] names={"hero","rat","item"};int[] cells={Dungeon.hero.pos,reviewRat.pos,reviewItem.pos};
        for(int i=0;i<3;i++) { if(i>0)json.append(',');com.watabou.noosa.Visual s=sprites[i];json.append("{\"name\":\"").append(names[i]).append("\",\"cell\":").append(cells[i]).append(",\"box\":").append(screenBox(s.x,s.y,s.width(),s.height())).append('}'); }
        json.append("]}");
        Gdx.files.absolute(reviewPath("room.json")).writeString(json.toString(),false,"UTF-8");
    }

    private void pocRoom(){
        com.shatteredpixel.shatteredpixeldungeon.levels.Level level=Dungeon.level;
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0]))com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);
        level.mobs.clear();level.heaps.clear();level.traps.clear();int w=level.width(),c=w*(level.height()/2)+w/2;
        for(int y=-4;y<=4;y++)for(int x=-5;x<=5;x++){
            int cell=c+x+y*w,tile=(Math.abs(x)==5||Math.abs(y)==4)?com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL:com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY;
            if(x<-2&&Math.abs(y)<3)tile=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WATER;
            if(x>2&&y>0&&y<4)tile=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.GRASS;
            com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell,tile);
        }
        com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(c+4*w,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR);
        level.cleanWalls();Dungeon.hero.pos=c;Dungeon.hero.viewDistance=8;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton skeleton=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton();skeleton.configure(1);skeleton.pos=c-1;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroGhoul ghoul=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroGhoul();ghoul.configure(1);ghoul.pos=c+1;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();rat.pos=c+2-2*w;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab crab=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab();crab.pos=c-2-2*w;
        // Keep the comparison fixture posed; normal runs and AI acceptance use live states.
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[]{skeleton,ghoul,rat,crab}){mob.state=mob.PASSIVE;level.mobs.add(mob);com.shatteredpixel.shatteredpixeldungeon.actors.Actor.add(mob);}
        Dungeon.observe();
    }

    /** Acceptance 24-26 use actual sprite draws into an RGBA framebuffer. No scene screenshots are measured. */
    private void geometryTests() {
        try {
            float zoom=com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.defaultZoom;
            com.watabou.noosa.Camera camera=new com.watabou.noosa.Camera(0,0,256,256,zoom);
            camera.fullScreen=true;
            camera.matrix[0]=2*zoom/256;camera.matrix[5]=-2*zoom/256;
            camera.matrix[12]=-1;camera.matrix[13]=1;
            com.badlogic.gdx.graphics.glutils.FrameBuffer buffer=new com.badlogic.gdx.graphics.glutils.FrameBuffer(Pixmap.Format.RGBA8888,256,256,false);
            java.util.List<String> failures=new java.util.ArrayList<>();
            int heroes=0,mobs=0,items=0,icons=0;
            HeroClass original=Dungeon.hero.heroClass;
            com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite originalSprite=Dungeon.hero.sprite;
            for(HeroClass hero:HeroClass.values()) {
                Dungeon.hero.heroClass=hero;
                com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
                band(sprite,16,.85f,.95f,buffer,camera,zoom,failures,"24 "+hero);heroes++;sprite.destroy();
                com.watabou.noosa.Image avatar=com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.avatar(hero,6);
                band(avatar,16,.85f,.95f,buffer,camera,zoom,failures,"24 avatar "+hero);avatar.destroy();
                GamesInProgress.set(99);
                com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton slot=new com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton();slot.setRect(0,0,160,28);slot.set(99);
                if(slot.portrait()==null)failures.add("24 missing save portrait "+hero);else band(slot.portrait(),16,.85f,.95f,buffer,camera,zoom,failures,"24 save "+hero);slot.destroy();
                com.watabou.noosa.Image splash=new com.watabou.noosa.Image(hero.splashArt());
                if(GameGeometry.opaqueHeight(splash.texture,splash.frame())==0)failures.add("36 empty splash "+hero);
                if(hero.shortDesc().contains("!!!"))failures.add("36 missing description "+hero);splash.destroy();
            }
            Dungeon.hero.heroClass=original;Dungeon.hero.sprite=originalSprite;
            java.io.File jar=new java.io.File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            try(java.util.jar.JarFile classes=new java.util.jar.JarFile(jar)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries=classes.entries();
                while(entries.hasMoreElements()) {
                    String name=entries.nextElement().getName();
                    if(!name.startsWith("com/shatteredpixel/shatteredpixeldungeon/sprites/")||!name.endsWith(".class"))continue;
                    Class<?> type=Class.forName(name.substring(0,name.length()-6).replace('/','.'));
                    if(!com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class.isAssignableFrom(type)
                            || type==com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.class
                            || java.lang.reflect.Modifier.isAbstract(type.getModifiers()))continue;
                    com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite=(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite)type.getDeclaredConstructor().newInstance();
                    if(sprite.texture==null)continue; // Base sprite classes have no art or frame.
                    float footprint=sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.DM300Sprite||sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.YogSprite||sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.VaultBossElementalSprite?32:16;
                    band(sprite,footprint,.85f,.95f,buffer,camera,zoom,failures,"24 "+type.getSimpleName());mobs++;sprite.destroy();
                }
            }
            System.out.println("TEST 24: heroes="+heroes+" mob sprites="+mobs+" failures="+failures.size());
            int before=failures.size();
            for(java.lang.reflect.Field field:com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.class.getFields()) {
                if(field.getType()!=int.class||field.getName().equals("SIZE"))continue;
                int index=field.getInt(null);if(index<0)continue;
                com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(index);
                if(GameGeometry.opaqueHeight(sprite.texture,sprite.frame())==0){failures.add("25 empty "+field.getName());continue;}
                band(sprite,16,.45f,.55f,buffer,camera,zoom,failures,"25 "+field.getName());
                exactRectangle(sprite,index,32,buffer,camera,zoom,failures,field.getName());items++;sprite.destroy();
                final int itemIndex=index;
                com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot slot=new com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot(new com.shatteredpixel.shatteredpixeldungeon.items.Item(){@Override public int image(){return itemIndex;}});
                slot.setRect(16,8,24,24);slot.camera=camera;
                java.lang.reflect.Field imageField=slot.getClass().getDeclaredField("sprite");imageField.setAccessible(true);
                com.watabou.noosa.Image buttonImage=(com.watabou.noosa.Image)imageField.get(slot);
                Pixmap actual=renderSprite(buttonImage,buffer,camera);int l=256,r=-1,t=256,b=-1;
                for(int yy=0;yy<256;yy++)for(int xx=0;xx<256;xx++)if((actual.getPixel(xx,yy)&255)!=0){l=Math.min(l,xx);r=Math.max(r,xx);t=Math.min(t,yy);b=Math.max(b,yy);}
                actual.dispose();if(Math.max(r-l+1,b-t+1)<24*.7f*zoom)failures.add("25/36 ItemSlot underfilled "+field.getName());
                buffer.begin();slot.draw();buffer.end();slot.destroy();
            }
            for(java.lang.reflect.Field field:com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.class.getFields()) {
                if(field.getType()!=int.class||field.getName().equals("SIZE"))continue;
                int index=field.getInt(null);if(index<0)continue;
                com.watabou.noosa.Image icon=new com.watabou.noosa.Image(Assets.Sprites.ITEM_ICONS);
                icon.frame(com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.film.get(index));
                exactRectangle(icon,index,32,buffer,camera,zoom,failures,"icon "+field.getName());icons++;icon.destroy();
            }
            System.out.println("TEST 25: items="+items+" identification icons="+icons+" failures="+(failures.size()-before));before=failures.size();
            int cell=Dungeon.hero.pos,terrain=Dungeon.level.map[cell];
            com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);
            for(int i=0;i<3;i++) {
                com.watabou.noosa.Image decal=GameScene.createBloodDecal(cell,i);
                if(decal==null)failures.add("26 no floor decal");
                else {band(decal,16,.30f,.60f,buffer,camera,zoom,failures,"26 blood "+i);decal.destroy();}
            }
            for(int invalid:new int[]{com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CHASM,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WATER,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.TRAP}){
                com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell,invalid);
                if(GameScene.createBloodDecal(cell,0)!=null)failures.add("26 decal on invalid terrain "+invalid);
            }
            com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell,terrain);
            System.out.println("TEST 26: three stains and floor/chasm/water/trap placement failures="+(failures.size()-before));
            before=failures.size();
            for(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent talent:com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.values()){
                com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon icon=new com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon(talent);
                if(Math.round(icon.frame().width()*icon.texture.width)!=32||GameGeometry.opaqueHeight(icon.texture,icon.frame())==0)failures.add("36 empty talent "+talent);
                Pixmap drawn=renderSprite(icon,buffer,camera);drawn.dispose();icon.destroy();
            }
            System.out.println("TEST 36: nine splashes, descriptions, portraits, all talents and ItemSlots failures="+(failures.size()-before));
            saveCompatibility(failures);
            System.out.println("TEST 35 RETIRED — superseded by recovery test 46 handler/network checks");
            buffer.dispose();for(String failure:failures)System.out.println("FAIL "+failure);
            if(!failures.isEmpty())throw new AssertionError("Rendering acceptance failures="+failures.size());
            System.out.println("TESTS 24-26, 34, 36 PASS; test 35 retired by recovery");
        }catch(Exception e){throw new RuntimeException(e);}
    }
    private void saveCompatibility(java.util.List<String> failures)throws Exception {
        int before=failures.size();
        Dungeon.saveAll();
        com.watabou.utils.Bundle original=com.watabou.utils.FileUtils.bundleFromFile(GamesInProgress.gameFile(99));
        try {
            for(int version:new int[]{1,Game.versionCode+1}){
                original.put("version",version);com.watabou.utils.FileUtils.bundleToFile(GamesInProgress.gameFile(98),original);
                GamesInProgress.setUnknown(98);
                com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton slot=new com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton();slot.setRect(0,0,160,28);slot.set(98);
                if(!slot.incompatible())failures.add("34 unsupported version offered Continue");slot.destroy();
            }
            original.put("version",Game.versionCode);com.watabou.utils.FileUtils.bundleToFile(GamesInProgress.gameFile(98),original);GamesInProgress.setUnknown(98);
            GamesInProgress.Info info=GamesInProgress.check(98);info.armorTier=99;
            com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton slot=new com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton();slot.setRect(0,0,160,28);slot.set(98);
            if(!slot.incompatible())failures.add("34 invalid portrait offered Continue");slot.destroy();
            com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton.deleteIncompatible(98);
            if(GamesInProgress.gameExists(98))failures.add("34 Delete did not remove incompatible save");
        }finally{com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton.deleteIncompatible(98);}
        System.out.println("TEST 34: old/future version, portrait exception and deletion failures="+(failures.size()-before));
    }
    private void contactScrub(java.io.File jar,java.util.List<String> failures)throws Exception {
        int before=failures.size();
        String[] forbidden={String.join("", "shattered","pixel.com"),String.join("","pat","reon"),String.join("","ev","an@")};
        try(java.util.jar.JarFile archive=new java.util.jar.JarFile(jar)){
            for(java.util.jar.JarEntry entry:java.util.Collections.list(archive.entries()))if(entry.getName().endsWith(".class")||entry.getName().endsWith(".properties")){
                String text=new String(archive.getInputStream(entry).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).toLowerCase(java.util.Locale.ROOT);
                for(String token:forbidden)if(text.contains(token))failures.add("35 packaged contact "+entry.getName());
            }
        }
        for(String directory:new String[]{"core/src/main","desktop/src/main","android/src/main","services"})try(java.util.stream.Stream<java.nio.file.Path> paths=java.nio.file.Files.walk(java.nio.file.Path.of(directory))){
            paths.filter(p->p.toString().endsWith(".java")||p.toString().endsWith(".properties")).forEach(p->{try{
                String text=java.nio.file.Files.readString(p).replaceAll("(?s)/\\*.*?\\*/", "").toLowerCase(java.util.Locale.ROOT);
                for(String token:forbidden)if(text.contains(token))failures.add("35 source contact "+p);
            }catch(Exception e){throw new RuntimeException(e);}});
        }
        System.out.println("TEST 35: packaged classes/resources and source contact scan failures="+(failures.size()-before));
    }
    /** Stage 7 extends this renderer, including GPU completion in the effects timing. */
    private void effectsTests() {
        boolean original=SPDSettings.enhancedEffects();float elapsed=Game.elapsed;
        try {
            java.util.List<String> failures=new java.util.ArrayList<>();
            float zoom=com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.defaultZoom;
            Camera camera=new Camera(0,0,256,256,zoom);camera.fullScreen=true;
            camera.matrix[0]=2*zoom/256;camera.matrix[5]=-2*zoom/256;camera.matrix[12]=-1;camera.matrix[13]=1;
            com.badlogic.gdx.graphics.glutils.FrameBuffer buffer=new com.badlogic.gdx.graphics.glutils.FrameBuffer(Pixmap.Format.RGBA8888,256,256,false);
            SPDSettings.enhancedEffects(false);GameScene.updateMap();Game.elapsed=1f/60;
            Image grass=com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap.tile(Dungeon.hero.pos,Terrain.HIGH_GRASS);
            com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle actual=new com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle();actual.reset(0,0);
            // The retained upstream particle is the reference, with its original lifetime and acceleration.
            com.watabou.noosa.particles.PixelParticle.Shrinking legacy=new com.watabou.noosa.particles.PixelParticle.Shrinking(){
                {color(0xEE7722);left=lifespan=.6f;size=4;acc.set(0,-80);}
                @Override public void update(){super.update();float p=left/lifespan;am=p>.8f?(1-p)*5:1;}
            };
            int changed=0;
            for(int f=0;f<20;f++){
                actual.update();legacy.update();
                Pixmap a=renderBurningGrass(grass,actual,buffer,camera),b=renderBurningGrass(grass,legacy,buffer,camera);
                for(int y=0;y<256;y++)for(int x=0;x<256;x++)if(a.getPixel(x,y)!=b.getPixel(x,y))changed++;
                if(f==12){PixmapIO.writePNG(Gdx.files.absolute("verification/effects-off.png"),a);PixmapIO.writePNG(Gdx.files.absolute("verification/effects-upstream.png"),b);}
                a.dispose();b.dispose();
            }
            actual.destroy();legacy.destroy();grass.destroy();
            if(changed!=0)failures.add("31 upstream-style burning grass pixel differences="+changed);
            SPDSettings.enhancedEffects(true);GameScene.updateMap();
            // Test actual fire expiration through the decal owner, including forbidden terrain.
            int cell=-1;for(int c=0;c<Dungeon.level.length();c++)if(Dungeon.level.insideMap(c)&&Dungeon.level.map[c]==Terrain.EMPTY
                    &&com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(c)==null&&Dungeon.level.heaps.get(c)==null&&Dungeon.level.traps.get(c)==null){cell=c;break;}
            if(cell<0)throw new AssertionError("No floor for scorch test");
            java.lang.reflect.Field field=GameScene.class.getDeclaredField("bloodDecals");field.setAccessible(true);Group decals=(Group)field.get(Game.scene());
            for(int i=0;i<3;i++){Image decal=GameScene.createScorchDecal(cell,i);if(decal==null)failures.add("32 floor missing");else{band(decal,16,.3f,.6f,buffer,camera,zoom,failures,"32 scorch "+i);decal.destroy();}}
            for(int terrain:new int[]{Terrain.EMPTY,Terrain.WATER,Terrain.CHASM}){
                Level.set(cell,terrain);int before=decals.length;Fire fire=new Fire();fire.seed(Dungeon.level,cell,1);fire.act();
                int expected=terrain==Terrain.EMPTY?1:0;if(decals.length-before!=expected)failures.add("32 expiration terrain="+terrain+" decals="+(decals.length-before));
                if(terrain!=Terrain.EMPTY&&GameScene.createScorchDecal(cell,0)!=null)failures.add("32 forbidden scorch "+terrain);
            }
            Level.set(cell,Terrain.EMPTY);
            System.out.println("TEST 32: three scorch sizes, actual floor fire expiration, water/chasm rejection failures="+failures.size());
            // Forty gas and ten fire cells inside one viewport, with the same emitter path as GameScene.
            ToxicGas gas=new ToxicGas();Fire fire=new Fire();int width=Dungeon.level.width();
            int start=2+2*width;boolean[] fov=Dungeon.level.heroFOV.clone();
            for(int i=0;i<40;i++){int c=start+i%8+(i/8)*width;gas.seed(Dungeon.level,c,100);Dungeon.level.heroFOV[c]=true;if(i<10)fire.seed(Dungeon.level,c,4);}
            buffer.dispose();buffer=new com.badlogic.gdx.graphics.glutils.FrameBuffer(Pixmap.Format.RGBA8888,768,768,false);
            camera.matrix[0]=2*zoom/768;camera.matrix[5]=-2*zoom/768;
            Group fx=new Group();fx.camera=camera;fx.add(new BlobEmitter(gas));fx.add(new BlobEmitter(fire));
            camera.matrix[12]=-1-32*2*zoom/768;camera.matrix[13]=1+32*2*zoom/768;
            double[] millis=new double[240];
            for(int i=-90;i<millis.length;i++){
                buffer.begin();Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);com.watabou.glwrap.Texture.clear();com.watabou.noosa.NoosaScript.get().resetCamera();Gdx.gl.glFinish();
                long begin=System.nanoTime();fx.update();fx.draw();Gdx.gl.glFinish();double ms=(System.nanoTime()-begin)/1e6;
                if(i>=0)millis[i]=ms;if(i==120){Pixmap image=Pixmap.createFromFrameBuffer(0,0,768,768);PixmapIO.writePNG(Gdx.files.absolute("verification/effects-on.png"),image);image.dispose();}buffer.end();
            }
            double average=java.util.Arrays.stream(millis).average().getAsDouble();java.util.Arrays.sort(millis);double p95=millis[227];
            if(average>=2||p95>=2)failures.add("31 enhanced mean="+average+"ms p95="+p95+"ms must both be < 2ms");
            System.out.printf(java.util.Locale.ROOT,"TEST 31: off pixel differences=%d; 40 gas + 10 fire cells, 240 GPU-completed frames mean=%.4fms p95=%.4fms failures=%d%n",changed,average,p95,failures.size());
            fx.destroy();System.arraycopy(fov,0,Dungeon.level.heroFOV,0,fov.length);buffer.dispose();
            if(!failures.isEmpty())throw new AssertionError(failures.toString());
        }catch(Exception e){throw new RuntimeException(e);}
        finally{SPDSettings.enhancedEffects(original);Game.elapsed=elapsed;GameScene.updateMap();}
    }
    private Pixmap renderBurningGrass(Image grass,Image flame,com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,Camera camera){
        buffer.begin();Gdx.gl.glDisable(Gdx.gl.GL_SCISSOR_TEST);Gdx.gl.glClearColor(0,0,0,0);Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
        com.watabou.glwrap.Blending.useDefault();com.watabou.glwrap.Texture.clear();com.watabou.noosa.NoosaScript.get().resetCamera();
        grass.x=grass.y=16;grass.camera=camera;grass.draw();flame.x=22;flame.y=24;flame.camera=camera;flame.draw();
        Pixmap image=Pixmap.createFromFrameBuffer(0,0,256,256);buffer.end();return image;
    }
    private Pixmap renderSprite(com.watabou.noosa.Image image,com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,com.watabou.noosa.Camera camera)throws Exception {
        // Generated ground shadows are separate from the sprite's alpha silhouette.
        if(image instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite){java.lang.reflect.Field shadow=com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class.getDeclaredField("renderShadow");shadow.setAccessible(true);shadow.setBoolean(image,false);}
        image.x=16;image.y=8;image.camera=camera;
        buffer.begin();Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_SCISSOR_TEST);Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);Gdx.gl.glClearColor(0,0,0,0);Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
        // Widget construction may upload a libGDX font texture between these draws.
        // Match the game's per-frame binding reset before drawing the measured image.
        com.watabou.glwrap.Texture.clear();com.watabou.noosa.NoosaScript.get().resetCamera();image.draw();
        Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,256,256);buffer.end();com.watabou.glwrap.Blending.useDefault();return pixels;
    }
    private void band(com.watabou.noosa.Image sprite,float footprint,float low,float high,com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,com.watabou.noosa.Camera camera,float zoom,java.util.List<String> failures,String name)throws Exception {
        Pixmap p=renderSprite(sprite,buffer,camera);int top=256,bottom=-1;
        for(int y=0;y<256;y++)for(int x=0;x<256;x++)if((p.getPixel(x,y)&255)!=0){top=Math.min(top,y);bottom=Math.max(bottom,y);}
        float ratio=(bottom-top+1)/(footprint*zoom);p.dispose();
        if(ratio<low||ratio>high)failures.add(name+" height/tile="+ratio+" logical="+sprite.width+"x"+sprite.height+" opaque="+GameGeometry.opaqueHeight(sprite.texture,sprite.frame())+" frame="+sprite.frame()+" scale="+sprite.scale);
    }
    private void exactRectangle(com.watabou.noosa.Image sprite,int index,int cellSize,com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,com.watabou.noosa.Camera camera,float zoom,java.util.List<String> failures,String name)throws Exception {
        com.watabou.utils.RectF uv=sprite.frame();int w=Math.round(uv.width()*sprite.texture.width),h=Math.round(uv.height()*sprite.texture.height);
        int x=index%16*cellSize,y=index/16*cellSize;
        if(Math.round(uv.left*sprite.texture.width)!=x||Math.round(uv.top*sprite.texture.height)!=y||w>cellSize||h>cellSize){failures.add("25 atlas boundary "+name);return;}
        sprite.logicalSize(w/zoom,h/zoom);Pixmap p=renderSprite(sprite,buffer,camera);
        for(int j=0;j<h;j++)for(int i=0;i<w;i++) {
            int expected=sprite.texture.bitmap.getPixel(x+i,y+j),actual=p.getPixel(Math.round(16*zoom)+i,255-Math.round(8*zoom)-j);
            if(actual!=expected){failures.add("25 pixel mismatch "+name+" at "+i+","+j);p.dispose();return;}
        }
        p.dispose();
    }
}
