package net.runelite.client.plugins.ox.cooking;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;

/**
 * Pier cooking loop: bank booth -> withdraw 28 raw -> use on range -> cook click -> bank again.
 */
final class CookingEngine {
    private static final int BASE_CANVAS_W = 765;
    private static final int BASE_CANVAS_H = 503;
    private static final int WITHDRAW_AMOUNT = 28;

    enum State {
        BANKING,
        WITHDRAWING,
        TO_RANGE,
        COOK_CLICK,
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
            case COOK_CLICK:
                return loopCookClick(client, config, now);
            case COOKING:
                return loopCooking(now);
            case TO_BANK:
                return loopToBank(config, now);
            default:
                return 600;
        }
    }

    private int loopBanking(CookingConfig config, List<CookFish> enabled, long now) {
        if (Bank.isOpen()) {
            this.status = "Bank open - depositing";
            try {
                Bank.depositInventory();
            } catch (Throwable ignored) {
                // best-effort
            }
            this.state = State.WITHDRAWING;
            this.lastActionMs = now;
            return 700;
        }

        if (now - this.lastActionMs < 1200L) {
            this.status = "Opening bank...";
            return 300;
        }

        TileObject booth = findObject(config.bankBoothId(), "Bank");
        if (booth == null) {
            this.status = "Bank booth " + config.bankBoothId() + " not found";
            return 800;
        }

        this.status = "Clicking bank booth";
        if (!walkOrInteract(booth, config.bankAction(), 6)) {
            this.status = "Could not open bank";
        }
        this.lastActionMs = now;
        return 1000;
    }

    private int loopWithdrawing(CookingConfig config, List<CookFish> enabled, long now) {
        if (!Bank.isOpen()) {
            this.state = State.BANKING;
            this.status = "Bank closed - reopening";
            return 400;
        }

        // Already holding raw from a bad interrupt / successful withdraw
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

        // Don't spam withdraw while the previous click is still settling
        if (now - this.lastActionMs < 1200L && this.activeRawId > 0) {
            this.status = "Waiting for withdraw...";
            return 300;
        }

        int[] pick = pickFromBank(enabled);
        if (pick == null) {
            this.status = "No enabled raw fish in bank";
            return 1500;
        }
        this.activeRawId = pick[0];
        this.activeLabel = labelForRaw(enabled, this.activeRawId);

        this.status = "Withdrawing 28x " + this.activeLabel + " (" + this.activeRawId + ")";
        try {
            Bank.withdraw(this.activeRawId, WITHDRAW_AMOUNT, Bank.WithdrawMode.ITEM);
        } catch (Throwable t) {
            try {
                Bank.withdrawAll(this.activeRawId, Bank.WithdrawMode.ITEM);
            } catch (Throwable ignored) {
                // ignore
            }
        }
        this.lastActionMs = now;
        // Give the bank a tick to populate invent before closing
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

        this.status = "Using " + this.activeLabel + " on range";
        try {
            raw.useOn(range);
        } catch (Throwable t) {
            try {
                MousePackets.queueClickPacket();
                range.interact("Cook");
            } catch (Throwable ignored) {
                // ignore
            }
        }
        this.lastActionMs = now;
        this.state = State.COOK_CLICK;
        return 1200;
    }

    private int loopCookClick(Client client, CookingConfig config, long now) {
        int[] xy = scaleClick(client, config.cookClickX(), config.cookClickY());
        this.status = "Cook interface click @ " + xy[0] + "," + xy[1];
        Mouse.click(xy[0], xy[1], true);
        this.cookStartedMs = now;
        this.lastRawCount = countAllRaw();
        this.lastRawChangeMs = now;
        this.lastActionMs = now;
        this.state = State.COOKING;
        return 800;
    }

    private int loopCooking(long now) {
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

        // Still cooking / waiting
        if (now - this.cookStartedMs < 45_000L) {
            this.status = "Cooking " + this.activeLabel + " (" + rawLeft + " left)";
            return 600;
        }

        // Stalled — re-open range flow
        this.status = "Cook stall - retry use-on-range";
        this.state = State.TO_RANGE;
        this.lastActionMs = now;
        return 600;
    }

    private int loopToBank(CookingConfig config, long now) {
        if (Bank.isOpen()) {
            this.state = State.BANKING;
            return 200;
        }
        if (now - this.lastActionMs < 1000L) {
            this.status = "Walking back to bank...";
            return 300;
        }
        TileObject booth = findObject(config.bankBoothId(), "Bank");
        if (booth == null) {
            this.status = "Bank booth not found";
            return 800;
        }
        this.status = "Opening bank to deposit";
        walkOrInteract(booth, config.bankAction(), 6);
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

    private static boolean walkOrInteract(TileObject obj, String action, int reach) {
        if (obj == null) {
            return false;
        }
        try {
            Player local = net.unethicalite.api.entities.Players.getLocal();
            WorldPoint here = local != null ? local.getWorldLocation() : null;
            WorldPoint there = obj.getWorldLocation();
            if (here != null && there != null && here.distanceTo(there) > reach) {
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(there);
                return true;
            }
        } catch (Throwable ignored) {
            // interact anyway
        }
        return safeInteract(obj, action);
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

    private static int[] scaleClick(Client client, int baseX, int baseY) {
        int canvasW = BASE_CANVAS_W;
        int canvasH = BASE_CANVAS_H;
        if (client != null) {
            try {
                int w = client.getCanvasWidth();
                int h = client.getCanvasHeight();
                if (w > 0 && h > 0) {
                    canvasW = w;
                    canvasH = h;
                }
            } catch (Throwable ignored) {
                // defaults
            }
        }
        int x = (int) Math.round(baseX * (canvasW / (double) BASE_CANVAS_W));
        int y = (int) Math.round(baseY * (canvasH / (double) BASE_CANVAS_H));
        return new int[] {x, y};
    }
}
