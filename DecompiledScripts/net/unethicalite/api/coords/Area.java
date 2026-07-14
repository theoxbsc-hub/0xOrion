/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Locatable
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.coords;

import java.util.Arrays;
import net.runelite.api.Locatable;
import net.runelite.api.coords.WorldPoint;

public interface Area {
    public boolean contains(WorldPoint var1);

    public static Area union(Area ... areas) {
        return point -> Arrays.stream(areas).anyMatch(a -> a.contains(point));
    }

    public static Area intersection(Area ... areas) {
        return point -> Arrays.stream(areas).allMatch(a -> a.contains(point));
    }

    default public Area minus(Area other) {
        return point -> this.contains(point) && !other.contains(point);
    }

    default public boolean contains(Locatable locatable) {
        return this.contains(locatable.getWorldLocation());
    }

    default public WorldPoint getRandomTile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

