/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.cox;

public enum OlmHeadStrategy {
    HANDS_THEN_HEAD("Hands then head (OSRS rules \u2014 recommended)"),
    AUTO("Auto (hands first, then probe the head)"),
    HEAD_ONLY("Head only");

    private final String label;

    private OlmHeadStrategy(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

