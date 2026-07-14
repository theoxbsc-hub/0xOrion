/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Random;
import net.unethicalite.api.input.naturalmouse.support.DefaultMouseInfoAccessor;
import net.unethicalite.api.input.naturalmouse.support.DefaultNoiseProvider;
import net.unethicalite.api.input.naturalmouse.support.DefaultOvershootManager;
import net.unethicalite.api.input.naturalmouse.support.DefaultSpeedManager;
import net.unethicalite.api.input.naturalmouse.support.DefaultSystemCalls;
import net.unethicalite.api.input.naturalmouse.support.MouseMotionNature;
import net.unethicalite.api.input.naturalmouse.support.SinusoidalDeviationProvider;

public class DefaultMouseMotionNature
extends MouseMotionNature {
    public static final int TIME_TO_STEPS_DIVIDER = 8;
    public static final int MIN_STEPS = 10;
    public static final int EFFECT_FADE_STEPS = 15;
    public static final int REACTION_TIME_BASE_MS = 20;
    public static final int REACTION_TIME_VARIATION_MS = 120;

    public DefaultMouseMotionNature() {
        try {
            this.setSystemCalls(new DefaultSystemCalls(new Robot()));
        }
        catch (AWTException e) {
            throw new RuntimeException(e);
        }
        this.setDeviationProvider(new SinusoidalDeviationProvider(10.0));
        this.setNoiseProvider(new DefaultNoiseProvider(2.0));
        this.setSpeedManager(new DefaultSpeedManager());
        this.setOvershootManager(new DefaultOvershootManager(new Random()));
        this.setEffectFadeSteps(15);
        this.setMinSteps(10);
        this.setMouseInfo(new DefaultMouseInfoAccessor());
        this.setReactionTimeBaseMs(20);
        this.setReactionTimeVariationMs(120);
        this.setTimeToStepsDivider(8.0);
    }
}

