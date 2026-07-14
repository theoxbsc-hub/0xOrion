/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 */
package net.runelite.client.plugins.ox.smithing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.ox.core.SafeBank;
import net.runelite.client.plugins.ox.smithing.SmeltRecipe;
import net.runelite.client.plugins.ox.smithing.SmithMetal;
import net.runelite.client.plugins.ox.smithing.SmithingConstants;
import net.runelite.client.plugins.ox.smithing.SmithingMode;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Production;

public final class SmithingEngine {
    private final Client client;
    private final Random random = new Random();
    private Settings settings;
    private SmState state = SmState.DECIDE;
    private String status = "Starting";
    private PlannedAction currentPlan = null;
    private int startXp = -1;
    private int stateEntryTick = 0;
    private long lastProgressAt = 0L;
    private int watchdogTrips = 0;
    private int xpAtWatchdogReset = -1;
    private WorldPoint walkTarget = null;
    private long walkStartMs = 0L;
    private long lastInteractMs = 0L;
    private long lastAnimAt = 0L;
    private int lastConsumedCount = -1;
    private long lastWorkAt = 0L;
    private long lastSelectAt = 0L;
    private int xpAtSelect = -1;
    private int wdMaterialIdx = -1;
    private int wdLastHave = -1;
    private long wdStableSince = 0L;
    private volatile boolean stationFailed = false;
    private boolean preferObjectAction = false;
    private int stationAttempts = 0;
    private final Set<Integer> smithItemBlacklist = ConcurrentHashMap.newKeySet();
    private final Set<Integer> smithMetalBlacklist = ConcurrentHashMap.newKeySet();
    private volatile int lastSmithItemId = -1;
    private int lastKnownSmithLevel = -1;
    private int smithBatchStartBars = -1;
    private static final Pattern BAR_TEXT = Pattern.compile("(\\d+)\\s*bars?\\b");
    private static final String[][] SMITH_ITEM_OFFSETS = new String[][]{{"2-hand sword", "14"}, {"2h sword", "14"}, {"two-handed", "14"}, {"longsword", "6"}, {"long sword", "6"}, {"platebody", "18"}, {"platelegs", "16"}, {"plateskirt", "16"}, {"full helm", "7"}, {"med helm", "3"}, {"medium helm", "3"}, {"square shield", "8"}, {"sq shield", "8"}, {"kiteshield", "12"}, {"kite shield", "12"}, {"chainbody", "11"}, {"chain body", "11"}, {"warhammer", "9"}, {"war hammer", "9"}, {"battleaxe", "10"}, {"battle axe", "10"}, {"scimitar", "5"}, {"claws", "13"}, {"throwing knife", "7"}, {"knife", "7"}, {"dart tip", "4"}, {"dart", "4"}, {"arrowtip", "5"}, {"arrowheads", "5"}, {"arrow", "5"}, {"nails", "4"}, {"dagger", "0"}, {"mace", "2"}, {"axe", "1"}, {"sword", "4"}};
    private int madeCount = 0;
    private int barsSmelted = 0;
    private int itemsSmithed = 0;
    private int tripCount = 0;

    public SmithingEngine(Client client) {
        this.client = client;
        this.reset();
    }

    public void reset() {
        this.state = SmState.DECIDE;
        this.status = "Starting";
        this.currentPlan = null;
        this.startXp = -1;
        this.watchdogTrips = 0;
        this.xpAtWatchdogReset = -1;
        this.lastProgressAt = System.currentTimeMillis();
        this.walkTarget = null;
        this.lastInteractMs = 0L;
        this.lastAnimAt = 0L;
        this.lastConsumedCount = -1;
        this.lastWorkAt = 0L;
        this.lastSelectAt = 0L;
        this.xpAtSelect = -1;
        this.wdMaterialIdx = -1;
        this.wdLastHave = -1;
        this.wdStableSince = 0L;
        this.stationFailed = false;
        this.preferObjectAction = false;
        this.stationAttempts = 0;
        this.smithItemBlacklist.clear();
        this.smithMetalBlacklist.clear();
        this.lastSmithItemId = -1;
        this.lastKnownSmithLevel = -1;
        this.smithBatchStartBars = -1;
        this.madeCount = 0;
        this.barsSmelted = 0;
        this.itemsSmithed = 0;
        this.tripCount = 0;
    }

