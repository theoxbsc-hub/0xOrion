/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.movement.pathfinder.model;

import net.runelite.api.coords.WorldPoint;

public final class Teleport {
    private final WorldPoint destination;
    private final int radius;
    private final Runnable handler;

    public Teleport(WorldPoint destination, int radius, Runnable handler) {
        this.destination = destination;
        this.radius = radius;
        this.handler = handler;
    }

    public WorldPoint getDestination() {
        return this.destination;
    }

    public int getRadius() {
        return this.radius;
    }

    public Runnable getHandler() {
        return this.handler;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Teleport)) {
            return false;
        }
        Teleport other = (Teleport)o;
        if (this.getRadius() != other.getRadius()) {
            return false;
        }
        WorldPoint this$destination = this.getDestination();
        WorldPoint other$destination = other.getDestination();
        if (this$destination == null ? other$destination != null : !this$destination.equals(other$destination)) {
            return false;
        }
        Runnable this$handler = this.getHandler();
        Runnable other$handler = other.getHandler();
        return !(this$handler == null ? other$handler != null : !this$handler.equals(other$handler));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getRadius();
        WorldPoint $destination = this.getDestination();
        result = result * 59 + ($destination == null ? 43 : $destination.hashCode());
        Runnable $handler = this.getHandler();
        result = result * 59 + ($handler == null ? 43 : $handler.hashCode());
        return result;
    }

    public String toString() {
        return "Teleport(destination=" + String.valueOf(this.getDestination()) + ", radius=" + this.getRadius() + ", handler=" + String.valueOf(this.getHandler()) + ")";
    }
}

