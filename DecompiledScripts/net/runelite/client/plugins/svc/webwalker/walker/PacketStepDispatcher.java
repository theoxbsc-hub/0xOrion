/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.walker;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.webwalker.walker.StepDispatcher;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MovementPackets;

public final class PacketStepDispatcher
implements StepDispatcher {
    @Override
    public void step(WorldPoint worldPoint, WorldPoint worldPoint2) {
        if (worldPoint2 == null) {
            return;
        }
        boolean bl = false;
        try {
            bl = Movement.isRunEnabled();
            if (!bl && Movement.getRunEnergy() >= 3000) {
                Movement.toggleRun();
                bl = true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            MovementPackets.sendMovement(worldPoint2, bl);
        }
        catch (Throwable throwable) {
            try {
                MovementPackets.sendMovement(worldPoint2);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
    }
}

