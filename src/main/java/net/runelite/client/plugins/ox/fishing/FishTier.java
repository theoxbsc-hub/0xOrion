package net.runelite.client.plugins.ox.fishing;

/**
 * Fishing progression ladder for Seabright Pier (spot NPC + action + fish item IDs).
 */
public enum FishTier {
    SHRIMP(1, "Shrimps", "Small Net", 1514, 317, 321),
    LOBSTER(40, "Lobsters", "Cage", 1510, 377),
    MONKFISH(62, "Monkfish", "Net", 4316, 7944, 371, 359),
    SHARK(75, "Sharks", "Harpoon", 1511, 383, 371, 359);

    public final int requiredLevel;
    public final String label;
    public final String fishingAction;
    public final int spotNpcId;
    public final int[] fishItemIds;

    FishTier(int requiredLevel, String label, String fishingAction, int spotNpcId, int... fishItemIds) {
        this.requiredLevel = requiredLevel;
        this.label = label;
        this.fishingAction = fishingAction;
        this.spotNpcId = spotNpcId;
        this.fishItemIds = fishItemIds;
    }

    public boolean matchesSpot(int npcId) {
        return npcId == this.spotNpcId;
    }

    public boolean matchesFish(int itemId) {
        for (int id : this.fishItemIds) {
            if (id == itemId) {
                return true;
            }
        }
        return false;
    }
}
