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
package net.runelite.client.plugins.svc.tob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.tob.TobConfig;
import net.runelite.client.plugins.svc.tob.TobPhase;
import net.runelite.client.plugins.svc.tob.TobPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class TobOverlay
extends OverlayPanel {
    private final Client client;
    private final TobPlugin plugin;
    private final TobConfig config;

    TobOverlay(Client client, TobPlugin tobPlugin, TobConfig tobConfig) {
        this.client = client;
        this.plugin = tobPlugin;
        this.config = tobConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(220, 170));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        TobPhase tobPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion ToB \u2014 Verzik").color(this.phaseColor(tobPhase)).build());
        this.addLine("Phase", tobPhase.toString());
        this.addLine("Method", this.plugin.getActiveMethod().toString());
        this.addLine("Status", this.plugin.getStatus());
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        this.addLine("HP", n + " / " + n2 + (this.plugin.isStunned() ? "  [STUNNED]" : ""));
        this.addLine("Prayer", n3 + (this.plugin.isPrayerViaWidgets() ? "  [widget mode]" : ""));
        this.addLine("Overhead", this.plugin.getOverheadReadout() + (this.plugin.arePrayersProven() ? " \u2713" : " ?"));
        String string = this.plugin.isDodgeFutile() ? "disabled (futile)" : (this.config.dodgeBombs() ? String.valueOf(this.plugin.getDodgesDone()) : "off");
        this.addLine("Dodges", string);
        String string2 = this.plugin.getLearnerReadout();
        if (!"none".equals(string2) && !"-".equals(string2)) {
            this.addLine("Learned", string2);
        }
        this.addLine("Worst hit", String.valueOf(this.plugin.getWorstHitSeen()));
        this.addLine("Spec", this.plugin.getSpecEnergyPercent() + "%");
        this.addLine("Kills", String.valueOf(this.plugin.getKillCount()));
        this.addLine("Deaths", String.valueOf(this.plugin.getDeathCount()));
        this.addLine("Runtime", this.formatRuntime(this.plugin.getStartTime()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private Color phaseColor(TobPhase tobPhase) {
        switch (tobPhase) {
            case P3_ENRAGED: {
                return Color.RED;
            }
            case P3_FLYING: {
                return Color.YELLOW;
            }
            case P2_SPIDER: 
            case P1_THRONE: {
                return Color.GREEN;
            }
            case COMPLETE: {
                return Color.CYAN;
            }
            case DEAD: {
                return Color.CYAN;
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

