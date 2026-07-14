/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.GameState
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.commons;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import net.runelite.api.GameState;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Time {
    private static final Logger logger = LoggerFactory.getLogger(Time.class);
    private static final int DEFAULT_POLLING_RATE = 10;

    public static boolean sleep(long ms) {
        if (Static.getClient().isClientThread()) {
            logger.debug("Tried to sleep on client thread!");
            return false;
        }
        try {
            Thread.sleep(ms);
            return true;
        }
        catch (InterruptedException e) {
            logger.debug("Sleep interrupted");
            return false;
        }
    }

    public static boolean sleep(int min, int max) {
        return Time.sleep(Rand.nextInt(min, max));
    }

    public static boolean sleepUntil(BooleanSupplier supplier, BooleanSupplier resetSupplier, int pollingRate, int timeOut) {
        if (Static.getClient().isClientThread()) {
            logger.debug("Tried to sleepUntil on client thread!");
            return false;
        }
        long start = System.currentTimeMillis();
        while (!supplier.getAsBoolean()) {
            if (System.currentTimeMillis() > start + (long)timeOut) {
                return false;
            }
            if (resetSupplier.getAsBoolean()) {
                start = System.currentTimeMillis();
            }
            if (Time.sleep(pollingRate)) continue;
            return false;
        }
        return true;
    }

    public static boolean sleepUntil(BooleanSupplier supplier, BooleanSupplier resetSupplier, int timeOut) {
        return Time.sleepUntil(supplier, resetSupplier, 10, timeOut);
    }

    public static boolean sleepUntil(BooleanSupplier supplier, int pollingRate, int timeOut) {
        return Time.sleepUntil(supplier, () -> false, pollingRate, timeOut);
    }

    public static boolean sleepUntil(BooleanSupplier supplier, int timeOut) {
        return Time.sleepUntil(supplier, 10, timeOut);
    }

    public static boolean sleepTicks(int ticks) {
        if (Static.getClient().isClientThread()) {
            logger.debug("Tried to sleep on client thread!");
            return false;
        }
        if (Game.getState() == GameState.LOGIN_SCREEN || Game.getState() == GameState.LOGIN_SCREEN_AUTHENTICATOR) {
            return false;
        }
        for (int i = 0; i < ticks; ++i) {
            long start = Static.getClient().getTickCount();
            while ((long)Static.getClient().getTickCount() == start) {
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException e) {
                    logger.debug("Sleep interrupted");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean sleepTick() {
        return Time.sleepTicks(1);
    }

    public static boolean sleepTicksUntil(BooleanSupplier supplier, int ticks) {
        if (Static.getClient().isClientThread()) {
            logger.debug("Tried to sleep on client thread!");
            return false;
        }
        if (Game.getState() == GameState.LOGIN_SCREEN || Game.getState() == GameState.LOGIN_SCREEN_AUTHENTICATOR) {
            return false;
        }
        for (int i = 0; i < ticks; ++i) {
            if (supplier.getAsBoolean()) {
                return true;
            }
            if (Time.sleepTick()) continue;
            return false;
        }
        return false;
    }

    public static String format(Duration duration) {
        long secs = Math.abs(duration.getSeconds());
        return String.format("%02d:%02d:%02d", secs / 3600L, secs % 3600L / 60L, secs % 60L);
    }
}

