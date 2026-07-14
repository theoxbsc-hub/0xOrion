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
package net.runelite.client.plugins.svc.mining;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.svc.mining.BarGoal;
import net.runelite.client.plugins.svc.mining.MiningConfig;
import net.runelite.client.plugins.svc.mining.MiningMode;
import net.runelite.client.plugins.svc.mining.MiningPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class MiningOverlay
extends OverlayPanel {
    private final Client client;
    private final MiningPlugin plugin;
    private final MiningConfig config;

    MiningOverlay(Client client, MiningPlugin miningPlugin, MiningConfig miningConfig) {
        this.client = client;
        this.plugin = miningPlugin;
        this.config = miningConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(200, 130));
    }

    public Dimension render(Graphics2D graphics2D) {
        String string;
        boolean bl;
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Mining Bot").color(Color.CYAN).build());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("State", this.plugin.getStateName());
        BarGoal barGoal = this.plugin.getBarGoal();
        boolean bl2 = bl = barGoal != null && barGoal.isActive();
        if (bl) {
            string = barGoal.target() > 0 ? " \u00d7" + String.format("%,d", barGoal.target()) : " (endless)";
            this.addLine("Bar goal", String.valueOf((Object)barGoal.bar()) + string);
            for (int i = 0; i < barGoal.ingredientCount(); ++i) {
                String string2 = String.format("%,d", barGoal.minedOf(i)) + (String)(barGoal.target() > 0 ? " / " + String.format("%,d", barGoal.requiredOf(i)) : "");
                this.addLine(barGoal.ingredientOre(i).toString(), string2);
            }
            String string3 = String.format("%,d", barGoal.barsGathered()) + (String)(barGoal.target() > 0 ? " / " + String.format("%,d", barGoal.target()) : "");
            this.addLine("Bars gathered", string3);
        } else {
            this.addLine("Ore", this.config.ore().toString());
        }
        string = !bl && this.config.miningMode() == MiningMode.DROP ? "Dropped" : "Banked";
        this.addLine(string, String.valueOf(this.plugin.getTripCount()));
        this.addLine("XP gained", String.format("%,d", this.plugin.getXpGained()));
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

