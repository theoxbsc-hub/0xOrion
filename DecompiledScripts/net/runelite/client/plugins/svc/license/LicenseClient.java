/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.license;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.plugins.svc.license.LicenseConfig;

final class LicenseClient {
    private static final Path ORION_DIR = Paths.get(System.getProperty("user.home"), ".orion");
    private static final Path KEY_FILE = ORION_DIR.resolve(".license_key");
    private volatile UpdateInfo pendingUpdate;
    private volatile String pendingEntitlements;

    LicenseClient() {
    }

    UpdateInfo lastUpdate() {
        return this.pendingUpdate;
    }

    String lastEntitlements() {
        return this.pendingEntitlements;
    }

    Result validate(String string, String string2) {
        String string3;
        String string4 = "{\"key\":" + LicenseClient.jsonStr(string) + ",\"hwid\":" + LicenseClient.jsonStr(string2) + "}";
        try {
            string3 = LicenseClient.post(LicenseConfig.SERVER_URL + "/validate", string4);
        }
        catch (IOException iOException) {
            return Result.fail("network");
        }
        boolean bl = LicenseClient.jsonBool(string3, "ok");
        if (!bl) {
            return Result.fail(LicenseClient.jsonStrVal(string3, "reason"));
        }
        long l = LicenseClient.jsonLong(string3, "expiresAt", -1L);
        long l2 = LicenseClient.jsonLong(string3, "issuedAt", -1L);
        String string5 = LicenseClient.jsonStrVal(string3, "signature");
        if (l < 0L || l2 < 0L || string5 == null) {
            return Result.fail("bad_signature");
        }
        String string6 = string + "|" + string2 + "|" + l + "|" + l2;
        if (!LicenseClient.verify(string6, string5)) {
            return Result.fail("bad_signature");
        }
        LicenseClient.saveKey(string);
        this.pendingUpdate = LicenseClient.parseUpdate(string3);
        this.pendingEntitlements = LicenseClient.parseEntitlements(string3, string, string2);
        return Result.ok(l, false);
    }

    private static String parseEntitlements(String string, String string2, String string3) {
        String string4 = LicenseClient.jsonStrVal(string, "entitlements");
        String string5 = LicenseClient.jsonStrVal(string, "entSig");
        if (string4 == null || string5 == null) {
            return null;
        }
        if (!LicenseClient.verify(string2 + "|" + string3 + "|" + string4, string5)) {
            return null;
        }
        return string4;
    }

    private static UpdateInfo parseUpdate(String string) {
        String string2 = LicenseClient.jsonStrVal(string, "updVersion");
        String string3 = LicenseClient.jsonStrVal(string, "updSha256");
        String string4 = LicenseClient.jsonStrVal(string, "updSig");
        long l = LicenseClient.jsonLong(string, "updSize", -1L);
        if (string2 == null || string3 == null || string4 == null) {
            return null;
        }
        if (!LicenseClient.verify(string2 + "|" + string3, string4)) {
            return null;
        }
        return new UpdateInfo(string2, string3, l);
    }

