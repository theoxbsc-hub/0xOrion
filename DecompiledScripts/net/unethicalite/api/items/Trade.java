/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class Trade {
    private static final Supplier<Widget> OUR_ITEMS = () -> Widgets.get(335, 25);
    private static final Supplier<Widget> THEIR_ITEMS = () -> Widgets.get(335, 28);
    private static final Supplier<Widget> INVENTORY = () -> Widgets.get(336, 0);
    private static final Supplier<Widget> ACCEPT_1 = () -> Widgets.get(335, 10);
    private static final Supplier<Widget> ACCEPT_2 = () -> Widgets.get(WidgetInfo.SECOND_TRADING_WITH_ACCEPT_BUTTON);
    private static final Supplier<Widget> DECLINE_1 = () -> Widgets.get(335, 13);
    private static final Supplier<Widget> DECLINE_2 = () -> Widgets.get(334, 14);
    private static final Supplier<Widget> ACCEPT_STATUS_1 = () -> Widgets.get(335, 30);
    private static final Supplier<Widget> ACCEPT_STATUS_2 = () -> Widgets.get(334, 4);

    public static boolean isOpen() {
        return Trade.isFirstScreenOpen() || Trade.isSecondScreenOpen();
    }

    public static boolean isSecondScreenOpen() {
        return Widgets.isVisible(ACCEPT_2.get());
    }

    public static boolean isFirstScreenOpen() {
        return Widgets.isVisible(ACCEPT_1.get());
    }

    public static void accept() {
        Trade.acceptFirstScreen();
        Trade.acceptSecondScreen();
    }

    public static void acceptFirstScreen() {
        Widget button = ACCEPT_1.get();
        if (Widgets.isVisible(button)) {
            button.interact("Accept");
        }
    }

    public static void acceptSecondScreen() {
        Widget button = ACCEPT_2.get();
        if (Widgets.isVisible(button)) {
            button.interact("Accept");
        }
    }

    public static void decline() {
        Trade.declineFirstScreen();
        Trade.declineSecondScreen();
    }

    public static void declineFirstScreen() {
        Widget button = DECLINE_1.get();
        if (Widgets.isVisible(button)) {
            button.interact("Decline");
        }
    }

    public static void declineSecondScreen() {
        Widget button = DECLINE_2.get();
        if (Widgets.isVisible(button)) {
            button.interact("Decline");
        }
    }

    public static boolean hasAccepted(boolean them) {
        return Trade.hasAcceptedFirstScreen(them) || Trade.hasAcceptedSecondScreen(them);
    }

    public static boolean hasAcceptedFirstScreen(boolean them) {
        Widget widget = ACCEPT_STATUS_1.get();
        return widget != null && widget.getText().equals(them ? "Other player has accepted." : "Waiting for other player...");
    }

    public static boolean hasAcceptedSecondScreen(boolean them) {
        Widget widget = ACCEPT_STATUS_2.get();
        return widget != null && widget.getText().equals(them ? "Other player has accepted." : "Waiting for other player...");
    }

    public static void offer(Predicate<Item> filter, int quantity) {
        Item item = Trade.getInventory(filter).stream().findFirst().orElse(null);
        if (item == null) {
            return;
        }
        switch (quantity) {
            case 1: {
                item.interact("Offer");
                break;
            }
            case 5: {
                item.interact("Offer-5");
                break;
            }
            case 10: {
                item.interact("Offer-10");
                break;
            }
            default: {
                if (quantity > Inventory.getCount(true, item.getId())) {
                    item.interact("Offer-All");
                    break;
                }
                item.interact("Offer-X");
                Dialog.enterAmount(quantity);
            }
        }
    }

    public static void offer(int id, int quantity) {
        Trade.offer((Item x) -> x.getId() == id, quantity);
    }

    public static void offer(String name, int quantity) {
        Trade.offer((Item x) -> x.getName() != null && x.getName().equals(name), quantity);
    }

    public static List<Item> getAll(boolean theirs, Predicate<Item> filter) {
        ArrayList<Item> items = new ArrayList<Item>();
        ItemContainer container = Static.getClient().getItemContainer(theirs ? InventoryID.TRADEOTHER : InventoryID.TRADE);
        if (container == null) {
            return items;
        }
        Item[] containerItems = container.getItems();
        int containerItemsLength = containerItems.length;
        for (int i = 0; i < containerItemsLength; ++i) {
            Item item = containerItems[i];
            if (item.getId() == -1 || item.getName() == null || item.getName().equals("null")) continue;
            Widget containerWidget = theirs ? THEIR_ITEMS.get() : OUR_ITEMS.get();
            item.setWidgetId(item.calculateWidgetId(containerWidget));
            item.setSlot(i);
            if (!filter.test(item)) continue;
            items.add(item);
        }
        return items;
    }

    public static List<Item> getInventory(Predicate<Item> filter) {
        ArrayList<Item> items = new ArrayList<Item>();
        ItemContainer container = Static.getClient().getItemContainer(InventoryID.INVENTORY);
        if (container == null) {
            return items;
        }
        Item[] containerItems = container.getItems();
        int containerItemsLength = containerItems.length;
        for (int i = 0; i < containerItemsLength; ++i) {
            Item item = containerItems[i];
            if (item.getId() == -1 || item.getName() == null || item.getName().equals("null")) continue;
            item.setWidgetId(item.calculateWidgetId(INVENTORY.get()));
            item.setSlot(i);
            if (!filter.test(item)) continue;
            items.add(item);
        }
        return items;
    }

    public static List<Item> getAll(boolean theirs) {
        return Trade.getAll(theirs, (Item x) -> true);
    }

    public static List<Item> getAll(boolean theirs, int ... ids) {
        return Trade.getAll(theirs, Predicates.ids(ids));
    }

    public static List<Item> getAll(boolean theirs, String ... names) {
        return Trade.getAll(theirs, Predicates.names(names));
    }

    public static Item getFirst(boolean theirs, Predicate<Item> filter) {
        return Trade.getAll(theirs, filter).stream().findFirst().orElse(null);
    }

    public static Item getFirst(boolean theirs, int ... ids) {
        return Trade.getFirst(theirs, Predicates.ids(ids));
    }

    public static Item getFirst(boolean theirs, String ... names) {
        return Trade.getFirst(theirs, Predicates.names(names));
    }

    public static boolean contains(boolean theirs, Predicate<Item> filter) {
        return Trade.getFirst(theirs, filter) != null;
    }

    public static boolean contains(boolean theirs, int ... ids) {
        return Trade.contains(theirs, Predicates.ids(ids));
    }

    public static boolean contains(boolean theirs, String ... names) {
        return Trade.contains(theirs, Predicates.names(names));
    }

    public static String getTradingPlayer() {
        return Vars.getVarcStr(357);
    }
}

