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
package net.runelite.client.plugins.svc.saradomin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.saradomin.SaradominOverheadPrayer;

@ConfigGroup(value="orionSaradomin")
public interface SaradominConfig
extends Config {
    @ConfigSection(name="Prayer", description="Overhead and offensive prayer settings", position=0, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Food & Healing", description="Eating settings", position=1, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Combat", description="Attack range settings", position=2, closedByDefault=false)
    public static final String combatSection = "combat";
    @ConfigSection(name="Resupply / Banking", description="Post-kill banking: loot \u2192 home teleport \u2192 rejuvenate \u2192 bank preset \u2192 gnome glider \u2192 Saradomin door", position=3, closedByDefault=false)
    public static final String resupplySection = "resupply";
    @ConfigSection(name="Display", description="Overlay options", position=4, closedByDefault=true)
    public static final String displaySection = "display";

    @ConfigItem(keyName="overheadPrayer", name="Overhead Prayer", description="Protection prayer to keep active during the fight. Protect from Magic is recommended (blocks Growler and Zilyana's magic).", section="prayer", position=0)
    default public SaradominOverheadPrayer overheadPrayer() {
        return SaradominOverheadPrayer.PROTECT_FROM_MAGIC;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage-boost prayer. Eagle Eye for ranged, Rigour if unlocked.", section="prayer", position=1)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.EAGLE_EYE;
    }

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Name of prayer restore item (e.g. Prayer potion, Super restore). Leave blank to disable.", section="prayer", position=2)
    default public String prayerPotionName() {
        return "Prayer potion";
    }

    @ConfigItem(keyName="prayerThresholdPct", name="Restore Prayer Below %", description="Drink prayer restore when prayer points drop below this percentage of max.", section="prayer", position=3)
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
        return 50;
    }

    @ConfigItem(keyName="attackRange", name="Ranged Attack Reach", description="Max tiles your weapon can attack Zilyana from. Crossbow/Blowpipe = 7-8. Used to decide when to attack vs. run.", section="combat", position=0)
    @Range(min=4, max=10)
    default public int attackRange() {
        return 8;
    }

    @ConfigItem(keyName="enableResupply", name="Enable Resupply Loop", description="After each kill: loot drops, bank via Rellekka, and travel back through the Saradomin door. Disable to keep fighting using only altar prayer restores.", section="resupply", position=0)
    default public boolean enableResupply() {
        return false;
    }

    @ConfigItem(keyName="lootItems", name="Loot Item Names", description="Comma-separated item names to pick up after the kill (e.g. 'saradomin sword,armadyl crossbow'). Leave blank to loot ALL nearby items.", section="resupply", position=1)
    default public String lootItems() {
        return "";
    }

    @ConfigItem(keyName="eatWhileLooting", name="Eat to Free Inventory Space", description="When inventory is full during looting, eat food to clear a slot for more loot.", section="resupply", position=2)
    default public boolean eatWhileLooting() {
        return true;
    }

    @ConfigItem(keyName="requiredItems", name="Skip Resupply If Stocked", description="Comma-separated 'item name:minimum count' pairs (e.g. 'shark:5,prayer potion:2,ranging potion:1'). If ALL listed items meet their minimums, skip banking and use the altar instead.", section="resupply", position=3)
    default public String requiredItems() {
        return "";
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Saradomin bot status panel on screen.", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

