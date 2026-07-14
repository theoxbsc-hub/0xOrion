/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.NPC
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Entities;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.query.entities.NPCQuery;
import net.unethicalite.client.Static;

public class NPCs
extends Entities<NPC> {
    private static final NPCs NPCS = new NPCs();

    private NPCs() {
    }

    @Override
    protected List<NPC> all(Predicate<? super NPC> filter) {
        ArrayList<NPC> out = new ArrayList<NPC>();
        List npcs = Static.getClient().getNpcs();
        for (NPC npc : npcs) {
            if (!filter.test((NPC)npc)) continue;
            out.add(npc);
        }
        return out;
    }

    public static NPCQuery query() {
        return NPCs.query(NPCs::getAll);
    }

    public static NPCQuery query(Supplier<List<NPC>> supplier) {
        return new NPCQuery(supplier);
    }

    public static List<NPC> getAll() {
        return NPCs.getAll((NPC x) -> true);
    }

    public static List<NPC> getAll(Predicate<NPC> filter) {
        return NPCS.all((Predicate<? super NPC>)filter);
    }

    public static List<NPC> getAll(int ... ids) {
        return NPCS.all(ids);
    }

    public static List<NPC> getAll(String ... names) {
        return NPCS.all(names);
    }

    public static NPC getNearest(WorldPoint to, Predicate<NPC> filter) {
        return NPCS.nearest(to, filter);
    }

    public static NPC getNearest(WorldPoint to, int ... ids) {
        return (NPC)NPCS.nearest(to, ids);
    }

    public static NPC getNearest(WorldPoint to, String ... names) {
        return (NPC)NPCS.nearest(to, names);
    }

    public static NPC getNearest(Predicate<NPC> filter) {
        return NPCs.getNearest(Players.getLocal().getWorldLocation(), filter);
    }

    public static NPC getNearest(int ... ids) {
        return NPCs.getNearest(Players.getLocal().getWorldLocation(), ids);
    }

    public static NPC getNearest(String ... names) {
        return NPCs.getNearest(Players.getLocal().getWorldLocation(), names);
    }
}

