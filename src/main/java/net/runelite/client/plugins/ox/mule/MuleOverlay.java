package net.runelite.client.plugins.ox.mule;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** Compact mule status card anchored to the bottom of the inventory. */
class MuleOverlay extends Overlay {
    private static final Color BG_TOP = new Color(42, 32, 52, 244);
    private static final Color BG_BOT = new Color(22, 18, 30, 248);
    private static final Color BORDER = new Color(120, 70, 180, 235);
    private static final Color PURPLE = new Color(0xB44DFF);
    private static final Color PURPLE_HI = new Color(0xD4A0FF);
    private static final Color CREAM = new Color(235, 230, 245);
    private static final Color DIM = new Color(160, 150, 180);
    private static final Color GREEN = new Color(120, 206, 110);
    private static final Color AMBER = new Color(236, 172, 72);
    private static final Color CYAN = new Color(110, 198, 214);

    private final Client client;
    private final MulePlugin plugin;
    private final MuleConfig config;

    private final Font fChip = new Font("SansSerif", Font.BOLD, 10);
    private final Font fTiny = new Font("SansSerif", Font.PLAIN, 10);
    private final Font fStat = new Font("SansSerif", Font.BOLD, 12);

    MuleOverlay(Client client, MulePlugin plugin, MuleConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle inv = inventoryBounds();
        if (inv != null) {
            drawInventoryPanel(g, inv);
        }
        return null;
    }

    private void drawInventoryPanel(Graphics2D g, Rectangle inv) {
        int w = inv.width - 4;
        int h = 54;
        int x = inv.x + 2;
        int y = inv.y + inv.height - h - 2;
        if (y < inv.y + 2) {
            y = inv.y + 2;
        }

        g.setPaint(new GradientPaint(x, y, BG_TOP, x, y + h, BG_BOT));
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1.4f));
        g.drawRoundRect(x, y, w - 1, h - 1, 12, 12);

        String state = safe(plugin.getState());
        text(g, "0xMU", x + 8, y + 14, PURPLE_HI, fChip);
        text(g, shortState(state), x + 48, y + 14, stateColor(state), fTiny);
        text(g, formatRuntime(plugin.getSessionStartMs()), x + w - 52, y + 14, DIM, fTiny);

        FontMetrics fm = g.getFontMetrics(fTiny);
        String status = ellipsize(fm, safe(plugin.getStatus()), w - 16);
        text(g, status, x + 8, y + 28, CREAM, fTiny);

        int tx = x + 8;
        tx = token(g, tx, y + 34, "TRADES", String.valueOf(plugin.getTradesAccepted()), GREEN);
        tx = token(g, tx, y + 34, "BANK", String.valueOf(plugin.getBankTrips()), CYAN);
        token(g, tx, y + 34, "FROM", shortLabel(plugin.getLastTrader()), PURPLE);
    }

    private static Color stateColor(String state) {
        if ("WAITING".equals(state)) {
            return GREEN;
        }
        if ("BANKING".equals(state) || "OPEN_BANK".equals(state) || "DEPOSIT".equals(state)) {
            return CYAN;
        }
        if ("TRADE1".equals(state) || "TRADE2".equals(state) || "ACCEPT".equals(state)) {
            return AMBER;
        }
        return AMBER;
    }

    private static String shortState(String state) {
        if (state == null) {
            return "-";
        }
        return state.length() > 10 ? state.substring(0, 10) : state;
    }

    private static String shortLabel(String label) {
        if (label == null || label.isEmpty() || "-".equals(label) || "n/a".equalsIgnoreCase(label)) {
            return "-";
        }
        return label.length() > 8 ? label.substring(0, 8) : label;
    }

    private int token(Graphics2D g, int x, int y, String label, String value, Color valueColor) {
        FontMetrics tiny = g.getFontMetrics(fTiny);
        FontMetrics stat = g.getFontMetrics(fStat);
        text(g, label, x, y + 11, DIM, fTiny);
        int nx = x + tiny.stringWidth(label) + 4;
        text(g, value, nx, y + 11, valueColor, fStat);
        return nx + stat.stringWidth(value) + 12;
    }

    private void text(Graphics2D g, String s, int x, int y, Color color, Font font) {
        g.setFont(font);
        g.setColor(new Color(0, 0, 0, 175));
        g.drawString(s, x + 1, y + 1);
        g.setColor(color);
        g.drawString(s, x, y);
    }

    private Rectangle inventoryBounds() {
        try {
            Widget widget = this.client.getWidget(149, 0);
            if (widget != null && !widget.isHidden()) {
                Rectangle b = widget.getBounds();
                if (b != null && b.width > 50 && b.height > 50) {
                    return b;
                }
            }
        } catch (Exception ignored) {
            // empty
        }
        return null;
    }

    private static String safe(String s) {
        return s == null || s.isEmpty() ? "-" : s;
    }

    private static String formatRuntime(long startMs) {
        if (startMs <= 0L) {
            return "00:00";
        }
        long ms = System.currentTimeMillis() - startMs;
        long s = ms / 1000L % 60L;
        long m = ms / 60000L % 60L;
        long h = ms / 3600000L;
        if (h > 0L) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }

    private static String ellipsize(FontMetrics fm, String s, int maxW) {
        if (maxW <= 0) {
            return "";
        }
        if (fm.stringWidth(s) <= maxW) {
            return s;
        }
        while (s.length() > 1 && fm.stringWidth(s + "\u2026") > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "\u2026";
    }
}
