/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.inferno;

public enum InfernoCombatStyle {
    RANGED("Ranged"),
    MAGIC("Magic"),
    MELEE("Melee");

    private final String label;

    private InfernoCombatStyle(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

