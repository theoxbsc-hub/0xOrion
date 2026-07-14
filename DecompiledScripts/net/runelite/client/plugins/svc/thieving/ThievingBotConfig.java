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
package net.runelite.client.plugins.svc.thieving;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(value="orionThievingBot")
public interface ThievingBotConfig
extends Config {
    @ConfigSection(name="Progression", description="Pick which preset stalls to cycle through. Enabled stalls (lowest level first) form a queue: the bot steals at one until your Thieving level reaches the next enabled stall's requirement, then sells everything to the current trader, walks to the next stall, and resumes. Enable just one for single-stall mode.", position=0, closedByDefault=false)
    public static final String progressionSection = "progression";
    @ConfigSection(name="Antiban", description="Click delay randomisation and sleep break settings", position=1, closedByDefault=false)
    public static final String antibanSection = "antiban";
    @ConfigSection(name="Failsafe", description="Auto-logout if the stall cannot be found", position=2, closedByDefault=false)
    public static final String failsafeSection = "failsafe";
    @ConfigSection(name="Muling", description="Offload accumulated GP to a mule account, then return to thieving", position=3, closedByDefault=true)
    public static final String muleSection = "mule";
    @ConfigSection(name="Display", description="On-screen overlay options", position=4, closedByDefault=false)
    public static final String displaySection = "display";

    @ConfigItem(keyName="stall1Enabled", name="Lvl 1 stall", description="Steal at the level 1 stall.", section="progression", position=0)
    default public boolean stall1Enabled() {
        return true;
    }

    @ConfigItem(keyName="stall2Enabled", name="Lvl 25 stall", description="Steal at the level 25 stall.", section="progression", position=1)
    default public boolean stall2Enabled() {
        return true;
    }

    @ConfigItem(keyName="stall3Enabled", name="Lvl 50 stall", description="Steal at the level 50 stall.", section="progression", position=2)
    default public boolean stall3Enabled() {
        return true;
    }

    @ConfigItem(keyName="stall4Enabled", name="Lvl 75 stall", description="Steal at the level 75 stall.", section="progression", position=3)
    default public boolean stall4Enabled() {
        return true;
    }

    @ConfigItem(keyName="stall5Enabled", name="Lvl 90 stall", description="Steal at the level 90 stall.", section="progression", position=4)
    default public boolean stall5Enabled() {
        return true;
    }

    @ConfigItem(keyName="minClickDelay", name="Min Click Delay (ms)", description="Minimum wait after each steal or sell action", section="antiban", position=0)
    @Range(min=200, max=5000)
    @Units(value="ms")
    default public int minClickDelay() {
        return 600;
    }

    @ConfigItem(keyName="maxClickDelay", name="Max Click Delay (ms)", description="Maximum wait after each steal or sell action", section="antiban", position=1)
    @Range(min=300, max=10000)
    @Units(value="ms")
    default public int maxClickDelay() {
        return 1200;
    }

    @ConfigItem(keyName="randomnessPercent", name="Randomness %", description="Extra jitter applied to delays (+/- this % of the computed delay)", section="antiban", position=2)
    @Range(min=0, max=50)
    @Units(value="%")
    default public int randomnessPercent() {
        return 15;
    }

    @ConfigItem(keyName="sellDelay", name="Sell Click Delay (ms)", description="Base delay between sell clicks in the shop (randomness % is also applied)", section="antiban", position=3)
    @Range(min=200, max=3000)
    @Units(value="ms")
    default public int sellDelay() {
        return 500;
    }

    @ConfigItem(keyName="enableSleepBreaks", name="Enable Sleep Breaks", description="Periodically pause stealing to simulate a human taking a break", section="antiban", position=4)
    default public boolean enableSleepBreaks() {
        return false;
    }

    @ConfigItem(keyName="sleepAfterMinutes", name="Sleep After (minutes)", description="Take a break after running for this many minutes", section="antiban", position=5)
    @Range(min=5, max=120)
    @Units(value=" mins")
    default public int sleepAfterMinutes() {
        return 30;
    }

    @ConfigItem(keyName="sleepDurationSeconds", name="Sleep Duration (seconds)", description="How long to pause during each break", section="antiban", position=6)
    @Range(min=10, max=300)
    @Units(value="s")
    default public int sleepDurationSeconds() {
        return 60;
    }

    @ConfigItem(keyName="enableLogoutFailsafe", name="Enable Logout Failsafe", description="Log out if the stall object is missing for too long", section="failsafe", position=0)
    default public boolean enableLogoutFailsafe() {
        return true;
    }

    @ConfigItem(keyName="stallMissingTimeoutSec", name="Stall Missing Timeout (sec)", description="Logout if the stall has not been detected for this many seconds", section="failsafe", position=1)
    @Range(min=10, max=180)
    @Units(value="s")
    default public int stallMissingTimeoutSec() {
        return 30;
    }

    @ConfigItem(keyName="enableMuling", name="Enable Muling", description="Automatically trade GP to a mule account when the trigger condition is met", section="mule", position=0)
    default public boolean enableMuling() {
        return false;
    }

    @ConfigItem(keyName="enableTimeMule", name="Use Time Interval", description="Trigger mule on a fixed time interval instead of GP threshold", section="mule", position=1)
    default public boolean enableTimeMule() {
        return false;
    }

    @ConfigItem(keyName="muleGpThreshold", name="GP Threshold", description="Trigger mule when your coin stack reaches this amount (ignored if Use Time Interval is on)", section="mule", position=2)
    @Range(min=1000, max=0x7FFFFFFF)
    default public int muleGpThreshold() {
        return 100000;
    }

    @ConfigItem(keyName="muleIntervalMinutes", name="Mule Interval (minutes)", description="Mule every N minutes (only used when Use Time Interval is enabled)", section="mule", position=3)
    @Range(min=1, max=240)
    @Units(value=" mins")
    default public int muleIntervalMinutes() {
        return 30;
    }

    @ConfigItem(keyName="muleName", name="Mule Player Name", description="Exact RSN of the mule account to trade gold to", section="mule", position=4)
    default public String muleName() {
        return "";
    }

    @ConfigItem(keyName="muleMeetTile", name="Meeting Point Tile", description="World tile to walk to and meet the mule for the trade. Format: x,y (or x,y,plane). Stand on the spot and read your coordinates from the Entity Inspector overlay. Required when muling is enabled.", section="mule", position=5)
    default public String muleMeetTile() {
        return "";
    }

    @ConfigItem(keyName="muleExtraItemIds", name="Extra Item IDs", description="Additional item IDs to trade to the mule alongside the GP. Comma-separated (e.g. 4151, 11840). Each is offered with Offer-All. GP (coins) is always traded; leave this blank to trade GP only. Use the Entity Inspector to find item IDs.", section="mule", position=6)
    default public String muleExtraItemIds() {
        return "";
    }

    @ConfigItem(keyName="showOverlay", name="Show Overlay", description="Display bot status and stats on screen", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

