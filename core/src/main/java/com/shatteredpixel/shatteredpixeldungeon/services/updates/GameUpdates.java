// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.services.updates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUpdater;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Optional release checks. Downloads never alter the running application or game rules. */
public final class GameUpdates {
    public interface Installer {
        String platform();
        int androidApi();
        String install(File packageFile, ReleaseInfo release) throws Exception;
        default void healthy() {}
        default File extraPreferences() { return null; }
    }
    public enum State { IDLE, CHECKING, CURRENT, AVAILABLE, DOWNLOADING, READY, INSTALLING, ERROR }
    private static Installer installer;
    public static volatile State state = State.IDLE;
    public static volatile ReleaseInfo release;
    public static volatile long downloaded;
    public static volatile String detail = "";
    private static long lastCheck;
    private static int promptedCode;
    private static boolean loaded;
    private static final AtomicBoolean cancelled = new AtomicBoolean();
    private GameUpdates() {}

    public static void initialize(Installer value) { installer = value; }
    public static boolean supported() { return installer != null; }
    public static void startupHealthy() { if (supported()) installer.healthy(); }
    public static boolean working() { return busy(); }
    public static File directory() { return FileUtils.getFileHandle("updates").file(); }
    public static File packageFile() { return new File(directory(), "package-" + release.code + (installer.platform().equals("android") ? ".apk" : ".zip")); }
    private static byte[] publicKey() { return Gdx.files.internal("updates/public-key.der").readBytes(); }
    private static boolean busy() { return state == State.CHECKING || state == State.DOWNLOADING || state == State.INSTALLING; }

    public static void home(boolean mayPrompt) {
        if (!supported()) return;
        startupHealthy();
        if (!loaded) { loaded = true; lastCheck=SPDSettings.getLong("update_last_check",0); state = State.CHECKING; worker(() -> { loadCached(); if(state==State.CHECKING)state=State.IDLE; }); }
        if (SPDSettings.updates() && state != State.READY && !busy() && System.currentTimeMillis() - lastCheck > 6*60*60*1000L
                && (!SPDSettings.WiFi() || Game.platform.connectedToUnmeteredNetwork())) check(false);
        if (mayPrompt && SPDSettings.updates() && (state == State.AVAILABLE || state == State.READY) && release != null && promptedCode != release.code
                && System.currentTimeMillis() > SPDSettings.getLong("update_deferred_until", 0)) {
            promptedCode = release.code; Game.scene().add(new WndUpdater());
        }
    }

    public static void open() {
        if (!supported()) return;
        if (state == State.IDLE || state == State.CURRENT || state == State.ERROR) check(true);
        Game.scene().add(new WndUpdater());
    }

    public static void defer() { SPDSettings.put("update_deferred_until", System.currentTimeMillis() + 24*60*60*1000L); }
    public static void channelChanged() {
        if (busy()) return;
        release = null; state = State.IDLE; promptedCode = 0; lastCheck = 0;
    }

    public static void check(boolean manual) {
        if (!supported() || busy() || state == State.READY) return;
        lastCheck = System.currentTimeMillis(); SPDSettings.put("update_last_check",lastCheck); state = State.CHECKING; detail = "";
        final boolean playtests = SPDSettings.betas(); final byte[] key = publicKey();
        worker(() -> {
            try {
                byte[] listing;
                try { listing = UpdateTransport.read("https://api.github.com/repos/" + ReleaseInfo.REPOSITORY + "/releases?per_page=20", 1024*1024); }
                catch (FileNotFoundException e) { state = State.CURRENT; return; }
                JsonValue list = new JsonReader().parse(new String(listing, StandardCharsets.UTF_8));
                if (!list.isArray()) throw new IOException("Invalid release response");
                ReleaseInfo best = null; int examined = 0;
                for (JsonValue candidate = list.child; candidate != null && examined < 5; candidate = candidate.next) {
                    if (candidate.getBoolean("draft", true) || (candidate.getBoolean("prerelease", false) && !playtests)) continue;
                    String tag = candidate.getString("tag_name", "");
                    if (!tag.matches("v[0-9]+\\.[0-9]+\\.[0-9]+(?:-[a-z0-9-]+)?")) continue;
                    boolean manifest = false, signature = false;
                    for (JsonValue asset = candidate.get("assets").child; asset != null; asset = asset.next) {
                        String name = asset.getString("name", "");
                        manifest |= name.equals("release-manifest.json"); signature |= name.equals("release-manifest.sig");
                    }
                    if (!manifest || !signature) continue; // Historical tags are not update releases.
                    examined++;
                    String base = ReleaseInfo.DOWNLOADS + tag + "/";
                    byte[] raw = UpdateTransport.read(base + "release-manifest.json", 32768);
                    byte[] sig = UpdateTransport.read(base + "release-manifest.sig", 2048);
                    ReleaseInfo info = ReleaseInfo.verify(raw, sig, key, tag, installer.platform(), candidate.getString("body", ""));
                    if (!info.newerThan(Game.versionCode, playtests) || (installer.platform().equals("android") && installer.androidApi() < info.minAndroid)) continue;
                    if (best == null || info.code > best.code) {
                        best = info;
                        if (!directory().isDirectory() && !directory().mkdirs()) throw new IOException("Update cache cannot be created");
                        write(new File(directory(), "manifest.json"), raw); write(new File(directory(), "manifest.sig"), sig);
                        write(new File(directory(), "tag.txt"), tag.getBytes(StandardCharsets.UTF_8));
                    }
                }
                release = best; state = best == null ? State.CURRENT : State.AVAILABLE;
            } catch (Exception e) { detail = "Could not check for a trusted update. Check your connection and try again."; state = State.ERROR; }
        });
    }

