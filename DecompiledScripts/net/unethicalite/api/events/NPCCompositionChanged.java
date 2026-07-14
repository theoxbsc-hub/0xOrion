/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public final class NPCCompositionChanged {
    private final int npcIndex;

    public NPCCompositionChanged(int npcIndex) {
        this.npcIndex = npcIndex;
    }

    public int getNpcIndex() {
        return this.npcIndex;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof NPCCompositionChanged)) {
            return false;
        }
        NPCCompositionChanged other = (NPCCompositionChanged)o;
        return this.getNpcIndex() == other.getNpcIndex();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getNpcIndex();
        return result;
    }

    public String toString() {
        return "NPCCompositionChanged(npcIndex=" + this.getNpcIndex() + ")";
    }
}

