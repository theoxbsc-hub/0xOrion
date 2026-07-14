/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 */
package net.runelite.client.plugins.svc.foundation;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.svc.foundation.WidgetInspectorConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

class WidgetInspectorOverlay
extends Overlay {
    private static final Font LABEL_FONT = new Font("Arial", 1, 10);
    private static final Color BG = new Color(0, 0, 0, 160);
    private static final Color ROOT_COL = new Color(255, 220, 0);
    private static final Color CHILD_COL = new Color(255, 255, 255);
    private static final Color DYN_COL = new Color(0, 220, 255);
    private static final Color HIDDEN_COL = new Color(120, 120, 120);
    private static final Color ITEM_COL = new Color(180, 255, 120);
    private static final Color HOVER_COL = new Color(255, 80, 80);
    private static final Color CLICK_COL = new Color(100, 200, 255);
    private static final Color DEEP_COL = new Color(255, 160, 50);
    private final Client client;
    private final WidgetInspectorConfig config;
    private final ConfigManager configManager;
    private final List<int[]> clickRects = new ArrayList<int[]>();
    private Rectangle backBtnRect = null;

    WidgetInspectorOverlay(Client client, WidgetInspectorConfig widgetInspectorConfig, ConfigManager configManager) {
        this.client = client;
        this.config = widgetInspectorConfig;
        this.configManager = configManager;
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.DYNAMIC);
    }

    void handleMouseClicked(int n, int n2) {
        if (this.backBtnRect != null && this.backBtnRect.contains(n, n2)) {
            this.configManager.setConfiguration("orionWidgetInspector", "inspectId", (Object)-1);
            return;
        }
        for (int[] nArray : this.clickRects) {
            if (n < nArray[0] || n2 < nArray[1] || n >= nArray[0] + nArray[2] || n2 >= nArray[1] + nArray[3]) continue;
            this.configManager.setConfiguration("orionWidgetInspector", "inspectId", (Object)nArray[4]);
            return;
        }
    }

    public Dimension render(Graphics2D graphics2D) {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        graphics2D.setFont(LABEL_FONT);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        this.clickRects.clear();
        this.backBtnRect = null;
        Point point = this.getMouseCanvasPos();
        this.drawMousePosition(graphics2D, fontMetrics, point);
        int n = this.config.inspectId();
        if (n < 0) {
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            Widget[][] widgetArray = this.client.getWidgets();
            if (widgetArray != null) {
                int n2 = Math.min(widgetArray.length, this.config.maxScanId() + 1);
                block0: for (int i = 0; i < n2; ++i) {
                    Widget[] widgetArray2 = widgetArray[i];
                    if (widgetArray2 == null || widgetArray2.length == 0) continue;
                    boolean bl = false;
                    for (Widget widget : widgetArray2) {
                        if (widget == null || widget.isHidden()) continue;
                        bl = true;
                        break;
                    }
                    if (!bl) continue;
                    arrayList.add(i);
                    for (Widget widget : widgetArray2) {
                        if (widget == null) continue;
                        this.drawLabel(graphics2D, fontMetrics, widget, "I:" + i, ROOT_COL);
                        continue block0;
                    }
                }
            }
            this.drawSummaryBox(graphics2D, fontMetrics, arrayList, point);
        } else {
            Widget widget;
            String string;
            int n3;
            Object object;
            Widget[][] widgetArray = this.client.getWidgets();
            if (widgetArray == null || n >= widgetArray.length || widgetArray[n] == null) {
                this.drawNotice(graphics2D, fontMetrics, "Interface " + n + " not loaded");
                return null;
            }
            Widget[] widgetArray3 = widgetArray[n];
            boolean bl = this.config.textOnly();
            int n4 = bl ? Integer.MAX_VALUE : this.config.maxChildren();
            int n5 = 0;
            int n6 = 0;
            for (int i = 0; i < widgetArray3.length; ++i) {
                object = widgetArray3[i];
                if (object == null) continue;
                ++n6;
                n3 = object.isHidden();
                if (n3 != 0 && !this.config.showHidden()) continue;
                String string2 = string = object.getText() != null ? object.getText() : "";
                if (bl && string.isEmpty() && !this.hasItem((Widget)object) || n5 >= n4) continue;
                String string3 = this.buildChildLabel("" + i, (Widget)object);
                Color color = n3 != 0 ? HIDDEN_COL : (this.hasItem((Widget)object) ? ITEM_COL : CHILD_COL);
                this.drawLabel(graphics2D, fontMetrics, (Widget)object, string3, color);
                ++n5;
            }
            this.drawDetailHeader(graphics2D, fontMetrics, n, n5, n6, bl, point);
            Widget widget2 = widget = point != null ? this.findHoveredWidget(point.x, point.y, n) : null;
            if (widget != null) {
                this.drawHoveredBox(graphics2D, fontMetrics, widget, point);
            }
            object = new ArrayList();
            for (n3 = 0; n3 < widgetArray3.length; ++n3) {
                string = widgetArray3[n3];
                if (string == null) continue;
                this.deepCollectText((Widget)string, n + ":" + n3, (List<String[]>)object, 0);
            }
            this.drawDeepScanPanel(graphics2D, fontMetrics, n, (List<String[]>)object);
        }
        return null;
    }

    /*
     * WARNING - void declaration
     */
    private void deepCollectText(Widget widget, String string, List<String[]> list, int n) {
        Widget[] widgetArray;
        Widget[] widgetArray2;
        boolean bl;
        String string2;
        if (widget == null || n > 10) {
            return;
        }
        String string3 = widget.getText() != null ? widget.getText().replaceAll("<[^>]*>", "").trim() : "";
        String[] stringArray = widget.getActions();
        String string4 = "";
        if (stringArray != null) {
            for (String widgetArray22 : stringArray) {
                if (widgetArray22 == null || widgetArray22.isEmpty()) continue;
                string4 = widgetArray22;
                break;
            }
        }
        String string5 = string2 = (bl = this.hasItem(widget)) ? "item:" + widget.getItemId() : "";
        if (!string3.isEmpty() || !string4.isEmpty() || bl) {
            list.add(new String[]{string, string3, string4, string2});
        }
        if ((widgetArray2 = widget.getChildren()) != null) {
            void var11_16;
            boolean i = false;
            while (var11_16 < widgetArray2.length) {
                this.deepCollectText(widgetArray2[var11_16], string + ".c" + (int)var11_16, list, n + 1);
                ++var11_16;
            }
        }
        if ((widgetArray = widget.getDynamicChildren()) != null) {
            for (int i = 0; i < widgetArray.length; ++i) {
                this.deepCollectText(widgetArray[i], string + ".d" + i, list, n + 1);
            }
        }
    }

    private void drawDeepScanPanel(Graphics2D graphics2D, FontMetrics fontMetrics, int n, List<String[]> list) {
        int n2;
        int n3 = fontMetrics.getHeight() + 1;
        String string = "=== I:" + n + " deep scan (text/action widgets) ===";
        if (list.isEmpty()) {
            int n4 = fontMetrics.stringWidth(string) + 10;
            int n5 = n3 * 2 + 10;
            int n6 = 6;
            int n7 = 130;
            graphics2D.setColor(new Color(0, 0, 0, 200));
            graphics2D.fillRoundRect(n6, n7, n4, n5, 4, 4);
            graphics2D.setColor(DEEP_COL);
            graphics2D.drawRoundRect(n6, n7, n4, n5, 4, 4);
            graphics2D.drawString(string, n6 + 5, n7 + 5 + fontMetrics.getAscent());
            graphics2D.setColor(Color.GRAY);
            graphics2D.drawString("No text/action widgets found anywhere in tree", n6 + 5, n7 + 5 + fontMetrics.getAscent() + n3);
            return;
        }
        int n8 = fontMetrics.stringWidth(string);
        int n9 = Math.min(list.size(), 50);
        for (n2 = 0; n2 < n9; ++n2) {
            String[] stringArray = list.get(n2);
            String string2 = stringArray[0] + "  " + (String)(stringArray[1].isEmpty() ? "" : "\"" + WidgetInspectorOverlay.truncate(stringArray[1], 25) + "\"  ") + (String)(stringArray[2].isEmpty() ? "" : "[" + stringArray[2] + "]  ") + stringArray[3];
            n8 = Math.max(n8, fontMetrics.stringWidth(string2));
        }
        n8 += 10;
        n2 = 5 + n3 + 5 + n9 * n3 + (list.size() > n9 ? n3 : 0) + 5;
        int n10 = 760;
        try {
            n10 = this.client.getCanvas().getWidth();
        }
        catch (Exception exception) {
            // empty catch block
        }
        int n11 = Math.max(160, n10 - n8 - 6);
        int n12 = 6;
        graphics2D.setColor(new Color(0, 0, 0, 215));
        graphics2D.fillRoundRect(n11, n12, n8, n2, 6, 6);
        graphics2D.setColor(DEEP_COL);
        graphics2D.drawRoundRect(n11, n12, n8, n2, 6, 6);
        int n13 = n12 + 5 + fontMetrics.getAscent();
        graphics2D.setColor(DEEP_COL);
        graphics2D.drawString(string, n11 + 5, n13);
        n13 += n3 + 5;
        for (int i = 0; i < n9; ++i) {
            String string3;
            String[] stringArray = list.get(i);
            String string4 = stringArray[0];
            String string5 = stringArray[1];
            String string6 = stringArray[2];
            String string7 = stringArray[3];
            int n14 = n11 + 5;
            graphics2D.setColor(ROOT_COL);
            graphics2D.drawString(string4, n14, n13);
            n14 += fontMetrics.stringWidth(string4) + 4;
            if (!string5.isEmpty()) {
                string3 = "\"" + WidgetInspectorOverlay.truncate(string5, 25) + "\"";
                graphics2D.setColor(Color.WHITE);
                graphics2D.drawString(string3, n14, n13);
                n14 += fontMetrics.stringWidth(string3) + 4;
            }
            if (!string6.isEmpty()) {
                string3 = "[" + string6 + "]";
                graphics2D.setColor(new Color(150, 220, 255));
                graphics2D.drawString(string3, n14, n13);
                n14 += fontMetrics.stringWidth(string3) + 4;
            }
            if (!string7.isEmpty()) {
                graphics2D.setColor(ITEM_COL);
                graphics2D.drawString(string7, n14, n13);
            }
            n13 += n3;
        }
        if (list.size() > n9) {
            graphics2D.setColor(Color.GRAY);
            graphics2D.drawString("  ...+" + (list.size() - n9) + " more (enable Show Hidden to see all)", n11 + 5, n13);
        }
    }

    private boolean hasItem(Widget widget) {
        return widget.getItemId() != -1 && widget.getItemId() != 0;
    }

    private String buildChildLabel(String string, Widget widget) {
        StringBuilder stringBuilder = new StringBuilder(string);
        String string2 = WidgetInspectorOverlay.trimText(widget.getText());
        if (!string2.isEmpty()) {
            stringBuilder.append(": \"").append(string2).append("\"");
        }
        if (this.hasItem(widget)) {
            stringBuilder.append(" item:").append(widget.getItemId());
            if (widget.getItemQuantity() > 1) {
                stringBuilder.append("x").append(widget.getItemQuantity());
            }
        }
        return stringBuilder.toString();
    }

    private Widget findHoveredWidget(int n, int n2, int n3) {
        Widget widget = null;
        int n4 = Integer.MAX_VALUE;
        Widget[][] widgetArray = this.client.getWidgets();
        if (widgetArray == null || n3 < 0 || n3 >= widgetArray.length) {
            return null;
        }
        Widget[] widgetArray2 = widgetArray[n3];
        if (widgetArray2 == null) {
            return null;
        }
        for (Widget widget2 : widgetArray2) {
            int n5;
            Rectangle rectangle;
            Widget widget3;
            if (widget2 == null || (widget3 = this.findInWidget(widget2, n, n2, n4)) == null || (rectangle = widget3.getBounds()) == null || (n5 = rectangle.width * rectangle.height) >= n4) continue;
            widget = widget3;
            n4 = n5;
        }
        return widget;
    }

    private Widget findInWidget(Widget widget, int n, int n2, int n3) {
        Rectangle rectangle;
        Widget[] widgetArray;
        if (widget == null || widget.isHidden()) {
            return null;
        }
        Rectangle rectangle2 = widget.getBounds();
        if (rectangle2 == null || !rectangle2.contains(n, n2)) {
            return null;
        }
        int n4 = rectangle2.width * rectangle2.height;
        if (n4 >= n3) {
            return null;
        }
        Object object = widget;
        Widget[] widgetArray2 = widget.getChildren();
        if (widgetArray2 != null) {
            widgetArray = widgetArray2;
            int n5 = widgetArray.length;
            for (int i = 0; i < n5; ++i) {
                Widget widget2 = widgetArray[i];
                Widget widget3 = this.findInWidget(widget2, n, n2, n4);
                if (widget3 == null || (rectangle = widget3.getBounds()) == null || rectangle.width * rectangle.height >= n4) continue;
                object = widget3;
                n4 = rectangle.width * rectangle.height;
            }
        }
        if ((widgetArray = widget.getDynamicChildren()) != null) {
            for (Widget widget3 : widgetArray) {
                Rectangle rectangle3;
                rectangle = this.findInWidget(widget3, n, n2, n4);
                if (rectangle == null || (rectangle3 = rectangle.getBounds()) == null || rectangle3.width * rectangle3.height >= n4) continue;
                object = rectangle;
                n4 = rectangle3.width * rectangle3.height;
            }
        }
        return object;
    }

    private void drawLabel(Graphics2D graphics2D, FontMetrics fontMetrics, Widget widget, String string, Color color) {
        Rectangle rectangle = widget.getBounds();
        if (rectangle == null || rectangle.width < 4 || rectangle.height < 4) {
            return;
        }
        graphics2D.setColor(color.darker());
        graphics2D.drawRect(rectangle.x, rectangle.y, rectangle.width - 1, rectangle.height - 1);
        if (rectangle.width < 20 || rectangle.height < 14) {
            return;
        }
        String string2 = this.fitText(fontMetrics, string, rectangle.width - 4);
        int n = fontMetrics.getHeight();
        int n2 = fontMetrics.stringWidth(string2);
        int n3 = rectangle.x + 1;
        int n4 = rectangle.y + 1;
        graphics2D.setColor(BG);
        graphics2D.fillRect(n3, n4, n2 + 4, n);
        graphics2D.setColor(color);
        graphics2D.drawString(string2, n3 + 2, n4 + n - 3);
    }

    private String fitText(FontMetrics fontMetrics, String string, int n) {
        int n2;
        if (fontMetrics.stringWidth(string) <= n) {
            return string;
        }
        for (n2 = string.length(); n2 > 4 && fontMetrics.stringWidth(string.substring(0, n2) + "\u2026") > n; --n2) {
        }
        return n2 > 0 ? string.substring(0, n2) + "\u2026" : string.substring(0, Math.min(4, string.length()));
    }

    private void drawSummaryBox(Graphics2D graphics2D, FontMetrics fontMetrics, List<Integer> list, Point point) {
        int n;
        int n22;
        if (list.isEmpty()) {
            return;
        }
        int n3 = fontMetrics.getHeight();
        String string = "Widget Inspector \u2014 Open Interfaces";
        String string2 = "Click an interface ID to inspect it";
        int n4 = 0;
        for (int n22 : list) {
            n = fontMetrics.stringWidth("  \u2192 " + n22 + "  ");
            if (n <= n4) continue;
            n4 = n;
        }
        int n5 = Math.max(fontMetrics.stringWidth(string), Math.max(fontMetrics.stringWidth(string2), n4)) + 14;
        n22 = n3 + 4;
        n = 7 + n3 + 4 + n3 + 7 + list.size() * n22 + 7;
        int n6 = 6;
        int n7 = 6;
        graphics2D.setColor(new Color(0, 0, 0, 215));
        graphics2D.fillRoundRect(n6, n7, n5, n, 8, 8);
        graphics2D.setColor(ROOT_COL);
        graphics2D.drawRoundRect(n6, n7, n5, n, 8, 8);
        int n8 = n7 + 7 + fontMetrics.getAscent();
        graphics2D.setColor(ROOT_COL);
        graphics2D.drawString(string, n6 + 7, n8);
        graphics2D.setColor(Color.LIGHT_GRAY);
        graphics2D.drawString(string2, n6 + 7, n8 += n3 + 4);
        n8 += 7;
        for (int n9 : list) {
            boolean bl;
            int n10 = n8;
            int n11 = n10 + n22;
            boolean bl2 = bl = point != null && point.x >= n6 + 1 && point.x < n6 + n5 - 1 && point.y >= n10 && point.y < n11;
            if (bl) {
                graphics2D.setColor(new Color(80, 160, 255, 70));
                graphics2D.fillRect(n6 + 1, n10, n5 - 2, n22);
            }
            graphics2D.setColor(bl ? CLICK_COL : Color.WHITE);
            graphics2D.drawString("  \u2192 " + n9, n6 + 7, n10 + fontMetrics.getAscent() + 2);
            this.clickRects.add(new int[]{n6 + 1, n10, n5 - 2, n22, n9});
            n8 += n22;
        }
    }

    private void drawDetailHeader(Graphics2D graphics2D, FontMetrics fontMetrics, int n, int n2, int n3, boolean bl, Point point) {
        int n4 = fontMetrics.getHeight();
        String string = "Widget Inspector  [interface " + n + "]";
        String string2 = n2 + " shown / " + n3 + " direct children" + (bl ? "  [TEXT ONLY]" : "");
        String string3 = "yel=root  wht=child  cyn=dyn  grn=item  gry=hidden";
        String string4 = "  \u2190  Back to Overview";
        int n5 = fontMetrics.stringWidth(string4) + 14 + 4;
        int n6 = Math.max(fontMetrics.stringWidth(string), Math.max(fontMetrics.stringWidth(string2), Math.max(fontMetrics.stringWidth(string3), n5))) + 14;
        int n7 = 7 + n4 * 3 + 8;
        int n8 = n4 + 6;
        int n9 = n7 + n8 + 7;
        int n10 = 6;
        int n11 = 6;
        graphics2D.setColor(new Color(0, 0, 0, 215));
        graphics2D.fillRoundRect(n10, n11, n6, n9, 8, 8);
        graphics2D.setColor(CHILD_COL);
        graphics2D.drawRoundRect(n10, n11, n6, n9, 8, 8);
        int n12 = n11 + 7 + fontMetrics.getAscent();
        graphics2D.setColor(ROOT_COL);
        graphics2D.drawString(string, n10 + 7, n12);
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString(string2, n10 + 7, n12 += n4);
        graphics2D.setColor(Color.LIGHT_GRAY);
        graphics2D.drawString(string3, n10 + 7, n12 += n4);
        int n13 = n10 + 7 - 2;
        int n14 = n11 + n7;
        int n15 = n6 - 14 + 4;
        this.backBtnRect = new Rectangle(n13, n14, n15, n8);
        boolean bl2 = point != null && this.backBtnRect.contains(point.x, point.y);
        graphics2D.setColor(bl2 ? new Color(60, 120, 220, 120) : new Color(30, 30, 60, 120));
        graphics2D.fillRoundRect(n13, n14, n15, n8, 5, 5);
        graphics2D.setColor(bl2 ? CLICK_COL : new Color(100, 160, 255));
        graphics2D.drawRoundRect(n13, n14, n15, n8, 5, 5);
        graphics2D.setColor(bl2 ? Color.WHITE : CLICK_COL);
        graphics2D.drawString(string4, n13 + 7, n14 + fontMetrics.getAscent() + 3);
    }

    private void drawHoveredBox(Graphics2D graphics2D, FontMetrics fontMetrics, Widget widget, Point point) {
        Widget[] widgetArray;
        Widget[] widgetArray2;
        String[] stringArray;
        Rectangle rectangle = widget.getBounds();
        if (rectangle != null) {
            graphics2D.setColor(HOVER_COL);
            Stroke stroke = graphics2D.getStroke();
            graphics2D.setStroke(new BasicStroke(2.0f));
            graphics2D.drawRect(rectangle.x, rectangle.y, rectangle.width - 1, rectangle.height - 1);
            graphics2D.setStroke(stroke);
        }
        int n = widget.getId() >> 16;
        int n2 = widget.getId() & 0xFFFF;
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add("== HOVERED WIDGET ==");
        arrayList.add("group:" + n + "  child:" + n2);
        arrayList.add("packed ID: " + widget.getId());
        String string = WidgetInspectorOverlay.trimText(widget.getText());
        if (!string.isEmpty()) {
            arrayList.add("Text: \"" + string + "\"");
        }
        if (this.hasItem(widget)) {
            arrayList.add("Item: " + widget.getItemId() + "  (" + this.getItemName(widget.getItemId()) + ")");
            if (widget.getItemQuantity() > 0) {
                arrayList.add("Qty:  " + widget.getItemQuantity());
            }
        }
        if ((stringArray = widget.getActions()) != null) {
            for (int i = 0; i < stringArray.length; ++i) {
                if (stringArray[i] == null || stringArray[i].isEmpty()) continue;
                arrayList.add("Action[" + i + "]: " + stringArray[i]);
            }
        }
        if ((widgetArray2 = widget.getChildren()) != null) {
            arrayList.add("getChildren().length: " + widgetArray2.length);
        }
        if ((widgetArray = widget.getDynamicChildren()) != null) {
            arrayList.add("getDynamicChildren().length: " + widgetArray.length);
        }
        if (widget.getModelId() > 0) {
            arrayList.add("Model: " + widget.getModelId());
        }
        arrayList.add("Type: " + widget.getType() + "  ContentType: " + widget.getContentType());
        if (rectangle != null) {
            arrayList.add("Bounds: " + rectangle.x + "," + rectangle.y + "  " + rectangle.width + "x" + rectangle.height);
        }
        int n3 = fontMetrics.getHeight();
        int n4 = arrayList.stream().mapToInt(fontMetrics::stringWidth).max().orElse(100) + 12;
        int n5 = n3 * arrayList.size() + 8;
        int n6 = point.x + 14;
        int n7 = point.y - n5 / 2;
        try {
            Canvas canvas = this.client.getCanvas();
            if (n6 + n4 > canvas.getWidth()) {
                n6 = point.x - n4 - 4;
            }
            if (n7 < 4) {
                n7 = 4;
            }
            if (n7 + n5 > canvas.getHeight()) {
                n7 = canvas.getHeight() - n5 - 4;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        graphics2D.setColor(new Color(0, 0, 0, 215));
        graphics2D.fillRoundRect(n6, n7, n4, n5, 6, 6);
        graphics2D.setColor(HOVER_COL);
        graphics2D.drawRoundRect(n6, n7, n4, n5, 6, 6);
        int n8 = n7 + fontMetrics.getAscent() + 4;
        for (int i = 0; i < arrayList.size(); ++i) {
            Color color = i == 0 ? HOVER_COL : (i == 1 ? ROOT_COL : Color.WHITE);
            graphics2D.setColor(color);
            graphics2D.drawString((String)arrayList.get(i), n6 + 6, n8);
            n8 += n3;
        }
    }

    private String getItemName(int n) {
        try {
            ItemComposition itemComposition = this.client.getItemDefinition(n);
            return itemComposition != null ? itemComposition.getName() : "?";
        }
        catch (Exception exception) {
            return "?";
        }
    }

    private void drawNotice(Graphics2D graphics2D, FontMetrics fontMetrics, String string) {
        int n = fontMetrics.stringWidth(string) + 10;
        graphics2D.setColor(BG);
        graphics2D.fillRoundRect(6, 6, n, fontMetrics.getHeight() + 8, 6, 6);
        graphics2D.setColor(Color.RED);
        graphics2D.drawString(string, 11, 6 + fontMetrics.getHeight());
    }

    private void drawMousePosition(Graphics2D graphics2D, FontMetrics fontMetrics, Point point) {
        int n;
        if (point == null) {
            return;
        }
        String string = "Mouse: " + point.x + ", " + point.y + "  (canvas px)";
        int n2 = fontMetrics.stringWidth(string) + 10;
        int n3 = fontMetrics.getHeight() + 4;
        try {
            n = this.client.getCanvas().getHeight() - n3 - 4;
        }
        catch (Exception exception) {
            n = 500;
        }
        graphics2D.setColor(new Color(0, 0, 0, 200));
        graphics2D.fillRoundRect(4, n, n2, n3, 4, 4);
        graphics2D.setColor(Color.GREEN);
        graphics2D.drawRoundRect(4, n, n2, n3, 4, 4);
        graphics2D.drawString(string, 9, n + fontMetrics.getAscent() + 2);
    }

    private Point getMouseCanvasPos() {
        try {
            Canvas canvas = this.client.getCanvas();
            Point point = canvas.getLocationOnScreen();
            Point point2 = MouseInfo.getPointerInfo().getLocation();
            return new Point(point2.x - point.x, point2.y - point.y);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static String trimText(String string) {
        if (string == null) {
            return "";
        }
        String string2 = string.replaceAll("<[^>]*>", "");
        return string2.length() > 30 ? string2.substring(0, 27) + "..." : string2;
    }

    private static String truncate(String string, int n) {
        if (string == null) {
            return "";
        }
        return string.length() <= n ? string : string.substring(0, n - 1) + "\u2026";
    }
}

