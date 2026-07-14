/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.herblore;

import net.runelite.client.plugins.svc.herblore.ItemMatcher;

final class Ingredient {
    final ItemMatcher matcher;
    final int perResult;
    final String displayName;

    Ingredient(ItemMatcher itemMatcher, int n, String string) {
        this.matcher = itemMatcher;
        this.perResult = n;
        this.displayName = string;
    }
}

