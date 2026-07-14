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
package net.runelite.client.plugins.svc.aioskiller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConfig;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerPlugin;
import net.runelite.client.plugins.svc.aioskiller.InputMode;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterState;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.unethicalite.api.input.Mouse;

class AioSkillerOverlay
extends Overlay {
    private final Client client;
    private final AioSkillerPlugin plugin;
    private final AioSkillerConfig config;
    private static final Color BG_TOP = new Color(62, 53, 38, 244);
    private static final Color BG_BOT = new Color(30, 25, 17, 248);
    private static final Color BORDER = new Color(123, 98, 53, 235);
    private static final Color BORDER_HI = new Color(201, 165, 92, 120);
    private static final Color HDR_R = new Color(46, 38, 25);
    private static final Color GOLD = new Color(255, 178, 60);
    private static final Color GOLD_HI = new Color(255, 216, 124);
    private static final Color CREAM = new Color(241, 231, 203);
    private static final Color DIM = new Color(178, 162, 122);
    private static final Color GREEN = new Color(120, 206, 110);
    private static final Color RED = new Color(222, 86, 66);
    private static final Color AMBER = new Color(236, 172, 72);
    private static final Color CYAN = new Color(110, 198, 214);
    private static final Color DIVIDER = new Color(201, 165, 92, 70);
    private static final Color TRACK = new Color(0, 0, 0, 150);
    private static final int PAD = 10;
    private static final int HEADER_H = 26;
    private static final int CHIP_H = 17;
    private final Font fTitle = new Font("SansSerif", 1, 14);
    private final Font fSub = new Font("SansSerif", 0, 9);
    private final Font fBody = new Font("SansSerif", 0, 12);
    private final Font fStat = new Font("SansSerif", 1, 13);
    private final Font fChip = new Font("SansSerif", 1, 10);
    private final Font fTiny = new Font("SansSerif", 0, 10);
    private final Font fTinyB = new Font("SansSerif", 1, 10);

    AioSkillerOverlay(Client client, AioSkillerPlugin aioSkillerPlugin, AioSkillerConfig aioSkillerConfig) {
        this.client = client;
        this.plugin = aioSkillerPlugin;
        this.config = aioSkillerConfig;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        Rectangle rectangle;
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle rectangle2 = this.chatboxBounds();
        if (rectangle2 != null) {
            this.drawStatusPanel(graphics2D, rectangle2);
        }
        if ((rectangle = this.inventoryBounds()) != null) {
            this.drawSkillBarsOverInventory(graphics2D, rectangle, this.plugin.skillRows());
        }
        return null;
    }

    private void drawStatusPanel(Graphics2D graphics2D, Rectangle rectangle) {
        boolean bl;
        int n;
        int n2 = rectangle.x + 2;
        int n3 = rectangle.y + 2;
        int n4 = rectangle.width - 4;
        int n5 = n2 + 10;
        int n6 = n4 - 20;
        int n7 = n5 + n6;
        boolean bl2 = this.plugin.hasErrors();
        int n8 = n3 + 26 + 6;
        int n9 = n8 + 17 + 4;
        int n10 = n9 + 6;
        int n11 = n10 + 15;
        boolean bl3 = this.plugin.eventWatcherOn();
        int n12 = n11;
        int n13 = n12 + 6;
        int n14 = n = bl3 ? n13 + 15 : n11;
        int n15 = n14 + 6;
        int n16 = n15 + 15;
        int n17 = n16 + 15;
        int n18 = bl2 ? 15 : 0;
        int n19 = n17 - n3 + 6 + n18;
        n19 = Math.min(n19, rectangle.height - 4);
        graphics2D.setPaint(new GradientPaint(n2, n3, BG_TOP, n2, n3 + n19, BG_BOT));
        graphics2D.fillRoundRect(n2, n3, n4, n19, 14, 14);
        graphics2D.setColor(BORDER);
        graphics2D.setStroke(new BasicStroke(1.6f));
        graphics2D.drawRoundRect(n2, n3, n4 - 1, n19 - 1, 14, 14);
        graphics2D.setColor(BORDER_HI);
        graphics2D.setStroke(new BasicStroke(1.0f));
        graphics2D.drawRoundRect(n2 + 2, n3 + 2, n4 - 5, n19 - 5, 11, 11);
        graphics2D.setPaint(new GradientPaint(n2, n3, new Color(96, 70, 30), n2 + n4, n3, HDR_R));
        graphics2D.fillRoundRect(n2, n3, n4, 26, 14, 14);
        graphics2D.fillRect(n2, n3 + 26 - 12, n4, 12);
        graphics2D.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 220));
        graphics2D.fillRect(n2, n3 + 26 - 2, n4, 2);
        this.starGlyph(graphics2D, n2 + 16, n3 + 13);
        this.text(graphics2D, "x64 AIO SKILLER", n2 + 30, n3 + 14, GOLD_HI, this.fTitle);
        this.text(graphics2D, "all-in-one skilling", n2 + 31, n3 + 23, DIM, this.fSub);
        String string = bl2 ? "\u26a0 " + this.plugin.getErrorCount() + (this.plugin.getErrorCount() == 1 ? " ISSUE" : " ISSUES") : "\u2713 CLEAN";
        Color color = bl2 ? RED : GREEN;
        int n20 = this.chipWidth(graphics2D, string);
        this.chip(graphics2D, n2 + n4 - n20 - 22, n3 + 4, string, color);
        this.statusDot(graphics2D, n2 + n4 - 12, n3 + 13, this.phaseColor(this.plugin.activeJobPhase(), bl2));
        Color color2 = this.accent(this.plugin.activeSkillId());
        String string2 = this.plugin.getActiveSkill();
        this.chip(graphics2D, n5, n8, string2.toUpperCase(), color2);
        int n21 = n5 + this.chipWidth(graphics2D, string2.toUpperCase()) + 5;
        String string3 = this.phaseLabel(this.plugin.activeJobPhase());
        this.chip(graphics2D, n21, n8, string3, this.phaseColor(this.plugin.activeJobPhase(), false));
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fBody);
        String string4 = this.ellipsize(fontMetrics, AioSkillerOverlay.safe(this.plugin.getStatus()), n7 - (n21 += this.chipWidth(graphics2D, string3) + 7));
        this.text(graphics2D, string4, n21, n8 + 13, CREAM, this.fBody);
        this.divider(graphics2D, n2, n4, n9);
        long l = this.plugin.getSessionStartMs() <= 0L ? 0L : System.currentTimeMillis() - this.plugin.getSessionStartMs();
        int n22 = this.plugin.getCompletedCount();
        int n23 = this.plugin.getEnabledCount();
        int n24 = n5;
        n24 = this.token(graphics2D, n24, n10, "TIME", AioSkillerOverlay.formatRuntime(l), CREAM);
        n24 = this.token(graphics2D, n24, n10, "DONE", n22 + "/" + n23, n22 >= n23 && n23 > 0 ? GREEN : GOLD);
        n24 = this.token(graphics2D, n24, n10, "XP", AioSkillerOverlay.abbrev(this.plugin.getSessionXpGained()), GREEN);
        n24 = this.token(graphics2D, n24, n10, "XP/H", AioSkillerOverlay.abbrev((long)this.plugin.getXpPerHour()), GREEN);
        this.token(graphics2D, n24, n10, "STATE", this.stateLabel(this.plugin.masterStateEnum()), this.stateColor(this.plugin.masterStateEnum()));
        this.divider(graphics2D, n2, n4, n14);
        boolean bl4 = this.plugin.telemetryActive();
        long l2 = this.plugin.telemetryLastAgeMs();
        boolean bl5 = bl4 && l2 >= 0L && l2 < 1500L;
        int n25 = n5;
        boolean bl6 = bl = this.plugin.inputMode() == InputMode.PACKETS_ONLY;
        n25 = this.token(graphics2D, n25, n15, "INPUT", bl ? "PACKETS" : (bl5 ? "MOUSE \u25cf" : "MOUSE"), bl ? AMBER : GREEN);
        n25 = this.token(graphics2D, n25, n15, "MOV", AioSkillerOverlay.abbrev(this.plugin.telemetryMoves()), CREAM);
        n25 = this.token(graphics2D, n25, n15, "HIT", AioSkillerOverlay.abbrev(this.plugin.telemetryHits()), GREEN);
        this.token(graphics2D, n25, n15, "PKT", AioSkillerOverlay.abbrev(this.plugin.telemetryFallbacks()), this.plugin.telemetryFallbacks() > 0L ? AMBER : DIM);
        Point point = null;
        try {
            point = Mouse.getPosition();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        String string5 = point != null ? "(" + point.x + "," + point.y + ")" : "\u2014";
        int n26 = n5;
        n26 = this.token(graphics2D, n26, n16, "CURSOR", string5, CYAN);
        n26 = this.token(graphics2D, n26, n16, "LAST", AioSkillerOverlay.safe(this.plugin.telemetryLast()), CREAM);
        this.token(graphics2D, n26, n16, "TEMPO", String.format("%.2fx", this.plugin.accountTempo()), GOLD);
        if (bl2) {
            int n27 = n3 + n19 - n18 - 1;
            graphics2D.setColor(new Color(RED.getRed(), RED.getGreen(), RED.getBlue(), 36));
            graphics2D.fillRect(n2 + 4, n27, n4 - 8, n18);
            FontMetrics fontMetrics2 = graphics2D.getFontMetrics(this.fTinyB);
            String string6 = "report opens on stop";
            int n28 = fontMetrics2.stringWidth(string6);
            Object object = "\u26a0 " + AioSkillerOverlay.safe(this.plugin.getLastError());
            object = this.ellipsize(graphics2D.getFontMetrics(this.fTinyB), (String)object, n6 - n28 - 12);
            this.text(graphics2D, (String)object, n5, n27 + 11, RED, this.fTinyB);
            this.text(graphics2D, string6, n7 - n28, n27 + 11, DIM, this.fTiny);
        }
    }

    private void drawSkillBarsOverInventory(Graphics2D graphics2D, Rectangle rectangle, List<AioSkillerPlugin.SkillRow> list) {
        int n;
        if (list.isEmpty()) {
            return;
        }
        int n2 = rectangle.x + 8;
        int n3 = rectangle.width - 16;
        int n4 = rectangle.y + 8;
        int n5 = rectangle.height - 8 - 8;
        if (n3 < 40 || n5 < 20) {
            return;
        }
        int n6 = list.size();
        int n7 = Math.max(15, Math.min(24, n5 / n6));
        int n8 = Math.max(12, Math.min(18, n7 - 4));
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fTinyB);
        for (int i = 0; i < n6 && (n = n4 + i * n7) + n8 <= rectangle.y + rectangle.height; ++i) {
            this.drawSkillBarLine(graphics2D, n2, n, n3, n8, list.get(i), fontMetrics);
        }
    }

    private void drawSkillBarLine(Graphics2D graphics2D, int n, int n2, int n3, int n4, AioSkillerPlugin.SkillRow skillRow, FontMetrics fontMetrics) {
        Color color = this.accent(skillRow.id);
        double d = skillRow.target <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, (double)skillRow.level / (double)skillRow.target));
        graphics2D.setColor(TRACK);
        graphics2D.fillRoundRect(n, n2, n3, n4, n4, n4);
        int n5 = (int)Math.round((double)n3 * d);
        if (n5 > 0) {
            Color color2 = skillRow.done ? GREEN : color;
            graphics2D.setPaint(new GradientPaint(n, 0.0f, color2.darker(), n + n3, 0.0f, color2));
            graphics2D.fillRoundRect(n, n2, Math.max(n4, n5), n4, n4, n4);
        }
        graphics2D.setColor(skillRow.active ? GOLD : new Color(0, 0, 0, 90));
        graphics2D.setStroke(new BasicStroke(skillRow.active ? 1.4f : 1.0f));
        graphics2D.drawRoundRect(n, n2, n3 - 1, n4 - 1, n4, n4);
        int n6 = n2 + (n4 - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
        String string = skillRow.id.displayName();
        String string2 = skillRow.done ? "\u2713 " + skillRow.target : skillRow.level + "/" + skillRow.target;
        int n7 = fontMetrics.stringWidth(string2);
        this.text(graphics2D, this.fit(fontMetrics, string, n3 - n7 - 16), n + 7, n6, CREAM, this.fTinyB);
        this.text(graphics2D, string2, n + n3 - n7 - 7, n6, skillRow.done ? GOLD_HI : CREAM, this.fTinyB);
    }

    private void divider(Graphics2D graphics2D, int n, int n2, int n3) {
        graphics2D.setColor(DIVIDER);
        graphics2D.fillRect(n + 10, n3, n2 - 20, 1);
    }

    private void chip(Graphics2D graphics2D, int n, int n2, String string, Color color) {
        int n3 = this.chipWidth(graphics2D, string);
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 46));
        graphics2D.fillRoundRect(n, n2, n3, 17, 17, 17);
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 175));
        graphics2D.setStroke(new BasicStroke(1.0f));
        graphics2D.drawRoundRect(n, n2, n3, 17, 17, 17);
        this.text(graphics2D, string, n + 8, n2 + 12, color, this.fChip);
    }

    private int chipWidth(Graphics2D graphics2D, String string) {
        return graphics2D.getFontMetrics(this.fChip).stringWidth(string) + 16;
    }

    private int token(Graphics2D graphics2D, int n, int n2, String string, String string2, Color color) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fTiny);
        FontMetrics fontMetrics2 = graphics2D.getFontMetrics(this.fStat);
        this.text(graphics2D, string, n, n2 + 11, DIM, this.fTiny);
        int n3 = n + fontMetrics.stringWidth(string) + 4;
        this.text(graphics2D, string2, n3, n2 + 11, color, this.fStat);
        return n3 + fontMetrics2.stringWidth(string2) + 12;
    }

    private void starGlyph(Graphics2D graphics2D, int n, int n2) {
        Polygon polygon = new Polygon();
        int n3 = 9;
        int n4 = 3;
        polygon.addPoint(n, n2 - n3);
        polygon.addPoint(n + n4, n2 - n4);
        polygon.addPoint(n + n3, n2);
        polygon.addPoint(n + n4, n2 + n4);
        polygon.addPoint(n, n2 + n3);
        polygon.addPoint(n - n4, n2 + n4);
        polygon.addPoint(n - n3, n2);
        polygon.addPoint(n - n4, n2 - n4);
        graphics2D.setColor(GOLD);
        graphics2D.fillPolygon(polygon);
        graphics2D.setColor(GOLD_HI);
        graphics2D.fillOval(n - 2, n2 - 3, 3, 3);
    }

    private void statusDot(Graphics2D graphics2D, int n, int n2, Color color) {
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90));
        graphics2D.fillOval(n - 6, n2 - 6, 12, 12);
        graphics2D.setColor(color);
        graphics2D.fillOval(n - 4, n2 - 4, 8, 8);
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

    private Rectangle inventoryBounds() {
        try {
            Rectangle rectangle;
            Widget widget = this.client.getWidget(149, 0);
            if (widget != null && !widget.isHidden() && (rectangle = widget.getBounds()) != null && rectangle.width > 50 && rectangle.height > 50) {
                return rectangle;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private Color accent(SkillId skillId) {
        if (skillId == null) {
            return GOLD;
        }
        switch (skillId) {
            case AGILITY: {
                return new Color(102, 170, 235);
            }
            case HUNTER: {
                return new Color(192, 150, 96);
            }
            case MINING: {
                return new Color(132, 176, 192);
            }
            case SMITHING: {
                return new Color(226, 142, 72);
            }
            case THIEVING: {
                return new Color(182, 132, 216);
            }
            case WOODCUTTING: {
                return new Color(112, 182, 92);
            }
            case CRAFTING: {
                return new Color(212, 168, 122);
            }
            case HERBLORE: {
                return new Color(152, 202, 92);
            }
            case FARMING: {
                return new Color(122, 196, 122);
            }
            case COMBAT: {
                return new Color(216, 92, 76);
            }
            case MAGIC: {
                return new Color(118, 138, 232);
            }
            case FISHING: {
                return new Color(96, 158, 214);
            }
            case RANGED: {
                return new Color(120, 176, 88);
            }
        }
        return GOLD;
    }

    private String phaseLabel(JobPhase jobPhase) {
        if (jobPhase == null) {
            return "IDLE";
        }
        switch (jobPhase) {
            case SETUP: {
                return "SETUP";
            }
            case TRAVEL: {
                return "TRAVELLING";
            }
            case TRAIN: {
                return "TRAINING";
            }
            case DONE: {
                return "DONE";
            }
            case SKIP: {
                return "SKIPPED";
            }
        }
        return "IDLE";
    }

    private Color phaseColor(JobPhase jobPhase, boolean bl) {
        if (bl && jobPhase == null) {
            return RED;
        }
        if (jobPhase == null) {
            return DIM;
        }
        switch (jobPhase) {
            case SETUP: {
                return AMBER;
            }
            case TRAVEL: {
                return CYAN;
            }
            case TRAIN: {
                return GREEN;
            }
            case DONE: {
                return GREEN;
            }
            case SKIP: {
                return RED;
            }
        }
        return DIM;
    }

    private String stateLabel(MasterState masterState) {
        if (masterState == null) {
            return "\u2014";
        }
        switch (masterState) {
            case BANK_RESET: {
                return "BANKING";
            }
            case SELECTING: {
                return "PICKING";
            }
            case RUNNING_JOB: {
                return "TRAINING";
            }
            case EVENT_BOSS: {
                return "BOSS EVENT";
            }
            case ON_BREAK: {
                return "ON BREAK";
            }
            case FINALIZING: {
                return "WRAPPING";
            }
            case ALL_DONE: {
                return "COMPLETE";
            }
        }
        return masterState.name();
    }

    private Color stateColor(MasterState masterState) {
        if (masterState == null) {
            return DIM;
        }
        switch (masterState) {
            case RUNNING_JOB: {
                return GREEN;
            }
            case EVENT_BOSS: {
                return RED;
            }
            case ALL_DONE: {
                return GOLD_HI;
            }
            case BANK_RESET: {
                return CYAN;
            }
            case ON_BREAK: {
                return new Color(170, 150, 225);
            }
        }
        return AMBER;
    }

    private void text(Graphics2D graphics2D, String string, int n, int n2, Color color, Font font) {
        graphics2D.setFont(font);
        graphics2D.setColor(new Color(0, 0, 0, 175));
        graphics2D.drawString(string, n + 1, n2 + 1);
        graphics2D.setColor(color);
        graphics2D.drawString(string, n, n2);
    }

    private static String safe(String string) {
        return string == null || string.isEmpty() ? "\u2014" : string;
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

    private static String abbrev(long l) {
        if (l < 1000L) {
            return String.valueOf(l);
        }
        if (l < 1000000L) {
            return AioSkillerOverlay.trim1((double)l / 1000.0) + "k";
        }
        return AioSkillerOverlay.trim1((double)l / 1000000.0) + "m";
    }

    private static String trim1(double d) {
        String string = String.format("%.1f", d);
        return string.endsWith(".0") ? string.substring(0, string.length() - 2) : string;
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
        if (n <= 0) {
            return "";
        }
        if (fontMetrics.stringWidth(string) <= n) {
            return string;
        }
        while (string.length() > 1 && fontMetrics.stringWidth(string + "\u2026") > n) {
            string = string.substring(0, string.length() - 1);
        }
        return string + "\u2026";
    }
}

