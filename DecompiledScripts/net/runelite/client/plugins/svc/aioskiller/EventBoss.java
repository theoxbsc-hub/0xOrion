/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.aioskiller;

import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;

public enum EventBoss {
    VOTE_BOSS("Vote boss", "::vb"),
    WORLD_BOSS("World boss", "::wb");

    private final String label;
    private final String command;

    private EventBoss(String string2, String string3) {
        this.label = string2;
        this.command = string3;
    }

    public String label() {
        return this.label;
    }

    public String command() {
        return this.command;
    }

    public static EventBoss detect(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        boolean bl = false;
        for (String string2 : AioSkillerConstants.EVENT_ANNOUNCE_KEYWORDS) {
            if (!string.contains(string2)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return null;
        }
        if (string.contains(EventBoss.WORLD_BOSS.command)) {
            return WORLD_BOSS;
        }
        if (string.contains(EventBoss.VOTE_BOSS.command)) {
            return VOTE_BOSS;
        }
        return null;
    }
}

