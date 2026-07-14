package net.runelite.client.plugins.ox.woodcutting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.ox.core.OxConsole;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.DepositBox;
import net.unethicalite.api.items.Inventory;

public final class WoodcuttingEngine {
    private final Client client;
    private final WcGliderTravel travel = new WcGliderTravel();
    private State state = State.CHOPPING;
    private String status = "Starting";
    private int cycleCount = 0;
    private int logsBanked = 0;
    private int logsDropped = 0;
    private int pendingClearCount = 0;
    private long depositInteractedAtMs = 0L;
    private long depositSentAtMs = 0L;
    private TreeTier activeTier;
    private TreeTier pendingTier;
    private boolean travelDone = false;

    public WoodcuttingEngine(Client client) {
        this.client = client;
    }

    public void reset() {
        this.state = State.CHOPPING;
        this.status = "Starting";
        this.depositInteractedAtMs = 0L;
        this.depositSentAtMs = 0L;
        this.activeTier = null;
        this.pendingTier = null;
        this.travelDone = false;
        this.pendingClearCount = 0;
        // keep banked/dropped across login blips in the same plugin enable
    }

    public void resetSessionStats() {
        this.cycleCount = 0;
        this.logsBanked = 0;
        this.logsDropped = 0;
        this.pendingClearCount = 0;
    }

    /** Call when freshly logged in so travel can run again if needed. */
    public void onSessionStart(Settings settings) {
        this.travelDone = false;
        maybeBeginTravel(settings);
    }

    private void maybeBeginTravel(Settings settings) {
        if (!settings.gliderTravel || this.travelDone) {
            return;
        }
        if (settings.skipTravelIfTreesNearby && treeNearby(settings)) {
            this.travelDone = true;
            this.state = State.CHOPPING;
            this.status = "Trees nearby — skipping travel";
            OxConsole.info("WCBot", this.status);
            return;
        }
        this.state = State.TRAVELING;
        this.travel.begin();
        this.status = "Travel: starting";
        OxConsole.info("WCBot", this.status);
    }

