package net.runelite.client.plugins.ox.fishing;

public enum FishingMode {
    BANK("Bank"),
    DROP("Drop");

    private final String label;

    FishingMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
