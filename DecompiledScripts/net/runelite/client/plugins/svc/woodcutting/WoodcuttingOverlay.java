package net.runelite.client.plugins.svc.woodcutting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class WoodcuttingOverlay extends OverlayPanel {
    private final Client client;
    private final WoodcuttingPlugin plugin;
    private final WoodcuttingConfig config;

    WoodcuttingOverlay(Client client, WoodcuttingPlugin plugin, WoodcuttingConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(200, 110));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(
            TitleComponent.builder().text("0xWCBot").color(Color.CYAN).build()
        );
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("State", this.plugin.getState());
        String cycleLabel = this.config.inventoryMode() == WoodcuttingMode.DROP ? "Dropped" : "Banked";
        this.addLine(cycleLabel, String.valueOf(this.plugin.getDropCount()));
        this.addLine("Session", this.formatTime(this.plugin.getSessionStartMs()));
        return super.render(graphics);
    }

    private void addLine(String left, String right) {
        this.getPanelComponent().getChildren().add(
            LineComponent.builder().left(left + ":").right(right).build()
        );
    }

    private String formatTime(long startMs) {
        if (startMs <= 0L) {
            return "0s";
        }
        long totalSec = (System.currentTimeMillis() - startMs) / 1000L;
        long hours = totalSec / 3600L;
        long minutes = totalSec % 3600L / 60L;
        long seconds = totalSec % 60L;
        if (hours > 0L) {
            return String.format("%dh %02dm", hours, minutes);
        }
        if (minutes > 0L) {
            return String.format("%dm %02ds", minutes, seconds);
        }
        return seconds + "s";
    }
}
