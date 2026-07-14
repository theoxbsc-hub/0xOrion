/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public final class ItemObtained {
    private final int itemId;
    private final int amount;

    public ItemObtained(int itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
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
        if (!(o instanceof ItemObtained)) {
            return false;
        }
        ItemObtained other = (ItemObtained)o;
        if (this.getItemId() != other.getItemId()) {
            return false;
        }
        return this.getAmount() == other.getAmount();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getItemId();
        result = result * 59 + this.getAmount();
        return result;
    }

    public String toString() {
        return "ItemObtained(itemId=" + this.getItemId() + ", amount=" + this.getAmount() + ")";
    }
}

