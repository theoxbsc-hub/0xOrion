/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.core;

public final class RandomEventState {
    private static volatile long activeUntilMs = 0L;
    private static volatile String label = "";

    private RandomEventState() {
    }

    public static void beginHandling(long l, String string) {
        activeUntilMs = System.currentTimeMillis() + Math.max(0L, l);
        label = string == null || string.isEmpty() ? "random event" : string;
    }

    public static void extend(long l) {
        long l2 = System.currentTimeMillis() + Math.max(0L, l);
        if (l2 > activeUntilMs) {
            activeUntilMs = l2;
        }
    }

    public static void clear() {
        activeUntilMs = 0L;
        label = "";
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < activeUntilMs;
    }

    public static String label() {
        return label;
    }

    public static long remainingMs() {
        return Math.max(0L, activeUntilMs - System.currentTimeMillis());
    }
}

