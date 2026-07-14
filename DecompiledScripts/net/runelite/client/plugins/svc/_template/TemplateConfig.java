/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 */
package net.runelite.client.plugins.svc._template;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(value="myScript")
public interface TemplateConfig
extends Config {
    @ConfigSection(name="General", description="General settings", position=0)
    public static final String generalSection = "general";

    @ConfigItem(keyName="targetName", name="Target Name", description="Name of the thing to interact with", section="general", position=0)
    default public String targetName() {
        return "Fishing spot";
    }

    @ConfigItem(keyName="dropFish", name="Drop Fish", description="Drop fish instead of banking", section="general", position=1)
    default public boolean dropFish() {
        return false;
    }

    @ConfigItem(keyName="radius", name="Search Radius", description="Tiles to search", section="general", position=2)
    @Range(min=1, max=20)
    default public int radius() {
        return 8;
    }
}

