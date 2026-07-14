/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.ox.mining;

public enum MiningMode {
    BANK("Bank"),
    DROP("Drop");

    private final String label;

    private MiningMode(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

