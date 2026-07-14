package net.runelite.client.plugins.ox.woodcutting;

/**
 * WC progression ladder (vanilla reqs + Orion/OSRS-style object/log IDs).
 * Object ID arrays include common variants - override in config if Orion differs (use 0xDEBUG).
 */
public enum TreeTier {
    NORMAL(1, "Normal", 1511, 1276, 1278, 1282, 1286),
    OAK(15, "Oaks", 1521, 10820, 1281),
    WILLOW(30, "Willows", 1519, 1308, 10819, 10829, 10833),
    MAPLE(45, "Maples", 1517, 1307, 10832),
    YEW(60, "Yews", 1515, 1309, 10822),
    MAGIC(75, "Magics", 1513, 47421, 1306, 10834);

    public final int requiredLevel;
    public final String label;
    public final int logItemId;
    public final int[] treeObjectIds;

    TreeTier(int requiredLevel, String label, int logItemId, int... treeObjectIds) {
        this.requiredLevel = requiredLevel;
        this.label = label;
        this.logItemId = logItemId;
        this.treeObjectIds = treeObjectIds;
    }

    public boolean matchesTree(int objectId) {
        for (int id : this.treeObjectIds) {
            if (id == objectId) {
                return true;
            }
        }
        return false;
    }
}
