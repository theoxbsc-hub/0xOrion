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
package net.runelite.client.plugins.svc.herblore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.client.plugins.svc.herblore.HerbloreConstants;
import net.runelite.client.plugins.svc.herblore.HerbloreTask;
import net.runelite.client.plugins.svc.herblore.Ingredient;
import net.runelite.client.plugins.svc.herblore.ItemMatcher;
import net.runelite.client.plugins.svc.herblore.Recipe;
import net.runelite.client.plugins.svc.herblore.RecipeKind;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;

public final class HerbloreEngine {
    private final Client client;
    private final Random random = new Random();
    private Settings settings;
    private State state = State.TO_BANK;
    private String status = "Starting";
    private int startXp = -1;
    private Recipe currentTask = null;
    private int makeableNow = 0;
    private int plannedQty = 0;
    private long combineSentAt = 0L;
    private long lastConsumeAt = 0L;
    private int combineRetries = 0;
    private long bankOpenAt = 0L;
    private long lastCleanAt = 0L;
    private int bankMisses = 0;
    private long progressAt = 0L;
    private int lastInputUnits = -1;
    private int lastHerbLevel = 0;
    private final Set<String> blacklist = new HashSet<String>();
    private int potionsMade = 0;
    private int trips = 0;

    public HerbloreEngine(Client client) {
        this.client = client;
    }

    public void reset() {
        this.settings = null;
        this.state = State.TO_BANK;
        this.status = "Starting";
        this.startXp = -1;
        this.currentTask = null;
        this.makeableNow = 0;
        this.plannedQty = 0;
        this.combineSentAt = 0L;
        this.lastConsumeAt = 0L;
        this.combineRetries = 0;
        this.bankOpenAt = 0L;
        this.lastCleanAt = 0L;
        this.bankMisses = 0;
        this.progressAt = 0L;
        this.lastInputUnits = -1;
        this.lastHerbLevel = 0;
        this.blacklist.clear();
        this.potionsMade = 0;
        this.trips = 0;
    }

