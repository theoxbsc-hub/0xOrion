/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.runelite.client.plugins.svc.aioskiller.services.Walk;
import net.runelite.client.plugins.svc.thieving.StallThiefEngine;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;
import net.unethicalite.api.widgets.Dialog;

public final class ScimAcquirer {
    private static final int MAX_SWITCH_SELL_SESSIONS = 6;
    private static final int STALL_ARRIVE_DIST = 5;
    private final MasterContext ctx;
    private final StallThiefEngine engine;
    private S s = S.DONE;
    private String status = "";
    private int wdAttempts;
    private int findAttempts;
    private int dialogWaits;
    private int buyAttempts;
    private boolean selling;
    private int startBankCoins;
    private int grindNeed;
    private StallThiefEngine.StallTarget currentStall;
    private boolean switchingStalls;
    private boolean switchRelocated;
    private int switchSellSessions;

    public ScimAcquirer(MasterContext masterContext) {
        this.ctx = masterContext;
        this.engine = new StallThiefEngine(masterContext.client, masterContext.input);
        this.engine.setNeverSellIds(AioSkillerConstants.COIN_POUCH_IDS);
    }

    public void begin() {
        this.s = S.HOME;
        this.status = "Acquiring dragon scimitar";
        this.buyAttempts = 0;
        this.dialogWaits = 0;
        this.findAttempts = 0;
        this.wdAttempts = 0;
        this.grindNeed = 0;
        this.startBankCoins = 0;
        this.selling = false;
        this.currentStall = null;
        this.switchingStalls = false;
        this.switchRelocated = false;
        this.switchSellSessions = 0;
        this.engine.reset();
    }

    public boolean isDone() {
        return this.s == S.DONE;
    }

    public boolean isFailed() {
        return this.s == S.FAILED;
    }

    public String status() {
        return this.status;
    }

    public void onChat(String string) {
        this.engine.onChatMessage(string);
    }

