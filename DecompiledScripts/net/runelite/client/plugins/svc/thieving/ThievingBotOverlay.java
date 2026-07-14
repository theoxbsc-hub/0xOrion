/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.Skill
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.thieving;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.thieving.ThievingBotConfig;
import net.runelite.client.plugins.svc.thieving.ThievingBotPlugin;
import net.runelite.client.plugins.svc.thieving.ThievingState;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.unethicalite.api.items.Inventory;

class ThievingBotOverlay
extends OverlayPanel {
    private final Client client;
    private final ThievingBotPlugin plugin;
    private final ThievingBotConfig config;

    ThievingBotOverlay(Client client, ThievingBotPlugin thievingBotPlugin, ThievingBotConfig thievingBotConfig) {
        this.client = client;
        this.plugin = thievingBotPlugin;
        this.config = thievingBotConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setLayer(OverlayLayer.ALWAYS_ON_TOP);
        this.setPreferredSize(new Dimension(250, 250));
    }

    public Dimension render(Graphics2D graphics2D) {
        long l;
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Thieving Bot").color(Color.CYAN).build());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("State", this.plugin.getState().name());
        int n = this.client.getRealSkillLevel(Skill.THIEVING);
        this.addLine("Thieving", String.valueOf(n));
        this.addLine("Stall", this.plugin.getCurrentStallLabel() + " (req " + this.plugin.getCurrentRequiredLevel() + ")");
        if (this.plugin.isAdvancing()) {
            this.addLine("Progressing", "\u2192 " + this.plugin.getTargetStallLabel());
        }
        this.addLine("Steals", String.valueOf(this.plugin.getStealCount()));
        this.addLine("Sold", String.valueOf(this.plugin.getSellCount()));
        this.addLine("Session", this.formatTime(this.plugin.getSessionStartMs()));
        this.addLine("Blacklisted", this.plugin.getUnsellableCount() + " items");
        if (this.config.enableMuling()) {
            long l2;
            Item item2 = Inventory.getFirst(item -> item.getId() == 995);
            int n2 = item2 != null ? item2.getQuantity() : 0;
            this.addLine("GP", this.formatGp(n2));
            ThievingState thievingState = this.plugin.getState();
            if (thievingState == ThievingState.MULE_WALKING || thievingState == ThievingState.MULE_TRADING || thievingState == ThievingState.MULE_RETURNING) {
                this.addLine("Mule phase", this.plugin.getMulePhase().name());
            }
            if ((l2 = this.plugin.getLastMuleTimeMs()) > 0L && l2 != this.plugin.getSessionStartMs()) {
                long l3 = (System.currentTimeMillis() - l2) / 60000L;
                this.addLine("Last mule", l3 + "m ago");
            }
        }
        if ((l = this.plugin.getLastStallSeenMs()) > 0L) {
            long l4 = (System.currentTimeMillis() - l) / 1000L;
            this.addLine("Stall seen", l4 + "s ago");
        }
        if (this.config.enableSleepBreaks()) {
            long l5 = this.plugin.getSleepUntilMs();
            long l6 = this.plugin.getLastBreakTimeMs();
            if (l5 > 0L && System.currentTimeMillis() < l5) {
                long l7 = (l5 - System.currentTimeMillis()) / 1000L;
                this.addLine("Break ends", l7 + "s");
            } else if (l6 > 0L) {
                long l8 = (long)this.config.sleepAfterMinutes() - (System.currentTimeMillis() - l6) / 60000L;
                this.addLine("Next break", Math.max(0L, l8) + "m");
            }
        }
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private String formatGp(int n) {
        if (n >= 1000000) {
            return String.format("%.1fM", (double)n / 1000000.0);
        }
        if (n >= 1000) {
            return String.format("%.1fk", (double)n / 1000.0);
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

