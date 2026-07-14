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
package net.runelite.client.plugins.svc.herblore;

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
import net.runelite.client.plugins.svc.herblore.HerbloreConfig;
import net.runelite.client.plugins.svc.herblore.HerbloreEngine;
import net.runelite.client.plugins.svc.herblore.HerbloreOverlay;
import net.runelite.client.plugins.svc.herblore.RecipeKind;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 AIO Herblore</font></nobr></html>", description="Reads the bank and auto-makes the best potion you can (finished \u2192 unfinished \u2192 clean herbs).", tags={"orion", "herblore", "bot", "potions", "aio", "skilling"}, enabledByDefault=false)
public class HerblorePlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private HerbloreConfig config;
    private HerbloreOverlay overlay;
    private HerbloreEngine engine;
    private boolean loggedInLastTick = false;
    private long sessionStartMs = 0L;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(HerbloreConfig.class).toInstance((Object)((HerbloreConfig)configManager.getConfig(HerbloreConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new HerbloreEngine(this.client);
        this.engine.reset();
        this.overlay = new HerbloreOverlay(this.client, this, this.config);
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
        if (this.engine != null) {
            this.engine.setStatus("Idle");
        }
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
        return this.engine.tick(player, new HerbloreEngine.Settings(this.config.priority1(), this.config.priority2(), this.config.priority3(), this.config.stopWhenIdle()));
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

    public boolean isCleaning() {
        return this.engine != null && this.engine.isCleaning();
    }

    public boolean isIdle() {
        return this.engine != null && this.engine.isIdle();
    }

    public String getTaskResult() {
        return this.engine != null ? this.engine.getTaskResult() : "\u2014";
    }

    public int getTaskLevel() {
        return this.engine != null ? this.engine.getTaskLevel() : 0;
    }

    public RecipeKind getTaskKind() {
        return this.engine != null ? this.engine.getTaskKind() : null;
    }

    public int getMakeableNow() {
        return this.engine != null ? this.engine.getMakeableNow() : 0;
    }

    public int getPlannedQty() {
        return this.engine != null ? this.engine.getPlannedQty() : 0;
    }

    public int getPotionsMade() {
        return this.engine != null ? this.engine.getPotionsMade() : 0;
    }

    public int getTrips() {
        return this.engine != null ? this.engine.getTrips() : 0;
    }

    public int getHerbLevel() {
        return this.engine != null ? this.engine.getHerbLevel() : 0;
    }

    public int getXpGained() {
        return this.engine != null ? this.engine.getXpGained() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }
}

