/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api;

import net.runelite.api.coords.WorldPoint;

public interface Positionable {
    public int getWorldX();

    public int getWorldY();

    public int getPlane();

    default public WorldPoint getWorldLocation() {
        return new WorldPoint(this.getWorldX(), this.getWorldY(), this.getPlane());
    }
}

