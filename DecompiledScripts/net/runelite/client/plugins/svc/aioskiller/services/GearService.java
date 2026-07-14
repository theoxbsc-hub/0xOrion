/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.Player
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

public final class GearService {
    private static final int COMBAT_OPTIONS_GROUP = 593;
    private final Client client;
    private final HumanInput input;
    private String status = "";

    public GearService(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public boolean isEquipped(int n) {
        String string = AioSkillerConstants.knownName(n);
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            try {
                Item item = Equipment.fromSlot(equipmentInventorySlot);
                if (item == null) continue;
                if (item.getId() == n) {
                    return true;
                }
                if (string == null || !string.equalsIgnoreCase(item.getName())) continue;
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    public boolean allEquipped(int[] nArray) {
        for (int n : nArray) {
            if (this.isEquipped(n)) continue;
            return false;
        }
        return true;
    }

    public boolean hasWeaponEquipped() {
        try {
            return Equipment.fromSlot(EquipmentInventorySlot.WEAPON) != null;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public boolean isEquipmentEmpty() {
        try {
            return Equipment.getAll().isEmpty();
        }
        catch (Throwable throwable) {
            return true;
        }
    }

    public void equip(Item item) {
        String string;
        block3: {
            string = "Wield";
            try {
                ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
                if (itemComposition == null || itemComposition.getInventoryActions() == null) break block3;
                for (String string2 : itemComposition.getInventoryActions()) {
                    String string3;
                    if (string2 == null || !(string3 = string2.toLowerCase()).equals("wield") && !string3.equals("wear") && !string3.equals("equip")) continue;
                    string = string2;
                    break;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.input.itemInteractVia(item, string);
    }

    public int equipSet(int[] nArray, Player player) {
        if (this.allEquipped(nArray)) {
            this.status = "Gear equipped";
            return Integer.MIN_VALUE;
        }
        for (int n : nArray) {
            String string;
            if (this.isEquipped(n)) continue;
            Item item = Inventory.getFirst(n);
            if (item == null && (string = AioSkillerConstants.knownName(n)) != null) {
                item = Inventory.getFirst(string);
            }
            if (item == null) continue;
            this.status = "Equipping " + item.getName();
            this.equip(item);
            return 600;
        }
        this.status = "Gear not in inventory";
        return 800;
    }

    public void bankItem(int n) {
        try {
            Bank.deposit(n, 1000);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void setStyle(int n) {
        int n2 = (Math.max(0, Math.min(3, n)) + 1) * 4;
        int n3 = 0x2510000 | n2;
        this.input.widgetResumePause(n3, -1);
        this.status = "Set attack style " + n;
    }

    public String status() {
        return this.status;
    }
}

