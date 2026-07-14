/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.fishing;

public enum FishingMode {
    BANK("Bank"),
    DROP("Drop");

    private final String label;

    private FishingMode(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

