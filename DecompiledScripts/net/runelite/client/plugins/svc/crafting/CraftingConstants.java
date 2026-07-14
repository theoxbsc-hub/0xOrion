/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.crafting;

final class CraftingConstants {
    static final int BANK_OBJECT_ID = 10083;
    static final String BANK_ACTION = "Use";
    static final String CHISEL_NAME = "Chisel";
    static final int GEMS_PER_INV = 27;
    static final int MAKE_INTERFACE_WAIT_MS = 900;
    static final int COMBINE_RETRY_MS = 2500;
    static final int BANK_OPEN_TIMEOUT_MS = 4000;
    static final int STUCK_MS = 4500;
    static final int IDLE_RECHECK_MS = 15000;
    static final int ANTI_BAN_MS = 180;

    private CraftingConstants() {
    }

    static boolean matches(String string, String string2) {
        if (string == null || string2 == null) {
            return false;
        }
        String string3 = CraftingConstants.normalize(string);
        for (String string4 : CraftingConstants.normalize(string2).split(" ")) {
            if (string4.isEmpty() || CraftingConstants.hasWord(string3, string4)) continue;
            return false;
        }
        return true;
    }

    private static boolean hasWord(String string, String string2) {
        for (String string3 : string.split(" ")) {
            if (!string3.equals(string2)) continue;
            return true;
        }
        return false;
    }

    static String normalize(String string) {
        if (string == null) {
            return "";
        }
        String string2 = string.toLowerCase().replaceAll("[^a-z0-9 ]", " ");
        return string2.replaceAll("\\s+", " ").trim();
    }
}

