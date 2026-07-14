/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.NPC
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.unethicalite.api.query.entities;

import java.util.List;
import java.util.function.Supplier;
import net.runelite.api.NPC;
import net.unethicalite.api.query.entities.ActorQuery;
import net.unethicalite.api.query.results.SceneEntityQueryResults;
import org.apache.commons.lang3.ArrayUtils;

public class NPCQuery
extends ActorQuery<NPC, NPCQuery> {
    private int[] indices = null;

    public NPCQuery(Supplier<List<NPC>> supplier) {
        super(supplier);
    }

    public NPCQuery indices(int ... indices) {
        this.indices = indices;
        return this;
    }

    @Override
    protected SceneEntityQueryResults<NPC> results(List<NPC> list) {
        return new SceneEntityQueryResults<NPC>(list);
    }

    @Override
    public boolean test(NPC npc) {
        if (this.indices != null && !ArrayUtils.contains((int[])this.indices, (int)npc.getIndex())) {
            return false;
        }
        return super.test(npc);
    }
}

