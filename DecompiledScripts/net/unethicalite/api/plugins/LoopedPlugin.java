/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.Plugin
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.plugins;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.plugins.PluginStoppedException;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.plugins.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoopedPlugin
extends Plugin
implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LoopedPlugin.class);
    private static final ScheduledExecutorService EXECUTOR = new LoggableExecutor(1);
    private final AtomicInteger ticks = new AtomicInteger(0);
    private volatile int nextSleep = 1000;
    private int currentSleep = 1000;
    private int sleepUntil = 0;
    private Future<?> task;

    protected abstract int loop();

    @Override
    public void run() {
        this.task = EXECUTOR.schedule(this.loopTask(), (long)this.nextSleep, TimeUnit.MILLISECONDS);
        while (this.isRunning()) {
            if (this.task.isDone()) {
                this.task = EXECUTOR.schedule(this.loopTask(), (long)this.nextSleep, TimeUnit.MILLISECONDS);
                continue;
            }
            Time.sleep(10L);
        }
        this.task = null;
    }

    private Runnable loopTask() {
        return () -> {
            int currentTick;
            block13: {
                currentTick = this.ticks.get();
                if (this.sleepUntil <= 0 || this.sleepUntil <= currentTick || !Game.isLoggedIn()) break block13;
                if (this.sleepUntil != 0) return;
                if (this.currentSleep < 0 && Game.isLoggedIn()) {
                    int currentTick2 = this.ticks.get();
                    this.sleepUntil = currentTick2 + Math.abs(this.currentSleep);
                    this.nextSleep = 0;
                    return;
                } else {
                    this.nextSleep = this.currentSleep < 0 ? 1000 : this.currentSleep;
                }
                return;
            }
            try {
                this.sleepUntil = 0;
                int n = this.currentSleep = this instanceof Script ? ((Script)this).outerLoop() : this.loop();
                if (this.sleepUntil != 0) return;
            }
            catch (PluginStoppedException e) {
                try {
                    SwingUtilities.invokeLater(() -> Plugins.stopPlugin(this));
                    if (this.sleepUntil != 0) return;
                }
                catch (Throwable throwable) {
                    if (this.sleepUntil != 0) throw throwable;
                    if (this.currentSleep < 0 && Game.isLoggedIn()) {
                        int currentTick3 = this.ticks.get();
                        this.sleepUntil = currentTick3 + Math.abs(this.currentSleep);
                        this.nextSleep = 0;
                        throw throwable;
                    } else {
                        this.nextSleep = this.currentSleep < 0 ? 1000 : this.currentSleep;
                    }
                    throw throwable;
                }
                if (this.currentSleep < 0 && Game.isLoggedIn()) {
                    int currentTick4 = this.ticks.get();
                    this.sleepUntil = currentTick4 + Math.abs(this.currentSleep);
                    this.nextSleep = 0;
                    return;
                } else {
                    this.nextSleep = this.currentSleep < 0 ? 1000 : this.currentSleep;
                }
                return;
            }
            if (this.currentSleep < 0 && Game.isLoggedIn()) {
                currentTick = this.ticks.get();
                this.sleepUntil = currentTick + Math.abs(this.currentSleep);
                this.nextSleep = 0;
                return;
            } else {
                this.nextSleep = this.currentSleep < 0 ? 1000 : this.currentSleep;
            }
            return;
        };
    }

    public boolean isRunning() {
        return this.task != null && !this.task.isCancelled();
    }

    public void stop() {
        if (this.isRunning()) {
            this.task.cancel(true);
        }
    }

    @Subscribe
    private void tickCounter(GameTick gameTick) {
        this.ticks.incrementAndGet();
    }

    private static class LoggableExecutor
    extends ScheduledThreadPoolExecutor {
        public LoggableExecutor(int corePoolSize) {
            super(corePoolSize);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t == null && r instanceof Future) {
                try {
                    Future future = (Future)((Object)r);
                    if (future.isDone()) {
                        future.get();
                    }
                }
                catch (CancellationException future) {
                }
                catch (ExecutionException ee) {
                    t = ee.getCause();
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            if (t != null) {
                log.error("Error in loop", t);
            }
        }
    }
}

