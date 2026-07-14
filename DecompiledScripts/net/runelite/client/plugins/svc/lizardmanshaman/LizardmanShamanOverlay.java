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
package net.runelite.client.plugins.svc.lizardmanshaman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.lizardmanshaman.LizardmanShamanConfig;
import net.runelite.client.plugins.svc.lizardmanshaman.LizardmanShamanPlugin;
import net.runelite.client.plugins.svc.lizardmanshaman.ShamanPhase;
import net.runelite.client.plugins.svc.lizardmanshaman.ShamanResupplyState;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class LizardmanShamanOverlay
extends OverlayPanel {
    private final Client client;
    private final LizardmanShamanPlugin plugin;
    private final LizardmanShamanConfig config;

    LizardmanShamanOverlay(Client client, LizardmanShamanPlugin lizardmanShamanPlugin, LizardmanShamanConfig lizardmanShamanConfig) {
        this.client = client;
        this.plugin = lizardmanShamanPlugin;
        this.config = lizardmanShamanConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(210, 180));
    }

    public Dimension render(Graphics2D graphics2D) {
        HumanInput humanInput;
        long l;
        ShamanResupplyState shamanResupplyState;
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        ShamanPhase shamanPhase = this.plugin.getPhase();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Lizardman Shaman").color(this.phaseColor(shamanPhase)).build());
        this.addLine("Phase", this.formatPhase(shamanPhase), this.phaseColor(shamanPhase));
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Kills", String.valueOf(this.plugin.getKillCount()));
        if (this.plugin.getDeathCount() > 0) {
            this.addLine("Deaths", String.valueOf(this.plugin.getDeathCount()), Color.RED);
        }
        if ((shamanResupplyState = this.plugin.getResupplyState()) != null) {
            this.addLine("Route", shamanResupplyState.name(), Color.CYAN);
        }
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n4 = this.client.getRealSkillLevel(Skill.PRAYER);
        int n5 = this.client.getBoostedSkillLevel(Skill.RANGED);
        int n6 = this.client.getRealSkillLevel(Skill.RANGED);
        int n7 = this.client.getEnergy() / 100;
        this.addLine("HP", n + " / " + n2, this.hpColor(n, n2));
        this.addLine("Prayer", n3 + " / " + n4, this.prayerColor(n3, n4));
        this.addLine("Ranged", (String)(n5 == n6 ? String.valueOf(n5) : n5 + " (+" + (n5 - n6) + ")"), n5 > n6 ? Color.CYAN : Color.WHITE);
        this.addLine("Run", n7 + "%", n7 < 20 ? Color.ORANGE : Color.WHITE);
        this.addLine("Runtime", this.formatRuntime(this.plugin.getStartTime()));
        if (this.plugin.getKillCount() > 0 && this.plugin.getStartTime() > 0L && (l = (System.currentTimeMillis() - this.plugin.getStartTime()) / 60000L) > 0L) {
            double d = (double)this.plugin.getKillCount() / (double)l * 60.0;
            this.addLine("K/hr", String.format("%.1f", d));
        }
        if (this.config.enableHighAlch() && this.plugin.getAlchProfit() > 0L) {
            this.addLine("Alch GP", this.formatGp(this.plugin.getAlchProfit()), new Color(255, 215, 0));
        }
        if (this.config.microBreaks()) {
            l = System.currentTimeMillis();
            long l2 = this.plugin.getBreakUntilMs();
            if (l2 > l) {
                this.addLine("Break", (l2 - l) / 1000L + "s left", Color.YELLOW);
            } else if (this.plugin.getNextBreakAtMs() > l) {
                this.addLine("Next break", this.formatMmSs(this.plugin.getNextBreakAtMs() - l), Color.GRAY);
            }
        }
        if ((humanInput = this.plugin.getInput()) != null) {
            this.addLine("Input", humanInput.humanizedCount() + " real / " + humanInput.fallbackCount() + " pkt", humanInput.fallbackCount() > humanInput.humanizedCount() ? Color.ORANGE : Color.GRAY);
        }
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.addLine(string, string2, Color.WHITE);
    }

    private void addLine(String string, String string2, Color color) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).rightColor(color).build());
    }

    private Color phaseColor(ShamanPhase shamanPhase) {
        switch (shamanPhase) {
            case DODGING_JUMP: {
                return Color.RED;
            }
            case FIGHTING: {
                return Color.GREEN;
            }
            case RESUPPLYING: {
                return Color.CYAN;
            }
            case DEAD: {
                return Color.RED;
            }
            case STUCK: {
                return Color.RED;
            }
            case BREAK: {
                return Color.YELLOW;
            }
        }
        return Color.WHITE;
    }

    private String formatPhase(ShamanPhase shamanPhase) {
        switch (shamanPhase) {
            case DODGING_JUMP: {
                return "DODGING JUMP";
            }
            case FIGHTING: {
                return "Fighting";
            }
            case RESUPPLYING: {
                return "Resupplying";
            }
            case DEAD: {
                return "DEAD";
            }
            case STUCK: {
                return "STUCK";
            }
            case BREAK: {
                return "On break";
            }
        }
        return "Idle";
    }

    private Color hpColor(int n, int n2) {
        if (n2 == 0) {
            return Color.WHITE;
        }
        int n3 = n * 100 / n2;
        if (n3 < 30) {
            return Color.RED;
        }
        if (n3 < 60) {
            return Color.ORANGE;
        }
        return Color.GREEN;
    }

    private Color prayerColor(int n, int n2) {
        if (n2 == 0) {
            return Color.WHITE;
        }
        int n3 = n * 100 / n2;
        if (n3 < 20) {
            return Color.RED;
        }
        if (n3 < 40) {
            return Color.ORANGE;
        }
        return new Color(160, 220, 255);
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

    private String formatMmSs(long l) {
        long l2 = Math.max(0L, l / 1000L);
        return String.format("%d:%02d", l2 / 60L, l2 % 60L);
    }

    private String formatGp(long l) {
        if (l >= 1000000L) {
            return String.format("%.1fM", (double)l / 1000000.0);
        }
        if (l >= 1000L) {
            return String.format("%.1fK", (double)l / 1000.0);
        }
        return String.valueOf(l);
    }
}

