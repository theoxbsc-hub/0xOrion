package net.runelite.client.plugins.ox.staffalert;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.client.plugins.ox.core.OxLoginGate;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class StaffAlertOverlay extends OverlayPanel {
    private static final Color PURPLE = new Color(0xB44DFF);
    private static final Color OK = new Color(80, 200, 120);
    private static final Color BAD = new Color(255, 90, 90);
    private static final Color WARN = new Color(255, 200, 60);
    private static final Color LABEL = new Color(190, 190, 200);
    private static final Color DIM = new Color(130, 130, 145);

    private final Client client;
    private final StaffAlertPlugin plugin;
    private final StaffAlertConfig config;

    StaffAlertOverlay(Client client, StaffAlertPlugin plugin, StaffAlertConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(260, 200));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!this.config.showOverlay()) {
            return null;
        }

        this.getPanelComponent().getChildren().add(
            TitleComponent.builder().text("0x STAFF ALERT").color(PURPLE).build()
        );

        boolean armed = this.config.enabled()
            && (this.plugin.watchedCount() > 0 || this.config.proximityPanic());
        addLine("Status", armed ? "ARMED" : "IDLE", LABEL, armed ? OK : WARN);
        addLine("Watchlist", String.valueOf(this.plugin.watchedCount()) + " names", LABEL, LABEL);
        addLine("Reactions", this.config.enabled() ? "ON" : "OFF", LABEL,
            this.config.enabled() ? OK : BAD);
        addLine("AFK ignore", this.config.ignoreAtAfkZones() ? "ON" : "OFF", LABEL, DIM);
        addLine("Home ignore", this.config.ignoreAtHome() ? "ON" : "OFF", LABEL, DIM);
        addLine("Event", this.plugin.getLastEvent(), LABEL, LABEL);
        addLine("Last", this.plugin.getLastTarget(), LABEL, DIM);

        if (OxLoginGate.isBlocked()) {
            long sec = OxLoginGate.remainingSeconds();
            String left = sec >= 60 ? (sec / 60) + "m " + (sec % 60) + "s" : sec + "s";
            addLine("Offline gate", left + " left", LABEL, BAD);
            addLine("Reason", OxLoginGate.getReason(), LABEL, WARN);
        } else {
            addLine("Offline gate", "open (AutoLogin OK)", LABEL, OK);
        }

        if (this.plugin.getLastCooldownMinutes() > 0) {
            addLine("Last cooldown", this.plugin.getLastCooldownMinutes() + " min", LABEL, DIM);
        }

        if (this.config.showNearby()) {
            List<StaffScanner.NearbyPlayer> near = this.plugin.nearbyPlayers();
            this.getPanelComponent().getChildren().add(
                LineComponent.builder().left("--- nearby ---").leftColor(DIM).build()
            );
            if (near.isEmpty()) {
                addLine("", "(none)", DIM, DIM);
            } else {
                for (StaffScanner.NearbyPlayer p : near) {
                    Color c = p.watched ? BAD : (p.friendly ? OK : LABEL);
                    String tag = p.watched ? " !!" : (p.friendly ? " f" : "");
                    addLine(p.name + tag, p.distance + "t", LABEL, c);
                }
            }
        }

        return super.render(graphics);
    }

    private void addLine(String left, String right, Color leftColor, Color rightColor) {
        this.getPanelComponent().getChildren().add(
            LineComponent.builder()
                .left(left)
                .right(right)
                .leftColor(leftColor)
                .rightColor(rightColor)
                .build()
        );
    }
}
