package net.runelite.client.plugins.ox.mining;

import com.google.inject.Binder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
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
import net.unethicalite.api.game.Skills;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>\u25cf 0xMine</font></nobr></html>",
    description = "Mines a selectable ore — or gathers ores for a chosen bar — and banks or power-drops when full.",
    tags = {"0x", "mining", "bot", "skilling", "automation"},
    enabledByDefault = false
)
public class MiningPlugin extends OxLoopedPlugin {
    private static final String SCRIPT_ID = "mine";

    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MiningConfig config;

    private MiningOverlay overlay;
    private MiningEngine engine;
    private BarGoal barGoal;
    private long sessionStartMs = 0L;
    private String sessionId = "";
    private boolean loggedInLastTick = false;
    private long lastHistoryFlushMs = 0L;
    private final Random breakRng = new Random();
    private long nextBreakAtMs = 0L;
    private long breakUntilMs = 0L;
    private volatile String pauseStatus = null;
    private MiningOre lastOre;
    private MiningMode lastMode = MiningMode.BANK;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(MiningConfig.class).toInstance(cm.getConfig(MiningConfig.class));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new MiningEngine(this.client);
        this.engine.reset();
        this.barGoal = new BarGoal();
        this.overlay = new MiningOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
        this.sessionId = UUID.randomUUID().toString();
        this.lastHistoryFlushMs = 0L;
        this.nextBreakAtMs = 0L;
        this.breakUntilMs = 0L;
        this.pauseStatus = null;
        this.lastOre = null;
        this.lastMode = this.config.miningMode();
        OxPluginRegistry.register("0xMine");
        publishLiveStats();
        OxConsole.info("Mine", "script ready");
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
        if (this.engine == null || event.getMessage() == null) {
            return;
        }
        this.engine.onChatMessage(event.getMessage().replaceAll("<[^>]*>", "").toLowerCase());
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
            OxConsole.info("Mine", "session started  mining=" + miningLevel()
                + "  ore=" + this.config.ore());
            publishLiveStats();
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }

        this.barGoal.sync(this.config.barType(), this.config.barAmount());
        this.barGoal.trackInventory();
        MiningOre ore = this.config.ore();
        MiningMode mode = this.config.miningMode();
        boolean bankAndHold = false;
        if (this.barGoal.isActive()) {
            mode = MiningMode.BANK;
            MiningOre decided = this.barGoal.decide(this.engine.getTripCount(), this.miningLevel(), this.config.stopWhenDone());
            if (decided == null) {
                bankAndHold = true;
            } else {
                ore = decided;
            }
        }
        this.lastOre = ore;
        this.lastMode = mode;

        int breakDelay = this.maybeBreak(bankAndHold);
        if (breakDelay > 0) {
            publishLiveStats();
            return breakDelay;
        }
        this.pauseStatus = null;
        int delay = this.engine.tick(player, new MiningEngine.Settings(
            ore, mode, this.config.returnToMine(), this.config.keepItems(),
            this.config.matchRockByName(), this.config.rockNameOverride(),
            this.config.avoidOtherPlayers(), bankAndHold
        ));
        publishLiveStats();
        maybeFlushHistory();
        return delay;
    }

    private int maybeBreak(boolean done) {
        if (!this.config.microBreaks()) {
            this.breakUntilMs = 0L;
            return 0;
        }
        long now = System.currentTimeMillis();
        if (this.breakUntilMs > 0L) {
            if (now < this.breakUntilMs) {
                this.pauseStatus = "Short break \u2014 " + (this.breakUntilMs - now) / 1000L + "s left";
                return 1000 + this.breakRng.nextInt(600);
            }
            this.breakUntilMs = 0L;
            this.scheduleNextBreak(now);
            return 0;
        }
        if (this.nextBreakAtMs == 0L) {
            this.scheduleNextBreak(now);
            return 0;
        }
        if (now >= this.nextBreakAtMs && !done && "MINING".equals(this.engine.getStateName())) {
            long duration = this.breakRng.nextDouble() < 0.8
                ? 15000L + (long) this.breakRng.nextInt(60000)
                : 90000L + (long) this.breakRng.nextInt(150000);
            this.breakUntilMs = now + duration;
            this.pauseStatus = "Short break \u2014 " + duration / 1000L + "s";
            return 1200;
        }
        return 0;
    }

    private void scheduleNextBreak(long now) {
        this.nextBreakAtMs = now + 420000L + (long) this.breakRng.nextInt(840000);
    }

    private int miningLevel() {
        int boosted = Skills.getBoostedLevel(Skill.MINING);
        return boosted > 0 ? boosted : Skills.getLevel(Skill.MINING);
    }

    private void publishLiveStats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Status", getStatus());
        stats.put("Ore", getTargetLabel());
        stats.put("Mining", String.valueOf(getMiningLevel()));
        stats.put("Trips", String.valueOf(getTripCount()));
        stats.put("XP", String.valueOf(getXpGained()));
        if (this.barGoal != null && this.barGoal.isActive()) {
            stats.put("Bars", String.valueOf(this.barGoal.barsGathered()));
        }
        stats.put("Runtime", formatRuntime(this.sessionStartMs));
        OxScriptActivity.update(SCRIPT_ID, "0xMine", stats);
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
        if (ended && getTripCount() == 0 && getXpGained() == 0
            && System.currentTimeMillis() - this.sessionStartMs < 15_000L) {
            return;
        }
        OxSessionHistory.Session session = new OxSessionHistory.Session();
        session.id = this.sessionId;
        session.script = "0xMine";
        session.accountId = System.getProperty("ox.accountId", "");
        session.accountName = firstNonBlank(
            System.getProperty("ox.displayName", ""),
            System.getProperty("ox.username", "")
        );
        session.startMs = this.sessionStartMs;
        session.endMs = ended ? System.currentTimeMillis() : 0L;
        session.bankTrips = getTripCount();
        session.xpGained = getXpGained();
        session.tree = getTargetLabel();
        session.note = this.lastMode != null ? this.lastMode.name() + " · " + getStatus() : getStatus();
        if (this.barGoal != null && this.barGoal.isActive()) {
            session.barsSmelted = (int) Math.min(Integer.MAX_VALUE, this.barGoal.barsGathered());
            long gathered = 0L;
            for (int i = 0; i < this.barGoal.ingredientCount(); i++) {
                gathered += this.barGoal.minedOf(i);
            }
            session.itemsGathered = (int) Math.min(Integer.MAX_VALUE, gathered);
        } else {
            session.itemsGathered = 0;
            session.logsBanked = this.lastMode == MiningMode.BANK ? getTripCount() : 0;
            session.logsDropped = this.lastMode == MiningMode.DROP ? getTripCount() : 0;
        }
        OxSessionHistory.save(session);
    }

    public String getStatus() {
        String pause = this.pauseStatus;
        if (pause != null) {
            return pause;
        }
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getState() {
        return this.engine != null ? this.engine.getStateName() : "MINING";
    }

    public String getStateName() {
        return getState();
    }

    public int getTripCount() {
        return this.engine != null ? this.engine.getTripCount() : 0;
    }

    public int getXpGained() {
        return this.engine != null ? this.engine.getXpGained() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public BarGoal getBarGoal() {
        return this.barGoal;
    }

    public int getMiningLevel() {
        try {
            return this.client.getRealSkillLevel(Skill.MINING);
        } catch (Exception e) {
            return 1;
        }
    }

    public String getTargetLabel() {
        if (this.barGoal != null && this.barGoal.isActive()) {
            return this.barGoal.bar().toString();
        }
        if (this.lastOre != null) {
            return this.lastOre.toString();
        }
        return this.config != null ? this.config.ore().toString() : "—";
    }

    public MiningMode getActiveMode() {
        return this.lastMode != null ? this.lastMode : MiningMode.BANK;
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
