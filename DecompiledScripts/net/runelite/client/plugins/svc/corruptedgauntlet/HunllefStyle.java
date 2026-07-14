/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.corruptedgauntlet;

import net.runelite.api.Prayer;

enum HunllefStyle {
    MAGIC("Magic", Prayer.PROTECT_FROM_MAGIC),
    RANGED("Ranged", Prayer.PROTECT_FROM_MISSILES);

    private final String label;
    final Prayer protect;

    private HunllefStyle(String string2, Prayer prayer) {
        this.label = string2;
        this.protect = prayer;
    }

    public String toString() {
        return this.label;
    }
}

