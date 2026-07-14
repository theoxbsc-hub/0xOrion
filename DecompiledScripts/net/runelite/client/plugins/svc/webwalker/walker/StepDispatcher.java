/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.webwalker.walker;

import net.runelite.api.coords.WorldPoint;

@FunctionalInterface
public interface StepDispatcher {
    public void step(WorldPoint var1, WorldPoint var2);
}

