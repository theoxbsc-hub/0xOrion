/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.smithing;

import com.google.inject.Binder;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.smithing.SmithingConfig;
import net.runelite.client.plugins.svc.smithing.SmithingEngine;
import net.runelite.client.plugins.svc.smithing.SmithingOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 Smithing Bot</font></nobr></html>", description="Auto smithing: banks at home, smelts the best ores into bars and smiths bars into the highest item your level allows, then loops.", tags={"orion", "smithing", "smith", "smelt", "bot", "skilling", "automation"}, enabledByDefault=false)
public class SmithingPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SmithingConfig config;
    private SmithingOverlay overlay;
    private SmithingEngine engine;
    private long sessionStartMs = 0L;
    private boolean loggedInLastTick = false;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(SmithingConfig.class).toInstance((Object)((SmithingConfig)configManager.getConfig(SmithingConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new SmithingEngine(this.client);
        this.overlay = new SmithingOverlay(this.client, this, this.config);
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

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getMessage() == null) {
            return;
        }
        if (this.engine != null) {
            this.engine.onChatMessage(chatMessage.getMessage().replaceAll("<[^>]*>", "").toLowerCase());
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            if (this.engine != null) {
                this.engine.setStatus("Loading...");
            }
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            if (this.engine != null) {
                this.engine.setStatus("Resuming...");
            }
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        return this.engine.tick(player, this.buildSettings());
    }

    private SmithingEngine.Settings buildSettings() {
        return new SmithingEngine.Settings(this.config.mode(), this.config.stopWhenOutOfMaterials(), this.config.antiBanMs());
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getStateName() {
        return this.engine != null ? this.engine.getStateName() : "DECIDE";
    }

    public String getTaskLabel() {
        return this.engine != null ? this.engine.getTaskLabel() : "-";
    }

    public int getMadeCount() {
        return this.engine != null ? this.engine.getMadeCount() : 0;
    }

    public int getBarsSmelted() {
        return this.engine != null ? this.engine.getBarsSmelted() : 0;
    }

    public int getItemsSmithed() {
        return this.engine != null ? this.engine.getItemsSmithed() : 0;
    }

    public int getTripCount() {
        return this.engine != null ? this.engine.getTripCount() : 0;
    }

    public int getSmithLevel() {
        return this.engine != null ? this.engine.getSmithLevel() : 1;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public int getXpGained() {
        return this.engine != null ? this.engine.getXpGained() : 0;
    }
}

