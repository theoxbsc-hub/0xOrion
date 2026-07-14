/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 */
package net.runelite.client.plugins.svc.agility;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value="orionAgility")
public interface AgilityConfig
extends Config {
    @ConfigSection(name="Courses", description="Enable the course you want to run. If more than one is enabled, the topmost takes priority.", position=0)
    public static final String coursesSection = "courses";
    @ConfigSection(name="General", description="Overlay and timing", position=1)
    public static final String generalSection = "general";

    @ConfigItem(keyName="enableLvl1Course", name="Lvl 1 Course (Gnome)", description="Run the level 1 course: pipe -> slippery log -> rope swing -> obstacle net -> rocks, looped.", section="courses", position=0)
    default public boolean enableLvl1Course() {
        return true;
    }

    @ConfigItem(keyName="enableLvl50Course", name="Lvl 50 Course (Ice)", description="Run the level 50 ice course (10 obstacles, ending at the cave), looped. If Lvl 1 is also enabled, Lvl 1 takes priority.", section="courses", position=1)
    default public boolean enableLvl50Course() {
        return false;
    }

    @ConfigItem(keyName="showOverlay", name="Show Overlay", description="Show the status overlay.", section="general", position=0)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="randomizeDelay", name="Randomize Click Delay", description="Add a small random reaction delay (~150-600ms) before each obstacle click for a more human cadence. Disable for instant clicks.", section="general", position=1)
    default public boolean randomizeDelay() {
        return true;
    }
}

