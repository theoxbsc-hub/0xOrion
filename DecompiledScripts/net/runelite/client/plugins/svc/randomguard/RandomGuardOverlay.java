/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.randomguard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.plugins.svc.randomguard.GuardWebhook;
import net.runelite.client.plugins.svc.randomguard.RandomGuardConfig;
import net.runelite.client.plugins.svc.randomguard.RandomGuardPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class RandomGuardOverlay
extends OverlayPanel {
    private static final Color TITLE = new Color(80, 200, 255);
    private static final Color LABEL = new Color(200, 200, 200);
    private static final Color OK = new Color(80, 220, 120);
    private static final Color WARN = new Color(255, 200, 60);
    private static final Color ALERT = new Color(255, 90, 90);
    private static final Color DIM = new Color(140, 140, 140);
    private final RandomGuardPlugin plugin;
    private final RandomGuardConfig config;

    RandomGuardOverlay(RandomGuardPlugin randomGuardPlugin, RandomGuardConfig randomGuardConfig) {
        this.plugin = randomGuardPlugin;
        this.config = randomGuardConfig;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(260, 150));
    }

    public Dimension render(Graphics2D graphics2D) {
        String string;
        long l;
        if (!this.config.showOverlay()) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("RANDOM EVENT GUARD").color(TITLE).build());
        boolean bl = this.plugin.isHandling();
        this.addLine("Status:", bl ? "HANDLING" : "Watching", LABEL, bl ? ALERT : OK);
        this.addLine("Webhook:", GuardWebhook.isConfigured() ? "Admin webhook" : "NOT SET", LABEL, GuardWebhook.isConfigured() ? OK : ALERT);
        this.addLine("Events:", String.valueOf(this.plugin.getEventCount()), LABEL, DIM);
        if (bl) {
            this.addLine("Phase:", this.plugin.getPhase(), LABEL, WARN);
            l = this.plugin.getDiagnosticSecondsLeft();
            if (l >= 0L) {
                this.addLine("Recording:", l + "s left", LABEL, WARN);
            }
            if ((string = this.plugin.getEventNpcTag()) != null) {
                this.addLine("Event NPC:", string, LABEL, ALERT);
            }
        }
        l = this.plugin.getCooldownSeconds();
        if (!bl && l > 0L) {
            this.addLine("Cooldown:", l + "s", LABEL, WARN);
        }
        if ((string = this.plugin.getLastEventText()) != null) {
            this.addLine("Last:", string, LABEL, DIM);
        }
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2, Color color, Color color2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string).leftColor(color).right(string2).rightColor(color2).build());
    }
}

