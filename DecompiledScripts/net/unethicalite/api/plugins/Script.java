/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.GameState
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.plugins;

import javax.inject.Inject;
import net.runelite.api.GameState;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.script.blocking_events.BlockingEvent;
import net.unethicalite.api.script.blocking_events.BlockingEventManager;
import net.unethicalite.api.script.paint.Paint;
import net.unethicalite.client.Static;
import net.unethicalite.client.minimal.plugins.MinimalPluginChanged;
import net.unethicalite.client.minimal.plugins.MinimalPluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Script
extends LoopedPlugin {
    protected final Logger logger;
    private boolean restart;
    private boolean paused;
    private boolean onLogin;
    @Inject
    private Paint paint;
    private final BlockingEventManager blockingEventManager = new BlockingEventManager();

    public Script() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    protected abstract int loop();

    public abstract void onStart(String ... var1);

    public void onStop() {
    }

    public void onLogin() {
    }

    public int outerLoop() {
        int loopSleep;
        if (this.paused) {
            return 1000;
        }
        if (this.restart) {
            this.restart = false;
            Static.getClient().getCallbacks().post((Object)new MinimalPluginChanged(this, MinimalPluginState.RESTARTING));
            return 1000;
        }
        if (Game.getState() == GameState.LOGGED_IN && !this.onLogin) {
            this.onLogin = true;
            this.onLogin();
            return 100;
        }
        if (!this.blockingEventManager.getBlockingEvents().isEmpty()) {
            for (BlockingEvent event : this.blockingEventManager.getBlockingEvents()) {
                if (!event.validate()) continue;
                return event.loop();
            }
        }
        return (loopSleep = this.loop()) != 0 ? loopSleep : 1000;
    }

    public void pauseScript() {
        boolean bl = this.paused = !this.paused;
        if (!this.paused) {
            Static.getClient().getCallbacks().post((Object)new MinimalPluginChanged(this, MinimalPluginState.STARTED));
        } else {
            Static.getClient().getCallbacks().post((Object)new MinimalPluginChanged(this, MinimalPluginState.PAUSED));
        }
    }

    public boolean isRestart() {
        return this.restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Paint getPaint() {
        return this.paint;
    }

    public BlockingEventManager getBlockingEventManager() {
        return this.blockingEventManager;
    }
}

