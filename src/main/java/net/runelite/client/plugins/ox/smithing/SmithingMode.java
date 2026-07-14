/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.ox.smithing;

public enum SmithingMode {
    AUTO("Auto (smelt + smith)"),
    SMELT_ONLY("Smelt only"),
    SMITH_ONLY("Smith only");

    private final String label;

    private SmithingMode(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

