package net.runelite.client.plugins.svc.woodcutting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.DepositBox;
import net.unethicalite.api.items.Inventory;

public final class WoodcuttingEngine {
    private final Client client;
    private final HumanInput input;
    private State state = State.CHOPPING;
    private String status = "Starting";
    private int cycleCount = 0;
    private final Random selectRng = new Random();
    private long reactUntilMs = 0L;
    private long depositInteractedAtMs = 0L;
    private long depositSentAtMs = 0L;

    public WoodcuttingEngine(Client client) {
        this(client, null);
    }

    public WoodcuttingEngine(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public void reset() {
        this.state = State.CHOPPING;
        this.status = "Starting";
        this.reactUntilMs = 0L;
        this.depositInteractedAtMs = 0L;
        this.depositSentAtMs = 0L;
    }

    public String getStatus() {
        return this.status;
    }

    public String getState() {
        return this.state.name();
    }

    public int getCycleCount() {
        return this.cycleCount;
    }

    /** @deprecated use {@link #getCycleCount()} */
    public int getDropCount() {
        return this.cycleCount;
    }

    public int tick(Player player, Settings settings) {
        switch (this.state) {
            case CHOPPING:
                return this.chop(settings);
            case OPENING_DEPOSIT:
                return this.openDeposit(settings);
            case DEPOSITING:
                return this.deposit(settings);
            case DROPPING:
                return this.drop(settings);
            default:
                return 600;
        }
    }

    private int chop(Settings settings) {
        if (Inventory.getFreeSlots() == 0) {
            if (settings.mode == WoodcuttingMode.DROP) {
                this.status = "Inventory full — dropping logs";
                this.state = State.DROPPING;
            } else {
                this.status = "Inventory full — finding deposit box";
                this.state = State.OPENING_DEPOSIT;
                this.depositInteractedAtMs = 0L;
            }
            return 300;
        }
        if (this.client.getLocalPlayer().getAnimation() != -1) {
            this.status = "Chopping...";
            this.clearReaction();
            return 600;
        }
        WorldPoint worldPoint = this.client.getLocalPlayer().getWorldLocation();
        TileObject tree = this.pickTree(worldPoint, settings);
        if (tree != null) {
            int wait = this.humanWait();
            if (wait > 0) {
                this.status = "Clicking tree";
                return wait;
            }
            this.clearReaction();
            this.status = "Clicking tree";
            if (this.input != null) {
                this.input.interactObjectVia(tree, settings.chopAction);
            } else {
                tree.interact(settings.chopAction);
            }
            return 1800;
        }
        this.clearReaction();
        this.status = "Waiting for tree...";
        return 800;
    }

    private int openDeposit(Settings settings) {
        if (DepositBox.isOpen()) {
            this.status = "Deposit box open — depositing";
            this.state = State.DEPOSITING;
            this.depositSentAtMs = 0L;
            return 300;
        }
        long now = System.currentTimeMillis();
        if (this.depositInteractedAtMs > 0L && now - this.depositInteractedAtMs < 4000L) {
            this.status = "Waiting for deposit box...";
            return 400;
        }
        TileObject box = TileObjects.getNearest(
            tileObject -> tileObject != null && tileObject.getId() == settings.depositObjectId
        );
        if (box != null) {
            this.status = "Opening deposit box";
            box.interact(settings.depositAction);
            this.depositInteractedAtMs = now;
            return 1200;
        }
        this.status = "Deposit box not found";
        return 800;
    }

    private int deposit(Settings settings) {
        if (!DepositBox.isOpen()) {
            this.status = "Returning to trees";
            this.state = State.CHOPPING;
            this.depositSentAtMs = 0L;
            return 600;
        }
        List<Integer> logIds = settings.logItemIds;
        boolean stillHasLogs = logIds.stream()
            .anyMatch(id -> Inventory.getFirst(item -> item.getId() == id) != null);
        if (!stillHasLogs) {
            this.status = "Closing deposit box";
            DepositBox.close();
            ++this.cycleCount;
            this.state = State.CHOPPING;
            this.depositSentAtMs = 0L;
            return 800;
        }
        long now = System.currentTimeMillis();
        if (this.depositSentAtMs > 0L && now - this.depositSentAtMs < 1500L) {
            this.status = "Depositing...";
            return 300;
        }
        // Same deposit-box UI path as x64 fishing (interface 192)
        Widget depositAll = this.client.getWidget(192, 19);
        if (depositAll != null && !depositAll.isHidden()) {
            depositAll.interact(0);
        }
        Widget inventory = this.client.getWidget(192, 2);
        if (inventory != null) {
            Widget[] children = inventory.getDynamicChildren();
            if (children != null) {
                for (Widget child : children) {
                    if (child == null || child.isHidden() || !logIds.contains(child.getItemId())) {
                        continue;
                    }
                    this.status = "Depositing logs";
                    child.interact(0);
                    this.depositSentAtMs = now;
                    return 600;
                }
            }
        }
        this.status = "Log slot not found";
        return 800;
    }

    private TileObject pickTree(WorldPoint worldPoint, Settings settings) {
        if (this.input == null || worldPoint == null) {
            return TileObjects.getNearest(
                tileObject -> tileObject != null && tileObject.getId() == settings.treeObjectId
            );
        }
        List<TileObject> trees = TileObjects.getAll(
            tileObject -> tileObject != null
                && tileObject.getId() == settings.treeObjectId
                && tileObject.getWorldLocation() != null
        );
        if (trees == null || trees.isEmpty()) {
            return null;
        }
        trees.sort(Comparator.comparingInt(t -> t.getWorldLocation().distanceTo(worldPoint)));
        if (trees.size() == 1) {
            return trees.get(0);
        }
        int nearestDist = trees.get(0).getWorldLocation().distanceTo(worldPoint);
        int candidates = 1;
        for (; candidates < trees.size() && candidates < 3
            && trees.get(candidates).getWorldLocation().distanceTo(worldPoint) <= nearestDist + 5;
            ++candidates) {
        }
        if (candidates == 1) {
            return trees.get(0);
        }
        double preferNearest = 0.55 + 0.3 * seedUnit(this.input.profile().seed(), 19);
        double roll = this.selectRng.nextDouble();
        if (roll < preferNearest) {
            return trees.get(0);
        }
        if (candidates == 2 || roll < preferNearest + (1.0 - preferNearest) * 0.72) {
            return trees.get(1);
        }
        return trees.get(2);
    }

    private int drop(Settings settings) {
        List<Item> logs = Inventory.getAll(
            item -> item != null && settings.logItemIds.contains(item.getId())
        );
        if (logs.isEmpty()) {
            this.status = "Dropped — resuming";
            ++this.cycleCount;
            this.state = State.CHOPPING;
            return 300;
        }
        this.status = "Dropping logs (" + logs.size() + ")";
        if (this.input != null) {
            int wait = this.humanWait();
            if (wait > 0) {
                return wait;
            }
            this.clearReaction();
        }
        this.burstDrop(logs);
        return this.input == null ? 300 : 140 + this.selectRng.nextInt(180);
    }

    private void burstDrop(List<Item> items) {
        if (items.isEmpty()) {
            return;
        }
        ArrayList<Item> queue = new ArrayList<>(items);
        if (this.input != null && queue.size() > 2 && this.selectRng.nextDouble() < 0.08) {
            int swapAt = this.selectRng.nextInt(queue.size() - 1);
            Collections.swap(queue, swapAt, swapAt + 1);
        }
        int count = this.input == null
            ? queue.size()
            : Math.min(queue.size(), 5 + this.selectRng.nextInt(5));
        double tempo = this.input == null ? 1.0 : this.input.profile().tempo();
        for (int i = 0; i < count; ++i) {
            Item item = queue.get(i);
            if (this.input != null) {
                this.input.itemInteractVia(item, "Drop");
            } else {
                item.interact("Drop");
            }
            int sleepMs = this.input == null
                ? 80
                : (int) Math.round((45 + this.selectRng.nextInt(130)) * tempo);
            if (this.input != null && this.selectRng.nextDouble() < 0.07) {
                sleepMs += 250 + this.selectRng.nextInt(600);
            }
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private int humanWait() {
        if (this.input == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        if (this.reactUntilMs <= 0L) {
            this.reactUntilMs = now + this.input.profile().reactionDelay();
        }
        long remaining = this.reactUntilMs - now;
        return remaining > 0L ? (int) Math.max(60L, Math.min(240L, remaining)) : 0;
    }

    private void clearReaction() {
        this.reactUntilMs = 0L;
    }

    private static double seedUnit(long seed, int shift) {
        return (double) (seed >>> shift & 0xFFL) / 255.0;
    }

    public boolean dropMatching(int[] ids) {
        List<Item> items = Inventory.getAll(item -> {
            if (item == null) {
                return false;
            }
            for (int id : ids) {
                if (item.getId() == id) {
                    return true;
                }
            }
            return false;
        });
        if (items.isEmpty()) {
            return true;
        }
        this.status = "Clearing logs before tier switch (" + items.size() + ")";
        this.burstDrop(items);
        return false;
    }

    private enum State {
        CHOPPING,
        OPENING_DEPOSIT,
        DEPOSITING,
        DROPPING
    }

    public static final class Settings {
        public final int treeObjectId;
        public final List<Integer> logItemIds;
        public final String chopAction;
        public final WoodcuttingMode mode;
        public final int depositObjectId;
        public final String depositAction;

        public Settings(
            int treeObjectId,
            List<Integer> logItemIds,
            String chopAction,
            WoodcuttingMode mode,
            int depositObjectId,
            String depositAction
        ) {
            this.treeObjectId = treeObjectId;
            this.logItemIds = logItemIds;
            this.chopAction = chopAction;
            this.mode = mode;
            this.depositObjectId = depositObjectId;
            this.depositAction = depositAction;
        }
    }
}
