/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.CollisionData
 *  net.runelite.api.GameObject
 *  net.runelite.api.Locatable
 *  net.runelite.api.Player
 *  net.runelite.api.Tile
 *  net.runelite.api.WallObject
 *  net.runelite.api.coords.Direction
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.movement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.GameObject;
import net.runelite.api.Locatable;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.Positionable;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.client.Static;

public class Reachable {
    private static final int MAX_ATTEMPTED_TILES = 4096;

    public static boolean check(int flag, int checkFlag) {
        return (flag & checkFlag) != 0;
    }

    public static boolean isObstacle(int endFlag) {
        return Reachable.check(endFlag, 0x1220100);
    }

    public static boolean isObstacle(WorldPoint worldPoint) {
        return Reachable.isObstacle(Reachable.getCollisionFlag(worldPoint));
    }

    public static int getCollisionFlag(WorldPoint point) {
        CollisionData[] collisionMaps = Static.getClient().getCollisionMaps();
        if (collisionMaps == null) {
            return 0xFFFFFF;
        }
        CollisionData collisionData = collisionMaps[Static.getClient().getPlane()];
        if (collisionData == null) {
            return 0xFFFFFF;
        }
        LocalPoint localPoint = LocalPoint.fromWorld((Client)Static.getClient(), (WorldPoint)point);
        if (localPoint == null) {
            return 0xFFFFFF;
        }
        return collisionData.getFlags()[localPoint.getSceneX()][localPoint.getSceneY()];
    }

    public static boolean isWalled(Direction direction, int startFlag) {
        switch (direction) {
            case NORTH: {
                return Reachable.check(startFlag, 2);
            }
            case SOUTH: {
                return Reachable.check(startFlag, 32);
            }
            case WEST: {
                return Reachable.check(startFlag, 128);
            }
            case EAST: {
                return Reachable.check(startFlag, 8);
            }
        }
        throw new IllegalArgumentException();
    }

    public static boolean isWalled(WorldPoint source, WorldPoint destination) {
        return Reachable.isWalled(Tiles.getAt(source), Tiles.getAt(destination));
    }

    public static boolean isWalled(Tile source, Tile destination) {
        WallObject wall = source.getWallObject();
        if (wall == null) {
            return false;
        }
        WorldPoint a = source.getWorldLocation();
        WorldPoint b = destination.getWorldLocation();
        switch (wall.getOrientationA()) {
            case 1: {
                return a.dx(-1).equals((Object)b) || a.dx(-1).dy(1).equals((Object)b) || a.dx(-1).dy(-1).equals((Object)b);
            }
            case 2: {
                return a.dy(1).equals((Object)b) || a.dx(-1).dy(1).equals((Object)b) || a.dx(1).dy(1).equals((Object)b);
            }
            case 4: {
                return a.dx(1).equals((Object)b) || a.dx(1).dy(1).equals((Object)b) || a.dx(1).dy(-1).equals((Object)b);
            }
            case 8: {
                return a.dy(-1).equals((Object)b) || a.dx(-1).dy(-1).equals((Object)b) || a.dx(-1).dy(1).equals((Object)b);
            }
        }
        return false;
    }

    public static boolean hasDoor(WorldPoint source, Direction direction) {
        Tile tile = Tiles.getAt(source);
        if (tile == null) {
            return false;
        }
        return Reachable.hasDoor(tile, direction);
    }

    public static boolean hasDoor(Tile source, Direction direction) {
        WallObject wall = source.getWallObject();
        if (wall == null) {
            return false;
        }
        return Reachable.isWalled(direction, Reachable.getCollisionFlag(source.getWorldLocation())) && wall.hasAction(new String[]{"Open", "Close"});
    }

    public static boolean isDoored(Tile source, Tile destination) {
        WallObject wall = source.getWallObject();
        if (wall == null) {
            return false;
        }
        return Reachable.isWalled(source, destination) && wall.hasAction(new String[]{"Open"});
    }

    public static boolean canWalk(Direction direction, int startFlag, int endFlag) {
        if (Reachable.isObstacle(endFlag)) {
            return false;
        }
        return !Reachable.isWalled(direction, startFlag);
    }

    public static WorldPoint getNeighbour(Direction direction, WorldPoint source) {
        switch (direction) {
            case NORTH: {
                return source.dy(1);
            }
            case SOUTH: {
                return source.dy(-1);
            }
            case WEST: {
                return source.dx(-1);
            }
            case EAST: {
                return source.dx(1);
            }
        }
        throw new IllegalArgumentException();
    }

    public static List<WorldPoint> getNeighbours(Positionable destination, Positionable current) {
        ArrayList<WorldPoint> out = new ArrayList<WorldPoint>();
        WorldPoint dest = current instanceof WorldPoint ? (WorldPoint)current : current.getWorldLocation();
        for (Direction dir : Direction.values()) {
            Locatable targetObject;
            boolean containsPoint;
            WorldPoint neighbour = Reachable.getNeighbour(dir, dest);
            if (!neighbour.isInScene(Static.getClient())) continue;
            if (destination instanceof Locatable && (containsPoint = (targetObject = (Locatable)destination) instanceof GameObject ? ((GameObject)targetObject).getWorldArea().contains(neighbour) : targetObject.getWorldLocation().equals((Object)neighbour)) && (!Reachable.isWalled(dir, Reachable.getCollisionFlag(dest)) || targetObject instanceof WallObject)) {
                out.add(neighbour);
                continue;
            }
            if (!Reachable.canWalk(dir, Reachable.getCollisionFlag(dest), Reachable.getCollisionFlag(neighbour))) continue;
            out.add(neighbour);
        }
        return out;
    }

    public static List<WorldPoint> getVisitedTiles(Positionable destination) {
        WorldPoint dest;
        Player local = Static.getClient().getLocalPlayer();
        WorldPoint worldPoint = dest = destination instanceof WorldPoint ? (WorldPoint)destination : destination.getWorldLocation();
        if (local == null || !dest.isInScene(Static.getClient())) {
            return Collections.emptyList();
        }
        ArrayList<WorldPoint> visitedTiles = new ArrayList<WorldPoint>();
        LinkedList<Object> queue = new LinkedList<Object>();
        if (local.getWorldLocation().getPlane() != dest.getPlane()) {
            return visitedTiles;
        }
        queue.add(local.getWorldLocation());
        while (!queue.isEmpty()) {
            if (visitedTiles.size() > 4096) {
                return visitedTiles;
            }
            WorldPoint current = (WorldPoint)queue.pop();
            visitedTiles.add(current);
            if (current.equals((Object)dest)) {
                return visitedTiles;
            }
            List neighbours = Reachable.getNeighbours(destination, (Positionable)current).stream().filter(x -> !visitedTiles.contains(x) && !queue.contains(x)).collect(Collectors.toList());
            queue.addAll(neighbours);
        }
        return visitedTiles;
    }

    public static List<WorldPoint> getVisitedTiles(Locatable locatable) {
        return Reachable.getVisitedTiles((Positionable)locatable);
    }

    public static List<WorldPoint> getVisitedTiles(WorldPoint worldPoint) {
        return Reachable.getVisitedTiles((Positionable)worldPoint);
    }

    public static boolean isInteractable(Locatable locatable) {
        return Reachable.getVisitedTiles(locatable).contains(locatable.getWorldLocation());
    }

    public static boolean isWalkable(WorldPoint worldPoint) {
        return Reachable.getVisitedTiles(worldPoint).contains(worldPoint);
    }
}

