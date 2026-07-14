/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.crafting;

import com.google.inject.Binder;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.crafting.CraftingConfig;
import net.runelite.client.plugins.svc.crafting.CraftingEngine;
import net.runelite.client.plugins.svc.crafting.CraftingOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 AIO Crafter</font></nobr></html>", description="Reads the bank and auto-cuts the highest-level gem you can with a chisel (most xp first).", tags={"orion", "crafting", "crafter", "gems", "gem", "chisel", "bot", "aio", "skilling"}, enabledByDefault=false)
public class CraftingPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CraftingConfig config;
    private CraftingOverlay overlay;
    private CraftingEngine engine;
    private boolean loggedInLastTick = false;
    private long sessionStartMs = 0L;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(CraftingConfig.class).toInstance((Object)((CraftingConfig)configManager.getConfig(CraftingConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new CraftingEngine(this.client);
        this.overlay = new CraftingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
    }

    @Override
    protected void onStop() throws Exception {
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
        }
        this.overlay = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            if (this.engine != null) {
                this.engine.setStatus("Logged out");
            }
            this.loggedInLastTick = false;
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.engine.setStatus("Loading...");
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.engine.setStatus("Resuming...");
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        return this.engine.tick(player, new CraftingEngine.Settings(this.config.stopWhenIdle()));
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getStateName() {
        return this.engine != null ? this.engine.getStateName() : "TO_BANK";
    }

    public boolean isBanking() {
        return this.engine != null && this.engine.isBanking();
    }

    public boolean isCutting() {
        return this.engine != null && this.engine.isCutting();
    }

    public boolean isIdle() {
        return this.engine != null && this.engine.isIdle();
    }

    public String getGemName() {
        return this.engine != null ? this.engine.getGemName() : "\u2014";
    }

    public int getGemLevel() {
        return this.engine != null ? this.engine.getGemLevel() : 0;
    }

    public int getCuttableNow() {
        return this.engine != null ? this.engine.getCuttableNow() : 0;
    }

    public int getPlannedQty() {
        return this.engine != null ? this.engine.getPlannedQty() : 0;
    }

    public int getGemsCut() {
        return this.engine != null ? this.engine.getGemsCut() : 0;
    }

    public int getTrips() {
        return this.engine != null ? this.engine.getTrips() : 0;
    }

    public int getCraftLevel() {
        return this.engine != null ? this.engine.getCraftLevel() : 0;
    }

    public int getXpGained() {
        return this.engine != null ? this.engine.getXpGained() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }
}

