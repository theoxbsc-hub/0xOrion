/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.incognito;

public enum IdentityMode {
    RANDOM_EACH_STARTUP("Random Each Startup"),
    PERSISTENT("Persistent (same fake ID)"),
    MANUAL("Manual Entry");

    private final String name;

    private IdentityMode(String string2) {
        this.name = string2;
    }

    public String toString() {
        return this.name;
    }
}

