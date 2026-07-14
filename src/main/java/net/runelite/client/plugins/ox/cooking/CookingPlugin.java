package net.runelite.client.plugins.ox.cooking;

import com.google.inject.Binder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
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
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>0xCook</font></nobr></html>",
    description = "Seabright Pier cooking: bank booth 10517 -> withdraw raw fish -> use on range 114 -> cook click -> bank.",
    tags = {"0x", "cooking", "cook", "fish", "pier", "bank", "bot"},
    enabledByDefault = false
)
public class CookingPlugin extends OxLoopedPlugin {
    private static final String SCRIPT_ID = "cook";

    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CookingConfig config;

    private CookingOverlay overlay;
    private CookingEngine engine;
    private long sessionStartMs;
    private String sessionId = "";
    private boolean loggedInLastTick;
    private long lastHistoryFlushMs;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(CookingConfig.class).toInstance(cm.getConfig(CookingConfig.class));
    }

    @Override
    protected void onStart() {
        this.engine = new CookingEngine();
        this.engine.reset();
        this.sessionStartMs = System.currentTimeMillis();
        this.sessionId = UUID.randomUUID().toString();
        this.lastHistoryFlushMs = 0L;
        this.loggedInLastTick = false;
        this.overlay = new CookingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        OxPluginRegistry.register("0xCook");
        publishLiveStats();
        OxConsole.info("Cook", "script ready - pier bank/range loop");
    }

    @Override
    protected void onStop() {
        persistSession(true);
        OxScriptActivity.clear(SCRIPT_ID);
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay) this.overlay);
        }
        this.overlay = null;
        this.engine = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
            this.loggedInLastTick = false;
        }
    }

    @Override
    protected int loop() {
        if (!Game.isLoggedIn() || this.client.getGameState() != GameState.LOGGED_IN) {
            this.loggedInLastTick = false;
            publishLiveStats();
            return 600;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            if (this.engine != null) {
                this.engine.reset();
            }
            OxConsole.info("Cook", "logged in - starting bank loop");
        }

        Player player = Players.getLocal();
        int delay = 600;
        if (this.engine != null && player != null) {
            delay = this.engine.tick(this.client, player, this.config);
        }
        publishLiveStats();
        maybeFlushHistory();
        return delay;
    }

    public String getState() {
        return this.engine != null ? this.engine.getState().name() : "IDLE";
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "-";
    }

    public String getFishLabel() {
        return this.engine != null ? this.engine.getActiveLabel() : "-";
    }

    public int getTrips() {
        return this.engine != null ? this.engine.getTrips() : 0;
    }

    public int getCookedEstimate() {
        return this.engine != null ? this.engine.getCookedEstimate() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    private void publishLiveStats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Status", getStatus());
        stats.put("Fish", getFishLabel());
        stats.put("State", getState());
        stats.put("Trips", String.valueOf(getTrips()));
        stats.put("Cooked", String.valueOf(getCookedEstimate()));
        OxScriptActivity.update(SCRIPT_ID, "0xCook", stats);
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
        if (ended && getTrips() == 0 && getCookedEstimate() == 0
            && System.currentTimeMillis() - this.sessionStartMs < 15_000L) {
            return;
        }
        try {
            OxSessionHistory.Session session = new OxSessionHistory.Session();
            session.id = this.sessionId;
            session.script = "0xCook";
            session.accountId = System.getProperty("ox.accountId", "");
            session.accountName = System.getProperty("ox.displayName",
                System.getProperty("ox.username", ""));
            session.startMs = this.sessionStartMs;
            session.endMs = ended ? System.currentTimeMillis() : 0L;
            session.bankTrips = getTrips();
            session.itemsGathered = getCookedEstimate();
            session.tree = getFishLabel();
            session.note = getStatus();
            OxSessionHistory.save(session);
            if (ended) {
                OxConsole.info("Cook", "session saved trips=" + getTrips()
                    + " cooked~" + getCookedEstimate());
            }
        } catch (Throwable t) {
            // non-fatal
        }
    }
}
