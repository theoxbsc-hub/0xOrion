/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.herblore;

import java.util.Arrays;
import java.util.List;
import net.runelite.client.plugins.svc.herblore.Ingredient;
import net.runelite.client.plugins.svc.herblore.RecipeKind;

final class Recipe {
    final RecipeKind kind;
    final String result;
    final int level;
    final double xp;
    final List<Ingredient> inputs;
    final int combineTargetIndex;
    final int rankTier;

    Recipe(RecipeKind recipeKind, String string, int n, double d, int n2, int n3, Ingredient ... ingredientArray) {
        this.kind = recipeKind;
        this.result = string;
        this.level = n;
        this.xp = d;
        this.combineTargetIndex = n2;
        this.rankTier = n3;
        this.inputs = Arrays.asList(ingredientArray);
    }

    int unitsPerPotion() {
        int n = 0;
        for (Ingredient ingredient : this.inputs) {
            n += ingredient.perResult;
        }
        return Math.max(1, n);
    }

    int potionsPerInv() {
        return Math.max(1, 28 / this.unitsPerPotion());
    }

    Ingredient primary() {
        return this.inputs.get(0);
    }

    Ingredient combineTarget() {
        return this.inputs.get(Math.min(this.combineTargetIndex, this.inputs.size() - 1));
    }
}

