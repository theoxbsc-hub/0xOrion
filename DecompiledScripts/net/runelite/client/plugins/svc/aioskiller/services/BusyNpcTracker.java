/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Actor
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;

public final class BusyNpcTracker {
    private static final long COOLDOWN_MS = 5000L;
    private final Map<Integer, Long> busy = new ConcurrentHashMap<Integer, Long>();
    private volatile NPC lastAttempted = null;

    public void markAttempted(NPC nPC) {
        this.lastAttempted = nPC;
    }

    public void onChat(String string) {
        if (string == null || !string.contains("already in combat")) {
            return;
        }
        NPC nPC = this.lastAttempted;
        if (nPC != null) {
            this.busy.put(nPC.getIndex(), System.currentTimeMillis() + 5000L);
        }
        this.lastAttempted = null;
    }

    public boolean isBusy(NPC nPC, Player player) {
        Actor actor;
        if (nPC == null) {
            return false;
        }
        Long l = this.busy.get(nPC.getIndex());
        if (l != null) {
            if (System.currentTimeMillis() >= l) {
                this.busy.remove(nPC.getIndex());
            } else {
                return true;
            }
        }
        return (actor = nPC.getInteracting()) != null && actor != player;
    }

    public void clear() {
        this.busy.clear();
        this.lastAttempted = null;
    }
}

