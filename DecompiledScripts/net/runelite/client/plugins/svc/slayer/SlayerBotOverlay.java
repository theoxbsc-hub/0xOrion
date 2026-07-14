/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 */
package net.runelite.client.plugins.svc.slayer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.svc.slayer.SlayerBotConfig;
import net.runelite.client.plugins.svc.slayer.SlayerBotPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class SlayerBotOverlay
extends Overlay {
    private static final Font F_TITLE = new Font("Arial", 1, 13);
    private static final Font F_LINE = new Font("Arial", 0, 11);
    private static final Font F_LABEL = new Font("Arial", 0, 10);
    private static final Font F_VALUE = new Font("Arial", 1, 12);
    private static final Font F_SMALL = new Font("Arial", 0, 9);
    private static final Color PANEL_BG = new Color(12, 14, 18, 232);
    private static final Color BORDER = new Color(0, 170, 70);
    private static final Color HEADER = new Color(45, 205, 95);
    private static final Color ACCENT = new Color(70, 200, 120);
    private static final Color LABEL = new Color(150, 155, 165);
    private static final Color VALUE = new Color(235, 238, 242);
    private static final Color STATUS = new Color(180, 190, 200);
    private static final Color GOOD = new Color(80, 220, 100);
    private static final Color WARN = new Color(240, 200, 60);
    private static final Color BAD = new Color(235, 90, 70);
    private static final Color FAINT = new Color(120, 125, 135);
    private final Client client;
    private final SlayerBotPlugin plugin;
    private final SlayerBotConfig config;

    SlayerBotOverlay(Client client, SlayerBotPlugin slayerBotPlugin, SlayerBotConfig slayerBotConfig) {
        this.client = client;
        this.plugin = slayerBotPlugin;
        this.config = slayerBotConfig;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        Object object;
        if (!this.config.debugOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        Rectangle rectangle = this.chatboxBounds();
        if (rectangle == null) {
            return null;
        }
        Object object2 = graphics2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int n = rectangle.x + 3;
        int n2 = rectangle.y + 3;
        int n3 = rectangle.width - 6;
        int n4 = rectangle.height - 6;
        graphics2D.setColor(PANEL_BG);
        graphics2D.fillRoundRect(n, n2, n3, n4, 10, 10);
        graphics2D.setColor(BORDER);
        graphics2D.drawRoundRect(n, n2, n3, n4, 10, 10);
        int n5 = n + 8;
        int n6 = n2 + 5;
        graphics2D.setFont(F_TITLE);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.setColor(HEADER);
        graphics2D.drawString("ORION SLAYER", n5, n6 + fontMetrics.getAscent());
        graphics2D.setFont(F_LINE);
        FontMetrics fontMetrics2 = graphics2D.getFontMetrics();
        String string = this.plugin.getState().name();
        graphics2D.setColor(ACCENT);
        graphics2D.drawString(string, n + n3 - 8 - fontMetrics2.stringWidth(string), n6 + fontMetrics.getAscent());
        int n7 = n6 + fontMetrics.getAscent() + 4;
        graphics2D.setColor(new Color(BORDER.getRed(), BORDER.getGreen(), BORDER.getBlue(), 110));
        graphics2D.drawLine(n5, n7, n + n3 - 8, n7);
        n6 = n7 + 4 + fontMetrics2.getAscent();
        String string2 = this.plugin.getCurrentTask();
        String string3 = string2 == null || string2.isEmpty() ? "None" : SlayerBotOverlay.capitalise(string2) + "  x" + this.plugin.getKillsRemaining();
        graphics2D.setColor(LABEL);
        graphics2D.drawString("Task:", n5, n6);
        int n8 = fontMetrics2.stringWidth("Task:  ");
        graphics2D.setColor(VALUE);
        graphics2D.drawString(SlayerBotOverlay.fit(fontMetrics2, string3, n3 - 16 - n8), n5 + n8, n6);
        graphics2D.setColor(LABEL);
        graphics2D.drawString("Doing:", n5, n6 += fontMetrics2.getHeight());
        int n9 = fontMetrics2.stringWidth("Doing:  ");
        graphics2D.setColor(STATUS);
        graphics2D.drawString(SlayerBotOverlay.fit(fontMetrics2, this.plugin.getStatus(), n3 - 16 - n9), n5 + n9, n6);
        List<Stat> list = this.buildStats();
        int n10 = n6 + 8;
        int n11 = n3 - 16;
        int n12 = Math.max(2, Math.min(5, n11 / 90));
        int n13 = n11 / n12;
        int n14 = 25;
        for (int i = 0; i < list.size(); ++i) {
            object = list.get(i);
            int n15 = i % n12;
            int n16 = i / n12;
            int n17 = n5 + n15 * n13;
            int n18 = n10 + n16 * n14;
            graphics2D.setFont(F_LABEL);
            graphics2D.setColor(LABEL);
            graphics2D.drawString(((Stat)object).label, n17, n18 + 9);
            graphics2D.setFont(F_VALUE);
            graphics2D.setColor(((Stat)object).color);
            graphics2D.drawString(SlayerBotOverlay.fit(graphics2D.getFontMetrics(), ((Stat)object).value, n13 - 6), n17, n18 + 21);
        }
        try {
            Canvas canvas = this.client.getCanvas();
            object = canvas.getLocationOnScreen();
            Point point = MouseInfo.getPointerInfo().getLocation();
            String string4 = "xy " + (point.x - ((Point)object).x) + "," + (point.y - ((Point)object).y);
            graphics2D.setFont(F_SMALL);
            FontMetrics fontMetrics3 = graphics2D.getFontMetrics();
            graphics2D.setColor(FAINT);
            graphics2D.drawString(string4, n + n3 - 8 - fontMetrics3.stringWidth(string4), n2 + n4 - 5);
        }
        catch (Exception exception) {
            // empty catch block
        }
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, object2 != null ? object2 : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        return null;
    }

    private List<Stat> buildStats() {
        long l;
        Player player;
        int n;
        ArrayList<Stat> arrayList = new ArrayList<Stat>();
        arrayList.add(new Stat("Kills (task)", String.valueOf(this.plugin.getKillsThisTask()), VALUE));
        arrayList.add(new Stat("Total kills", String.valueOf(this.plugin.getTotalKills()), VALUE));
        int n2 = this.plugin.getWatchdogTrips();
        long l2 = this.plugin.getStalledSeconds();
        if (n2 > 0 || l2 > 30L) {
            arrayList.add(new Stat("Stalled", l2 + "s", n2 > 0 ? BAD : WARN));
            if (n2 > 0) {
                arrayList.add(new Stat("Recoveries", String.valueOf(n2), BAD));
            }
        }
        Object object = (n = this.config.stopAfterTasks()) > 0 ? this.plugin.getTasksCompleted() + "/" + n : String.valueOf(this.plugin.getTasksCompleted());
        arrayList.add(new Stat("Tasks done", (String)object, ACCENT));
        long l3 = this.plugin.getRuntimeMs();
        double d = (double)l3 / 3600000.0;
        int n3 = d > 0.01 ? (int)((double)this.plugin.getTotalKills() / d) : 0;
        arrayList.add(new Stat("Kills/hr", String.valueOf(n3), VALUE));
        arrayList.add(new Stat("Runtime", SlayerBotOverlay.fmtDuration(l3), VALUE));
        if (this.config.enableHighAlch()) {
            long l4 = this.plugin.getAlchProfit();
            arrayList.add(new Stat("Alch profit", SlayerBotOverlay.fmtGp(l4), ACCENT));
            long l5 = d > 0.01 ? (long)((double)l4 / d) : 0L;
            arrayList.add(new Stat("Alch GP/hr", SlayerBotOverlay.fmtGp(l5), VALUE));
        }
        if ((player = this.client.getLocalPlayer()) != null) {
            int n4;
            int n5 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
            int n6 = this.client.getRealSkillLevel(Skill.HITPOINTS);
            Color color = n6 > 0 && n5 * 100 / n6 < 40 ? BAD : GOOD;
            arrayList.add(new Stat("HP", n5 + "/" + n6, color));
            int n7 = this.client.getEnergy() / 100;
            arrayList.add(new Stat("Run", n7 + "%", n7 < 30 ? WARN : VALUE));
            if (this.config.enablePrayer()) {
                n4 = this.client.getBoostedSkillLevel(Skill.PRAYER);
                arrayList.add(new Stat("Prayer", n4 + " pts", n4 < 10 ? WARN : VALUE));
            }
            if (this.config.enableSpec()) {
                n4 = this.client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
                arrayList.add(new Stat("Spec", n4 + "%", VALUE));
            }
        }
        if ((l = this.plugin.getHumanizedActions()) > 0L) {
            long l6 = this.plugin.getInputFallbacks();
            arrayList.add(new Stat("Real inputs", (String)(l >= 1000L ? l / 1000L + "k" : String.valueOf(l)), l6 > 0L ? WARN : GOOD));
            if (l6 > 0L) {
                arrayList.add(new Stat("Fallbacks", String.valueOf(l6), WARN));
            }
        }
        return arrayList;
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

    private static String fmtGp(long l) {
        if (l >= 1000000L) {
            return String.format("%.2fM", (double)l / 1000000.0);
        }
        if (l >= 10000L) {
            return l / 1000L + "k";
        }
        if (l >= 1000L) {
            return String.format("%.1fk", (double)l / 1000.0);
        }
        return String.valueOf(l);
    }

    private static String fmtDuration(long l) {
        long l2 = l / 1000L;
        long l3 = l2 / 3600L;
        long l4 = (l2 %= 3600L) / 60L;
        l2 %= 60L;
        if (l3 > 0L) {
            return l3 + "h" + String.format("%02dm", l4);
        }
        if (l4 > 0L) {
            return l4 + "m" + String.format("%02ds", l2);
        }
        return l2 + "s";
    }

    private static String fit(FontMetrics fontMetrics, String string, int n) {
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

    private static String capitalise(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String string2 : string.split("\\s+")) {
            if (string2.isEmpty()) continue;
            stringBuilder.append(Character.toUpperCase(string2.charAt(0))).append(string2.substring(1).toLowerCase()).append(' ');
        }
        return stringBuilder.toString().trim();
    }

    private static final class Stat {
        final String label;
        final String value;
        final Color color;

        Stat(String string, String string2, Color color) {
            this.label = string;
            this.value = string2;
            this.color = color;
        }
    }
}

