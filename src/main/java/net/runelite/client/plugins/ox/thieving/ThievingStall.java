package net.runelite.client.plugins.ox.thieving;

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

    ThievingStall(int requiredLevel, int x, int y, int plane, int objectId, int traderNpcId, String label) {
        this.requiredLevel = requiredLevel;
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.objectId = objectId;
        this.traderNpcId = traderNpcId;
        this.label = label;
    }

    WorldPoint coords() {
        return new WorldPoint(this.x, this.y, this.plane);
    }
}
