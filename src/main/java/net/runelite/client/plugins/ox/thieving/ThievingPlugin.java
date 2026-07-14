package net.runelite.client.plugins.ox.thieving;

import com.google.inject.Binder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
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
    name = "<html><nobr><font color='#B44DFF'>0xThieve</font></nobr></html>",
    description = "Stall thieving with level progression, shop selling, antiban, failsafe, and optional muling",
    tags = {"0x", "thieving", "stall", "bot", "automation", "mule"},
    enabledByDefault = false
)
public class ThievingPlugin extends OxLoopedPlugin {
    private static final String SCRIPT_ID = "thieve";

    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ThievingConfig config;

    private ThievingOverlay overlay;
    private ThievingEngine engine;
    private long sessionStartMs;
    private String sessionId = "";
    private boolean loggedInLastTick;
    private long lastHistoryFlushMs;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(ThievingConfig.class).toInstance(cm.getConfig(ThievingConfig.class));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new ThievingEngine(this.client);
        this.sessionStartMs = System.currentTimeMillis();
        this.sessionId = UUID.randomUUID().toString();
        this.lastHistoryFlushMs = 0L;
        this.loggedInLastTick = false;
        this.engine.reset(this.config, this.sessionStartMs);
        this.overlay = new ThievingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        OxPluginRegistry.register("0xThieve");
        publishLiveStats();
        OxConsole.info("Thieve", "script ready (enable stalls in Progression)");
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

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (this.engine != null) {
            this.engine.onChatMessage(event.getMessage());
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
            OxConsole.info("Thieve", "session started  thieving=" + getThievingLevel()
                + "  stall=" + getCurrentStallLabel());
            publishLiveStats();
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        int delay = this.engine.tick(player, this.config);
        publishLiveStats();
        maybeFlushHistory();
        return delay;
    }

    private void publishLiveStats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Status", getStatus());
        stats.put("Stall", getCurrentStallLabel());
        stats.put("Thieving", String.valueOf(getThievingLevel()));
        stats.put("Steals", String.valueOf(getStealCount()));
        stats.put("Sells", String.valueOf(getSellCount()));
        stats.put("Runtime", formatRuntime(this.sessionStartMs));
        OxScriptActivity.update(SCRIPT_ID, "0xThieve", stats);
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
        if (ended && getStealCount() == 0 && getSellCount() == 0
            && System.currentTimeMillis() - this.sessionStartMs < 15_000L) {
            return;
        }
        OxSessionHistory.Session session = new OxSessionHistory.Session();
        session.id = this.sessionId;
        session.script = "0xThieve";
        session.accountId = System.getProperty("ox.accountId", "");
        session.accountName = firstNonBlank(
            System.getProperty("ox.displayName", ""),
            System.getProperty("ox.username", "")
        );
        session.startMs = this.sessionStartMs;
        session.endMs = ended ? System.currentTimeMillis() : 0L;
        session.steals = getStealCount();
        session.sells = getSellCount();
        session.tree = getCurrentStallLabel();
        session.note = getStatus();
        OxSessionHistory.save(session);
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getState() {
        return this.engine != null ? this.engine.getState().name() : "STEALING";
    }

    public int getStealCount() {
        return this.engine != null ? this.engine.getStealCount() : 0;
    }

    public int getSellCount() {
        return this.engine != null ? this.engine.getSellCount() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public int getThievingLevel() {
        try {
            return this.client.getRealSkillLevel(Skill.THIEVING);
        } catch (Exception e) {
            return 1;
        }
    }

    public String getCurrentStallLabel() {
        return this.engine != null ? this.engine.getCurrentStallLabel() : "-";
    }

    public int getCurrentRequiredLevel() {
        return this.engine != null ? this.engine.getCurrentRequiredLevel() : 0;
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
