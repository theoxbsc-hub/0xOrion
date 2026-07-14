package net.runelite.client.plugins.ox.cooking;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.ox.core.SafeBank;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Production;
import net.unethicalite.api.widgets.Widgets;

/**
 * Pier cooking loop: bank -> withdraw-all raw -> use on range -> All + Space (0xSmith style) -> bank.
 */
final class CookingEngine {
    /** Multi-skill / "What would you like to cook?" group (same as smithing smelt). */
    private static final int PROD_GROUP = 270;
    private static final int PROD_ALL_CHILD = 12;

    enum State {
        BANKING,
        WITHDRAWING,
        TO_RANGE,
        COOK_ALL,   // click quantity All (270:12)
        COOK_SPACE, // Space to start cooking
        COOKING,
        TO_BANK
    }

    private State state = State.BANKING;
    private String status = "Starting";
    private String activeLabel = "-";
    private int activeRawId = -1;
    private int trips;
    private int cookedEstimate;
    private long lastActionMs;
    private long cookStartedMs;
    private int lastRawCount = -1;
    private long lastRawChangeMs;

    void reset() {
        this.state = State.BANKING;
        this.status = "Starting";
        this.activeLabel = "-";
        this.activeRawId = -1;
        this.trips = 0;
        this.cookedEstimate = 0;
        this.lastActionMs = 0L;
        this.cookStartedMs = 0L;
        this.lastRawCount = -1;
        this.lastRawChangeMs = 0L;
    }

    State getState() {
        return this.state;
    }

    String getStatus() {
        return this.status;
    }

    String getActiveLabel() {
        return this.activeLabel;
    }

    int getTrips() {
        return this.trips;
    }

    int getCookedEstimate() {
        return this.cookedEstimate;
    }

    int tick(Client client, Player player, CookingConfig config) {
        long now = System.currentTimeMillis();
        List<CookFish> enabled = enabledFish(config);
        if (enabled.isEmpty()) {
            this.status = "No fish enabled - tick Fish types in config";
            return 1000;
        }

        switch (this.state) {
            case BANKING:
                return loopBanking(config, enabled, now);
            case WITHDRAWING:
                return loopWithdrawing(config, enabled, now);
            case TO_RANGE:
                return loopToRange(config, now);
            case COOK_ALL:
                return loopCookAll(now);
            case COOK_SPACE:
                return loopCookSpace(now);
            case COOKING:
                return loopCooking(now);
            case TO_BANK:
                return loopToBank(config, now);
            default:
                return 600;
        }
    }

    private int loopBanking(CookingConfig config, List<CookFish> enabled, long now) {
        if (SafeBank.clearStrayAmountPrompt()) {
            this.status = "Clearing bank amount prompt";
            return 400;
        }

        if (Bank.isOpen()) {
            this.status = "Bank open - depositing";
            try {
                if (!Inventory.isEmpty()) {
                    Bank.depositInventory();
                    this.lastActionMs = now;
                    return 700;
                }
            } catch (Throwable ignored) {
                // best-effort
            }
            this.state = State.WITHDRAWING;
            this.activeRawId = -1;
            this.lastActionMs = now;
            return 400;
        }

        TileObject booth = findBankBooth(config);
        if (booth == null) {
            this.status = "Bank booth " + config.bankBoothId() + " / 'Bank booth' not found";
            return 800;
        }

        Player local = net.unethicalite.api.entities.Players.getLocal();
        WorldPoint here = local != null ? local.getWorldLocation() : null;
        WorldPoint there = booth.getWorldLocation();
        if (here != null && there != null && here.distanceTo(there) > 4) {
            this.status = "Walking to bank booth (" + here.distanceTo(there) + "t)";
            try {
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(there);
            } catch (Throwable ignored) {
                // ignore
            }
            this.lastActionMs = now;
            return 1200;
        }

        if (now - this.lastActionMs < 1800L) {
            this.status = "Opening bank...";
            return 400;
        }

        // Pier booths open with "Use" (same as 0xSmith) — never guess "Bank" on the wrong object
        this.status = "Opening bank (Use) id=" + booth.getId();
        try {
            booth.interact("Use");
        } catch (Throwable t) {
            String action = resolveBankAction(booth, config.bankAction());
            safeInteract(booth, action);
        }
        this.lastActionMs = now;
        return 1000;
    }

