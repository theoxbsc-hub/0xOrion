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
import net.unethicalite.api.coords.RegionPoint;
import net.unethicalite.client.Static;

public final class ScenePoint {
    private final int x;
    private final int y;
    private final int plane;

    public WorldPoint toWorld() {
        Client client = Static.getClient();
        return new WorldPoint(this.x + client.getBaseX(), this.y + client.getBaseY(), this.plane);
    }

    public RegionPoint toRegion() {
        return RegionPoint.fromWorld(this.toWorld());
    }

    public int distanceTo(ScenePoint other) {
        if (other.plane != this.plane) {
            return Integer.MAX_VALUE;
        }
        return (int)Math.sqrt(Math.pow(other.x - this.x, 2.0) + Math.pow(other.y - this.y, 2.0));
    }

    public static ScenePoint fromRegion(RegionPoint regionPoint) {
        return ScenePoint.fromWorld(regionPoint.toWorld());
    }

    public static ScenePoint fromWorld(WorldPoint worldPoint) {
        Client client = Static.getClient();
        return new ScenePoint(worldPoint.getX() - client.getBaseX(), worldPoint.getY() - client.getBaseY(), 0);
    }

    public ScenePoint(int x, int y, int plane) {
        this.x = x;
        this.y = y;
        this.plane = plane;
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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ScenePoint)) {
            return false;
        }
        ScenePoint other = (ScenePoint)o;
        if (this.getX() != other.getX()) {
            return false;
        }
        if (this.getY() != other.getY()) {
            return false;
        }
        return this.getPlane() == other.getPlane();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getX();
        result = result * 59 + this.getY();
        result = result * 59 + this.getPlane();
        return result;
    }

    public String toString() {
        return "ScenePoint(x=" + this.getX() + ", y=" + this.getY() + ", plane=" + this.getPlane() + ")";
    }
}

