/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.nex;

public enum NexCombatStyle {
    RANGED("Ranged"),
    MAGE("Magic"),
    MELEE("Melee");

    private final String label;

    private NexCombatStyle(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

