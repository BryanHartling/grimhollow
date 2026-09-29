// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.watabou.utils.Bundle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Bounded, per-save message history, independent of the short on-screen log. */
public final class MessageHistory {
    public static final int LIMIT=500;
    private static final ArrayDeque<String> entries=new ArrayDeque<>();
    private MessageHistory(){}
    public static synchronized void add(String message){
        if(message==null || message.trim().isEmpty())return;
        entries.addLast(message);
        while(entries.size()>LIMIT)entries.removeFirst();
    }
    public static synchronized List<String> snapshot(){return new ArrayList<>(entries);}
    public static synchronized void clear(){entries.clear();}
    public static synchronized void store(Bundle b){b.put("message_history",entries.toArray(new String[0]));}
    public static synchronized void restore(Bundle b){
        clear();
        if(b.contains("message_history"))for(String message:b.getStringArray("message_history"))add(message);
    }
}
