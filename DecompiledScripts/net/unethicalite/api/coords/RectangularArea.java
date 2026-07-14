/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.coords;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.coords.Area;
import net.unethicalite.api.entities.Players;

public class RectangularArea
implements Area {
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int plane;

    public RectangularArea(int x1, int y1, int x2, int y2, int plane) {
        this.plane = plane;
        if (x1 <= x2) {
            this.minX = x1;
            this.maxX = x2;
        } else {
            this.minX = x2;
            this.maxX = x1;
        }
        if (y1 <= y2) {
            this.minY = y1;
            this.maxY = y2;
        } else {
            this.minY = y2;
            this.maxY = y1;
        }
    }

    public RectangularArea(int x1, int y1, int x2, int y2) {
        this(x1, y1, x2, y2, 0);
    }

    public RectangularArea(WorldPoint sw, int width, int height) {
        this(sw.getX(), sw.getY(), sw.getX() + width, sw.getY() + height, sw.getPlane());
    }

    public RectangularArea(WorldPoint sw, WorldPoint ne) {
        this(sw.getX(), sw.getY(), ne.getX(), ne.getY(), sw.getPlane());
    }

    @Override
    public WorldPoint getRandomTile() {
        return new WorldPoint(Rand.nextInt(this.minX, this.maxX), Rand.nextInt(this.minY, this.maxY), this.plane);
    }

    public WorldPoint getCenter() {
        return new WorldPoint((this.minX + this.maxX) / 2, (this.minY + this.maxY) / 2, this.plane);
    }

    public WorldPoint getNearest() {
        WorldPoint current = Players.getLocal().getWorldLocation();
        if (this.contains(current)) {
            return current;
        }
        int x = current.getX() <= this.minX ? this.minX : (current.getX() >= this.maxX ? this.maxX : current.getX());
        int y = current.getY() <= this.minY ? this.minY : (current.getY() >= this.maxY ? this.maxY : current.getY());
        return new WorldPoint(x, y, this.plane);
    }

    @Override
    public boolean contains(WorldPoint worldPoint) {
        if (worldPoint.getPlane() == -1 || worldPoint.getPlane() != this.plane) {
            return false;
        }
        int x = worldPoint.getX();
        int y = worldPoint.getY();
        return x >= this.minX && y >= this.minY && x <= this.maxX && y <= this.maxY;
    }

    public int getMinX() {
        return this.minX;
    }

    public int getMaxX() {
        return this.maxX;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getMaxY() {
        return this.maxY;
    }

    public int getPlane() {
        return this.plane;
    }
}

