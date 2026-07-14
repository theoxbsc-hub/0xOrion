/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.webwalker.engine.PathStep;

public final class Path {
    public final List<PathStep> steps;
    public final boolean reachedTarget;
    public final int nodesExpanded;

    public Path(List<PathStep> arrayList, boolean bl, int n) {
        this.steps = arrayList != null ? arrayList : new ArrayList();
        this.reachedTarget = bl;
        this.nodesExpanded = n;
    }

    public static Path empty() {
        return new Path(Collections.emptyList(), false, 0);
    }

    public boolean isEmpty() {
        return this.steps.isEmpty();
    }

    public int size() {
        return this.steps.size();
    }

    public WorldPoint last() {
        return this.steps.isEmpty() ? null : this.steps.get((int)(this.steps.size() - 1)).point;
    }

    public List<WorldPoint> points() {
        ArrayList<WorldPoint> arrayList = new ArrayList<WorldPoint>(this.steps.size());
        for (PathStep pathStep : this.steps) {
            arrayList.add(pathStep.point);
        }
        return arrayList;
    }
}

