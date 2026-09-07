// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.util.Arrays;

/** Seeds the real generator and uses the upstream debug descent route, with saves each floor. */
public class SmokeRun {
    public static void main(String[] args) throws Exception {
        Path output=Path.of(System.getProperty("grimhollow.smokeOutput"));
        java.nio.file.Files.createDirectories(output);
        boolean upstream=Arrays.asList(args).contains("--upstream");
        String[] classes=upstream ? Arrays.stream(HeroClass.values()).map(Enum::name).toArray(String[]::new)
                : new String[]{"NECROMANCER","ENCHANTER","PSYCHIC"};
        int failures=0;
        try (PrintWriter log=new PrintWriter(output.resolve("smoke.log").toFile())) {
            HeadlessApplicationConfiguration config=new HeadlessApplicationConfiguration();
            config.updatesPerSecond=-1;
            config.preferencesDirectory=output.resolve("prefs").toString();
            HeadlessApplication app=new HeadlessApplication(new ApplicationAdapter(){},config);
            new ShatteredPixelDungeon(null);
            Game.version="0.1.0"; Game.versionCode=896;
            FileUtils.setDefaultFileProperties(Files.FileType.Absolute,output.resolve("saves").toString()+"/");
            for(String name:classes) for(int seed=0;seed<10;seed++) {
                try {
                    GamesInProgress.selectedClass=HeroClass.valueOf(name);
                    GamesInProgress.curSlot=99;
                    Dungeon.seed=seed;
                    Dungeon.init();
                    if(Dungeon.hero.heroClass!=GamesInProgress.selectedClass) throw new AssertionError("Wrong hero class");
                    for(int depth=1;depth<=6;depth++) {
                        Dungeon.depth=depth;
                        Level level=Dungeon.newLevel();
                        Dungeon.switchLevel(level,-1);
                        if(!Dungeon.level.insideMap(Dungeon.hero.pos)) throw new AssertionError("Invalid hero placement");
                        Dungeon.saveAll();
                        if(depth==6) {
                            Dungeon.loadGame(99);
                            Dungeon.switchLevel(Dungeon.loadLevel(99),Dungeon.hero.pos);
                            if(Dungeon.depth!=6) throw new AssertionError("Save/load depth mismatch");
                        }
                    }
                    String line="PASS "+name+" seed="+seed+" floor=6 save/load=ok";
                    System.out.println(line); log.println(line);
                } catch(Throwable error) {
                    failures++;
                    String line="FAIL "+name+" seed="+seed+": "+error;
                    System.out.println(line); log.println(line); error.printStackTrace(log);
                }
                log.flush();
            }
            app.exit();
            String result="Runs="+(classes.length*10)+" failures="+failures;
            System.out.println(result); log.println(result);
        }
        if(failures>0) System.exit(1);
    }
}
