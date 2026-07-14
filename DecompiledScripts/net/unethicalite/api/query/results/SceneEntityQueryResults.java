/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Locatable
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.query.results;

import java.util.Comparator;
import java.util.List;
import net.runelite.api.Locatable;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.query.results.QueryResults;

public class SceneEntityQueryResults<T extends SceneEntity>
extends QueryResults<T, SceneEntityQueryResults<T>> {
    public SceneEntityQueryResults(List<T> results) {
        super(results);
    }

    public SceneEntityQueryResults<T> sortedByDistance(WorldPoint to) {
        return (SceneEntityQueryResults)this.sorted(Comparator.comparingDouble(entity -> entity.distanceTo(to)));
    }

    public SceneEntityQueryResults<T> sortedByDistance(Locatable to) {
        return this.sortedByDistance(to.getWorldLocation());
    }

    public SceneEntityQueryResults<T> sortedByDistance() {
        return this.sortedByDistance((Locatable)Players.getLocal());
    }

    public T nearest() {
        return (T)((SceneEntity)this.sortedByDistance().first());
    }

    public T nearest(Locatable to) {
        return (T)((SceneEntity)this.sortedByDistance(to).first());
    }

    public T nearest(WorldPoint to) {
        return (T)((SceneEntity)this.sortedByDistance(to).first());
    }

    public T farthest() {
        return (T)((SceneEntity)this.sortedByDistance().last());
    }

    public T farthest(Locatable locatable) {
        return (T)((SceneEntity)this.sortedByDistance(locatable).last());
    }

    public T farthest(WorldPoint to) {
        return (T)((SceneEntity)this.sortedByDistance(to).last());
    }
}

