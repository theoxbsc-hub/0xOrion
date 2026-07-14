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
package net.runelite.client.plugins.svc.combat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.AttackStyle;
import net.runelite.client.plugins.svc.combat.CombatPotion;
import net.runelite.client.plugins.svc.combat.DefensivePrayer;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.combat.PrayerFlickMode;

@ConfigGroup(value="orionAioCombat")
public interface AioCombatConfig
extends Config {
    @ConfigSection(name="Gemstone Crab", description="Dedicated AFK Gemstone Crab mode", position=0, closedByDefault=false)
    public static final String gemstoneSection = "gemstone";
    @ConfigSection(name="Target NPC", description="Which NPCs to attack", position=1, closedByDefault=false)
    public static final String targetSection = "target";
    @ConfigSection(name="Food & Healing", description="Eating and healing settings", position=2, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Prayer", description="Prayer and flicking settings", position=3, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Potions & Boosts", description="Stat-boosting potions", position=4, closedByDefault=true)
    public static final String potionSection = "potions";
    @ConfigSection(name="Looting", description="Ground item looting settings", position=5, closedByDefault=false)
    public static final String lootSection = "loot";
    @ConfigSection(name="Special Attack", description="Special attack settings", position=6, closedByDefault=true)
    public static final String specSection = "spec";
    @ConfigSection(name="Banking", description="Auto-banking when supplies run out", position=7, closedByDefault=true)
    public static final String bankSection = "bank";
    @ConfigSection(name="Miscellaneous", description="Other options", position=8, closedByDefault=true)
    public static final String miscSection = "misc";

    @ConfigItem(keyName="gemstoneCrabMode", name="Gemstone Crab Mode", description="AFK Gemstone Crab mode: attack the Gemstone Crab while it is present, then crawl through the cave to the next chamber once it is defeated. When enabled this overrides all normal targeting and combat logic below.", section="gemstone", position=0)
    default public boolean gemstoneCrabMode() {
        return false;
    }

    @ConfigItem(keyName="npcNames", name="NPC Names", description="Comma-separated NPC names to attack (e.g. Goblin,Cow)", section="target", position=0)
    default public String npcNames() {
        return "Goblin";
    }

    @ConfigItem(keyName="searchRadius", name="Search Radius", description="Tiles to search for target NPCs from your position", section="target", position=1)
    @Range(min=1, max=30)
    default public int searchRadius() {
        return 10;
    }

    @ConfigItem(keyName="onlyIdle", name="Only Attack Idle NPCs", description="Skip NPCs already in combat with another player", section="target", position=2)
    default public boolean onlyIdle() {
        return true;
    }

    @ConfigItem(keyName="attackStyle", name="Attack Style", description="Combat attack style to use", section="target", position=3)
    default public AttackStyle attackStyle() {
        return AttackStyle.AUTO;
    }

    @ConfigItem(keyName="enableStyleSwap", name="Cycle Combat Styles", description="Automatically rotate through combat styles on a timer", section="target", position=4)
    default public boolean enableStyleSwap() {
        return false;
    }

    @ConfigItem(keyName="styleSwapInterval", name="Swap Interval (minutes)", description="How many minutes between each style swap", section="target", position=5)
    @Range(min=1, max=60)
    default public int styleSwapInterval() {
        return 5;
    }

    @ConfigItem(keyName="styleSwapCycle", name="Styles to Cycle (0-3)", description="Comma-separated button indices to rotate: 0=first style, 1=second, 2=third, 3=fourth. E.g. \"0,1\" cycles Accurate\u2194Aggressive.", section="target", position=6)
    default public String styleSwapCycle() {
        return "0,1";
    }

    @ConfigItem(keyName="safespot", name="Safe Spot Tile", description="World coordinates to return to between kills (format: x,y). Leave blank to disable.", section="target", position=7)
    default public String safespot() {
        return "";
    }

    @ConfigItem(keyName="eatFood", name="Eat Food", description="Automatically eat food when HP falls below threshold", section="food", position=0)
    default public boolean eatFood() {
        return true;
    }

    @ConfigItem(keyName="foodNames", name="Food Names", description="Comma-separated food names (e.g. Lobster,Shark,Anglerfish)", section="food", position=1)
    default public String foodNames() {
        return "Lobster";
    }

    @ConfigItem(keyName="eatAtHpPercent", name="Eat Below HP %", description="Eat when current HP falls below this percentage", section="food", position=2)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatAtHpPercent() {
        return 50;
    }

    @ConfigItem(keyName="eatComboFood", name="Combo Food", description="Second food to eat alongside main food (e.g. Karambwan). Leave blank to disable.", section="food", position=3)
    default public String eatComboFood() {
        return "";
    }

    @ConfigItem(keyName="drinkAntipoisonName", name="Antipoison/Antivenom", description="Potion name to drink when poisoned (e.g. Antidote++, Antivenom+). Leave blank to disable.", section="food", position=4)
    default public String drinkAntipoisonName() {
        return "";
    }

    @ConfigItem(keyName="logoutOnNoFood", name="Home Tele & Logout When No Food", description="When HP is below eat threshold and no food remains: open magic tab, cast Orion Home Teleport to escape combat, then logout.", section="food", position=5)
    default public boolean logoutOnNoFood() {
        return false;
    }

    @ConfigItem(keyName="enablePrayer", name="Enable Prayer", description="Use prayers during combat", section="prayer", position=0)
    default public boolean enablePrayer() {
        return false;
    }

    @ConfigItem(keyName="defensivePrayer", name="Protection Prayer", description="Prayer to use for protection", section="prayer", position=1)
    default public DefensivePrayer defensivePrayer() {
        return DefensivePrayer.NONE;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Prayer to boost your damage/accuracy", section="prayer", position=2)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.NONE;
    }

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Item to restore prayer points (e.g. Prayer potion(4), Super restore(4)). Leave blank to disable.", section="prayer", position=3)
    default public String prayerPotionName() {
        return "Prayer potion(4)";
    }

    @ConfigItem(keyName="drinkPrayerAt", name="Drink Prayer Pot Below", description="Drink prayer restore when prayer points fall below this value", section="prayer", position=4)
    @Range(min=1, max=99)
    @Units(value="pt")
    default public int drinkPrayerAt() {
        return 20;
    }

    @ConfigItem(keyName="quickPrayers", name="Use Quick Prayers", description="Activate quick prayers instead of individual prayer toggling. Mutually exclusive with Flick Mode \u2014 when enabled, flicking is bypassed.", section="prayer", position=5)
    default public boolean quickPrayers() {
        return false;
    }

    @ConfigItem(keyName="flickMode", name="Flick Mode", description="Always on = prayers stay active (no drain saving). Defensive flick = protection prayer held during combat to deflect hits. Offensive flick = boost prayer toggles per player swing for the damage bonus. Full flick = both. Ignored when Use Quick Prayers is enabled.", section="prayer", position=6)
    default public PrayerFlickMode flickMode() {
        return PrayerFlickMode.ALWAYS_ON;
    }

    @ConfigItem(keyName="offensiveFlickEnabled", name="Flick Boost Prayer", description="When using Offensive flick or Full flick mode, toggle the boost prayer per player attack swing instead of leaving it always-on.", section="prayer", position=7)
    default public boolean offensiveFlickEnabled() {
        return true;
    }

    @ConfigItem(keyName="playerWeaponSpeedTicks", name="Weapon Speed (ticks)", description="Your weapon's attack rate in ticks. Used for predictive offensive prayer flicking. Common values: 4 = whip / scimitar, 5 = godswords / most 2h, 6 = elder maul, 3 = abyssal dagger, 5 = trident. Set to 0 to disable prediction.", section="prayer", position=8)
    @Range(min=0, max=10)
    default public int playerWeaponSpeedTicks() {
        return 4;
    }

    @ConfigItem(keyName="combatPotion", name="Combat Boost Potion", description="Potion to boost combat stats", section="potions", position=0)
    default public CombatPotion combatPotion() {
        return CombatPotion.NONE;
    }

    @ConfigItem(keyName="redrinkBelowBoost", name="Re-drink When Boost Expires", description="Drink another dose when the stat boost fully wears off", section="potions", position=1)
    default public boolean redrinkBelowBoost() {
        return true;
    }

    @ConfigItem(keyName="drinkStamina", name="Drink Stamina Potions", description="Drink Stamina potions to keep run energy up", section="potions", position=2)
    default public boolean drinkStamina() {
        return false;
    }

    @ConfigItem(keyName="staminaThreshold", name="Stamina Below Run %", description="Drink stamina potion when run energy drops below this %", section="potions", position=3)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int staminaThreshold() {
        return 30;
    }

    @ConfigItem(keyName="enableLooting", name="Enable Looting", description="Pick up items from the ground after kills", section="loot", position=0)
    default public boolean enableLooting() {
        return true;
    }

    @ConfigItem(keyName="lootNames", name="Loot Item Names", description="Comma-separated item names to loot (e.g. Coins,Dragon bones)", section="loot", position=1)
    default public String lootNames() {
        return "Coins";
    }

    @ConfigItem(keyName="excludeLootNames", name="Exclude Item Names", description="Comma-separated item names to never loot, even if matched by Loot Item Names or HA value (e.g. Dragonhide,Dragon dart)", section="loot", position=2)
    default public String excludeLootNames() {
        return "";
    }

    @ConfigItem(keyName="lootRadius", name="Loot Radius", description="Tiles to search for loot around your position", section="loot", position=3)
    @Range(min=1, max=10)
    default public int lootRadius() {
        return 4;
    }

    @ConfigItem(keyName="lootOnlyWhenSafe", name="Loot Only When Safe", description="Wait until not in combat before looting", section="loot", position=4)
    default public boolean lootOnlyWhenSafe() {
        return false;
    }

    @ConfigItem(keyName="eatToLoot", name="Eat Food to Pick Up Loot", description="When inventory is full and there is loot on the ground, eat a food item to free a slot before picking it up. Only applies to items in Loot Item Names or above the HA value threshold \u2014 not bones/ashes.", section="loot", position=5)
    default public boolean eatToLoot() {
        return false;
    }

    @ConfigItem(keyName="lootMinValue", name="Loot Items Over HA Value (GP)", description="Also loot any item whose High Alch value is at or above this amount. Coins use stack size (e.g. 10000 picks up 10k+ coin stacks). Set to 0 to disable.", section="loot", position=6)
    @Range(min=0, max=10000000)
    default public int lootMinValue() {
        return 0;
    }

    @ConfigItem(keyName="buryBones", name="Bury Bones", description="Automatically bury bone items after picking them up (buried immediately, before looting more)", section="loot", position=7)
    default public boolean buryBones() {
        return false;
    }

    @ConfigItem(keyName="boneTypes", name="Bone Types to Pick Up & Bury", description="Comma-separated bone names to loot and bury (e.g. Dragon bones,Lava dragon bones). The bot picks these up and buries them immediately. Leave blank to bury anything with 'bones' or 'ashes' in the name.", section="loot", position=8)
    default public String boneTypes() {
        return "Bones";
    }

    @ConfigItem(keyName="enableSpec", name="Enable Special Attack", description="Use special attack when energy is available", section="spec", position=0)
    default public boolean enableSpec() {
        return false;
    }

    @ConfigItem(keyName="specAt", name="Spec Above Energy %", description="Use special attack when energy is at or above this value", section="spec", position=1)
    @Range(min=10, max=100)
    @Units(value="%")
    default public int specAt() {
        return 100;
    }

    @ConfigItem(keyName="enableBanking", name="Enable Auto-banking", description="Go bank and restock when supplies run low", section="bank", position=0)
    default public boolean enableBanking() {
        return false;
    }

    @ConfigItem(keyName="bankWhenFoodLeft", name="Bank When Food Below", description="Bank when fewer than this many food items remain in inventory", section="bank", position=1)
    @Range(min=0, max=27)
    default public int bankWhenFoodLeft() {
        return 2;
    }

    @ConfigItem(keyName="withdrawFood", name="Food to Withdraw", description="How many food items to withdraw from bank each trip", section="bank", position=2)
    @Range(min=1, max=27)
    default public int withdrawFood() {
        return 18;
    }

    @ConfigItem(keyName="enableRun", name="Use Run", description="Enable run when energy is above 30%", section="misc", position=0)
    default public boolean enableRun() {
        return true;
    }

    @ConfigItem(keyName="alchItems", name="Alch Items", description="Comma-separated item names to High Alch after looting. Requires Nature rune + fire runes.", section="misc", position=1)
    default public String alchItems() {
        return "";
    }

    @ConfigItem(keyName="stopAfterKills", name="Stop After X Kills (0=never)", description="Automatically stop the bot after this many kills (0 = run forever)", section="misc", position=2)
    @Range(min=0, max=10000)
    default public int stopAfterKills() {
        return 0;
    }

    @ConfigItem(keyName="debugOverlay", name="Show Status Overlay", description="Display bot status and kill count on screen", section="misc", position=3)
    default public boolean debugOverlay() {
        return true;
    }
}

