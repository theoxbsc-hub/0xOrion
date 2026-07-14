/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import net.runelite.client.plugins.svc.webwalker.engine.CollisionMap;
import net.runelite.client.plugins.svc.webwalker.engine.RegionCollisionMap;

public final class CompositeCollisionMap
implements CollisionMap {
    private final RegionCollisionMap bundled;
    private final CollisionMap live;

    public CompositeCollisionMap(RegionCollisionMap regionCollisionMap, CollisionMap collisionMap) {
        this.bundled = regionCollisionMap;
        this.live = collisionMap;
    }

    @Override
    public boolean n(int n, int n2, int n3) {
        if (this.bundled != null && this.bundled.hasData(n, n2, n3)) {
            return this.bundled.n(n, n2, n3);
        }
        return this.live != null ? this.live.n(n, n2, n3) : true;
    }

    @Override
    public boolean e(int n, int n2, int n3) {
        if (this.bundled != null && this.bundled.hasData(n, n2, n3)) {
            return this.bundled.e(n, n2, n3);
        }
        return this.live != null ? this.live.e(n, n2, n3) : true;
    }

    @Override
    public boolean hasData(int n, int n2, int n3) {
        return this.bundled != null && this.bundled.hasData(n, n2, n3) || this.live != null && this.live.hasData(n, n2, n3);
    }
}

