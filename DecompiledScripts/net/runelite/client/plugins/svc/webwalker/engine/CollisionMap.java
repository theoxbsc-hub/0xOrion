/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import net.runelite.api.coords.WorldPoint;

public interface CollisionMap {
    public boolean n(int var1, int var2, int var3);

    public boolean e(int var1, int var2, int var3);

    public boolean hasData(int var1, int var2, int var3);

    default public boolean s(int n, int n2, int n3) {
        return this.n(n, n2 - 1, n3);
    }

    default public boolean w(int n, int n2, int n3) {
        return this.e(n - 1, n2, n3);
    }

    default public boolean ne(int n, int n2, int n3) {
        return this.n(n, n2, n3) && this.e(n, n2 + 1, n3) && this.e(n, n2, n3) && this.n(n + 1, n2, n3);
    }

    default public boolean nw(int n, int n2, int n3) {
        return this.n(n, n2, n3) && this.w(n, n2 + 1, n3) && this.w(n, n2, n3) && this.n(n - 1, n2, n3);
    }

    default public boolean se(int n, int n2, int n3) {
        return this.s(n, n2, n3) && this.e(n, n2 - 1, n3) && this.e(n, n2, n3) && this.s(n + 1, n2, n3);
    }

    default public boolean sw(int n, int n2, int n3) {
        return this.s(n, n2, n3) && this.w(n, n2 - 1, n3) && this.w(n, n2, n3) && this.s(n - 1, n2, n3);
    }

    default public boolean fullBlock(int n, int n2, int n3) {
        return !this.n(n, n2, n3) && !this.s(n, n2, n3) && !this.e(n, n2, n3) && !this.w(n, n2, n3);
    }

    default public boolean n(WorldPoint worldPoint) {
        return this.n(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    default public boolean e(WorldPoint worldPoint) {
        return this.e(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    default public boolean s(WorldPoint worldPoint) {
        return this.s(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    default public boolean w(WorldPoint worldPoint) {
        return this.w(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    default public boolean hasData(WorldPoint worldPoint) {
        return this.hasData(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }

    default public boolean fullBlock(WorldPoint worldPoint) {
        return this.fullBlock(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane());
    }
}

