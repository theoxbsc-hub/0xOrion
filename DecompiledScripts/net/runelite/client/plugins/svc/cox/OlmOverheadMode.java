/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.cox;

public enum OlmOverheadMode {
    ALWAYS_MAGIC("Always Protect from Magic"),
    ALWAYS_MISSILES("Always Protect from Missiles"),
    PROJECTILE_SWITCH("Switch on Projectile");

    private final String label;

    private OlmOverheadMode(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

