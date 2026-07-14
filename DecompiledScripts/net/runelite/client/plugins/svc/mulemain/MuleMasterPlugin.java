/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.mulemain;

import com.google.inject.Binder;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.mulemain.MuleMasterConfig;
import net.runelite.client.plugins.svc.mulemain.MuleMasterOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Widgets;

@PluginDescriptor(name="<html><!--1--><nobr><font color='#FFB000'>\u25cf x64 Mule Master</font></nobr></html>", description="Accepts incoming trade requests and clicks Accept on both trade screens.", tags={"orion", "mule", "trade", "bot"}, enabledByDefault=false)
public class MuleMasterPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MuleMasterConfig config;
    private MuleMasterOverlay overlay;
    private String status = "Idle";
    private boolean loggedInLastTick = false;
    private long screen1OpenedMs = 0L;
    private volatile String pendingTraderName = null;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(MuleMasterConfig.class).toInstance((Object)((MuleMasterConfig)configManager.getConfig(MuleMasterConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new MuleMasterOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.status = "Starting";
        this.loggedInLastTick = false;
        this.screen1OpenedMs = 0L;
        this.pendingTraderName = null;
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.status = "Idle";
        this.pendingTraderName = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            this.loggedInLastTick = false;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String string;
        String string2 = chatMessage.getMessage();
        if (string2 == null) {
            return;
        }
        if (!string2.replaceAll("<[^>]*>", "").toLowerCase().contains("wishes to trade")) {
            return;
        }
        String string3 = chatMessage.getName();
        if (string3 != null && !string3.isEmpty() && !(string3 = string3.replaceAll("<[^>]*>", "").replaceAll("^[^a-zA-Z0-9]+", "").trim()).isEmpty()) {
            this.pendingTraderName = string3;
            return;
        }
        String string4 = string2.replaceAll("<[^>]*>", "");
        int n = string4.toLowerCase().indexOf("wishes to trade");
        if (n > 0 && !(string = string4.substring(0, n).trim().replaceAll("^[^a-zA-Z0-9]+", "").trim()).isEmpty()) {
            this.pendingTraderName = string;
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
            return 1200;
        }
        if (this.client.getLocalPlayer() == null) {
            return 600;
        }
        Widget widget = this.client.getWidget(334, 0);
        if (widget != null && !widget.isHidden()) {
            this.screen1OpenedMs = 0L;
            Widget widget2 = this.client.getWidget(334, 13);
            if (widget2 != null && !widget2.isHidden()) {
                this.status = "Accepting trade screen 2";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(widget2);
                return 1000;
            }
            this.status = "Screen 2 open \u2014 334:13 not found";
            return 800;
        }
        Widget widget3 = this.client.getWidget(335, 0);
        if (widget3 != null && !widget3.isHidden()) {
            long l = System.currentTimeMillis();
            if (this.screen1OpenedMs == 0L) {
                this.screen1OpenedMs = l;
                this.status = "Trade screen 1 \u2014 waiting for items...";
                return 600;
            }
            long l2 = l - this.screen1OpenedMs;
            long l3 = this.config.screen1AcceptDelayMs();
            if (l2 < l3) {
                this.status = "Trade screen 1 \u2014 accepting in " + (l3 - l2) / 1000L + "s";
                return 600;
            }
            Widget widget4 = this.client.getWidget(335, 10);
            if (widget4 != null && !widget4.isHidden()) {
                this.status = "Accepting trade screen 1";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(widget4);
            } else {
                this.status = "Screen 1 open \u2014 335:10 not found";
            }
            return 800;
        }
        this.screen1OpenedMs = 0L;
        Widget widget5 = this.findChatboxAcceptTradeWidget();
        if (widget5 != null) {
            String string = this.pendingTraderName;
            if (string == null || string.isEmpty()) {
                string = this.extractTradeRequesterName(widget5);
            }
            Player player = null;
            if (string != null && !string.isEmpty()) {
                player = this.findPlayerByName(string);
            }
            if (player != null) {
                this.status = "Trading with " + player.getName();
                this.pendingTraderName = null;
                player.interact("Trade with");
                return 1200;
            }
            if (string != null && !string.isEmpty()) {
                this.status = "Trade from " + string + " \u2014 not found nearby";
            } else {
                this.status = "Accepting trade (unknown player)";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(widget5);
            }
            return 1200;
        }
        this.pendingTraderName = null;
        this.status = "Waiting for trade request...";
        return 600;
    }

    public String getStatus() {
        return this.status;
    }

    public String getPendingTrader() {
        return this.pendingTraderName != null ? this.pendingTraderName : "n/a";
    }

    private Widget findChatboxAcceptTradeWidget() {
        List<Widget> list = Widgets.getAll(162, widget -> {
            String[] stringArray = widget.getActions();
            if (stringArray == null) {
                return false;
            }
            for (String string : stringArray) {
                if (string == null || !string.toLowerCase().startsWith("accept trade")) continue;
                return true;
            }
            return false;
        });
        return list.isEmpty() ? null : list.get(0);
    }

    private String extractTradeRequesterName(Widget widget2) {
        Object object;
        String string;
        String[] stringArray = widget2.getActions();
        String string2 = string = stringArray != null && stringArray[0] != null ? stringArray[0].trim() : "";
        if (string.length() > "Accept trade ".length()) {
            object = string.substring("Accept trade ".length()).trim();
            if (!((String)(object = ((String)object).replaceAll("<[^>]*>", "").replaceAll("^[^a-zA-Z0-9]+", "").trim())).isEmpty()) {
                return object;
            }
        }
        object = Widgets.getAll(162, widget -> {
            String string = widget.getText();
            return string != null && string.toLowerCase().contains("wishes to trade");
        });
        Iterator<Widget> iterator = object.iterator();
        while (iterator.hasNext()) {
            String string3;
            Widget widget3 = iterator.next();
            String string4 = widget3.getText().replaceAll("<[^>]*>", "");
            int n = string4.toLowerCase().indexOf("wishes to trade");
            if (n <= 0 || (string3 = string4.substring(0, n).trim().replaceAll("^[^a-zA-Z0-9]+", "").trim()).isEmpty()) continue;
            return string3;
        }
        return "";
    }

    private Player findPlayerByName(String string) {
        String string2 = string.replaceAll("<[^>]*>", "").replaceAll("^[^a-zA-Z0-9]+", "").trim();
        String string3 = string2.toLowerCase();
        if (string3.isEmpty()) {
            return null;
        }
        Player player2 = Players.getNearest(string2);
        if (player2 != null) {
            return player2;
        }
        player2 = Players.getNearest(player -> {
            if (player == null || player.getName() == null) {
                return false;
            }
            return player.getName().replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim().equalsIgnoreCase(string2);
        });
        if (player2 != null) {
            return player2;
        }
        player2 = Players.getNearest(player -> {
            if (player == null || player.getName() == null) {
                return false;
            }
            return player.getName().replaceAll("<[^>]*>", "").replaceAll("^[^a-zA-Z0-9]+", "").trim().toLowerCase().contains(string3);
        });
        if (player2 != null) {
            return player2;
        }
        int n = Integer.MAX_VALUE;
        WorldPoint worldPoint = this.client.getLocalPlayer().getWorldLocation();
        for (Player player3 : this.client.getPlayers()) {
            int n2;
            String string4;
            if (player3 == null || player3.getName() == null || !(string4 = player3.getName().replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim()).equalsIgnoreCase(string2) && !string4.toLowerCase().contains(string3) || (n2 = player3.getWorldLocation().distanceTo(worldPoint)) >= n) continue;
            n = n2;
            player2 = player3;
        }
        return player2;
    }
}

