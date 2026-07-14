package net.runelite.client.plugins.ox.woodcutting;

import java.util.ArrayList;
import java.util.List;

/** Resolves which tree to chop, thieving-style: highest enabled tier you qualify for. */
public final class TreeProgression {
    private TreeProgression() {
    }

    public static List<TreeTier> enabledTiers(WoodcuttingConfig config) {
        ArrayList<TreeTier> list = new ArrayList<>();
        if (config.enableNormal()) {
            list.add(TreeTier.NORMAL);
        }
        if (config.enableOak()) {
            list.add(TreeTier.OAK);
        }
        if (config.enableWillow()) {
            list.add(TreeTier.WILLOW);
        }
        if (config.enableMaple()) {
            list.add(TreeTier.MAPLE);
        }
        if (config.enableYew()) {
            list.add(TreeTier.YEW);
        }
        if (config.enableMagic()) {
            list.add(TreeTier.MAGIC);
        }
        return list;
    }

    /**
     * Highest enabled tier whose requirement is &lt;= woodcuttingLevel.
     * If nothing qualifies yet, returns the lowest enabled tier (wait / underleveled).
     */
    public static TreeTier resolve(WoodcuttingConfig config, int woodcuttingLevel) {
        List<TreeTier> enabled = enabledTiers(config);
        if (enabled.isEmpty()) {
            return null;
        }
        TreeTier best = null;
        for (TreeTier tier : enabled) {
            if (woodcuttingLevel >= tier.requiredLevel) {
                best = tier;
            }
        }
        return best != null ? best : enabled.get(0);
    }

    public static int[] treeObjectIds(TreeTier tier, WoodcuttingConfig config) {
        int override = objectOverride(tier, config);
        if (override > 0) {
            return new int[]{override};
        }
        return tier.treeObjectIds;
    }

    public static int logItemId(TreeTier tier, WoodcuttingConfig config) {
        int override = logOverride(tier, config);
        return override > 0 ? override : tier.logItemId;
    }

    /**
     * Every known log item ID (all progression tiers + ID overrides), whether enabled or not.
     * Banking/dropping must clear leftovers after a tier is switched off or upgraded.
     */
    public static List<Integer> depositLogIds(WoodcuttingConfig config) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (TreeTier tier : TreeTier.values()) {
            int id = logItemId(tier, config);
            if (id > 0 && !ids.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    /** True if this inventory item is a log we should bank/drop. */
    public static boolean isLogItem(net.runelite.api.Item item, WoodcuttingConfig config) {
        if (item == null || item.getId() < 0) {
            return false;
        }
        int itemId = item.getId();
        for (TreeTier tier : TreeTier.values()) {
            if (logItemId(tier, config) == itemId) {
                return true;
            }
        }
        String name = item.getName();
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        // "Oak logs", "Logs", "Magic logs" — avoid matching "longbow"
        return lower.contains("logs") || lower.equals("log");
    }


    private static int objectOverride(TreeTier tier, WoodcuttingConfig config) {
        switch (tier) {
            case NORMAL:
                return config.normalTreeObjectId();
            case OAK:
                return config.oakTreeObjectId();
            case WILLOW:
                return config.willowTreeObjectId();
            case MAPLE:
                return config.mapleTreeObjectId();
            case YEW:
                return config.yewTreeObjectId();
            case MAGIC:
                return config.magicTreeObjectId();
            default:
                return 0;
        }
    }

    private static int logOverride(TreeTier tier, WoodcuttingConfig config) {
        switch (tier) {
            case NORMAL:
                return config.normalLogItemId();
            case OAK:
                return config.oakLogItemId();
            case WILLOW:
                return config.willowLogItemId();
            case MAPLE:
                return config.mapleLogItemId();
            case YEW:
                return config.yewLogItemId();
            case MAGIC:
                return config.magicLogItemId();
            default:
                return 0;
        }
    }
}
