/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.hunter;

public enum HunterMethod {
    BIRD_SNARE("Bird snare"),
    BOX_TRAP("Box trap (Chinchompa)");

    private final String label;

    private HunterMethod(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

