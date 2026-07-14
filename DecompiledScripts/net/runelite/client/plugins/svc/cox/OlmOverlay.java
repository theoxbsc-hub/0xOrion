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
package net.runelite.client.plugins.svc.cox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.cox.OlmConfig;
import net.runelite.client.plugins.svc.cox.OlmHeadStrategy;
import net.runelite.client.plugins.svc.cox.OlmPhase;
import net.runelite.client.plugins.svc.cox.OlmPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class OlmOverlay
extends OverlayPanel {
    private final Client client;
    private final OlmPlugin plugin;
    private final OlmConfig config;

    OlmOverlay(Client client, OlmPlugin olmPlugin, OlmConfig olmConfig) {
        this.client = client;
        this.plugin = olmPlugin;
        this.config = olmConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(210, 235));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        OlmPhase olmPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion CoX \u2014 Olm").color(this.phaseColor(olmPhase)).build());
        this.addLine("Phase", olmPhase.toString());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Target", this.plugin.getCurrentTarget());
        if (olmPhase == OlmPhase.COMBAT) {
            this.addLine("Reason", this.plugin.getTargetReason());
            this.addLine("Mage HP", this.plugin.getMageHandHp());
            this.addLine("Melee HP", this.plugin.getMeleeHandHp());
            this.addLine("Head HP", this.plugin.getHeadHp());
        }
        this.addLine("Mechanic", this.plugin.getActiveMechanic());
        this.addLine("Elemental", this.plugin.getElemental());
        if (this.config.headStrategy() == OlmHeadStrategy.AUTO) {
            this.addLine("Head dmg?", this.plugin.getHeadDamageable());
        }
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        this.addLine("HP", n + " / " + n2);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n4 = this.client.getRealSkillLevel(Skill.PRAYER);
        this.addLine("Prayer", n3 + " / " + n4);
        this.addLine("Kills", String.valueOf(this.plugin.getKillCount()));
        this.addLine("Runtime", this.formatRuntime(this.plugin.getStartTime()));
        if (this.plugin.hasInertMechanic()) {
            this.getPanelComponent().getChildren().add(LineComponent.builder().left("\u26a0 some dodges inert").right("(IDs unmapped)").leftColor(Color.ORANGE).rightColor(Color.ORANGE).build());
        }
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private Color phaseColor(OlmPhase olmPhase) {
        switch (olmPhase) {
            case COMBAT: {
                return Color.GREEN;
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

