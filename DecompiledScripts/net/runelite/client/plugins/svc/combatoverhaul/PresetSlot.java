/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.combatoverhaul;

public enum PresetSlot {
    PRESET_1(0),
    PRESET_2(1),
    PRESET_3(2),
    PRESET_4(3),
    PRESET_5(4),
    PRESET_6(5);

    public final int index;

    private PresetSlot(int n2) {
        this.index = n2;
    }

    public String toString() {
        return "Preset " + (this.index + 1);
    }
}

