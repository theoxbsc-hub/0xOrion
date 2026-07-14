/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MovementPackets;

public final class Walk {
    public static final int STEP = 15;

    private Walk() {
    }

    public static boolean arrived(WorldPoint worldPoint, WorldPoint worldPoint2, int n) {
        return worldPoint != null && worldPoint2 != null && worldPoint.getPlane() == worldPoint2.getPlane() && worldPoint.distanceTo(worldPoint2) <= n;
    }

    public static void toward(WorldPoint worldPoint, WorldPoint worldPoint2) {
        if (worldPoint == null || worldPoint2 == null) {
            return;
        }
        try {
            if (Movement.getRunEnergy() >= 3000 && !Movement.isRunEnabled()) {
                Movement.toggleRun();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        MovementPackets.sendMovement(Walk.waypoint(worldPoint, worldPoint2));
    }

    public static WorldPoint waypoint(WorldPoint worldPoint, WorldPoint worldPoint2) {
        int n = worldPoint2.getX() - worldPoint.getX();
        int n2 = worldPoint2.getY() - worldPoint.getY();
        int n3 = worldPoint.getX() + Integer.signum(n) * Math.min(Math.abs(n), 15);
        int n4 = worldPoint.getY() + Integer.signum(n2) * Math.min(Math.abs(n2), 15);
        return new WorldPoint(n3, n4, worldPoint.getPlane());
    }
}

