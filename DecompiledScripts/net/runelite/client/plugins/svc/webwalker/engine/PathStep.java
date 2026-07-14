/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.pathfinder.model.Transport;

public final class PathStep {
    public final WorldPoint point;
    public final Transport transport;

    public PathStep(WorldPoint worldPoint, Transport transport) {
        this.point = worldPoint;
        this.transport = transport;
    }

    public boolean isTransport() {
        return this.transport != null;
    }

    public String toString() {
        return (this.transport != null ? "[T]" : "") + String.valueOf(this.point);
    }
}

