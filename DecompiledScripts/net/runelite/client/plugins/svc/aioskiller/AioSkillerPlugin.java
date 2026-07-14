/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.aioskiller;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConfig;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerOverlay;
import net.runelite.client.plugins.svc.aioskiller.EventBoss;
import net.runelite.client.plugins.svc.aioskiller.InputMode;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.MasterState;
import net.runelite.client.plugins.svc.aioskiller.OrderMode;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.SkillerLog;
import net.runelite.client.plugins.svc.aioskiller.jobs.AfkCombatJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.AfkSkillJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.AgilityJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.CombatJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.CraftingJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.EventBossJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.FarmingJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.HerbloreJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.HunterJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.MagicJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.MiningJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.SmithingJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.ThievingJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.WoodcuttingJob;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.RandomEventState;
import net.runelite.client.plugins.svc.core.ShieldLoginGate;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.client.managers.interaction.InteractMethod;
import net.unethicalite.client.managers.interaction.MouseBehavior;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 AIO Skiller</font></nobr></html>", description="Trains Combat, Agility, Hunter, Mining, Smithing, Thieving, Woodcutting, Crafting, Herblore and Farming to target levels. Optional 'AFK all skills' mode trains everything (plus Fishing and Ranged) via the server ::afk command.", tags={"orion", "aio", "skiller", "skilling", "bot", "automation"}, enabledByDefault=false)
public class AioSkillerPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AioSkillerConfig config;
    private AioSkillerOverlay overlay;
    private MasterContext ctx;
    private final Map<SkillId, SkillJob> jobs = new EnumMap<SkillId, SkillJob>(SkillId.class);
    private final Set<SkillId> completed = EnumSet.noneOf(SkillId.class);
    private final Random random = new Random();
    private EventBossJob eventJob;
    private volatile EventBoss pendingBoss;
    private int eventsAttended = 0;
    private int eventKills = 0;
    private int eventDeaths = 0;
    private SkillJob activeJob;
    private MasterState masterState = MasterState.SELECTING;
    private String status = "Starting";
    private boolean loggedInLastTick = false;
    private long sessionStartMs = 0L;
    private static final String UNETH_GROUP = "unethicalite";
    private String savedInteractMethod;
    private String savedNaturalMouse;
    private String savedMouseBehavior;
    private boolean inputModeApplied = false;
    private long activeJobDeadlineMs = 0L;
    private SkillId lastTimedOutSkill = null;
    private long nextBreakAtMs = 0L;
    private long breakUntilMs = 0L;
    private boolean breakPending = false;
    private static final long STUCK_MS = 300000L;
    private long xpBaseline = -1L;
    private SkillId wdSkill = null;
    private JobPhase wdPhase = null;
    private long wdXp = -1L;
    private long wdSinceMs = 0L;
    private boolean finalized = false;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(AioSkillerConfig.class).toInstance((Object)((AioSkillerConfig)configManager.getConfig(AioSkillerConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.sessionStartMs = System.currentTimeMillis();
        this.ctx = new MasterContext(this.client, this.config, this.sessionStartMs);
        this.jobs.clear();
        for (SkillId skillId : SkillId.values()) {
            SkillJob skillJob;
            if (!this.configEnabled(skillId) || this.config.afkAllSkills() && !this.afkCapable(skillId) || (skillJob = this.buildJob(skillId)) == null) continue;
            this.jobs.put(skillId, skillJob);
        }
        this.completed.clear();
        this.activeJob = null;
        this.activeJobDeadlineMs = 0L;
        this.lastTimedOutSkill = null;
        this.armNextBreak();
        this.eventJob = new EventBossJob(this.ctx);
        this.pendingBoss = null;
        this.eventsAttended = 0;
        this.eventKills = 0;
        this.eventDeaths = 0;
        this.ctx.reset.begin();
        this.masterState = MasterState.BANK_RESET;
        this.status = "Starting";
        this.loggedInLastTick = false;
        this.xpBaseline = -1L;
        this.wdSkill = null;
        this.wdPhase = null;
        this.wdXp = -1L;
        this.wdSinceMs = 0L;
        this.finalized = false;
        this.overlay = new AioSkillerOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.applyInputMode();
    }

    @Override
    protected void onStop() throws Exception {
        this.restoreInputMode();
        this.finalizeRun();
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
        }
        this.overlay = null;
    }

    private void applyInputMode() {
        try {
            if (!this.inputModeApplied) {
                this.savedInteractMethod = this.configManager.getConfiguration(UNETH_GROUP, "interactMethod");
                this.savedNaturalMouse = this.configManager.getConfiguration(UNETH_GROUP, "naturalMouse");
                this.savedMouseBehavior = this.configManager.getConfiguration(UNETH_GROUP, "mouseBehavior");
                this.inputModeApplied = true;
            }
            if (this.config.inputMode() == InputMode.MOUSE_MOVEMENTS) {
                this.configManager.setConfiguration(UNETH_GROUP, "interactMethod", (Object)InteractMethod.MOUSE_EVENTS);
                this.configManager.setConfiguration(UNETH_GROUP, "naturalMouse", (Object)true);
                this.configManager.setConfiguration(UNETH_GROUP, "mouseBehavior", (Object)MouseBehavior.CLICKBOXES);
            } else {
                this.configManager.setConfiguration(UNETH_GROUP, "interactMethod", (Object)InteractMethod.PACKETS);
            }
            if (this.ctx != null && this.ctx.input != null) {
                this.ctx.input.setPacketsOnly(this.config.inputMode() == InputMode.PACKETS_ONLY);
            }
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

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
        }
        if (this.activeJob != null) {
            this.activeJob.onGameState(gameState);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        EventBoss eventBoss;
        String string = chatMessage.getMessage();
        if (string == null) {
            return;
        }
        String string2 = string.replaceAll("<[^>]*>", "").toLowerCase();
        if (this.config.eventWatcherEnabled() && this.pendingBoss == null && this.masterState != MasterState.EVENT_BOSS && (eventBoss = EventBoss.detect(string2)) != null && this.watchEnabled(eventBoss)) {
            this.pendingBoss = eventBoss;
        }
        if (this.activeJob != null) {
            this.activeJob.onChat(string2);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (configChanged == null || !"orionAioSkiller".equals(configChanged.getGroup())) {
            return;
        }
        if ("inputMode".equals(configChanged.getKey())) {
            this.applyInputMode();
            return;
        }
        if (!"true".equals(configChanged.getNewValue())) {
            return;
        }
        if ("captureEventGear".equals(configChanged.getKey())) {
            this.captureWornGearInto("eventGearIds");
            this.configManager.setConfiguration("orionAioSkiller", "captureEventGear", (Object)false);
        } else if ("captureCombatGear".equals(configChanged.getKey())) {
            this.captureWornGearInto("combatGearIds");
            this.configManager.setConfiguration("orionAioSkiller", "captureCombatGear", (Object)false);
        }
    }

    private void captureWornGearInto(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (Item item : Equipment.getAll()) {
                if (item == null || item.getId() <= 0) continue;
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(item.getId());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.configManager.setConfiguration("orionAioSkiller", string, stringBuilder.toString());
    }

    private boolean watchEnabled(EventBoss eventBoss) {
        switch (eventBoss) {
            case VOTE_BOSS: {
                return this.config.watchVoteBoss();
            }
            case WORLD_BOSS: {
                return this.config.watchWorldBoss();
            }
        }
        return false;
    }

    @Override
    protected int loop() {
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
        this.ctx.local = player;
        if (this.xpBaseline < 0L) {
            this.xpBaseline = this.totalEnabledXp();
        }
        if (RandomEventState.isActive()) {
            this.status = "Paused \u2014 Random Event Guard handling " + RandomEventState.label();
            return 600;
        }
        if (this.pendingBoss != null && this.masterState != MasterState.EVENT_BOSS) {
            EventBoss eventBoss = this.pendingBoss;
            this.pendingBoss = null;
            this.eventJob.begin(eventBoss);
            ++this.eventsAttended;
            this.masterState = MasterState.EVENT_BOSS;
            this.status = eventBoss.label() + " spawned \u2014 interrupting to fight";
            return 300;
        }
        switch (this.masterState) {
            case BANK_RESET: {
                int n = this.ctx.reset.step(player);
                this.status = "Between skills \u2014 " + this.ctx.reset.status();
                if (n == Integer.MIN_VALUE) {
                    this.activeJob = null;
                    if (this.breakPending) {
                        this.startBreak();
                        return 600;
                    }
                    this.masterState = MasterState.SELECTING;
                    return 300;
                }
                return n;
            }
            case SELECTING: {
                SkillId skillId = this.pickNextSkill();
                if (skillId == null) {
                    this.status = "All enabled skills complete";
                    this.masterState = MasterState.ALL_DONE;
                    this.finalizeRun();
                    return -1;
                }
                this.activeJob = this.jobs.get((Object)skillId);
                this.activeJob.reset();
                this.rollSkillTimer();
                this.status = "Selected " + skillId.displayName();
                if (!this.activeJob.requiresEquipment() && !this.ctx.gear.isEquipmentEmpty()) {
                    this.ctx.reset.beginStrip();
                    this.masterState = MasterState.STRIP_GEAR;
                } else {
                    this.masterState = MasterState.RUNNING_JOB;
                }
                return 300;
            }
            case STRIP_GEAR: {
                if (this.activeJob == null) {
                    this.masterState = MasterState.SELECTING;
                    return 300;
                }
                int n = this.ctx.reset.step(player);
                this.status = this.activeJob.id().displayName() + " \u2014 stowing equipment: " + this.ctx.reset.status();
                if (n == Integer.MIN_VALUE) {
                    this.masterState = MasterState.RUNNING_JOB;
                    return 300;
                }
                return n;
            }
            case RUNNING_JOB: {
                int n;
                if (this.activeJob == null) {
                    this.masterState = MasterState.SELECTING;
                    return 300;
                }
                if (this.activeJob.isComplete()) {
                    this.completed.add(this.activeJob.id());
                    this.status = this.activeJob.id().displayName() + " complete";
                    this.ctx.reset.begin();
                    this.masterState = MasterState.BANK_RESET;
                    return 300;
                }
                if (this.config.breaksEnabled()) {
                    if (this.nextBreakAtMs == 0L) {
                        this.armNextBreak();
                    }
                    if (this.nextBreakAtMs > 0L && System.currentTimeMillis() >= this.nextBreakAtMs) {
                        this.breakPending = true;
                        this.status = this.activeJob.id().displayName() + " \u2014 banking for a break";
                        this.ctx.reset.begin();
                        this.masterState = MasterState.BANK_RESET;
                        return 300;
                    }
                } else {
                    this.nextBreakAtMs = 0L;
                }
                if (this.activeJobDeadlineMs > 0L && System.currentTimeMillis() >= this.activeJobDeadlineMs) {
                    this.lastTimedOutSkill = this.activeJob.id();
                    this.status = this.activeJob.id().displayName() + " \u2014 time slice ended, rotating";
                    this.activeJobDeadlineMs = 0L;
                    this.ctx.reset.begin();
                    this.masterState = MasterState.BANK_RESET;
                    return 300;
                }
                try {
                    n = this.activeJob.tick();
                }
                catch (Throwable throwable) {
                    this.ctx.log.record(SkillerLog.Kind.EXCEPTION, this.activeJob.id().displayName(), this.activeJob.phase().name(), AioSkillerPlugin.exceptionLine(throwable), AioSkillerPlugin.exceptionDetail(throwable));
                    this.status = this.activeJob.id().displayName() + " \u2014 error: " + throwable.getClass().getSimpleName();
                    return 1200;
                }
                this.status = this.activeJob.id().displayName() + " \u2014 " + this.activeJob.status();
                this.runWatchdog(this.activeJob);
                if (n == Integer.MIN_VALUE || n < 0) {
                    return 600;
                }
                return n;
            }
            case EVENT_BOSS: {
                int n;
                this.pendingBoss = null;
                if (this.eventJob.isFinished()) {
                    if (this.eventJob.outcome() == EventBossJob.Outcome.KILLED) {
                        ++this.eventKills;
                    }
                    this.eventDeaths += this.eventJob.deaths();
                    this.status = "Event done (" + this.eventJob.outcomeLabel() + ") \u2014 resuming skilling";
                    this.ctx.reset.begin();
                    this.masterState = MasterState.BANK_RESET;
                    return 300;
                }
                try {
                    n = this.eventJob.tick();
                }
                catch (Throwable throwable) {
                    this.ctx.log.record(SkillerLog.Kind.EXCEPTION, "Event " + this.eventJob.boss().label(), this.eventJob.phase().name(), AioSkillerPlugin.exceptionLine(throwable), AioSkillerPlugin.exceptionDetail(throwable));
                    this.status = "Event error: " + throwable.getClass().getSimpleName() + " \u2014 resuming skilling";
                    this.ctx.reset.begin();
                    this.masterState = MasterState.BANK_RESET;
                    return 1200;
                }
                this.status = "Event: " + this.eventJob.status();
                if (n == Integer.MIN_VALUE || n < 0) {
                    return 600;
                }
                return n;
            }
            case ON_BREAK: {
                long l = this.breakUntilMs - System.currentTimeMillis();
                if (l <= 0L) {
                    this.status = "Break over \u2014 resuming";
                    this.armNextBreak();
                    this.ctx.reset.begin();
                    this.masterState = MasterState.BANK_RESET;
                    return 300;
                }
                if (this.config.breakLogout()) {
                    ShieldLoginGate.blockFor(l);
                    try {
                        if (Game.isLoggedIn()) {
                            Game.logout();
                        }
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    this.status = "On break \u2014 " + AioSkillerPlugin.formatDuration(l) + " left (logging out)";
                    return 2000;
                }
                this.status = "On break \u2014 " + AioSkillerPlugin.formatDuration(l) + " left (idling)";
                return (int)Math.min(Math.max(l, 500L), 5000L);
            }
            case FINALIZING: {
                this.ctx.reset.begin();
                this.masterState = MasterState.BANK_RESET;
                return 300;
            }
        }
        return -1;
    }

    @Override
    protected long jitterDelay(long l) {
        if (this.ctx == null || this.ctx.profile == null) {
            return l;
        }
        long l2 = this.ctx.profile.jitterDelay(l);
        long l3 = this.ctx.input == null ? 0L : this.ctx.input.takeLastMoveMs();
        return Math.max(1L, l2 - l3);
    }

    private SkillId pickNextSkill() {
        Object object;
        ArrayList<SkillId> arrayList = new ArrayList<SkillId>();
        for (SkillId skillId : SkillId.values()) {
            if (this.completed.contains((Object)skillId) || !this.enabledFor(skillId) || (object = this.jobs.get((Object)skillId)) == null) continue;
            if (object.currentLevel() >= object.targetLevel()) {
                this.completed.add(skillId);
                continue;
            }
            arrayList.add(skillId);
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        if (!this.useAfk(SkillId.MINING) && !this.useAfk(SkillId.SMITHING) && arrayList.contains((Object)SkillId.MINING) && arrayList.contains((Object)SkillId.SMITHING)) {
            arrayList.remove((Object)SkillId.SMITHING);
        }
        if (!this.useAfk(SkillId.COMBAT) && arrayList.contains((Object)SkillId.COMBAT) && arrayList.contains((Object)SkillId.CRAFTING)) {
            arrayList.remove((Object)SkillId.CRAFTING);
        }
        if (this.config.timeLimitEnabled() && this.lastTimedOutSkill != null && arrayList.size() > 1 && arrayList.contains((Object)this.lastTimedOutSkill)) {
            arrayList.remove((Object)this.lastTimedOutSkill);
        }
        this.lastTimedOutSkill = null;
        if (this.config.orderMode() == OrderMode.FIXED) {
            for (String string : this.config.fixedOrder().split(",")) {
                object = SkillId.fromKey(string);
                if (object == null || !arrayList.contains(object)) continue;
                return object;
            }
            return (SkillId)((Object)arrayList.get(0));
        }
        return (SkillId)((Object)arrayList.get(this.random.nextInt(arrayList.size())));
    }

    private boolean enabledFor(SkillId skillId) {
        if (!this.configEnabled(skillId)) {
            return false;
        }
        return !this.config.afkAllSkills() || this.afkCapable(skillId);
    }

    private boolean configEnabled(SkillId skillId) {
        switch (skillId) {
            case AGILITY: {
                return this.config.agilityEnabled();
            }
            case HUNTER: {
                return this.config.hunterEnabled();
            }
            case MINING: {
                return this.config.miningEnabled();
            }
            case SMITHING: {
                return this.config.smithingEnabled();
            }
            case THIEVING: {
                return this.config.thievingEnabled();
            }
            case WOODCUTTING: {
                return this.config.woodcuttingEnabled();
            }
            case CRAFTING: {
                return this.config.craftingEnabled();
            }
            case HERBLORE: {
                return this.config.herbloreEnabled();
            }
            case FARMING: {
                return this.config.farmingEnabled();
            }
            case COMBAT: {
                return this.config.combatEnabled();
            }
            case MAGIC: {
                return this.config.magicEnabled();
            }
            case FISHING: {
                return this.config.fishingEnabled();
            }
            case RANGED: {
                return this.config.rangedEnabled();
            }
        }
        return false;
    }

    private boolean afkCapable(SkillId skillId) {
        switch (skillId) {
            case AGILITY: 
            case THIEVING: 
            case WOODCUTTING: 
            case CRAFTING: {
                return false;
            }
        }
        return true;
    }

    private boolean useAfk(SkillId skillId) {
        switch (skillId) {
            case HERBLORE: 
            case FARMING: 
            case FISHING: 
            case RANGED: {
                return true;
            }
            case COMBAT: {
                return this.config.afkAllSkills() || this.config.combatAfk();
            }
            case MAGIC: {
                return this.config.afkAllSkills() || this.config.magicAfk();
            }
            case MINING: {
                return this.config.afkAllSkills() || this.config.miningAfk();
            }
            case SMITHING: {
                return this.config.afkAllSkills() || this.config.smithingAfk();
            }
            case HUNTER: {
                return this.config.afkAllSkills() || this.config.hunterAfk();
            }
        }
        return false;
    }

    private SkillJob buildJob(SkillId skillId) {
        switch (skillId) {
            case AGILITY: {
                return new AgilityJob(this.ctx);
            }
            case THIEVING: {
                return new ThievingJob(this.ctx);
            }
            case WOODCUTTING: {
                return new WoodcuttingJob(this.ctx);
            }
            case CRAFTING: {
                return new CraftingJob(this.ctx);
            }
            case HERBLORE: {
                return new HerbloreJob(this.ctx);
            }
            case FARMING: {
                return new FarmingJob(this.ctx);
            }
            case FISHING: {
                return new AfkSkillJob(this.ctx, SkillId.FISHING);
            }
            case RANGED: {
                return new AfkSkillJob(this.ctx, SkillId.RANGED);
            }
            case COMBAT: {
                return this.useAfk(skillId) ? new AfkCombatJob(this.ctx) : new CombatJob(this.ctx);
            }
            case MAGIC: {
                return this.useAfk(skillId) ? new AfkSkillJob(this.ctx, SkillId.MAGIC) : new MagicJob(this.ctx);
            }
            case MINING: {
                return this.useAfk(skillId) ? new AfkSkillJob(this.ctx, SkillId.MINING) : new MiningJob(this.ctx);
            }
            case SMITHING: {
                return this.useAfk(skillId) ? new AfkSkillJob(this.ctx, SkillId.SMITHING) : new SmithingJob(this.ctx);
            }
            case HUNTER: {
                return this.useAfk(skillId) ? new AfkSkillJob(this.ctx, SkillId.HUNTER) : new HunterJob(this.ctx);
            }
        }
        return null;
    }

    private void rollSkillTimer() {
        if (!this.config.timeLimitEnabled()) {
            this.activeJobDeadlineMs = 0L;
            return;
        }
        int n = Math.max(1, this.config.timeLimitMinMinutes());
        int n2 = Math.max(n, this.config.timeLimitMaxMinutes());
        int n3 = n2 > n ? n + this.random.nextInt(n2 - n + 1) : n;
        this.activeJobDeadlineMs = System.currentTimeMillis() + (long)n3 * 60000L;
    }

    private void armNextBreak() {
        this.breakPending = false;
        this.breakUntilMs = 0L;
        if (!this.config.breaksEnabled()) {
            this.nextBreakAtMs = 0L;
            return;
        }
        int n = Math.max(1, this.config.breakEveryMinMinutes());
        int n2 = Math.max(n, this.config.breakEveryMaxMinutes());
        int n3 = n2 > n ? n + this.random.nextInt(n2 - n + 1) : n;
        this.nextBreakAtMs = System.currentTimeMillis() + (long)n3 * 60000L;
    }

    private void startBreak() {
        this.breakPending = false;
        int n = Math.max(1, this.config.breakForMinMinutes());
        int n2 = Math.max(n, this.config.breakForMaxMinutes());
        int n3 = n2 > n ? n + this.random.nextInt(n2 - n + 1) : n;
        long l = (long)n3 * 60000L;
        this.breakUntilMs = System.currentTimeMillis() + l;
        this.masterState = MasterState.ON_BREAK;
        if (this.config.breakLogout()) {
            ShieldLoginGate.blockFor(l);
            try {
                Game.logout();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.status = "On break " + n3 + (n3 == 1 ? " min" : " mins") + " \u2014 logging out (Auto Login will relog)";
        } else {
            this.status = "On break " + n3 + (n3 == 1 ? " min" : " mins") + " \u2014 idling in-world";
        }
    }

    private static String formatDuration(long l) {
        if (l < 0L) {
            l = 0L;
        }
        long l2 = l / 1000L;
        long l3 = l2 / 60L;
        long l4 = l2 % 60L;
        return l3 > 0L ? l3 + "m " + l4 + "s" : l4 + "s";
    }

    private void runWatchdog(SkillJob skillJob) {
        SkillId skillId = skillJob.id();
        JobPhase jobPhase = skillJob.phase();
        long l = this.skillXp(skillId);
        long l2 = System.currentTimeMillis();
        if (skillJob.watchdogSuppressed()) {
            this.wdSkill = skillId;
            this.wdPhase = jobPhase;
            this.wdXp = l;
            this.wdSinceMs = l2;
            return;
        }
        if (this.wdSkill != skillId || this.wdPhase != jobPhase) {
            this.wdSkill = skillId;
            this.wdPhase = jobPhase;
            this.wdXp = l;
            this.wdSinceMs = l2;
            return;
        }
        if (l > this.wdXp) {
            this.wdXp = l;
            this.wdSinceMs = l2;
            return;
        }
        if (l2 - this.wdSinceMs >= 300000L) {
            this.ctx.log.record(SkillerLog.Kind.NO_PROGRESS, skillId.displayName(), jobPhase.name(), "No XP gained for 5+ minutes while training this skill.", "Last status: \"" + skillJob.status() + "\"");
            this.wdSinceMs = l2;
        }
    }

    private void finalizeRun() {
        if (this.finalized) {
            return;
        }
        this.finalized = true;
        if (this.ctx != null) {
            this.ctx.log.finish(this.config.openReportOnProblem());
        }
    }

    private long skillXp(SkillId skillId) {
        try {
            if (skillId == SkillId.COMBAT) {
                return (long)Skills.getExperience(Skill.ATTACK) + (long)Skills.getExperience(Skill.STRENGTH) + (long)Skills.getExperience(Skill.DEFENCE) + (long)Skills.getExperience(Skill.HITPOINTS);
            }
            return Skills.getExperience(skillId.rlSkill());
        }
        catch (Throwable throwable) {
            return 0L;
        }
    }

    private long totalEnabledXp() {
        long l = 0L;
        for (SkillId skillId : SkillId.values()) {
            if (!this.enabledFor(skillId)) continue;
            l += this.skillXp(skillId);
        }
        return l;
    }

    private static String exceptionLine(Throwable throwable) {
        String string = throwable.getMessage();
        return throwable.getClass().getSimpleName() + (String)(string != null && !string.isEmpty() ? ": " + string : "");
    }

    private static String exceptionDetail(Throwable throwable) {
        StackTraceElement[] stackTraceElementArray = throwable.getStackTrace();
        String string = throwable.getMessage();
        String string2 = stackTraceElementArray != null && stackTraceElementArray.length > 0 ? stackTraceElementArray[0].toString() : "no stack frame";
        String string3 = null;
        if (stackTraceElementArray != null) {
            for (StackTraceElement stackTraceElement : stackTraceElementArray) {
                if (!stackTraceElement.getClassName().startsWith("net.runelite.client.plugins.svc")) continue;
                string3 = stackTraceElement.toString();
                break;
            }
        }
        String string4 = string3 != null && !string3.equals(string2) ? string2 + "  (our code: " + string3 + ")" : string2;
        return throwable.getClass().getName() + (String)(string != null ? ": " + string : "") + "  @ " + string4;
    }

    public String getStatus() {
        return this.status;
    }

    public String getMasterState() {
        return this.masterState.name();
    }

    public MasterState masterStateEnum() {
        return this.masterState;
    }

    public InputMode inputMode() {
        return this.config.inputMode();
    }

    public String getActiveSkill() {
        return this.activeJob == null ? "\u2014" : this.activeJob.id().displayName();
    }

    public SkillId activeSkillId() {
        return this.activeJob == null ? null : this.activeJob.id();
    }

    public JobPhase activeJobPhase() {
        return this.activeJob == null ? null : this.activeJob.phase();
    }

    public String getActivePhase() {
        return this.activeJob == null ? "\u2014" : this.activeJob.phase().name();
    }

    public int getCompletedCount() {
        return this.completed.size();
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public boolean eventWatcherOn() {
        return this.config.eventWatcherEnabled();
    }

    public int getEventsAttended() {
        return this.eventsAttended;
    }

    public int getEventKills() {
        return this.eventKills;
    }

    public int getEventDeaths() {
        return this.eventDeaths;
    }

    public boolean telemetryActive() {
        return this.ctx != null && this.ctx.input != null;
    }

    public long telemetryMoves() {
        return this.ctx == null || this.ctx.input == null ? 0L : this.ctx.input.moveCount();
    }

    public long telemetryHits() {
        return this.ctx == null || this.ctx.input == null ? 0L : this.ctx.input.humanizedCount();
    }

    public long telemetryFallbacks() {
        return this.ctx == null || this.ctx.input == null ? 0L : this.ctx.input.fallbackCount();
    }

    public double accountTempo() {
        return this.ctx == null || this.ctx.profile == null ? 0.0 : this.ctx.profile.tempo();
    }

    public String telemetryLast() {
        if (this.ctx == null || this.ctx.input == null || this.ctx.input.lastTargetX() < 0) {
            return "\u2014";
        }
        return "(" + this.ctx.input.lastTargetX() + "," + this.ctx.input.lastTargetY() + ") d=" + this.ctx.input.lastMoveDist();
    }

    public long telemetryLastAgeMs() {
        if (this.ctx == null || this.ctx.input == null || this.ctx.input.lastActionAtMs() <= 0L) {
            return -1L;
        }
        return System.currentTimeMillis() - this.ctx.input.lastActionAtMs();
    }

    public int getEnabledCount() {
        int n = 0;
        for (SkillId skillId : SkillId.values()) {
            if (!this.enabledFor(skillId)) continue;
            ++n;
        }
        return n;
    }

    public boolean hasErrors() {
        return this.ctx != null && this.ctx.log.hasErrors();
    }

    public int getErrorCount() {
        return this.ctx == null ? 0 : this.ctx.log.errorCount();
    }

    public String getLastError() {
        return this.ctx == null ? "" : this.ctx.log.lastSummary();
    }

    public long getSessionXpGained() {
        return this.ctx == null || this.xpBaseline < 0L ? 0L : Math.max(0L, this.totalEnabledXp() - this.xpBaseline);
    }

    public double getXpPerHour() {
        long l = this.getSessionXpGained();
        double d = this.sessionStartMs <= 0L ? 0.0 : (double)(System.currentTimeMillis() - this.sessionStartMs) / 3600000.0;
        return d > 0.001 ? (double)l / d : 0.0;
    }

    public List<SkillRow> skillRows() {
        ArrayList<SkillRow> arrayList = new ArrayList<SkillRow>();
        for (SkillId skillId : SkillId.values()) {
            SkillJob skillJob;
            if (!this.enabledFor(skillId) || (skillJob = this.jobs.get((Object)skillId)) == null) continue;
            arrayList.add(new SkillRow(skillId, skillJob.currentLevel(), skillJob.targetLevel(), this.completed.contains((Object)skillId), this.activeJob == skillJob));
        }
        return arrayList;
    }

    public static final class SkillRow {
        public final SkillId id;
        public final int level;
        public final int target;
        public final boolean done;
        public final boolean active;

        SkillRow(SkillId skillId, int n, int n2, boolean bl, boolean bl2) {
            this.id = skillId;
            this.level = n;
            this.target = n2;
            this.done = bl;
            this.active = bl2;
        }
    }
}

