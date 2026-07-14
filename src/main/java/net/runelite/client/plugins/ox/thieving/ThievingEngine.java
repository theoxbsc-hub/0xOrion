package net.runelite.client.plugins.ox.thieving;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.WidgetPackets;

/**
 * Stall thieving engine - stolen/sold from x64 ThievingBot, cleaned for 0x.
 */
final class ThievingEngine {
    private static final String TRADER_ACTION = "Open-shop";
    private static final String MULE_TRADE_ACTION = "Trade with";
    private static final int SEARCH_RADIUS = 13;
    private static final int ARRIVE_DIST = 5;
    private static final int CLOSE_SHOP_X = 482;
    private static final int CLOSE_SHOP_Y = 37;
    private static final int SELL_OP_INDEX = 4;
    private static final int SELL_CONFIRM_TIMEOUT_MS = 2000;
    private static final int CLOSE_SHOP_TIMEOUT_MS = 4000;
    private static final int LOGOUT_WIDGET_ID = 0xB6000B;
    private static final int WALK_TIMEOUT_MS = 60_000;
    private static final int PROGRESS_TRADE_TIMEOUT_MS = 15_000;
    private static final int COINS_ID = 995;
    private static final int MULE_PHASE_TIMEOUT_MS = 8_000;
    private static final int MULE_WALK_TIMEOUT_MS = 45_000;

    private final Client client;
    private final Random random = new Random();

    private ThievingState state = ThievingState.STEALING;
    private String status = "Starting";
    private int stealCount;
    private int sellCount;
    private long lastBreakTimeMs;
    private long sleepUntilMs;
    private long lastStallSeenMs;
    private final Set<Integer> unsellableItemIds = new HashSet<>();
    private final List<Integer> pendingSellIds = new ArrayList<>();
    private final List<Integer> pendingOfferIds = new ArrayList<>();
    private long nextSellAtMs;
    private long tradingStartMs;
    private int currentSellItemId = -1;
    private int currentSellQtyBefore;
    private long sellSentAtMs;
    private boolean sellRejected;
    private long closeShopAttemptMs;

    private final List<ThievingStall> stalls = new ArrayList<>();
    private int currentStallIndex;
    private boolean startIndexResolved;
    private boolean advanceAfterSell;
    private int targetStallIndex = -1;
    private long progressTradeStartMs;
    private long walkStartMs;
    private WorldPoint lastWalkPos;
    private long lastWalkPosCheckMs;

    private MulePhase mulePhase = MulePhase.FIND_MULE;
    private WorldPoint returnLocation;
    private long lastMuleTimeMs;
    private long mulePhaseStartMs;
    private long muleWalkStartMs;
    private WorldPoint lastMuleWalkPos;
    private long lastMuleWalkPosCheckMs;

    ThievingEngine(Client client) {
        this.client = client;
    }

    void reset(ThievingConfig config, long nowMs) {
        buildEnabledStalls(config);
        this.currentStallIndex = 0;
        this.startIndexResolved = false;
        this.advanceAfterSell = false;
        this.targetStallIndex = -1;
        this.progressTradeStartMs = 0L;
        this.walkStartMs = 0L;
        this.lastWalkPos = null;
        this.lastWalkPosCheckMs = 0L;
        this.state = ThievingState.STEALING;
        this.status = this.stalls.isEmpty()
            ? "No stalls enabled - enable one in Progression"
            : "Starting";
        this.stealCount = 0;
        this.sellCount = 0;
        this.lastBreakTimeMs = nowMs;
        this.lastStallSeenMs = nowMs;
        this.sleepUntilMs = 0L;
        this.unsellableItemIds.clear();
        this.pendingSellIds.clear();
        this.pendingOfferIds.clear();
        this.nextSellAtMs = 0L;
        this.tradingStartMs = 0L;
        this.currentSellItemId = -1;
        this.currentSellQtyBefore = 0;
        this.sellSentAtMs = 0L;
        this.sellRejected = false;
        this.closeShopAttemptMs = 0L;
        this.lastMuleTimeMs = nowMs;
        this.mulePhase = MulePhase.FIND_MULE;
        this.returnLocation = null;
        this.mulePhaseStartMs = 0L;
        this.muleWalkStartMs = 0L;
        this.lastMuleWalkPos = null;
        this.lastMuleWalkPosCheckMs = 0L;
    }

