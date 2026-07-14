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
package net.runelite.client.plugins.svc.jad;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;

@ConfigGroup(value="orionJadBot")
public interface JadConfig
extends Config {
    @ConfigSection(name="Prayer", description="Protection prayer auto-switching, prayer restore, and offensive prayer settings", position=0, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Combat", description="Food, HP threshold, and positioning settings", position=1, closedByDefault=false)
    public static final String combatSection = "combat";
    @ConfigSection(name="Resupply / Banking", description="Automatic banking between attempts using the bank chest beside the cave entrance, loading a bank preset before re-entering.", position=2, closedByDefault=false)
    public static final String resupplySection = "resupply";
    @ConfigSection(name="Display", description="Overlay settings", position=3, closedByDefault=false)
    public static final String displaySection = "display";

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Item name to drink when prayer points run low (e.g. 'Prayer potion', 'Super restore'). Leave blank to disable.", section="prayer", position=0)
    default public String prayerPotionName() {
        return "Prayer potion";
    }

    @ConfigItem(keyName="prayerThresholdPct", name="Restore Prayer Below %", description="Drink the prayer restore item when prayer points drop below this percentage of your maximum.", section="prayer", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int prayerThresholdPct() {
        return 30;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage-boost prayer to keep active while fighting. Eagle Eye for most ranged setups; Rigour if unlocked.", section="prayer", position=2)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.EAGLE_EYE;
    }

    @ConfigItem(keyName="foodName", name="Food Name", description="Partial item name of food to eat (e.g. 'shark', 'monkfish'). Used for the pre-fight inventory space check and eating when HP drops below the threshold.", section="combat", position=0)
    default public String foodName() {
        return "shark";
    }

    @ConfigItem(keyName="eatBelowHpPct", name="Eat Below HP %", description="Eat food when current HP falls below this percentage of maximum HP.", section="combat", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatBelowHpPct() {
        return 50;
    }

    @ConfigItem(keyName="attackRange", name="Attack Range (tiles)", description="Max tiles your ranged weapon can attack from. Crossbow / Longbow = 7-10, Blowpipe = 5.", section="combat", position=2)
    @Range(min=3, max=15)
    default public int attackRange() {
        return 7;
    }

    @ConfigItem(keyName="smartHealerAggro", name="Smart Healer Aggro", description="Pull healers OFF Jad instead of killing them: attack any healer that is healing Jad until it aggros onto you, then resume Jad immediately. Re-pulls any healer that drifts back to Jad. Disable to fully kill every healer before resuming Jad.", section="combat", position=3)
    default public boolean smartHealerAggro() {
        return true;
    }

    @ConfigItem(keyName="healerJadRadius", name="Healer Jad Radius (tiles)", description="How close (in tiles) a healer must be to Jad to be treated as 'healing Jad' while it has no clear target (e.g. mid-path). A healer directly attached to Jad is always pulled regardless of this value. Only used when Smart Healer Aggro is enabled.", section="combat", position=4)
    @Range(min=2, max=15)
    default public int healerJadRadius() {
        return 6;
    }

    @ConfigItem(keyName="safeDistance", name="Min Safe Distance from Jad", description="Step back if closer than this many tiles to Jad. Jad is 5x5 tiles; distance 2 keeps you clear of melee range.", section="combat", position=5)
    @Range(min=1, max=10)
    default public int safeDistance() {
        return 2;
    }

    @ConfigItem(keyName="enableResupply", name="Enable Resupply Loop", description="After each death or completed Jad: check supplies, and if below minimum, open the nearby bank chest and load a bank preset before re-entering.", section="resupply", position=0)
    default public boolean enableResupply() {
        return false;
    }

    @ConfigItem(keyName="requiredItems", name="Minimum Supplies", description="Comma-separated 'item name:minimum count' pairs (e.g. 'shark:5,prayer potion:2'). If ALL listed items are present in at least those quantities, the bot skips banking. Leave blank to always bank.", section="resupply", position=1)
    default public String requiredItems() {
        return "";
    }

    @ConfigItem(keyName="showOverlay", name="Show Overlay", description="Display the Orion Jad Bot status panel.", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

