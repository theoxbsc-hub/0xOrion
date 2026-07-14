/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.runelite.client.plugins.svc.thieving;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.thieving.ThievingStall;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.WidgetPackets;

public final class StallThiefEngine {
    private static final String TRADER_ACTION = "Open-shop";
    private static final int SEARCH_RADIUS = 13;
    private static final int ARRIVE_DIST = 5;
    private static final int CLOSE_SHOP_X = 482;
    private static final int CLOSE_SHOP_Y = 37;
    private static final int SELL_OP_INDEX = 4;
    private static final int SELL_CONFIRM_TIMEOUT_MS = 2000;
    private static final int CLOSE_SHOP_TIMEOUT_MS = 4000;
    private static final int SHOP_OPEN_WAIT_MS = 5000;
    private static final int MIN_CLICK_DELAY = 600;
    private static final int MAX_CLICK_DELAY = 1200;
    private static final int RANDOMNESS_PERCENT = 15;
    private static final int SELL_DELAY = 500;
    private final Client client;
    private final HumanInput input;
    private final Random random = new Random();
    private String status = "Starting";
    private int stealCount = 0;
    private int sellCount = 0;
    private SellPhase sellPhase = SellPhase.OPEN_SHOP;
    private final Set<Integer> unsellableItemIds = new LinkedHashSet<Integer>();
    private final Set<Integer> neverSellIds = new LinkedHashSet<Integer>();
    private final Set<String> neverSellNames = new LinkedHashSet<String>(Arrays.asList("coins", "coin pouch"));
    private final List<Integer> pendingSellIds = new ArrayList<Integer>();
    private long nextSellAtMs = 0L;
    private long tradingStartMs = 0L;
    private int currentSellItemId = -1;
    private int currentSellQtyBefore = 0;
    private long sellSentAtMs = 0L;
    private boolean sellRejected = false;
    private long closeShopAttemptMs = 0L;
    private boolean blacklistClearedThisSession = false;

    public StallThiefEngine(Client client) {
        this(client, null);
    }

    public StallThiefEngine(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public void setNeverSellIds(int ... nArray) {
        this.neverSellIds.clear();
        if (nArray != null) {
            for (int n : nArray) {
                this.neverSellIds.add(n);
            }
        }
    }

    public void setNeverSellNames(String ... stringArray) {
        this.neverSellNames.clear();
        if (stringArray != null) {
            for (String string : stringArray) {
                if (string == null || string.isEmpty()) continue;
                this.neverSellNames.add(string.toLowerCase());
            }
        }
    }

    private boolean skipSell(int n) {
        return this.unsellableItemIds.contains(n) || this.neverSellIds.contains(n);
    }

    private boolean isNeverSell(Item item) {
        if (item == null) {
            return false;
        }
        if (this.neverSellIds.contains(item.getId())) {
            return true;
        }
        String string = item.getName();
        if (string != null) {
            String string2 = string.toLowerCase();
            for (String string3 : this.neverSellNames) {
                if (!string2.contains(string3)) continue;
                return true;
            }
        }
        return false;
    }

    public void reset() {
        this.status = "Starting";
        this.stealCount = 0;
        this.sellCount = 0;
        this.sellPhase = SellPhase.OPEN_SHOP;
        this.unsellableItemIds.clear();
        this.pendingSellIds.clear();
        this.nextSellAtMs = 0L;
        this.tradingStartMs = 0L;
        this.currentSellItemId = -1;
        this.currentSellQtyBefore = 0;
        this.sellSentAtMs = 0L;
        this.sellRejected = false;
        this.closeShopAttemptMs = 0L;
        this.blacklistClearedThisSession = false;
    }

    public int stealStep(Player player, int n) {
        WorldPoint worldPoint = player.getWorldLocation();
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == n && tileObject.getWorldLocation().distanceTo(worldPoint) <= 13);
        if (tileObject2 != null) {
            this.status = "Stealing from stall";
            if (this.input != null) {
                this.input.interactObjectVia(tileObject2, "Steal-from");
            } else {
                tileObject2.interact("Steal-from");
            }
            ++this.stealCount;
            return this.randomDelay();
        }
        TileObject tileObject3 = TileObjects.getNearest(n);
        if (tileObject3 != null) {
            WorldPoint worldPoint2 = tileObject3.getWorldLocation();
            if (worldPoint2 != null && worldPoint.distanceTo(worldPoint2) > 5) {
                this.status = "Walking to stall";
                if (this.input != null) {
                    this.input.walkToward(player.getWorldLocation(), worldPoint2);
                } else {
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(worldPoint2);
                }
                return 1200;
            }
            this.status = "Stealing from stall";
            if (this.input != null) {
                this.input.interactObjectVia(tileObject3, "Steal-from");
            } else {
                tileObject3.interact("Steal-from");
            }
            ++this.stealCount;
            return this.randomDelay();
        }
        this.status = "Stall not found...";
        return 600;
    }

