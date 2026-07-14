/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.coords;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.coords.ScenePoint;
import net.unethicalite.client.Static;

public final class RegionPoint {
    private final int x;
    private final int y;
    private final int plane;
    private final int regionId;

    public WorldPoint toWorld() {
        return WorldPoint.fromRegion((int)this.regionId, (int)this.x, (int)this.y, (int)this.plane);
    }

    public ScenePoint toScene() {
        return ScenePoint.fromWorld(this.toWorld());
    }

    public int distanceTo(RegionPoint other) {
        if (other.regionId != this.regionId || other.plane != this.plane) {
            return Integer.MAX_VALUE;
        }
        return (int)Math.sqrt(Math.pow(other.x - this.x, 2.0) + Math.pow(other.y - this.y, 2.0));
    }

    public static RegionPoint fromScene(ScenePoint scenePoint) {
        Client client = Static.getClient();
        WorldPoint world = new WorldPoint(client.getBaseX() + scenePoint.getX(), client.getBaseY() + scenePoint.getY(), scenePoint.getPlane());
        return RegionPoint.fromWorld(world);
    }

    public static RegionPoint fromWorld(WorldPoint worldPoint) {
        return new RegionPoint(worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane(), worldPoint.getRegionID());
    }

    public RegionPoint(int x, int y, int plane, int regionId) {
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.regionId = regionId;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getPlane() {
        return this.plane;
    }

    public int getRegionId() {
        return this.regionId;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RegionPoint)) {
            return false;
        }
        RegionPoint other = (RegionPoint)o;
        if (this.getX() != other.getX()) {
            return false;
        }
        if (this.getY() != other.getY()) {
            return false;
        }
        if (this.getPlane() != other.getPlane()) {
            return false;
        }
        return this.getRegionId() == other.getRegionId();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getX();
        result = result * 59 + this.getY();
        result = result * 59 + this.getPlane();
        result = result * 59 + this.getRegionId();
        return result;
    }

    public String toString() {
        return "RegionPoint(x=" + this.getX() + ", y=" + this.getY() + ", plane=" + this.getPlane() + ", regionId=" + this.getRegionId() + ")";
    }
}

