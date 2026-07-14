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
package net.runelite.client.plugins.svc.nex;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.nex.NexCombatStyle;
import net.runelite.client.plugins.svc.nex.NexOverheadPrayer;

@ConfigGroup(value="orionNex")
public interface NexConfig
extends Config {
    @ConfigSection(name="General", description="Combat style and kill count", position=0, closedByDefault=false)
    public static final String generalSection = "general";
    @ConfigSection(name="Prayer", description="Per-phase overhead and offensive prayers", position=1, closedByDefault=false)
    public static final String prayerSection = "prayer";
    @ConfigSection(name="Combat", description="Attack range, positioning and minions", position=2, closedByDefault=false)
    public static final String combatSection = "combat";
    @ConfigSection(name="Supplies", description="Brews, restores, food and potions", position=3, closedByDefault=false)
    public static final String suppliesSection = "supplies";
    @ConfigSection(name="Mechanics", description="Enable/disable each special-attack reaction", position=4, closedByDefault=false)
    public static final String mechanicsSection = "mechanics";
    @ConfigSection(name="Looting", description="Drop pickup after a kill", position=5, closedByDefault=false)
    public static final String lootingSection = "looting";

    @ConfigItem(keyName="combatStyle", name="Combat Style", description="Main style used to fight Nex. RANGED (Twisted bow / Bow of faerdhinen) is the implemented, recommended solo method \u2014 one weapon kills Nex and the minions, no gear swaps.", section="general", position=0)
    default public NexCombatStyle combatStyle() {
        return NexCombatStyle.RANGED;
    }

    @ConfigItem(keyName="targetKillCount", name="Stop After N Kills", description="Stop the plugin after this many Nex kills. 0 = run indefinitely (idle for respawn after each kill).", section="general", position=1)
    @Range(min=0, max=1000)
    default public int targetKillCount() {
        return 0;
    }

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the Nex bot status panel on screen.", section="general", position=2)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="autoPrayerSwitch", name="Auto Prayer Switching", description="Automatically keep the correct per-phase overhead prayer active. Disable to manage overheads yourself.", section="prayer", position=0)
    default public boolean autoPrayerSwitch() {
        return true;
    }

    @ConfigItem(keyName="smokePrayer", name="Smoke Phase Overhead", description="Overhead prayer during the Smoke phase. Default Protect from Magic (correct for a ranged camper).", section="prayer", position=1)
    default public NexOverheadPrayer smokePrayer() {
        return NexOverheadPrayer.PROTECT_FROM_MAGIC;
    }

    @ConfigItem(keyName="shadowPrayer", name="Shadow Phase Overhead", description="Overhead prayer during the Shadow phase \u2014 the only phase where Nex attacks with Ranged. Default Protect from Missiles.", section="prayer", position=2)
    default public NexOverheadPrayer shadowPrayer() {
        return NexOverheadPrayer.PROTECT_FROM_MISSILES;
    }

    @ConfigItem(keyName="bloodPrayer", name="Blood Phase Overhead", description="Overhead prayer during the Blood phase. Default Protect from Magic.", section="prayer", position=3)
    default public NexOverheadPrayer bloodPrayer() {
        return NexOverheadPrayer.PROTECT_FROM_MAGIC;
    }

    @ConfigItem(keyName="icePrayer", name="Ice Phase Overhead", description="Overhead prayer during the Ice phase. Default Protect from Magic (also reduces freeze frequency).", section="prayer", position=4)
    default public NexOverheadPrayer icePrayer() {
        return NexOverheadPrayer.PROTECT_FROM_MAGIC;
    }

    @ConfigItem(keyName="zarosPrayer", name="Zaros Phase Overhead", description="Overhead prayer during the final Zaros phase. Default Protect from Magic (correct when fighting at range; use Melee only if you stack under her in melee).", section="prayer", position=5)
    default public NexOverheadPrayer zarosPrayer() {
        return NexOverheadPrayer.PROTECT_FROM_MAGIC;
    }

    @ConfigItem(keyName="offensivePrayer", name="Offensive Prayer", description="Damage prayer kept active during combat. Rigour for ranged, Augury for mage, Piety for melee, Eagle Eye as a budget ranged option.", section="prayer", position=6)
    default public OffensivePrayer offensivePrayer() {
        return OffensivePrayer.RIGOUR;
    }

    @ConfigItem(keyName="attackRange", name="Weapon Range (tiles)", description="Maximum tiles your weapon reaches from Nex's hitbox. Twisted bow = 10, Bow of faerdhinen = 9 (10 spec), blowpipe = 5, crossbow = 7. Used to reposition when out of range.", section="combat", position=0)
    @Range(min=1, max=12)
    default public int attackRange() {
        return 10;
    }

    @ConfigItem(keyName="shadowPhaseDistance", name="Shadow Phase Distance", description="During the Shadow phase, keep at least this many tiles from Nex (proximity causes extra shadow damage).", section="combat", position=1)
    @Range(min=0, max=12)
    default public int shadowPhaseDistance() {
        return 8;
    }

    @ConfigItem(keyName="sameWeaponForMinions", name="Same Weapon For Minions", description="Use the main weapon to kill the elemental minions (true for Ranged/Mage). Turn off only for a Melee main that swaps to a ranged weapon for minions.", section="combat", position=2)
    default public boolean sameWeaponForMinions() {
        return true;
    }

    @ConfigItem(keyName="minionWeapon", name="Minion Weapon (optional)", description="If 'Same Weapon For Minions' is off, wield the item whose name contains this text before killing a minion (e.g. 'blowpipe'). Leave blank to never swap.", section="combat", position=3)
    default public String minionWeapon() {
        return "";
    }

    @ConfigItem(keyName="brewName", name="Brew Item", description="Healing item name (partial match). Default 'Saradomin brew'.", section="supplies", position=0)
    default public String brewName() {
        return "Saradomin brew";
    }

    @ConfigItem(keyName="brewHpPct", name="Brew Below HP %", description="Sip a brew dose when HP falls below this percentage of maximum.", section="supplies", position=1)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int brewHpPct() {
        return 50;
    }

    @ConfigItem(keyName="hardFood", name="Emergency Food", description="High-heal food eaten alongside a brew in an emergency (e.g. 'Anglerfish', 'Karambwan'). Leave blank to skip.", section="supplies", position=2)
    default public String hardFood() {
        return "Anglerfish";
    }

    @ConfigItem(keyName="emergencyHpPct", name="Emergency HP %", description="Below this HP percentage, eat the emergency food AND sip a brew in the same tick.", section="supplies", position=3)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int emergencyHpPct() {
        return 35;
    }

    @ConfigItem(keyName="restorePotion", name="Restore Item", description="Prayer/stat restore potion (partial match). Default 'Super restore' ('Sanfew' also works). Restores prayer AND undoes the brew stat-drain.", section="supplies", position=4)
    default public String restorePotion() {
        return "Super restore";
    }

    @ConfigItem(keyName="prayerPct", name="Restore Below Prayer %", description="Sip a restore when prayer points drop below this percentage of maximum.", section="supplies", position=5)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int prayerPct() {
        return 28;
    }

    @ConfigItem(keyName="brewsPerRestore", name="Brews Per Restore", description="After this many brew doses, sip one restore to undo the accumulated stat-drain (independent of the prayer threshold).", section="supplies", position=6)
    @Range(min=1, max=8)
    default public int brewsPerRestore() {
        return 3;
    }

    @ConfigItem(keyName="useStamina", name="Use Stamina", description="Sip a stamina potion when run energy is low.", section="supplies", position=7)
    default public boolean useStamina() {
        return true;
    }

    @ConfigItem(keyName="staminaName", name="Stamina Item", description="Stamina potion name (partial match).", section="supplies", position=8)
    default public String staminaName() {
        return "Stamina";
    }

    @ConfigItem(keyName="staminaPct", name="Stamina Below Energy %", description="Sip a stamina potion when run energy falls below this percentage.", section="supplies", position=9)
    @Range(min=1, max=99)
    @Units(value="%")
    default public int staminaPct() {
        return 30;
    }

    @ConfigItem(keyName="useDivineRanging", name="Use Divine Ranging", description="Sip a divine ranging (or other combat) potion at fight start and re-dose periodically. Note: divine does NOT stop brew stat-drain, so restores still run.", section="supplies", position=10)
    default public boolean useDivineRanging() {
        return false;
    }

    @ConfigItem(keyName="divineName", name="Divine/Combat Potion Item", description="Combat boost potion name (partial match), e.g. 'Divine ranging', 'Ranging', 'Bastion'.", section="supplies", position=11)
    default public String divineName() {
        return "Divine ranging";
    }

    @ConfigItem(keyName="divineRedoseMins", name="Divine Re-dose (min)", description="Re-dose the divine/combat potion this many minutes after the last dose.", section="supplies", position=12)
    @Range(min=1, max=10)
    default public int divineRedoseMins() {
        return 5;
    }

    @ConfigItem(keyName="stopWhenOutOfSupplies", name="Stop When Out Of Supplies", description="Stop the plugin when brews and restores are exhausted (prevents dying with no sustain).", section="supplies", position=13)
    default public boolean stopWhenOutOfSupplies() {
        return true;
    }

    @ConfigItem(keyName="doBloodSacrificeRun", name="Blood Sacrifice Run", description="When marked for a blood sacrifice, run away from Nex so the damage misses (and Nex isn't healed).", section="mechanics", position=0)
    default public boolean doBloodSacrificeRun() {
        return true;
    }

    @ConfigItem(keyName="sacrificeRunTiles", name="Sacrifice Run Tiles", description="How many tiles to run from Nex when marked for a blood sacrifice.", section="mechanics", position=1)
    @Range(min=3, max=12)
    default public int sacrificeRunTiles() {
        return 7;
    }

    @ConfigItem(keyName="stopDpsOnSiphon", name="Stop DPS On Siphon", description="Stop attacking Nex during a Blood Siphon (damage to Nex heals her).", section="mechanics", position=2)
    default public boolean stopDpsOnSiphon() {
        return true;
    }

    @ConfigItem(keyName="attackReavers", name="Attack Blood Reavers", description="During a Blood Siphon, attack the summoned Blood Reavers instead (killing them heals you).", section="mechanics", position=3)
    default public boolean attackReavers() {
        return true;
    }

    @ConfigItem(keyName="handleEmbraceDarkness", name="Handle Embrace Darkness", description="On 'Embrace darkness!', retreat and stay far from Nex for the duration (proximity damage).", section="mechanics", position=4)
    default public boolean handleEmbraceDarkness() {
        return true;
    }

    @ConfigItem(keyName="embraceDistance", name="Embrace Distance", description="Tiles to keep from Nex during Embrace Darkness.", section="mechanics", position=5)
    @Range(min=4, max=14)
    default public int embraceDistance() {
        return 8;
    }

    @ConfigItem(keyName="dodgeShadowSmash", name="Dodge Shadow Smash", description="Step off the shadow tile that spawns under you during the Shadow phase.", section="mechanics", position=6)
    default public boolean dodgeShadowSmash() {
        return true;
    }

    @ConfigItem(keyName="dodgeContainment", name="Dodge Containment", description="On 'Contain this!', step out of the icicle field around Nex.", section="mechanics", position=7)
    default public boolean dodgeContainment() {
        return true;
    }

    @ConfigItem(keyName="breakIcePrison", name="Break Ice Prison", description="On 'Prison of ice!', move out of the ice prison cage before it shatters.", section="mechanics", position=8)
    default public boolean breakIcePrison() {
        return true;
    }

    @ConfigItem(keyName="dodgeSmokeDash", name="Dodge Smoke Dash", description="On the smoke dash telegraph, step sideways out of Nex's dash lane.", section="mechanics", position=9)
    default public boolean dodgeSmokeDash() {
        return true;
    }

    @ConfigItem(keyName="dodgeWrath", name="Dodge Death Wrath", description="Run away from Nex's corpse when she dies in the Zaros phase (5x5 death AoE).", section="mechanics", position=10)
    default public boolean dodgeWrath() {
        return true;
    }

    @ConfigItem(keyName="wrathTiles", name="Wrath Run Tiles", description="Tiles to run from Nex's corpse to avoid the death wrath.", section="mechanics", position=11)
    @Range(min=2, max=8)
    default public int wrathTiles() {
        return 4;
    }

    @ConfigItem(keyName="lootItems", name="Loot Items", description="Comma-separated item names to pick up after a kill (partial match). Leave blank to loot everything within radius.", section="looting", position=0)
    default public String lootItems() {
        return "";
    }

    @ConfigItem(keyName="lootRadius", name="Loot Radius", description="Pick up drops within this many tiles of where Nex died.", section="looting", position=1)
    @Range(min=1, max=15)
    default public int lootRadius() {
        return 8;
    }

    @ConfigItem(keyName="lootValueItemsOnly", name="Skip Looting (fight only)", description="Pure fight-only mode: do not pick up any drops after a kill (go straight to idle).", section="looting", position=2)
    default public boolean skipLooting() {
        return false;
    }
}