    void onChatMessage(String raw) {
        if (raw == null) {
            return;
        }
        String cleaned = raw.replaceAll("<[^>]*>", "");
        if (cleaned.contains("You can't sell this item")) {
            this.sellRejected = true;
        }
    }

    int tick(Player player, ThievingConfig config) {
        long now = System.currentTimeMillis();
        if (this.state == ThievingState.STOPPING) {
            return -1;
        }
        if (this.stalls.isEmpty()) {
            buildEnabledStalls(config);
            if (this.stalls.isEmpty()) {
                this.status = "No stalls enabled - enable one in Progression";
                return 2000;
            }
        }
        if (!this.startIndexResolved) {
            int idx = highestEnabledIndex(thievingLevel());
            this.currentStallIndex = idx < 0 ? 0 : idx;
            this.startIndexResolved = true;
            this.status = "Starting at " + current().label + " stall";
        }

        if (this.state == ThievingState.SLEEPING) {
            if (now < this.sleepUntilMs) {
                this.status = "Sleeping break (" + ((this.sleepUntilMs - now) / 1000L) + "s)";
                return 1000;
            }
            this.state = ThievingState.STEALING;
            this.lastBreakTimeMs = now;
            this.lastStallSeenMs = now;
            this.status = "Resuming from break";
            return 600;
        }

        if (this.state == ThievingState.STEALING && config.enableLogoutFailsafe()) {
            long missingSec = (now - this.lastStallSeenMs) / 1000L;
            if (missingSec > (long) config.stallMissingTimeoutSec()) {
                this.status = "Failsafe: stall missing " + missingSec + "s, logging out";
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePauseWidgetPacket(LOGOUT_WIDGET_ID, -1);
                this.state = ThievingState.STOPPING;
                return 2000;
            }
        }

        if (this.state == ThievingState.STEALING && config.enableSleepBreaks()
            && (now - this.lastBreakTimeMs) / 60_000L >= (long) config.sleepAfterMinutes()) {
            this.state = ThievingState.SLEEPING;
            this.sleepUntilMs = now + (long) config.sleepDurationSeconds() * 1000L;
            this.status = "Taking break...";
            return 1000;
        }

        if (this.state == ThievingState.WALKING) {
            return loopWalking(player, now);
        }

        if (config.enableMuling()) {
            if (this.state == ThievingState.MULE_WALKING) {
                return loopMuleWalking(player, now, config);
            }
            if (this.state == ThievingState.MULE_TRADING) {
                return loopMuleTrading(now, config);
            }
            if (this.state == ThievingState.MULE_RETURNING) {
                return loopMuleReturning(player);
            }
            if (this.state == ThievingState.STEALING && shouldMule(now, config)) {
                if (parseMuleMeet(config) == null) {
                    this.status = "Mule triggered - set Meeting Point Tile under Muling";
                    return 2000;
                }
                this.returnLocation = player.getWorldLocation();
                this.state = ThievingState.MULE_WALKING;
                this.mulePhase = MulePhase.FIND_MULE;
                this.muleWalkStartMs = now;
                this.lastMuleWalkPos = null;
                this.lastMuleWalkPosCheckMs = now;
                this.status = "Mule triggered - walking to meeting point";
                return 300;
            }
        }

        Widget shop = this.client.getWidget(300, 0);
        if (shop != null && !shop.isHidden()) {
            return loopSelling(now, config);
        }

        if (this.state == ThievingState.TRADING) {
            if (now - this.tradingStartMs < 5000L) {
                this.status = "Waiting for shop to open...";
                return 400;
            }
            NPC trader = findTrader(player);
            if (trader != null) {
                this.status = "Re-opening shop";
                MousePackets.queueClickPacket();
                trader.interact(TRADER_ACTION);
                this.tradingStartMs = now;
                return 3000;
            }
            this.state = ThievingState.STEALING;
            return 600;
        }

        if (this.state == ThievingState.SELLING) {
            return finishSelling(now);
        }

        int next = highestEnabledIndex(thievingLevel());
        if (!this.advanceAfterSell && next > this.currentStallIndex) {
            this.advanceAfterSell = true;
            this.targetStallIndex = next;
            this.progressTradeStartMs = now;
            this.status = "Reached " + this.stalls.get(next).requiredLevel
                + " Thieving - selling to move to " + this.stalls.get(next).label;
        }

        if (this.advanceAfterSell || Inventory.getFreeSlots() <= 2) {
            if (!hasSellableItems()) {
                if (this.advanceAfterSell) {
                    this.status = "Nothing to sell - advancing to " + this.stalls.get(this.targetStallIndex).label;
                    return beginWalkToTarget(now);
                }
                this.unsellableItemIds.clear();
                this.status = "All items blacklisted - clearing, retrying";
                return 5000;
            }
            if (this.advanceAfterSell && now - this.progressTradeStartMs > PROGRESS_TRADE_TIMEOUT_MS) {
                this.status = "Trader unreachable - advancing to " + this.stalls.get(this.targetStallIndex).label;
                return beginWalkToTarget(now);
            }
            NPC trader = findTrader(player);
            if (trader != null) {
                this.status = "Opening shop";
                MousePackets.queueClickPacket();
                trader.interact(TRADER_ACTION);
                this.state = ThievingState.TRADING;
                this.tradingStartMs = now;
                return randomDelay(config);
            }
            this.status = "Searching for trader NPC...";
            return 800;
        }

        TileObject stall = TileObjects.getNearest(obj ->
            obj != null
                && obj.getId() == activeObjectId()
                && obj.getWorldLocation().distanceTo(player.getWorldLocation()) <= SEARCH_RADIUS);
        if (stall != null) {
            this.lastStallSeenMs = now;
            this.status = "Stealing from " + current().label + " stall";
            stall.interact("Steal-from");
            ++this.stealCount;
            return randomDelay(config);
        }

        WorldPoint coords = current().coords();
        if (player.getWorldLocation().distanceTo(coords) > ARRIVE_DIST) {
            this.state = ThievingState.WALKING;
            this.walkStartMs = now;
            this.lastWalkPos = null;
            this.lastWalkPosCheckMs = now;
            this.status = "Walking to " + current().label + " stall";
            return 600;
        }
        this.status = current().label + " stall not found...";
        return 600;
    }

