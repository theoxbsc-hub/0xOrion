/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 */
package net.runelite.client.plugins.ox.mining;

import java.util.List;
import net.runelite.api.Item;
import net.runelite.client.plugins.ox.mining.BarType;
import net.runelite.client.plugins.ox.mining.MiningOre;
import net.unethicalite.api.items.Inventory;

final class BarGoal {
    private BarType bar = BarType.NONE;
    private int targetBars;
    private long[] mined = new long[0];
    private long[] lastSeen = new long[0];
    private MiningOre currentPick;
    private int lastTripCount = -1;

    BarGoal() {
    }

    void sync(BarType barType, int n) {
        if (barType == null) {
            barType = BarType.NONE;
        }
        if (barType != this.bar) {
            this.bar = barType;
            this.mined = new long[barType.ingredients().length];
            this.lastSeen = new long[barType.ingredients().length];
            this.currentPick = null;
            this.lastTripCount = -1;
        }
        this.targetBars = Math.max(0, n);
    }

    boolean isActive() {
        return !this.bar.isNone();
    }

    void trackInventory() {
        if (!this.isActive()) {
            return;
        }
        BarType.Ingredient[] ingredientArray = this.bar.ingredients();
        for (int i = 0; i < ingredientArray.length; ++i) {
            long l = BarGoal.countInInventory(ingredientArray[i].ore);
            long l2 = l - this.lastSeen[i];
            if (l2 > 0L) {
                int n = i;
                this.mined[n] = this.mined[n] + l2;
            }
            this.lastSeen[i] = l;
        }
    }

    MiningOre decide(int n, int n2, boolean bl) {
        boolean bl2;
        if (!this.isActive()) {
            return null;
        }
        boolean bl3 = this.isComplete();
        if (bl3 && bl) {
            this.currentPick = null;
            return null;
        }
        boolean bl4 = this.targetBars <= 0 || bl3;
        boolean bl5 = bl2 = this.currentPick == null || n != this.lastTripCount || !bl4 && this.quotaMet(this.indexOf(this.currentPick));
        if (bl2) {
            this.lastTripCount = n;
            this.currentPick = this.choose(n2, bl4);
        }
        return this.currentPick;
    }

    boolean isComplete() {
        if (!this.isActive() || this.targetBars <= 0) {
            return false;
        }
        for (int i = 0; i < this.mined.length; ++i) {
            if (this.quotaMet(i)) continue;
            return false;
        }
        return true;
    }

    private MiningOre choose(int n, boolean bl) {
        BarType.Ingredient[] ingredientArray = this.bar.ingredients();
        int n2 = -1;
        double d = 0.0;
        boolean bl2 = false;
        for (int i = 0; i < ingredientArray.length; ++i) {
            boolean bl3;
            if (!bl && this.quotaMet(i)) continue;
            boolean bl4 = ingredientArray[i].ore.getTier() <= n;
            double d2 = (double)this.mined[i] / (double)ingredientArray[i].perBar;
            boolean bl5 = bl3 = n2 < 0 || bl4 && !bl2 || bl4 == bl2 && d2 < d;
            if (!bl3) continue;
            n2 = i;
            d = d2;
            bl2 = bl4;
        }
        return n2 >= 0 ? ingredientArray[n2].ore : null;
    }

    private boolean quotaMet(int n) {
        return this.targetBars > 0 && n >= 0 && this.mined[n] >= this.requiredOf(n);
    }

    private int indexOf(MiningOre miningOre) {
        BarType.Ingredient[] ingredientArray = this.bar.ingredients();
        for (int i = 0; i < ingredientArray.length; ++i) {
            if (ingredientArray[i].ore != miningOre) continue;
            return i;
        }
        return -1;
    }

    private static long countInInventory(MiningOre miningOre) {
        List<Item> list = Inventory.getAll(item -> item != null && BarGoal.matchesOre(item.getName(), miningOre));
        long l = 0L;
        for (Item item2 : list) {
            l += (long)Math.max(1, item2.getQuantity());
        }
        return l;
    }

    private static boolean matchesOre(String string, MiningOre miningOre) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : miningOre.getItemNames()) {
            if (!string2.contains(string3.toLowerCase())) continue;
            return true;
        }
        return false;
    }

    BarType bar() {
        return this.bar;
    }

    int target() {
        return this.targetBars;
    }

    MiningOre currentPick() {
        return this.currentPick;
    }

    int ingredientCount() {
        return this.bar.ingredients().length;
    }

    MiningOre ingredientOre(int n) {
        return this.bar.ingredients()[n].ore;
    }

    long minedOf(int n) {
        return n >= 0 && n < this.mined.length ? this.mined[n] : 0L;
    }

    long requiredOf(int n) {
        return (long)this.targetBars * (long)this.bar.ingredients()[n].perBar;
    }

    long barsGathered() {
        if (!this.isActive()) {
            return 0L;
        }
        BarType.Ingredient[] ingredientArray = this.bar.ingredients();
        long l = Long.MAX_VALUE;
        for (int i = 0; i < ingredientArray.length; ++i) {
            l = Math.min(l, this.mined[i] / (long)ingredientArray[i].perBar);
        }
        return l == Long.MAX_VALUE ? 0L : l;
    }
}

