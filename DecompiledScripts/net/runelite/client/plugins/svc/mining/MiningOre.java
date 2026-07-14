/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.mining;

public enum MiningOre {
    ANY("Any rock (power-mine)", 0, "", new String[0]),
    CLAY("Clay", 1, "clay", "Clay"),
    COPPER("Copper ore", 1, "copper", "Copper ore"),
    TIN("Tin ore", 1, "tin", "Tin ore"),
    IRON("Iron ore", 15, "iron", "Iron ore"),
    SILVER("Silver ore", 20, "silver", "Silver ore"),
    COAL("Coal", 30, "coal", "Coal"),
    SANDSTONE("Sandstone", 35, "sandstone", "Sandstone"),
    GOLD("Gold ore", 40, "gold", "Gold ore"),
    GEM_ROCK("Gem rock (uncut gems)", 40, "gem", "Uncut"),
    GRANITE("Granite", 45, "granite", "Granite"),
    MITHRIL("Mithril ore", 55, "mithril", "Mithril ore"),
    ADAMANTITE("Adamantite ore", 70, "adamant", "Adamantite ore"),
    RUNITE("Runite ore", 85, "runite", "Runite ore"),
    AMETHYST("Amethyst", 92, "amethyst", "Amethyst");

    private final String label;
    private final int tier;
    private final String rockKeyword;
    private final String[] itemNames;

    private MiningOre(String string2, int n2, String string3, String ... stringArray) {
        this.label = string2;
        this.tier = n2;
        this.rockKeyword = string3;
        this.itemNames = stringArray;
    }

    public String[] getItemNames() {
        return this.itemNames;
    }

    public String getRockKeyword() {
        return this.rockKeyword;
    }

    public int getTier() {
        return this.tier;
    }

    public boolean isAny() {
        return this.itemNames.length == 0;
    }

    public String toString() {
        return this.label;
    }
}