    public void onChatMessage(String string) {
        boolean bl;
        if (string == null) {
            return;
        }
        if (string.contains("you smelt") || string.contains("you make") || string.contains("you smith") || string.contains("you hammer") || string.contains("you retrieve") || string.contains("you superheat")) {
            this.markProgress();
            return;
        }
        boolean bl2 = bl = string.contains("level") && (string.contains("need") || string.contains("higher") || string.contains("require")) || string.contains("higher") && string.contains("smith");
        if (bl && this.state == SmState.PROCESS_SMITH) {
            int n = this.lastSmithItemId;
            if (n > 0) {
                this.smithItemBlacklist.add(n);
            }
            this.markProgress();
            return;
        }
        if (string.contains("space")) {
            return;
        }
        if (string.contains("not enough") || string.contains("need more") || string.contains("don't have") || string.contains("you cannot") || string.contains("you need") || string.contains("require a higher")) {
            this.stationFailed = true;
        }
    }

    public int tick(Player player, Settings settings) {
        this.settings = settings;
        if (this.startXp < 0) {
            this.startXp = Skills.getExperience(Skill.SMITHING);
        }
        int n = Skills.getLevel(Skill.SMITHING);
        if (this.lastKnownSmithLevel < 0) {
            this.lastKnownSmithLevel = n;
        } else if (n > this.lastKnownSmithLevel) {
            this.lastKnownSmithLevel = n;
            this.smithItemBlacklist.clear();
            this.smithMetalBlacklist.clear();
        }
        if (this.state == SmState.STOPPED) {
            return -1;
        }
        if (SafeBank.clearStrayAmountPrompt()) {
            this.status = "Closing stray amount prompt";
            this.markProgress();
            return 700;
        }
        if (System.currentTimeMillis() - this.lastProgressAt > 60000L) {
            if (++this.watchdogTrips >= 4) {
                this.status = "Stuck with no progress \u2014 stopping";
                this.state = SmState.STOPPED;
                return -1;
            }
            this.status = "No progress \u2014 re-banking";
            return this.enter(SmState.OPEN_BANK, 600);
        }
        switch (this.state.ordinal()) {
            case 0: {
                return this.loopDecide();
            }
            case 1: {
                return this.loopOpenBank(player);
            }
            case 2: {
                return this.loopDeposit();
            }
            case 3: {
                return this.loopWithdraw();
            }
            case 4: {
                return this.loopWalkToStation(player);
            }
            case 5: {
                return this.loopProcess(player, false);
            }
            case 6: {
                return this.loopProcess(player, true);
            }
            case 7: {
                return this.loopWalkToBank(player);
            }
        }
        return 600;
    }

    private int loopDecide() {
        if (!Bank.isOpen()) {
            return this.enter(SmState.OPEN_BANK, 300);
        }
        int n = Skills.getLevel(Skill.SMITHING);
        PlannedAction plannedAction = this.decideAction(n);
        if (plannedAction == null) {
            if (this.settings.stopWhenOutOfMaterials) {
                this.status = "Nothing to smelt/smith at level " + n + " \u2014 stopping";
                Bank.close();
                this.state = SmState.STOPPED;
                return -1;
            }
            this.markProgress();
            this.status = "Nothing to make \u2014 waiting for materials";
            return 5000;
        }
        this.currentPlan = plannedAction;
        this.wdMaterialIdx = -1;
        int n2 = Skills.getExperience(Skill.SMITHING);
        if (this.xpAtWatchdogReset < 0 || n2 > this.xpAtWatchdogReset) {
            this.watchdogTrips = 0;
            this.xpAtWatchdogReset = n2;
        }
        this.status = "Plan: " + plannedAction.label;
        return this.enter(SmState.DEPOSIT, 300);
    }

