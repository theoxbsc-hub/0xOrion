/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public final class WorldHopped {
    private final int worldId;

    public WorldHopped(int worldId) {
        this.worldId = worldId;
    }

    public int getWorldId() {
        return this.worldId;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WorldHopped)) {
            return false;
        }
        WorldHopped other = (WorldHopped)o;
        return this.getWorldId() == other.getWorldId();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getWorldId();
        return result;
    }

    public String toString() {
        return "WorldHopped(worldId=" + this.getWorldId() + ")";
    }
}

