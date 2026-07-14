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
package net.runelite.client.plugins.svc.lizardmanshaman;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.DefensivePrayer;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;

@ConfigGroup(value="orionLizardmanShaman")
public interface LizardmanShamanConfig
extends Config {
    @ConfigSection(name="Prayer", description="Overhead and offensive prayer settings", position=0, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Food & Healing", description="Eating, antipoison and emergency settings", position=1, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Potions", description="Ranging and stamina potions", position=2, closedByDefault=false)
    public static final String potionSection = "potions";
    @ConfigSection(name="Jump Handling", description="Settings for dodging the shaman's leap attack", position=3, closedByDefault=false)
    public static final String jumpSection = "jump";
    @ConfigSection(name="Looting", description="Ground item pickup settings", position=4, closedByDefault=false)
    public static final String lootSection = "loot";
    @ConfigSection(name="Target", description="Shaman targeting settings", position=5, closedByDefault=false)
    public static final String targetSection = "target";
    @ConfigSection(name="Resupply / Banking", description="Banking loop + death walk-back", position=6, closedByDefault=false)
    public static final String resupplySection = "resupply";
    @ConfigSection(name="High Alchemy", description="Auto-alch items not worth keeping", position=7, closedByDefault=true)
    public static final String alchSection = "alch";
    @ConfigSection(name="Anti-Ban", description="Humanization and alerting", position=8, closedByDefault=false)
    public static final String antibanSection = "antiban";
    @ConfigSection(name="Display", description="Overlay options", position=9, closedByDefault=true)
    public static final String displaySection = "display";

    @ConfigItem(keyName="overheadPrayer", name="Base Overhead Prayer", description="Protection prayer for normal combat. Protect from Missiles is standard for ranged setups (the shaman's acid spit is their main attack). Switch to Protect from Magic if tanking without Shayzien armour.", section="prayer", position=0)
    default public DefensivePrayer overheadPrayer() {
        return DefensivePrayer.PROTECT_FROM_MISSILES;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage-boost prayer. Eagle Eye for ranged, Rigour if unlocked.", section="prayer", position=1)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.EAGLE_EYE;
    }

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Name of the prayer restore item to drink (e.g. 'Prayer potion', 'Super restore', 'Sanfew serum'). Leave blank to disable prayer restoring.", section="prayer", position=2)
    default public String prayerPotionName() {
        return "Prayer potion";
    }

    @ConfigItem(keyName="prayerThresholdPct", name="Restore Prayer Below %", description="Drink a prayer restore when prayer points drop below this percentage of your maximum.", section="prayer", position=3)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int prayerThresholdPct() {
        return 30;
    }

    @ConfigItem(keyName="foodName", name="Food Name", description="Item name to eat (partial match, e.g. 'shark' matches Shark, Manta ray, etc.).", section="food", position=0)
    default public String foodName() {
        return "shark";
    }

    @ConfigItem(keyName="eatAtHpPercent", name="Eat Below HP %", description="Eat when current HP falls below this percentage.", section="food", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatAtHpPercent() {
        return 55;
    }

    @ConfigItem(keyName="comboFoodName", name="Combo Food (optional)", description="Second food to eat alongside main food for emergency healing (e.g. Karambwan). Leave blank to disable.", section="food", position=2)
    default public String comboFoodName() {
        return "";
    }

    @ConfigItem(keyName="comboEatAtHpPercent", name="Combo Eat Below HP %", description="Eat the combo food when HP falls below this percentage (should be lower than main food threshold).", section="food", position=3)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int comboEatAtHpPercent() {
        return 30;
    }

    @ConfigItem(keyName="antipoisonName", name="Antipoison Item", description="Potion to drink when poisoned (e.g. 'Antidote++', 'Antivenom+', 'Super antipoison'). Leave blank to disable. Shamans' acid pools can poison you.", section="food", position=4)
    default public String antipoisonName() {
        return "Antidote++";
    }

    @ConfigItem(keyName="retreatWhenNoFood", name="Retreat When Out of Food", description="Failsafe: with no food left and HP below 35%, stop fighting and back away from the shamans until HP regenerates (or resupply immediately if the resupply loop is enabled) instead of fighting to the death.", section="food", position=5)
    default public boolean retreatWhenNoFood() {
        return true;
    }

    @ConfigItem(keyName="rangingPotionName", name="Ranging Potion Name", description="Potion to boost Ranged level (e.g. 'Ranging potion', 'Super ranging', 'Bastion potion'). Leave blank to disable. The bot re-drinks when the boost wears off (only while actually fighting, so doses aren't wasted between kills).", section="potions", position=0)
    default public String rangingPotionName() {
        return "Ranging potion";
    }

    @ConfigItem(keyName="drinkStamina", name="Drink Stamina Potions", description="Drink Stamina potions when run energy drops below the threshold. Useful when actively chasing shamans.", section="potions", position=1)
    default public boolean drinkStamina() {
        return false;
    }

    @ConfigItem(keyName="staminaThreshold", name="Stamina Below %", description="Drink stamina potion when run energy falls below this percentage.", section="potions", position=2)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int staminaThreshold() {
        return 30;
    }

    @ConfigItem(keyName="jumpEscapeDistance", name="Jump Escape Distance (tiles)", description="How many tiles to run from the shaman when it performs its leap attack. The AoE is 3x3 around your tile; 5 tiles safely clears both the splash zone and melee reach after landing.", section="jump", position=0)
    @Range(min=3, max=10)
    default public int jumpEscapeDistance() {
        return 5;
    }

    @ConfigItem(keyName="fleeFromSpawns", name="Move Away From Purple Spawns", description="When summoned spawns appear within 2 tiles, step away from them. They deal 5-10 area damage when they self-destruct. Minor quality-of-life \u2014 disable if it disrupts your attack rhythm.", section="jump", position=1)
    default public boolean fleeFromSpawns() {
        return true;
    }

    @ConfigItem(keyName="enableLooting", name="Enable Looting", description="Pick up items from the ground after kills.", section="loot", position=0)
    default public boolean enableLooting() {
        return true;
    }

    @ConfigItem(keyName="lootNames", name="Loot Item Names", description="Comma-separated item names to loot (e.g. 'Dragon warhammer,Xerician fabric,Dragon bones,Coins'). Leave blank to loot ALL nearby items.", section="loot", position=1)
    default public String lootNames() {
        return "Dragon warhammer,Xerician fabric,Dragon med helm,Dragon platelegs,Dragon plateskirt,Dragon 2h sword,Coins";
    }

    @ConfigItem(keyName="excludeLootNames", name="Exclude Item Names", description="Comma-separated item names to never loot (even if matched by name list or HA threshold).", section="loot", position=2)
    default public String excludeLootNames() {
        return "";
    }

    @ConfigItem(keyName="lootRadius", name="Loot Radius (tiles)", description="Tiles to search for loot around your current position.", section="loot", position=3)
    @Range(min=1, max=12)
    default public int lootRadius() {
        return 5;
    }

    @ConfigItem(keyName="lootMinValue", name="Loot Items Over HA Value (GP)", description="Also loot any item whose High Alch value is at or above this amount. Set to 0 to disable value-based looting.", section="loot", position=4)
    @Range(min=0, max=10000000)
    default public int lootMinValue() {
        return 5000;
    }

    @ConfigItem(keyName="eatToLoot", name="Eat Food to Make Inventory Space", description="When inventory is full and there are valuable items on the ground, eat food to free a slot.", section="loot", position=5)
    default public boolean eatToLoot() {
        return true;
    }

    @ConfigItem(keyName="buryBones", name="Bury Bones", description="Automatically bury looted bone/ashes items between fights (one per pass, never mid-combat).", section="loot", position=6)
    default public boolean buryBones() {
        return false;
    }

    @ConfigItem(keyName="searchRadius", name="Search Radius (tiles)", description="Tiles to search for Lizardman Shamans from your position.", section="target", position=0)
    @Range(min=1, max=30)
    default public int searchRadius() {
        return 12;
    }

    @ConfigItem(keyName="onlyIdle", name="Only Target Idle Shamans", description="Skip shamans already in combat with another player. Disable in single-combat areas where you're the only player.", section="target", position=1)
    default public boolean onlyIdle() {
        return false;
    }

    @ConfigItem(keyName="stopAfterKills", name="Stop After X Kills (0=never)", description="Automatically stop the bot after this many of YOUR kills (0 = run forever).", section="target", position=2)
    @Range(min=0, max=10000)
    default public int stopAfterKills() {
        return 0;
    }

    @ConfigItem(keyName="enableResupply", name="Enable Resupply Loop", description="When food or prayer restores run out (or after a death): loot drops \u2192 home teleport \u2192 Enchanted Geyser \u2192 bank Last-preset \u2192 gnome glider (searched destination) \u2192 walk back to the shamans. Requires a saved, unlocked bank preset containing your supplies.", section="resupply", position=0)
    default public boolean enableResupply() {
        return false;
    }

    @ConfigItem(keyName="gliderDestination", name="Glider Search Term", description="Text typed into the gnome glider's search box to reach the shaman area (prefix match, e.g. 'Lizardman'). Verify the exact destination name in-game once.", section="resupply", position=1)
    default public String gliderDestination() {
        return "Lizardman";
    }

    @ConfigItem(keyName="resupplyLootNames", name="Loot Before Banking", description="Comma-separated item names to pick up before teleporting out. Leave blank to use the Looting section's list. If 'Disable Looting' is off but you still want to loot before banking, list items here.", section="resupply", position=2)
    default public String resupplyLootNames() {
        return "";
    }

    @ConfigItem(keyName="requiredItems", name="Skip Resupply If Stocked", description="Comma-separated 'item name:min count' pairs (e.g. 'shark:5,prayer potion:2,ranging potion:1'). If ALL listed items are present in the required quantities, skip banking and do another kill immediately.", section="resupply", position=3)
    default public String requiredItems() {
        return "";
    }

    @ConfigItem(keyName="enableHighAlch", name="Enable High Alchemy", description="Pick up and cast High Alchemy on items that meet the criteria below. Items in Loot Names are always kept and excluded from alching. Alching only runs while no shaman is attacking you.", section="alch", position=0)
    default public boolean enableHighAlch() {
        return false;
    }

    @ConfigItem(keyName="alchMinValue", name="Alch Items Over HA Value (GP)", description="Pick up and High Alch any item whose High Alchemy value is at or above this. Items that also meet the Loot Min Value threshold are kept, not alched. Set 0 to disable value-based alching.", section="alch", position=1)
    @Range(min=0, max=10000000)
    default public int alchMinValue() {
        return 1000;
    }

    @ConfigItem(keyName="alchItemNames", name="Always Alch Item Names", description="Comma-separated item names to always pick up and High Alch regardless of value (e.g. Rune med helm,Adamant platebody). Items in Loot Names are always excluded even if listed here.", section="alch", position=2)
    default public String alchItemNames() {
        return "";
    }

    @ConfigItem(keyName="alchExcludeNames", name="Never Alch Names (Extra)", description="Comma-separated item names to never pick up or alch, even if they exceed the value threshold. Loot Names are automatically excluded \u2014 use this for additional exclusions.", section="alch", position=3)
    default public String alchExcludeNames() {
        return "";
    }

    @ConfigItem(keyName="verifyAlchRunes", name="Check Rune Requirements", description="Verify that Fire runes (x5) and a Nature rune (x1) are in inventory before casting. Disable if using a fire staff.", section="alch", position=4)
    default public boolean verifyAlchRunes() {
        return true;
    }

    @ConfigItem(keyName="eatToAlch", name="Eat Food to Pick Up Alch Item", description="When inventory is full and an alch-eligible item is on the ground, eat a food to free a slot and pick it up.", section="alch", position=5)
    default public boolean eatToAlch() {
        return false;
    }

    @ConfigItem(keyName="dropToAlch", name="Drop Food to Pick Up Alch Item", description="When inventory is full, drop a food item to pick up and alch a ground item, then recover the dropped food afterwards.", section="alch", position=6)
    default public boolean dropToAlch() {
        return false;
    }

    @ConfigItem(keyName="microBreaks", name="Take Short Breaks", description="Pause on a randomized schedule (every 7-21 min, mostly 15-75 s, occasionally a few minutes). Breaks only start between fights; eating, antipoison and jump-dodging stay active, and being attacked ends the break early.", section="antiban", position=0)
    default public boolean microBreaks() {
        return true;
    }

    @ConfigItem(keyName="discordAlerts", name="Discord Alerts", description="Post an alert to the admin Discord channel on a death, a hard-stuck resupply route, or when the kill target is reached \u2014 so an unattended account never fails silently.", section="antiban", position=1)
    default public boolean discordAlerts() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Lizardman Shaman bot status panel on screen.", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

