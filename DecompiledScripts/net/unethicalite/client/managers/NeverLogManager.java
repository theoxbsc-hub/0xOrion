/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 */
package net.unethicalite.client.managers;

import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.client.config.UnethicaliteConfig;

@Singleton
public class NeverLogManager {
    @Inject
    private Client client;
    @Inject
    private UnethicaliteConfig config;
    private long randomDelay = 0L;

    @Inject
    NeverLogManager(EventBus eventBus) {
        eventBus.register((Object)this);
    }

    @Subscribe
    private void onGameTick(GameTick e) {
        if (this.config.neverLog() && this.checkIdle()) {
            this.randomDelay = this.randomDelay();
            Executors.newSingleThreadExecutor().submit(this::pressKey);
        }
    }

    private long randomDelay() {
        return (long)this.clamp(Math.round(ThreadLocalRandom.current().nextGaussian() * 8000.0));
    }

    private double clamp(double value) {
        return Math.max(1.0, Math.min(13000.0, value));
    }

    private void pressKey() {
        KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), 401, System.currentTimeMillis(), 1024, 8);
        this.client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), 402, System.currentTimeMillis(), 0, 8);
        this.client.getCanvas().dispatchEvent(keyRelease);
        KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), 400, System.currentTimeMillis(), 0, 8);
        this.client.getCanvas().dispatchEvent(keyTyped);
    }

    private boolean checkIdle() {
        int idleClientTicks = this.client.getKeyboardIdleTicks();
        if (this.client.getMouseIdleTicks() < idleClientTicks) {
            idleClientTicks = this.client.getMouseIdleTicks();
        }
        return (long)idleClientTicks >= this.randomDelay;
    }
}

