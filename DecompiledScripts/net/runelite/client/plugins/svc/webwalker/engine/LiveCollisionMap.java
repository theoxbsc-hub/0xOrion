/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.webwalker.engine.CollisionMap;
import net.unethicalite.api.movement.Reachable;

public final class LiveCollisionMap
implements CollisionMap {
    private static final int NORTH = 2;
    private static final int SOUTH = 32;
    private static final int EAST = 8;
    private static final int WEST = 128;
    private static final int FULL = 2359552;

    private static int flag(int n, int n2, int n3) {
        try {
            return Reachable.getCollisionFlag(new WorldPoint(n, n2, n3));
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    @Override
    public boolean n(int n, int n2, int n3) {
        int n4 = LiveCollisionMap.flag(n, n2, n3);
        int n5 = LiveCollisionMap.flag(n, n2 + 1, n3);
        if (n4 == 0 && n5 == 0) {
            return true;
        }
        return (n4 & 2) == 0 && (n5 & 0x240120) == 0;
    }

    @Override
    public boolean e(int n, int n2, int n3) {
        int n4 = LiveCollisionMap.flag(n, n2, n3);
        int n5 = LiveCollisionMap.flag(n + 1, n2, n3);
        if (n4 == 0 && n5 == 0) {
            return true;
        }
        return (n4 & 8) == 0 && (n5 & 0x240180) == 0;
    }

    @Override
    public boolean hasData(int n, int n2, int n3) {
        return LiveCollisionMap.flag(n, n2, n3) != 0;
    }
}

