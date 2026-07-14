package net.runelite.client.plugins.ox.core;

/**
 * Fire-and-forget event bus for Discord / other notifiers.
 * Scripts call {@link #staffAlert}, {@link #scriptBreak}, etc.; 0xNotify consumes when enabled.
 */
public final class OxNotify {
    public enum Kind {
        STAFF_ALERT,
        LOGGED_OUT,
        LOGGED_IN,
        SCRIPT_BREAK,
        SCRIPT_STOP,
        TEST,
        INFO
    }

    @FunctionalInterface
    public interface Sink {
        void onNotify(Kind kind, String title, String detail);
    }

    private static volatile Sink sink;

    private OxNotify() {
    }

    public static void setSink(Sink next) {
        sink = next;
    }

    public static void clearSink(Sink expected) {
        if (sink == expected) {
            sink = null;
        }
    }

    public static void staffAlert(String target, int distanceTiles, int cooldownMinutes, String reason) {
        String detail = "target=" + safe(target)
            + "  dist=" + distanceTiles + "t"
            + "  cooldown=" + cooldownMinutes + "m"
            + "  why=" + safe(reason);
        fire(Kind.STAFF_ALERT, "Staff alert - logged out", detail);
    }

    public static void loggedOut(String reason) {
        fire(Kind.LOGGED_OUT, "Logged out", safe(reason));
    }

    public static void loggedIn(String account) {
        fire(Kind.LOGGED_IN, "Logged in", "account=" + safe(account));
    }

    public static void scriptBreak(String plugin, Throwable error) {
        String msg = error == null ? "unknown" : error.getClass().getSimpleName() + ": " + safe(error.getMessage());
        if (msg.length() > 400) {
            msg = msg.substring(0, 400) + "...";
        }
        fire(Kind.SCRIPT_BREAK, "Script break - " + safe(plugin), msg);
    }

    public static void scriptStop(String plugin, String detail) {
        fire(Kind.SCRIPT_STOP, "Script stopped - " + safe(plugin), safe(detail));
    }

    public static void info(String title, String detail) {
        fire(Kind.INFO, safe(title), safe(detail));
    }

    public static void test(String detail) {
        fire(Kind.TEST, "0xNotify test ping", safe(detail));
    }

    private static void fire(Kind kind, String title, String detail) {
        Sink s = sink;
        if (s == null) {
            return;
        }
        try {
            s.onNotify(kind, title, detail);
        } catch (Throwable t) {
            System.err.println("[0xNotify] sink failed: " + t.getMessage());
        }
    }

    private static String safe(String s) {
        return s == null || s.isEmpty() ? "-" : s;
    }
}
