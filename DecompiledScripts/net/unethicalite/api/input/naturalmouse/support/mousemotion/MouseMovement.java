/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support.mousemotion;

import net.unethicalite.api.input.naturalmouse.support.Flow;

public class MouseMovement {
    public final int destX;
    public final int destY;
    public final double distance;
    public final int xDistance;
    public final int yDistance;
    public final long time;
    public final Flow flow;

    public MouseMovement(int destX, int destY, double distance, int xDistance, int yDistance, long time, Flow flow) {
        this.destX = destX;
        this.destY = destY;
        this.distance = distance;
        this.xDistance = xDistance;
        this.yDistance = yDistance;
        this.time = time;
        this.flow = flow;
    }

    public String toString() {
        return "Movement{destX=" + this.destX + ", destY=" + this.destY + ", xDistance=" + this.xDistance + ", yDistance=" + this.yDistance + ", time=" + this.time + "}";
    }
}

