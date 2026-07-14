/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.core;

public final class IdentitySnapshot {
    private static volatile boolean set;
    private static volatile String macLive;
    private static volatile boolean macOk;
    private static volatile String gameUid;
    private static volatile String cacheDir;
    private static volatile String javaVendor;
    private static volatile String javaVersion;
    private static volatile String revision;

    private IdentitySnapshot() {
    }

    public static void set(String string, boolean bl, String string2, String string3, String string4, String string5, String string6) {
        macLive = IdentitySnapshot.nz(string);
        macOk = bl;
        gameUid = IdentitySnapshot.nz(string2);
        cacheDir = IdentitySnapshot.nz(string3);
        javaVendor = IdentitySnapshot.nz(string4);
        javaVersion = IdentitySnapshot.nz(string5);
        revision = IdentitySnapshot.nz(string6);
        set = true;
    }

    public static boolean isSet() {
        return set;
    }

    public static String report() {
        if (!set) {
            return "    (Incognito Shield not active this session \u2014 identity was NOT spoofed)";
        }
        return "    MAC (live) : " + macLive + (macOk ? "  [spoof OK]" : "  *** REAL MAC LEAKING ***") + "\n    Game UID   : " + gameUid + "  (signlink.uid \u2014 must be UNIQUE vs your other accounts)\n    Cache dir  : " + cacheDir + "\n    Java       : " + javaVendor + " " + javaVersion + "\n    Revision   : " + revision + "  (shared by ALL Orion clients \u2014 expected to match)";
    }

    private static String nz(String string) {
        return string == null || string.isEmpty() ? "?" : string;
    }

    static {
        macLive = "?";
        gameUid = "?";
        cacheDir = "(default)";
        javaVendor = "?";
        javaVersion = "?";
        revision = "?";
    }
}

