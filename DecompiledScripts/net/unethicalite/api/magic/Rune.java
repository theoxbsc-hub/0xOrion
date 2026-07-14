/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 */
package net.unethicalite.api.magic;

import java.util.Arrays;
import net.runelite.api.Item;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.RunePouch;

public enum Rune {
    AIR(556, "Air", "Smoke", "Mist", "Dust"),
    EARTH(557, "Earth", "Lava", "Mud", "Dust"),
    FIRE(554, "Fire", "Lava", "Smoke", "Steam"),
    WATER(555, "Water", "Mud", "Steam", "Mist"),
    MIND(558, "Mind"),
    BODY(559, "Body"),
    COSMIC(564, "Cosmic"),
    CHAOS(562, "Chaos"),
    NATURE(561, "Nature"),
    LAW(563, "Law"),
    DEATH(560, "Death"),
    ASTRAL(9075, "Astral"),
    BLOOD(565, "Blood"),
    SOUL(566, "Soul"),
    WRATH(21880, "Wrath");

    private final int runeId;
    private final String[] runeNames;

    private Rune(int runeId, String ... runeNames) {
        this.runeId = runeId;
        this.runeNames = runeNames;
    }

    public String[] getRuneNames() {
        return this.runeNames;
    }

    public int getRuneId() {
        return this.runeId;
    }

    public int getQuantity() {
        if (this.isStaffEquipped() || this.isTomeEquipped()) {
            return Integer.MAX_VALUE;
        }
        Item rune = Inventory.getFirst(x -> x.getName() != null && x.getName().contains("rune") && Arrays.stream(this.runeNames).anyMatch(name -> x.getId() == this.runeId || x.getName().contains((CharSequence)name)));
        if (rune == null) {
            return RunePouch.getQuantity(this);
        }
        return rune.getQuantity() + RunePouch.getQuantity(this);
    }

    private boolean isStaffEquipped() {
        return Equipment.contains(x -> x.getName() != null && x.getName().toLowerCase().contains("staff") && Arrays.stream(this.runeNames).anyMatch(n -> x.getName().toLowerCase().contains(n.toLowerCase())));
    }

    private boolean isTomeEquipped() {
        return Equipment.contains(x -> x.getName() != null && x.getName().startsWith("Tome of") && !x.getName().endsWith("(empty") && Arrays.stream(this.runeNames).anyMatch(n -> x.getName().toLowerCase().contains(n.toLowerCase())));
    }
}

