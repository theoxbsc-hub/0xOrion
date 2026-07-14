/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.plugins.Plugin
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.core;

import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OrionLoopedPlugin
extends Plugin
implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(OrionLoopedPlugin.class);
    private static final long TICK_MS = 600L;
    private volatile Thread loopThread;

    protected abstract int loop();

    protected long jitterDelay(long l) {
        return l;
    }

    protected void onStart() throws Exception {
    }

    protected void onStop() throws Exception {
    }

    public final boolean isRunning() {
        return this.loopThread != null;
    }

    protected final void startUp() throws Exception {
        this.stopLoop();
        this.onStart();
        Thread thread = new Thread((Runnable)this, this.getClass().getSimpleName() + "-loop");
        thread.setDaemon(true);
        this.loopThread = thread;
        thread.start();
    }

    protected final void shutDown() throws Exception {
        this.stopLoop();
        try {
            this.onStop();
        }
        catch (Throwable throwable) {
            log.warn("{} onStop() failed", (Object)this.getClass().getSimpleName(), (Object)throwable);
        }
    }

    private void stopLoop() {
        Thread thread = this.loopThread;
        this.loopThread = null;
        if (thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
        }
    }

    @Override
    public final void run() {
        Thread thread = Thread.currentThread();
        while (this.loopThread == thread) {
            int n;
            try {
                n = this.loop();
            }
            catch (Throwable throwable) {
                log.warn("{} loop() threw", (Object)this.getClass().getSimpleName(), (Object)throwable);
                n = 1000;
            }
            if (this.loopThread != thread) break;
            long l = n < 0 ? Math.max(1L, -((long)n)) * 600L : (long)n;
            if ((l = this.jitterDelay(l)) <= 0L) {
                l = 20L;
            }
            Time.sleep(l);
        }
    }
}