    private int loopWithdrawing(CookingConfig config, List<CookFish> enabled, long now) {
        if (SafeBank.clearStrayAmountPrompt()) {
            this.status = "Dismissing amount prompt - will withdraw-all next";
            return 500;
        }

        if (!Bank.isOpen()) {
            this.state = State.BANKING;
            this.status = "Bank closed - reopening";
            return 400;
        }

        // Already holding raw from a successful withdraw-all
        int holding = countRaw(enabled);
        if (holding > 0) {
            this.activeRawId = firstInvRawId(enabled);
            this.activeLabel = labelForRaw(enabled, this.activeRawId);
            try {
                Bank.close();
            } catch (Throwable ignored) {
                // ignore
            }
            this.state = State.TO_RANGE;
            this.status = "Have " + holding + " raw - heading to range";
            this.lastActionMs = now;
            return 500;
        }

        // Don't spam withdraw while previous click is settling
        if (now - this.lastActionMs < 1400L && this.activeRawId > 0) {
            this.status = "Waiting for withdraw-all...";
            return 300;
        }

        int[] pick = pickFromBank(enabled);
        if (pick == null) {
            this.status = "No enabled raw fish in bank";
            return 1500;
        }
        this.activeRawId = pick[0];
        this.activeLabel = labelForRaw(enabled, this.activeRawId);

        this.status = "Withdraw-all " + this.activeLabel + " (" + this.activeRawId + ")";
        try {
            // Left-click mode = All (belt-and-braces with withdrawAll menu action)
            Bank.setQuantityMode(Bank.QuantityMode.ALL);
        } catch (Throwable ignored) {
            // optional
        }
        // Never Bank.withdraw(n) — amounts like 28 become Withdraw-X ("How many?")
        SafeBank.withdrawAll(this.activeRawId);
        this.lastActionMs = now;
        return 900;
    }

    private int loopToRange(CookingConfig config, long now) {
        if (Bank.isOpen()) {
            try {
                Bank.close();
            } catch (Throwable ignored) {
                // ignore
            }
            return 400;
        }

        // Already on the cook menu — skip re-use
        if (isCookInterfaceOpen()) {
            this.state = State.COOK_ALL;
            this.status = "Cook menu open - selecting All";
            return 200;
        }

        List<CookFish> enabled = enabledFish(config);
        if (countRaw(enabled) <= 0) {
            this.state = State.BANKING;
            this.status = "No raw fish in invent - banking";
            return 400;
        }

        if (now - this.lastActionMs < 900L) {
            this.status = "Approaching range...";
            return 300;
        }

        Item raw = firstInvRaw(enabled);
        TileObject range = findObject(config.rangeObjectId(), "Cook", "Range", "Fire");
        if (raw == null) {
            this.state = State.BANKING;
            return 400;
        }
        if (range == null) {
            this.status = "Cooking range " + config.rangeObjectId() + " not found";
            return 800;
        }

        Player local = net.unethicalite.api.entities.Players.getLocal();
        WorldPoint here = local != null ? local.getWorldLocation() : null;
        WorldPoint there = range.getWorldLocation();
        if (here != null && there != null && here.distanceTo(there) > 4) {
            this.status = "Walking to range";
            try {
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(there);
            } catch (Throwable ignored) {
                // ignore
            }
            this.lastActionMs = now;
            return 1200;
        }

        this.status = "Using " + this.activeLabel + " on range";
        useRawOnRange(raw, range);
        this.lastActionMs = now;
        this.state = State.COOK_ALL;
        return 1200;
    }

    private void useRawOnRange(Item raw, TileObject range) {
        try {
            raw.useOn(range);
            return;
        } catch (Throwable ignored) {
            // packet fallback
        }
        try {
            MousePackets.queueClickPacket();
            ObjectPackets.useItemOnTileObject(raw, range);
            return;
        } catch (Throwable ignored) {
            // last resort
        }
        try {
            MousePackets.queueClickPacket();
            range.interact("Cook");
        } catch (Throwable ignored) {
            // ignore
        }
    }

    /**
     * Tick 1 of smithing-style confirm: click quantity All (270:12) when clickable.
     */
    private int loopCookAll(long now) {
        if (!isCookInterfaceOpen()) {
            if (now - this.lastActionMs < 2500L) {
                this.status = "Waiting for cook interface...";
                return 300;
            }
            this.status = "Cook interface missing - retry use-on-range";
            this.state = State.TO_RANGE;
            this.lastActionMs = 0L;
            return 400;
        }

        if (allQuantityClickable()) {
            this.status = "Cook quantity: All";
            clickAllQuantity();
            this.lastActionMs = now;
            this.state = State.COOK_SPACE;
            return 500; // let All register before Space (smith does same-tick; cooking needs a beat)
        }

        // All already selected / not exposed — Space next
        this.status = "Cook All already set - Space next";
        this.state = State.COOK_SPACE;
        return 200;
    }

