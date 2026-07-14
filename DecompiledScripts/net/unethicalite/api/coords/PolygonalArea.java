/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.coords;

import java.awt.Polygon;
import java.awt.Rectangle;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.coords.Area;

public class PolygonalArea
implements Area {
    private final Polygon polygon;
    private final int plane;

    public PolygonalArea(int plane, WorldPoint ... points) {
        this.plane = plane;
        this.polygon = new Polygon();
        for (WorldPoint point : points) {
            this.polygon.addPoint(point.getX(), point.getY());
        }
    }

    @Override
    public boolean contains(WorldPoint worldPoint) {
        if (worldPoint.getPlane() == -1 || worldPoint.getPlane() != this.plane) {
            return false;
        }
        return this.polygon.contains(worldPoint.getX(), worldPoint.getY());
    }

    @Override
    public WorldPoint getRandomTile() {
        int y;
        int x;
        Rectangle r = this.polygon.getBounds();
        while (!this.polygon.contains(x = Rand.nextInt(r.x, r.x + r.width), y = Rand.nextInt(r.y, r.y + r.height))) {
        }
        return new WorldPoint(x, y, this.plane);
    }
}

