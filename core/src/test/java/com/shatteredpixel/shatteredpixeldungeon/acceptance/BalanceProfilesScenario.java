// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.acceptance;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.HatchlingMimic;
import com.watabou.utils.Bundle;
import static com.shatteredpixel.shatteredpixeldungeon.BalanceTuning.Key.*;

/** Exercises persisted profiles, save isolation and real ranking submission in the existing smoke gate. */
final class BalanceProfilesScenario {
    private static void check(boolean ok,String reason){if(!ok)throw new AssertionError("Balance profiles: "+reason);}
    static void run() throws Exception {
        SPDSettings.put("balance_profiles_v1","{}");SPDSettings.put("balance_selected_v1","default");
        BalanceTuning.reset();GenerationToggles.reset();Playtest.reset();
        String seeded=TuningScenario.sequence();
        BalanceProfiles.select("default");Playtest.reset();
        check(seeded.equals(TuningScenario.sequence()),"Default consumes identical seeded RNG");
        BalanceTuning.setShared(FLOOR_LOOT,200);BalanceTuning.setShared(DRAGON_HEALTH,240);GenerationToggles.toggle(HatchlingMimic.class);
        BalanceProfiles.Profile high=BalanceProfiles.create("High Loot");BalanceProfiles.select(high.id);Dungeon.init();
        check(!Playtest.customBalance()&&Playtest.rankingEligible()&&!Playtest.enabled(),"named standard is ranked in its own table without enabling tools");
        check(Playtest.unranked(),"named profiles must not enter Default badge/bones/catalog eligibility");
        check(BalanceTuning.get(FLOOR_LOOT)==200&&!GenerationToggles.allowed(HatchlingMimic.class),"selected numeric and generation values");
        Bundle first=new Bundle();Playtest.store(first);String firstKey=BalanceProfiles.runBoardKey();
        check(!first.getBoolean("custom_balance")&&BalanceProfiles.savedName(first).equals("High Loot"),"named save label");
        Dungeon.switchLevel(Dungeon.newLevel(),-1);Dungeon.saveAll();
        BalanceTuning.setShared(FLOOR_LOOT,50);GenerationToggles.reset();BalanceProfiles.Profile low=BalanceProfiles.create("Low Loot");BalanceProfiles.select(low.id);
        check(BalanceProfiles.runName().equals("High Loot"),"selecting a future profile does not replace live identity");
        Dungeon.loadGame(GamesInProgress.curSlot);Dungeon.switchLevel(Dungeon.loadLevel(GamesInProgress.curSlot),Dungeon.hero.pos);
        check(BalanceTuning.get(FLOOR_LOOT)==200&&!GenerationToggles.allowed(HatchlingMimic.class)&&BalanceProfiles.runBoardKey().equals(firstKey)&&!Playtest.customBalance(),"full disk save restores original rules and generation exclusions");
        Playtest.reset();check(BalanceProfiles.runName().equals("Low Loot")&&BalanceTuning.get(FLOOR_LOOT)==50&&GenerationToggles.allowed(HatchlingMimic.class),"future games use selected standard");
        BalanceProfiles.select(high.id);BalanceTuning.loadShared();GenerationToggles.load();Playtest.reset();
        BalanceTuning.setShared(FLOOR_LOOT,175);check(Playtest.customBalance(),"editing a live run excludes its scores");
        BalanceProfiles.Profile revision=BalanceProfiles.update(high.id);check(revision.revision==2&&!revision.boardKey().equals(firstKey),"rule updates create distinct ranking revisions");
        BalanceProfiles.select(high.id);Playtest.reset();Bundle second=new Bundle();Playtest.store(second);
        BalanceProfiles.rename(high.id,"More Treasure");check(BalanceProfiles.selected().name.equals("More Treasure")&&BalanceProfiles.selected().boardKey().equals(revision.boardKey()),"rename retains revision identity");
        com.badlogic.gdx.Preferences disk=new com.badlogic.gdx.backends.headless.HeadlessPreferences(com.badlogic.gdx.Gdx.files.absolute(System.getProperty("grimhollow.smokeOutput")+"/prefs/settings.xml"));
        SPDSettings.set(disk);Playtest.reset();check(BalanceProfiles.selected().name.equals("More Treasure")&&BalanceTuning.get(FLOOR_LOOT)==175,"library, selected profile and values survive preference reload");
        Playtest.restore(first);check(BalanceTuning.get(FLOOR_LOOT)==200&&BalanceProfiles.runBoardKey().equals(firstKey)&&!Playtest.customBalance(),"prior revision stays playable and eligible");
        Rankings.INSTANCE.load();int defaultSize=Rankings.INSTANCE.records.size(),defaultTotal=Rankings.INSTANCE.totalNumber;
        Rankings.INSTANCE.submit(false,Rat.class);
        ProfileRankings.Board board=ProfileRankings.load(firstKey,"High Loot (v1)");
        check(board.records.size()==1&&board.totalNumber==1&&board.records.get(0).profileKey.equals(firstKey),"real run submits to its profile table");
        check(Rankings.INSTANCE.records.size()==defaultSize&&Rankings.INSTANCE.totalNumber==defaultTotal,"Default scores and totals unchanged");
        Playtest.restore(second);Rankings.INSTANCE.submit(false,Rat.class);
        check(ProfileRankings.load(revision.boardKey(),revision.boardName()).records.size()==1&&ProfileRankings.load(firstKey,"High Loot (v1)").records.size()==1,"revisions do not mix scores");
        Dungeon.daily=true;Dungeon.dailyReplay=true;Rankings.INSTANCE.submit(false,Rat.class);Dungeon.daily=false;Dungeon.dailyReplay=false;
        check(ProfileRankings.load(revision.boardKey(),revision.boardName()).records.size()==1,"daily replays must not enter profile rankings");
        Playtest.enable();int ranked=ProfileRankings.load(revision.boardKey(),revision.boardName()).records.size();Rankings.INSTANCE.submit(false,Rat.class);
        check(ProfileRankings.load(revision.boardKey(),revision.boardName()).records.size()==ranked,"testing tools remain excluded");
        Playtest.restore(second);BalanceTuning.setShared(FLOOR_LOOT,176);Rankings.INSTANCE.submit(false,Rat.class);
        check(ProfileRankings.load(revision.boardKey(),revision.boardName()).records.size()==ranked,"mid-run rule edits remain excluded");
        BalanceProfiles.delete(high.id);check(BalanceProfiles.selected().isDefault(),"deleting the selected preset selects Default");
        Playtest.restore(first);check(BalanceProfiles.runName().equals("High Loot")&&BalanceTuning.get(FLOOR_LOOT)==200&&!Playtest.customBalance(),"deleting and renaming presets cannot alter old saves");
        check(ProfileRankings.boards().containsKey(firstKey)&&ProfileRankings.boards().containsKey(revision.boardKey()),"deleted profile rankings remain accessible");
        for(String name:new String[]{"","Default","Low Loot","bad\nname"}){
            boolean rejected=false;try{BalanceProfiles.create(name);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"invalid or duplicate name: "+name);
        }
        boolean protectedDefault=false;try{BalanceProfiles.delete("default");}catch(IllegalArgumentException expected){protectedDefault=true;}check(protectedDefault,"Default cannot be deleted");
        BalanceProfiles.select("default");Playtest.reset();check(!Playtest.unranked()&&Playtest.rankingEligible()&&BalanceTuning.changedCount()==0&&GenerationToggles.changedCount()==0,"restoring Default enables ordinary future games");
        SPDSettings.put("balance_profiles_v1","{}");SPDSettings.put("balance_rankings_v1","{}");ProfileRankings.viewKey="default";ProfileRankings.viewName="Default";
        System.out.println("BALANCE PROFILES PASS: named presets, selected standard, numeric/generation snapshots, disk restart, immutable saves/revisions/deletion, separate rankings, Default preservation and Playtest/edit exclusions; failures=0");
    }
}
