/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.GrandExchangeOffer
 *  net.runelite.api.Item
 *  net.runelite.api.TileObject
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.Item;
import net.runelite.api.TileObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class GrandExchange {
    private static final int F2P_SLOTS = 3;
    private static final int P2P_SLOTS = 8;
    private static final int PRICE_VARBIT = 4398;
    private static final int QUANTITY_VARBIT = 4396;
    private static final Supplier<Widget> COLLECT_BUTTON = () -> Widgets.get(465, 6, 0);
    private static final Supplier<Widget> CONFIRM_BUTTON = () -> Widgets.get(465, 29);
    private static final Supplier<Widget> OFFER_PRICE = () -> Widgets.get(465, 27);

    public static View getView() {
        Widget setupWindow = Widgets.get(WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER);
        if (Widgets.isVisible(setupWindow)) {
            String text = setupWindow.getChild(18).getText();
            if (text == null || text.isEmpty()) {
                return View.UNKNOWN;
            }
            if (text.equals("Sell offer")) {
                return View.SELLING;
            }
            if (text.equals("Buy offer")) {
                return View.BUYING;
            }
            return View.UNKNOWN;
        }
        Widget geWindow = Widgets.get(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER);
        if (Widgets.isVisible(geWindow)) {
            return View.OFFERS;
        }
        return View.CLOSED;
    }

    public static boolean isOpen() {
        return GrandExchange.getView() != View.CLOSED && GrandExchange.getView() != View.UNKNOWN;
    }

    public static boolean isSetupOpen() {
        return GrandExchange.getView() == View.BUYING || GrandExchange.getView() == View.SELLING;
    }

    public static void openBank() {
        TileObject bank = TileObjects.getFirstSurrounding(3163, 3490, 0, 5, x -> x.getName() != null && x.getName().toLowerCase().contains("exchange booth") && x.hasAction(new String[]{"Bank"}));
        if (bank != null) {
            bank.interact("Bank");
        }
    }

    public static boolean isSelling() {
        return GrandExchange.getView() == View.SELLING;
    }

    public static boolean isBuying() {
        return GrandExchange.getView() == View.BUYING;
    }

    public static int getItemId() {
        return Vars.getVarp(VarPlayer.CURRENT_GE_ITEM.getId());
    }

    public static void setItem(int id) {
        GameThread.invoke(() -> Static.getClient().runScript(new Object[]{754, id, 84}));
    }

    public static int getPrice() {
        return Vars.getBit(4398);
    }

    public static void setPrice(int price) {
        Widget enterPriceButton = Widgets.get(WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER);
        if (enterPriceButton != null && enterPriceButton.getChild(12) != null) {
            enterPriceButton.getChild(12).interact("Enter price");
            Dialog.enterAmount(price);
        }
    }

    public static int getQuantity() {
        return Vars.getBit(4396);
    }

    public static void setQuantity(int quantity) {
        Widget enterPriceButton = Widgets.get(WidgetInfo.GRAND_EXCHANGE_OFFER_CONTAINER);
        if (enterPriceButton != null && enterPriceButton.getChild(7) != null) {
            enterPriceButton.getChild(7).interact("Enter quantity");
            Dialog.enterAmount(quantity);
        }
    }

    public static int getGuidePrice() {
        Widget priceWidget = OFFER_PRICE.get();
        if (priceWidget != null) {
            return Integer.parseInt(priceWidget.getText().replaceAll("[^0-9]", ""));
        }
        return -1;
    }

    public static void open() {
        TileObject booth = TileObjects.getFirstSurrounding(3163, 3490, 0, 5, x -> x.hasAction(new String[]{"Exchange"}));
        if (booth != null) {
            booth.interact("Exchange");
        }
    }

    public static void sell(Predicate<Item> filter) {
        Item item = Inventory.getFirst(filter);
        if (item != null) {
            Static.getClient().interact(1, 57, item.getSlot(), 30605312);
        }
    }

    public static void sell(int ... ids) {
        GrandExchange.sell((Item x) -> Arrays.stream(ids).anyMatch(y -> y == x.getId() || y == x.getNotedId()));
    }

    public static void sell(String ... names) {
        GrandExchange.sell(Predicates.names(names));
    }

    public static void createBuyOffer() {
        List<Widget> geRoot = Widgets.get(465);
        if (GrandExchange.isFull()) {
            return;
        }
        if (geRoot == null) {
            return;
        }
        for (int i = 7; i < 15; ++i) {
            Widget buyButton;
            Widget box = geRoot.get(i);
            if (box == null || !Widgets.isVisible(buyButton = box.getChild(3))) continue;
            buyButton.interact(0);
            return;
        }
    }

    public static void abortOffer(int itemId) {
        List<Widget> geRoot = Widgets.get(465);
        if (GrandExchange.isEmpty()) {
            return;
        }
        if (geRoot == null) {
            return;
        }
        for (int i = 7; i < 15; ++i) {
            Widget itemIdBox;
            Widget abortBox;
            Widget box = geRoot.get(i);
            if (box == null || (abortBox = box.getChild(2)) == null || !abortBox.hasAction(new String[]{"Abort offer"}) || !abortBox.isVisible() || (itemIdBox = box.getChild(18)) == null || !itemIdBox.isVisible() || itemIdBox.getItemId() != itemId) continue;
            abortBox.interact("Abort offer");
            return;
        }
    }

    public static boolean isFull() {
        return GrandExchange.getEmptySlots() == 0;
    }

    public static boolean isEmpty() {
        return GrandExchange.getOffers().size() == 0;
    }

    public static int getEmptySlots() {
        return Game.getMembershipDays() <= 0 ? 3 - GrandExchange.getOffers().size() : 8 - GrandExchange.getOffers().size();
    }

    public static List<GrandExchangeOffer> getOffers() {
        ArrayList<GrandExchangeOffer> out = new ArrayList<GrandExchangeOffer>();
        GrandExchangeOffer[] offers = Static.getClient().getGrandExchangeOffers();
        if (offers != null) {
            for (GrandExchangeOffer offer : offers) {
                if (offer.getItemId() <= 0) continue;
                out.add(offer);
            }
        }
        return out;
    }

    public static boolean canCollect() {
        return Widgets.isVisible(COLLECT_BUTTON.get());
    }

    public static void collect() {
        GrandExchange.collect(false);
    }

    public static void collect(boolean toBank) {
        Widget collect = COLLECT_BUTTON.get();
        if (Widgets.isVisible(collect)) {
            collect.interact(toBank ? "Collect to bank" : "Collect to inventory");
        }
    }

    public static void confirm() {
        Widget confirm = CONFIRM_BUTTON.get();
        if (Widgets.isVisible(confirm)) {
            confirm.interact("Confirm");
        }
    }

    public static boolean isSearchingItem() {
        return Vars.getVarcInt(5) == 14;
    }

    public static void openItemSearch() {
        Static.getClient().interact(1, 57, 0, 30474264);
    }

    public static boolean sell(int itemId, int quantity, int price) {
        return GrandExchange.exchange(false, itemId, quantity, price, true, false);
    }

    public static boolean sell(int itemId, int quantity, int price, boolean collect, boolean toBank) {
        return GrandExchange.exchange(false, itemId, quantity, price, collect, toBank);
    }

    public static boolean buy(int itemId, int quantity, int price) {
        return GrandExchange.exchange(true, itemId, quantity, price, true, false);
    }

    public static boolean buy(int itemId, int quantity, int price, boolean collect, boolean toBank) {
        return GrandExchange.exchange(true, itemId, quantity, price, collect, toBank);
    }

    public static boolean exchange(boolean buy, int itemId, int quantity, int price) {
        return GrandExchange.exchange(buy, itemId, quantity, price, true, false);
    }

    public static boolean exchange(boolean buy, int itemId, int quantity, int price, boolean collect, boolean toBank) {
        int notedId;
        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return false;
        }
        if (collect && GrandExchange.canCollect()) {
            GrandExchange.collect(toBank);
            return false;
        }
        if (buy) {
            if (!GrandExchange.isBuying()) {
                GrandExchange.createBuyOffer();
                return false;
            }
        } else if (!GrandExchange.isSelling()) {
            GrandExchange.sell(itemId);
            return false;
        }
        int n = notedId = Static.getClient().isItemDefinitionCached(itemId) ? Static.getClient().getItemComposition(itemId).getLinkedNoteId() : GameThread.invokeLater(() -> Static.getClient().getItemComposition(itemId).getLinkedNoteId()).intValue();
        if (GrandExchange.getItemId() == -1 || GrandExchange.getItemId() != itemId && GrandExchange.getItemId() != notedId) {
            if (buy) {
                if (!GrandExchange.isSearchingItem()) {
                    GrandExchange.openItemSearch();
                }
                GrandExchange.setItem(itemId);
            } else {
                GrandExchange.sell(itemId);
            }
            return false;
        }
        if (GrandExchange.getPrice() != price) {
            GrandExchange.setPrice(price);
        }
        if (GrandExchange.getQuantity() != quantity) {
            GrandExchange.setQuantity(quantity);
        }
        Time.sleepUntil(() -> GrandExchange.getPrice() == price && GrandExchange.getQuantity() == quantity, 3000);
        if (GrandExchange.getPrice() == price && GrandExchange.getQuantity() == quantity) {
            GrandExchange.confirm();
            return true;
        }
        return false;
    }

    public static enum View {
        CLOSED,
        OFFERS,
        BUYING,
        SELLING,
        UNKNOWN;

    }
}

