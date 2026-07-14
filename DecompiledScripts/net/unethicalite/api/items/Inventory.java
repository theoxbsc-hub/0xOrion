/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.items;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.items.Items;
import net.unethicalite.api.query.items.ItemQuery;
import net.unethicalite.client.managers.InventoryManager;

public class Inventory
extends Items {
    private static final Inventory INVENTORY = new Inventory();

    private Inventory() {
        super(InventoryID.INVENTORY, item -> {
            item.setWidgetId(WidgetInfo.INVENTORY.getPackedId());
            return true;
        });
    }

    public static ItemQuery query() {
        return Inventory.query(Inventory::getAll);
    }

    public static ItemQuery query(Supplier<List<Item>> supplier) {
        return new ItemQuery(supplier);
    }

    public static List<Item> getAll(Predicate<Item> filter) {
        return INVENTORY.all(filter);
    }

    public static List<Item> getAll() {
        return Inventory.getAll((Item x) -> true);
    }

    public static List<Item> getAll(int ... ids) {
        return INVENTORY.all(ids);
    }

    public static List<Item> getAll(String ... names) {
        return INVENTORY.all(names);
    }

    public static Item getFirst(Predicate<Item> filter) {
        return INVENTORY.first(filter);
    }

    public static Item getFirst(int ... ids) {
        return INVENTORY.first(ids);
    }

    public static Item getFirst(String ... names) {
        return INVENTORY.first(names);
    }

    public static Item getItem(int slot) {
        Item[] container = InventoryManager.getCachedContainers().get(InventoryID.INVENTORY.getId());
        if (container == null) {
            return null;
        }
        Item item = container[slot];
        if (item == null || item.getId() == -1 || item.getName() == null || item.getName().equals("null")) {
            return null;
        }
        item.setWidgetId(WidgetInfo.INVENTORY.getPackedId());
        return item;
    }

    public static boolean contains(Predicate<Item> filter) {
        return INVENTORY.exists(filter);
    }

    public static boolean contains(int ... id) {
        return INVENTORY.exists(id);
    }

    public static boolean contains(String ... name) {
        return INVENTORY.exists(name);
    }

    public static int getCount(boolean stacks, Predicate<Item> filter) {
        return INVENTORY.count(stacks, filter);
    }

    public static int getCount(boolean stacks, int ... ids) {
        return INVENTORY.count(stacks, ids);
    }

    public static int getCount(boolean stacks, String ... names) {
        return INVENTORY.count(stacks, names);
    }

    public static int getCount(Predicate<Item> filter) {
        return INVENTORY.count(false, filter);
    }

    public static int getCount(int ... ids) {
        return INVENTORY.count(false, ids);
    }

    public static int getCount(String ... names) {
        return INVENTORY.count(false, names);
    }

    public static boolean isFull() {
        return Inventory.getFreeSlots() == 0;
    }

    public static boolean isEmpty() {
        return Inventory.getFreeSlots() == 28;
    }

    public static int getFreeSlots() {
        return 28 - Inventory.getAll().size();
    }
}