    public int step(Player player) {
        switch (this.s.ordinal()) {
            case 0: {
                int n = this.ctx.nav.ensureHome(player);
                this.status = "Scim: home \u2014 " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                return n;
            }
            case 1: {
                int n = this.ctx.bank.openStep(player);
                this.status = "Scim: bank \u2014 " + this.ctx.bank.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.CHECK;
                    return 300;
                }
                return n;
            }
            case 2: {
                if (!this.ctx.bank.isOpen()) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                if (this.owns(4587)) {
                    this.ctx.bank.close();
                    this.status = "Scim: already owned";
                    this.s = S.DONE;
                    return 300;
                }
                if (this.ctx.bank.has(4587) || this.ctx.bank.hasItemNamed("Dragon scimitar")) {
                    this.wdAttempts = 0;
                    this.s = S.WITHDRAW_SCIM;
                    return 300;
                }
                int n = this.ctx.bank.count(995) + this.invCoins();
                if (n >= 115000) {
                    if (this.invCoins() >= 115000) {
                        this.ctx.bank.close();
                        this.findAttempts = 0;
                        this.s = S.SHOP_FIND;
                        return 400;
                    }
                    this.wdAttempts = 0;
                    this.s = S.WITHDRAW_GP;
                    return 300;
                }
                this.startBankCoins = this.ctx.bank.count(995);
                this.grindNeed = Math.max(1, 115000 - this.startBankCoins);
                this.ctx.bank.close();
                this.engine.reset();
                this.selling = false;
                this.currentStall = null;
                this.switchingStalls = false;
                this.switchRelocated = false;
                this.switchSellSessions = 0;
                this.s = S.GRIND_HOME;
                return 400;
            }
            case 3: {
                if (!this.ctx.bank.isOpen()) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                if (this.owns(4587)) {
                    this.ctx.bank.close();
                    this.s = S.DONE;
                    return 300;
                }
                if (++this.wdAttempts > 6) {
                    return this.fail("Could not withdraw banked dragon scimitar");
                }
                this.status = "Scim: withdrawing scimitar";
                this.ctx.bank.withdrawAllByName("Dragon scimitar");
                return 700;
            }
            case 4: {
                if (!this.ctx.bank.isOpen()) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                if (this.invCoins() >= 115000) {
                    this.ctx.bank.close();
                    this.findAttempts = 0;
                    this.s = S.SHOP_FIND;
                    return 400;
                }
                if (Inventory.getFreeSlots() <= 0) {
                    this.status = "Scim: inv full \u2014 depositing";
                    this.ctx.bank.depositInventory();
                    return 500;
                }
                if (++this.wdAttempts > 6) {
                    return this.fail("Could not withdraw gp for scimitar");
                }
                this.status = "Scim: withdrawing gp";
                this.ctx.bank.withdrawAll(995);
                return 700;
            }
            case 5: {
                int n = this.ctx.nav.ensureHome(player);
                this.status = "Scim gp grind: home \u2014 " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.GRIND;
                    return 400;
                }
                return n;
            }
            case 6: {
                WorldPoint worldPoint;
                if (this.ctx.bank.openNextCoinPouch()) {
                    this.status = "Scim gp grind: opening coin pouches";
                    return 600;
                }
                if (this.invCoins() >= this.grindNeed) {
                    this.s = S.GRIND_BANK;
                    return 400;
                }
                StallThiefEngine.StallTarget stallTarget = StallThiefEngine.bestStallForLevel(LevelUtil.level(Skill.THIEVING));
                if (this.currentStall == null) {
                    this.currentStall = stallTarget;
                }
                if (this.switchingStalls || stallTarget.objectId != this.currentStall.objectId) {
                    if (!this.switchingStalls) {
                        this.switchingStalls = true;
                        this.switchRelocated = false;
                    }
                    if (!this.switchRelocated && this.switchSellSessions == 0 && !this.selling) {
                        WorldPoint worldPoint2;
                        WorldPoint worldPoint3 = worldPoint2 = player == null ? null : player.getWorldLocation();
                        if (worldPoint2 != null && !Walk.arrived(worldPoint2, this.currentStall.coords(), 5)) {
                            this.status = "Scim gp grind: returning to " + this.currentStall.label + " stall to sell";
                            this.ctx.input.walkToward(worldPoint2, this.currentStall.coords());
                            return 1000;
                        }
                        this.switchRelocated = true;
                    }
                    this.selling = true;
                    int n = this.engine.sellAllStep(player, this.currentStall.traderNpcId);
                    this.status = "Scim gp grind \u2014 switching, selling at " + this.currentStall.label + " (" + this.switchSellSessions + "/6): " + this.engine.getStatus();
                    if (this.engine.isSellingDone()) {
                        if (this.engine.hasSellableItems() && this.switchSellSessions < 6) {
                            ++this.switchSellSessions;
                            return 300;
                        }
                        this.engine.clearSellBlacklist();
                        this.switchingStalls = false;
                        this.switchRelocated = false;
                        this.selling = false;
                        this.switchSellSessions = 0;
                        this.currentStall = stallTarget;
                        return 400;
                    }
                    return n;
                }
                if (this.engine.inventoryFull() || this.selling) {
                    this.selling = true;
                    int n = this.engine.sellAllStep(player, this.currentStall.traderNpcId);
                    this.status = "Scim gp grind \u2014 selling: " + this.engine.getStatus();
                    if (this.engine.isSellingDone()) {
                        this.selling = false;
                    }
                    return n;
                }
                WorldPoint worldPoint4 = worldPoint = player == null ? null : player.getWorldLocation();
                if (worldPoint != null && !Walk.arrived(worldPoint, this.currentStall.coords(), 5)) {
                    this.status = "Scim gp grind: walking to " + this.currentStall.label + " stall";
                    this.ctx.input.walkToward(worldPoint, this.currentStall.coords());
                    return 1000;
                }
                int n = this.engine.stealStep(player, this.currentStall.objectId);
                this.status = "Scim gp grind (" + this.currentStall.label + "): " + this.invCoins() + "/" + this.grindNeed + " gp";
                return n;
            }
            case 7: {
                int n = this.ctx.bank.openStep(player);
                this.status = "Scim gp grind: banking \u2014 " + this.ctx.bank.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.GRIND_DEPOSIT;
                    return 300;
                }
                return n;
            }
            case 8: {
                if (!this.ctx.bank.isOpen()) {
                    this.s = S.GRIND_BANK;
                    return 300;
                }
                this.status = "Scim gp grind: depositing";
                this.ctx.bank.depositInventory();
                this.wdAttempts = 0;
                this.s = S.CHECK;
                return 600;
            }
            case 9: {
                WorldPoint worldPoint = player == null ? null : player.getWorldLocation();
                NPC nPC = this.findKeeper(player);
                if (nPC != null && worldPoint != null) {
                    WorldPoint worldPoint5 = nPC.getWorldLocation();
                    if (Walk.arrived(worldPoint, worldPoint5, 3)) {
                        this.dialogWaits = 0;
                        this.s = S.SHOP_OPEN;
                        return 400;
                    }
                    this.status = "Scim: walking to warrior";
                    this.ctx.input.walkToward(worldPoint, worldPoint5);
                    return 1200;
                }
                int n = this.ctx.nav.ensureHome(player);
                if (n != Integer.MIN_VALUE) {
                    this.status = "Scim: teleporting home \u2014 " + this.ctx.nav.status();
                    return n;
                }
                WorldPoint worldPoint6 = worldPoint = player == null ? null : player.getWorldLocation();
                if (worldPoint != null && !Walk.arrived(worldPoint, AioSkillerConstants.SCIM_SHOP_TILE, 3)) {
                    this.status = "Scim: walking to warrior tile";
                    this.ctx.input.walkToward(worldPoint, AioSkillerConstants.SCIM_SHOP_TILE);
                    return 1200;
                }
                this.status = "Scim: locating warrior (" + this.findAttempts + ")";
                if (++this.findAttempts > 20) {
                    return this.fail("Scimitar shopkeeper 1158 not found");
                }
                return 1000;
            }
            case 10: {
                if (this.ctx.shop.isOpen()) {
                    this.buyAttempts = 0;
                    this.s = S.SHOP_BUY;
                    return 500;
                }
                if (this.dialogOptionsOpen()) {
                    this.s = S.SHOP_DIALOG;
                    return 200;
                }
                NPC nPC = this.findKeeper(player);
                if (nPC != null) {
                    this.status = "Scim: Open-shop";
                    this.ctx.input.npcActionVia(nPC, "Open-shop");
                    this.dialogWaits = 0;
                    this.s = S.SHOP_DIALOG;
                    return 2500;
                }
                this.s = S.SHOP_FIND;
                return 600;
            }
            case 11: {
                if (this.ctx.shop.isOpen()) {
                    this.buyAttempts = 0;
                    this.s = S.SHOP_BUY;
                    return 500;
                }
                if (this.dialogOptionsOpen()) {
                    this.status = "Scim: choosing Weaponry (opt 2)";
                    boolean bl = false;
                    try {
                        bl = Dialog.chooseOption("Weaponry");
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    if (!bl) {
                        try {
                            bl = Dialog.chooseOption(2);
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    if (!bl) {
                        this.ctx.input.key('2');
                    }
                    return 1500;
                }
                if (++this.dialogWaits > 8) {
                    this.s = S.SHOP_OPEN;
                    return 600;
                }
                this.status = "Scim: waiting for shop dialogue";
                return 500;
            }
            case 12: {
                if (this.owns(4587)) {
                    this.s = S.SHOP_CLOSE;
                    return 400;
                }
                if (++this.buyAttempts > 8) {
                    return this.fail("Dragon scimitar not bought (out of gp or not stocked?)");
                }
                this.status = "Scim: buying";
                try {
                    Shop.buyOne("Dragon scimitar");
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                return 1500;
            }
            case 13: {
                this.status = "Scim: closing shop";
                this.ctx.input.clickWidgetOrFixed(300, 1, 482, 37);
                this.s = S.DONE;
                return 800;
            }
        }
        return Integer.MIN_VALUE;
    }

    private int fail(String string) {
        this.status = string;
        this.s = S.FAILED;
        return 600;
    }

    private boolean owns(int n) {
        if (this.ctx.gear.isEquipped(n)) {
            return true;
        }
        try {
            if (Inventory.getFirst(n) != null) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        String string = AioSkillerConstants.knownName(n);
        if (string != null) {
            try {
                if (Inventory.getFirst(string) != null) {
                    return true;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private int invCoins() {
        int n = 0;
        try {
            for (Item item : Inventory.getAll(995)) {
                if (item == null) continue;
                n += Math.max(1, item.getQuantity());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return n;
    }

    private boolean isKeeper(NPC nPC) {
        if (nPC == null) {
            return false;
        }
        if (nPC.getId() == 1158) {
            return true;
        }
        String string = nPC.getName();
        return string != null && string.equalsIgnoreCase("Warrior");
    }

    private NPC findKeeper(Player player) {
        NPC nPC2 = NPCs.getNearest(this::isKeeper);
        if (nPC2 != null) {
            return nPC2;
        }
        return NPCs.getNearest(nPC -> this.isKeeper((NPC)nPC) && nPC.getWorldLocation() != null && nPC.getWorldLocation().distanceTo(AioSkillerConstants.SCIM_SHOP_TILE) <= 15);
    }

    private boolean dialogOptionsOpen() {
        try {
            return Dialog.isOpen() && Dialog.isViewingOptions();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private static enum S {
        HOME,
        BANK_OPEN,
        CHECK,
        WITHDRAW_SCIM,
        WITHDRAW_GP,
        GRIND_HOME,
        GRIND,
        GRIND_BANK,
        GRIND_DEPOSIT,
        SHOP_FIND,
        SHOP_OPEN,
        SHOP_DIALOG,
        SHOP_BUY,
        SHOP_CLOSE,
        DONE,
        FAILED;

    }
}

