// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.utils.Bundle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Named device presets and immutable rules captured by each run. */
public final class BalanceProfiles {
    private BalanceProfiles() {}
    private static final String LIBRARY="balance_profiles_v1", SELECTED="balance_selected_v1";
    private static final String RUN="balance_run_profile", TUNING="balance_run_tuning", EXCLUSIONS="balance_run_exclusions";
    private static Profile run;

    public static final class Profile {
        public final String id,name,tuning,exclusions;
        public final int revision;
        private Profile(String id,String name,int revision,String tuning,String exclusions){
            this.id=id;this.name=name;this.revision=revision;this.tuning=tuning;this.exclusions=exclusions;
        }
        public boolean isDefault(){return id.equals("default");}
        public String boardKey(){return isDefault()?"default":id+"-"+revision;}
        public String boardName(){return isDefault()?name:name+" (v"+revision+")";}
        private Bundle bundle(){Bundle b=new Bundle();b.put("id",id);b.put("name",name);b.put("revision",revision);b.put("tuning",tuning);b.put("exclusions",exclusions);return b;}
        private boolean matches(){return tuning.equals(BalanceTuning.snapshot())&&exclusions.equals(GenerationToggles.snapshot());}
    }
    public static Profile defaults(){
        StringBuilder settings=new StringBuilder();
        for(BalanceTuning.Key key:BalanceTuning.Key.values())settings.append(key.id()).append('=').append(key.baseline).append(';');
        return new Profile("default","Default",0,settings.toString(),"");
    }
    private static Profile read(Bundle b){
        String id=b.getString("id"),name=b.getString("name");int revision=b.getInt("revision");
        if(id.equals("default"))return new Profile("default","Default",0,b.contains("tuning")?b.getString("tuning"):defaults().tuning,"");
        if(!id.matches("[0-9a-f-]{36}")||name.trim().isEmpty()||revision<1)return null;
        return new Profile(id,name,revision,b.getString("tuning"),b.getString("exclusions"));
    }
    static Bundle decode(String text){
        try{return Bundle.read(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));}
        catch(IOException|RuntimeException invalid){return new Bundle();}
    }
    public static List<Profile> list(){
        List<Profile> profiles=new ArrayList<>();profiles.add(defaults());
        Bundle library=decode(SPDSettings.getString(LIBRARY,"{}"));
        for(String id:library.contains("ids")?library.getStringArray("ids"):new String[0]){
            Profile p=read(library.getBundle(id));if(p!=null&&!p.isDefault()&&profiles.stream().noneMatch(old->old.id.equals(p.id)))profiles.add(p);
        }
        return profiles;
    }
    public static Profile selected(){
        String id=SPDSettings.getString(SELECTED,"default");
        for(Profile p:list())if(p.id.equals(id))return p;
        return defaults();
    }
    private static void persist(List<Profile> profiles){
        Bundle b=new Bundle();List<String> ids=new ArrayList<>();
        for(Profile p:profiles)if(!p.isDefault()){ids.add(p.id);b.put(p.id,p.bundle());}
        b.put("ids",ids.toArray(new String[0]));SPDSettings.put(LIBRARY,b.toString());
    }
    private static String validName(String name,String except){
        name=name==null?"":name.trim();
        if(name.isEmpty()||name.length()>32||name.chars().anyMatch(c->Character.isISOControl(c)||c=='_'))
            throw new IllegalArgumentException("Use a profile name of 1–32 characters, without underscores or line breaks.");
        for(Profile p:list())if(!p.id.equals(except)&&p.name.equalsIgnoreCase(name))throw new IllegalArgumentException("That profile name already exists.");
        return name;
    }
    public static Profile create(String name){
        Profile p=new Profile(UUID.randomUUID().toString(),validName(name,null),1,BalanceTuning.snapshot(),GenerationToggles.snapshot());
        List<Profile> profiles=list();profiles.add(p);persist(profiles);return p;
    }
    public static Profile update(String id){
        List<Profile> profiles=list();
        for(int i=0;i<profiles.size();i++)if(profiles.get(i).id.equals(id)){
            Profile old=profiles.get(i);if(old.isDefault())throw new IllegalArgumentException("Default is protected. Save a new profile instead.");
            if(old.matches())return old;
            Profile p=new Profile(id,old.name,old.revision+1,BalanceTuning.snapshot(),GenerationToggles.snapshot());
            profiles.set(i,p);persist(profiles);return p;
        }
        throw new IllegalArgumentException("Profile no longer exists.");
    }
    public static void rename(String id,String name){
        List<Profile> profiles=list();name=validName(name,id);
        for(int i=0;i<profiles.size();i++)if(profiles.get(i).id.equals(id)){
            Profile p=profiles.get(i);if(p.isDefault())throw new IllegalArgumentException("Default is protected.");
            profiles.set(i,new Profile(id,name,p.revision,p.tuning,p.exclusions));persist(profiles);return;
        }
        throw new IllegalArgumentException("Profile no longer exists.");
    }
    public static void select(String id){
        for(Profile p:list())if(p.id.equals(id)){
            // Change future-run preferences only. A live run retains its own values.
            SPDSettings.put("balance_profile_v1",p.tuning);SPDSettings.put("generation_exclusions_v1",p.exclusions);SPDSettings.put(SELECTED,id);return;
        }
        throw new IllegalArgumentException("Profile no longer exists.");
    }
    public static void delete(String id){
        if(id.equals("default"))throw new IllegalArgumentException("Default is protected.");
        List<Profile> profiles=list();if(!profiles.removeIf(p->p.id.equals(id)))return;
        boolean wasSelected=selected().id.equals(id);persist(profiles);if(wasSelected)select("default");
    }
    public static void beginRun(){Profile p=selected();run=p.matches()?p:null;}
    public static boolean modifiedRun(){return run==null?BalanceTuning.changedCount()>0||GenerationToggles.changedCount()>0:!run.matches();}
    public static boolean namedRun(){return run!=null&&!run.isDefault();}
    public static String runName(){return run==null?"":run.name;}
    public static String runBoardKey(){return run==null?"default":run.boardKey();}
    public static String runBoardName(){return run==null?"Default":run.boardName();}
    public static String savedName(Bundle b){return b.contains(RUN)?b.getBundle(RUN).getString("name"):"";}
    public static void storeRun(Bundle b){
        if(run!=null)b.put(RUN,run.bundle());
        b.put(TUNING,BalanceTuning.snapshot());b.put(EXCLUSIONS,GenerationToggles.snapshot());
    }
    public static void restoreRun(Bundle b){
        run=b.contains(RUN)?read(b.getBundle(RUN)):null;
        if(b.contains(TUNING)){
            BalanceTuning.applySnapshot(b.getString(TUNING));GenerationToggles.applySnapshot(b.getString(EXCLUSIONS));
        }else{
            BalanceTuning.restore(b);
            // Older saves did not record generation exclusions. Preserve their available legacy profile once.
            if(b.getBoolean("custom_balance"))GenerationToggles.load();else GenerationToggles.applySnapshot("");
        }
        if(run==null&&!b.getBoolean("custom_balance")&&BalanceTuning.changedCount()==0&&GenerationToggles.changedCount()==0)run=defaults();
    }
    public static String summary(Profile p){
        StringBuilder s=new StringBuilder();Map<String,Integer> settings=BalanceTuning.parseSnapshot(p.tuning);
        for(BalanceTuning.Key key:BalanceTuning.Key.values()){
            int value=settings.getOrDefault(key.id(),key.baseline);
            if(value!=key.baseline)s.append(com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(BalanceTuning.class,key.id())).append(": ").append(key.display(value)).append('\n');
        }
        if(!p.exclusions.isEmpty())s.append("Generation exclusions: ").append(p.exclusions.split(";").length).append('\n');
        return s.length()==0?"Current standard settings.":s.toString().trim();
    }
}
