/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.hunter;

import com.google.inject.Binder;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.hunter.BirdSnareHunterEngine;
import net.runelite.client.plugins.svc.hunter.BoxTrapHunter;
import net.runelite.client.plugins.svc.hunter.HunterConfig;
import net.runelite.client.plugins.svc.hunter.HunterMethod;
import net.runelite.client.plugins.svc.hunter.HunterOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.game.Skills;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 Hunter Bot</font></nobr></html>", description="Lays bird snares, checks them for loot/XP, drops junk and keeps the cluster topped up.", tags={"orion", "hunter", "bird", "snare", "trap", "skilling", "bot", "automation"}, enabledByDefault=false)
public class HunterPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private HunterConfig config;
    private HunterOverlay overlay;
    private BoxTrapHunter boxTrap;
    private BirdSnareHunterEngine birdEngine;
    private String status = "Starting";
    private int startXp = -1;
    private long sessionStartMs;
    private boolean loggedInLastTick;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(HunterConfig.class).toInstance((Object)((HunterConfig)configManager.getConfig(HunterConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new HunterOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.boxTrap = new BoxTrapHunter(this.client, this.config);
        this.birdEngine = new BirdSnareHunterEngine(this.client);
        this.resetState();
        this.sessionStartMs = System.currentTimeMillis();
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.status = "Idle";
    }

    private void resetState() {
        this.status = "Starting";
        this.startXp = -1;
        this.loggedInLastTick = false;
        if (this.boxTrap != null) {
            this.boxTrap.reset();
        }
        if (this.birdEngine != null) {
            this.birdEngine.reset();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.status = "Logged out";
            this.loggedInLastTick = false;
            if (this.boxTrap != null) {
                this.boxTrap.onSceneLost();
            }
            if (this.birdEngine != null) {
                this.birdEngine.onSceneLost();
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String string = chatMessage.getMessage();
        if (string == null) {
            return;
        }
        String string2 = string.toLowerCase();
        if (this.boxTrap != null) {
            this.boxTrap.onChatMessage(string2);
        }
        if (this.birdEngine != null) {
            this.birdEngine.onChatMessage(string2);
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
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (this.startXp < 0) {
            this.startXp = Skills.getExperience(Skill.HUNTER);
        }
        switch (this.config.method()) {
            case BIRD_SNARE: {
                int n = this.birdEngine.tick(player, this.buildBirdSettings());
                this.status = this.birdEngine.getStatus();
                return n;
            }
            case BOX_TRAP: {
                int n = this.boxTrap.loop(player);
                this.status = this.boxTrap.getStatus();
                return n;
            }
        }
        this.status = "Method not implemented";
        return 1000;
    }

    private BirdSnareHunterEngine.Settings buildBirdSettings() {
        return new BirdSnareHunterEngine.Settings(this.config.birdNames(), this.config.maxTraps(), this.config.clusterRadius(), this.config.birdSearchRadius(), this.config.walkToBirds(), this.config.dropItems(), this.config.keepItems());
    }

    private boolean boxTrapActive() {
        return this.config.method() == HunterMethod.BOX_TRAP && this.boxTrap != null;
    }

    public String getStatus() {
        return this.status;
    }

    public int getActiveTraps() {
        return this.boxTrapActive() ? this.boxTrap.getActiveTraps() : this.birdEngine.getActiveTraps();
    }

    public int getEffectiveMaxTraps() {
        return this.boxTrapActive() ? this.boxTrap.getEffectiveMaxBoxes() : this.birdEngine.getEffectiveMaxTraps();
    }

    public String getTrapLabel() {
        return this.boxTrapActive() ? "Boxes" : "Traps";
    }

    public String getPrimaryStatLabel() {
        return this.boxTrapActive() ? "Caught" : "Collected";
    }

    public int getPrimaryStatValue() {
        return this.boxTrapActive() ? this.boxTrap.getCaught() : this.birdEngine.getCollected();
    }

    public String getSecondaryStatLabel() {
        return this.boxTrapActive() ? "Laid" : "Dropped";
    }

    public int getSecondaryStatValue() {
        return this.boxTrapActive() ? this.boxTrap.getLaid() : this.birdEngine.getDropped();
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public int getXpGained() {
        if (this.startXp < 0) {
            return 0;
        }
        return Math.max(0, Skills.getExperience(Skill.HUNTER) - this.startXp);
    }
}

