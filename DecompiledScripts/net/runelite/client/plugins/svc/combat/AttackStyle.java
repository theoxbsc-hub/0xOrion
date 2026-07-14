/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.combat;

public enum AttackStyle {
    AUTO("Auto (game default)"),
    ACCURATE("Accurate"),
    AGGRESSIVE("Aggressive"),
    DEFENSIVE("Defensive"),
    CONTROLLED("Controlled"),
    RAPID("Rapid"),
    LONGRANGE("Longrange");

    private final String name;

    private AttackStyle(String string2) {
        this.name = string2;
    }

    public String toString() {
        return this.name;
    }
}

