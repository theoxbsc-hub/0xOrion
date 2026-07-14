/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.slayer;

public enum SlayerMaster {
    VANNAKA("Vannaka", 403),
    NIEVE("Nieve", 6797),
    KRYSTILIA("Krystilia", 7663);

    private final String displayName;
    private final int npcId;

    private SlayerMaster(String string2, int n2) {
        this.displayName = string2;
        this.npcId = n2;
    }

    public int getNpcId() {
        return this.npcId;
    }

    public String toString() {
        return this.displayName;
    }
}

