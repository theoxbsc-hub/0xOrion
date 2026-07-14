/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.runelite.api.Client
 *  net.runelite.api.Perspective
 *  net.runelite.api.Point
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.api.worldmap.WorldMap
 */
package net.unethicalite.api.utils;

import java.awt.Rectangle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.worldmap.WorldMap;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class CoordUtils {
    public static Point worldPointToWorldMap(WorldPoint worldPoint) {
        WorldMap wm = Static.getClient().getWorldMap();
        if (!wm.getWorldMapData().surfaceContainsPosition(worldPoint.getX(), worldPoint.getY())) {
            return null;
        }
        float pixelsPerTile = wm.getWorldMapZoom();
        Widget map = Widgets.get(WidgetInfo.WORLD_MAP_VIEW);
        if (map != null) {
            Rectangle worldMapRect = map.getBounds();
            int widthInTiles = (int)Math.ceil(worldMapRect.getWidth() / (double)pixelsPerTile);
            int heightInTiles = (int)Math.ceil(worldMapRect.getHeight() / (double)pixelsPerTile);
            Point worldMapPosition = wm.getWorldMapPosition();
            int yTileMax = worldMapPosition.getY() - heightInTiles / 2;
            int yTileOffset = (yTileMax - worldPoint.getY() - 1) * -1;
            int xTileOffset = worldPoint.getX() + widthInTiles / 2 - worldMapPosition.getX();
            int xGraphDiff = (int)((float)xTileOffset * pixelsPerTile);
            int yGraphDiff = (int)((float)yTileOffset * pixelsPerTile);
            yGraphDiff = (int)((double)yGraphDiff - ((double)pixelsPerTile - Math.ceil(pixelsPerTile / 2.0f)));
            xGraphDiff = (int)((double)xGraphDiff + ((double)pixelsPerTile - Math.ceil(pixelsPerTile / 2.0f)));
            yGraphDiff = worldMapRect.height - yGraphDiff;
            return new Point(xGraphDiff += (int)worldMapRect.getX(), yGraphDiff += (int)worldMapRect.getY());
        }
        return null;
    }

    public static WorldPoint worldMapToWorldPoint(Point point) {
        float zoom = Static.getClient().getWorldMap().getWorldMapZoom();
        WorldMap worldMap = Static.getClient().getWorldMap();
        WorldPoint mapPoint = new WorldPoint(worldMap.getWorldMapPosition().getX(), worldMap.getWorldMapPosition().getY(), 0);
        Point middle = CoordUtils.worldPointToWorldMap(mapPoint);
        if (middle == null) {
            return null;
        }
        int dx = (int)((float)(point.getX() - middle.getX()) / zoom);
        int dy = (int)((float)(-(point.getY() - middle.getY())) / zoom);
        return mapPoint.dx(dx).dy(dy);
    }

    @Nullable
    public static Point localToMinimap(@Nonnull Client client, @Nonnull LocalPoint point, int distance) {
        int y;
        LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();
        int x = point.getX() / 32 - localLocation.getX() / 32;
        int dist = x * x + (y = point.getY() / 32 - localLocation.getY() / 32) * y;
        if (dist < distance) {
            Widget minimap1;
            Widget minimapDrawWidget = client.isResized() ? ((minimap1 = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA)) != null ? minimap1 : client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA)) : client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
            if (minimapDrawWidget == null || !minimapDrawWidget.isVisible()) {
                return null;
            }
            int angle = client.getCameraYawTarget() & 0x7FF;
            int sin = Perspective.SINE[angle];
            int cos = Perspective.COSINE[angle];
            int xx = y * sin + cos * x >> 16;
            int yy = sin * x - y * cos >> 16;
            Point loc = minimapDrawWidget.getCanvasLocation();
            int miniMapX = loc.getX() + xx + minimapDrawWidget.getWidth() / 2;
            int miniMapY = minimapDrawWidget.getHeight() / 2 + loc.getY() + yy;
            return new Point(miniMapX, miniMapY);
        }
        return null;
    }
}

