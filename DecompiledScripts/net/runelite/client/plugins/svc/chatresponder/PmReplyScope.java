/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.chatresponder;

public enum PmReplyScope {
    ALL("All PMs (filters apply)"),
    WHITELIST_ONLY("Whitelist only");

    private final String label;

    private PmReplyScope(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

