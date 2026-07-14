/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Tile
 *  net.runelite.api.coords.WorldArea
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.entities.Entities;
import net.unethicalite.api.scene.Tiles;

public abstract class TileEntities<T extends SceneEntity>
extends Entities<T> {
    protected abstract List<T> at(Tile var1, Predicate<? super T> var2);

    protected List<T> at(Tile tile, int ... ids) {
        return this.at(tile, Predicates.ids(ids));
    }

    protected List<T> at(Tile tile, String ... names) {
        return this.at(tile, Predicates.names(names));
    }

    protected List<T> surrounding(int worldX, int worldY, int plane, int radius, Predicate<? super T> filter) {
        ArrayList<? super T> out = new ArrayList<T>();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                out.addAll(this.at(Tiles.getAt(worldX + x, worldY + y, plane), filter));
            }
        }
        return out;
    }

    protected List<T> surrounding(int worldX, int worldY, int plane, int radius, int ... ids) {
        return this.surrounding(worldX, worldY, plane, radius, Predicates.ids(ids));
    }

    protected List<T> surrounding(int worldX, int worldY, int plane, int radius, String ... names) {
        return this.surrounding(worldX, worldY, plane, radius, Predicates.names(names));
    }

    protected List<T> in(WorldArea area, Predicate<? super T> filter) {
        ArrayList<? super T> out = new ArrayList<T>();
        for (WorldPoint worldPoint : area.toWorldPointList()) {
            out.addAll(this.at(Tiles.getAt(worldPoint), filter));
        }
        return out;
    }

    protected List<T> in(WorldArea area, int ... ids) {
        return this.in(area, Predicates.ids(ids));
    }

    protected List<T> in(WorldArea area, String ... names) {
        return this.in(area, Predicates.names(names));
    }
}

