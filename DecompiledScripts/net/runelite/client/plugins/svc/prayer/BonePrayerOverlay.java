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
package net.runelite.client.plugins.svc.prayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.svc.prayer.BonePrayerConfig;
import net.runelite.client.plugins.svc.prayer.BonePrayerPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class BonePrayerOverlay
extends OverlayPanel {
    private final Client client;
    private final BonePrayerPlugin plugin;
    private final BonePrayerConfig config;

    BonePrayerOverlay(Client client, BonePrayerPlugin bonePrayerPlugin, BonePrayerConfig bonePrayerConfig) {
        this.client = client;
        this.plugin = bonePrayerPlugin;
        this.config = bonePrayerConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(200, 140));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion AIO Prayer").color(Color.CYAN).build());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("State", this.plugin.getStateName());
        this.addLine("Bones held", String.valueOf(this.plugin.getBonesInInventory()));
        this.addLine("Bones offered", String.format("%,d", this.plugin.getBonesOffered()));
        this.addLine("Prayer lvl", String.valueOf(this.plugin.getPrayerLevel()));
        this.addLine("XP gained", String.format("%,d", this.plugin.getXpGained()));
        this.addLine("Bank trips", String.valueOf(this.plugin.getTrips()));
        this.addLine("Session", this.formatTime(this.plugin.getSessionStartMs()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
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

