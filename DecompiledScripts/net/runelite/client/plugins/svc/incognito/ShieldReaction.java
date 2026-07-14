/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.incognito;

public enum ShieldReaction {
    TELEPORT_HOME("Teleport home (::home)"),
    LOGOUT("Log out");

    private final String label;

    private ShieldReaction(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

