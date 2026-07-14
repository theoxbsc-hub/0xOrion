/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Entitlements {
    private static volatile boolean installed;
    private static volatile boolean all;
    private static volatile Set<String> allowed;

    private Entitlements() {
    }

    public static synchronized void installAll() {
        all = true;
        allowed = Collections.emptySet();
        installed = true;
    }

    public static synchronized void install(String string) {
        if (string == null) {
            all = false;
            allowed = Collections.emptySet();
            installed = true;
            return;
        }
        String string2 = string.trim();
        if (string2.equals("*")) {
            Entitlements.installAll();
            return;
        }
        HashSet<String> hashSet = new HashSet<String>();
        for (String string3 : string2.split(",")) {
            String string4 = string3.trim();
            if (string4.isEmpty()) continue;
            hashSet.add(string4);
        }
        all = false;
        allowed = Collections.unmodifiableSet(hashSet);
        installed = true;
    }

    public static boolean installed() {
        return installed;
    }

    public static boolean isAll() {
        return all;
    }

    public static boolean isAllowed(String string) {
        if (all) {
            return true;
        }
        return string != null && allowed.contains(string);
    }

    public static Set<String> allowed() {
        return allowed;
    }

    static {
        allowed = Collections.emptySet();
    }
}