    /**
     * Tick 2: Space confirms first cookable (same as 0xSmith first smelt option).
     */
    private int loopCookSpace(long now) {
        if (!isCookInterfaceOpen()) {
            // Menu closed after All — treat as started, or retry
            if (now - this.lastActionMs < 1500L) {
                this.cookStartedMs = now;
                this.lastRawCount = countAllRaw();
                this.lastRawChangeMs = now;
                this.state = State.COOKING;
                this.status = "Cooking " + this.activeLabel;
                return 600;
            }
            this.state = State.TO_RANGE;
            this.lastActionMs = 0L;
            return 400;
        }

        this.status = "Cook confirm (Space) " + this.activeLabel;
        try {
            Keyboard.sendSpace();
        } catch (Throwable t) {
            try {
                Production.choosePreviousOption();
            } catch (Throwable ignored) {
                // ignore
            }
        }
        this.cookStartedMs = now;
        this.lastRawCount = countAllRaw();
        this.lastRawChangeMs = now;
        this.lastActionMs = now;
        this.state = State.COOKING;
        return 800;
    }

    private int loopCooking(long now) {
        // Prompt still up — re-run All → Space
        if (isCookInterfaceOpen()) {
            this.status = "Cook prompt still open - All then Space";
            this.state = State.COOK_ALL;
            return 200;
        }

        int rawLeft = countAllRaw();
        if (rawLeft != this.lastRawCount) {
            if (rawLeft < this.lastRawCount && this.lastRawCount >= 0) {
                this.cookedEstimate += (this.lastRawCount - rawLeft);
            }
            this.lastRawCount = rawLeft;
            this.lastRawChangeMs = now;
        }

        if (rawLeft <= 0) {
            this.trips++;
            this.state = State.TO_BANK;
            this.status = "Batch done - returning to bank";
            this.lastActionMs = now;
            return 500;
        }

        // Still animating = keep waiting even if invent hasn't ticked yet
        boolean animating = isLocalAnimating();
        if (animating) {
            this.lastRawChangeMs = now; // don't stall while cook anim plays
            this.status = "Cooking " + this.activeLabel + " (" + rawLeft + " left)";
            return 500;
        }

        // Invent stopped decreasing with fish left (classic: 1 raw left after All stopped)
        long idleMs = now - this.lastRawChangeMs;
        if (idleMs >= 3500L) {
            this.status = "Cook idle (" + rawLeft + " left) - reuse on range";
            this.state = State.TO_RANGE;
            this.lastActionMs = 0L;
            return 400;
        }

        this.status = "Cooking " + this.activeLabel + " (" + rawLeft + " left)";
        return 500;
    }

