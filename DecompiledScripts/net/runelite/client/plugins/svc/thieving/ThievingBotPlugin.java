/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.thieving;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.thieving.MulePhase;
import net.runelite.client.plugins.svc.thieving.ThievingBotConfig;
import net.runelite.client.plugins.svc.thieving.ThievingBotOverlay;
import net.runelite.client.plugins.svc.thieving.ThievingStall;
import net.runelite.client.plugins.svc.thieving.ThievingState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.WidgetPackets;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 Thieving Bot</font></nobr></html>", description="Steals from stalls and sells to a nearby shopkeeper. Configurable antiban and logout failsafe.", tags={"orion", "thieving", "stall", "bot", "automation"}, enabledByDefault=false)
public class ThievingBotPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ThievingBotConfig config;
    private ThievingBotOverlay overlay;
    private ThievingState state = ThievingState.STEALING;
    private String status = "Starting";
    private boolean loggedInLastTick = false;
    private int stealCount = 0;
    private int sellCount = 0;
    private long sessionStartMs = 0L;
    private final Set<Integer> unsellableItemIds = new HashSet<Integer>();
    private long lastBreakTimeMs = 0L;
    private long sleepUntilMs = 0L;
    private long lastStallSeenMs = 0L;
    private final List<Integer> pendingSellIds = new ArrayList<Integer>();
    private final List<Integer> pendingOfferIds = new ArrayList<Integer>();
    private long nextSellAtMs = 0L;
    private long tradingStartMs = 0L;
    private int currentSellItemId = -1;
    private int currentSellQtyBefore = 0;
    private long sellSentAtMs = 0L;
    private boolean sellRejected = false;
    private static final int SELL_CONFIRM_TIMEOUT_MS = 2000;
    private long closeShopAttemptMs = 0L;
    private static final int CLOSE_SHOP_TIMEOUT_MS = 4000;
    private final Random random = new Random();
    private static final String TRADER_ACTION = "Open-shop";
    private static final int SEARCH_RADIUS = 13;
    private static final int CLOSE_SHOP_X = 482;
    private static final int CLOSE_SHOP_Y = 37;
    private static final int SELL_OP_INDEX = 4;
    private static final int LOGOUT_WIDGET_ID = 0xB6000B;
    private static final String MULE_TRADE_ACTION = "Trade with";
    private static final int ARRIVE_DIST = 5;
    private static final int WALK_TIMEOUT_MS = 60000;
    private static final int PROGRESS_TRADE_TIMEOUT_MS = 15000;
    private final List<ThievingStall> stalls = new ArrayList<ThievingStall>();
    private int currentStallIndex = 0;
    private boolean startIndexResolved = false;
    private boolean advanceAfterSell = false;
    private int targetStallIndex = -1;
    private long progressTradeStartMs = 0L;
    private long walkStartMs = 0L;
    private WorldPoint lastWalkPos = null;
    private long lastWalkPosCheckMs = 0L;
    private static final int COINS_ID = 995;
    private static final int MULE_PHASE_TIMEOUT_MS = 8000;
    private static final int MULE_WALK_TIMEOUT_MS = 45000;
    private MulePhase mulePhase = MulePhase.FIND_MULE;
    private WorldPoint returnLocation = null;
    private long lastMuleTimeMs = 0L;
    private long mulePhaseStartMs = 0L;
    private long muleWalkStartMs = 0L;
    private WorldPoint lastMuleWalkPos = null;
    private long lastMuleWalkPosCheckMs = 0L;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(ThievingBotConfig.class).toInstance((Object)((ThievingBotConfig)configManager.getConfig(ThievingBotConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new ThievingBotOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.buildEnabledStalls();
        this.currentStallIndex = 0;
        this.startIndexResolved = false;
        this.advanceAfterSell = false;
        this.targetStallIndex = -1;
        this.progressTradeStartMs = 0L;
        this.walkStartMs = 0L;
        this.lastWalkPos = null;
        this.lastWalkPosCheckMs = 0L;
        this.state = ThievingState.STEALING;
        this.status = this.stalls.isEmpty() ? "No stalls enabled \u2014 enable one in Progression" : "Starting";
        this.loggedInLastTick = false;
        this.stealCount = 0;
        this.sellCount = 0;
        this.lastBreakTimeMs = this.sessionStartMs = System.currentTimeMillis();
        this.lastStallSeenMs = this.sessionStartMs;
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
        this.lastMuleTimeMs = this.sessionStartMs;
        this.mulePhase = MulePhase.FIND_MULE;
        this.returnLocation = null;
        this.mulePhaseStartMs = 0L;
        this.muleWalkStartMs = 0L;
        this.lastMuleWalkPos = null;
        this.lastMuleWalkPosCheckMs = 0L;
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.status = "Idle";
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        switch (gameStateChanged.getGameState()) {
            case CONNECTION_LOST: 
            case LOGIN_SCREEN: {
                this.status = "Logged out";
                this.loggedInLastTick = false;
                break;
            }
            case LOGGED_IN: {
                this.loggedInLastTick = false;
                break;
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String string = chatMessage.getMessage().replaceAll("<[^>]*>", "");
        if (string.contains("You can't sell this item")) {
            this.sellRejected = true;
        }
    }

    @Override
    protected int loop() {
        int n;
        Widget widget;
        boolean bl;
        long l;
        long l2;
        int n2;
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.status = "Loading...";
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.status = "Resuming...";
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        long l3 = System.currentTimeMillis();
        if (this.state == ThievingState.STOPPING) {
            return -1;
        }
        if (this.stalls.isEmpty()) {
            this.status = "No stalls enabled \u2014 enable one in Progression";
            return 2000;
        }
        if (!this.startIndexResolved) {
            n2 = this.highestEnabledIndex(this.thievingLevel());
            this.currentStallIndex = n2 < 0 ? 0 : n2;
            this.startIndexResolved = true;
            this.status = "Starting at " + this.current().label + " stall";
        }
        if (this.state == ThievingState.SLEEPING) {
            if (l3 < this.sleepUntilMs) {
                long l4 = (this.sleepUntilMs - l3) / 1000L;
                this.status = "Sleeping break (" + l4 + "s)";
                return 1000;
            }
            this.state = ThievingState.STEALING;
            this.lastBreakTimeMs = l3;
            this.lastStallSeenMs = l3;
            this.status = "Resuming from break";
            return 600;
        }
        if (this.state == ThievingState.STEALING && this.config.enableLogoutFailsafe() && (l2 = (l3 - this.lastStallSeenMs) / 1000L) > (long)this.config.stallMissingTimeoutSec()) {
            this.status = "Failsafe: stall missing " + l2 + "s, logging out";
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(0xB6000B, -1);
            this.state = ThievingState.STOPPING;
            return 2000;
        }
        if (this.state == ThievingState.STEALING && this.config.enableSleepBreaks() && (l = (l3 - this.lastBreakTimeMs) / 60000L) >= (long)this.config.sleepAfterMinutes()) {
            this.state = ThievingState.SLEEPING;
            this.sleepUntilMs = l3 + (long)this.config.sleepDurationSeconds() * 1000L;
            this.status = "Taking break...";
            return 1000;
        }
        if (this.state == ThievingState.WALKING) {
            return this.loopWalking(player, l3);
        }
        if (this.config.enableMuling()) {
            if (this.state == ThievingState.MULE_WALKING) {
                return this.loopMuleWalking(player, l3);
            }
            if (this.state == ThievingState.MULE_TRADING) {
                return this.loopMuleTrading(l3);
            }
            if (this.state == ThievingState.MULE_RETURNING) {
                return this.loopMuleReturning(player);
            }
            if (this.state == ThievingState.STEALING) {
                n2 = 0;
                if (this.config.enableTimeMule()) {
                    n2 = (l3 - this.lastMuleTimeMs) / 60000L >= (long)this.config.muleIntervalMinutes() ? 1 : 0;
                } else {
                    Item item2 = Inventory.getFirst(item -> item.getId() == 995);
                    int n3 = n2 = item2 != null && item2.getQuantity() >= this.config.muleGpThreshold() ? 1 : 0;
                }
                if (n2 != 0) {
                    if (this.parseMuleMeet() == null) {
                        this.status = "Mule triggered, but Meeting Point Tile is not set \u2014 configure it under Muling";
                        return 2000;
                    }
                    this.returnLocation = player.getWorldLocation();
                    this.state = ThievingState.MULE_WALKING;
                    this.mulePhase = MulePhase.FIND_MULE;
                    this.muleWalkStartMs = l3;
                    this.lastMuleWalkPos = null;
                    this.lastMuleWalkPosCheckMs = l3;
                    this.status = "Mule triggered \u2014 walking to meeting point";
                    return 300;
                }
            }
        }
        boolean bl2 = bl = (widget = this.client.getWidget(300, 0)) != null && !widget.isHidden();
        if (bl) {
            return this.loopSelling(l3);
        }
        if (this.state == ThievingState.TRADING) {
            if (l3 - this.tradingStartMs < 5000L) {
                this.status = "Waiting for shop to open...";
                return 400;
            }
            NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == this.activeTraderNpcId() && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= 13);
            if (nPC2 != null) {
                this.status = "Re-opening shop";
                MousePackets.queueClickPacket();
                nPC2.interact(TRADER_ACTION);
                this.tradingStartMs = l3;
                return 3000;
            }
            this.state = ThievingState.STEALING;
            return 600;
        }
        if (this.state == ThievingState.SELLING) {
            return this.finishSelling(l3);
        }
        if (!this.advanceAfterSell && (n = this.highestEnabledIndex(this.thievingLevel())) > this.currentStallIndex) {
            this.advanceAfterSell = true;
            this.targetStallIndex = n;
            this.progressTradeStartMs = l3;
            this.status = "Reached " + this.stalls.get((int)n).requiredLevel + " Thieving \u2014 selling up to move to " + this.stalls.get((int)n).label;
        }
        if (this.advanceAfterSell || Inventory.getFreeSlots() <= 2) {
            n = this.hasSellableItems() ? 1 : 0;
            if (n == 0) {
                if (this.advanceAfterSell) {
                    this.status = "Nothing to sell \u2014 advancing to " + this.stalls.get((int)this.targetStallIndex).label;
                    return this.beginWalkToTarget(l3);
                }
                this.unsellableItemIds.clear();
                this.status = "All items blacklisted \u2014 clearing blacklist, retrying";
                return 5000;
            }
            if (this.advanceAfterSell && l3 - this.progressTradeStartMs > 15000L) {
                this.status = "Trader unreachable \u2014 advancing to " + this.stalls.get((int)this.targetStallIndex).label;
                return this.beginWalkToTarget(l3);
            }
            NPC nPC3 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == this.activeTraderNpcId() && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= 13);
            if (nPC3 != null) {
                this.status = "Opening shop";
                MousePackets.queueClickPacket();
                nPC3.interact(TRADER_ACTION);
                this.state = ThievingState.TRADING;
                this.tradingStartMs = l3;
                return this.randomDelay();
            }
            this.status = "Searching for trader NPC...";
            return 800;
        }
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == this.activeObjectId() && tileObject.getWorldLocation().distanceTo(player.getWorldLocation()) <= 13);
        if (tileObject2 != null) {
            this.lastStallSeenMs = l3;
            this.status = "Stealing from " + this.current().label + " stall";
            tileObject2.interact("Steal-from");
            ++this.stealCount;
            return this.randomDelay();
        }
        WorldPoint worldPoint = this.current().coords();
        if (player.getWorldLocation().distanceTo(worldPoint) > 5) {
            this.state = ThievingState.WALKING;
            this.walkStartMs = l3;
            this.lastWalkPos = null;
            this.lastWalkPosCheckMs = l3;
            this.status = "Walking to " + this.current().label + " stall";
            return 600;
        }
        this.status = this.current().label + " stall not found...";
        return 600;
    }

    private int loopSelling(long l) {
        Item item2;
        if (this.state != ThievingState.SELLING) {
            this.pendingSellIds.clear();
            ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
            if (itemContainer != null) {
                item2 = new LinkedHashSet();
                for (Item item3 : itemContainer.getItems()) {
                    if (item3.getId() == -1 || this.unsellableItemIds.contains(item3.getId())) continue;
                    item2.add(item3.getId());
                }
                this.pendingSellIds.addAll((Collection<Integer>)item2);
            }
            this.state = ThievingState.SELLING;
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
                this.status = "Shop close timeout \u2014 resetting";
                return this.finishSelling(l);
            }
            this.status = "Closing shop";
            Mouse.click(482, 37, true);
            return 400;
        }
        int n = this.pendingSellIds.get(0);
        if (this.unsellableItemIds.contains(n)) {
            this.pendingSellIds.remove(0);
            return 100;
        }
        item2 = Inventory.getFirst(item -> item.getId() == n);
        if (item2 == null) {
            this.pendingSellIds.remove(0);
            return 100;
        }
        this.status = "Selling " + this.itemName(n);
        this.currentSellItemId = n;
        this.currentSellQtyBefore = Inventory.getAll(item -> item.getId() == n).size();
        this.sellSentAtMs = l;
        this.sellRejected = false;
        MousePackets.queueClickPacket();
        WidgetPackets.widgetItemAction(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER, item2, 4);
        ++this.sellCount;
        this.nextSellAtMs = l + (long)this.sellDelay();
        return 100;
    }

    private int loopWalking(Player player, long l) {
        WorldPoint worldPoint = this.current().coords();
        WorldPoint worldPoint2 = player.getWorldLocation();
        int n = worldPoint2.distanceTo(worldPoint);
        if (n <= 5) {
            this.lastStallSeenMs = l;
            this.walkStartMs = 0L;
            this.lastWalkPos = null;
            this.state = ThievingState.STEALING;
            this.status = "Arrived at " + this.current().label + " stall";
            return 600;
        }
        if (this.walkStartMs > 0L && l - this.walkStartMs > 60000L) {
            this.status = "Walk timeout (" + n + " tiles) \u2014 resuming at " + this.current().label;
            this.lastStallSeenMs = l;
            this.walkStartMs = 0L;
            this.lastWalkPos = null;
            this.state = ThievingState.STEALING;
            return 600;
        }
        if (this.lastWalkPos == null) {
            this.lastWalkPos = worldPoint2;
            this.lastWalkPosCheckMs = l;
        } else if (l - this.lastWalkPosCheckMs > 5000L) {
            if (worldPoint2.equals((Object)this.lastWalkPos)) {
                this.status = "Walk: stuck at " + n + " tiles \u2014 retrying";
            }
            this.lastWalkPos = worldPoint2;
            this.lastWalkPosCheckMs = l;
        }
        this.status = "Walking to " + this.current().label + " stall (" + n + " tiles)";
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint);
        return 1200;
    }

    private int finishSelling(long l) {
        this.pendingSellIds.clear();
        this.currentSellItemId = -1;
        this.sellRejected = false;
        this.closeShopAttemptMs = 0L;
        if (this.advanceAfterSell) {
            return this.beginWalkToTarget(l);
        }
        this.state = ThievingState.STEALING;
        return 600;
    }

    private int beginWalkToTarget(long l) {
        this.currentStallIndex = this.targetStallIndex;
        this.advanceAfterSell = false;
        this.targetStallIndex = -1;
        this.progressTradeStartMs = 0L;
        this.pendingSellIds.clear();
        this.currentSellItemId = -1;
        this.sellRejected = false;
        this.closeShopAttemptMs = 0L;
        this.state = ThievingState.WALKING;
        this.walkStartMs = l;
        this.lastWalkPos = null;
        this.lastWalkPosCheckMs = l;
        this.status = "Walking to " + this.current().label + " stall";
        return 600;
    }

    private int loopMuleWalking(Player player, long l) {
        WorldPoint worldPoint = this.parseMuleMeet();
        if (worldPoint == null) {
            this.status = "Mule: Meeting Point Tile not set \u2014 aborting mule";
            this.state = ThievingState.STEALING;
            this.lastMuleTimeMs = l;
            this.muleWalkStartMs = 0L;
            this.lastMuleWalkPos = null;
            return 2000;
        }
        WorldPoint worldPoint2 = player.getWorldLocation();
        int n = worldPoint2.distanceTo(worldPoint);
        if (n <= 3) {
            this.status = "Mule: arrived \u2014 starting trade";
            this.state = ThievingState.MULE_TRADING;
            this.mulePhase = MulePhase.FIND_MULE;
            this.mulePhaseStartMs = l;
            return 300;
        }
        if (this.muleWalkStartMs > 0L && l - this.muleWalkStartMs > 45000L) {
            this.status = "Mule: walk timeout (" + n + " tiles) \u2014 aborting mule";
            this.state = ThievingState.STEALING;
            this.lastMuleTimeMs = l;
            this.muleWalkStartMs = 0L;
            this.lastMuleWalkPos = null;
            return 600;
        }
        if (this.lastMuleWalkPos == null) {
            this.lastMuleWalkPos = worldPoint2;
            this.lastMuleWalkPosCheckMs = l;
        } else if (l - this.lastMuleWalkPosCheckMs > 5000L) {
            if (worldPoint2.equals((Object)this.lastMuleWalkPos)) {
                this.status = "Mule: stuck at " + n + " tiles \u2014 retrying walk";
            }
            this.lastMuleWalkPos = worldPoint2;
            this.lastMuleWalkPosCheckMs = l;
        }
        this.status = "Mule: walking to meeting point (" + n + " tiles)";
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint);
        return 1200;
    }

    private int loopMuleTrading(long l) {
        if (this.mulePhase != MulePhase.WAIT_COMPLETE && this.mulePhase != MulePhase.OFFER_GOLD && l - this.mulePhaseStartMs > 8000L) {
            this.status = "Mule: phase timeout \u2014 retrying";
            this.mulePhase = MulePhase.FIND_MULE;
            this.mulePhaseStartMs = l;
            return 600;
        }
        switch (this.mulePhase) {
            case FIND_MULE: {
                String string = this.config.muleName().trim();
                if (string.isEmpty()) {
                    this.status = "Mule: player name not configured!";
                    this.finishMule(l);
                    return 600;
                }
                String string2 = string.toLowerCase();
                Player player2 = Players.getNearest(string);
                if (player2 == null) {
                    player2 = Players.getNearest(player -> {
                        if (player == null || player.getName() == null) {
                            return false;
                        }
                        return player.getName().replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim().equalsIgnoreCase(string);
                    });
                }
                if (player2 == null) {
                    player2 = Players.getNearest(player -> player != null && player.getName() != null && player.getName().toLowerCase().contains(string2));
                }
                if (player2 == null) {
                    int n = Integer.MAX_VALUE;
                    WorldPoint worldPoint = this.client.getLocalPlayer().getWorldLocation();
                    for (Player player3 : this.client.getPlayers()) {
                        int n2;
                        String string3;
                        boolean bl;
                        if (player3 == null || player3.getName() == null || !(bl = (string3 = player3.getName().replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim()).equalsIgnoreCase(string) || player3.getName().toLowerCase().contains(string2)) || (n2 = player3.getWorldLocation().distanceTo(worldPoint)) >= n) continue;
                        n = n2;
                        player2 = player3;
                    }
                }
                if (player2 == null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Player player4 : this.client.getPlayers()) {
                        if (player4 == null) continue;
                        stringBuilder.append("[").append(player4.getName()).append("]");
                        if (stringBuilder.length() <= 80) continue;
                        break;
                    }
                    this.status = "No match [" + string + "]. List: " + String.valueOf(stringBuilder.length() > 0 ? stringBuilder : "EMPTY");
                    return 600;
                }
                this.status = "Mule: found [" + player2.getName() + "] \u2014 sending trade";
                player2.interact(MULE_TRADE_ACTION);
                this.mulePhase = MulePhase.INTERACT_MULE;
                this.mulePhaseStartMs = l;
                return 800;
            }
            case INTERACT_MULE: {
                Widget widget = this.client.getWidget(335, 0);
                if (widget != null && !widget.isHidden()) {
                    this.pendingOfferIds.clear();
                    int[] nArray = this.parseMuleItemIds();
                    int n = nArray.length;
                    for (int i = 0; i < n; ++i) {
                        int n3;
                        int n4 = n3 = nArray[i];
                        if (this.pendingOfferIds.contains(n3) || Inventory.getFirst(item -> item.getId() == n4) == null) continue;
                        this.pendingOfferIds.add(n3);
                    }
                    this.status = "Mule: trade open \u2014 offering " + this.pendingOfferIds.size() + " item type(s)";
                    this.mulePhase = MulePhase.OFFER_GOLD;
                    this.mulePhaseStartMs = l;
                    return 600;
                }
                this.status = "Mule: waiting for trade screen 1...";
                return 400;
            }
            case OFFER_GOLD: {
                int n;
                Widget widget = this.client.getWidget(335, 0);
                if (widget == null || widget.isHidden()) {
                    this.pendingOfferIds.clear();
                    this.mulePhase = MulePhase.FIND_MULE;
                    this.mulePhaseStartMs = l;
                    return 400;
                }
                if (this.pendingOfferIds.isEmpty()) {
                    this.status = "Mule: all items offered \u2014 accepting screen 1";
                    this.mulePhase = MulePhase.ACCEPT_SCREEN1;
                    this.mulePhaseStartMs = l;
                    return 800;
                }
                int n5 = n = this.pendingOfferIds.remove(0).intValue();
                Widget widget2 = this.client.getWidget(336, 0);
                if (widget2 == null || widget2.isHidden()) {
                    this.status = "Mule: trade inventory widget missing \u2014 retrying";
                    this.pendingOfferIds.add(0, n);
                    return 400;
                }
                Widget[] widgetArray = widget2.getDynamicChildren();
                if (widgetArray == null) {
                    widgetArray = widget2.getChildren();
                }
                Widget widget3 = null;
                if (widgetArray != null) {
                    for (Widget widget4 : widgetArray) {
                        if (widget4 == null || widget4.getItemId() != n5) continue;
                        widget3 = widget4;
                        break;
                    }
                }
                if (widget3 == null) {
                    return 200;
                }
                this.status = "Mule: offering all " + this.itemName(n);
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFourthOption(widget3);
                return 1200;
            }
            case ACCEPT_SCREEN1: {
                Widget widget = this.client.getWidget(335, 0);
                if (widget == null || widget.isHidden()) {
                    Widget widget5 = this.client.getWidget(334, 0);
                    if (widget5 != null && !widget5.isHidden()) {
                        this.mulePhase = MulePhase.ACCEPT_SCREEN2;
                        this.mulePhaseStartMs = l;
                        return 300;
                    }
                    this.finishMule(l);
                    return 600;
                }
                Widget widget6 = this.client.getWidget(335, 10);
                if (widget6 == null || widget6.isHidden()) {
                    this.status = "Mule: 335:10 not found \u2014 retrying";
                    return 400;
                }
                this.status = "Mule: accepting screen 1";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(widget6);
                this.mulePhase = MulePhase.WAIT_SCREEN2;
                this.mulePhaseStartMs = l;
                return 800;
            }
            case WAIT_SCREEN2: {
                Widget widget = this.client.getWidget(334, 0);
                if (widget != null && !widget.isHidden()) {
                    this.status = "Mule: trade screen 2 open \u2014 accepting";
                    this.mulePhase = MulePhase.ACCEPT_SCREEN2;
                    this.mulePhaseStartMs = l;
                    return 300;
                }
                Widget widget7 = this.client.getWidget(335, 0);
                if (widget7 == null || widget7.isHidden()) {
                    this.finishMule(l);
                    return 600;
                }
                this.status = "Mule: waiting for screen 2...";
                return 400;
            }
            case ACCEPT_SCREEN2: {
                Widget widget = this.client.getWidget(334, 0);
                if (widget == null || widget.isHidden()) {
                    this.mulePhase = MulePhase.WAIT_COMPLETE;
                    this.mulePhaseStartMs = l;
                    return 300;
                }
                Widget widget8 = this.client.getWidget(334, 13);
                if (widget8 == null || widget8.isHidden()) {
                    this.status = "Mule: 334:13 not found \u2014 retrying";
                    return 400;
                }
                this.status = "Mule: accepting screen 2";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(widget8);
                this.mulePhase = MulePhase.WAIT_COMPLETE;
                this.mulePhaseStartMs = l;
                return 800;
            }
            case WAIT_COMPLETE: {
                boolean bl;
                Widget widget = this.client.getWidget(335, 0);
                Widget widget9 = this.client.getWidget(334, 0);
                boolean bl2 = widget == null || widget.isHidden();
                boolean bl3 = bl = widget9 == null || widget9.isHidden();
                if (bl2 && bl) {
                    this.status = "Mule: trade complete";
                    this.finishMule(l);
                    return 300;
                }
                if (widget9 != null && !widget9.isHidden()) {
                    this.mulePhase = MulePhase.ACCEPT_SCREEN2;
                    this.mulePhaseStartMs = l;
                }
                this.status = "Mule: waiting for trade to finalise...";
                return 500;
            }
        }
        return 600;
    }

    private int loopMuleReturning(Player player) {
        if (this.returnLocation == null || player.getWorldLocation().distanceTo(this.returnLocation) <= 3) {
            this.returnLocation = null;
            this.state = ThievingState.STEALING;
            this.status = "Mule: returned \u2014 resuming";
            return 300;
        }
        this.status = "Mule: returning (" + player.getWorldLocation().distanceTo(this.returnLocation) + " tiles)";
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(this.returnLocation);
        return 1200;
    }

    private void finishMule(long l) {
        this.lastMuleTimeMs = l;
        this.state = ThievingState.MULE_RETURNING;
        this.mulePhase = MulePhase.FIND_MULE;
        this.pendingOfferIds.clear();
        this.status = "Mule: done \u2014 returning to steal spot";
    }

    private void buildEnabledStalls() {
        this.stalls.clear();
        if (this.config.stall1Enabled()) {
            this.stalls.add(ThievingStall.LVL_1);
        }
        if (this.config.stall2Enabled()) {
            this.stalls.add(ThievingStall.LVL_25);
        }
        if (this.config.stall3Enabled()) {
            this.stalls.add(ThievingStall.LVL_50);
        }
        if (this.config.stall4Enabled()) {
            this.stalls.add(ThievingStall.LVL_75);
        }
        if (this.config.stall5Enabled()) {
            this.stalls.add(ThievingStall.LVL_90);
        }
    }

    private int highestEnabledIndex(int n) {
        int n2 = -1;
        for (int i = 0; i < this.stalls.size(); ++i) {
            if (n < this.stalls.get((int)i).requiredLevel) continue;
            n2 = i;
        }
        return n2;
    }

    private ThievingStall current() {
        return this.stalls.get(Math.min(this.currentStallIndex, this.stalls.size() - 1));
    }

    private int activeObjectId() {
        return this.current().objectId;
    }

    private int activeTraderNpcId() {
        return this.current().traderNpcId;
    }

    private int thievingLevel() {
        return this.client.getRealSkillLevel(Skill.THIEVING);
    }

    private boolean hasSellableItems() {
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return false;
        }
        for (Item item : itemContainer.getItems()) {
            if (item.getId() == -1 || this.unsellableItemIds.contains(item.getId())) continue;
            return true;
        }
        return false;
    }

    private int randomDelay() {
        int n;
        int n2 = this.config.minClickDelay();
        int n3 = n2 + this.random.nextInt((n = Math.max(n2 + 1, this.config.maxClickDelay())) - n2);
        int n4 = (int)((double)(n3 * this.config.randomnessPercent()) / 100.0);
        if (n4 > 0) {
            n3 += this.random.nextInt(n4 * 2 + 1) - n4;
        }
        return Math.max(200, n3);
    }

    private int sellDelay() {
        int n = this.config.sellDelay();
        int n2 = (int)((double)(n * this.config.randomnessPercent()) / 100.0);
        if (n2 > 0) {
            n += this.random.nextInt(n2 * 2 + 1) - n2;
        }
        return Math.max(100, n);
    }

    private String itemName(int n) {
        ItemComposition itemComposition = this.client.getItemDefinition(n);
        return itemComposition != null ? itemComposition.getName() : String.valueOf(n);
    }

    private WorldPoint parseMuleMeet() {
        String string = this.config.muleMeetTile().trim();
        if (string.isEmpty()) {
            return null;
        }
        try {
            int n;
            String[] stringArray = string.split("[,\\s]+");
            if (stringArray.length < 2) {
                return null;
            }
            int n2 = Integer.parseInt(stringArray[0].trim());
            int n3 = Integer.parseInt(stringArray[1].trim());
            int n4 = n = stringArray.length >= 3 ? Integer.parseInt(stringArray[2].trim()) : this.client.getPlane();
            if (n2 <= 0 || n3 <= 0) {
                return null;
            }
            return new WorldPoint(n2, n3, n);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private int[] parseMuleItemIds() {
        int n;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        arrayList.add(995);
        String string = this.config.muleExtraItemIds().trim();
        if (!string.isEmpty()) {
            String[] objectArray = string.split("[,\\s]+");
            n = objectArray.length;
            for (int i = 0; i < n; ++i) {
                String string2 = objectArray[i];
                try {
                    int numberFormatException = Integer.parseInt(string2.trim());
                    if (numberFormatException <= 0 || arrayList.contains(numberFormatException)) continue;
                    arrayList.add(numberFormatException);
                    continue;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        int[] nArray = new int[arrayList.size()];
        for (n = 0; n < arrayList.size(); ++n) {
            nArray[n] = (Integer)arrayList.get(n);
        }
        return nArray;
    }

    private static boolean playerNameMatches(String string, String string2) {
        if (string == null || string2 == null || string2.isEmpty()) {
            return false;
        }
        String string3 = string.replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim();
        return string3.equalsIgnoreCase(string2);
    }

    public String getStatus() {
        return this.status;
    }

    public ThievingState getState() {
        return this.state;
    }

    public int getStealCount() {
        return this.stealCount;
    }

    public int getSellCount() {
        return this.sellCount;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public int getUnsellableCount() {
        return this.unsellableItemIds.size();
    }

    public long getLastStallSeenMs() {
        return this.lastStallSeenMs;
    }

    public long getSleepUntilMs() {
        return this.sleepUntilMs;
    }

    public long getLastBreakTimeMs() {
        return this.lastBreakTimeMs;
    }

    public MulePhase getMulePhase() {
        return this.mulePhase;
    }

    public long getLastMuleTimeMs() {
        return this.lastMuleTimeMs;
    }

    public String getCurrentStallLabel() {
        return this.stalls.isEmpty() ? "\u2014" : this.current().label;
    }

    public int getCurrentRequiredLevel() {
        return this.stalls.isEmpty() ? 0 : this.current().requiredLevel;
    }

    public boolean isAdvancing() {
        return this.advanceAfterSell;
    }

    public String getTargetStallLabel() {
        return this.targetStallIndex >= 0 && this.targetStallIndex < this.stalls.size() ? this.stalls.get((int)this.targetStallIndex).label : "\u2014";
    }
}

