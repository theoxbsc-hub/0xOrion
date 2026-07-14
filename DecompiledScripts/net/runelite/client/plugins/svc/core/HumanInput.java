/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.Perspective
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.runelite.client.plugins.svc.core;

import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.svc.core.AccountProfile;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.utils.CoordUtils;

public final class HumanInput {
    private static final int WALK_STEP = 15;
    private static final int MINIMAP_RANGE = 6400;
    private static final int WALK_STALL_LIMIT = 2;
    private static final int MINIMAP_GROUP = 160;
    private static final int MINIMAP_CHILD = 0;
    private final Client client;
    private final AccountProfile profile;
    private final Random jitterRng;
    private final Random pathRng;
    private static final int FIXED_JITTER_RAD = 6;
    private long lastMoveMs;
    private long moveCount;
    private long humanizedCount;
    private long fallbackCount;
    private int lastTargetX = -1;
    private int lastTargetY = -1;
    private int lastMoveDist;
    private long lastActionAtMs;
    private WorldPoint lastWalkFrom;
    private WorldPoint lastWalkDest;
    private WorldPoint lastIssuedWaypoint;
    private int walkReclickDist = 3;
    private int walkStallCount;
    private boolean preClickAttack = true;
    private volatile boolean packetsOnly;
    private static final int INVENTORY_GROUP = 149;
    private static final int INVENTORY_CHILD = 0;

    public HumanInput(Client client, AccountProfile accountProfile) {
        this.client = client;
        this.profile = accountProfile;
        this.jitterRng = new Random(System.nanoTime() ^ (long)System.identityHashCode(this) * -7046029254386353131L);
        this.pathRng = new Random(System.nanoTime() * 2685821657736338717L ^ (long)System.identityHashCode(this));
    }

    public void setPreClickAttack(boolean bl) {
        this.preClickAttack = bl;
    }

    public void setPacketsOnly(boolean bl) {
        this.packetsOnly = bl;
    }

    public boolean isPacketsOnly() {
        return this.packetsOnly;
    }

    public AccountProfile profile() {
        return this.profile;
    }

    public long takeLastMoveMs() {
        long l = this.lastMoveMs;
        this.lastMoveMs = 0L;
        return l;
    }

    public long moveCount() {
        return this.moveCount;
    }

    public long humanizedCount() {
        return this.humanizedCount;
    }

    public long fallbackCount() {
        return this.fallbackCount;
    }

    public int lastTargetX() {
        return this.lastTargetX;
    }

    public int lastTargetY() {
        return this.lastTargetY;
    }

    public int lastMoveDist() {
        return this.lastMoveDist;
    }

    public long lastActionAtMs() {
        return this.lastActionAtMs;
    }

    public void interactObject(TileObject tileObject, String string) {
        if (tileObject == null) {
            return;
        }
        this.legit(this.objectPoint(tileObject), () -> tileObject.interact(string), () -> ObjectPackets.tileObjectAction(tileObject, string, false));
    }

    public void interactObjectFirst(TileObject tileObject) {
        if (tileObject == null) {
            return;
        }
        this.legit(this.objectPoint(tileObject), () -> tileObject.interact(0), () -> ObjectPackets.tileObjectFirstOption(tileObject, false));
    }

    public void interactObjectVia(TileObject tileObject, String string) {
        if (tileObject == null) {
            return;
        }
        this.legit(this.objectPoint(tileObject), () -> tileObject.interact(string), () -> ObjectPackets.tileObjectAction(tileObject, string, false));
    }

    public void attackNpc(NPC nPC) {
        if (nPC == null) {
            return;
        }
        this.legit(this.npcPoint(nPC), () -> nPC.interact("Attack"), null);
    }

    public void npcActionVia(NPC nPC, String string) {
        if (nPC == null) {
            return;
        }
        this.legit(this.npcPoint(nPC), () -> nPC.interact(string), null);
    }

