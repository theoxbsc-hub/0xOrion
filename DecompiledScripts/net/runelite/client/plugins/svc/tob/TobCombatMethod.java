/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 */
package net.runelite.client.plugins.svc.tob;

import java.util.Locale;
import net.runelite.api.Item;

public enum TobCombatMethod {
    AUTO("Auto-detect"),
    MELEE("Melee"),
    DHAROKS("Dharok's (low HP)"),
    RANGED("Ranged"),
    MAGIC("Magic");

    private final String label;
    private static final String[] RANGED_HINTS;
    private static final String[] MAGIC_HINTS;
    private static final String[] DHAROK_HINTS;

    private TobCombatMethod(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }

    static TobCombatMethod classifyWeapon(String string) {
        if (string == null) {
            return MELEE;
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        for (String string3 : DHAROK_HINTS) {
            if (!string2.contains(string3)) continue;
            return DHAROKS;
        }
        for (String string3 : MAGIC_HINTS) {
            if (!string2.contains(string3)) continue;
            return MAGIC;
        }
        for (String string3 : RANGED_HINTS) {
            if (!string2.contains(string3)) continue;
            return RANGED;
        }
        return MELEE;
    }

    static TobCombatMethod detect(Item item, Iterable<Item> iterable, boolean bl) {
        TobCombatMethod tobCombatMethod;
        TobCombatMethod tobCombatMethod2 = tobCombatMethod = item != null ? TobCombatMethod.classifyWeapon(item.getName()) : null;
        if (tobCombatMethod == DHAROKS) {
            return bl ? DHAROKS : MELEE;
        }
        if (tobCombatMethod != null && tobCombatMethod != MELEE) {
            return tobCombatMethod;
        }
        if (bl && iterable != null) {
            for (Item item2 : iterable) {
                if (item2 == null || item2.getName() == null || TobCombatMethod.classifyWeapon(item2.getName()) != DHAROKS) continue;
                return DHAROKS;
            }
        }
        return tobCombatMethod != null ? tobCombatMethod : MELEE;
    }

    static {
        RANGED_HINTS = new String[]{"bow", "blowpipe", "crossbow", "c'bow", "ballista", "chinchompa", "dart", "knife", "thrownaxe", "throwing", "javelin", "bofa", "eclipse atlatl"};
        MAGIC_HINTS = new String[]{"staff", "trident", "sanguinesti", "sang ", "wand", "sceptre", "crozier", "tumeken", "shadow", "harmonised", "nightmare staff", "dawnbringer", "swamp"};
        DHAROK_HINTS = new String[]{"dharok's greataxe", "dharoks greataxe"};
    }
}

