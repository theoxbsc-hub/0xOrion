/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.MenuEntry
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetItem
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 */
package net.unethicalite.client.devtools.widgetinspector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.unethicalite.client.devtools.widgetinspector.WidgetInspector;

public class WidgetInspectorOverlay
extends Overlay {
    private final Client client;
    private final WidgetInspector inspector;

    @Inject
    public WidgetInspectorOverlay(Client client, WidgetInspector inspector) {
        this.client = client;
        this.inspector = inspector;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGHEST);
    }

    public Dimension render(Graphics2D g) {
        Widget w = this.inspector.getSelectedWidget();
        if (w != null) {
            Widget wiw = w;
            if (this.inspector.getSelectedItem() != -1) {
                wiw = w.getWidgetItem(this.inspector.getSelectedItem());
            }
            this.renderWiw(g, wiw, WidgetInspector.SELECTED_WIDGET_COLOR);
        }
        if (this.inspector.isPickerSelected()) {
            int i;
            boolean menuOpen = this.client.isMenuOpen();
            MenuEntry[] entries = this.client.getMenuEntries();
            int n = i = menuOpen ? 0 : entries.length - 1;
            while (i < entries.length) {
                MenuEntry e = entries[i];
                Object wiw = this.inspector.getWidgetOrWidgetItemForMenuOption(e.getOpcode(), e.getParam0(), e.getParam1());
                if (wiw != null) {
                    Color color = this.inspector.colorForWidget(i, entries.length);
                    this.renderWiw(g, wiw, color);
                }
                ++i;
            }
        }
        return null;
    }

    private void renderWiw(Graphics2D g, Object wiw, Color color) {
        g.setColor(color);
        if (wiw instanceof WidgetItem) {
            WidgetItem wi = (WidgetItem)wiw;
            Rectangle bounds = wi.getCanvasBounds();
            g.draw(bounds);
            String text = "" + wi.getId();
            FontMetrics fm = g.getFontMetrics();
            Rectangle2D textBounds = fm.getStringBounds(text, g);
            int textX = (int)(bounds.getX() + bounds.getWidth() / 2.0 - textBounds.getWidth() / 2.0);
            int textY = (int)(bounds.getY() + bounds.getHeight() / 2.0 + textBounds.getHeight() / 2.0);
            g.setColor(Color.BLACK);
            g.drawString(text, textX + 1, textY + 1);
            g.setColor(Color.ORANGE);
            g.drawString(text, textX, textY);
        } else {
            Widget w = (Widget)wiw;
            g.draw(w.getBounds());
        }
    }
}

