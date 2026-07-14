/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Point
 *  net.runelite.api.Tile
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.api.worldmap.WorldMap
 */
package net.unethicalite.api.scene;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.worldmap.WorldMap;
import net.unethicalite.api.coords.RegionPoint;
import net.unethicalite.api.coords.ScenePoint;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class Tiles {
    public static List<Tile> getAll(Predicate<Tile> filter) {
        ArrayList<Tile> out = new ArrayList<Tile>();
        for (int x = 0; x < 104; ++x) {
            for (int y = 0; y < 104; ++y) {
                Tile tile = Static.getClient().getScene().getTiles()[Static.getClient().getPlane()][x][y];
                if (tile == null || !filter.test(tile)) continue;
                out.add(tile);
            }
        }
        return out;
    }

    public static List<Tile> getAll() {
        return Tiles.getAll(x -> true);
    }

    public static Tile getAt(WorldPoint worldPoint) {
        return Tiles.getAt(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    public static Tile getAt(LocalPoint localPoint) {
        return Static.getClient().getScene().getTiles()[Static.getClient().getPlane()][localPoint.getSceneX()][localPoint.getSceneY()];
    }

    public static Tile getAt(int worldX, int worldY, int plane) {
        int correctedY;
        Client client = Static.getClient();
        int correctedX = worldX < 104 ? worldX + client.getBaseX() : worldX;
        int n = correctedY = worldY < 104 ? worldY + client.getBaseY() : worldY;
        if (!WorldPoint.isInScene((Client)client, (int)correctedX, (int)correctedY)) {
            return null;
        }
        int x = correctedX - client.getBaseX();
        int y = correctedY - client.getBaseY();
        return client.getScene().getTiles()[plane][x][y];
    }

    public static Tile getAt(RegionPoint regionPoint) {
        return Tiles.getAt(regionPoint.toWorld());
    }

    public static Tile getAt(ScenePoint scenePoint) {
        return Static.getClient().getScene().getTiles()[scenePoint.getPlane()][scenePoint.getX()][scenePoint.getY()];
    }

    public static List<Tile> getSurrounding(WorldPoint worldPoint, int radius) {
        ArrayList<Tile> out = new ArrayList<Tile>();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                out.add(Tiles.getAt(worldPoint.dx(x).dx(y)));
            }
        }
        return out;
    }

    public static Tile getHoveredTile() {
        return Static.getClient().getSelectedSceneTile();
    }

    public static Set<WorldPoint> getWorldMapTiles() {
        Widget worldMap = Widgets.get(WidgetInfo.WORLD_MAP_VIEW);
        if (worldMap == null) {
            return Collections.emptySet();
        }
        WorldMap wm = Static.getClient().getWorldMap();
        HashSet<WorldPoint> out = new HashSet<WorldPoint>();
        int topLeftX = wm.getWorldMapX() - wm.getWorldMapDisplayWidth() / 2;
        int topLeftY = wm.getWorldMapY() - wm.getWorldMapDisplayHeight() / 2;
        for (int x = topLeftX; x < topLeftX + wm.getWorldMapDisplayWidth(); ++x) {
            for (int y = topLeftY; y < topLeftY + wm.getWorldMapDisplayHeight(); ++y) {
                WorldPoint worldPoint = wm.getWorldPointFromMap(x, y);
                if (worldPoint == null) continue;
                out.add(worldPoint);
            }
        }
        return out;
    }

    public static List<WorldPoint> getWorldMapTiles(int plane) {
        Widget worldMap = Widgets.get(WidgetInfo.WORLD_MAP_VIEW);
        if (worldMap == null) {
            return Collections.emptyList();
        }
        ArrayList<WorldPoint> out = new ArrayList<WorldPoint>();
        WorldMap wm = Static.getClient().getWorldMap();
        Rectangle worldMapRect = worldMap.getBounds();
        float pixelsPerTile = wm.getWorldMapZoom();
        int widthInTiles = (int)Math.ceil(worldMapRect.getWidth() / (double)pixelsPerTile);
        int heightInTiles = (int)Math.ceil(worldMapRect.getHeight() / (double)pixelsPerTile);
        Point worldMapPosition = wm.getWorldMapPosition();
        int leftX = worldMapPosition.getX() - widthInTiles / 2;
        int rightX = leftX + widthInTiles;
        int topY = worldMapPosition.getY() + heightInTiles / 2;
        int bottomY = topY - heightInTiles;
        for (int x = leftX; x < rightX; ++x) {
            for (int y = topY; y >= bottomY; --y) {
                out.add(new WorldPoint(x, y, plane));
            }
        }
        return out;
    }
}

