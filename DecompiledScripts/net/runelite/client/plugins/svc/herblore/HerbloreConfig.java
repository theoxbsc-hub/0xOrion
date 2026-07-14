/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 */
package net.runelite.client.plugins.svc.herblore;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.svc.herblore.HerbloreTask;

@ConfigGroup(value="orionHerbloreBot")
public interface HerbloreConfig
extends Config {
    @ConfigSection(name="Task priority", description="Order the three kinds of herblore work. Each trip the bot tries Priority 1 first and only falls through to Priority 2/3 when the higher slot has nothing doable. Set a slot to \"skip\" to disable that category entirely.", position=0)
    public static final String prioritySection = "priority";
    @ConfigSection(name="Herblore", description="AIO herblore behaviour", position=1)
    public static final String herbloreSection = "herblore";

    @ConfigItem(keyName="priority1", name="Priority 1", description="First choice. \"Finished potions\" uses the unfinished potions you already have; \"Unfinished potions\" mixes clean herbs with vials of water; \"Clean grimy herbs\" cleans grimy herbs. Within a category the bot always picks the best one for your level.", section="priority", position=0)
    default public HerbloreTask priority1() {
        return HerbloreTask.FINISHED_POTIONS;
    }

    @ConfigItem(keyName="priority2", name="Priority 2", description="Second choice, used only when Priority 1 has nothing makeable this trip.", section="priority", position=1)
    default public HerbloreTask priority2() {
        return HerbloreTask.UNFINISHED_POTIONS;
    }

    @ConfigItem(keyName="priority3", name="Priority 3", description="Third choice, used only when Priorities 1 and 2 both have nothing makeable.", section="priority", position=2)
    default public HerbloreTask priority3() {
        return HerbloreTask.CLEAN_HERBS;
    }

    @ConfigItem(keyName="stopWhenIdle", name="Stop when out of materials", description="Stop the bot when nothing is makeable or cleanable. Turn off to instead stay logged in and re-check the bank periodically (useful if you restock mid-session).", section="herblore", position=1)
    default public boolean stopWhenIdle() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show overlay", description="Show the x64 AIO Herblore HUD over the chatbox.", section="herblore", position=2)
    default public boolean showOverlay() {
        return true;
    }
}