    private static boolean isLocalAnimating() {
        try {
            Player local = net.unethicalite.api.entities.Players.getLocal();
            return local != null && local.getAnimation() != -1;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Mirror 0xSmith isMakeInterfaceOpen / isSmeltInterfaceOpen — Production.isOpen alone
     * misses Orion's cook chatbox when MULTI_SKILL_MENU isn't flagged.
     *
     * Note: do NOT treat Dialog.canContinue as the cook menu — level-up/chat continues
     * would steal the loop away from finishing the last raw.
     */
    private static boolean isCookInterfaceOpen() {
        if (isProdWidgetOpen()) {
            return true;
        }
        try {
            return Production.isOpen();
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isProdWidgetOpen() {
        for (int i = 14; i <= 30; ++i) {
            Widget w = safeWidget(PROD_GROUP, i);
            if (w != null && !w.isHidden() && firstItemId(w) > 0) {
                return true;
            }
        }
        return false;
    }

    /** Same gate as SmithingEngine.allQuantityClickable. */
    private static boolean allQuantityClickable() {
        Widget widget = safeWidget(PROD_GROUP, PROD_ALL_CHILD);
        if (widget == null || widget.isHidden()) {
            return false;
        }
        if (usableAction(widget) != null) {
            return true;
        }
        for (Widget kid : kidsOf(widget)) {
            if (usableAction(kid) != null) {
                return true;
            }
        }
        return false;
    }

    private static void clickAllQuantity() {
        Widget all = safeWidget(PROD_GROUP, PROD_ALL_CHILD);
        if (all == null) {
            return;
        }
        try {
            MousePackets.queueClickPacket();
            WidgetPackets.widgetFirstOption(all);
        } catch (Throwable t) {
            try {
                all.interact(0);
            } catch (Throwable ignored) {
                // ignore
            }
        }
    }

    private static String usableAction(Widget widget) {
        if (widget == null) {
            return null;
        }
        try {
            String[] actions = widget.getActions();
            if (actions != null) {
                for (String action : actions) {
                    if (action == null) {
                        continue;
                    }
                    String clean = action.replaceAll("<[^>]*>", "").trim();
                    if (!clean.isEmpty() && !clean.equalsIgnoreCase("Cancel")) {
                        return clean;
                    }
                }
            }
        } catch (Throwable ignored) {
            // ignore
        }
        return null;
    }

    private static int firstItemId(Widget widget) {
        if (widget == null) {
            return -1;
        }
        try {
            int id = widget.getItemId();
            if (id > 0) {
                return id;
            }
        } catch (Throwable ignored) {
            // ignore
        }
        for (Widget kid : kidsOf(widget)) {
            int id = firstItemId(kid);
            if (id > 0) {
                return id;
            }
        }
        return -1;
    }

    private static List<Widget> kidsOf(Widget widget) {
        List<Widget> out = new ArrayList<>();
        if (widget == null) {
            return out;
        }
        try {
            Widget[] kids = widget.getChildren();
            if (kids != null) {
                for (Widget kid : kids) {
                    if (kid != null) {
                        out.add(kid);
                    }
                }
            }
        } catch (Throwable ignored) {
            // ignore
        }
        try {
            Widget[] dyn = widget.getDynamicChildren();
            if (dyn != null) {
                for (Widget kid : dyn) {
                    if (kid != null) {
                        out.add(kid);
                    }
                }
            }
        } catch (Throwable ignored) {
            // ignore
        }
        return out;
    }

    private static Widget safeWidget(int group, int child) {
        try {
            return Widgets.get(group, child);
        } catch (Throwable t) {
            return null;
        }
    }

    private int loopToBank(CookingConfig config, long now) {
        if (SafeBank.clearStrayAmountPrompt()) {
            return 400;
        }
        if (Bank.isOpen()) {
            this.state = State.BANKING;
            return 200;
        }
        if (now - this.lastActionMs < 1000L) {
            this.status = "Walking back to bank...";
            return 300;
        }
        TileObject booth = findBankBooth(config);
        if (booth == null) {
            this.status = "Bank booth not found";
            return 800;
        }

        Player local = net.unethicalite.api.entities.Players.getLocal();
        WorldPoint here = local != null ? local.getWorldLocation() : null;
        WorldPoint there = booth.getWorldLocation();
        if (here != null && there != null && here.distanceTo(there) > 4) {
            this.status = "Walking back to bank";
            try {
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(there);
            } catch (Throwable ignored) {
                // ignore
            }
            this.lastActionMs = now;
            return 1200;
        }

        this.status = "Opening bank to deposit (Use)";
        try {
            booth.interact("Use");
        } catch (Throwable t) {
            String action = resolveBankAction(booth, config.bankAction());
            safeInteract(booth, action);
        }
        this.lastActionMs = now;
        this.state = State.BANKING;
        return 1000;
    }

    private static List<CookFish> enabledFish(CookingConfig config) {
        List<CookFish> list = new ArrayList<>();
        // Highest first so enabled shark/angler is preferred over shrimp if both banked
        if (config.enableAngler()) {
            list.add(CookFish.ANGLER);
        }
        if (config.enableShark()) {
            list.add(CookFish.SHARK);
        }
        if (config.enableMonkfish()) {
            list.add(CookFish.MONKFISH);
        }
        if (config.enableLobster()) {
            list.add(CookFish.LOBSTER);
        }
        if (config.enableShrimp()) {
            list.add(CookFish.SHRIMP);
        }
        return list;
    }

    private static int[] pickFromBank(List<CookFish> enabled) {
        for (CookFish fish : enabled) {
            for (int id : fish.rawIds) {
                try {
                    Item inBank = Bank.getFirst(id);
                    if (inBank != null && inBank.getQuantity() > 0) {
                        return new int[] {id};
                    }
                } catch (Throwable ignored) {
                    // try next
                }
            }
        }
        return null;
    }

    private static int countRaw(List<CookFish> enabled) {
        int n = 0;
        for (CookFish fish : enabled) {
            for (int id : fish.rawIds) {
                try {
                    n += Inventory.getCount(id);
                } catch (Throwable ignored) {
                    // ignore
                }
            }
        }
        return n;
    }

    private int countAllRaw() {
        int n = 0;
        for (CookFish fish : CookFish.values()) {
            for (int id : fish.rawIds) {
                try {
                    n += Inventory.getCount(id);
                } catch (Throwable ignored) {
                    // ignore
                }
            }
        }
        return n;
    }

    private static Item firstInvRaw(List<CookFish> enabled) {
        for (CookFish fish : enabled) {
            for (int id : fish.rawIds) {
                try {
                    Item item = Inventory.getFirst(id);
                    if (item != null) {
                        return item;
                    }
                } catch (Throwable ignored) {
                    // ignore
                }
            }
        }
        return null;
    }

    private static int firstInvRawId(List<CookFish> enabled) {
        Item item = firstInvRaw(enabled);
        return item != null ? item.getId() : -1;
    }

    private static String labelForRaw(List<CookFish> enabled, int rawId) {
        for (CookFish fish : enabled) {
            if (fish.matchesRaw(rawId)) {
                return fish.label;
            }
        }
        for (CookFish fish : CookFish.values()) {
            if (fish.matchesRaw(rawId)) {
                return fish.label;
            }
        }
        return "Raw " + rawId;
    }

    /**
     * Prefer exact booth IDs / exact name. Do NOT fuzzy-match "Bank" — that can
     * latch onto the fishing deposit barrel and never open a real bank UI.
     */
    private static TileObject findBankBooth(CookingConfig config) {
        int configured = config.bankBoothId();
        // Pier smith booth is 10083; cook config default is 10517 — try both
        int[] ids = configured > 0
            ? new int[] {configured, 10083, 10517}
            : new int[] {10083, 10517};
        for (int id : ids) {
            try {
                TileObject byId = TileObjects.getNearest(obj -> obj != null && obj.getId() == id);
                if (byId != null) {
                    return byId;
                }
            } catch (Throwable ignored) {
                // try next
            }
        }
        try {
            return TileObjects.getNearest("Bank booth");
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Orion booths often use "Use" (same as 0xSmith). Prefer configured action if present.
     */
    private static String resolveBankAction(TileObject booth, String preferred) {
        String[] actions;
        try {
            actions = booth.getActions();
        } catch (Throwable t) {
            actions = null;
        }
        if (preferred != null && !preferred.trim().isEmpty() && hasAction(actions, preferred)) {
            return preferred.trim();
        }
        for (String candidate : new String[] {"Use", "Bank", "Open"}) {
            if (hasAction(actions, candidate)) {
                return candidate;
            }
        }
        return preferred != null && !preferred.trim().isEmpty() ? preferred.trim() : "Use";
    }

    private static boolean hasAction(String[] actions, String want) {
        if (actions == null || want == null) {
            return false;
        }
        for (String a : actions) {
            if (a != null && a.equalsIgnoreCase(want)) {
                return true;
            }
        }
        return false;
    }

    private static TileObject findObject(int id, String... nameHints) {
        if (id > 0) {
            try {
                TileObject byId = TileObjects.getNearest(id);
                if (byId != null) {
                    return byId;
                }
            } catch (Throwable ignored) {
                // fall through
            }
        }
        if (nameHints != null) {
            for (String hint : nameHints) {
                if (hint == null || hint.isEmpty()) {
                    continue;
                }
                try {
                    TileObject byName = TileObjects.getNearest(obj ->
                        obj != null
                            && obj.getName() != null
                            && obj.getName().toLowerCase(Locale.ROOT).contains(hint.toLowerCase(Locale.ROOT)));
                    if (byName != null) {
                        return byName;
                    }
                } catch (Throwable ignored) {
                    // try next
                }
            }
        }
        return null;
    }

    private static boolean safeInteract(TileObject obj, String action) {
        try {
            if (action != null && !action.isEmpty()) {
                String[] actions = obj.getActions();
                if (actions != null) {
                    for (String a : actions) {
                        if (a != null && a.equalsIgnoreCase(action)) {
                            obj.interact(action);
                            return true;
                        }
                    }
                    for (String a : actions) {
                        if (a != null && a.toLowerCase(Locale.ROOT).contains(action.toLowerCase(Locale.ROOT))) {
                            obj.interact(a);
                            return true;
                        }
                    }
                }
                obj.interact(action);
                return true;
            }
            obj.interact(0);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
