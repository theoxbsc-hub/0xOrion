/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.chatresponder;

public enum ResponseMode {
    NAME_ONLY("Name mentioned only"),
    NEARBY("Nearby players"),
    ALL_PUBLIC("All public chat");

    private final String label;

    private ResponseMode(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

