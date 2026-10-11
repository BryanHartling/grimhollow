// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.android;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.GameUpdates;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.ReleaseInfo;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public final class AndroidUpdateInstaller implements GameUpdates.Installer {
    @Override public String platform() { return "android"; }
    @Override public int androidApi() { return Build.VERSION.SDK_INT; }
    @Override public File extraPreferences() { return new File(AndroidLauncher.instance.getFilesDir().getParentFile(),"shared_prefs"); }

    @SuppressWarnings("deprecation")
    @Override public String install(File packageFile, ReleaseInfo release) throws Exception {
        android.app.Activity activity = AndroidLauncher.instance;
        PackageManager manager = activity.getPackageManager();
        int flags = Build.VERSION.SDK_INT >= 28 ? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES;
        PackageInfo incoming = manager.getPackageArchiveInfo(packageFile.getAbsolutePath(),flags);
        PackageInfo installed = manager.getPackageInfo(activity.getPackageName(),flags);
        if(incoming==null || !activity.getPackageName().equals(incoming.packageName)
                || incoming.versionCode!=release.code || incoming.versionCode<=installed.versionCode
                || !release.version.equals(incoming.versionName))throw new IOException("The APK does not match this game or release.");
        Signature[] current = Build.VERSION.SDK_INT>=28 ? installed.signingInfo.getApkContentsSigners() : installed.signatures;
        Signature[] next = Build.VERSION.SDK_INT>=28 ? incoming.signingInfo.getApkContentsSigners() : incoming.signatures;
        if(current==null || next==null || current.length!=1 || next.length!=1 || !Arrays.equals(current[0].toByteArray(),next[0].toByteArray()))
            throw new IOException("This APK uses a different signing identity. Installation was stopped to protect your saved games. Keep this installation and report the mismatch.");
        File shared = new File(activity.getFilesDir(),"updates/install.apk");
        // Expose exactly one verified APK, never the saved-game directory or an arbitrary path.
        try (java.io.InputStream input=new java.io.FileInputStream(packageFile);java.io.OutputStream output=new java.io.FileOutputStream(shared)) {
            byte[] bytes=new byte[65536];int read;while((read=input.read(bytes))!=-1)output.write(bytes,0,read);
        }
        release.verifyFile(shared);
        if(Build.VERSION.SDK_INT>=26 && !manager.canRequestPackageInstalls()) {
            activity.runOnUiThread(() -> activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName()))));
            return "Allow installations from Grimhollow, return to this screen, then select Install again.";
        }
        Uri uri=Uri.parse("content://"+activity.getPackageName()+".updates/install.apk");
        Intent install=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.runOnUiThread(() -> {
            try { activity.startActivity(install); }
            catch(Exception e) { com.badlogic.gdx.Gdx.app.postRunnable(() -> { GameUpdates.detail="Android could not open its installer. Your current game is unchanged.";GameUpdates.state=GameUpdates.State.ERROR; }); }
        });
        return "Approve the update in Android's installer. Your current installation stays intact if you cancel.";
    }
}