    public int sellAllStep(Player player, int n) {
        Widget widget;
        boolean bl;
        long l = System.currentTimeMillis();
        if (this.sellPhase == SellPhase.DONE) {
            this.sellPhase = SellPhase.OPEN_SHOP;
            this.blacklistClearedThisSession = false;
        }
        boolean bl2 = bl = (widget = this.client.getWidget(300, 0)) != null && !widget.isHidden();
        if (bl) {
            return this.loopSelling(l);
        }
        if (this.sellPhase == SellPhase.AWAIT_SHOP) {
            if (l - this.tradingStartMs < 5000L) {
                this.status = "Waiting for shop to open...";
                return 400;
            }
            NPC nPC = this.findTrader(player, n);
            if (nPC != null) {
                this.status = "Re-opening shop";
                if (this.input != null) {
                    this.input.npcActionVia(nPC, TRADER_ACTION);
                } else {
                    MousePackets.queueClickPacket();
                    nPC.interact(TRADER_ACTION);
                }
                this.tradingStartMs = l;
                return 3000;
            }
            this.sellPhase = SellPhase.OPEN_SHOP;
            return 600;
        }
        if (this.sellPhase == SellPhase.SELLING) {
            return this.finishSelling();
        }
        if (!this.hasSellableItems()) {
            if (!Inventory.isEmpty() && !this.blacklistClearedThisSession) {
                this.unsellableItemIds.clear();
                this.blacklistClearedThisSession = true;
                this.status = "All items blacklisted \u2014 clearing blacklist, retrying";
                return 5000;
            }
            this.status = "Nothing left to sell";
            this.sellPhase = SellPhase.DONE;
            return 600;
        }
        NPC nPC = this.findTrader(player, n);
        if (nPC != null) {
            this.status = "Opening shop";
            if (this.input != null) {
                this.input.npcActionVia(nPC, TRADER_ACTION);
            } else {
                MousePackets.queueClickPacket();
                nPC.interact(TRADER_ACTION);
            }
            this.sellPhase = SellPhase.AWAIT_SHOP;
            this.tradingStartMs = l;
            return this.randomDelay();
        }
        this.status = "Searching for trader NPC...";
        return 800;
    }

    public boolean isSellingDone() {
        return this.sellPhase == SellPhase.DONE;
    }

    public void clearSellBlacklist() {
        this.unsellableItemIds.clear();
        this.blacklistClearedThisSession = false;
    }

    private int loopSelling(long l) {
        Item item2;
        if (this.sellPhase != SellPhase.SELLING) {
            this.pendingSellIds.clear();
            ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
            if (itemContainer != null) {
                item2 = new LinkedHashSet();
                for (Item item3 : itemContainer.getItems()) {
                    if (item3.getId() == -1 || this.unsellableItemIds.contains(item3.getId()) || this.isNeverSell(item3)) continue;
                    item2.add(item3.getId());
                }
                this.pendingSellIds.addAll((Collection<Integer>)item2);
            }
            this.sellPhase = SellPhase.SELLING;
            this.currentSellItemId = -1;
            this.sellRejected = false;
            this.closeShopAttemptMs = 0L;
        }
        if (this.currentSellItemId != -1) {
            boolean bl;
            int n = Inventory.getAll(item -> item.getId() == this.currentSellItemId).size();
            boolean bl2 = n < this.currentSellQtyBefore;
            boolean bl3 = bl = l - this.sellSentAtMs > 2000L;
            if (bl2 || bl) {
                this.pendingSellIds.remove((Object)this.currentSellItemId);
                this.currentSellItemId = -1;
                this.sellRejected = false;
                return 100;
            }
            if (this.sellRejected) {
                this.unsellableItemIds.add(this.currentSellItemId);
                this.pendingSellIds.remove((Object)this.currentSellItemId);
                this.status = "Blacklisted " + this.itemName(this.currentSellItemId);
                this.currentSellItemId = -1;
                this.sellRejected = false;
                return 100;
            }
            this.status = "Waiting sell response...";
            return 100;
        }
        if (l < this.nextSellAtMs) {
            this.status = "Selling (waiting)";
            return 100;
        }
        if (this.pendingSellIds.isEmpty()) {
            if (this.closeShopAttemptMs == 0L) {
                this.closeShopAttemptMs = l;
            }
            if (l - this.closeShopAttemptMs > 4000L) {
                this.status = "Shop close timeout \u2014 finishing";
                return this.finishSelling();
            }
            this.status = "Closing shop";
            if (this.input != null) {
                this.input.clickWidgetOrFixed(300, 1, 482, 37);
            } else {
                Mouse.click(482, 37, true);
            }
            return 400;
        }
        int n = this.pendingSellIds.get(0);
        if (this.skipSell(n)) {
            this.pendingSellIds.remove(0);
            return 100;
        }
        item2 = Inventory.getFirst(item -> item.getId() == n);
        if (item2 == null || this.isNeverSell(item2)) {
            this.pendingSellIds.remove(0);
            return 100;
        }
        this.status = "Selling " + this.itemName(n);
        this.currentSellItemId = n;
        this.currentSellQtyBefore = Inventory.getAll(item -> item.getId() == n).size();
        this.sellSentAtMs = l;
        this.sellRejected = false;
        if (this.input != null) {
            this.input.widgetItemAction(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER, item2, 4);
        } else {
            MousePackets.queueClickPacket();
            WidgetPackets.widgetItemAction(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER, item2, 4);
        }
        ++this.sellCount;
        this.nextSellAtMs = l + (long)this.sellDelay();
        return 100;
    }

