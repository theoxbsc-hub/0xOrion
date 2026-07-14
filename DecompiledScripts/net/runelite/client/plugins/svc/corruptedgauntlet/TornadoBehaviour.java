/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.corruptedgauntlet;

public enum TornadoBehaviour {
    ATTACK_AND_EVADE("Attack + Evade (max DPS, high APM)"),
    EVADE_ONLY("Evade Only (no attacking, low APM)"),
    MIX("Mix (use Evade-Only Chance %)");

    private final String label;

    private TornadoBehaviour(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

