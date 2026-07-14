package net.runelite.client.plugins.ox.notify;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import net.runelite.client.plugins.ox.core.OxNotify;

/** Async Discord webhook poster (embed JSON). */
final class DiscordWebhookClient {
    private final ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "0x-discord-webhook");
        t.setDaemon(true);
        return t;
    });
    private final AtomicLong lastSentAt = new AtomicLong(0L);

    void shutdown() {
        this.exec.shutdownNow();
    }

    void sendEmbed(
        String webhookUrl,
        String botName,
        OxNotify.Kind kind,
        String title,
        String description,
        String account,
        String location,
        String scripts,
        int minIntervalMs
    ) {
        if (webhookUrl == null || !webhookUrl.startsWith("https://discord.com/api/webhooks/")) {
            return;
        }
        String payload = buildPayload(botName, kind, title, description, account, location, scripts);
        this.exec.submit(() -> {
            try {
                long wait = (long) Math.max(0, minIntervalMs) - (System.currentTimeMillis() - this.lastSentAt.get());
                if (wait > 0L) {
                    Thread.sleep(wait);
                }
                post(webhookUrl.trim(), payload);
                this.lastSentAt.set(System.currentTimeMillis());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                System.err.println("[0xNotify] webhook failed: " + t.getMessage());
            }
        });
    }

    private static String buildPayload(
        String botName,
        OxNotify.Kind kind,
        String title,
        String description,
        String account,
        String location,
        String scripts
    ) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"username\":").append(jsonStr(botName == null || botName.isEmpty() ? "0x Orion" : botName)).append(",");
        sb.append("\"embeds\":[{");
        sb.append("\"title\":").append(jsonStr(title)).append(",");
        sb.append("\"color\":").append(colorFor(kind)).append(",");
        sb.append("\"description\":").append(jsonStr(description == null ? "-" : description)).append(",");
        sb.append("\"fields\":[");
        sb.append(field("Account", account, true)).append(",");
        sb.append(field("Kind", kind.name(), true)).append(",");
        sb.append(field("Location", location, true)).append(",");
        sb.append(field("Scripts", scripts, false));
        sb.append("],");
        sb.append("\"footer\":{\"text\":").append(jsonStr("0xNotify")).append("},");
        sb.append("\"timestamp\":").append(jsonStr(Instant.now().toString()));
        sb.append("}]}");
        return sb.toString();
    }

    private static int colorFor(OxNotify.Kind kind) {
        switch (kind) {
            case STAFF_ALERT:
                return 0xE74C3C; // red
            case SCRIPT_BREAK:
                return 0xE67E22; // orange
            case LOGGED_OUT:
                return 0xF1C40F; // yellow
            case LOGGED_IN:
                return 0x2ECC71; // green
            case SCRIPT_STOP:
                return 0x95A5A6; // grey
            case TEST:
                return 0xB44DFF; // purple brand
            case INFO:
            default:
                return 0x3498DB; // blue
        }
    }

    private static String field(String name, String value, boolean inline) {
        String v = value == null || value.isEmpty() ? "-" : value;
        if (v.length() > 1000) {
            v = v.substring(0, 1000) + "...";
        }
        return "{\"name\":" + jsonStr(name) + ",\"value\":" + jsonStr(v) + ",\"inline\":" + inline + "}";
    }

    private static void post(String url, String json) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            System.err.println("[0xNotify] webhook HTTP " + code);
        }
        conn.disconnect();
    }

    private static String jsonStr(String s) {
        if (s == null) {
            s = "";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
