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
package net.runelite.client.plugins.svc.nex;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.nex.NexConfig;
import net.runelite.client.plugins.svc.nex.NexPhase;
import net.runelite.client.plugins.svc.nex.NexPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class NexOverlay
extends OverlayPanel {
    private final Client client;
    private final NexPlugin plugin;
    private final NexConfig config;

    NexOverlay(Client client, NexPlugin nexPlugin, NexConfig nexConfig) {
        this.client = client;
        this.plugin = nexPlugin;
        this.config = nexConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(220, 190));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        NexPhase nexPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Nex").color(this.phaseColor(nexPhase)).build());
        this.addLine("Phase", nexPhase.toString());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Overhead", this.plugin.getCurrentOverheadName());
        if (this.plugin.isMustKillMinion()) {
            this.addLine("Target", "MINION");
        }
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        this.addLine("HP", n + " / " + n2);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n4 = this.client.getRealSkillLevel(Skill.PRAYER);
        this.addLine("Prayer", n3 + " / " + n4);
        this.addLine("Brews", this.plugin.getBrewDoses() + " since restore");
        this.addLine("Kills", String.valueOf(this.plugin.getKillCount()));
        this.addLine("Runtime", this.formatRuntime(this.plugin.getStartTime()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private Color phaseColor(NexPhase nexPhase) {
        switch (nexPhase) {
            case SMOKE: {
                return Color.LIGHT_GRAY;
            }
            case SHADOW: {
                return new Color(150, 110, 200);
            }
            case BLOOD: {
                return new Color(200, 60, 60);
            }
            case ICE: {
                return new Color(120, 190, 230);
            }
            case ZAROS: {
                return Color.ORANGE;
            }
            case LOOTING: {
                return Color.CYAN;
            }
            case DONE: {
                return Color.GRAY;
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

