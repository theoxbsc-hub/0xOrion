/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Player
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.entities;

import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.Player;
import net.unethicalite.api.query.entities.ActorQuery;
import net.unethicalite.api.query.results.SceneEntityQueryResults;
import org.apache.commons.lang3.ArrayUtils;

public class PlayerQuery
extends ActorQuery<Player, PlayerQuery> {
    private int[] pids = null;

    public PlayerQuery(Supplier<List<Player>> supplier) {
        super(supplier);
    }

    public PlayerQuery playerIds(int ... pids) {
        this.pids = pids;
        return this;
    }

    @Override
    protected SceneEntityQueryResults<Player> results(List<Player> list) {
        return new SceneEntityQueryResults<Player>(list);
    }

    @Override
    public boolean test(Player player) {
        if (this.pids != null && !ArrayUtils.contains((int[])this.pids, (int)player.getIndex())) {
            return false;
        }
        return super.test(player);
    }
}

