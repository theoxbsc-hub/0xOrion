/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.client.devtools.widgetinspector;

import javax.swing.tree.DefaultMutableTreeNode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.client.devtools.widgetinspector.WidgetInspector;

class WidgetTreeNode
extends DefaultMutableTreeNode {
    private final String type;

    public WidgetTreeNode(String type, Widget widget) {
        super(widget);
        this.type = type;
    }

    public Widget getWidget() {
        return (Widget)this.getUserObject();
    }

    @Override
    public String toString() {
        WidgetInfo info;
        Widget widget = this.getWidget();
        int id = widget.getId();
        String str = this.type + " " + WidgetInfo.TO_GROUP((int)id) + "." + WidgetInfo.TO_CHILD((int)id);
        if (widget.getIndex() != -1) {
            str = str + "[" + widget.getIndex() + "]";
        }
        if ((info = WidgetInspector.getWidgetInfo(id)) != null) {
            str = str + " " + info.name();
        }
        return str;
    }
}

