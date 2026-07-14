package net.runelite.client.plugins.ox.info;

/**
 * Snapshot of the launch identity (account / MAC / proxy) for the current JVM.
 */
public final class OxRuntimeInfo {
    private final String accountId;
    private final String displayName;
    private final String username;
    private final String mac;
    private final String hostname;
    private final String proxyRaw;
    private final boolean proxyEnabled;

    private OxRuntimeInfo(
        String accountId,
        String displayName,
        String username,
        String mac,
        String hostname,
        String proxyRaw,
        boolean proxyEnabled
    ) {
        this.accountId = accountId;
        this.displayName = displayName;
        this.username = username;
        this.mac = mac;
        this.hostname = hostname;
        this.proxyRaw = proxyRaw;
        this.proxyEnabled = proxyEnabled;
    }

    public static OxRuntimeInfo read() {
        String accountId = prop("ox.accountId");
        String displayName = prop("ox.displayName");
        String username = prop("ox.username");
        String mac = firstNonBlank(prop("ox.mac"), prop("incognito.mac"));
        String hostname = prop("ox.hostname");
        String proxyRaw = prop("ox.proxy");
        boolean proxyEnabled = !proxyRaw.isEmpty();
        return new OxRuntimeInfo(accountId, displayName, username, mac, hostname, proxyRaw, proxyEnabled);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getDisplayName() {
        return displayName.isEmpty() ? "(unknown)" : displayName;
    }

    public String getUsername() {
        return username.isEmpty() ? "(none)" : username;
    }

    public String getMac() {
        return mac.isEmpty() ? "(not set)" : mac;
    }

    public String getHostname() {
        return hostname.isEmpty() ? "(none)" : hostname;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public String getProxySummary() {
        if (!proxyEnabled) {
            return "DIRECT";
        }
        String[] parts = proxyRaw.split(":", -1);
        if (parts.length >= 4) {
            return parts[0] + ":" + parts[1] + "  user=" + parts[2] + "  pass=****";
        }
        if (parts.length >= 2) {
            return parts[0] + ":" + parts[1];
        }
        return proxyRaw;
    }

    public String getProxyHostPort() {
        if (!proxyEnabled) {
            return "";
        }
        String[] parts = proxyRaw.split(":", -1);
        if (parts.length >= 2) {
            return parts[0] + ":" + parts[1];
        }
        return proxyRaw;
    }

    private static String prop(String key) {
        String v = System.getProperty(key, "");
        return v == null ? "" : v.trim();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        return b == null ? "" : b;
    }
}
