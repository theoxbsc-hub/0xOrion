/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.hunter;

public enum ChinchompaType {
    GREY("Grey Chinchompa", 2910, "chinchompa", 10033),
    RED("Red Chinchompa", -1, "red chinchompa", 10034),
    BLACK("Black Chinchompa", -1, "black chinchompa", 11959);

    private final String label;
    private final int npcId;
    private final String nameKeyword;
    private final int caughtItemId;

    private ChinchompaType(String string2, int n2, String string3, int n3) {
        this.label = string2;
        this.npcId = n2;
        this.nameKeyword = string3;
        this.caughtItemId = n3;
    }

    public int npcId() {
        return this.npcId;
    }

    public String nameKeyword() {
        return this.nameKeyword;
    }

    public int caughtItemId() {
        return this.caughtItemId;
    }

    public String toString() {
        return this.label;
    }
}

