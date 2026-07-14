/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.unethicalite.api.input.naturalmouse.api.SpeedManager;
import net.unethicalite.api.input.naturalmouse.support.Flow;
import net.unethicalite.api.input.naturalmouse.util.FlowTemplates;
import net.unethicalite.api.input.naturalmouse.util.Pair;

public class DefaultSpeedManager
implements SpeedManager {
    private static final double SMALL_DELTA = 1.0E-5;
    private final List<Flow> flows = new ArrayList<Flow>();
    private long mouseMovementTimeMs = 500L;

    public DefaultSpeedManager(Collection<Flow> flows) {
        this.flows.addAll(flows);
    }

    public DefaultSpeedManager() {
        this(Arrays.asList(new Flow(FlowTemplates.constantSpeed()), new Flow(FlowTemplates.variatingFlow()), new Flow(FlowTemplates.interruptedFlow()), new Flow(FlowTemplates.interruptedFlow2()), new Flow(FlowTemplates.slowStartupFlow()), new Flow(FlowTemplates.slowStartup2Flow()), new Flow(FlowTemplates.adjustingFlow()), new Flow(FlowTemplates.jaggedFlow()), new Flow(FlowTemplates.stoppingFlow())));
    }

    @Override
    public Pair<Flow, Long> getFlowWithTime(double distance) {
        double time = this.mouseMovementTimeMs + (long)(Math.random() * (double)this.mouseMovementTimeMs);
        Flow flow = this.flows.get((int)(Math.random() * (double)this.flows.size()));
        double timePerBucket = time / (double)flow.getFlowCharacteristics().length;
        for (double bucket : flow.getFlowCharacteristics()) {
            if (!(Math.abs(bucket - 0.0) < 1.0E-5)) continue;
            time += timePerBucket;
        }
        return new Pair<Flow, Long>(flow, (long)time);
    }

    public void setMouseMovementBaseTimeMs(long mouseMovementSpeedMs) {
        this.mouseMovementTimeMs = mouseMovementSpeedMs;
    }
}

