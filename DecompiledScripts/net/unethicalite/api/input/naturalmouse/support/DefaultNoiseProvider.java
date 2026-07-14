/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

import java.util.Random;
import net.unethicalite.api.input.naturalmouse.api.NoiseProvider;
import net.unethicalite.api.input.naturalmouse.support.DoublePoint;

public class DefaultNoiseProvider
implements NoiseProvider {
    public static final double DEFAULT_NOISINESS_DIVIDER = 2.0;
    private static final double SMALL_DELTA = 1.0E-5;
    private final double noisinessDivider;

    public DefaultNoiseProvider(double noisinessDivider) {
        this.noisinessDivider = noisinessDivider;
    }

    @Override
    public DoublePoint getNoise(Random random, double xStepSize, double yStepSize) {
        if (Math.abs(xStepSize - 0.0) < 1.0E-5 && Math.abs(yStepSize - 0.0) < 1.0E-5) {
            return DoublePoint.ZERO;
        }
        double noiseX = 0.0;
        double noiseY = 0.0;
        double stepSize = Math.hypot(xStepSize, yStepSize);
        double noisiness = Math.max(0.0, 8.0 - stepSize) / 50.0;
        if (random.nextDouble() < noisiness) {
            noiseX = (random.nextDouble() - 0.5) * Math.max(0.0, 8.0 - stepSize) / this.noisinessDivider;
            noiseY = (random.nextDouble() - 0.5) * Math.max(0.0, 8.0 - stepSize) / this.noisinessDivider;
        }
        return new DoublePoint(noiseX, noiseY);
    }
}

