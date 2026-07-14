/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Tile
 *  net.runelite.api.coords.Direction
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.movement.pathfinder;

import net.runelite.api.Tile;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.CollisionMap;
import net.unethicalite.api.scene.Tiles;

public class LocalCollisionMap
implements CollisionMap {
    private final boolean blockDoors;

    public LocalCollisionMap(boolean blockDoors) {
        this.blockDoors = blockDoors;
    }

    @Override
    public boolean n(int x, int y, int z) {
        WorldPoint current = new WorldPoint(x, y, z);
        if (Reachable.isObstacle(current)) {
            return false;
        }
        Tile currentTile = Tiles.getAt(current);
        Tile destinationTile = Tiles.getAt(current.dy(1));
        if (currentTile != null && destinationTile != null && (Reachable.isDoored(currentTile, destinationTile) || Reachable.isDoored(destinationTile, currentTile)) && !this.blockDoors) {
            return !Reachable.isObstacle(destinationTile.getWorldLocation());
        }
        return Reachable.canWalk(Direction.NORTH, Reachable.getCollisionFlag(current), Reachable.getCollisionFlag(current.dy(1)));
    }

    @Override
    public boolean e(int x, int y, int z) {
        WorldPoint current = new WorldPoint(x, y, z);
        if (Reachable.isObstacle(current)) {
            return false;
        }
        Tile currentTile = Tiles.getAt(current);
        Tile destinationTile = Tiles.getAt(current.dx(1));
        if (currentTile != null && destinationTile != null && (Reachable.isDoored(currentTile, destinationTile) || Reachable.isDoored(destinationTile, currentTile)) && !this.blockDoors) {
            return !Reachable.isObstacle(destinationTile.getWorldLocation());
        }
        return Reachable.canWalk(Direction.EAST, Reachable.getCollisionFlag(current), Reachable.getCollisionFlag(current.dx(1)));
    }
}

