/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.combat;

import net.runelite.api.Prayer;

public enum OffensivePrayer {
    NONE("None", null),
    BURST_OF_STRENGTH("Burst of Strength", Prayer.BURST_OF_STRENGTH),
    SUPERHUMAN_STRENGTH("Superhuman Strength", Prayer.SUPERHUMAN_STRENGTH),
    ULTIMATE_STRENGTH("Ultimate Strength", Prayer.ULTIMATE_STRENGTH),
    CLARITY_OF_THOUGHT("Clarity of Thought", Prayer.CLARITY_OF_THOUGHT),
    IMPROVED_REFLEXES("Improved Reflexes", Prayer.IMPROVED_REFLEXES),
    INCREDIBLE_REFLEXES("Incredible Reflexes", Prayer.INCREDIBLE_REFLEXES),
    PIETY("Piety", Prayer.PIETY),
    SHARP_EYE("Sharp Eye", Prayer.SHARP_EYE),
    HAWK_EYE("Hawk Eye", Prayer.HAWK_EYE),
    EAGLE_EYE("Eagle Eye", Prayer.EAGLE_EYE),
    RIGOUR("Rigour", Prayer.RIGOUR),
    MYSTIC_WILL("Mystic Will", Prayer.MYSTIC_WILL),
    MYSTIC_LORE("Mystic Lore", Prayer.MYSTIC_LORE),
    MYSTIC_MIGHT("Mystic Might", Prayer.MYSTIC_MIGHT),
    AUGURY("Augury", Prayer.AUGURY);

    private final String name;
    public final Prayer prayer;

    private OffensivePrayer(String string2, Prayer prayer) {
        this.name = string2;
        this.prayer = prayer;
    }

    public String toString() {
        return this.name;
    }
}