    private static boolean verify(String string, String string2) {
        try {
            byte[] byArray = Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtsg/kcPcTFL4QtvWazM+cvJowcbrj6NO9eJRjjhqmQ7Zl4hqRXTtBBKFM6PBWoFuwTU64T3Jtg2I4/5mV5iRWP7EDQiXnEcJDcShhvhI0mcEtJpFbOI3A0Nbb2Pv4DfxkyC5ca7G8V9+kfR1qvL7Y2xWwcxK4NMH8lxin6rnLNhc2EACfRLB+L4EKmtGYfYQjWqsqOgOVPNZI1nfFMhD13haANduawDgzUCfwtXYrPh4vRcoArN6rxBID0qCj8YJ5LJTD8G20wiuzkGrkbwqOsdJriUmBFFi/XgWQkYOYRJxRux9VPgzqT+a4pWA66OwbCJYa64WM6DRiREnVhwX6QIDAQAB");
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(byArray));
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(string.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(string2));
        }
        catch (Exception exception) {
            return false;
        }
    }

    static String readSavedKey() {
        String string = LicenseClient.readFile(KEY_FILE);
        return string == null || string.trim().isEmpty() ? null : string.trim();
    }

    private static void saveKey(String string) {
        LicenseClient.writeFile(KEY_FILE, string.trim());
    }

    private static String post(String string, String string2) throws IOException {
        int n;
        InputStream inputStream;
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string).openConnection();
        httpURLConnection.setConnectTimeout(8000);
        httpURLConnection.setReadTimeout(8000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setDoOutput(true);
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            outputStream.write(string2.getBytes(StandardCharsets.UTF_8));
        }
        int n2 = httpURLConnection.getResponseCode();
        InputStream inputStream2 = inputStream = n2 >= 200 && n2 < 300 ? httpURLConnection.getInputStream() : httpURLConnection.getErrorStream();
        if (inputStream == null) {
            return "{\"ok\":false,\"reason\":\"server_error\"}";
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byArray = new byte[2048];
        while ((n = inputStream.read(byArray)) != -1) {
            byteArrayOutputStream.write(byArray, 0, n);
        }
        return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String jsonStr(String string) {
        StringBuilder stringBuilder = new StringBuilder("\"");
        for (char c : string.toCharArray()) {
            if (c == '\"' || c == '\\') {
                stringBuilder.append('\\');
            }
            stringBuilder.append(c);
        }
        return stringBuilder.append('\"').toString();
    }

    private static String jsonStrVal(String string, String string2) {
        Matcher matcher = Pattern.compile("\"" + string2 + "\"\\s*:\\s*\"([^\"]*)\"").matcher(string);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static long jsonLong(String string, String string2, long l) {
        Matcher matcher = Pattern.compile("\"" + string2 + "\"\\s*:\\s*(-?\\d+)").matcher(string);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : l;
    }

    private static boolean jsonBool(String string, String string2) {
        Matcher matcher = Pattern.compile("\"" + string2 + "\"\\s*:\\s*(true|false)").matcher(string);
        return matcher.find() && "true".equals(matcher.group(1));
    }

    private static String readFile(Path path) {
        try {
            return Files.exists(path, new LinkOption[0]) ? new String(Files.readAllBytes(path), StandardCharsets.UTF_8) : null;
        }
        catch (IOException iOException) {
            return null;
        }
    }

    private static void writeFile(Path path, String string) {
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.write(path, string.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    static final class UpdateInfo {
        final String version;
        final String sha256;
        final long size;

        UpdateInfo(String string, String string2, long l) {
            this.version = string;
            this.sha256 = string2;
            this.size = l;
        }
    }

    static final class Result {
        final boolean ok;
        final String reason;
        final long expiresAt;
        final boolean offline;

        private Result(boolean bl, String string, long l, boolean bl2) {
            this.ok = bl;
            this.reason = string;
            this.expiresAt = l;
            this.offline = bl2;
        }

        static Result ok(long l, boolean bl) {
            return new Result(true, null, l, bl);
        }

        static Result fail(String string) {
            return new Result(false, string, 0L, false);
        }

        String message() {
            if (this.ok) {
                String string = this.expiresAt == 0L ? "Lifetime license" : "Valid until " + String.valueOf(new Date(this.expiresAt));
                return (this.offline ? "Verified offline. " : "") + string;
            }
            if (this.reason == null) {
                return "Could not verify license.";
            }
            switch (this.reason) {
                case "invalid_key": {
                    return "That key was not recognised.";
                }
                case "revoked": {
                    return "This key has been revoked.";
                }
                case "expired": {
                    return "This license has expired.";
                }
                case "hwid_mismatch": {
                    return "This key is already bound to another computer.";
                }
                case "missing_fields": {
                    return "Please enter a license key.";
                }
                case "network": {
                    return "Can't reach the license server \u2014 you must be online to use Orion.";
                }
                case "offline_expired": {
                    return "Offline grace period ended. Connect to the internet.";
                }
                case "bad_signature": {
                    return "Server response failed verification. Try again.";
                }
            }
            return "License error: " + this.reason;
        }
    }
}

