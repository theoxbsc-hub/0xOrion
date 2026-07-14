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
package net.runelite.client.plugins.svc.mining;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.svc.mining.BarType;
import net.runelite.client.plugins.svc.mining.MiningMode;
import net.runelite.client.plugins.svc.mining.MiningOre;

@ConfigGroup(value="orionMiningBot")
public interface MiningConfig
extends Config {
    @ConfigSection(name="Mining", description="Mining settings", position=0)
    public static final String miningSection = "mining";
    @ConfigSection(name="Bar goal", description="Mine exactly the ores needed to smelt a chosen bar", position=1)
    public static final String barSection = "barGoal";
    @ConfigSection(name="Advanced", description="Optional overrides \u2014 leave defaults unless mining isn't working", position=2)
    public static final String advancedSection = "advanced";

    @ConfigItem(keyName="ore", name="Ore to mine", description="Which ore to mine. Drives what gets banked/dropped. Pick 'Any rock' to power-mine whatever rocks are nearby.", section="mining", position=0)
    default public MiningOre ore() {
        return MiningOre.IRON;
    }

    @ConfigItem(keyName="miningMode", name="When inventory full", description="Bank: deposit at the nearest bank chest and return. Drop: drop the ore on the ground.", section="mining", position=1)
    default public MiningMode miningMode() {
        return MiningMode.BANK;
    }

    @ConfigItem(keyName="returnToMine", name="Return after banking", description="After banking, walk back to where you were mining (Bank mode only).", section="mining", position=2)
    default public boolean returnToMine() {
        return true;
    }

    @ConfigItem(keyName="microBreaks", name="Human breaks", description="Occasionally pause like a player looking away \u2014 usually 15-75 seconds, sometimes a few minutes. Costs a little XP/hr, makes the session pattern far less bot-like.", section="mining", position=3)
    default public boolean microBreaks() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show overlay", description="Show the live status overlay.", section="mining", position=4)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="barType", name="Mine for bar", description="Gather the ores needed to smelt this bar (e.g. Adamant bar = 1 adamantite ore + 6 coal per bar). Overrides 'Ore to mine' and always banks. Off = mine the selected ore normally.", section="barGoal", position=0)
    default public BarType barType() {
        return BarType.NONE;
    }

    @Range(min=0, max=50000)
    @ConfigItem(keyName="barAmount", name="Number of bars", description="How many bars' worth of ore to gather. 0 = no target: keep mining the bar's ores in ratio forever. Progress counts ore gathered while the bot runs (restarting the plugin restarts the count).", section="barGoal", position=1)
    default public int barAmount() {
        return 0;
    }

    @ConfigItem(keyName="stopWhenDone", name="Stop when target reached", description="Once the bar target is gathered, bank the last load and stop mining (the overlay shows DONE). Off = keep mining the bar's ores in ratio past the target.", section="barGoal", position=2)
    default public boolean stopWhenDone() {
        return true;
    }

    @ConfigItem(keyName="keepItems", name="Keep items", description="Comma-separated item names (substring) that are NEVER banked or dropped, e.g. your pickaxe, chisel, gem bag.", section="advanced", position=0)
    default public String keepItems() {
        return "pickaxe";
    }

    @ConfigItem(keyName="matchRockByName", name="Match rock by name", description="Only mine rocks whose name matches the selected ore, and skip depleted 'Rocks'. Leave ON. Turn off only if your server doesn't name rocks after their ore.", section="advanced", position=1)
    default public boolean matchRockByName() {
        return true;
    }

    @ConfigItem(keyName="rockNameOverride", name="Rock name (optional)", description="Substring of the live rock's name to mine (case-insensitive). Leave blank to use the selected ore's default keyword. Set this if your server names the rock differently.", section="advanced", position=2)
    default public String rockNameOverride() {
        return "";
    }

    @ConfigItem(keyName="avoidOtherPlayers", name="Avoid others' rocks", description="Skip a rock that another player is standing next to (already mining it).", section="advanced", position=3)
    default public boolean avoidOtherPlayers() {
        return true;
    }
}

