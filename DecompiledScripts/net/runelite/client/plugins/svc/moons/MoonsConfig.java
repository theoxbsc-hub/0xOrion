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
package net.runelite.client.plugins.svc.moons;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.CombatPotion;
import net.runelite.client.plugins.svc.combat.DefensivePrayer;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.moons.TargetMoon;

@ConfigGroup(value="orionMoons")
public interface MoonsConfig
extends Config {
    @ConfigSection(name="Boss Selection", description="Which Moon boss to farm", position=0, closedByDefault=false)
    public static final String bossSection = "boss";
    @ConfigSection(name="Prayer", description="Protection prayer per boss (on this server it DOES block the boss's escalating melee combo) + offensive prayer + restore.", position=1, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Food & Healing", description="Eating settings", position=2, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Potions", description="Combat boost and antipoison settings", position=3, closedByDefault=false)
    public static final String potionSection = "potions";
    @ConfigSection(name="Mechanics", description="Behaviour toggles for the boss mechanics. The actual NPC/object IDs are hardcoded.", position=4, closedByDefault=false)
    public static final String mechanicsSection = "mechanics";
    @ConfigSection(name="Travel & Resupply", description="After each kill the bot loots, then farms in-room (default) or \u2014 when supplies run low \u2014 runs the resupply route: home teleport \u2192 bank Last-preset (with manual failsafe) \u2192 glider 'Previous' \u2192 re-enter the mural.", position=5, closedByDefault=false)
    public static final String travelSection = "travel";
    @ConfigSection(name="Display", description="Overlay options", position=6, closedByDefault=false)
    public static final String displaySection = "display";

    @ConfigItem(keyName="targetMoon", name="Target Moon", description="Which boss to farm. The bot enters the selected moon's door, fights it, then resupplies and repeats.", section="boss", position=0)
    default public TargetMoon targetMoon() {
        return TargetMoon.BLOOD;
    }

    @ConfigItem(keyName="bossName", name="Boss Name Override", description="Substring of the boss NPC name to attack (e.g. 'Blood Moon'). Leave blank to use the default name for the selected moon (the bot matches by ID first anyway).", section="boss", position=1)
    default public String bossName() {
        return "";
    }

    @ConfigItem(keyName="bloodPrayer", name="Blood Moon Protection", description="Protection prayer kept active vs Blood Moon. Its escalating combo is melee here, so Protect from Melee (default) blocks it.", section="prayer", position=0)
    default public DefensivePrayer bloodPrayer() {
        return DefensivePrayer.PROTECT_FROM_MELEE;
    }

    @ConfigItem(keyName="bluePrayer", name="Blue Moon Protection", description="Protection prayer kept active vs Blue Moon. The Blue Moon's standard attack is the same escalating melee combo as Blood Moon, so Protect from Melee blocks it.", section="prayer", position=1)
    default public DefensivePrayer bluePrayer() {
        return DefensivePrayer.PROTECT_FROM_MELEE;
    }

    @ConfigItem(keyName="eclipsePrayer", name="Eclipse Moon Protection", description="Protection prayer kept active vs Eclipse Moon (Missiles by default \u2014 verify on your server).", section="prayer", position=2)
    default public DefensivePrayer eclipsePrayer() {
        return DefensivePrayer.PROTECT_FROM_MISSILES;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage-boost prayer kept active during the fight (Piety for melee). Set to None to disable.", section="prayer", position=3)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.PIETY;
    }

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Name of the prayer restore item (e.g. Prayer potion, Super restore). Leave blank to disable.", section="prayer", position=4)
    default public String prayerPotionName() {
        return "Prayer potion";
    }

    @ConfigItem(keyName="prayerThresholdPct", name="Restore Prayer Below %", description="Drink prayer restore when prayer points drop below this percentage of max.", section="prayer", position=5)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int prayerThresholdPct() {
        return 30;
    }

    @ConfigItem(keyName="foodName", name="Food Name", description="Item name to eat (partial match, e.g. 'shark'). Leave blank to disable eating.", section="food", position=0)
    default public String foodName() {
        return "shark";
    }

    @ConfigItem(keyName="eatAtHpPercent", name="Eat Below HP %", description="Eat when current HP falls below this percentage of max.", section="food", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatAtHpPercent() {
        return 50;
    }

    @ConfigItem(keyName="comboFood", name="Combo Food (optional)", description="Optional second food eaten in the same tick (e.g. 'karambwan'). Leave blank to disable.", section="food", position=2)
    default public String comboFood() {
        return "";
    }

    @ConfigItem(keyName="combatPotion", name="Combat Potion", description="Combat-boost potion to drink during the fight. Set to None to disable.", section="potions", position=0)
    default public CombatPotion combatPotion() {
        return CombatPotion.NONE;
    }

    @ConfigItem(keyName="combatPotionRedrinkMins", name="Re-drink Every (mins)", description="Re-drink the combat potion this often to maintain the boost.", section="potions", position=1)
    @Range(min=1, max=30)
    default public int combatPotionRedrinkMins() {
        return 5;
    }

    @ConfigItem(keyName="antipoisonName", name="Antipoison Item", description="Item name to drink when poisoned/venomed (e.g. Antivenom+, Anti-poison). Leave blank to disable.", section="potions", position=2)
    default public String antipoisonName() {
        return "";
    }

    @ConfigItem(keyName="followSafeTiles", name="Follow Safe Tile (lit tile)", description="CRITICAL for all three moons: continuously stand on the lit safe tile that moves around the boss and attack from there. Standing off it takes exponentially escalating damage. Leave ON.", section="mechanics", position=0)
    default public boolean followSafeTiles() {
        return true;
    }

    @ConfigItem(keyName="enableBossMechanics", name="Enable Boss Mechanics", description="Handle each moon's special phase (jaguars/pools, Blue ice-entomb + frost-storm brazier relighting, Eclipse Moon-Shield hide + clone parry). Leave ON.", section="mechanics", position=1)
    default public boolean enableBossMechanics() {
        return true;
    }

    @ConfigItem(keyName="meleeReach", name="Melee Weapon Reach (tiles)", description="Your weapon's attack reach. The lit safe tile sits ~2 tiles from the boss, so: 1 = normal melee (bot stands on the in-between tile, touching both); 2 = halberd/scythe (bot stands EXACTLY on the lit tile and reaches the boss from it).", section="mechanics", position=2)
    @Range(min=1, max=3)
    default public int meleeReach() {
        return 1;
    }

    @ConfigItem(keyName="safeTileRadius", name="Safe-Tile Radius (tiles)", description="How far off the lit marker tile the bot may stand and still count as 'safe'. 1 = recommended (with a 1-tile weapon the bot stands on the in-between tile). 0 = strict (needs a 2-tile weapon).", section="mechanics", position=3)
    @Range(min=0, max=2)
    default public int safeTileRadius() {
        return 1;
    }

    @ConfigItem(keyName="eclipseCloneEatPct", name="Eclipse: Clone-Phase Eat Below HP %", description="During the Eclipse clone bind the bot eats below this higher HP % (the clones hit hard and ignore prayer, so a bigger buffer is the safety net for imperfect parries).", section="mechanics", position=4)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eclipseCloneEatPct() {
        return 75;
    }

    @ConfigItem(keyName="continueInRoom", name="Farm In-Room (wait for respawn)", description="After looting, stay in the room and wait for the boss to respawn, then keep killing it \u2014 as long as supplies last. The bot only leaves to resupply when food/prayer run low (or you die).", section="travel", position=0)
    default public boolean continueInRoom() {
        return true;
    }

    @ConfigItem(keyName="respawnWaitSecs", name="Respawn Wait Timeout (secs)", description="How long to wait for the boss to respawn in-room before giving up (re-entering the mural or resupplying).", section="travel", position=1)
    @Range(min=5, max=180)
    default public int respawnWaitSecs() {
        return 40;
    }

    @ConfigItem(keyName="enableResupply", name="Enable Resupply Loop", description="When supplies run low (or after a death), return to bank and travel back via the glider. If off, the bot just idles when it runs out of supplies.", section="travel", position=2)
    default public boolean enableResupply() {
        return false;
    }

    @ConfigItem(keyName="lootItems", name="Loot Item Names", description="Comma-separated item names to pick up after the kill. Leave blank to loot ALL nearby items.", section="travel", position=3)
    default public String lootItems() {
        return "";
    }

    @ConfigItem(keyName="eatWhileLooting", name="Eat to Free Inventory Space", description="When inventory is full during looting, eat food to clear a slot so loot can be picked up.", section="travel", position=4)
    default public boolean eatWhileLooting() {
        return false;
    }

    @ConfigItem(keyName="requiredItems", name="Resupply Minimums", description="Comma-separated 'item name:min count' pairs that trigger a resupply (e.g. 'shark:3,super restore:1'). After each kill the bot banks if it has LESS than ANY listed minimum (fewer than 3 sharks OR fewer than 1 super restore in this example); if every item is at or above its minimum it skips banking and starts the next boss round. Leave blank to fall back to a simple 'have any food and prayer restore' check.", section="travel", position=5)
    default public String requiredItems() {
        return "";
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Moons bot status panel over the chatbox.", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

