package net.runelite.client.plugins.ox.woodcutting;

import com.google.inject.Binder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
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

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>0xWCBot</font></nobr></html>",
    description = "Auto woodcutting with level progression (Normal->Oak->Willow->Maple->Yew->Magic). Deposit log IDs follow the active tree.",
    tags = {"0x", "woodcutting", "bot", "automation", "deposit", "progression"},
    enabledByDefault = false
)
public class WoodcuttingPlugin extends OxLoopedPlugin {
    private static final String SCRIPT_ID = "wcbot";

    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private WoodcuttingConfig config;

    private WoodcuttingOverlay overlay;
    private WoodcuttingEngine engine;
    private long sessionStartMs = 0L;
    private String sessionId = "";
    private boolean loggedInLastTick = false;
    private TreeTier lastResolvedTier;
    private long lastHistoryFlushMs = 0L;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(WoodcuttingConfig.class).toInstance(cm.getConfig(WoodcuttingConfig.class));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new WoodcuttingEngine(this.client);
        this.engine.resetSessionStats();
        this.overlay = new WoodcuttingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
        this.sessionId = UUID.randomUUID().toString();
        this.lastResolvedTier = null;
        this.lastHistoryFlushMs = 0L;
        this.engine.reset();
        OxPluginRegistry.register("0xWCBot");
        publishLiveStats();
        OxConsole.info("WCBot", "script ready (enable travel/trees in config)");
    }

    @Override
    protected void onStop() throws Exception {
        persistSession(true);
        OxScriptActivity.clear(SCRIPT_ID);
        this.overlayManager.remove((Overlay) this.overlay);
        this.overlay = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();
        if (state == GameState.LOGIN_SCREEN || state == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
            persistSession(false);
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            publishLiveStats();
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            int wcLevel = this.client.getRealSkillLevel(Skill.WOODCUTTING);
            TreeTier tier = TreeProgression.resolve(this.config, wcLevel);
            this.lastResolvedTier = tier;
            WoodcuttingEngine.Settings settings = this.buildSettings(tier);
            this.engine.onSessionStart(settings);
            OxConsole.info("WCBot", "session started  wc=" + wcLevel
                + "  tier=" + (tier != null ? tier.label : "none"));
            publishLiveStats();
            return 1200;
        }
        if (this.client.getLocalPlayer() == null) {
            return 600;
        }

        int wcLevel = this.client.getRealSkillLevel(Skill.WOODCUTTING);
        TreeTier tier = TreeProgression.resolve(this.config, wcLevel);
        this.lastResolvedTier = tier;

        int delay = this.engine.tick(this.client.getLocalPlayer(), this.buildSettings(tier));
        publishLiveStats();
        maybeFlushHistory();
        return delay;
    }

    private void publishLiveStats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Status", getStatus());
        stats.put("Tree", shortTree(getActiveTreeLabel()));
        stats.put("WC Level", String.valueOf(getWcLevel()));
        stats.put("Logs Banked", String.valueOf(getLogsBanked()));
        stats.put("Logs Dropped", String.valueOf(getLogsDropped()));
        stats.put("Bank Trips", String.valueOf(getCycleCount()));
        stats.put("Runtime", formatRuntime(this.sessionStartMs));
        OxScriptActivity.update(SCRIPT_ID, "0xWCBot", stats);
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
        if (this.sessionId == null || this.sessionId.isEmpty() || this.engine == null) {
            return;
        }
        // Skip empty sessions that never did useful work
        if (ended && getLogsBanked() == 0 && getLogsDropped() == 0 && getCycleCount() == 0
            && System.currentTimeMillis() - this.sessionStartMs < 15_000L) {
            return;
        }
        OxSessionHistory.Session session = new OxSessionHistory.Session();
        session.id = this.sessionId;
        session.script = "0xWCBot";
        session.accountId = System.getProperty("ox.accountId", "");
        session.accountName = firstNonBlank(
            System.getProperty("ox.displayName", ""),
            System.getProperty("ox.username", "")
        );
        session.startMs = this.sessionStartMs;
        session.endMs = ended ? System.currentTimeMillis() : 0L;
        session.logsBanked = getLogsBanked();
        session.logsDropped = getLogsDropped();
        session.bankTrips = getCycleCount();
        session.tree = shortTree(getActiveTreeLabel());
        session.note = getStatus();
        OxSessionHistory.save(session);
    }

    private WoodcuttingEngine.Settings buildSettings(TreeTier tier) {
        return new WoodcuttingEngine.Settings(
            tier,
            this.config,
            this.config.chopAction(),
            this.config.inventoryMode(),
            this.config.depositObjectId(),
            this.config.depositAction(),
            TreeProgression.depositLogIds(this.config),
            this.config.gliderTravel(),
            this.config.gliderDestination(),
            this.config.skipTravelIfTreesNearby()
        );
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getState() {
        return this.engine != null ? this.engine.getState() : "CHOPPING";
    }

    public int getCycleCount() {
        return this.engine != null ? this.engine.getCycleCount() : 0;
    }

    public int getLogsBanked() {
        return this.engine != null ? this.engine.getLogsBanked() : 0;
    }

    public int getLogsDropped() {
        return this.engine != null ? this.engine.getLogsDropped() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public int getWcLevel() {
        try {
            return this.client.getRealSkillLevel(Skill.WOODCUTTING);
        } catch (Exception e) {
            return 1;
        }
    }

    public String getActiveTreeLabel() {
        TreeTier active = this.engine != null ? this.engine.getActiveTier() : null;
        if (active != null) {
            return active.label + " (req " + active.requiredLevel + ")";
        }
        if (this.lastResolvedTier != null) {
            return this.lastResolvedTier.label;
        }
        return "-";
    }

    public String getActiveLogId() {
        TreeTier active = this.engine != null ? this.engine.getActiveTier() : this.lastResolvedTier;
        if (active == null) {
            return "-";
        }
        return String.valueOf(TreeProgression.logItemId(active, this.config));
    }

    private static String shortTree(String label) {
        if (label == null || label.isEmpty()) {
            return "-";
        }
        int cut = label.indexOf(" (");
        return cut > 0 ? label.substring(0, cut) : label;
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
