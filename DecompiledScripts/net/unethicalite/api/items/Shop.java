/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.widgets.Widget
 *  org.apache.commons.lang3.StringUtils
 */
package net.unethicalite.api.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.widgets.Widgets;
import org.apache.commons.lang3.StringUtils;

public class Shop {
    private static final Supplier<Widget> SHOP = () -> Widgets.get(300, 0);
    private static final Supplier<Widget> SHOP_ITEMS = () -> Widgets.get(300, 16);
    private static final Supplier<Widget> INVENTORY = () -> Widgets.get(301, 0);

    public static boolean isOpen() {
        return Widgets.isVisible(SHOP.get());
    }

    public static int getStock(int itemId) {
        Widget items = SHOP_ITEMS.get();
        if (!Widgets.isVisible(items)) {
            return 0;
        }
        Widget[] children = items.getChildren();
        if (children == null) {
            return 0;
        }
        return Arrays.stream(children).filter(child -> child.getItemId() == itemId).mapToInt(Widget::getItemQuantity).sum();
    }

    public static void buyOne(int itemId) {
        Shop.buy(itemId, 1);
    }

    public static void buyOne(String itemName) {
        Shop.buy(itemName, 1);
    }

    public static void buyFive(int itemId) {
        Shop.buy(itemId, 5);
    }

    public static void buyFive(String itemName) {
        Shop.buy(itemName, 5);
    }

    public static void buyTen(int itemId) {
        Shop.buy(itemId, 10);
    }

    public static void buyTen(String itemName) {
        Shop.buy(itemName, 10);
    }

    public static void buyFifty(int itemId) {
        Shop.buy(itemId, 50);
    }

    public static void buyFifty(String itemName) {
        Shop.buy(itemName, 50);
    }

    public static void sellOne(int itemId) {
        Shop.sell(itemId, 1);
    }

    public static void sellFive(int itemId) {
        Shop.sell(itemId, 5);
    }

    public static void sellTen(int itemId) {
        Shop.sell(itemId, 10);
    }

    public static void sellFifty(int itemId) {
        Shop.sell(itemId, 50);
    }

    public static List<Integer> getItems() {
        ArrayList<Integer> out = new ArrayList<Integer>();
        Widget container = SHOP_ITEMS.get();
        if (container == null) {
            return out;
        }
        Widget[] items = container.getChildren();
        if (items == null) {
            return out;
        }
        for (Widget item : items) {
            if (item.getItemId() == -1) continue;
            out.add(item.getItemId());
        }
        return out;
    }

    private static void buy(int itemId, int amount) {
        Shop.exchange(itemId, amount, SHOP_ITEMS.get());
    }

    private static void buy(String itemName, int amount) {
        Shop.exchange(itemName, amount, SHOP_ITEMS.get());
    }

    private static void sell(int itemId, int amount) {
        Shop.exchange(itemId, amount, INVENTORY.get());
    }

    private static void exchange(int itemId, int amount, Widget container) {
        if (container == null) {
            return;
        }
        Widget[] items = container.getChildren();
        if (items == null) {
            return;
        }
        for (Widget item : items) {
            if (item.getItemId() != itemId) continue;
            String action = Arrays.stream(item.getActions()).filter(x -> x != null && x.contains(String.valueOf(amount))).findFirst().orElse(null);
            if (action == null) {
                return;
            }
            item.interact(action);
            return;
        }
    }

    private static void exchange(String itemName, int amount, Widget container) {
        if (container == null) {
            return;
        }
        Widget[] items = container.getChildren();
        if (items == null) {
            return;
        }
        for (Widget item : items) {
            String nestedName = StringUtils.substringBetween((String)item.getName(), (String)">", (String)"<");
            if (nestedName == null || !nestedName.equals(itemName)) continue;
            String action = Arrays.stream(item.getActions()).filter(x -> x != null && x.contains(String.valueOf(amount))).findFirst().orElse(null);
            if (action == null) {
                return;
            }
            item.interact(action);
            return;
        }
    }
}

