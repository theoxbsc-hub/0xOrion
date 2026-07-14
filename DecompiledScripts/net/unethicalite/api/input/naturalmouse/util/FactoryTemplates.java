/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.util;

import java.util.ArrayList;
import java.util.Arrays;
import net.unethicalite.api.input.naturalmouse.api.MouseMotionFactory;
import net.unethicalite.api.input.naturalmouse.api.SpeedManager;
import net.unethicalite.api.input.naturalmouse.support.DefaultMouseMotionNature;
import net.unethicalite.api.input.naturalmouse.support.DefaultNoiseProvider;
import net.unethicalite.api.input.naturalmouse.support.DefaultOvershootManager;
import net.unethicalite.api.input.naturalmouse.support.DefaultSpeedManager;
import net.unethicalite.api.input.naturalmouse.support.DoublePoint;
import net.unethicalite.api.input.naturalmouse.support.Flow;
import net.unethicalite.api.input.naturalmouse.support.MouseMotionNature;
import net.unethicalite.api.input.naturalmouse.support.SinusoidalDeviationProvider;
import net.unethicalite.api.input.naturalmouse.util.FlowTemplates;
import net.unethicalite.api.input.naturalmouse.util.Pair;

public class FactoryTemplates {
    public static MouseMotionFactory createGrannyMotionFactory() {
        return FactoryTemplates.createGrannyMotionFactory(new DefaultMouseMotionNature());
    }

    public static MouseMotionFactory createGrannyMotionFactory(MouseMotionNature nature) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        ArrayList<Flow> flows = new ArrayList<Flow>(Arrays.asList(new Flow(FlowTemplates.jaggedFlow()), new Flow(FlowTemplates.random()), new Flow(FlowTemplates.interruptedFlow()), new Flow(FlowTemplates.interruptedFlow2()), new Flow(FlowTemplates.adjustingFlow()), new Flow(FlowTemplates.stoppingFlow())));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(9.0));
        factory.setNoiseProvider(new DefaultNoiseProvider(1.6));
        factory.getNature().setReactionTimeBaseMs(100);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager)factory.getOvershootManager();
        overshootManager.setOvershoots(3);
        overshootManager.setMinDistanceForOvershoots(3L);
        overshootManager.setMinOvershootMovementMs(400L);
        overshootManager.setOvershootRandomModifierDivider(10.0);
        overshootManager.setOvershootSpeedupDivider(3.6);
        factory.getNature().setTimeToStepsDivider(6.0);
        manager.setMouseMovementBaseTimeMs(1000L);
        factory.setSpeedManager(manager);
        return factory;
    }

    public static MouseMotionFactory createDemoRobotMotionFactory(long motionTimeMsPer100Pixels) {
        return FactoryTemplates.createDemoRobotMotionFactory(new DefaultMouseMotionNature(), motionTimeMsPer100Pixels);
    }

    public static MouseMotionFactory createDemoRobotMotionFactory(MouseMotionNature nature, long motionTimeMsPer100Pixels) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        Flow flow = new Flow(FlowTemplates.constantSpeed());
        double timePerPixel = (double)motionTimeMsPer100Pixels / 100.0;
        SpeedManager manager = distance -> new Pair<Flow, Long>(flow, (long)(timePerPixel * distance));
        factory.setDeviationProvider((totalDistanceInPixels, completionFraction) -> DoublePoint.ZERO);
        factory.setNoiseProvider((random, xStepSize, yStepSize) -> DoublePoint.ZERO);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager)factory.getOvershootManager();
        overshootManager.setOvershoots(0);
        factory.setSpeedManager(manager);
        return factory;
    }

    public static MouseMotionFactory createFastGamerMotionFactory() {
        return FactoryTemplates.createFastGamerMotionFactory(new DefaultMouseMotionNature());
    }

    public static MouseMotionFactory createFastGamerMotionFactory(MouseMotionNature nature) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        ArrayList<Flow> flows = new ArrayList<Flow>(Arrays.asList(new Flow(FlowTemplates.variatingFlow()), new Flow(FlowTemplates.slowStartupFlow()), new Flow(FlowTemplates.slowStartup2Flow()), new Flow(FlowTemplates.adjustingFlow()), new Flow(FlowTemplates.jaggedFlow())));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(10.0));
        factory.setNoiseProvider(new DefaultNoiseProvider(2.0));
        factory.getNature().setReactionTimeVariationMs(100);
        manager.setMouseMovementBaseTimeMs(250L);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager)factory.getOvershootManager();
        overshootManager.setOvershoots(4);
        factory.setSpeedManager(manager);
        return factory;
    }

    public static MouseMotionFactory createAverageComputerUserMotionFactory() {
        return FactoryTemplates.createAverageComputerUserMotionFactory(new DefaultMouseMotionNature());
    }

    public static MouseMotionFactory createAverageComputerUserMotionFactory(MouseMotionNature nature) {
        MouseMotionFactory factory = new MouseMotionFactory(nature);
        ArrayList<Flow> flows = new ArrayList<Flow>(Arrays.asList(new Flow(FlowTemplates.variatingFlow()), new Flow(FlowTemplates.interruptedFlow()), new Flow(FlowTemplates.interruptedFlow2()), new Flow(FlowTemplates.slowStartupFlow()), new Flow(FlowTemplates.slowStartup2Flow()), new Flow(FlowTemplates.adjustingFlow()), new Flow(FlowTemplates.jaggedFlow()), new Flow(FlowTemplates.stoppingFlow())));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(10.0));
        factory.setNoiseProvider(new DefaultNoiseProvider(2.0));
        factory.getNature().setReactionTimeVariationMs(110);
        manager.setMouseMovementBaseTimeMs(400L);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager)factory.getOvershootManager();
        overshootManager.setOvershoots(4);
        factory.setSpeedManager(manager);
        return factory;
    }
}