    public void interactItemFirst(Item item) {
        if (item == null) {
            return;
        }
        this.legit(this.itemPoint(item), () -> item.interact(0), () -> ItemPackets.itemFirstOption(item));
    }

    public void itemInteractVia(Item item, String string) {
        if (item == null) {
            return;
        }
        this.legit(this.itemPoint(item), () -> item.interact(string), () -> ItemPackets.itemAction(item, string));
    }

    public void useItemOnItem(Item item, Item item2) {
        if (item == null || item2 == null) {
            return;
        }
        this.legit(this.itemPoint(item2), () -> item.useOn(item2), () -> ItemPackets.useItemOnItem(item, item2));
    }

    public void useItemOnItemPacket(Item item, Item item2) {
        this.useItemOnItem(item, item2);
    }

    public void useItemOnObject(Item item, TileObject tileObject) {
        if (item == null || tileObject == null) {
            return;
        }
        this.legit(this.objectPoint(tileObject), () -> item.useOn(tileObject), () -> ObjectPackets.useItemOnTileObject(item, tileObject));
    }

    public void interactGroundItem(TileItem tileItem, String string) {
        if (tileItem == null) {
            return;
        }
        this.legit(this.tileItemPoint(tileItem), () -> tileItem.interact(string), null);
    }

    public void widgetResumePause(int n, int n2) {
        java.awt.Point point = this.widgetPoint(n >>> 16, n & 0xFFFF);
        if (point != null) {
            if (!this.packetsOnly) {
                this.realClick(point);
                return;
            }
            this.primeForPacket(point);
            try {
                MousePackets.queueClickPacket(point.x, point.y);
            }
            catch (Throwable throwable) {}
        } else {
            this.driftAndSettle();
        }
        this.recordHud(point);
        ++this.fallbackCount;
        try {
            WidgetPackets.queueResumePauseWidgetPacket(n, n2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void widgetFirstOption(Widget widget) {
        if (widget == null) {
            return;
        }
        java.awt.Point point = null;
        try {
            if (!widget.isHidden()) {
                point = this.pointInside(widget.getBounds());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (!this.packetsOnly) {
            if (point != null) {
                this.realClick(point);
                return;
            }
        } else if (point != null) {
            this.primeForPacket(point);
            try {
                MousePackets.queueClickPacket(point.x, point.y);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        ++this.fallbackCount;
        try {
            WidgetPackets.widgetFirstOption(widget);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void widgetItemAction(WidgetInfo widgetInfo, Item item, int n) {
        Widget widget = this.containerItemWidget(widgetInfo, item);
        java.awt.Point point = null;
        if (widget != null) {
            try {
                if (!widget.isHidden()) {
                    point = this.pointInside(widget.getBounds());
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.recordHud(point);
            if (point != null) {
                this.primeForPacket(point);
            }
            try {
                widget.interact(n);
                ++this.humanizedCount;
                return;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        ++this.fallbackCount;
        try {
            if (point != null) {
                MousePackets.queueClickPacket(point.x, point.y);
            } else {
                MousePackets.queueClickPacket();
            }
            WidgetPackets.widgetItemAction(widgetInfo, item, n);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void primedItemOp(Item item, Runnable runnable) {
        java.awt.Point point;
        java.awt.Point point2 = point = item != null ? this.itemPoint(item) : null;
        if (point != null) {
            this.recordHud(point);
            this.primeForPacket(point);
        } else {
            this.driftAndSettle();
        }
        this.runPrimedOp(runnable);
    }

    public void primedUiOp(int n, int n2, Runnable runnable) {
        java.awt.Point point;
        java.awt.Point point2 = point = n > 0 ? this.widgetPoint(n, n2) : null;
        if (point != null) {
            this.recordHud(point);
            this.primeForPacket(point);
        } else {
            this.driftAndSettle();
        }
        this.runPrimedOp(runnable);
    }

    public void primedInventoryOp(Runnable runnable) {
        this.primedUiOp(149, 0, runnable);
    }

    public void primedDriftOp(Runnable runnable) {
        this.primedUiOp(-1, -1, runnable);
    }

    private void runPrimedOp(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        try {
            runnable.run();
            ++this.humanizedCount;
        }
        catch (Throwable throwable) {
            ++this.fallbackCount;
        }
    }

    private void driftAndSettle() {
        try {
            Canvas canvas = this.client.getCanvas();
            java.awt.Point point = Mouse.getPosition();
            if (canvas != null && point != null) {
                double[] dArray = this.idleWander(canvas, point.x, point.y);
                this.recordHud(new java.awt.Point((int)dArray[0], (int)dArray[1]));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.dwellSettle();
    }

    public void clickFixed(int n, int n2) {
        this.clickFixed(n, n2, 6, 6);
    }

    public void clickFixed(int n, int n2, int n3, int n4) {
        this.realClick(this.fixedPoint(n, n2, n3, n4));
    }

    public void clickFixedMouse(int n, int n2) {
        this.clickFixedMouse(n, n2, 6, 6);
    }

    public void clickFixedMouse(int n, int n2, int n3, int n4) {
        this.realMouseClick(this.fixedPoint(n, n2, n3, n4));
    }

    private java.awt.Point fixedPoint(int n, int n2, int n3, int n4) {
        return new java.awt.Point(n + this.profile.aimBiasX() + this.gaussOffset(n3), n2 + this.profile.aimBiasY() + this.gaussOffset(n4));
    }

    public void clickWidgetOrFixed(int n, int n2, int n3, int n4) {
        if (this.packetsOnly) {
            block7: {
                try {
                    Widget widget = this.client.getWidget(n, n2);
                    if (widget == null || widget.isHidden()) break block7;
                    java.awt.Point point = null;
                    try {
                        point = this.pointInside(widget.getBounds());
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    if (point != null) {
                        this.primeForPacket(point);
                    }
                    ++this.fallbackCount;
                    WidgetPackets.widgetFirstOption(widget);
                    return;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            this.clickFixed(n3, n4);
            return;
        }
        java.awt.Point point = this.widgetPoint(n, n2);
        if (point != null) {
            this.realClick(point);
            return;
        }
        this.clickFixed(n3, n4);
    }

    public void walkToward(WorldPoint worldPoint, WorldPoint worldPoint2) {
        WorldPoint worldPoint3;
        if (worldPoint == null || worldPoint2 == null) {
            return;
        }
        try {
            if (Movement.getRunEnergy() >= 2500 + this.pathRng.nextInt(1200) && !Movement.isRunEnabled() && this.pathRng.nextDouble() < 0.85) {
                Movement.toggleRun();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (!worldPoint2.equals((Object)this.lastWalkDest)) {
            this.walkReclickDist = 2 + this.pathRng.nextInt(4);
        }
        if (worldPoint2.equals((Object)this.lastWalkDest) && this.lastWalkFrom != null && !worldPoint.equals((Object)this.lastWalkFrom) && this.lastIssuedWaypoint != null && worldPoint.getPlane() == this.lastIssuedWaypoint.getPlane() && worldPoint.distanceTo(this.lastIssuedWaypoint) > this.walkReclickDist) {
            this.walkStallCount = 0;
            this.lastWalkFrom = worldPoint;
            return;
        }
        this.lastIssuedWaypoint = worldPoint3 = this.jitteredWaypoint(worldPoint, worldPoint2);
        this.walkStallCount = worldPoint.equals((Object)this.lastWalkFrom) && worldPoint2.equals((Object)this.lastWalkDest) ? ++this.walkStallCount : 0;
        this.lastWalkFrom = worldPoint;
        this.lastWalkDest = worldPoint2;
        if (this.packetsOnly) {
            java.awt.Point point = this.minimapPoint(worldPoint3);
            if (point == null) {
                point = this.tilePoint(worldPoint3);
            }
            if (point != null) {
                this.primeForPacket(this.jittered(point.x, point.y, 4));
            } else {
                this.recordHud(null);
            }
            ++this.fallbackCount;
            try {
                MovementPackets.sendMovement(worldPoint3);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
        if (this.walkStallCount >= 2) {
            this.walkStallCount = 0;
            this.recordHud(null);
            ++this.fallbackCount;
            try {
                MovementPackets.sendMovement(worldPoint3);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
        java.awt.Point point = this.minimapPoint(worldPoint3);
        if (point != null) {
            this.realClick(this.jittered(point.x, point.y, 4));
            return;
        }
        java.awt.Point point2 = this.tilePoint(worldPoint3);
        if (point2 != null) {
            this.realClick(point2);
            return;
        }
        this.recordHud(null);
        ++this.fallbackCount;
        try {
            MovementPackets.sendMovement(worldPoint3);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void command(String string) {
        if (string == null || string.isEmpty()) {
            return;
        }
        this.sleep(this.profile.reactionDelay());
        for (int i = 0; i < string.length(); ++i) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                Keyboard.type(string.charAt(i));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.sleep(this.keyGapMs());
        }
        this.sleep(this.keyGapMs() + 10);
        try {
            Keyboard.sendEnter();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private int keyGapMs() {
        int n = (int)Math.round(62.0 * Math.exp(this.pathRng.nextGaussian() * 0.38));
        return Math.max(30, Math.min(280, n));
    }

    public void key(char c) {
        this.sleep(this.profile.reactionDelay());
        try {
            Keyboard.type(c);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void legit(java.awt.Point point, Runnable runnable, Runnable runnable2) {
        block5: {
            this.recordHud(point);
            if (this.packetsOnly) {
                this.primeForPacket(point);
            }
            try {
                runnable.run();
                ++this.humanizedCount;
            }
            catch (Throwable throwable) {
                ++this.fallbackCount;
                if (runnable2 == null) break block5;
                try {
                    runnable2.run();
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
            }
        }
        this.maybeGlance(point);
    }

    private void recordHud(java.awt.Point point) {
        this.lastActionAtMs = System.currentTimeMillis();
        if (point != null) {
            this.lastTargetX = point.x;
            this.lastTargetY = point.y;
        }
    }

    private void moveTo(java.awt.Point point) {
        java.awt.Point point2;
        Canvas canvas;
        if (point == null || Thread.currentThread().isInterrupted()) {
            return;
        }
        try {
            canvas = this.client.getCanvas();
        }
        catch (Throwable throwable) {
            canvas = null;
        }
        if (canvas == null) {
            return;
        }
        long l = System.currentTimeMillis();
        try {
            point2 = Mouse.getPosition();
        }
        catch (Throwable throwable) {
            point2 = null;
        }
        double d = point2 != null ? (double)point2.x : (double)point.x;
        double d2 = point2 != null ? (double)point2.y : (double)point.y;
        double d3 = (double)point.x - d;
        double d4 = (double)point.y - d2;
        double d5 = Math.hypot(d3, d4);
        this.lastTargetX = point.x;
        this.lastTargetY = point.y;
        if (this.pathRng.nextDouble() < 0.08) {
            double[] dArray = this.idleWander(canvas, d, d2);
            d = dArray[0];
            d2 = dArray[1];
            d3 = (double)point.x - d;
            d4 = (double)point.y - d2;
            d5 = Math.hypot(d3, d4);
        }
        if (d5 >= 2.0) {
            boolean bl;
            ++this.moveCount;
            this.lastMoveDist = (int)Math.round(d5);
            boolean bl2 = bl = d5 > 120.0 && this.pathRng.nextDouble() < 0.35 + 0.4 * this.profile.overshootBias();
            if (bl) {
                double d6 = 1.05 + this.pathRng.nextDouble() * 0.12;
                double d7 = Math.max(1.0, d5);
                double d8 = (this.pathRng.nextDouble() * 2.0 - 1.0) * Math.min(18.0, d5 * 0.06);
                double d9 = -d4 / d7;
                double d10 = d3 / d7;
                java.awt.Point point3 = new java.awt.Point((int)Math.round(d + d3 * d6 + d9 * d8), (int)Math.round(d2 + d4 * d6 + d10 * d8));
                this.glide(canvas, d, d2, point3.x, point3.y, false);
                d = point3.x;
                d2 = point3.y;
                this.glide(canvas, d, d2, point.x, point.y, true);
                if (this.pathRng.nextDouble() < 0.2) {
                    int n = point.x + (this.pathRng.nextInt(5) - 2);
                    int n2 = point.y + (this.pathRng.nextInt(5) - 2);
                    this.glide(canvas, point.x, point.y, n, n2, true);
                    this.glide(canvas, n, n2, point.x, point.y, true);
                }
            } else {
                this.glide(canvas, d, d2, point.x, point.y, false);
            }
        }
        try {
            Mouse.moved(point.x, point.y, canvas, System.currentTimeMillis());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.lastMoveMs += Math.max(0L, System.currentTimeMillis() - l);
    }

    private void glide(Canvas canvas, double d, double d2, double d3, double d4, boolean bl) {
        double d5 = d3 - d;
        double d6 = d4 - d2;
        double d7 = Math.hypot(d5, d6);
        if (d7 < 1.0) {
            try {
                Mouse.moved((int)Math.round(d3), (int)Math.round(d4), canvas, System.currentTimeMillis());
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
        double d8 = (bl ? 4.0 : 6.0) + this.pathRng.nextDouble() * 6.0;
        int n = (int)Math.max(bl ? 2.0 : 3.0, Math.min(60.0, d7 / (d8 / this.profile.speedScale())));
        double d9 = bl ? Math.min(6.0, d7 * 0.1) : Math.min(45.0, d7 * 0.15);
        double d10 = (this.pathRng.nextDouble() * 2.0 - 1.0) * d9 * this.profile.bowScale();
        double d11 = -d6 / d7;
        double d12 = d5 / d7;
        int n2 = HumanInput.canvasW(canvas);
        int n3 = HumanInput.canvasH(canvas);
        int n4 = !bl && n >= 8 && this.pathRng.nextDouble() < 0.12 ? 2 + this.pathRng.nextInt(n - 3) : -1;
        for (int i = 1; i <= n && !Thread.currentThread().isInterrupted(); ++i) {
            double d13 = (double)i / (double)n;
            double d14 = d13 < 0.5 ? 2.0 * d13 * d13 : 1.0 - Math.pow(-2.0 * d13 + 2.0, 2.0) / 2.0;
            double d15 = Math.sin(Math.PI * d13) * d10;
            double d16 = bl ? 0.6 : 1.2;
            int n5 = HumanInput.clampInt((int)Math.round(d + d5 * d14 + d11 * d15 + (this.pathRng.nextDouble() * 2.0 - 1.0) * d16), 0, n2);
            int n6 = HumanInput.clampInt((int)Math.round(d2 + d6 * d14 + d12 * d15 + (this.pathRng.nextDouble() * 2.0 - 1.0) * d16), 0, n3);
            try {
                Mouse.moved(n5, n6, canvas, System.currentTimeMillis());
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (i == n4) {
                this.sleep(40 + this.pathRng.nextInt(120));
                continue;
            }
            int n7 = 3 + (int)Math.round(Math.abs(this.pathRng.nextGaussian()) * 4.0);
            if (this.pathRng.nextDouble() < 0.06) {
                n7 += 8 + this.pathRng.nextInt(22);
            }
            this.sleep(n7);
        }
    }

    private double[] idleWander(Canvas canvas, double d, double d2) {
        int n = 1 + this.pathRng.nextInt(2);
        double d3 = d;
        double d4 = d2;
        for (int i = 0; i < n && !Thread.currentThread().isInterrupted(); ++i) {
            double d5 = this.pathRng.nextDouble() * Math.PI * 2.0;
            double d6 = 8 + this.pathRng.nextInt(34);
            double d7 = HumanInput.clampInt((int)Math.round(d3 + Math.cos(d5) * d6), 0, HumanInput.canvasW(canvas));
            double d8 = HumanInput.clampInt((int)Math.round(d4 + Math.sin(d5) * d6), 0, HumanInput.canvasH(canvas));
            this.glide(canvas, d3, d4, d7, d8, true);
            d3 = d7;
            d4 = d8;
            if (i + 1 >= n) continue;
            this.sleep(30 + this.pathRng.nextInt(90));
        }
        return new double[]{d3, d4};
    }

    private static int clampInt(int n, int n2, int n3) {
        return n < n2 ? n2 : (n > n3 ? n3 : n);
    }

    private static int canvasW(Canvas canvas) {
        try {
            int n = canvas.getWidth();
            return n > 1 ? n - 1 : 1;
        }
        catch (Throwable throwable) {
            return 1;
        }
    }

    private static int canvasH(Canvas canvas) {
        try {
            int n = canvas.getHeight();
            return n > 1 ? n - 1 : 1;
        }
        catch (Throwable throwable) {
            return 1;
        }
    }

    private void primeForPacket(java.awt.Point point) {
        if (point == null) {
            return;
        }
        this.moveTo(point);
        this.dwellSettle();
    }

    private void dwellSettle() {
        int n;
        int n2 = 0;
        while ((n = (int)Math.round(this.profile.settleBase() * Math.exp(this.pathRng.nextGaussian() * this.profile.settleSigma()))) > 700 && ++n2 < 6) {
        }
        if (n > 700) {
            n = 700;
        }
        if (n < 15) {
            n = 15;
        }
        this.sleep(n);
        this.lastMoveMs += (long)n;
    }

    private void maybeGlance(java.awt.Point point) {
        if (point == null) {
            return;
        }
        if (this.jitterRng.nextDouble() >= 0.06) {
            return;
        }
        try {
            int n = this.client.getCameraYawTarget();
            int n2 = (this.jitterRng.nextInt(2) == 0 ? -1 : 1) * (40 + this.jitterRng.nextInt(200));
            this.client.setCameraYawTarget(((n + n2) % 2048 + 2048) % 2048);
            if (this.jitterRng.nextInt(4) == 0) {
                int n3 = this.client.getCameraPitchTarget() + (this.jitterRng.nextInt(41) - 20);
                n3 = Math.max(128, Math.min(383, n3));
                this.client.setCameraPitchTarget(n3);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void realClick(java.awt.Point point) {
        if (point == null) {
            return;
        }
        if (this.packetsOnly) {
            this.primeForPacket(point);
            this.lastActionAtMs = System.currentTimeMillis();
            this.lastTargetX = point.x;
            this.lastTargetY = point.y;
            ++this.fallbackCount;
            try {
                MousePackets.queueClickPacket(point.x, point.y);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
        this.realMouseClick(point);
    }

    private void realMouseClick(java.awt.Point point) {
        if (point == null) {
            return;
        }
        this.lastActionAtMs = System.currentTimeMillis();
        ++this.humanizedCount;
        this.moveTo(point);
        this.dwellSettle();
        try {
            Mouse.click(point.x, point.y, true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void sleep(int n) {
        if (n <= 0) {
            return;
        }
        try {
            Time.sleep(n);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private java.awt.Point pointInside(Shape shape) {
        int n;
        int n2;
        Rectangle rectangle;
        if (shape == null) {
            return null;
        }
        try {
            rectangle = shape.getBounds();
        }
        catch (Throwable throwable) {
            return null;
        }
        if (rectangle == null || rectangle.width <= 0 || rectangle.height <= 0) {
            return null;
        }
        double d = rectangle.getCenterX() + (double)this.profile.aimBiasX();
        double d2 = rectangle.getCenterY() + (double)this.profile.aimBiasY();
        double d3 = Math.max(1.0, (double)rectangle.width / 4.0);
        double d4 = Math.max(1.0, (double)rectangle.height / 4.0);
        for (n2 = 0; n2 < 16; ++n2) {
            n = (int)Math.round(d + this.jitterRng.nextGaussian() * d3);
            int n3 = (int)Math.round(d2 + this.jitterRng.nextGaussian() * d4);
            if (n < rectangle.x || n >= rectangle.x + rectangle.width || n3 < rectangle.y || n3 >= rectangle.y + rectangle.height || !shape.contains(n, n3)) continue;
            return new java.awt.Point(n, n3);
        }
        n2 = (int)Math.max((long)rectangle.x, Math.min((long)(rectangle.x + rectangle.width - 1), Math.round(d)));
        return shape.contains(n2, n = (int)Math.max((long)rectangle.y, Math.min((long)(rectangle.y + rectangle.height - 1), Math.round(d2)))) ? new java.awt.Point(n2, n) : new java.awt.Point((int)rectangle.getCenterX(), (int)rectangle.getCenterY());
    }

    private java.awt.Point objectPoint(TileObject tileObject) {
        java.awt.Point point;
        if (tileObject == null) {
            return null;
        }
        try {
            point = this.pointInside(tileObject.getClickbox());
            if (point != null) {
                return point;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            point = this.pointInside(tileObject.getCanvasTilePoly());
            if (point != null) {
                return point;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            point = tileObject.getCanvasLocation();
            if (point != null) {
                return new java.awt.Point(point.getX(), point.getY());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private java.awt.Point npcPoint(NPC nPC) {
        java.awt.Point point;
        if (nPC == null) {
            return null;
        }
        try {
            point = this.pointInside(nPC.getConvexHull());
            if (point != null) {
                return point;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            point = this.pointInside(nPC.getCanvasTilePoly());
            if (point != null) {
                return point;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private java.awt.Point itemPoint(Item item) {
        if (item == null) {
            return null;
        }
        try {
            Point point = item.getClickPoint();
            if (point != null && point.getX() > 0 && point.getY() > 0) {
                return this.jittered(point.getX(), point.getY(), 6);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private java.awt.Point tileItemPoint(TileItem tileItem) {
        if (tileItem == null) {
            return null;
        }
        try {
            LocalPoint localPoint = tileItem.getLocalLocation();
            if (localPoint != null) {
                return this.pointInside(Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)localPoint));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private java.awt.Point widgetPoint(int n, int n2) {
        try {
            Widget widget = this.client.getWidget(n, n2);
            if (widget != null && !widget.isHidden()) {
                return this.pointInside(widget.getBounds());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private Widget containerItemWidget(WidgetInfo widgetInfo, Item item) {
        if (item == null) {
            return null;
        }
        try {
            Widget widget = this.client.getWidget(widgetInfo);
            if (widget == null) {
                return null;
            }
            Widget[] widgetArray = widget.getDynamicChildren();
            if (widgetArray != null) {
                for (Widget widget2 : widgetArray) {
                    if (widget2 == null || widget2.isHidden() || widget2.getItemId() != item.getId()) continue;
                    return widget2;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private java.awt.Point tilePoint(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return null;
        }
        try {
            java.awt.Point point;
            LocalPoint localPoint = LocalPoint.fromWorld((Client)this.client, (WorldPoint)worldPoint);
            if (localPoint != null && (point = this.pointInside(Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)localPoint))) != null && this.onCanvas(point.x, point.y)) {
                return point;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private java.awt.Point minimapPoint(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return null;
        }
        try {
            LocalPoint localPoint = LocalPoint.fromWorld((Client)this.client, (WorldPoint)worldPoint);
            if (localPoint == null) {
                return null;
            }
            java.awt.Point point = this.minimapViaWidget(localPoint);
            if (point != null) {
                return point;
            }
            Point point2 = CoordUtils.localToMinimap(this.client, localPoint, 6400);
            if (point2 != null && this.onCanvas(point2.getX(), point2.getY())) {
                return new java.awt.Point(point2.getX(), point2.getY());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private java.awt.Point minimapViaWidget(LocalPoint localPoint) {
        int n;
        LocalPoint localPoint2;
        Widget widget = this.client.getWidget(160, 0);
        if (widget == null || widget.isHidden()) {
            return null;
        }
        Point point = widget.getCanvasLocation();
        if (point == null) {
            return null;
        }
        Player player = this.client.getLocalPlayer();
        LocalPoint localPoint3 = localPoint2 = player == null ? null : player.getLocalLocation();
        if (localPoint2 == null) {
            return null;
        }
        int n2 = localPoint.getX() / 32 - localPoint2.getX() / 32;
        int n3 = localPoint.getY() / 32 - localPoint2.getY() / 32;
        int n4 = this.client.getCameraYawTarget() & 0x7FF;
        int n5 = Perspective.SINE[n4];
        int n6 = Perspective.COSINE[n4];
        int n7 = n3 * n5 + n6 * n2 >> 16;
        int n8 = n5 * n2 - n3 * n6 >> 16;
        int n9 = point.getX() + widget.getWidth() / 2 + n7;
        return this.onCanvas(n9, n = point.getY() + widget.getHeight() / 2 + n8) ? new java.awt.Point(n9, n) : null;
    }

    private boolean onCanvas(int n, int n2) {
        try {
            Canvas canvas = this.client.getCanvas();
            if (canvas == null) {
                return false;
            }
            int n3 = canvas.getWidth();
            int n4 = canvas.getHeight();
            return n3 > 0 && n4 > 0 && n >= 0 && n2 >= 0 && n < n3 && n2 < n4;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private java.awt.Point jittered(int n, int n2, int n3) {
        return new java.awt.Point(n + this.gaussOffset(n3), n2 + this.gaussOffset(n3));
    }

    private int gaussOffset(int n) {
        if (n <= 0) {
            return 0;
        }
        long l = Math.round(this.jitterRng.nextGaussian() * ((double)n / 2.0));
        return (int)Math.max((long)(-n), Math.min((long)n, l));
    }

    private WorldPoint jitteredWaypoint(WorldPoint worldPoint, WorldPoint worldPoint2) {
        int n = 7 + this.pathRng.nextInt(Math.max(1, 9));
        int n2 = worldPoint2.getX() - worldPoint.getX();
        int n3 = worldPoint2.getY() - worldPoint.getY();
        int n4 = Math.max(Math.abs(n2), Math.abs(n3));
        if (n4 <= n) {
            int n5 = worldPoint2.getX() + (this.pathRng.nextInt(3) - 1);
            int n6 = worldPoint2.getY() + (this.pathRng.nextInt(3) - 1);
            return new WorldPoint(n5, n6, worldPoint.getPlane());
        }
        int n7 = worldPoint.getX() + Integer.signum(n2) * Math.min(Math.abs(n2), n);
        int n8 = worldPoint.getY() + Integer.signum(n3) * Math.min(Math.abs(n3), n);
        int n9 = this.pathRng.nextInt(5) - 2;
        if (Math.abs(n2) >= Math.abs(n3)) {
            n8 += n9;
        } else {
            n7 += n9;
        }
        return new WorldPoint(n7, n8, worldPoint.getPlane());
    }
}

