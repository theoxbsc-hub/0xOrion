/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Skill
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.saradomin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.saradomin.SaradominConfig;
import net.runelite.client.plugins.svc.saradomin.SaradominPhase;
import net.runelite.client.plugins.svc.saradomin.SaradominPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class SaradominOverlay
extends OverlayPanel {
    private final Client client;
    private final SaradominPlugin plugin;
    private final SaradominConfig config;

    SaradominOverlay(Client client, SaradominPlugin saradominPlugin, SaradominConfig saradominConfig) {
        this.client = client;
        this.plugin = saradominPlugin;
        this.config = saradominConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(200, 150));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        SaradominPhase saradominPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Saradomin").color(this.phaseColor(saradominPhase)).build());
        this.addLine("Phase", saradominPhase.toString());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Kills", String.valueOf(this.plugin.getKillCount()));
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        this.addLine("HP", n + " / " + n2);
        this.addLine("Prayer", String.valueOf(n3));
        this.addLine("Runtime", this.formatRuntime(this.plugin.getStartTime()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private Color phaseColor(SaradominPhase saradominPhase) {
        switch (saradominPhase) {
            case FIGHTING: {
                return Color.GREEN;
            }
            case LOOTING: {
                return Color.YELLOW;
            }
            case USING_ALTAR: {
                return Color.CYAN;
            }
            case RESUPPLYING: {
                return Color.ORANGE;
            }
        }
        return Color.WHITE;
    }

    private String formatRuntime(long l) {
        if (l == 0L) {
            return "00:00:00";
        }
        long l2 = System.currentTimeMillis() - l;
        long l3 = l2 / 1000L % 60L;
        long l4 = l2 / 60000L % 60L;
        long l5 = l2 / 3600000L;
        return String.format("%02d:%02d:%02d", l5, l4, l3);
    }
}

