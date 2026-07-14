/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 */
package net.runelite.client.plugins.svc.crafting;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.core.SafeBank;
import net.runelite.client.plugins.svc.crafting.CraftingConstants;
import net.runelite.client.plugins.svc.crafting.Gem;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Production;

public final class CraftingEngine {
    private final Client client;
    private final HumanInput input;
    private final Random random = new Random();
    private State state = State.TO_BANK;
    private String status = "Starting";
    private int startXp = -1;
    private Gem currentGem = null;
    private int cuttableNow = 0;
    private int plannedQty = 0;
    private long combineSentAt = 0L;
    private long lastAnimAt = 0L;
    private long bankOpenAt = 0L;
    private int bankMisses = 0;
    private long progressAt = 0L;
    private int lastUncut = -1;
    private int lastCraftLevel = 0;
    private final Set<Gem> blacklist = new HashSet<Gem>();
    private boolean preferMenuCut = true;
    private int gemsCut = 0;
    private int trips = 0;

    public CraftingEngine(Client client) {
        this(client, null);
    }

    public CraftingEngine(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public void reset() {
        this.state = State.TO_BANK;
        this.status = "Starting";
        this.currentGem = null;
        this.cuttableNow = 0;
        this.plannedQty = 0;
        this.combineSentAt = 0L;
        this.lastAnimAt = 0L;
        this.bankMisses = 0;
        this.progressAt = 0L;
        this.lastUncut = -1;
        this.lastCraftLevel = 0;
        this.blacklist.clear();
        this.preferMenuCut = true;
        this.gemsCut = 0;
        this.trips = 0;
        this.startXp = -1;
    }

    public void setStatus(String string) {
        this.status = string;
    }

    public int tick(Player player, Settings settings) {
        if (this.startXp < 0) {
            this.startXp = Skills.getExperience(Skill.CRAFTING);
        }
        if (SafeBank.clearStrayAmountPrompt()) {
            this.status = "Closing stray amount prompt";
            return 700;
        }
        switch (this.state.ordinal()) {
            case 0: {
                return this.toBank(player);
            }
            case 1: {
                return this.waitBankOpen();
            }
            case 2: {
                return this.decide(settings);
            }
            case 3: {
                return this.deposit();
            }
            case 4: {
                return this.withdraw();
            }
            case 5: {
                return this.closeBank();
            }
            case 6: {
                return this.cut(player);
            }
            case 7: {
                return this.idle();
            }
        }
        return 600;
    }

    private TileObject findBank() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 10083);
        if (tileObject2 != null) {
            return tileObject2;
        }
        TileObject tileObject3 = TileObjects.getNearest("Bank booth");
        return tileObject3 != null ? tileObject3 : TileObjects.getNearest("Bank chest");
    }

    private int toBank(Player player) {
        if (Bank.isOpen()) {
            this.state = State.DECIDE;
            return 250;
        }
        TileObject tileObject = this.findBank();
        if (tileObject == null) {
            if (++this.bankMisses > 10) {
                this.status = "Bank not found \u2014 stopping";
                return -1;
            }
            this.status = "Bank not found";
            return 800;
        }
        this.bankMisses = 0;
        if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 3) {
            this.status = "Walking to bank";
            if (this.input != null) {
                this.input.walkToward(player.getWorldLocation(), tileObject.getWorldLocation());
            } else {
                MovementPackets.sendMovement(tileObject.getWorldLocation());
            }
            return 1200;
        }
        this.status = "Opening bank";
        if (this.input != null) {
            this.input.interactObjectVia(tileObject, "Use");
        } else {
            tileObject.interact("Use");
        }
        this.bankOpenAt = System.currentTimeMillis();
        this.state = State.WAIT_BANK_OPEN;
        return 1000;
    }

    private int waitBankOpen() {
        if (Bank.isOpen()) {
            this.state = State.DECIDE;
            return 250;
        }
        if (System.currentTimeMillis() - this.bankOpenAt > 4000L) {
            this.status = "Bank didn't open \u2014 retrying";
            this.state = State.TO_BANK;
            return 600;
        }
        this.status = "Waiting for bank...";
        return 400;
    }

    private int decide(Settings settings) {
        boolean bl;
        if (!Bank.isOpen()) {
            this.state = State.TO_BANK;
            return 500;
        }
        boolean bl2 = bl = Inventory.contains(CraftingEngine::isChisel) || Bank.getFirst(CraftingEngine::isChisel) != null;
        if (!bl) {
            this.currentGem = null;
            this.cuttableNow = 0;
            this.plannedQty = 0;
            Bank.close();
            this.status = "No chisel found \u2014 stopped";
            return -1;
        }
        int n = this.client.getBoostedSkillLevel(Skill.CRAFTING);
        if (n > this.lastCraftLevel) {
            this.blacklist.clear();
        }
        this.lastCraftLevel = n;
        this.currentGem = this.selectGem(n);
        if (this.currentGem == null) {
            this.cuttableNow = 0;
            this.plannedQty = 0;
            Bank.close();
            if (settings.stopWhenIdle) {
                this.status = "No cuttable gems \u2014 stopped";
                return -1;
            }
            this.status = "No cuttable gems \u2014 re-checking soon";
            this.state = State.IDLE;
            return 15000;
        }
        this.cuttableNow = this.bankCount(this.currentGem);
        this.plannedQty = Math.max(1, Math.min(27, this.cuttableNow));
        this.status = "Cutting " + this.currentGem.cutName + " (lvl " + this.currentGem.level + ")";
        this.state = State.DEPOSIT;
        return 350;
    }

    private int deposit() {
        boolean bl;
        if (!Bank.isOpen()) {
            this.state = State.TO_BANK;
            return 500;
        }
        boolean bl2 = bl = !Inventory.getAll(item -> item != null && !CraftingEngine.isChisel(item)).isEmpty();
        if (bl) {
            this.status = "Depositing";
            Bank.depositAllExcept("Chisel");
            return 450;
        }
        this.state = State.WITHDRAW;
        return 250;
    }

    private int withdraw() {
        if (!Bank.isOpen()) {
            this.state = State.TO_BANK;
            return 500;
        }
        if (!Inventory.contains(CraftingEngine::isChisel)) {
            Item item = Bank.getFirst(CraftingEngine::isChisel);
            if (item == null) {
                this.status = "No chisel in bank \u2014 re-deciding";
                this.state = State.DECIDE;
                return 500;
            }
            Bank.withdraw(item.getId(), 1, Bank.WithdrawMode.ITEM);
            return 350;
        }
        if (Inventory.contains(CraftingEngine.uncutPredicate(this.currentGem))) {
            this.status = "Withdrew " + this.currentGem.uncutName;
            this.state = State.CLOSE_BANK;
            return 250;
        }
        Item item = Bank.getFirst(CraftingEngine.uncutPredicate(this.currentGem));
        if (item == null) {
            this.status = "Out of " + this.currentGem.uncutName + " \u2014 re-deciding";
            this.state = State.DECIDE;
            return 500;
        }
        this.status = "Withdrawing " + this.currentGem.uncutName;
        Bank.withdrawAll(item.getId(), Bank.WithdrawMode.ITEM);
        return 600;
    }

    private int closeBank() {
        if (Bank.isOpen()) {
            Bank.close();
        }
        this.combineSentAt = 0L;
        this.lastAnimAt = 0L;
        this.progressAt = System.currentTimeMillis();
        this.lastUncut = -1;
        this.state = State.CUT;
        return 450;
    }

    private int cut(Player player) {
        long l = System.currentTimeMillis();
        int n = Inventory.getAll(CraftingEngine.uncutPredicate(this.currentGem)).size();
        if (player.getAnimation() != -1 || this.lastUncut < 0 || n < this.lastUncut) {
            this.lastUncut = n;
            this.progressAt = l;
        } else if (l - this.progressAt > 4500L) {
            if (this.preferMenuCut) {
                this.preferMenuCut = false;
                this.progressAt = l;
                this.combineSentAt = 0L;
                this.status = "Switching cut method";
                return 400;
            }
            this.blacklist.add(this.currentGem);
            this.status = "Can't cut " + this.currentGem.cutName + " (need lvl " + this.currentGem.level + "+) \u2014 skipping";
            this.state = State.TO_BANK;
            return 500;
        }
        if (n == 0) {
            this.gemsCut += this.plannedQty;
            ++this.trips;
            this.status = "Batch done \u2014 banking";
            this.state = State.TO_BANK;
            return 400;
        }
        if (player.getAnimation() != -1) {
            this.lastAnimAt = l;
            this.status = "Cutting " + this.currentGem.cutName + " (" + n + " left)";
            return this.jitter(900);
        }
        if (this.lastAnimAt > 0L && l - this.lastAnimAt < 1000L) {
            return 350;
        }
        if (this.isMakeInterfaceOpen() || this.combineSentAt > 0L) {
            this.status = "Cut-all " + this.currentGem.cutName;
            Keyboard.sendSpace();
            this.combineSentAt = 0L;
            this.lastAnimAt = l;
            return this.jitter(900);
        }
        Item item = Inventory.getFirst(CraftingEngine::isChisel);
        Item item2 = Inventory.getFirst(CraftingEngine.uncutPredicate(this.currentGem));
        if (item != null && item2 != null) {
            this.status = "Using chisel on " + this.currentGem.uncutName;
            this.cutGem(item, item2);
            this.combineSentAt = l;
            return 900;
        }
        this.status = "Chisel missing \u2014 banking";
        this.state = State.TO_BANK;
        return 500;
    }

    private boolean isMakeInterfaceOpen() {
        try {
            if (Production.isOpen()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return Dialog.canContinue() || Dialog.isViewingOptions();
    }

    private void cutGem(Item item, Item item2) {
        if (this.preferMenuCut) {
            if (this.input != null) {
                this.input.useItemOnItem(item, item2);
            } else {
                item.useOn(item2);
            }
        } else if (this.input != null) {
            this.input.useItemOnItemPacket(item, item2);
        } else {
            MousePackets.queueClickPacket();
            ItemPackets.useItemOnItem(item, item2);
        }
    }

    private int idle() {
        this.status = "Idle \u2014 re-checking bank";
        this.state = State.TO_BANK;
        return 600;
    }

    public boolean hasCuttableGems() {
        int n = this.client.getBoostedSkillLevel(Skill.CRAFTING);
        for (Gem gem : Gem.byLevelDesc()) {
            if (n < gem.level) continue;
            if (this.bankCount(gem) >= 1) {
                return true;
            }
            if (!Inventory.contains(CraftingEngine.uncutPredicate(gem))) continue;
            return true;
        }
        return false;
    }

    private Gem selectGem(int n) {
        for (Gem gem : Gem.byLevelDesc()) {
            if (this.blacklist.contains((Object)gem) || n < gem.level || this.bankCount(gem) < 1) continue;
            return gem;
        }
        return null;
    }

    private int bankCount(Gem gem) {
        List<Item> list = Bank.getAll();
        if (list == null) {
            return 0;
        }
        int n = 0;
        Predicate<Item> predicate = CraftingEngine.uncutPredicate(gem);
        for (Item item : list) {
            if (!predicate.test(item)) continue;
            n += Math.max(1, item.getQuantity());
        }
        return n;
    }

    private static Predicate<Item> uncutPredicate(Gem gem) {
        return item -> item != null && item.getName() != null && CraftingConstants.matches(item.getName(), gem.uncutName);
    }

    private static boolean isChisel(Item item) {
        return item != null && item.getName() != null && CraftingConstants.matches(item.getName(), "Chisel");
    }

    private int jitter(int n) {
        int n2 = 180;
        return n2 > 0 ? n + this.random.nextInt(n2 + 1) : n;
    }

    public String getStatus() {
        return this.status;
    }

    public String getStateName() {
        return this.state.name();
    }

    public boolean isBanking() {
        return this.state == State.TO_BANK || this.state == State.WAIT_BANK_OPEN || this.state == State.DECIDE || this.state == State.DEPOSIT || this.state == State.WITHDRAW || this.state == State.CLOSE_BANK;
    }

    public boolean isCutting() {
        return this.state == State.CUT;
    }

    public boolean isIdle() {
        return this.state == State.IDLE;
    }

    public String getGemName() {
        return this.currentGem == null ? "\u2014" : this.currentGem.cutName;
    }

    public int getGemLevel() {
        return this.currentGem == null ? 0 : this.currentGem.level;
    }

    public int getCuttableNow() {
        return this.cuttableNow;
    }

    public int getPlannedQty() {
        return this.plannedQty;
    }

    public int getGemsCut() {
        return this.gemsCut;
    }

    public int getTrips() {
        return this.trips;
    }

    public int getCraftLevel() {
        return this.client.getBoostedSkillLevel(Skill.CRAFTING);
    }

    public int getXpGained() {
        return this.startXp < 0 ? 0 : Math.max(0, Skills.getExperience(Skill.CRAFTING) - this.startXp);
    }

    private static enum State {
        TO_BANK,
        WAIT_BANK_OPEN,
        DECIDE,
        DEPOSIT,
        WITHDRAW,
        CLOSE_BANK,
        CUT,
        IDLE;

    }

    public static final class Settings {
        public final boolean stopWhenIdle;

        public Settings(boolean bl) {
            this.stopWhenIdle = bl;
        }
    }
}

