// SPDX-License-Identifier: GPL-3.0-or-later
package com.shatteredpixel.shatteredpixeldungeon.services.updates;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateSafetyTest {
    private static KeyPair keys;
    private static final String HASH="ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
    @BeforeClass public static void key() throws Exception { KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);keys=generator.generateKeyPair(); }
    private String manifest() { return "{\"schema\":1,\"repository\":\"bryanhartling/grimhollow\",\"tag\":\"v1.30.1\",\"versionName\":\"1.30.1\",\"versionCode\":1019,\"channel\":\"stable\",\"minAndroid\":21,\"assets\":{\"windows-x64\":{\"name\":\"grimhollow-windows-x64.zip\",\"url\":\"https://github.com/bryanhartling/grimhollow/releases/download/v1.30.1/grimhollow-windows-x64.zip\",\"size\":3,\"sha256\":\""+HASH+"\"}}}"; }
    private byte[] sign(byte[] data) throws Exception { Signature signer=Signature.getInstance("SHA256withRSA");signer.initSign(keys.getPrivate());signer.update(data);return Base64.getEncoder().encode(signer.sign()); }
    private ReleaseInfo verify(String raw) throws Exception { byte[] bytes=raw.getBytes(StandardCharsets.UTF_8);return ReleaseInfo.verify(bytes,sign(bytes),keys.getPublic().getEncoded(),"v1.30.1","windows-x64",""); }
    private void rejected(String raw) throws Exception { try { verify(raw);fail("Untrusted release accepted"); }catch(IOException expected) {} }
    @Test public void signedBytesCannotBeAltered() throws Exception {
        byte[] raw=manifest().getBytes(StandardCharsets.UTF_8),sig=sign(raw);raw[0]='[';
        try { ReleaseInfo.verify(raw,sig,keys.getPublic().getEncoded(),"v1.30.1","windows-x64","");fail(); }catch(IOException expected) {}
        KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
        try { ReleaseInfo.verify(manifest().getBytes(StandardCharsets.UTF_8),sign(manifest().getBytes(StandardCharsets.UTF_8)),generator.generateKeyPair().getPublic().getEncoded(),"v1.30.1","windows-x64","");fail(); }catch(IOException expected) {}
    }
    @Test public void versionsAndChannelsNeverDowngrade() throws Exception {
        ReleaseInfo info=verify(manifest());assertTrue(info.newerThan(1018,false));assertFalse(info.newerThan(1019,true));assertFalse(info.newerThan(1020,true));
        info=verify(manifest().replace("\"stable\"","\"playtest\""));assertFalse(info.newerThan(1018,false));assertTrue(info.newerThan(1018,true));
    }
    @Test public void releaseIdentityIsBoundToRepositoryAndTag() throws Exception {
        rejected(manifest().replace("\"schema\":1","\"schema\":2"));rejected(manifest().replace("\"repository\":\"bryanhartling/grimhollow\"","\"repository\":\"other/repo\""));
        rejected(manifest().replace("\"tag\":\"v1.30.1\"","\"tag\":\"v1.30.0\""));rejected(manifest().replace("\"versionName\":\"1.30.1\"","\"versionName\":\"9.9.9\""));
    }
    @Test public void packageContractsRejectForeignPathsHashesAndSizes() throws Exception {
        rejected(manifest().replace("https://github.com/","https://example.org/"));rejected(manifest().replace("\"name\":\"grimhollow-windows-x64.zip\"","\"name\":\"../game.exe\""));
        rejected(manifest().replace(HASH,"bad"));rejected(manifest().replace("\"size\":3","\"size\":0"));rejected(manifest().replace("\"size\":3","\"size\":2147483647"));
    }
    @Test public void networkBoundaryRejectsUnsafeHostsAndRedirects() {
        for(String address:new String[]{"http://github.com/bryanhartling/grimhollow/releases/download/v1/a.zip","https://github.com.evil.test/bryanhartling/grimhollow/releases/download/v1/a.zip","https://user:password@github.com/bryanhartling/grimhollow/releases/download/v1/a.zip","https://api.github.com/repos/00-Evan/shattered-pixel-dungeon/releases","https://127.0.0.1/","https://github.com:444/bryanhartling/grimhollow/releases/download/v1/a.zip","https://github.com/bryanhartling/grimhollow/releases/download/../a.zip","https://objects.githubusercontent.com/file#fragment"})assertFalse(address,ReleaseInfo.allowedTransport(address));
        assertTrue(ReleaseInfo.allowedTransport("https://api.github.com/repos/bryanhartling/grimhollow/releases?per_page=20"));
        assertTrue(ReleaseInfo.allowedTransport("https://release-assets.githubusercontent.com/file?signature=abc"));
    }
    @Test public void streamingRejectsTruncationExpansionAndCancellation() throws Exception {
        ReleaseInfo info=verify(manifest());ByteArrayOutputStream out=new ByteArrayOutputStream();
        UpdateTransport.receive(info,new ByteArrayInputStream(new byte[]{1,2,3}),out,new AtomicBoolean(),n->{});assertEquals(3,out.size());
        for(byte[] bytes:new byte[][]{new byte[]{1,2},new byte[]{1,2,3,4}}) {
            try { UpdateTransport.receive(info,new ByteArrayInputStream(bytes),new ByteArrayOutputStream(),new AtomicBoolean(),n->{});fail(); }catch(IOException expected) {}
        }
        try { UpdateTransport.receive(info,new ByteArrayInputStream(new byte[]{1,2,3}),new ByteArrayOutputStream(),new AtomicBoolean(true),n->{});fail(); }catch(InterruptedIOException expected) {}
    }
    @Test public void completedFilesRequireExactHashAndSize() throws Exception {
        ReleaseInfo info=verify(manifest());File file=File.createTempFile("grimhollow-update-",".zip");
        try {
            Files.write(file.toPath(),"abc".getBytes(StandardCharsets.US_ASCII));info.verifyFile(file);
            Files.write(file.toPath(),"abd".getBytes(StandardCharsets.US_ASCII));try { info.verifyFile(file);fail(); }catch(IOException expected) {}
            Files.write(file.toPath(),"ab".getBytes(StandardCharsets.US_ASCII));try { info.verifyFile(file);fail(); }catch(IOException expected) {}
        } finally { file.delete(); }
    }
    @Test public void oversizedMetadataFailsBeforeParsing() throws Exception {
        try { ReleaseInfo.verify(new byte[32769],new byte[0],new byte[0],"v1.30.1","windows-x64","");fail(); }catch(IOException expected) {}
        try { ReleaseInfo.verify(new byte[1],new byte[2049],new byte[0],"v1.30.1","windows-x64","");fail(); }catch(IOException expected) {}
    }
}
