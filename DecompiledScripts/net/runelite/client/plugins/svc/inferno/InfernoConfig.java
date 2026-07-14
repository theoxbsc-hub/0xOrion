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
package net.runelite.client.plugins.svc.inferno;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.inferno.InfernoCombatStyle;
import net.runelite.client.plugins.svc.inferno.InfernoOverheadPrayer;

@ConfigGroup(value="orionInferno")
public interface InfernoConfig
extends Config {
    @ConfigSection(name="General", description="Combat style and stop condition", position=0, closedByDefault=false)
    public static final String generalSection = "general";
    @ConfigSection(name="Prayer", description="Auto-switching and offensive prayer", position=1, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Combat", description="Targeting, attack range and healers", position=2, closedByDefault=false)
    public static final String combatSection = "combat";
    @ConfigSection(name="Positioning", description="Stand-and-pray vs optional safespot", position=3, closedByDefault=false)
    public static final String positionSection = "positioning";
    @ConfigSection(name="Supplies", description="Auto-eats any food and drinks brews/restores \u2014 just set the thresholds", position=4, closedByDefault=false)
    public static final String suppliesSection = "supplies";
    @ConfigSection(name="Zuk (optional)", description="Best-effort Zuk support \u2014 off by default until the fight is supported on this server", position=5, closedByDefault=true)
    public static final String zukSection = "zuk";

    @ConfigItem(keyName="combatStyle", name="Combat Style", description="Main style used to fight. RANGED is the recommended default (attack from distance so melee monsters can't reach you). Sets positioning defaults; the damage prayer is chosen below.", section="general", position=0)
    default public InfernoCombatStyle combatStyle() {
        return InfernoCombatStyle.RANGED;
    }

    @ConfigItem(keyName="stopAfterWave", name="Stop After Wave N", description="Stop when the chat reports reaching this wave. 0 = run indefinitely (fight through to the end).", section="general", position=1)
    @Range(min=0, max=100)
    default public int stopAfterWave() {
        return 0;
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Inferno status panel on screen.", section="general", position=2)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="autoPrayerSwitch", name="Auto Prayer Switching", description="Automatically keep the correct overhead protection prayer active. Disable to manage overheads yourself.", section="prayer", position=0)
    default public boolean autoPrayerSwitch() {
        return true;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage prayer kept active during combat. Rigour for ranged, Augury for mage, Piety for melee, Eagle Eye as a budget ranged option.", section="prayer", position=1)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.RIGOUR;
    }

    @ConfigItem(keyName="attackEnabled", name="Auto Attack", description="Attack monsters automatically. Turn off for a prayer-only assist mode (you control the attacking).", section="combat", position=0)
    default public boolean attackEnabled() {
        return true;
    }

    @ConfigItem(keyName="attackRange", name="Weapon Range (tiles)", description="Max tiles your weapon reaches. Used for threat detection and (if a safespot is set) repositioning. Blowpipe = 5, crossbow = 7, bow = 9-10.", section="combat", position=1)
    @Range(min=1, max=12)
    default public int attackRange() {
        return 10;
    }

    @ConfigItem(keyName="killHealersFirst", name="Kill Boss Healers First", description="In the Jad/Zuk phase, attack whichever healer is currently healing the boss until none are (so they can't heal it back up). Ignoring the healers that spawn at 50% fully re-heals the boss. Healers are only attacked from range; the bot never walks under the boss to reach one.", section="combat", position=2)
    default public boolean killHealersFirst() {
        return true;
    }

    @ConfigItem(keyName="ketKehAsThreat", name="Treat Ket-Keh As Threat", description="If on, the Ket-Keh monster is prayed against / killed using the style chosen below. If off, it is ignored for prayer selection.", section="combat", position=3)
    default public boolean ketKehAsThreat() {
        return true;
    }

    @ConfigItem(keyName="ketKehCaste", name="Ket-Keh Attack Style", description="Attack style to treat the Ket-Keh monster as for prayer/priority. Default Melee \u2014 change if you find it mages, ranges or heals.", section="combat", position=4)
    default public InfernoOverheadPrayer ketKehCaste() {
        return InfernoOverheadPrayer.PROTECT_FROM_MELEE;
    }

    @ConfigItem(keyName="bossSafeDistance", name="Boss Min Distance (tiles)", description="Never get closer than this many tiles to Jad/Zuk's hitbox. Their MELEE attack ignores Protect from Magic/Missiles and kills fast. 0 disables (NOT recommended for ranged).", section="combat", position=5)
    @Range(min=0, max=8)
    default public int bossSafeDistance() {
        return 3;
    }

    @ConfigItem(keyName="standStill", name="Stand Still (tank & pray)", description="Open-arena default: do not move, just pray and attack. Disable to walk to and hold the safespot tile below.", section="positioning", position=0)
    default public boolean standStill() {
        return true;
    }

    @ConfigItem(keyName="safespot", name="Safespot Tile (x,y)", description="Optional world tile to hold, format 'x,y'. Only used when Stand Still is OFF. Leave blank to never move.", section="positioning", position=1)
    default public String safespot() {
        return "";
    }

    @ConfigItem(keyName="healBelowHpPct", name="Heal Below HP %", description="When HP drops below this %, sip a brew if you have one, otherwise eat any food. Kept high (60) because a single unprotected boss hit is huge \u2014 staying near full means the rare hit that slips through is survived.", section="supplies", position=0)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int healBelowHpPct() {
        return 60;
    }

    @ConfigItem(keyName="emergencyHpPct", name="Emergency HP %", description="Below this HP %, sip a brew AND eat a food in the same tick (combo-heal). 45 so the bot top-heals before a second big hit can land.", section="supplies", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int emergencyHpPct() {
        return 45;
    }

    @ConfigItem(keyName="restorePrayerPct", name="Restore Below Prayer %", description="Drink a restore (any '*restore*' or 'prayer potion') when prayer points drop below this %.", section="supplies", position=2)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int restorePrayerPct() {
        return 28;
    }

    @ConfigItem(keyName="brewsPerRestore", name="Brews Per Restore", description="After this many brew doses, sip one restore to undo the brew stat-drain (independent of the prayer threshold).", section="supplies", position=3)
    @Range(min=1, max=8)
    default public int brewsPerRestore() {
        return 3;
    }

    @ConfigItem(keyName="useStamina", name="Use Stamina", description="Drink a stamina potion (any '*stamina*') when run energy is low.", section="supplies", position=4)
    default public boolean useStamina() {
        return true;
    }

    @ConfigItem(keyName="staminaPct", name="Stamina Below Energy %", description="Drink a stamina potion when run energy falls below this %.", section="supplies", position=5)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int staminaPct() {
        return 30;
    }

    @ConfigItem(keyName="stopWhenOutOfSupplies", name="Stop When Out Of Supplies", description="Stop the plugin when no food, brews or restores remain (prevents dying with no sustain).", section="supplies", position=6)
    default public boolean stopWhenOutOfSupplies() {
        return true;
    }

    @ConfigItem(keyName="zukEnabled", name="Enable Zuk Phase (best-effort)", description="If your run continues past Jad to Zuk, enable best-effort prayer support for it.", section="zuk", position=0)
    default public boolean zukEnabled() {
        return false;
    }

    @ConfigItem(keyName="zukDefaultPrayer", name="Zuk Default Overhead", description="Overhead to camp during the Zuk fight (best-effort).", section="zuk", position=1)
    default public InfernoOverheadPrayer zukDefaultPrayer() {
        return InfernoOverheadPrayer.PROTECT_FROM_MAGIC;
    }
}

