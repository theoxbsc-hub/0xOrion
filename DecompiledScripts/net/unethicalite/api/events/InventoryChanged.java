/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public final class InventoryChanged {
    private final ChangeType changeType;
    private final int itemId;
    private final int amount;

    public InventoryChanged(ChangeType changeType, int itemId, int amount) {
        this.changeType = changeType;
        this.itemId = itemId;
        this.amount = amount;
    }

    public ChangeType getChangeType() {
        return this.changeType;
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getAmount() {
        return this.amount;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof InventoryChanged)) {
            return false;
        }
        InventoryChanged other = (InventoryChanged)o;
        if (this.getItemId() != other.getItemId()) {
            return false;
        }
        if (this.getAmount() != other.getAmount()) {
            return false;
        }
        ChangeType this$changeType = this.getChangeType();
        ChangeType other$changeType = other.getChangeType();
        return !(this$changeType == null ? other$changeType != null : !((Object)((Object)this$changeType)).equals((Object)other$changeType));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getItemId();
        result = result * 59 + this.getAmount();
        ChangeType $changeType = this.getChangeType();
        result = result * 59 + ($changeType == null ? 43 : ((Object)((Object)$changeType)).hashCode());
        return result;
    }

    public String toString() {
        return "InventoryChanged(changeType=" + String.valueOf((Object)this.getChangeType()) + ", itemId=" + this.getItemId() + ", amount=" + this.getAmount() + ")";
    }

    public static enum ChangeType {
        ITEM_ADDED,
        ITEM_REMOVED;

    }
}

