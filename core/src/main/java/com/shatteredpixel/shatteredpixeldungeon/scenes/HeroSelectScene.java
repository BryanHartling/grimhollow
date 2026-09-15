/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChallenges;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndKeyBindings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVictoryCongrats;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HeroSelectScene extends PixelScene {

	private Image background;
	private Image fadeLeft, fadeRight;
	private IconButton btnFade; //only on landscape

	//fading UI elements
	private RenderedTextBlock title;
	private ArrayList<StyledButton> heroBtns = new ArrayList<>();
	private RenderedTextBlock heroName; //only on landscape
	private RenderedTextBlock heroDesc; //only on landscape
	private StyledButton startBtn;
	private IconButton infoButton;
	private IconButton btnOptions;
	private GameOptions optionsPane;
	private IconButton btnExit;

	private RectF insets;

	private static boolean heroWasRandomized = true;
	private static boolean chalWasRandomized = false;

	@Override
	public void create() {
		super.create();

		Dungeon.hero = null;

		Badges.loadGlobal();
		Journal.loadGlobal();

		insets = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK).scale(1f/defaultZoom);

		float w = (Camera.main.width - insets.left - insets.right);
		float h = (Camera.main.height - insets.top - insets.bottom);

		background = new Image(TextureCache.createSolid(0xFF2d2f31), 0, 0, 800, 450){
			@Override
			public void update() {
				if (GamesInProgress.selectedClass != null) {
					if (rm > 1f) {
						rm -= Game.elapsed;
						gm = bm = rm;
					} else {
						rm = gm = bm = 1;
					}
				}
			}
		};
		background.scale.set(Camera.main.height/background.height);

		background.x = (Camera.main.width - background.width())/2f;
		background.y = (Camera.main.height - background.height())/2f;
		PixelScene.align(background);
		add(background);

		fadeLeft = new Image(TextureCache.createGradient(0xFF000000, 0xFF000000, 0x00000000));
		fadeLeft.x = background.x-2;
		fadeLeft.scale.set(3, background.height());
		add(fadeLeft);

		fadeRight = new Image(fadeLeft);
		fadeRight.x = background.x + background.width() + 2;
		fadeRight.y = background.y + background.height();
		fadeRight.angle = 180;
		add(fadeRight);

		title = PixelScene.renderTextBlock(Messages.get(this, "title"), 12);
		title.hardlight(Window.TITLE_COLOR);
		PixelScene.align(title);
		add(title);

		startBtn = new StyledButton(Chrome.Type.GREY_BUTTON_TR, ""){
			@Override
			protected void onClick() {
				super.onClick();

				if (GamesInProgress.selectedClass == null || !GamesInProgress.selectedClass.isUnlocked()) return;

				Dungeon.hero = null;
				Dungeon.daily = Dungeon.dailyReplay = false;
				Dungeon.initSeed();
				ActionIndicator.clearAction();
				InterlevelScene.mode = InterlevelScene.Mode.DESCEND;

				Game.switchScene( InterlevelScene.class );
			}
		};
		startBtn.icon(Icons.get(Icons.ENTER));
		startBtn.setSize(80, 21);
		startBtn.textColor(Window.TITLE_COLOR);
		add(startBtn);
		startBtn.visible = startBtn.active = false;

		infoButton = new IconButton(Icons.get(Icons.INFO)){
			@Override
			protected void onClick() {
				super.onClick();
				HeroClass cls = GamesInProgress.selectedClass;
				if (cls != null) {
					Window info = new WndHeroInfo(GamesInProgress.selectedClass);
					if (landscape()) {
						info.offset(Math.max(0,(int)(w/3-info.camera.width/2-6)), 0);
					}
					ShatteredPixelDungeon.scene().addToFront(info);
				}
			}

			@Override
			protected String hoverText() {
				return Messages.titleCase(Messages.get(WndKeyBindings.class, "hero_info"));
			}
		};
		infoButton.visible = infoButton.active = false;
		infoButton.setSize(20, 21);
		add(infoButton);

		for (HeroClass cl : HeroClass.values()){
			HeroBtn button = new HeroBtn(cl);
			add(button);
			heroBtns.add(button);
		}

		optionsPane = new GameOptions();
		optionsPane.visible = optionsPane.active = false;
		optionsPane.layout();
		add(optionsPane);

		btnOptions = new IconButton(Icons.get(Icons.PREFS)){
			@Override
			protected void onClick() {
				super.onClick();
				optionsPane.visible = !optionsPane.visible;
				optionsPane.active = !optionsPane.active;
			}

			@Override
			protected void onPointerDown() {
				super.onPointerDown();
			}

			@Override
			protected void onPointerUp() {
				updateOptionsColor();
			}

			@Override
			protected String hoverText() {
				return Messages.get(HeroSelectScene.class, "options");
			}
		};
		updateOptionsColor();
		btnOptions.visible = false;

		if(!SPDSettings.intro()){
			add(btnOptions);
		}

		if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
			Dungeon.challenges = 0;
			SPDSettings.challenges(0);
			SPDSettings.customSeed("");
		}

        heroName = renderTextBlock(landscape()?10:14);
        heroName.hardlight(Window.TITLE_COLOR);
        add(heroName);
        heroDesc = renderTextBlock(landscape()?6:9);
        heroDesc.align(RenderedTextBlock.CENTER_ALIGN);
        add(heroDesc);
        float panelWidth = landscape() ? Math.max(100,w/3f) : w;
        int cols = landscape() ? 3 : 5;
        int rows = (heroBtns.size()+cols-1)/cols;
        float btnWidth = Math.min(42,(panelWidth-12)/cols);
        float btnHeight = landscape() ? Math.min(40,(h-124)/3f) : 33;
        float gridTop = landscape() ? insets.top+29 : insets.top+h-rows*(btnHeight+2)-4;
        for(int i=0;i<heroBtns.size();i++){
            int row=i/cols, col=i%cols;
            int count=Math.min(cols,heroBtns.size()-row*cols);
            float rowWidth=count*(btnWidth+2)-2;
            StyledButton button=heroBtns.get(i);
            button.icon().logicalSize(Math.min(30,btnHeight-4),Math.min(30,btnHeight-4));
            button.setRect(insets.left+(panelWidth-rowWidth)/2+col*(btnWidth+2),
                    gridTop+row*(btnHeight+2),btnWidth,btnHeight);
            align(button);
        }
        title.setPos(insets.left+(panelWidth-title.width())/2,insets.top+8);
        heroName.setPos(insets.left,landscape()?gridTop+rows*(btnHeight+2)+8:insets.top+12);
        startBtn.text(Messages.titleCase(Messages.get(this,"start")));
        startBtn.setSize(72,23);
        startBtn.setPos(insets.left+(panelWidth-startBtn.width())/2,
                landscape()?insets.top+h-30:gridTop-29);
        btnOptions.setRect(startBtn.right()+2,startBtn.top(),22,23);
        optionsPane.setPos(insets.left+4,Math.max(insets.top+24,startBtn.top()-optionsPane.height()-3));
        // Portrait text sits over a dark backing, below the face and above Start.
        if(!landscape()){
            fadeLeft.x=0;fadeLeft.y=startBtn.top()-58;
            fadeLeft.texture(TextureCache.createSolid(0xB8000000));
            fadeLeft.frame(0,0,1,1);fadeLeft.scale.set(Camera.main.width,58);
            fadeRight.visible=false;
        }

		btnExit = new ExitButton();
		int ofs = PixelScene.landscape() ? 0 : 4;
		btnExit.setPos( Camera.main.width - btnExit.width() - ofs, ofs );
		add( btnExit );
		btnExit.visible = btnExit.active = !SPDSettings.intro();

		PointerArea fadeResetter = new PointerArea(0, 0, Camera.main.width, Camera.main.height){
			@Override
			public boolean onSignal(PointerEvent event) {
				if (event != null && event.type == PointerEvent.Type.UP){
					if (uiAlpha == 0 && landscape()){
						parent.add(new Tweener(parent, 0.5f) {
							@Override
							protected void updateValues(float progress) {
								uiAlpha = progress;
								updateFade();
							}

							@Override
							protected void onComplete() {
								resetFade();
							}
						});
					} else {
						resetFade();
					}
				}
				return false;
			}
		};
		add(fadeResetter);
		resetFade();

		if (GamesInProgress.selectedClass != null){
			setSelectedHero(GamesInProgress.selectedClass);
		}

		if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		fadeIn();

	}

	private void updateOptionsColor(){
		if (!SPDSettings.customSeed().isEmpty()){
			btnOptions.icon().hardlight(1f, 1.5f, 0.67f);
		} else if (SPDSettings.challenges() != 0){
			btnOptions.icon().hardlight(2f, 1.33f, 0.5f);
		} else {
			btnOptions.icon().resetColor();
		}
	}

	private void setSelectedHero(HeroClass cl){
		GamesInProgress.selectedClass = cl;
		GamesInProgress.randomizedClass = false;

		try {
			// Retain a usable selection screen if a painting cannot be loaded.
			background.texture(cl.splashArt());
            background.frame(0,0,background.texture.width,background.texture.height);
            background.logicalSize(800,450);
            background.texture.filter(com.badlogic.gdx.graphics.GL20.GL_LINEAR,com.badlogic.gdx.graphics.GL20.GL_LINEAR);
		} catch (Exception e){
			Game.reportException(e);
			background.texture(TextureCache.createSolid(0xFF2d2f31));
			background.frame(0, 0, 800, 450);
		}
		background.visible = true;
		background.resetColor();

        float panelWidth=landscape()?Math.max(100,(Camera.main.width-insets.left-insets.right)/3f)
                : Camera.main.width-insets.left-insets.right;
        title.visible=false;
        heroName.text(Messages.titleCase(cl.title()));
        heroName.setPos(insets.left+(panelWidth-heroName.width())/2,
                landscape()?heroName.top():insets.top+12);
        heroDesc.text(cl.theme());
        heroDesc.maxWidth((int)(landscape()?panelWidth-18:Math.min(panelWidth-30,260)));
        heroDesc.setPos(insets.left+(panelWidth-heroDesc.width())/2,
                landscape()?heroName.bottom()+7:startBtn.top()-heroDesc.height()-7);
        align(heroName);align(heroDesc);
        startBtn.visible=true;
        startBtn.enable(cl.isUnlocked());
        startBtn.text(Messages.titleCase(Messages.get(this,cl.isUnlocked()?"start":"locked")));
        infoButton.visible=infoButton.active=true;
        infoButton.setRect(startBtn.left()-24,startBtn.top(),22,23);
        btnOptions.visible=btnOptions.active=!SPDSettings.intro();

		updateOptionsColor();
	}

	private float uiAlpha;

	@Override
	public void update() {
		super.update();
		if (SPDSettings.intro() && Rankings.INSTANCE.totalNumber > 0){
			SPDSettings.intro(false);
		}
		btnExit.visible = btnExit.active = !SPDSettings.intro();
		//do not fade when a window is open
		for (Object v : members){
			if (v instanceof Window) resetFade();
		}
	}

	private void updateFade(){
		float alpha = GameMath.gate(0f, uiAlpha, 1f);
		title.alpha(alpha);
		for (StyledButton b : heroBtns){
			b.enable(alpha != 0);
			b.alpha(alpha);
		}
		if (heroName != null){
			heroName.alpha(alpha);
			heroDesc.alpha(alpha);
			if(btnFade!=null){btnFade.enable(alpha != 0);btnFade.icon().alpha(alpha);}
		}
		startBtn.enable(alpha != 0 && GamesInProgress.selectedClass != null && GamesInProgress.selectedClass.isUnlocked());
		startBtn.alpha(alpha);
		btnExit.enable(btnExit.visible && alpha != 0);
		btnExit.icon().alpha(alpha);
		optionsPane.active = optionsPane.visible && alpha != 0;
		optionsPane.alpha(alpha);
		btnOptions.enable(alpha != 0);
		btnOptions.icon().alpha(alpha);
		infoButton.enable(alpha != 0);
		infoButton.icon().alpha(alpha);

		if (landscape()){

			int w = (int)(Camera.main.width - insets.left - insets.right);

			background.x = insets.left + (w - background.width())/2f;

			float leftPortion = Math.max(100, w/3f);

			background.x += (leftPortion/2f)*alpha;

			float fadeLeftScale = 47 * (leftPortion - (background.x - insets.left))/leftPortion;
			fadeLeft.scale.x = 3 + Math.max(fadeLeftScale, 0)*alpha;
			fadeLeft.x = background.x-4;
			fadeRight.x = background.x + background.width() + 4;
		}

		if(!landscape())return;
		fadeLeft.x = background.x-5;
		fadeRight.x = background.x + background.width() + 5;

		fadeLeft.visible = background.x > 0 || (alpha > 0 && landscape());
		fadeRight.visible = background.x + background.width() < Camera.main.width;
	}

	private void resetFade(){
		// Selection controls stay available, including on touch screens.
		uiAlpha = 2f;
		updateFade();
	}

	@Override
	protected void onBackPressed() {
		if (btnExit.active){
			ShatteredPixelDungeon.switchScene(TitleScene.class);
		} else {
			super.onBackPressed();
		}
	}

	private class HeroBtn extends StyledButton {

		private HeroClass cl;

		private static final int MIN_WIDTH = 20;
		private static final int HEIGHT = 24;

		HeroBtn ( HeroClass cl ){
			super(Chrome.Type.GREY_BUTTON_TR, "");

			this.cl = cl;

			icon(new com.shatteredpixel.shatteredpixeldungeon.ui.HeroPortrait(cl,30));

		}

		@Override
		public void update() {
			super.update();
			if (cl != GamesInProgress.selectedClass){
				if (!cl.isUnlocked()){
					icon.brightness(0.42f);
				} else {
					icon.brightness(0.82f);
				}
			} else {
				icon.brightness(1f);
			}
		}

        @Override protected String hoverText(){return Messages.titleCase(cl.title());}

		@Override
		protected void onClick() {
			super.onClick();

			if (GamesInProgress.selectedClass == cl) {
				Window w = new WndHeroInfo(cl);
				if (landscape()){
					w.offset((int)Math.max(0,(Camera.main.width/3-w.camera.width/2-6)), 0);
				}
				ShatteredPixelDungeon.scene().addToFront(w);
			} else {
				setSelectedHero(cl);
			}
		}

	}

	private class GameOptions extends Component {

		private NinePatch bg;

		private ArrayList<StyledButton> buttons;
		private ArrayList<ColorBlock> spacers;

		protected StyledButton challengeButton;

		@Override
		protected void createChildren() {

			bg = Chrome.get(Chrome.Type.GREY_BUTTON_TR);
			add(bg);

			buttons = new ArrayList<>();
			spacers = new ArrayList<>();
			StyledButton seedButton = new StyledButton(Chrome.Type.BLANK, Messages.get(HeroSelectScene.class, "custom_seed"), 6){
				@Override
				protected void onClick() {
					if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
						ShatteredPixelDungeon.scene().addToFront( new WndTitledMessage(
								Icons.get(Icons.SEED),
								Messages.get(HeroSelectScene.class, "custom_seed"),
								Messages.get(HeroSelectScene.class, "custom_seed_nowin"))
						);
						return;
					}

					String existingSeedtext = SPDSettings.customSeed();
					ShatteredPixelDungeon.scene().addToFront( new WndTextInput(Messages.get(HeroSelectScene.class, "custom_seed_title"),
							Messages.get(HeroSelectScene.class, "custom_seed_desc"),
							existingSeedtext,
							20,
							false,
							Messages.get(HeroSelectScene.class, "custom_seed_set"),
							Messages.get(HeroSelectScene.class, "custom_seed_clear")){
						@Override
						public void onSelect(boolean positive, String text) {
							text = DungeonSeed.formatText(text);
							long seed = DungeonSeed.convertFromText(text);

							if (positive && seed != -1){

								for (GamesInProgress.Info info : GamesInProgress.checkAll()){
									if (info.customSeed.isEmpty() && info.seed == seed){
										SPDSettings.customSeed("");
										icon.resetColor();
										ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(HeroSelectScene.class, "custom_seed_duplicate")));
										return;
									}
								}

								SPDSettings.customSeed(text);
								icon.hardlight(1f, 1.5f, 0.67f);
							} else {
								SPDSettings.customSeed("");
								icon.resetColor();
							}
							updateOptionsColor();
						}
					});
				}
			};
			seedButton.leftJustify = true;
			seedButton.icon(Icons.get(Icons.SEED));
			if (!SPDSettings.customSeed().isEmpty()) seedButton.icon().hardlight(1f, 1.5f, 0.67f);;
			buttons.add(seedButton);
			add(seedButton);

			StyledButton dailyButton = new StyledButton(Chrome.Type.BLANK, Messages.get(HeroSelectScene.class, "daily"), 6){

				private static final long SECOND = 1000;
				private static final long MINUTE = 60 * SECOND;
				private static final long HOUR = 60 * MINUTE;
				private static final long DAY = 24 * HOUR;

				@Override
				protected void onClick() {
					super.onClick();

					if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
						ShatteredPixelDungeon.scene().addToFront( new WndTitledMessage(
								Icons.get(Icons.CALENDAR),
								Messages.get(HeroSelectScene.class, "daily"),
								Messages.get(HeroSelectScene.class, "daily_nowin"))
						);
						return;
					}

					long diff = (SPDSettings.lastDaily() + DAY) - Game.realTime;
					if (diff > 24*HOUR){
						ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(HeroSelectScene.class, "daily_unavailable_long", (diff / DAY)+1)));
						return;
					}

					for (GamesInProgress.Info game : GamesInProgress.checkAll()){
						if (game.daily){
							ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(HeroSelectScene.class, "daily_existing")));
							return;
						}
					}

					Image icon = Icons.get(Icons.CALENDAR);
					if (diff <= 0)  icon.hardlight(0.5f, 1f, 2f);
					else            icon.hardlight(1f, 0.5f, 2f);
					ShatteredPixelDungeon.scene().addToFront(new WndOptions(
							icon,
							Messages.get(HeroSelectScene.class, "daily"),
							diff > 0 ?
								Messages.get(HeroSelectScene.class, "daily_repeat") :
								Messages.get(HeroSelectScene.class, "daily_desc"),
							Messages.get(HeroSelectScene.class, "daily_yes"),
							Messages.get(HeroSelectScene.class, "daily_no")){
						@Override
						protected void onSelect(int index) {
							if (index == 0){
								if (diff <= 0) {
									long time = Game.realTime - (Game.realTime % DAY);

									//earliest possible daily for v4.0 is Apr 01 2026
									//which is 20,544 days after Jan 1 1970
									time = Math.max(time, 20_544 * DAY);

									SPDSettings.lastDaily(time);
									Dungeon.dailyReplay = false;
								} else {
									Dungeon.dailyReplay = true;
								}

								Dungeon.hero = null;
								Dungeon.daily = true;
								Dungeon.initSeed();
								ActionIndicator.clearAction();
								InterlevelScene.mode = InterlevelScene.Mode.DESCEND;

								Game.switchScene( InterlevelScene.class );
							}
						}
					});
				}

				private long timeToUpdate = 0;

				private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);
				{
					dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				}

				@Override
				public void update() {
					super.update();

					if (Game.realTime > timeToUpdate && visible){
						long diff = (SPDSettings.lastDaily() + DAY) - Game.realTime;

						if (diff > 0){
							if (diff > 30*HOUR){
								text("30:00:00+");
							} else {
								text(dateFormat.format(new Date(diff)));
							}
							timeToUpdate = Game.realTime + SECOND;
						} else {
							text(Messages.get(HeroSelectScene.class, "daily"));
							timeToUpdate = Long.MAX_VALUE;
						}
					}

				}
			};
			dailyButton.leftJustify = true;
			dailyButton.icon(Icons.get(Icons.CALENDAR));
			add(dailyButton);
			buttons.add(dailyButton);

			challengeButton = new StyledButton(Chrome.Type.BLANK, Messages.get(WndChallenges.class, "title"), 6){
				@Override
				protected void onClick() {
					if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
						ShatteredPixelDungeon.scene().addToFront( new WndTitledMessage(
								Icons.get(Icons.CHALLENGE_GREY),
								Messages.get(WndChallenges.class, "title"),
								Messages.get(HeroSelectScene.class, "challenges_nowin")
						));
						return;
					}

					ShatteredPixelDungeon.scene().addToFront(new WndChallenges(SPDSettings.challenges(), true) {
						public void onBackPressed() {
							super.onBackPressed();
							icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
							updateOptionsColor();
						}
					} );
				}
			};
			challengeButton.leftJustify = true;
			challengeButton.icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
			add(challengeButton);
			buttons.add(challengeButton);

			int unlockedCount = 0;
			for (HeroClass cls : HeroClass.values()){
				if (cls.isUnlocked()) unlockedCount++;
			}

			if (unlockedCount >= 2) {
				StyledButton randomButton = new StyledButton(Chrome.Type.BLANK, Messages.get(HeroSelectScene.class, "randomize"), 6) {
					@Override
					protected void onClick() {

						if (Badges.isUnlocked(Badges.Badge.VICTORY) || DeviceCompat.isDebug()){
							ShatteredPixelDungeon.scene().addToFront(new WndRandomize());
						} else {

							HeroClass randomCls;
							do {
								randomCls = Random.oneOf(HeroClass.values());
							} while (!randomCls.isUnlocked());
							setSelectedHero(randomCls);
							GamesInProgress.randomizedClass = true;
						}
					}
				};
				randomButton.leftJustify = true;
				randomButton.icon(Icons.SHUFFLE.get());
				buttons.add(randomButton);
				add(randomButton);
			}

			for (int i = 1; i < buttons.size(); i++){
				ColorBlock spc = new ColorBlock(1, 1, 0xFF000000);
				add(spc);
				spacers.add(spc);
			}
		}

		private class WndRandomize extends Window {

			CheckBox chkHero;
			CheckBox chkChals;
			OptionSlider optChals;

			public WndRandomize(){
				super();

				chkHero = new CheckBox(Messages.get(HeroSelectScene.class, "randomize_hero")){
					@Override
					public void checked(boolean value) {
						super.checked(value);
						heroWasRandomized = value;
					}
				};
				chkHero.setRect(0, 0, 120, 16);
				chkHero.checked(heroWasRandomized);
				add(chkHero);

				chkChals = new CheckBox(Messages.get(HeroSelectScene.class, "randomize_chals")){
					@Override
					public void checked(boolean value) {
						super.checked(value);
						optChals.enable(value);
						chalWasRandomized = value;
					}
				};
				chkChals.setRect(0, 20, 120, 16);
				add(chkChals);

				int max = Challenges.MAX_CHALS;
				optChals = new OptionSlider(Messages.get(HeroSelectScene.class, "randomize_chals_title"), "0", Integer.toString(max), 0, max) {
					@Override
					protected void onChange() {
						//do nothing immediately
					}
				};
				optChals.enable(false);
				optChals.setSelectedValue(Challenges.activeChallenges(SPDSettings.challenges()));
				optChals.setRect(0, 38, 120, 22);
				add(optChals);

				chkChals.checked(chalWasRandomized);

				RedButton btnCancel = new RedButton(Messages.get(HeroSelectScene.class, "randomize_cancel")){
					@Override
					protected void onClick() {
						super.onClick();
						hide();
					}
				};
				btnCancel.setRect(61, 64, 60, 16);
				add(btnCancel);

				RedButton btnConfirm = new RedButton(Messages.get(HeroSelectScene.class, "randomize_confirm")){
					@Override
					protected void onClick() {
						super.onClick();
						hide();

						if (chkChals.checked()){
							int chals = optChals.getSelectedValue();
							ArrayList<Integer> chalMasks = new ArrayList<>();
							for (int i = 0; i < Challenges.MAX_CHALS; i++){
								chalMasks.add((int)Math.pow(2, i));
							}
							Random.shuffle(chalMasks);
							int mask = 0;
							for (int i = 0; i < chals; i++){
								mask += chalMasks.remove(0);
							}
							SPDSettings.challenges(mask);
							challengeButton.icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
							ShatteredPixelDungeon.scene().addToFront(new WndChallenges(mask, false));
						}

						if (chkHero.checked()){
							HeroClass randomCls;
							do {
								randomCls = Random.oneOf(HeroClass.values());
							} while (!randomCls.isUnlocked());
							setSelectedHero(randomCls);
							GamesInProgress.randomizedClass = true;
						} else {
							setSelectedHero(GamesInProgress.selectedClass);
						}
					}
				};
				btnConfirm.setRect(0, 64, 60, 16);
				add(btnConfirm);

				resize(120, (int)btnConfirm.bottom());

			}

		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;

			int width = 0;
			for (StyledButton btn : buttons){
				if (width < btn.reqWidth()) width = (int)btn.reqWidth();
			}
			width += bg.marginHor();

			int top = (int)y + bg.marginTop() - 1;
			int i = 0;
			for (StyledButton btn : buttons){
				btn.setRect(x+bg.marginLeft(), top, width - bg.marginHor(), 16);
				top = (int)btn.bottom();
				if (i < spacers.size()) {
					spacers.get(i).size(btn.width(), 1);
					spacers.get(i).x = btn.left();
					spacers.get(i).y = PixelScene.align(btn.bottom()-0.5f);
					i++;
				}
			}

			this.width = width;
			this.height = top+bg.marginBottom()-y-1;
			bg.size(this.width, this.height);

		}

		private void alpha( float value ){
			bg.alpha(value);

			for (StyledButton btn : buttons){
				btn.alpha(value);
			}

			for (ColorBlock spc : spacers){
				spc.alpha(value);
			}
		}
	}

}