    public int tick(Player player, Settings settings) {
        this.settings = settings;
        if (this.startXp < 0) {
            this.startXp = Skills.getExperience(Skill.HERBLORE);
        }
        switch (this.state.ordinal()) {
            case 0: {
                return this.toBank(player);
            }
            case 1: {
                return this.waitBankOpen();
            }
            case 2: {
                return this.decide();
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
                return this.make(player);
            }
            case 7: {
                return this.clean();
            }
            case 8: {
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
            MovementPackets.sendMovement(tileObject.getWorldLocation());
            return 1200;
        }
        this.status = "Opening bank";
        tileObject.interact("Use");
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

    private int decide() {
        if (!Bank.isOpen()) {
            this.state = State.TO_BANK;
            return 500;
        }
        Map<String, Integer> map = this.snapshotBank();
        int n = this.client.getBoostedSkillLevel(Skill.HERBLORE);
        if (n > this.lastHerbLevel) {
            this.blacklist.clear();
        }
        this.lastHerbLevel = n;
        this.currentTask = this.selectTask(n, map);
        if (this.currentTask == null) {
            this.makeableNow = 0;
            this.plannedQty = 0;
            Bank.close();
            if (this.settings.stopWhenIdle) {
                this.status = "Out of materials \u2014 stopped";
                return -1;
            }
            this.status = "Out of materials \u2014 re-checking soon";
            this.state = State.IDLE;
            return 15000;
        }
        this.makeableNow = this.makeableCount(this.currentTask, map);
        this.plannedQty = Math.max(1, Math.min(this.currentTask.potionsPerInv(), this.makeableNow));
        this.status = HerbloreEngine.describe(this.currentTask);
        this.state = State.DEPOSIT;
        return 350;
    }

    private int deposit() {
        if (!Bank.isOpen()) {
            this.state = State.TO_BANK;
            return 500;
        }
        if (Inventory.getFreeSlots() < 28) {
            this.status = "Depositing";
            Bank.depositInventory();
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
        int n = this.plannedQty;
        for (Ingredient ingredient : this.currentTask.inputs) {
            Item item = Bank.getFirst(HerbloreEngine.matchPredicate(ingredient.matcher));
            if (item == null) {
                this.status = "Missing " + ingredient.displayName + " \u2014 re-deciding";
                this.state = State.DECIDE;
                return 500;
            }
            int n2 = ingredient.perResult * n;
            int n3 = Math.max(1, item.getQuantity());
            Bank.withdraw(item.getId(), Math.min(n2, n3), Bank.WithdrawMode.ITEM);
        }
        this.status = "Withdrew for " + this.currentTask.result;
        this.state = State.CLOSE_BANK;
        return 700;
    }

    private int closeBank() {
        if (Bank.isOpen()) {
            Bank.close();
        }
        this.combineSentAt = 0L;
        this.lastConsumeAt = 0L;
        this.combineRetries = 0;
        this.progressAt = System.currentTimeMillis();
        this.lastInputUnits = -1;
        this.state = this.currentTask.kind == RecipeKind.CLEAN ? State.CLEAN : State.MAKE;
        return 450;
    }

    private int make(Player player) {
        long l = System.currentTimeMillis();
        boolean bl = player.getAnimation() != -1;
        int n = this.remainingInputUnits();
        if (this.lastInputUnits < 0) {
            this.lastInputUnits = n;
        } else if (n < this.lastInputUnits) {
            this.lastInputUnits = n;
            this.lastConsumeAt = l;
            this.combineRetries = 0;
            this.combineSentAt = 0L;
        }
        if (bl) {
            this.lastConsumeAt = l;
            this.combineRetries = 0;
        }
        if (!this.allInputsPresent()) {
            this.potionsMade += HerbloreEngine.countResults(this.currentTask.result);
            ++this.trips;
            this.status = "Batch done \u2014 banking";
            this.state = State.TO_BANK;
            return 400;
        }
        if (l - this.lastConsumeAt < 1400L) {
            this.status = "Making " + this.currentTask.result + "...";
            return this.jitter(450);
        }
        if (this.combineSentAt > 0L && l - this.combineSentAt < 1800L) {
            this.status = "Confirming make (space)";
            Keyboard.sendSpace();
            return 250;
        }
        if (this.combineRetries >= 4 && l - this.lastConsumeAt > 4000L) {
            this.blacklist.add(HerbloreEngine.blacklistKey(this.currentTask));
            this.status = "Can't make " + this.currentTask.result + " \u2014 skipping";
            this.state = State.TO_BANK;
            return 500;
        }
        Item item = HerbloreEngine.invFirst(this.currentTask.primary().matcher);
        Item item2 = HerbloreEngine.invFirst(this.currentTask.combineTarget().matcher);
        if (item == null || item2 == null) {
            this.potionsMade += HerbloreEngine.countResults(this.currentTask.result);
            ++this.trips;
            this.state = State.TO_BANK;
            return 400;
        }
        boolean bl2 = this.combineRetries % 2 == 0;
        this.status = this.combineRetries == 0 ? "Combining " + this.currentTask.result : "Re-combining " + this.currentTask.result + " (try " + (this.combineRetries + 1) + ")";
        this.combineItems(item, item2, bl2);
        this.combineSentAt = l;
        this.progressAt = l;
        ++this.combineRetries;
        return 300;
    }

    private boolean allInputsPresent() {
        for (Ingredient ingredient : this.currentTask.inputs) {
            if (HerbloreEngine.invFirst(ingredient.matcher) != null) continue;
            return false;
        }
        return true;
    }

    private void combineItems(Item item, Item item2, boolean bl) {
        if (bl) {
            item.useOn(item2);
        } else {
            MousePackets.queueClickPacket();
            ItemPackets.useItemOnItem(item, item2);
        }
    }

    private int clean() {
        long l = System.currentTimeMillis();
        int n = Inventory.getAll(HerbloreEngine.matchPredicate(this.currentTask.primary().matcher)).size();
        if (n == 0) {
            this.potionsMade += this.plannedQty;
            ++this.trips;
            this.status = "Cleaned \u2014 banking";
            this.state = State.TO_BANK;
            return 400;
        }
        if (this.lastInputUnits < 0 || n < this.lastInputUnits) {
            this.lastInputUnits = n;
            this.progressAt = l;
        } else if (l - this.progressAt > 4000L) {
            this.blacklist.add(HerbloreEngine.blacklistKey(this.currentTask));
            this.status = "Can't clean " + this.currentTask.result + " (need lvl " + this.currentTask.level + "+) \u2014 skipping";
            this.state = State.TO_BANK;
            return 500;
        }
        if (l - this.lastCleanAt < 280L) {
            return 120;
        }
        Item item = Inventory.getFirst(HerbloreEngine.matchPredicate(this.currentTask.primary().matcher));
        if (item != null) {
            this.status = "Cleaning " + this.currentTask.result + " (" + n + " left)";
            MousePackets.queueClickPacket();
            ItemPackets.itemFirstOption(item);
        }
        this.lastCleanAt = l;
        return this.jitter(280);
    }

    private int idle() {
        this.status = "Idle \u2014 re-checking bank";
        this.state = State.TO_BANK;
        return 600;
    }

    private Recipe selectTask(int n, Map<String, Integer> map) {
        HashSet<RecipeKind> hashSet = new HashSet<RecipeKind>();
        for (HerbloreTask herbloreTask : this.orderedPriorities()) {
            Recipe recipe;
            if (herbloreTask.kind == null || !hashSet.add(herbloreTask.kind) || (recipe = this.bestInTable(herbloreTask.table(), n, map)) == null) continue;
            return recipe;
        }
        return null;
    }

    private HerbloreTask[] orderedPriorities() {
        return new HerbloreTask[]{this.settings.priority1, this.settings.priority2, this.settings.priority3};
    }

    private Recipe bestInTable(List<Recipe> list, int n, Map<String, Integer> map) {
        Recipe recipe = null;
        for (Recipe recipe2 : list) {
            if (this.isBlacklisted(recipe2) || n < recipe2.level || this.makeableCount(recipe2, map) < 1 || recipe != null && recipe2.rankTier <= recipe.rankTier) continue;
            recipe = recipe2;
        }
        return recipe;
    }

    private boolean isBlacklisted(Recipe recipe) {
        return this.blacklist.contains(HerbloreEngine.blacklistKey(recipe));
    }

    private static String blacklistKey(Recipe recipe) {
        return String.valueOf((Object)recipe.kind) + ":" + recipe.result;
    }

    private int remainingInputUnits() {
        int n = 0;
        for (Ingredient ingredient : this.currentTask.inputs) {
            n += Inventory.getAll(HerbloreEngine.matchPredicate(ingredient.matcher)).size();
        }
        return n;
    }

    private int makeableCount(Recipe recipe, Map<String, Integer> map) {
        int n = Integer.MAX_VALUE;
        for (Ingredient ingredient : recipe.inputs) {
            int n2 = map.getOrDefault(ingredient.matcher.key(), 0);
            n = Math.min(n, n2 / ingredient.perResult);
        }
        return n == Integer.MAX_VALUE ? 0 : n;
    }

    private Map<String, Integer> snapshotBank() {
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        List<Item> list = Bank.getAll();
        if (list == null) {
            return hashMap;
        }
        for (ItemMatcher itemMatcher : HerbloreConstants.ALL_MATCHERS) {
            int n = 0;
            for (Item item : list) {
                if (item == null || item.getName() == null || !itemMatcher.matches(item.getName())) continue;
                n += Math.max(1, item.getQuantity());
            }
            if (n <= 0) continue;
            hashMap.put(itemMatcher.key(), n);
        }
        return hashMap;
    }

    private static Predicate<Item> matchPredicate(ItemMatcher itemMatcher) {
        return item -> item != null && item.getName() != null && itemMatcher.matches(item.getName());
    }

    private static Item invFirst(ItemMatcher itemMatcher) {
        return Inventory.getFirst(HerbloreEngine.matchPredicate(itemMatcher));
    }

    private static int countResults(String string) {
        ItemMatcher itemMatcher = ItemMatcher.named(string);
        return Inventory.getAll(HerbloreEngine.matchPredicate(itemMatcher)).size();
    }

    private static String describe(Recipe recipe) {
        switch (recipe.kind) {
            case FINISHED: {
                return "Making " + recipe.result + " (lvl " + recipe.level + ")";
            }
            case UNF: {
                return "Making " + recipe.result;
            }
            case CLEAN: {
                return "Cleaning " + recipe.result + " (lvl " + recipe.level + ")";
            }
        }
        return recipe.result;
    }

    private int jitter(int n) {
        int n2 = 180;
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

    public boolean isBanking() {
        return this.state == State.TO_BANK || this.state == State.WAIT_BANK_OPEN || this.state == State.DECIDE || this.state == State.DEPOSIT || this.state == State.WITHDRAW || this.state == State.CLOSE_BANK;
    }

    public boolean isCleaning() {
        return this.state == State.CLEAN || this.currentTask != null && this.currentTask.kind == RecipeKind.CLEAN;
    }

    public boolean isIdle() {
        return this.state == State.IDLE;
    }

    public String getTaskResult() {
        return this.currentTask == null ? "\u2014" : this.currentTask.result;
    }

    public int getTaskLevel() {
        return this.currentTask == null ? 0 : this.currentTask.level;
    }

    public RecipeKind getTaskKind() {
        return this.currentTask == null ? null : this.currentTask.kind;
    }

    public int getMakeableNow() {
        return this.makeableNow;
    }

    public int getPlannedQty() {
        return this.plannedQty;
    }

    public int getPotionsMade() {
        return this.potionsMade;
    }

    public int getTrips() {
        return this.trips;
    }

    public int getHerbLevel() {
        return this.client.getBoostedSkillLevel(Skill.HERBLORE);
    }

    public int getXpGained() {
        return this.startXp < 0 ? 0 : Math.max(0, Skills.getExperience(Skill.HERBLORE) - this.startXp);
    }

    private static enum State {
        TO_BANK,
        WAIT_BANK_OPEN,
        DECIDE,
        DEPOSIT,
        WITHDRAW,
        CLOSE_BANK,
        MAKE,
        CLEAN,
        IDLE;

    }

    public static final class Settings {
        public final HerbloreTask priority1;
        public final HerbloreTask priority2;
        public final HerbloreTask priority3;
        public final boolean stopWhenIdle;

        public Settings(HerbloreTask herbloreTask, HerbloreTask herbloreTask2, HerbloreTask herbloreTask3, boolean bl) {
            this.priority1 = herbloreTask;
            this.priority2 = herbloreTask2;
            this.priority3 = herbloreTask3;
            this.stopWhenIdle = bl;
        }
    }
}

