/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.combat;

import net.runelite.api.Prayer;

public enum DefensivePrayer {
    NONE("None", null),
    PROTECT_FROM_MELEE("Protect from Melee", Prayer.PROTECT_FROM_MELEE),
    PROTECT_FROM_MISSILES("Protect from Missiles", Prayer.PROTECT_FROM_MISSILES),
    PROTECT_FROM_MAGIC("Protect from Magic", Prayer.PROTECT_FROM_MAGIC),
    RETRIBUTION("Retribution", Prayer.RETRIBUTION),
    SMITE("Smite", Prayer.SMITE),
    REDEMPTION("Redemption", Prayer.REDEMPTION);

    private final String name;
    public final Prayer prayer;

    private DefensivePrayer(String string2, Prayer prayer) {
        this.name = string2;
        this.prayer = prayer;
    }

    public String toString() {
        return this.name;
    }
}

