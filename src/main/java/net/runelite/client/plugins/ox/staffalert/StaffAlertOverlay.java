package net.runelite.client.plugins.ox.staffalert;

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
import net.runelite.client.plugins.ox.core.OxLoginGate;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Slim StaffAlert bar anchored to the top of the chatbox — status at a glance, no big panel.
 */
class StaffAlertOverlay extends Overlay {
    private static final Color BG_TOP = new Color(42, 32, 52, 244);
    private static final Color BG_BOT = new Color(22, 18, 30, 248);
    private static final Color BORDER = new Color(120, 70, 180, 235);
    private static final Color PURPLE = new Color(0xD4A0FF);
    private static final Color CREAM = new Color(235, 230, 245);
    private static final Color DIM = new Color(160, 150, 180);
    private static final Color GREEN = new Color(120, 206, 110);
    private static final Color AMBER = new Color(236, 172, 72);
    private static final Color RED = new Color(255, 100, 100);

    private static final int BAR_H = 22;

    private final Client client;
    private final StaffAlertPlugin plugin;
    private final StaffAlertConfig config;

    private final Font fChip = new Font("SansSerif", Font.BOLD, 10);
    private final Font fText = new Font("SansSerif", Font.PLAIN, 11);

    StaffAlertOverlay(Client client, StaffAlertPlugin plugin, StaffAlertConfig config) {
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

        Rectangle chat = chatboxBounds();
        if (chat == null) {
            return null;
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = chat.x + 2;
        int y = chat.y + 2;
        int w = Math.max(120, chat.width - 4);

        g.setPaint(new GradientPaint(x, y, BG_TOP, x, y + BAR_H, BG_BOT));
        g.fillRoundRect(x, y, w, BAR_H, 8, 8);
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(x, y, w - 1, BAR_H - 1, 8, 8);

        boolean reactions = this.config.enabled();
        boolean armed = reactions
            && (this.plugin.watchedCount() > 0 || this.config.proximityPanic());
        boolean gated = OxLoginGate.isBlocked();

        Color stateColor = gated ? RED : (armed ? GREEN : AMBER);
        String state = gated ? "GATE" : (armed ? "ARMED" : "OFF");

        int tx = x + 8;
        int ty = y + 15;
        tx = draw(g, "0xSA", tx, ty, PURPLE, fChip) + 8;
        tx = draw(g, state, tx, ty, stateColor, fChip) + 10;

        // Middle: gate timer or last clear/event
        FontMetrics fm = g.getFontMetrics(fText);
        String middle;
        Color midColor = CREAM;
        if (gated) {
            long sec = OxLoginGate.remainingSeconds();
            String left = sec >= 60 ? (sec / 60) + "m" + (sec % 60) + "s" : sec + "s";
            middle = left + "  " + clip(OxLoginGate.getReason(), 28);
            midColor = RED;
        } else {
            String event = this.plugin.getLastEvent();
            String target = this.plugin.getLastTarget();
            if (target != null && !target.isEmpty() && !"-".equals(target)
                && this.plugin.getLastTriggerAtMs() > 0L
                && System.currentTimeMillis() - this.plugin.getLastTriggerAtMs() < 120_000L) {
                middle = "last " + clip(target, 32);
                midColor = AMBER;
            } else {
                middle = clip(event == null || event.isEmpty() ? "watching" : event, 36);
            }
        }
        middle = ellipsize(fm, middle, w - (tx - x) - 70);
        tx = draw(g, middle, tx, ty, midColor, fText) + 10;

        // Right: watch count
        String right = "W:" + this.plugin.watchedCount();
        if (this.config.proximityPanic()) {
            right += " P";
        }
        int rw = g.getFontMetrics(fChip).stringWidth(right);
        draw(g, right, x + w - rw - 8, ty, DIM, fChip);

        return null;
    }

    private static int draw(Graphics2D g, String s, int x, int y, Color color, Font font) {
        g.setFont(font);
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(s, x + 1, y + 1);
        g.setColor(color);
        g.drawString(s, x, y);
        return x + g.getFontMetrics(font).stringWidth(s);
    }

    private static String clip(String s, int max) {
        if (s == null || s.isEmpty()) {
            return "-";
        }
        String clean = s.replace('\n', ' ').trim();
        return clean.length() <= max ? clean : clean.substring(0, max - 1) + "...";
    }

    private static String ellipsize(FontMetrics fm, String s, int maxW) {
        if (maxW <= 8 || s == null) {
            return "";
        }
        if (fm.stringWidth(s) <= maxW) {
            return s;
        }
        while (s.length() > 1 && fm.stringWidth(s + "...") > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }

    /** Chatbox parent (group 162) — fallback to message viewport. */
    private Rectangle chatboxBounds() {
        int[][] ids = {
            {162, 0},
            {162, 55},
            {162, 34},
        };
        for (int[] id : ids) {
            try {
                Widget widget = this.client.getWidget(id[0], id[1]);
                if (widget == null || widget.isHidden()) {
                    continue;
                }
                Rectangle b = widget.getBounds();
                if (b != null && b.width > 120 && b.height > 20) {
                    return b;
                }
            } catch (Exception ignored) {
                // try next
            }
        }
        return null;
    }
}
