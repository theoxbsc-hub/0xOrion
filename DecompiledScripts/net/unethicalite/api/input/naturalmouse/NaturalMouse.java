/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 */
package net.unethicalite.api.input.naturalmouse;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.input.naturalmouse.api.MouseInfoAccessor;
import net.unethicalite.api.input.naturalmouse.api.MouseMotion;
import net.unethicalite.api.input.naturalmouse.api.MouseMotionFactory;
import net.unethicalite.api.input.naturalmouse.api.SystemCalls;
import net.unethicalite.api.input.naturalmouse.support.DefaultMouseMotionNature;
import net.unethicalite.api.input.naturalmouse.support.DefaultNoiseProvider;
import net.unethicalite.api.input.naturalmouse.support.DefaultOvershootManager;
import net.unethicalite.api.input.naturalmouse.support.DefaultSpeedManager;
import net.unethicalite.api.input.naturalmouse.support.Flow;
import net.unethicalite.api.input.naturalmouse.support.MouseMotionNature;
import net.unethicalite.api.input.naturalmouse.support.SinusoidalDeviationProvider;
import net.unethicalite.api.input.naturalmouse.util.FlowTemplates;
import net.unethicalite.api.input.naturalmouse.util.Pair;

@Singleton
public class NaturalMouse {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final MouseMotionNature nature;
    @Inject
    private Client client;
    @Inject
    private ExecutorService executorService;
    private List<Flow> flows = List.of(new Flow(FlowTemplates.variatingFlow()), new Flow(FlowTemplates.slowStartupFlow()), new Flow(FlowTemplates.slowStartup2Flow()), new Flow(FlowTemplates.jaggedFlow()), new Flow(FlowTemplates.interruptedFlow()), new Flow(FlowTemplates.interruptedFlow2()), new Flow(FlowTemplates.stoppingFlow()), new Flow(FlowTemplates.adjustingFlow()), new Flow(FlowTemplates.random()));

    @Inject
    private NaturalMouse() {
        this.nature = new DefaultMouseMotionNature();
        this.nature.setSystemCalls(new SystemCallsImpl());
        this.nature.setMouseInfo(new MouseInfoImpl());
    }

    public synchronized void moveTo(int dx, int dy) {
        if (!this.client.isClientThread()) {
            this.move(dx, dy);
        } else {
            this.executorService.submit(() -> this.move(dx, dy));
        }
    }

    private synchronized void move(int dx, int dy) {
        MouseMotion motion = this.getFactory().build(dx, dy);
        try {
            motion.move();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public MouseMotionFactory getFactory() {
        MouseMotionFactory factory = new MouseMotionFactory();
        factory.setNature(this.nature);
        factory.setRandom(this.random);
        SpeedManagerImpl manager = new SpeedManagerImpl(this.flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(10.0));
        factory.setNoiseProvider(new DefaultNoiseProvider(2.0));
        factory.getNature().setReactionTimeVariationMs(75);
        manager.setMouseMovementBaseTimeMs(150L);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager)factory.getOvershootManager();
        overshootManager.setOvershoots(4);
        factory.setSpeedManager(manager);
        return factory;
    }

    public void moveOffScreen() {
        if (this.random.nextInt(4) == 0) {
            int horizontal = this.random.nextBoolean() ? -1 : this.client.getCanvasWidth() + 1;
            int vertical = this.random.nextBoolean() ? -1 : this.client.getCanvasHeight() + 1;
            boolean exitHorizontally = this.random.nextBoolean();
            if (exitHorizontally) {
                this.moveTo(horizontal, this.random.nextInt(0, this.client.getCanvasHeight() + 1));
            } else {
                this.moveTo(this.random.nextInt(0, this.client.getCanvasWidth() + 1), vertical);
            }
            if (this.random.nextInt(4) == 0) {
                this.client.setFocused(false);
            }
        }
    }

    public List<Flow> getFlows() {
        return this.flows;
    }

    public void setFlows(List<Flow> flows) {
        this.flows = flows;
    }

    private class SystemCallsImpl
    implements SystemCalls {
        private SystemCallsImpl() {
        }

        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        @Override
        public void sleep(long time) {
            Time.sleep(time);
        }

        @Override
        public Dimension getScreenSize() {
            return NaturalMouse.this.client.getCanvas().getSize();
        }

        @Override
        public void setMousePosition(int x, int y) {
            NaturalMouse.this.client.getMouseHandler().sendMovement(x, y);
        }
    }

    private static class MouseInfoImpl
    implements MouseInfoAccessor {
        private MouseInfoImpl() {
        }

        @Override
        public Point getMousePosition() {
            return Mouse.getPosition();
        }
    }

    private static class SpeedManagerImpl
    extends DefaultSpeedManager {
        private SpeedManagerImpl(Collection<Flow> flows) {
            super(flows);
        }

        @Override
        public Pair<Flow, Long> getFlowWithTime(double distance) {
            Pair<Flow, Long> pair = super.getFlowWithTime(distance);
            return new Pair<Flow, Long>((Flow)pair.x, (Long)pair.y);
        }
    }
}

