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
package net.runelite.client.plugins.svc.jad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.jad.JadConfig;
import net.runelite.client.plugins.svc.jad.JadPhase;
import net.runelite.client.plugins.svc.jad.JadPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class JadOverlay
extends OverlayPanel {
    private final Client client;
    private final JadPlugin plugin;
    private final JadConfig config;

    JadOverlay(Client client, JadPlugin jadPlugin, JadConfig jadConfig) {
        this.client = client;
        this.plugin = jadPlugin;
        this.config = jadConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(210, 185));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        JadPhase jadPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Jad Bot").color(this.phaseColor(jadPhase)).build());
        this.addLine("Phase", jadPhase.toString());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Prayer", this.plugin.getActivePrayerName());
        this.addLine("Healers", this.plugin.getHealersOnJad() + " on Jad / " + this.plugin.getHealersAlive() + " alive");
        int n = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n2 = this.client.getRealSkillLevel(Skill.PRAYER);
        this.addLine("Prayer Pts", n + " / " + n2);
        int n3 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n4 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        this.addLine("HP", n3 + " / " + n4);
        int n5 = this.plugin.getJadHealthRatio();
        if (n5 >= 0) {
            this.addLine("Jad HP", n5 + "%");
        }
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private Color phaseColor(JadPhase jadPhase) {
        switch (jadPhase) {
            case FIGHTING: {
                return Color.GREEN;
            }
            case COMPLETE: {
                return Color.CYAN;
            }
            case ENTERING: {
                return Color.YELLOW;
            }
        }
        return Color.WHITE;
    }
}

