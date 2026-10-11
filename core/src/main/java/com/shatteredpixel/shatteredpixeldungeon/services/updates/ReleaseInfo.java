// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.services.updates;

import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/** An exact-byte signed release contract; game state and display names are not involved. */
public final class ReleaseInfo {
    public static final String REPOSITORY = "bryanhartling/grimhollow";
    public static final String DOWNLOADS = "https://github.com/" + REPOSITORY + "/releases/download/";
    public static final long MAX_PACKAGE = 1024L * 1024 * 1024;
    public final String version, tag, channel, notes;
    public final int code, minAndroid;
    public final Asset asset;

    public static final class Asset {
        public final String name, url, sha256;
        public final long size;
        Asset(JsonValue value, String tag, String platform) throws IOException {
            name = value.getString("name"); url = value.getString("url");
            sha256 = value.getString("sha256"); size = value.getLong("size");
            String expected = platform.equals("android") ? "grimhollow-android.apk" : "grimhollow-windows-x64.zip";
            if (!name.equals(expected) || !url.equals(DOWNLOADS + tag + "/" + name)
                    || !sha256.matches("[0-9a-f]{64}") || size < 1 || size > MAX_PACKAGE)
                throw new IOException("Invalid update package contract");
        }
    }

    private ReleaseInfo(JsonValue json, String expectedTag, String platform, String notes) throws IOException {
        if (json.getInt("schema") != 1 || !json.getString("repository").equals(REPOSITORY))
            throw new IOException("Unknown update manifest");
        tag = json.getString("tag"); version = json.getString("versionName");
        code = json.getInt("versionCode"); channel = json.getString("channel"); minAndroid = json.getInt("minAndroid");
        if (!version.matches("[0-9]+\\.[0-9]+\\.[0-9]+") || !tag.equals(expectedTag)
                || !tag.matches("v" + version.replace(".", "\\.") + "(?:-[a-z0-9-]+)?")
                || code < 1 || minAndroid < 21 || !(channel.equals("stable") || channel.equals("playtest")))
            throw new IOException("Invalid release identity");
        if (!(platform.equals("android") || platform.equals("windows-x64"))) throw new IOException("Unsupported update platform");
        asset = new Asset(json.get("assets").get(platform), tag, platform);
        this.notes = notes == null ? "" : notes.substring(0, Math.min(notes.length(), 3000)).replace("_", "");
    }

    public static ReleaseInfo verify(byte[] bytes, byte[] signature, byte[] publicKey, String tag,
                                     String platform, String notes) throws IOException {
        if (bytes.length == 0 || bytes.length > 32768 || signature.length > 2048) throw new IOException("Oversized release metadata");
        try {
            PublicKey key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
            Signature checker = Signature.getInstance("SHA256withRSA"); checker.initVerify(key); checker.update(bytes);
            if (!checker.verify(Base64Coder.decode(new String(signature, StandardCharsets.US_ASCII).trim())))
                throw new IOException("Update signature did not verify");
            return new ReleaseInfo(new JsonReader().parse(new String(bytes, StandardCharsets.UTF_8)), tag, platform, notes);
        } catch (IOException e) { throw e; }
        catch (Exception e) { throw new IOException("Untrusted or malformed release", e); }
    }

    public static boolean allowedTransport(String address) {
        try {
            URI uri = new URI(address);
            if (!"https".equals(uri.getScheme()) || uri.getUserInfo() != null || (uri.getPort() != -1 && uri.getPort() != 443)
                    || uri.getFragment() != null) return false;
            String host = uri.getHost(); String path = uri.getPath();
            if ("api.github.com".equals(host)) return path.equals("/repos/" + REPOSITORY + "/releases")
                    || path.equals("/repos/" + REPOSITORY + "/releases/latest");
            if ("github.com".equals(host)) return path.startsWith("/" + REPOSITORY + "/releases/download/")
                    && !path.contains("..") && uri.getQuery() == null;
            return "release-assets.githubusercontent.com".equals(host) || "objects.githubusercontent.com".equals(host);
        } catch (Exception e) { return false; }
    }

    public boolean newerThan(int installed, boolean includePlaytests) {
        return code > installed && (includePlaytests || channel.equals("stable"));
    }

    public static String sha256(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[65536]; int read;
                while ((read = stream.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) hex.append(Character.forDigit((b >>> 4) & 15, 16)).append(Character.forDigit(b & 15, 16));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) { throw new IOException(e); }
    }

    public void verifyFile(File file) throws IOException {
        if (file.length() != asset.size || !sha256(file).equals(asset.sha256)) throw new IOException("Update download is incomplete or altered");
    }
}
