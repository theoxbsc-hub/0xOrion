/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.incognito;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.runelite.client.plugins.svc.incognito.PlayerWatcher;

final class ShieldWebhook {
    static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL";
    private static final int MIN_GAP_MS = 2500;
    private static final int COLOR_WATCH = 15158332;
    private static final int COLOR_PANIC = 15844367;
    private static final String FOOTER = "x64 Incognito Shield";
    private final ExecutorService exec = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "orion-shield-webhook");
        thread.setDaemon(true);
        return thread;
    });
    private long lastSentAt = 0L;

    ShieldWebhook() {
    }

    static boolean isConfigured() {
        return WEBHOOK_URL != null && WEBHOOK_URL.startsWith("https://discord.com/api/webhooks/") && !WEBHOOK_URL.contains("PASTE_");
    }

    void notifyDetection(PlayerWatcher.Detection detection, String string, int n, String string2, byte[] byArray) {
        if (!ShieldWebhook.isConfigured() || detection == null) {
            return;
        }
        boolean bl = byArray != null && byArray.length > 0;
        String string3 = ShieldWebhook.buildPayload(detection, string, n, string2, bl ? "shield_shot.png" : null);
        byte[] byArray2 = (byte[])(bl ? byArray : null);
        this.exec.submit(() -> {
            try {
                long l = 2500L - (System.currentTimeMillis() - this.lastSentAt);
                if (l > 0L) {
                    Thread.sleep(l);
                }
                if (byArray2 != null) {
                    ShieldWebhook.postMultipart(WEBHOOK_URL, string3, byArray2, "shield_shot.png");
                } else {
                    ShieldWebhook.postJson(WEBHOOK_URL, string3);
                }
                this.lastSentAt = System.currentTimeMillis();
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            catch (Throwable throwable) {
                System.err.println("[Incognito] shield webhook failed: " + throwable.getMessage());
            }
        });
    }

    void shutdown() {
        this.exec.shutdownNow();
    }

    private static String buildPayload(PlayerWatcher.Detection detection, String string, int n, String string2, String string3) {
        boolean bl = detection.trigger == PlayerWatcher.Trigger.WATCHLIST;
        String string4 = bl ? "\u26a0 Watched player detected" : "\u26a0 Player within range";
        int n2 = bl ? 15158332 : 15844367;
        String string5 = detection.location == null ? "?" : detection.location.getX() + ", " + detection.location.getY() + " P" + detection.location.getPlane();
        String string6 = detection.distance + (detection.distance == 1 ? " tile" : " tiles");
        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"embeds\":[{");
        stringBuilder.append("\"title\":").append(ShieldWebhook.jsonStr(string4)).append(",");
        stringBuilder.append("\"color\":").append(n2).append(",");
        stringBuilder.append("\"description\":").append(ShieldWebhook.jsonStr("**" + detection.rawName + "** was spotted " + string6 + " away.")).append(",");
        stringBuilder.append("\"fields\":[");
        stringBuilder.append(ShieldWebhook.field("Player", detection.rawName, true)).append(",");
        stringBuilder.append(ShieldWebhook.field("Trigger", bl ? "Watch list" : "Proximity panic", true)).append(",");
        stringBuilder.append(ShieldWebhook.field("Distance", string6, true)).append(",");
        stringBuilder.append(ShieldWebhook.field("Location", string5, true)).append(",");
        stringBuilder.append(ShieldWebhook.field("World", n > 0 ? String.valueOf(n) : "?", true)).append(",");
        stringBuilder.append(ShieldWebhook.field("Account", string == null ? "?" : string, true)).append(",");
        stringBuilder.append(ShieldWebhook.field("Action", string2 == null || string2.isEmpty() ? "-" : string2, false));
        stringBuilder.append("]");
        if (string3 != null) {
            stringBuilder.append(",\"image\":{\"url\":\"attachment://").append(string3).append("\"}");
        }
        stringBuilder.append(",\"footer\":{\"text\":").append(ShieldWebhook.jsonStr(FOOTER)).append("}");
        stringBuilder.append(",\"timestamp\":").append(ShieldWebhook.jsonStr(Instant.now().toString()));
        stringBuilder.append("}]}");
        return stringBuilder.toString();
    }

    private static String field(String string, String string2, boolean bl) {
        return "{\"name\":" + ShieldWebhook.jsonStr(string) + ",\"value\":" + ShieldWebhook.jsonStr(string2 == null || string2.isEmpty() ? "-" : string2) + ",\"inline\":" + bl + "}";
    }

    private static void postJson(String string, String string2) throws Exception {
        HttpURLConnection httpURLConnection = ShieldWebhook.open(string);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            outputStream.write(string2.getBytes(StandardCharsets.UTF_8));
        }
        ShieldWebhook.finish(httpURLConnection);
    }

    private static void postMultipart(String string, String string2, byte[] byArray, String string3) throws Exception {
        String string4 = "----orionShield" + Long.toHexString(System.currentTimeMillis());
        byte[] byArray2 = ("--" + string4 + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] byArray3 = "\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(byArray2);
        byteArrayOutputStream.write("Content-Disposition: form-data; name=\"payload_json\"\r\nContent-Type: application/json\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        byteArrayOutputStream.write(string2.getBytes(StandardCharsets.UTF_8));
        byteArrayOutputStream.write(byArray3);
        byteArrayOutputStream.write(byArray2);
        byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"files[0]\"; filename=\"" + string3 + "\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrayOutputStream.write(byArray);
        byteArrayOutputStream.write(byArray3);
        byteArrayOutputStream.write(("--" + string4 + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpURLConnection httpURLConnection = ShieldWebhook.open(string);
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + string4);
        httpURLConnection.setDoOutput(true);
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            byteArrayOutputStream.writeTo(outputStream);
        }
        ShieldWebhook.finish(httpURLConnection);
    }

    private static HttpURLConnection open(String string) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string).openConnection();
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(8000);
        httpURLConnection.setRequestMethod("POST");
        return httpURLConnection;
    }

    private static void finish(HttpURLConnection httpURLConnection) throws Exception {
        int n = httpURLConnection.getResponseCode();
        if (n < 200 || n >= 300) {
            System.err.println("[Incognito] shield webhook HTTP " + n);
        }
        httpURLConnection.disconnect();
    }

    private static String jsonStr(String string) {
        if (string == null) {
            string = "";
        }
        StringBuilder stringBuilder = new StringBuilder("\"");
        block7: for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            switch (c) {
                case '\"': {
                    stringBuilder.append("\\\"");
                    continue block7;
                }
                case '\\': {
                    stringBuilder.append("\\\\");
                    continue block7;
                }
                case '\n': {
                    stringBuilder.append("\\n");
                    continue block7;
                }
                case '\r': {
                    stringBuilder.append("\\r");
                    continue block7;
                }
                case '\t': {
                    stringBuilder.append("\\t");
                    continue block7;
                }
                default: {
                    if (c < ' ') {
                        stringBuilder.append(String.format("\\u%04x", c));
                        continue block7;
                    }
                    stringBuilder.append(c);
                }
            }
        }
        return stringBuilder.append('\"').toString();
    }
}

