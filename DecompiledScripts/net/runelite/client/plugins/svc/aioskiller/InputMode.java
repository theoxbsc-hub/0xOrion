/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.aioskiller;

public enum InputMode {
    MOUSE_MOVEMENTS("Mouse movements (real cursor)"),
    PACKETS_ONLY("Packets (primed)");

    private final String label;

    private InputMode(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

