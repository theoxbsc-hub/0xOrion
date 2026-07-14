/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.agility;

import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.agility.AgilityCourse;
import net.runelite.client.plugins.svc.agility.AgilityObstacle;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;

public final class AgilityEngine {
    private static final int STAND_REACH = 1;
    private static final long NO_MOVE_RECLICK_MS = 2400L;
    private static final long CLICK_FAIL_TIMEOUT_MS = 4000L;
    private static final int CROSS_TILES = 3;
    private static final long OBSTACLE_STUCK_MS = 20000L;
    private static final int MAX_FAILS_BEFORE_SKIP = 4;
    private static final int PLANE_PENALTY = 8;
    private static final long RECOVERY_STUCK_MS = 90000L;
    private final Client client;
    private final HumanInput input;
    private AgilityCourse runningCourse;
    private int currentIndex;
    private boolean awaitingTraversal;
    private boolean traverseStarted;
    private boolean sawExpectedAnim;
    private boolean sawPlaneChange;
    private int planeAtClick;
    private WorldPoint preTraversePos;
    private long lastClickMs;
    private long obstacleStartMs;
    private int failCount;
    private boolean needsResync;
    private int consecutiveSkips;
    private long lastCompletionMs;
    private boolean recoveryNeeded;
    private int laps;
    private int obstaclesDone;
    private String status = "Starting";

    public AgilityEngine(Client client) {
        this(client, null);
    }

