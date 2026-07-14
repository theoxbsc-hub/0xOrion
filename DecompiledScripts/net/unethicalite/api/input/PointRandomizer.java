/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.Item$Type
 *  net.runelite.api.Model
 *  net.runelite.api.Perspective
 *  net.runelite.api.Point
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.widgets.Widget
 */
package net.unethicalite.api.input;

import java.awt.Rectangle;
import java.awt.Shape;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.util.Randomizer;
import net.unethicalite.client.Static;

public class PointRandomizer {
    public static Point getRandomPointIn(Widget widget) {
        return Randomizer.getRandomPointIn(PointRandomizer.getBoundsFor(widget));
    }

    public static Rectangle getBoundsFor(Widget widget) {
        return widget.getBounds();
    }

    public static Point getRandomPointIn(TileItem item) {
        return Randomizer.getRandomPointIn(PointRandomizer.getBoundsFor(item));
    }

    public static Rectangle getBoundsFor(TileItem item) {
        Shape shape = Perspective.getClickbox((Client)Static.getClient(), (Model)item.getModel(), (int)0, (LocalPoint)item.getLocalLocation());
        if (shape != null) {
            return shape.getBounds();
        }
        Point screenCoords = Perspective.localToCanvas((Client)Static.getClient(), (LocalPoint)item.getLocalLocation(), (int)Static.getClient().getPlane());
        if (screenCoords != null) {
            return new Rectangle(screenCoords.getX(), screenCoords.getY(), 0, 0);
        }
        return new Rectangle(-1, -1, 0, 0);
    }

    public static Point getRandomPointIn(Item item) {
        return Randomizer.getRandomPointIn(PointRandomizer.getBoundsFor(item));
    }

    public static Rectangle getBoundsFor(Item item) {
        Widget slot;
        Widget widget = Static.getClient().getWidget(item.getWidgetId());
        if (widget == null) {
            return new Rectangle(-1, -1, 0, 0);
        }
        if (item.getType() != Item.Type.EQUIPMENT && (slot = widget.getChild(item.getSlot())) != null) {
            return slot.getBounds() != null ? slot.getBounds() : new Rectangle(-1, -1, 0, 0);
        }
        Rectangle bounds = widget.getBounds();
        if (bounds != null) {
            Rectangle itemBounds = widget.getWidgetItem(item.getSlot()).getCanvasBounds();
            return itemBounds != null ? itemBounds : new Rectangle(-1, -1, 0, 0);
        }
        return new Rectangle(-1, -1, 0, 0);
    }

    public static Point getRandomPointIn(Actor npc) {
        return Randomizer.getRandomPointIn(PointRandomizer.getBoundsFor(npc));
    }

    public static Rectangle getBoundsFor(Actor npc) {
        Shape shape = Perspective.getClickbox((Client)Static.getClient(), (Model)npc.getModel(), (int)0, (LocalPoint)npc.getLocalLocation());
        if (shape != null) {
            return shape.getBounds();
        }
        return npc.getCanvasTilePoly().getBounds();
    }

    public static Point getRandomPointIn(TileObject object) {
        return Randomizer.getRandomPointIn(PointRandomizer.getBoundsFor(object));
    }

    public static Rectangle getBoundsFor(TileObject object) {
        Shape shape = object.getClickbox();
        if (shape != null) {
            return shape.getBounds();
        }
        Point screenCoords = Perspective.localToCanvas((Client)Static.getClient(), (LocalPoint)object.getLocalLocation(), (int)Static.getClient().getPlane());
        if (screenCoords != null) {
            return new Rectangle(screenCoords.getX(), screenCoords.getY(), 0, 0);
        }
        return new Rectangle(-1, -1, 0, 0);
    }
}

