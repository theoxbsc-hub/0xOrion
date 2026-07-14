/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.input.naturalmouse.api;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Random;
import net.unethicalite.api.input.naturalmouse.api.DeviationProvider;
import net.unethicalite.api.input.naturalmouse.api.MouseInfoAccessor;
import net.unethicalite.api.input.naturalmouse.api.MouseMotionObserver;
import net.unethicalite.api.input.naturalmouse.api.NoiseProvider;
import net.unethicalite.api.input.naturalmouse.api.OvershootManager;
import net.unethicalite.api.input.naturalmouse.api.SpeedManager;
import net.unethicalite.api.input.naturalmouse.api.SystemCalls;
import net.unethicalite.api.input.naturalmouse.support.DoublePoint;
import net.unethicalite.api.input.naturalmouse.support.Flow;
import net.unethicalite.api.input.naturalmouse.support.MouseMotionNature;
import net.unethicalite.api.input.naturalmouse.support.mousemotion.MouseMovement;
import net.unethicalite.api.input.naturalmouse.support.mousemotion.MovementFactory;
import net.unethicalite.api.input.naturalmouse.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MouseMotion {
    private static final Logger log = LoggerFactory.getLogger(MouseMotion.class);
    private static final int SLEEP_AFTER_ADJUSTMENT_MS = 2;
    private final int minSteps;
    private final int effectFadeSteps;
    private final int reactionTimeBaseMs;
    private final int reactionTimeVariationMs;
    private final double timeToStepsDivider;
    private final Dimension screenSize;
    private final SystemCalls systemCalls;
    private final DeviationProvider deviationProvider;
    private final NoiseProvider noiseProvider;
    private final SpeedManager speedManager;
    private final OvershootManager overshootManager;
    private final int xDest;
    private final int yDest;
    private final Random random;
    private final MouseInfoAccessor mouseInfo;
    private Point mousePosition;

    public MouseMotion(MouseMotionNature nature, Random random, int xDest, int yDest) {
        this.deviationProvider = nature.getDeviationProvider();
        this.noiseProvider = nature.getNoiseProvider();
        this.systemCalls = nature.getSystemCalls();
        this.screenSize = this.systemCalls.getScreenSize();
        this.xDest = this.limitByScreenWidth(xDest);
        this.yDest = this.limitByScreenHeight(yDest);
        this.random = random;
        this.mouseInfo = nature.getMouseInfo();
        this.speedManager = nature.getSpeedManager();
        this.timeToStepsDivider = nature.getTimeToStepsDivider();
        this.minSteps = nature.getMinSteps();
        this.effectFadeSteps = nature.getEffectFadeSteps();
        this.reactionTimeBaseMs = nature.getReactionTimeBaseMs();
        this.reactionTimeVariationMs = nature.getReactionTimeVariationMs();
        this.overshootManager = nature.getOvershootManager();
    }

    public void move() throws InterruptedException {
        this.move((x, y) -> {});
    }

    public void move(MouseMotionObserver observer) throws InterruptedException {
        this.updateMouseInfo();
        MovementFactory movementFactory = new MovementFactory(this.xDest, this.yDest, this.speedManager, this.overshootManager, this.screenSize);
        ArrayDeque<MouseMovement> movements = movementFactory.createMovements(this.mousePosition);
        int overshoots = movements.size() - 1;
        while (this.mousePosition.x != this.xDest || this.mousePosition.y != this.yDest) {
            if (movements.isEmpty()) {
                this.updateMouseInfo();
                movements = movementFactory.createMovements(this.mousePosition);
            }
            MouseMovement movement = movements.removeFirst();
            double distance = movement.distance;
            long mouseMovementMs = movement.time;
            Flow flow = movement.flow;
            double xDistance = movement.xDistance;
            double yDistance = movement.yDistance;
            int steps = (int)Math.ceil(Math.min(distance, Math.max((double)mouseMovementMs / this.timeToStepsDivider, (double)this.minSteps)));
            long startTime = this.systemCalls.currentTimeMillis();
            long stepTime = (long)((double)mouseMovementMs / (double)steps);
            this.updateMouseInfo();
            double simulatedMouseX = this.mousePosition.x;
            double simulatedMouseY = this.mousePosition.y;
            double deviationMultiplierX = (this.random.nextDouble() - 0.5) * 2.0;
            double deviationMultiplierY = (this.random.nextDouble() - 0.5) * 2.0;
            double completedXDistance = 0.0;
            double completedYDistance = 0.0;
            double noiseX = 0.0;
            double noiseY = 0.0;
            for (int i = 0; i < steps; ++i) {
                double timeCompletion = (double)i / (double)steps;
                double effectFadeStep = Math.max(i - (steps - this.effectFadeSteps) + 1, 0);
                double effectFadeMultiplier = ((double)this.effectFadeSteps - effectFadeStep) / (double)this.effectFadeSteps;
                double xStepSize = flow.getStepSize(xDistance, steps, timeCompletion);
                double yStepSize = flow.getStepSize(yDistance, steps, timeCompletion);
                double completedDistance = Math.hypot(completedXDistance += xStepSize, completedYDistance += yStepSize);
                double completion = Math.min(1.0, completedDistance / distance);
                DoublePoint noise = this.noiseProvider.getNoise(this.random, xStepSize, yStepSize);
                DoublePoint deviation = this.deviationProvider.getDeviation(distance, completion);
                long endTime = startTime + stepTime * (long)(i + 1);
                int mousePosX = MathUtil.roundTowards((simulatedMouseX += xStepSize) + deviation.getX() * deviationMultiplierX * effectFadeMultiplier + (noiseX += noise.getX()) * effectFadeMultiplier, movement.destX);
                int mousePosY = MathUtil.roundTowards((simulatedMouseY += yStepSize) + deviation.getY() * deviationMultiplierY * effectFadeMultiplier + (noiseY += noise.getY()) * effectFadeMultiplier, movement.destY);
                mousePosX = this.limitByScreenWidth(mousePosX);
                mousePosY = this.limitByScreenHeight(mousePosY);
                this.systemCalls.setMousePosition(mousePosX, mousePosY);
                observer.observe(mousePosX, mousePosY);
                long timeLeft = endTime - this.systemCalls.currentTimeMillis();
                this.sleepAround(Math.max(timeLeft, 0L), 0L);
            }
            this.updateMouseInfo();
            if (this.mousePosition.x != movement.destX || this.mousePosition.y != movement.destY) {
                this.systemCalls.setMousePosition(movement.destX, movement.destY);
                this.sleepAround(2L, 0L);
                this.updateMouseInfo();
            }
            if (this.mousePosition.x == this.xDest && this.mousePosition.y == this.yDest) continue;
            this.sleepAround(this.reactionTimeBaseMs, this.reactionTimeVariationMs);
        }
    }

    private int limitByScreenWidth(int value) {
        return Math.max(0, Math.min(this.screenSize.width - 1, value));
    }

    private int limitByScreenHeight(int value) {
        return Math.max(0, Math.min(this.screenSize.height - 1, value));
    }

    private void sleepAround(long sleepMin, long randomPart) {
        long sleepTime = (long)((double)sleepMin + this.random.nextDouble() * (double)randomPart);
        if (log.isTraceEnabled() && sleepTime > 0L) {
            this.updateMouseInfo();
        }
        this.systemCalls.sleep(sleepTime);
    }

    private void updateMouseInfo() {
        this.mousePosition = this.mouseInfo.getMousePosition();
    }
}

