/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

import net.unethicalite.api.input.naturalmouse.api.DeviationProvider;
import net.unethicalite.api.input.naturalmouse.support.DoublePoint;

public class SinusoidalDeviationProvider
implements DeviationProvider {
    public static final int DEFAULT_SLOPE_DIVIDER = 10;
    private final double slopeDivider;

    public SinusoidalDeviationProvider(double slopeDivider) {
        this.slopeDivider = slopeDivider;
    }

    @Override
    public DoublePoint getDeviation(double totalDistanceInPixels, double completionFraction) {
        double deviationFunctionResult = (1.0 - Math.cos(completionFraction * Math.PI * 2.0)) / 2.0;
        double deviationX = totalDistanceInPixels / this.slopeDivider;
        double deviationY = totalDistanceInPixels / this.slopeDivider;
        return new DoublePoint(deviationFunctionResult * deviationX, deviationFunctionResult * deviationY);
    }
}

