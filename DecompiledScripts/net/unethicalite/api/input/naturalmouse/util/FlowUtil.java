/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.util;

import java.util.Arrays;
import java.util.function.Function;

public class FlowUtil {
    public static double[] stretchFlow(double[] flow, int targetLength) {
        return FlowUtil.stretchFlow(flow, targetLength, a -> a);
    }

    public static double[] stretchFlow(double[] flow, int targetLength, Function<Double, Double> modifier) {
        int stepLength;
        if (targetLength < flow.length) {
            throw new IllegalArgumentException("Target bucket length smaller than flow. " + targetLength + " vs " + flow.length);
        }
        int tempLength = targetLength;
        if (flow.length != 1 && (tempLength - flow.length) % (flow.length - 1) != 0) {
            tempLength = (flow.length - 1) * (tempLength - flow.length) + 1;
        }
        double[] result = new double[tempLength];
        int insider = flow.length - 2;
        int countToNextStep = stepLength = (int)((double)(tempLength - 2) / (double)(insider + 1)) + 1;
        int fillValueIndex = 0;
        for (int i = 0; i < tempLength; ++i) {
            double fillValueBottom = flow[fillValueIndex];
            double fillValueTop = fillValueIndex + 1 < flow.length ? flow[fillValueIndex + 1] : flow[fillValueIndex];
            double completion = (double)(stepLength - countToNextStep) / (double)stepLength;
            result[i] = fillValueBottom * (1.0 - completion) + fillValueTop * completion;
            if (--countToNextStep != 0) continue;
            countToNextStep = stepLength;
            ++fillValueIndex;
        }
        if (tempLength != targetLength) {
            result = FlowUtil.reduceFlow(result, targetLength);
        }
        return Arrays.stream(result).map(modifier::apply).toArray();
    }

    public static double[] reduceFlow(double[] flow, int targetLength) {
        if (flow.length <= targetLength) {
            throw new IllegalArgumentException("Bad arguments [" + flow.length + ", " + targetLength + "]");
        }
        double multiplier = (double)targetLength / (double)flow.length;
        double[] result = new double[targetLength];
        for (int i = 0; i < flow.length; ++i) {
            double index = (double)i * multiplier;
            int indexInt = (int)index;
            double untilIndex = (double)(i + 1) * multiplier;
            int untilIndexInt = (int)untilIndex;
            if (indexInt != untilIndexInt) {
                double resultIndexPortion = 1.0 - (index - (double)indexInt);
                double nextResultIndexPortion = untilIndex - (double)untilIndexInt;
                int n = indexInt;
                result[n] = result[n] + flow[i] * resultIndexPortion;
                if (untilIndexInt >= result.length) continue;
                int n2 = untilIndexInt;
                result[n2] = result[n2] + flow[i] * nextResultIndexPortion;
                continue;
            }
            int n = indexInt;
            result[n] = result[n] + flow[i] * (untilIndex - index);
        }
        return result;
    }
}