    private static void loadCached() {
        try {
            File root = directory();
            String tag = new String(read(new File(root, "tag.txt"), 128), StandardCharsets.UTF_8);
            ReleaseInfo cached = ReleaseInfo.verify(read(new File(root, "manifest.json"), 32768), read(new File(root, "manifest.sig"), 2048), publicKey(), tag, installer.platform(), "");
            if (!cached.newerThan(Game.versionCode, SPDSettings.betas())) return;
            release = cached;
            if (packageFile().isFile()) { cached.verifyFile(packageFile()); state = State.READY; }
            else state = State.AVAILABLE;
            File status = new File(root,"install-status.json");
            if(status.isFile()) {
                JsonValue result=new JsonReader().parse(new String(read(status,4096),StandardCharsets.UTF_8));
                if(result.getString("phase","").equals("rolled-back") || result.getString("phase","").equals("failed"))
                    detail="The previous update did not finish. Your current application was retained or restored. You can retry from this screen.";
            }
        } catch (Exception ignored) { release = null; state = State.IDLE; }
    }

    public static void download() {
        if (release == null || busy()) return;
        final ReleaseInfo expected = release; downloaded = 0; cancelled.set(false); state = State.DOWNLOADING; detail = "";
        worker(() -> {
            File partial = new File(directory(), "download.partial");
            try {
                if (!directory().isDirectory() && !directory().mkdirs()) throw new IOException("No update cache");
                if (directory().getUsableSpace() < expected.asset.size * 3 + 128*1024*1024L) throw new IOException("Not enough free storage for a safe update");
                UpdateTransport.download(expected, partial, cancelled, bytes -> downloaded = bytes);
                File ready = packageFile();
                if (ready.exists() && !ready.delete()) throw new IOException("Cannot replace previous download");
                if (!partial.renameTo(ready)) throw new IOException("Cannot finish update download");
                state = State.READY;
            } catch (Exception e) {
                partial.delete(); detail = cancelled.get() ? "Download cancelled. Your current game is unchanged." : "Download failed or could not be verified. Check storage and connection, then retry.";
                state = cancelled.get() ? State.AVAILABLE : State.ERROR;
            }
        });
    }

    public static void cancel() { cancelled.set(true); }

    public static void install() {
        if (state != State.READY || release == null || !(Game.scene() instanceof TitleScene)) return;
        state = State.INSTALLING; detail = ""; defer();
        worker(() -> {
            try {
                release.verifyFile(packageFile()); backup();
                // APK inspection/copying and Windows helper staging must not stall a tablet's render thread.
                try { detail = installer.install(packageFile(), release); state = State.READY; }
                catch (Exception e) { detail = e.getMessage() == null ? "Installation could not start. Your current game is unchanged." : e.getMessage(); state = State.READY; }
            } catch (Exception e) { detail = "Installation stopped: the package or save backup could not be verified. Your current game is unchanged."; state = State.ERROR; }
        });
    }

    private static void backup() throws IOException {
        File saves = directory().getCanonicalFile().getParentFile();
        File folder = new File(directory(), "backups");
        if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException("Cannot back up saves");
        File pending = new File(folder, "saves-" + Game.versionCode + "-" + System.currentTimeMillis() + ".partial");
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(pending))) {
            int[] count = {0}; long[] bytes = {0};
            backupFiles(saves, saves, zip, "game/", count, bytes, new java.util.HashSet<>());
            File prefs = installer.extraPreferences();
            if (prefs != null && prefs.isDirectory()) backupFiles(prefs, prefs, zip, "preferences/", count, bytes, new java.util.HashSet<>());
        } catch (IOException e) { pending.delete(); throw e; }
        File completed = new File(folder, pending.getName().replace(".partial", ".zip"));
        if (!pending.renameTo(completed)) throw new IOException("Save backup could not be completed");
    }

    private static void backupFiles(File root, File folder, ZipOutputStream zip, String prefix, int[] count, long[] bytes, java.util.Set<String> visited) throws IOException {
        if (!visited.add(folder.getCanonicalPath())) throw new IOException("Saved data contains a directory loop");
        File[] contents = folder.listFiles(); if (contents == null) throw new IOException("Cannot read saved data");
        for (File file : contents) {
            if (file.getCanonicalFile().equals(directory().getCanonicalFile())) continue;
            if (!file.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator)) throw new IOException("Saved data leaves its directory");
            if (file.isDirectory()) { backupFiles(root, file, zip, prefix, count, bytes, visited); continue; }
            if (++count[0] > 10000 || (bytes[0] += file.length()) > 256*1024*1024L) throw new IOException("Save backup exceeds safety limit");
            zip.putNextEntry(new ZipEntry(prefix + root.toURI().relativize(file.toURI()).getPath()));
            try (InputStream stream = new FileInputStream(file)) { byte[] buffer = new byte[65536]; int read; while ((read = stream.read(buffer)) != -1) zip.write(buffer,0,read); }
            zip.closeEntry();
        }
    }

    private static void worker(Runnable task) { Thread thread = new Thread(task, "Grimhollow updates"); thread.setDaemon(true); thread.start(); }
    private static void write(File file, byte[] bytes) throws IOException { try (FileOutputStream output = new FileOutputStream(file)) { output.write(bytes); output.getFD().sync(); } }
    private static byte[] read(File file, int limit) throws IOException {
        if (file.length() > limit) throw new IOException("Oversized cached metadata");
        try (InputStream input = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count;
            while ((count = input.read(buffer)) != -1) { if (out.size()+count > limit) throw new IOException("Oversized cached metadata"); out.write(buffer,0,count); }
            return out.toByteArray();
        }
    }
}
