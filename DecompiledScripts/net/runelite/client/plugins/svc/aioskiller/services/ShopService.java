/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.services.BankService;
import net.runelite.client.plugins.svc.aioskiller.services.Navigator;
import net.runelite.client.plugins.svc.aioskiller.services.Walk;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;

public final class ShopService {
    private final Client client;
    private final BankService bank;
    private final Navigator nav;
    private final HumanInput input;
    private String status = "";
    private Acq acq = Acq.DONE;
    private int acqItem;
    private int acqQty;
    private int acqStartOwned;
    private int acqAttempts;
    private Vial vial = Vial.DONE;

    public ShopService(Client client, BankService bankService, Navigator navigator, HumanInput humanInput) {
        this.client = client;
        this.bank = bankService;
        this.nav = navigator;
        this.input = humanInput;
    }

    public String status() {
        return this.status;
    }

    public boolean isOpen() {
        try {
            Widget widget = this.client.getWidget(300, 0);
            if (widget != null && !widget.isHidden()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            return Shop.isOpen();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public void beginAcquire(int n, int n2, boolean bl) {
        this.acqItem = n;
        this.acqQty = n2;
        this.acqStartOwned = this.ownedCount(n);
        this.acqAttempts = 0;
        this.acq = bl ? Acq.GP_OPEN : Acq.WALK_SHOP;
    }

    public int stepAcquire(Player player) {
        switch (this.acq.ordinal()) {
            case 0: {
                int n = this.bank.openStep(player);
                if (n == Integer.MIN_VALUE) {
                    this.acq = Acq.GP_WITHDRAW;
                    return 300;
                }
                return n;
            }
            case 1: {
                this.status = "Withdrawing GP";
                this.bank.withdrawAll(995);
                this.acq = Acq.GP_CLOSE;
                return 800;
            }
            case 2: {
                this.bank.close();
                this.acq = Acq.WALK_SHOP;
                return 600;
            }
            case 3: {
                WorldPoint worldPoint = player.getWorldLocation();
                if (Walk.arrived(worldPoint, AioSkillerConstants.SHOP_TILE, 3)) {
                    this.acq = Acq.OPEN_SHOP;
                    return 400;
                }
                if (worldPoint == null || worldPoint.distanceTo(AioSkillerConstants.SHOP_TILE) > 10 && !this.nav.atHome(worldPoint)) {
                    int n = this.nav.ensureHome(player);
                    this.status = "Shop: teleporting home \u2014 " + this.nav.status();
                    if (n != Integer.MIN_VALUE) {
                        return n;
                    }
                    worldPoint = player.getWorldLocation();
                }
                this.status = "Walking to shop";
                this.input.walkToward(worldPoint, AioSkillerConstants.SHOP_TILE);
                return 1500;
            }
            case 4: {
                if (this.isOpen()) {
                    this.acq = Acq.BUYING;
                    this.acqAttempts = 0;
                    return 500;
                }
                NPC nPC = this.findKeeper(player);
                if (nPC != null) {
                    this.status = "Open-shop";
                    this.input.npcActionVia(nPC, "Open-shop");
                    return 2000;
                }
                this.status = "Shopkeeper missing";
                return 1000;
            }
            case 5: {
                int n = this.ownedCount(this.acqItem) - this.acqStartOwned;
                if (n >= this.acqQty || this.ownedCount(this.acqItem) >= this.acqQty) {
                    this.acq = Acq.CLOSE_SHOP;
                    return 400;
                }
                if (this.acqAttempts++ > 16) {
                    this.status = "Buy gave up";
                    this.acq = Acq.CLOSE_SHOP;
                    return 400;
                }
                this.status = "Buying item " + this.acqItem;
                this.buyOneStep(this.acqItem, this.acqQty - n);
                return 900 + this.acqAttempts * 60;
            }
            case 6: {
                this.closeShop();
                this.acq = Acq.DONE;
                return 800;
            }
        }
        return Integer.MIN_VALUE;
    }

    public void beginVialPacks() {
        this.vial = Vial.ACQUIRE;
        this.beginAcquire(11879, 3, true);
    }

    public int stepVialPacks(Player player) {
        switch (this.vial.ordinal()) {
            case 0: {
                int n = this.stepAcquire(player);
                if (n == Integer.MIN_VALUE) {
                    this.vial = Vial.HOME;
                    return 400;
                }
                return n;
            }
            case 1: {
                int n = this.nav.ensureHome(player);
                if (n == Integer.MIN_VALUE) {
                    this.vial = Vial.BANK_OPEN;
                    return 400;
                }
                return n;
            }
            case 2: {
                int n = this.bank.openStep(player);
                if (n == Integer.MIN_VALUE) {
                    this.vial = Vial.OPEN_ONE;
                    return 400;
                }
                return n;
            }
            case 3: {
                Item item = Inventory.getFirst(11879);
                if (item != null) {
                    this.status = "Opening vial pack";
                    this.input.interactItemFirst(item);
                    this.vial = Vial.DEPOSIT;
                    return 1200;
                }
                this.vial = Vial.DONE;
                return 400;
            }
            case 4: {
                this.status = "Banking vials";
                this.bank.deposit(227, 1000);
                this.vial = Vial.OPEN_ONE;
                return 600;
            }
        }
        this.bank.close();
        return Integer.MIN_VALUE;
    }

    private NPC findKeeper(Player player) {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 2813);
        if (nPC2 != null) {
            return nPC2;
        }
        return NPCs.getNearest(nPC -> nPC != null && nPC.getWorldLocation().distanceTo(AioSkillerConstants.SHOP_TILE) <= 6);
    }

    private void buyOneStep(int n, int n2) {
        Runnable runnable = n2 >= 50 ? () -> Shop.buyFifty(n) : (n2 >= 10 ? () -> Shop.buyTen(n) : (n2 >= 5 ? () -> Shop.buyFive(n) : () -> Shop.buyOne(n)));
        this.input.primedUiOp(300, 16, () -> {
            try {
                runnable.run();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    private void closeShop() {
        this.input.clickWidgetOrFixed(300, 1, 482, 37);
    }

    private int ownedCount(int n) {
        int n2 = 0;
        for (Item item : Inventory.getAll(n)) {
            if (item == null) continue;
            n2 += Math.max(1, item.getQuantity());
        }
        return n2;
    }

    private static enum Acq {
        GP_OPEN,
        GP_WITHDRAW,
        GP_CLOSE,
        WALK_SHOP,
        OPEN_SHOP,
        BUYING,
        CLOSE_SHOP,
        DONE;

    }

    private static enum Vial {
        ACQUIRE,
        HOME,
        BANK_OPEN,
        OPEN_ONE,
        DEPOSIT,
        DONE;

    }
}

