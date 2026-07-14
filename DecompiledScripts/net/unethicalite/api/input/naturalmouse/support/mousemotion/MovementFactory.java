/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.input.naturalmouse.support.mousemotion;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Iterator;
import net.unethicalite.api.input.naturalmouse.api.OvershootManager;
import net.unethicalite.api.input.naturalmouse.api.SpeedManager;
import net.unethicalite.api.input.naturalmouse.support.Flow;
import net.unethicalite.api.input.naturalmouse.support.mousemotion.MouseMovement;
import net.unethicalite.api.input.naturalmouse.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementFactory {
    private static final Logger log = LoggerFactory.getLogger(MovementFactory.class);
    private final int xDest;
    private final int yDest;
    private final SpeedManager speedManager;
    private final OvershootManager overshootManager;
    private final Dimension screenSize;

    public MovementFactory(int xDest, int yDest, SpeedManager speedManager, OvershootManager overshootManager, Dimension screenSize) {
        this.xDest = xDest;
        this.yDest = yDest;
        this.speedManager = speedManager;
        this.overshootManager = overshootManager;
        this.screenSize = screenSize;
    }

    public ArrayDeque<MouseMovement> createMovements(Point currentMousePosition) {
        ArrayDeque<MouseMovement> movements = new ArrayDeque<MouseMovement>();
        int lastMousePositionX = currentMousePosition.x;
        int lastMousePositionY = currentMousePosition.y;
        int xDistance = this.xDest - lastMousePositionX;
        int yDistance = this.yDest - lastMousePositionY;
        double initialDistance = Math.hypot(xDistance, yDistance);
        Pair<Flow, Long> flowTime = this.speedManager.getFlowWithTime(initialDistance);
        Flow flow = (Flow)flowTime.x;
        long mouseMovementMs = (Long)flowTime.y;
        int overshoots = this.overshootManager.getOvershoots(flow, mouseMovementMs, initialDistance);
        if (overshoots == 0) {
            movements.add(new MouseMovement(this.xDest, this.yDest, initialDistance, xDistance, yDistance, mouseMovementMs, flow));
            return movements;
        }
        for (int i = overshoots; i > 0; --i) {
            Point overshoot = this.overshootManager.getOvershootAmount(this.xDest - lastMousePositionX, this.yDest - lastMousePositionY, mouseMovementMs, i);
            int currentDestinationX = this.limitByScreenWidth(this.xDest + overshoot.x);
            int currentDestinationY = this.limitByScreenHeight(this.yDest + overshoot.y);
            xDistance = currentDestinationX - lastMousePositionX;
            yDistance = currentDestinationY - lastMousePositionY;
            double distance = Math.hypot(xDistance, yDistance);
            flow = (Flow)this.speedManager.getFlowWithTime((double)distance).x;
            movements.add(new MouseMovement(currentDestinationX, currentDestinationY, distance, xDistance, yDistance, mouseMovementMs, flow));
            lastMousePositionX = currentDestinationX;
            lastMousePositionY = currentDestinationY;
            mouseMovementMs = this.overshootManager.deriveNextMouseMovementTimeMs(mouseMovementMs, i - 1);
        }
        Iterator it = movements.descendingIterator();
        boolean remove = true;
        while (it.hasNext() && remove) {
            MouseMovement movement = (MouseMovement)it.next();
            if (movement.destX == this.xDest && movement.destY == this.yDest) {
                lastMousePositionX = movement.destX - movement.xDistance;
                lastMousePositionY = movement.destY - movement.yDistance;
                it.remove();
                continue;
            }
            remove = false;
        }
        xDistance = this.xDest - lastMousePositionX;
        yDistance = this.yDest - lastMousePositionY;
        double distance = Math.hypot(xDistance, yDistance);
        Pair<Flow, Long> movementToTargetFlowTime = this.speedManager.getFlowWithTime(distance);
        long finalMovementTime = this.overshootManager.deriveNextMouseMovementTimeMs((Long)movementToTargetFlowTime.y, 0);
        MouseMovement finalMove = new MouseMovement(this.xDest, this.yDest, distance, xDistance, yDistance, finalMovementTime, (Flow)movementToTargetFlowTime.x);
        movements.add(finalMove);
        return movements;
    }

    private int limitByScreenWidth(int value) {
        return Math.max(0, Math.min(this.screenSize.width - 1, value));
    }

    private int limitByScreenHeight(int value) {
        return Math.max(0, Math.min(this.screenSize.height - 1, value));
    }
}

