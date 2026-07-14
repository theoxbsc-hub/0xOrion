package net.runelite.client.plugins.ox.debug;

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

class DebugInventoryOverlay extends Overlay {
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 9);
    private static final Color BG = new Color(0, 0, 0, 180);
    private static final Color TEXT_COL = new Color(0xB4, 0x4D, 0xFF);
    private static final int INV_GROUP = 149;
    private static final int INV_CHILD = 0;

    private final Client client;

    DebugInventoryOverlay(Client client) {
        this.client = client;
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        ItemContainer inv = this.client.getItemContainer(InventoryID.INVENTORY);
        if (inv == null) {
            return null;
        }
        Widget widget = this.client.getWidget(INV_GROUP, INV_CHILD);
        if (widget == null || widget.isHidden()) {
            return null;
        }
        Widget[] slots = widget.getDynamicChildren();
        if (slots == null) {
            return null;
        }
        Item[] items = inv.getItems();
        g.setFont(LABEL_FONT);
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < slots.length && i < items.length; ++i) {
            Item item = items[i];
            if (item == null || item.getId() == -1) {
                continue;
            }
            Rectangle bounds = slots[i].getBounds();
            if (bounds == null || bounds.width < 4) {
                continue;
            }
            String label = String.valueOf(item.getId());
            int w = fm.stringWidth(label);
            int h = fm.getHeight();
            int x = bounds.x + (bounds.width - w) / 2;
            int y = bounds.y + h - 2;
            g.setColor(BG);
            g.fillRect(x - 1, y - h + 2, w + 2, h - 1);
            g.setColor(TEXT_COL);
            g.drawString(label, x, y);
        }
        return null;
    }
}
