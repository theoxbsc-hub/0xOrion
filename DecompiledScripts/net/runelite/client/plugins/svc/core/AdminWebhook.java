/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.core;

public final class AdminWebhook {
    public static final String URL = "https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL";

    private AdminWebhook() {
    }

    public static boolean isConfigured() {
        return URL != null && URL.startsWith("https://discord.com/api/webhooks/") && !URL.contains("PASTE_");
    }
}

