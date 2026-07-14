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
package net.runelite.client.plugins.svc.tob;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.tob.TobCombatMethod;
import net.runelite.client.plugins.svc.tob.TobOverheadPrayer;

@ConfigGroup(value="orionTob")
public interface TobConfig
extends Config {
    @ConfigSection(name="Combat Method", description="How Verzik is fought and how the fight is started", position=0, closedByDefault=false)
    public static final String methodSection = "method";
    @ConfigSection(name="Prayer", description="Reactive overhead protection \u2014 the core defence", position=1, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Food & Healing", description="Eating / brewing settings", position=2, closedByDefault=false)
    public static final String foodSection = "food";
    @ConfigSection(name="Special Attack", description="Spec-weapon swap settings", position=3, closedByDefault=false)
    public static final String specSection = "spec";
    @ConfigSection(name="Potions", description="Overload / boost potion", position=4, closedByDefault=false)
    public static final String potionSection = "potion";
    @ConfigSection(name="Mechanics", description="Final-phase handling", position=5, closedByDefault=false)
    public static final String mechSection = "mechanics";
    @ConfigSection(name="Display", description="Overlay options", position=6, closedByDefault=true)
    public static final String displaySection = "display";

    @ConfigItem(keyName="combatMethod", name="Combat Method", description="How to fight Verzik. Auto-detect reads your worn weapon at fight start: Dharok's set \u2192 Dharok's low-HP method, bow/blowpipe/etc \u2192 Ranged, staff/trident \u2192 Magic, anything else \u2192 Melee. Pick one explicitly to override.", section="method", position=0)
    default public TobCombatMethod combatMethod() {
        return TobCombatMethod.AUTO;
    }

    @ConfigItem(keyName="groundedWeapon", name="Main Weapon", description="Weapon used for the fight (partial name match). LEAVE BLANK to fight with whatever is equipped \u2014 the bot never blocks on missing gear; a named weapon is equipped only if it's actually carried.", section="method", position=1)
    default public String groundedWeapon() {
        return "";
    }

    @ConfigItem(keyName="offHand", name="Off-hand / Shield", description="Shield or defender worn with a one-handed main weapon (partial match). Re-equipped after a 2-handed spec. Leave blank for none / two-handed weapons.", section="method", position=2)
    default public String offHand() {
        return "";
    }

    @ConfigItem(keyName="flyingWeapon", name="Flying-phase Weapon (melee)", description="Optional 2-tile reach weapon (Noxious halberd / scythe) equipped ONLY for the flying/bomb phase (8372) \u2014 its reach lets the bot weave off the tile-bombs while still hitting her. MELEE method only. The TRUE FORM (8374) has no bomb dance, so it drops back to your Main Weapon + Off-hand (whip/tentacle + defender) for higher DPS. Leave blank to keep the main weapon through both phases.", section="method", position=3)
    default public String flyingWeapon() {
        return "";
    }

    @ConfigItem(keyName="startEncounter", name="Auto-start (Talk-to Verzik)", description="'Talk-to' the throne-phase Verzik to begin the encounter and answer the intro dialog automatically.", section="method", position=4)
    default public boolean startEncounter() {
        return true;
    }

    @ConfigItem(keyName="startDialogOption", name="Start Dialog Option", description="Text (partial match) of the dialog option that begins the fight. Default: 'Yes, let's begin.'", section="method", position=5)
    default public String startDialogOption() {
        return "Yes, let's begin";
    }

    @ConfigItem(keyName="autoEnter", name="Auto Party + Enter", description="At the Ver Sinhaza entrance (including after a death that dumps you outside), make a solo party at the notice board if needed and 'Enter' the Theatre automatically.", section="method", position=6)
    default public boolean autoEnter() {
        return true;
    }

    @ConfigItem(keyName="openingWalkTiles", name="Opening Walk South (tiles)", description="After the start dialog ends, run this many tiles SOUTH (decreasing Y) to get into fighting position before the first bomb. Set to 0 to disable. Default 6.", section="method", position=7)
    @Range(min=0, max=15)
    default public int openingWalkTiles() {
        return 6;
    }

    @ConfigItem(keyName="dodgeBombs", name="Dodge Bombs (tile-dance)", description="PRIMARY (and at the bomb phase the ONLY) defence. Verzik's big attack (projectile 1583, 36-50 every 4 ticks) is lobbed at the tile you stand on and NO prayer reduces it \u2014 so the bot weaves every tick inside a reach weapon's range of her, ending 2+ tiles off each launch tile so the bomb lands where you WERE (a clean 0, not the 12-23 splash a single step still eats). Needs a 2-tile reach weapon (Noxious halberd / scythe) so you keep hitting her while weaving. At the final form it instead holds to DPS and only steps to flee ground hazards / webs.", section="prayer", position=0)
    default public boolean dodgeBombs() {
        return true;
    }

    @ConfigItem(keyName="reactivePrayer", name="Reactive Overhead Switching", description="Switch the overhead per incoming projectile id (for the later phase, where she alternates magic and ranged autos). Second defence layer behind the tile-dance.", section="prayer", position=1)
    default public boolean reactivePrayer() {
        return true;
    }

    @ConfigItem(keyName="restingOverheadV2", name="Resting Overhead Prayer", description="Overhead kept up when no classified projectile is in flight (and the fallback with reactive switching off). Default Protect from MAGIC \u2014 the 8372 auto (proj 1583) is a magic attack on this server (OSRS Verzik P1 + the reference video both pray Magic here); the pre-07-05 default of Missiles was wrong and ate every hit.", section="prayer", position=2)
    default public TobOverheadPrayer restingOverhead() {
        return TobOverheadPrayer.PROTECT_FROM_MAGIC;
    }

    @ConfigItem(keyName="rangedProjIds", name="Ranged Projectile IDs", description="Verzik projectile ids prayed with Protect from Missiles (comma-separated). 1593 = the final-form (8374) ranged barb auto. If a listed id keeps hitting 25+ through the right prayer, the bot flips that id automatically mid-fight.", section="prayer", position=3)
    default public String rangedProjIds() {
        return "1593";
    }

    @ConfigItem(keyName="magicProjIds", name="Magic Projectile IDs", description="Verzik projectile ids prayed with Protect from Magic (comma-separated). 1591/1594 = the final-form (8374) magic (blue) autos. NOTE: 1583 (the 8372 tile-bomb) is UNBLOCKABLE on this server \u2014 no overhead reduces it, so it is force-classified as a mechanic in code and defended by DODGING, not prayer.", section="prayer", position=4)
    default public String magicProjIds() {
        return "1591,1594";
    }

    @ConfigItem(keyName="ignoredProjIds", name="Mechanic Projectile IDs", description="Projectile ids that are MECHANICS, not prayable attacks \u2014 they never change the overhead (comma-separated). 1583 = the unblockable tile-bomb (dodged, never prayed), 1585 = the chain stun (~12 chip + ~3s stun, prayer-independent), 1596 = yellows, 1598 = green ball, 1601 = web fling.", section="prayer", position=5)
    default public String ignoredProjIds() {
        return "1583,1585,1596,1598,1601";
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage prayer to keep on. OFF (None) by default: on this RSPS a damage prayer (Piety/Rigour/Augury) is mutually exclusive with the protection overhead \u2014 keeping one on silently drops Protect-from-X and lets full 40+ hits through. Only pick one if you've verified both stay active together.", section="prayer", position=6)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.NONE;
    }

    @ConfigItem(keyName="restoreItem", name="Prayer Restore Item", description="Item drunk to restore prayer (and brew-lowered stats). Partial match. Leave blank to disable. Default: Super restore.", section="prayer", position=7)
    default public String restoreItem() {
        return "Super restore";
    }

    @ConfigItem(keyName="restoreBelowPct", name="Restore Prayer Below %", description="Drink the restore item when prayer points drop below this percentage of max.", section="prayer", position=8)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int restoreBelowPct() {
        return 40;
    }

    @ConfigItem(keyName="foodName", name="Food / Brew Name", description="Item eaten to heal (partial match). If neither this nor the backup matches anything carried, the bot falls back to ANY item with an 'Eat' action (e.g. Anglerfish), then to any healing brew \u2014 it never starves holding food.", section="food", position=0)
    default public String foodName() {
        return "Saradomin brew";
    }

    @ConfigItem(keyName="backupFood", name="Backup Food Name", description="Optional second food used when the main brew runs out (partial match). Leave blank to disable.", section="food", position=1)
    default public String backupFood() {
        return "Shark";
    }

    @ConfigItem(keyName="eatBelowHpPct", name="Eat Below HP %", description="Heal when current HP falls below this percentage of max. The bot ALSO eats predictively whenever HP is within one worst-observed-hit of dying, whatever this is set to \u2014 so a mis-set threshold can't repeat the 07-04 deaths.", section="food", position=2)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int eatBelowHpPct() {
        return 65;
    }

    @ConfigItem(keyName="emergencyHpPct", name="Emergency Eat Below HP %", description="Below this HP the bot drops everything (spec, attacking) and heals every tick until it climbs back out. Should be lower than Eat Below HP %.", section="food", position=3)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int emergencyHpPct() {
        return 45;
    }

    @ConfigItem(keyName="dharokFloorPct", name="Dharok's: HP Floor %", description="Dharok's method only \u2014 eat when HP falls below this percentage. Low HP is the whole point of Dharok's, so this replaces Eat Below HP % while the prayers are verified to be blocking (a big unprayed hit temporarily restores safe eating).", section="food", position=4)
    @Range(min=10, max=90)
    @Units(value="%")
    default public int dharokFloorPct() {
        return 35;
    }

    @ConfigItem(keyName="dharokCeilPct", name="Dharok's: HP Ceiling %", description="Dharok's method only \u2014 stop topping up once HP reaches this percentage (eating past it just wastes the strength bonus).", section="food", position=5)
    @Range(min=20, max=99)
    @Units(value="%")
    default public int dharokCeilPct() {
        return 55;
    }

    @ConfigItem(keyName="enableSpec", name="Special Attack On Cooldown", description="Fire the special attack (e.g. Bandos godsword to drop Verzik's Defence) whenever spec energy is at/above 'Spec At Energy %' AND there's a clean window (no bomb landing, not web-bound, not enrage-kiting), in BOTH fighting phases. The BGS costs 50% energy, so it fires twice from full and again as energy regenerates \u2014 draining Defence early and adding burst. Never fires on the passive transition or without the spec weapon.", section="spec", position=0)
    default public boolean enableSpec() {
        return true;
    }

    @ConfigItem(keyName="specWeapon", name="Spec Weapon", description="Weapon equipped to special attack, then swapped out again (partial match). Leave blank to disable.", section="spec", position=1)
    default public String specWeapon() {
        return "Bandos godsword";
    }

    @ConfigItem(keyName="specAtPct", name="Spec At Energy %", description="Use the special attack when spec energy is at or above this percentage.", section="spec", position=2)
    @Range(min=25, max=100)
    @Units(value="%")
    default public int specAtPct() {
        return 50;
    }

    @ConfigItem(keyName="drinkOverload", name="Drink Overload at Start", description="Drink a boost potion once when the encounter begins (re-armed after a death \u2014 boosts reset when you die).", section="potion", position=0)
    default public boolean drinkOverload() {
        return true;
    }

    @ConfigItem(keyName="overloadName", name="Overload / Boost Name", description="Boost potion name (partial match). Default: Overload.", section="potion", position=1)
    default public String overloadName() {
        return "Overload";
    }

    @ConfigItem(keyName="overloadMinHpPct", name="Only Overload Above HP %", description="Skip the overload while HP is below this percentage. Overload deals ~50 self-damage on drink, so the bot only sips on a clean opening with no Verzik attack inbound.", section="potion", position=2)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int overloadMinHpPct() {
        return 85;
    }

    @ConfigItem(keyName="handleWebs", name="Web Handling (run + tank)", description="Final-form web attack handling. When Verzik flings webs (anim 8127 / proj 1601 / Web NPCs 8376) the bot RUNS around her to the far side every tick so the tile-targeted webs miss \u2014 instead of standing and getting bound. If it DOES get bound ('cannot free yourself'), it eats to keep HP above 60% and keeps attacking Verzik, tanking the web hits. Turn off to ignore webs entirely.", section="mechanics", position=0)
    default public boolean handleWebs() {
        return true;
    }

    @ConfigItem(keyName="panicBrewOnSap", name="Emergency Heal on Health-Sap", description="When Verzik casts her final-phase 'saps your health' power-up (a large burst hit), immediately heal if HP is below 85%.", section="mechanics", position=1)
    default public boolean panicBrewOnSap() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Verzik bot status panel on screen.", section="display", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

