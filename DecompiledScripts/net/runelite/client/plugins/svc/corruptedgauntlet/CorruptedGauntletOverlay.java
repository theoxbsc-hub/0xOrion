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
package net.runelite.client.plugins.svc.corruptedgauntlet;

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
import net.runelite.client.plugins.svc.corruptedgauntlet.CorruptedGauntletConfig;
import net.runelite.client.plugins.svc.corruptedgauntlet.CorruptedGauntletPlugin;
import net.runelite.client.plugins.svc.corruptedgauntlet.HunllefPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class CorruptedGauntletOverlay
extends Overlay {
    private final Client client;
    private final CorruptedGauntletPlugin plugin;
    private final CorruptedGauntletConfig config;
    private static final Color BG_TOP = new Color(59, 50, 38, 242);
    private static final Color BG_BOT = new Color(33, 28, 21, 244);
    private static final Color EDGE = new Color(164, 132, 78, 190);
    private static final Color HDR_L = new Color(92, 73, 43);
    private static final Color HDR_R = new Color(54, 43, 27);
    private static final Color ACCENT = new Color(214, 178, 110);
    private static final Color GOLD = new Color(255, 196, 64);
    private static final Color LIME = new Color(122, 174, 78);
    private static final Color RED = new Color(190, 58, 44);
    private static final Color BLOOD = new Color(168, 48, 40);
    private static final Color TXT = new Color(229, 217, 189);
    private static final Color DIM = new Color(160, 144, 114);
    private static final Color DIVIDER = new Color(164, 132, 78, 60);
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

    CorruptedGauntletOverlay(Client client, CorruptedGauntletPlugin corruptedGauntletPlugin, CorruptedGauntletConfig corruptedGauntletConfig) {
        this.client = client;
        this.plugin = corruptedGauntletPlugin;
        this.config = corruptedGauntletConfig;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        int n;
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
        HunllefPhase hunllefPhase = this.plugin.getPhase();
        boolean bl = hunllefPhase == HunllefPhase.FIGHTING;
        int n6 = n2 + 10;
        int n7 = n4 - 20;
        int n8 = n6 + n7;
        graphics2D.setPaint(new GradientPaint(n2, n3, BG_TOP, n2, n3 + n5, BG_BOT));
        graphics2D.fillRoundRect(n2, n3, n4, n5, 14, 14);
        graphics2D.setColor(EDGE);
        graphics2D.setStroke(new BasicStroke(1.4f));
        graphics2D.drawRoundRect(n2, n3, n4 - 1, n5 - 1, 14, 14);
        graphics2D.setPaint(new GradientPaint(n2, n3, HDR_L, n2 + n4, n3, HDR_R));
        graphics2D.fillRoundRect(n2, n3, n4, 24, 14, 14);
        graphics2D.fillRect(n2, n3 + 24 - 12, n4, 12);
        this.crystal(graphics2D, n2 + 16, n3 + 12);
        this.text(graphics2D, "x64 ORION GAUNTLET BOT", n2 + 30, n3 + 16, GOLD, this.fTitle);
        this.statusDot(graphics2D, n2 + n4 - 14, n3 + 12, hunllefPhase, bl);
        int n9 = n3 + 24 + 6;
        String string = this.phaseLabel(hunllefPhase);
        this.chip(graphics2D, n6, n9, string, this.phaseColor(hunllefPhase));
        int n10 = this.chipWidth(graphics2D, string);
        FontMetrics fontMetrics = graphics2D.getFontMetrics(this.fBody);
        String string2 = this.ellipsize(fontMetrics, CorruptedGauntletOverlay.safe(this.plugin.getStatus()), n7 - n10 - 12);
        this.text(graphics2D, string2, n6 + n10 + 12, n9 + 13, TXT, this.fBody);
        n9 += 21;
        if (!bl) {
            FontMetrics fontMetrics2 = graphics2D.getFontMetrics(this.fTiny);
            String string3 = this.ellipsize(fontMetrics2, "\u203a " + CorruptedGauntletOverlay.safe(this.plugin.getLastAction()), n7);
            this.text(graphics2D, string3, n6, n9 + 8, DIM, this.fTiny);
            n9 += 13;
        }
        this.divider(graphics2D, n2, n4, n9);
        n9 += 6;
        int n11 = this.plugin.getKillCount();
        int n12 = this.plugin.getDeathCount();
        int n13 = this.plugin.getTotalRuns();
        long l = this.plugin.getStartTime() == 0L ? 0L : System.currentTimeMillis() - this.plugin.getStartTime();
        double d = (double)l / 3600000.0;
        String string4 = d > 0.01 ? CorruptedGauntletOverlay.trim1((double)n11 / d) : "\u2014";
        String string5 = n13 > 0 ? n11 * 100 / n13 + "%" : "\u2014";
        String[] stringArray = new String[]{"RUNTIME", "COMPLETED", "DEATHS", "RUNS", "PER HOUR", "SUCCESS"};
        String[] stringArray2 = new String[]{CorruptedGauntletOverlay.formatRuntime(l), String.valueOf(n11), String.valueOf(n12), String.valueOf(n13), string4, string5};
        Color[] colorArray = new Color[]{ACCENT, GOLD, n12 > 0 ? RED : DIM, TXT, LIME, GOLD};
        int n14 = n7 / 6;
        for (n = 0; n < 6; ++n) {
            this.statCell(graphics2D, n6 + n * n14, n9, n14, stringArray[n], stringArray2[n], colorArray[n]);
        }
        graphics2D.setColor(ACCENT);
        graphics2D.fillRect(n6, (n9 += 29) - 1, 3, 11);
        n = n6 + 8;
        n = this.token(graphics2D, n, n9, "AVG", CorruptedGauntletOverlay.fmtClock(this.plugin.getAvgBossMs()), ACCENT);
        n = this.token(graphics2D, n, n9, "BEST", CorruptedGauntletOverlay.fmtClock(this.plugin.getBestBossMs()), LIME);
        n = this.token(graphics2D, n, n9, "WORST", CorruptedGauntletOverlay.fmtClock(this.plugin.getWorstBossMs()), GOLD);
        if (bl) {
            this.token(graphics2D, n, n9, "NOW", CorruptedGauntletOverlay.fmtClock(this.plugin.getCurrentFightMs()), TXT);
        }
        n9 += 15;
        if (bl) {
            this.divider(graphics2D, n2, n4, n9);
            n9 += 6;
            int n15 = (n7 - 10) / 2;
            int n16 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
            int n17 = Math.max(1, this.client.getRealSkillLevel(Skill.HITPOINTS));
            double d2 = (double)n16 / (double)n17;
            Color color = d2 > 0.5 ? LIME : (d2 > 0.25 ? GOLD : RED);
            this.bar(graphics2D, n6, n9, n15, d2, color, "HP", n16 + " / " + n17);
            int n18 = this.plugin.getBossHpPercent();
            this.bar(graphics2D, n6 + n15 + 10, n9, n15, (double)n18 / 100.0, BLOOD, "HUNLLEF", n18 + "%");
            n9 += 19;
            boolean bl2 = this.plugin.isOnHazardTile();
            String string6 = this.plugin.getActivePrayersName();
            boolean bl3 = string6 != null && (string6.contains("Mag") || string6.contains("Mis") || string6.contains("Mel"));
            int n19 = this.plugin.getTornadoCount();
            int n20 = this.config.avoidLavaFloor() ? this.plugin.getHazardTileCount() : 0;
            int n21 = n6;
            n21 = this.token(graphics2D, n21, n9, "WPN", String.valueOf((Object)this.plugin.getCurrentWeapon()), this.plugin.isLowWeaponWarning() ? RED : ACCENT);
            n21 = this.token(graphics2D, n21, n9, "PRAY", bl3 ? string6 : "NONE", bl3 ? LIME : RED);
            n21 = this.token(graphics2D, n21, n9, "TRN", String.valueOf(n19), n19 > 0 ? GOLD : DIM);
            n21 = this.token(graphics2D, n21, n9, "LAVA", String.valueOf(n20), n20 > 0 ? GOLD : DIM);
            if (bl2) {
                this.chip(graphics2D, n8 - 60, n9 - 12, "DANGER", RED);
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

    private void crystal(Graphics2D graphics2D, int n, int n2) {
        this.hexagon(graphics2D, n, n2, 9, new Color(181, 146, 84));
        this.hexagon(graphics2D, n, n2, 5, new Color(120, 92, 50, 230));
        graphics2D.setColor(new Color(245, 224, 170, 170));
        graphics2D.fillRect(n - 1, n2 - 6, 2, 12);
    }

    private void hexagon(Graphics2D graphics2D, int n, int n2, int n3, Color color) {
        int[] nArray = new int[6];
        int[] nArray2 = new int[6];
        for (int i = 0; i < 6; ++i) {
            double d = Math.toRadians(60 * i - 30);
            nArray[i] = (int)Math.round((double)n + (double)n3 * Math.cos(d));
            nArray2[i] = (int)Math.round((double)n2 + (double)n3 * Math.sin(d));
        }
        graphics2D.setColor(color);
        graphics2D.fillPolygon(nArray, nArray2, 6);
    }

    private void statusDot(Graphics2D graphics2D, int n, int n2, HunllefPhase hunllefPhase, boolean bl) {
        Color color = bl ? (this.plugin.isOnHazardTile() ? RED : LIME) : (hunllefPhase == HunllefPhase.LOOT_CHEST || hunllefPhase == HunllefPhase.BANK || hunllefPhase == HunllefPhase.REENTER ? GOLD : (hunllefPhase == HunllefPhase.DONE ? ACCENT : new Color(210, 215, 225)));
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

    private String phaseLabel(HunllefPhase hunllefPhase) {
        switch (hunllefPhase) {
            case FIGHTING: {
                return "IN COMBAT";
            }
            case LOOT_CHEST: {
                return "LOOTING";
            }
            case BANK: {
                return "BANKING";
            }
            case REENTER: {
                return "RE-ENTERING";
            }
            case DEAD: {
                return "RESETTING";
            }
            case DONE: {
                return "FINISHED";
            }
        }
        return "IDLE";
    }

    private Color phaseColor(HunllefPhase hunllefPhase) {
        switch (hunllefPhase) {
            case FIGHTING: {
                return LIME;
            }
            case LOOT_CHEST: {
                return GOLD;
            }
            case BANK: {
                return ACCENT;
            }
            case REENTER: {
                return ACCENT;
            }
            case DONE: {
                return ACCENT;
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
        if (fontMetrics.stringWidth(string) <= n) {
            return string;
        }
        while (string.length() > 1 && fontMetrics.stringWidth(string + "\u2026") > n) {
            string = string.substring(0, string.length() - 1);
        }
        return string + "\u2026";
    }
}

