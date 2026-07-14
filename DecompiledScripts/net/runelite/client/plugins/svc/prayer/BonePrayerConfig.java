/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 */
package net.runelite.client.plugins.svc.prayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(value="orionBonePrayer")
public interface BonePrayerConfig
extends Config {
    @ConfigSection(name="Prayer", description="Bone offering settings", position=0)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Advanced", description="Optional overrides \u2014 leave defaults unless something isn't working", position=1)
    public static final String advancedSection = "advanced";

    @ConfigItem(keyName="offerAllBones", name="Offer all bones", description="Offer every type of bone in your bank (Bones, Big bones, Dragon bones, etc.). Turn off to only offer the specific bones listed below.", section="prayer", position=0)
    default public boolean offerAllBones() {
        return true;
    }

    @ConfigItem(keyName="specificBones", name="Specific bones", description="Comma-separated bone names (substring, case-insensitive) to offer when 'Offer all bones' is OFF \u2014 e.g. 'Dragon bones, Superior dragon bones'.", section="prayer", position=1)
    default public String specificBones() {
        return "Dragon bones";
    }

    @ConfigItem(keyName="stopWhenOut", name="Stop when out of bones", description="Stop the bot once the bank has no more bones. Off = idle and re-check the bank periodically.", section="prayer", position=2)
    default public boolean stopWhenOut() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show overlay", description="Show the live status overlay.", section="prayer", position=3)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="keepItems", name="Keep items", description="Comma-separated item names (substring) that are NEVER deposited at the bank, e.g. a teleport tab or charged jewellery you keep on you.", section="advanced", position=0)
    default public String keepItems() {
        return "";
    }
}

