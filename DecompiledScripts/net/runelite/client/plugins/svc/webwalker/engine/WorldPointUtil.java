/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import net.runelite.api.coords.WorldPoint;

public final class WorldPointUtil {
    public static final int UNDEFINED = -1;

    private WorldPointUtil() {
    }

    public static int pack(int n, int n2, int n3) {
        return n & Short.MAX_VALUE | (n2 & Short.MAX_VALUE) << 15 | (n3 & 3) << 30;
    }

    public static int pack(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return -1;
        }
        return WorldPointUtil.pack(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    public static int x(int n) {
        return n & Short.MAX_VALUE;
    }

    public static int y(int n) {
        return n >> 15 & Short.MAX_VALUE;
    }

    public static int plane(int n) {
        return n >> 30 & 3;
    }

    public static WorldPoint unpack(int n) {
        return new WorldPoint(WorldPointUtil.x(n), WorldPointUtil.y(n), WorldPointUtil.plane(n));
    }

    public static int offset(int n, int n2, int n3) {
        return WorldPointUtil.pack(WorldPointUtil.x(n) + n2, WorldPointUtil.y(n) + n3, WorldPointUtil.plane(n));
    }

    public static int chebyshev(int n, int n2) {
        return Math.max(Math.abs(WorldPointUtil.x(n) - WorldPointUtil.x(n2)), Math.abs(WorldPointUtil.y(n) - WorldPointUtil.y(n2)));
    }

    public static int chebyshev(WorldPoint worldPoint, WorldPoint worldPoint2) {
        return Math.max(Math.abs(worldPoint.getX() - worldPoint2.getX()), Math.abs(worldPoint.getY() - worldPoint2.getY()));
    }

    public static long euclidSq(int n, int n2) {
        long l = WorldPointUtil.x(n) - WorldPointUtil.x(n2);
        long l2 = WorldPointUtil.y(n) - WorldPointUtil.y(n2);
        return l * l + l2 * l2;
    }
}

