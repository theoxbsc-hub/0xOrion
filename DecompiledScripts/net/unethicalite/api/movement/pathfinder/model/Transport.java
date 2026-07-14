/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.movement.pathfinder.model;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirements;

public final class Transport {
    private final WorldPoint source;
    private final WorldPoint destination;
    private final int sourceRadius;
    private final int destinationRadius;
    private final Runnable handler;
    private final Requirements requirements;

    public Transport(WorldPoint source, WorldPoint destination, int sourceRadius, int destinationRadius, Runnable handler) {
        this.source = source;
        this.destination = destination;
        this.sourceRadius = sourceRadius;
        this.destinationRadius = destinationRadius;
        this.handler = handler;
        this.requirements = new Requirements();
    }

    public WorldPoint getSource() {
        return this.source;
    }

    public WorldPoint getDestination() {
        return this.destination;
    }

    public int getSourceRadius() {
        return this.sourceRadius;
    }

    public int getDestinationRadius() {
        return this.destinationRadius;
    }

    public Runnable getHandler() {
        return this.handler;
    }

    public Requirements getRequirements() {
        return this.requirements;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Transport)) {
            return false;
        }
        Transport other = (Transport)o;
        if (this.getSourceRadius() != other.getSourceRadius()) {
            return false;
        }
        if (this.getDestinationRadius() != other.getDestinationRadius()) {
            return false;
        }
        WorldPoint this$source = this.getSource();
        WorldPoint other$source = other.getSource();
        if (this$source == null ? other$source != null : !this$source.equals(other$source)) {
            return false;
        }
        WorldPoint this$destination = this.getDestination();
        WorldPoint other$destination = other.getDestination();
        if (this$destination == null ? other$destination != null : !this$destination.equals(other$destination)) {
            return false;
        }
        Runnable this$handler = this.getHandler();
        Runnable other$handler = other.getHandler();
        if (this$handler == null ? other$handler != null : !this$handler.equals(other$handler)) {
            return false;
        }
        Requirements this$requirements = this.getRequirements();
        Requirements other$requirements = other.getRequirements();
        return !(this$requirements == null ? other$requirements != null : !((Object)this$requirements).equals(other$requirements));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getSourceRadius();
        result = result * 59 + this.getDestinationRadius();
        WorldPoint $source = this.getSource();
        result = result * 59 + ($source == null ? 43 : $source.hashCode());
        WorldPoint $destination = this.getDestination();
        result = result * 59 + ($destination == null ? 43 : $destination.hashCode());
        Runnable $handler = this.getHandler();
        result = result * 59 + ($handler == null ? 43 : $handler.hashCode());
        Requirements $requirements = this.getRequirements();
        result = result * 59 + ($requirements == null ? 43 : ((Object)$requirements).hashCode());
        return result;
    }

    public String toString() {
        return "Transport(source=" + String.valueOf(this.getSource()) + ", destination=" + String.valueOf(this.getDestination()) + ", sourceRadius=" + this.getSourceRadius() + ", destinationRadius=" + this.getDestinationRadius() + ", handler=" + String.valueOf(this.getHandler()) + ", requirements=" + String.valueOf(this.getRequirements()) + ")";
    }

    public Transport(WorldPoint source, WorldPoint destination, int sourceRadius, int destinationRadius, Runnable handler, Requirements requirements) {
        this.source = source;
        this.destination = destination;
        this.sourceRadius = sourceRadius;
        this.destinationRadius = destinationRadius;
        this.handler = handler;
        this.requirements = requirements;
    }
}

