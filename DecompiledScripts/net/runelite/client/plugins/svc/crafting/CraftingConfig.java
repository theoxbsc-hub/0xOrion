/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 */
package net.runelite.client.plugins.svc.crafting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value="orionCrafterBot")
public interface CraftingConfig
extends Config {
    @ConfigSection(name="Crafting", description="AIO gem-cutting behaviour", position=0)
    public static final String craftingSection = "crafting";

    @ConfigItem(keyName="stopWhenIdle", name="Stop when out of gems", description="Stop the bot when there are no uncut gems left to cut. Turn off to instead stay logged in and re-check the bank periodically (useful if you restock mid-session).", section="crafting", position=0)
    default public boolean stopWhenIdle() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show overlay", description="Show the x64 AIO Crafter HUD over the chatbox.", section="crafting", position=1)
    default public boolean showOverlay() {
        return true;
    }
}

