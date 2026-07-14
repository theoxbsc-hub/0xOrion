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
package net.runelite.client.plugins.svc.inferno;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.inferno.InfernoConfig;
import net.runelite.client.plugins.svc.inferno.InfernoPhase;
import net.runelite.client.plugins.svc.inferno.InfernoPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class InfernoOverlay
extends OverlayPanel {
    private final Client client;
    private final InfernoPlugin plugin;
    private final InfernoConfig config;

    InfernoOverlay(Client client, InfernoPlugin infernoPlugin, InfernoConfig infernoConfig) {
        this.client = client;
        this.plugin = infernoPlugin;
        this.config = infernoConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(230, 210));
    }

    public Dimension render(Graphics2D graphics2D) {
        int n;
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        InfernoPhase infernoPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Inferno").color(this.phaseColor(infernoPhase)).build());
        this.addLine("Phase", infernoPhase.toString());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Wave", this.plugin.getCurrentWave() + " (best " + this.plugin.getHighestWave() + ")");
        this.addLine("Overhead", this.plugin.getCurrentOverheadName());
        if (infernoPhase == InfernoPhase.WAVES) {
            this.addLine("Threat", this.plugin.getDominantCaste().toString());
        }
        if (infernoPhase == InfernoPhase.JAD && (n = this.plugin.getJadCount()) > 1) {
            this.addLine("Jads", String.valueOf(n));
        }
        if (infernoPhase == InfernoPhase.JAD || infernoPhase == InfernoPhase.ZUK) {
            this.addLine("Next hit", this.plugin.getNextHitInfo());
        }
        if ((infernoPhase == InfernoPhase.JAD || infernoPhase == InfernoPhase.ZUK) && this.plugin.getHealersUp() > 0) {
            this.addLine("Healers", String.valueOf(this.plugin.getHealersUp()));
        }
        n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        this.addLine("HP", n + " / " + n2);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n4 = this.client.getRealSkillLevel(Skill.PRAYER);
        this.addLine("Prayer", n3 + " / " + n4);
        this.addLine("Brews", this.plugin.getBrewDoses() + " since restore");
        this.addLine("Boss kills", String.valueOf(this.plugin.getBossKills()));
        this.addLine("Runtime", this.formatRuntime(this.plugin.getStartTime()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private Color phaseColor(InfernoPhase infernoPhase) {
        switch (infernoPhase) {
            case WAVES: {
                return Color.LIGHT_GRAY;
            }
            case JAD: {
                return new Color(230, 120, 40);
            }
            case ZUK: {
                return new Color(200, 50, 50);
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

