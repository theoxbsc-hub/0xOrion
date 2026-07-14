/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.widgets.WidgetItem
 */
package net.unethicalite.client.devtools.widgetinspector;

import javax.swing.tree.DefaultMutableTreeNode;
import net.runelite.api.widgets.WidgetItem;

class WidgetItemNode
extends DefaultMutableTreeNode {
    private final WidgetItem widgetItem;

    public WidgetItemNode(WidgetItem widgetItem) {
        super(widgetItem);
        this.widgetItem = widgetItem;
    }

    public WidgetItem getWidgetItem() {
        return this.widgetItem;
    }

    @Override
    public String toString() {
        return "I " + this.widgetItem.getIndex();
    }
}

