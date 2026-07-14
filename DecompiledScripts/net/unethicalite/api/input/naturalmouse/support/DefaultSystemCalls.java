/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.input.naturalmouse.api.SystemCalls;

public class DefaultSystemCalls
implements SystemCalls {
    private final Robot robot;

    public DefaultSystemCalls(Robot robot) {
        this.robot = robot;
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public void sleep(long time) {
        Time.sleep(time);
    }

    @Override
    public Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    @Override
    public void setMousePosition(int x, int y) {
        this.robot.mouseMove(x, y);
    }
}

