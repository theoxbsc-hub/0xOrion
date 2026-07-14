/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.client.plugins.svc.webwalker.engine.WorldPointUtil;
import net.unethicalite.api.movement.pathfinder.TransportLoader;
import net.unethicalite.api.movement.pathfinder.model.Transport;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirements;

public final class TransportGraph {
    private TransportGraph() {
    }

    public static Map<Integer, List<Transport>> build(boolean bl) {
        List<Transport> list;
        try {
            TransportLoader.refreshTransports();
            list = TransportLoader.buildTransports();
        }
        catch (Throwable throwable) {
            return Collections.emptyMap();
        }
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        HashMap<Integer, List<Transport>> hashMap = new HashMap<Integer, List<Transport>>();
        for (Transport transport : list) {
            if (transport == null || transport.getSource() == null || transport.getDestination() == null || bl && !TransportGraph.fulfilled(transport.getRequirements())) continue;
            int n2 = WorldPointUtil.pack(transport.getSource());
            hashMap.computeIfAbsent(n2, n -> new ArrayList()).add(transport);
        }
        return hashMap;
    }

    private static boolean fulfilled(Requirements requirements) {
        if (requirements == null) {
            return true;
        }
        try {
            return requirements.fulfilled();
        }
        catch (Throwable throwable) {
            return false;
        }
    }
}

