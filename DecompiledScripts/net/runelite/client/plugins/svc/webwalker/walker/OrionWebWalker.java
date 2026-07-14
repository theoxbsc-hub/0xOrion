/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.walker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.webwalker.engine.CollisionMap;
import net.runelite.client.plugins.svc.webwalker.engine.Path;
import net.runelite.client.plugins.svc.webwalker.engine.PathStep;
import net.runelite.client.plugins.svc.webwalker.engine.Pathfinder;
import net.runelite.client.plugins.svc.webwalker.engine.TransportGraph;
import net.runelite.client.plugins.svc.webwalker.engine.WorldPointUtil;
import net.runelite.client.plugins.svc.webwalker.walker.PacketStepDispatcher;
import net.runelite.client.plugins.svc.webwalker.walker.StepDispatcher;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.pathfinder.model.Transport;

public final class OrionWebWalker {
    private int arriveRadius = 1;
    private int walkStep = 12;
    private int maxNodes = 200000;
    private boolean avoidWilderness = false;
    private boolean useTransports = true;
    private int replanEveryTicks = 10;
    private int offPathDist = 12;
    private int stallLimit = 4;
    private int transportWaitTicks = 12;
    private long transportRefreshMs = 60000L;
    private final CollisionMap collision;
    private final StepDispatcher dispatcher;
    private Path path;
    private int idx;
    private WorldPoint planDest;
    private int ticksSincePlan;
    private WorldPoint lastLoc;
    private int idleTicks;
    private WorldPoint lastDispatch;
    private Transport firedTransport;
    private int transportWait;
    private Map<Integer, List<Transport>> transportsCache;
    private long transportsBuiltAt;
    private String status = "idle";

    public OrionWebWalker(CollisionMap collisionMap, StepDispatcher stepDispatcher) {
        this.collision = collisionMap;
        this.dispatcher = stepDispatcher != null ? stepDispatcher : new PacketStepDispatcher();
    }

    public Result step(WorldPoint worldPoint) {
        if (worldPoint == null) {
            this.status = "no destination";
            return Result.STUCK;
        }
        Player player = Players.getLocal();
        if (player == null || player.getWorldLocation() == null) {
            this.status = "no player";
            return Result.RUNNING;
        }
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (worldPoint2.getPlane() == worldPoint.getPlane() && WorldPointUtil.chebyshev(worldPoint2, worldPoint) <= this.arriveRadius) {
            this.reset();
            this.status = "arrived";
            return Result.ARRIVED;
        }
        if (worldPoint2.equals((Object)this.lastLoc)) {
            ++this.idleTicks;
        } else {
            this.idleTicks = 0;
            this.lastLoc = worldPoint2;
        }
        this.ensurePlan(worldPoint2, worldPoint);
        if (this.path == null || this.path.isEmpty()) {
            WorldPoint worldPoint3 = this.clampToward(worldPoint2, worldPoint, this.walkStep);
            this.dispatchIfNeeded(worldPoint2, worldPoint3);
            this.status = "greedy \u2192 " + String.valueOf(worldPoint);
            if (this.idleTicks > this.stallLimit * 3) {
                this.status = "stuck: no route & no progress";
                return Result.STUCK;
            }
            return Result.RUNNING;
        }
        this.advanceReached(worldPoint2);
        if (this.idx >= this.path.steps.size()) {
            this.path = null;
            return Result.RUNNING;
        }
        PathStep pathStep = this.path.steps.get(this.idx);
        if (pathStep.isTransport()) {
            return this.handleTransport(worldPoint2, pathStep);
        }
        WorldPoint worldPoint4 = this.walkWaypoint(worldPoint2);
        this.dispatchIfNeeded(worldPoint2, worldPoint4);
        this.status = "walk \u2192 " + String.valueOf(worldPoint4);
        if (this.idleTicks > this.stallLimit) {
            this.path = null;
            if (this.idleTicks > this.stallLimit * 3) {
                this.status = "stuck: walk no progress";
                return Result.STUCK;
            }
        }
        return Result.RUNNING;
    }

