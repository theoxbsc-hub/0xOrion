/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.diagnostics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.plugins.svc.diagnostics.DiagnosticsConfig;
import net.runelite.client.plugins.svc.diagnostics.DiagnosticsPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class DiagnosticsOverlay
extends OverlayPanel {
    private final DiagnosticsPlugin plugin;
    private final DiagnosticsConfig config;

    DiagnosticsOverlay(DiagnosticsPlugin diagnosticsPlugin, DiagnosticsConfig diagnosticsConfig) {
        this.plugin = diagnosticsPlugin;
        this.config = diagnosticsConfig;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(232, 160));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("x64 Diagnostics  \u25cf REC").color(Color.RED).build());
        this.addLine("Recording", this.plugin.elapsedClock());
        this.addLine("Watching", this.plugin.watchingSummary());
        this.addLine("Captured", this.plugin.recordedEvents() + " events");
        int n = this.plugin.recordedErrors();
        this.getPanelComponent().getChildren().add(LineComponent.builder().left("Errors seen:").right(String.valueOf(n)).rightColor(n > 0 ? Color.RED : Color.GREEN).build());
        this.getPanelComponent().getChildren().add(LineComponent.builder().left("Turn OFF to save report").leftColor(Color.YELLOW).build());
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }
}

