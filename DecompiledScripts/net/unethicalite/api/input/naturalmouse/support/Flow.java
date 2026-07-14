/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

public class Flow {
    private static final int AVERAGE_BUCKET_VALUE = 100;
    private final double[] buckets;

    public Flow(double[] characteristics) {
        this.buckets = this.normalizeBuckets(characteristics);
    }

    private double[] normalizeBuckets(double[] flowCharacteristics) {
        double[] buckets = new double[flowCharacteristics.length];
        long sum = 0L;
        for (int i = 0; i < flowCharacteristics.length; ++i) {
            if (flowCharacteristics[i] < 0.0) {
                throw new IllegalArgumentException("Invalid FlowCharacteristics at [" + i + "] : " + flowCharacteristics[i]);
            }
            sum = (long)((double)sum + flowCharacteristics[i]);
        }
        if (sum == 0L) {
            throw new IllegalArgumentException("Invalid FlowCharacteristics. All array elements can't be 0.");
        }
        double multiplier = 100.0 * (double)buckets.length / (double)sum;
        for (int i = 0; i < flowCharacteristics.length; ++i) {
            buckets[i] = flowCharacteristics[i] * multiplier;
        }
        return buckets;
    }

    public double[] getFlowCharacteristics() {
        return this.buckets;
    }

    public double getStepSize(double distance, int steps, double completion) {
        double completionStep = 1.0 / (double)steps;
        double bucketFrom = completion * (double)this.buckets.length;
        double bucketUntil = (completion + completionStep) * (double)this.buckets.length;
        double bucketContents = this.getBucketsContents(bucketFrom, bucketUntil);
        double distancePerBucketContent = distance / (double)(this.buckets.length * 100);
        return bucketContents * distancePerBucketContent;
    }

    private double getBucketsContents(double bucketFrom, double bucketUntil) {
        double sum = 0.0;
        int i = (int)bucketFrom;
        while ((double)i < bucketUntil) {
            double value = this.buckets[i];
            double endMultiplier = 1.0;
            double startMultiplier = 0.0;
            if (bucketUntil < (double)(i + 1)) {
                endMultiplier = bucketUntil - (double)((int)bucketUntil);
            }
            if ((int)bucketFrom == i) {
                startMultiplier = bucketFrom - (double)((int)bucketFrom);
            }
            sum += (value *= endMultiplier - startMultiplier);
            ++i;
        }
        return sum;
    }
}

