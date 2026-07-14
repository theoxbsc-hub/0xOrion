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
package net.runelite.client.plugins.svc.slayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.CombatPotion;
import net.runelite.client.plugins.svc.combat.DefensivePrayer;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.combat.PrayerFlickMode;
import net.runelite.client.plugins.svc.slayer.SlayerMaster;

@ConfigGroup(value="orionSlayerBot")
public interface SlayerBotConfig
extends Config {
    @ConfigSection(name="General", description="General slayer settings", position=0, closedByDefault=false)
    public static final String generalSection = "general";
    @ConfigSection(name="Graveyard Slayer", description="Graveyard Slayer mode (RSPS): get tasks at the graveyard via the ::graveyard teleport, pay the per-task GP cost, and fight there instead of using the normal slayer master + Gnome Glider.", position=1, closedByDefault=false)
    public static final String graveyardSection = "graveyard";
    @ConfigSection(name="Combat", description="Attack, targeting and stat-boost settings", position=2, closedByDefault=false)
    public static final String combatSection = "combat";
    @ConfigSection(name="Food & Healing", description="Eating and healing. Food is auto-detected \u2014 the bot recognises the common OSRS foods (Shark, Manta ray, Anglerfish, Dark crab, Monkfish, Swordfish, Lobster, Tuna, Salmon, Karambwan, Sea turtle, Bass, Trout) automatically.", position=3, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Prayer", description="Prayer and flicking. Restore potions are auto-detected (Super restore, Prayer potion, Sanfew serum, Prayer mix).", position=4, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Looting", description="Ground-item looting. Looting happens between kills \u2014 the bot finishes a fight and clears the drop pile before engaging the next monster, so it never skates back and forth between loot and target.", position=5, closedByDefault=false)
    public static final String lootSection = "loot";
    @ConfigSection(name="High Alchemy", description="Pick up and High Alch items by value or name. The ONLY items kept instead of alched are those in Keep Item Names (Looting section) \u2014 everything else that's picked up is alched. Alching runs between kills, never mid-attack.", position=6, closedByDefault=true)
    public static final String alchSection = "alch";
    @ConfigSection(name="Anti-Ban", description="Humanization. The bot delivers its actions through the client's real mouse pipeline (genuine cursor moves + clicks, minimap walking, per-account timing) instead of bare packets. These settings tune the optional extras on top.", position=7, closedByDefault=false)
    public static final String antibanSection = "antiban";
    @ConfigSection(name="Display", description="On-screen overlay options", position=8, closedByDefault=true)
    public static final String displaySection = "display";
    @ConfigSection(name="Reliability", description="Stall watchdog and give-up behaviour. The watchdog catches the bot looping without progress (no state change, kill, task, or bank confirm) and escalates: re-home and resupply, then after several failed recoveries either stop or keep retrying.", position=9, closedByDefault=true)
    public static final String reliabilitySection = "reliability";

    @ConfigItem(keyName="slayerMaster", name="Slayer Master", description="Which slayer master to get tasks from.", section="general", position=0)
    default public SlayerMaster slayerMaster() {
        return SlayerMaster.VANNAKA;
    }

    @ConfigItem(keyName="stopAfterTasks", name="Stop After X Tasks (0 = never)", description="Stop the bot after completing this many slayer tasks. 0 = run forever.", section="general", position=1)
    @Range(min=0, max=1000)
    default public int stopAfterTasks() {
        return 0;
    }

    @ConfigItem(keyName="graveyardMode", name="Enable Graveyard Mode", description="When on, the bot gets tasks at the graveyard via the ::graveyard teleport and fights there. When off, it uses the normal slayer master + Gnome Glider flow.", section="graveyard", position=0)
    default public boolean graveyardMode() {
        return false;
    }

    @ConfigItem(keyName="graveyardTaskCost", name="Task Cost (GP)", description="Coins required in the inventory before getting a graveyard assignment. The bot resupplies via the bank preset when below this.", section="graveyard", position=1)
    @Range(min=0, max=2000000000)
    default public int graveyardTaskCost() {
        return 1500000;
    }

    @ConfigItem(keyName="onlyAttackIdle", name="Only Attack Idle NPCs", description="Skip NPCs already in combat with another player (detected via interaction, health bar, or a server 'already in combat' / 'belongs to' refusal). Leave on for crowded areas.", section="combat", position=0)
    default public boolean onlyAttackIdle() {
        return true;
    }

    @ConfigItem(keyName="enableSpec", name="Use Special Attack", description="Automatically use the special attack when energy reaches the threshold. Only fires when the equipped weapon actually has a special attack \u2014 a weapon with no spec (e.g. a rune sword) is left alone.", section="combat", position=1)
    default public boolean enableSpec() {
        return false;
    }

    @ConfigItem(keyName="specAt", name="Spec At Energy %", description="Use the special attack when spec energy is at or above this percentage.", section="combat", position=2)
    @Range(min=10, max=100)
    @Units(value="%")
    default public int specAt() {
        return 100;
    }

    @ConfigItem(keyName="combatPotion", name="Combat Boost Potion", description="Stat-boosting potion to drink during combat (auto-detected by name). Set to None to disable.", section="combat", position=3)
    default public CombatPotion combatPotion() {
        return CombatPotion.NONE;
    }

    @ConfigItem(keyName="redrinkBelowBoost", name="Re-drink When Boost Expires", description="Drink another dose when the stat boost fully wears off (instead of only once per task).", section="combat", position=4)
    default public boolean redrinkBelowBoost() {
        return true;
    }

    @ConfigItem(keyName="eatFood", name="Eat Food", description="Automatically eat food when HP falls below the threshold. Common OSRS foods are recognised automatically.", section="food", position=0)
    default public boolean eatFood() {
        return true;
    }

    @ConfigItem(keyName="eatAtHpPercent", name="Eat Below HP %", description="Eat when HP falls below this percentage.", section="food", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatAtHpPercent() {
        return 50;
    }

    @ConfigItem(keyName="bankWhenFoodLeft", name="Bank When Food Below", description="Return to bank when fewer than this many food items remain in the inventory.", section="food", position=2)
    @Range(min=0, max=27)
    default public int bankWhenFoodLeft() {
        return 3;
    }

    @ConfigItem(keyName="eatFoodForLoot", name="Eat Food to Free Loot Space", description="Eat a food item to free an inventory slot when lootable items are on the ground but the inventory is full.", section="food", position=3)
    default public boolean eatFoodForLoot() {
        return false;
    }

    @ConfigItem(keyName="enablePrayer", name="Enable Prayer", description="Use prayers during combat.", section="prayer", position=0)
    default public boolean enablePrayer() {
        return false;
    }

    @ConfigItem(keyName="defensivePrayer", name="Protection Prayer", description="Protection prayer to use against the monster's attacks.", section="prayer", position=1)
    default public DefensivePrayer defensivePrayer() {
        return DefensivePrayer.NONE;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Prayer to boost damage/accuracy.", section="prayer", position=2)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.NONE;
    }

    @ConfigItem(keyName="flickMode", name="Flick Mode", description="Always On keeps prayers on; Defensive Flick activates protection only on NPC attacks; Offensive Flick flicks the boost prayer on your swings; Full Flick does both.", section="prayer", position=3)
    default public PrayerFlickMode flickMode() {
        return PrayerFlickMode.ALWAYS_ON;
    }

    @ConfigItem(keyName="quickPrayers", name="Use Quick Prayers", description="Activate quick prayers instead of toggling individual prayers (only used in Always On mode).", section="prayer", position=4)
    default public boolean quickPrayers() {
        return false;
    }

    @ConfigItem(keyName="playerWeaponSpeedTicks", name="Your Weapon Speed (ticks)", description="Your weapon's attack speed in ticks \u2014 used for offensive flick timing (e.g. scimitar = 4, whip = 4, 2h = 7).", section="prayer", position=5)
    @Range(min=0, max=10)
    default public int playerWeaponSpeedTicks() {
        return 4;
    }

    @ConfigItem(keyName="drinkPrayerAt", name="Drink Prayer Pot Below", description="Drink a prayer restore when prayer points fall below this value.", section="prayer", position=6)
    @Range(min=1, max=99)
    @Units(value="pt")
    default public int drinkPrayerAt() {
        return 20;
    }

    @ConfigItem(keyName="enableLooting", name="Enable Looting", description="Pick up ground items after kills.", section="loot", position=0)
    default public boolean enableLooting() {
        return true;
    }

    @ConfigItem(keyName="lootNames", name="Keep Item Names (Never Alch)", description="Items to pick up and KEEP (banked, never alched). When High Alchemy is on, this is the ONLY thing that stops an item being alched \u2014 every other item that gets picked up is alched. Comma-separated, case-insensitive substring match (e.g. Whip,Visage,Casket).", section="loot", position=1)
    default public String lootNames() {
        return "Bones,Coins";
    }

    @ConfigItem(keyName="lootMinValue", name="Pick Up Items Over Value (GP)", description="Also pick up any item worth at least this much (High Alchemy value; coin piles counted by total amount). With High Alchemy OFF these are kept; with High Alchemy ON they're alched unless listed in Keep Item Names. Set 0 to disable.", section="loot", position=2)
    @Range(min=0, max=10000000)
    default public int lootMinValue() {
        return 0;
    }

    @ConfigItem(keyName="excludeLootNames", name="Never Pick Up Names", description="Items to never pick up or touch at all \u2014 overrides the keep list, value thresholds and alching. Comma-separated (e.g. Dragonhide,Dragon bones).", section="loot", position=3)
    default public String excludeLootNames() {
        return "";
    }

    @ConfigItem(keyName="lootRadius", name="Loot Radius (tiles)", description="How far to search for loot around the player.", section="loot", position=4)
    @Range(min=1, max=10)
    default public int lootRadius() {
        return 5;
    }

    @ConfigItem(keyName="buryBones", name="Bury Bones", description="Automatically pick up and bury (or scatter) bones/ashes dropped by kills.", section="loot", position=5)
    default public boolean buryBones() {
        return false;
    }

    @ConfigItem(keyName="boneTypes", name="Bone Types", description="Comma-separated bone names to bury (e.g. Bones,Big bones). Leave blank to bury any bones/ashes.", section="loot", position=6)
    default public String boneTypes() {
        return "Bones";
    }

    @ConfigItem(keyName="lootOnlyWhenSafe", name="Loot Only When Safe", description="Only pick up loot / bury bones between kills (never while still in combat). Turn off to loot mid-fight.", section="loot", position=7)
    default public boolean lootOnlyWhenSafe() {
        return true;
    }

    @ConfigItem(keyName="enableHighAlch", name="Enable High Alchemy", description="Pick up and cast High Alchemy on items that meet the criteria below. The only items kept instead of alched are those in Keep Item Names (Looting section); every other item picked up is alched.", section="alch", position=0)
    default public boolean enableHighAlch() {
        return false;
    }

    @ConfigItem(keyName="alchMinValue", name="Alch Items Over Value (GP)", description="Pick up and High Alch any item whose High Alchemy value is at or above this. Items in Keep Item Names are kept instead. Set 0 to disable value-based alching.", section="alch", position=1)
    @Range(min=0, max=10000000)
    default public int alchMinValue() {
        return 1000;
    }

    @ConfigItem(keyName="alchItemNames", name="Always Alch Item Names", description="Comma-separated item names to always pick up and High Alch regardless of value (e.g. Rune med helm,Adamant platebody). Items in Keep Item Names are still kept \u2014 that list always wins.", section="alch", position=2)
    default public String alchItemNames() {
        return "";
    }

    @ConfigItem(keyName="alchExcludeNames", name="Never Alch Names (Extra)", description="Comma-separated item names to never pick up or alch, even if over the value threshold. Use for extra exclusions on top of Keep Item Names (e.g. Uncut diamond).", section="alch", position=3)
    default public String alchExcludeNames() {
        return "";
    }

    @ConfigItem(keyName="verifyAlchRunes", name="Check Rune Requirements", description="Verify that Fire runes (x5) and a Nature rune (x1) are in the inventory before casting. Disable if using a fire staff that supplies the Fire runes automatically.", section="alch", position=4)
    default public boolean verifyAlchRunes() {
        return true;
    }

    @ConfigItem(keyName="alchOnlyWhenSafe", name="Alch Only When Safe", description="Only cast High Alchemy between kills (never while still in combat), so a mid-fight alch can't interrupt attacking. Turn off to alch as soon as an item is held.", section="alch", position=5)
    default public boolean alchOnlyWhenSafe() {
        return true;
    }

    @ConfigItem(keyName="microBreaks", name="Take Short Breaks", description="Occasionally pause for a short 'looked away' break (15-90s) \u2014 only during safe lulls (never while a monster is attacking you, and never mid-teleport/bank). Spaced 8-24 minutes apart.", section="antiban", position=0)
    default public boolean microBreaks() {
        return true;
    }

    @ConfigItem(keyName="debugOverlay", name="Show Status Overlay", description="Display the slayer bot status and task info on screen.", section="display", position=0)
    default public boolean debugOverlay() {
        return true;
    }

    @ConfigItem(keyName="watchdogStallSeconds", name="Watchdog Stall (seconds)", description="No-forward-progress duration before the watchdog escalates (re-home + resupply). Set 0 to disable the watchdog entirely.", section="reliability", position=0)
    @Range(min=0, max=1800)
    @Units(value="s")
    default public int watchdogStallSeconds() {
        return 180;
    }

    @ConfigItem(keyName="watchdogMaxTrips", name="Watchdog Max Recoveries", description="Re-home/recovery attempts before giving up; each recovery gets a fresh stall window.", section="reliability", position=1)
    @Range(min=1, max=10)
    default public int watchdogMaxTrips() {
        return 3;
    }

    @ConfigItem(keyName="watchdogStopOnGiveUp", name="Stop When Giving Up", description="On give-up (watchdog, or a not-found lookup like bank booth / graveyard master that never resolves), STOP the bot for investigation. Off = keep retrying / re-homing indefinitely for unattended running.", section="reliability", position=2)
    default public boolean watchdogStopOnGiveUp() {
        return true;
    }
}

