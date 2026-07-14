/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Tile
 *  net.runelite.api.TileObject
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.entities;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.unethicalite.api.query.entities.SceneEntityQuery;
import net.unethicalite.api.query.results.SceneEntityQueryResults;
import net.unethicalite.api.scene.Tiles;
import org.apache.commons.lang3.ArrayUtils;

public class TileObjectQuery
extends SceneEntityQuery<TileObject, TileObjectQuery> {
    private Tile[] tiles = null;
    private Class<? extends TileObject>[] is = null;

    public TileObjectQuery(Supplier<List<TileObject>> supplier) {
        super(supplier);
    }

    public TileObjectQuery tiles(Tile ... tiles) {
        this.tiles = tiles;
        return this;
    }

    @SafeVarargs
    public final TileObjectQuery is(Class<? extends TileObject> ... classes) {
        this.is = classes;
        return this;
    }

    @Override
    protected SceneEntityQueryResults<TileObject> results(List<TileObject> list) {
        return new SceneEntityQueryResults<TileObject>(list);
    }

    @Override
    public boolean test(TileObject tileObject) {
        if (this.tiles != null && !ArrayUtils.contains((Object[])this.tiles, (Object)Tiles.getAt(tileObject.getWorldLocation()))) {
            return false;
        }
        if (this.is != null && Arrays.stream(this.is).noneMatch(clazz -> clazz.isInstance(tileObject))) {
            return false;
        }
        return super.test(tileObject);
    }
}

