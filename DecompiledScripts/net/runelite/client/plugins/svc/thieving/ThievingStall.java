/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.thieving;

import net.runelite.api.coords.WorldPoint;

enum ThievingStall {
    LVL_1(1, 2797, 3378, 0, 47339, 12368, "Lvl 1"),
    LVL_25(25, 2780, 3374, 0, 47376, 8693, "Lvl 25"),
    LVL_50(50, 2764, 3376, 0, 47343, 12369, "Lvl 50"),
    LVL_75(75, 2761, 3377, 0, 47340, 12367, "Lvl 75"),
    LVL_90(90, 2771, 3387, 0, 47375, 155, "Lvl 90");

    final int requiredLevel;
    final int x;
    final int y;
    final int plane;
    final int objectId;
    final int traderNpcId;
    final String label;

    private ThievingStall(int n2, int n3, int n4, int n5, int n6, int n7, String string2) {
        this.requiredLevel = n2;
        this.x = n3;
        this.y = n4;
        this.plane = n5;
        this.objectId = n6;
        this.traderNpcId = n7;
        this.label = string2;
    }

    WorldPoint coords() {
        return new WorldPoint(this.x, this.y, this.plane);
    }
}

