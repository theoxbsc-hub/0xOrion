/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.tools;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import net.unethicalite.api.input.naturalmouse.api.MouseInfoAccessor;
import net.unethicalite.api.input.naturalmouse.api.SystemCalls;
import net.unethicalite.api.input.naturalmouse.support.DefaultMouseInfoAccessor;
import net.unethicalite.api.input.naturalmouse.support.DefaultSystemCalls;

public class SystemDiagnosis {
    public static void validateMouseMovement() {
        try {
            Robot robot = new Robot();
            SystemDiagnosis.validateMouseMovement(new DefaultSystemCalls(robot), new DefaultMouseInfoAccessor());
        }
        catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static void validateMouseMovement(SystemCalls system, MouseInfoAccessor accessor) {
        Dimension dimension = system.getScreenSize();
        for (int y = 0; y < dimension.height; y += 50) {
            for (int x = 0; x < dimension.width; x += 50) {
                system.setMousePosition(x, y);
                try {
                    Thread.sleep(1L);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Point p = accessor.getMousePosition();
                if (x == p.x && y == p.y) continue;
                throw new IllegalStateException("Tried to move mouse to (" + x + ", " + y + "). Actually moved to (" + p.x + ", " + p.y + ")This means NaturalMouseMotion is not able to work optimally on this system as the cursor move calls may miss the target pixels on the screen.");
            }
        }
    }
}

