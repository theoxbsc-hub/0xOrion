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
package net.runelite.client.plugins.svc.agility;

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
import net.runelite.client.plugins.svc.agility.AgilityConfig;
import net.runelite.client.plugins.svc.agility.AgilityCourse;
import net.runelite.client.plugins.svc.agility.AgilityEngine;
import net.runelite.client.plugins.svc.agility.AgilityOverlay;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 Agility Bot</font></nobr></html>", description="Automates agility courses: clicks each obstacle in order, waits out the animation, and loops. Lvl 1 (Gnome) and Lvl 50 (Ice) courses, each individually toggleable.", tags={"orion", "agility", "course", "bot", "automation"}, enabledByDefault=false)
public class AgilityPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AgilityConfig config;
    private AgilityOverlay overlay;
    private AgilityEngine engine;
    private long sessionStartMs;
    private boolean loggedInLastTick;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(AgilityConfig.class).toInstance((Object)((AgilityConfig)configManager.getConfig(AgilityConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new AgilityEngine(this.client);
        this.overlay = new AgilityOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.engine.reset();
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
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
            return 800;
        }
        if (gameState != GameState.LOGGED_IN) {
            this.engine.setStatus("Logged out");
            this.loggedInLastTick = false;
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.engine.setStatus("Resuming...");
            this.engine.requestResync();
            return 1000;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        AgilityCourse agilityCourse = this.selectedCourse();
        int n = this.engine.tick(player, new AgilityEngine.Settings(agilityCourse, this.config.randomizeDelay()));
        if (this.engine.needsRecovery()) {
            this.engine.clearRecovery();
            this.engine.setStatus("Off-course \u2014 re-syncing (walk back to the course if this repeats)");
        }
        return n;
    }

    private AgilityCourse selectedCourse() {
        return this.config.enableLvl1Course() ? AgilityCourse.GNOME_LVL1 : (this.config.enableLvl50Course() ? AgilityCourse.ICE_LVL50 : null);
    }

    public AgilityCourse getRunningCourse() {
        AgilityCourse agilityCourse = this.engine != null ? this.engine.getRunningCourse() : null;
        return agilityCourse != null ? agilityCourse : this.selectedCourse();
    }

    public int getCurrentIndex() {
        return this.engine != null ? this.engine.getCurrentIndex() : 0;
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public int getLaps() {
        return this.engine != null ? this.engine.getLaps() : 0;
    }

    public int getObstaclesDone() {
        return this.engine != null ? this.engine.getObstaclesDone() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }
}