    private boolean shouldMule(long now, ThievingConfig config) {
        if (config.enableTimeMule()) {
            return (now - this.lastMuleTimeMs) / 60_000L >= (long) config.muleIntervalMinutes();
        }
        Item coins = Inventory.getFirst(item -> item.getId() == COINS_ID);
        return coins != null && coins.getQuantity() >= config.muleGpThreshold();
    }

    private int loopSelling(long now, ThievingConfig config) {
        if (this.state != ThievingState.SELLING) {
            this.pendingSellIds.clear();
            ItemContainer inv = this.client.getItemContainer(InventoryID.INVENTORY);
            if (inv != null) {
                LinkedHashSet<Integer> ids = new LinkedHashSet<>();
                for (Item item : inv.getItems()) {
                    if (item.getId() == -1 || this.unsellableItemIds.contains(item.getId()) || isNeverSell(item)) {
                        continue;
                    }
                    ids.add(item.getId());
                }
                this.pendingSellIds.addAll(ids);
            }
            this.state = ThievingState.SELLING;
            this.currentSellItemId = -1;
            this.sellRejected = false;
            this.closeShopAttemptMs = 0L;
        }

        if (this.currentSellItemId != -1) {
            int qty = Inventory.getAll(item -> item.getId() == this.currentSellItemId).size();
            boolean reduced = qty < this.currentSellQtyBefore;
            boolean timedOut = now - this.sellSentAtMs > SELL_CONFIRM_TIMEOUT_MS;
            if (reduced || timedOut) {
                this.pendingSellIds.remove((Integer) this.currentSellItemId);
                this.currentSellItemId = -1;
                this.sellRejected = false;
                return 100;
            }
            if (this.sellRejected) {
                this.unsellableItemIds.add(this.currentSellItemId);
                this.pendingSellIds.remove((Integer) this.currentSellItemId);
                this.status = "Blacklisted " + itemName(this.currentSellItemId);
                this.currentSellItemId = -1;
                this.sellRejected = false;
                return 100;
            }
            this.status = "Waiting sell response...";
            return 100;
        }

        if (now < this.nextSellAtMs) {
            this.status = "Selling (waiting)";
            return 100;
        }

        if (this.pendingSellIds.isEmpty()) {
            if (this.closeShopAttemptMs == 0L) {
                this.closeShopAttemptMs = now;
            }
            if (now - this.closeShopAttemptMs > CLOSE_SHOP_TIMEOUT_MS) {
                this.status = "Shop close timeout - resetting";
                return finishSelling(now);
            }
            this.status = "Closing shop";
            Mouse.click(CLOSE_SHOP_X, CLOSE_SHOP_Y, true);
            return 400;
        }

        int id = this.pendingSellIds.get(0);
        if (this.unsellableItemIds.contains(id)) {
            this.pendingSellIds.remove(0);
            return 100;
        }
        Item item = Inventory.getFirst(i -> i.getId() == id);
        if (item == null || isNeverSell(item)) {
            this.pendingSellIds.remove(0);
            return 100;
        }

        this.status = "Selling " + itemName(id);
        this.currentSellItemId = id;
        this.currentSellQtyBefore = Inventory.getAll(i -> i.getId() == id).size();
        this.sellSentAtMs = now;
        this.sellRejected = false;
        MousePackets.queueClickPacket();
        WidgetPackets.widgetItemAction(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER, item, SELL_OP_INDEX);
        ++this.sellCount;
        this.nextSellAtMs = now + (long) sellDelay(config);
        return 100;
    }

