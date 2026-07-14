/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.packets.IsaacCipher
 */
package net.unethicalite.api.events;

import java.util.Arrays;
import net.runelite.api.packets.IsaacCipher;

public final class IsaacCipherGenerated {
    private final int[] seed;
    private final IsaacCipher isaacCipher;

    public IsaacCipherGenerated(int[] seed, IsaacCipher isaacCipher) {
        this.seed = seed;
        this.isaacCipher = isaacCipher;
    }

    public int[] getSeed() {
        return this.seed;
    }

    public IsaacCipher getIsaacCipher() {
        return this.isaacCipher;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof IsaacCipherGenerated)) {
            return false;
        }
        IsaacCipherGenerated other = (IsaacCipherGenerated)o;
        if (!Arrays.equals(this.getSeed(), other.getSeed())) {
            return false;
        }
        IsaacCipher this$isaacCipher = this.getIsaacCipher();
        IsaacCipher other$isaacCipher = other.getIsaacCipher();
        return !(this$isaacCipher == null ? other$isaacCipher != null : !this$isaacCipher.equals(other$isaacCipher));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + Arrays.hashCode(this.getSeed());
        IsaacCipher $isaacCipher = this.getIsaacCipher();
        result = result * 59 + ($isaacCipher == null ? 43 : $isaacCipher.hashCode());
        return result;
    }

    public String toString() {
        return "IsaacCipherGenerated(seed=" + Arrays.toString(this.getSeed()) + ", isaacCipher=" + String.valueOf(this.getIsaacCipher()) + ")";
    }
}