    public void reset() {
        this.path = null;
        this.idx = 0;
        this.planDest = null;
        this.ticksSincePlan = 0;
        this.firedTransport = null;
        this.transportWait = 0;
        this.lastDispatch = null;
        this.idleTicks = 0;
        this.lastLoc = null;
    }

    private Result handleTransport(WorldPoint worldPoint, PathStep pathStep) {
        Transport transport = pathStep.transport;
        WorldPoint worldPoint2 = transport.getSource();
        WorldPoint worldPoint3 = pathStep.point;
        int n = Math.max(1, transport.getSourceRadius());
        if (worldPoint2 != null && (worldPoint.getPlane() != worldPoint2.getPlane() || WorldPointUtil.chebyshev(worldPoint, worldPoint2) > n)) {
            WorldPoint worldPoint4 = this.clampToward(worldPoint, worldPoint2, this.walkStep);
            this.dispatchIfNeeded(worldPoint, worldPoint4);
            this.status = "\u2192 transport @ " + String.valueOf(worldPoint2);
            if (this.idleTicks > this.stallLimit * 3) {
                this.path = null;
                return Result.STUCK;
            }
            return Result.RUNNING;
        }
        if (this.firedTransport == transport) {
            boolean bl;
            boolean bl2 = bl = worldPoint3 != null && worldPoint.getPlane() == worldPoint3.getPlane() && WorldPointUtil.chebyshev(worldPoint, worldPoint3) <= Math.max(1, transport.getDestinationRadius());
            if (bl) {
                this.firedTransport = null;
                ++this.idx;
                this.lastDispatch = null;
                this.status = "transport done";
                return Result.RUNNING;
            }
            if (--this.transportWait <= 0) {
                this.firedTransport = null;
                this.path = null;
                this.status = "transport failed, re-planning";
            }
            return Result.RUNNING;
        }
        try {
            Runnable runnable = transport.getHandler();
            if (runnable != null) {
                runnable.run();
            }
        }
        catch (Throwable throwable) {
            this.status = "transport handler threw";
        }
        this.firedTransport = transport;
        this.transportWait = this.transportWaitTicks;
        this.status = "transport " + String.valueOf(worldPoint2) + " \u2192 " + String.valueOf(worldPoint3);
        return Result.RUNNING;
    }

    private void ensurePlan(WorldPoint worldPoint, WorldPoint worldPoint2) {
        Path path;
        boolean bl;
        ++this.ticksSincePlan;
        boolean bl2 = bl = this.path == null || this.planDest == null || !this.planDest.equals((Object)worldPoint2);
        if (!bl && (this.ticksSincePlan >= this.replanEveryTicks || this.offPath(worldPoint))) {
            bl = true;
        }
        if (!bl) {
            return;
        }
        Map<Integer, List<Transport>> map = this.useTransports ? this.transports() : Collections.emptyMap();
        this.path = path = new Pathfinder(this.collision, map).find(worldPoint, worldPoint2, this.arriveRadius, this.avoidWilderness, this.maxNodes);
        this.idx = 0;
        this.planDest = worldPoint2;
        this.ticksSincePlan = 0;
        this.firedTransport = null;
        this.lastDispatch = null;
    }

    private boolean offPath(WorldPoint worldPoint) {
        if (this.path == null || this.path.isEmpty()) {
            return false;
        }
        int n = Math.min(this.path.steps.size(), this.idx + 30);
        int n2 = Integer.MAX_VALUE;
        for (int i = this.idx; i < n; ++i) {
            WorldPoint worldPoint2 = this.path.steps.get((int)i).point;
            if (worldPoint2.getPlane() != worldPoint.getPlane()) continue;
            n2 = Math.min(n2, WorldPointUtil.chebyshev(worldPoint, worldPoint2));
        }
        return n2 > this.offPathDist;
    }

