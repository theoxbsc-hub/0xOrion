/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.inferno;

import net.runelite.api.Prayer;

enum InfernoCaste {
    MAGIC(0, Prayer.PROTECT_FROM_MAGIC),
    RANGED(1, Prayer.PROTECT_FROM_MISSILES),
    MELEE(2, Prayer.PROTECT_FROM_MELEE),
    UNKNOWN(3, null);

    final int priority;
    final Prayer protect;

    private InfernoCaste(int n2, Prayer prayer) {
        this.priority = n2;
        this.protect = prayer;
    }
}

