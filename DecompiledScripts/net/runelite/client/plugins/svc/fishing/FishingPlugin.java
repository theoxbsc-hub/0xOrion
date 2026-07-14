/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.TileObject
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.fishing;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.fishing.FishingConfig;
import net.runelite.client.plugins.svc.fishing.FishingMode;
import net.runelite.client.plugins.svc.fishing.FishingOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.DepositBox;
import net.unethicalite.api.items.Inventory;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 Fishing Bot</font></nobr></html>", description="Fishes at a configurable spot and deposits into a nearby barrel when full.", tags={"orion", "fishing", "bot", "automation"}, enabledByDefault=false)
public class FishingPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FishingConfig config;
    private FishingOverlay overlay;
    private FishingState state = FishingState.FISHING;
    private String status = "Starting";
    private int depositCount = 0;
    private long sessionStartMs = 0L;
    private boolean loggedInLastTick = false;
    private long barrelInteractedAtMs = 0L;
    private long depositSentAtMs = 0L;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(FishingConfig.class).toInstance((Object)((FishingConfig)configManager.getConfig(FishingConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new FishingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.state = FishingState.FISHING;
        this.status = "Starting";
        this.loggedInLastTick = false;
        this.depositCount = 0;
        this.sessionStartMs = System.currentTimeMillis();
        this.barrelInteractedAtMs = 0L;
        this.depositSentAtMs = 0L;
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.status = "Idle";
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.status = "Logged out";
            this.loggedInLastTick = false;
        }
    }

    @Override
    protected int loop() {
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
        if (this.client.getLocalPlayer() == null) {
            return 600;
        }
        switch (this.state.ordinal()) {
            case 0: {
                return this.loopFishing();
            }
            case 1: {
                return this.loopOpeningBarrel();
            }
            case 2: {
                return this.loopDepositing();
            }
            case 3: {
                return this.loopDropping();
            }
        }
        return 600;
    }

    private int loopFishing() {
        if (Inventory.getFreeSlots() == 0) {
            if (this.config.fishingMode() == FishingMode.DROP) {
                this.status = "Inventory full \u2014 dropping fish";
                this.state = FishingState.DROPPING;
            } else {
                this.status = "Inventory full \u2014 finding barrel";
                this.state = FishingState.OPENING_BARREL;
                this.barrelInteractedAtMs = 0L;
            }
            return 300;
        }
        if (this.client.getLocalPlayer().getAnimation() != -1) {
            this.status = "Fishing...";
            return 600;
        }
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == this.config.fishingSpotId() && !nPC.isDead());
        if (nPC2 != null) {
            this.status = "Clicking spot";
            nPC2.interact(this.config.fishingAction());
            return 1800;
        }
        this.status = "Waiting for spot...";
        return 800;
    }

    private int loopOpeningBarrel() {
        if (DepositBox.isOpen()) {
            this.status = "Deposit box open \u2014 depositing";
            this.state = FishingState.DEPOSITING;
            this.depositSentAtMs = 0L;
            return 300;
        }
        long l = System.currentTimeMillis();
        if (this.barrelInteractedAtMs > 0L && l - this.barrelInteractedAtMs < 4000L) {
            this.status = "Waiting for barrel...";
            return 400;
        }
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 29108);
        if (tileObject2 != null) {
            this.status = "Opening barrel";
            tileObject2.interact(this.config.barrelAction());
            this.barrelInteractedAtMs = l;
            return 1200;
        }
        this.status = "Barrel not found";
        return 800;
    }

    private int loopDepositing() {
        Widget[] widgetArray;
        Widget widget;
        if (!DepositBox.isOpen()) {
            this.status = "Returning to spot";
            this.state = FishingState.FISHING;
            this.depositSentAtMs = 0L;
            return 600;
        }
        List<Integer> list = this.parseFishIds();
        boolean bl = list.stream().anyMatch(n -> Inventory.getFirst(item -> item.getId() == n.intValue()) != null);
        if (!bl) {
            this.status = "Closing deposit box";
            DepositBox.close();
            ++this.depositCount;
            this.state = FishingState.FISHING;
            this.depositSentAtMs = 0L;
            return 800;
        }
        long l = System.currentTimeMillis();
        if (this.depositSentAtMs > 0L && l - this.depositSentAtMs < 1500L) {
            this.status = "Depositing...";
            return 300;
        }
        Widget widget2 = this.client.getWidget(192, 19);
        if (widget2 != null && !widget2.isHidden()) {
            widget2.interact(0);
        }
        if ((widget = this.client.getWidget(192, 2)) != null && (widgetArray = widget.getDynamicChildren()) != null) {
            for (Widget widget3 : widgetArray) {
                if (widget3 == null || widget3.isHidden() || !list.contains(widget3.getItemId())) continue;
                this.status = "Depositing fish";
                widget3.interact(0);
                this.depositSentAtMs = l;
                return 600;
            }
        }
        this.status = "Fish slot not found";
        return 800;
    }

    private int loopDropping() {
        List<Integer> list = this.parseFishIds();
        List<Item> list2 = Inventory.getAll(item -> item != null && list.contains(item.getId()));
        if (list2.isEmpty()) {
            this.status = "Dropped \u2014 resuming";
            ++this.depositCount;
            this.state = FishingState.FISHING;
            return 300;
        }
        this.status = "Dropping fish (" + list2.size() + ")";
        for (Item item2 : list2) {
            item2.interact("Drop");
            try {
                Thread.sleep(80L);
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                return 300;
            }
        }
        return 300;
    }

    private List<Integer> parseFishIds() {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        if (this.config.fishItemId1() > 0) {
            arrayList.add(this.config.fishItemId1());
        }
        if (this.config.fishItemId2() > 0) {
            arrayList.add(this.config.fishItemId2());
        }
        return arrayList;
    }

    public String getStatus() {
        return this.status;
    }

    public String getState() {
        return this.state.name();
    }

    public int getDepositCount() {
        return this.depositCount;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    private static enum FishingState {
        FISHING,
        OPENING_BARREL,
        DEPOSITING,
        DROPPING;

    }
}