    private int finishSelling() {
        this.pendingSellIds.clear();
        this.currentSellItemId = -1;
        this.sellRejected = false;
        this.closeShopAttemptMs = 0L;
        this.sellPhase = SellPhase.DONE;
        this.status = "Sold \u2014 shop closed";
        return 600;
    }

    public void onChatMessage(String string) {
        if (string == null) {
            return;
        }
        if (string.contains("you can't sell this item")) {
            this.sellRejected = true;
        }
    }

    public boolean inventoryFull() {
        return Inventory.getFreeSlots() == 0;
    }

    public String getStatus() {
        return this.status;
    }

    public int getStealCount() {
        return this.stealCount;
    }

    public int getSellCount() {
        return this.sellCount;
    }

    public int getBlacklistedCount() {
        return this.unsellableItemIds.size();
    }

    private NPC findTrader(Player player, int n) {
        WorldPoint worldPoint = player.getWorldLocation();
        return NPCs.getNearest(nPC -> nPC != null && nPC.getId() == n && nPC.getWorldLocation().distanceTo(worldPoint) <= 13);
    }

    public boolean hasSellableItems() {
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return false;
        }
        for (Item item : itemContainer.getItems()) {
            if (item.getId() == -1 || this.unsellableItemIds.contains(item.getId()) || this.isNeverSell(item)) continue;
            return true;
        }
        return false;
    }

    private int randomDelay() {
        int n = 600;
        int n2 = Math.max(n + 1, 1200);
        if (this.input != null) {
            double d = (this.random.nextDouble() + this.random.nextDouble()) / 2.0;
            int n3 = (int)Math.round(((double)n + d * (double)(n2 - n)) * this.input.profile().tempo());
            if (this.random.nextDouble() < 0.05) {
                n3 += 400 + this.random.nextInt(1600);
            }
            return Math.max(200, n3);
        }
        int n4 = n + this.random.nextInt(n2 - n);
        int n5 = (int)((double)(n4 * 15) / 100.0);
        if (n5 > 0) {
            n4 += this.random.nextInt(n5 * 2 + 1) - n5;
        }
        return Math.max(200, n4);
    }

    private int sellDelay() {
        if (this.input != null) {
            double d = (this.random.nextDouble() + this.random.nextDouble()) / 2.0;
            int n = (int)Math.round(500.0 * (0.7 + 0.6 * d) * this.input.profile().tempo());
            if (this.random.nextDouble() < 0.04) {
                n += 300 + this.random.nextInt(900);
            }
            return Math.max(150, n);
        }
        int n = 500;
        int n2 = (int)((double)(n * 15) / 100.0);
        if (n2 > 0) {
            n += this.random.nextInt(n2 * 2 + 1) - n2;
        }
        return Math.max(100, n);
    }

    private String itemName(int n) {
        ItemComposition itemComposition = this.client.getItemDefinition(n);
        return itemComposition != null ? itemComposition.getName() : String.valueOf(n);
    }

    public static StallTarget bestStallForLevel(int n) {
        ThievingStall thievingStall = null;
        for (ThievingStall thievingStall2 : ThievingStall.values()) {
            if (n < thievingStall2.requiredLevel) continue;
            thievingStall = thievingStall2;
        }
        if (thievingStall == null) {
            thievingStall = ThievingStall.values()[0];
        }
        return new StallTarget(thievingStall);
    }

    private static enum SellPhase {
        OPEN_SHOP,
        AWAIT_SHOP,
        SELLING,
        DONE;

    }

    public static final class StallTarget {
        public final int requiredLevel;
        public final int objectId;
        public final int traderNpcId;
        public final int x;
        public final int y;
        public final int plane;
        public final String label;

        private StallTarget(ThievingStall thievingStall) {
            this.requiredLevel = thievingStall.requiredLevel;
            this.objectId = thievingStall.objectId;
            this.traderNpcId = thievingStall.traderNpcId;
            this.x = thievingStall.x;
            this.y = thievingStall.y;
            this.plane = thievingStall.plane;
            this.label = thievingStall.label;
        }

        public WorldPoint coords() {
            return new WorldPoint(this.x, this.y, this.plane);
        }
    }
}

