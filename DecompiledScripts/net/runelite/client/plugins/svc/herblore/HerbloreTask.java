/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.herblore;

import java.util.Collections;
import java.util.List;
import net.runelite.client.plugins.svc.herblore.HerbloreConstants;
import net.runelite.client.plugins.svc.herblore.Recipe;
import net.runelite.client.plugins.svc.herblore.RecipeKind;

public enum HerbloreTask {
    FINISHED_POTIONS("Finished potions", RecipeKind.FINISHED),
    UNFINISHED_POTIONS("Unfinished potions", RecipeKind.UNF),
    CLEAN_HERBS("Clean grimy herbs", RecipeKind.CLEAN),
    NONE("\u2014 skip \u2014", null);

    private final String label;
    final RecipeKind kind;

    private HerbloreTask(String string2, RecipeKind recipeKind) {
        this.label = string2;
        this.kind = recipeKind;
    }

    List<Recipe> table() {
        if (this.kind == null) {
            return Collections.emptyList();
        }
        switch (this.kind) {
            case FINISHED: {
                return HerbloreConstants.FINISHED;
            }
            case UNF: {
                return HerbloreConstants.UNF;
            }
            case CLEAN: {
                return HerbloreConstants.CLEAN;
            }
        }
        return Collections.emptyList();
    }

    public String toString() {
        return this.label;
    }
}

