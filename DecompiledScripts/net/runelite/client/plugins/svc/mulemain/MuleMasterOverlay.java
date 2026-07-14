/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.mulemain;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.svc.mulemain.MuleMasterConfig;
import net.runelite.client.plugins.svc.mulemain.MuleMasterPlugin;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class MuleMasterOverlay
extends OverlayPanel {
    private final Client client;
    private final MuleMasterPlugin plugin;
    private final MuleMasterConfig config;

    MuleMasterOverlay(Client client, MuleMasterPlugin muleMasterPlugin, MuleMasterConfig muleMasterConfig) {
        this.client = client;
        this.plugin = muleMasterPlugin;
        this.config = muleMasterConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setLayer(OverlayLayer.ALWAYS_ON_TOP);
        this.setPreferredSize(new Dimension(220, 90));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Mule Master").color(Color.YELLOW).build());
        this.addLine("Status", this.plugin.getStatus());
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }
}

