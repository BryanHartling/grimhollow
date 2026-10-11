// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.GameUpdates;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.ReleaseInfo;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class WindowsUpdateInstaller implements GameUpdates.Installer {
    private final long started = System.nanoTime();
    private boolean marked;
    @Override public String platform() { return "windows-x64"; }
    @Override public int androidApi() { return 0; }

    @Override public String install(File packageFile, ReleaseInfo release) throws Exception {
        File jar = new File(DesktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File application = jar.getParentFile().getParentFile().getCanonicalFile();
        if (!jar.isFile() || !application.getName().equals("Grimhollow") || !new File(application,"Grimhollow.exe").isFile()
                || !new File(application,"update-package.json").isFile())
            throw new IOException("Automatic installation requires the packaged Windows launcher. Download the Windows package for this first installation.");
        File permission = new File(application,".update-write-check");
        try (FileOutputStream output = new FileOutputStream(permission)) { output.write(1); }
        finally { permission.delete(); }
        File cache = GameUpdates.directory().getCanonicalFile();
        File script = new File(cache,"windows-update.ps1");
        Files.write(script.toPath(),Gdx.files.internal("updates/windows-update.ps1").readBytes());
        // Copy outside the application so Windows can release and replace every launcher/runtime file.
        String powershell = new File(System.getenv("SystemRoot"),"System32/WindowsPowerShell/v1.0/powershell.exe").getAbsolutePath();
        new ProcessBuilder(powershell,"-NoProfile","-NonInteractive","-WindowStyle","Hidden","-ExecutionPolicy","Bypass","-File",script.getAbsolutePath(),
                "-Target",application.getAbsolutePath(),"-Archive",packageFile.getCanonicalPath(),"-ExpectedSHA256",release.asset.sha256,
                "-VersionCode",Integer.toString(release.code),"-ParentPid",Long.toString(ProcessHandle.current().pid()),"-StatusDirectory",cache.getAbsolutePath())
                .redirectErrorStream(true).redirectOutput(new File(cache,"installer.log")).start();
        Gdx.app.exit();
        return "Installing after the game closes. The previous application and save backup will be retained.";
    }

    @Override public void healthy() {
        if (marked || System.nanoTime()-started < TimeUnit.SECONDS.toNanos(3)) return;
        marked=true;
        String path=System.getenv("GRIMHOLLOW_UPDATE_HEALTH_FILE"),token=System.getenv("GRIMHOLLOW_UPDATE_HEALTH_TOKEN");
        if(path==null || token==null || !token.matches("[a-f0-9]{32}"))return;
        try {
            File file=new File(path).getCanonicalFile();
            if(!file.equals(new File(GameUpdates.directory().getCanonicalFile(),"launch-health.txt")))return;
            Files.write(file.toPath(),token.getBytes(StandardCharsets.US_ASCII));
        } catch(IOException ignored) { /* The external helper fails closed and restores the old app. */ }
    }
}
