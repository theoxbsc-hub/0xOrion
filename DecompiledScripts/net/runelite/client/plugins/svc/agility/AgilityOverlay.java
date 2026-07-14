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
package net.runelite.client.plugins.svc.agility;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.svc.agility.AgilityConfig;
import net.runelite.client.plugins.svc.agility.AgilityCourse;
import net.runelite.client.plugins.svc.agility.AgilityObstacle;
import net.runelite.client.plugins.svc.agility.AgilityPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class AgilityOverlay
extends OverlayPanel {
    private final Client client;
    private final AgilityPlugin plugin;
    private final AgilityConfig config;

    AgilityOverlay(Client client, AgilityPlugin agilityPlugin, AgilityConfig agilityConfig) {
        this.client = client;
        this.plugin = agilityPlugin;
        this.config = agilityConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(220, 150));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Agility").color(Color.GREEN).build());
        AgilityCourse agilityCourse = this.plugin.getRunningCourse();
        this.addLine("Course", agilityCourse != null ? agilityCourse.displayName() : "\u2014");
        if (agilityCourse != null) {
            int n = this.plugin.getCurrentIndex();
            if (n < 0 || n >= agilityCourse.size()) {
                n = 0;
            }
            AgilityObstacle agilityObstacle = agilityCourse.obstacles().get(n);
            this.addLine("Obstacle", n + 1 + "/" + agilityCourse.size() + " " + agilityObstacle.label());
        }
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Laps", String.valueOf(this.plugin.getLaps()));
        this.addLine("Obstacles", String.valueOf(this.plugin.getObstaclesDone()));
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

