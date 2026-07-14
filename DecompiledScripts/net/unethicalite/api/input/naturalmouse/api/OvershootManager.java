/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.api;

import java.awt.Point;
import net.unethicalite.api.input.naturalmouse.support.Flow;

public interface OvershootManager {
    public int getOvershoots(Flow var1, long var2, double var4);

    public Point getOvershootAmount(double var1, double var3, long var5, int var7);

    public long deriveNextMouseMovementTimeMs(long var1, int var3);
}

