/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Keybind
 *  net.runelite.client.config.Range
 *  net.runelite.client.config.Units
 */
package net.runelite.client.plugins.svc.combatoverhaul;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.combatoverhaul.GearPrayer;
import net.runelite.client.plugins.svc.combatoverhaul.GearSpell;
import net.runelite.client.plugins.svc.combatoverhaul.PresetSlot;

@ConfigGroup(value="combatOverhaul")
public interface CombatOverhaulConfig
extends Config {
    public static final String GROUP = "combatOverhaul";
    @ConfigSection(name="Gear Switcher", description="Hotkey-driven gear/prayer/spec presets", position=0, closedByDefault=false)
    public static final String gearSwitcherSection = "gearSwitcher";
    @ConfigSection(name="Preset 1", description="Gear-switch preset 1", position=1, closedByDefault=true)
    public static final String preset1Section = "preset1";
    @ConfigSection(name="Preset 2", description="Gear-switch preset 2", position=2, closedByDefault=true)
    public static final String preset2Section = "preset2";
    @ConfigSection(name="Preset 3", description="Gear-switch preset 3", position=3, closedByDefault=true)
    public static final String preset3Section = "preset3";
    @ConfigSection(name="Preset 4", description="Gear-switch preset 4", position=4, closedByDefault=true)
    public static final String preset4Section = "preset4";
    @ConfigSection(name="Preset 5", description="Gear-switch preset 5", position=5, closedByDefault=true)
    public static final String preset5Section = "preset5";
    @ConfigSection(name="Preset 6", description="Gear-switch preset 6", position=6, closedByDefault=true)
    public static final String preset6Section = "preset6";

    @ConfigItem(keyName="gearSwitcherEnabled", name="Enable Gear Switcher", description="Master toggle for the gear-switch hotkeys, the CTRL+right-click 'Add to preset' menu, and snapshots.", section="gearSwitcher", position=0)
    default public boolean gearSwitcherEnabled() {
        return true;
    }

    @ConfigItem(keyName="fastGearSwap", name="Fast Gear Swap (1 tick)", description="Equip every item in the preset together in a single game tick (a burst) instead of one item per 'Delay Between Actions' step. Gives a 1-2 tick swap. Turn off for a slower, more human-paced swap.", section="gearSwitcher", position=1)
    default public boolean fastGearSwap() {
        return true;
    }

    @ConfigItem(keyName="fastSwapItemDelayMs", name="Fast Swap: Item Spacing", description="Micro-delay between each item's equip packet inside a fast-swap burst. 0 = fire them as fast as possible. A small value (e.g. 15-25ms) still lands within one tick but is gentler on the server.", section="gearSwitcher", position=2)
    @Range(min=0, max=100)
    @Units(value="ms")
    default public int fastSwapItemDelayMs() {
        return 20;
    }

    @ConfigItem(keyName="equipDelayMs", name="Delay Between Actions", description="Delay between switch steps (prayers, spec, spell \u2014 and between each item when Fast Gear Swap is OFF). Higher = more human, slower.", section="gearSwitcher", position=3)
    @Range(min=0, max=1000)
    @Units(value="ms")
    default public int equipDelayMs() {
        return 150;
    }

    @ConfigItem(keyName="capturePreset", name="Capture Into Preset", description="Which preset receives items added via CTRL+right-click or the 'Snapshot worn gear' button/hotkey.", section="gearSwitcher", position=4)
    default public PresetSlot capturePreset() {
        return PresetSlot.PRESET_1;
    }

    @ConfigItem(keyName="requireCtrlForMenu", name="Require CTRL For 'Add'", description="When on, the 'Add to preset' right-click option only appears while CTRL is held (CTRL+right-click). When off, it always appears.", section="gearSwitcher", position=5)
    default public boolean requireCtrlForMenu() {
        return true;
    }

    @ConfigItem(keyName="snapshotHotkey", name="Snapshot Hotkey", description="Press to capture everything you're currently wearing into the 'Capture Into Preset' slot.", section="gearSwitcher", position=6)
    default public Keybind snapshotHotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName="specAutoAttack", name="Spec: Auto-Attack Target", description="After enabling the special attack, automatically click your current combat target so the spec fires immediately.", section="gearSwitcher", position=7)
    default public boolean specAutoAttack() {
        return true;
    }

    @ConfigItem(keyName="specMinEnergyPercent", name="Spec: Min Energy", description="Only enable the special attack when spec energy is at least this %.", section="gearSwitcher", position=8)
    @Range(min=0, max=100)
    @Units(value="%")
    default public int specMinEnergyPercent() {
        return 100;
    }

    @ConfigItem(keyName="spellAutoCast", name="Spell: Auto-Cast On Target", description="When on, a preset's spell is cast directly on your current target. When off, the spell is just selected/highlighted so your next click casts it.", section="gearSwitcher", position=9)
    default public boolean spellAutoCast() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show Overlay", description="Show the Gear Switcher status overlay (with the clickable Snapshot button).", section="gearSwitcher", position=10)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="preset1Name", name="Name", description="Label for this preset.", section="preset1", position=0)
    default public String preset1Name() {
        return "Preset 1";
    }

    @ConfigItem(keyName="preset1Hotkey", name="Hotkey", description="Press to equip this preset.", section="preset1", position=1)
    default public Keybind preset1Hotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName="preset1Items", name="Items", description="Gear list as 'id:name' tokens (auto-filled by CTRL+right-click / snapshot; hand-editable).", section="preset1", position=2)
    default public String preset1Items() {
        return "";
    }

    @ConfigItem(keyName="preset1OffensivePrayer", name="Offensive Prayer", description="Prayer to enable (e.g. Rigour/Piety/Augury).", section="preset1", position=3)
    default public GearPrayer preset1OffensivePrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset1ProtectionPrayer", name="Protection Prayer", description="Prayer to enable (e.g. Protect from Magic).", section="preset1", position=4)
    default public GearPrayer preset1ProtectionPrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset1Spec", name="Special Attack", description="Enable the special attack after equipping this preset.", section="preset1", position=5)
    default public boolean preset1Spec() {
        return false;
    }

    @ConfigItem(keyName="preset1Spell", name="Spell", description="Combat spell to cast/select after equipping (behaviour set by 'Spell: Auto-Cast On Target').", section="preset1", position=6)
    default public GearSpell preset1Spell() {
        return GearSpell.NONE;
    }

    @ConfigItem(keyName="preset1ContinueAttack", name="Continue Attacking", description="After the switch, automatically click your current target to resume combat. Skipped if the preset already engaged via spec auto-attack or a cast spell.", section="preset1", position=7)
    default public boolean preset1ContinueAttack() {
        return false;
    }

    @ConfigItem(keyName="preset2Name", name="Name", description="Label for this preset.", section="preset2", position=0)
    default public String preset2Name() {
        return "Preset 2";
    }

    @ConfigItem(keyName="preset2Hotkey", name="Hotkey", description="Press to equip this preset.", section="preset2", position=1)
    default public Keybind preset2Hotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName="preset2Items", name="Items", description="Gear list as 'id:name' tokens (auto-filled by CTRL+right-click / snapshot; hand-editable).", section="preset2", position=2)
    default public String preset2Items() {
        return "";
    }

    @ConfigItem(keyName="preset2OffensivePrayer", name="Offensive Prayer", description="Prayer to enable (e.g. Rigour/Piety/Augury).", section="preset2", position=3)
    default public GearPrayer preset2OffensivePrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset2ProtectionPrayer", name="Protection Prayer", description="Prayer to enable (e.g. Protect from Magic).", section="preset2", position=4)
    default public GearPrayer preset2ProtectionPrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset2Spec", name="Special Attack", description="Enable the special attack after equipping this preset.", section="preset2", position=5)
    default public boolean preset2Spec() {
        return false;
    }

    @ConfigItem(keyName="preset2Spell", name="Spell", description="Combat spell to cast/select after equipping (behaviour set by 'Spell: Auto-Cast On Target').", section="preset2", position=6)
    default public GearSpell preset2Spell() {
        return GearSpell.NONE;
    }

    @ConfigItem(keyName="preset2ContinueAttack", name="Continue Attacking", description="After the switch, automatically click your current target to resume combat. Skipped if the preset already engaged via spec auto-attack or a cast spell.", section="preset2", position=7)
    default public boolean preset2ContinueAttack() {
        return false;
    }

    @ConfigItem(keyName="preset3Name", name="Name", description="Label for this preset.", section="preset3", position=0)
    default public String preset3Name() {
        return "Preset 3";
    }

    @ConfigItem(keyName="preset3Hotkey", name="Hotkey", description="Press to equip this preset.", section="preset3", position=1)
    default public Keybind preset3Hotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName="preset3Items", name="Items", description="Gear list as 'id:name' tokens (auto-filled by CTRL+right-click / snapshot; hand-editable).", section="preset3", position=2)
    default public String preset3Items() {
        return "";
    }

    @ConfigItem(keyName="preset3OffensivePrayer", name="Offensive Prayer", description="Prayer to enable (e.g. Rigour/Piety/Augury).", section="preset3", position=3)
    default public GearPrayer preset3OffensivePrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset3ProtectionPrayer", name="Protection Prayer", description="Prayer to enable (e.g. Protect from Magic).", section="preset3", position=4)
    default public GearPrayer preset3ProtectionPrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset3Spec", name="Special Attack", description="Enable the special attack after equipping this preset.", section="preset3", position=5)
    default public boolean preset3Spec() {
        return false;
    }

    @ConfigItem(keyName="preset3Spell", name="Spell", description="Combat spell to cast/select after equipping (behaviour set by 'Spell: Auto-Cast On Target').", section="preset3", position=6)
    default public GearSpell preset3Spell() {
        return GearSpell.NONE;
    }

    @ConfigItem(keyName="preset3ContinueAttack", name="Continue Attacking", description="After the switch, automatically click your current target to resume combat. Skipped if the preset already engaged via spec auto-attack or a cast spell.", section="preset3", position=7)
    default public boolean preset3ContinueAttack() {
        return false;
    }

    @ConfigItem(keyName="preset4Name", name="Name", description="Label for this preset.", section="preset4", position=0)
    default public String preset4Name() {
        return "Preset 4";
    }

    @ConfigItem(keyName="preset4Hotkey", name="Hotkey", description="Press to equip this preset.", section="preset4", position=1)
    default public Keybind preset4Hotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName="preset4Items", name="Items", description="Gear list as 'id:name' tokens (auto-filled by CTRL+right-click / snapshot; hand-editable).", section="preset4", position=2)
    default public String preset4Items() {
        return "";
    }

    @ConfigItem(keyName="preset4OffensivePrayer", name="Offensive Prayer", description="Prayer to enable (e.g. Rigour/Piety/Augury).", section="preset4", position=3)
    default public GearPrayer preset4OffensivePrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset4ProtectionPrayer", name="Protection Prayer", description="Prayer to enable (e.g. Protect from Magic).", section="preset4", position=4)
    default public GearPrayer preset4ProtectionPrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset4Spec", name="Special Attack", description="Enable the special attack after equipping this preset.", section="preset4", position=5)
    default public boolean preset4Spec() {
        return false;
    }

    @ConfigItem(keyName="preset4Spell", name="Spell", description="Combat spell to cast/select after equipping (behaviour set by 'Spell: Auto-Cast On Target').", section="preset4", position=6)
    default public GearSpell preset4Spell() {
        return GearSpell.NONE;
    }

    @ConfigItem(keyName="preset4ContinueAttack", name="Continue Attacking", description="After the switch, automatically click your current target to resume combat. Skipped if the preset already engaged via spec auto-attack or a cast spell.", section="preset4", position=7)
    default public boolean preset4ContinueAttack() {
        return false;
    }

    @ConfigItem(keyName="preset5Name", name="Name", description="Label for this preset.", section="preset5", position=0)
    default public String preset5Name() {
        return "Preset 5";
    }

    @ConfigItem(keyName="preset5Hotkey", name="Hotkey", description="Press to equip this preset.", section="preset5", position=1)
    default public Keybind preset5Hotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName="preset5Items", name="Items", description="Gear list as 'id:name' tokens (auto-filled by CTRL+right-click / snapshot; hand-editable).", section="preset5", position=2)
    default public String preset5Items() {
        return "";
    }

    @ConfigItem(keyName="preset5OffensivePrayer", name="Offensive Prayer", description="Prayer to enable (e.g. Rigour/Piety/Augury).", section="preset5", position=3)
    default public GearPrayer preset5OffensivePrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset5ProtectionPrayer", name="Protection Prayer", description="Prayer to enable (e.g. Protect from Magic).", section="preset5", position=4)
    default public GearPrayer preset5ProtectionPrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset5Spec", name="Special Attack", description="Enable the special attack after equipping this preset.", section="preset5", position=5)
    default public boolean preset5Spec() {
        return false;
    }

    @ConfigItem(keyName="preset5Spell", name="Spell", description="Combat spell to cast/select after equipping (behaviour set by 'Spell: Auto-Cast On Target').", section="preset5", position=6)
    default public GearSpell preset5Spell() {
        return GearSpell.NONE;
    }

    @ConfigItem(keyName="preset5ContinueAttack", name="Continue Attacking", description="After the switch, automatically click your current target to resume combat. Skipped if the preset already engaged via spec auto-attack or a cast spell.", section="preset5", position=7)
    default public boolean preset5ContinueAttack() {
        return false;
    }

    @ConfigItem(keyName="preset6Name", name="Name", description="Label for this preset.", section="preset6", position=0)
    default public String preset6Name() {
        return "Preset 6";
    }

    @ConfigItem(keyName="preset6Hotkey", name="Hotkey", description="Press to equip this preset.", section="preset6", position=1)
    default public Keybind preset6Hotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName="preset6Items", name="Items", description="Gear list as 'id:name' tokens (auto-filled by CTRL+right-click / snapshot; hand-editable).", section="preset6", position=2)
    default public String preset6Items() {
        return "";
    }

    @ConfigItem(keyName="preset6OffensivePrayer", name="Offensive Prayer", description="Prayer to enable (e.g. Rigour/Piety/Augury).", section="preset6", position=3)
    default public GearPrayer preset6OffensivePrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset6ProtectionPrayer", name="Protection Prayer", description="Prayer to enable (e.g. Protect from Magic).", section="preset6", position=4)
    default public GearPrayer preset6ProtectionPrayer() {
        return GearPrayer.NONE;
    }

    @ConfigItem(keyName="preset6Spec", name="Special Attack", description="Enable the special attack after equipping this preset.", section="preset6", position=5)
    default public boolean preset6Spec() {
        return false;
    }

    @ConfigItem(keyName="preset6Spell", name="Spell", description="Combat spell to cast/select after equipping (behaviour set by 'Spell: Auto-Cast On Target').", section="preset6", position=6)
    default public GearSpell preset6Spell() {
        return GearSpell.NONE;
    }

    @ConfigItem(keyName="preset6ContinueAttack", name="Continue Attacking", description="After the switch, automatically click your current target to resume combat. Skipped if the preset already engaged via spec auto-attack or a cast spell.", section="preset6", position=7)
    default public boolean preset6ContinueAttack() {
        return false;
    }
}