    private int loopWalking(Player player, long now) {
        WorldPoint dest = current().coords();
        WorldPoint here = player.getWorldLocation();
        int dist = here.distanceTo(dest);
        if (dist <= ARRIVE_DIST) {
            this.lastStallSeenMs = now;
            this.walkStartMs = 0L;
            this.lastWalkPos = null;
            this.state = ThievingState.STEALING;
            this.status = "Arrived at " + current().label + " stall";
            return 600;
        }
        if (this.walkStartMs > 0L && now - this.walkStartMs > WALK_TIMEOUT_MS) {
            this.status = "Walk timeout (" + dist + " tiles) - resuming at " + current().label;
            this.lastStallSeenMs = now;
            this.walkStartMs = 0L;
            this.lastWalkPos = null;
            this.state = ThievingState.STEALING;
            return 600;
        }
        if (this.lastWalkPos == null) {
            this.lastWalkPos = here;
            this.lastWalkPosCheckMs = now;
        } else if (now - this.lastWalkPosCheckMs > 5000L) {
            if (here.equals(this.lastWalkPos)) {
                this.status = "Walk: stuck at " + dist + " tiles - retrying";
            }
            this.lastWalkPos = here;
            this.lastWalkPosCheckMs = now;
        }
        this.status = "Walking to " + current().label + " stall (" + dist + " tiles)";
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(dest);
        return 1200;
    }

