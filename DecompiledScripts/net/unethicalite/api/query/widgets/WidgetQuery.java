/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.widgets.Widget
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.query.Query;
import net.unethicalite.api.query.results.WidgetQueryResults;
import org.apache.commons.lang3.ArrayUtils;

public class WidgetQuery
extends Query<Widget, WidgetQuery, WidgetQueryResults> {
    private int[] widgetIds = null;
    private int[] types = null;
    private String[] texts = null;
    private String[] actions = null;
    private Boolean visible = null;

    public WidgetQuery(Supplier<List<Widget>> supplier) {
        super(supplier);
    }

    @Override
    protected WidgetQueryResults results(List<Widget> list) {
        return new WidgetQueryResults(list);
    }

    public WidgetQuery ids(int ... ids) {
        this.widgetIds = ids;
        return this;
    }

    public WidgetQuery types(int ... types) {
        this.types = types;
        return this;
    }

    public WidgetQuery texts(String ... texts) {
        this.texts = texts;
        return this;
    }

    public WidgetQuery actions(String ... actions) {
        this.actions = actions;
        return this;
    }

    public WidgetQuery visible(Boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public boolean test(Widget widget) {
        if (this.widgetIds != null && !ArrayUtils.contains((int[])this.widgetIds, (int)widget.getId())) {
            return false;
        }
        if (this.types != null && !ArrayUtils.contains((int[])this.types, (int)widget.getType())) {
            return false;
        }
        if (this.texts != null && !ArrayUtils.contains((Object[])this.texts, (Object)widget.getText())) {
            return false;
        }
        if (this.actions != null && Arrays.stream(this.actions).noneMatch(Predicates.texts(widget.getActions()))) {
            return false;
        }
        if (this.visible != null && this.visible.booleanValue() != widget.isVisible()) {
            return false;
        }
        return super.test(widget);
    }
}

