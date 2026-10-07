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
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;

/** Opt-in launch diagnostic: renders real OpenGL frames, writes evidence, exits. */
final class DesktopSmokeProbe extends ShatteredPixelDungeon {
    private final boolean sewers;
    private final boolean vault=Boolean.getBoolean("grimhollow.vault");
    private final boolean encounters=Boolean.getBoolean("grimhollow.encounterTests");
    private final boolean interfaceReview=Boolean.getBoolean("grimhollow.interfaceReview");
    private final boolean expeditionReview=Boolean.getBoolean("grimhollow.expeditionReview");
    private final boolean horrorReview=Boolean.getBoolean("grimhollow.horrorReview");
    private final boolean bountyReview=Boolean.getBoolean("grimhollow.bountyReview");
    private int bountyStep;
    private com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole bountyCole;
    private final java.util.List<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob> bountyQuarries=new java.util.ArrayList<>();
    private int horrorFrames, horrorStep, horrorStart, horrorHealth;
    private com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LurkingHorror reviewHorror;
    private boolean[] horrorFov, horrorVisited, horrorMapped;
    private boolean horrorWarningCaptured;
    private int expeditionStep, expeditionFrames, expeditionTown, expeditionExitAttempts;
    private com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter expeditionHunter;
    private final boolean roomReview=Boolean.getBoolean("grimhollow.roomReview");
    private int roomStep, roomFrames, ritualTable, ritualCage, roomCenter;
    private final boolean presentationReview=Boolean.getBoolean("grimhollow.presentationReview");
    private boolean presentationStarted;
    private final java.util.HashSet<Integer> loadingCaptures=new java.util.HashSet<>();
    private boolean[] hatchlingFov,hatchlingVisited,hatchlingMapped;
    private int hatchlingHiddenCell;
    private com.shatteredpixel.shatteredpixeldungeon.items.Item warningMeal;
    private int presentationGameFrames;
    private int encounterActions, encounterSteps, encounterAttacks, encounterLastCell=-1;
    private int[] encounterVisits;
    private volatile boolean encounterDrops, encounterSummon, encounterDeath;
    private boolean encounterDeathScheduled;
    private int encounterDeathFrames;
    private com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.vault.VaultFinalRoom vaultArena;
    private com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.VaultBossElemental vaultBoss;
    private int frames;
    private int polishWaitFrames;
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
        // A fresh isolated settings directory defaults to the sealed tutorial room.
        // Encounter coverage requires an ordinary generated dungeon, like the saved run.
        if(encounters||interfaceReview||presentationReview)SPDSettings.intro(false);
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
        if(interfaceReview && Game.scene() instanceof InterlevelScene && InterlevelScene.lastRegion>0
                && !loadingCaptures.contains(InterlevelScene.lastRegion)){
            Image painting=(Image)RecoveryChecks.field(Game.scene(),"background");
            if(painting.texture.width!=1600||painting.width()+.01f<Camera.main.width
                    ||painting.height()+.01f<Camera.main.height)throw new AssertionError("Region painting does not fill the viewport");
            // The scene switch occurs after the previous scene's draw on this frame.
            Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);Game.scene().draw();
            capture((expeditionReview?"expedition-loading-":"loading-")+new String[]{"sewers","prison","caves","city","halls"}[Math.min(4,InterlevelScene.lastRegion-1)]);
            loadingCaptures.add(InterlevelScene.lastRegion);
        }
        if(horrorReview && frames>180) { horrorTick(); return; }
        if(expeditionReview && frames>180) { expeditionTick(); return; }
        if(presentationReview && frames>180) { presentationTick(); return; }
        if(roomReview && frames>180) { roomTick(); return; }
        if(interfaceReview && frames>180) { interfaceTick(); return; }
        if(encounters && frames>180) { encounterTick(); return; }
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
        if(Boolean.getBoolean("grimhollow.polishReview")){
            if(frames==100){
                RecoveryChecks.titleControls();capture("polish-home");
                pointerGestureReview(RecoveryChecks.field(Game.scene(),"btnChanges"),
                        Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);
            }else if(frames==125){
                if(!(Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene))throw new AssertionError("Home Update Log did not open");
                capture("polish-update-log");scrollReview(Game.scene());
            }else if(frames==150){
                capture("polish-update-log-scrolled");
                SPDSettings.version(com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.versionCode-1);
                SPDSettings.intro(false);
                switchNoFade(com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene.class);
            }else if(frames==170){
                if(!(Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene))throw new AssertionError("Update scene did not launch");
                capture("polish-updated");
                boolean clicked=false;
                for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))
                    if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton &&
                            com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene.class,"continue").equals(((com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)child).text())){
                        pointerGestureReview(child,Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);clicked=true;break;
                    }
                if(!clicked)throw new AssertionError("Update Continue button missing");
            }
        }
        if (vault && sewers) vaultFrames();
        if (frames==180) {
            if (!(Game.scene() instanceof TitleScene)) throw new AssertionError("Title scene did not launch");
            if(!expeditionReview && !horrorReview)capture("title");
            if(presentationReview) {
                GamesInProgress.selectedClass=null;
                switchNoFade(com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene.class);
                return;
            }
            if(recovery!=null) {
                RecoveryChecks.titleControls();
                // Journal deliberately clears run item-identification state on entry;
                // exercise its menu flow before creating the diagnostic dungeon.
                if(reviewRegion==0)RecoveryChecks.linkHandlers();
            }
            if (!sewers) { Gdx.app.exit(); return; }
            GamesInProgress.selectedClass=(encounters||vault||Boolean.getBoolean("grimhollow.renderPoc"))?HeroClass.NECROMANCER:HeroClass.WARRIOR;
            if(interfaceReview)GamesInProgress.selectedClass=HeroClass.PSYCHIC;
            if(System.getProperty("grimhollow.heroClass")!=null)
                GamesInProgress.selectedClass=HeroClass.valueOf(System.getProperty("grimhollow.heroClass"));
            GamesInProgress.curSlot=99;
            if(encounters && Integer.getInteger("grimhollow.saveSlot",0)>0) {
                try {
                    GamesInProgress.curSlot=Integer.getInteger("grimhollow.saveSlot");
                    Dungeon.loadGame(GamesInProgress.curSlot);
                    Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot),Dungeon.hero.pos);
                } catch(java.io.IOException e) { throw new AssertionError(e); }
            }
            else if(recovery!=null) recovery.prepare();
            else if(vault) vaultRoom();
            else if(Boolean.getBoolean("grimhollow.iteration")) iterationRoom();
            else {
                Dungeon.seed=Long.getLong("grimhollow.seed",417L);
                Dungeon.init();
                if(bountyReview)Dungeon.depth=7;
                if(roomReview)prepareRooms(0);
                else Dungeon.switchLevel(Dungeon.newLevel(),-1);
                if(Boolean.getBoolean("grimhollow.renderPoc"))pocRoom();
                if(horrorReview)prepareHorror();
            }
            // Optional armor variant for the existing native character-art capture.
            // Isolated diagnostic saves only; normal game equipment is untouched.
            if (Integer.getInteger("grimhollow.heroArmorTier",1)==5) {
                Dungeon.hero.belongings.armor = new com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor();
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

    /** Quest coverage inside the existing native fixture, using real mouse/touch input. */
    private void bountyTick(){
        {
            int step=bountyStep++;
            if(step==0){
                Playtest.enable();Dungeon.hero.HT=Dungeon.hero.HP=1000;Dungeon.hero.lvl=30;Dungeon.gold=30000;
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole)bountyCole=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Cole)m;
                if(bountyCole==null)throw new AssertionError("Generated office has no Cole");
                for(int n:com.watabou.utils.PathFinder.NEIGHBOURS8)if(Dungeon.level.passable[bountyCole.pos+n]&&Actor.findChar(bountyCole.pos+n)==null){Dungeon.hero.pos=bountyCole.pos+n;break;}
                Dungeon.hero.sprite.place(Dungeon.hero.pos);Dungeon.observe();Camera.main.snapTo(Dungeon.hero.sprite.center());return;
            }
            if(step<=18){HeroClass hero=HeroClass.values()[(step-1)/2];
                if(step%2==1){if(step==1)capture("bounty-office");closeReviewWindows();Dungeon.hero.heroClass=hero;bountyCole.interact(Dungeon.hero);}
                else{interfaceBounds();checkReviewText(Game.scene());if(!allReviewText(Game.scene()).contains(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(bountyCole,"greet_"+hero.name())))throw new AssertionError("Missing greeting "+hero);capture("bounty-greet-"+hero.name().toLowerCase());}return;
            }
            if(step<=32){int species=(step-19)/2;
                if(step%2==1){closeReviewWindows();BountyBoard.Contract c=new BountyBoard.Contract();c.index=species>=5?3:species>=2?1:0;c.species=species;c.floor=species>=5?10:8;c.payment=c.index==1?1200:600;GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract(c));}
                else{interfaceBounds();checkReviewText(Game.scene());bountyPosterLayoutCheck();capture("bounty-poster-"+species);scrollReview(Game.scene());}return;
            }
            if(step==33){closeReviewWindows();BountyBoard.Contract c=new BountyBoard.Contract();c.index=2;c.species=2;c.floor=8;GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract(c));return;}
            if(step==34){interfaceBounds();checkReviewText(Game.scene());bountyPosterLayoutCheck();capture("bounty-poster-7");return;}
            if(step==35){closeReviewWindows();bountySkinCheck();placeBountyQuarries();return;}
            if(step==36){capture("bounty-quarries");for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:bountyQuarries){m.destroy();m.sprite.killAndErase();}bountyQuarries.clear();return;}
            step-=4;
            switch(step){
                case 33:closeReviewWindows();BountyBoard.contracts[0].floor=7;GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract(BountyBoard.contracts[0]));break;
                case 34:playtestClick(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(bountyCole,"accept"));break;
                case 35:
                    if(!BountyBoard.contracts[0].accepted||!com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.ownedContract(0))throw new AssertionError("Native acceptance failed");
                    bountySealCheck();
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.class)));break;
                case 36:interfaceBounds();capture("bounty-warrant");closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob(bountyCole));break;
                case 37:
                    interfaceBounds();capture("bounty-cole-description");
                    for(com.watabou.noosa.Gizmo child:new java.util.ArrayList<>(RecoveryChecks.members(Game.scene())))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob){Object title=RecoveryChecks.field(child,"titlebar");pointerGestureReview(RecoveryChecks.field(title,"artworkButton"),Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);break;}break;
                case 38:
                    interfaceBounds();boolean enlarged=false;for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndArtwork)enlarged=true;
                    if(!enlarged)throw new AssertionError("Cole image did not open artwork viewer");capture("bounty-cole-artwork");closeReviewWindows();
                    for(com.shatteredpixel.shatteredpixeldungeon.items.Heap h:Dungeon.level.heaps.valueList())if(BountyBoard.owns(h)&&h.coleSlot==0){GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem(h));break;}break;
                case 39:interfaceBounds();capture("bounty-shop");playtestClick(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem.class,"buy",200));break;
                case 40:if(BountyBoard.stock[0]!=null)throw new AssertionError("Native purchase failed");closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal());break;
                case 41:interfaceBounds();capture("bounty-journal");closeReviewWindows();com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.quests();break;
                case 42:menuEntry("Cole and Bounty Board");menuEntry("Bounty balance controls");menuEntry("Hunter crews and Cole combat");menuEntry("Cole Bolas ammunition: 2");break;
                case 43:interfaceBounds();capture("bounty-tuning-input");playtestInput("3","Apply");break;
                case 44:
                    if(BalanceTuning.get(BalanceTuning.Key.COLE_BOLAS)!=3)throw new AssertionError("Native tuning failed");BalanceTuning.setShared(BalanceTuning.Key.COLE_BOLAS,2);closeReviewWindows();BountyBoard.betrayed=true;BountyBoard.arrive(Dungeon.level);
                    for(int i=0;i<3;i++){BountyBoard.crews[i].complete=true;com.shatteredpixel.shatteredpixeldungeon.items.quest.Warrant.hunter(i).collect(Dungeon.hero.belongings.backpack);}BountyBoard.showSettlement(3);break;
                case 45:interfaceBounds();checkReviewText(Game.scene());for(com.shatteredpixel.shatteredpixeldungeon.items.Item item:BountyBoard.settlementThree)if(!allReviewText(Game.scene()).contains(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(item.title())))throw new AssertionError("Missing professional item name");capture("bounty-professional-offers");closeReviewWindows();BountyBoard.showSettlement(2);break;
                case 46:interfaceBounds();checkReviewText(Game.scene());for(com.shatteredpixel.shatteredpixeldungeon.items.Item item:BountyBoard.settlementTwo)if(!allReviewText(Game.scene()).contains(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(item.title())))throw new AssertionError("Missing negotiated item name");capture("bounty-negotiated-offers");closeReviewWindows();break;
                default:
                    if(step>=47&&step<65){HeroClass hero=HeroClass.values()[(step-47)/2];
                        if(step%2==1){closeReviewWindows();Dungeon.hero.heroClass=hero;GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyBetrayal(bountyCole));}
                        else{interfaceBounds();checkReviewText(Game.scene());capture("bounty-betray-"+hero.name().toLowerCase());scrollReview(Game.scene());}
                    }else if(step>=65&&step<71){
                        int choice=(step-65)/3,phase=(step-65)%3;
                        if(phase==0){
                            closeReviewWindows();questField(GameScene.class,"scene",null);Dungeon.init();Playtest.enable();Dungeon.hero.HT=Dungeon.hero.HP=1000;Dungeon.hero.lvl=30;Dungeon.depth=10;
                            BountyBoard.planContracts();BountyBoard.contracts[0].returned=BountyBoard.contracts[1].returned=true;BountyBoard.bossChoice=choice;BountyBoard.planBoss();
                            Dungeon.switchLevel(Dungeon.newLevel(),-1);if(!BountyBoard.accept(3))throw new AssertionError("Actual boss contract failed");InterlevelScene.mode=InterlevelScene.Mode.DESCEND;switchNoFade(GameScene.class);
                        }else if(phase==1){
                            com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel level=(com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level;
                            level.progress();level.progress();level.progress();
                        }else{
                            com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel level=(com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel)Dungeon.level;
                            level.bountyBoss().HP=0;level.progress();boolean mask=false;
                            for(com.shatteredpixel.shatteredpixeldungeon.items.Heap heap:level.heaps.valueList())for(com.shatteredpixel.shatteredpixeldungeon.items.Item item:heap.items)if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.TengusMask)mask=true;
                            if(!mask||!BountyBoard.contracts[3].complete||level.state()!=com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel.State.WON)throw new AssertionError("Actual boss reward/progression lost "+choice);
                            capture("bounty-boss-"+choice);
                        }
                    }else if(step==71){closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract(BountyBoard.contracts[3]));}
                    else if(step==72){interfaceBounds();checkReviewText(Game.scene());bountyPosterLayoutCheck();capture("bounty-completed-poster");}
                    else if(step==73||step==76){
                        closeReviewWindows();questField(GameScene.class,"scene",null);Dungeon.init();Playtest.enable();
                        Dungeon.hero.HT=Dungeon.hero.HP=1000;Dungeon.hero.lvl=30;Dungeon.depth=step==73?25:26;
                        Dungeon.switchLevel(Dungeon.newLevel(),-1);
                        Dungeon.hero.pos=step==73?Dungeon.level.exit()+3*Dungeon.level.width()+2:com.shatteredpixel.shatteredpixeldungeon.levels.LastLevel.AMULET_POS+2;
                        InterlevelScene.mode=InterlevelScene.Mode.DESCEND;switchNoFade(GameScene.class);
                    }else if(step==74||step==77){Dungeon.observe();Camera.main.snapTo(Dungeon.hero.sprite.center());}
                    else if(step==75||step==78){capture(step==75?"painted-yog-platform":"painted-amulet-sanctum");}
                    else{closeReviewWindows();System.out.println("BOUNTY UI PASS: all nine greetings/betrayals, eight painted posters and completion stamp, six unique quarry skins/save-load/unchanged species animations, mouse/touch acceptance and purchase, unseen/invisible wanted-seal suppression, Warrant/journal/artwork, tuning, settlements, both actual boss deaths/mask rewards and painted final sanctums; failures=0");Gdx.app.exit();}
            }
        }
    }

    private BountyBoard.Contract quarryContract(int i){
        BountyBoard.Contract c=new BountyBoard.Contract();c.index=i==5?2:i<2?0:1;c.species=new int[]{0,1,2,3,4,2}[i];return c;
    }

    private void bountySkinCheck(){
        java.util.Set<Object> textures=new java.util.HashSet<>();
        for(int i=0;i<6;i++){
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob quarry=quarryContract(i).preview();
            com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite painted=quarry.sprite();
            if(painted.getClass()!=com.shatteredpixel.shatteredpixeldungeon.sprites.WantedSprites.typeFor(quarry)||painted.getClass()==quarry.spriteClass)throw new AssertionError("Missing named quarry skin "+i);
            if(!textures.add(painted.texture))throw new AssertionError("Two quarry skins share a texture");
            com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();quarry.storeInBundle(saved);
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob restored=com.watabou.utils.Reflection.newInstance(quarry.getClass());restored.restoreFromBundle(saved);
            if(restored.sprite().getClass()!=painted.getClass())throw new AssertionError("Quarry skin lost on reload "+i);
            quarry.bountyContract=-1;
            com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite ordinary=quarry.sprite();
            if(ordinary.getClass()!=quarry.spriteClass||ordinary.texture==painted.texture)throw new AssertionError("Ordinary species artwork changed "+i);
            for(String name:new String[]{"idle","run","attack","die","zap"}){
                com.watabou.noosa.MovieClip.Animation a=(com.watabou.noosa.MovieClip.Animation)RecoveryChecks.field(painted,name),b=(com.watabou.noosa.MovieClip.Animation)RecoveryChecks.field(ordinary,name);
                if(a==null||b==null){if(a!=b)throw new AssertionError("Quarry animation missing "+name);continue;}
                if(a.delay!=b.delay||a.looped!=b.looped||a.frames.length!=b.frames.length)throw new AssertionError("Quarry animation timing changed "+i+" "+name);
                for(int f=0;f<a.frames.length;f++)if(a.frames[f].left!=b.frames[f].left||a.frames[f].top!=b.frames[f].top||a.frames[f].right!=b.frames[f].right||a.frames[f].bottom!=b.frames[f].bottom)throw new AssertionError("Quarry animation layout changed "+i+" "+name);
            }
        }
        System.out.println("WANTED SKINS: six distinct textures; ordinary species unchanged; save/load and animation timing/layout preserved; failures=0");
    }

    private void placeBountyQuarries(){
        for(int i=0;i<6;i++){
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob=quarryContract(i).preview();int cell=-1;
            for(int distance=1;distance<=5&&cell<0;distance++)for(int p=0;p<Dungeon.level.length();p++)if(Dungeon.level.heroFOV[p]&&Dungeon.level.passable[p]&&Dungeon.level.distance(Dungeon.hero.pos,p)==distance&&Actor.findChar(p)==null){cell=p;break;}
            if(cell<0)throw new AssertionError("No visible quarry review cell "+i);
            mob.pos=cell;mob.state=mob.PASSIVE;GameScene.add(mob);bountyQuarries.add(mob);
        }
        Dungeon.observe();
    }

    private void bountyPosterLayoutCheck(){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene())){
            if(!(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndBountyContract))continue;
            float center=((Number)RecoveryChecks.field(child,"paperWidth")).floatValue()/2;
            float previous=-1;
            for(String name:new String[]{"heading","quarry","target","flavorTitle","flavor","location","reward","urgency","signature","seal"}){
                Object part=RecoveryChecks.field(child,name);if(part==null)continue;
                float x,y,width,height;
                if(part instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock){
                    com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock text=(com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)part;
                    x=text.left();y=text.top();width=text.width();height=text.height();
                    if(name.equals("heading")&&!text.text().equals("WANTED"))throw new AssertionError("Poster announcement is not WANTED");
                    java.util.Map<Integer,float[]> lines=new java.util.LinkedHashMap<>();
                    for(com.watabou.noosa.Gizmo glyph:RecoveryChecks.members(text))if(glyph instanceof com.watabou.noosa.RenderedText){
                        com.watabou.noosa.RenderedText word=(com.watabou.noosa.RenderedText)glyph;
                        if((Boolean)RecoveryChecks.field(word,"border"))throw new AssertionError("Poster ink uses a heavy outline");
                        float[] line=lines.computeIfAbsent(Math.round(word.y*256),key->new float[]{Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY});
                        if(line[1]!=Float.NEGATIVE_INFINITY&&word.x-line[1]<1.5f)throw new AssertionError("Cramped poster word spacing "+name);
                        line[0]=Math.min(line[0],word.x);line[1]=Math.max(line[1],word.x+word.width());
                    }
                    for(float[] line:lines.values())if(Math.abs((line[0]+line[1])/2-center)>.75f)throw new AssertionError("Off-center ink line "+name);
                }else{
                    com.watabou.noosa.Image image=(com.watabou.noosa.Image)part;
                    x=image.x;y=image.y;width=image.width();height=image.height();
                    float pixels=image.frame().width()*image.texture.width;
                    if(name.equals("target")&&(Math.abs(pixels-512)>.1f||height<96))throw new AssertionError("Low-resolution or small wanted portrait");
                    if(name.equals("seal")&&(Math.abs(pixels-256)>.1f||height<60))throw new AssertionError("Low-resolution or small rarity seal");
                }
                if(Math.abs(x+width/2-center)>.75f)throw new AssertionError("Off-center poster part "+name);
                if(y<previous)throw new AssertionError("Poster hierarchy overlaps at "+name);
                previous=y+height;
            }
        }
    }

    private void bountySealCheck(){
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob=BountyBoard.contracts[0].target;
        if(mob==null||mob.sprite==null)throw new AssertionError("Accepted target not placed");
        boolean fov=Dungeon.level.heroFOV[mob.pos],shown=mob.sprite.visible;int hidden=mob.invisible,index=mob.bountyContract;
        com.shatteredpixel.shatteredpixeldungeon.ui.CharHealthIndicator indicator=new com.shatteredpixel.shatteredpixeldungeon.ui.CharHealthIndicator(mob);
        try{
            mob.sprite.visible=true;mob.invisible=0;Dungeon.level.heroFOV[mob.pos]=false;indicator.update();
            com.watabou.noosa.Image seal=(com.watabou.noosa.Image)RecoveryChecks.field(indicator,"wantedSeal");
            if(seal.visible)throw new AssertionError("Wanted seal reveals an unseen target");
            Dungeon.level.heroFOV[mob.pos]=true;
            for(int rarity=0;rarity<3;rarity++){mob.bountyContract=rarity;indicator.update();if(!seal.visible)throw new AssertionError("Missing visible wanted seal "+rarity);}
            mob.invisible=1;indicator.update();if(seal.visible)throw new AssertionError("Wanted seal reveals invisible target");
            mob.invisible=0;mob.sprite.visible=false;indicator.update();if(seal.visible)throw new AssertionError("Wanted seal reveals hidden sprite");
        }finally{Dungeon.level.heroFOV[mob.pos]=fov;mob.sprite.visible=shown;mob.invisible=hidden;mob.bountyContract=index;indicator.killAndErase();}
    }

    /** Exercise actual selection buttons and their read-only progression windows. */
    private void presentationTick() {
        int step=frames-220;
        if(step<0)return;
        int index=step/120, phase=step%120;
        if(index>=HeroClass.values().length){
            if(!presentationStarted){
                closeReviewWindows();
                @SuppressWarnings("unchecked") java.util.List<Object> choices=(java.util.List<Object>)RecoveryChecks.field(Game.scene(),"heroBtns");
                clickReview(choices.get(HeroClass.DUELIST.ordinal()));
                GamesInProgress.curSlot=99;
                clickReview(RecoveryChecks.field(Game.scene(),"startBtn"));
                presentationStarted=true;return;
            }
            if(!(Game.scene() instanceof GameScene)){
                if(Game.scene() instanceof InterlevelScene){
                    Object proceed=RecoveryChecks.field(Game.scene(),"btnContinue");
                    if(proceed instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton
                            && ((com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)proceed).active)clickReview(proceed);
                }
                if(frames>4000)throw new AssertionError("Duelist start stalled");
                return;
            }
            if(presentationGameFrames++==0){
                if(Dungeon.hero.heroClass!=HeroClass.DUELIST)throw new AssertionError("Duelist Start loaded the wrong hero");
                pocRoom();
                for(int shape=0;shape<7;shape++){
                    com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap();
                    trap.shape=shape;trap.color=shape;trap.visible=true;
                    int pos=Dungeon.hero.pos+(1+shape/4)*Dungeon.level.width()+shape%4-3;
                    Level.set(pos,Terrain.TRAP);Dungeon.level.setTrap(trap,pos);
                }
                Dungeon.observe();switchNoFade(GameScene.class);return;
            }
            Camera.main.edgeScroll.set(0);
            Camera.main.snapTo(Dungeon.hero.sprite.center().x,Dungeon.hero.sprite.center().y);
            if(presentationGameFrames<90)return;
            if(presentationGameFrames>90){botanyAndInscriptionReview(presentationGameFrames);return;}
            capture("painted-traps-and-duelist-hud");
            if(!HeroClass.DUELIST.isUnlocked())throw new AssertionError("Duelist cannot be selected");
            System.out.println("TEST 36 PRESENTATION: classes=9 matchingPortraits=9 firstSelections=9 secondSelections=9 infoButtons=9 handbookPages=36 orientation="
                    +(Boolean.getBoolean("grimhollow.interfacePortrait")?"portrait":"landscape")+" duelistStart=true trapShapes=7 failures=0");
            return;
        }
        HeroClass hero=HeroClass.values()[index];
        @SuppressWarnings("unchecked") java.util.List<com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton> buttons=
                (java.util.List<com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton>)RecoveryChecks.field(Game.scene(),"heroBtns");
        if(phase==0){closeReviewWindows();clickReview(buttons.get(index));}
        if(phase==15){
            if(GamesInProgress.selectedClass!=hero)throw new AssertionError("Selection did not choose "+hero);
            for(com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton b:buttons){
                if(b.left()<0||b.top()<0||b.right()>Camera.main.width||b.bottom()>Camera.main.height||!b.active)
                    throw new AssertionError("Class button clipped or inactive");
            }
            if(hero.theme().contains("!!!")||hero.theme().length()<20)throw new AssertionError("Missing class theme");
            Image background=(Image)RecoveryChecks.field(Game.scene(),"background");
            if(background.texture!=com.watabou.gltextures.TextureCache.get(hero.splashArt()))throw new AssertionError("Wrong painting");
            Image portrait=buttons.get(index).icon();
            int sx=Math.round(portrait.frame().left*portrait.texture.width),sy=Math.round(portrait.frame().top*portrait.texture.height);
            if(sx!=index%3*128||sy!=index/3*128)throw new AssertionError("Wrong portrait crop");
            capture("selection-"+hero.name().toLowerCase(java.util.Locale.ROOT));
        }
        if(phase==20)clickReview(buttons.get(index));
        if(phase==35||phase==50||phase==70||phase==90){
            interfaceBounds();
            com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo handbook=reviewHandbook();
            @SuppressWarnings("unchecked") java.util.List<com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane> pages=
                    (java.util.List<com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane>)RecoveryChecks.field(handbook,"pages");
            if(pages.size()!=4)throw new AssertionError("Incomplete class handbook");
            int visible=0;
            for(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane page:pages)if(page.visible){
                visible++;
                com.watabou.utils.Point at=page.camera().cameraToScreen(page.left(),page.top());
                if(page.content().camera.x!=at.x||page.content().camera.y!=at.y)throw new AssertionError("Detached handbook scrolling camera");
                checkReviewText(page.content());
            }
            if(visible!=1)throw new AssertionError("Handbook page visibility");
            if(hero==HeroClass.PSYCHIC)capture("handbook-"+(phase==35?"profile":phase==50?"growth":phase==70?"paths":"armor"));
        }
        if(phase==40)reviewHandbook().select(1);
        if(phase==60)reviewHandbook().select(2);
        if(phase==80)reviewHandbook().select(3);
        if(phase==95)scrollReview(reviewHandbook());
        if(phase==105){interfaceBounds();if(hero==HeroClass.PSYCHIC)capture("handbook-armor-bottom");}
        if(phase==110){closeReviewWindows();clickReview(RecoveryChecks.field(Game.scene(),"infoButton"));}
        if(phase==115){reviewHandbook();interfaceBounds();closeReviewWindows();}
    }
    private void botanyAndInscriptionReview(int frame){
        try{
            if(frame==100){
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])){
                    com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);if(mob.sprite!=null)mob.sprite.killAndErase();
                }
                Dungeon.level.mobs.clear();
                for(int i=0;i<PAINTED_PLANTS.length;i++){
                    int pos=Dungeon.hero.pos+(i/4-2)*Dungeon.level.width()+i%4-4;
                    Level.set(pos,Terrain.EMPTY);Dungeon.level.traps.remove(pos);
                    com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed seed=(com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed)Class.forName(plantClass(i)+"$Seed").getDeclaredConstructor().newInstance();
                    if(Dungeon.level.plant(seed,pos)==null)throw new AssertionError("Plant did not sprout");
                }
                Dungeon.observe();GameScene.updateMap();
            }
            if(frame==170)capture("painted-sprouted-plants");
            if(frame==180){
                Dungeon.hero.heroClass=HeroClass.ENCHANTER;
                com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic.class);
                com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush brush=new com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush();
                Dungeon.hero.belongings.artifact=brush;brush.activate(Dungeon.hero);
                brush.execute(Dungeon.hero,"CAST");
            }
            if(frame==200){interfaceBounds();capture("enchanter-spell-icons");}
            if(frame==210)clickReviewLabel(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class,"inscribe"));
            if(frame==220 && !clickInventoryItem(Game.scene(),Dungeon.hero.belongings.armor))throw new AssertionError("Armor missing from Inscribe equipment picker");
            if(frame==235){interfaceBounds();capture("inscribe-starter-armor");}
            if(frame==240){
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(inscriptionWindow(),"pane");
                pointerGestureReview(RecoveryChecks.members(pane.content()).get(1),com.watabou.input.PointerEvent.NONE,0);
                boolean description=false;
                for(com.watabou.noosa.Gizmo g:new java.util.ArrayList<>(RecoveryChecks.members(Game.scene())))if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage){description=true;((com.shatteredpixel.shatteredpixeldungeon.ui.Window)g).hide();}
                if(!description||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).charges()!=3)
                    throw new AssertionError("Touching inscription info did not open a free description");
            }
            if(frame==245){
                com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe wnd=inscriptionWindow();
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(wnd,"pane");
                pointerClickReview(RecoveryChecks.members(pane.content()).get(0));
                if(Dungeon.hero.belongings.armor.inscribed==null||Dungeon.hero.belongings.armor.inscriptionTurns<=0||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).charges()!=2)
                    throw new AssertionError("Actual armor inscription click failed");
            }
            if(frame==265){
                for(Class<?>[] tier:new Class<?>[][]{com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.common,com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.uncommon,com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.rare})
                    java.util.Collections.addAll(Statistics.itemTypesDiscovered,tier);
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe(Dungeon.hero,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class),Dungeon.hero.belongings.armor));
            }
            if(frame==285){
                interfaceBounds();capture("inscribe-full-library");
                com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe wnd=inscriptionWindow();
                wnd.offset(4,-3);
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(wnd,"pane");
                if(pane.content().height()<=pane.height())throw new AssertionError("Full library did not exercise scrolling");
                pane.scrollTo(0,pane.content().height());
                com.watabou.utils.Point at=pane.camera().cameraToScreen(pane.left(),pane.top());
                if(pane.content().camera.x!=at.x||pane.content().camera.y!=at.y)throw new AssertionError("Inscription library scroll camera detached");
            }
            if(frame==300){interfaceBounds();capture("inscribe-full-library-bottom");}
            if(frame==310){
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(inscriptionWindow(),"pane");
                java.util.List<Class<?>> choices=new java.util.ArrayList<>(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnchanterMagic.state().choices(true));
                choices.sort(java.util.Comparator.comparing(com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe::name));
                java.util.List<com.watabou.noosa.Gizmo> rows=RecoveryChecks.members(pane.content());
                pointerClickReview(rows.get(rows.size()-2));
                if(Dungeon.hero.belongings.armor.inscribed.getClass()!=choices.get(choices.size()-1))throw new AssertionError("Bottom inscription chose wrong glyph");
                if(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).charges()!=1)
                    throw new AssertionError("Scrolled inscription did not spend exactly one charge");
                System.out.println("TEST 33/36 BOTANY UI: sprouted plants="+PAINTED_PLANTS.length+" pointer armor selections=2 glyph library="+choices.size()+" scrolling/offset=PASS failures=0");
            }
            if(frame==335){
                com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush brush=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class);brush.gainCharge(3);
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe(Dungeon.hero,brush,Dungeon.hero.belongings.weapon));
            }
            if(frame==345){
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(inscriptionWindow(),"pane");
                pointerGestureReview(RecoveryChecks.members(pane.content()).get(0),com.watabou.input.PointerEvent.NONE,40);
                if(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).charges()!=3
                        ||((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)Dungeon.hero.belongings.weapon).inscribed!=null)
                    throw new AssertionError("Dragging the inscription list cast a spell");
                pane.scrollTo(0,0);
            }
            if(frame==350){
                interfaceBounds();capture("inscribe-starter-weapon");
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(inscriptionWindow(),"pane");
                pointerGestureReview(RecoveryChecks.members(pane.content()).get(0),com.watabou.input.PointerEvent.NONE,0);
                if(((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)Dungeon.hero.belongings.weapon).inscribed==null
                        ||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).charges()!=2)
                    throw new AssertionError("Touch weapon inscription failed");
            }
            if(frame==370){
                com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush brush=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class);brush.gainCharge(-brush.charges());
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe(Dungeon.hero,brush,Dungeon.hero.belongings.armor));
            }
            if(frame==390){
                float time=Dungeon.hero.cooldown();Object prior=Dungeon.hero.belongings.armor.inscribed;
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(inscriptionWindow(),"pane");
                pointerClickReview(RecoveryChecks.members(pane.content()).get(0));
                if(Dungeon.hero.cooldown()!=time||Dungeon.hero.belongings.armor.inscribed!=prior)throw new AssertionError("Empty Brush cast or spent a turn");
                inscriptionWindow();capture("inscribe-no-charge");
                System.out.println("TEST 33 POINTER: mouse armor, scrolled bottom glyph, touch weapon/info, drag suppression, zero-charge rejection; failures=0");Gdx.app.exit();
            }
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe inscriptionWindow(){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe)return (com.shatteredpixel.shatteredpixeldungeon.windows.WndInscribe)child;
        throw new AssertionError("Inscription window missing");
    }
    private void clickReviewLabel(String label){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.Window)
            for(com.watabou.noosa.Gizmo button:RecoveryChecks.members((Group)child))if(button instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton&&label.equals(((com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)button).text())){clickReview(button);return;}
        throw new AssertionError("Missing button "+label);
    }
    private static void clickReview(Object button){
        try{
            for(Class<?> type=button.getClass();type!=null;type=type.getSuperclass())try{
                java.lang.reflect.Method click=type.getDeclaredMethod("onClick");click.setAccessible(true);click.invoke(button);return;
            }catch(NoSuchMethodException ignored){}
            throw new AssertionError("Missing click handler");
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static void pointerClickReview(Object button){
        pointerGestureReview(button,com.watabou.input.PointerEvent.LEFT,0);
    }
    private static void pointerGestureReview(Object button,int pointerButton,int dragPixels){
        com.watabou.noosa.ui.Component target=(com.watabou.noosa.ui.Component)button;
        com.watabou.utils.Point at=target.camera().cameraToScreen(target.centerX(),target.centerY());
        com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(at.x,at.y,901,com.watabou.input.PointerEvent.Type.DOWN,pointerButton));
        com.watabou.input.PointerEvent.processPointerEvents();
        if(dragPixels!=0)for(int step=1;step<=2;step++){
            com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(at.x,at.y+dragPixels*step,901,com.watabou.input.PointerEvent.Type.DOWN,pointerButton));
            com.watabou.input.PointerEvent.processPointerEvents();
        }
        com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(at.x,at.y+dragPixels*2,901,com.watabou.input.PointerEvent.Type.UP,pointerButton));
        com.watabou.input.PointerEvent.processPointerEvents();
    }
    private com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo reviewHandbook(){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo)
                return (com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo)child;
        throw new AssertionError("Class handbook did not open");
    }
    private static void checkReviewText(Group group){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(group)){
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock){
                String text=((com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)child).text();
                if(text.contains("!!!")||text.matches("(?s).*%[0-9]*[$]?[dsf].*"))throw new AssertionError("Unresolved handbook text: "+text);
            }
            if(child instanceof Group)checkReviewText((Group)child);
        }
    }

    /** Review actual inventory, scrolling descriptions and class controls in both orientations. */
    private void interfaceTick() {
        if(bountyReview){if(Game.scene() instanceof GameScene&&frames>=220&&frames%20==0)bountyTick();return;}
        if(Boolean.getBoolean("grimhollow.artworkReview")){
            // Run the same artwork assertions independently of clipboard-heavy
            // Playtest coverage. Normal CI still executes the entire fixture.
            if(Game.scene() instanceof GameScene && frames>=230 && frames%20==0){
                try{if(artworkReview())Gdx.app.exit();}
                catch(ReflectiveOperationException error){throw new AssertionError(error);}
            }
            return;
        }
        if(Boolean.getBoolean("grimhollow.polishReview")){polishTick();return;}
        if(frames>=1200){playtestTick();return;}
        if(!(Game.scene() instanceof GameScene))return;
        Camera.main.edgeScroll.set(0);
        com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal crystal=Dungeon.hero.belongings.getItem(
                com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class);
        if(frames==220)Camera.main.snapTo(Dungeon.hero.sprite.center().x,Dungeon.hero.sprite.center().y);
        if(frames==230)capture("hud");
        if(frames==240) {
            String[] fixtures={"artifacts.HornOfPlenty","artifacts.DriedRose","potions.PotionOfMindVision",
                    "scrolls.ScrollOfUpgrade","scrolls.exotic.ScrollOfEnchantment","stones.StoneOfAugmentation",
                    "wands.WandOfBlastWave","weapon.missiles.darts.PoisonDart","food.Pasty","quest.Pickaxe",
                    "spells.PhaseShift","spells.Alchemize"};
            try {
                for(String name:fixtures)Dungeon.hero.belongings.backpack.items.add(
                        (com.shatteredpixel.shatteredpixeldungeon.items.Item)Class.forName(
                                "com.shatteredpixel.shatteredpixeldungeon.items."+name).getDeclaredConstructor().newInstance());
            }catch(Exception e){throw new RuntimeException(e);}
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBag(Dungeon.hero.belongings.backpack));
        } else if(frames==300) { interfaceBounds();capture("inventory"); }
        else if(frames==320) {
            closeReviewWindows();GameScene.centerNextWndOnInvPane();
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,crystal));
        } else if(frames==380) { interfaceBounds();capture("crystal-description");scrollReview(Game.scene()); }
        else if(frames==420)capture("crystal-description-bottom");
        else if(frames==440) {
            closeReviewWindows();Dungeon.hero.subClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.PUPPETEER;
            crystal.execute(Dungeon.hero,"CAST");
        } else if(frames==500) { interfaceBounds();capture("psychic-spells"); }
        else if(frames==520) { closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndHero()); }
        else if(frames==580) { interfaceBounds();capture("hero-sheet"); }
        else if(frames==600) { closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings()); }
        else if(frames==660) { interfaceBounds();capture("settings"); }
        else if(frames==680) {
            closeReviewWindows();
            if(Boolean.getBoolean("grimhollow.geometryTests"))geometryTests();
            System.out.println("INTERFACE: inventory, item actions, scrollable Crystal details, five-spell wheel, hero sheet and settings fit "
                    +Gdx.graphics.getWidth()+"x"+Gdx.graphics.getHeight()+"; failures=0");
        }
        if(frames>=700)readabilityReview();
    }
    private void journalNoteReview(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark landmark){
        com.shatteredpixel.shatteredpixeldungeon.journal.Notes.LandmarkRecord record=
                new com.shatteredpixel.shatteredpixeldungeon.journal.Notes.LandmarkRecord(landmark,Dungeon.depth);
        GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndJournalItem(record.icon(),record.title(),record.desc()));
    }

    private void polishTick(){
        if(!(Game.scene() instanceof GameScene))return;
        // Advance the fixture only at genuine input boundaries; never remove a mob
        // while its animation is responsible for releasing the actor scheduler.
        if((frames==790||frames==820||frames==850||frames==940)&&!Dungeon.hero.ready){
            if(++polishWaitFrames>1200)throw new AssertionError("Polish action did not finish at "+frames);
            frames--;return;
        }
        polishWaitFrames=0;
        if(frames==220){
            Camera.main.snapTo(Dungeon.hero.sprite.center().x,Dungeon.hero.sprite.center().y);
            for(int i=0;i<40;i++)com.shatteredpixel.shatteredpixeldungeon.utils.MessageHistory.add(com.shatteredpixel.shatteredpixeldungeon.utils.GLog.WARNING+"History entry "+i+": your hatchling mimic grumbles hungrily. It needs to eat!");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGame());
        }else if(frames==240){interfaceBounds();capture("polish-game-menu");playtestClick("Message History");}
        else if(frames==270){
            interfaceBounds();capture("polish-history-newest");
            for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(Game.scene()))
                if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndMessageHistory)
                    for(com.watabou.noosa.Gizmo p:RecoveryChecks.members((Group)g))
                        if(p instanceof com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)
                            ((com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)p).scrollTo(0,0);
        }else if(frames==300){
            interfaceBounds();capture("polish-history-oldest");closeReviewWindows();
            HatchlingMimic pet=new HatchlingMimic();pet.collect();while(!pet.hungry())pet.tick(Dungeon.hero);
            String hunger=com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(HatchlingMimic.class,"hunger_hungry");
            if(!pet.info().contains(hunger)||pet.info().contains("75%")||pet.info().contains("%1$d")||pet.info().contains("%2$d"))throw new AssertionError("Hatchling hunger hint or description formatting");
            Dungeon.hero.belongings.backpack.items.add(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing());
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,pet));
        }else if(frames==330){interfaceBounds();capture("polish-hatchling-actions");playtestClick("FEED");}
        else if(frames==360){
            if(Boolean.getBoolean("grimhollow.interfacePortrait"))interfaceBounds();
            else {
                com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane)RecoveryChecks.field(Game.scene(),"inventory");
                if(!pane.isSelecting()||!pane.getSelector().textPrompt().contains("Feed"))throw new AssertionError("61: Feed inventory selection missing");
            }
            capture("polish-feed-picker");closeReviewWindows();GameScene.cancel();
            int cell=Dungeon.hero.pos;
            com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell-1,Terrain.ENTRANCE);
            com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(cell+1,Terrain.EXIT);
            GameScene.updateMap();Dungeon.observe();
            com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(cell).pour(
                    com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle.FACTORY,.15f);
            com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite.show(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite.FOOD);
        }else if(frames==385){capture("polish-stairs-meal-motes");}
        else if(frames==390){
            if(descriptionReviewFrames==0)longItemInputReview();
            if(!descriptionReviewTick()){frames--;return;}
        }
        else if(frames==410){closeReviewWindows();notificationReview();}
        else if(frames==418){capture("polish-painted-notifications");}
        else if(frames==440){targetPortraitReview();}
        else if(frames==460){
            capture("polish-target-portrait");
            System.out.println("TEST 61 UI PASS: home Update Log, history, Feed, painted notification/healing/spell icons and visible-body target portraits; failures=0");
        }else if(frames==480){
            closeReviewWindows();GameScene.cancel();
            Playtest.enable();
            Playtest.heroClass(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.ENCHANTER);
            com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor oldArmor=Dungeon.hero.belongings.armor;
            oldArmor.collect();
            Dungeon.hero.belongings.armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor();
            Dungeon.hero.belongings.armor.identify();
            Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).execute(Dungeon.hero,"ETCH");
        }else if(frames==510){
            if(Boolean.getBoolean("grimhollow.interfacePortrait"))interfaceBounds();
            capture("polish-armor-etch-picker");
            com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector selector=null;
            Group picker=null;
            for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(Game.scene()))
                if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndBag){
                    picker=(Group)g;
                    selector=((com.shatteredpixel.shatteredpixeldungeon.windows.WndBag)g).getSelector();
                }
            if(selector==null){
                com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane)RecoveryChecks.field(Game.scene(),"inventory");
                if(!pane.isSelecting())throw new AssertionError("37: inline Etch inventory selection missing");
                selector=pane.getSelector();picker=pane;
            }
            if(selector==null||!selector.itemSelectable(Dungeon.hero.belongings.armor)||selector.itemSelectable(Dungeon.hero.belongings.weapon))throw new AssertionError("37: Etch picker must offer armor, excluding its current weapon carrier");
            boolean clicked=false;
            for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(picker))
                if(g instanceof com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot&&((com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot)g).item()==Dungeon.hero.belongings.armor){
                    pointerGestureReview(g,Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);clicked=true;break;
                }
            if(!clicked)throw new AssertionError("37: armor slot missing from Etch picker");
        }else if(frames==540){
            if(Dungeon.hero.belongings.armor.runeEtching==null || ((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)Dungeon.hero.belongings.weapon).runeEtching==null || Dungeon.hero.belongings.armor.runeEtching==((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)Dungeon.hero.belongings.weapon).runeEtching)throw new AssertionError("37: Etch picker did not attach armor rune");
            String text=Dungeon.hero.belongings.armor.info();
            if(!text.contains("Rune Etching:")||!text.contains("travels with the rune")||text.contains("%1$s")||text.contains("%2$d"))throw new AssertionError("37: rendered armor description identity or placeholders");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(Dungeon.hero.belongings.armor));
        }else if(frames==570){
            interfaceBounds();capture("polish-armor-etch-description");
            System.out.println("TEST 37 UI PASS: actual Etch selector accepts armor, rejects current carrier, retains independent weapon rune, attaches armor glyph, and renders resolved description; failures=0");
            closeReviewWindows();
        }else if(frames==590){
            Object menu=RecoveryChecks.field(Game.scene(),"menu");
            pointerGestureReview((com.watabou.noosa.Gizmo)RecoveryChecks.field(menu,"btnMenu"),Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);
        }else if(frames==610){
            interfaceBounds();capture("journal-menu");closeReviewWindows();
            for(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark landmark:com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark.values())
                com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(landmark);
            com.shatteredpixel.shatteredpixeldungeon.Statistics.deepestFloor=Math.max(com.shatteredpixel.shatteredpixeldungeon.Statistics.deepestFloor,Dungeon.depth);
            com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal.last_index=0;
            Object menu=RecoveryChecks.field(Game.scene(),"menu");
            com.shatteredpixel.shatteredpixeldungeon.ui.MenuPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.MenuPane)menu;
            com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey key=new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey();key.depth=Dungeon.depth;
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(key);pane.updateKeys();
            pane.flashForPage(com.shatteredpixel.shatteredpixeldungeon.journal.Document.ADVENTURERS_GUIDE,com.shatteredpixel.shatteredpixeldungeon.journal.Document.GUIDE_SURPRISE_ATKS);
            pane.update();Object button=RecoveryChecks.field(menu,"btnJournal");
            Image book=(Image)RecoveryChecks.field(button,"journalIcon");
            if(!book.visible||book.am!=1||!((com.watabou.noosa.Gizmo)RecoveryChecks.field(button,"unread")).visible)throw new AssertionError("Unread journal must keep book opaque beside keys");
            hudKeysReview(pane);
            com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey gold=new com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey(Dungeon.depth);gold.quantity(3);
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(gold);
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(new com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey(Dungeon.depth));
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(new com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey(Dungeon.depth));pane.updateKeys();
            pointerGestureReview((com.watabou.noosa.Gizmo)RecoveryChecks.field(menu,"btnJournal"),Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);
            closeReviewWindows();
            if(((com.watabou.noosa.Gizmo)RecoveryChecks.field(button,"unread")).visible)throw new AssertionError("Journal acknowledgement did not clear unread badge");
            pointerGestureReview((com.watabou.noosa.Gizmo)RecoveryChecks.field(menu,"btnJournal"),Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);
        }else if(frames==630){
            interfaceBounds();capture("journal-notes");capture("collected-key-hud");closeReviewWindows();
            journalNoteReview(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark.CHASM_FLOOR);
        }else if(frames==640){
            interfaceBounds();capture("journal-note-chasm");closeReviewWindows();
            journalNoteReview(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark.WATER_FLOOR);
        }else if(frames==645){
            interfaceBounds();capture("journal-note-water");closeReviewWindows();
            journalNoteReview(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark.LIGHTNING_TREASURY);
        }else if(frames==650){
            interfaceBounds();checkReviewText(Game.scene());capture("journal-note-treasury");closeReviewWindows();
            for(String page:com.shatteredpixel.shatteredpixeldungeon.journal.Document.ALCHEMY_GUIDE.pageNames())com.shatteredpixel.shatteredpixeldungeon.journal.Document.ALCHEMY_GUIDE.findPage(page);
            com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal.last_index=2;
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal());
        }else if(frames==660){
            interfaceBounds();capture("journal-alchemy");closeReviewWindows();
            com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal.last_index=3;
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal());
        }else if(frames==670){
            interfaceBounds();capture("journal-catalog");
            for(int i=0;i<16;i++){
                Image icon=com.shatteredpixel.shatteredpixeldungeon.ui.JournalIcons.get(i);
                if(icon.width()!=16||icon.height()!=16||icon.texture.width!=256)throw new AssertionError("Journal logical/texture scale "+i);
                icon.destroy();
            }
            for(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark landmark:com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark.values()){
                Image icon=new com.shatteredpixel.shatteredpixeldungeon.journal.Notes.LandmarkRecord(landmark,Dungeon.depth).icon();
                if(icon.texture==com.watabou.gltextures.TextureCache.get(com.shatteredpixel.shatteredpixeldungeon.Assets.Interfaces.ICONS)
                        ||icon.width()>16.01f||icon.height()>16.01f
                        ||com.shatteredpixel.shatteredpixeldungeon.GameGeometry.opaqueHeight(icon.texture,icon.frame())<24)
                    throw new AssertionError("Journal landmark must use fitted painted art: "+landmark+" "+icon.width()+"x"+icon.height());
                icon.destroy();
            }
            System.out.println("JOURNAL UI PASS: real HUD input, all 26 painted landmarks, chasm/water/treasury descriptions, notes/alchemy/catalog bounds and navigation icon dimensions");
            closeReviewWindows();
        }else if(frames==690){
            com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll.initLabels();
            new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify().identify();
            new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation().identify();
            new com.shatteredpixel.shatteredpixeldungeon.items.BlankParchment().quantity(5).collect();
            Dungeon.energy=100;
            com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush brush=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class);
            brush.gainCharge(100);brush.advance(0);brush.execute(Dungeon.hero,"SCRIBE");
        }else if(frames==720){
            interfaceBounds();checkReviewText(Game.scene());capture("enchanter-scribe");
            boolean clicked=false;
            for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(Game.scene()))if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndScribe){
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(g,"pane");
                pointerGestureReview(RecoveryChecks.members(pane.content()).get(0),Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);clicked=true;break;
            }
            if(!clicked)throw new AssertionError("Scribe picker missing");
        }else if(frames==760){
            if(Dungeon.energy!=88||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.BlankParchment.class).quantity()!=4
                    ||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).charges()!=2
                    ||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify.class)==null)throw new AssertionError("Scribe actual pointer purchase/resources");
            capture("enchanter-scribe-complete");
            System.out.println("TEST 63 UI PASS: native Scribe menu, readable costs, scroll selector and actual mouse/touch purchase");
        }else if(frames==790){
            closeReviewWindows();
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])){
                com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);
                for(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff buff:mob.buffs())com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(buff);
                if(mob.sprite!=null)mob.sprite.killAndErase();
            }
            Dungeon.level.mobs.clear();Playtest.god(true);
            Level level=Dungeon.level;int center=Dungeon.hero.pos,w=level.width();
            level.elementalCaches.clear();
            for(int y=-4;y<=1;y++)for(int x=-4;x<=4;x++) {
                int cell=center+x+y*w;Level.set(cell,y==-3?Terrain.WALL:Terrain.EMPTY_SP);
                level.visited[cell]=y>-3;level.mapped[cell]=false;
            }
            for(int i=0;i<3;i++) {
                com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache c=new com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache();
                c.door=center+(i-1)*2-3*w;c.mechanism=c.door+w;c.keyCost=4+i;
                c.kind=com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.Kind.values()[i];
                level.elementalCaches.add(c);Level.set(c.door,Terrain.SECRET_DOOR);
                com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.MechanismTile tile=new com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache.MechanismTile();
                tile.pos(c.mechanism);level.customTiles.add(tile);GameScene.add(tile,false);
            }
            level.cleanWalls();GameScene.resetMap();Dungeon.observe();
        }else if(frames==820){
            capture("elemental-seals-concealed");
            try {
                int target=Dungeon.level.elementalCaches.get(0).door;
                java.lang.reflect.Method map=com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight.class.getDeclaredMethod("affectMap",com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.class);map.setAccessible(true);
                map.invoke(new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight(),new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,target,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.STOP_TARGET));
                if(Dungeon.level.map[target]!=Terrain.LOCKED_DOOR)throw new AssertionError("Prismatic light must expose seal");
                target=Dungeon.level.elementalCaches.get(2).door;
                com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight talisman=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight();talisman.playtestRecharge();
                java.lang.reflect.Field user=com.shatteredpixel.shatteredpixeldungeon.items.Item.class.getDeclaredField("curUser");user.setAccessible(true);user.set(null,Dungeon.hero);
                talisman.scry.onSelect(target);
                if(Dungeon.level.map[target]!=Terrain.LOCKED_DOOR)throw new AssertionError("Talisman must expose seal");
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
        }else if(frames==850){
            for(com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache c:Dungeon.level.elementalCaches)Level.set(c.door,Terrain.SECRET_DOOR);
            new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping().doRead();
            for(com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache c:Dungeon.level.elementalCaches)
                if(c.opened||Dungeon.level.map[c.door]!=Terrain.LOCKED_DOOR)throw new AssertionError("Mapping must reveal, never unlock, every seal");
        }else if(frames==880){
            capture("elemental-seals-revealed");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(Dungeon.level.elementalCaches.get(1).mechanism));
        }else if(frames==910){
            interfaceBounds();checkReviewText(Game.scene());capture("elemental-fountain-description");closeReviewWindows();
            com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache c=Dungeon.level.elementalCaches.get(1);
            Dungeon.hero.pos=c.mechanism+Dungeon.level.width();Dungeon.hero.sprite.place(Dungeon.hero.pos);Dungeon.observe();
            Camera.main.snapTo(Dungeon.hero.sprite.center().x,Dungeon.hero.sprite.center().y);
        }else if(frames==940){
            com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache c=Dungeon.level.elementalCaches.get(1);
            if(!Dungeon.hero.ready)throw new AssertionError("Prior discovery animation has not returned control");
            com.shatteredpixel.shatteredpixeldungeon.items.Waterskin skin=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class);skin.fill();skin.execute(Dungeon.hero,"POUR");
            pointerCell(c.mechanism);
        }else if(frames==980){
            if(!Dungeon.level.elementalCaches.get(1).opened||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class).isFull())throw new AssertionError("Actual Pour target did not open water seal");
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame().shatter(Dungeon.level.elementalCaches.get(0).mechanism);
            com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache rod=Dungeon.level.elementalCaches.get(2);
            for(int method=0;method<4;method++) {
                rod.opened=false;Level.set(rod.door,Terrain.LOCKED_DOOR);
                if(method==0)new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning().onZap(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(Dungeon.hero.pos,rod.mechanism,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.STOP_TARGET));
                if(method==1)try {
                    java.lang.reflect.Method activate=com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock.class.getDeclaredMethod("activate",int.class);activate.setAccessible(true);activate.invoke(new com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock(),rod.mechanism);
                }catch(ReflectiveOperationException e){throw new AssertionError(e);}
                if(method==2)new com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew().shatter(rod.mechanism);
                if(method==3)new com.shatteredpixel.shatteredpixeldungeon.items.bombs.FlashBangBomb().explode(rod.mechanism);
                if(!rod.opened)throw new AssertionError("Lightning key method "+method);
            }
        }else if(frames==1010){
            for(com.shatteredpixel.shatteredpixeldungeon.levels.features.ElementalCache c:Dungeon.level.elementalCaches)if(!c.opened||!Dungeon.level.passable[c.door])throw new AssertionError("Elemental effect failed to open seal");
            capture("elemental-seals-open");
            System.out.println("TEST 64 UI PASS: painted mechanisms, concealed/revealed/open states, fitted descriptions and actual Pour pointer targeting");
        }else if(frames==1030){
            closeReviewWindows();GameScene.cancel();Playtest.enable();
            for(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact a:Dungeon.hero.belongings.getAllItems(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.class))if(a.isEquipped(Dungeon.hero))a.doUnequip(Dungeon.hero,true,false);
            reviewCoin=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.FickleDoubloon();reviewCoin.identify();reviewCoin.playtestLevel(10);reviewCoin.collect();
            if(!reviewCoin.doEquip(Dungeon.hero))throw new AssertionError("Coin equip failed");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,reviewCoin));
        }else if(frames==1060){
            interfaceBounds();checkReviewText(Game.scene());capture("doubloon-actions");closeReviewWindows();
            com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();
        }else if(frames==1090){
            if(!playtestClickPage("Fickle Doubloon")){frames--;return;}
        }else if(frames==1120){
            interfaceBounds();checkReviewText(Game.scene());capture("doubloon-tuning");playtestClick("Heads chance at +0: 50%");
        }else if(frames==1150){
            interfaceBounds();checkReviewText(Game.scene());capture("doubloon-odds-setting");playtestInput("51","Apply");
            if(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.configured(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.COIN_HEADS)!=51)throw new AssertionError("Coin tuning input failed");
            closeReviewWindows();com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();
        }else if(frames==1180){
            if(!playtestClickPage("Golden Mimic companion")){frames--;return;}
        }else if(frames==1210){
            interfaceBounds();checkReviewText(Game.scene());capture("doubloon-companion-tuning");closeReviewWindows();
            com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.reset();
            com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.set(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.COIN_HEADS,70);
            reviewCoin.playtestRecharge();
            if(!reviewCoin.flip(Dungeon.hero)||reviewCoin.image!=com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.FICKLE_HEADS)throw new AssertionError("Painted Heads flip failed");
        }else if(frames==1240){
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.FickleDoubloon.Luck luck=Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.FickleDoubloon.Luck.class);
            if(luck==null||!luck.favor)throw new AssertionError("Live coin favor missing");
            com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon icon=new com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon(luck,true);icon.refresh(luck);
            if(icon.width()!=16||icon.height()!=16||icon.texture.height!=2240)throw new AssertionError("Luck painted icon geometry");icon.destroy();
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoBuff(luck));
        }else if(frames==1270){
            interfaceBounds();checkReviewText(Game.scene());capture("doubloon-favor");closeReviewWindows();
            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.FickleDoubloon.Luck.class);
            HatchlingMimic pet=Dungeon.hero.belongings.getItem(HatchlingMimic.class);
            while(!pet.hungry())pet.tick(Dungeon.hero);
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DoubloonFeeding.confirm(Dungeon.hero,pet,reviewCoin);
        }else if(frames==1300){
            interfaceBounds();checkReviewText(Game.scene());capture("doubloon-feeding-warning");playtestClick("FEED THE COIN");
        }else if(frames==1330){
            com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion harness=com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.carried();
            if(harness==null||harness.ally()==null)throw new AssertionError("Live golden companion missing");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(harness));
        }else if(frames==1360){
            interfaceBounds();checkReviewText(Game.scene());capture("doubloon-golden-companion");
            closeReviewWindows();
            com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion harness=com.shatteredpixel.shatteredpixeldungeon.items.trinkets.GoldenMimicCompanion.carried();
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,harness));
            interfaceBounds();playtestClick("DIRECT");
            int guard=-1;
            for(int offset:com.watabou.utils.PathFinder.NEIGHBOURS8){int cell=Dungeon.hero.pos+offset;if(Dungeon.level.passable[cell]&&Dungeon.level.heroFOV[cell]&&Actor.findChar(cell)==null){guard=cell;break;}}
            if(guard<0)throw new AssertionError("Golden Mimic guard fixture has no empty cell");
            pointerCell(guard);
            if((int)RecoveryChecks.field(harness.ally(),"defendingPos")!=guard)throw new AssertionError("Harness direction command failed");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob(harness.ally()));
            interfaceBounds();playtestClick(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob.class,"direct"));pointerCell(Dungeon.hero.pos);
            if((int)RecoveryChecks.field(harness.ally(),"defendingPos")!=-1)throw new AssertionError("Inspection follow command failed");
            com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.reset();
            System.out.println("DOUBLOON UI PASS: fitted painted actions, both tuning menus, odds input, Heads flip, repeated custom buff refresh, irreversible feeding confirmation and Golden Mimic transformation; failures=0");
        }else if(frames==1390){
            closeReviewWindows();GameScene.cancel();
            reviewChart=new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WaywardChart();reviewChart.level(3);reviewChart.identify();
            if(!reviewChart.collect())throw new AssertionError("Chart fixture inventory full");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(reviewChart));
        }else if(frames==1420){
            interfaceBounds();checkReviewText(Game.scene());capture("wayward-description");closeReviewWindows();
            com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();
        }else if(frames==1450){
            if(!playtestClickPage("Wayward Chart")){frames--;return;}
        }else if(frames==1480){
            interfaceBounds();checkReviewText(Game.scene());capture("wayward-tuning");playtestClick("Chart discovery at +0: 10%");
        }else if(frames==1510){
            playtestInput("0","Apply");
            if(BalanceTuning.configured(BalanceTuning.Key.CHART_CHANCE_0)!=0)throw new AssertionError("Chart tuning input");
            closeReviewWindows();WaywardJourney.reset();
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0]))Actor.remove(m);
            Dungeon.level.mobs.clear();WaywardJourney.mapping();
            if(WaywardJourney.caches().size()!=1)throw new AssertionError("Chart paid discovery");
            reviewCache=WaywardJourney.caches().get(0);int cell=reviewCache.pos;
            Dungeon.level.visited[cell]=Dungeon.level.mapped[cell]=Dungeon.level.heroFOV[cell]=false;
            GameScene.updateFog();Camera.main.snapTo((cell%Dungeon.level.width()+.5f)*16,(cell/Dungeon.level.width()+.5f)*16);
        }else if(frames==1540){
            if(Dungeon.level.visited[reviewCache.pos]||Dungeon.level.mapped[reviewCache.pos]||Dungeon.level.heroFOV[reviewCache.pos])throw new AssertionError("Chart X reveals terrain");
            capture("wayward-unexplored-marker");
            Dungeon.hero.pos=reviewCache.pos;Dungeon.hero.sprite.place(Dungeon.hero.pos);Dungeon.observe();
            Camera.main.snapTo(Dungeon.hero.sprite.center().x,Dungeon.hero.sprite.center().y);
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(Dungeon.level.heaps.get(reviewCache.pos)));
        }else if(frames==1570){
            interfaceBounds();checkReviewText(Game.scene());capture("wayward-mound-description");closeReviewWindows();
            // Existing room exploration is the real substrate for a claim's memory cost.
            java.util.Arrays.fill(Dungeon.level.visited,true);WaywardJourney.captureRooms();
            com.shatteredpixel.shatteredpixeldungeon.items.Heap heap=Dungeon.level.heaps.get(reviewCache.pos);
            if(heap.sprite.frame().width()*heap.sprite.texture.width!=64)throw new AssertionError("Chart mound texture size");
            if(!Dungeon.hero.ready){frames--;return;}pointerCell(reviewCache.pos);
        }else if(frames==1610){
            if(!reviewCache.claimed||reviewCache.state()!=1)throw new AssertionError("Actual pointer pickup did not claim painted cache");
            capture("wayward-mound-partial");
            com.shatteredpixel.shatteredpixeldungeon.items.Heap heap=Dungeon.level.heaps.get(reviewCache.pos);
            while(!heap.isEmpty())heap.pickUp();
            for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap tile:Dungeon.level.customTiles)
                if(tile instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.WaywardMoundTile)tile.updateKnowledge();
        }else if(frames==1640){
            if(reviewCache.state()!=2)throw new AssertionError("Exhausted mound state");capture("wayward-mound-exhausted");
            WaywardJourney.Memory memory=WaywardJourney.memories().stream().filter(m->m.forgotten).findFirst().orElseThrow(()->new AssertionError("No claim memory veil"));
            java.util.Arrays.fill(Dungeon.level.heroFOV,false);GameScene.updateFog();
            int cell=memory.cells[memory.cells.length/2];Camera.main.snapTo((cell%Dungeon.level.width()+.5f)*16,(cell/Dungeon.level.width()+.5f)*16);
        }else if(frames==1670){
            if(!java.util.stream.IntStream.range(0,Dungeon.level.length()).anyMatch(i->WaywardJourney.veilCells()[i]))throw new AssertionError("Remembered terrain veil missing");
            capture("wayward-forgotten-room");WaywardJourney.mapping();
        }else if(frames==1700){
            if(WaywardJourney.memories().stream().anyMatch(m->m.forgotten))throw new AssertionError("Mapping did not restore native veil");
            capture("wayward-restored-room");BalanceTuning.reset();
            System.out.println("WAYWARD UI PASS: painted Chart and mound states, no terrain-revealing marker, native mouse/touch claim, accumulated memory layer, mapping restore and tuning input; failures=0");Gdx.app.exit();
        }
    }
    private com.shatteredpixel.shatteredpixeldungeon.items.artifacts.FickleDoubloon reviewCoin;
    private com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WaywardChart reviewChart;
    private WaywardJourney.Cache reviewCache;
    private void notificationReview(){
        com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite spell=new com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite();
        for(int i=0;i<8;i++){
            spell.reset(i);
            if(spell.texture.width!=512||spell.width()!=16||spell.height()!=16)throw new AssertionError("Painted spell geometry "+i);
        }
        spell.destroy();
        com.shatteredpixel.shatteredpixeldungeon.effects.Speck heart=new com.shatteredpixel.shatteredpixeldungeon.effects.Speck();
        heart.reset(0,0,0,com.shatteredpixel.shatteredpixeldungeon.effects.Speck.HEALING);
        if(heart.rm<=heart.gm*2||heart.rm<=heart.bm*2)throw new AssertionError("Healing hearts are not red");heart.destroy();
        int[] indices={0,1,2,3,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,23,24,36,37,38,39,40,41,42,43,44,45,46,47,54,55,56,57,58,59,60,61,62,63,64,65,72,73,74,75,76,77,78,79,80,81,82};
        for(int i=0;i<indices.length;i++){
            com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText text=new com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText();
            text.reset(30+(i%8)*25,50+(i/8)*13,"+1",0xFFFFFF,indices[i],true);
            Image icon=(Image)RecoveryChecks.field(text,"icon");
            if(icon.texture.width!=1008||icon.width()!=7||icon.height()!=8)throw new AssertionError("Painted floating icon geometry "+indices[i]);
            text.camera=com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera;Game.scene().add(text);
        }
        Dungeon.hero.sprite.emitter().burst(com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(com.shatteredpixel.shatteredpixeldungeon.effects.Speck.HEALING),8);
    }
    private void targetPortraitReview(){
        com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator indicator=(com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator)RecoveryChecks.field(Game.scene(),"attack");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[] targets={new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.RedShaman(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo()};
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:targets){
            mob.pos=Dungeon.hero.pos+1;
            com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator.target(mob);
            indicator.setRect(100,50,24,24);
            for(boolean flip:new boolean[]{false,true}){
                indicator.flip(flip);
                com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite=(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite)RecoveryChecks.field(indicator,"sprite");
                com.watabou.utils.RectF body=sprite.visibleBounds();
                if(Math.abs(Math.max(body.width(),body.height())-20)>.05f||body.left<99.9f||body.right>124.1f||body.top<49.9f||body.bottom>74.1f)throw new AssertionError("Target portrait fit "+mob.getClass()+" "+body);
            }
        }
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
        rat.pos=Dungeon.hero.pos+1;Level.set(rat.pos,Terrain.EMPTY);GameScene.add(rat);Dungeon.observe();Dungeon.hero.checkVisibleMobs();
        com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator.updateState();
        com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator.target(rat);
        if(!((Image)RecoveryChecks.field(indicator,"sprite")).visible)throw new AssertionError("Attack portrait is hidden");
    }
    private int inspectPosition,inspectGold,inspectCharges;
    private float inspectTime;
    private com.shatteredpixel.shatteredpixeldungeon.items.Heap inspectLoot,inspectShop;
    private com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern reviewLantern;
    private com.shatteredpixel.shatteredpixeldungeon.items.Item reviewFuel;
    private void readabilityReview(){
        com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar toolbar=(com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar)RecoveryChecks.field(Game.scene(),"toolbar");
        Object search=RecoveryChecks.field(toolbar,"btnSearch");
        if(frames==700){
            closeReviewWindows();int w=Dungeon.level.width();
            for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])){
                com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);if(mob.sprite!=null)mob.sprite.killAndErase();
            }
            Dungeon.level.mobs.clear();
            inspectPosition=Dungeon.hero.pos;
            // Controlled visible fixture. The five generated-region fog checks remain separate.
            for(int y=-3;y<=3;y++)for(int x=-4;x<=4;x++){
                int cell=inspectPosition+y*w+x;if(!Dungeon.level.insideMap(cell))continue;
                Level.set(cell,Math.abs(x)==4||Math.abs(y)==3?Terrain.WALL:Terrain.EMPTY);
                Dungeon.level.heroFOV[cell]=Dungeon.level.visited[cell]=true;
                Dungeon.level.traps.remove(cell);
            }
            Level.set(inspectPosition-2*w,Terrain.LOCKED_DOOR);
            Level.set(inspectPosition-2*w+2,Terrain.DOOR);
            Level.set(inspectPosition-w+1,Terrain.LOCKED_DOOR);
            Level.set(inspectPosition-w-3,Terrain.CRYSTAL_DOOR);
            inspectLoot=Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(),inspectPosition-2);
            inspectShop=Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing(),inspectPosition+2);
            inspectShop.type=com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.FOR_SALE;
            inspectLoot.seen=inspectShop.seen=true;
            ToxicGas gas=new ToxicGas();
            for(int i=0;i<4;i++)gas.seed(Dungeon.level,inspectPosition+w+(i-2),70);
            GameScene.add(gas);
            SacrificialFire altar=new SacrificialFire();altar.seed(Dungeon.level,inspectPosition-w-2,10);GameScene.add(altar);
            ((Group)RecoveryChecks.field(Game.scene(),"levelVisuals")).add(new com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel.Torch(inspectPosition-w+2));
            GameScene.updateMap();
            inspectGold=Dungeon.gold;inspectTime=Dungeon.hero.cooldown();
            inspectCharges=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class).charges();
            clickReview(search);clickReview(search);
            if(!com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar.examineLocked())throw new AssertionError("Second Examine press did not latch");
        }
        if(frames==730){
            com.shatteredpixel.shatteredpixeldungeon.ui.Toast prompt=(com.shatteredpixel.shatteredpixeldungeon.ui.Toast)RecoveryChecks.field(Game.scene(),"prompt");
            if(prompt.left()<0||prompt.right()>prompt.camera().width||prompt.top()<0||prompt.bottom()>prompt.camera().height)
                throw new AssertionError("Examine prompt outside screen");
            capture("readability-room");GameScene.handleCell(inspectLoot.pos);
        }
        if(frames==750){assertInspection();capture("examine-loot");closeReviewWindows();}
        if(frames==775)GameScene.handleCell(inspectShop.pos);
        if(frames==795){assertInspection();capture("examine-shop");closeReviewWindows();}
        if(frames==820)GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBag(Dungeon.hero.belongings.backpack));
        if(frames==840)clickInventoryItem(Game.scene(),Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class));
        if(frames==855){assertInspection();capture("examine-inventory");
            for(com.watabou.noosa.Gizmo g:new java.util.ArrayList<>(RecoveryChecks.members(Game.scene())))
                if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem)((com.shatteredpixel.shatteredpixeldungeon.ui.Window)g).hide();
        }
        if(frames==875)clickInventoryItem(Game.scene(),Dungeon.hero.belongings.weapon());
        if(frames==890){assertInspection();closeReviewWindows();
            if(Dungeon.hero.pos!=inspectPosition||Dungeon.gold!=inspectGold||Dungeon.hero.cooldown()!=inspectTime
                    ||Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class).charges()!=inspectCharges)
                throw new AssertionError("Repeated inspection changed gameplay state");
            if(!inspectLoot.sprite.groundOutline()||!inspectShop.sprite.groundOutline())throw new AssertionError("Ground contrast missing");
            inspectLoot.hidden=true;if(inspectLoot.sprite.groundOutline())throw new AssertionError("Hidden loot highlighted");inspectLoot.hidden=false;
            clickReview(search);if(com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar.examineLocked())throw new AssertionError("Third press did not unlatch");
            clickReview(search);clickReview(search);GameScene.cancel();
            if(com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar.examineLocked())throw new AssertionError("Back/Escape did not unlatch");
            Level.set(inspectPosition-2*Dungeon.level.width(),Terrain.OPEN_DOOR);GameScene.updateMap(inspectPosition-2*Dungeon.level.width());
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndBadge(Badges.Badge.HIGH_SCORE_5,true));
        }
        if(frames==915){interfaceBounds();capture("painted-badge");closeReviewWindows();}
        if(frames==940){
            for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(Game.scene()))if(g instanceof com.shatteredpixel.shatteredpixeldungeon.effects.ReadabilityEffects.Locks){
                Image[] locks=(Image[])RecoveryChecks.field(g,"locks");
                if(locks[inspectPosition-2*Dungeon.level.width()].visible)throw new AssertionError("Opened door retained lock");
            }
            int badges=0;
            for(Badges.Badge badge:Badges.Badge.values())if(badge.image>=0){
                Image art=com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner.image(badge.image);
                if(art.width()!=16||art.height()!=16||Math.round(art.frame().width()*art.texture.width)!=64)
                    throw new AssertionError("Badge density/geometry "+badge);art.destroy();badges++;
            }
            System.out.println("TEST 36 READABILITY: locked Examine loot/shop/two inventory targets; toggle and Back exit; no turns, gold, charges or movement; hidden loot excluded; painted badges="+badges+" failures=0");
        }
        if(frames==960){
            reviewLantern=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern();
            reviewLantern.identify();Dungeon.hero.belongings.artifact=reviewLantern;reviewLantern.activate(Dungeon.hero);
            com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();reviewLantern.storeInBundle(state);state.put("charge",2);reviewLantern.restoreFromBundle(state);
            reviewFuel=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame().identify();
            Dungeon.hero.belongings.backpack.items.add(reviewFuel);
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,reviewLantern));
        }
        if(frames==985){capture("ashlight-open");interfaceBounds();checkReviewText(Game.scene());scrollReview(Game.scene());}
        if(frames==1010){capture("ashlight-riders");inspectTime=Dungeon.hero.cooldown();inspectCharges=reviewLantern.charges();clickReviewLabel("SHUTTER");}
        if(frames==1030){
            if(!reviewLantern.shuttered()||Dungeon.hero.cooldown()!=inspectTime||reviewLantern.charges()!=inspectCharges)
                throw new AssertionError("51 native shutter consumed time or charge");
            if(reviewLantern.image()!=com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.ASHLIGHT_CLOSED)
                throw new AssertionError("25 wrong shuttered lantern icon");
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,reviewLantern));
        }
        if(frames==1050){interfaceBounds();capture("ashlight-shuttered");clickReviewLabel("UNSHUTTER");}
        if(frames==1070)GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,reviewLantern));
        if(frames==1090)clickReviewLabel("FEED");
        if(frames==1110){
            if(GameScene.showingWindow())interfaceBounds();
            else if(!((com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane)RecoveryChecks.field(Game.scene(),"inventory")).isSelecting())
                throw new AssertionError("51 ingredient selector missing");
            capture("ashlight-feed");
            if(!clickInventoryItem(Game.scene(),reviewFuel))throw new AssertionError("51 fire ingredient not selectable");}
        if(frames==1140){
            if(reviewLantern.level()!=1)throw new AssertionError("51 native feeding did not level artifact");
            closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,reviewLantern));
        }
        if(frames==1160){inspectCharges=reviewLantern.charges();clickReviewLabel("FLARE");}
        if(frames==1170)capture("ashlight-flare");
        if(frames==1190){
            if(reviewLantern.charges()!=inspectCharges-1)throw new AssertionError("51 native Flare did not spend one charge");
            System.out.println("TEST 51 UI: open/shuttered painted icons; scrollable lore/riders; free menu toggle; ingredient selection/feeding; Flare; failures=0");
            SPDSettings.dynamicLighting(originalLighting);SPDSettings.zoom(originalZoom);
        }
    }
    private int playtestStep=-13,playtestWait;
    private void playtestTick(){
        if(playtestStep>=32){tabletTick();return;}
        if(++playtestWait>12000)throw new AssertionError("Playtest menu scenario stalled at "+playtestStep);
        if(Game.scene() instanceof InterlevelScene){
            Object button=RecoveryChecks.field(Game.scene(),"btnContinue");
            if(button instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton && ((com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)button).active)pointerClickReview(button);
            return;
        }
        if(frames%20!=0)return;
        if(playtestStep>=0 && !(Game.scene() instanceof GameScene))return;
        switch(playtestStep){
            case -13:
                if(!Dungeon.hero.ready)return;
                closeReviewWindows();GamesInProgress.curSlot=1;
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGame());
                if(playtestButton("Playtest")!=null||playtestButton("Playtest (active)")!=null)throw new AssertionError("Playtest entry remains in active-run menu");
                capture("playtest-run-menu");break;
            case -12:playtestClick("Main Menu");break;
            case -11:
                if(!(Game.scene() instanceof TitleScene))return;
                RecoveryChecks.titleControls();capture("playtest-home");
                Dungeon.hero=null;Dungeon.level=null; // Also exercise the cold-launch case.
                pointerClickReview(RecoveryChecks.field(Game.scene(),"btnPlaytest"));break;
            case -10:interfaceBounds();capture("playtest-home-menu");playtestClick("Start a new run for testing");break;
            case -9:
                if(!(Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene))return;
                if(GamesInProgress.curSlot!=2)throw new AssertionError("Home new-run action reused an occupied slot");
                pointerClickReview(RecoveryChecks.field(Game.scene(),"btnExit"));break;
            case -8:
                if(!(Game.scene() instanceof TitleScene))return;
                try {
                    java.lang.reflect.Field pending=com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.class.getDeclaredField("requestedSlot");pending.setAccessible(true);
                    if(pending.getInt(null)!=-1)throw new AssertionError("Cancelled new-run request leaked");
                }catch(ReflectiveOperationException e){throw new AssertionError(e);}
                pointerClickReview(RecoveryChecks.field(Game.scene(),"btnPlaytest"));break;
            case -7:playtestClick("Balance tuning");break;
            case -6:interfaceBounds();playtestClick("Encounters");break;
            case -5:playtestClick("Enemy population: 100%");break;
            case -4:interfaceBounds();playtestInput("125","Apply");break;
            case -3:
                BalanceTuning.loadShared();
                if(BalanceTuning.configured(BalanceTuning.Key.DENSITY)!=125||Dungeon.hero!=null||Playtest.enabled())throw new AssertionError("Home tuning required or mutated a live hero");
                interfaceBounds();capture("playtest-home-balance");BalanceTuning.reset();closeReviewWindows();
                com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.openHome();break;
            case -2:if(!playtestClickPage("Open tools for a saved run"))return;break;
            case -1:
                interfaceBounds();capture("playtest-home-saves");
                GamesInProgress.Info saved=GamesInProgress.check(1);
                playtestClick("Slot 1: "+com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(saved.heroClass.title())+" | Level "+saved.level+" | Floor "+saved.depth);break;
            case 0:
                if(!Dungeon.hero.ready)return;
                if(GamesInProgress.curSlot!=1||Playtest.enabled()||playtestButton("Enable Playtest for this save")==null)throw new AssertionError("Selected save did not load with its test controls and original flags");
                System.out.println("TEST 52 HOME PASS: home button, cold-launch tuning, saved-run selection, new-run slot safety/cancel and no Playtest entry in ordinary saves");
                playtestStep=1;break;
            case 2:interfaceBounds();capture("playtest-enable");playtestClick("Enable Playtest for this save");break;
            case 3:
                if(!Playtest.enabled())throw new AssertionError("Playtest enable pointer failed");
                closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGame());
                interfaceBounds();capture("playtest-enabled-run-menu");playtestClick("Playtest");
                playtestClick("God mode: OFF");break;
            case 4:if(!Playtest.god())throw new AssertionError("God toggle failed");interfaceBounds();capture("playtest-menu");playtestClick("Create items");break;
            case 5:interfaceBounds();playtestClick("Search all items");break;
            case 6:playtestInput("Ashlight","Search");break;
            case 7:interfaceBounds();capture("playtest-search");playtestClick("Ashlight Lantern");break;
            case 8:playtestClick("Upgrade level: 0");break;
            case 9:playtestInput("10","Apply");break;
            case 10:interfaceBounds();capture("playtest-create");playtestClick("Create 1 (single item)");break;
            case 11:
                if(Dungeon.hero.belongings.getAllItems(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern.class).stream().noneMatch(i->i.level()==10))throw new AssertionError("Native +10 lantern creation failed");
                closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
            case 12:if(!playtestClickPage("Hero, class and progression"))return;break;
            case 13:playtestClick("Set hero level");break;
            case 14:playtestInput("24","Apply");break;
            case 15:if(Dungeon.hero.lvl!=24)throw new AssertionError("Native level input failed");playtestClick("Choose subclass");break;
            case 16:playtestClick("Seer");break;
            case 17:
                if(Dungeon.hero.subClass!=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.SEER)throw new AssertionError("Native subclass/load failed");
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGame());
                interfaceBounds();playtestClick("Playtest");break;
            case 18:if(!playtestClickPage("Hero, class and progression"))return;break;
            case 19:if(!playtestClickPage("Choose armor ability / grant class armor"))return;break;
            case 20:playtestClick(Dungeon.hero.heroClass.armorAbilities()[0].name());break;
            case 21:
                if(!(Dungeon.hero.belongings.armor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor))throw new AssertionError("Native class armor failed");
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
            case 22:if(!playtestClickPage("Travel to any floor / quest branch"))return;break;
            case 23:interfaceBounds();if(!playtestClickPage("Floor 21 - Halls"))return;break;
            case 24:
                if(Dungeon.depth!=21||Dungeon.hero.lvl!=24||!Playtest.god())throw new AssertionError("Native travel/load lost hero or flags");
                capture("playtest-halls");GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGame());
                interfaceBounds();playtestClick("Playtest");break;
            case 25:if(!playtestClickPage("Hero, class and progression"))return;break;
            case 26:playtestClick("Change class and starter kit");break;
            case 27:interfaceBounds();capture("playtest-classes");if(!playtestClickPage("Enchanter"))return;break;
            case 28:
                if(Dungeon.hero.heroClass!=HeroClass.ENCHANTER||Dungeon.hero.lvl!=24||Dungeon.depth!=21||!(Dungeon.hero.belongings.artifact instanceof com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush))throw new AssertionError("Native class switch/kit failed");
                capture("playtest-enchanter-halls");GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
            case 29:if(!playtestClickPage("God mode: ON"))return;break;
            case 30:
                if(!Playtest.enabled()||Playtest.god())throw new AssertionError("Native toggle did not preserve save marker");
                closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGameInProgress(GamesInProgress.curSlot));break;
            case 31:interfaceBounds();capture("playtest-save");
                System.out.println("TEST 52 UI: real pointer enable/god/search/+10 artifact/hero level/subclass/armor/class switch/floor-21 travel and save marker; landscape or portrait bounds; failures=0");
                break;
        }
        playtestStep++;
    }
    private com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat hurlTarget;
    private int hurlStart,hurlHero,hurlCharge;
    private String firstRankText;
    private void tabletTick(){
        if(!(Game.scene() instanceof GameScene)||frames%20!=0)return;
        try {
            switch(playtestStep){
                case 32:
                    closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo(HeroClass.PSYCHIC));
                    reviewHandbook().select(3);break;
                case 33:{
                    com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane page=handbookPage(3);
                    com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton talent=rankButton(page.content(),com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.KINETIC_SURGE);
                    page.scrollTo(0,Math.max(0,talent.bottom()-page.height()+4));
                    pointerGestureReview(talent,com.watabou.input.PointerEvent.NONE,-20);
                    if(talentWindow()!=null)throw new AssertionError("Talent drag opened or purchased a rank");
                    page.scrollTo(0,Math.max(0,talent.bottom()-page.height()+4));
                    pointerGestureReview(talent,com.watabou.input.PointerEvent.NONE,0);break;
                }
                case 34:
                    if(talentWindow()==null)throw new AssertionError("Handbook talent pointer did not open details");
                    interfaceBounds();firstRankText=allReviewText(talentWindow());
                    for(int rank=1;rank<=4;rank++){
                        String detail=com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.class,"kinetic_surge.rank"+rank);
                        if(detail.length()<40||!firstRankText.contains(detail)||!firstRankText.contains("Rank "+rank))throw new AssertionError("Rank "+rank+" thematic progression missing from description");
                        if(rank>1&&detail.equals(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.class,"kinetic_surge.rank"+(rank-1))))throw new AssertionError("Indistinguishable rank descriptions");
                    }
                    if(firstRankText.contains("12.5%")||firstRankText.contains("50%"))throw new AssertionError("Hidden formula still exposed in talent text");
                    com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane progression=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(talentWindow(),"description");
                    pointerGestureReview(progression,com.watabou.input.PointerEvent.NONE,-25);
                    if(progression.content().height()>progression.height() && progression.content().camera.scroll.y<=0)throw new AssertionError("Overflowing progression does not scroll");
                    if(playtestButton("+1")!=null||playtestButton("+4")!=null)throw new AssertionError("Rank toggle buttons still present");
                    capture("tablet-talent-rank1");break;
                case 35:
                    interfaceBounds();String fourth=allReviewText(talentWindow());
                    if(!fourth.equals(firstRankText)||!fourth.contains("Rank 4"))throw new AssertionError("Scrolling changed progression content");
                    capture("tablet-talent-rank4");closeReviewWindows();
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage(
                            com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.INFO),
                            "Long description",String.join("\n\n",java.util.Collections.nCopies(24,"A long description must remain readable at the bottom of this window."))+"\n\nEND OF DESCRIPTION"));break;
                case 36:{
                    interfaceBounds();com.shatteredpixel.shatteredpixeldungeon.ui.Window window=null;
                    for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage)window=(com.shatteredpixel.shatteredpixeldungeon.ui.Window)child;
                    com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(window,"description");
                    pointerGestureReview(pane,com.watabou.input.PointerEvent.NONE,-25);
                    if(pane.content().camera.scroll.y<=0)throw new AssertionError("Long-description touch drag did not scroll");
                    pane.scrollTo(0,100000);float bottom=pane.content().camera.scroll.y+pane.height();
                    if(Math.abs(bottom-pane.content().height())>.1)throw new AssertionError("Description bottom inaccessible");
                    capture("tablet-long-description");closeReviewWindows();break;
                }
                case 37:
                    Playtest.heroClass(HeroClass.PSYCHIC);Playtest.subclass(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.SEER);
                    for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])){
                        com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(mob);if(mob.sprite!=null)mob.sprite.killAndErase();
                    }
                    Dungeon.level.mobs.clear();Dungeon.level.traps.clear();Dungeon.level.heaps.clear();
                    int w=Dungeon.level.width();hurlHero=w*(Dungeon.level.height()/2)+w/2;
                    for(int y=-3;y<=3;y++)for(int x=-4;x<=4;x++)Level.set(hurlHero+x+y*w,Terrain.EMPTY);
                    Playtest.teleport(hurlHero);Dungeon.hero.viewDistance=8;
                    hurlTarget=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();hurlTarget.pos=hurlStart=hurlHero-w-2;hurlTarget.HP=hurlTarget.HT=500;
                    hurlTarget.state=hurlTarget.PASSIVE;Dungeon.level.mobs.add(hurlTarget);GameScene.add(hurlTarget);
                    com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(hurlTarget,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis.class,100);
                    Dungeon.observe();GameScene.updateMap();Camera.main.edgeScroll.set(0);Camera.main.snapTo(Dungeon.hero.sprite.center());
                    com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal crystal=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class);
                    crystal.level(0);crystal.gainCharge(20);hurlCharge=crystal.charges();crystal.execute(Dungeon.hero,"CAST");break;
                case 38:playtestClick(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class,"hurl"));break;
                case 39:pointerCell(hurlStart);break;
                case 40:
                    com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector selector=(com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector)RecoveryChecks.field(Game.scene(),"cellSelector");
                    if(!selector.listener.prompt().equals(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class,"direction")))throw new AssertionError("Hurl direction selector was cleared after selecting enemy");
                    pointerCell(hurlStart+1);break;
                case 41:
                    if(hurlTarget.pos!=hurlStart+4||Dungeon.hero.pos!=hurlHero)throw new AssertionError("Hurl input moved hero or failed to throw enemy four cells: "+hurlTarget.pos+" expected "+(hurlStart+4));
                    if(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal.class).charges()!=hurlCharge-1)throw new AssertionError("Hurl charge count");
                    capture("tablet-hurl");tabletTerrainChecks();
                    com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.upgrade(Dungeon.hero);break;
                case 42:capture("tablet-upgrade");
                    System.out.println("TEST 53 UI PASS: real touch handbook talent/drag/all ranks in one scrolling description, long-description scroll/bounds, Hurl enemy/direction/charge, unknown-source overhangs and barricade presentation; failures=0");
                    Playtest.heroClass(HeroClass.ENCHANTER);Playtest.subclass(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.ARTIFICER);
                    Dungeon.hero.lvl=24;Dungeon.hero.talents.get(2).put(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.MASTER_CRAFT,2);
                    com.shatteredpixel.shatteredpixeldungeon.windows.WndHero.lastIdx=1;
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndHero());break;
                case 43:
                    pointerGestureReview(rankButton(Game.scene(),com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.MASTER_CRAFT),Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);break;
                case 44:
                    int dialogs=0;for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent)dialogs++;
                    if(dialogs!=1)throw new AssertionError("One talent tap opened "+dialogs+" upgrade windows");
                    capture("enchanter-upgrade-offer");
                    playtestClick(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent.class,"upgrade")));break;
                case 45:
                    com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent capped=com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.MASTER_CRAFT;
                    if(Dungeon.hero.pointsInTalent(capped)!=3||talentWindow()!=null)throw new AssertionError("Upgrade failed or duplicate popup remains: rank="+Dungeon.hero.pointsInTalent(capped)+", popup="+(talentWindow()!=null)+", available="+Dungeon.hero.talentPointsAvailable(3));
                    int unspent=Dungeon.hero.talentPointsAvailable(3);
                    com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton button=rankButton(Game.scene(),capped);button.upgradeTalent();button.upgradeTalent();
                    if(Dungeon.hero.pointsInTalent(capped)!=3||Dungeon.hero.talentPointsAvailable(3)!=unspent)throw new AssertionError("Repeated upgrade exceeded rank cap");
                    capture("enchanter-rank-cap");System.out.println("TEST 54 UI PASS: one pointer tap opens one offer; rank 2 to 3 spends one point; repeated upgrades cannot exceed cap; failures=0");
                    closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndSupportPrompt("Test diagnostic: reproducible issue text"));break;
                case 46:
                    interfaceBounds();capture("copyable-issue-report");playtestClick("Copy report");break;
                case 47:
                    if(!Gdx.app.getClipboard().getContents().contains("Test diagnostic: reproducible issue text"))throw new AssertionError("Issue report clipboard lost diagnostic text");
                    System.out.println("ISSUE REPORT PASS: visible diagnostic, viewport bounds and real clipboard copy");
                    closeReviewWindows();
                    for(int dy=-3;dy<=3;dy++)for(int dx=-4;dx<=4;dx++)Level.set(Dungeon.hero.pos+dx+dy*Dungeon.level.width(),Terrain.EMPTY);
                    for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs.toArray(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0])){mob.destroy();if(mob.sprite!=null)mob.sprite.killAndErase();}
                    Playtest.spawnMob(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute.class,Dungeon.hero.pos-2);
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper merchant=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper();
                    merchant.pos=Dungeon.hero.pos-3-Dungeon.level.width();GameScene.add(merchant);
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton.raise(Dungeon.hero.pos+3-Dungeon.level.width(),false,false);
                    com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(Dungeon.hero.pos-Dungeon.level.width()-1).pour(com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle.GENERAL,.10f);
                    com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(Dungeon.hero.pos-2*Dungeon.level.width()).pour(com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle.CURSE,.1f);
                    GameScene.add(Blob.seed(Dungeon.hero.pos+1+Dungeon.level.width(),80,com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity.class));
                    Playtest.spawnMob(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.RedShaman.class,Dungeon.hero.pos+2);
                    com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(Dungeon.hero.pos-Dungeon.level.width()).pour(com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.TENGU_SMOKE,.15f);
                    com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get(Dungeon.hero.pos+Dungeon.level.width()).pour(com.shatteredpixel.shatteredpixeldungeon.effects.EnhancedEffects.TENGU_SPARK,.15f);
                    Dungeon.observe();GameScene.updateMap();Camera.main.snapTo(Dungeon.hero.sprite.center());break;
                case 48:
                    Dungeon.hero.sprite.parent.add(new com.shatteredpixel.shatteredpixeldungeon.effects.Lightning(Dungeon.hero.pos-1+Dungeon.level.width(),Dungeon.hero.pos+1+Dungeon.level.width(),null,true));
                    Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);Game.scene().draw();capture("creatures-and-tengu-effects");
                    break;
                case 49:
                    com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic hatchling=
                            new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic();
                    hatchling.level(3);hatchling.collect();
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(hatchling));break;
                case 50:
                    interfaceBounds();checkReviewText(Game.scene());capture("hatchling-description");
                    scrollReview(Game.scene());break;
                case 51:
                    interfaceBounds();capture("hatchling-description-bottom");closeReviewWindows();
                    int center=Dungeon.hero.pos,width=Dungeon.level.width();
                    for(int i=0;i<3;i++){
                        int cell=center-1+i+width;
                        com.shatteredpixel.shatteredpixeldungeon.items.Heap heap=Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.Gold(20),cell);
                        heap.type=i==0?com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.CHEST:i==1?com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.SKELETON:com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.HEAP;
                        heap.sprite.link();GameScene.add(Blob.seed(cell,80,ToxicGas.class));
                    }
                    Dungeon.observe();GameScene.updateMap();
                    hatchlingHiddenCell=center+3+2*width;
                    Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(Dungeon.depth),hatchlingHiddenCell);
                    // Item Sense now selects one undiscovered heap; dropping into current FOV marks it seen.
                    Dungeon.level.heaps.get(hatchlingHiddenCell).seen=false;
                    Dungeon.level.heroFOV[hatchlingHiddenCell]=Dungeon.level.visited[hatchlingHiddenCell]=Dungeon.level.mapped[hatchlingHiddenCell]=false;
                    GameScene.updateFog();
                    hatchlingFov=Dungeon.level.heroFOV.clone();hatchlingVisited=Dungeon.level.visited.clone();hatchlingMapped=Dungeon.level.mapped.clone();
                    com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(Dungeon.hero,
                            com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.ItemSense.class).refresh(3,
                            com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic.Tier.EXCEPTIONAL);break;
                case 52:
                    if(!java.util.Arrays.equals(hatchlingFov,Dungeon.level.heroFOV)||!java.util.Arrays.equals(hatchlingVisited,Dungeon.level.visited)||!java.util.Arrays.equals(hatchlingMapped,Dungeon.level.mapped))throw new AssertionError("Hatchling sensing revealed terrain");
                    boolean marker=false;
                    for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.effects.HatchlingSenseLayer)
                        marker=((java.util.Map<?,?>)RecoveryChecks.field(child,"markers")).containsKey(hatchlingHiddenCell);
                    if(!marker)throw new AssertionError("Unseen sensed item has no marker");
                    capture("hatchling-sense-and-gas-loot");
                    System.out.println("TEST 55 UI PASS: painted Hatchling, scrolling hunger/benefits, object-only fog markers, gas loot overlay; failures=0");
                    Playtest.heroClass(HeroClass.ENCHANTER);Playtest.subclass(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.SCRIVENER);
                    Dungeon.hero.talents.get(0).put(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.FIELD_REPAIR,2);
                    Playtest.recharge();Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class).execute(Dungeon.hero,"CAST");break;
                case 53:
                    interfaceBounds();checkReviewText(Game.scene());
                    if(playtestButton("Defensive Sigil (1)")==null)throw new AssertionError("56: missing Defensive Sigil action");
                    capture("defensive-sigil-menu");playtestClick("Defensive Sigil (1)");break;
                case 54:
                    com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DefensiveSigil sigil=Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DefensiveSigil.class);
                    if(sigil==null||sigil.shielding()!=10)throw new AssertionError("56: menu did not cast rank-two shield");
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoBuff(sigil));break;
                case 55:
                    interfaceBounds();checkReviewText(Game.scene());capture("defensive-sigil-active");closeReviewWindows();
                    Playtest.heroClass(HeroClass.NECROMANCER);Playtest.recharge();
                    Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Phylactery.class).execute(Dungeon.hero,"CAST");break;
                case 56:
                    interfaceBounds();if(playtestButton("Raise Dead")==null||playtestButton("Skeleton")!=null)throw new AssertionError("56: Phylactery root label");
                    capture("raise-dead-menu");closeReviewWindows();
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton inspected=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton();inspected.remaining=17;
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob(inspected));break;
                case 57:
                    interfaceBounds();checkReviewText(Game.scene());if(!allReviewText(Game.scene()).contains("17 more turns"))throw new AssertionError("56: minion lifetime hidden");
                    capture("raised-undead-lifetime");closeReviewWindows();System.out.println("TEST 56 UI PASS: six-spell Brush menu, real shield cast/inspection, Raise Dead root, minion remaining turns");
                    displacedShamanChecks();
                    hatchlingPresentationChecks();
                    break;
                case 58:
                    if(!Dungeon.hero.belongings.contains(warningMeal))throw new AssertionError("55: meal eaten before player response");
                    if(!allReviewText(Game.scene()).contains("Your hatchling mimic grumbles hungrily"))throw new AssertionError("55: missing modal hunger warning");
                    boolean modal=false;
                    for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage)modal=true;
                    if(!modal)throw new AssertionError("55: warning is not a modal popup");
                    if(Dungeon.hero.curAction!=null || Dungeon.hero.lastAction!=null || Dungeon.hero.resting)throw new AssertionError("55: queued actions survived warning");
                    capture("hatchling-warning-and-hud");
                    closeReviewWindows();
                    Dungeon.hero.rest(false);break;
                case 59:
                    if(Dungeon.hero.belongings.contains(warningMeal)) {
                        HatchlingMimic pending=HatchlingMimic.carried();
                        HatchlingMimic.Feeding clock=Dungeon.hero.buff(HatchlingMimic.Feeding.class);
                        java.lang.reflect.Field actor=Actor.class.getDeclaredField("current");actor.setAccessible(true);
                        throw new AssertionError("55: feeding did not resume: ready="+Dungeon.hero.ready+" paralysis="+Dungeon.hero.paralysed
                                +" heroCooldown="+Dungeon.hero.cooldown()+" feeding="+(clock==null?"absent":clock.cooldown())
                                +" barrier="+RecoveryChecks.field(pending,"awaitingChoice")+" current="+actor.get(null));
                    }
                    if(allReviewText(Game.scene()).contains("com.shatteredpixel"))throw new AssertionError("55: object identity in meal log");
                    capture("hatchling-meal-name");
                    reviewRegionTransition(5);break;
                case 60:reviewRegionTransition(10);break;
                case 61:reviewRegionTransition(15);break;
                case 62:reviewRegionTransition(20);break;
                case 63:GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
                case 64:if(!playtestClickPage("Balance tuning"))return;break;
                case 65:interfaceBounds();capture("balance-tuning");playtestClick("Grimhollow enemies");break;
                case 66:interfaceBounds();playtestClick("Curse-bound chance: 10%");break;
                case 67:interfaceBounds();capture("balance-tuning-value");playtestInput("0","Apply");break;
                case 68:
                    if(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.get(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.CURSEBOUND)!=0)throw new AssertionError("58: tuning input did not apply");
                    interfaceBounds();capture("balance-tuning-enemies");
                    closeReviewWindows();com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();break;
                case 69:playtestClick("Item category weights");break;
                case 70:if(!playtestClickPage("Gold: 100%"))return;break;
                case 71:interfaceBounds();playtestInput("50","Apply");break;
                case 72:
                    if(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.get(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.GOLD)!=50)throw new AssertionError("58: paged tuning input");
                    closeReviewWindows();com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();break;
                case 73:if(!playtestClickPage("Reset all balance tuning"))return;break;
                case 74:
                    if(com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.changedCount()!=0||!Playtest.enabled())throw new AssertionError("58: tuning reset/playtest isolation");
                    if(mysteryMenuStep<6 && !mysteryMenuChecks())return;
                    if(!artworkReview())return;
                    System.out.println("TEST 58 UI PASS: balance menu, paging, numeric input, saved changes and reset via native pointer input");
                    Gdx.app.exit();return;
            }
            playtestStep++;
        }catch(ReflectiveOperationException | java.io.IOException error){throw new AssertionError(error);}
    }
    private static void questField(Class<?> type,String name,Object value) {
        try {java.lang.reflect.Field field=type.getDeclaredField(name);field.setAccessible(true);field.set(null,value);}
        catch(ReflectiveOperationException error){throw new AssertionError(error);}
    }
    private void prepareRooms(int kind) {
        // Like InterlevelScene, generate with no live GameScene receiving map
        // callbacks for the outgoing floor; the next scene installs itself.
        questField(GameScene.class,"scene",null);
        Dungeon.branch=kind==2||kind==3?1:0;Dungeon.depth=kind==0?8:kind==4?5:14;
        if(kind==0)questField(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker.Quest.class,"type",2);
        if(kind==2||kind==3)questField(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith.Quest.class,"type",kind==2?2:1);
        Level level=Dungeon.newLevel();
        int center=level.entrance();
        if(kind==0){
            center=com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle.ritualPos+2*level.width();
            for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap map:level.customTerrain)
                if(map instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.RitualSiteRoom.Table)
                    ritualTable=map.tileX+(map.tileY+1)*level.width();
            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room room=((com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)level).room(center);
            for(int cell=0;cell<level.length();cell++)if(level.map[cell]==Terrain.REGION_DECO && room.inside(level.cellToPoint(cell)))ritualCage=cell;
        } else if(kind==1){
            for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap map:level.customTiles)
                if(map instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom.SmithyVisuals)
                    center=map.tileX+map.tileW/2+(map.tileY+map.tileH/2)*level.width();
        } else if(kind==4){
            center=level.exit()+level.width();
        } else {
            for(int cell=level.width();cell<level.length()-level.width();cell++)
                if(level.map[cell]==Terrain.WALL_DECO && level.passable[cell+level.width()]){center=cell+level.width();break;}
        }
        // Keep real generated quest terrain, actors and rewards; remove ordinary combat only.
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:new java.util.ArrayList<>(level.mobs))
            if(mob.alignment==Char.Alignment.ENEMY)level.mobs.remove(mob);
        Dungeon.switchLevel(level,center);roomCenter=center;
        if(kind==0)sharedSightCheck();
        java.util.Arrays.fill(level.visited,true);
        Dungeon.observe();
    }
    private void sharedSightCheck(){
        Dungeon.hero.viewDistance=8;
        Dungeon.level.updateFieldOfView(Dungeon.hero,Dungeon.level.heroFOV);
        int distant=-1;
        for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.passable[cell]&&!Dungeon.level.heroFOV[cell]
                && Dungeon.level.distance(cell,Dungeon.hero.pos)>12){distant=cell;break;}
        if(distant<0)throw new AssertionError("No remote sight fixture");
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton ally=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton();
        ally.pos=distant;ally.configure(8);Dungeon.level.mobs.add(ally);
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent talent=com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.CORPSE_SENSE;
        Dungeon.hero.talents.get(1).put(talent,1);
        Dungeon.level.updateFieldOfView(Dungeon.hero,Dungeon.level.heroFOV);
        if(!Dungeon.level.heroFOV[distant])throw new AssertionError("Corpse Sense lost remote ally sight");
        Dungeon.level.mobs.remove(ally);Dungeon.hero.talents.get(1).remove(talent);
        Dungeon.level.updateFieldOfView(Dungeon.hero,Dungeon.level.heroFOV);
        if(Dungeon.level.heroFOV[distant])throw new AssertionError("Remote light remained after shared sight removed");
        System.out.println("TEST 57 SIGHT PASS: remote ally reveals a separate patch only while Corpse Sense supplies vision");
    }
    private static void tilePreview(Image image) {
        if(image==null)throw new AssertionError("Missing tile preview");
        float[] vertices=(float[])RecoveryChecks.field(image,"vertices");
        float maxX=0,maxY=0;
        for(int i=0;i<16;i+=4){maxX=Math.max(maxX,vertices[i]);maxY=Math.max(maxY,vertices[i+1]);}
        if(image.width()>16.01f || image.height()>16.01f || maxX>16.01f || maxY>16.01f)
            throw new AssertionError("Preview layout/drawn vertices disagree: "+image.width()+"/"+maxX+" x "+image.height()+"/"+maxY);
        image.destroy();
    }
    private void prepareHorror() {
        Level level=Dungeon.level;
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:level.mobs)Actor.remove(mob);
        level.mobs.clear();level.heaps.clear();level.traps.clear();level.plants.clear();
        for(Blob blob:level.blobs.values())Actor.remove(blob);
        level.blobs.clear();level.customTiles.clear();level.customWalls.clear();level.customTerrain.clear();
        level.freshRemains.clear();
        int w=level.width(),cx=w/2,cy=level.height()/2;
        for(int c=0;c<level.length();c++) {
            if(!level.insideMap(c))continue;
            boolean inside=Math.abs(c%w-cx)<=8 && Math.abs(c/w-cy)<=6;
            Level.set(c,inside?Terrain.EMPTY:Terrain.WALL);
        }
        level.cleanWalls();
        java.util.Arrays.fill(level.visited,false);java.util.Arrays.fill(level.mapped,false);
        Dungeon.hero.pos=horrorStart=cx+cy*w;Dungeon.hero.HP=Dungeon.hero.HT=200;
        Dungeon.hero.viewDistance=8;
        // Disable the native random passive search so this fixture specifically exercises the warning.
        com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight talisman=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight();
        talisman.cursed=true;Dungeon.hero.belongings.artifact=talisman;talisman.activate(Dungeon.hero);
        reviewHorror=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LurkingHorror();
        reviewHorror.pos=horrorStart-w;level.mobs.add(reviewHorror);Actor.add(reviewHorror);
        Dungeon.observe();
    }
    private void horrorTick() {
        if(!(Game.scene() instanceof GameScene))return;
        if(reviewHorror.phase()==com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LurkingHorror.Phase.WARNING
                && Dungeon.hero.ready && !horrorWarningCaptured) {
            if(horrorNoticeVisible()){capture("horror-warning");horrorWarningCaptured=true;}
        }
        if(++horrorFrames%60!=0)return;
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero=Dungeon.hero;
        switch(horrorStep++) {
            case 0:
                if(!hero.ready){horrorStep--;return;}
                horrorHealth=hero.HP;
                if(reviewHorror.sprite.visible)throw new AssertionError("Shadowmeld sprite visible before detection");
                pointerCell(horrorStart+6);break;
            case 1:
                if(!hero.ready || hero.pos!=horrorStart+1 || hero.HP!=horrorHealth
                        || reviewHorror.phase()!=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LurkingHorror.Phase.WARNING)
                    throw new AssertionError("Real pointer travel failed to stop for warning: pos="+hero.pos+" start="+horrorStart+" ready="+hero.ready+" hp="+hero.HP+" phase="+reviewHorror.phase());
                break;
            case 2:
                if(hero.HP!=horrorHealth || reviewHorror.phase()!=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LurkingHorror.Phase.WARNING)
                    throw new AssertionError("Warning attacked before a fresh player action");
                if(!horrorNoticeVisible())throw new AssertionError("Ambush HUD warning faded before the player responded");
                pointerCell(horrorStart+2);break;
            case 3:
                if(hero.HP!=horrorHealth || hero.pos!=horrorStart+2 || reviewHorror.shadowmelded())
                    throw new AssertionError("Fresh movement did not evade ambush");
                capture("horror-exposed");
                reviewHorror.pos=hero.pos-5;reviewHorror.sprite.place(reviewHorror.pos);reviewHorror.rooted=true;
                hero.viewDistance=2;
                java.util.Arrays.fill(Dungeon.level.visited,false);java.util.Arrays.fill(Dungeon.level.mapped,false);
                Dungeon.observe();GameScene.updateFog();
                horrorFov=Dungeon.level.heroFOV.clone();horrorVisited=Dungeon.level.visited.clone();horrorMapped=Dungeon.level.mapped.clone();
                Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision.class,10);Dungeon.observe();break;
            case 4:
                if(!reviewHorror.sensed() || reviewHorror.sprite.visible
                        || !java.util.Arrays.equals(horrorFov,Dungeon.level.heroFOV)
                        || !java.util.Arrays.equals(horrorVisited,Dungeon.level.visited)
                        || !java.util.Arrays.equals(horrorMapped,Dungeon.level.mapped))throw new AssertionError("Native Mind Vision leaked terrain or drew the ordinary sprite");
                if(Actor.findChar(reviewHorror.pos)!=reviewHorror)throw new AssertionError("Hidden entity lost collision occupancy");
                capture("horror-entity-only-sense");GameScene.examineCell(reviewHorror.pos);break;
            case 5:
                interfaceBounds();checkReviewText(Game.scene());
                if(!allReviewText(Game.scene()).contains("Lurking Horror"))throw new AssertionError("Unknown-cell entity inspection failed");
                capture("horror-description");closeReviewWindows();
                Buff.detach(hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision.class);hero.viewDistance=8;
                reviewHorror.pos=hero.pos-3;reviewHorror.sprite.place(reviewHorror.pos);
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat dead=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
                dead.pos=hero.pos+Dungeon.level.width();
                Dungeon.level.freshRemains.put(dead.pos,new com.shatteredpixel.shatteredpixeldungeon.levels.features.FreshRemains(dead));
                Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.Gold(7),dead.pos);
                dead.pos--;
                Dungeon.level.freshRemains.put(dead.pos,new com.shatteredpixel.shatteredpixeldungeon.levels.features.FreshRemains(dead));
                Dungeon.observe();break;
            case 6:
                capture("horror-and-fresh-remains");GameScene.examineCell(hero.pos+Dungeon.level.width());break;
            case 7:
                interfaceBounds();capture("horror-remains-and-loot");
                playtestClick(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(Dungeon.level.freshRemains.get(hero.pos+Dungeon.level.width()).name()));break;
            case 8:
                interfaceBounds();checkReviewText(Game.scene());
                if(!allReviewText(Game.scene()).contains("narrow wounds"))throw new AssertionError("Missing remains evidence text");
                capture("horror-remains-description");closeReviewWindows();
                new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBone().onZap(new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(hero.pos,hero.pos+3,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.STOP_TARGET));
                capture("painted-bone-wall");GameScene.examineCell(hero.pos+3);break;
            case 9:
                interfaceBounds();checkReviewText(Game.scene());
                if(!allReviewText(Game.scene()).contains("Bone Wall") || !allReviewText(Game.scene()).contains("binding holds for"))throw new AssertionError("Bone-wall inspection missing identity/lifetime");
                Image wall=com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell.cellImage(hero.pos+3);
                if(wall.texture!=com.watabou.gltextures.TextureCache.get(com.shatteredpixel.shatteredpixeldungeon.effects.BoneWallArt.TEXTURE))throw new AssertionError("Bone-wall inspection uses old barricade art");wall.destroy();
                capture("bone-wall-inspection");closeReviewWindows();
                Playtest.enable();com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();break;
            case 10:if(!playtestClickPage("Lurking Horror"))horrorStep--;break;
            case 11:
                interfaceBounds();capture("horror-balance-tuning");closeReviewWindows();
                if(!horrorWarningCaptured)throw new AssertionError("Warning evidence not captured");
                System.out.println("TEST 60 NATIVE PASS: persistent ambush HUD, real pointer auto-travel interruption, fresh-action evasion, painted exposed sprite, entity-only Mind Vision, unknown-cell inspection, remains/loot and painted bone-wall lifetime inspection, balance menu; failures=0");
                Gdx.app.exit();break;
        }
    }
    private boolean horrorNoticeVisible(){
        for(com.watabou.noosa.Gizmo member:RecoveryChecks.members(Game.scene()))if(member instanceof com.shatteredpixel.shatteredpixeldungeon.effects.HorrorSenseLayer)
            return ((com.shatteredpixel.shatteredpixeldungeon.effects.HorrorSenseLayer)member).ambushNoticeVisible();
        return false;
    }
    private void expeditionFloor(int depth) {
        questField(GameScene.class,"scene",null);
        Dungeon.branch=DragonExpedition.BRANCH;Dungeon.depth=depth;
        Level level=Dungeon.newLevel();
        int arrival=depth==DragonExpedition.CAVERN?com.shatteredpixel.shatteredpixeldungeon.levels.DragonCavernLevel.CENTER
                :depth==DragonExpedition.HOARD?com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel.TREASURE+3*level.width():level.entrance();
        Dungeon.switchLevel(level,arrival);
        if(DragonExpedition.dragon!=null && level.mobs.contains(DragonExpedition.dragon))DragonExpedition.dragon.pos=arrival-2*level.width();
        Dungeon.observe();switchNoFade(GameScene.class);
    }
    private void expeditionTick(){
        if(!(Game.scene() instanceof GameScene)||++expeditionFrames%60!=0)return;
        switch(expeditionStep++) {
            case 0:
                for(Class<?> type:new Class[]{com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon.class,
                        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother.class,
                        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter.class,
                        com.shatteredpixel.shatteredpixeldungeon.items.quest.ExpeditionMap.class})
                    for(String key:new String[]{"name","desc"})
                        if(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(type,key).contains("NO TEXT"))throw new AssertionError("Missing expedition text: "+type+"/"+key);
                expeditionFloor(DragonExpedition.CHASM);break;
            case 1:
                if(!(DragonExpedition.dragon.sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.ExpeditionDragonSprite)
                        || DragonExpedition.dragon.sprite.visualFootprint()!=40)throw new AssertionError("Dragon art/size");
                capture("expedition-chasm");
                DragonExpedition.dragon.damage(0,Dungeon.hero);
                DragonExpedition.dragon.prepare(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon.Attack.BREATH,Dungeon.hero.pos);break;
            case 2:
                if(RecoveryChecks.field(RecoveryChecks.field(Game.scene(),"boss"),"boss")!=DragonExpedition.dragon)
                    throw new AssertionError("Expedition dragon boss HUD missing");
                capture("expedition-breath-warning");
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob(DragonExpedition.dragon));break;
            case 3:
                interfaceBounds();capture("expedition-dragon-details");closeReviewWindows();
                expeditionFloor(DragonExpedition.CAVERN);break;
            case 4:
                capture("expedition-cavern-darkness");
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother brood=null;
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)
                    if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother)brood=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother)m;
                if(brood==null || brood.sprite.visualFootprint()!=26)throw new AssertionError("Broodmother art/size");
                for(int d:com.watabou.utils.PathFinder.NEIGHBOURS8)
                    if(Dungeon.level.passable[brood.pos+d]&&Dungeon.level.findMob(brood.pos+d)==null){Dungeon.hero.pos=brood.pos+d;break;}
                Dungeon.hero.sprite.place(Dungeon.hero.pos);Dungeon.observe();
                com.watabou.noosa.Camera.main.panFollow(Dungeon.hero.sprite, 5);
                brood.damage(0,Dungeon.hero);
                break;
            case 5:
                if(!(RecoveryChecks.field(RecoveryChecks.field(Game.scene(),"boss"),"boss") instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother))
                    throw new AssertionError("Expedition broodmother boss HUD missing");
                capture("expedition-broodmother");
                Dungeon.hero.pos=com.shatteredpixel.shatteredpixeldungeon.levels.DragonCavernLevel.CENTER;
                Dungeon.hero.sprite.place(Dungeon.hero.pos);Playtest.enable();Playtest.reveal();
                com.watabou.noosa.Camera.main.zoom(1f);
                com.watabou.noosa.Camera.main.panFollow(Dungeon.hero.sprite,5);expeditionStep=42;break;
            case 6:
                if(Dungeon.level.heaps.size!=0)throw new AssertionError("Locked hoard has stealable loot");
                for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap tile:Dungeon.level.customTiles)
                    if(tile instanceof com.shatteredpixel.shatteredpixeldungeon.tiles.ExpeditionHoardTiles)tilePreview(tile.image(3,2));
                capture("expedition-hoard-sealed");
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel.TREASURE));break;
            case 7:
                interfaceBounds();capture("expedition-hoard-details");closeReviewWindows();
                DragonExpedition.dragonSlain=true;DragonExpedition.dragon.destroy();DragonExpedition.dragon.sprite.killAndErase();
                ((com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel)Dungeon.level).unlockHoard();
                Dungeon.observe();break;
            case 8:
                capture("expedition-hoard-reward");
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter hunter=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter();
                hunter.pos=Dungeon.hero.pos+1;GameScene.add(hunter);hunter.interact(Dungeon.hero);break;
            case 9:
                interfaceBounds();capture("expedition-hunter");closeReviewWindows();
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(new com.shatteredpixel.shatteredpixeldungeon.items.quest.ExpeditionMap()));break;
            case 10:
                interfaceBounds();capture("expedition-map");closeReviewWindows();Playtest.enable();
                com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();break;
            case 11:
                if(!playtestClickPage("Expedition dragon"))expeditionStep--;break;
            case 12:
                interfaceBounds();capture("expedition-tuning-dragon");playtestClick("Dragon health: 480");break;
            case 13:
                interfaceBounds();capture("expedition-tuning-value");playtestInput("720","Apply");break;
            case 14:
                if(BalanceTuning.get(BalanceTuning.Key.DRAGON_HEALTH)!=720)throw new AssertionError("Expedition numeric tuning input");
                closeReviewWindows();com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();break;
            case 15:
                if(!playtestClickPage("Expedition cavern"))expeditionStep--;break;
            case 16:
                interfaceBounds();capture("expedition-tuning-cavern");closeReviewWindows();com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();break;
            case 17:
                if(!playtestClickPage("Expedition supplies and hoard"))expeditionStep--;break;
            case 18:
                interfaceBounds();capture("expedition-tuning-hoard");closeReviewWindows();
                com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest.tuning();expeditionStep=40;break;
            case 40:
                if(!playtestClickPage("Cavern remains loot"))expeditionStep--;break;
            case 41:
                interfaceBounds();capture("expedition-tuning-remains");closeReviewWindows();BalanceTuning.reset();expeditionStep=19;break;
            case 42:
                capture("expedition-cavern-overview");expeditionFloor(DragonExpedition.HOARD);expeditionStep=6;break;
            case 19:
                questField(GameScene.class,"scene",null);Dungeon.init();
                Dungeon.depth=expeditionTown=DragonExpedition.hunterDepth;
                Level city=Dungeon.newLevel();expeditionHunter=null;
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:new java.util.ArrayList<>(city.mobs)) {
                    if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter)expeditionHunter=(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.TreasureHunter)m;
                    else if(m.alignment==Char.Alignment.ENEMY)city.mobs.remove(m);
                }
                if(expeditionHunter==null)throw new AssertionError("Campaign hunter missing");
                int approach=-1;
                for(int d:new int[]{-1,1,-city.width(),city.width()})if(city.passable[expeditionHunter.pos+d] && city.findMob(expeditionHunter.pos+d)==null){approach=expeditionHunter.pos+d;break;}
                if(approach<0)throw new AssertionError("No hunter approach");
                Dungeon.switchLevel(city,approach);Playtest.enable();Playtest.god(true);
                for(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing potion:Dungeon.hero.belongings.getAllItems(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class))potion.detachAll(Dungeon.hero.belongings.backpack);
                new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing().quantity(2).collect();
                switchNoFade(GameScene.class);break;
            case 20:
                expeditionHunter.interact(Dungeon.hero);break;
            case 21:
                interfaceBounds();capture("expedition-city-offer");playtestClick("Give a potion of healing");break;
            case 22:
                if(!DragonExpedition.accepted || Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.class).quantity()!=1)
                    throw new AssertionError("Native healing exchange");
                interfaceBounds();capture("expedition-city-thanks");closeReviewWindows();break;
            case 23:
                Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.quest.ExpeditionMap.class).execute(Dungeon.hero,com.shatteredpixel.shatteredpixeldungeon.items.quest.ExpeditionMap.OPEN);break;
            case 24:
                interfaceBounds();capture("expedition-entry-warning");playtestClick("Enter the expedition");break;
            case 25:
                if(Dungeon.branch!=2 || !(Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.DragonChasmLevel))throw new AssertionError("Native map did not enter expedition");
                capture("expedition-entry-complete");
                try {Dungeon.saveAll();}catch(java.io.IOException error){throw new AssertionError(error);}
                InterlevelScene.mode=InterlevelScene.Mode.CONTINUE;Game.switchScene(InterlevelScene.class);break;
            case 26:
                if(Dungeon.branch!=2 || Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ExpeditionDragon).count()!=1)
                    throw new AssertionError("Native continue lost/duplicated boss");
                com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.heroFall(Dungeon.hero.pos+1);break;
            case 27:
                if(!(Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.DragonCavernLevel))throw new AssertionError("Native fall destination");
                if(Dungeon.level.activateTransition(Dungeon.hero,Dungeon.level.getTransition(null)))throw new AssertionError("Native living broodmother permits climb");
                capture("expedition-fall-and-blocked-climb");break;
            case 28:
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:new java.util.ArrayList<>(Dungeon.level.mobs))if(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Broodmother){m.HP=0;m.die(Dungeon.hero);}
                Dungeon.hero.pos=com.shatteredpixel.shatteredpixeldungeon.levels.DragonCavernLevel.CENTER;Dungeon.hero.sprite.place(Dungeon.hero.pos);
                Dungeon.level.activateTransition(Dungeon.hero,Dungeon.level.getTransition(null));break;
            case 29:
                if(!(Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.DragonChasmLevel) || Dungeon.hero.pos!=com.shatteredpixel.shatteredpixeldungeon.levels.DragonChasmLevel.centerCell())throw new AssertionError("Native climb destination");
                DragonExpedition.dragon.HP=0;DragonExpedition.dragon.die(Dungeon.hero);Dungeon.hero.spendAndNext(1f);break;
            case 30:
                if(!(Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel) || !DragonExpedition.rewardsCreated || DragonExpedition.victoryPending)
                    throw new AssertionError("Scheduled native victory transport/reward");
                capture("expedition-victory-arrival");
                Dungeon.hero.pos=com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel.RETURN;Dungeon.hero.sprite.place(Dungeon.hero.pos);Dungeon.observe();
                pointerCell(com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel.RETURN);break;
            case 31:
                if(Dungeon.branch==2 && expeditionExitAttempts++<3){pointerCell(com.shatteredpixel.shatteredpixeldungeon.levels.DragonHoardLevel.RETURN);expeditionStep--;break;}
                if(Dungeon.branch!=0 || Dungeon.depth!=expeditionTown || Dungeon.hero.pos!=DragonExpedition.returnCell)throw new AssertionError("Native hoard exit lost original City return");
                capture("expedition-safe-return");break;
            default:
                System.out.println("TEST 59 NATIVE PASS: painted assets, dark cavern, menu input, actual NPC exchange/map/continue/fall/blocked climb/boss clear/scheduled victory and pointer exit to original City; orientation="+(Boolean.getBoolean("grimhollow.interfacePortrait")?"portrait":"landscape"));
                Gdx.app.exit();
        }
    }
    private void roomTick(){
        if(!(Game.scene() instanceof GameScene)||++roomFrames%60!=0)return;
        try {
            switch(roomStep++){
                case 0:
                    for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap map:Dungeon.level.customTiles)
                        if(map instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.RitualSiteRoom.RitualMarker)tilePreview(map.image(2,2));
                    for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap map:Dungeon.level.customTerrain)
                        if(map instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.RitualSiteRoom.Table)tilePreview(map.image(0,1));
                    capture("ritual-room");GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(ritualTable));break;
                case 1:
                    interfaceBounds();capture("ritual-table-popup");closeReviewWindows();
                    tilePreview(com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap.tile(ritualCage,Terrain.REGION_DECO));
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(ritualCage));break;
                case 2:
                    interfaceBounds();capture("cage-popup");closeReviewWindows();
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle.ritualPos));break;
                case 3:
                    interfaceBounds();capture("ritual-circle-popup");closeReviewWindows();
                    int ritual=com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle.ritualPos;
                    java.lang.reflect.Method drop=com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle.class.getDeclaredMethod("onThrow",int.class);drop.setAccessible(true);
                    int[] offsets={-Dungeon.level.width(),1,Dungeon.level.width(),-1};
                    for(int i=0;i<4;i++){
                        drop.invoke(new com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle(),ritual+offsets[i]);
                        long count=Dungeon.level.mobs.stream().filter(m->m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental.NewbornFireElemental).count();
                        if(count!=(i==3?1:0))throw new AssertionError("Ritual required four cardinal candles: "+i+" -> "+count);
                    }
                    break;
                case 4:
                    capture("ritual-elemental-summoned");
                    for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:new java.util.ArrayList<>(Dungeon.level.mobs))
                        if(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental.NewbornFireElemental)mob.die(Dungeon.hero);
                    com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers embers=null;
                    for(com.shatteredpixel.shatteredpixeldungeon.items.Heap heap:Dungeon.level.heaps.valueList())
                        for(com.shatteredpixel.shatteredpixeldungeon.items.Item item:heap.items)
                            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers)embers=(com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers)item;
                    if(embers==null || !embers.collect())throw new AssertionError("Elemental did not yield collectable quest embers");
                    questField(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker.Quest.class,"given",true);
                    for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs)
                        if(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker)mob.interact(Dungeon.hero);
                    break;
                case 5:
                    interfaceBounds();capture("wandmaker-reward");
                    boolean reward=false;
                    for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))
                        if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndWandmaker){
                            java.lang.reflect.Method select=child.getClass().getDeclaredMethod("selectReward",com.shatteredpixel.shatteredpixeldungeon.items.Item.class);select.setAccessible(true);
                            com.shatteredpixel.shatteredpixeldungeon.items.Item wand=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker.Quest.wand1;
                            select.invoke(child,wand);
                            if(!Dungeon.hero.belongings.contains(wand)||com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker.Quest.wand1!=null)throw new AssertionError("Quest reward not completed");
                            reward=true;
                        }
                    if(!reward)throw new AssertionError("Wandmaker reward dialog absent");
                    System.out.println("TEST 57 RITUAL PASS: four real candle throws, elemental, embers and Wandmaker reward completion; 16-unit popup vertices");
                    closeReviewWindows();prepareRooms(1);switchNoFade(GameScene.class);break;
                case 6:
                    capture("smithy-room");
                    for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap map:Dungeon.level.customTiles)
                        if(map instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.BlacksmithRoom.SmithyVisuals)tilePreview(map.image(0,0));
                    // Exercise all rail junctions and ensure unknown neighbors do not select a branch.
                    int c=roomCenter,w=Dungeon.level.width();int[] near={c-w,c+1,c+w,c-1};
                    Level.set(c,Terrain.REGION_DECO);
                    for(int cell:near)Level.set(cell,Terrain.REGION_DECO);
                    for(int mask=0;mask<16;mask++){
                        for(int i=0;i<4;i++){Dungeon.level.heroFOV[near[i]]=false;Dungeon.level.mapped[near[i]]=false;Dungeon.level.visited[near[i]]=(mask&(1<<i))!=0;}
                        if(com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap.railConnections(c)!=mask)throw new AssertionError("Rail adjacency/knowledge mismatch");
                    }
                    java.util.Arrays.fill(Dungeon.level.visited,true);GameScene.updateMap();
                    GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(c));break;
                case 7:
                    interfaceBounds();capture("rail-popup");closeReviewWindows();prepareRooms(2);switchNoFade(GameScene.class);break;
                case 8:
                    miningTorchChecks();capture("gnoll-mine-ore");
                    // Removing the source also retires a previously created flame on the next frame.
                    int ore=roomCenter-Dungeon.level.width();Level.set(ore,Terrain.WALL_DECO);
                    EnhancedEffects.Torch flame=new EnhancedEffects.Torch(ore);Level.set(ore,Terrain.EMPTY);flame.update();
                    if(flame.alive)throw new AssertionError("Flame survived source mining");
                    GameScene.updateMap();break;
                case 9:
                    capture("gnoll-mine-after-mining");prepareRooms(3);switchNoFade(GameScene.class);break;
                case 10:
                    miningTorchChecks();capture("crystal-mine");
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Chainwarden boss=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Chainwarden();
                    boss.pos=Dungeon.hero.pos+1;boss.HP=boss.HT/2;GameScene.add(boss);com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar.assignBoss(boss);
                    break;
                case 11:
                    capture("painted-boss-bar");
                    for(Icons icon:new Icons[]{Icons.DEPTH,Icons.DEPTH_CHASM,Icons.DEPTH_WATER,Icons.DEPTH_GRASS,Icons.DEPTH_DARK,Icons.DEPTH_LARGE,Icons.DEPTH_TRAPS,Icons.DEPTH_SECRETS}){
                        Image visual=icon.get();if(visual.frame().width()*visual.texture.width<63)throw new AssertionError("Pixel depth icon: "+icon);visual.destroy();
                    }
                    System.out.println("TEST 57 ROOMS PASS: generated ritual/smithy/both mines, quest completion, preview vertices, 16 rail junctions, no ore flames/light, source removal, shared sight and painted boss/depth HUD");
                    prepareRooms(4);switchNoFade(GameScene.class);break;
                case 12:
                    sewerExitArtworkCheck(15);capture("sewer-exit-locked");
                    Level.set(Dungeon.level.exit(),Terrain.EXIT);GameScene.updateMap(Dungeon.level.exit());break;
                case 13:
                    sewerExitArtworkCheck(7);capture("sewer-exit-open");
                    for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs)
                        if(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.RatKing){Dungeon.hero.pos=mob.pos+Dungeon.level.width();Dungeon.hero.sprite.place(Dungeon.hero.pos);Dungeon.observe();}
                    break;
                case 14:
                    capture("rat-king-cushion");
                    System.out.println("PAINTED EXIT PASS: generated floor-five gateway, locked/open live cell refresh and Rat King cushion; failures=0");
                    Gdx.app.exit();return;
            }
        } catch(ReflectiveOperationException error){throw new AssertionError(error);}
    }
    private void sewerExitArtworkCheck(int index){
        boolean found=false;
        for(com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap tile:Dungeon.level.customTiles)
            if(tile instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.sewerboss.SewerBossExitRoom.SewerExit){
                com.watabou.noosa.Tilemap vis=(com.watabou.noosa.Tilemap)RecoveryChecks.field(tile,"vis");
                Image middle=vis.image(1,0);
                com.watabou.utils.RectF expected=new com.watabou.noosa.TextureFilm("environment/custom_tiles/painted_sewer_exit.png",64,64).get(index);
                com.watabou.utils.RectF actual=middle.frame();
                if(middle.texture.width!=192 || middle.texture.height!=384 || actual.left!=expected.left || actual.top!=expected.top || actual.right!=expected.right || actual.bottom!=expected.bottom)throw new AssertionError("Sewer exit painted state: "+index);
                middle.destroy();found=true;
            }
        if(!found)throw new AssertionError("Missing generated sewer exit");
    }
    private void miningTorchChecks(){
        int ore=0;
        for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.map[cell]==Terrain.WALL_DECO){
            ore++;if(EnhancedEffects.torchAt(Dungeon.level,cell))throw new AssertionError("Mining ore emits torch light");
        }
        if(ore==0)throw new AssertionError("No ore in generated mine");
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))
            if(child instanceof EnhancedEffects.Torch)throw new AssertionError("Ore torch was instantiated");
    }

    private void reviewRegionTransition(int floor) throws java.io.IOException {
        InterlevelScene.returnDepth=floor+1;InterlevelScene.returnBranch=0;
        InterlevelScene.mode=InterlevelScene.Mode.PLAYTEST;
        ShatteredPixelDungeon.switchScene(InterlevelScene.class);
    }

    private void hatchlingPresentationChecks() throws ReflectiveOperationException {
        for(Buff buff:Dungeon.hero.buffs()) if(buff.icon()!=BuffIndicator.NONE)buff.detach();
        for(int i=0;i<8;i++) {
            final int symbol=i;
            new Buff(){@Override public int icon(){return symbol;}}.attachTo(Dungeon.hero);
        }
        com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane status=
                (com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane)RecoveryChecks.field(Game.scene(),"status");
        BuffIndicator bar=(BuffIndicator)RecoveryChecks.field(status,"buffs");
        bar.setRect(bar.left(),bar.top(),bar.width(),bar.height());
        com.watabou.noosa.Visual panel=(com.watabou.noosa.Visual)RecoveryChecks.field(status,"bg");
        int count=0;float firstTop=-1;
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(bar)) if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.IconButton && child.visible){
            Image icon=(Image)RecoveryChecks.field(child,"icon");
            if(icon.x+icon.width()>panel.x+panel.width()-.5f)throw new AssertionError("HUD effect outside its panel: "+count);
            if(count==0)firstTop=icon.y;
            if(count==6 && !((Boolean)RecoveryChecks.field(status,"large")) && icon.y<=firstTop)throw new AssertionError("Seventh effect failed to wrap");
            count++;
        }
        if(count!=8)throw new AssertionError("HUD lost effect icons: "+count);
        for(Icons kind:new Icons[]{Icons.ARROW,Icons.SKULL}){
            Image icon=kind.get();
            if(icon.frame().width()*icon.texture.width<28)throw new AssertionError("Legacy HUD glyph: "+kind);
            icon.destroy();
        }
        // A real long-move command must be cancelled, even while a slow action has more ticks due.
        HatchlingMimic item=HatchlingMimic.carried();
        if(item==null)item=new HatchlingMimic();
        for(com.shatteredpixel.shatteredpixeldungeon.items.Item candidate:new java.util.ArrayList<>(Dungeon.hero.belongings.backpack.items))
            if(HatchlingMimic.foodPriority(candidate,Dungeon.hero)>=0)candidate.detachAll(Dungeon.hero.belongings.backpack);
        // Make space before pickup; earlier catalogue tests can fill the backpack.
        // Recollect after class changes so the real feeding Actor is installed.
        item.detachAll(Dungeon.hero.belongings.backpack);
        if(!item.collect() || HatchlingMimic.carried()!=item)throw new AssertionError("Hatchling fixture pickup failed");
        warningMeal=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword().identify();
        if(!warningMeal.collect())throw new AssertionError("Hatchling fixture meal pickup failed");
        com.watabou.utils.Bundle state=new com.watabou.utils.Bundle();item.storeInBundle(state);state.put("hunger_left",2);state.put("hunger_warned",false);item.restoreFromBundle(state);
        Dungeon.hero.curAction=new com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction.Move(Dungeon.hero.pos+5);
        Dungeon.hero.resting=true;
        item.tick(Dungeon.hero);
        for(int i=0;i<5;i++)item.tick(Dungeon.hero);
        if(Dungeon.hero.curAction!=null || Dungeon.hero.resting || !Dungeon.hero.belongings.contains(warningMeal))throw new AssertionError("55: warning failed to stop travel/rest before consumption");
        Dungeon.hero.act(); // The real readiness path releases the barrier, without spending a turn.
        // Exercise the NPC's real summoning emitter using its already-painted RATTLE path.
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer necro=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer();
        necro.pos=Dungeon.hero.pos-3;necro.summoning=true;necro.summoningPos=Dungeon.hero.pos-2;
        Level.set(necro.pos,Terrain.EMPTY);Level.set(necro.summoningPos,Terrain.EMPTY);
        Dungeon.level.heroFOV[necro.pos]=Dungeon.level.heroFOV[necro.summoningPos]=true;
        com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.NecromancerSprite();
        Dungeon.hero.sprite.parent.add(sprite);sprite.link(necro);sprite.visible=true;
        com.watabou.noosa.particles.Emitter emitter=(com.watabou.noosa.particles.Emitter)RecoveryChecks.field(sprite,"summoningBones");
        if(emitter==null)throw new AssertionError("NPC summoning emitter missing");
        System.out.println("TEST 55 UI WARNING PASS: real Hero readiness, slow-turn barrier, title log, painted HUD glyphs and eight contained effects; NPC RATTLE emitter active");
    }

    private void displacedShamanChecks() throws ReflectiveOperationException {
        float elapsed=Game.elapsed;
        try {
            Game.elapsed=.02f;
            for(Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob> type:new Class[]{
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.RedShaman.class,
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.BlueShaman.class,
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.PurpleShaman.class,
                    com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Hexcaster.class}) {
                com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob shaman=type.getDeclaredConstructor().newInstance();
                int origin=Dungeon.hero.pos-2,destination=Dungeon.hero.pos+1,w=Dungeon.level.width();
                for(int dy=-2;dy<=2;dy++)for(int dx=-4;dx<=3;dx++)Level.set(Dungeon.hero.pos+dx+dy*w,Terrain.EMPTY);
                if(Actor.findChar(destination)!=null)throw new AssertionError("Shaman fixture destination occupied");
                shaman.pos=origin;Group sprites=new Group();
                shaman.sprite=shaman.sprite();sprites.add(shaman.sprite);shaman.sprite.link(shaman);
                Actor.add(shaman);Dungeon.level.mobs.add(shaman);
                Dungeon.level.heroFOV[origin]=true;shaman.sprite.visible=true;
                if(shaman instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Hexcaster){
                    shaman.pos=Dungeon.hero.pos-1;shaman.sprite.place(shaman.pos);
                    shaman.fieldOfView=new boolean[Dungeon.level.length()];java.util.Arrays.fill(shaman.fieldOfView,true);
                    java.lang.reflect.Field enemy=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob.class.getDeclaredField("enemy");enemy.setAccessible(true);enemy.set(shaman,Dungeon.hero);
                    int previous=shaman.pos;shaman.state=shaman.HUNTING;shaman.state.act(true,false);
                    if(shaman.pos==previous)throw new AssertionError("Hexcaster retreat did not move");
                    for(int frame=0;frame<30;frame++)sprites.update();
                    com.watabou.utils.PointF expected=shaman.sprite.worldToCamera(shaman.pos);
                    if(Math.abs(shaman.sprite.x-expected.x)>.01f||Math.abs(shaman.sprite.y-expected.y)>.01f)throw new AssertionError("Hexcaster retreat sprite remained in old cell");
                    shaman.pos=origin;shaman.sprite.place(origin);
                }

                Buff.affect(shaman,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo.class,10);
                for(int attempt=0;attempt<30 && shaman.pos==origin;attempt++)shaman.move(origin+1);
                if(shaman.pos==origin)throw new AssertionError("Vertigo movement fixture did not move");
                Dungeon.level.heroFOV[shaman.pos]=true;shaman.sprite.visible=true;
                shaman.sprite.move(origin,shaman.pos); // Visible Mob AI animates after Char.move's Vertigo animation.
                sprites.update();
                com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation.appear(shaman,destination);
                for(int frame=0;frame<30;frame++)sprites.update();
                com.watabou.utils.PointF expected=shaman.sprite.worldToCamera(shaman.pos);
                if(Math.abs(shaman.sprite.x-expected.x)>.01f || Math.abs(shaman.sprite.y-expected.y)>.01f)
                    throw new AssertionError("Displaced shaman: stale movement overwrote teleport position for "+type.getSimpleName());
                if(Actor.findChar(destination)!=shaman || !Dungeon.hero.canAttack(shaman))throw new AssertionError("Shaman occupancy/melee target mismatch");
                java.lang.reflect.Method objects=GameScene.class.getDeclaredMethod("getObjectsAtCell",int.class);objects.setAccessible(true);
                Dungeon.level.heroFOV[destination]=true;
                if(!((java.util.List<?>)objects.invoke(null,destination)).contains(shaman))throw new AssertionError("Examine cannot find displaced shaman");
                // On tablets a movement tween can outlast a knockback (0.18s versus 0.15s).
                Buff.detach(shaman,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo.class);
                com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.setMoveInterval(.3f);
                shaman.pos=origin;shaman.sprite.place(origin);
                shaman.sprite.move(origin,origin+1);shaman.pos=origin+1;
                com.shatteredpixel.shatteredpixeldungeon.effects.Pushing push=
                        new com.shatteredpixel.shatteredpixeldungeon.effects.Pushing(shaman,shaman.pos,destination,()->shaman.pos=destination);
                push.new Effect();
                for(int frame=0;frame<30;frame++)sprites.update();
                expected=shaman.sprite.worldToCamera(shaman.pos);
                if(Math.abs(shaman.sprite.x-expected.x)>.01f || Math.abs(shaman.sprite.y-expected.y)>.01f)
                    throw new AssertionError("Displaced shaman: movement outlasted knockback for "+type.getSimpleName());
                shaman.HP=0;Actor.remove(shaman);Dungeon.level.mobs.remove(shaman);sprites.destroy();
            }
        } finally {Game.elapsed=elapsed;com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.setMoveInterval(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.DEFAULT_MOVE_INTERVAL);}
        System.out.println("SHAMAN POSITION PASS: all three variants and Hexcaster retreat, overlapping movement/teleport/knockback, sprite cell equals occupancy, melee and examine targets");
    }
    @SuppressWarnings("unchecked") private com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane handbookPage(int index){
        return ((java.util.List<com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane>)RecoveryChecks.field(reviewHandbook(),"pages")).get(index);
    }
    private com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton rankButton(Group group,com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent talent){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(group)){
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton&&RecoveryChecks.field(child,"talent")==talent)return (com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton)child;
            if(child instanceof Group){com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton found=rankButton((Group)child,talent);if(found!=null)return found;}
        }return null;
    }
    private com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent talentWindow(){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent)return (com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent)child;
        return null;
    }
    private String allReviewText(Group group){
        checkScrollCameras(group);
        StringBuilder text=new StringBuilder();for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(group)){
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)text.append(((com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)child).text());
            if(child instanceof Group)text.append(allReviewText((Group)child));
        }return text.toString();
    }
    private void checkScrollCameras(Group group){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(group))if(child!=null&&child.visible){
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane){
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)child;
                com.watabou.utils.Point at=pane.camera().cameraToScreen(pane.left(),pane.top());
                if(pane.content().camera.x!=at.x||pane.content().camera.y!=at.y)throw new AssertionError("Description camera did not follow resized window");
            }
            if(child instanceof Group)checkScrollCameras((Group)child);
        }
    }
    private void pointerCell(int cell){
        com.watabou.utils.Point point=Camera.main.cameraToScreen(cell%Dungeon.level.width()*16+8,cell/Dungeon.level.width()*16+8);
        for(com.watabou.input.PointerEvent.Type type:new com.watabou.input.PointerEvent.Type[]{com.watabou.input.PointerEvent.Type.DOWN,com.watabou.input.PointerEvent.Type.UP}){
            com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(point.x,point.y,902,type,com.watabou.input.PointerEvent.NONE));
            com.watabou.input.PointerEvent.processPointerEvents();
        }
    }
    private void tabletTerrainChecks()throws ReflectiveOperationException{
        int w=Dungeon.level.width(),top=Dungeon.hero.pos+2*w,source=top+w;
        com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap walls=new com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap();
        java.lang.reflect.Method visual=walls.getClass().getDeclaredMethod("getTileVisual",int.class,int.class,boolean.class);visual.setAccessible(true);
        Level.set(top,Terrain.EMPTY);Dungeon.level.heroFOV[top]=true;
        for(int terrain:new int[]{Terrain.WALL,Terrain.DOOR,Terrain.LOCKED_DOOR,Terrain.STATUE,Terrain.BARRICADE}){
            Level.set(source,terrain);Dungeon.level.heroFOV[source]=Dungeon.level.visited[source]=Dungeon.level.mapped[source]=false;
            if((int)visual.invoke(walls,top,Terrain.EMPTY,false)!=-1)throw new AssertionError("Unexplored source leaked overhang "+terrain);
            Dungeon.level.visited[source]=true;
            if(terrain!=Terrain.BARRICADE&&(int)visual.invoke(walls,top,Terrain.EMPTY,false)<0)throw new AssertionError("Known source overhang lost "+terrain);
        }
        Level.set(source,Terrain.WALL);Dungeon.level.visited[source]=Dungeon.level.visited[top]=true;
        Dungeon.level.heroFOV[top]=true;int visibleCap=(int)visual.invoke(walls,top,Terrain.EMPTY,false);
        Dungeon.level.heroFOV[top]=false;
        if(visibleCap<0 || (int)visual.invoke(walls,top,Terrain.EMPTY,false)!=visibleCap)throw new AssertionError("Remembered bottom wall lost its visible cap");
        Dungeon.level.visited[source]=Dungeon.level.mapped[source]=false;
        if((int)visual.invoke(walls,top,Terrain.EMPTY,false)!=-1)throw new AssertionError("Remembered room leaks unseen wall cap");
        Dungeon.level.heroFOV[top]=true;
        Level.set(top,Terrain.LOCKED_EXIT);Level.set(source,Terrain.EMPTY);
        Dungeon.level.heroFOV[source]=Dungeon.level.visited[source]=Dungeon.level.mapped[source]=false;
        if((int)visual.invoke(walls,top,Terrain.LOCKED_EXIT,false)!=com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet.EXIT_UNDERHANG)throw new AssertionError("Visible exit artwork depends on unknown neighbor");
        Level.set(top,Terrain.WALL);Level.set(source,Terrain.EMPTY);
        Dungeon.level.heroFOV[source]=true;
        int neighbor=top-1;
        Dungeon.level.heroFOV[neighbor]=Dungeon.level.visited[neighbor]=Dungeon.level.mapped[neighbor]=false;
        com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap terrainTiles=
                (com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap)RecoveryChecks.field(Game.scene(),"tiles");
        java.lang.reflect.Method face=terrainTiles.getClass().getDeclaredMethod("getTileVisual",int.class,int.class,boolean.class);face.setAccessible(true);
        Integer wallExpected=null,faceExpected=null;
        for(int hidden:new int[]{Terrain.WALL,Terrain.EMPTY,Terrain.WATER,Terrain.DOOR,Terrain.HIGH_GRASS}){
            Level.set(neighbor,hidden);
            int cap=(int)visual.invoke(walls,top,Terrain.WALL,false),front=(int)face.invoke(terrainTiles,top,Terrain.WALL,false);
            if(wallExpected==null){wallExpected=cap;faceExpected=front;}
            else if(cap!=wallExpected||front!=faceExpected)throw new AssertionError("Unknown neighbor leaks wall geometry: "+hidden);
        }
        walls.destroy();Level.set(top,Terrain.EMPTY);Level.set(source,Terrain.EMPTY);
        int vertical=Dungeon.hero.pos-2,horizontal=Dungeon.hero.pos+2;
        Level.set(vertical,Terrain.BARRICADE);Level.set(vertical-w,Terrain.WALL);Level.set(vertical+w,Terrain.WALL);
        Level.set(horizontal,Terrain.BARRICADE);Level.set(horizontal-1,Terrain.EMPTY);Level.set(horizontal+1,Terrain.WALL);
        Dungeon.observe();GameScene.updateMap();
    }
    private void menuEntry(String label){
        for(int tries=0;tries<100;tries++)if(playtestClickPage(label))return;
        throw new AssertionError("Unable to reach Playtest menu entry: "+label);
    }
    private int mysteryMenuStep,mysteryRememberedPage;
    private boolean mysteryMenuChecks(){
        // Each state gets genuine rendered frames before its screenshot.
        String potionName=com.shatteredpixel.shatteredpixeldungeon.messages.Messages.titleCase(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision().trueName());
        switch(mysteryMenuStep++){
            case 0:
                closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());
                menuEntry("Generation toggles");menuEntry("Artifacts");
                for(int page=0;page<2;page++)if(playtestButton(">")!=null && playtestButton(">").active)playtestClick(">");
                mysteryRememberedPage=SPDSettings.getInt("playtest_page_Generation: Artifacts",0);
                if(mysteryRememberedPage==0)throw new AssertionError("Artifact page fixture did not advance");break;
            case 1:
                interfaceBounds();capture("generation-artifacts-page");playtestClick("Main Playtest menu");menuEntry("Resume last menu");break;
            case 2:
                if(SPDSettings.getInt("playtest_page_Generation: Artifacts",0)!=mysteryRememberedPage || !allReviewText(Game.scene()).contains("Generation: Artifacts"))throw new AssertionError("Resume lost submenu or page");
                playtestClick("Main Playtest menu");menuEntry("Generation toggles");menuEntry("Items");menuEntry("Potions, brews and elixirs");menuEntry("ON: "+potionName);
                if(com.shatteredpixel.shatteredpixeldungeon.GenerationToggles.allowed(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision.class))throw new AssertionError("Generation toggle did not disable potion");break;
            case 3:
                interfaceBounds();capture("generation-potions");menuEntry("OFF: "+potionName);
                playtestClick("Main Playtest menu");menuEntry("Create items");menuEntry("Class items");break;
            case 4:
                interfaceBounds();capture("playtest-class-items");menuEntry("Focus Crystal");break;
            case 5:
                if(!allReviewText(Game.scene()).contains("Focus Crystal") || playtestButton("Create 1 (single item)")==null)throw new AssertionError("Focus Crystal creation is not in class items");
                closeReviewWindows();com.shatteredpixel.shatteredpixeldungeon.GenerationToggles.reset();
                System.out.println("PLAYTEST ORGANIZATION PASS: grouped item exclusions, native toggle input, class Crystal, persistent submenu page and main/resume navigation; failures=0");return true;
        }
        return false;
    }
    private int artworkStep;
    private com.shatteredpixel.shatteredpixeldungeon.ui.Window artworkOrigin;
    private Image artworkSource;
    private float artworkScaleX,artworkScaleY,artworkTime;
    private int artworkCharge,artworkBagSize;
    private com.watabou.utils.RectF artworkFrame;
    private boolean artworkPotionKnown;
    private boolean artworkPotionPreviouslyKnown;
    private com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision artworkPotion;
    private final com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal artworkCrystal=new com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal();
    private static final String[] ART_SUBJECTS={"item","unknown-potion","creature","plant","trap","terrain","talent","buff","journal","class","composite-tile","identified-potion","retained-talent"};

    /** Extend the existing native interface fixture with real mouse/touch artwork taps. */
    private boolean artworkReview() throws ReflectiveOperationException {
        com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal crystal=artworkCrystal;
        int subject=artworkStep/4,phase=artworkStep++%4;
        if(subject>=ART_SUBJECTS.length){
            System.out.println("ARTWORK UI PASS: 13 subjects, source-resolution exports/fallback, known consumable emblems in inspection and artwork, hidden unknown identities, native mouse/touch, modal return, independent scale, no turns/charges/identification changes; failures=0");
            return true;
        }
        if(phase==0){
            closeReviewWindows();
            artworkPotion=null;
            switch(subject){
                case 0:artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,crystal);break;
                case 1:
                    artworkPotionPreviouslyKnown=com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.getKnown().remove(com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision.class);
                    artworkPotion=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision();
                    artworkPotionKnown=artworkPotion.isIdentified();
                    if(artworkPotionKnown)throw new AssertionError("Unknown-potion fixture starts identified");
                    artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(artworkPotion);break;
                case 2:artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton());break;
                case 3:
                    com.shatteredpixel.shatteredpixeldungeon.plants.Plant plant=new com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass();
                    plant.pos=Dungeon.hero.pos;Dungeon.level.plants.put(plant.pos,plant);
                    artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoPlant(plant);break;
                case 4:
                    com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap().set(Dungeon.hero.pos).reveal();
                    Dungeon.level.traps.put(trap.pos,trap);
                    artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTrap(trap);break;
                case 5:artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(Dungeon.hero.pos);break;
                case 6:artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.WRENCH,1,null);break;
                case 7:artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoBuff(new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste());break;
                case 8:
                    com.shatteredpixel.shatteredpixeldungeon.journal.Notes.LandmarkRecord note=new com.shatteredpixel.shatteredpixeldungeon.journal.Notes.LandmarkRecord(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark.DISTANT_WELL,Dungeon.depth);
                    artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndJournalItem(note.icon(),note.title(),note.desc());break;
                case 9:artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo(HeroClass.NECROMANCER);break;
                case 11:artworkPotion=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision();artworkPotion.identify();artworkPotionKnown=artworkPotion.isIdentified();artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem(artworkPotion);break;
                case 12:artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.STRONGMAN,1,null);break;
                default:
                    Image composite=new Image("environment/custom_tiles/painted_sewer_exit.png",0,0,64,128);
                    composite.logicalSize(16,32);
                    artworkOrigin=new com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage(composite,"Gateway","A composite tile keeps its original frame.");
            }
            GameScene.show(artworkOrigin);
            artworkTime=Dungeon.hero.cooldown();artworkCharge=crystal.charges();artworkBagSize=Dungeon.hero.belongings.backpack.items.size();
        }else if(phase==1){
            interfaceBounds();
            capture("artwork-"+ART_SUBJECTS[subject]+"-description");
            Object button=findArtworkButton(artworkOrigin);
            if(button==null)throw new AssertionError("Missing artwork tap: "+ART_SUBJECTS[subject]);
            @SuppressWarnings("unchecked") java.util.function.Supplier<Image> supplier=(java.util.function.Supplier<Image>)RecoveryChecks.field(button,"artwork");
            artworkSource=supplier.get();artworkScaleX=artworkSource.scale.x;artworkScaleY=artworkSource.scale.y;artworkFrame=artworkSource.frame();
            if(subject==1||subject==11){
                Object titlebar=((com.watabou.noosa.Gizmo)button).parent;
                Image identity=(Image)RecoveryChecks.field(titlebar,"identity");
                if((identity!=null&&identity.visible)!=(subject==11))throw new AssertionError("Inspection identity disclosure mismatch");
                capture("inspection-"+ART_SUBJECTS[subject]);
            }
            if(artworkSource==null||artworkSource.texture==null)throw new AssertionError("Empty examination artwork");
            pointerGestureReview(button,Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);
        }else if(phase==2){
            com.shatteredpixel.shatteredpixeldungeon.windows.WndArtwork viewer=null;
            for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndArtwork)viewer=(com.shatteredpixel.shatteredpixeldungeon.windows.WndArtwork)child;
            if(viewer==null||artworkOrigin.parent==null)throw new AssertionError("Artwork modal did not preserve its description: "+ART_SUBJECTS[subject]+" source="+artworkSource.width+"x"+artworkSource.height+" origin="+artworkOrigin.parent+" viewer="+viewer);
            interfaceBounds();checkReviewText(viewer);
            Image large=(Image)RecoveryChecks.field(viewer,"artwork");
            if(subject==1||subject==11){
                int emblems=0;for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(viewer))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.ItemIdentityIcon)emblems++;
                if(emblems!=(subject==11?1:0))throw new AssertionError("Artwork identity disclosure mismatch");
            }
            if(large.scale==artworkSource.scale||large.width()<=artworkSource.width()*2||large.height()<=artworkSource.height()*2)throw new AssertionError("Artwork not independently enlarged");
            if(large.width()>viewer.camera.width||large.height()>viewer.camera.height)throw new AssertionError("Artwork clipped");
            if(subject!=5 && subject!=10 && (large.width<256||large.height<256))throw new AssertionError("Missing high-resolution source: "+ART_SUBJECTS[subject]);
            capture("artwork-"+ART_SUBJECTS[subject]);
            if(subject==10)viewer.onBackPressed();else playtestClick(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(viewer,"close"));
            if(RecoveryChecks.field(viewer,"ownedTexture")!=null)throw new AssertionError("Preview texture retained after closing");
        }else{
            com.watabou.utils.RectF frame=artworkSource.frame();
            if(artworkOrigin.parent==null || artworkSource.scale.x!=artworkScaleX || artworkSource.scale.y!=artworkScaleY || frame.left!=artworkFrame.left || frame.top!=artworkFrame.top || frame.right!=artworkFrame.right || frame.bottom!=artworkFrame.bottom)throw new AssertionError("Preview changed source/description");
            if(Dungeon.hero.cooldown()!=artworkTime || crystal.charges()!=artworkCharge || Dungeon.hero.belongings.backpack.items.size()!=artworkBagSize || (artworkPotion!=null && artworkPotion.isIdentified()!=artworkPotionKnown))throw new AssertionError("Preview consumed a turn/charge or identified an item");
            closeReviewWindows();
            if(subject==1 && artworkPotionPreviouslyKnown)artworkPotion.setKnown();
            if(subject==3)Dungeon.level.plants.remove(Dungeon.hero.pos);
            if(subject==4)Dungeon.level.traps.remove(Dungeon.hero.pos);
        }
        return false;
    }
    private Object findArtworkButton(Group group){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(group)){
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.ArtworkButton)return child;
            if(child instanceof Group){Object found=findArtworkButton((Group)child);if(found!=null)return found;}
        }
        return null;
    }
    private com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton playtestButton(String label){
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.Window)
            for(com.watabou.noosa.Gizmo button:RecoveryChecks.members((Group)child))
                if(button instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton && label.equals(((com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)button).text()))
                    return (com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)button;
        return null;
    }
    private void playtestClick(String label){
        Object button=playtestButton(label);if(button==null)throw new AssertionError("Playtest missing button: "+label+" at step "+playtestStep);
        pointerGestureReview(button,Boolean.getBoolean("grimhollow.interfacePortrait")?com.watabou.input.PointerEvent.NONE:com.watabou.input.PointerEvent.LEFT,0);
    }
    private String playtestSearchLabel;
    private boolean playtestSearchStarted;
    private boolean playtestClickPage(String label){
        if(!label.equals(playtestSearchLabel)){playtestSearchLabel=label;playtestSearchStarted=false;}
        if(playtestButton(label)!=null){playtestClick(label);playtestSearchLabel=null;playtestSearchStarted=false;return true;}
        String parent=null;
        if(label.equals("Fickle Doubloon") || label.equals("Golden Mimic companion") || label.equals("Wayward Chart")){
            if(playtestButton("Artifacts and trinkets")!=null)parent="Artifacts and trinkets";
            else if(label.equals("Golden Mimic companion") && playtestButton("Hatchling Mimic")!=null)parent="Hatchling Mimic";
        }else if(label.equals("Lurking Horror") && playtestButton("Grimhollow enemies")!=null)parent="Grimhollow enemies";
        else if(label.startsWith("Expedition ") || label.equals("Cavern remains loot")){if(playtestButton("Dragon expedition")!=null)parent="Dragon expedition";}
        else if(label.startsWith("Floor 21") && playtestButton("Halls")!=null)parent="Halls";
        if(parent!=null){playtestClick(parent);playtestSearchStarted=false;return false;}
        if(!playtestSearchStarted && playtestButton("<")!=null && playtestButton("<").active){playtestClick("<");return false;}
        playtestSearchStarted=true;
        if(playtestButton(">")!=null && playtestButton(">").active){playtestClick(">");return false;}
        throw new AssertionError("Playtest entry missing from paged menu: "+label);
    }
    private void playtestInput(String text,String action){
        for(com.watabou.noosa.Gizmo window:RecoveryChecks.members(Game.scene()))if(window instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput){
            ((com.watabou.noosa.TextInput)RecoveryChecks.field(window,"textBox")).setText(text);playtestClick(action);return;
        }
        throw new AssertionError("Playtest text field missing");
    }
    private void assertInspection(){
        if(!com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar.examineLocked())throw new AssertionError("Inspection lost its latch");
        boolean found=false;
        for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(Game.scene())){
            if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem)throw new AssertionError("Examine opened item actions");
            if(g instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem)found=true;
        }
        if(!found)throw new AssertionError("Expected item information window");interfaceBounds();
    }
    private boolean clickInventoryItem(Group group,com.shatteredpixel.shatteredpixeldungeon.items.Item item){
        for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(group)){
            if(g instanceof com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot
                    &&((com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot)g).item()==item){clickReview(g);return true;}
            if(g instanceof Group&&clickInventoryItem((Group)g,item))return true;
        }
        return false;
    }
    private void closeReviewWindows() {
        for(com.watabou.noosa.Gizmo child:new java.util.ArrayList<>(RecoveryChecks.members(Game.scene())))
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.Window)
                ((com.shatteredpixel.shatteredpixeldungeon.ui.Window)child).hide();
    }
    private void longItemInputReview() {
        closeReviewWindows();
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff();
        staff.enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vorpal());staff.identify();
        com.shatteredpixel.shatteredpixeldungeon.items.Item[] items={staff,new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern().identify(),new com.shatteredpixel.shatteredpixeldungeon.items.Phylactery().identify()};
        int overflow=0,oldStrength=Dungeon.hero.STR;float time=Dungeon.hero.cooldown();
        Dungeon.hero.STR=2;
        try {
            for(com.shatteredpixel.shatteredpixeldungeon.items.Item item:items){
                Dungeon.hero.belongings.backpack.items.add(item);
                com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem wnd=new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,item);GameScene.show(wnd);
                wnd.offset(Boolean.getBoolean("grimhollow.interfacePortrait")?0:-12,3);wnd.boundOffsetWithMargin(3);
                interfaceBounds();
                com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(wnd,"description");
                // Exercise a compact viewport as well as the naturally long artifact text.
                pane.setSize(pane.width(),Math.min(60,Math.max(20,pane.content().height()-20)));
                if(pane.content().height()>pane.height()+1){
                    overflow++;
                    pointerGestureReview(pane,com.watabou.input.PointerEvent.NONE,-60);
                    if(pane.content().camera.scroll.y<=0)throw new AssertionError("Item touch scroll did not move: "+item.name());
                    pane.scrollTo(0,0);
                    com.watabou.utils.Point at=pane.camera().cameraToScreen(pane.centerX(),pane.centerY());
                    com.watabou.input.ScrollEvent.addScrollEvent(new com.watabou.input.ScrollEvent(new com.watabou.utils.PointF(at.x,at.y),100));
                    com.watabou.input.ScrollEvent.processScrollEvents();
                    if(Math.abs(pane.content().camera.scroll.y-(pane.content().height()-pane.height()))>.1f)throw new AssertionError("Item mouse wheel cannot reach last line: "+item.name());
                    pane.scrollTo(0,0);pane.update();
                    com.shatteredpixel.shatteredpixeldungeon.ui.IconButton down=(com.shatteredpixel.shatteredpixeldungeon.ui.IconButton)RecoveryChecks.field(pane,"down");
                    for(int clicks=0;down.active&&clicks<30;clicks++)pointerGestureReview(down,com.watabou.input.PointerEvent.NONE,0);
                    if(down.active||Math.abs(pane.content().camera.scroll.y-(pane.content().height()-pane.height()))>.1f)throw new AssertionError("Description down control did not reach end");
                    com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock text=(com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)RecoveryChecks.members(pane.content()).get(0);
                    com.watabou.noosa.RenderedText last=null;
                    for(com.watabou.noosa.Gizmo word:RecoveryChecks.members(text))if(word instanceof com.watabou.noosa.RenderedText)last=(com.watabou.noosa.RenderedText)word;
                    if(last==null||last.camera()!=pane.content().camera||last.y+last.height()>pane.content().camera.scroll.y+pane.height()-.9f)throw new AssertionError("Description final glyph is clipped or uses stale camera");
                }
                for(com.watabou.noosa.Gizmo g:RecoveryChecks.members(wnd))if(g instanceof com.shatteredpixel.shatteredpixeldungeon.ui.RedButton){
                    com.shatteredpixel.shatteredpixeldungeon.ui.RedButton button=(com.shatteredpixel.shatteredpixeldungeon.ui.RedButton)g;
                    if(button.top()<pane.bottom()||button.bottom()>wnd.camera().height)throw new AssertionError("Item action clipped/overlapped by description");
                }
                if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern){
                    String text=item.info();
                    if(!text.contains("Alchemy feeds the ember")||text.contains("Levels 1-6")||text.contains("\u00e2")||text.contains("%1$"))throw new AssertionError("Lantern mystery/encoding regression");
                }
                wnd.hide();Dungeon.hero.belongings.backpack.items.remove(item);
            }
            if(overflow!=items.length)throw new AssertionError("Long-item fixture did not exercise every compact viewport: "+overflow);
            if(Dungeon.hero.cooldown()!=time)throw new AssertionError("Reading item description spent a turn");
        } finally {Dungeon.hero.STR=oldStrength;}
        System.out.println("LONG ITEM INPUT PASS: Vorpal Quarterstaff, Ashlight and Phylactery; real touch drag and mouse wheel to last line; action/footer bounds; Lantern text; failures=0");
    }

    private int descriptionReviewFrames;
    private com.shatteredpixel.shatteredpixeldungeon.items.Item descriptionItem;
    private com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem descriptionWindow;
    private boolean descriptionReviewTick(){
        int tick=descriptionReviewFrames++,kind=tick/24,phase=tick%24;
        if(kind>=3)return true;
        String name=kind==0?"vorpal":kind==1?"ashlight":"phylactery";
        if(phase==0){
            descriptionItem=kind==0?new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff():kind==1?new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AshlightLantern():new com.shatteredpixel.shatteredpixeldungeon.items.Phylactery();
            if(kind==0)((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff)descriptionItem).enchant(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Vorpal());
            descriptionItem.identify();Dungeon.hero.belongings.backpack.items.add(descriptionItem);
            descriptionWindow=new com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem(null,descriptionItem);GameScene.show(descriptionWindow);
        } else if(phase==8){interfaceBounds();capture(name+"-description-top");}
        else if(phase==9){
            com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)RecoveryChecks.field(descriptionWindow,"description");
            com.watabou.utils.Point at=pane.camera().cameraToScreen(pane.centerX(),pane.centerY());
            com.watabou.input.ScrollEvent.addScrollEvent(new com.watabou.input.ScrollEvent(new com.watabou.utils.PointF(at.x,at.y),100));com.watabou.input.ScrollEvent.processScrollEvents();
        }else if(phase==16){interfaceBounds();capture(name+"-description-bottom");}
        else if(phase==23){descriptionWindow.hide();Dungeon.hero.belongings.backpack.items.remove(descriptionItem);}
        return false;
    }

    private void hudKeysReview(com.shatteredpixel.shatteredpixeldungeon.ui.MenuPane menu){
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.journal.Notes.storeInBundle(saved);
        int depth=Dungeon.depth,branch=Dungeon.branch;
        try {
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.reset();
            com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey iron=new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(depth);
            iron.quantity(2);iron.doPickUp(Dungeon.hero,Dungeon.hero.pos);
            com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey gold=new com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey(depth);gold.quantity(3);
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(gold);
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(new com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey(depth));
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(new com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey(depth));
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(depth-1));
            menu.updateKeys();
            com.shatteredpixel.shatteredpixeldungeon.ui.KeyDisplay keys=(com.shatteredpixel.shatteredpixeldungeon.ui.KeyDisplay)RecoveryChecks.field(menu,"keys");
            java.util.List<?> icons=(java.util.List<?>)RecoveryChecks.field(keys,"icons");
            if(keys.keyCount()!=8||icons.size()!=5||!keys.visible||keys.left()<0||keys.right()>keys.camera().width)throw new AssertionError("HUD key count/types/bounds");
            com.watabou.noosa.Visual bg=(com.watabou.noosa.Visual)RecoveryChecks.field(menu,"bg");
            if(keys.top()<bg.y+bg.height())throw new AssertionError("Keys overlap version/menu row");
            for(Object object:icons){Image icon=(Image)object;if(Math.max(icon.width(),icon.height())<8||icon.y<keys.top()||icon.y+icon.height()>keys.bottom())throw new AssertionError("Key glyph too small/clipped");}
            com.watabou.noosa.ui.Component danger=(com.watabou.noosa.ui.Component)RecoveryChecks.field(menu,"danger");
            if(danger.top()<keys.bottom())throw new AssertionError("Enemy counter overlaps keys");
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.remove(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(depth));menu.updateKeys();
            if(keys.keyCount()!=7)throw new AssertionError("HUD key consumption did not update");
            com.watabou.utils.Bundle reload=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.journal.Notes.storeInBundle(reload);
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.reset();com.shatteredpixel.shatteredpixeldungeon.journal.Notes.restoreFromBundle(reload);menu.updateKeys();
            if(keys.keyCount()!=7)throw new AssertionError("HUD key reload lost counts");
            Dungeon.depth++;menu.updateKeys();if(keys.keyCount()!=1)throw new AssertionError("Past-floor keys must collapse to one reminder");
            Dungeon.depth=depth;Dungeon.branch=1;menu.updateKeys();if(keys.keyCount()!=1)throw new AssertionError("Main-floor keys shown as usable inside branch");
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.reset();menu.updateKeys();if(keys.visible)throw new AssertionError("Empty key row remains visible");
        } finally {Dungeon.depth=depth;Dungeon.branch=branch;com.shatteredpixel.shatteredpixeldungeon.journal.Notes.restoreFromBundle(saved);menu.updateKeys();}
        System.out.println("HUD KEYS PASS: actual pickup, all four key types/counts, separate row, consumption, save/load, floor/branch rules and empty state; failures=0");
    }

    private void scrollReview(Group group) {
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(group)) {
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)
                ((com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)child).scrollTo(0,10000);
            else if(child instanceof Group)scrollReview((Group)child);
        }
    }
    private void interfaceBounds() {
        if(Boolean.getBoolean("grimhollow.interfacePortrait") && Gdx.graphics.getWidth()>=Gdx.graphics.getHeight())
            throw new AssertionError("Portrait review requires a tall render surface");
        boolean found=false;
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(Game.scene()))if(child instanceof com.shatteredpixel.shatteredpixeldungeon.ui.Window) {
            found=true;Camera camera=((com.shatteredpixel.shatteredpixeldungeon.ui.Window)child).camera();
            if(camera.x<0||camera.y<0||camera.x+camera.width*camera.zoom>Gdx.graphics.getWidth()+1
                    ||camera.y+camera.height*camera.zoom>Gdx.graphics.getHeight()+1)
                throw new AssertionError("Interface window clipped: "+child.getClass().getSimpleName()+" x="+camera.x+" y="+camera.y+" size="+camera.width+"x"+camera.height+" zoom="+camera.zoom);
            if(child instanceof com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem)
                for(com.watabou.noosa.Gizmo part:RecoveryChecks.members((Group)child))
                    if(part instanceof com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane) {
                        com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane pane=(com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane)part;
                        com.watabou.utils.Point at=pane.camera().cameraToScreen(pane.left(),pane.top());
                        Camera content=pane.content().camera;
                        if(content.x!=at.x||content.y!=at.y)throw new AssertionError("Item scroll camera detached from moved window");
                    }
        }
        if(!found)throw new AssertionError("Interface review window missing");
        interfaceTabs(Game.scene());
    }
    private void interfaceTabs(Group group) {
        for(com.watabou.noosa.Gizmo child:RecoveryChecks.members(group))if(child instanceof Group) {
            for(Class<?> type=child.getClass();type!=null;type=type.getSuperclass())if(type.getName().endsWith("WndTabbed$IconTab")) {
                try {
                    java.lang.reflect.Field field=type.getDeclaredField("icon");field.setAccessible(true);
                    Image icon=(Image)field.get(child);
                    if(icon.width()>24||icon.height()>24)throw new AssertionError("Tab icon reverted to texture dimensions");
                }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
            }
            interfaceTabs((Group)child);
        }
    }

    /** Exercise real movement/combat with mobs retained, using an isolated save home. */
    private void encounterTick() {
        if(!(Game.scene() instanceof GameScene)||frames<240)return;
        if(frames>36000)throw new AssertionError("Encounter replay stalled");
        // The actor deliberately kills the hero after the replay. Wait for the
        // whole callback, rather than observing HP=0 halfway through die().
        if(encounterDeathScheduled&&!encounterDeath)return;
        if(encounterDeath) {
            if(++encounterDeathFrames<120)return;
            if(Dungeon.hero.isAlive()||!encounterDrops||!encounterSummon)throw new AssertionError("Incomplete encounter/death coverage");
            System.out.println("ENCOUNTERS actions="+encounterActions+" steps="+encounterSteps+" attacks="+encounterAttacks
                    +" actorDrops=true actorPickup=true summon=true deathFrames="+encounterDeathFrames+" failures=0");
            Gdx.app.exit();return;
        }
        if(!Dungeon.hero.isAlive())throw new AssertionError("Encounter hero died after "+encounterActions+" actions");
        if(!Dungeon.hero.ready||Dungeon.hero.sprite.isMoving||frames%6!=0)return;
        Level level=Dungeon.level;
        if(encounterVisits==null) {
            encounterVisits=new int[level.length()];
            encounterLastCell=Dungeon.hero.pos;
            System.out.println("ENCOUNTER START class="+Dungeon.hero.heroClass+" seed="+Dungeon.seed+" pos="+Dungeon.hero.pos);
            if(Boolean.getBoolean("grimhollow.encounterFixture")) {
                // Extra health is confined to this opt-in renderer fixture, not normal play.
                Dungeon.hero.HT=Dungeon.hero.HP=1000;
                System.out.println("ENCOUNTER FIXTURE health=1000; terrain and hostile AI retained");
                com.shatteredpixel.shatteredpixeldungeon.actors.Actor.add(new com.shatteredpixel.shatteredpixeldungeon.actors.Actor() {
                    { actPriority=VFX_PRIO; }
                    @Override protected boolean act() {
                        for(int offset:com.watabou.utils.PathFinder.NEIGHBOURS8) {
                            int cell=Dungeon.hero.pos+offset;
                            if(level.insideMap(cell)&&level.passable[cell]&&!level.avoid[cell]&&level.findMob(cell)==null
                                    &&!level.heaps.containsKey(cell)&&cell!=Dungeon.hero.pos) {
                                com.shatteredpixel.shatteredpixeldungeon.items.Heap heap=level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.food.Food(),cell);
                                heap.drop(new com.shatteredpixel.shatteredpixeldungeon.items.Gold());
                                // Removing the top item changes the already uploaded atlas frame.
                                if(heap.pickUp()==null)throw new AssertionError("Actor pickup failed");
                                // Key pickup must also rebuild its text count safely off this actor thread.
                                com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey key=new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(Dungeon.depth);
                                key.quantity(2);if(!key.doPickUp(Dungeon.hero,cell))throw new AssertionError("Actor key pickup failed");
                                encounterDrops=true;
                                encounterSummon=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.NecroSkeleton.raise(cell,false,false)!=null;
                                break;
                            }
                        }
                        com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(this);return true;
                    }
                });
            }
        }
        if(encounterLastCell!=Dungeon.hero.pos) {encounterSteps++;encounterLastCell=Dungeon.hero.pos;}
        encounterVisits[Dungeon.hero.pos]++;
        if(encounterActions>=150) {
            if(encounterSteps<50||encounterAttacks==0)throw new AssertionError("Insufficient live encounter coverage");
            if(!Boolean.getBoolean("grimhollow.encounterFixture")) {
                System.out.println("ENCOUNTER REPLAY actions="+encounterActions+" steps="+encounterSteps+" attacks="+encounterAttacks+" failures=0");
                Gdx.app.exit();return;
            }
            com.shatteredpixel.shatteredpixeldungeon.ui.MenuPane menu=(com.shatteredpixel.shatteredpixeldungeon.ui.MenuPane)RecoveryChecks.field(Game.scene(),"menu");
            com.shatteredpixel.shatteredpixeldungeon.ui.KeyDisplay keys=(com.shatteredpixel.shatteredpixeldungeon.ui.KeyDisplay)RecoveryChecks.field(menu,"keys");
            if(keys.keyCount()<2||!keys.visible)throw new AssertionError("Actor pickup did not reach rendered key HUD");
            System.out.println("ACTOR KEY HUD PASS: pickup on gameplay thread, count labels on render thread; failures=0");
            encounterDeathScheduled=true;
            com.shatteredpixel.shatteredpixeldungeon.actors.Actor.add(new com.shatteredpixel.shatteredpixeldungeon.actors.Actor() {
                { actPriority=VFX_PRIO; }
                @Override protected boolean act() {
                    Dungeon.hero.HP=0;Dungeon.hero.die(this);
                    encounterDeath=true;com.shatteredpixel.shatteredpixeldungeon.actors.Actor.remove(this);return true;
                }
            });
            Dungeon.hero.rest(false);return;
        }
        int destination=-1;
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:level.mobs)
            if(mob.isAlive()&&mob.alignment==com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY
                    &&level.heroFOV[mob.pos]&&level.adjacent(Dungeon.hero.pos,mob.pos)) {
                destination=mob.pos;encounterAttacks++;break;
            }
        if(destination<0) {
            int[] previous=new int[level.length()];java.util.Arrays.fill(previous,-1);
            java.util.ArrayDeque<Integer> queue=new java.util.ArrayDeque<>();
            int start=Dungeon.hero.pos;previous[start]=start;queue.add(start);
            int best=-1,bestVisits=Integer.MAX_VALUE;
            while(!queue.isEmpty()) {
                int cell=queue.remove();
                if(cell!=start&&encounterVisits[cell]<bestVisits) {best=cell;bestVisits=encounterVisits[cell];if(bestVisits==0)break;}
                for(int offset:com.watabou.utils.PathFinder.NEIGHBOURS8) {
                    int next=cell+offset;
                    if(level.insideMap(next)&&previous[next]<0&&(level.passable[next]||level.map[next]==Terrain.SECRET_DOOR)&&!level.avoid[next]
                            &&!level.traps.containsKey(next)&&level.findMob(next)==null
                            &&(!level.heaps.containsKey(next)||level.heaps.get(next).type==com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.HEAP)
                            &&level.map[next]!=Terrain.EXIT) {previous[next]=cell;queue.add(next);}
                }
            }
            if(best>=0) {while(previous[best]!=start)best=previous[best];destination=best;}
        }
        if(!Boolean.getBoolean("grimhollow.encounterFixture")||encounterActions%25==0)
            System.out.println("ENCOUNTER action="+encounterActions+" pos="+Dungeon.hero.pos+" to="+destination+" hp="+Dungeon.hero.HP);
        encounterActions++;
        if(destination<0)Dungeon.hero.rest(false);
        else if(level.map[destination]==Terrain.SECRET_DOOR)Dungeon.hero.search(true);
        else if(Dungeon.hero.handle(destination))Dungeon.hero.next();
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
            com.badlogic.gdx.utils.JsonValue semantics=new com.badlogic.gdx.utils.JsonReader().parse(
                    Gdx.files.local("desktop/src/test/resources/item-semantics.json"));
            com.badlogic.gdx.utils.JsonValue painted=new com.badlogic.gdx.utils.JsonReader().parse(Gdx.files.internal("painted-assets.json")).get("assets");
            int sharpAtlases=0;
            for(com.badlogic.gdx.utils.JsonValue asset:painted){
                if(!asset.has("painted_rects"))continue;
                com.watabou.gltextures.SmartTexture texture=com.watabou.gltextures.TextureCache.get(asset.name);
                if(GameGeometry.characterDensity(asset.name)!=8||GameGeometry.characterDensity(texture)!=8)
                    failures.add("56 inconsistent creature density "+asset.name);
                if(texture.width>4096||texture.height>4096)failures.add("56 atlas exceeds mobile texture budget "+asset.name);
                sharpAtlases++;
            }
            System.out.println("TEST 56 CREATURES: eightfold source density atlases="+sharpAtlases+" maximum texture dimension=4096 failures="+failures.size());
            HeroClass original=Dungeon.hero.heroClass;
            com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite originalSprite=Dungeon.hero.sprite;
            for(HeroClass hero:HeroClass.values()) {
                Dungeon.hero.heroClass=hero;
                com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
                band(sprite,20,.85f,.95f,buffer,camera,zoom,failures,"24 "+hero);heroes++;sprite.destroy();
                // All armor rows and action frames must survive the higher
                // resolution hero atlas, including save portraits/reflections.
                for(int tier=0;tier<8;tier++)for(int pose=0;pose<21;pose++) {
                    Image part=GameGeometry.heroImage(hero.spritesheet(),pose*12,tier*15,12,15);
                    if(part.width()!=12||part.height()!=15||GameGeometry.opaqueHeight(part.texture,part.frame())==0)
                        failures.add("24 hero atlas "+hero+" tier="+tier+" pose="+pose);
                    part.destroy();
                }
                com.watabou.noosa.Image avatar=com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.avatar(hero,6);
                band(avatar,28,.85f,.95f,buffer,camera,zoom,failures,"24 avatar "+hero);avatar.destroy();
                GamesInProgress.set(99);
                com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton slot=new com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene.SaveSlotButton();slot.setRect(0,0,160,28);slot.set(99);
                if(slot.portrait()==null)failures.add("24 missing save portrait "+hero);else band(slot.portrait(),16,.85f,.95f,buffer,camera,zoom,failures,"24 save "+hero);slot.destroy();
                com.watabou.noosa.Image splash=new com.watabou.noosa.Image(hero.splashArt());
                if(GameGeometry.opaqueHeight(splash.texture,splash.frame())==0)failures.add("36 empty splash "+hero);
                if(hero.shortDesc().contains("!!!"))failures.add("36 missing description "+hero);splash.destroy();
            }
            Dungeon.hero.heroClass=original;Dungeon.hero.sprite=originalSprite;
            heroEquipmentChecks(buffer,camera,failures);
            hasteVisualChecks(buffer,camera,failures);
            grassDepthChecks(failures);
            java.io.File jar=new java.io.File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            try(java.util.jar.JarFile classes=new java.util.jar.JarFile(jar)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries=classes.entries();
                while(entries.hasMoreElements()) {
                    String name=entries.nextElement().getName();
                    boolean outsideSprite=java.util.Arrays.stream(new String[]{"SpiritHawk$HawkSprite.class","SmokeBomb$NinjaLogSprite.class",
                            "Necromancer$NecroSkeleton$NecroSkeletonSprite.class","GuardianTrap$GuardianSprite.class",
                            "ShadowClone$ShadowSprite.class","PowerOfMany$LightAllySprite.class","Feint$AfterImage$AfterImageSprite.class",
                            "SurfaceScene$Pet.class"}).anyMatch(name::endsWith);
                    if((!name.startsWith("com/shatteredpixel/shatteredpixeldungeon/sprites/")&&!outsideSprite)||!name.endsWith(".class"))continue;
                    Class<?> type=Class.forName(name.substring(0,name.length()-6).replace('/','.'));
                    if(!com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class.isAssignableFrom(type)
                            || type==com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite.class
                            || java.lang.reflect.Modifier.isAbstract(type.getModifiers()))continue;
                    java.lang.reflect.Constructor<?> constructor=type.getDeclaredConstructor();constructor.setAccessible(true);
                    com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite=(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite)constructor.newInstance();
                    if(sprite.texture==null)continue; // Base sprite classes have no art or frame.
                    float footprint=sprite.visualFootprint();
                    paintedAnimations(sprite,painted,failures);
                    steadyIdle(sprite,failures);
                    if(sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite) {
                        for(int tier=0;tier<=5;tier++) {
                            ((com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite)sprite).setArmor(tier);
                            paintedAnimations(sprite,painted,failures);
                        }
                        ((com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite)sprite).setArmor(0);
                    }
                    if(sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.MimicSprite) {
                        // Test its concealed frame against ordinary chest height, separately
                        // from the larger open-mouthed attack animation.
                        java.lang.reflect.Field hidden=com.shatteredpixel.shatteredpixeldungeon.sprites.MimicSprite.class.getDeclaredField("advancedHiding");
                        hidden.setAccessible(true);sprite.play((com.watabou.noosa.MovieClip.Animation)hidden.get(sprite));
                    }
                    band(sprite,footprint,.85f,.95f,buffer,camera,zoom,failures,"24 "+type.getSimpleName());mobs++;sprite.destroy();
                }
            }
            System.out.println("TEST 24: heroes="+heroes+" mob sprites="+mobs+" steady idle checks="+mobs+" failures="+failures.size());
            com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite heroScale=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
            com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite ratScale=new com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite();
            float ratio=heroScale.visibleBounds().height()/ratScale.visibleBounds().height();
            if(ratio<2||ratio>2.5f)failures.add("Hero/rat visible size ratio outside 2..2.5: "+ratio);
            heroScale.destroy();ratScale.destroy();
            int statusCount=0;
            for(int index=0;index<89;index++) for(boolean large:new boolean[]{false,true}) {
                com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon status=new com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon(index,large);
                if(status.width()!=(large?16:7)||status.height()!=(large?16:7)||GameGeometry.opaqueHeight(status.texture,status.frame())==0)
                    failures.add("Empty/misscaled painted status "+index+" large="+large);
                Pixmap pixels=renderSprite(status,buffer,camera);pixels.dispose();status.destroy();statusCount++;
            }
            System.out.println("PAINTED: complete animation coverage; hero/rat height ratio="+ratio+" status sizes rendered="+statusCount);
            int before=failures.size();
            for(java.lang.reflect.Field field:com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.class.getFields()) {
                if(field.getType()!=int.class||field.getName().equals("SIZE"))continue;
                int index=field.getInt(null);if(index<0)continue;
                com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(index);
                if(GameGeometry.opaqueHeight(sprite.texture,sprite.frame())==0){failures.add("25 empty "+field.getName());continue;}
                band(sprite,16,.45f,.55f,buffer,camera,zoom,failures,"25 "+field.getName());
                com.badlogic.gdx.utils.JsonValue expected=semantics.get("items").get(field.getName());
                semanticItem(sprite,index,expected,field.getName(),failures);
                exactRectangle(sprite,expected.getInt("artIndex"),com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.SIZE,buffer,camera,zoom,failures,field.getName());items++;sprite.destroy();
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
            java.util.Set<String> identitySources=new java.util.HashSet<>(),identityPixels=new java.util.HashSet<>();
            for(java.lang.reflect.Field field:com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.class.getFields()) {
                if(field.getType()!=int.class||field.getName().equals("SIZE"))continue;
                int index=field.getInt(null);if(index<0)continue;
                com.watabou.noosa.Image icon=new com.watabou.noosa.Image(Assets.Sprites.ITEM_ICONS);
                icon.frame(com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.film.get(index));
                com.badlogic.gdx.utils.JsonValue expected=semantics.get("icons").get(field.getName());
                if(!identitySources.add(expected.getString("sourcePainting"))||!identityPixels.add(expected.getString("rgbaSha256")))
                    failures.add("25 duplicate identity painting: "+field.getName());
                semanticItem(icon,index,expected,"icon "+field.getName(),failures);
                exactRectangle(icon,expected.getInt("artIndex"),32,buffer,camera,zoom,failures,"icon "+field.getName());icons++;icon.destroy();
            }
            if(items!=semantics.get("items").size||icons!=semantics.get("icons").size)failures.add("25 incomplete named atlas inventory");
            Image serpent=com.shatteredpixel.shatteredpixeldungeon.ui.Icons.SNAKE.get();
            if(serpent.texture!=com.watabou.gltextures.TextureCache.get("interfaces/painted_snake.png")
                    ||serpent.texture.width!=64||serpent.texture.height!=64||GameGeometry.opaqueHeight(serpent.texture,serpent.frame())==0)
                failures.add("25 guide serpent needs its dedicated painting");
            serpent.destroy();
            namedItems(semantics,failures);
            java.util.Set<String> trapPixels=new java.util.HashSet<>();
            for(int shape=0;shape<7;shape++)for(int color=0;color<9;color++){
                com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap trap=new com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap();
                trap.shape=shape;trap.color=color;trap.active=color!=8;trap.visible=true;
                Image visual=com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap.getTrapVisual(trap);
                com.watabou.utils.RectF uv=visual.frame();
                int x=color*64,y=shape*64;
                // DungeonTilemap samples texel centers, with a half-texel guard
                // on all four edges. Assert that exact contract, not raw cell edges.
                if(uv.left*visual.texture.width!=x+.5f||uv.top*visual.texture.height!=y+.5f
                        ||uv.right*visual.texture.width!=x+63.5f||uv.bottom*visual.texture.height!=y+63.5f
                        ||visual.width()!=16||visual.height()!=16||GameGeometry.opaqueHeight(visual.texture,uv)==0)
                    failures.add("25 trap index "+shape+":"+color);
                java.security.MessageDigest hash=java.security.MessageDigest.getInstance("SHA-256");
                for(int yy=0;yy<64;yy++)for(int xx=0;xx<64;xx++){
                    int pixel=visual.texture.bitmap.getPixel(x+xx,y+yy);
                    for(int shift=0;shift<32;shift+=8)hash.update((byte)(pixel>>>shift));
                }
                trapPixels.add(java.util.Arrays.toString(hash.digest()));visual.destroy();
            }
            if(trapPixels.size()!=63)failures.add("25 duplicate or missing painted trap states");
            System.out.println("PAINTED TRAPS: shapes=7 color/state combinations="+trapPixels.size());
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
                int expected=64;
                if(Math.round(icon.frame().width()*icon.texture.width)!=expected||icon.width()!=16||GameGeometry.opaqueHeight(icon.texture,icon.frame())==0)failures.add("36 empty or mis-scaled talent "+talent);
                Pixmap drawn=renderSprite(icon,buffer,camera);drawn.dispose();icon.destroy();
            }
            int selectors=0;
            Class<?> selectorsType=com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.class;
            for(java.lang.reflect.Field field:selectorsType.getFields()){
                if(field.getDeclaringClass()!=selectorsType||field.getType()!=int.class||field.getName().equals("NONE")||field.getName().equals("SPELL_ACTION_OFFSET"))continue;
                int id=field.getInt(null);
                for(int variant=0;variant<(id>=40&&id<=66?2:1);variant++){
                    com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon icon=new com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon(id+variant*32);
                    if(Math.round(icon.frame().width()*icon.texture.width)!=64||icon.width()!=16||icon.height()!=16||GameGeometry.opaqueHeight(icon.texture,icon.frame())==0)failures.add("36 empty/mis-scaled hero selector "+field.getName()+":"+variant);
                    Pixmap drawn=renderSprite(icon,buffer,camera);drawn.dispose();icon.destroy();selectors++;
                }
            }
            System.out.println("PAINTED HERO SELECTORS: subclass, armor, Cleric and action symbols="+selectors);
            paintedSkillsAndPlants(buffer,camera,failures);
            rankAndKeyChecks(buffer,camera,failures);
            System.out.println("TEST 36: nine splashes, descriptions, portraits, all talents and ItemSlots failures="+(failures.size()-before));
            saveCompatibility(failures);
            System.out.println("TEST 35 RETIRED — superseded by recovery test 46 handler/network checks");
            buffer.dispose();for(String failure:failures)System.out.println("FAIL "+failure);
            if(!failures.isEmpty())throw new AssertionError("Rendering acceptance failures="+failures.size());
            System.out.println("TESTS 24-26, 34, 36 PASS; test 35 retired by recovery");
        }catch(Exception e){throw new RuntimeException(e);}
    }
    private void grassDepthChecks(java.util.List<String> failures)throws Exception {
        Class<?> type=com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap.class;
        java.lang.reflect.Field behindField=type.getDeclaredField("behindCells"),nextField=type.getDeclaredField("next");
        behindField.setAccessible(true);nextField.setAccessible(true);
        java.util.Set<Integer> behind=(java.util.Set<Integer>)behindField.get(null),next=(java.util.Set<Integer>)nextField.get(null);
        java.util.Set<Integer> savedBehind=new java.util.HashSet<>(behind),savedSkip=new java.util.HashSet<>(com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap.skipCells);
        java.lang.reflect.Method protect=type.getDeclaredMethod("protectUpperBody",com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class);
        java.lang.reflect.Method visual=type.getDeclaredMethod("getTileVisual",int.class,int.class,boolean.class);
        protect.setAccessible(true);visual.setAccessible(true);
        HeroClass original=Dungeon.hero.heroClass;com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite originalSprite=Dungeon.hero.sprite;
        java.util.Map<Integer,Integer> terrain=new java.util.HashMap<>();
        int cell=Dungeon.hero.pos,width=Dungeon.level.width(),checks=0;
        for(int dy=-2;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){int p=cell+dy*width+dx;if(p>=0&&p<Dungeon.level.length())terrain.put(p,Dungeon.level.map[p]);}
        com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap back=new com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap(true),front=new com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap();
        try {
            for(HeroClass hero:HeroClass.values()){
                Dungeon.hero.heroClass=hero;
                com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
                for(int state:new int[]{Terrain.HIGH_GRASS,Terrain.FURROWED_GRASS}){
                    for(int p:terrain.keySet())Dungeon.level.map[p]=state;
                    for(float offset:new float[]{-4,0,4}){
                        sprite.place(cell);sprite.x+=offset;sprite.y+=offset;
                        next.clear();protect.invoke(null,sprite);behind.clear();behind.addAll(next);
                        for(int p:terrain.keySet()){
                            int a=(Integer)visual.invoke(back,p,state,false),b=(Integer)visual.invoke(front,p,state,false);
                            if((a>=0)==(b>=0))failures.add("24 grass omitted/duplicated "+hero+" cell="+p);
                        }
                        if(offset==0){
                            if((Integer)visual.invoke(front,cell-width,state,false)>=0)failures.add("24 grass covers head "+hero);
                            if((Integer)visual.invoke(front,cell,state,false)<0)failures.add("24 grass missing at feet "+hero);
                        }
                        checks++;
                    }
                }
                sprite.destroy();
            }
        }finally{
            for(java.util.Map.Entry<Integer,Integer> e:terrain.entrySet())Dungeon.level.map[e.getKey()]=e.getValue();
            Dungeon.hero.heroClass=original;Dungeon.hero.sprite=originalSprite;
            behind.clear();behind.addAll(savedBehind);next.clear();
            com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap.skipCells.clear();com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap.skipCells.addAll(savedSkip);
            back.destroy();front.destroy();
        }
        System.out.println("GRASS DEPTH: nine heroes, standing/rustled, movement offsets, head/feet partition checks="+checks+" failures="+failures.size());
    }
    private void heroEquipmentChecks(com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,Camera camera,java.util.List<String> failures)throws Exception {
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero=Dungeon.hero;
        HeroClass original=hero.heroClass;com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite originalSprite=hero.sprite;
        com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon originalWeapon=hero.belongings.weapon,second=hero.belongings.abilityWeapon;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor originalArmor=hero.belongings.armor;
        com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon[] weapons={null,
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff(),
                new com.shatteredpixel.shatteredpixeldungeon.items.FocusCrystal(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneRod(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WarScythe(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Whip(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BattleAxe(),
                new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail()};
        com.badlogic.gdx.utils.JsonValue anchors=new com.badlogic.gdx.utils.JsonReader().parse(Gdx.files.internal("sprites/hero-grips.json"));
        int checks=0;
        try {
            for(com.badlogic.gdx.utils.JsonValue entry:anchors){hero.heroClass=HeroClass.valueOf(entry.name.toUpperCase(java.util.Locale.ROOT));
                for(int tier=0;tier<8;tier++){
                    hero.belongings.armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor();
                    hero.belongings.armor.tier=tier;
                    com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
                    long[][] genericPixels=new long[2][21];
                    for(com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon item:weapons){hero.belongings.weapon=item;
                        for(boolean flip:new boolean[]{false,true})for(int pose:new int[]{0,2,5,13,14,15}){
                            sprite.flipHorizontal=flip;sprite.frame(sprite.texture.uvRect(pose*96,tier*120,(pose+1)*96,(tier+1)*120));
                            Pixmap pixels=renderSprite(sprite,buffer,camera);Image held=sprite.displayedEquipment();
                            // Per-item grip tests are retired: the user requested generic painted poses.
                            // Exercise every loadout/pose anyway and reject any floating item overlay.
                            if(held!=null)failures.add("24 generic hero still draws an inventory weapon "+entry.name+" "+item);
                            java.util.zip.CRC32 pixelHash=new java.util.zip.CRC32();pixelHash.update(pixels.getPixels().duplicate());
                            if(item==null)genericPixels[flip?1:0][pose]=pixelHash.getValue();
                            else if(pixelHash.getValue()!=genericPixels[flip?1:0][pose])failures.add("24 generic pose retains weapon pixels "+entry.name+" "+item);
                            if(!flip&&pose==0&&(tier==1||tier==5)){String label=item==null?"empty":item.getClass().getSimpleName();
                                PixmapIO.writePNG(Gdx.files.absolute("verification/heroes/pilot/weapons/"+entry.name+"-"+tier+"-"+label+".png"),pixels,-1,true);}
                            if(hero.heroClass==HeroClass.NECROMANCER&&tier==1&&(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BoneRod))
                                PixmapIO.writePNG(Gdx.files.absolute("verification/heroes/pilot/weapons/necromancer-"+item.getClass().getSimpleName()+"-pose"+pose+"-"+(flip?"left":"right")+".png"),pixels,-1,true);
                            if(hero.heroClass==HeroClass.PSYCHIC&&tier==1&&!flip&&pose==0&&item!=null&&java.util.Arrays.asList("Whip","BattleAxe","Flail").contains(item.getClass().getSimpleName()))
                                PixmapIO.writePNG(Gdx.files.absolute("verification/interface/generic-"+item.getClass().getSimpleName()+".png"),pixels,-1,true);
                            pixels.dispose();checks++;
                        }
                    }
                    hero.belongings.weapon=weapons[1];hero.belongings.abilityWeapon=weapons[2];sprite.attack(hero.pos);
                    if(sprite.displayedWeapon()!=weapons[2])failures.add("24 secondary ability weapon not captured");
                    sprite.idle();sprite.presentProjectile(weapons[6]);sprite.zap(hero.pos);
                    if(sprite.displayedWeapon()!=weapons[6])failures.add("24 thrown action weapon not captured");
                    sprite.idle();if(sprite.displayedWeapon()!=weapons[1])failures.add("24 action weapon not cleared");
                    hero.belongings.abilityWeapon=null;sprite.destroy();
                }
            }
        }finally{hero.heroClass=original;hero.sprite=originalSprite;hero.belongings.weapon=originalWeapon;hero.belongings.abilityWeapon=second;hero.belongings.armor=originalArmor;}
        System.out.println("HERO EQUIPMENT: native draws="+checks+" eight armor rows, both facings, fifteen loadouts, generic movement/action pixels match empty loadout, secondary and thrown capture; prior grip/axis assertions retired by user request; failures="+failures.size());
    }

    private void hasteVisualChecks(com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,Camera camera,java.util.List<String> failures)throws Exception{
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero=Dungeon.hero;
        com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite original=hero.sprite;
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste prior=hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste.class);
        boolean enhanced=SPDSettings.enhancedEffects();float elapsed=Game.elapsed;
        com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite sprite=new com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite();
        Pixmap baseline=null,moving=null,stopped=null;
        try{
            SPDSettings.enhancedEffects(true);
            baseline=renderSprite(sprite,buffer,camera);
            com.shatteredpixel.shatteredpixeldungeon.effects.HasteTrail trail=(com.shatteredpixel.shatteredpixeldungeon.effects.HasteTrail)RecoveryChecks.field(sprite,"hasteTrail");
            trail.update();
            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste.class);
            sprite.isMoving=true;sprite.x+=.25f;trail.update();
            moving=renderSprite(sprite,buffer,camera);
            int changed=0,aboveBoots=0;
            float midline=256-(8+sprite.height/2)*camera.zoom;
            for(int y=0;y<256;y++)for(int x=0;x<256;x++)if(baseline.getPixel(x,y)!=moving.getPixel(x,y)){changed++;if(y>midline)aboveBoots++;}
            if(changed==0||aboveBoots>0)failures.add("32 Haste movement trail missing or reaches upper body: "+changed+"/"+aboveBoots);
            PixmapIO.writePNG(Gdx.files.absolute("verification/interface/haste-moving.png"),moving,-1,true);
            sprite.isMoving=false;Game.elapsed=.2f;trail.update();stopped=renderSprite(sprite,buffer,camera);
            java.util.zip.CRC32 a=new java.util.zip.CRC32(),b=new java.util.zip.CRC32();a.update(baseline.getPixels().duplicate());b.update(stopped.getPixels().duplicate());
            if(a.getValue()!=b.getValue())failures.add("32 Haste still draws while standing");
            PixmapIO.writePNG(Gdx.files.absolute("verification/interface/haste-standing.png"),stopped,-1,true);
            System.out.println("HASTE VISUAL: moving boot pixels="+changed+", upper-body pixels="+aboveBoots+", standing matches normal sprite; failures="+failures.size());
        }finally{
            if(prior==null)com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach(hero,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste.class);
            sprite.destroy();hero.sprite=original;Game.elapsed=elapsed;SPDSettings.enhancedEffects(enhanced);
            if(baseline!=null)baseline.dispose();if(moving!=null)moving.dispose();if(stopped!=null)stopped.dispose();
        }
    }

    private void rankAndKeyChecks(com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,Camera camera,java.util.List<String> failures)throws Exception{
        int points=0;
        for(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent talent:new com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent[]{com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.NECROTIC_TOUCH,com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.MASTER_CRAFT,com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.WIDER_BLAST})
            for(int rank=0;rank<=talent.maxPoints();rank++){
                com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton button=new com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton(1,talent,rank,com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton.Mode.INFO);button.setRect(0,0,20,26);
                Image[] centers=(Image[])RecoveryChecks.field(button,"centers");
                for(int i=0;i<centers.length;i++){
                    Pixmap drawn=renderSprite(centers[i],buffer,camera);int brightest=0,gold=0;
                    for(int y=0;y<256;y++)for(int x=0;x<256;x++){int px=drawn.getPixel(x,y);if((px&255)>200){brightest=Math.max(brightest,px>>>24);if((px>>>24)>220&&(px>>>16&255)>160&&(px>>>8&255)<120)gold++;}}
                    if(i<rank?brightest<180:brightest>=180)failures.add("36 bronze talent pip value "+talent+" rank="+rank+" socket="+i);
                    int cellX=Math.round(centers[i].frame().left*centers[i].texture.width);
                    if((centers[i].texture.bitmap.getPixel(cellX+2,2)&255)!=0||(centers[i].texture.bitmap.getPixel(cellX+32,32)&255)<240)
                        failures.add("36 rank pip is not rounded/opaque at centre");
                    drawn.dispose();points++;
                }button.destroy();
            }
        com.watabou.utils.Bundle notes=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.journal.Notes.storeInBundle(notes);
        com.shatteredpixel.shatteredpixeldungeon.ui.KeyDisplay strip=new com.shatteredpixel.shatteredpixeldungeon.ui.KeyDisplay();
        try{
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(Dungeon.depth));
            com.shatteredpixel.shatteredpixeldungeon.journal.Notes.add(new com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey(Dungeon.depth));
            strip.updateKeys();int count=strip.keyCount();strip.camera=camera;strip.setPos(16,8);
            for(int pass=0;pass<3;pass++){
                if(pass==1)strip.setPos(24,14);strip.invalidateLayout();
                buffer.begin();Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_SCISSOR_TEST);Gdx.gl.glClearColor(0,0,0,0);Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);com.watabou.glwrap.Texture.clear();com.watabou.noosa.NoosaScript.get().resetCamera();strip.draw();
                Pixmap drawn=Pixmap.createFromFrameBuffer(0,0,256,256);buffer.end();int visible=0;for(int y=0;y<256;y++)for(int x=0;x<256;x++)if((drawn.getPixel(x,y)&255)>80)visible++;
                if(visible==0||strip.keyCount()!=count)failures.add("25 keys disappear or mutate on layout pass "+pass);drawn.dispose();
            }
            Image compass=com.shatteredpixel.shatteredpixeldungeon.ui.Icons.COMPASS.get();if(compass.texture.width!=32||compass.texture.height!=24)failures.add("25 legacy stair compass");compass.destroy();
            Pixmap pointer=new Pixmap(Gdx.files.internal("gdx/grimhollow_cursor.png"));if(pointer.getWidth()!=64||pointer.getHeight()!=64)failures.add("25 painted pointer missing");pointer.dispose();
        }finally{strip.destroy();com.shatteredpixel.shatteredpixeldungeon.journal.Notes.restoreFromBundle(notes);GameScene.updateKeyDisplay();}
        if(RecoveryChecks.members(Game.scene()).indexOf((com.watabou.noosa.Gizmo)RecoveryChecks.field(Game.scene(),"levelWallVisuals"))>RecoveryChecks.members(Game.scene()).indexOf((com.watabou.noosa.Gizmo)RecoveryChecks.field(Game.scene(),"mobs")))failures.add("24 torches drawn over actors");
        Image shuffle=com.shatteredpixel.shatteredpixeldungeon.ui.Icons.SHUFFLE.get();
        if(shuffle.texture.width!=512||shuffle.texture.height!=64||shuffle.width()!=14)failures.add("36 legacy random talent icon");shuffle.destroy();
        for(int state=2;state<=4;state++){
            Image pip=com.shatteredpixel.shatteredpixeldungeon.ui.TalentMarkers.image(state,6);Pixmap rendered=renderSprite(pip,buffer,camera);
            if(GameGeometry.opaqueHeight(pip.texture,pip.frame())<45)failures.add("36 tier point indicator too small");rendered.dispose();pip.destroy();
        }
        int originalLevel=Dungeon.hero.lvl;
        try{
            Dungeon.hero.lvl=30;
            for(int tier=1;tier<=4;tier++)for(int width:new int[]{128,180}){
                com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane.TalentTierPane pane=new com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane.TalentTierPane(Dungeon.hero.talents.get(0),tier,com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton.Mode.UPGRADE);
                pane.setRect(0,0,width,0);
                java.util.List<Image> stars=(java.util.List<Image>)RecoveryChecks.field(pane,"stars");
                com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock summary=(com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock)RecoveryChecks.field(pane,"available");
                for(Image star:stars)if(star.x<0||star.x+star.width()>width||star.y+star.height()>summary.top())failures.add("36 tier marker clips/overlaps tier="+tier+" width="+width);
                for(Object member:(java.util.List<?>)RecoveryChecks.field(pane,"buttons"))if(((com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton)member).top()<summary.bottom())failures.add("36 tier summary overlaps ranks");
                pane.destroy();
            }
        }finally{Dungeon.hero.lvl=originalLevel;}
        System.out.println("UI RANK/KEY PASS: rendered bronze pips="+points+" rounded alpha; available/spent/future indicators, modern shuffle and eight tier/width layouts; key counts/paint survive three layouts; painted cursor/compass; torches behind actors; failures="+failures.size());
    }

    private void paintedSkillsAndPlants(com.badlogic.gdx.graphics.glutils.FrameBuffer buffer,Camera camera,java.util.List<String> failures)throws Exception {
        java.util.Set<Integer> ids=new java.util.TreeSet<>();
        for(int id=224;id<=298;id++)ids.add(com.shatteredpixel.shatteredpixeldungeon.ui.SkillIcon.talentIndex(id));
        for(java.lang.reflect.Field f:com.shatteredpixel.shatteredpixeldungeon.ui.SkillIcon.class.getFields())
            if(f.getType()==int.class&&f.getInt(null)>=1000)ids.add(f.getInt(null)-1000);
        java.util.Set<String> unique=new java.util.HashSet<>();
        for(int id:ids){
            Image icon=new com.shatteredpixel.shatteredpixeldungeon.ui.SkillIcon(id);
            if(icon.width()!=16||icon.height()!=16||GameGeometry.opaqueHeight(icon.texture,icon.frame())==0)failures.add("36 empty/oversized skill "+id);
            java.security.MessageDigest hash=java.security.MessageDigest.getInstance("SHA-256");
            for(int yy=0;yy<64;yy++)for(int xx=0;xx<64;xx++){
                int pixel=icon.texture.bitmap.getPixel(id%16*64+xx,id/16*64+yy);
                for(int shift=0;shift<32;shift+=8)hash.update((byte)(pixel>>>shift));
            }
            unique.add(java.util.Arrays.toString(hash.digest()));
            renderSprite(icon,buffer,camera).dispose();icon.destroy();
        }
        if(ids.size()!=113||unique.size()!=113)failures.add("36 missing/duplicate skill art "+ids.size()+"/"+unique.size());
        for(int i=0;i<PAINTED_PLANTS.length;i++){
            com.shatteredpixel.shatteredpixeldungeon.plants.Plant plant=(com.shatteredpixel.shatteredpixeldungeon.plants.Plant)Class.forName(plantClass(i)).getDeclaredConstructor().newInstance();
            Image icon=com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap.getPlantVisual(plant);
            if(plant.image!=i||icon.width()!=16||icon.height()!=16||GameGeometry.opaqueHeight(icon.texture,icon.frame())==0)failures.add("25 plant "+PAINTED_PLANTS[i]);
            if(Math.abs(icon.frame().left*icon.texture.width-(i*64+.5f))>.01f||Math.abs(icon.frame().top*icon.texture.height-(7*64+.5f))>.01f)failures.add("25 plant texel alignment "+PAINTED_PLANTS[i]);
            renderSprite(icon,buffer,camera).dispose();icon.destroy();
        }
        System.out.println("PAINTED SKILLS/PLANTS: unique skills="+unique.size()+" plants="+PAINTED_PLANTS.length);
    }
    private static final String[] PAINTED_PLANTS={"Rotberry","Firebloom","Swiftthistle","Sungrass","Icecap","Stormvine","Sorrowmoss","Mageroyal","Earthroot","Starflower","Fadeleaf","Blindweed","BlandfruitBush","Dewcatcher","Seedpod"};
    private static String plantClass(int index) {
        return "com.shatteredpixel.shatteredpixeldungeon."+(index<13?"plants.":"items.wands.WandOfRegrowth$")+PAINTED_PLANTS[index];
    }
    private void steadyIdle(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite,java.util.List<String> failures)throws Exception {
        if(sprite instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite)return;
        com.watabou.noosa.MovieClip.Animation idle=(com.watabou.noosa.MovieClip.Animation)RecoveryChecks.field(sprite,"idle");
        if(idle==null||!idle.looped)return;
        sprite.play(idle,true);
        com.watabou.utils.RectF frame=new com.watabou.utils.RectF(sprite.frame());
        java.lang.reflect.Method advance=com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class.getDeclaredMethod("updateAnimation");
        advance.setAccessible(true);
        float elapsed=Game.elapsed;
        try {Game.elapsed=1f/60f;for(int i=0;i<600;i++)advance.invoke(sprite);}
        finally{Game.elapsed=elapsed;}
        com.watabou.utils.RectF after=sprite.frame();
        if(frame.left!=after.left||frame.top!=after.top||frame.right!=after.right||frame.bottom!=after.bottom)
            failures.add("Continuously animated idle "+sprite.getClass().getSimpleName());
        com.watabou.noosa.MovieClip.Animation run=(com.watabou.noosa.MovieClip.Animation)RecoveryChecks.field(sprite,"run");
        if(run!=null&&run.looped){
            sprite.play(run);com.watabou.utils.RectF pose=new com.watabou.utils.RectF(sprite.frame());
            try{Game.elapsed=1f/60f;for(int i=0;i<120;i++)advance.invoke(sprite);}finally{Game.elapsed=elapsed;}
            after=sprite.frame();
            if(pose.left!=after.left||pose.top!=after.top||pose.right!=after.right||pose.bottom!=after.bottom)failures.add("Twitching travel pose "+sprite.getClass().getSimpleName());
            sprite.idle();
        }
    }

    private void paintedAnimations(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite,
                                   com.badlogic.gdx.utils.JsonValue painted,java.util.List<String> failures)throws Exception {
        com.badlogic.gdx.utils.JsonValue asset=null;
        for(com.badlogic.gdx.utils.JsonValue entry:painted) {
            if(com.watabou.gltextures.TextureCache.contains(entry.name)
                    &&com.watabou.gltextures.TextureCache.get(entry.name)==sprite.texture){asset=entry;break;}
        }
        String who=sprite.getClass().getName().replace("com.shatteredpixel.shatteredpixeldungeon.sprites.","");
        if(asset==null){failures.add("Unpainted creature texture: "+who);return;}
        if(sprite.texture.fModeMax!=com.badlogic.gdx.graphics.GL20.GL_LINEAR)failures.add("Unfiltered creature: "+who);
        if(asset.name.startsWith("sprites/hero_"))return; // Reflections share the reviewed hero sheet.
        java.util.Set<String> rectangles=new java.util.HashSet<>();
        for(com.badlogic.gdx.utils.JsonValue box:asset.get("painted_rects"))rectangles.add(java.util.Arrays.toString(box.asIntArray()));
        java.util.Set<com.watabou.noosa.MovieClip.Animation> animations=new java.util.HashSet<>();
        for(Class<?> type=sprite.getClass();type!=null;type=type.getSuperclass())for(java.lang.reflect.Field field:type.getDeclaredFields()) {
            if(java.lang.reflect.Modifier.isStatic(field.getModifiers()))continue;
            if(field.getType()==com.watabou.noosa.MovieClip.Animation.class) {
                field.setAccessible(true);animations.add((com.watabou.noosa.MovieClip.Animation)field.get(sprite));
            }else if(field.getType()==com.watabou.noosa.MovieClip.Animation[].class) {
                field.setAccessible(true);java.util.Collections.addAll(animations,(com.watabou.noosa.MovieClip.Animation[])field.get(sprite));
            }
        }
        for(com.watabou.noosa.MovieClip.Animation animation:animations)if(animation!=null&&animation.frames!=null)
            for(com.watabou.utils.RectF frame:animation.frames) {
                int[] box={Math.round(frame.left*sprite.texture.width),Math.round(frame.top*sprite.texture.height),
                        Math.round(frame.right*sprite.texture.width),Math.round(frame.bottom*sprite.texture.height)};
                if(!rectangles.contains(java.util.Arrays.toString(box)))failures.add("Unpainted pose: "+who+" "+java.util.Arrays.toString(box));
            }
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
            int paintedFamilies=0;
            for(com.shatteredpixel.shatteredpixeldungeon.effects.PaintedParticle particle:new com.shatteredpixel.shatteredpixeldungeon.effects.PaintedParticle[]{
                    new com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle(),
                    new com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle(),
                    new com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle(),
                    new com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle(),
                    new com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.EarthParticle(),
                    new com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle()}){
                particle.reset(0,0,0xFFFFFF,8,1);
                if(particle instanceof com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.EarthParticle)
                    ((com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.EarthParticle)particle).reset(0,0);
                SPDSettings.enhancedEffects(false);Pixmap oldPixels=renderSprite(particle,buffer,camera);
                SPDSettings.enhancedEffects(true);Pixmap newPixels=renderSprite(particle,buffer,camera);
                int difference=0,soft=0,opaque=0;
                for(int y=0;y<256;y++)for(int x=0;x<256;x++){
                    int a=newPixels.getPixel(x,y)&255;if(a>8&&a<247)soft++;if(a>=247)opaque++;
                    if(oldPixels.getPixel(x,y)!=newPixels.getPixel(x,y))difference++;
                }
                if(difference==0||soft<10||soft+opaque<20)failures.add("56 empty, square or unchanged particle "+particle.getClass().getSimpleName());
                if(particle instanceof com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.EarthParticle)
                    PixmapIO.writePNG(Gdx.files.absolute("verification/living-earth.png"),newPixels);
                oldPixels.dispose();newPixels.dispose();particle.destroy();paintedFamilies++;
            }
            int speckKinds=0;
            for(int kind:new int[]{Speck.HEALING,Speck.STAR,Speck.QUESTION,Speck.BONE,Speck.RATTLE,Speck.WOOL,Speck.ROCK,Speck.NOTE,Speck.CHANGE,Speck.HEART,Speck.BUBBLE,Speck.STEAM,Speck.COIN,Speck.STORM,Speck.BLIZZARD,Speck.INFERNO}){
                Speck speck=new Speck();speck.reset(0,0,0,kind);speck.alpha(1);speck.scale.set(2);Pixmap pixels=renderSprite(speck,buffer,camera);
                int visible=0;for(int y=0;y<256;y++)for(int x=0;x<256;x++)if((pixels.getPixel(x,y)&255)>8)visible++;
                if(visible==0)failures.add("56 invisible Speck "+kind);pixels.dispose();speck.destroy();speckKinds++;
            }
            System.out.println("TEST 56 EFFECTS: painted solid-particle families="+paintedFamilies+" soft alpha edges, visible Speck kinds="+speckKinds+" failures="+failures.size());
            for(com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type kind:new com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type[]{
                    com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type.LIGHTNING,com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type.DEATH_RAY,
                    com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type.LIGHT_RAY,com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type.HEALTH_RAY,
                    com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type.RIPPLE,com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type.WOUND}){
                Image ray=com.shatteredpixel.shatteredpixeldungeon.effects.Effects.get(kind);int visible=0,soft=0;
                if(ray.width!=16||ray.height!=(kind==com.shatteredpixel.shatteredpixeldungeon.effects.Effects.Type.RIPPLE?16:8))failures.add("56 ray logical dimensions "+kind);
                Pixmap pixels=renderSprite(ray,buffer,camera);for(int y=0;y<256;y++)for(int x=0;x<256;x++){int alpha=pixels.getPixel(x,y)&255;if(alpha>8)visible++;if(alpha>8&&alpha<247)soft++;}
                if(visible<20||soft<10)failures.add("56 empty or unfiltered ray "+kind);pixels.dispose();ray.destroy();
            }
            System.out.println("TEST 56 RAYS: lightning/death/light/healing, wound and ripple preserve logical size with painted soft-alpha output; failures="+failures.size());
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
            java.lang.reflect.Field batchField=BlobEmitter.class.getDeclaredField("fireBatch");batchField.setAccessible(true);
            Object fireBatch=batchField.get(RecoveryChecks.members(fx).get(1));
            if(fireBatch==null)throw new AssertionError("31 fire batching did not execute");
            java.lang.reflect.Field batchBuffer=fireBatch.getClass().getDeclaredField("buffer");batchBuffer.setAccessible(true);
            if(batchBuffer.get(fireBatch)==null)throw new AssertionError("31 fire batching fell back to individual draws");
            // Compare the unchanged particles with individual draws outside the timing sample.
            buffer.begin();Pixmap batched=Pixmap.createFromFrameBuffer(0,0,768,768);
            Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);com.watabou.glwrap.Texture.clear();
            com.watabou.noosa.NoosaScript.get().resetCamera();
            RecoveryChecks.members(fx).get(0).draw();com.watabou.glwrap.Blending.setLightMode();
            for(com.watabou.noosa.Gizmo particle:RecoveryChecks.members((Group)RecoveryChecks.members(fx).get(1)))
                if(particle!=null&&particle.isVisible())particle.draw();
            com.watabou.glwrap.Blending.setNormalMode();
            Pixmap individual=Pixmap.createFromFrameBuffer(0,0,768,768);buffer.end();
            int maxChannelDifference=0;
            for(int y=0;y<768;y++)for(int x=0;x<768;x++) {
                int a=batched.getPixel(x,y),b=individual.getPixel(x,y);
                for(int shift=0;shift<32;shift+=8)maxChannelDifference=Math.max(maxChannelDifference,
                        Math.abs(((a>>>shift)&255)-((b>>>shift)&255)));
            }
            batched.dispose();individual.dispose();
            // CPU/GPU transform rounding and additive accumulation can differ by one byte.
            if(maxChannelDifference>2)failures.add("31 batched fire differs from individual draws: "+maxChannelDifference);
            System.out.println("TEST 31 FIRE BATCH: individual-render max channel difference="+maxChannelDifference);
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
        // Hero body and boot wisps use the game's alpha blending; raw atlas
        // pixel comparisons below remain unblended.
        if(image instanceof com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite)com.watabou.glwrap.Blending.useDefault();
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

    private void semanticItem(Image image,int id,com.badlogic.gdx.utils.JsonValue expected,String name,java.util.List<String> failures)throws Exception {
        if(expected==null)throw new AssertionError("25 no semantic reference for "+name);
        if(id!=expected.getInt("id"))failures.add("25 changed identity "+name);
        java.security.MessageDigest digest=java.security.MessageDigest.getInstance("SHA-256");
        com.watabou.utils.RectF uv=image.frame();int x=Math.round(uv.left*image.texture.width),y=Math.round(uv.top*image.texture.height);
        int cellSize=expected.getInt("cellSize",32);
        for(int j=0;j<cellSize;j++)for(int i=0;i<cellSize;i++) {
            int rgba=image.texture.bitmap.getPixel(x+i,y+j);
            digest.update(new byte[]{(byte)(rgba>>>24),(byte)(rgba>>>16),(byte)(rgba>>>8),(byte)rgba});
        }
        StringBuilder hash=new StringBuilder();for(byte b:digest.digest())hash.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
        if(!hash.toString().equals(expected.getString("rgbaSha256")))failures.add("25 wrong semantic art "+name);
    }

    private void namedItems(com.badlogic.gdx.utils.JsonValue semantics,java.util.List<String> failures)throws Exception {
        String[][] names={
            {"Waterskin","WATERSKIN"},{"wands.WandOfNecrosis","WAND_NECROSIS"},{"wands.WandOfGravity","WAND_GRAVITY"},
            {"wands.WandOfBone","WAND_BONE"},{"spells.Soulfire","SOULFIRE"},{"weapon.melee.BoneScythe","BONE_SCYTHE"},
            {"weapon.melee.ReapersScythe","REAPER_SCYTHE"},{"weapon.melee.GraveScythe","GRAVE_SCYTHE"},
            {"armor.BoneArmor","ARMOR_BONE"},{"artifacts.HourglassOfAshes","HOURGLASS_ASHES"},{"artifacts.AshlightLantern","ASHLIGHT_OPEN"},
            {"weapon.melee.BoneRod","BONE_ROD"},{"Phylactery","PHYLACTERY"},{"armor.NecromancerArmor","ARMOR_NECROMANCER"},
            {"weapon.melee.RunedBaton","RUNED_BATON"},{"SigilBrush","SIGIL_BRUSH"},{"armor.EnchanterArmor","ARMOR_ENCHANTER"},
            {"weapon.melee.FocusRing","FOCUS_RING"},{"FocusCrystal","FOCUS_CRYSTAL"},{"armor.PsychicArmor","ARMOR_PSYCHIC"},{"RuneEtching","RUNE_ETCHING"}
        };
        for(String[] row:names) {
            com.shatteredpixel.shatteredpixeldungeon.items.Item item=(com.shatteredpixel.shatteredpixeldungeon.items.Item)
                    Class.forName("com.shatteredpixel.shatteredpixeldungeon.items."+row[0]).getDeclaredConstructor().newInstance();
            if(item.image()!=semantics.get("items").get(row[1]).getInt("id"))failures.add("25 item class maps to wrong art: "+row[0]);
        }
        for(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor armor:new com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor[]{
                new com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor(),new com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor()}) {
            com.watabou.utils.Bundle b=new com.watabou.utils.Bundle();armor.storeInBundle(b);b.put("leather_variant",true);armor.restoreFromBundle(b);
            String name=armor.tier==2?"ARMOR_LEATHER_OCHRE":"ARMOR_LEATHER_ASH";
            if(armor.image()!=semantics.get("items").get(name).getInt("id"))failures.add("25 leather variant "+name);
        }
        com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision mind=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision();
        // Bottle colours are randomized by the saved identification handler, not by effect.
        if(mind.image()<352||mind.image()>363||mind.icon!=82)failures.add("25 Mind Vision bottle/identity");
        Image eye=new Image(Assets.Sprites.ITEM_ICONS);eye.frame(com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.film.get(mind.icon));
        semanticItem(eye,mind.icon,semantics.get("icons").get("POTION_MINDVIS"),"Potion of Mind Vision eye",failures);eye.destroy();
        System.out.println("TEST 25 semantics: all "+semantics.get("items").size+" named item IDs + "+semantics.get("icons").size
                +" icons, Waterskin=480, MindVision=third-eye@"+semantics.get("icons").get("POTION_MINDVIS").getInt("artIndex")
                +" (ID 82), section9 items=11, class items=10, Ashlight open/closed=537/538");
    }
}