    private int finishSelling(long now) {
        this.pendingSellIds.clear();
        this.currentSellItemId = -1;
        this.sellRejected = false;
        this.closeShopAttemptMs = 0L;
        if (this.advanceAfterSell) {
            return beginWalkToTarget(now);
        }
        this.state = ThievingState.STEALING;
        return 600;
    }

    private int beginWalkToTarget(long now) {
        this.currentStallIndex = this.targetStallIndex;
        this.advanceAfterSell = false;
        this.targetStallIndex = -1;
        this.progressTradeStartMs = 0L;
        this.pendingSellIds.clear();
        this.currentSellItemId = -1;
        this.sellRejected = false;
        this.closeShopAttemptMs = 0L;
        this.state = ThievingState.WALKING;
        this.walkStartMs = now;
        this.lastWalkPos = null;
        this.lastWalkPosCheckMs = now;
        this.status = "Walking to " + current().label + " stall";
        return 600;
    }

    private int loopMuleWalking(Player player, long now, ThievingConfig config) {
        WorldPoint meet = parseMuleMeet(config);
        if (meet == null) {
            this.status = "Mule: meeting tile not set - aborting";
            this.state = ThievingState.STEALING;
            this.lastMuleTimeMs = now;
            this.muleWalkStartMs = 0L;
            this.lastMuleWalkPos = null;
            return 2000;
        }
        WorldPoint here = player.getWorldLocation();
        int dist = here.distanceTo(meet);
        if (dist <= 3) {
            this.status = "Mule: arrived - starting trade";
            this.state = ThievingState.MULE_TRADING;
            this.mulePhase = MulePhase.FIND_MULE;
            this.mulePhaseStartMs = now;
            return 300;
        }
        if (this.muleWalkStartMs > 0L && now - this.muleWalkStartMs > MULE_WALK_TIMEOUT_MS) {
            this.status = "Mule: walk timeout - aborting";
            this.state = ThievingState.STEALING;
            this.lastMuleTimeMs = now;
            this.muleWalkStartMs = 0L;
            this.lastMuleWalkPos = null;
            return 600;
        }
        if (this.lastMuleWalkPos == null) {
            this.lastMuleWalkPos = here;
            this.lastMuleWalkPosCheckMs = now;
        } else if (now - this.lastMuleWalkPosCheckMs > 5000L) {
            if (here.equals(this.lastMuleWalkPos)) {
                this.status = "Mule: stuck at " + dist + " tiles - retrying";
            }
            this.lastMuleWalkPos = here;
            this.lastMuleWalkPosCheckMs = now;
        }
        this.status = "Mule: walking to meeting point (" + dist + " tiles)";
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(meet);
        return 1200;
    }

