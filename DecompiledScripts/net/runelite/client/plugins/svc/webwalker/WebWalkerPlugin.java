/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.ClientToolbar
 *  net.runelite.client.ui.NavigationButton
 *  net.runelite.client.ui.PluginPanel
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.webwalker;

import com.google.inject.Binder;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.webwalker.WebWalkerConfig;
import net.runelite.client.plugins.svc.webwalker.WebWalkerOverlay;
import net.runelite.client.plugins.svc.webwalker.engine.CollisionMapLoader;
import net.runelite.client.plugins.svc.webwalker.engine.CompositeCollisionMap;
import net.runelite.client.plugins.svc.webwalker.engine.LiveCollisionMap;
import net.runelite.client.plugins.svc.webwalker.engine.RegionCollisionMap;
import net.runelite.client.plugins.svc.webwalker.ui.WebWalkerPanel;
import net.runelite.client.plugins.svc.webwalker.walker.OrionWebWalker;
import net.runelite.client.plugins.svc.webwalker.walker.PacketStepDispatcher;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;

@PluginDescriptor(name="Web Walker", description="Enter coordinates and walk there \u2014 standalone web-walking / pathfinding foundation", tags={"orion", "walk", "webwalk", "pathfinding", "travel", "navigation"}, enabledByDefault=false)
public class WebWalkerPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private WebWalkerConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientToolbar clientToolbar;
    private final PacketStepDispatcher dispatcher = new PacketStepDispatcher();
    private volatile OrionWebWalker walker;
    private volatile boolean collisionLoading;
    private volatile int collisionRegions = -1;
    private volatile WorldPoint target;
    private volatile boolean walking;
    private volatile String status = "Idle";
    private WebWalkerPanel panel;
    private NavigationButton navButton;
    private WebWalkerOverlay overlay;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(WebWalkerConfig.class).toInstance((Object)((WebWalkerConfig)configManager.getConfig(WebWalkerConfig.class)));
    }

    @Override
    protected void onStart() {
        this.overlay = new WebWalkerOverlay(this.client, this);
        this.overlayManager.add((Overlay)this.overlay);
        this.panel = new WebWalkerPanel(this);
        this.navButton = NavigationButton.builder().tooltip("Web Walker").icon(WebWalkerPlugin.buildIcon()).priority(6).panel((PluginPanel)this.panel).build();
        this.clientToolbar.addNavigation(this.navButton);
        this.target = new WorldPoint(this.config.destX(), this.config.destY(), this.config.destPlane());
    }

    @Override
    protected void onStop() {
        this.walking = false;
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
        }
        if (this.navButton != null) {
            this.clientToolbar.removeNavigation(this.navButton);
        }
        if (this.walker != null) {
            this.walker.reset();
        }
    }

    @Override
    protected int loop() {
        if (this.walker == null) {
            if (!this.collisionLoading) {
                this.collisionLoading = true;
                this.status = "Loading collision map\u2026";
            }
            RegionCollisionMap regionCollisionMap = CollisionMapLoader.loadBundled();
            this.collisionRegions = regionCollisionMap.regionCount();
            this.walker = new OrionWebWalker(new CompositeCollisionMap(regionCollisionMap, new LiveCollisionMap()), this.dispatcher);
            this.status = this.collisionRegions > 0 ? "Ready (" + this.collisionRegions + " regions)" : "Ready (no collision map \u2014 greedy mode)";
            this.applyConfig();
            return 200;
        }
        if (!this.walking) {
            return 400;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            this.status = "Waiting for login\u2026";
            return 800;
        }
        WorldPoint worldPoint = this.target;
        if (worldPoint == null) {
            this.walking = false;
            this.status = "No destination";
            return 400;
        }
        OrionWebWalker.Result result = this.walker.step(worldPoint);
        switch (result) {
            case ARRIVED: {
                this.status = "Arrived at " + WebWalkerPlugin.fmt(worldPoint);
                if (this.config.stopOnArrival()) {
                    this.walking = false;
                }
                return 400;
            }
            case STUCK: {
                this.status = "Stuck: " + this.walker.getStatus();
                this.walking = false;
                return 400;
            }
        }
        this.status = this.walker.getStatus();
        return 450;
    }

    public void startWalk(int n, int n2, int n3) {
        this.target = new WorldPoint(n, n2, n3);
        this.persistTarget(n, n2, n3);
        this.applyConfig();
        if (this.walker != null) {
            this.walker.reset();
        }
        this.status = "Walking to " + WebWalkerPlugin.fmt(this.target);
        this.walking = true;
    }

    public void stop() {
        this.walking = false;
        this.status = "Stopped";
        if (this.walker != null) {
            this.walker.reset();
        }
    }

    public WorldPoint currentTile() {
        try {
            return Players.getLocal() != null ? Players.getLocal().getWorldLocation() : null;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    public boolean isWalking() {
        return this.walking;
    }

    public String getStatus() {
        return this.status;
    }

    public OrionWebWalker getWalker() {
        return this.walker;
    }

    public WorldPoint getTarget() {
        return this.target;
    }

    public WebWalkerConfig getConfig() {
        return this.config;
    }

    private void applyConfig() {
        if (this.walker == null) {
            return;
        }
        this.walker.arriveRadius(this.config.arriveRadius()).walkStep(this.config.walkStep()).avoidWilderness(this.config.avoidWilderness()).useTransports(this.config.useTransports());
    }

    private void persistTarget(int n, int n2, int n3) {
        try {
            ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
            configManager.setConfiguration("orionWebWalker", "destX", (Object)n);
            configManager.setConfiguration("orionWebWalker", "destY", (Object)n2);
            configManager.setConfiguration("orionWebWalker", "destPlane", (Object)n3);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static String fmt(WorldPoint worldPoint) {
        return worldPoint == null ? "?" : worldPoint.getX() + ", " + worldPoint.getY() + ", " + worldPoint.getPlane();
    }

    private static BufferedImage buildIcon() {
        BufferedImage bufferedImage = new BufferedImage(16, 16, 2);
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(new Color(0, 150, 200));
        graphics.fillRect(0, 0, 16, 16);
        graphics.setColor(Color.WHITE);
        graphics.drawString("W", 4, 12);
        graphics.dispose();
        return bufferedImage;
    }
}

