package net.runelite.client.plugins.ox.thieving;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("oxThieveBot")
public interface ThievingConfig extends Config {
    @ConfigSection(
        name = "Progression",
        description = "Enable stalls lowest->highest. Bot steals until Thieving reaches the next enabled stall, then sells and walks there. Enable one for single-stall mode.",
        position = 0
    )
    String progressionSection = "progression";

    @ConfigSection(name = "Antiban", description = "Click delays and optional sleep breaks", position = 1)
    String antibanSection = "antiban";

    @ConfigSection(name = "Failsafe", description = "Logout if stall disappears", position = 2)
    String failsafeSection = "failsafe";

    @ConfigSection(name = "Muling", description = "Offload GP to a mule, then return", position = 3, closedByDefault = true)
    String muleSection = "mule";

    @ConfigSection(name = "Overlay", description = "Inventory status card", position = 4)
    String overlaySection = "overlay";

    @ConfigItem(keyName = "stall1Enabled", name = "Lvl 1 stall", description = "Steal at the level 1 stall", section = progressionSection, position = 0)
    default boolean stall1Enabled() {
        return true;
    }

    @ConfigItem(keyName = "stall2Enabled", name = "Lvl 25 stall", description = "Steal at the level 25 stall", section = progressionSection, position = 1)
    default boolean stall2Enabled() {
        return true;
    }

    @ConfigItem(keyName = "stall3Enabled", name = "Lvl 50 stall", description = "Steal at the level 50 stall", section = progressionSection, position = 2)
    default boolean stall3Enabled() {
        return true;
    }

    @ConfigItem(keyName = "stall4Enabled", name = "Lvl 75 stall", description = "Steal at the level 75 stall", section = progressionSection, position = 3)
    default boolean stall4Enabled() {
        return true;
    }

    @ConfigItem(keyName = "stall5Enabled", name = "Lvl 90 stall", description = "Steal at the level 90 stall", section = progressionSection, position = 4)
    default boolean stall5Enabled() {
        return true;
    }

    @ConfigItem(keyName = "minClickDelay", name = "Min Click Delay (ms)", description = "Minimum wait after each steal or sell", section = antibanSection, position = 0)
    @Range(min = 200, max = 5000)
    @Units("ms")
    default int minClickDelay() {
        return 600;
    }

    @ConfigItem(keyName = "maxClickDelay", name = "Max Click Delay (ms)", description = "Maximum wait after each steal or sell", section = antibanSection, position = 1)
    @Range(min = 300, max = 10000)
    @Units("ms")
    default int maxClickDelay() {
        return 1200;
    }

    @ConfigItem(keyName = "randomnessPercent", name = "Randomness %", description = "Extra jitter on delays", section = antibanSection, position = 2)
    @Range(min = 0, max = 50)
    @Units("%")
    default int randomnessPercent() {
        return 15;
    }

    @ConfigItem(keyName = "sellDelay", name = "Sell Click Delay (ms)", description = "Base delay between shop sell clicks", section = antibanSection, position = 3)
    @Range(min = 200, max = 3000)
    @Units("ms")
    default int sellDelay() {
        return 500;
    }

    @ConfigItem(keyName = "enableSleepBreaks", name = "Enable Sleep Breaks", description = "Periodically pause to simulate a break", section = antibanSection, position = 4)
    default boolean enableSleepBreaks() {
        return false;
    }

    @ConfigItem(keyName = "sleepAfterMinutes", name = "Sleep After (minutes)", description = "Break after this many minutes", section = antibanSection, position = 5)
    @Range(min = 5, max = 120)
    @Units(" mins")
    default int sleepAfterMinutes() {
        return 30;
    }

    @ConfigItem(keyName = "sleepDurationSeconds", name = "Sleep Duration (seconds)", description = "How long each break lasts", section = antibanSection, position = 6)
    @Range(min = 10, max = 300)
    @Units("s")
    default int sleepDurationSeconds() {
        return 60;
    }

    @ConfigItem(keyName = "enableLogoutFailsafe", name = "Enable Logout Failsafe", description = "Log out if the stall is missing too long", section = failsafeSection, position = 0)
    default boolean enableLogoutFailsafe() {
        return true;
    }

    @ConfigItem(keyName = "stallMissingTimeoutSec", name = "Stall Missing Timeout (sec)", description = "Logout if stall not seen for this long", section = failsafeSection, position = 1)
    @Range(min = 10, max = 180)
    @Units("s")
    default int stallMissingTimeoutSec() {
        return 30;
    }

    @ConfigItem(keyName = "enableMuling", name = "Enable Muling", description = "Trade GP to a mule when triggered", section = muleSection, position = 0)
    default boolean enableMuling() {
        return false;
    }

    @ConfigItem(keyName = "enableTimeMule", name = "Use Time Interval", description = "Trigger mule on a timer instead of GP threshold", section = muleSection, position = 1)
    default boolean enableTimeMule() {
        return false;
    }

    @ConfigItem(keyName = "muleGpThreshold", name = "GP Threshold", description = "Mule when coin stack reaches this (if not using time)", section = muleSection, position = 2)
    @Range(min = 1000, max = Integer.MAX_VALUE)
    default int muleGpThreshold() {
        return 100000;
    }

    @ConfigItem(keyName = "muleIntervalMinutes", name = "Mule Interval (minutes)", description = "Mule every N minutes when time mode is on", section = muleSection, position = 3)
    @Range(min = 1, max = 240)
    @Units(" mins")
    default int muleIntervalMinutes() {
        return 30;
    }

    @ConfigItem(keyName = "muleName", name = "Mule Player Name", description = "Exact RSN of the mule", section = muleSection, position = 4)
    default String muleName() {
        return "";
    }

    @ConfigItem(keyName = "muleMeetTile", name = "Meeting Point Tile", description = "Format: x,y or x,y,plane", section = muleSection, position = 5)
    default String muleMeetTile() {
        return "";
    }

    @ConfigItem(keyName = "muleExtraItemIds", name = "Extra Item IDs", description = "Comma-separated IDs offered with GP (e.g. 4151,11840)", section = muleSection, position = 6)
    default String muleExtraItemIds() {
        return "";
    }

    @ConfigItem(keyName = "showOverlay", name = "Show Overlay", description = "Compact status card under the inventory", section = overlaySection, position = 0)
    default boolean showOverlay() {
        return true;
    }
}