    private int loopMuleTrading(long now, ThievingConfig config) {
        if (this.mulePhase != MulePhase.WAIT_COMPLETE
            && this.mulePhase != MulePhase.OFFER_GOLD
            && now - this.mulePhaseStartMs > MULE_PHASE_TIMEOUT_MS) {
            this.status = "Mule: phase timeout - retrying";
            this.mulePhase = MulePhase.FIND_MULE;
            this.mulePhaseStartMs = now;
            return 600;
        }

        switch (this.mulePhase) {
            case FIND_MULE: {
                String name = config.muleName().trim();
                if (name.isEmpty()) {
                    this.status = "Mule: player name not configured";
                    finishMule(now);
                    return 600;
                }
                Player mule = findMule(name);
                if (mule == null) {
                    this.status = "Mule: waiting for [" + name + "]";
                    return 600;
                }
                this.status = "Mule: found [" + mule.getName() + "] - sending trade";
                mule.interact(MULE_TRADE_ACTION);
                this.mulePhase = MulePhase.INTERACT_MULE;
                this.mulePhaseStartMs = now;
                return 800;
            }
            case INTERACT_MULE: {
                Widget screen = this.client.getWidget(335, 0);
                if (screen != null && !screen.isHidden()) {
                    this.pendingOfferIds.clear();
                    for (int id : parseMuleItemIds(config)) {
                        if (!this.pendingOfferIds.contains(id) && Inventory.getFirst(i -> i.getId() == id) != null) {
                            this.pendingOfferIds.add(id);
                        }
                    }
                    this.status = "Mule: trade open - offering " + this.pendingOfferIds.size() + " type(s)";
                    this.mulePhase = MulePhase.OFFER_GOLD;
                    this.mulePhaseStartMs = now;
                    return 600;
                }
                this.status = "Mule: waiting for trade screen 1...";
                return 400;
            }
            case OFFER_GOLD: {
                Widget screen = this.client.getWidget(335, 0);
                if (screen == null || screen.isHidden()) {
                    this.pendingOfferIds.clear();
                    this.mulePhase = MulePhase.FIND_MULE;
                    this.mulePhaseStartMs = now;
                    return 400;
                }
                if (this.pendingOfferIds.isEmpty()) {
                    this.status = "Mule: all offered - accepting screen 1";
                    this.mulePhase = MulePhase.ACCEPT_SCREEN1;
                    this.mulePhaseStartMs = now;
                    return 800;
                }
                int id = this.pendingOfferIds.remove(0);
                Widget inv = this.client.getWidget(336, 0);
                if (inv == null || inv.isHidden()) {
                    this.pendingOfferIds.add(0, id);
                    this.status = "Mule: trade inventory missing - retrying";
                    return 400;
                }
                Widget slot = findItemWidget(inv, id);
                if (slot == null) {
                    return 200;
                }
                this.status = "Mule: offering all " + itemName(id);
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFourthOption(slot);
                return 1200;
            }
            case ACCEPT_SCREEN1: {
                Widget screen1 = this.client.getWidget(335, 0);
                if (screen1 == null || screen1.isHidden()) {
                    Widget screen2 = this.client.getWidget(334, 0);
                    if (screen2 != null && !screen2.isHidden()) {
                        this.mulePhase = MulePhase.ACCEPT_SCREEN2;
                        this.mulePhaseStartMs = now;
                        return 300;
                    }
                    finishMule(now);
                    return 600;
                }
                Widget accept = this.client.getWidget(335, 10);
                if (accept == null || accept.isHidden()) {
                    this.status = "Mule: accept button missing - retrying";
                    return 400;
                }
                this.status = "Mule: accepting screen 1";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(accept);
                this.mulePhase = MulePhase.WAIT_SCREEN2;
                this.mulePhaseStartMs = now;
                return 800;
            }
            case WAIT_SCREEN2: {
                Widget screen2 = this.client.getWidget(334, 0);
                if (screen2 != null && !screen2.isHidden()) {
                    this.mulePhase = MulePhase.ACCEPT_SCREEN2;
                    this.mulePhaseStartMs = now;
                    return 300;
                }
                Widget screen1 = this.client.getWidget(335, 0);
                if (screen1 == null || screen1.isHidden()) {
                    finishMule(now);
                    return 600;
                }
                this.status = "Mule: waiting for screen 2...";
                return 400;
            }
            case ACCEPT_SCREEN2: {
                Widget screen2 = this.client.getWidget(334, 0);
                if (screen2 == null || screen2.isHidden()) {
                    this.mulePhase = MulePhase.WAIT_COMPLETE;
                    this.mulePhaseStartMs = now;
                    return 300;
                }
                Widget accept = this.client.getWidget(334, 13);
                if (accept == null || accept.isHidden()) {
                    this.status = "Mule: screen 2 accept missing - retrying";
                    return 400;
                }
                this.status = "Mule: accepting screen 2";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(accept);
                this.mulePhase = MulePhase.WAIT_COMPLETE;
                this.mulePhaseStartMs = now;
                return 800;
            }
            case WAIT_COMPLETE: {
                Widget screen1 = this.client.getWidget(335, 0);
                Widget screen2 = this.client.getWidget(334, 0);
                boolean closed1 = screen1 == null || screen1.isHidden();
                boolean closed2 = screen2 == null || screen2.isHidden();
                if (closed1 && closed2) {
                    this.status = "Mule: trade complete";
                    finishMule(now);
                    return 300;
                }
                if (screen2 != null && !screen2.isHidden()) {
                    this.mulePhase = MulePhase.ACCEPT_SCREEN2;
                    this.mulePhaseStartMs = now;
                }
                this.status = "Mule: waiting for trade to finalise...";
                return 500;
            }
            default:
                return 600;
        }
    }

