/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.DecorativeObject
 *  net.runelite.api.GameObject
 *  net.runelite.api.GroundObject
 *  net.runelite.api.Tile
 *  net.runelite.api.TileObject
 *  net.runelite.api.WallObject
 *  net.runelite.api.coords.WorldArea
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileEntities;
import net.unethicalite.api.query.entities.TileObjectQuery;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.client.Static;

public class TileObjects
extends TileEntities<TileObject> {
    private static final TileObjects TILE_OBJECTS = new TileObjects();

    private TileObjects() {
    }

    public static TileObjectQuery query() {
        return TileObjects.query(TileObjects::getAll);
    }

    public static TileObjectQuery query(Supplier<List<TileObject>> supplier) {
        return new TileObjectQuery(supplier);
    }

    public static List<TileObject> getAll() {
        return TileObjects.getAll((TileObject x) -> true);
    }

    public static List<TileObject> getAll(Predicate<TileObject> filter) {
        return TILE_OBJECTS.all((Predicate<? super TileObject>)filter);
    }

    public static List<TileObject> getAll(int ... ids) {
        return TILE_OBJECTS.all(ids);
    }

    public static List<TileObject> getAll(String ... names) {
        return TILE_OBJECTS.all(names);
    }

    public static TileObject getNearest(Predicate<TileObject> filter) {
        return TILE_OBJECTS.nearest(Players.getLocal().getWorldLocation(), filter);
    }

    public static TileObject getNearest(int ... ids) {
        return (TileObject)TILE_OBJECTS.nearest(Players.getLocal().getWorldLocation(), ids);
    }

    public static TileObject getNearest(String ... names) {
        return (TileObject)TILE_OBJECTS.nearest(Players.getLocal().getWorldLocation(), names);
    }

    public static TileObject getNearest(WorldPoint to, Predicate<TileObject> filter) {
        return TILE_OBJECTS.nearest(to, filter);
    }

    public static TileObject getNearest(WorldPoint to, int ... ids) {
        return (TileObject)TILE_OBJECTS.nearest(to, ids);
    }

    public static TileObject getNearest(WorldPoint to, String ... names) {
        return (TileObject)TILE_OBJECTS.nearest(to, names);
    }

    public static List<TileObject> getAt(int worldX, int worldY, int plane, int ... ids) {
        return TILE_OBJECTS.at(Tiles.getAt(worldX, worldY, plane), ids);
    }

    public static List<TileObject> getAt(int worldX, int worldY, int plane, String ... names) {
        return TILE_OBJECTS.at(Tiles.getAt(worldX, worldY, plane), names);
    }

    public static List<TileObject> getAt(int worldX, int worldY, int plane, Predicate<TileObject> filter) {
        return TILE_OBJECTS.at(Tiles.getAt(worldX, worldY, plane), (Predicate<? super TileObject>)filter);
    }

    public static List<TileObject> getAt(WorldPoint worldPoint, Predicate<TileObject> filter) {
        return TILE_OBJECTS.at(Tiles.getAt(worldPoint), (Predicate<? super TileObject>)filter);
    }

    public static List<TileObject> getAt(WorldPoint worldPoint, int ... ids) {
        return TILE_OBJECTS.at(Tiles.getAt(worldPoint), ids);
    }

    public static List<TileObject> getAt(WorldPoint worldPoint, String ... names) {
        return TILE_OBJECTS.at(Tiles.getAt(worldPoint), names);
    }

    public static List<TileObject> getAt(Tile tile, int ... ids) {
        return TILE_OBJECTS.at(tile, ids);
    }

    public static List<TileObject> getAt(Tile tile, String ... names) {
        return TILE_OBJECTS.at(tile, names);
    }

    public static List<TileObject> getAt(Tile tile, Predicate<TileObject> filter) {
        if (tile == null) {
            return Collections.emptyList();
        }
        return TILE_OBJECTS.at(tile, (Predicate<? super TileObject>)filter);
    }

    public static TileObject getFirstAt(int worldX, int worldY, int plane, int ... ids) {
        return TileObjects.getAt(worldX, worldY, plane, ids).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(int worldX, int worldY, int plane, String ... names) {
        return TileObjects.getAt(worldX, worldY, plane, names).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(int worldX, int worldY, int plane, Predicate<TileObject> filter) {
        return TileObjects.getAt(worldX, worldY, plane, filter).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(WorldPoint worldPoint, int ... ids) {
        return TileObjects.getAt(worldPoint, ids).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(WorldPoint worldPoint, String ... names) {
        return TileObjects.getAt(worldPoint, names).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(WorldPoint worldPoint, Predicate<TileObject> filter) {
        return TileObjects.getAt(worldPoint, filter).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(Tile tile, int ... ids) {
        return TileObjects.getAt(tile, ids).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(Tile tile, String ... names) {
        return TileObjects.getAt(tile, names).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstAt(Tile tile, Predicate<TileObject> filter) {
        return TileObjects.getAt(tile, filter).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(int worldX, int worldY, int plane, int radius, int ... ids) {
        return TileObjects.getSurrounding(worldX, worldY, plane, radius, ids).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(int worldX, int worldY, int plane, int radius, String ... names) {
        return TileObjects.getSurrounding(worldX, worldY, plane, radius, names).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(int worldX, int worldY, int plane, int radius, Predicate<TileObject> filter) {
        return TileObjects.getSurrounding(worldX, worldY, plane, radius, filter).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(WorldPoint worldPoint, int radius, int ... ids) {
        return TileObjects.getSurrounding(worldPoint, radius, ids).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(WorldPoint worldPoint, int radius, String ... names) {
        return TileObjects.getSurrounding(worldPoint, radius, names).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(WorldPoint worldPoint, int radius, Predicate<TileObject> filter) {
        return TileObjects.getSurrounding(worldPoint, radius, filter).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(Tile tile, int radius, int ... ids) {
        return TileObjects.getSurrounding(tile, radius, ids).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(Tile tile, int radius, String ... names) {
        return TileObjects.getSurrounding(tile, radius, names).stream().findFirst().orElse(null);
    }

    public static TileObject getFirstSurrounding(Tile tile, int radius, Predicate<TileObject> filter) {
        return TileObjects.getSurrounding(tile, radius, filter).stream().findFirst().orElse(null);
    }

    public static List<TileObject> getSurrounding(int worldX, int worldY, int plane, int radius, int ... ids) {
        return TILE_OBJECTS.surrounding(worldX, worldY, plane, radius, ids);
    }

    public static List<TileObject> getSurrounding(int worldX, int worldY, int plane, int radius, String ... names) {
        return TILE_OBJECTS.surrounding(worldX, worldY, plane, radius, names);
    }

    public static List<TileObject> getSurrounding(int worldX, int worldY, int plane, int radius, Predicate<TileObject> filter) {
        return TILE_OBJECTS.surrounding(worldX, worldY, plane, radius, filter);
    }

    public static List<TileObject> getSurrounding(WorldPoint worldPoint, int radius, int ... ids) {
        return TileObjects.getSurrounding(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane(), radius, ids);
    }

    public static List<TileObject> getSurrounding(WorldPoint worldPoint, int radius, String ... names) {
        return TileObjects.getSurrounding(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane(), radius, names);
    }

    public static List<TileObject> getSurrounding(WorldPoint worldPoint, int radius, Predicate<TileObject> filter) {
        return TileObjects.getSurrounding(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane(), radius, filter);
    }

    public static List<TileObject> getSurrounding(Tile tile, int radius, int ... ids) {
        return TileObjects.getSurrounding(tile.getWorldX(), tile.getWorldY(), tile.getPlane(), radius, ids);
    }

    public static List<TileObject> getSurrounding(Tile tile, int radius, String ... names) {
        return TileObjects.getSurrounding(tile.getWorldX(), tile.getWorldY(), tile.getPlane(), radius, names);
    }

    public static List<TileObject> getSurrounding(Tile tile, int radius, Predicate<TileObject> filter) {
        return TileObjects.getSurrounding(tile.getWorldX(), tile.getWorldY(), tile.getPlane(), radius, filter);
    }

    public static List<TileObject> within(WorldArea area, String ... names) {
        return TILE_OBJECTS.in(area, names);
    }

    public static List<TileObject> within(WorldArea area, int ... ids) {
        return TILE_OBJECTS.in(area, ids);
    }

    public static List<TileObject> within(WorldArea area, Predicate<TileObject> filter) {
        return TILE_OBJECTS.in(area, filter);
    }

    private static List<TileObject> getTileObjects(Tile tile) {
        GameObject[] gameObjects;
        GroundObject grnd;
        WallObject wall;
        ArrayList<TileObject> out = new ArrayList<TileObject>();
        if (tile == null) {
            return out;
        }
        DecorativeObject dec = tile.getDecorativeObject();
        if (dec != null && dec.getId() != -1) {
            out.add((TileObject)dec);
        }
        if ((wall = tile.getWallObject()) != null && wall.getId() != -1) {
            out.add((TileObject)wall);
        }
        if ((grnd = tile.getGroundObject()) != null && grnd.getId() != -1) {
            out.add((TileObject)grnd);
        }
        if ((gameObjects = tile.getGameObjects()) != null) {
            for (GameObject gameObject : gameObjects) {
                if (gameObject == null || !Static.getClient().isTileObjectValid(tile, (TileObject)gameObject) || gameObject.getId() == -1) continue;
                out.add((TileObject)gameObject);
            }
        }
        return out;
    }

    @Override
    protected List<TileObject> all(Predicate<? super TileObject> filter) {
        ArrayList<TileObject> out = new ArrayList<TileObject>();
        for (Tile tile : Tiles.getAll()) {
            out.addAll(TileObjects.getTileObjects(tile));
        }
        return out.stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    protected List<TileObject> at(Tile tile, Predicate<? super TileObject> pred) {
        return TileObjects.getTileObjects(tile).stream().filter(pred).collect(Collectors.toList());
    }
}

