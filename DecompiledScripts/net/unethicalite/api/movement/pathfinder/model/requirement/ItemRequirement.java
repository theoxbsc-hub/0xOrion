/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.movement.pathfinder.model.requirement;

import java.util.List;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.requirement.Reduction;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirement;

public final class ItemRequirement
implements Requirement {
    private final Reduction reduction;
    private final boolean equipped;
    private final List<Integer> ids;
    private final int amount;

    @Override
    public Boolean get() {
        switch (this.reduction) {
            case AND: {
                if (this.equipped) {
                    return this.ids.stream().allMatch(it -> Equipment.getCount(true, it) >= this.amount);
                }
                return this.ids.stream().allMatch(it -> Inventory.getCount(true, it) >= this.amount);
            }
            case OR: {
                if (this.equipped) {
                    return this.ids.stream().anyMatch(it -> Equipment.getCount(true, it) >= this.amount);
                }
                return this.ids.stream().anyMatch(it -> Inventory.getCount(true, it) >= this.amount);
            }
        }
        return false;
    }

    public ItemRequirement(Reduction reduction, boolean equipped, List<Integer> ids, int amount) {
        this.reduction = reduction;
        this.equipped = equipped;
        this.ids = ids;
        this.amount = amount;
    }

    public Reduction getReduction() {
        return this.reduction;
    }

    public boolean isEquipped() {
        return this.equipped;
    }

    public List<Integer> getIds() {
        return this.ids;
    }

    public int getAmount() {
        return this.amount;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ItemRequirement)) {
            return false;
        }
        ItemRequirement other = (ItemRequirement)o;
        if (this.isEquipped() != other.isEquipped()) {
            return false;
        }
        if (this.getAmount() != other.getAmount()) {
            return false;
        }
        Reduction this$reduction = this.getReduction();
        Reduction other$reduction = other.getReduction();
        if (this$reduction == null ? other$reduction != null : !((Object)((Object)this$reduction)).equals((Object)other$reduction)) {
            return false;
        }
        List<Integer> this$ids = this.getIds();
        List<Integer> other$ids = other.getIds();
        return !(this$ids == null ? other$ids != null : !((Object)this$ids).equals(other$ids));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isEquipped() ? 79 : 97);
        result = result * 59 + this.getAmount();
        Reduction $reduction = this.getReduction();
        result = result * 59 + ($reduction == null ? 43 : ((Object)((Object)$reduction)).hashCode());
        List<Integer> $ids = this.getIds();
        result = result * 59 + ($ids == null ? 43 : ((Object)$ids).hashCode());
        return result;
    }

    public String toString() {
        return "ItemRequirement(reduction=" + String.valueOf((Object)this.getReduction()) + ", equipped=" + this.isEquipped() + ", ids=" + String.valueOf(this.getIds()) + ", amount=" + this.getAmount() + ")";
    }
}

