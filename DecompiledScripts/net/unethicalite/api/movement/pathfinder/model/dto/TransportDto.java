/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.movement.pathfinder.model.dto;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.pathfinder.TransportLoader;
import net.unethicalite.api.movement.pathfinder.model.Transport;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirements;

public final class TransportDto {
    private final WorldPoint source;
    private final WorldPoint destination;
    private final String action;
    private final Integer objectId;
    private final Requirements requirements;

    public Transport toTransport() {
        return TransportLoader.objectTransport(this.source, this.destination, this.objectId, this.action, this.requirements);
    }

    public TransportDto(WorldPoint source, WorldPoint destination, String action, Integer objectId, Requirements requirements) {
        this.source = source;
        this.destination = destination;
        this.action = action;
        this.objectId = objectId;
        this.requirements = requirements;
    }

    public WorldPoint getSource() {
        return this.source;
    }

    public WorldPoint getDestination() {
        return this.destination;
    }

    public String getAction() {
        return this.action;
    }

    public Integer getObjectId() {
        return this.objectId;
    }

    public Requirements getRequirements() {
        return this.requirements;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TransportDto)) {
            return false;
        }
        TransportDto other = (TransportDto)o;
        Integer this$objectId = this.getObjectId();
        Integer other$objectId = other.getObjectId();
        if (this$objectId == null ? other$objectId != null : !((Object)this$objectId).equals(other$objectId)) {
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
        String this$action = this.getAction();
        String other$action = other.getAction();
        if (this$action == null ? other$action != null : !this$action.equals(other$action)) {
            return false;
        }
        Requirements this$requirements = this.getRequirements();
        Requirements other$requirements = other.getRequirements();
        return !(this$requirements == null ? other$requirements != null : !((Object)this$requirements).equals(other$requirements));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Integer $objectId = this.getObjectId();
        result = result * 59 + ($objectId == null ? 43 : ((Object)$objectId).hashCode());
        WorldPoint $source = this.getSource();
        result = result * 59 + ($source == null ? 43 : $source.hashCode());
        WorldPoint $destination = this.getDestination();
        result = result * 59 + ($destination == null ? 43 : $destination.hashCode());
        String $action = this.getAction();
        result = result * 59 + ($action == null ? 43 : $action.hashCode());
        Requirements $requirements = this.getRequirements();
        result = result * 59 + ($requirements == null ? 43 : ((Object)$requirements).hashCode());
        return result;
    }

    public String toString() {
        return "TransportDto(source=" + String.valueOf(this.getSource()) + ", destination=" + String.valueOf(this.getDestination()) + ", action=" + this.getAction() + ", objectId=" + this.getObjectId() + ", requirements=" + String.valueOf(this.getRequirements()) + ")";
    }
}

