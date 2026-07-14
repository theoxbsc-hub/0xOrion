/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.core;

public final class ShieldLoginGate {
    private static volatile long blockLoginUntil = 0L;

    private ShieldLoginGate() {
    }

    public static void blockFor(long l) {
        if (l <= 0L) {
            return;
        }
        long l2 = System.currentTimeMillis() + l;
        if (l2 > blockLoginUntil) {
            blockLoginUntil = l2;
        }
    }

    public static void clear() {
        blockLoginUntil = 0L;
    }

    public static boolean isBlocked() {
        return System.currentTimeMillis() < blockLoginUntil;
    }

    public static long remainingMs() {
        long l = blockLoginUntil - System.currentTimeMillis();
        return l > 0L ? l : 0L;
    }

    public static long remainingSeconds() {
        long l = ShieldLoginGate.remainingMs();
        return l > 0L ? (l + 999L) / 1000L : 0L;
    }
}

