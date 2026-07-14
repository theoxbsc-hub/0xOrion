package net.runelite.client.plugins.ox.core;

/**
 * Shared login freeze used by 0xStaffAlert so 0xAutoLogin stays offline after a panic logout.
 */
public final class OxLoginGate {
    private static volatile long blockLoginUntilMs = 0L;
    private static volatile String reason = "";

    private OxLoginGate() {
    }

    public static void blockFor(long durationMs, String why) {
        if (durationMs <= 0L) {
            return;
        }
        long until = System.currentTimeMillis() + durationMs;
        if (until > blockLoginUntilMs) {
            blockLoginUntilMs = until;
            if (why != null && !why.isEmpty()) {
                reason = why;
            }
        }
    }

    public static void clear() {
        blockLoginUntilMs = 0L;
        reason = "";
    }

    public static boolean isBlocked() {
        return System.currentTimeMillis() < blockLoginUntilMs;
    }

    public static long remainingMs() {
        long left = blockLoginUntilMs - System.currentTimeMillis();
        return left > 0L ? left : 0L;
    }

    public static long remainingSeconds() {
        long ms = remainingMs();
        return ms > 0L ? (ms + 999L) / 1000L : 0L;
    }

    public static String getReason() {
        return reason == null ? "" : reason;
    }
}
