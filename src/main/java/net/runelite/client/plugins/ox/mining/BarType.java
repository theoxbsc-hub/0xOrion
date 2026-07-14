/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.ox.mining;

import net.runelite.client.plugins.ox.mining.MiningOre;

public enum BarType {
    NONE("Off \u2014 mine the selected ore", new Ingredient[0]),
    BRONZE("Bronze bar", BarType.req(MiningOre.COPPER, 1), BarType.req(MiningOre.TIN, 1)),
    IRON("Iron bar", BarType.req(MiningOre.IRON, 1)),
    SILVER("Silver bar", BarType.req(MiningOre.SILVER, 1)),
    STEEL("Steel bar", BarType.req(MiningOre.IRON, 1), BarType.req(MiningOre.COAL, 2)),
    GOLD("Gold bar", BarType.req(MiningOre.GOLD, 1)),
    MITHRIL("Mithril bar", BarType.req(MiningOre.MITHRIL, 1), BarType.req(MiningOre.COAL, 4)),
    ADAMANT("Adamant bar", BarType.req(MiningOre.ADAMANTITE, 1), BarType.req(MiningOre.COAL, 6)),
    RUNE("Rune bar", BarType.req(MiningOre.RUNITE, 1), BarType.req(MiningOre.COAL, 8));

    private final String label;
    private final Ingredient[] ingredients;

    private BarType(String string2, Ingredient ... ingredientArray) {
        this.label = string2;
        this.ingredients = ingredientArray;
    }

    private static Ingredient req(MiningOre miningOre, int n) {
        return new Ingredient(miningOre, n);
    }

    public Ingredient[] ingredients() {
        return this.ingredients;
    }

    public boolean isNone() {
        return this.ingredients.length == 0;
    }

    public String toString() {
        return this.label;
    }

    public static final class Ingredient {
        public final MiningOre ore;
        public final int perBar;

        Ingredient(MiningOre miningOre, int n) {
            this.ore = miningOre;
            this.perBar = n;
        }
    }
}

