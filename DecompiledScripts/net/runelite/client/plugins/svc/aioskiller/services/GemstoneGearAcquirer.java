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

public final class GemstoneGearAcquirer {
    private static final int MAX_SWITCH_SELL_SESSIONS = 6;
    private static final int STALL_ARRIVE_DIST = 5;
    private final MasterContext ctx;
    private final StallThiefEngine engine;
    private S s = S.DONE;
    private String status = "";
    private int withdrawId = -1;
    private int withdrawTries = 0;
    private boolean gpWithdrawn = false;
    private boolean selling;
    private int startBankCoins;
    private int grindNeed;
    private StallThiefEngine.StallTarget currentStall;
    private boolean switchingStalls;
    private boolean switchRelocated;
    private int switchSellSessions;
    private Tab tab = Tab.WEAPONS;
    private B shopState = B.FIND;
    private int findAttempts;
    private int dialogWaits;
    private int buyAttempts;
    private int buyId = -1;

    public GemstoneGearAcquirer(MasterContext masterContext) {
        this.ctx = masterContext;
        this.engine = new StallThiefEngine(masterContext.client, masterContext.input);
        this.engine.setNeverSellIds(AioSkillerConstants.COIN_POUCH_IDS);
    }

    public void begin() {
        this.s = S.HOME;
        this.status = "Acquiring gemstone gear";
        this.withdrawId = -1;
        this.withdrawTries = 0;
        this.gpWithdrawn = false;
        this.selling = false;
        this.grindNeed = 0;
        this.startBankCoins = 0;
        this.currentStall = null;
        this.switchingStalls = false;
        this.switchRelocated = false;
        this.switchSellSessions = 0;
        this.tab = Tab.WEAPONS;
        this.shopState = B.FIND;
        this.buyAttempts = 0;
        this.dialogWaits = 0;
        this.findAttempts = 0;
        this.buyId = -1;
        this.engine.reset();
    }

