/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Tile
 *  net.runelite.api.TileItem
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.entities;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.query.entities.SceneEntityQuery;
import net.unethicalite.api.query.results.SceneEntityQueryResults;
import org.apache.commons.lang3.ArrayUtils;

public class TileItemQuery
extends SceneEntityQuery<TileItem, TileItemQuery> {
    private int[] quantities = null;
    private Tile[] tiles = null;
    private Boolean tradable = null;
    private Boolean stackable = null;
    private Boolean noted = null;
    private Boolean members = null;
    private Integer storePrice = null;
    private String[] inventoryActions = null;

    public TileItemQuery(Supplier<List<TileItem>> supplier) {
        super(supplier);
    }

    public TileItemQuery quantities(int ... quantities) {
        this.quantities = quantities;
        return this;
    }

    public TileItemQuery tiles(Tile ... tiles) {
        this.tiles = tiles;
        return this;
    }

    public TileItemQuery tradable(boolean tradable) {
        this.tradable = tradable;
        return this;
    }

    public TileItemQuery stackable(boolean stackable) {
        this.stackable = stackable;
        return this;
    }

    public TileItemQuery noted(boolean noted) {
        this.noted = noted;
        return this;
    }

    public TileItemQuery members(boolean members) {
        this.members = members;
        return this;
    }

    public TileItemQuery storePrice(int storePrice) {
        this.storePrice = storePrice;
        return this;
    }

    public TileItemQuery inventoryActions(String ... inventoryActions) {
        this.inventoryActions = inventoryActions;
        return this;
    }

    @Override
    protected SceneEntityQueryResults<TileItem> results(List<TileItem> list) {
        return new SceneEntityQueryResults<TileItem>(list);
    }

    @Override
    public boolean test(TileItem tileItem) {
        if (this.quantities != null && !ArrayUtils.contains((int[])this.quantities, (int)tileItem.getQuantity())) {
            return false;
        }
        if (this.tiles != null && !ArrayUtils.contains((Object[])this.tiles, (Object)tileItem.getTile())) {
            return false;
        }
        if (this.tradable != null && !this.tradable.equals(tileItem.isTradable())) {
            return false;
        }
        if (this.stackable != null && !this.stackable.equals(tileItem.isStackable())) {
            return false;
        }
        if (this.noted != null && !this.noted.equals(tileItem.isNoted())) {
            return false;
        }
        if (this.members != null && !this.members.equals(tileItem.isMembers())) {
            return false;
        }
        if (this.storePrice != null && !this.storePrice.equals(tileItem.getStorePrice())) {
            return false;
        }
        if (this.inventoryActions != null && Arrays.stream(this.inventoryActions).noneMatch(Predicates.texts(tileItem.getInventoryActions()))) {
            return false;
        }
        return super.test(tileItem);
    }
}