    public AgilityEngine(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public void reset() {
        this.runningCourse = null;
        this.currentIndex = 0;
        this.awaitingTraversal = false;
        this.traverseStarted = false;
        this.sawExpectedAnim = false;
        this.sawPlaneChange = false;
        this.preTraversePos = null;
        this.lastClickMs = 0L;
        this.obstacleStartMs = 0L;
        this.failCount = 0;
        this.needsResync = true;
        this.consecutiveSkips = 0;
        this.lastCompletionMs = System.currentTimeMillis();
        this.recoveryNeeded = false;
        this.laps = 0;
        this.obstaclesDone = 0;
        this.status = "Starting";
    }

    public void requestResync() {
        this.needsResync = true;
    }

    public boolean needsRecovery() {
        return this.recoveryNeeded;
    }

    public void clearRecovery() {
        this.recoveryNeeded = false;
        this.consecutiveSkips = 0;
        this.lastCompletionMs = System.currentTimeMillis();
        this.needsResync = true;
    }

    public int tick(Player player, Settings settings) {
        boolean bl;
        int n;
        AgilityObstacle agilityObstacle;
        int n2;
        AgilityCourse agilityCourse = settings.course;
        if (agilityCourse == null) {
            this.status = "No course enabled";
            return 800;
        }
        if (agilityCourse != this.runningCourse) {
            this.runningCourse = agilityCourse;
            this.currentIndex = 0;
            this.awaitingTraversal = false;
            this.preTraversePos = null;
            this.needsResync = true;
        }
        int n3 = agilityCourse.size();
        WorldPoint worldPoint = player.getWorldLocation();
        int n4 = player.getAnimation();
        boolean bl2 = n4 != -1 || player.isMoving() || Movement.isWalking();
        long l = System.currentTimeMillis();
        if (bl2) {
            if (this.awaitingTraversal) {
                this.traverseStarted = true;
                AgilityObstacle agilityObstacle2 = agilityCourse.obstacles().get(this.currentIndex);
                if (agilityObstacle2.matchesAnim(n4)) {
                    this.sawExpectedAnim = true;
                }
                if (worldPoint != null && worldPoint.getPlane() != this.planeAtClick) {
                    this.sawPlaneChange = true;
                }
                this.status = "Traversing " + agilityObstacle2.label();
            }
            return 300;
        }
        if (this.needsResync) {
            n2 = this.nearestStandIndex(agilityCourse, worldPoint);
            if (n2 >= 0) {
                this.currentIndex = n2;
            }
            this.needsResync = false;
            this.awaitingTraversal = false;
            this.obstacleStartMs = l;
            this.failCount = 0;
            this.consecutiveSkips = 0;
            this.lastCompletionMs = l;
        }
        if ((n2 = this.standOnIndex(agilityCourse, worldPoint)) >= 0 && n2 != this.currentIndex) {
            this.arriveAt(agilityCourse, n2);
        } else if (n2 < 0 && this.awaitingTraversal && this.traverseStarted && worldPoint != null && !worldPoint.equals((Object)this.preTraversePos)) {
            agilityObstacle = agilityCourse.obstacles().get(this.currentIndex);
            AgilityObstacle agilityObstacle3 = agilityCourse.obstacles().get((this.currentIndex + 1) % n3);
            n = AgilityEngine.crossedToward(worldPoint, agilityObstacle.anchor(), agilityObstacle3.anchor());
            if (this.sawExpectedAnim || this.sawPlaneChange || n != 0) {
                this.arriveAt(agilityCourse, (this.currentIndex + 1) % n3);
            }
        }
        agilityObstacle = agilityCourse.obstacles().get(this.currentIndex);
        if (this.consecutiveSkips >= n3 || l - this.lastCompletionMs > 90000L) {
            this.recoveryNeeded = true;
            this.status = "Off-course \u2014 recovering (re-travel to start)";
            return 600;
        }
        if (this.awaitingTraversal) {
            boolean bl3;
            boolean bl4 = bl3 = worldPoint != null && !worldPoint.equals((Object)this.preTraversePos);
            if (bl3) {
                if (l - this.lastClickMs < 4000L) {
                    this.status = "Traversing " + agilityObstacle.label();
                    return 300;
                }
                this.awaitingTraversal = false;
            } else {
                if (l - this.lastClickMs < 2400L) {
                    this.status = "Clicking " + agilityObstacle.label();
                    return 250;
                }
                this.awaitingTraversal = false;
                ++this.failCount;
            }
        }
        if (l - this.obstacleStartMs > 20000L || this.failCount >= 4) {
            this.skipForward(agilityCourse, agilityObstacle);
            return 600;
        }
        boolean bl5 = bl = worldPoint != null && worldPoint.getPlane() == agilityObstacle.stand().getPlane() && AgilityEngine.dist2D(worldPoint, agilityObstacle.stand()) <= 1;
        if (!bl) {
            if (agilityObstacle.stand().getPlane() != worldPoint.getPlane() && (n = this.nearestSamePlaneStandIndex(agilityCourse, worldPoint)) >= 0 && n != this.currentIndex) {
                this.currentIndex = n;
                this.awaitingTraversal = false;
                this.obstacleStartMs = l;
                this.failCount = 0;
                this.status = "Re-syncing (wrong plane) \u2192 " + agilityCourse.obstacles().get(n).label();
                return 400;
            }
            this.status = "Walking to " + agilityObstacle.label();
            this.walkTo(agilityObstacle.stand());
            return 1000;
        }
        TileObject tileObject = TileObjects.getNearest(agilityObstacle.anchor(), agilityObstacle.ids());
        if (tileObject == null) {
            this.status = "Waiting for " + agilityObstacle.label();
            return 600;
        }
        if (settings.randomizeDelay) {
            Time.sleep(Rand.nextInt(150, 600));
        }
        if (this.input != null) {
            this.input.interactObjectFirst(tileObject);
        } else {
            MousePackets.queueClickPacket();
            ObjectPackets.tileObjectFirstOption(tileObject, false);
        }
        this.preTraversePos = worldPoint;
        this.planeAtClick = worldPoint.getPlane();
        this.lastClickMs = l;
        this.awaitingTraversal = true;
        this.traverseStarted = false;
        this.sawExpectedAnim = false;
        this.sawPlaneChange = false;
        this.status = "Clicking " + agilityObstacle.label();
        return 600;
    }

    private void arriveAt(AgilityCourse agilityCourse, int n) {
        int n2 = agilityCourse.size();
        if (n == (this.currentIndex + 1) % n2) {
            ++this.obstaclesDone;
            this.consecutiveSkips = 0;
            this.lastCompletionMs = System.currentTimeMillis();
            if (n == 0) {
                ++this.laps;
            }
        }
        this.currentIndex = n;
        this.awaitingTraversal = false;
        this.traverseStarted = false;
        this.sawExpectedAnim = false;
        this.sawPlaneChange = false;
        this.obstacleStartMs = System.currentTimeMillis();
        this.failCount = 0;
        this.status = "At " + agilityCourse.obstacles().get(n).label();
    }

    private void skipForward(AgilityCourse agilityCourse, AgilityObstacle agilityObstacle) {
        int n = (this.currentIndex + 1) % agilityCourse.size();
        if (n == 0) {
            ++this.laps;
        }
        this.currentIndex = n;
        this.awaitingTraversal = false;
        this.traverseStarted = false;
        this.failCount = 0;
        ++this.consecutiveSkips;
        this.obstacleStartMs = System.currentTimeMillis();
        this.status = "Skipping stuck " + agilityObstacle.label();
        this.walkTo(agilityCourse.obstacles().get(n).stand());
    }

    private int standOnIndex(AgilityCourse agilityCourse, WorldPoint worldPoint) {
        if (worldPoint == null) {
            return -1;
        }
        int n = -1;
        int n2 = Integer.MAX_VALUE;
        List<AgilityObstacle> list = agilityCourse.obstacles();
        for (int i = 0; i < list.size(); ++i) {
            int n3;
            WorldPoint worldPoint2 = list.get(i).stand();
            if (worldPoint2 == null || worldPoint2.getPlane() != worldPoint.getPlane() || (n3 = AgilityEngine.dist2D(worldPoint, worldPoint2)) > 1 || n3 >= n2) continue;
            n2 = n3;
            n = i;
        }
        return n;
    }

    private int nearestSamePlaneStandIndex(AgilityCourse agilityCourse, WorldPoint worldPoint) {
        if (worldPoint == null) {
            return -1;
        }
        int n = -1;
        int n2 = Integer.MAX_VALUE;
        List<AgilityObstacle> list = agilityCourse.obstacles();
        for (int i = 0; i < list.size(); ++i) {
            int n3;
            WorldPoint worldPoint2 = list.get(i).stand();
            if (worldPoint2 == null || worldPoint2.getPlane() != worldPoint.getPlane() || (n3 = AgilityEngine.dist2D(worldPoint, worldPoint2)) >= n2) continue;
            n2 = n3;
            n = i;
        }
        return n;
    }

    private int nearestStandIndex(AgilityCourse agilityCourse, WorldPoint worldPoint) {
        if (worldPoint == null) {
            return -1;
        }
        int n = -1;
        int n2 = Integer.MAX_VALUE;
        List<AgilityObstacle> list = agilityCourse.obstacles();
        for (int i = 0; i < list.size(); ++i) {
            int n3 = AgilityEngine.score(worldPoint, list.get(i).stand());
            if (n3 >= n2) continue;
            n2 = n3;
            n = i;
        }
        return n;
    }

    private static int score(WorldPoint worldPoint, WorldPoint worldPoint2) {
        if (worldPoint == null || worldPoint2 == null) {
            return Integer.MAX_VALUE;
        }
        int n = AgilityEngine.dist2D(worldPoint, worldPoint2);
        if (n == Integer.MAX_VALUE) {
            return n;
        }
        return n + (worldPoint.getPlane() != worldPoint2.getPlane() ? 8 : 0);
    }

    private static boolean crossedToward(WorldPoint worldPoint, WorldPoint worldPoint2, WorldPoint worldPoint3) {
        if (worldPoint == null || worldPoint2 == null || worldPoint3 == null) {
            return false;
        }
        int n = worldPoint3.getX() - worldPoint2.getX();
        int n2 = worldPoint3.getY() - worldPoint2.getY();
        if (Math.abs(n) >= Math.abs(n2)) {
            return n >= 0 ? worldPoint.getX() - worldPoint2.getX() >= 3 : worldPoint2.getX() - worldPoint.getX() >= 3;
        }
        return n2 >= 0 ? worldPoint.getY() - worldPoint2.getY() >= 3 : worldPoint2.getY() - worldPoint.getY() >= 3;
    }

    private void walkTo(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return;
        }
        if (this.input != null) {
            this.input.walkToward(this.client.getLocalPlayer().getWorldLocation(), worldPoint);
        } else {
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(worldPoint);
        }
    }

    private static int dist2D(WorldPoint worldPoint, WorldPoint worldPoint2) {
        if (worldPoint == null || worldPoint2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(Math.abs(worldPoint.getX() - worldPoint2.getX()), Math.abs(worldPoint.getY() - worldPoint2.getY()));
    }

    public AgilityCourse getRunningCourse() {
        return this.runningCourse;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String string) {
        this.status = string;
    }

    public int getLaps() {
        return this.laps;
    }

    public int getObstaclesDone() {
        return this.obstaclesDone;
    }

    public static final class Settings {
        public final AgilityCourse course;
        public final boolean randomizeDelay;

        public Settings(AgilityCourse agilityCourse, boolean bl) {
            this.course = agilityCourse;
            this.randomizeDelay = bl;
        }
    }
}

