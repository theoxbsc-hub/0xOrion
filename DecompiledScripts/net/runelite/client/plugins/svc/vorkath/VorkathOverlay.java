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
package net.runelite.client.plugins.svc.vorkath;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.vorkath.VorkathConfig;
import net.runelite.client.plugins.svc.vorkath.VorkathPhase;
import net.runelite.client.plugins.svc.vorkath.VorkathPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class VorkathOverlay
extends OverlayPanel {
    private final Client client;
    private final VorkathPlugin plugin;
    private final VorkathConfig config;

    VorkathOverlay(Client client, VorkathPlugin vorkathPlugin, VorkathConfig vorkathConfig) {
        this.client = client;
        this.plugin = vorkathPlugin;
        this.config = vorkathConfig;
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
        VorkathPhase vorkathPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Vorkath").color(this.phaseColor(vorkathPhase)).build());
        this.addLine("Phase", vorkathPhase.toString());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Attacks", this.plugin.getAttackCount() + " / 6");
        this.addLine("Kills", String.valueOf(this.plugin.getKillCount()));
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        this.addLine("HP", n + " / " + n2);
        this.addLine("Prayer", n3 + (this.config.prayFlick() ? " [flick]" : ""));
        this.addLine("Runtime", this.formatRuntime(this.plugin.getStartTime()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }

    private Color phaseColor(VorkathPhase vorkathPhase) {
        switch (vorkathPhase) {
            case ZOMBIE_SPAWN: {
                return Color.RED;
            }
            case ACID_PHASE: {
                return Color.YELLOW;
            }
            case FIGHTING: {
                return Color.GREEN;
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

