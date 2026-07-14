/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.combat;

public enum CombatPotion {
    NONE("None"),
    ATTACK("Attack potion"),
    STRENGTH("Strength potion"),
    DEFENCE("Defence potion"),
    COMBAT("Combat potion"),
    SUPER_ATTACK("Super attack"),
    SUPER_STRENGTH("Super strength"),
    SUPER_DEFENCE("Super defence"),
    SUPER_COMBAT("Super combat potion"),
    RANGING("Ranging potion"),
    SUPER_RANGING("Super ranging potion"),
    MAGIC("Magic potion"),
    BASTION("Bastion potion"),
    BATTLEMAGE("Battlemage potion");

    private final String name;

    private CombatPotion(String string2) {
        this.name = string2;
    }

    public String toString() {
        return this.name;
    }
}

