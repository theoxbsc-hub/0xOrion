/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.Item
 *  net.runelite.api.Player
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.client.plugins.svc.aioskiller.services.BankService;
import net.runelite.client.plugins.svc.aioskiller.services.Navigator;
import net.runelite.client.plugins.svc.aioskiller.services.ShopService;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

public final class Acquirer {
    private final Navigator nav;
    private final BankService bank;
    private final ShopService shop;
    private S s = S.DONE;
    private int itemId;
    private int qty;
    private int wdAttempts;
    private boolean needGp;
    private boolean requireInInv;
    private String status = "";

    public Acquirer(Navigator navigator, BankService bankService, ShopService shopService) {
        this.nav = navigator;
        this.bank = bankService;
        this.shop = shopService;
    }

    public void begin(int n, int n2, boolean bl, boolean bl2) {
        this.itemId = n;
        this.qty = Math.max(1, n2);
        this.needGp = bl;
        this.requireInInv = bl2;
        this.wdAttempts = 0;
        this.s = S.HOME;
    }

    public boolean isDone() {
        return this.s == S.DONE;
    }

    public String status() {
        return this.status;
    }

    public int step(Player player) {
        switch (this.s.ordinal()) {
            case 0: {
                int n = this.nav.ensureHome(player);
                if (n == Integer.MIN_VALUE) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                this.status = "Going home";
                return n;
            }
            case 1: {
                int n = this.bank.openStep(player);
                if (n == Integer.MIN_VALUE) {
                    this.s = S.CHECK;
                    return 300;
                }
                return n;
            }
            case 2: {
                boolean bl;
                int n = this.owned(this.itemId);
                int n2 = this.bank.count(this.itemId);
                boolean bl2 = this.requireInInv ? n >= this.qty : (bl = n + n2 >= this.qty);
                if (bl) {
                    this.bank.close();
                    this.s = S.DONE;
                    this.status = "Have item " + this.itemId;
                    return 300;
                }
                if (!this.requireInInv && n2 > 0) {
                    this.bank.close();
                    this.s = S.DONE;
                    return 300;
                }
                if (n2 > 0) {
                    this.s = S.WITHDRAW;
                    return 300;
                }
                this.bank.close();
                this.shop.beginAcquire(this.itemId, this.qty, this.needGp);
                this.s = S.SHOP;
                return 400;
            }
            case 3: {
                int n = this.qty - this.owned(this.itemId);
                if (n <= 0) {
                    this.bank.close();
                    this.s = S.DONE;
                    return 300;
                }
                if (Inventory.getFreeSlots() <= 0) {
                    this.status = "Inventory full \u2014 depositing to make room";
                    this.bank.depositInventory();
                    return 500;
                }
                this.status = "Withdrawing item " + this.itemId;
                this.bank.withdrawExact(this.itemId, n);
                this.s = S.WAIT_WD;
                return 700;
            }
            case 4: {
                if (this.owned(this.itemId) >= this.qty) {
                    this.bank.close();
                    this.s = S.DONE;
                    return 300;
                }
                if (++this.wdAttempts > 5) {
                    this.bank.close();
                    this.shop.beginAcquire(this.itemId, this.qty, this.needGp);
                    this.s = S.SHOP;
                    return 400;
                }
                this.s = S.WITHDRAW;
                return 300;
            }
            case 5: {
                int n = this.shop.stepAcquire(player);
                this.status = this.shop.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.DONE;
                    return 300;
                }
                return n;
            }
        }
        return Integer.MIN_VALUE;
    }

    private int owned(int n) {
        int n2 = 0;
        for (Item item : Inventory.getAll(n)) {
            if (item == null) continue;
            n2 += Math.max(1, item.getQuantity());
        }
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            Item item = Equipment.fromSlot(equipmentInventorySlot);
            if (item == null || item.getId() != n) continue;
            ++n2;
        }
        return n2;
    }

    private static enum S {
        HOME,
        BANK_OPEN,
        CHECK,
        WITHDRAW,
        WAIT_WD,
        SHOP,
        DONE;

    }
}

