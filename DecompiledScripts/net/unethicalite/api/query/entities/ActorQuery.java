/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Actor
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.entities;

import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.Actor;
import net.unethicalite.api.Interactable;
import net.unethicalite.api.query.entities.SceneEntityQuery;
import org.apache.commons.lang3.ArrayUtils;

public abstract class ActorQuery<T extends Actor, Q extends ActorQuery<T, Q>>
extends SceneEntityQuery<T, Q> {
    private int[] levels = null;
    private int[] animations = null;
    private Interactable[] targeting = null;
    private Boolean moving = null;

    protected ActorQuery(Supplier<List<T>> supplier) {
        super(supplier);
    }

    public Q levels(int ... levels) {
        this.levels = levels;
        return (Q)this;
    }

    public Q animations(int ... animations) {
        this.animations = animations;
        return (Q)this;
    }

    public Q targeting(Interactable ... targets) {
        this.targeting = targets;
        return (Q)this;
    }

    public Q moving(Boolean moving) {
        this.moving = moving;
        return (Q)this;
    }

    @Override
    public boolean test(T t) {
        if (this.levels != null && ArrayUtils.contains((int[])this.levels, (int)t.getCombatLevel())) {
            return false;
        }
        if (this.animations != null && ArrayUtils.contains((int[])this.animations, (int)t.getAnimation())) {
            return false;
        }
        if (this.moving != null && this.moving.booleanValue() != t.isMoving()) {
            return false;
        }
        if (this.targeting != null && !ArrayUtils.contains((Object[])this.targeting, (Object)t.getInteracting())) {
            return false;
        }
        return super.test(t);
    }
}