    public void reset() {
        this.s = S.DONE;
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
                if (this.ownsAll()) {
                    this.status = "Gem gear: kit already owned (worn/inv)";
                    this.s = S.DONE;
                    return 300;
                }
                int n = this.ctx.nav.ensureHome(player);
                this.status = "Gem gear: home \u2014 " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                return n;
            }
            case 1: {
                int n = this.ctx.bank.openStep(player);
                this.status = "Gem gear: bank \u2014 " + this.ctx.bank.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.CHECK;
                    return 300;
                }
                return n;
            }
            case 2: {
                int n;
                if (!this.ctx.bank.isOpen()) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                if (this.ownsAll()) {
                    this.ctx.bank.close();
                    this.status = "Gem gear: kit secured";
                    this.s = S.DONE;
                    return 300;
                }
                if (this.needBuy() && (n = this.ctx.bank.count(995) + this.invCoins()) < 400000) {
                    this.startBankCoins = this.ctx.bank.count(995);
                    this.grindNeed = Math.max(1, 400000 - this.startBankCoins);
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
                this.withdrawId = -1;
                this.withdrawTries = 0;
                this.gpWithdrawn = false;
                this.s = S.WITHDRAW;
                return 300;
            }
            case 3: {
                int n = this.ctx.nav.ensureHome(player);
                this.status = "Gem gear gp grind: home \u2014 " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.GRIND;
                    return 400;
                }
                return n;
            }
            case 4: {
                WorldPoint worldPoint;
                if (this.ctx.bank.openNextCoinPouch()) {
                    this.status = "Gem gear gp grind: opening coin pouches";
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
                            this.status = "Gem gear gp grind: returning to " + this.currentStall.label + " stall to sell";
                            this.ctx.input.walkToward(worldPoint2, this.currentStall.coords());
                            return 1000;
                        }
                        this.switchRelocated = true;
                    }
                    this.selling = true;
                    int n = this.engine.sellAllStep(player, this.currentStall.traderNpcId);
                    this.status = "Gem gear gp grind \u2014 switching, selling at " + this.currentStall.label + " (" + this.switchSellSessions + "/6): " + this.engine.getStatus();
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
                    this.status = "Gem gear gp grind \u2014 selling: " + this.engine.getStatus();
                    if (this.engine.isSellingDone()) {
                        this.selling = false;
                    }
                    return n;
                }
                WorldPoint worldPoint4 = worldPoint = player == null ? null : player.getWorldLocation();
                if (worldPoint != null && !Walk.arrived(worldPoint, this.currentStall.coords(), 5)) {
                    this.status = "Gem gear gp grind: walking to " + this.currentStall.label + " stall";
                    this.ctx.input.walkToward(worldPoint, this.currentStall.coords());
                    return 1000;
                }
                int n = this.engine.stealStep(player, this.currentStall.objectId);
                this.status = "Gem gear gp grind (" + this.currentStall.label + "): " + this.invCoins() + "/" + this.grindNeed + " gp";
                return n;
            }
            case 5: {
                int n = this.ctx.bank.openStep(player);
                this.status = "Gem gear gp grind: banking \u2014 " + this.ctx.bank.status();
                if (n == Integer.MIN_VALUE) {
                    this.s = S.GRIND_DEPOSIT;
                    return 300;
                }
                return n;
            }
            case 6: {
                if (!this.ctx.bank.isOpen()) {
                    this.s = S.GRIND_BANK;
                    return 300;
                }
                this.status = "Gem gear gp grind: depositing";
                this.ctx.bank.depositInventory();
                this.s = S.CHECK;
                return 600;
            }
            case 7: {
                if (!this.ctx.bank.isOpen()) {
                    this.s = S.BANK_OPEN;
                    return 300;
                }
                if (this.needBuy() && !this.gpWithdrawn) {
                    if (this.ctx.bank.count(995) > 0) {
                        this.status = "Gem gear: withdrawing gp";
                        this.ctx.bank.withdrawAll(995);
                        this.gpWithdrawn = true;
                        return 700;
                    }
                    this.gpWithdrawn = true;
                    return 200;
                }
                int n = this.firstBankedUnowned();
                if (n != -1) {
                    if (n != this.withdrawId) {
                        this.withdrawId = n;
                        this.withdrawTries = 0;
                    }
                    if (++this.withdrawTries > 6) {
                        return this.fail("Could not withdraw banked gear: " + n);
                    }
                    String string = AioSkillerConstants.knownName(n);
                    this.status = "Gem gear: withdrawing banked (" + (string != null ? string : String.valueOf(n)) + ")";
                    if (string != null) {
                        this.ctx.bank.withdrawAllByName(string);
                    } else {
                        this.ctx.bank.withdrawAll(n);
                    }
                    return 500;
                }
                this.s = S.CLOSE_BANK;
                return 300;
            }
            case 8: {
                this.ctx.bank.close();
                this.tab = Tab.WEAPONS;
                this.shopState = B.FIND;
                this.buyAttempts = 0;
                this.dialogWaits = 0;
                this.findAttempts = 0;
                this.buyId = -1;
                this.s = S.SHOP;
                return 600;
            }
            case 9: {
                return this.stepShop(player);
            }
        }
        return Integer.MIN_VALUE;
    }

    private int stepShop(Player player) {
        String string;
        int[] nArray = this.tab == Tab.WEAPONS ? AioSkillerConstants.GEMSTONE_WEAPON_BUY_IDS : AioSkillerConstants.GEMSTONE_ARMOUR_BUY_IDS;
        int n = this.tab == Tab.WEAPONS ? 2 : 1;
        String string2 = string = this.tab == Tab.WEAPONS ? "Weaponry" : "Equipment";
        if (!this.anyMissing(nArray)) {
            return this.advanceTab();
        }
        switch (this.shopState.ordinal()) {
            case 0: {
                WorldPoint worldPoint = player == null ? null : player.getWorldLocation();
                NPC nPC = this.findKeeper(player);
                if (nPC != null && worldPoint != null) {
                    WorldPoint worldPoint2 = nPC.getWorldLocation();
                    if (Walk.arrived(worldPoint, worldPoint2, 3)) {
                        this.dialogWaits = 0;
                        this.shopState = B.OPEN;
                        return 400;
                    }
                    this.status = "Gem gear shop: walking to warrior";
                    this.ctx.input.walkToward(worldPoint, worldPoint2);
                    return 1200;
                }
                int n2 = this.ctx.nav.ensureHome(player);
                if (n2 != Integer.MIN_VALUE) {
                    this.status = "Gem gear shop: teleporting home \u2014 " + this.ctx.nav.status();
                    return n2;
                }
                WorldPoint worldPoint3 = worldPoint = player == null ? null : player.getWorldLocation();
                if (worldPoint != null && !Walk.arrived(worldPoint, AioSkillerConstants.SCIM_SHOP_TILE, 3)) {
                    this.status = "Gem gear shop: walking to warrior tile";
                    this.ctx.input.walkToward(worldPoint, AioSkillerConstants.SCIM_SHOP_TILE);
                    return 1200;
                }
                this.status = "Gem gear shop: locating warrior (" + this.findAttempts + ")";
                if (++this.findAttempts > 20) {
                    return this.fail("Warrior shopkeeper 1158 not found");
                }
                return 1000;
            }
            case 1: {
                if (this.ctx.shop.isOpen()) {
                    this.buyAttempts = 0;
                    this.buyId = -1;
                    this.shopState = B.BUY;
                    return 500;
                }
                if (this.dialogOptionsOpen()) {
                    this.shopState = B.DIALOG;
                    return 200;
                }
                NPC nPC = this.findKeeper(player);
                if (nPC != null) {
                    this.status = "Gem gear shop: Open-shop (" + string + ")";
                    this.ctx.input.npcActionVia(nPC, "Open-shop");
                    this.dialogWaits = 0;
                    this.shopState = B.DIALOG;
                    return 2500;
                }
                this.shopState = B.FIND;
                return 600;
            }
            case 2: {
                if (this.ctx.shop.isOpen()) {
                    this.buyAttempts = 0;
                    this.buyId = -1;
                    this.shopState = B.BUY;
                    return 500;
                }
                if (this.dialogOptionsOpen()) {
                    this.status = "Gem gear shop: choosing " + string + " (opt " + n + ")";
                    boolean bl = false;
                    try {
                        bl = Dialog.chooseOption(string);
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    if (!bl) {
                        try {
                            bl = Dialog.chooseOption(n);
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    if (!bl) {
                        this.ctx.input.key((char)(48 + n));
                    }
                    return 1500;
                }
                if (++this.dialogWaits > 8) {
                    this.shopState = B.OPEN;
                    return 600;
                }
                this.status = "Gem gear shop: waiting for dialogue";
                return 500;
            }
            case 3: {
                int n3 = this.firstMissing(nArray);
                if (n3 == -1) {
                    this.shopState = B.CLOSE;
                    return 400;
                }
                if (!this.ctx.shop.isOpen()) {
                    this.shopState = B.OPEN;
                    return 400;
                }
                if (n3 != this.buyId) {
                    this.buyId = n3;
                    this.buyAttempts = 0;
                }
                if (++this.buyAttempts > 8) {
                    return this.fail("Gem gear not bought: " + n3 + " (out of gp / not stocked?)");
                }
                String string3 = AioSkillerConstants.knownName(n3);
                this.status = "Gem gear shop: buying " + (string3 != null ? string3 : String.valueOf(n3));
                try {
                    if (string3 != null) {
                        Shop.buyOne(string3);
                    } else {
                        Shop.buyOne(n3);
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                return 1200;
            }
        }
        if (this.ctx.shop.isOpen()) {
            this.status = "Gem gear shop: closing";
            this.ctx.input.clickWidgetOrFixed(300, 1, 482, 37);
            return 600;
        }
        return this.advanceTab();
    }

    private int advanceTab() {
        if (this.tab == Tab.WEAPONS) {
            this.tab = Tab.ARMOUR;
            this.shopState = B.FIND;
            this.buyAttempts = 0;
            this.dialogWaits = 0;
            this.findAttempts = 0;
            this.buyId = -1;
            return 600;
        }
        if (this.ownsAll()) {
            this.status = "Gem gear: kit secured";
            this.s = S.DONE;
            return 400;
        }
        return this.fail("Could not acquire full gemstone kit (out of gp / not stocked?)");
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

    private boolean bankHas(int n) {
        if (this.ctx.bank.has(n)) {
            return true;
        }
        String string = AioSkillerConstants.knownName(n);
        return string != null && this.ctx.bank.hasItemNamed(string);
    }

    private int firstMissing(int[] nArray) {
        for (int n : nArray) {
            if (this.owns(n)) continue;
            return n;
        }
        return -1;
    }

    private boolean anyMissing(int[] nArray) {
        return this.firstMissing(nArray) != -1;
    }

    private boolean ownsAll() {
        return this.firstMissing(AioSkillerConstants.GEMSTONE_WEAPON_BUY_IDS) == -1 && this.firstMissing(AioSkillerConstants.GEMSTONE_ARMOUR_BUY_IDS) == -1 && this.starterSatisfied();
    }

    private boolean starterSatisfied() {
        for (int n : AioSkillerConstants.GEMSTONE_WITHDRAW_IDS) {
            if (this.owns(n) || !this.bankHas(n)) continue;
            return false;
        }
        return true;
    }

    private boolean needBuy() {
        for (int n : AioSkillerConstants.GEMSTONE_WEAPON_BUY_IDS) {
            if (this.owns(n) || this.bankHas(n)) continue;
            return true;
        }
        for (int n : AioSkillerConstants.GEMSTONE_ARMOUR_BUY_IDS) {
            if (this.owns(n) || this.bankHas(n)) continue;
            return true;
        }
        return false;
    }

    private int firstBankedUnowned() {
        for (int n : AioSkillerConstants.GEMSTONE_WEAPON_BUY_IDS) {
            if (this.owns(n) || !this.bankHas(n)) continue;
            return n;
        }
        for (int n : AioSkillerConstants.GEMSTONE_ARMOUR_BUY_IDS) {
            if (this.owns(n) || !this.bankHas(n)) continue;
            return n;
        }
        for (int n : AioSkillerConstants.GEMSTONE_WITHDRAW_IDS) {
            if (this.owns(n) || !this.bankHas(n)) continue;
            return n;
        }
        return -1;
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
        GRIND_HOME,
        GRIND,
        GRIND_BANK,
        GRIND_DEPOSIT,
        WITHDRAW,
        CLOSE_BANK,
        SHOP,
        DONE,
        FAILED;

    }

    private static enum Tab {
        WEAPONS,
        ARMOUR;

    }

    private static enum B {
        FIND,
        OPEN,
        DIALOG,
        BUY,
        CLOSE;

    }
}

