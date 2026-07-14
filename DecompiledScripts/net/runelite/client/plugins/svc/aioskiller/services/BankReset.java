/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Player
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Player;
import net.runelite.client.plugins.svc.aioskiller.services.BankService;
import net.runelite.client.plugins.svc.aioskiller.services.Navigator;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

public final class BankReset {
    private final Navigator nav;
    private final BankService bank;
    private S s = S.DONE;
    private int attempts;
    private boolean stripEquipment;
    private String status = "";

    public BankReset(Navigator navigator, BankService bankService) {
        this.nav = navigator;
        this.bank = bankService;
    }

    public void begin() {
        this.s = S.HOME;
        this.attempts = 0;
        this.stripEquipment = false;
        this.status = "Bank reset: starting";
    }

    public void beginStrip() {
        this.s = S.HOME;
        this.attempts = 0;
        this.stripEquipment = true;
        this.status = "Stowing gear: starting";
    }

    public String status() {
        return this.status;
    }

    public int step(Player player) {
        switch (this.s.ordinal()) {
            case 0: {
                int n = this.nav.ensureHome(player);
                this.status = "Bank reset: " + this.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.OPEN;
                    return 300;
                }
                return n;
            }
            case 1: {
                if (this.isInventoryEmpty() && !this.needStrip()) {
                    this.s = S.DONE;
                    return 200;
                }
                int n = this.bank.openStep(player);
                this.status = (this.stripEquipment ? "Stowing gear: " : "Bank reset: ") + this.bank.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.DEPOSIT;
                    return 300;
                }
                return n;
            }
            case 2: {
                this.status = this.stripEquipment ? "Stowing gear: depositing inventory + equipment" : "Bank reset: depositing inventory";
                this.bank.depositInventory();
                if (this.stripEquipment) {
                    this.bank.depositEquipment();
                }
                this.s = S.VERIFY;
                return 700;
            }
            case 3: {
                if (this.isInventoryEmpty() && !this.needStrip()) {
                    this.s = S.CLOSE;
                    return 200;
                }
                if (++this.attempts > 4) {
                    this.s = S.CLOSE;
                    return 200;
                }
                this.s = S.DEPOSIT;
                return 300;
            }
            case 4: {
                this.bank.close();
                this.s = S.DONE;
                return 400;
            }
        }
        return Integer.MIN_VALUE;
    }

    private boolean isInventoryEmpty() {
        try {
            return Inventory.isEmpty();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private boolean isEquipmentEmpty() {
        try {
            return Equipment.getAll().isEmpty();
        }
        catch (Throwable throwable) {
            return true;
        }
    }

    private boolean needStrip() {
        return this.stripEquipment && !this.isEquipmentEmpty();
    }

    private static enum S {
        HOME,
        OPEN,
        DEPOSIT,
        VERIFY,
        CLOSE,
        DONE;

    }
}