    private Map<Integer, List<Transport>> transports() {
        long l = System.currentTimeMillis();
        if (this.transportsCache == null || l - this.transportsBuiltAt > this.transportRefreshMs) {
            this.transportsCache = TransportGraph.build(true);
            this.transportsBuiltAt = l;
        }
        return this.transportsCache;
    }

    private void advanceReached(WorldPoint worldPoint) {
        PathStep pathStep;
        while (this.idx < this.path.steps.size() && !(pathStep = this.path.steps.get(this.idx)).isTransport() && pathStep.point.getPlane() == worldPoint.getPlane() && WorldPointUtil.chebyshev(worldPoint, pathStep.point) <= 1) {
            ++this.idx;
        }
    }

    private WorldPoint walkWaypoint(WorldPoint worldPoint) {
        PathStep pathStep;
        WorldPoint worldPoint2 = this.path.steps.get((int)this.idx).point;
        for (int i = this.idx; i < this.path.steps.size() && !(pathStep = this.path.steps.get(i)).isTransport() && pathStep.point.getPlane() == worldPoint.getPlane() && WorldPointUtil.chebyshev(worldPoint, pathStep.point) <= this.walkStep; ++i) {
            worldPoint2 = pathStep.point;
        }
        return worldPoint2;
    }

    private WorldPoint clampToward(WorldPoint worldPoint, WorldPoint worldPoint2, int n) {
        if (worldPoint.getPlane() != worldPoint2.getPlane()) {
            return worldPoint2;
        }
        int n2 = worldPoint2.getX() - worldPoint.getX();
        int n3 = worldPoint2.getY() - worldPoint.getY();
        int n4 = Math.max(Math.abs(n2), Math.abs(n3));
        if (n4 <= n) {
            return worldPoint2;
        }
        int n5 = worldPoint.getX() + Math.round((float)n2 * (float)n / (float)n4);
        int n6 = worldPoint.getY() + Math.round((float)n3 * (float)n / (float)n4);
        return new WorldPoint(n5, n6, worldPoint.getPlane());
    }

    private void dispatchIfNeeded(WorldPoint worldPoint, WorldPoint worldPoint2) {
        boolean bl;
        if (worldPoint2 == null || worldPoint2.equals((Object)worldPoint)) {
            return;
        }
        boolean bl2 = !worldPoint2.equals((Object)this.lastDispatch);
        boolean bl3 = bl = this.idleTicks >= 1;
        if (bl2 || bl) {
            this.dispatcher.step(worldPoint, worldPoint2);
            this.lastDispatch = worldPoint2;
        }
    }

    public String getStatus() {
        return this.status;
    }

    public WorldPoint getDestination() {
        return this.planDest;
    }

    public boolean hasCollisionData() {
        return this.collision != null && this.collision.hasData(3222, 3218, 0);
    }

    public List<WorldPoint> getRemainingPath() {
        if (this.path == null) {
            return Collections.emptyList();
        }
        ArrayList<WorldPoint> arrayList = new ArrayList<WorldPoint>();
        for (int i = this.idx; i < this.path.steps.size(); ++i) {
            arrayList.add(this.path.steps.get((int)i).point);
        }
        return arrayList;
    }

    public OrionWebWalker arriveRadius(int n) {
        this.arriveRadius = Math.max(0, n);
        return this;
    }

    public OrionWebWalker walkStep(int n) {
        this.walkStep = Math.max(1, n);
        return this;
    }

    public OrionWebWalker maxNodes(int n) {
        this.maxNodes = Math.max(1000, n);
        return this;
    }

    public OrionWebWalker avoidWilderness(boolean bl) {
        this.avoidWilderness = bl;
        return this;
    }

    public OrionWebWalker useTransports(boolean bl) {
        this.useTransports = bl;
        return this;
    }

    public static enum Result {
        ARRIVED,
        RUNNING,
        STUCK;

    }
}

