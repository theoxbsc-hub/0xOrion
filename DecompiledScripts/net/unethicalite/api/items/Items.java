/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 */
package net.unethicalite.api.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.client.Static;
import net.unethicalite.client.managers.InventoryManager;

public abstract class Items {
    private final InventoryID inventoryID;
    private final Function<Item, Boolean> modification;

    protected Items(InventoryID inventoryID, Function<Item, Boolean> modification) {
        this.inventoryID = inventoryID;
        this.modification = modification;
    }

    protected List<Item> all(Predicate<Item> filter) {
        ArrayList<Item> items = new ArrayList<Item>();
        Item[] containerItems = InventoryManager.getCachedContainers().get(this.inventoryID.getId());
        if (containerItems == null) {
            return items;
        }
        this.cacheUncachedItems(containerItems);
        for (int i = 0; i < containerItems.length; ++i) {
            Item item = containerItems[i];
            if (item == null || item.getId() == -1 || "null".equals(item.getName())) continue;
            item.setSlot(i);
            if (!this.modification.apply(item).booleanValue() || !filter.test(item)) continue;
            items.add(item);
        }
        return items;
    }

    protected List<Item> all(String ... names) {
        return this.all(Predicates.names(names));
    }

    protected List<Item> all(int ... ids) {
        return this.all(Predicates.ids(ids));
    }

    protected Item first(Predicate<Item> filter) {
        return this.all(filter).stream().findFirst().orElse(null);
    }

    protected Item first(int ... ids) {
        return this.first(Predicates.ids(ids));
    }

    protected Item first(String ... names) {
        return this.first(Predicates.names(names));
    }

    protected boolean exists(Predicate<Item> filter) {
        return this.first(filter) != null;
    }

    protected boolean exists(String ... name) {
        return this.first(name) != null;
    }

    protected boolean exists(int ... id) {
        return this.first(id) != null;
    }

    protected int count(boolean stacks, Predicate<Item> filter) {
        return this.all(filter).stream().mapToInt(x -> stacks ? x.getQuantity() : 1).sum();
    }

    protected int count(boolean stacks, int ... ids) {
        return this.all(ids).stream().mapToInt(x -> stacks ? x.getQuantity() : 1).sum();
    }

    protected int count(boolean stacks, String ... names) {
        return this.all(names).stream().mapToInt(x -> stacks ? x.getQuantity() : 1).sum();
    }

    protected void cacheUncachedItems(Item[] items) {
        Client client = Static.getClient();
        List uncachedItems = Arrays.stream(items).filter(i -> !client.isItemDefinitionCached(i.getId())).collect(Collectors.toList());
        if (!uncachedItems.isEmpty()) {
            GameThread.invoke(() -> {
                for (Item uncachedItem : uncachedItems) {
                    int id = uncachedItem.getId();
                    client.cacheItem(id, client.getItemComposition(id));
                }
            });
        }
    }

    public InventoryID getInventoryID() {
        return this.inventoryID;
    }
}

