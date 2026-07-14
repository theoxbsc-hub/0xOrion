/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Skill
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 */
package net.runelite.client.plugins.svc.moons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.svc.moons.MoonsConfig;
import net.runelite.client.plugins.svc.moons.MoonsPhase;
import net.runelite.client.plugins.svc.moons.MoonsPlugin;
import net.runelite.client.plugins.svc.moons.MoonsResupplyState;
import net.runelite.client.plugins.svc.moons.TargetMoon;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class MoonsOverlay
extends Overlay {
    private final Client client;
    private final MoonsPlugin plugin;
    private final MoonsConfig config;
    private static final Color BG_TOP = new Color(30, 30, 52, 242);
    private static final Color BG_BOT = new Color(13, 13, 26, 245);
    private static final Color EDGE = new Color(150, 162, 205, 185);
    private static final Color HDR_R = new Color(34, 34, 60);
    private static final Color SILVER = new Color(214, 222, 246);
    private static final Color GOLD = new Color(255, 205, 96);
    private static final Color LIME = new Color(120, 200, 110);
    private static final Color RED = new Color(214, 72, 60);
    private static final Color BLOOD = new Color(176, 46, 42);
    private static final Color CYAN = new Color(110, 206, 224);
    private static final Color PINK = new Color(224, 132, 196);
    private static final Color ORANGE = new Color(232, 150, 70);
    private static final Color TXT = new Color(224, 228, 244);
    private static final Color DIM = new Color(150, 158, 188);
    private static final Color DIVIDER = new Color(150, 162, 205, 60);
    private static final Color TRACK = new Color(0, 0, 0, 130);
    private static final int PAD = 10;
    private static final int HEADER_H = 24;
    private static final int CHIP_H = 18;
    private static final int BAR_H = 15;
    private final Font fTitle = new Font("SansSerif", 1, 13);
    private final Font fBody = new Font("SansSerif", 0, 12);
    private final Font fStat = new Font("SansSerif", 1, 14);
    private final Font fChip = new Font("SansSerif", 1, 10);
    private final Font fTiny = new Font("SansSerif", 0, 10);
    private final Font fTinyB = new Font("SansSerif", 1, 10);

    MoonsOverlay(Client client, MoonsPlugin moonsPlugin, MoonsConfig moonsConfig) {
        this.client = client;
        this.plugin = moonsPlugin;
        this.config = moonsConfig;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        int n;
        Object object;
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
        int n2 = rectangle.x + 2;
        int n3 = rectangle.y + 2;
        int n4 = rectangle.width - 4;
        int n5 = rectangle.height - 4;
        MoonsPhase moonsPhase = this.plugin.getPhase();
        boolean bl = moonsPhase == MoonsPhase.FIGHTING || moonsPhase == MoonsPhase.DODGING;
        Color color = this.moonAccent(this.plugin.getTargetMoon());
        int n6 = n2 + 10;
        int n7 = n4 - 20;
        int n8 = n6 + n7;
        graphics2D.setPaint(new GradientPaint(n2, n3, BG_TOP, n2, n3 + n5, BG_BOT));
        graphics2D.fillRoundRect(n2, n3, n4, n5, 14, 14);
        graphics2D.setColor(EDGE);
        graphics2D.setStroke(new BasicStroke(1.4f));
        graphics2D.drawRoundRect(n2, n3, n4 - 1, n5 - 1, 14, 14);
        graphics2D.setPaint(new GradientPaint(n2, n3, color.darker().darker(), n2 + n4, n3, HDR_R));
        graphics2D.fillRoundRect(n2, n3, n4, 24, 14, 14);
        graphics2D.fillRect(n2, n3 + 24 - 12, n4, 12);
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 220));
        graphics2D.fillRect(n2, n3 + 24 - 2, n4, 2);
        this.moonGlyph(graphics2D, n2 + 16, n3 + 12, color);
        this.text(graphics2D, "x64 ORION MOONS BOT", n2 + 30, n3 + 16, SILVER, this.fTitle);
        this.statusDot(graphics2D, n2 + n4 - 14, n3 + 12, moonsPhase);
        int n9 = n3 + 24 + 6;
        String string = this.phaseLabel(moonsPhase);
        Color color2 = this.phaseColor(moonsPhase);
        this.chip(graphics2D, n6, n9, string, color2);
        int n10 = this.chipWidth(graphics2D, string);
        int n11 = n6 + n10 + 6;
        MoonsResupplyState moonsResupplyState = this.plugin.getResupplyState();
        if (moonsPhase == MoonsPhase.RESUPPLYING && moonsResupplyState != null) {
            object = moonsResupplyState.toString();
            this.chip(graphics2D, n11, n9, (String)object, ORANGE);
            n11 += this.chipWidth(graphics2D, (String)object) + 6;
        }
        object = graphics2D.getFontMetrics(this.fBody);
        String string2 = this.ellipsize((FontMetrics)object, MoonsOverlay.safe(this.plugin.getStatus()), n8 - n11 - 6);
        this.text(graphics2D, string2, n11 + 6, n9 + 13, TXT, this.fBody);
        n9 += 21;
        if (!bl) {
            FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fTiny);
            String string3 = this.ellipsize(fontMetrics, "\u203a target: " + String.valueOf((Object)this.plugin.getTargetMoon()) + "   \u00b7   boss: " + this.plugin.getBossHpText(), n7);
            this.text(graphics2D, string3, n6, n9 + 8, DIM, this.fTiny);
            n9 += 13;
        }
        this.divider(graphics2D, n2, n4, n9);
        n9 += 6;
        int n12 = this.plugin.getKillCount();
        int n13 = this.plugin.getDeathCount();
        int n14 = this.plugin.getTotalRuns();
        long l = this.plugin.getStartTime() == 0L ? 0L : System.currentTimeMillis() - this.plugin.getStartTime();
        double d = (double)l / 3600000.0;
        String string4 = d > 0.01 ? MoonsOverlay.trim1((double)n12 / d) : "\u2014";
        String string5 = n14 > 0 ? n12 * 100 / n14 + "%" : "\u2014";
        String[] stringArray = new String[]{"RUNTIME", "KILLS", "DEATHS", "RUNS", "KILLS/HR", "SUCCESS"};
        String[] stringArray2 = new String[]{MoonsOverlay.formatRuntime(l), String.valueOf(n12), String.valueOf(n13), String.valueOf(n14), string4, string5};
        Color[] colorArray = new Color[]{SILVER, GOLD, n13 > 0 ? RED : DIM, TXT, LIME, string5.equals("100%") ? LIME : GOLD};
        int n15 = n7 / 6;
        for (n = 0; n < 6; ++n) {
            this.statCell(graphics2D, n6 + n * n15, n9, n15, stringArray[n], stringArray2[n], colorArray[n]);
        }
        graphics2D.setColor(color);
        graphics2D.fillRect(n6, (n9 += 29) - 1, 3, 11);
        n = n6 + 8;
        n = this.token(graphics2D, n, n9, "AVG", MoonsOverlay.fmtClock(this.plugin.getAvgKillMs()), color);
        n = this.token(graphics2D, n, n9, "BEST", MoonsOverlay.fmtClock(this.plugin.getBestKillMs()), LIME);
        n = this.token(graphics2D, n, n9, "WORST", MoonsOverlay.fmtClock(this.plugin.getWorstKillMs()), GOLD);
        if (bl) {
            this.token(graphics2D, n, n9, "NOW", MoonsOverlay.fmtClock(this.plugin.getCurrentFightMs()), SILVER);
        }
        n9 += 15;
        if (bl) {
            this.divider(graphics2D, n2, n4, n9);
            n9 += 6;
            int n16 = (n7 - 10) / 2;
            int n17 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
            int n18 = Math.max(1, this.client.getRealSkillLevel(Skill.HITPOINTS));
            double d2 = (double)n17 / (double)n18;
            Color color3 = d2 > 0.5 ? LIME : (d2 > 0.25 ? GOLD : RED);
            this.bar(graphics2D, n6, n9, n16, d2, color3, "HP", n17 + " / " + n18);
            int n19 = this.client.getBoostedSkillLevel(Skill.PRAYER);
            int n20 = Math.max(1, this.client.getRealSkillLevel(Skill.PRAYER));
            double d3 = (double)n19 / (double)n20;
            Color color4 = d3 > 0.3 ? CYAN : RED;
            this.bar(graphics2D, n6 + n16 + 10, n9, n16, d3, color4, "PRAY", n19 + " / " + n20);
            int n21 = this.plugin.getBossHpPercent();
            double d4 = n21 < 0 ? 0.0 : (double)n21 / 100.0;
            this.bar(graphics2D, n6, n9 += 19, n7, d4, BLOOD, this.safeMoonName(this.plugin.getTargetMoon()), (String)(n21 < 0 ? "\u2014" : n21 + "%"));
            int n22 = n6;
            n22 = this.token(graphics2D, n22, n9 += 19, "TARGET", this.safeMoonName(this.plugin.getTargetMoon()), color);
            n22 = this.token(graphics2D, n22, n9, "OFF", MoonsOverlay.safe(this.plugin.getOffensivePrayerName()), LIME);
            n22 = this.token(graphics2D, n22, n9, "PROT", MoonsOverlay.safe(this.plugin.getProtectionPrayerName()), CYAN);
            this.token(graphics2D, n22, n9, "ATK", String.valueOf(this.plugin.getAttackCount()), GOLD);
            if (moonsPhase == MoonsPhase.DODGING) {
                this.chip(graphics2D, n8 - 64, n9 - 12, "DODGING", GOLD);
            }
        }
        return null;
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

    private int token(Graphics2D graphics2D, int n, int n2, String string, String string2, Color color) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fTiny);
        FontMetrics fontMetrics2 = graphics2D.getFontMetrics(this.fTinyB);
        this.text(graphics2D, string, n, n2 + 10, DIM, this.fTiny);
        int n3 = n + fontMetrics.stringWidth(string) + 4;
        this.text(graphics2D, string2, n3, n2 + 10, color, this.fTinyB);
        return n3 + fontMetrics2.stringWidth(string2) + 12;
    }

    private void bar(Graphics2D graphics2D, int n, int n2, int n3, double d, Color color, String string, String string2) {
        d = Math.max(0.0, Math.min(1.0, d));
        graphics2D.setColor(TRACK);
        graphics2D.fillRoundRect(n, n2, n3, 15, 15, 15);
        int n4 = (int)Math.round((double)n3 * d);
        if (n4 > 0) {
            graphics2D.setPaint(new GradientPaint(n, 0.0f, color.darker(), n + n3, 0.0f, color));
            graphics2D.fillRoundRect(n, n2, Math.max(15, n4), 15, 15, 15);
        }
        this.text(graphics2D, string, n + 8, n2 + 15 - 4, Color.WHITE, this.fTinyB);
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fTinyB);
        this.text(graphics2D, string2, n + n3 - fontMetrics.stringWidth(string2) - 8, n2 + 15 - 4, Color.WHITE, this.fTinyB);
    }

    private void moonGlyph(Graphics2D graphics2D, int n, int n2, Color color) {
        graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 235));
        graphics2D.fillOval(n - 8, n2 - 8, 16, 16);
        graphics2D.setColor(HDR_R);
        graphics2D.fillOval(n - 3, n2 - 9, 15, 15);
        graphics2D.setColor(new Color(245, 248, 255, 150));
        graphics2D.fillOval(n - 7, n2 - 5, 3, 3);
    }

    private void statusDot(Graphics2D graphics2D, int n, int n2, MoonsPhase moonsPhase) {
        Color color = this.phaseColor(moonsPhase);
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

    private Color moonAccent(TargetMoon targetMoon) {
        if (targetMoon == null) {
            return SILVER;
        }
        switch (targetMoon) {
            case BLOOD: {
                return new Color(214, 72, 60);
            }
            case BLUE: {
                return new Color(96, 190, 230);
            }
            case ECLIPSE: {
                return new Color(176, 120, 224);
            }
        }
        return SILVER;
    }

    private String safeMoonName(TargetMoon targetMoon) {
        if (targetMoon == null) {
            return "BOSS";
        }
        String string = targetMoon.toString();
        int n = string.indexOf(32);
        return (n > 0 ? string.substring(0, n) : string).toUpperCase();
    }

    private String phaseLabel(MoonsPhase moonsPhase) {
        switch (moonsPhase) {
            case FIGHTING: {
                return "IN COMBAT";
            }
            case DODGING: {
                return "DODGING";
            }
            case LOOTING: {
                return "LOOTING";
            }
            case WAITING_RESPAWN: {
                return "RESPAWN";
            }
            case RESUPPLYING: {
                return "RESUPPLY";
            }
            case ENTERING_ROOM: {
                return "ENTERING";
            }
            case AT_LOBBY: {
                return "AT LOBBY";
            }
            case DEAD: {
                return "KILL!";
            }
            case TRAVELING: {
                return "TRAVELLING";
            }
        }
        return "IDLE";
    }

    private Color phaseColor(MoonsPhase moonsPhase) {
        switch (moonsPhase) {
            case FIGHTING: {
                return LIME;
            }
            case DODGING: {
                return GOLD;
            }
            case DEAD: {
                return GOLD;
            }
            case LOOTING: {
                return CYAN;
            }
            case WAITING_RESPAWN: {
                return PINK;
            }
            case RESUPPLYING: {
                return ORANGE;
            }
            case ENTERING_ROOM: {
                return new Color(176, 120, 224);
            }
            case AT_LOBBY: {
                return SILVER;
            }
        }
        return DIM;
    }

    private static String safe(String string) {
        return string == null || string.isEmpty() ? "\u2014" : string;
    }

    private static String trim1(double d) {
        String string = String.format("%.1f", d);
        return string.endsWith(".0") ? string.substring(0, string.length() - 2) : string;
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

    private static String fmtClock(long l) {
        if (l <= 0L) {
            return "\u2014";
        }
        long l2 = l / 1000L % 60L;
        long l3 = l / 60000L % 60L;
        long l4 = l / 3600000L;
        return l4 > 0L ? String.format("%d:%02d:%02d", l4, l3, l2) : String.format("%d:%02d", l3, l2);
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

