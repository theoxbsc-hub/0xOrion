/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.combatoverhaul;

import net.runelite.api.Prayer;

public enum GearPrayer {
    NONE("None", null),
    PROTECT_FROM_MELEE("Protect from Melee", Prayer.PROTECT_FROM_MELEE),
    PROTECT_FROM_MISSILES("Protect from Missiles", Prayer.PROTECT_FROM_MISSILES),
    PROTECT_FROM_MAGIC("Protect from Magic", Prayer.PROTECT_FROM_MAGIC),
    RETRIBUTION("Retribution", Prayer.RETRIBUTION),
    REDEMPTION("Redemption", Prayer.REDEMPTION),
    SMITE("Smite", Prayer.SMITE),
    BURST_OF_STRENGTH("Burst of Strength", Prayer.BURST_OF_STRENGTH),
    SUPERHUMAN_STRENGTH("Superhuman Strength", Prayer.SUPERHUMAN_STRENGTH),
    ULTIMATE_STRENGTH("Ultimate Strength", Prayer.ULTIMATE_STRENGTH),
    CHIVALRY("Chivalry", Prayer.CHIVALRY),
    PIETY("Piety", Prayer.PIETY),
    SHARP_EYE("Sharp Eye", Prayer.SHARP_EYE),
    HAWK_EYE("Hawk Eye", Prayer.HAWK_EYE),
    EAGLE_EYE("Eagle Eye", Prayer.EAGLE_EYE),
    RIGOUR("Rigour", Prayer.RIGOUR),
    MYSTIC_WILL("Mystic Will", Prayer.MYSTIC_WILL),
    MYSTIC_LORE("Mystic Lore", Prayer.MYSTIC_LORE),
    MYSTIC_MIGHT("Mystic Might", Prayer.MYSTIC_MIGHT),
    AUGURY("Augury", Prayer.AUGURY),
    THICK_SKIN("Thick Skin", Prayer.THICK_SKIN),
    ROCK_SKIN("Rock Skin", Prayer.ROCK_SKIN),
    STEEL_SKIN("Steel Skin", Prayer.STEEL_SKIN),
    PROTECT_ITEM("Protect Item", Prayer.PROTECT_ITEM),
    PRESERVE("Preserve", Prayer.PRESERVE),
    RAPID_RESTORE("Rapid Restore", Prayer.RAPID_RESTORE),
    RAPID_HEAL("Rapid Heal", Prayer.RAPID_HEAL);

    private final String label;
    public final Prayer prayer;

    private GearPrayer(String string2, Prayer prayer) {
        this.label = string2;
        this.prayer = prayer;
    }

    public String toString() {
        return this.label;
    }
}

