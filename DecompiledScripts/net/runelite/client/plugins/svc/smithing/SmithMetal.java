/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.smithing;

public enum SmithMetal {
    BRONZE("Bronze", "Bronze bar", 2349, 1),
    IRON("Iron", "Iron bar", 2351, 15),
    STEEL("Steel", "Steel bar", 2353, 30),
    MITHRIL("Mithril", "Mithril bar", 2359, 50),
    ADAMANT("Adamant", "Adamantite bar", 2361, 70),
    RUNE("Rune", "Runite bar", 2363, 85);

    public final String metalName;
    public final String barName;
    public final int barId;
    public final int smithLevel;

    private SmithMetal(String string2, String string3, int n2, int n3) {
        this.metalName = string2;
        this.barName = string3;
        this.barId = n2;
        this.smithLevel = n3;
    }

    public String toString() {
        return this.barName;
    }
}

