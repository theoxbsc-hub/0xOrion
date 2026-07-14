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
package net.runelite.client.plugins.svc.cox;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.cox.OlmHeadStrategy;
import net.runelite.client.plugins.svc.cox.OlmOverheadMode;
import net.runelite.client.plugins.svc.cox.OlmSpell;

@ConfigGroup(value="orionCox")
public interface OlmConfig
extends Config {
    @ConfigSection(name="General", description="Mode and combat order settings", position=0, closedByDefault=false)
    public static final String generalSection = "general";
    @ConfigSection(name="Prayer", description="Overhead protection and offensive prayer settings", position=1, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Food & Healing", description="HP threshold and food settings", position=2, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Mage Gear", description="Items to equip when targeting the Mage Hand (right hand)", position=3, closedByDefault=false)
    public static final String mageGearSection = "mageGear";
    @ConfigSection(name="Melee Gear", description="Items to equip when targeting the Melee Hand (left hand)", position=4, closedByDefault=false)
    public static final String meleeGearSection = "meleeGear";
    @ConfigSection(name="Ranged Gear", description="Items to equip when attacking Olm's head", position=5, closedByDefault=false)
    public static final String rangedGearSection = "rangedGear";
    @ConfigSection(name="Mechanics", description="Toggle which Olm special attacks the bot auto-handles", position=6, closedByDefault=false)
    public static final String mechanicsSection = "mechanics";
    @ConfigSection(name="Potions", description="Combat boost potion settings", position=7, closedByDefault=false)
    public static final String potionSection = "potions";
    @ConfigSection(name="Display", description="Overlay display options", position=8, closedByDefault=true)
    public static final String displaySection = "display";

    @ConfigItem(keyName="mageFirst", name="Cripple Mage Hand First", description="When crippling hands: attack the Mage Hand (Olm's RIGHT hand, weak to magic) first, then the Melee Hand (Olm's LEFT hand, weak to melee). When OFF: melee hand first. NOTE: if magic does poor damage to the hand the bot targets, your Mage/Melee gear lists are swapped \u2014 fix the gear IDs, don't expect the bot to guess.", section="general", position=1)
    default public boolean mageFirst() {
        return true;
    }

    @ConfigItem(keyName="headStrategy", name="Head Strategy", description="How the bot decides when to attack Olm's HEAD vs the hands. HANDS THEN HEAD (recommended, default) follows real Olm rules: it softens BOTH hands to low HP, disables them within a couple ticks of each other (so neither heals back), and only attacks the head once both are down and stay down (the final phase). AUTO does the same but also probes whether this server lets the head be damaged any time and commits to it if so. HEAD ONLY always attacks the head \u2014 only use it if you KNOW this server lets the head be damaged at any time.", section="general", position=2)
    default public OlmHeadStrategy headStrategy() {
        return OlmHeadStrategy.HANDS_THEN_HEAD;
    }

    @ConfigItem(keyName="overheadMode", name="Resting Overhead", description="The bot ALWAYS reacts to Olm's incoming magic/ranged projectiles and switches protection prayer to match (zero auto-attack damage). This setting only chooses the RESTING prayer used between attacks: ALWAYS MAGIC rests on Protect from Magic (recommended), ALWAYS MISSILES rests on Protect from Missiles. SWITCH ON PROJECTILE behaves the same as ALWAYS MAGIC now that reactive switching is always on.", section="prayer", position=0)
    default public OlmOverheadMode overheadMode() {
        return OlmOverheadMode.ALWAYS_MAGIC;
    }

    @ConfigItem(keyName="sphereProtect", name="Sphere Prayer Switching", description="Briefly switch to the matching protection prayer when Olm fires a prayer-sapping sphere at you. Driven by the chat tell (reliable): 'sphere of aggression' = Melee, 'sphere of accuracy and dexterity' = Missiles, 'sphere of magical power' = Magic. An unprayed sphere hits for ~50% of your current HP, so this is recommended ON.", section="prayer", position=1)
    default public boolean sphereProtect() {
        return true;
    }

    @ConfigItem(keyName="mageOffensivePrayer", name="Mage Offensive Prayer", description="Damage/accuracy prayer kept active while attacking the Mage Hand (magic). Augury is best; Mystic Might is the budget option; None to disable.", section="prayer", position=2)
    default public OffensivePrayer mageOffensivePrayer() {
        return OffensivePrayer.AUGURY;
    }

    @ConfigItem(keyName="meleeOffensivePrayer", name="Melee Offensive Prayer", description="Damage/accuracy prayer kept active while attacking the Melee Hand (melee). Piety is best; Ultimate Strength / Superhuman Strength are budget options; None to disable.", section="prayer", position=3)
    default public OffensivePrayer meleeOffensivePrayer() {
        return OffensivePrayer.PIETY;
    }

    @ConfigItem(keyName="rangedOffensivePrayer", name="Ranged Offensive Prayer", description="Damage/accuracy prayer kept active while attacking the head (ranged, final phase). Rigour is best; Eagle Eye / Hawk Eye are budget options; None to disable.", section="prayer", position=4)
    default public OffensivePrayer rangedOffensivePrayer() {
        return OffensivePrayer.RIGOUR;
    }

    @ConfigItem(keyName="prayerPotionName", name="Prayer Restore Item", description="Name of prayer restore potion in inventory (e.g. Super restore, Prayer potion). Leave blank to disable prayer restoration.", section="prayer", position=5)
    default public String prayerPotionName() {
        return "Super restore";
    }

    @ConfigItem(keyName="prayerThresholdPct", name="Restore Prayer Below %", description="Drink a prayer restore when prayer points drop below this percentage of maximum.", section="prayer", position=6)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int prayerThresholdPct() {
        return 30;
    }

    @ConfigItem(keyName="foodName", name="Food Name", description="Item name to eat (partial match, case-insensitive). E.g. 'shark' matches Shark, 'manta' matches Manta ray.", section="food", position=0)
    default public String foodName() {
        return "shark";
    }

    @ConfigItem(keyName="eatAtHpPercent", name="Eat Below HP %", description="Eat food when current HP falls below this percentage of maximum.", section="food", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatAtHpPercent() {
        return 50;
    }

    @ConfigItem(keyName="comboFood", name="Combo Food", description="Optional second food to eat alongside the main food in the same tick (e.g. 'karambwan'). Leave blank to disable.", section="food", position=2)
    default public String comboFood() {
        return "";
    }

    @ConfigItem(keyName="escapeItemName", name="Panic Escape Item", description="Item to use to teleport OUT of the fight when HP is critically low and you are out of food (e.g. 'Teleport to house', 'Royal seed pod', a teleport tab). The bot uses its first inventory option, then stops. Leave blank to disable (the bot will fight to the death instead).", section="food", position=3)
    default public String escapeItemName() {
        return "";
    }

    @ConfigItem(keyName="escapeBelowHpPercent", name="Panic Escape Below HP %", description="When HP falls below this percentage AND no food remains, use the Panic Escape Item and stop. Only active if an escape item is set.", section="food", position=4)
    @Range(min=1, max=90)
    @Units(value="%")
    default public int escapeBelowHpPercent() {
        return 30;
    }

    @ConfigItem(keyName="mageGearIds", name="Mage Item IDs", description="Comma-separated ITEM IDs to wield for the mage hand phase (covers weapon + armour). LIST THE WEAPON FIRST so the style-defining staff equips before the bot starts casting. E.g: '22323,4712,4714,12002'. Find IDs with the Entity Inspector / item lookups. Each item is equipped one per tick.", section="mageGear", position=0)
    default public String mageGearIds() {
        return "";
    }

    @ConfigItem(keyName="mageSpell", name="Spell to Cast", description="Spell to cast on the Mage Hand. Select AUTOCAST to rely on your weapon's autocast (Trident, Sang staff, Kodai Wand). Only select an explicit spell if using a non-autocasting weapon.", section="mageGear", position=1)
    default public OlmSpell mageSpell() {
        return OlmSpell.AUTOCAST;
    }

    @ConfigItem(keyName="meleeGearIds", name="Melee Item IDs", description="Comma-separated ITEM IDs to wield when crippling the Melee Hand (Olm's LEFT hand, weak to melee). Covers weapon + armour. LIST THE WEAPON FIRST so the melee weapon equips before the bot starts attacking. E.g: '4151,11832,11834,22322'. Each item is equipped one per tick.", section="meleeGear", position=0)
    default public String meleeGearIds() {
        return "";
    }

    @ConfigItem(keyName="rangedGearIds", name="Ranged Item IDs", description="Comma-separated ITEM IDs for attacking the head with RANGED (the head mitigates 66% of all non-ranged damage, so range it). Covers weapon + armour. LIST THE WEAPON FIRST so the ranged weapon equips before the bot starts attacking. E.g: '21012,21021,21024,19547'. Each item is equipped one per tick.", section="rangedGear", position=0)
    default public String rangedGearIds() {
        return "";
    }

    @ConfigItem(keyName="attackRange", name="Ranged Weapon Range (tiles)", description="Maximum tiles your ranged weapon can reach from the edge of Olm's hitbox. Twisted bow = 10, blowpipe = 5, crossbow = 7. Used to reposition if player is out of range.", section="rangedGear", position=1)
    @Range(min=4, max=12)
    default public int attackRange() {
        return 10;
    }

    @ConfigItem(keyName="dodgeAcid", name="Dodge Acid Pools", description="Tick-walk through the gaps during the acid spray/drip special instead of standing in acid.", section="mechanics", position=0)
    default public boolean dodgeAcid() {
        return true;
    }

    @ConfigItem(keyName="dodgeFireWall", name="Dodge Fire Walls", description="Step perpendicular to an approaching fire wall. INERT until OBJ_FIRE_WALL is harvested in-game (Diagnostics) \u2014 the overlay shows it as 'inert' while the ID is unmapped. Default OFF to avoid false confidence.", section="mechanics", position=1)
    default public boolean dodgeFireWall() {
        return false;
    }

    @ConfigItem(keyName="dodgeCrystalBurst", name="Dodge Crystal Burst", description="Move 2+ tiles when the crystal-burst shove special is detected.", section="mechanics", position=2)
    default public boolean dodgeCrystalBurst() {
        return true;
    }

    @ConfigItem(keyName="dodgeFallingCrystals", name="Dodge Falling Crystals", description="When you are marked for falling crystals, step one tile off your current tile. INERT until GFX_FALLING_CRYSTAL is harvested \u2014 shown as 'inert' in the overlay. Default OFF.", section="mechanics", position=3)
    default public boolean dodgeFallingCrystals() {
        return false;
    }

    @ConfigItem(keyName="dodgeCrystalBombs", name="Flee Crystal Bombs", description="Run away from deployed crystal bombs before they detonate. INERT until OBJ_CRYSTAL_BOMB is harvested \u2014 shown as 'inert' in the overlay. Default OFF.", section="mechanics", position=4)
    default public boolean dodgeCrystalBombs() {
        return false;
    }

    @ConfigItem(keyName="dodgeLightning", name="Dodge Lightning", description="React to the lightning electrocute / prayer-disable. Driven by the chat tell ('electrocuted to the spot' / 'can't use protection prayers') \u2014 the bot holds prayer flicks while disabled and re-flicks the instant it expires. (A pre-emptive column dodge stays inert until OBJ_LIGHTNING is harvested.)", section="mechanics", position=5)
    default public boolean dodgeLightning() {
        return true;
    }

    @ConfigItem(keyName="handleTeleportSwap", name="Handle Teleport Swap", description="On the solo teleport tell ('Olm has no one to pair you with'), walk onto Olm's tile to minimise the distance-scaled damage. Driven by the chat tell.", section="mechanics", position=6)
    default public boolean handleTeleportSwap() {
        return true;
    }

    @ConfigItem(keyName="useLifeSiphonShield", name="Use Life-Siphon Shield", description="Stand on the blue shield tile from Olm's Life Siphon to deny the heal. INERT until OBJ_LIFE_SIPHON_SHIELD is harvested \u2014 shown as 'inert' in the overlay. Default OFF.", section="mechanics", position=8)
    default public boolean useLifeSiphonShield() {
        return false;
    }

    @ConfigItem(keyName="combatPotionName", name="Combat Boost Potion", description="Potion name to drink when the relevant boosted skill drops to base level (e.g. 'Overload', 'Super combat', 'Bastion potion', 'Divine ranging'). Leave blank to disable.", section="potions", position=0)
    default public String combatPotionName() {
        return "";
    }

    @ConfigItem(keyName="drinkAtBaseline", name="Redose When Back to Base", description="Re-drink the combat potion each time the boosted skill returns to its base level. Works for Overload (every ~5 min) and standard potions alike.", section="potions", position=1)
    default public boolean drinkAtBaseline() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Olm bot status panel on screen.", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

