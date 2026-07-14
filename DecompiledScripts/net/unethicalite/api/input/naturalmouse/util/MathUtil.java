/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.util;

public class MathUtil {
    public static int roundTowards(double value, int target) {
        if ((double)target > value) {
            return (int)Math.ceil(value);
        }
        return (int)Math.floor(value);
    }
}

