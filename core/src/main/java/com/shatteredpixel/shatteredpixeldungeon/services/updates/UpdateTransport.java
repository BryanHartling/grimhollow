// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.services.updates;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** The only approved update network boundary. No credentials, plain HTTP or arbitrary endpoints. */
public final class UpdateTransport {
    public interface Progress { void accept(long bytes); }
    private UpdateTransport() {}

    private static long contentLength(HttpURLConnection connection) {
        try { return Long.parseLong(connection.getHeaderField("Content-Length")); }
        catch (Exception ignored) { return -1; }
    }

    private static HttpURLConnection connect(String address) throws IOException {
        for (int redirect = 0; redirect < 6; redirect++) {
            if (!ReleaseInfo.allowedTransport(address)) throw new IOException("Blocked update address");
            HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
            connection.setConnectTimeout(8000); connection.setReadTimeout(15000); connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "Grimhollow-Updater/1");
            connection.setRequestProperty("Accept", address.startsWith("https://api.github.com/") ? "application/vnd.github+json" : "application/octet-stream");
            int status = connection.getResponseCode();
            if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                String next = connection.getHeaderField("Location"); connection.disconnect();
                if (next == null) throw new IOException("Missing update redirect");
                address = URI.create(address).resolve(next).toString(); continue;
            }
            if (status == 404) { connection.disconnect(); throw new FileNotFoundException("No published update release"); }
            if (status != 200) { connection.disconnect(); throw new IOException("Update server returned HTTP " + status); }
            return connection;
        }
        throw new IOException("Too many update redirects");
    }

    public static byte[] read(String url, int limit) throws IOException {
        HttpURLConnection connection = connect(url);
        try {
            if (contentLength(connection) > limit) throw new IOException("Oversized update response");
            try (InputStream stream = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = stream.read(buffer)) != -1) {
                    if (output.size() + count > limit) throw new IOException("Oversized update response");
                    output.write(buffer, 0, count);
                }
                return output.toByteArray();
            }
        } finally { connection.disconnect(); }
    }

    public static void download(ReleaseInfo release, File partial, AtomicBoolean cancelled, Progress progress) throws IOException {
        HttpURLConnection connection = connect(release.asset.url);
        try {
            if (contentLength(connection) > release.asset.size) throw new IOException("Package size differs from manifest");
            try (InputStream stream = connection.getInputStream(); FileOutputStream output = new FileOutputStream(partial)) {
                receive(release, stream, output, cancelled, progress);
                output.getFD().sync();
            }
            release.verifyFile(partial);
        } finally { connection.disconnect(); }
    }

    static void receive(ReleaseInfo release, InputStream stream, OutputStream output, AtomicBoolean cancelled, Progress progress) throws IOException {
        byte[] buffer = new byte[65536]; int count; long total = 0;
        while ((count = stream.read(buffer)) != -1) {
            if (cancelled.get()) throw new InterruptedIOException("Download cancelled");
            total += count;
            if (total > release.asset.size) throw new IOException("Package exceeds its signed size");
            output.write(buffer, 0, count); progress.accept(total);
        }
        if (cancelled.get()) throw new InterruptedIOException("Download cancelled");
        if (total != release.asset.size) throw new IOException("Incomplete update download");
    }
}
