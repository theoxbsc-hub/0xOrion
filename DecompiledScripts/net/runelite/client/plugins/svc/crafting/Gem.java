/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

enum Gem {
    OPAL("Uncut opal", "Opal", 1, 15.0),
    JADE("Uncut jade", "Jade", 13, 20.0),
    RED_TOPAZ("Uncut red topaz", "Red topaz", 16, 25.0),
    SAPPHIRE("Uncut sapphire", "Sapphire", 20, 50.0),
    EMERALD("Uncut emerald", "Emerald", 27, 67.5),
    RUBY("Uncut ruby", "Ruby", 34, 85.0),
    DIAMOND("Uncut diamond", "Diamond", 43, 107.5),
    DRAGONSTONE("Uncut dragonstone", "Dragonstone", 55, 137.5),
    ONYX("Uncut onyx", "Onyx", 67, 167.5),
    ZENYTE("Uncut zenyte", "Zenyte", 89, 200.0);

    final String uncutName;
    final String cutName;
    final int level;
    final double xp;

    private Gem(String string2, String string3, int n2, double d) {
        this.uncutName = string2;
        this.cutName = string3;
        this.level = n2;
        this.xp = d;
    }

    static List<Gem> byLevelDesc() {
        ArrayList<Gem> arrayList = new ArrayList<Gem>(Arrays.asList(Gem.values()));
        arrayList.sort((gem, gem2) -> Integer.compare(gem2.level, gem.level));
        return Collections.unmodifiableList(arrayList);
    }
}

