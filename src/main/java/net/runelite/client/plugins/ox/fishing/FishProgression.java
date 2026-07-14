package net.runelite.client.plugins.ox.fishing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.runelite.api.Item;

/** Highest enabled fishing tier; deposit any known / named fish (including noted). */
public final class FishProgression {
    /**
     * All bankable fish IDs: unnoted + noted where known.
     * Includes shrimp/anchovy, tuna, swordfish, lobster, shark, monkfish.
     */
    private static final int[] EXTRA_DEPOSIT_IDS = {
        // shrimp / anchovy
        317, 318, 321, 322,
        // tuna
        359, 360,
        // swordfish
        371, 372,
        // lobster
        377, 378,
        // shark
        383, 384,
        // monkfish
        7944, 7945
    };

    private FishProgression() {
    }

    public static List<FishTier> enabledTiers(FishingConfig config) {
        ArrayList<FishTier> list = new ArrayList<>();
        if (config.enableShrimp()) {
            list.add(FishTier.SHRIMP);
        }
        if (config.enableLobster()) {
            list.add(FishTier.LOBSTER);
        }
        if (config.enableMonkfish()) {
            list.add(FishTier.MONKFISH);
        }
        if (config.enableShark()) {
            list.add(FishTier.SHARK);
        }
        return list;
    }

    public static FishTier resolve(FishingConfig config, int fishingLevel) {
        List<FishTier> enabled = enabledTiers(config);
        if (enabled.isEmpty()) {
            return null;
        }
        FishTier best = null;
        for (FishTier tier : enabled) {
            if (fishingLevel >= tier.requiredLevel) {
                best = tier;
            }
        }
        return best != null ? best : enabled.get(0);
    }

    public static List<Integer> depositFishIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (FishTier tier : FishTier.values()) {
            for (int id : tier.fishItemIds) {
                addId(ids, id);
                addId(ids, id + 1); // common noted pairing
            }
        }
        for (int id : EXTRA_DEPOSIT_IDS) {
            addId(ids, id);
        }
        return ids;
    }

    private static void addId(List<Integer> ids, int id) {
        if (id > 0 && !ids.contains(id)) {
            ids.add(id);
        }
    }

    public static boolean isFishItem(Item item) {
        if (item == null || item.getId() < 0) {
            return false;
        }
        if (isFishItemId(item.getId())) {
            return true;
        }
        return isFishName(item.getName());
    }

    public static boolean isFishItemId(int itemId) {
        if (itemId <= 0) {
            return false;
        }
        for (FishTier tier : FishTier.values()) {
            if (tier.matchesFish(itemId)) {
                return true;
            }
            // noted twin
            for (int id : tier.fishItemIds) {
                if (itemId == id + 1) {
                    return true;
                }
            }
        }
        for (int id : EXTRA_DEPOSIT_IDS) {
            if (id == itemId) {
                return true;
            }
        }
        return false;
    }

    /** Name fallback when Orion remaps item IDs. */
    public static boolean isFishName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (!(lower.contains("raw ") || lower.startsWith("raw") || lower.contains("noted"))) {
            // still allow plain "Shark" / "Monkfish" / "Lobster" / etc.
            if (!(lower.contains("shark")
                || lower.contains("monk")
                || lower.contains("lobster")
                || lower.contains("swordfish")
                || lower.contains("tuna")
                || lower.contains("shrimp")
                || lower.contains("anchov")
                || lower.contains("karambwan"))) {
                return false;
            }
        }
        return lower.contains("shark")
            || lower.contains("monk")
            || lower.contains("lobster")
            || lower.contains("swordfish")
            || lower.contains("tuna")
            || lower.contains("shrimp")
            || lower.contains("anchov")
            || lower.contains("karambwan")
            || lower.contains("herring")
            || lower.contains("sardine")
            || lower.contains("trout")
            || lower.contains("salmon")
            || lower.contains("pike");
    }
}
