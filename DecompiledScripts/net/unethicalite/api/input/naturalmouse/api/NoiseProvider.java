/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.api;

import java.util.Random;
import net.unethicalite.api.input.naturalmouse.support.DoublePoint;

public interface NoiseProvider {
    public DoublePoint getNoise(Random var1, double var2, double var4);
}

