/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.api;

import net.unethicalite.api.input.naturalmouse.support.Flow;
import net.unethicalite.api.input.naturalmouse.util.Pair;

public interface SpeedManager {
    public Pair<Flow, Long> getFlowWithTime(double var1);
}

