/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.ox.smithing;

public enum SmeltRecipe {
    BRONZE("Bronze bar", 1, 2349, "Bronze bar", new int[]{436, 438}, new String[]{"Copper ore", "Tin ore"}, new int[]{1, 1}),
    IRON("Iron bar", 15, 2351, "Iron bar", new int[]{440}, new String[]{"Iron ore"}, new int[]{1}),
    SILVER("Silver bar", 20, 2355, "Silver bar", new int[]{442}, new String[]{"Silver ore"}, new int[]{1}),
    STEEL("Steel bar", 30, 2353, "Steel bar", new int[]{440, 453}, new String[]{"Iron ore", "Coal"}, new int[]{1, 2}),
    GOLD("Gold bar", 40, 2357, "Gold bar", new int[]{444}, new String[]{"Gold ore"}, new int[]{1}),
    MITHRIL("Mithril bar", 50, 2359, "Mithril bar", new int[]{447, 453}, new String[]{"Mithril ore", "Coal"}, new int[]{1, 4}),
    ADAMANT("Adamantite bar", 70, 2361, "Adamantite bar", new int[]{449, 453}, new String[]{"Adamantite ore", "Coal"}, new int[]{1, 6}),
    RUNITE("Runite bar", 85, 2363, "Runite bar", new int[]{451, 453}, new String[]{"Runite ore", "Coal"}, new int[]{1, 8});

    public final String label;
    public final int level;
    public final int barId;
    public final String barName;
    public final int[] ingredientIds;
    public final String[] ingredientNames;
    public final int[] perBar;

    private SmeltRecipe(String string2, int n2, int n3, String string3, int[] nArray, String[] stringArray, int[] nArray2) {
        this.label = string2;
        this.level = n2;
        this.barId = n3;
        this.barName = string3;
        this.ingredientIds = nArray;
        this.ingredientNames = stringArray;
        this.perBar = nArray2;
    }

    public int oresPerBar() {
        int n = 0;
        for (int n2 : this.perBar) {
            n += n2;
        }
        return n;
    }

    public String toString() {
        return this.label;
    }
}

