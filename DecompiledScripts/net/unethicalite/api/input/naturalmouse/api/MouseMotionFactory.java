/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.api;

import java.util.Random;
import net.unethicalite.api.input.naturalmouse.api.DeviationProvider;
import net.unethicalite.api.input.naturalmouse.api.MouseInfoAccessor;
import net.unethicalite.api.input.naturalmouse.api.MouseMotion;
import net.unethicalite.api.input.naturalmouse.api.NoiseProvider;
import net.unethicalite.api.input.naturalmouse.api.OvershootManager;
import net.unethicalite.api.input.naturalmouse.api.SpeedManager;
import net.unethicalite.api.input.naturalmouse.api.SystemCalls;
import net.unethicalite.api.input.naturalmouse.support.DefaultMouseMotionNature;
import net.unethicalite.api.input.naturalmouse.support.MouseMotionNature;

public class MouseMotionFactory {
    private static final MouseMotionFactory defaultFactory = new MouseMotionFactory();
    private MouseMotionNature nature;
    private Random random = new Random();

    public MouseMotionFactory(MouseMotionNature nature) {
        this.nature = nature;
    }

    public MouseMotionFactory() {
        this(new DefaultMouseMotionNature());
    }

    public static MouseMotionFactory getDefault() {
        return defaultFactory;
    }

    public MouseMotion build(int xDest, int yDest) {
        return new MouseMotion(this.nature, this.random, xDest, yDest);
    }

    public void move(int xDest, int yDest) throws InterruptedException {
        this.build(xDest, yDest).move();
    }

    public SystemCalls getSystemCalls() {
        return this.nature.getSystemCalls();
    }

    public void setSystemCalls(SystemCalls systemCalls) {
        this.nature.setSystemCalls(systemCalls);
    }

    public DeviationProvider getDeviationProvider() {
        return this.nature.getDeviationProvider();
    }

    public void setDeviationProvider(DeviationProvider deviationProvider) {
        this.nature.setDeviationProvider(deviationProvider);
    }

    public NoiseProvider getNoiseProvider() {
        return this.nature.getNoiseProvider();
    }

    public void setNoiseProvider(NoiseProvider noiseProvider) {
        this.nature.setNoiseProvider(noiseProvider);
    }

    public Random getRandom() {
        return this.random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public MouseInfoAccessor getMouseInfo() {
        return this.nature.getMouseInfo();
    }

    public void setMouseInfo(MouseInfoAccessor mouseInfo) {
        this.nature.setMouseInfo(mouseInfo);
    }

    public SpeedManager getSpeedManager() {
        return this.nature.getSpeedManager();
    }

    public void setSpeedManager(SpeedManager speedManager) {
        this.nature.setSpeedManager(speedManager);
    }

    public MouseMotionNature getNature() {
        return this.nature;
    }

    public void setNature(MouseMotionNature nature) {
        this.nature = nature;
    }

    public OvershootManager getOvershootManager() {
        return this.nature.getOvershootManager();
    }

    public void setOvershootManager(OvershootManager manager) {
        this.nature.setOvershootManager(manager);
    }
}

