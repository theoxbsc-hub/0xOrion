/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.chatresponder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.runelite.client.plugins.svc.core.AdminWebhook;

final class DiscordWebhook {
    private static final String USERNAME = "Orion Chat Responder";
    private final ExecutorService exec = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "orion-chat-webhook");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicInteger sent = new AtomicInteger();
    private long lastSentAt = 0L;

    DiscordWebhook() {
    }

    int getSent() {
        return this.sent.get();
    }

    void notifyEvent(int n, boolean bl, String string, String string2, String string3, String string4, String string5, long l, String string6) {
        if (!AdminWebhook.isConfigured()) {
            return;
        }
        String string7 = DiscordWebhook.buildPayload(USERNAME, bl, string, string2, string3, string4, string5, l, string6);
        int n2 = Math.max(0, n);
        this.exec.submit(() -> {
            try {
                long l = (long)n2 - (System.currentTimeMillis() - this.lastSentAt);
                if (l > 0L) {
                    Thread.sleep(l);
                }
                DiscordWebhook.post("https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL", string7);
                this.lastSentAt = System.currentTimeMillis();
                this.sent.incrementAndGet();
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            catch (Throwable throwable) {
                System.err.println("[ChatResponder] webhook failed: " + throwable.getMessage());
            }
        });
    }

    void notifyRichReply(int n, String string, String string2, String string3, String string4, String string5, long l, boolean bl, boolean bl2, String string6, String string7, byte[] byArray) {
        if (!AdminWebhook.isConfigured()) {
            return;
        }
        boolean bl3 = byArray != null && byArray.length > 0;
        String string8 = DiscordWebhook.buildRichPayload(USERNAME, string, string2, string3, string4, string5, l, bl, bl2, string6, string7, bl3 ? "chat_shot.png" : null);
        byte[] byArray2 = (byte[])(bl3 ? byArray : null);
        int n2 = Math.max(0, n);
        try {
            this.exec.submit(() -> {
                try {
                    long l = (long)n2 - (System.currentTimeMillis() - this.lastSentAt);
                    if (l > 0L) {
                        Thread.sleep(l);
                    }
                    if (byArray2 != null) {
                        DiscordWebhook.postMultipart("https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL", string8, byArray2, "chat_shot.png");
                    } else {
                        DiscordWebhook.post("https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL", string8);
                    }
                    this.lastSentAt = System.currentTimeMillis();
                    this.sent.incrementAndGet();
                }
                catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                catch (Throwable throwable) {
                    System.err.println("[ChatResponder] rich webhook failed: " + throwable.getMessage());
                }
            });
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    void shutdown() {
        this.exec.shutdownNow();
    }

    private static String buildPayload(String string, boolean bl, String string2, String string3, String string4, String string5, String string6, long l, String string7) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**Mode:** ").append(DiscordWebhook.safe(string2)).append("\\n");
        stringBuilder.append("**From:** ").append(DiscordWebhook.safe(string3)).append("\\n");
        stringBuilder.append("**Said:** ").append(DiscordWebhook.safe(string4)).append("\\n");
        stringBuilder.append("**Intent:** ").append(DiscordWebhook.safe(string5));
        if (bl) {
            stringBuilder.append("\\n**Replied:** ").append(DiscordWebhook.safe(string6));
            stringBuilder.append("\\n**Delay:** ").append(String.format("%.1fs", (double)l / 1000.0));
        } else if (string7 != null && !string7.isEmpty()) {
            stringBuilder.append("\\n**Ignored:** ").append(DiscordWebhook.safe(string7));
        }
        int n = bl ? 3066993 : 9807270;
        String string8 = bl ? "Replied to chat" : "Chat detected (ignored)";
        StringBuilder stringBuilder2 = new StringBuilder("{");
        stringBuilder2.append("\"username\":").append(DiscordWebhook.jsonStr(string == null ? USERNAME : string)).append(",");
        stringBuilder2.append("\"embeds\":[{");
        stringBuilder2.append("\"title\":").append(DiscordWebhook.jsonStr(string8)).append(",");
        stringBuilder2.append("\"color\":").append(n).append(",");
        stringBuilder2.append("\"description\":\"").append((CharSequence)stringBuilder).append("\"");
        stringBuilder2.append("}]}");
        return stringBuilder2.toString();
    }

    private static String buildRichPayload(String string, String string2, String string3, String string4, String string5, String string6, long l, boolean bl, boolean bl2, String string7, String string8, String string9) {
        String string10;
        String string11;
        String string12 = bl && bl2 ? "Replied to watched player (PM)" : (bl ? "Replied to a PM" : (string11 = bl2 ? "Replied to a watched player" : "Replied to chat"));
        String string13 = bl && bl2 ? "Watched player \u00b7 PM" : (bl ? "Private message" : (bl2 ? "Watched player (public)" : (string2 == null || string2.isEmpty() ? "Public" : string2)));
        String string14 = string10 = string8 == null ? "" : string8.replace('`', '\'');
        if (string10.length() > 3500) {
            string10 = string10.substring(string10.length() - 3500);
        }
        if (string10.isEmpty()) {
            string10 = "(no recent chat captured)";
        }
        String string15 = "**Recent chatbox:**\n```\n" + string10 + "\n```";
        StringBuilder stringBuilder = new StringBuilder("{");
        stringBuilder.append("\"username\":").append(DiscordWebhook.jsonStr(string == null ? USERNAME : string)).append(",");
        stringBuilder.append("\"embeds\":[{");
        stringBuilder.append("\"title\":").append(DiscordWebhook.jsonStr(string11)).append(",");
        stringBuilder.append("\"color\":").append(3447003).append(",");
        stringBuilder.append("\"description\":").append(DiscordWebhook.jsonStr(string15)).append(",");
        stringBuilder.append("\"fields\":[");
        stringBuilder.append(DiscordWebhook.field("From", string3, true)).append(",");
        stringBuilder.append(DiscordWebhook.field("Trigger", string13, true)).append(",");
        stringBuilder.append(DiscordWebhook.field("Account", string7, true)).append(",");
        stringBuilder.append(DiscordWebhook.field("Intent", string5, true)).append(",");
        stringBuilder.append(DiscordWebhook.field("Delay", String.format("%.1fs", (double)l / 1000.0), true)).append(",");
        stringBuilder.append(DiscordWebhook.field("Said", string4, false)).append(",");
        stringBuilder.append(DiscordWebhook.field("Replied", string6, false));
        stringBuilder.append("]");
        if (string9 != null) {
            stringBuilder.append(",\"image\":{\"url\":\"attachment://").append(string9).append("\"}");
        }
        stringBuilder.append(",\"footer\":{\"text\":").append(DiscordWebhook.jsonStr("x64 Chat Responder")).append("}");
        stringBuilder.append(",\"timestamp\":").append(DiscordWebhook.jsonStr(Instant.now().toString()));
        stringBuilder.append("}]}");
        return stringBuilder.toString();
    }

    private static String field(String string, String string2, boolean bl) {
        Object object;
        Object object2 = object = string2 == null || string2.isEmpty() ? "-" : string2;
        if (((String)object).length() > 1000) {
            object = ((String)object).substring(0, 1000) + "\u2026";
        }
        return "{\"name\":" + DiscordWebhook.jsonStr(string) + ",\"value\":" + DiscordWebhook.jsonStr((String)object) + ",\"inline\":" + bl + "}";
    }

    private static void postMultipart(String string, String string2, byte[] byArray, String string3) throws Exception {
        String string4 = "----orionChat" + Long.toHexString(System.currentTimeMillis());
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
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string).openConnection();
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(8000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + string4);
        httpURLConnection.setDoOutput(true);
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            byteArrayOutputStream.writeTo(outputStream);
        }
        int n = httpURLConnection.getResponseCode();
        if (n < 200 || n >= 300) {
            System.err.println("[ChatResponder] rich webhook HTTP " + n);
        }
        httpURLConnection.disconnect();
    }

    private static void post(String string, String string2) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string).openConnection();
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            outputStream.write(string2.getBytes(StandardCharsets.UTF_8));
        }
        int n = httpURLConnection.getResponseCode();
        if (n < 200 || n >= 300) {
            System.err.println("[ChatResponder] webhook HTTP " + n);
        }
        httpURLConnection.disconnect();
    }

    private static String jsonStr(String string) {
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

    private static String safe(String string) {
        if (string == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        block5: for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            switch (c) {
                case '\"': {
                    stringBuilder.append("\\\"");
                    continue block5;
                }
                case '\\': {
                    stringBuilder.append("\\\\");
                    continue block5;
                }
                case '\n': 
                case '\r': {
                    stringBuilder.append(' ');
                    continue block5;
                }
                default: {
                    if (c < ' ') continue block5;
                    stringBuilder.append(c);
                }
            }
        }
        return stringBuilder.toString();
    }
}

