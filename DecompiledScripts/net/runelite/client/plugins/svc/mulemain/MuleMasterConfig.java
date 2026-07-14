/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 *  net.runelite.client.config.Units
 */
package net.runelite.client.plugins.svc.mulemain;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(value="orionMuleMaster")
public interface MuleMasterConfig
extends Config {
    @ConfigSection(name="Trade Timing", description="Delay before accepting trade screen 1, to let the thieving bot finish offering items", position=0, closedByDefault=false)
    public static final String timingSection = "timing";
    @ConfigSection(name="Debug", description="Overlay options", position=1, closedByDefault=true)
    public static final String debugSection = "debug";

    @ConfigItem(keyName="screen1AcceptDelayMs", name="Screen 1 Accept Delay (ms)", description="How long to wait after trade screen 1 opens before accepting. Increase if items are missing from the trade.", section="timing", position=0)
    @Range(min=0, max=15000)
    @Units(value="ms")
    default public int screen1AcceptDelayMs() {
        return 4000;
    }

    @ConfigItem(keyName="showOverlay", name="Show Overlay", description="Display mule status on screen", section="debug", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

