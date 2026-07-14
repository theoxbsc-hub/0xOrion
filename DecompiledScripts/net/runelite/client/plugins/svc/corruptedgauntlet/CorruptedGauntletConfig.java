/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Alpha
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 *  net.runelite.client.config.Units
 */
package net.runelite.client.plugins.svc.corruptedgauntlet;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.corruptedgauntlet.HunllefWeapon;

@ConfigGroup(value="orionCorruptedGauntlet")
public interface CorruptedGauntletConfig
extends Config {
    @ConfigSection(name="General", description="Kill count and overlay", position=0, closedByDefault=false)
    public static final String generalSection = "general";
    @ConfigSection(name="Auto Re-Enter", description="Re-enter the Corrupted Gauntlet after each run", position=7, closedByDefault=false)
    public static final String reenterSection = "reenter";
    @ConfigSection(name="Looting & Banking", description="Loot the reward chest and bank between runs", position=8, closedByDefault=false)
    public static final String bankingSection = "banking";
    @ConfigSection(name="Prayer", description="Defensive flicking + per-style offensive prayers", position=1, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Weapons", description="Which combat styles to use + swap cadence", position=2, closedByDefault=false)
    public static final String weaponsSection = "weapons";
    @ConfigSection(name="Supplies", description="Food and prayer restore", position=3, closedByDefault=false)
    public static final String suppliesSection = "supplies";
    @ConfigSection(name="Dodging", description="Lava avoidance, tornado kiting and positioning", position=4, closedByDefault=false)
    public static final String dodgingSection = "dodging";
    @ConfigSection(name="Humanization / Anti-Ban", description="One slider for how human (1) vs how strong (5) the bot plays", position=5, closedByDefault=false)
    public static final String realismSection = "realism";
    @ConfigSection(name="Overlay Theme", description="Overlay title colours", position=6, closedByDefault=true)
    public static final String themeSection = "theme";

    @ConfigItem(keyName="targetKillCount", name="Stop After N Kills", description="Stop the plugin after this many Hunllef kills. 0 = run indefinitely.", section="general", position=0)
    @Range(min=0, max=1000)
    default public int targetKillCount() {
        return 0;
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Corrupted Gauntlet status panel over the chatbox.", section="general", position=1)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="autoReenter", name="Auto Re-Enter Gauntlet", description="After the fight ends (you die OR kill the Hunllef and haven't hit the stop-kill count), walk to the Gauntlet entrance and re-enter the CORRUPTED gauntlet automatically, looping indefinitely.", section="reenter", position=0)
    default public boolean autoReenter() {
        return true;
    }

    @ConfigItem(keyName="bankAfterRun", name="Loot + Bank Between Runs", description="After a run, open the reward chest then deposit everything at the bank before re-entering. Turn OFF to re-enter immediately without banking (the reward chest is always looted, as the gauntlet gate stays locked until it is).", section="banking", position=0)
    default public boolean bankAfterRun() {
        return true;
    }

    @ConfigItem(keyName="autoPrayerSwitch", name="Auto Prayer Switching", description="Automatically flick Protect from Magic / Missiles to match the Hunllef's incoming attack. Disable to manage overheads yourself.", section="prayer", position=0)
    default public boolean autoPrayerSwitch() {
        return true;
    }

    @ConfigItem(keyName="predictPrayerByCount", name="Predict Prayer (count attacks)", description="The Hunllef fires 4 of one style then 4 of the other. Pre-pray the next style by counting its attacks instead of only reacting to each shot, so the overhead is already correct when the next attack launches.", section="prayer", position=1)
    default public boolean predictPrayerByCount() {
        return true;
    }

    @ConfigItem(keyName="useOffensivePrayers", name="Use Offensive Prayers", description="Keep a damage prayer (Rigour/Augury/Piety) active, matched to the equipped weapon. OFF by default: on this server a damage prayer appears to be mutually exclusive with the protection overhead, so enabling it can drop Protect-from-X and let full hits through. Only turn on if you've confirmed both can be active together.", section="prayer", position=2)
    default public boolean useOffensivePrayers() {
        return false;
    }

    @ConfigItem(keyName="rangedPrayer", name="Ranged Offensive Prayer", description="Damage prayer used while the bow is equipped. Rigour (level 74) or Eagle Eye (44) as a budget option.", section="prayer", position=3)
    default public OffensivePrayer rangedPrayer() {
        return OffensivePrayer.RIGOUR;
    }

    @ConfigItem(keyName="magicPrayer", name="Magic Offensive Prayer", description="Damage prayer used while the staff is equipped. Augury (level 77) or Mystic Might (45) as a budget option.", section="prayer", position=4)
    default public OffensivePrayer magicPrayer() {
        return OffensivePrayer.AUGURY;
    }

    @ConfigItem(keyName="meleePrayer", name="Melee Offensive Prayer", description="Damage prayer used while the halberd is equipped. Piety (level 70) or Ultimate Strength as a budget option.", section="prayer", position=5)
    default public OffensivePrayer meleePrayer() {
        return OffensivePrayer.PIETY;
    }

    @ConfigItem(keyName="useRanged", name="Use Ranged (bow)", description="Allow the bot to use the bow. Part of the style rotation that bypasses the boss's overhead prayer.", section="weapons", position=0)
    default public boolean useRanged() {
        return true;
    }

    @ConfigItem(keyName="useMagic", name="Use Magic (staff)", description="Allow the bot to use the staff. Part of the style rotation that bypasses the boss's overhead prayer.", section="weapons", position=1)
    default public boolean useMagic() {
        return true;
    }

    @ConfigItem(keyName="useMelee", name="Use Melee (halberd)", description="Allow the bot to use the halberd (forces melee range). Part of the style rotation that bypasses the boss's overhead prayer. Turn off to fight at range only.", section="weapons", position=2)
    default public boolean useMelee() {
        return true;
    }

    @ConfigItem(keyName="weapon1", name="Weapon 1 (order)", description="First weapon in the swap-cycle ORDER. Only used if its style is enabled above.", section="weapons", position=3)
    default public HunllefWeapon weapon1() {
        return HunllefWeapon.RANGED;
    }

    @ConfigItem(keyName="weapon2", name="Weapon 2 (order)", description="Second weapon in the swap-cycle ORDER. Only used if its style is enabled above.", section="weapons", position=4)
    default public HunllefWeapon weapon2() {
        return HunllefWeapon.MAGIC;
    }

    @ConfigItem(keyName="useThirdWeapon", name="Use Third Weapon Slot", description="Include the third order-slot below in the cycle. (The actual styles used are still governed by Use Ranged/Magic/Melee above.)", section="weapons", position=5)
    default public boolean useThirdWeapon() {
        return true;
    }

    @ConfigItem(keyName="weapon3", name="Weapon 3 (order)", description="Third weapon in the swap-cycle ORDER (only used if 'Use Third Weapon Slot' is on AND its style is enabled above).", section="weapons", position=6)
    default public HunllefWeapon weapon3() {
        return HunllefWeapon.MELEE;
    }

    @ConfigItem(keyName="useOverheadPrayer", name="Read Boss Overhead Prayer (best)", description="Read the Hunllef's OVERHEAD protection prayer directly and never attack with the style it is protecting (protect-melee -> not halberd, protect-range -> not bow, protect-magic -> not staff). This is the most accurate way to bypass its protection. If your server doesn't expose the overhead, it falls back to the damage-stall detector below. Keep ON.", section="weapons", position=7)
    default public boolean useOverheadPrayer() {
        return true;
    }

    @ConfigItem(keyName="swapOnDamageStall", name="Swap When Damage Stalls (fallback)", description="Fallback used when the boss's overhead prayer can't be read: keep attacking with the current weapon while the boss's HP is dropping, and only swap when its HP stops dropping (it has prayed against your style). Also acts as a safety net even with overhead reading on. Keep ON.", section="weapons", position=8)
    default public boolean swapOnDamageStall() {
        return true;
    }

    @ConfigItem(keyName="stallAttacks", name="Stall Before Swap (attacks)", description="How many of your LANDED attacks with NO boss-HP drop before deciding the boss is praying your style and swapping. Lower = swaps sooner (less wasted DPS) but can mis-swap off a working weapon on a coarse HP bar; 4 is a safe default.", section="weapons", position=9)
    @Range(min=2, max=10)
    default public int stallAttacks() {
        return 4;
    }

    @ConfigItem(keyName="attacksPerSwap", name="Attacks Per Swap (fallback)", description="Fallback ONLY used when the boss's health bar can't be read (so 'Swap When Damage Stalls' has no signal): swap weapon after this many of your attacks. With a readable HP bar the stall detector drives swaps instead and this is ignored.", section="weapons", position=10)
    @Range(min=1, max=12)
    default public int attacksPerSwap() {
        return 6;
    }

    @ConfigItem(keyName="foodName", name="Food Item", description="Healing item name (partial match). Gauntlet default 'Paddlefish'.", section="supplies", position=0)
    default public String foodName() {
        return "Paddlefish";
    }

    @ConfigItem(keyName="eatAtHpPercent", name="Eat Below HP %", description="Eat food when HP falls below this percentage of maximum.", section="supplies", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatAtHpPercent() {
        return 50;
    }

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Prayer restore item name (partial match). Gauntlet default 'Egniol potion'.", section="supplies", position=2)
    default public String prayerPotionName() {
        return "Egniol potion";
    }

    @ConfigItem(keyName="prayerThresholdPct", name="Restore Below Prayer %", description="Drink a prayer restore when prayer points fall below this percentage of maximum.", section="supplies", position=3)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int prayerThresholdPct() {
        return 30;
    }

    @ConfigItem(keyName="stopWhenOutOfSupplies", name="Stop When Out Of Supplies", description="Stop the plugin when both food and prayer restores are exhausted (prevents dying with no sustain).", section="supplies", position=4)
    default public boolean stopWhenOutOfSupplies() {
        return true;
    }

    @ConfigItem(keyName="avoidLavaFloor", name="Avoid Lava Floor", description="Step off the damaging lava floor. The bot reads the floor tiles and moves to the nearest safe tile in attack range whenever the floor under you is (about to be) lava. Keep ON.", section="dodging", position=0)
    default public boolean avoidLavaFloor() {
        return true;
    }

    @ConfigItem(keyName="dodgeTornadoes", name="Dodge Tornadoes", description="Kite away from the chasing tornadoes (they damage you while on/next to your tile). On this server the tornadoes are the main damage \u2014 keep ON.", section="dodging", position=1)
    default public boolean dodgeTornadoes() {
        return true;
    }

    @ConfigItem(keyName="dodgeBossShots", name="Dodge Boss Shots (experimental)", description="EXPERIMENTAL \u2014 try this ONLY if the bot is dying to chip damage while standing on SAFE floor with prayer on. It makes the bot step 2 tiles off the Hunllef's tile-targeted ranged/magic shot before it lands (the shot samples your tile at launch, lands ~4 ticks later, 1-tile AoE). It is OFF by default because on this server the shots are believed to be PRAYER attacks that the correct overhead mitigates (so moving just loses DPS) \u2014 but if you watch the bot and see shots landing for ~12 THROUGH the correct prayer, turn this ON: moving off the tile then takes them to ~0. Costs ~1 attack per 5 ticks (~20% DPS) for near-total shot avoidance. A/B test it against prayer-only to see which your server actually needs.", section="dodging", position=2)
    default public boolean dodgeBossShots() {
        return false;
    }

    @ConfigItem(keyName="realismEnabled", name="Humanize (master)", description="Master switch for the whole humanisation system (the Performance Level slider below). Turn OFF to instantly restore frame-perfect play (instant swaps, no jitter/hesitation, never misses a prayer, kites tornadoes while attacking) regardless of the slider.", section="realism", position=0)
    default public boolean realismEnabled() {
        return true;
    }

    @ConfigItem(keyName="humanizationLevel", name="Performance Level (1=human \u00b7 5=max)", description="Drag to set how human the bot LOOKS vs how strong it plays \u2014 every humanisation setting moves together. Note: survival-critical play (dodging the boss's tile-shots, escaping lava, breaking a tornado swarm) runs at EVERY level, so even level 1 can win \u2014 this only changes how human it looks:\n\u2022 1 \u2014 Most human: slow ~0.6-1.8s swap reactions, heavy timing jitter, some hesitation, MISSES ~1 in 7 prayer flicks, and backs off a lone tornado about half the time (still attacks through the rest).\n\u2022 2-3 \u2014 Balanced: quicker and tighter, occasional missed prayer, increasingly fights through tornadoes.\n\u2022 4 \u2014 Strong: fast swaps, light jitter, rarely misses a prayer (default \u2014 good DPS, still humanised).\n\u2022 5 \u2014 Max performance: instant swaps, zero jitter/hesitation, never misses a prayer, pre-steps off lava a tick early, kites tornadoes while keeping full DPS.\nOnly takes effect while 'Humanize (master)' is ON.", section="realism", position=1)
    @Range(min=1, max=5)
    default public int humanizationLevel() {
        return 4;
    }

    @ConfigItem(keyName="oneActionPerTick", name="One Action Per Tick (Anti-stack)", description="Independent safety toggle (works at any Performance Level, even with Humanize off): never issue a discretionary attack/weapon-swap click on a game tick where the bot already stepped or flicked a prayer. Stops the humanly-impossible 'move + pray + attack in the same 600ms' stacking that stands out in click logs. Safe: attacking auto-repeats, so a deferred re-click costs no DPS. Turn OFF only for maximum speed.", section="realism", position=2)
    default public boolean oneActionPerTick() {
        return true;
    }

    @Alpha
    @ConfigItem(keyName="titleColorFighting", name="Fighting Colour", description="Overlay title colour while fighting the Hunllef.", section="theme", position=0)
    default public Color titleColorFighting() {
        return new Color(150, 110, 200);
    }

    @Alpha
    @ConfigItem(keyName="titleColorIdle", name="Idle Colour", description="Overlay title colour while waiting for the Hunllef.", section="theme", position=1)
    default public Color titleColorIdle() {
        return Color.WHITE;
    }

    @Alpha
    @ConfigItem(keyName="titleColorDone", name="Done/Dead Colour", description="Overlay title colour after a kill / when stopped.", section="theme", position=2)
    default public Color titleColorDone() {
        return Color.CYAN;
    }
}

