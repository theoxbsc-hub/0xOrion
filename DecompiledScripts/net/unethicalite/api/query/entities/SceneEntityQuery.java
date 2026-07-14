/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Locatable
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.entities;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.Locatable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.query.Query;
import net.unethicalite.api.query.results.SceneEntityQueryResults;
import org.apache.commons.lang3.ArrayUtils;

public abstract class SceneEntityQuery<T extends SceneEntity, Q extends SceneEntityQuery<T, Q>>
extends Query<T, Q, SceneEntityQueryResults<T>> {
    private Integer maxDistance = null;
    private WorldPoint distanceSrc = null;
    private int[] ids = null;
    private String[] names = null;
    private String[] actions = null;
    private WorldPoint[] locations = null;
    private LocalPoint[] localLocations = null;

    protected SceneEntityQuery(Supplier<List<T>> supplier) {
        super(supplier);
    }

    public Q ids(int ... ids) {
        this.ids = ids;
        return (Q)this;
    }

    public Q names(String ... names) {
        this.names = names;
        return (Q)this;
    }

    public Q actions(String ... actions) {
        this.actions = actions;
        return (Q)this;
    }

    public Q locations(WorldPoint ... locations) {
        this.locations = locations;
        return (Q)this;
    }

    public Q localLocations(LocalPoint ... localLocations) {
        this.localLocations = localLocations;
        return (Q)this;
    }

    public Q distance(Locatable source, int maxDistance) {
        return this.distance(source.getWorldLocation(), maxDistance);
    }

    public Q distance(WorldPoint source, int maxDistance) {
        this.distanceSrc = source;
        this.maxDistance = maxDistance;
        return (Q)this;
    }

    public Q distance(int maxDistance) {
        this.maxDistance = maxDistance;
        return (Q)this;
    }

    @Override
    public boolean test(T t) {
        if (this.ids != null && !ArrayUtils.contains((int[])this.ids, (int)t.getId())) {
            return false;
        }
        if (this.names != null && !ArrayUtils.contains((Object[])this.names, (Object)t.getName())) {
            return false;
        }
        if (this.locations != null && !ArrayUtils.contains((Object[])this.locations, (Object)t.getWorldLocation())) {
            return false;
        }
        if (this.localLocations != null && !ArrayUtils.contains((Object[])this.localLocations, (Object)t.getLocalLocation())) {
            return false;
        }
        if (this.actions != null && Arrays.stream(this.actions).noneMatch(Predicates.texts(t.getActions()))) {
            return false;
        }
        if (this.maxDistance != null) {
            if (this.distanceSrc == null) {
                this.distanceSrc = Players.getLocal().getWorldLocation();
            }
            if (this.distanceSrc.distanceTo(t.getWorldLocation()) > this.maxDistance) {
                return false;
            }
        }
        return super.test(t);
    }
}

