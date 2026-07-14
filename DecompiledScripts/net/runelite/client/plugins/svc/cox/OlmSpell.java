/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.cox;

public enum OlmSpell {
    AUTOCAST("Autocast (weapon)"),
    FIRE_SURGE("Fire Surge"),
    FIRE_WAVE("Fire Wave"),
    FIRE_BLAST("Fire Blast"),
    FIRE_BOLT("Fire Bolt"),
    IBAN_BLAST("Iban Blast"),
    MAGIC_DART("Magic Dart"),
    SHADOW_BARRAGE("Shadow Barrage"),
    SHADOW_BURST("Shadow Burst"),
    ICE_BARRAGE("Ice Barrage"),
    ICE_BURST("Ice Burst"),
    BLOOD_BARRAGE("Blood Barrage"),
    BLOOD_BURST("Blood Burst"),
    SMOKE_BARRAGE("Smoke Barrage"),
    SMOKE_BURST("Smoke Burst");

    private final String label;

    private OlmSpell(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}

