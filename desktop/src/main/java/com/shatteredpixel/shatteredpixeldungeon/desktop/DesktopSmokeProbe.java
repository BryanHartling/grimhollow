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
    private final boolean encounters=Boolean.getBoolean("grimhollow.encounterTests");
    private final boolean interfaceReview=Boolean.getBoolean("grimhollow.interfaceReview");
    private final boolean presentationReview=Boolean.getBoolean("grimhollow.presentationReview");
    private boolean presentationStarted;
    private int presentationGameFrames;
    private int encounterActions, encounterSteps, encounterAttacks, encounterLastCell=-1;
    private int[] encounterVisits;
    private volatile boolean encounterDrops, encounterSummon, encounterDeath;
    private int encounterDeathFrames;
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
        if(presentationReview && frames>180) { presentationTick(); return; }
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
        if (vault && sewers) vaultFrames();
        if (frames==180) {
            if (!(Game.scene() instanceof TitleScene)) throw new AssertionError("Title scene did not launch");
            capture("title");
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
                    com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed seed=(com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed)Class.forName("com.shatteredpixel.shatteredpixeldungeon.plants."+PAINTED_PLANTS[i]+"$Seed").getDeclaredConstructor().newInstance();
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
            if(frame==220)clickReviewLabel(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush.class,"armor"));
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
                System.out.println("TEST 33/36 BOTANY UI: sprouted plants=13 pointer armor selections=2 glyph library="+choices.size()+" scrolling/offset=PASS failures=0");
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
    private int playtestStep,playtestWait;
    private void playtestTick(){
        if(++playtestWait>12000)throw new AssertionError("Playtest menu scenario stalled at "+playtestStep);
        if(Game.scene() instanceof InterlevelScene){
            Object button=RecoveryChecks.field(Game.scene(),"btnContinue");
            if(button instanceof com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton && ((com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton)button).active)pointerClickReview(button);
            return;
        }
        if(!(Game.scene() instanceof GameScene)||frames%20!=0)return;
        switch(playtestStep){
            case 0:closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGame());break;
            case 1:playtestClick("Playtest");break;
            case 2:interfaceBounds();capture("playtest-enable");playtestClick("Enable Playtest for this save");break;
            case 3:if(!Playtest.enabled())throw new AssertionError("Playtest enable pointer failed");playtestClick("God mode: OFF");break;
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
            case 12:playtestClick("Hero, class and progression");break;
            case 13:playtestClick("Set hero level");break;
            case 14:playtestInput("24","Apply");break;
            case 15:if(Dungeon.hero.lvl!=24)throw new AssertionError("Native level input failed");playtestClick("Choose subclass");break;
            case 16:playtestClick("Seer");break;
            case 17:
                if(Dungeon.hero.subClass!=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.SEER)throw new AssertionError("Native subclass/load failed");
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
            case 18:playtestClick("Hero, class and progression");break;
            case 19:if(!playtestClickPage("Choose armor ability / grant class armor"))return;break;
            case 20:playtestClick(Dungeon.hero.heroClass.armorAbilities()[0].name());break;
            case 21:
                if(!(Dungeon.hero.belongings.armor instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor))throw new AssertionError("Native class armor failed");
                GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
            case 22:if(!playtestClickPage("Travel to any floor / quest branch"))return;break;
            case 23:interfaceBounds();if(!playtestClickPage("Floor 21 - Halls"))return;break;
            case 24:
                if(Dungeon.depth!=21||Dungeon.hero.lvl!=24||!Playtest.god())throw new AssertionError("Native travel/load lost hero or flags");
                capture("playtest-halls");GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
            case 25:playtestClick("Hero, class and progression");break;
            case 26:playtestClick("Change class and starter kit");break;
            case 27:interfaceBounds();capture("playtest-classes");if(!playtestClickPage("Enchanter"))return;break;
            case 28:
                if(Dungeon.hero.heroClass!=HeroClass.ENCHANTER||Dungeon.hero.lvl!=24||Dungeon.depth!=21||!(Dungeon.hero.belongings.artifact instanceof com.shatteredpixel.shatteredpixeldungeon.items.SigilBrush))throw new AssertionError("Native class switch/kit failed");
                capture("playtest-enchanter-halls");GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndPlaytest());break;
            case 29:playtestClick("God mode: ON");break;
            case 30:
                if(!Playtest.enabled()||Playtest.god())throw new AssertionError("Native toggle did not preserve save marker");
                closeReviewWindows();GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndGameInProgress(99));break;
            case 31:interfaceBounds();capture("playtest-save");
                System.out.println("TEST 52 UI: real pointer enable/god/search/+10 artifact/hero level/subclass/armor/class switch/floor-21 travel and save marker; landscape or portrait bounds; failures=0");
                Gdx.app.exit();return;
        }
        playtestStep++;
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
    private boolean playtestClickPage(String label){
        if(playtestButton(label)!=null){playtestClick(label);return true;}
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
            for(java.lang.reflect.Field field:com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.class.getFields()) {
                if(field.getType()!=int.class||field.getName().equals("SIZE"))continue;
                int index=field.getInt(null);if(index<0)continue;
                com.watabou.noosa.Image icon=new com.watabou.noosa.Image(Assets.Sprites.ITEM_ICONS);
                icon.frame(com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.Icons.film.get(index));
                com.badlogic.gdx.utils.JsonValue expected=semantics.get("icons").get(field.getName());
                semanticItem(icon,index,expected,"icon "+field.getName(),failures);
                exactRectangle(icon,expected.getInt("artIndex"),32,buffer,camera,zoom,failures,"icon "+field.getName());icons++;icon.destroy();
            }
            if(items!=semantics.get("items").size||icons!=semantics.get("icons").size)failures.add("25 incomplete named atlas inventory");
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
                int expected=com.shatteredpixel.shatteredpixeldungeon.ui.SkillIcon.talentIndex(talent.icon())>=0?64:32;
                if(Math.round(icon.frame().width()*icon.texture.width)!=expected||icon.width()!=16||GameGeometry.opaqueHeight(icon.texture,icon.frame())==0)failures.add("36 empty or mis-scaled talent "+talent);
                Pixmap drawn=renderSprite(icon,buffer,camera);drawn.dispose();icon.destroy();
            }
            paintedSkillsAndPlants(buffer,camera,failures);
            System.out.println("TEST 36: nine splashes, descriptions, portraits, all talents and ItemSlots failures="+(failures.size()-before));
            saveCompatibility(failures);
            System.out.println("TEST 35 RETIRED — superseded by recovery test 46 handler/network checks");
            buffer.dispose();for(String failure:failures)System.out.println("FAIL "+failure);
            if(!failures.isEmpty())throw new AssertionError("Rendering acceptance failures="+failures.size());
            System.out.println("TESTS 24-26, 34, 36 PASS; test 35 retired by recovery");
        }catch(Exception e){throw new RuntimeException(e);}
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
            com.shatteredpixel.shatteredpixeldungeon.plants.Plant plant=(com.shatteredpixel.shatteredpixeldungeon.plants.Plant)Class.forName("com.shatteredpixel.shatteredpixeldungeon.plants."+PAINTED_PLANTS[i]).getDeclaredConstructor().newInstance();
            Image icon=com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap.getPlantVisual(plant);
            if(plant.image!=i||icon.width()!=16||icon.height()!=16||GameGeometry.opaqueHeight(icon.texture,icon.frame())==0)failures.add("25 plant "+PAINTED_PLANTS[i]);
            if(Math.abs(icon.frame().left*icon.texture.width-(i*64+.5f))>.01f||Math.abs(icon.frame().top*icon.texture.height-(7*64+.5f))>.01f)failures.add("25 plant texel alignment "+PAINTED_PLANTS[i]);
            renderSprite(icon,buffer,camera).dispose();icon.destroy();
        }
        System.out.println("PAINTED SKILLS/PLANTS: unique skills="+unique.size()+" plants="+PAINTED_PLANTS.length);
    }
    private static final String[] PAINTED_PLANTS={"Rotberry","Firebloom","Swiftthistle","Sungrass","Icecap","Stormvine","Sorrowmoss","Mageroyal","Earthroot","Starflower","Fadeleaf","Blindweed","BlandfruitBush"};
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
        System.out.println("TEST 25 semantics: all 383 named item IDs + 60 icons, Waterskin=480, MindVision=eye@98 (ID 82), section9 items=11, class items=10, Ashlight open/closed=537/538");
    }
}
