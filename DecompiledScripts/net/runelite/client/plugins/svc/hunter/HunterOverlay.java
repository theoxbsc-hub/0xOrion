/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.hunter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.svc.hunter.HunterConfig;
import net.runelite.client.plugins.svc.hunter.HunterPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class HunterOverlay
extends OverlayPanel {
    private final Client client;
    private final HunterPlugin plugin;
    private final HunterConfig config;

    HunterOverlay(Client client, HunterPlugin hunterPlugin, HunterConfig hunterConfig) {
        this.client = client;
        this.plugin = hunterPlugin;
        this.config = hunterConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(210, 140));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("x64 Hunter Bot").color(Color.CYAN).build());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Method", this.config.method().toString());
        this.addLine(this.plugin.getTrapLabel(), this.plugin.getActiveTraps() + "/" + this.plugin.getEffectiveMaxTraps());
        this.addLine(this.plugin.getPrimaryStatLabel(), String.valueOf(this.plugin.getPrimaryStatValue()));
        this.addLine(this.plugin.getSecondaryStatLabel(), String.valueOf(this.plugin.getSecondaryStatValue()));
        this.addLine("XP gained", this.formatNumber(this.plugin.getXpGained()));
        this.addLine("Session", this.formatTime(this.plugin.getSessionStartMs()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private String formatNumber(int n) {
        if (n >= 1000000) {
            return String.format("%.1fM", (double)n / 1000000.0);
        }
        if (n >= 1000) {
            return String.format("%.1fK", (double)n / 1000.0);
        }
        return String.valueOf(n);
    }

    private String formatTime(long l) {
        if (l <= 0L) {
            return "0s";
        }
        long l2 = (System.currentTimeMillis() - l) / 1000L;
        long l3 = l2 / 3600L;
        long l4 = l2 % 3600L / 60L;
        long l5 = l2 % 60L;
        if (l3 > 0L) {
            return String.format("%dh %02dm", l3, l4);
        }
        if (l4 > 0L) {
            return String.format("%dm %02ds", l4, l5);
        }
        return l5 + "s";
    }
}