    private boolean treeNearby(Settings settings) {
        TreeTier tier = this.activeTier != null ? this.activeTier : settings.tier;
        if (tier == null) {
            return false;
        }
        int[] ids = settings.treeObjectIdsFor(tier);
        return TileObjects.getNearest(obj -> matchesTree(obj, ids, tier)) != null;
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

    public int getLogsBanked() {
        return this.logsBanked;
    }

    public int getLogsDropped() {
        return this.logsDropped;
    }

    public TreeTier getActiveTier() {
        return this.activeTier;
    }

    public int tick(Player player, Settings settings) {
        this.syncTier(settings);
        if (!this.travelDone && settings.gliderTravel && this.state != State.TRAVELING) {
            maybeBeginTravel(settings);
        }
        switch (this.state) {
            case TRAVELING:
                return this.doTravel(player, settings);
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

    private int doTravel(Player player, Settings settings) {
        int delay = this.travel.step(player, settings.gliderDestination);
        this.status = this.travel.status();
        if (delay == Integer.MIN_VALUE) {
            this.travelDone = true;
            this.state = State.CHOPPING;
            this.status = "Arrived — starting woodcutting";
            OxConsole.info("WCBot", this.status);
            return 800;
        }
        return delay;
    }

    private void syncTier(Settings settings) {
        TreeTier resolved = settings.tier;
        if (resolved == null) {
            return;
        }
        if (this.activeTier == null) {
            this.activeTier = resolved;
            this.pendingTier = null;
            return;
        }
        if (resolved != this.activeTier) {
            // Upgrade/downgrade: clear inventory of old logs before switching trees
            if (this.hasAnyDepositLogs(settings) && Inventory.getFreeSlots() < 28) {
                this.pendingTier = resolved;
                if (this.state == State.CHOPPING) {
                    this.pendingClearCount = countLogs(settings);
                    if (settings.mode == WoodcuttingMode.DROP) {
                        this.state = State.DROPPING;
                        this.status = "Tier change — dropping old logs";
                    } else {
                        this.state = State.OPENING_DEPOSIT;
                        this.depositInteractedAtMs = 0L;
                        this.status = "Tier change — banking old logs";
                    }
                }
            } else {
                this.activeTier = resolved;
                this.pendingTier = null;
            }
        }
    }

    private boolean hasAnyDepositLogs(Settings settings) {
        return Inventory.getFirst(item -> TreeProgression.isLogItem(item, settings.config)) != null;
    }

    private List<Integer> logIdsInInventory(Settings settings) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Item item : Inventory.getAll(i -> TreeProgression.isLogItem(i, settings.config))) {
            int id = item.getId();
            if (!ids.contains(id)) {
                ids.add(id);
            }
        }
        // Always include known IDs so deposit-box widgets still match even mid-frame
        for (Integer known : settings.depositLogIds) {
            if (!ids.contains(known)) {
                ids.add(known);
            }
        }
        return ids;
    }

    private int[] activeTreeIds(Settings settings) {
        TreeTier tier = this.activeTier != null ? this.activeTier : settings.tier;
        if (tier == null) {
            return new int[0];
        }
        return settings.treeObjectIdsFor(tier);
    }

    private int chop(Settings settings) {
        if (settings.tier == null) {
            this.status = "No trees enabled — tick Progression";
            return 1500;
        }
        if (Inventory.getFreeSlots() == 0) {
            if (settings.mode == WoodcuttingMode.DROP) {
                this.status = "Inventory full — dropping logs";
                this.pendingClearCount = countLogs(settings);
                this.state = State.DROPPING;
            } else {
                this.status = "Inventory full — finding deposit box";
                this.pendingClearCount = countLogs(settings);
                this.state = State.OPENING_DEPOSIT;
                this.depositInteractedAtMs = 0L;
            }
            return 300;
        }
        if (this.client.getLocalPlayer().getAnimation() != -1) {
            String label = this.activeTier != null ? this.activeTier.label : settings.tier.label;
            this.status = "Chopping " + label + "...";
            return 600;
        }
        int[] treeIds = this.activeTreeIds(settings);
        TreeTier tier = this.activeTier != null ? this.activeTier : settings.tier;
        TileObject tree = TileObjects.getNearest(obj -> matchesTree(obj, treeIds, tier));
        if (tree != null) {
            String label = tier != null ? tier.label : "tree";
            this.status = "Clicking " + label + " (id " + tree.getId() + ")";
            tree.interact(settings.chopAction);
            return 1800;
        }
        String label = tier != null ? tier.label : "tree";
        this.status = "Waiting for " + label + "...";
        return 800;
    }

    /** Match by configured object IDs, then by object name for the active tier (server ID drift). */
    private static boolean matchesTree(TileObject obj, int[] treeIds, TreeTier tier) {
        if (obj == null) {
            return false;
        }
        if (containsId(treeIds, obj.getId()) || containsId(treeIds, obj.getActualId())) {
            return true;
        }
        if (tier == null) {
            return false;
        }
        try {
            String name = obj.getName();
            if (name == null || name.isEmpty() || "null".equalsIgnoreCase(name)) {
                return false;
            }
            String lower = name.toLowerCase();
            switch (tier) {
                case NORMAL:
                    return lower.equals("tree") || lower.equals("dead tree") || lower.equals("evergreen");
                case OAK:
                    return lower.contains("oak");
                case WILLOW:
                    return lower.contains("willow");
                case MAPLE:
                    return lower.contains("maple");
                case YEW:
                    return lower.contains("yew");
                case MAGIC:
                    return lower.contains("magic");
                default:
                    return false;
            }
        } catch (Throwable t) {
            return false;
        }
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
            obj -> obj != null && obj.getId() == settings.depositObjectId
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
            if (this.pendingClearCount > 0 && !hasAnyDepositLogs(settings)) {
                this.logsBanked += this.pendingClearCount;
                this.pendingClearCount = 0;
                ++this.cycleCount;
            }
            this.afterInventoryCleared(settings);
            return 600;
        }
        List<Integer> logIds = this.logIdsInInventory(settings);
        boolean stillHasLogs = Inventory.getFirst(item -> TreeProgression.isLogItem(item, settings.config)) != null;
        if (!stillHasLogs) {
            this.status = "Closing deposit box";
            DepositBox.close();
            this.logsBanked += Math.max(0, this.pendingClearCount);
            this.pendingClearCount = 0;
            ++this.cycleCount;
            this.afterInventoryCleared(settings);
            this.depositSentAtMs = 0L;
            return 800;
        }
        long now = System.currentTimeMillis();
        if (this.depositSentAtMs > 0L && now - this.depositSentAtMs < 1500L) {
            this.status = "Depositing...";
            return 300;
        }
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
                    this.status = "Depositing logs (id " + child.getItemId() + ")";
                    child.interact(0);
                    this.depositSentAtMs = now;
                    return 600;
                }
            }
        }
        this.status = "Log slot not found";
        return 800;
    }

    private int drop(Settings settings) {
        List<Item> logs = Inventory.getAll(item -> TreeProgression.isLogItem(item, settings.config));
        if (logs.isEmpty()) {
            this.status = "Dropped — resuming";
            this.logsDropped += Math.max(0, this.pendingClearCount);
            this.pendingClearCount = 0;
            ++this.cycleCount;
            this.afterInventoryCleared(settings);
            return 300;
        }
        if (this.pendingClearCount <= 0) {
            this.pendingClearCount = countLogs(settings);
        }
        this.status = "Dropping logs (" + logs.size() + ")";
        for (Item item : logs) {
            item.interact("Drop");
            try {
                Thread.sleep(80L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return 300;
            }
        }
        return 300;
    }

    private int countLogs(Settings settings) {
        int total = 0;
        for (Item item : Inventory.getAll(i -> TreeProgression.isLogItem(i, settings.config))) {
            if (item == null) {
                continue;
            }
            total += Math.max(1, item.getQuantity());
        }
        return total;
    }

    private void afterInventoryCleared(Settings settings) {
        if (this.pendingTier != null) {
            this.activeTier = this.pendingTier;
            this.pendingTier = null;
            this.status = "Upgraded to " + this.activeTier.label;
            OxConsole.info("WCBot", this.status);
        } else if (settings.tier != null) {
            this.activeTier = settings.tier;
        }
        this.state = State.CHOPPING;
    }

    private static boolean containsId(int[] ids, int id) {
        for (int value : ids) {
            if (value == id) {
                return true;
            }
        }
        return false;
    }

    private enum State {
        TRAVELING,
        CHOPPING,
        OPENING_DEPOSIT,
        DEPOSITING,
        DROPPING
    }

    public static final class Settings {
        public final TreeTier tier;
        public final String chopAction;
        public final WoodcuttingMode mode;
        public final int depositObjectId;
        public final String depositAction;
        public final List<Integer> depositLogIds;
        public final boolean gliderTravel;
        public final String gliderDestination;
        public final boolean skipTravelIfTreesNearby;
        private final WoodcuttingConfig config;

        public Settings(
            TreeTier tier,
            WoodcuttingConfig config,
            String chopAction,
            WoodcuttingMode mode,
            int depositObjectId,
            String depositAction,
            List<Integer> depositLogIds,
            boolean gliderTravel,
            String gliderDestination,
            boolean skipTravelIfTreesNearby
        ) {
            this.tier = tier;
            this.config = config;
            this.chopAction = chopAction;
            this.mode = mode;
            this.depositObjectId = depositObjectId;
            this.depositAction = depositAction;
            this.depositLogIds = new ArrayList<>(depositLogIds);
            this.gliderTravel = gliderTravel;
            this.gliderDestination = gliderDestination;
            this.skipTravelIfTreesNearby = skipTravelIfTreesNearby;
        }

        int[] treeObjectIdsFor(TreeTier tier) {
            return TreeProgression.treeObjectIds(tier, this.config);
        }

        @Override
        public String toString() {
            return "Settings{tier=" + this.tier + ", trees=" + Arrays.toString(
                this.tier == null ? new int[0] : treeObjectIdsFor(this.tier)
            ) + "}";
        }
    }
}
