/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.corruptedgauntlet;

public enum HunllefWeapon {
    RANGED("Ranged (bow)", "Corrupted bow"),
    MAGIC("Magic (staff)", "Corrupted staff"),
    MELEE("Melee (halberd)", "Corrupted halberd");

    private final String label;
    final String defaultNameFragment;

    private HunllefWeapon(String string2, String string3) {
        this.label = string2;
        this.defaultNameFragment = string3;
    }

    public String toString() {
        return this.label;
    }
}

