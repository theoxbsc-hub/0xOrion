package net.runelite.client.plugins.svc.woodcutting;

public enum WoodcuttingMode {
    BANK("Bank"),
    DROP("Drop");

    private final String label;

    WoodcuttingMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