    private int loopMuleReturning(Player player) {
        if (this.returnLocation == null || player.getWorldLocation().distanceTo(this.returnLocation) <= 3) {
            this.returnLocation = null;
            this.state = ThievingState.STEALING;
            this.status = "Mule: returned - resuming";
            return 300;
        }
        this.status = "Mule: returning (" + player.getWorldLocation().distanceTo(this.returnLocation) + " tiles)";
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(this.returnLocation);
        return 1200;
    }

    private void finishMule(long now) {
        this.lastMuleTimeMs = now;
        this.state = ThievingState.MULE_RETURNING;
        this.mulePhase = MulePhase.FIND_MULE;
        this.pendingOfferIds.clear();
        this.status = "Mule: done - returning to steal spot";
    }

    private Player findMule(String name) {
        String needle = name.toLowerCase(Locale.ROOT);
        Player found = Players.getNearest(name);
        if (found != null) {
            return found;
        }
        found = Players.getNearest(p -> p != null && p.getName() != null
            && cleanName(p.getName()).equalsIgnoreCase(name));
        if (found != null) {
            return found;
        }
        found = Players.getNearest(p -> p != null && p.getName() != null
            && p.getName().toLowerCase(Locale.ROOT).contains(needle));
        if (found != null) {
            return found;
        }
        int best = Integer.MAX_VALUE;
        Player localPlayer = this.client.getLocalPlayer();
        WorldPoint here = localPlayer != null ? localPlayer.getWorldLocation() : null;
        Player pick = null;
        for (Player p : this.client.getPlayers()) {
            if (p == null || p.getName() == null || here == null) {
                continue;
            }
            String cleaned = cleanName(p.getName());
            if (!cleaned.equalsIgnoreCase(name) && !p.getName().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            int dist = p.getWorldLocation().distanceTo(here);
            if (dist < best) {
                best = dist;
                pick = p;
            }
        }
        return pick;
    }

    private static String cleanName(String raw) {
        return raw.replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim();
    }

    private static Widget findItemWidget(Widget inv, int itemId) {
        Widget[] children = inv.getDynamicChildren();
        if (children == null) {
            children = inv.getChildren();
        }
        if (children == null) {
            return null;
        }
        for (Widget child : children) {
            if (child != null && child.getItemId() == itemId) {
                return child;
            }
        }
        return null;
    }

    private void buildEnabledStalls(ThievingConfig config) {
        this.stalls.clear();
        if (config.stall1Enabled()) {
            this.stalls.add(ThievingStall.LVL_1);
        }
        if (config.stall2Enabled()) {
            this.stalls.add(ThievingStall.LVL_25);
        }
        if (config.stall3Enabled()) {
            this.stalls.add(ThievingStall.LVL_50);
        }
        if (config.stall4Enabled()) {
            this.stalls.add(ThievingStall.LVL_75);
        }
        if (config.stall5Enabled()) {
            this.stalls.add(ThievingStall.LVL_90);
        }
    }

    private int highestEnabledIndex(int level) {
        int best = -1;
        for (int i = 0; i < this.stalls.size(); i++) {
            if (level >= this.stalls.get(i).requiredLevel) {
                best = i;
            }
        }
        return best;
    }

    private ThievingStall current() {
        return this.stalls.get(Math.min(this.currentStallIndex, this.stalls.size() - 1));
    }

    private int activeObjectId() {
        return current().objectId;
    }

    private int activeTraderNpcId() {
        return current().traderNpcId;
    }

    private int thievingLevel() {
        return this.client.getRealSkillLevel(Skill.THIEVING);
    }

    private NPC findTrader(Player player) {
        WorldPoint here = player.getWorldLocation();
        int traderId = activeTraderNpcId();
        return NPCs.getNearest(npc ->
            npc != null
                && npc.getId() == traderId
                && npc.getWorldLocation().distanceTo(here) <= SEARCH_RADIUS);
    }

    private boolean hasSellableItems() {
        ItemContainer inv = this.client.getItemContainer(InventoryID.INVENTORY);
        if (inv == null) {
            return false;
        }
        for (Item item : inv.getItems()) {
            if (item.getId() == -1 || this.unsellableItemIds.contains(item.getId()) || isNeverSell(item)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean isNeverSell(Item item) {
        if (item == null) {
            return false;
        }
        if (item.getId() == COINS_ID) {
            return true;
        }
        String name = item.getName();
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("coin pouch") || lower.equals("coins");
    }

    private int randomDelay(ThievingConfig config) {
        int min = config.minClickDelay();
        int max = Math.max(min + 1, config.maxClickDelay());
        int delay = min + this.random.nextInt(max - min);
        int jitter = (int) ((delay * config.randomnessPercent()) / 100.0);
        if (jitter > 0) {
            delay += this.random.nextInt(jitter * 2 + 1) - jitter;
        }
        return Math.max(200, delay);
    }

    private int sellDelay(ThievingConfig config) {
        int delay = config.sellDelay();
        int jitter = (int) ((delay * config.randomnessPercent()) / 100.0);
        if (jitter > 0) {
            delay += this.random.nextInt(jitter * 2 + 1) - jitter;
        }
        return Math.max(100, delay);
    }

    private String itemName(int id) {
        ItemComposition def = this.client.getItemDefinition(id);
        return def != null ? def.getName() : String.valueOf(id);
    }

    private WorldPoint parseMuleMeet(ThievingConfig config) {
        String raw = config.muleMeetTile().trim();
        if (raw.isEmpty()) {
            return null;
        }
        try {
            String[] parts = raw.split("[,\\s]+");
            if (parts.length < 2) {
                return null;
            }
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int plane = parts.length >= 3 ? Integer.parseInt(parts[2].trim()) : this.client.getPlane();
            if (x <= 0 || y <= 0) {
                return null;
            }
            return new WorldPoint(x, y, plane);
        } catch (Exception e) {
            return null;
        }
    }

    private int[] parseMuleItemIds(ThievingConfig config) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(COINS_ID);
        String extra = config.muleExtraItemIds().trim();
        if (!extra.isEmpty()) {
            for (String part : extra.split("[,\\s]+")) {
                try {
                    int id = Integer.parseInt(part.trim());
                    if (id > 0 && !ids.contains(id)) {
                        ids.add(id);
                    }
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
        int[] out = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            out[i] = ids.get(i);
        }
        return out;
    }

    String getStatus() {
        return this.status;
    }

    ThievingState getState() {
        return this.state;
    }

    int getStealCount() {
        return this.stealCount;
    }

    int getSellCount() {
        return this.sellCount;
    }

    int getUnsellableCount() {
        return this.unsellableItemIds.size();
    }

    String getCurrentStallLabel() {
        return this.stalls.isEmpty() ? "-" : current().label;
    }

    int getCurrentRequiredLevel() {
        return this.stalls.isEmpty() ? 0 : current().requiredLevel;
    }

    MulePhase getMulePhase() {
        return this.mulePhase;
    }
}
