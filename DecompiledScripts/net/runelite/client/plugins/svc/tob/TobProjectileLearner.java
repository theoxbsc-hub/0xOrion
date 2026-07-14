/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 */
package net.runelite.client.plugins.svc.tob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.api.Prayer;

class TobProjectileLearner {
    static final int BIG_HIT = 25;
    static final int STRIKES_TO_FLIP = 2;
    static final int MAX_FLIPS_PER_ID = 2;
    private final Map<Integer, Style> baseStyles;
    private final Map<Integer, Style> learned = new ConcurrentHashMap<Integer, Style>();
    private final Map<Integer, Integer> strikes = new ConcurrentHashMap<Integer, Integer>();
    private final Map<Integer, Integer> flipsById = new ConcurrentHashMap<Integer, Integer>();
    private volatile int flips = 0;

    TobProjectileLearner(Map<Integer, Style> map) {
        this.baseStyles = map;
    }

    Style styleOf(int n) {
        Style style = this.learned.get(n);
        if (style != null) {
            return style;
        }
        return this.baseStyles.getOrDefault(n, Style.NONE);
    }

    boolean recordPrayedHit(int n, int n2) {
        if (n2 < 25) {
            return false;
        }
        Style style = this.styleOf(n);
        if (style == Style.NONE) {
            return false;
        }
        if (this.flipsById.getOrDefault(n, 0) >= 2) {
            return false;
        }
        int n3 = this.strikes.merge(n, 1, Integer::sum);
        if (n3 >= 2) {
            this.learned.put(n, style.flipped());
            this.strikes.remove(n);
            this.flipsById.merge(n, 1, Integer::sum);
            ++this.flips;
            return true;
        }
        return false;
    }

    boolean isUnblockable(int n) {
        return this.flipsById.getOrDefault(n, 0) >= 2;
    }

    int flipCount() {
        return this.flips;
    }

    String describeFlips() {
        if (this.learned.isEmpty()) {
            return "none";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, Style> entry : this.learned.entrySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("  ");
            }
            stringBuilder.append(entry.getKey()).append('\u2192').append((Object)entry.getValue());
        }
        return stringBuilder.toString();
    }

    static enum Style {
        RANGED(Prayer.PROTECT_FROM_MISSILES),
        MAGIC(Prayer.PROTECT_FROM_MAGIC),
        NONE(null);

        final Prayer protect;

        private Style(Prayer prayer) {
            this.protect = prayer;
        }

        Style flipped() {
            if (this == RANGED) {
                return MAGIC;
            }
            if (this == MAGIC) {
                return RANGED;
            }
            return NONE;
        }
    }
}

