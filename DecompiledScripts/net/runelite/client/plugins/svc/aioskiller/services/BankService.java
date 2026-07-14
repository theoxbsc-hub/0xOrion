/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.Player
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.services.Walk;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.core.SafeBank;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;

public final class BankService {
    private final Client client;
    private final HumanInput input;
    private String status = "";

    public BankService(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public boolean isOpen() {
        try {
            return Bank.isOpen();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public TileObject findBooth() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 10083);
        return tileObject2 != null ? tileObject2 : TileObjects.getNearest("Bank booth");
    }

    private static boolean looksLikeCoinPouch(Item item) {
        if (item == null || item.getName() == null) {
            return false;
        }
        return item.getName().toLowerCase().contains("coin pouch");
    }

    public boolean hasCoinPouches() {
        for (int n : AioSkillerConstants.COIN_POUCH_IDS) {
            try {
                if (Inventory.getFirst(n) == null) continue;
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        try {
            Object object = Inventory.getAll().iterator();
            while (object.hasNext()) {
                Item item = (Item)object.next();
                if (!BankService.looksLikeCoinPouch(item)) continue;
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public boolean openNextCoinPouch() {
        for (int n : AioSkillerConstants.COIN_POUCH_IDS) {
            Item item;
            try {
                item = Inventory.getFirst(n);
            }
            catch (Throwable throwable) {
                item = null;
            }
            if (item == null) continue;
            this.input.itemInteractVia(item, "Open-all");
            return true;
        }
        try {
            Object object = Inventory.getAll().iterator();
            while (object.hasNext()) {
                Item item = (Item)object.next();
                if (!BankService.looksLikeCoinPouch(item)) continue;
                this.input.itemInteractVia(item, "Open-all");
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public int openStep(Player player) {
        if (this.isOpen()) {
            this.status = "Bank open";
            return Integer.MIN_VALUE;
        }
        if (this.openNextCoinPouch()) {
            this.status = "Emptying coin pouches (can't bank)";
            return 600;
        }
        WorldPoint worldPoint = player == null ? null : player.getWorldLocation();
        TileObject tileObject = this.findBooth();
        if (tileObject == null) {
            if (worldPoint != null && !Walk.arrived(worldPoint, AioSkillerConstants.BANK_TILE, 4)) {
                this.status = "Walking to bank (booth not in range)";
                this.input.walkToward(worldPoint, AioSkillerConstants.BANK_TILE);
                return 1200;
            }
            this.status = "Bank booth not found";
            return 1000;
        }
        if (worldPoint != null && !Walk.arrived(worldPoint, tileObject.getWorldLocation(), 4)) {
            this.status = "Walking to bank";
            this.input.walkToward(worldPoint, tileObject.getWorldLocation());
            return 1200;
        }
        this.status = "Opening bank";
        this.input.interactObjectVia(tileObject, "Use");
        return 1500;
    }

    public void close() {
        this.input.primedDriftOp(() -> {
            try {
                Bank.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    public int count(int n) {
        try {
            int n2 = 0;
            for (Item item : Bank.getAll(n)) {
                if (item == null) continue;
                n2 += Math.max(1, item.getQuantity());
            }
            return n2;
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    public int countName(String string) {
        try {
            String string2 = string.toLowerCase();
            int n = 0;
            for (Item item : Bank.getAll()) {
                if (item == null || item.getName() == null || !item.getName().toLowerCase().contains(string2)) continue;
                n += Math.max(1, item.getQuantity());
            }
            return n;
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    public boolean has(int n) {
        try {
            return Bank.contains(n);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public boolean hasName(String string) {
        return this.countName(string) > 0;
    }

    public boolean hasItemNamed(String string) {
        if (string == null) {
            return false;
        }
        try {
            for (Item item : Bank.getAll()) {
                if (item == null || !string.equalsIgnoreCase(item.getName())) continue;
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public void withdrawAll(int n) {
        this.input.primedItemOp(this.bankSlot(n), () -> {
            try {
                SafeBank.withdrawAll(n);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    public void withdrawAllByName(String string) {
        if (string == null) {
            return;
        }
        this.input.primedItemOp(this.bankSlotByName(string), () -> {
            try {
                Bank.withdrawAll(string, Bank.WithdrawMode.ITEM);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    public void withdrawExact(int n, int n2) {
        this.input.primedItemOp(this.bankSlot(n), () -> {
            try {
                SafeBank.withdrawExact(n, n2);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    public void depositInventory() {
        this.input.primedInventoryOp(() -> {
            try {
                Bank.depositInventory();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    public void depositEquipment() {
        this.input.primedDriftOp(() -> {
            try {
                Bank.depositEquipment();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    public void deposit(int n, int n2) {
        Item item = null;
        try {
            item = Inventory.getFirst(n);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.input.primedItemOp(item, () -> {
            try {
                Bank.deposit(n, n2);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    private Item bankSlot(int n) {
        try {
            return Bank.getFirst(n);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private Item bankSlotByName(String string) {
        if (string == null) {
            return null;
        }
        try {
            for (Item item : Bank.getAll()) {
                if (item == null || !string.equalsIgnoreCase(item.getName())) continue;
                return item;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    public String status() {
        return this.status;
    }
}

