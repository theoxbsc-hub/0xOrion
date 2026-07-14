package net.runelite.client.plugins.ox.core;

import net.runelite.client.plugins.Plugin;
import net.unethicalite.api.commons.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loop host for 0x scripts (same pattern as OrionLoopedPlugin, our package).
 */
public abstract class OxLoopedPlugin extends Plugin implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(OxLoopedPlugin.class);
    private volatile Thread loopThread;

    protected abstract int loop();

    protected long jitterDelay(long delayMs) {
        return delayMs;
    }

    protected void onStart() throws Exception {
    }

    protected void onStop() throws Exception {
    }

    public final boolean isRunning() {
        return this.loopThread != null;
    }

    @Override
    protected final void startUp() throws Exception {
        this.stopLoop();
        this.onStart();
        Thread thread = new Thread(this, this.getClass().getSimpleName() + "-loop");
        thread.setDaemon(true);
        this.loopThread = thread;
        thread.start();
    }

    @Override
    protected final void shutDown() throws Exception {
        this.stopLoop();
        try {
            this.onStop();
        } catch (Throwable t) {
            log.warn("{} onStop() failed", this.getClass().getSimpleName(), t);
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
        Thread self = Thread.currentThread();
        while (this.loopThread == self) {
            int sleepMs;
            try {
                sleepMs = this.loop();
            } catch (Throwable t) {
                log.warn("{} loop() threw", this.getClass().getSimpleName(), t);
                sleepMs = 1000;
            }
            if (this.loopThread != self) {
                break;
            }
            long delay = sleepMs < 0
                ? Math.max(1L, -((long) sleepMs)) * 600L
                : (long) sleepMs;
            delay = this.jitterDelay(delay);
            if (delay <= 0L) {
                delay = 20L;
            }
            Time.sleep(delay);
        }
    }
}
