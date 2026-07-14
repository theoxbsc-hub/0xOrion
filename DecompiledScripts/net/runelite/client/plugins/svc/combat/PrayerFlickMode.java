/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.combat;

public enum PrayerFlickMode {
    ALWAYS_ON("Always on"),
    DEFENSIVE_FLICK("Defensive flick"),
    OFFENSIVE_FLICK("Offensive flick"),
    FULL_FLICK("Full flick");

    private final String label;

    private PrayerFlickMode(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

