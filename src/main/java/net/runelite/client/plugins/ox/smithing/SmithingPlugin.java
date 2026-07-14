package net.runelite.client.plugins.ox.smithing;

import com.google.inject.Binder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
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
    name = "<html><nobr><font color='#B44DFF'>0xSmith</font></nobr></html>",
    description = "Auto smithing: bank, smelt ores into bars, smith bars into the highest item your level allows.",
    tags = {"0x", "smithing", "smith", "smelt", "bot", "skilling", "automation"},
    enabledByDefault = false
)
public class SmithingPlugin extends OxLoopedPlugin {
    private static final String SCRIPT_ID = "smith";

    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SmithingConfig config;

    private SmithingOverlay overlay;
    private SmithingEngine engine;
    private long sessionStartMs;
    private String sessionId = "";
    private boolean loggedInLastTick;
    private long lastHistoryFlushMs;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(SmithingConfig.class).toInstance(cm.getConfig(SmithingConfig.class));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new SmithingEngine(this.client);
        this.sessionStartMs = System.currentTimeMillis();
        this.sessionId = UUID.randomUUID().toString();
        this.lastHistoryFlushMs = 0L;
        this.loggedInLastTick = false;
        this.overlay = new SmithingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        OxPluginRegistry.register("0xSmith");
        publishLiveStats();
        OxConsole.info("Smith", "script ready");
    }

    @Override
    protected void onStop() throws Exception {
        persistSession(true);
        OxScriptActivity.clear(SCRIPT_ID);
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay) this.overlay);
        }
        this.overlay = null;
        if (this.engine != null) {
            this.engine.setStatus("Idle");
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();
        if (state == GameState.LOGIN_SCREEN || state == GameState.CONNECTION_LOST) {
            if (this.engine != null) {
                this.engine.setStatus("Logged out");
            }
            this.loggedInLastTick = false;
            persistSession(false);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getMessage() == null || this.engine == null) {
            return;
        }
        this.engine.onChatMessage(event.getMessage().replaceAll("<[^>]*>", "").toLowerCase());
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            if (this.engine != null) {
                this.engine.setStatus("Loading...");
            }
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            publishLiveStats();
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            if (this.engine != null) {
                this.engine.setStatus("Resuming...");
            }
            OxConsole.info("Smith", "session started  smith=" + getSmithLevel()
                + "  task=" + getTaskLabel());
            publishLiveStats();
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        int delay = this.engine.tick(player, this.buildSettings());
        publishLiveStats();
        maybeFlushHistory();
        return delay;
    }

    private SmithingEngine.Settings buildSettings() {
        return new SmithingEngine.Settings(
            this.config.mode(),
            this.config.stopWhenOutOfMaterials(),
            this.config.antiBanMs()
        );
    }

    private void publishLiveStats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Status", getStatus());
        stats.put("Task", getTaskLabel());
        stats.put("Smithing", String.valueOf(getSmithLevel()));
        stats.put("Bars", String.valueOf(getBarsSmelted()));
        stats.put("Items", String.valueOf(getItemsSmithed()));
        stats.put("Trips", String.valueOf(getTripCount()));
        stats.put("XP", String.valueOf(getXpGained()));
        stats.put("Runtime", formatRuntime(this.sessionStartMs));
        OxScriptActivity.update(SCRIPT_ID, "0xSmith", stats);
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
        if (this.sessionId.isEmpty() || this.engine == null) {
            return;
        }
        if (ended && getBarsSmelted() == 0 && getItemsSmithed() == 0 && getTripCount() == 0
            && System.currentTimeMillis() - this.sessionStartMs < 15_000L) {
            return;
        }
        OxSessionHistory.Session session = new OxSessionHistory.Session();
        session.id = this.sessionId;
        session.script = "0xSmith";
        session.accountId = System.getProperty("ox.accountId", "");
        session.accountName = firstNonBlank(
            System.getProperty("ox.displayName", ""),
            System.getProperty("ox.username", "")
        );
        session.startMs = this.sessionStartMs;
        session.endMs = ended ? System.currentTimeMillis() : 0L;
        session.barsSmelted = getBarsSmelted();
        session.itemsSmithed = getItemsSmithed();
        session.bankTrips = getTripCount();
        session.xpGained = getXpGained();
        session.tree = getTaskLabel();
        session.note = getStatus();
        OxSessionHistory.save(session);
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getStateName() {
        return this.engine != null ? this.engine.getStateName() : "DECIDE";
    }

    public String getTaskLabel() {
        return this.engine != null ? this.engine.getTaskLabel() : "-";
    }

    public int getMadeCount() {
        return this.engine != null ? this.engine.getMadeCount() : 0;
    }

    public int getBarsSmelted() {
        return this.engine != null ? this.engine.getBarsSmelted() : 0;
    }

    public int getItemsSmithed() {
        return this.engine != null ? this.engine.getItemsSmithed() : 0;
    }

    public int getTripCount() {
        return this.engine != null ? this.engine.getTripCount() : 0;
    }

    public int getSmithLevel() {
        return this.engine != null ? this.engine.getSmithLevel() : 1;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public int getXpGained() {
        return this.engine != null ? this.engine.getXpGained() : 0;
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
