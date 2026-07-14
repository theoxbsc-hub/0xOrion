/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.MouseRecorder
 *  net.runelite.api.Point
 */
package net.runelite.client.plugins.svc.incognito;

import java.awt.Canvas;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import net.runelite.api.Client;
import net.runelite.api.MouseRecorder;
import net.runelite.api.Point;
import net.runelite.client.plugins.svc.incognito.IncognitoConfig;
import net.unethicalite.api.input.Mouse;

final class PresenceGuard {
    private final Client client;
    private final IncognitoConfig config;
    private final Random rng = new Random();
    private long lastFidgetMs;
    private long lastCamMs;
    private final AtomicBoolean fidgetInFlight = new AtomicBoolean(false);
    private volatile boolean lastFocused;
    private volatile int lastMouseIdle = -1;
    private volatile int lastKbIdle = -1;
    private volatile int recorderSpread = -1;
    private volatile long corrections;
    private volatile long fidgets;

    PresenceGuard(Client client, IncognitoConfig incognitoConfig) {
        this.client = client;
        this.config = incognitoConfig;
    }

    void tick() {
        try {
            this.lastFocused = this.client.isFocused();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.lastMouseIdle = this.client.getMouseIdleTicks();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.lastKbIdle = this.client.getKeyboardIdleTicks();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.recorderSpread = this.computeRecorderSpread();
        int n = Math.max(20, this.config.idleCeilingTicks());
        long l = System.currentTimeMillis();
        if (this.config.keepFocused()) {
            try {
                if (!this.lastFocused) {
                    this.client.setFocused(true);
                    ++this.corrections;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        boolean bl = false;
        if (this.config.fidget() && this.lastMouseIdle > n && l - this.lastFidgetMs >= this.jitter((long)this.config.fidgetSeconds() * 1000L, 0.4) && (bl = this.startFidget())) {
            this.lastFidgetMs = l;
        }
        if (this.config.clampIdle()) {
            if (!this.config.fidget() && this.lastMouseIdle > n) {
                try {
                    this.client.setMouseIdleTicks(1 + this.rng.nextInt(14));
                    ++this.corrections;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            if (this.lastKbIdle > n) {
                try {
                    this.client.setKeyboardIdleTicks(1 + this.rng.nextInt(14));
                    ++this.corrections;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        if (this.config.normalizeLastClick()) {
            this.normalizeLastClick(l);
        }
        if (this.config.moveCamera() && this.lastMouseIdle > n && l - this.lastCamMs >= this.jitter(20000L, 0.5)) {
            this.lastCamMs = l;
            this.nudgeCamera();
        }
    }

    private boolean startFidget() {
        Canvas canvas;
        if (!this.fidgetInFlight.compareAndSet(false, true)) {
            return false;
        }
        try {
            canvas = this.client.getCanvas();
        }
        catch (Throwable throwable) {
            this.fidgetInFlight.set(false);
            return false;
        }
        if (canvas == null) {
            this.fidgetInFlight.set(false);
            return false;
        }
        Thread thread = new Thread(() -> {
            try {
                this.easedMove(canvas);
                ++this.fidgets;
            }
            catch (Throwable throwable) {
            }
            finally {
                this.fidgetInFlight.set(false);
            }
        }, "incognito-fidget");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    private void easedMove(Canvas canvas) {
        int n = canvas.getWidth();
        int n2 = canvas.getHeight();
        if (n <= 8 || n2 <= 8) {
            return;
        }
        Point point = null;
        try {
            point = this.client.getMouseCanvasPosition();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        double d = point != null && point.getX() > 0 && point.getX() < n ? (double)point.getX() : (double)n / 2.0;
        double d2 = point != null && point.getY() > 0 && point.getY() < n2 ? (double)point.getY() : (double)n2 / 2.0;
        int n3 = PresenceGuard.clamp((int)d + (this.rng.nextInt(221) - 110), 4, n - 4);
        int n4 = PresenceGuard.clamp((int)d2 + (this.rng.nextInt(221) - 110), 4, n2 - 4);
        double d3 = (double)n3 - d;
        double d4 = (double)n4 - d2;
        double d5 = Math.hypot(d3, d4);
        int n5 = (int)Math.max(4.0, Math.min(40.0, d5 / (6.0 + this.rng.nextDouble() * 6.0)));
        double d6 = (this.rng.nextDouble() * 2.0 - 1.0) * Math.min(40.0, d5 * 0.15);
        double d7 = d5 < 1.0 ? 0.0 : -d4 / d5;
        double d8 = d5 < 1.0 ? 0.0 : d3 / d5;
        for (int i = 1; i <= n5; ++i) {
            double d9 = (double)i / (double)n5;
            double d10 = d9 < 0.5 ? 2.0 * d9 * d9 : 1.0 - Math.pow(-2.0 * d9 + 2.0, 2.0) / 2.0;
            double d11 = Math.sin(Math.PI * d9) * d6;
            int n6 = PresenceGuard.clamp((int)Math.round(d + d3 * d10 + d7 * d11 + (this.rng.nextDouble() * 2.0 - 1.0) * 1.2), 1, n - 1);
            int n7 = PresenceGuard.clamp((int)Math.round(d2 + d4 * d10 + d8 * d11 + (this.rng.nextDouble() * 2.0 - 1.0) * 1.2), 1, n2 - 1);
            try {
                Mouse.moved(n6, n7, canvas, System.currentTimeMillis());
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            PresenceGuard.sleep(12 + this.rng.nextInt(26));
        }
        try {
            Mouse.moved(n3, n4, canvas, System.currentTimeMillis());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void normalizeLastClick(long l) {
        long l2;
        try {
            l2 = this.client.getMouseLastPressedMillis();
        }
        catch (Throwable throwable) {
            return;
        }
        if (l2 > 0L && l - l2 < 90000L) {
            return;
        }
        long l3 = l - (2000L + (long)this.rng.nextInt(40000));
        int n = -1;
        int n2 = -1;
        try {
            Point point = this.client.getMouseCanvasPosition();
            if (point != null) {
                n = point.getX();
                n2 = point.getY();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.client.setMouseLastPressedMillis(l3);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.client.setClientMouseLastPressedMillis(l3);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (n >= 0) {
            try {
                this.client.setMouseLastPressedX(n);
                this.client.setMouseLastPressedY(n2);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        ++this.corrections;
    }

    private void nudgeCamera() {
        try {
            int n = this.client.getCameraYawTarget();
            int n2 = (this.rng.nextInt(2) == 0 ? -1 : 1) * (60 + this.rng.nextInt(220));
            this.client.setCameraYawTarget(((n + n2) % 2048 + 2048) % 2048);
            if (this.rng.nextInt(3) == 0) {
                int n3 = PresenceGuard.clamp(this.client.getCameraPitchTarget() + (this.rng.nextInt(61) - 30), 128, 383);
                this.client.setCameraPitchTarget(n3);
            }
            ++this.corrections;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private int computeRecorderSpread() {
        try {
            MouseRecorder mouseRecorder = this.client.getMouseRecorder();
            if (mouseRecorder == null) {
                return -1;
            }
            int[] nArray = mouseRecorder.getXs();
            int[] nArray2 = mouseRecorder.getYs();
            if (nArray == null || nArray2 == null || nArray.length == 0) {
                return -1;
            }
            int n = Integer.MAX_VALUE;
            int n2 = Integer.MIN_VALUE;
            int n3 = Integer.MAX_VALUE;
            int n4 = Integer.MIN_VALUE;
            int n5 = Math.min(nArray.length, nArray2.length);
            for (int i = 0; i < n5; ++i) {
                if (nArray[i] == 0 && nArray2[i] == 0) continue;
                if (nArray[i] < n) {
                    n = nArray[i];
                }
                if (nArray[i] > n2) {
                    n2 = nArray[i];
                }
                if (nArray2[i] < n3) {
                    n3 = nArray2[i];
                }
                if (nArray2[i] <= n4) continue;
                n4 = nArray2[i];
            }
            if (n2 < n) {
                return 0;
            }
            return Math.max(n2 - n, n4 - n3);
        }
        catch (Throwable throwable) {
            return -1;
        }
    }

    boolean lastFocused() {
        return this.lastFocused;
    }

    int lastMouseIdle() {
        return this.lastMouseIdle;
    }

    int lastKbIdle() {
        return this.lastKbIdle;
    }

    int recorderSpread() {
        return this.recorderSpread;
    }

    long corrections() {
        return this.corrections;
    }

    long fidgets() {
        return this.fidgets;
    }

    private long jitter(long l, double d) {
        double d2 = 1.0 + (this.rng.nextDouble() * 2.0 - 1.0) * d;
        return Math.max(1L, Math.round((double)l * d2));
    }

    private static int clamp(int n, int n2, int n3) {
        return n < n2 ? n2 : (n > n3 ? n3 : n);
    }

    private static void sleep(int n) {
        if (n <= 0) {
            return;
        }
        try {
            Thread.sleep(n);
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}

