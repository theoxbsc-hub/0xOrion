/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.items;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.Item;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.query.Query;
import net.unethicalite.api.query.results.ItemQueryResults;
import org.apache.commons.lang3.ArrayUtils;

public class ItemQuery
extends Query<Item, ItemQuery, ItemQueryResults> {
    private int[] ids = null;
    private int[] notedIds = null;
    private int[] slots = null;
    private String[] names = null;
    private String[] actions = null;
    private Boolean tradable = null;
    private Boolean stackable = null;
    private Boolean members = null;
    private Boolean noted = null;
    private Integer storePrice = null;

    public ItemQuery(Supplier<List<Item>> supplier) {
        super(supplier);
    }

    public ItemQuery ids(int ... ids) {
        this.ids = ids;
        return this;
    }

    public ItemQuery notedIds(int ... notedIds) {
        this.notedIds = notedIds;
        return this;
    }

    public ItemQuery slots(int ... slots) {
        this.slots = slots;
        return this;
    }

    public ItemQuery names(String ... names) {
        this.names = names;
        return this;
    }

    public ItemQuery actions(String ... actions) {
        this.actions = actions;
        return this;
    }

    public ItemQuery tradable(boolean tradable) {
        this.tradable = tradable;
        return this;
    }

    public ItemQuery stackable(boolean stackable) {
        this.stackable = stackable;
        return this;
    }

    public ItemQuery members(boolean members) {
        this.members = members;
        return this;
    }

    public ItemQuery noted(boolean noted) {
        this.noted = noted;
        return this;
    }

    public ItemQuery storePrice(int storePrice) {
        this.storePrice = storePrice;
        return this;
    }

    @Override
    protected ItemQueryResults results(List<Item> list) {
        return new ItemQueryResults(list);
    }

    @Override
    public boolean test(Item item) {
        if (this.ids != null && !ArrayUtils.contains((int[])this.ids, (int)item.getId())) {
            return false;
        }
        if (this.notedIds != null && !ArrayUtils.contains((int[])this.notedIds, (int)item.getId())) {
            return false;
        }
        if (this.slots != null && !ArrayUtils.contains((int[])this.slots, (int)item.getSlot())) {
            return false;
        }
        if (this.names != null && !ArrayUtils.contains((Object[])this.names, (Object)item.getName())) {
            return false;
        }
        if (this.actions != null && Arrays.stream(this.actions).noneMatch(Predicates.texts(item.getActions()))) {
            return false;
        }
        if (this.tradable != null && this.tradable.booleanValue() != item.isTradable()) {
            return false;
        }
        if (this.stackable != null && this.stackable.booleanValue() != item.isStackable()) {
            return false;
        }
        if (this.members != null && this.members.booleanValue() != item.isMembers()) {
            return false;
        }
        if (this.noted != null && this.noted.booleanValue() != item.isNoted()) {
            return false;
        }
        if (this.storePrice != null && this.storePrice.intValue() != item.getStorePrice()) {
            return false;
        }
        return super.test(item);
    }
}

