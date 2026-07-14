/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.chatresponder;

public enum Persona {
    SLANG("Casual slang"),
    NEUTRAL("Neutral"),
    MINIMAL("Minimal / low-effort");

    private final String label;

    private Persona(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

