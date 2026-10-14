// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.FileUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Independent local tables; the existing Default table and daily history stay untouched. */
public final class ProfileRankings {
    private ProfileRankings() {}
    private static final String INDEX="balance_rankings_v1";
    public static String viewKey="default",viewName="Default";
    public static final class Board {
        public final String key,name;
        public final ArrayList<Rankings.Record> records=new ArrayList<>();
        public int lastRecord=-1,totalNumber,wonNumber;
        private Board(String key,String name){this.key=key;this.name=name;}
    }
    public static String file(String key){
        try{
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder name=new StringBuilder("rankings-profile-");for(byte b:hash)name.append(String.format(Locale.ROOT,"%02x",b&255));
            return name+".dat";
        }catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
    public static Board load(String key,String name){
        Board board=new Board(key,name);
        try{
            Bundle b=FileUtils.bundleFromFile(file(key));
            for(Bundlable record:b.getCollection("records"))if(record instanceof Rankings.Record)board.records.add((Rankings.Record)record);
            board.lastRecord=b.getInt("latest");board.totalNumber=b.getInt("total");board.wonNumber=b.getInt("won");
        }catch(IOException missing){ /* An unplayed profile has an empty table. */ }
        return board;
    }
    public static void submit(Rankings.Record record){
        Board b=load(record.profileKey,record.profileName);b.records.add(record);b.records.sort(Rankings.scoreComparator);
        b.lastRecord=b.records.indexOf(record);
        while(b.records.size()>Rankings.TABLE_SIZE){
            int last=b.records.size()-1;
            if(b.lastRecord==last){b.records.remove(last-1);b.lastRecord--;}else b.records.remove(last);
        }
        if(record.customSeed.isEmpty()){b.totalNumber++;if(record.win)b.wonNumber++;}
        Bundle data=new Bundle();data.put("records",b.records);data.put("latest",b.lastRecord);data.put("total",b.totalNumber);data.put("won",b.wonNumber);
        try{
            FileUtils.bundleToFile(file(b.key),data);
            Bundle index=BalanceProfiles.decode(SPDSettings.getString(INDEX,"{}"));index.put(b.key,b.name);SPDSettings.put(INDEX,index.toString());
        }catch(IOException error){ShatteredPixelDungeon.reportException(error);}
        viewKey=b.key;viewName=b.name;
    }
    public static LinkedHashMap<String,String> boards(){
        LinkedHashMap<String,String> boards=new LinkedHashMap<>();boards.put("default","Default");
        Bundle index=BalanceProfiles.decode(SPDSettings.getString(INDEX,"{}"));
        for(String key:index.getKeys())boards.put(key,index.getString(key)+" [archived]");
        for(BalanceProfiles.Profile p:BalanceProfiles.list())boards.put(p.boardKey(),p.boardName());
        return boards;
    }
}
