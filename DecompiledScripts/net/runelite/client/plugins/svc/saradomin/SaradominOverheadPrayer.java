/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.saradomin;

import net.runelite.api.Prayer;

public enum SaradominOverheadPrayer {
    PROTECT_FROM_MAGIC("Protect from Magic", Prayer.PROTECT_FROM_MAGIC),
    PROTECT_FROM_MISSILES("Protect from Missiles", Prayer.PROTECT_FROM_MISSILES);

    private final String label;
    public final Prayer prayer;

    private SaradominOverheadPrayer(String string2, Prayer prayer) {
        this.label = string2;
        this.prayer = prayer;
    }

    public String toString() {
        return this.label;
    }
}

