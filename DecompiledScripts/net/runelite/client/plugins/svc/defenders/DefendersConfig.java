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
package net.runelite.client.plugins.svc.defenders;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(value="x64defenders")
public interface DefendersConfig
extends Config {
    @ConfigSection(name="Combat", description="Eating and target selection", position=0)
    public static final String combatSection = "combat";
    @ConfigSection(name="Looting", description="Defender looting and stop condition", position=1)
    public static final String lootSection = "looting";
    @ConfigSection(name="Overlay", description="On-screen HUD", position=2)
    public static final String overlaySection = "overlay";

    @ConfigItem(keyName="eatFood", name="Eat Food", description="Automatically eat any food in the inventory when HP gets low", section="combat", position=0)
    default public boolean eatFood() {
        return true;
    }

    @ConfigItem(keyName="eatAtHpPercent", name="Eat At HP %", description="Eat when hitpoints drop below this percentage of your maximum", section="combat", position=1)
    @Range(min=5, max=95)
    default public int eatAtHpPercent() {
        return 50;
    }

    @ConfigItem(keyName="onlyAttackIdle", name="Only Attack Idle Cyclopes", description="Skip any Cyclops already in combat (e.g. being fought by another player) and move on to the next one", section="combat", position=2)
    default public boolean onlyAttackIdle() {
        return true;
    }

    @ConfigItem(keyName="keepRunEnabled", name="Keep Run Enabled", description="Toggle run on whenever there is enough energy", section="combat", position=3)
    default public boolean keepRunEnabled() {
        return true;
    }

    @ConfigItem(keyName="stopAfterDragon", name="Stop After Dragon Defender", description="Once a Dragon defender (the max) is looted, stop the bot. The Dragon defender is always looted first, so it is never left on the ground.", section="looting", position=0)
    default public boolean stopAfterDragon() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show HUD", description="Draw the x64 Auto Defenders status panel over the chatbox", section="overlay", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

