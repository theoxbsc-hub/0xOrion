/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.movement.pathfinder.model.requirement;

import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirement;

public final class WorldRequirement
implements Requirement {
    private final boolean memberWorld;

    @Override
    public Boolean get() {
        return !this.memberWorld || Worlds.inMembersWorld();
    }

    public WorldRequirement(boolean memberWorld) {
        this.memberWorld = memberWorld;
    }

    public boolean isMemberWorld() {
        return this.memberWorld;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WorldRequirement)) {
            return false;
        }
        WorldRequirement other = (WorldRequirement)o;
        return this.isMemberWorld() == other.isMemberWorld();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isMemberWorld() ? 79 : 97);
        return result;
    }

    public String toString() {
        return "WorldRequirement(memberWorld=" + this.isMemberWorld() + ")";
    }
}

