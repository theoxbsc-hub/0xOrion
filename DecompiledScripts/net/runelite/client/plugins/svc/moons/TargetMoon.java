/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.moons;

import net.runelite.api.Prayer;

public enum TargetMoon {
    BLOOD("Blood Moon", 51380, 13011, 29468, "Blood Moon", Prayer.PROTECT_FROM_MELEE, 0),
    BLUE("Blue Moon", 51382, 13013, 30042, "Blue Moon", Prayer.PROTECT_FROM_MELEE, 0),
    ECLIPSE("Eclipse Moon", 51384, 13012, 29426, "Eclipse Moon", Prayer.PROTECT_FROM_MISSILES, 34936);

    private final String label;
    public final int muralId;
    public final int bossNpcId;
    public final int roomRegion;
    public final String defaultBossName;
    public final Prayer defaultPrayer;
    public final int projectileId;

    private TargetMoon(String string2, int n2, int n3, int n4, String string3, Prayer prayer, int n5) {
        this.label = string2;
        this.muralId = n2;
        this.bossNpcId = n3;
        this.roomRegion = n4;
        this.defaultBossName = string3;
        this.defaultPrayer = prayer;
        this.projectileId = n5;
    }

    public String toString() {
        return this.label;
    }
}

