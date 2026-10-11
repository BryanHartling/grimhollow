// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.GameUpdates;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.ReleaseInfo;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

/** Painted native controls; release notes scroll independently of the download/install buttons. */
public class WndUpdater extends Window {
    private final RenderedTextBlock status, notes;
    private final RedButton action;
    private final ScrollPane scroll;
    private GameUpdates.State shown;
    private String shownDetail;
    private final int contentWidth;

    public WndUpdater() {
        contentWidth = Math.min(PixelScene.landscape() ? 220 : 170, (int)PixelScene.uiCamera.width - 20);
        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 10);
        title.hardlight(TITLE_COLOR); title.maxWidth(contentWidth); title.setPos(0,0); add(title);
        status = PixelScene.renderTextBlock(7); status.maxWidth(contentWidth); status.setPos(0,title.bottom()+5); add(status);
        Component body = new Component(); notes = PixelScene.renderTextBlock(6); notes.maxWidth(contentWidth-5); body.add(notes);
        scroll = new ScrollPane(body); add(scroll);
        action = new RedButton("") { @Override protected void onClick() { act(); } }; add(action);
        RedButton later = new RedButton(Messages.get(this, "later")) { @Override protected void onClick() { GameUpdates.defer(); hide(); } }; add(later);
        int total = Math.min(230, (int)PixelScene.uiCamera.height - 30);
        action.setRect(0,total-43,contentWidth,20); later.setRect(0,total-20,contentWidth,20);
        CheckBox auto = new CheckBox(Messages.get(this,"automatic")) {
            @Override protected void onClick() { super.onClick(); SPDSettings.updates(checked()); }
        }; auto.checked(SPDSettings.updates()); auto.setRect(0,total-68,contentWidth,20); add(auto);
        resize(contentWidth,total); refresh();
    }

    private void act() {
        switch (GameUpdates.state) {
            case DOWNLOADING: GameUpdates.cancel(); break;
            case READY:
                if (!(Game.scene() instanceof TitleScene)) return;
                Game.scene().add(new WndOptions(Messages.get(this,"install_title"), Messages.get(this,DeviceCompat.isAndroid()?"install_android":"install_windows"),Messages.get(this,"install"),Messages.get(this,"later")) {
                    @Override protected void onSelect(int index) { if(index==0)GameUpdates.install(); }
                }); break;
            case AVAILABLE:
                if (SPDSettings.WiFi() && !Game.platform.connectedToUnmeteredNetwork()) {
                    Game.scene().add(new WndOptions(Messages.get(this,"mobile_title"),Messages.get(this,"mobile_body"),Messages.get(this,"download"),Messages.get(this,"later")) {
                        @Override protected void onSelect(int index) { if(index==0)GameUpdates.download(); }
                    });
                } else GameUpdates.download(); break;
            case ERROR:
                if (GameUpdates.release != null) GameUpdates.download(); else GameUpdates.check(true); break;
            default: GameUpdates.check(true);
        }
    }

    @Override public void update() { super.update(); refresh(); }
    private void refresh() {
        GameUpdates.State current = GameUpdates.state;
        if (shown == current && current != GameUpdates.State.DOWNLOADING && String.valueOf(shownDetail).equals(GameUpdates.detail)) return;
        shown = current; shownDetail = GameUpdates.detail;
        ReleaseInfo release = GameUpdates.release;
        String text = Messages.get(this,"state_" + current.name().toLowerCase(java.util.Locale.ROOT));
        if(release!=null)text = "Grimhollow " + release.version + "\n" + text;
        if(current==GameUpdates.State.DOWNLOADING && release!=null)text += " " + Math.min(100,100*GameUpdates.downloaded/release.asset.size) + "%";
        status.text(text,contentWidth);
        String body=release==null ? Messages.get(this,"explanation") : release.notes.isEmpty()?Messages.get(this,"ready_notes"):release.notes;
        if(!GameUpdates.detail.isEmpty())body=GameUpdates.detail+"\n\n"+body;
        if(current==GameUpdates.State.READY && !(Game.scene() instanceof TitleScene))body=Messages.get(this,"return_home")+"\n\n"+body;
        notes.text(body,contentWidth-5);
        scroll.content().setSize(contentWidth-5,notes.height()+4);
        scroll.setRect(0,status.bottom()+5,contentWidth,Math.max(15,action.top()-30-status.bottom()));
        boolean active=current!=GameUpdates.State.CHECKING && current!=GameUpdates.State.INSTALLING && (current!=GameUpdates.State.READY || Game.scene() instanceof TitleScene);
        action.enable(active);
        action.text(Messages.get(this,current==GameUpdates.State.DOWNLOADING?"cancel":current==GameUpdates.State.READY?"install":current==GameUpdates.State.AVAILABLE || current==GameUpdates.State.ERROR && release!=null?"download":"check"));
    }
}
