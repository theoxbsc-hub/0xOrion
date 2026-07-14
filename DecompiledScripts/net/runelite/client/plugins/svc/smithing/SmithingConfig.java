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
package net.runelite.client.plugins.svc.smithing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.svc.smithing.SmithingMode;

@ConfigGroup(value="orionSmithingBot")
public interface SmithingConfig
extends Config {
    @ConfigSection(name="Smithing", description="What to make", position=0)
    public static final String mainSection = "main";
    @ConfigSection(name="Misc", description="Anti-ban and overlay", position=1)
    public static final String miscSection = "misc";

    @ConfigItem(keyName="mode", name="Mode", description="Auto: each cycle, do the highest-level thing you can make from the bank (smelt ores or smith bars). Or force one: Smelt only / Smith only.", section="main", position=0)
    default public SmithingMode mode() {
        return SmithingMode.AUTO;
    }

    @ConfigItem(keyName="stopWhenOutOfMaterials", name="Stop when out of materials", description="Stop the bot when nothing in the bank can be smelted or smithed at your level. Turn off to idle and re-check instead (e.g. if a runner restocks your bank).", section="main", position=1)
    default public boolean stopWhenOutOfMaterials() {
        return true;
    }

    @ConfigItem(keyName="antiBanMs", name="Anti-ban delay (ms)", description="Random extra delay (0\u2013this) added to actions to vary timing.", section="misc", position=0)
    @Range(min=0, max=600)
    default public int antiBanMs() {
        return 180;
    }

    @ConfigItem(keyName="showOverlay", name="Show overlay", description="Show the live status overlay.", section="misc", position=1)
    default public boolean showOverlay() {
        return true;
    }
}

