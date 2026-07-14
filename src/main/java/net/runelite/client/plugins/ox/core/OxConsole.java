package net.runelite.client.plugins.ox.core;

/**
 * Clean CMD event lines for 0x (keeps console readable vs RuneLite spam).
 */
public final class OxConsole {
    private OxConsole() {
    }

    public static void banner() {
        String account = prop("ox.displayName");
        String user = prop("ox.username");
        String mac = first(prop("ox.mac"), prop("incognito.mac"));
        String proxy = prop("ox.proxy");
        boolean proxied = proxy != null && !proxy.isEmpty();
        String proxyShow = proxied ? maskProxy(proxy) : "DIRECT";

        line();
        out("  0x Client");
        sep();
        out("  connected : " + (proxied ? "proxy" : "direct"));
        out("  account   : " + (account.isEmpty() ? "(none)" : account)
            + (user.isEmpty() ? "" : "  (" + user + ")"));
        out("  mac       : " + (mac.isEmpty() ? "(none)" : mac));
        out("  proxy     : " + proxyShow);
        sep();
        out("  waiting for plugins...");
        line();
    }

    public static void plugins(String... names) {
        sep();
        out("  enabled plugins:");
        if (names == null || names.length == 0) {
            out("    (none reported yet)");
        } else {
            for (String name : names) {
                out("    - " + name);
            }
        }
        line();
    }

    public static void info(String plugin, String message) {
        out("[0x] " + pad(plugin) + "  " + message);
    }

    public static void warn(String plugin, String message) {
        out("[0x] " + pad(plugin) + "  ! " + message);
    }

    public static void alert(String plugin, String message) {
        out("[0x] " + pad(plugin) + "  !! " + message);
    }

    private static String pad(String plugin) {
        if (plugin == null) {
            plugin = "";
        }
        if (plugin.length() >= 12) {
            return plugin.substring(0, 12);
        }
        StringBuilder sb = new StringBuilder(plugin);
        while (sb.length() < 12) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String maskProxy(String raw) {
        String[] p = raw.split(":", -1);
        if (p.length >= 4) {
            return p[0] + ":" + p[1] + "  user=" + p[2];
        }
        if (p.length >= 2) {
            return p[0] + ":" + p[1];
        }
        return raw;
    }

    private static String prop(String key) {
        String v = System.getProperty(key, "");
        return v == null ? "" : v.trim();
    }

    private static String first(String a, String b) {
        return !a.isEmpty() ? a : b;
    }

    private static void out(String s) {
        System.out.println(s);
    }

    private static void line() {
        out("============================================================");
    }

    private static void sep() {
        out("------------------------------------------------------------");
    }
}
