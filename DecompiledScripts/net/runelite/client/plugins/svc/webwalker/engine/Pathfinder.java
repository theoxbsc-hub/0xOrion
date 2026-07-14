/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.webwalker.engine.CollisionMap;
import net.runelite.client.plugins.svc.webwalker.engine.Path;
import net.runelite.client.plugins.svc.webwalker.engine.PathStep;
import net.runelite.client.plugins.svc.webwalker.engine.WorldPointUtil;
import net.unethicalite.api.movement.pathfinder.model.Transport;

public final class Pathfinder {
    private static final int TRANSPORT_COST = 5;
    private static final int[] DX = new int[]{0, 0, 1, -1, 1, -1, 1, -1};
    private static final int[] DY = new int[]{1, -1, 0, 0, 1, 1, -1, -1};
    private final CollisionMap map;
    private final Map<Integer, List<Transport>> transports;

    public Pathfinder(CollisionMap collisionMap, Map<Integer, List<Transport>> map) {
        this.map = collisionMap;
        this.transports = map != null ? map : Collections.emptyMap();
    }

    public Path find(WorldPoint worldPoint, WorldPoint worldPoint2, int n, boolean bl, int n2) {
        int n3;
        if (worldPoint == null || worldPoint2 == null) {
            return Path.empty();
        }
        int n4 = WorldPointUtil.pack(worldPoint);
        int n5 = WorldPointUtil.pack(worldPoint2);
        int n6 = Math.max(0, n);
        boolean bl2 = Pathfinder.isInWilderness(worldPoint2.getX(), worldPoint2.getY());
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> hashMap2 = new HashMap<Integer, Integer>();
        HashMap<Integer, Transport> hashMap3 = new HashMap<Integer, Transport>();
        PriorityQueue<int[]> priorityQueue = new PriorityQueue<int[]>((nArray, nArray2) -> {
            if (nArray[2] != nArray2[2]) {
                return Integer.compare(nArray[2], nArray2[2]);
            }
            return Integer.compare(nArray2[1], nArray[1]);
        });
        hashMap.put(n4, 0);
        priorityQueue.add(new int[]{n4, 0, WorldPointUtil.chebyshev(n4, n5)});
        int n7 = 0;
        int n8 = n4;
        int n9 = WorldPointUtil.chebyshev(n4, n5);
        int n10 = -1;
        while (!priorityQueue.isEmpty()) {
            int[] nArray3 = (int[])priorityQueue.poll();
            n3 = nArray3[0];
            int n11 = nArray3[1];
            Integer n12 = (Integer)hashMap.get(n3);
            if (n12 == null || n11 > n12) continue;
            int n13 = WorldPointUtil.chebyshev(n3, n5);
            if (n13 < n9) {
                n9 = n13;
                n8 = n3;
            }
            if (n13 <= n6) {
                n10 = n3;
                break;
            }
            if (++n7 > n2) break;
            int n14 = WorldPointUtil.x(n3);
            int n15 = WorldPointUtil.y(n3);
            int n16 = WorldPointUtil.plane(n3);
            for (int i = 0; i < 8; ++i) {
                if (!this.walkable(n14, n15, n16, i)) continue;
                int n17 = n14 + DX[i];
                int n18 = n15 + DY[i];
                if (n17 < 0 || n18 < 0 || bl && !bl2 && Pathfinder.isInWilderness(n17, n18)) continue;
                int n19 = WorldPointUtil.pack(n17, n18, n16);
                this.relax(n3, n19, n11 + 1, null, hashMap, hashMap2, hashMap3, priorityQueue, n5);
            }
            List<Transport> list = this.transports.get(n3);
            if (list == null) continue;
            for (Transport transport : list) {
                WorldPoint worldPoint3 = transport.getDestination();
                if (worldPoint3 == null || bl && !bl2 && Pathfinder.isInWilderness(worldPoint3.getX(), worldPoint3.getY())) continue;
                int n20 = WorldPointUtil.pack(worldPoint3);
                this.relax(n3, n20, n11 + 5, transport, hashMap, hashMap2, hashMap3, priorityQueue, n5);
            }
        }
        boolean bl3 = n10 != -1;
        n3 = bl3 ? n10 : n8;
        return this.reconstruct(n4, n3, hashMap2, hashMap3, bl3, n7);
    }

    private void relax(int n, int n2, int n3, Transport transport, Map<Integer, Integer> map, Map<Integer, Integer> map2, Map<Integer, Transport> map3, PriorityQueue<int[]> priorityQueue, int n4) {
        Integer n5 = map.get(n2);
        if (n5 != null && n3 >= n5) {
            return;
        }
        map.put(n2, n3);
        map2.put(n2, n);
        if (transport != null) {
            map3.put(n2, transport);
        } else {
            map3.remove(n2);
        }
        priorityQueue.add(new int[]{n2, n3, n3 + WorldPointUtil.chebyshev(n2, n4)});
    }

    private boolean walkable(int n, int n2, int n3, int n4) {
        switch (n4) {
            case 0: {
                return this.map.n(n, n2, n3);
            }
            case 1: {
                return this.map.s(n, n2, n3);
            }
            case 2: {
                return this.map.e(n, n2, n3);
            }
            case 3: {
                return this.map.w(n, n2, n3);
            }
            case 4: {
                return this.map.ne(n, n2, n3);
            }
            case 5: {
                return this.map.nw(n, n2, n3);
            }
            case 6: {
                return this.map.se(n, n2, n3);
            }
            case 7: {
                return this.map.sw(n, n2, n3);
            }
        }
        return false;
    }

    private Path reconstruct(int n, int n2, Map<Integer, Integer> map, Map<Integer, Transport> map2, boolean bl, int n3) {
        if (n2 == n && !map.containsKey(n2)) {
            return new Path(Collections.emptyList(), bl, n3);
        }
        ArrayList<PathStep> arrayList = new ArrayList<PathStep>();
        int n4 = n2;
        int n5 = 0;
        while (true) {
            Integer n6;
            arrayList.add(new PathStep(WorldPointUtil.unpack(n4), map2.get(n4)));
            if (n4 == n || (n6 = map.get(n4)) == null || ++n5 > 200000) break;
            n4 = n6;
        }
        Collections.reverse(arrayList);
        if (!arrayList.isEmpty() && ((PathStep)arrayList.get((int)0)).point.getX() == WorldPointUtil.x(n) && ((PathStep)arrayList.get((int)0)).point.getY() == WorldPointUtil.y(n) && ((PathStep)arrayList.get((int)0)).point.getPlane() == WorldPointUtil.plane(n) && !((PathStep)arrayList.get(0)).isTransport()) {
            arrayList.remove(0);
        }
        return new Path(arrayList, bl, n3);
    }

    public static boolean isInWilderness(int n, int n2) {
        return n >= 2944 && n <= 3391 && n2 >= 3523 && n2 <= 3967;
    }
}

