/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 */
package net.runelite.client.plugins.svc.foundation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

class EntityInspectorInventoryOverlay
extends Overlay {
    private static final Font LABEL_FONT = new Font("Arial", 1, 9);
    private static final Color BG = new Color(0, 0, 0, 180);
    private static final Color TEXT_COL = new Color(255, 220, 80);
    private static final int INV_GROUP = 149;
    private static final int INV_CHILD = 0;
    private final Client client;

    EntityInspectorInventoryOverlay(Client client) {
        this.client = client;
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.DYNAMIC);
    }

    public Dimension render(Graphics2D graphics2D) {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return null;
        }
        Widget widget = this.client.getWidget(149, 0);
        if (widget == null || widget.isHidden()) {
            return null;
        }
        Widget[] widgetArray = widget.getDynamicChildren();
        if (widgetArray == null) {
            return null;
        }
        Item[] itemArray = itemContainer.getItems();
        graphics2D.setFont(LABEL_FONT);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        for (int i = 0; i < widgetArray.length && i < itemArray.length; ++i) {
            Rectangle rectangle;
            Item item = itemArray[i];
            if (item == null || item.getId() == -1 || (rectangle = widgetArray[i].getBounds()) == null || rectangle.width < 4) continue;
            String string = String.valueOf(item.getId());
            int n = fontMetrics.stringWidth(string);
            int n2 = fontMetrics.getHeight();
            int n3 = rectangle.x + (rectangle.width - n) / 2;
            int n4 = rectangle.y + n2 - 2;
            graphics2D.setColor(BG);
            graphics2D.fillRect(n3 - 1, n4 - n2 + 2, n + 2, n2 - 1);
            graphics2D.setColor(TEXT_COL);
            graphics2D.drawString(string, n3, n4);
        }
        return null;
    }
}

