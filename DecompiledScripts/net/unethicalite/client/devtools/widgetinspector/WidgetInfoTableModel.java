/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.inject.Inject
 *  net.runelite.api.Point
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.callback.ClientThread
 */
package net.unethicalite.client.devtools.widgetinspector;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.unethicalite.client.devtools.widgetinspector.WidgetField;

class WidgetInfoTableModel
extends AbstractTableModel {
    private static final int COL_FIELD = 0;
    private static final int COL_VALUE = 1;
    private static final List<WidgetField> fields = WidgetInfoTableModel.populateWidgetFields();
    @Inject
    private ClientThread clientThread;
    private Widget widget = null;
    private Map<WidgetField, Object> values = null;

    WidgetInfoTableModel() {
    }

    private static List<WidgetField> populateWidgetFields() {
        ArrayList<WidgetField> out = new ArrayList<WidgetField>();
        out.add(new WidgetField<Integer>("Id", Widget::getId));
        out.add(new WidgetField<String[]>("Actions", Widget::getActions));
        out.add(new WidgetField<Integer>("Type", Widget::getType, Widget::setType, Integer.class));
        out.add(new WidgetField<Integer>("ContentType", Widget::getContentType, Widget::setContentType, Integer.class));
        out.add(new WidgetField<Integer>("ParentId", Widget::getParentId));
        out.add(new WidgetField<Boolean>("SelfHidden", Widget::isSelfHidden, Widget::setHidden, Boolean.class));
        out.add(new WidgetField<Boolean>("Hidden", Widget::isHidden));
        out.add(new WidgetField<String>("Text", Widget::getText, Widget::setText, String.class));
        out.add(new WidgetField<String>("TextColor", w -> Integer.toString(w.getTextColor(), 16), (w, str) -> w.setTextColor(Integer.parseInt(str, 16)), String.class));
        out.add(new WidgetField<Integer>("Opacity", Widget::getOpacity, Widget::setOpacity, Integer.class));
        out.add(new WidgetField<Integer>("FontId", Widget::getFontId, Widget::setFontId, Integer.class));
        out.add(new WidgetField<Boolean>("TextShadowed", Widget::getTextShadowed, Widget::setTextShadowed, Boolean.class));
        out.add(new WidgetField<String>("Name", w -> w.getName().trim(), Widget::setName, String.class));
        out.add(new WidgetField<Integer>("ItemId", Widget::getItemId, Widget::setItemId, Integer.class));
        out.add(new WidgetField<Integer>("ItemQuantity", Widget::getItemQuantity, Widget::setItemQuantity, Integer.class));
        out.add(new WidgetField<Integer>("ItemQuantityMode", Widget::getItemQuantityMode, Widget::setItemQuantityMode, Integer.class));
        out.add(new WidgetField<Integer>("ModelId", Widget::getModelId, Widget::setModelId, Integer.class));
        out.add(new WidgetField<Integer>("ModelType", Widget::getModelType, Widget::setModelType, Integer.class));
        out.add(new WidgetField<Integer>("RotationX", Widget::getRotationX, Widget::setRotationX, Integer.class));
        out.add(new WidgetField<Integer>("RotationY", Widget::getRotationY, Widget::setRotationY, Integer.class));
        out.add(new WidgetField<Integer>("RotationZ", Widget::getRotationZ, Widget::setRotationZ, Integer.class));
        out.add(new WidgetField<Integer>("ModelZoom", Widget::getModelZoom, Widget::setModelZoom, Integer.class));
        out.add(new WidgetField<Integer>("SpriteId", Widget::getSpriteId, Widget::setSpriteId, Integer.class));
        out.add(new WidgetField<Integer>("BorderType", Widget::getBorderType, Widget::setBorderType, Integer.class));
        out.add(new WidgetField<Boolean>("IsIf3", Widget::isIf3));
        out.add(new WidgetField<Boolean>("HasListener", Widget::hasListener, Widget::setHasListener, Boolean.class));
        out.add(new WidgetField<Boolean>("Filled", Widget::isFilled, Widget::setFilled, Boolean.class));
        out.add(new WidgetField<Integer>("OriginalX", Widget::getOriginalX, Widget::setOriginalX, Integer.class));
        out.add(new WidgetField<Integer>("OriginalY", Widget::getOriginalY, Widget::setOriginalY, Integer.class));
        out.add(new WidgetField<Integer>("OriginalWidth", Widget::getOriginalWidth, Widget::setOriginalWidth, Integer.class));
        out.add(new WidgetField<Integer>("OriginalHeight", Widget::getOriginalHeight, Widget::setOriginalHeight, Integer.class));
        out.add(new WidgetField<Integer>("XPositionMode", Widget::getXPositionMode, Widget::setXPositionMode, Integer.class));
        out.add(new WidgetField<Integer>("YPositionMode", Widget::getYPositionMode, Widget::setYPositionMode, Integer.class));
        out.add(new WidgetField<Integer>("WidthMode", Widget::getWidthMode, Widget::setWidthMode, Integer.class));
        out.add(new WidgetField<Integer>("HeightMode", Widget::getHeightMode, Widget::setHeightMode, Integer.class));
        out.add(new WidgetField<Integer>("XTextAlignment", Widget::getXTextAlignment, Widget::setXTextAlignment, Integer.class));
        out.add(new WidgetField<Integer>("YTextAlignment", Widget::getYTextAlignment, Widget::setYTextAlignment, Integer.class));
        out.add(new WidgetField<Integer>("RelativeX", Widget::getRelativeX, Widget::setRelativeX, Integer.class));
        out.add(new WidgetField<Integer>("RelativeY", Widget::getRelativeY, Widget::setRelativeY, Integer.class));
        out.add(new WidgetField<Integer>("Width", Widget::getWidth, Widget::setWidth, Integer.class));
        out.add(new WidgetField<Integer>("Height", Widget::getHeight, Widget::setHeight, Integer.class));
        out.add(new WidgetField<Integer>("RotationX", Widget::getRotationX, Widget::setRotationX, Integer.class));
        out.add(new WidgetField<Integer>("RotationY", Widget::getRotationY, Widget::setRotationY, Integer.class));
        out.add(new WidgetField<Integer>("RotationZ", Widget::getRotationZ, Widget::setRotationZ, Integer.class));
        out.add(new WidgetField<Integer>("ModelZoom", Widget::getModelZoom, Widget::setModelZoom, Integer.class));
        out.add(new WidgetField<Point>("CanvasLocation", Widget::getCanvasLocation));
        out.add(new WidgetField<Rectangle>("Bounds", Widget::getBounds));
        out.add(new WidgetField<Integer>("ScrollX", Widget::getScrollX, Widget::setScrollX, Integer.class));
        out.add(new WidgetField<Integer>("ScrollY", Widget::getScrollY, Widget::setScrollY, Integer.class));
        out.add(new WidgetField<Integer>("ScrollWidth", Widget::getScrollWidth, Widget::setScrollWidth, Integer.class));
        out.add(new WidgetField<Integer>("ScrollHeight", Widget::getScrollHeight, Widget::setScrollHeight, Integer.class));
        out.add(new WidgetField<Integer>("DragDeadZone", Widget::getDragDeadZone, Widget::setDragDeadZone, Integer.class));
        out.add(new WidgetField<Integer>("DragDeadTime", Widget::getDragDeadTime, Widget::setDragDeadTime, Integer.class));
        out.add(new WidgetField<Boolean>("NoClickThrough", Widget::getNoClickThrough, Widget::setNoClickThrough, Boolean.class));
        out.add(new WidgetField<Boolean>("NoScrollThrough", Widget::getNoScrollThrough, Widget::setNoScrollThrough, Boolean.class));
        out.add(new WidgetField<String>("TargetVerb", Widget::getTargetVerb, Widget::setTargetVerb, String.class));
        return out;
    }

    public void setWidget(Widget w) {
        this.clientThread.invoke(() -> {
            Map newValues = w == null ? null : (Map)fields.stream().collect(ImmutableMap.toImmutableMap(Function.identity(), i -> i.getValue(w)));
            SwingUtilities.invokeLater(() -> {
                this.widget = w;
                this.values = newValues;
                this.fireTableStructureChanged();
            });
        });
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0: {
                return "Field";
            }
            case 1: {
                return "Value";
            }
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        if (this.values == null) {
            return 0;
        }
        return this.values.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        WidgetField field = fields.get(rowIndex);
        switch (columnIndex) {
            case 0: {
                return field.getName();
            }
            case 1: {
                return this.values.get(field);
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            WidgetField field = fields.get(rowIndex);
            return field.isSettable();
        }
        return false;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        WidgetField field = fields.get(rowIndex);
        this.clientThread.invoke(() -> {
            field.setValue(this.widget, value);
            this.setWidget(this.widget);
        });
    }
}

