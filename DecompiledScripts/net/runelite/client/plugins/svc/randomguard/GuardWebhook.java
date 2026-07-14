/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.randomguard;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.runelite.client.plugins.svc.core.AdminWebhook;

final class GuardWebhook {
    private static final int MIN_GAP_MS = 2500;
    private static final int COLOR = 5814783;
    private static final String FOOTER = "x64 Random Event Guard";
    private final ExecutorService exec = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "orion-randomguard-webhook");
        thread.setDaemon(true);
        return thread;
    });
    private long lastSentAt = 0L;

    GuardWebhook() {
    }

    static boolean isConfigured() {
        return AdminWebhook.isConfigured();
    }

    void send(String string, String string2, List<Field> list, byte[] byArray) {
        if (!GuardWebhook.isConfigured()) {
            return;
        }
        boolean bl = byArray != null && byArray.length > 0;
        String string3 = GuardWebhook.buildPayload(string, string2, list, bl ? "random_event.png" : null);
        byte[] byArray2 = (byte[])(bl ? byArray : null);
        this.exec.submit(() -> {
            try {
                long l = 2500L - (System.currentTimeMillis() - this.lastSentAt);
                if (l > 0L) {
                    Thread.sleep(l);
                }
                if (byArray2 != null) {
                    GuardWebhook.postMultipart("https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL", string3, byArray2, "random_event.png");
                } else {
                    GuardWebhook.postJson("https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL", string3);
                }
                this.lastSentAt = System.currentTimeMillis();
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            catch (Throwable throwable) {
                System.err.println("[RandomGuard] webhook failed: " + throwable.getMessage());
            }
        });
    }

    void shutdown() {
        this.exec.shutdownNow();
    }

    private static String buildPayload(String string, String string2, List<Field> arrayList, String string3) {
        ArrayList arrayList2 = arrayList != null ? arrayList : new ArrayList();
        StringBuilder stringBuilder = new StringBuilder("{\"embeds\":[{");
        stringBuilder.append("\"title\":").append(GuardWebhook.jsonStr(string)).append(',');
        stringBuilder.append("\"color\":").append(5814783).append(',');
        stringBuilder.append("\"description\":").append(GuardWebhook.jsonStr(string2));
        if (!arrayList2.isEmpty()) {
            stringBuilder.append(",\"fields\":[");
            for (int i = 0; i < arrayList2.size(); ++i) {
                if (i > 0) {
                    stringBuilder.append(',');
                }
                Field field = (Field)arrayList2.get(i);
                stringBuilder.append("{\"name\":").append(GuardWebhook.jsonStr(field.name)).append(",\"value\":").append(GuardWebhook.jsonStr(field.value == null || field.value.isEmpty() ? "-" : field.value)).append(",\"inline\":").append(field.inline).append('}');
            }
            stringBuilder.append(']');
        }
        if (string3 != null) {
            stringBuilder.append(",\"image\":{\"url\":\"attachment://").append(string3).append("\"}");
        }
        stringBuilder.append(",\"footer\":{\"text\":").append(GuardWebhook.jsonStr(FOOTER)).append('}');
        stringBuilder.append(",\"timestamp\":").append(GuardWebhook.jsonStr(Instant.now().toString()));
        stringBuilder.append("}]}");
        return stringBuilder.toString();
    }

    private static void postJson(String string, String string2) throws Exception {
        HttpURLConnection httpURLConnection = GuardWebhook.open(string);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            outputStream.write(string2.getBytes(StandardCharsets.UTF_8));
        }
        GuardWebhook.finish(httpURLConnection);
    }

    private static void postMultipart(String string, String string2, byte[] byArray, String string3) throws Exception {
        String string4 = "----orionGuard" + Long.toHexString(System.nanoTime());
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
        HttpURLConnection httpURLConnection = GuardWebhook.open(string);
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + string4);
        httpURLConnection.setDoOutput(true);
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            byteArrayOutputStream.writeTo(outputStream);
        }
        GuardWebhook.finish(httpURLConnection);
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
            System.err.println("[RandomGuard] webhook HTTP " + n);
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

    static final class Field {
        final String name;
        final String value;
        final boolean inline;

        Field(String string, String string2, boolean bl) {
            this.name = string;
            this.value = string2;
            this.inline = bl;
        }
    }
}

