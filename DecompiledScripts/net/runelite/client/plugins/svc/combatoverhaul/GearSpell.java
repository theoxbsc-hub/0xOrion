/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.combatoverhaul;

import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;

public enum GearSpell {
    NONE("None", null),
    WIND_STRIKE("Wind Strike", SpellBook.Standard.WIND_STRIKE),
    WATER_STRIKE("Water Strike", SpellBook.Standard.WATER_STRIKE),
    EARTH_STRIKE("Earth Strike", SpellBook.Standard.EARTH_STRIKE),
    FIRE_STRIKE("Fire Strike", SpellBook.Standard.FIRE_STRIKE),
    WIND_BOLT("Wind Bolt", SpellBook.Standard.WIND_BOLT),
    WATER_BOLT("Water Bolt", SpellBook.Standard.WATER_BOLT),
    EARTH_BOLT("Earth Bolt", SpellBook.Standard.EARTH_BOLT),
    FIRE_BOLT("Fire Bolt", SpellBook.Standard.FIRE_BOLT),
    WIND_BLAST("Wind Blast", SpellBook.Standard.WIND_BLAST),
    WATER_BLAST("Water Blast", SpellBook.Standard.WATER_BLAST),
    EARTH_BLAST("Earth Blast", SpellBook.Standard.EARTH_BLAST),
    FIRE_BLAST("Fire Blast", SpellBook.Standard.FIRE_BLAST),
    WIND_WAVE("Wind Wave", SpellBook.Standard.WIND_WAVE),
    WATER_WAVE("Water Wave", SpellBook.Standard.WATER_WAVE),
    EARTH_WAVE("Earth Wave", SpellBook.Standard.EARTH_WAVE),
    FIRE_WAVE("Fire Wave", SpellBook.Standard.FIRE_WAVE),
    WIND_SURGE("Wind Surge", SpellBook.Standard.WIND_SURGE),
    WATER_SURGE("Water Surge", SpellBook.Standard.WATER_SURGE),
    EARTH_SURGE("Earth Surge", SpellBook.Standard.EARTH_SURGE),
    FIRE_SURGE("Fire Surge", SpellBook.Standard.FIRE_SURGE),
    SARADOMIN_STRIKE("Saradomin Strike", SpellBook.Standard.SARADOMIN_STRIKE),
    CLAWS_OF_GUTHIX("Claws of Guthix", SpellBook.Standard.CLAWS_OF_GUTHIX),
    FLAMES_OF_ZAMORAK("Flames of Zamorak", SpellBook.Standard.FLAMES_OF_ZAMORAK),
    CRUMBLE_UNDEAD("Crumble Undead", SpellBook.Standard.CRUMBLE_UNDEAD),
    IBAN_BLAST("Iban Blast", SpellBook.Standard.IBAN_BLAST),
    MAGIC_DART("Magic Dart", SpellBook.Standard.MAGIC_DART),
    BIND("Bind", SpellBook.Standard.BIND),
    SNARE("Snare", SpellBook.Standard.SNARE),
    ENTANGLE("Entangle", SpellBook.Standard.ENTANGLE),
    SMOKE_RUSH("Smoke Rush", SpellBook.Ancient.SMOKE_RUSH),
    SHADOW_RUSH("Shadow Rush", SpellBook.Ancient.SHADOW_RUSH),
    BLOOD_RUSH("Blood Rush", SpellBook.Ancient.BLOOD_RUSH),
    ICE_RUSH("Ice Rush", SpellBook.Ancient.ICE_RUSH),
    SMOKE_BURST("Smoke Burst", SpellBook.Ancient.SMOKE_BURST),
    SHADOW_BURST("Shadow Burst", SpellBook.Ancient.SHADOW_BURST),
    BLOOD_BURST("Blood Burst", SpellBook.Ancient.BLOOD_BURST),
    ICE_BURST("Ice Burst", SpellBook.Ancient.ICE_BURST),
    SMOKE_BLITZ("Smoke Blitz", SpellBook.Ancient.SMOKE_BLITZ),
    SHADOW_BLITZ("Shadow Blitz", SpellBook.Ancient.SHADOW_BLITZ),
    BLOOD_BLITZ("Blood Blitz", SpellBook.Ancient.BLOOD_BLITZ),
    ICE_BLITZ("Ice Blitz", SpellBook.Ancient.ICE_BLITZ),
    SMOKE_BARRAGE("Smoke Barrage", SpellBook.Ancient.SMOKE_BARRAGE),
    SHADOW_BARRAGE("Shadow Barrage", SpellBook.Ancient.SHADOW_BARRAGE),
    BLOOD_BARRAGE("Blood Barrage", SpellBook.Ancient.BLOOD_BARRAGE),
    ICE_BARRAGE("Ice Barrage", SpellBook.Ancient.ICE_BARRAGE);

    private final String label;
    public final Spell spell;

    private GearSpell(String string2, Spell spell) {
        this.label = string2;
        this.spell = spell;
    }

    public String toString() {
        return this.label;
    }
}

