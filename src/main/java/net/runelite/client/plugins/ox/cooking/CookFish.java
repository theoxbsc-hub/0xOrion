package net.runelite.client.plugins.ox.cooking;

/**
 * Raw fish cookables at Seabright Pier — IDs match {@code ox.fishing.FishTier} catch IDs.
 */
public enum CookFish {
    SHRIMP("Shrimps", 317, 321),
    LOBSTER("Lobsters", 377),
    MONKFISH("Monkfish", 7944),
    SHARK("Sharks", 383),
    ANGLER("Anglers", 13439);

    public final String label;
    /** Primary + optional bycatch raw IDs (withdraw prefers first ID present in the bank). */
    public final int[] rawIds;

    CookFish(String label, int... rawIds) {
        this.label = label;
        this.rawIds = rawIds;
    }

    public boolean matchesRaw(int itemId) {
        for (int id : this.rawIds) {
            if (id == itemId) {
                return true;
            }
        }
        return false;
    }
}
