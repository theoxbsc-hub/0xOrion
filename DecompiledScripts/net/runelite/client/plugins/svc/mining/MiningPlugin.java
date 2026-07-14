/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.mining;

import com.google.inject.Binder;
import java.util.Random;
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
import net.runelite.client.plugins.svc.core.AccountProfile;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.RandomEventState;
import net.runelite.client.plugins.svc.mining.BarGoal;
import net.runelite.client.plugins.svc.mining.MiningConfig;
import net.runelite.client.plugins.svc.mining.MiningEngine;
import net.runelite.client.plugins.svc.mining.MiningMode;
import net.runelite.client.plugins.svc.mining.MiningOre;
import net.runelite.client.plugins.svc.mining.MiningOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.game.Skills;
import net.unethicalite.client.managers.interaction.InteractMethod;
import net.unethicalite.client.managers.interaction.MouseBehavior;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 Mining Bot</font></nobr></html>", description="Mines a selectable ore \u2014 or gathers the exact ores for a chosen bar \u2014 and banks (or power-drops) when full.", tags={"orion", "mining", "bot", "skilling", "automation"}, enabledByDefault=false)
public class MiningPlugin
extends OrionLoopedPlugin {
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
    private AccountProfile profile;
    private HumanInput input;
    private BarGoal barGoal;
    private long sessionStartMs = 0L;
    private boolean loggedInLastTick = false;
    private final Random breakRng = new Random();
    private long nextBreakAtMs = 0L;
    private long breakUntilMs = 0L;
    private volatile String pauseStatus = null;
    private static final String UNETH_GROUP = "unethicalite";
    private String savedInteractMethod;
    private String savedNaturalMouse;
    private String savedMouseBehavior;
    private boolean inputModeApplied = false;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(MiningConfig.class).toInstance((Object)((MiningConfig)configManager.getConfig(MiningConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.profile = new AccountProfile(this.client);
        this.input = new HumanInput(this.client, this.profile);
        this.engine = new MiningEngine(this.client, this.input);
        this.barGoal = new BarGoal();
        this.overlay = new MiningOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
        this.nextBreakAtMs = 0L;
        this.breakUntilMs = 0L;
        this.pauseStatus = null;
        this.applyInputMode();
    }

    @Override
    protected void onStop() throws Exception {
        this.restoreInputMode();
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (this.engine == null) {
            return;
        }
        String string = chatMessage.getMessage();
        if (string == null) {
            return;
        }
        String string2 = string.replaceAll("<[^>]*>", "").toLowerCase();
        this.engine.onChatMessage(string2);
    }

    @Override
    protected int loop() {
        int n;
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (RandomEventState.isActive()) {
            this.pauseStatus = "Paused \u2014 " + RandomEventState.label();
            return 1500;
        }
        this.barGoal.sync(this.config.barType(), this.config.barAmount());
        this.barGoal.trackInventory();
        MiningOre miningOre = this.config.ore();
        MiningMode miningMode = this.config.miningMode();
        boolean bl = false;
        if (this.barGoal.isActive()) {
            miningMode = MiningMode.BANK;
            MiningOre miningOre2 = this.barGoal.decide(this.engine.getTripCount(), this.miningLevel(), this.config.stopWhenDone());
            if (miningOre2 == null) {
                bl = true;
            } else {
                miningOre = miningOre2;
            }
        }
        if ((n = this.maybeBreak(bl)) > 0) {
            return n;
        }
        this.pauseStatus = null;
        return this.engine.tick(player, new MiningEngine.Settings(miningOre, miningMode, this.config.returnToMine(), this.config.keepItems(), this.config.matchRockByName(), this.config.rockNameOverride(), this.config.avoidOtherPlayers(), bl));
    }

    @Override
    protected long jitterDelay(long l) {
        if (this.profile == null) {
            return l;
        }
        long l2 = this.profile.jitterDelay(l);
        long l3 = this.input == null ? 0L : this.input.takeLastMoveMs();
        return Math.max(1L, l2 - l3);
    }

    private int maybeBreak(boolean bl) {
        if (!this.config.microBreaks()) {
            this.breakUntilMs = 0L;
            return 0;
        }
        long l = System.currentTimeMillis();
        if (this.breakUntilMs > 0L) {
            if (l < this.breakUntilMs) {
                this.pauseStatus = "Short break \u2014 " + (this.breakUntilMs - l) / 1000L + "s left";
                return 1000 + this.breakRng.nextInt(600);
            }
            this.breakUntilMs = 0L;
            this.scheduleNextBreak(l);
            return 0;
        }
        if (this.nextBreakAtMs == 0L) {
            this.scheduleNextBreak(l);
            return 0;
        }
        if (l >= this.nextBreakAtMs && !bl && "MINING".equals(this.engine.getStateName())) {
            long l2 = this.breakRng.nextDouble() < 0.8 ? 15000L + (long)this.breakRng.nextInt(60000) : 90000L + (long)this.breakRng.nextInt(150000);
            this.breakUntilMs = l + l2;
            this.pauseStatus = "Short break \u2014 " + l2 / 1000L + "s";
            return 1200;
        }
        return 0;
    }

    private void scheduleNextBreak(long l) {
        this.nextBreakAtMs = l + 420000L + (long)this.breakRng.nextInt(840000);
    }

    private int miningLevel() {
        int n = Skills.getBoostedLevel(Skill.MINING);
        return n > 0 ? n : Skills.getLevel(Skill.MINING);
    }

    private void applyInputMode() {
        try {
            if (!this.inputModeApplied) {
                this.savedInteractMethod = this.configManager.getConfiguration(UNETH_GROUP, "interactMethod");
                this.savedNaturalMouse = this.configManager.getConfiguration(UNETH_GROUP, "naturalMouse");
                this.savedMouseBehavior = this.configManager.getConfiguration(UNETH_GROUP, "mouseBehavior");
                this.inputModeApplied = true;
            }
            this.configManager.setConfiguration(UNETH_GROUP, "interactMethod", (Object)InteractMethod.MOUSE_EVENTS);
            this.configManager.setConfiguration(UNETH_GROUP, "naturalMouse", (Object)true);
            this.configManager.setConfiguration(UNETH_GROUP, "mouseBehavior", (Object)MouseBehavior.CLICKBOXES);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void restoreInputMode() {
        if (!this.inputModeApplied) {
            return;
        }
        this.restoreUneth("interactMethod", this.savedInteractMethod);
        this.restoreUneth("naturalMouse", this.savedNaturalMouse);
        this.restoreUneth("mouseBehavior", this.savedMouseBehavior);
        this.inputModeApplied = false;
    }

    private void restoreUneth(String string, String string2) {
        try {
            if (string2 == null) {
                this.configManager.unsetConfiguration(UNETH_GROUP, string);
            } else {
                this.configManager.setConfiguration(UNETH_GROUP, string, string2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public String getStatus() {
        String string = this.pauseStatus;
        if (string != null) {
            return string;
        }
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getStateName() {
        return this.engine != null ? this.engine.getStateName() : "MINING";
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
}

