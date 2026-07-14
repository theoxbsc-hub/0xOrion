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
package net.runelite.client.plugins.svc.vorkath;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.vorkath.VorkathOverheadPrayer;

@ConfigGroup(value="orionVorkath")
public interface VorkathConfig
extends Config {
    @ConfigSection(name="Prayer", description="Overhead and offensive prayer settings", position=0, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Food & Healing", description="Eating settings", position=1, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Antifire", description="Antifire potion settings", position=2, closedByDefault=false)
    public static final String antifireSection = "antifire";
    @ConfigSection(name="Antivenom", description="Antivenom/antipoison potion settings", position=3, closedByDefault=false)
    public static final String antivenomSection = "antivenom";
    @ConfigSection(name="Special Attacks", description="Zombie spawn and acid phase settings", position=4, closedByDefault=false)
    public static final String specialSection = "special";
    @ConfigSection(name="Fireball Dodge", description="Instant-kill fireball avoidance settings", position=5, closedByDefault=false)
    public static final String fireballSection = "fireball";
    @ConfigSection(name="Resupply / Banking", description="Post-kill banking loop: loot drops \u2192 home teleport \u2192 Rellekka bank \u2192 Gnome Glider \u2192 Fremennik Boat \u2192 enter lair", position=7, closedByDefault=false)
    public static final String resupplySection = "resupply";
    @ConfigSection(name="Display", description="Overlay options", position=8, closedByDefault=true)
    public static final String displaySection = "display";

    @ConfigItem(keyName="overheadPrayer", name="Overhead Prayer", description="Protection prayer to keep active. Use Magic for most setups (ranged, blowpipe). Use Missiles if tanking with a dragonfire shield.", section="prayer", position=0)
    default public VorkathOverheadPrayer overheadPrayer() {
        return VorkathOverheadPrayer.PROTECT_FROM_MAGIC;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage-boost prayer. Eagle Eye for ranged, Rigour if unlocked, Piety for melee.", section="prayer", position=1)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.EAGLE_EYE;
    }

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Name of prayer restore item in inventory (e.g. Prayer potion, Super restore). Leave blank to disable.", section="prayer", position=2)
    default public String prayerPotionName() {
        return "Prayer potion";
    }

    @ConfigItem(keyName="prayerThresholdPct", name="Restore Prayer Below %", description="Drink prayer restore when prayer points drop below this percentage of max.", section="prayer", position=3)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int prayerThresholdPct() {
        return 30;
    }

    @ConfigItem(keyName="prayFlick", name="Prayer Flick (save prayer)", description="Instead of keeping overhead protection on continuously, flick it on for 2 ticks when Vorkath attacks, then off. Saves prayer points.", section="prayer", position=4)
    default public boolean prayFlick() {
        return false;
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

    @ConfigItem(keyName="antifireName", name="Antifire Potion Name", description="Potion name to drink for dragonfire protection (e.g. Super antifire, Antifire). Leave blank to disable.", section="antifire", position=0)
    default public String antifireName() {
        return "Super antifire";
    }

    @ConfigItem(keyName="antifireDurationSeconds", name="Potion Duration (seconds)", description="How long the antifire potion lasts in seconds. Super antifire = 180, Antifire = 360. Bot re-drinks 30 seconds before expiry.", section="antifire", position=1)
    @Range(min=60, max=600)
    default public int antifireDurationSeconds() {
        return 180;
    }

    @ConfigItem(keyName="antivenomName", name="Antivenom Potion Name", description="Potion name to drink for venom/poison protection (e.g. Antivenom+, Antivenom, Anti-poison). Leave blank to disable.", section="antivenom", position=0)
    default public String antivenomName() {
        return "";
    }

    @ConfigItem(keyName="antivenomDurationSeconds", name="Potion Duration (seconds)", description="How long the antivenom potion lasts in seconds. Antivenom+ = 720, Antivenom = 300. Bot re-drinks 30 seconds before expiry.", section="antivenom", position=1)
    @Range(min=30, max=1200)
    default public int antivenomDurationSeconds() {
        return 720;
    }

    @ConfigItem(keyName="useCrumbleUndead", name="Use Crumble Undead on Zombie", description="Cast Crumble Undead to instantly kill the Zombified Spawn. Required for melee setups (frozen players can't reach the zombie). Disable to use a standard weapon attack instead (works for ranged).", section="special", position=0)
    default public boolean useCrumbleUndead() {
        return true;
    }

    @ConfigItem(keyName="wooxWalk", name="Woox Walk During Acid Phase", description="Advanced: attack Vorkath while walking during the acid phase for extra DPS. Only works well with fast weapons (blowpipe, crossbow). Disable for safe, prayer-only dodging.", section="special", position=1)
    default public boolean wooxWalk() {
        return false;
    }

    @ConfigItem(keyName="attackRange", name="Weapon Attack Range", description="How many tiles your weapon can attack from. Crossbow = 7, Shortbow = 7, Blowpipe = 5. Lets the bot keep attacking during and after the fireball dodge without stepping onto the landing tile.", section="fireball", position=0)
    @Range(min=4, max=10)
    default public int attackRange() {
        return 7;
    }

    @ConfigItem(keyName="enableResupply", name="Enable Resupply Loop", description="After each kill: loot drops, bank at Rellekka, and travel back via Gnome Glider + Fremennik Boat. Disable to keep fighting without banking.", section="resupply", position=0)
    default public boolean enableResupply() {
        return false;
    }

    @ConfigItem(keyName="lootItems", name="Loot Item Names", description="Comma-separated item names to pick up after the kill (e.g. 'draconic visage,dragonbone necklace,superior dragon bones'). Leave blank to loot ALL nearby items.", section="resupply", position=1)
    default public String lootItems() {
        return "";
    }

    @ConfigItem(keyName="eatWhileLooting", name="Eat to Free Inventory Space", description="When inventory is full, eat food to clear a slot so loot can be picked up.", section="resupply", position=2)
    default public boolean eatWhileLooting() {
        return false;
    }

    @ConfigItem(keyName="requiredItems", name="Skip Resupply If Stocked", description="Comma-separated 'item name:minimum count' pairs (e.g. 'shark:5,prayer potion:2,super antifire:1'). If ALL listed items are present in at least those quantities, the bot skips banking and does another kill immediately.", section="resupply", position=3)
    default public String requiredItems() {
        return "";
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Vorkath bot status panel on screen.", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

