/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 */
package net.runelite.client.plugins.svc.crafting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.svc.crafting.CraftingConfig;
import net.runelite.client.plugins.svc.crafting.CraftingPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class CraftingOverlay
extends Overlay {
    private final Client client;
    private final CraftingPlugin plugin;
    private final CraftingConfig config;
    private static final Color BG_TOP = new Color(26, 36, 54, 242);
    private static final Color BG_BOT = new Color(15, 21, 34, 244);
    private static final Color EDGE = new Color(120, 168, 220, 190);
    private static final Color HDR_L = new Color(46, 78, 128);
    private static final Color HDR_R = new Color(26, 44, 78);
    private static final Color ACCENT = new Color(140, 196, 240);
    private static final Color GOLD = new Color(255, 196, 64);
    private static final Color CYAN = new Color(120, 214, 224);
    private static final Color RUBY = new Color(232, 86, 96);
    private static final Color TXT = new Color(224, 232, 244);
    private static final Color DIM = new Color(140, 156, 180);
    private static final Color DIVIDER = new Color(120, 168, 220, 60);
    private static final int PAD = 10;
    private static final int HEADER_H = 24;
    private static final int CHIP_H = 18;
    private final Font fTitle = new Font("SansSerif", 1, 13);
    private final Font fBody = new Font("SansSerif", 0, 12);
    private final Font fStat = new Font("SansSerif", 1, 14);
    private final Font fChip = new Font("SansSerif", 1, 10);
    private final Font fTiny = new Font("SansSerif", 0, 10);
    private final Font fTinyB = new Font("SansSerif", 1, 10);

    CraftingOverlay(Client client, CraftingPlugin craftingPlugin, CraftingConfig craftingConfig) {
        this.client = client;
        this.plugin = craftingPlugin;
        this.config = craftingConfig;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        Rectangle rectangle = this.chatboxBounds();
        if (rectangle == null) {
            return null;
        }
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int n = rectangle.x + 2;
        int n2 = rectangle.y + 2;
        int n3 = rectangle.width - 4;
        int n4 = rectangle.height - 4;
        int n5 = n + 10;
        int n6 = n3 - 20;
        String string = this.activityLabel();
        Color color = this.activityColor();
        graphics2D.setPaint(new GradientPaint(n, n2, BG_TOP, n, n2 + n4, BG_BOT));
        graphics2D.fillRoundRect(n, n2, n3, n4, 14, 14);
        graphics2D.setColor(EDGE);
        graphics2D.setStroke(new BasicStroke(1.4f));
        graphics2D.drawRoundRect(n, n2, n3 - 1, n4 - 1, 14, 14);
        graphics2D.setPaint(new GradientPaint(n, n2, HDR_L, n + n3, n2, HDR_R));
        graphics2D.fillRoundRect(n, n2, n3, 24, 14, 14);
        graphics2D.fillRect(n, n2 + 24 - 12, n3, 12);
        this.gem(graphics2D, n + 16, n2 + 12);
        this.text(graphics2D, "x64 AIO CRAFTER", n + 30, n2 + 16, GOLD, this.fTitle);
        this.statusDot(graphics2D, n + n3 - 14, n2 + 12, color);
        int n7 = n2 + 24 + 6;
        this.chip(graphics2D, n5, n7, string, color);
        int n8 = this.chipWidth(graphics2D, string);
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fBody);
        String string2 = this.ellipsize(fontMetrics, CraftingOverlay.safe(this.plugin.getStatus()), n6 - n8 - 12);
        this.text(graphics2D, string2, n5 + n8 + 12, n7 + 13, TXT, this.fBody);
        n7 += 22;
        Object object = this.plugin.getGemName();
        if (this.plugin.getGemLevel() > 0) {
            object = (String)object + "  (lvl " + this.plugin.getGemLevel() + ")";
        }
        FontMetrics fontMetrics2 = graphics2D.getFontMetrics(this.fTinyB);
        this.text(graphics2D, this.fit(fontMetrics2, (String)object, n6 - 90), n5, n7 + 9, ACCENT, this.fTinyB);
        String string3 = "to cut " + this.plugin.getCuttableNow();
        this.text(graphics2D, string3, n5 + n6 - fontMetrics2.stringWidth(string3), n7 + 9, DIM, this.fTinyB);
        this.divider(graphics2D, n, n3, n7 += 13);
        n7 += 6;
        long l = this.plugin.getSessionStartMs() == 0L ? 0L : System.currentTimeMillis() - this.plugin.getSessionStartMs();
        double d = (double)l / 3600000.0;
        int n9 = this.plugin.getGemsCut();
        int n10 = this.plugin.getXpGained();
        String string4 = d > 0.01 ? CraftingOverlay.compactInt((int)Math.round((double)n9 / d)) : "\u2014";
        String string5 = d > 0.01 ? CraftingOverlay.compact((double)n10 / d) : "\u2014";
        String[] stringArray = new String[]{"RUNTIME", "CRAFT LVL", "CUT", "CUT/HR", "XP", "XP/HR"};
        String[] stringArray2 = new String[]{CraftingOverlay.formatRuntime(l), String.valueOf(this.plugin.getCraftLevel()), String.valueOf(n9), string4, CraftingOverlay.compactInt(n10), string5};
        Color[] colorArray = new Color[]{ACCENT, GOLD, CYAN, CYAN, GOLD, ACCENT};
        int n11 = n6 / 6;
        for (int i = 0; i < 6; ++i) {
            this.statCell(graphics2D, n5 + i * n11, n7, n11, stringArray[i], stringArray2[i], colorArray[i]);
        }
        return null;
    }

    private String activityLabel() {
        switch (this.plugin.getStateName()) {
            case "CUT": {
                return "CUTTING";
            }
            case "IDLE": {
                return "IDLE";
            }
        }
        return "BANKING";
    }

    private Color activityColor() {
        switch (this.plugin.getStateName()) {
            case "CUT": {
                return CYAN;
            }
            case "IDLE": {
                return DIM;
            }
        }
        return GOLD;
    }

    private Rectangle chatboxBounds() {
        Object object;
        try {
            object = this.client.getWidgets();
            if (object != null && ((Widget[][])object).length > 162 && object[162] != null) {
                Rectangle rectangle = null;
                long l = 0L;
                for (Object object2 : object[162]) {
                    long l2;
                    Rectangle rectangle2;
                    if (object2 == null || object2.isHidden() || (rectangle2 = object2.getBounds()) == null || (l2 = (long)rectangle2.width * (long)rectangle2.height) <= l) continue;
                    l = l2;
                    rectangle = rectangle2;
                }
                if (rectangle != null && rectangle.width > 100 && rectangle.height > 50) {
                    return rectangle;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            object = this.client.getCanvas();
            int n = 519;
            int n2 = 165;
            return new Rectangle(0, Math.max(0, ((Component)object).getHeight() - n2), Math.min(n, ((Component)object).getWidth()), n2);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private void divider(Graphics2D graphics2D, int n, int n2, int n3) {
        graphics2D.setColor(DIVIDER);
        graphics2D.fillRect(n + 10, n3, n2 - 20, 1);
    }

    private void chip(Graphics2D graphics2D, int n, int n2, String string, Color color) {
        int n3 = this.chipWidth(graphics2D, string);
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 46));
        graphics2D.fillRoundRect(n, n2, n3, 18, 18, 18);
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 170));
        graphics2D.setStroke(new BasicStroke(1.0f));
        graphics2D.drawRoundRect(n, n2, n3, 18, 18, 18);
        this.text(graphics2D, string, n + 8, n2 + 13, color, this.fChip);
    }

    private int chipWidth(Graphics2D graphics2D, String string) {
        return graphics2D.getFontMetrics(this.fChip).stringWidth(string) + 16;
    }

    private void statCell(Graphics2D graphics2D, int n, int n2, int n3, String string, String string2, Color color) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fTiny);
        this.text(graphics2D, this.fit(fontMetrics, string, n3 - 3), n, n2 + 8, DIM, this.fTiny);
        FontMetrics fontMetrics2 = graphics2D.getFontMetrics(this.fStat);
        this.text(graphics2D, this.fit(fontMetrics2, string2, n3 - 3), n, n2 + 25, color, this.fStat);
    }

    private void gem(Graphics2D graphics2D, int n, int n2) {
        Polygon polygon = new Polygon();
        polygon.addPoint(n - 5, n2 - 5);
        polygon.addPoint(n + 5, n2 - 5);
        polygon.addPoint(n + 7, n2 - 1);
        polygon.addPoint(n, n2 + 7);
        polygon.addPoint(n - 7, n2 - 1);
        graphics2D.setColor(new Color(120, 214, 224));
        graphics2D.fillPolygon(polygon);
        graphics2D.setColor(new Color(36, 60, 84));
        graphics2D.setStroke(new BasicStroke(1.0f));
        graphics2D.drawLine(n - 5, n2 - 5, n + 5, n2 - 5);
        graphics2D.drawLine(n - 7, n2 - 1, n + 7, n2 - 1);
        graphics2D.drawLine(n - 5, n2 - 5, n, n2 + 7);
        graphics2D.drawLine(n + 5, n2 - 5, n, n2 + 7);
        graphics2D.setColor(new Color(224, 244, 248, 210));
        graphics2D.drawLine(n - 3, n2 - 4, n + 1, n2 - 4);
    }

    private void statusDot(Graphics2D graphics2D, int n, int n2, Color color) {
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90));
        graphics2D.fillOval(n - 6, n2 - 6, 12, 12);
        graphics2D.setColor(color);
        graphics2D.fillOval(n - 4, n2 - 4, 8, 8);
    }

    private void text(Graphics2D graphics2D, String string, int n, int n2, Color color, Font font) {
        graphics2D.setFont(font);
        graphics2D.setColor(new Color(0, 0, 0, 170));
        graphics2D.drawString(string, n + 1, n2 + 1);
        graphics2D.setColor(color);
        graphics2D.drawString(string, n, n2);
    }

    private static String safe(String string) {
        return string == null || string.isEmpty() ? "\u2014" : string;
    }

    private static String trim1(double d) {
        String string = String.format("%.1f", d);
        return string.endsWith(".0") ? string.substring(0, string.length() - 2) : string;
    }

    private static String compact(double d) {
        if (d >= 1000.0) {
            return CraftingOverlay.trim1(d / 1000.0) + "k";
        }
        return CraftingOverlay.trim1(d);
    }

    private static String compactInt(int n) {
        if (n >= 1000) {
            return CraftingOverlay.trim1((double)n / 1000.0) + "k";
        }
        return String.valueOf(n);
    }

    private static String formatRuntime(long l) {
        if (l <= 0L) {
            return "00:00:00";
        }
        long l2 = l / 1000L % 60L;
        long l3 = l / 60000L % 60L;
        long l4 = l / 3600000L;
        return String.format("%02d:%02d:%02d", l4, l3, l2);
    }

    private String fit(FontMetrics fontMetrics, String string, int n) {
        int n2;
        if (string == null) {
            return "";
        }
        if (fontMetrics.stringWidth(string) <= n) {
            return string;
        }
        for (n2 = string.length(); n2 > 1 && fontMetrics.stringWidth(string.substring(0, n2) + "\u2026") > n; --n2) {
        }
        return string.substring(0, n2) + "\u2026";
    }

    private String ellipsize(FontMetrics fontMetrics, String string, int n) {
        if (fontMetrics.stringWidth(string) <= n) {
            return string;
        }
        while (string.length() > 1 && fontMetrics.stringWidth(string + "\u2026") > n) {
            string = string.substring(0, string.length() - 1);
        }
        return string + "\u2026";
    }
}