    private int loopOpenBank(Player player) {
        if (Bank.isOpen()) {
            return this.enter(SmState.DECIDE, 600);
        }
        TileObject tileObject = this.findBankBooth();
        if (tileObject == null) {
            this.status = "Bank booth not found \u2014 walking to bank";
            this.stepToward(SmithingConstants.BANK_TILE, player);
            return 1200;
        }
        if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 4) {
            this.status = "Walking to bank";
            this.stepToward(tileObject.getWorldLocation(), player);
            return 1200;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastInteractMs < 2500L) {
            this.status = "Opening bank...";
            return 500;
        }
        this.status = "Opening bank (Use)";
        tileObject.interact("Use");
        this.lastInteractMs = l;
        return 1000;
    }

    private int loopDeposit() {
        if (!Bank.isOpen()) {
            return this.enter(SmState.OPEN_BANK, 300);
        }
        if (!Inventory.isEmpty()) {
            this.status = "Depositing";
            Bank.depositInventory();
            return 700;
        }
        return this.enter(SmState.WITHDRAW, 300);
    }

    private int loopWithdraw() {
        if (!Bank.isOpen()) {
            return this.enter(SmState.OPEN_BANK, 300);
        }
        if (this.currentPlan == null) {
            return this.enter(SmState.DECIDE, 300);
        }
        long l = System.currentTimeMillis();
        if (this.currentPlan.kind == PlannedAction.Kind.SMITH && this.invCount(2347, "Hammer") < 1) {
            this.status = "Withdrawing hammer";
            this.issueWithdraw(-2, 2347, "Hammer", this.invCount(2347, "Hammer"), 1, l);
            return 500;
        }
        for (int i = 0; i < this.currentPlan.wIds.length; ++i) {
            int n = this.invCount(this.currentPlan.wIds[i], this.currentPlan.wNames[i]);
            int n2 = this.currentPlan.wQtys[i] - n;
            if (n2 <= 0) continue;
            if (Inventory.getFreeSlots() <= 0) break;
            this.status = "Withdrawing " + this.currentPlan.wNames[i];
            this.issueWithdraw(i, this.currentPlan.wIds[i], this.currentPlan.wNames[i], n, n2, l);
            if (this.ticksInState() > 14) {
                this.status = "Materials changed \u2014 re-planning";
                return this.enter(SmState.DECIDE, 400);
            }
            return 500;
        }
        Bank.close();
        this.stationFailed = false;
        this.lastAnimAt = 0L;
        this.lastConsumedCount = -1;
        this.lastWorkAt = 0L;
        this.lastSelectAt = 0L;
        this.xpAtSelect = -1;
        this.lastInteractMs = 0L;
        this.wdMaterialIdx = -1;
        return this.enter(SmState.WALK_TO_STATION, 800);
    }

    private void issueWithdraw(int n, int n2, String string, int n3, int n4, long l) {
        if (n4 <= 0) {
            return;
        }
        if (n != this.wdMaterialIdx) {
            this.wdMaterialIdx = n;
            this.wdLastHave = n3;
            this.wdStableSince = l;
            this.withdrawMaterial(n2, string, n4);
            return;
        }
        if (n3 != this.wdLastHave) {
            this.wdLastHave = n3;
            this.wdStableSince = l;
            return;
        }
        if (l - this.wdStableSince >= 1000L) {
            this.wdStableSince = l;
            this.withdrawMaterial(n2, string, n4);
        }
    }

    private int loopWalkToStation(Player player) {
        if (this.arrivedAt(SmithingConstants.STATION_TILE, 5, player)) {
            boolean bl = this.currentPlan != null && this.currentPlan.kind == PlannedAction.Kind.SMITH;
            return this.enter(bl ? SmState.PROCESS_SMITH : SmState.PROCESS_SMELT, 400);
        }
        this.status = "Walking to furnace/anvil";
        this.stepToward(SmithingConstants.STATION_TILE, player);
        return 1200;
    }

    private int loopProcess(Player player, boolean bl) {
        TileObject tileObject;
        long l = System.currentTimeMillis();
        if (Dialog.canContinue() && !this.isSmithInterfaceOpen() && !this.isSmeltInterfaceOpen()) {
            String string = "";
            try {
                string = Dialog.getText() == null ? "" : Dialog.getText().toLowerCase();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (bl && string.contains("level") && this.lastSmithItemId > 0) {
                this.smithItemBlacklist.add(this.lastSmithItemId);
            }
            try {
                Dialog.continueSpace();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.markProgress();
            this.status = "Dismissing dialog" + (string.contains("level") ? " (level gate)" : "");
            return 600;
        }
        if (bl && this.smithBatchStartBars < 0) {
            this.smithBatchStartBars = this.invCount(this.currentPlan.wIds[0], this.currentPlan.wNames[0]);
        }
        int n = this.consumedMaterialCount(bl);
        if (this.lastConsumedCount >= 0 && n < this.lastConsumedCount) {
            this.lastWorkAt = l;
            this.lastSelectAt = 0L;
            this.stationAttempts = 0;
            this.markProgress();
        }
        this.lastConsumedCount = n;
        int n2 = Skills.getExperience(Skill.SMITHING);
        if (this.xpAtSelect >= 0 && n2 > this.xpAtSelect) {
            this.lastWorkAt = l;
            this.lastSelectAt = 0L;
            this.xpAtSelect = n2;
            this.stationAttempts = 0;
            this.markProgress();
        }
        if (player.getAnimation() != -1) {
            this.lastAnimAt = l;
            this.lastWorkAt = l;
            this.lastSelectAt = 0L;
            this.stationAttempts = 0;
            this.markProgress();
            this.status = (bl ? "Smithing " : "Smelting ") + this.currentPlan.outputName;
            return this.jitter(900);
        }
        if (this.stationFailed || this.isBatchDone(bl)) {
            int n3;
            if (bl) {
                int n4 = this.invCount(this.currentPlan.wIds[0], this.currentPlan.wNames[0]);
                n3 = Math.max(0, (this.smithBatchStartBars < 0 ? 0 : this.smithBatchStartBars) - n4);
                this.itemsSmithed += n3;
            } else {
                n3 = Math.max(0, this.invCount(this.currentPlan.outputId, this.currentPlan.outputName));
                this.barsSmelted += n3;
            }
            this.madeCount += n3;
            ++this.tripCount;
            this.stationFailed = false;
            this.smithBatchStartBars = -1;
            this.status = "Made " + n3 + " " + this.currentPlan.outputName + " \u2014 banking";
            return this.enter(SmState.WALK_TO_BANK, 400);
        }
        if (bl ? this.isSmithInterfaceOpen() : this.isMakeInterfaceOpen()) {
            this.stationAttempts = 0;
            if (Inventory.getFreeSlots() <= 0) {
                this.status = "Inventory full \u2014 re-banking";
                return this.enter(SmState.WALK_TO_BANK, 400);
            }
            if (bl) {
                if (this.selectSmithTarget()) {
                    this.lastWorkAt = l;
                    this.lastSelectAt = l;
                    this.xpAtSelect = Skills.getExperience(Skill.SMITHING);
                    this.status = "Smithing best item (" + this.currentPlan.metal.metalName + ")";
                    return 1200;
                }
                this.smithMetalBlacklist.add(this.currentPlan.wIds[0]);
                this.status = "No " + this.currentPlan.metal.metalName + " item at level " + this.getSmithLevel() + " \u2014 re-planning";
                this.stationFailed = true;
                return 600;
            }
            this.status = "Confirming make (" + this.currentPlan.outputName + ")";
            if (this.selectSmeltTarget()) {
                this.lastAnimAt = l;
                this.lastWorkAt = l;
                this.lastSelectAt = l;
                this.xpAtSelect = Skills.getExperience(Skill.SMITHING);
                return 1200;
            }
            this.selectTarget(this.currentPlan);
            this.lastSelectAt = l;
            this.xpAtSelect = Skills.getExperience(Skill.SMITHING);
            return 1200;
        }
        if (bl && this.invCount(2347, "Hammer") < 1) {
            this.status = "No hammer \u2014 banking";
            return this.enter(SmState.WALK_TO_BANK, 400);
        }
        if (Movement.isWalking()) {
            this.status = "Approaching " + (bl ? "Anvil" : "Furnace");
            return 500;
        }
        if (this.lastSelectAt > 0L && l - this.lastSelectAt < 5000L) {
            this.status = "Waiting for " + (bl ? "smith" : "smelt") + " to start (" + this.currentPlan.outputName + ")";
            return 500;
        }
        if (this.lastWorkAt > 0L && l - this.lastWorkAt < 3600L) {
            this.status = (bl ? "Smithing " : "Smelting ") + this.currentPlan.outputName;
            return 400;
        }
        if (l - this.lastInteractMs < 2500L) {
            return 500;
        }
        TileObject tileObject2 = tileObject = bl ? this.findObject(28563, "Anvil") : this.findObject(165, "Furnace");
        if (tileObject == null) {
            this.status = (bl ? "Anvil" : "Furnace") + " not found";
            return 800;
        }
        this.status = (bl ? "Using anvil" : "Using furnace") + (this.preferObjectAction ? " (click)" : " (use bar/ore)");
        this.openStation(tileObject, bl);
        if (++this.stationAttempts >= 3) {
            this.preferObjectAction = !this.preferObjectAction;
            this.stationAttempts = 0;
        }
        this.lastInteractMs = l;
        return 1200;
    }

    private void openStation(TileObject tileObject, boolean bl) {
        Item item;
        if (!this.preferObjectAction && (item = this.stationItem(bl)) != null) {
            item.useOn(tileObject);
            return;
        }
        MousePackets.queueClickPacket();
        ObjectPackets.tileObjectFirstOption(tileObject, false);
    }

    private Item stationItem(boolean bl) {
        if (this.currentPlan == null) {
            return null;
        }
        if (bl) {
            return this.invFirst(this.currentPlan.wIds[0], this.currentPlan.wNames[0]);
        }
        SmeltRecipe smeltRecipe = this.currentPlan.recipe;
        return smeltRecipe == null || smeltRecipe.ingredientIds.length == 0 ? null : this.invFirst(smeltRecipe.ingredientIds[0], smeltRecipe.ingredientNames[0]);
    }

    private Item invFirst(int n, String string) {
        if (n > 0) {
            try {
                Item item2 = Inventory.getFirst(n);
                if (item2 != null) {
                    return item2;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (string != null) {
            try {
                return Inventory.getFirst(item -> item != null && item.getName() != null && item.getName().equalsIgnoreCase(string));
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private int consumedMaterialCount(boolean bl) {
        if (this.currentPlan == null) {
            return 0;
        }
        if (bl) {
            return this.invCount(this.currentPlan.wIds[0], this.currentPlan.wNames[0]);
        }
        SmeltRecipe smeltRecipe = this.currentPlan.recipe;
        if (smeltRecipe == null || smeltRecipe.ingredientIds.length == 0) {
            return 0;
        }
        return this.invCount(smeltRecipe.ingredientIds[0], smeltRecipe.ingredientNames[0]);
    }

    private boolean isBatchDone(boolean bl) {
        if (this.currentPlan == null) {
            return true;
        }
        if (bl) {
            return this.invCount(this.currentPlan.wIds[0], this.currentPlan.wNames[0]) < 1;
        }
        SmeltRecipe smeltRecipe = this.currentPlan.recipe;
        for (int i = 0; i < smeltRecipe.ingredientIds.length; ++i) {
            if (this.invCount(smeltRecipe.ingredientIds[i], smeltRecipe.ingredientNames[i]) >= smeltRecipe.perBar[i]) continue;
            return true;
        }
        return false;
    }

    private boolean isMakeInterfaceOpen() {
        if (this.isSmeltInterfaceOpen()) {
            return true;
        }
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

    private boolean isSmeltInterfaceOpen() {
        for (int i = 14; i <= 30; ++i) {
            Widget widget = this.safeWidget(270, i);
            if (widget == null || widget.isHidden() || this.firstItemId(widget) <= 0) continue;
            return true;
        }
        return false;
    }

    private boolean selectSmeltTarget() {
        if (this.currentPlan == null) {
            return false;
        }
        int n = this.currentPlan.outputId;
        Widget widget = null;
        int n2 = -1;
        int n3 = 0;
        for (int i = 14; i <= 30; ++i) {
            int n4;
            Widget widget2 = this.safeWidget(270, i);
            if (widget2 == null || widget2.isHidden() || (n4 = this.firstItemId(widget2)) <= 0) continue;
            if (n4 == n) {
                widget = widget2;
                n2 = n3;
                break;
            }
            ++n3;
        }
        if (widget == null) {
            return false;
        }
        if (this.allQuantityClickable()) {
            Widget widget3 = this.safeWidget(270, 12);
            try {
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(widget3);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        try {
            if (n2 == 0) {
                Keyboard.sendSpace();
            } else if (n2 > 0) {
                Keyboard.type((char)(49 + n2));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.status = "Smelting " + this.currentPlan.outputName + " [key " + (n2 == 0 ? "space" : Character.toString((char)(49 + n2))) + "]";
        return true;
    }

    private boolean allQuantityClickable() {
        Widget widget = this.safeWidget(270, 12);
        if (widget == null || widget.isHidden()) {
            return false;
        }
        if (this.usableAction(widget) != null) {
            return true;
        }
        for (Widget widget2 : this.kidsOf(widget)) {
            if (this.usableAction(widget2) == null) continue;
            return true;
        }
        return false;
    }

    private String usableAction(Widget widget) {
        if (widget == null) {
            return null;
        }
        try {
            String[] stringArray = widget.getActions();
            if (stringArray != null) {
                for (String string : stringArray) {
                    String string2;
                    if (string == null || (string2 = string.replaceAll("<[^>]*>", "").trim()).isEmpty() || string2.equalsIgnoreCase("Cancel")) continue;
                    return string2;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private void selectTarget(PlannedAction plannedAction) {
        String string = plannedAction.outputName;
        try {
            if (Production.isOpen()) {
                Production.chooseOption(string);
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (Dialog.isViewingOptions() && Dialog.chooseOption(string)) {
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private boolean isSmithInterfaceOpen() {
        Widget widget = this.safeWidget(312, 0);
        if (widget != null && !widget.isHidden()) {
            return true;
        }
        for (int i = 9; i <= 50; ++i) {
            Widget widget2 = this.safeWidget(312, i);
            if (widget2 == null || widget2.isHidden()) continue;
            return true;
        }
        return false;
    }

    private boolean selectSmithTarget() {
        Widget widget = this.safeWidget(312, 7);
        if (widget != null && !widget.isHidden()) {
            try {
                WidgetPackets.widgetFirstOption(widget);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        int n = Skills.getLevel(Skill.SMITHING);
        int n2 = this.invCount(this.currentPlan.wIds[0], this.currentPlan.wNames[0]);
        SmithMetal smithMetal = this.currentPlan.metal;
        Widget widget2 = null;
        int n3 = 0;
        int n4 = -1;
        for (int i = 9; i <= 50; ++i) {
            int n5;
            int n6;
            int n7;
            Widget widget3 = this.safeWidget(312, i);
            if (widget3 == null || widget3.isHidden() || !this.hasSmithAction(widget3) || (n7 = this.firstItemId(widget3)) <= 0 || this.smithItemBlacklist.contains(n7) || (n6 = this.firstBarCount(widget3)) < 1 || n6 > n2 || (n5 = this.smithRequiredLevel(widget3, smithMetal)) > 0 && n < n5 || n6 <= n3) continue;
            widget2 = widget3;
            n3 = n6;
            n4 = n7;
        }
        if (widget2 == null) {
            return false;
        }
        this.lastSmithItemId = n4;
        try {
            WidgetPackets.widgetFirstOption(widget2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return true;
    }

    private Widget safeWidget(int n, int n2) {
        try {
            return this.client.getWidget(n, n2);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private boolean hasSmithAction(Widget widget) {
        if (widget == null) {
            return false;
        }
        try {
            String[] actions = widget.getActions();
            if (actions != null) {
                for (String action : actions) {
                    if (action == null || !action.toLowerCase().contains("smith")) {
                        continue;
                    }
                    return true;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (Widget child : this.kidsOf(widget)) {
            if (!this.hasSmithAction(child)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private int firstItemId(Widget widget) {
        if (widget == null) {
            return -1;
        }
        try {
            int n = widget.getItemId();
            if (n > 0) {
                return n;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (Widget widget2 : this.kidsOf(widget)) {
            int n = this.firstItemId(widget2);
            if (n <= 0) continue;
            return n;
        }
        return -1;
    }

    private int firstBarCount(Widget widget) {
        if (widget == null) {
            return -1;
        }
        int n = this.parseBars(this.textOf(widget));
        if (n > 0) {
            return n;
        }
        for (Widget widget2 : this.kidsOf(widget)) {
            int n2 = this.firstBarCount(widget2);
            if (n2 <= 0) continue;
            return n2;
        }
        return -1;
    }

    private List<Widget> kidsOf(Widget widget) {
        Widget[] widgetArray;
        ArrayList<Widget> arrayList = new ArrayList<Widget>();
        try {
            widgetArray = widget.getChildren();
            if (widgetArray != null) {
                for (Widget widget2 : widgetArray) {
                    if (widget2 == null) continue;
                    arrayList.add(widget2);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            widgetArray = widget.getDynamicChildren();
            if (widgetArray != null) {
                for (Widget widget2 : widgetArray) {
                    if (widget2 == null) continue;
                    arrayList.add(widget2);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            widgetArray = widget.getNestedChildren();
            if (widgetArray != null) {
                for (Widget widget2 : widgetArray) {
                    if (widget2 == null) continue;
                    arrayList.add(widget2);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return arrayList;
    }

    private String textOf(Widget widget) {
        try {
            return widget.getText() == null ? "" : widget.getText().replaceAll("<[^>]*>", "");
        }
        catch (Throwable throwable) {
            return "";
        }
    }

    private int parseBars(String string) {
        if (string == null || string.isEmpty()) {
            return -1;
        }
        Matcher matcher = BAR_TEXT.matcher(string.toLowerCase());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    private int smithRequiredLevel(Widget widget, SmithMetal smithMetal) {
        if (smithMetal == null) {
            return -1;
        }
        int n = this.itemTypeOffset(this.itemName(this.firstItemId(widget)));
        if (n < 0) {
            n = this.itemTypeOffset(this.collectText(widget));
        }
        if (n < 0) {
            return -1;
        }
        return Math.min(99, smithMetal.smithLevel + n);
    }

    private int itemTypeOffset(String string) {
        if (string == null || string.isEmpty()) {
            return -1;
        }
        String string2 = string.toLowerCase();
        for (String[] stringArray : SMITH_ITEM_OFFSETS) {
            if (!string2.contains(stringArray[0])) continue;
            return Integer.parseInt(stringArray[1]);
        }
        return -1;
    }

    private String itemName(int n) {
        if (n <= 0) {
            return "";
        }
        try {
            ItemComposition itemComposition = this.client.getItemComposition(n);
            return itemComposition == null || itemComposition.getName() == null ? "" : itemComposition.getName();
        }
        catch (Throwable throwable) {
            return "";
        }
    }

    private String collectText(Widget widget) {
        StringBuilder stringBuilder = new StringBuilder();
        this.collectText(widget, stringBuilder);
        return stringBuilder.toString();
    }

    private void collectText(Widget widget, StringBuilder stringBuilder) {
        if (widget == null) {
            return;
        }
        String string = this.textOf(widget);
        if (string != null && !string.isEmpty()) {
            stringBuilder.append(string).append(' ');
        }
        for (Widget widget2 : this.kidsOf(widget)) {
            this.collectText(widget2, stringBuilder);
        }
    }

    private int loopWalkToBank(Player player) {
        if (this.arrivedAt(SmithingConstants.BANK_TILE, 4, player) || Bank.isOpen()) {
            return this.enter(SmState.DECIDE, 400);
        }
        this.status = "Walking to bank";
        this.stepToward(SmithingConstants.BANK_TILE, player);
        return 1200;
    }

    private PlannedAction decideAction(int n) {
        SmithingMode smithingMode = this.settings.mode;
        PlannedAction plannedAction = this.bestSmeltAction(n, smithingMode);
        PlannedAction plannedAction2 = this.bestSmithAction(n, smithingMode, 20);
        if (plannedAction2 != null && plannedAction != null && plannedAction.outputId == plannedAction2.metal.barId) {
            return plannedAction;
        }
        if (plannedAction2 == null && plannedAction == null && smithingMode != SmithingMode.SMELT_ONLY) {
            plannedAction2 = this.bestSmithAction(n, smithingMode, 1);
        }
        return this.better(plannedAction2, plannedAction);
    }

    private PlannedAction bestSmeltAction(int n, SmithingMode smithingMode) {
        if (smithingMode == SmithingMode.SMITH_ONLY) {
            return null;
        }
        PlannedAction plannedAction = null;
        for (SmeltRecipe smeltRecipe : SmeltRecipe.values()) {
            if (n < smeltRecipe.level) continue;
            int n2 = 27 / smeltRecipe.oresPerBar();
            for (int i = 0; i < smeltRecipe.ingredientIds.length; ++i) {
                int n3 = this.bankQuantity(smeltRecipe.ingredientIds[i], smeltRecipe.ingredientNames[i]);
                n2 = Math.min(n2, n3 / smeltRecipe.perBar[i]);
            }
            if (n2 < 1) continue;
            plannedAction = this.better(plannedAction, PlannedAction.smelt(smeltRecipe, n2));
        }
        return plannedAction;
    }

    private PlannedAction bestSmithAction(int n, SmithingMode smithingMode, int n2) {
        if (smithingMode == SmithingMode.SMELT_ONLY) {
            return null;
        }
        int n3 = 26;
        PlannedAction plannedAction = null;
        for (SmithMetal smithMetal : SmithMetal.values()) {
            int n4;
            if (n < smithMetal.smithLevel || this.smithMetalBlacklist.contains(smithMetal.barId) || (n4 = this.bankQuantity(smithMetal.barId, smithMetal.barName)) < n2) continue;
            int n5 = Math.min(n3, n4);
            plannedAction = this.better(plannedAction, PlannedAction.smith(smithMetal, n5));
        }
        return plannedAction;
    }

    private PlannedAction better(PlannedAction plannedAction, PlannedAction plannedAction2) {
        if (plannedAction == null) {
            return plannedAction2;
        }
        if (plannedAction2 == null) {
            return plannedAction;
        }
        if (plannedAction2.keyLevel != plannedAction.keyLevel) {
            return plannedAction2.keyLevel > plannedAction.keyLevel ? plannedAction2 : plannedAction;
        }
        if (plannedAction.kind != plannedAction2.kind) {
            return plannedAction2.kind == PlannedAction.Kind.SMITH ? plannedAction2 : plannedAction;
        }
        return plannedAction2.outputTarget > plannedAction.outputTarget ? plannedAction2 : plannedAction;
    }

    private int bankQuantity(int n, String string) {
        Item item;
        try {
            item = Bank.getFirst(n);
            if (item != null) {
                return item.getQuantity();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        item = this.bankExact(string);
        return item == null ? 0 : item.getQuantity();
    }

    private Item bankExact(String string) {
        if (string == null) {
            return null;
        }
        try {
            return Bank.getFirst(item -> item != null && item.getName() != null && item.getName().equalsIgnoreCase(string));
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private int invCount(int n, String string) {
        int n2 = 0;
        if (n > 0) {
            try {
                n2 = Inventory.getCount(n);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (n2 > 0) {
            return n2;
        }
        if (string == null) {
            return 0;
        }
        try {
            return Inventory.getAll(item -> item != null && item.getName() != null && item.getName().equalsIgnoreCase(string)).size();
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private void withdrawMaterial(int n, String string, int n2) {
        if (n2 <= 0) {
            return;
        }
        int n3 = this.resolveBankId(n, string);
        if (n3 <= 0) {
            return;
        }
        SafeBank.withdrawExact(n3, n2);
    }

    private int resolveBankId(int n, String string) {
        try {
            if (n > 0 && Bank.getFirst(n) != null) {
                return n;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        Item item = this.bankExact(string);
        return item == null ? -1 : item.getId();
    }

    private TileObject findBankBooth() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 10083);
        return tileObject2 != null ? tileObject2 : TileObjects.getNearest("Bank booth");
    }

    private TileObject findObject(int n, String string) {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == n);
        return tileObject2 != null ? tileObject2 : TileObjects.getNearest(string);
    }

    private boolean arrivedAt(WorldPoint worldPoint, int n, Player player) {
        WorldPoint worldPoint2 = player.getWorldLocation();
        return worldPoint2 != null && worldPoint2.getPlane() == worldPoint.getPlane() && worldPoint2.distanceTo(worldPoint) <= n;
    }

    private void stepToward(WorldPoint worldPoint, Player player) {
        long l = System.currentTimeMillis();
        if (this.walkTarget == null || !this.walkTarget.equals((Object)worldPoint)) {
            this.walkTarget = worldPoint;
            this.walkStartMs = l;
        }
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint);
    }

    private int enter(SmState smState, int n) {
        this.state = smState;
        this.stateEntryTick = this.client.getTickCount();
        this.lastProgressAt = System.currentTimeMillis();
        this.walkTarget = null;
        return n;
    }

    private int ticksInState() {
        return this.client.getTickCount() - this.stateEntryTick;
    }

    private void markProgress() {
        this.lastProgressAt = System.currentTimeMillis();
    }

    private int jitter(int n) {
        int n2 = this.settings.antiBanMs;
        return n2 > 0 ? n + this.random.nextInt(n2 + 1) : n;
    }

    public void setStatus(String string) {
        this.status = string;
    }

    public String getStatus() {
        return this.status;
    }

    public String getStateName() {
        return this.state.name();
    }

    public String getTaskLabel() {
        return this.currentPlan != null ? this.currentPlan.label : "-";
    }

    public int getMadeCount() {
        return this.madeCount;
    }

    public int getBarsSmelted() {
        return this.barsSmelted;
    }

    public int getItemsSmithed() {
        return this.itemsSmithed;
    }

    public int getTripCount() {
        return this.tripCount;
    }

    public int getSmithLevel() {
        return this.client.getBoostedSkillLevel(Skill.SMITHING);
    }

    public int getXpGained() {
        if (this.startXp < 0) {
            return 0;
        }
        return Math.max(0, Skills.getExperience(Skill.SMITHING) - this.startXp);
    }

    private static enum SmState {
        DECIDE,
        OPEN_BANK,
        DEPOSIT,
        WITHDRAW,
        WALK_TO_STATION,
        PROCESS_SMELT,
        PROCESS_SMITH,
        WALK_TO_BANK,
        STOPPED;

    }

    private static final class PlannedAction {
        final Kind kind;
        final SmeltRecipe recipe;
        final SmithMetal metal;
        final int keyLevel;
        final int outputId;
        final String outputName;
        final int outputTarget;
        final int[] wIds;
        final String[] wNames;
        final int[] wQtys;
        final String label;

        private PlannedAction(Kind kind, SmeltRecipe smeltRecipe, SmithMetal smithMetal, int n, int n2, String string, int n3, int[] nArray, String[] stringArray, int[] nArray2, String string2) {
            this.kind = kind;
            this.recipe = smeltRecipe;
            this.metal = smithMetal;
            this.keyLevel = n;
            this.outputId = n2;
            this.outputName = string;
            this.outputTarget = n3;
            this.wIds = nArray;
            this.wNames = stringArray;
            this.wQtys = nArray2;
            this.label = string2;
        }

        static PlannedAction smelt(SmeltRecipe smeltRecipe, int n) {
            int[] nArray = new int[smeltRecipe.perBar.length];
            for (int i = 0; i < nArray.length; ++i) {
                nArray[i] = n * smeltRecipe.perBar[i];
            }
            return new PlannedAction(Kind.SMELT, smeltRecipe, null, smeltRecipe.level, smeltRecipe.barId, smeltRecipe.barName, n, (int[])smeltRecipe.ingredientIds.clone(), (String[])smeltRecipe.ingredientNames.clone(), nArray, "Smelt " + n + " " + smeltRecipe.barName);
        }

        static PlannedAction smith(SmithMetal smithMetal, int n) {
            return new PlannedAction(Kind.SMITH, null, smithMetal, smithMetal.smithLevel, -1, smithMetal.metalName + " items", n, new int[]{smithMetal.barId}, new String[]{smithMetal.barName}, new int[]{n}, "Smith " + n + " " + smithMetal.barName);
        }

        static enum Kind {
            SMELT,
            SMITH;

        }
    }

    public static final class Settings {
        public final SmithingMode mode;
        public final boolean stopWhenOutOfMaterials;
        public final int antiBanMs;

        public Settings(SmithingMode smithingMode, boolean bl, int n) {
            this.mode = smithingMode;
            this.stopWhenOutOfMaterials = bl;
            this.antiBanMs = n;
        }
    }
}

