/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Singleton
 */
package net.unethicalite.client.managers;

import javax.inject.Singleton;

@Singleton
public class MinimalFpsManager
implements Runnable {
    private static final int SAMPLE_SIZE = 4;
    private final long[] lastDelays = new long[4];
    private long targetDelay = 0L;
    private long lastMillis = 0L;
    private int lastDelayIndex = 0;
    private long sleepDelay = 0L;

    public void reloadConfig(int fps) {
        this.lastMillis = System.currentTimeMillis();
        this.sleepDelay = this.targetDelay = (long)(1000 / Math.max(1, fps));
        for (int i = 0; i < 4; ++i) {
            this.lastDelays[i] = this.targetDelay;
        }
    }

    @Override
    public void run() {
        long now;
        long before = this.lastMillis;
        this.lastMillis = now = System.currentTimeMillis();
        this.lastDelayIndex = (this.lastDelayIndex + 1) % 4;
        this.lastDelays[this.lastDelayIndex] = now - before;
        long averageDelay = 0L;
        for (int i = 0; i < 4; ++i) {
            averageDelay += this.lastDelays[i];
        }
        if ((averageDelay /= (long)this.lastDelays.length) > this.targetDelay) {
            --this.sleepDelay;
        } else if (averageDelay < this.targetDelay) {
            ++this.sleepDelay;
        }
        if (this.sleepDelay > 0L) {
            try {
                Thread.sleep(this.sleepDelay);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
    }
}

