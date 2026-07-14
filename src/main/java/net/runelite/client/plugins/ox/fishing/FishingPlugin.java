package net.runelite.client.plugins.ox.fishing;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ox.core.OxConsole;
import net.runelite.client.plugins.ox.core.OxLoopedPlugin;
import net.runelite.client.plugins.ox.core.OxPluginRegistry;
import net.runelite.client.plugins.ox.core.OxScriptActivity;
import net.runelite.client.plugins.ox.core.OxSessionHistory;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.DepositBox;
import net.unethicalite.api.items.Inventory;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>\u25cf 0xFish</font></nobr></html>",
    description = "Progressive Seabright Pier fishing (shrimp→lobster→monk→shark) with glider travel and deposit any fish.",
    tags = {"0x", "fishing", "bot", "automation", "glider", "progression"},
    enabledByDefault = false
)
public class FishingPlugin extends OxLoopedPlugin {
    private static final String SCRIPT_ID = "fish";

    private enum FishingState {
        TRAVELING,
        FISHING,
        OPENING_BARREL,
        DEPOSITING,
        DROPPING
    }

    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FishingConfig config;

    private FishingOverlay overlay;
    private final FishGliderTravel travel = new FishGliderTravel();
    private FishingState state = FishingState.FISHING;
    private String status = "Starting";
    private int depositCount;
    private int fishBanked;
    private int pendingClearCount;
    private long sessionStartMs;
    private String sessionId = "";
    private boolean loggedInLastTick;
    private long lastHistoryFlushMs;
    private long barrelInteractedAtMs;
    private long depositSentAtMs;
    private boolean travelDone;
    private FishTier activeTier;
    private FishTier lastLoggedTier;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(FishingConfig.class).toInstance(cm.getConfig(FishingConfig.class));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new FishingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        this.state = FishingState.FISHING;
        this.status = "Starting";
        this.loggedInLastTick = false;
        this.depositCount = 0;
        this.fishBanked = 0;
        this.pendingClearCount = 0;
        this.sessionStartMs = System.currentTimeMillis();
        this.sessionId = UUID.randomUUID().toString();
        this.lastHistoryFlushMs = 0L;
        this.barrelInteractedAtMs = 0L;
        this.depositSentAtMs = 0L;
        this.travelDone = false;
        this.activeTier = null;
        this.lastLoggedTier = null;
        OxPluginRegistry.register("0xFish");
        publishLiveStats();
        OxConsole.info("Fish", "script ready (enable tiers/travel in config)");
    }

    @Override
    protected void onStop() throws Exception {
        persistSession(true);
        OxScriptActivity.clear(SCRIPT_ID);
        this.overlayManager.remove((Overlay) this.overlay);
        this.overlay = null;
        this.status = "Idle";
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState gameState = event.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.status = "Logged out";
            this.loggedInLastTick = false;
            persistSession(false);
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.status = "Loading...";
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            publishLiveStats();
            return 1500;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }

        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.travelDone = false;
            this.activeTier = FishProgression.resolve(this.config, fishingLevel());
            maybeBeginTravel(player);
            OxConsole.info("Fish", "session started  fish=" + fishingLevel()
                + "  tier=" + (this.activeTier != null ? this.activeTier.label : "none")
                + "  mode=" + getModeLabel());
            publishLiveStats();
            return 1200;
        }

        syncTier();
        if (!this.travelDone && this.config.gliderTravel() && this.state != FishingState.TRAVELING) {
            maybeBeginTravel(player);
        }

        int delay;
        switch (this.state) {
            case TRAVELING:
                delay = loopTravel(player);
                break;
            case FISHING:
                delay = loopFishing();
                break;
            case OPENING_BARREL:
                delay = loopOpeningBarrel();
                break;
            case DEPOSITING:
                delay = loopDepositing();
                break;
            case DROPPING:
                delay = loopDropping();
                break;
            default:
                delay = 600;
        }
        publishLiveStats();
        maybeFlushHistory();
        return delay;
    }

    private void maybeBeginTravel(Player player) {
        if (!this.config.gliderTravel() || this.travelDone) {
            return;
        }
        FishTier tier = this.activeTier != null
            ? this.activeTier
            : FishProgression.resolve(this.config, fishingLevel());

        // Already at pier (spot in range) — never ::home
        if (this.config.skipTravelIfSpotNearby() && spotNearby(tier)) {
            this.travelDone = true;
            this.state = FishingState.FISHING;
            this.status = "At pier — skipping travel";
            OxConsole.info("Fish", this.status);
            return;
        }

        // Only leave for the glider when we are actually at home
        if (shouldTravelFromHome(player)) {
            this.state = FishingState.TRAVELING;
            this.travel.begin();
            this.status = "Travel: starting from home";
            OxConsole.info("Fish", this.status);
            return;
        }

        // Started away from home (e.g. already at pier before spots load) — stay put
        this.travelDone = true;
        this.state = FishingState.FISHING;
        this.status = "Not at home — skipping travel";
        OxConsole.info("Fish", this.status);
    }

    /** At Seers home / glider area — same tiles WC uses. */
    private static boolean shouldTravelFromHome(Player player) {
        if (player == null || player.getWorldLocation() == null) {
            return false;
        }
        WorldPoint here = player.getWorldLocation();
        return here.distanceTo(FishGliderTravel.HOME_CENTER) <= 40
            || here.distanceTo(FishGliderTravel.GLIDER_TILE) <= 40;
    }

    private boolean spotNearby(FishTier tier) {
        if (tier == null) {
            return false;
        }
        return NPCs.getNearest(npc -> npc != null && !npc.isDead() && tier.matchesSpot(npc.getId())) != null;
    }

    private int loopTravel(Player player) {
        int delay = this.travel.step(player, this.config.gliderDestination());
        this.status = this.travel.status();
        if (delay == Integer.MIN_VALUE) {
            this.travelDone = true;
            this.state = FishingState.FISHING;
            this.status = "Arrived — starting fishing";
            OxConsole.info("Fish", this.status);
            return 800;
        }
        return delay;
    }

    private void syncTier() {
        FishTier resolved = FishProgression.resolve(this.config, fishingLevel());
        if (resolved == null) {
            return;
        }
        if (this.activeTier == null) {
            this.activeTier = resolved;
            return;
        }
        if (resolved != this.activeTier) {
            if (hasAnyFish() && Inventory.getFreeSlots() < 28) {
                // Clear leftovers from previous tier before switching spots
                this.pendingClearCount = countFish();
                if (this.config.fishingMode() == FishingMode.DROP) {
                    this.state = FishingState.DROPPING;
                    this.status = "Tier change — dropping leftover fish";
                } else {
                    this.state = FishingState.OPENING_BARREL;
                    this.barrelInteractedAtMs = 0L;
                    this.status = "Tier change — banking leftover fish";
                }
            }
            this.activeTier = resolved;
            if (this.lastLoggedTier != resolved) {
                this.lastLoggedTier = resolved;
                OxConsole.info("Fish", "tier → " + resolved.label
                    + "  spot=" + resolved.spotNpcId
                    + "  action=" + resolved.fishingAction);
            }
        }
    }

    private int loopFishing() {
        if (Inventory.getFreeSlots() == 0) {
            return beginInventoryClear("Inventory full");
        }

        FishTier tier = this.activeTier;
        if (tier == null) {
            this.status = "No fishing tier enabled";
            return 1500;
        }
        if (fishingLevel() < tier.requiredLevel) {
            this.status = "Need Fishing " + tier.requiredLevel + " for " + tier.label;
            return 1500;
        }

        if (this.client.getLocalPlayer() != null && this.client.getLocalPlayer().getAnimation() != -1) {
            this.status = "Fishing " + tier.label + "...";
            return 600;
        }

        NPC spot = NPCs.getNearest(npc -> npc != null && !npc.isDead() && tier.matchesSpot(npc.getId()));
        if (spot != null) {
            this.status = "Clicking " + tier.label + " (" + tier.fishingAction + ")";
            if (!interactSpot(spot, tier)) {
                this.status = "No '" + tier.fishingAction + "' on spot " + tier.spotNpcId;
                return 800;
            }
            return 1800;
        }
        this.status = "Waiting for " + tier.label + " spot (" + tier.spotNpcId + ")...";
        return 800;
    }

    /**
     * Click the configured action only. For Monkfish this must be Net — never Harpoon.
     */
    private static boolean interactSpot(NPC spot, FishTier tier) {
        String wanted = tier.fishingAction;
        if (wanted == null || wanted.isEmpty()) {
            return false;
        }
        String[] actions = spot.getActions();
        if (actions == null) {
            spot.interact(wanted);
            return true;
        }

        int exact = -1;
        int contains = -1;
        String wantedLower = wanted.toLowerCase();
        for (int i = 0; i < actions.length; i++) {
            String action = actions[i];
            if (action == null || action.isEmpty()) {
                continue;
            }
            String lower = action.toLowerCase();
            // Monkfish / Net tier: never pick Harpoon even if Net is missing later
            if (tier == FishTier.MONKFISH && lower.contains("harpoon")) {
                continue;
            }
            if (lower.equals(wantedLower)) {
                exact = i;
                break;
            }
            if (contains < 0 && lower.contains(wantedLower)) {
                contains = i;
            }
        }
        int idx = exact >= 0 ? exact : contains;
        if (idx >= 0) {
            spot.interact(idx);
            return true;
        }
        // Last resort: string interact (still avoid harpoon text for monk)
        if (tier == FishTier.MONKFISH) {
            for (String action : actions) {
                if (action != null && action.toLowerCase().contains("net")
                    && !action.toLowerCase().contains("harpoon")) {
                    spot.interact(action);
                    return true;
                }
            }
            return false;
        }
        spot.interact(wanted);
        return true;
    }

    private int beginInventoryClear(String reason) {
        if (this.config.fishingMode() == FishingMode.DROP) {
            this.status = reason + " — dropping fish";
            this.state = FishingState.DROPPING;
        } else {
            this.status = reason + " — finding barrel";
            this.state = FishingState.OPENING_BARREL;
            this.barrelInteractedAtMs = 0L;
        }
        this.pendingClearCount = countFish();
        return 300;
    }

    private int loopOpeningBarrel() {
        if (DepositBox.isOpen()) {
            this.status = "Deposit box open — depositing";
            this.state = FishingState.DEPOSITING;
            this.depositSentAtMs = 0L;
            return 300;
        }
        long now = System.currentTimeMillis();
        if (this.barrelInteractedAtMs > 0L && now - this.barrelInteractedAtMs < 4000L) {
            this.status = "Waiting for barrel...";
            return 400;
        }
        int barrelId = this.config.barrelObjectId();
        TileObject barrel = TileObjects.getNearest(obj -> obj != null && obj.getId() == barrelId);
        if (barrel != null) {
            this.status = "Opening barrel";
            barrel.interact(this.config.barrelAction());
            this.barrelInteractedAtMs = now;
            return 1200;
        }
        this.status = "Barrel " + barrelId + " not found";
        return 800;
    }

    private int loopDepositing() {
        if (!DepositBox.isOpen()) {
            this.status = "Returning to spot";
            this.state = FishingState.FISHING;
            this.depositSentAtMs = 0L;
            return 600;
        }
        if (!hasAnyFish()) {
            this.status = "Closing deposit box";
            DepositBox.close();
            if (this.pendingClearCount > 0) {
                this.fishBanked += this.pendingClearCount;
                this.pendingClearCount = 0;
            }
            ++this.depositCount;
            this.state = FishingState.FISHING;
            this.depositSentAtMs = 0L;
            return 800;
        }
        long now = System.currentTimeMillis();
        if (this.depositSentAtMs > 0L && now - this.depositSentAtMs < 1500L) {
            this.status = "Depositing...";
            return 300;
        }

        // Same pattern as WC: click fish item slots in deposit-box inventory (192:2).
        // Do NOT click 192:19 — on this client that is the quantity-All control.
        List<Integer> fishIds = fishIdsInInventory();
        Widget inventory = this.client.getWidget(192, 2);
        if (inventory != null) {
            Widget[] children = inventory.getDynamicChildren();
            if (children != null) {
                for (Widget child : children) {
                    if (child == null || child.isHidden()) {
                        continue;
                    }
                    int itemId = child.getItemId();
                    if (itemId <= 0 || !fishIds.contains(itemId)) {
                        continue;
                    }
                    this.status = "Depositing fish (id " + itemId + ")";
                    child.interact(0);
                    this.depositSentAtMs = now;
                    return 600;
                }
            }
        }

        this.status = "Fish slot not found (ids=" + fishIds + ")";
        return 800;
    }

    /** IDs currently in inventory that we should bank, plus known fish IDs (like WC logs). */
    private List<Integer> fishIdsInInventory() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Item item : Inventory.getAll(FishProgression::isFishItem)) {
            if (item == null) {
                continue;
            }
            int id = item.getId();
            if (id > 0 && !ids.contains(id)) {
                ids.add(id);
            }
        }
        for (Integer known : FishProgression.depositFishIds()) {
            if (known != null && known > 0 && !ids.contains(known)) {
                ids.add(known);
            }
        }
        return ids;
    }

    private int loopDropping() {
        List<Item> fish = Inventory.getAll(FishProgression::isFishItem);
        if (fish.isEmpty()) {
            this.status = "Dropped — resuming";
            if (this.pendingClearCount > 0) {
                // counted as "cleared" for session; drop mode does not bank
                this.pendingClearCount = 0;
            }
            ++this.depositCount;
            this.state = FishingState.FISHING;
            return 300;
        }
        this.status = "Dropping fish (" + fish.size() + ")";
        for (Item item : fish) {
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

    private boolean hasAnyFish() {
        return Inventory.getFirst(FishProgression::isFishItem) != null;
    }

    private int countFish() {
        int n = 0;
        for (Item item : Inventory.getAll(FishProgression::isFishItem)) {
            if (item != null) {
                n += Math.max(1, item.getQuantity());
            }
        }
        return n;
    }

    private int fishingLevel() {
        try {
            return this.client.getRealSkillLevel(Skill.FISHING);
        } catch (Exception e) {
            return 1;
        }
    }

    private void publishLiveStats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Status", getStatus());
        stats.put("Tier", getFishLabel());
        stats.put("Fishing", String.valueOf(fishingLevel()));
        stats.put("Mode", getModeLabel());
        stats.put("Trips", String.valueOf(getDepositCount()));
        stats.put("Fish banked", String.valueOf(this.fishBanked));
        stats.put("Runtime", formatRuntime(this.sessionStartMs));
        OxScriptActivity.update(SCRIPT_ID, "0xFish", stats);
    }

    private void maybeFlushHistory() {
        long now = System.currentTimeMillis();
        if (now - this.lastHistoryFlushMs < 30_000L) {
            return;
        }
        this.lastHistoryFlushMs = now;
        persistSession(false);
    }

    private void persistSession(boolean ended) {
        if (this.sessionId.isEmpty()) {
            return;
        }
        if (ended && getDepositCount() == 0 && this.fishBanked == 0
            && System.currentTimeMillis() - this.sessionStartMs < 15_000L) {
            return;
        }
        OxSessionHistory.Session session = new OxSessionHistory.Session();
        session.id = this.sessionId;
        session.script = "0xFish";
        session.accountId = System.getProperty("ox.accountId", "");
        session.accountName = firstNonBlank(
            System.getProperty("ox.displayName", ""),
            System.getProperty("ox.username", "")
        );
        session.startMs = this.sessionStartMs;
        session.endMs = ended ? System.currentTimeMillis() : 0L;
        session.bankTrips = getDepositCount();
        session.itemsGathered = this.fishBanked;
        session.tree = getFishLabel();
        session.note = getModeLabel() + " · " + getStatus();
        OxSessionHistory.save(session);
    }

    public String getStatus() {
        return this.status;
    }

    public String getState() {
        return this.state.name();
    }

    public int getDepositCount() {
        return this.depositCount;
    }

    public int getFishBanked() {
        return this.fishBanked;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public String getModeLabel() {
        return this.config != null && this.config.fishingMode() == FishingMode.DROP ? "DROP" : "BANK";
    }

    public String getFishLabel() {
        FishTier tier = this.activeTier;
        if (tier == null && this.config != null) {
            tier = FishProgression.resolve(this.config, fishingLevel());
        }
        return tier != null ? tier.label : "—";
    }

    public int getFishingLevel() {
        return fishingLevel();
    }

    private static String formatRuntime(long startMs) {
        if (startMs <= 0L) {
            return "0s";
        }
        long sec = Math.max(0L, (System.currentTimeMillis() - startMs) / 1000L);
        long h = sec / 3600L;
        long m = (sec % 3600L) / 60L;
        long s = sec % 60L;
        if (h > 0L) {
            return h + "h " + m + "m";
        }
        if (m > 0L) {
            return m + "m";
        }
        return s + "s";
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b == null ? "" : b.trim();
    }
}
