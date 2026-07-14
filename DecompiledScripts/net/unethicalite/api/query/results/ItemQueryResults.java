/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 */
package net.unethicalite.api.query.results;

import java.util.List;
import net.runelite.api.Item;
import net.unethicalite.api.query.results.QueryResults;

public class ItemQueryResults
extends QueryResults<Item, ItemQueryResults> {
    public ItemQueryResults(List<Item> results) {
        super(results);
    }
}

