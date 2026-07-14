/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.CollisionData
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.GameState
 *  net.runelite.api.GroundObject
 *  net.runelite.api.HeadIcon
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.ObjectComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Projectile
 *  net.runelite.api.Skill
 *  net.runelite.api.Tile
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.AnimationChanged
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.ProjectileMoved
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.corruptedgauntlet;

import com.google.inject.Binder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.HeadIcon;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.corruptedgauntlet.CorruptedGauntletConfig;
import net.runelite.client.plugins.svc.corruptedgauntlet.CorruptedGauntletConstants;
import net.runelite.client.plugins.svc.corruptedgauntlet.CorruptedGauntletOverlay;
import net.runelite.client.plugins.svc.corruptedgauntlet.HumanizationProfile;
import net.runelite.client.plugins.svc.corruptedgauntlet.HunllefPhase;
import net.runelite.client.plugins.svc.corruptedgauntlet.HunllefStyle;
import net.runelite.client.plugins.svc.corruptedgauntlet.HunllefWeapon;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Prayers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Corr. Gauntlet</font></nobr></html>", description="Corrupted Hunllef fight: prayer flicking, weapon switching, tornado kiting, supplies", tags={"orion", "gauntlet", "corrupted", "hunllef", "boss", "combat", "bot"}, enabledByDefault=false)
public class CorruptedGauntletPlugin
extends OrionLoopedPlugin {
    private static final Logger log = LoggerFactory.getLogger(CorruptedGauntletPlugin.class);
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CorruptedGauntletConfig config;
    private CorruptedGauntletOverlay overlay;
    private HunllefPhase phase = HunllefPhase.IDLE;
    private String status = "Idle";
    private int killCount = 0;
    private int deathCount = 0;
    private long startTime = 0L;
    private boolean loggedInLastTick = false;
    private long fightStartMs = 0L;
    private long bestBossMs = 0L;
    private long worstBossMs = 0L;
    private long totalBossMs = 0L;
    private int bossTimedCount = 0;
    private boolean runResolved = false;
    private boolean sawBossLowHp = false;
    private boolean lootedRewardChest = false;
    private int lootClickLoops = 0;
    private int noBankLoops = 0;
    private int rewardLootBounces = 0;
    private BankStep bankStep = BankStep.PRESET;
    private volatile boolean presetLoadConfirmed = false;
    private volatile boolean lockedPresetDetected = false;
    private long bankVerifyAt = 0L;
    private long bankOpenAt = 0L;
    private HunllefStyle currentStyle = HunllefStyle.RANGED;
    private Prayer prayerOverride = null;
    private int prayerOverrideUntilTick = -1;
    private HunllefStyle lastSeenBossStyle = HunllefStyle.RANGED;
    private int bossAttacksInStyle = 0;
    private HunllefStyle predictedNextStyle = HunllefStyle.RANGED;
    private int lastBossProjTick = -100;
    private Prayer activeOverheadPrayer = null;
    private Prayer activeOffensivePrayer = null;
    private boolean prayerWasDisabled = false;
    private int lastOverheadToggleTick = -1;
    private int lastOffensiveToggleTick = -1;
    private Prayer lastDesiredOverhead = null;
    private boolean skipOverheadThisWindow = false;
    private int playerAttackCount = 0;
    private int lastPlayerAnim = -1;
    private int lastPlayerAttackTick = -1;
    private int lastBossTargetTick = -1;
    private int lastAteTick = -1;
    private int weaponCycleIndex = 0;
    private HunllefWeapon currentWeapon = HunllefWeapon.RANGED;
    private long lastWeaponSwapMs = 0L;
    private int swapTick = -1;
    private int equipAttempts = 0;
    private boolean lowWeaponWarning = false;
    private int usableWeaponCount = 0;
    private String lastSwapReason = "-";
    private int lastEngageTick = -1;
    private int swapPendingSinceTick = -1;
    private int swapReactionDelay = 0;
    private int lastHunllefId = -1;
    private int lastHunllefAnim = -1;
    private int bossMissingTicks = 0;
    private int bossHpPercent = 100;
    private int lastBossHpDropTick = -1;
    private int lastBossHpRatio = -1;
    private boolean bossHpKnown = false;
    private int stallSinceDropAttacks = 0;
    private HunllefWeapon lastProtectedStyle = null;
    private HunllefWeapon overheadRaw = null;
    private int overheadStableTicks = 0;
    private List<NPC> tornadoes = Collections.emptyList();
    private WorldPoint lastDodgeTile = null;
    private boolean tornadoEncounterActive = false;
    private boolean tornadoEncounterFlee = false;
    private final Set<Integer> ambientNpcIndices = new HashSet<Integer>();
    private int lastBossAttackTick = -100;
    private int nextExpectedAttackTick = -100;
    private final Deque<WorldPoint> recentTargetedTiles = new ArrayDeque<WorldPoint>();
    private final Deque<int[]> recentShotLaunches = new ArrayDeque<int[]>();
    private int orbitDir = 1;
    private int lastMoveTick = -1;
    private int movesDone = 0;
    private WorldPoint moveIntendedPos = null;
    private WorldPoint lastStepFrom = null;
    private boolean lavaUnderPlayer = false;
    private int hazardTileCount = 0;
    private boolean onHazardTile = false;
    private String lastAction = "-";
    private String nearbyNpcDebug = "";
    private int cachedHumanLevel = -1;
    private HumanizationProfile cachedProfile = HumanizationProfile.of(4);
    private static final int COST_HOT_LAVA = 10000;
    private static final int COST_WARN_LAVA = 6000;
    private static final int COST_TORNADO_ON = 5500;
    private static final int COST_TORNADO_NEAR = 1800;
    private static final int COST_ADJ_HOT_LAVA = 1200;
    private static final int COST_ADJ_LAVA = 400;
    private static final int COST_SHOT = 1500;
    private static final int[][] DIRS8 = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private void diag(String string) {
        this.lastAction = string;
    }

    private HumanizationProfile hp() {
        int n = this.config.humanizationLevel();
        if (n != this.cachedHumanLevel) {
            this.cachedHumanLevel = n;
            this.cachedProfile = HumanizationProfile.of(n);
        }
        return this.cachedProfile;
    }

    private int sampleReactionTicks() {
        int n;
        if (!this.config.realismEnabled()) {
            return 0;
        }
        int n2 = Math.max(0, this.hp().reactionMinTicks);
        return n2 == (n = Math.max(n2, this.hp().reactionMaxTicks)) ? n2 : Rand.nextInt(n2, n);
    }

    private int jitter(int n) {
        int n2 = this.config.realismEnabled() ? this.hp().jitterPercent : 0;
        int n3 = n * n2 / 100;
        return n3 <= 0 ? n : n + Rand.nextInt(-n3, n3);
    }

    private int tornadoKeepDistance() {
        return Math.max(1, this.hp().tornadoKeepDistance);
    }

    private boolean updateTornadoEncounter(boolean bl) {
        if (!bl) {
            this.tornadoEncounterActive = false;
            this.tornadoEncounterFlee = false;
            return false;
        }
        if (!this.config.realismEnabled()) {
            this.tornadoEncounterFlee = false;
            return false;
        }
        if (!this.tornadoEncounterActive) {
            this.tornadoEncounterActive = true;
            switch (this.hp().tornadoBehaviour) {
                case ATTACK_AND_EVADE: {
                    this.tornadoEncounterFlee = false;
                    break;
                }
                case EVADE_ONLY: {
                    this.tornadoEncounterFlee = true;
                    break;
                }
                default: {
                    int n = Math.max(0, Math.min(100, this.hp().tornadoEvadeChance));
                    this.tornadoEncounterFlee = n > 0 && Rand.nextInt(0, 99) < n;
                }
            }
        }
        return this.tornadoEncounterFlee;
    }

    private boolean inEvadeOnlyTornado() {
        return this.config.realismEnabled() && this.tornadoEncounterActive && this.tornadoEncounterFlee;
    }

    private boolean pacedThisTick() {
        int n = this.client.getTickCount();
        return this.lastMoveTick == n || this.lastOverheadToggleTick == n || this.lastOffensiveToggleTick == n;
    }

    private boolean hesitate() {
        if (!this.config.realismEnabled()) {
            return false;
        }
        int n = Math.max(0, Math.min(40, this.hp().hesitationPercent));
        return n > 0 && Rand.nextInt(0, 99) < n;
    }

    private int prayerMissChancePercent() {
        if (!this.config.realismEnabled()) {
            return 0;
        }
        return Math.max(0, Math.min(100, this.hp().prayerMissPercent));
    }

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(CorruptedGauntletConfig.class).toInstance((Object)((CorruptedGauntletConfig)configManager.getConfig(CorruptedGauntletConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new CorruptedGauntletOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
        this.runResolved = false;
        this.startTime = System.currentTimeMillis();
        this.status = "Starting";
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        if (this.client.getGameState() == GameState.LOGGED_IN) {
            Prayers.disableAll();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGGED_IN) {
            this.loggedInLastTick = false;
        } else if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
            this.phase = HunllefPhase.IDLE;
        } else if (gameState == GameState.LOADING && this.phase == HunllefPhase.FIGHTING) {
            if (this.runConfirmsKill()) {
                this.recordKill("teleport-out");
                this.resetState();
                if (this.config.targetKillCount() > 0 && this.killCount >= this.config.targetKillCount()) {
                    this.phase = HunllefPhase.DONE;
                    this.status = "Done \u2014 target kills reached";
                } else if (this.config.autoReenter()) {
                    this.phase = HunllefPhase.LOOT_CHEST;
                    this.status = "Run complete \u2014 looting";
                } else {
                    this.status = "Run complete \u2014 reset";
                }
            } else {
                this.diag("LOADING while fighting but no kill evidence \u2014 re-syncing (not counted)");
                this.resetState();
                this.status = "Re-syncing\u2026";
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String string = chatMessage.getMessage().replaceAll("<[^>]*>", "").toLowerCase();
        if (string.contains("inventory was loaded successfully") || string.contains("equipment was loaded successfully")) {
            this.presetLoadConfirmed = true;
        }
        if (string.contains("locked preset")) {
            this.lockedPresetDetected = true;
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved projectileMoved) {
        HunllefStyle hunllefStyle;
        Projectile projectile = projectileMoved.getProjectile();
        if (projectile == null || projectile.getRemainingCycles() <= 0) {
            return;
        }
        int n = projectile.getId();
        int n2 = this.client.getTickCount();
        if (n == 1708 || n == 1714) {
            hunllefStyle = HunllefStyle.MAGIC;
            this.prayerOverride = Prayer.PROTECT_FROM_MAGIC;
            if (n == 1714) {
                this.prayerWasDisabled = true;
            }
        } else if (n == 1712) {
            hunllefStyle = HunllefStyle.RANGED;
            this.prayerOverride = Prayer.PROTECT_FROM_MISSILES;
        } else {
            return;
        }
        this.currentStyle = hunllefStyle;
        this.prayerOverrideUntilTick = n2 + 4;
        if (hunllefStyle != this.lastSeenBossStyle || n2 - this.lastBossProjTick > 4) {
            this.bossAttacksInStyle = hunllefStyle == this.lastSeenBossStyle ? this.bossAttacksInStyle + 1 : 1;
            this.lastSeenBossStyle = hunllefStyle;
            this.lastBossProjTick = n2;
            this.recordBossLaunch(n2);
        }
        this.predictedNextStyle = this.bossAttacksInStyle >= 4 ? CorruptedGauntletPlugin.opposite(hunllefStyle) : hunllefStyle;
    }

    private static HunllefStyle opposite(HunllefStyle hunllefStyle) {
        return hunllefStyle == HunllefStyle.MAGIC ? HunllefStyle.RANGED : HunllefStyle.MAGIC;
    }

    private void recordBossLaunch(int n) {
        if (n == this.lastBossAttackTick) {
            return;
        }
        this.lastBossAttackTick = n;
        this.nextExpectedAttackTick = n + 5;
        Player player = this.client.getLocalPlayer();
        if (player != null && player.getWorldLocation() != null) {
            WorldPoint worldPoint = player.getWorldLocation();
            this.recentTargetedTiles.addFirst(worldPoint);
            while (this.recentTargetedTiles.size() > 4) {
                this.recentTargetedTiles.removeLast();
            }
            this.recentShotLaunches.addFirst(new int[]{worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane(), n});
            while (this.recentShotLaunches.size() > 6) {
                this.recentShotLaunches.removeLast();
            }
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged animationChanged) {
        boolean bl;
        if (this.phase != HunllefPhase.FIGHTING) {
            return;
        }
        Actor actor = animationChanged.getActor();
        if (actor == null) {
            return;
        }
        int n = this.client.getTickCount();
        if (actor instanceof NPC && this.matchesHunllef((NPC)actor)) {
            int n2 = actor.getAnimation();
            if (n2 == 8419) {
                this.recordBossLaunch(n);
            } else if (n2 == 8754) {
                this.predictedNextStyle = CorruptedGauntletPlugin.opposite(this.lastSeenBossStyle);
                this.bossAttacksInStyle = 4;
            } else if (n2 == 8418) {
                this.prayerWasDisabled = true;
            }
            return;
        }
        Player player = this.client.getLocalPlayer();
        if (actor != player || player == null) {
            return;
        }
        int n3 = player.getAnimation();
        if (n3 == -1) {
            return;
        }
        if (this.lastAteTick >= 0 && n - this.lastAteTick <= 2) {
            return;
        }
        if (this.lastMoveTick == n) {
            return;
        }
        boolean bl2 = bl = this.lastBossTargetTick >= 0 && n - this.lastBossTargetTick <= 2;
        if (!bl) {
            return;
        }
        NPC nPC = NPCs.getNearest(this::matchesHunllef);
        if (nPC != null && this.chebyshevToNpc(player.getWorldLocation(), nPC) > this.effectiveAttackRange()) {
            return;
        }
        if (this.lastPlayerAttackTick >= 0 && n - this.lastPlayerAttackTick < 3) {
            return;
        }
        if (this.swapTick >= 0 && n == this.swapTick) {
            return;
        }
        ++this.playerAttackCount;
        ++this.stallSinceDropAttacks;
        this.lastPlayerAttackTick = n;
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        int n = this.client.getTickCount();
        if (this.client.getBoostedSkillLevel(Skill.HITPOINTS) == 0 && this.phase == HunllefPhase.FIGHTING) {
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
            this.recordDeath();
            this.resetState();
            if (this.config.autoReenter()) {
                this.phase = HunllefPhase.LOOT_CHEST;
                this.status = "Died \u2014 re-entering";
            } else {
                this.status = "Died \u2014 reset";
            }
            return;
        }
        if (this.phase != HunllefPhase.FIGHTING) {
            return;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return;
        }
        NPC nPC = NPCs.getNearest(this::matchesHunllef);
        if (this.interactingIsHunllef(player)) {
            this.lastBossTargetTick = n;
        }
        this.lastPlayerAnim = player.getAnimation();
        if (nPC != null) {
            HunllefWeapon hunllefWeapon;
            int n2;
            int n3 = this.bossHpPercent;
            this.updateBossHp(nPC);
            if (this.bossHpPercent < n3) {
                this.lastBossHpDropTick = n;
            }
            if (this.bossHpKnown && this.bossHpPercent <= 15) {
                this.sawBossLowHp = true;
            }
            if ((n2 = nPC.getHealthRatio()) >= 0) {
                this.bossHpKnown = true;
                if (this.lastBossHpRatio >= 0 && n2 < this.lastBossHpRatio) {
                    this.stallSinceDropAttacks = 0;
                }
                this.lastBossHpRatio = n2;
            }
            HunllefWeapon hunllefWeapon2 = hunllefWeapon = this.config.useOverheadPrayer() ? this.bossProtectedStyle(nPC) : null;
            if (hunllefWeapon == this.overheadRaw) {
                if (this.overheadStableTicks < 5) {
                    ++this.overheadStableTicks;
                }
            } else {
                this.overheadRaw = hunllefWeapon;
                this.overheadStableTicks = 1;
            }
            if (this.overheadStableTicks >= 2) {
                this.lastProtectedStyle = hunllefWeapon;
            }
        }
        this.refreshTornadoes();
        if (nPC != null) {
            this.stepEvade(player, nPC, n);
        }
        this.maintainPrayer();
    }

    @Override
    protected int loop() {
        try {
            return this.loopBody();
        }
        catch (Exception exception) {
            this.diag("EXCEPTION: " + exception.getClass().getSimpleName() + " " + exception.getMessage());
            log.warn("[CorruptedGauntlet] loop error", (Throwable)exception);
            return 600;
        }
    }

    private int loopBody() {
        int n;
        if (this.client.getGameState() == GameState.LOADING) {
            this.diag("loading");
            return 1000;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            this.diag("not logged in");
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.diag("login settle");
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            this.diag("no local player");
            return 600;
        }
        NPC nPC = NPCs.getNearest(this::matchesHunllef);
        if (nPC != null) {
            this.lastHunllefId = nPC.getId();
            n = nPC.getAnimation();
            if (n != -1) {
                this.lastHunllefAnim = n;
            }
        }
        if (this.phase == HunllefPhase.DONE) {
            this.status = "Done \u2014 target kills reached";
            this.diag("done");
            return -1;
        }
        if (nPC == null) {
            this.captureNearbyNpcs();
            if (this.phase == HunllefPhase.FIGHTING) {
                ++this.bossMissingTicks;
                if (this.bossMissingTicks < 4) {
                    this.status = "Boss morphing\u2026";
                    this.diag("boss null " + this.bossMissingTicks + "t \u2014 waiting (morph?)");
                    return 200;
                }
                if (Prayers.anyActive()) {
                    Prayers.disableAll();
                }
                if (this.runResolved || this.runConfirmsKill()) {
                    this.recordKill("in-room despawn");
                    if (this.config.targetKillCount() > 0 && this.killCount >= this.config.targetKillCount()) {
                        this.phase = HunllefPhase.DONE;
                        return -1;
                    }
                    this.phase = this.config.autoReenter() ? HunllefPhase.LOOT_CHEST : HunllefPhase.DEAD;
                    return 600;
                }
                this.status = "Boss morphing\u2026";
                this.diag("boss gone " + this.bossMissingTicks + "t, no kill evidence yet \u2014 holding (not counted)");
                return 200;
            }
            this.bossMissingTicks = 0;
            if (this.phase == HunllefPhase.DEAD) {
                this.phase = HunllefPhase.IDLE;
            }
            if (this.config.autoReenter() && (this.phase == HunllefPhase.REENTER || this.phase == HunllefPhase.IDLE || this.phase == HunllefPhase.LOOT_CHEST || this.phase == HunllefPhase.BANK)) {
                if (this.phase == HunllefPhase.IDLE) {
                    this.phase = HunllefPhase.LOOT_CHEST;
                }
                if (Prayers.anyActive()) {
                    Prayers.disableAll();
                }
                switch (this.phase) {
                    case LOOT_CHEST: {
                        return this.handleLootChest(player);
                    }
                    case BANK: {
                        return this.handleBank(player);
                    }
                }
                return this.handleReenter(player);
            }
            this.status = "Waiting for Hunllef";
            this.runResolved = false;
            this.diag("boss not found; nearby=[" + this.nearbyNpcDebug + "]");
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
            return 600;
        }
        this.bossMissingTicks = 0;
        if (this.phase != HunllefPhase.FIGHTING) {
            if (this.runResolved) {
                if (Prayers.anyActive()) {
                    Prayers.disableAll();
                }
                this.diag("post-run: ignoring transient boss (run already counted) \u2014 phase=" + String.valueOf((Object)this.phase));
                return 400;
            }
            this.resetFightState();
            this.detectEquippedWeapon();
            this.snapshotAmbientNpcs();
            this.phase = HunllefPhase.FIGHTING;
            this.lastBossHpDropTick = this.swapTick = this.client.getTickCount();
            this.fightStartMs = System.currentTimeMillis();
            this.status = "Engaging Hunllef";
            this.diag("engage start, weapon=" + String.valueOf((Object)this.currentWeapon));
        }
        this.refreshWeaponAvailability();
        n = this.handleSupplies(player);
        if (n != 0) {
            return n;
        }
        if (this.lastMoveTick == this.client.getTickCount()) {
            this.diag("move tick \u2014 yield");
            return this.jitter(300);
        }
        if (this.inEvadeOnlyTornado()) {
            this.status = "Evading tornado (no attack)";
            this.diag("evade-only tornado \u2014 not attacking/swapping");
            return this.jitter(300);
        }
        int n2 = this.chebyshevToNpc(player.getWorldLocation(), nPC);
        if (this.shouldSwapWeapon()) {
            this.doWeaponSwap();
            return this.jitter(600);
        }
        if (!this.ensureWeaponWielded()) {
            return this.jitter(350);
        }
        if (this.config.oneActionPerTick() && this.pacedThisTick()) {
            this.diag("anti-stack: defer attack (1 action/tick)");
            return this.jitter(300);
        }
        if (this.hesitate()) {
            this.diag("humanize: hesitate");
            return this.jitter(400);
        }
        boolean bl = this.interactingIsHunllef(player);
        this.attack(player, nPC);
        String string = this.lastProtectedStyle != null ? this.lastProtectedStyle.name() : "?";
        this.status = "Attacking [" + String.valueOf((Object)this.currentWeapon) + "] prot=" + string + " edge=" + n2;
        this.diag((bl ? "attacking" : "ENGAGE click") + " [" + String.valueOf((Object)this.currentWeapon) + "] prot=" + string + " edge=" + n2 + " atk=" + this.playerAttackCount + "/" + this.config.attacksPerSwap());
        return this.jitter(300);
    }

    private void maintainPrayer() {
        Object object;
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        int n = this.client.getTickCount();
        if (this.prayerWasDisabled) {
            this.activeOverheadPrayer = null;
            this.activeOffensivePrayer = null;
            this.prayerWasDisabled = false;
        }
        if (this.config.autoPrayerSwitch()) {
            if (this.config.predictPrayerByCount()) {
                object = this.predictedNextStyle.protect;
            } else {
                Prayer prayer = object = this.prayerOverride != null && n <= this.prayerOverrideUntilTick ? this.prayerOverride : this.currentStyle.protect;
            }
            if (object != null) {
                int n2;
                if (!object.equals((Object)this.lastDesiredOverhead)) {
                    this.lastDesiredOverhead = object;
                    n2 = this.prayerMissChancePercent();
                    boolean bl = this.skipOverheadThisWindow = n2 > 0 && Rand.nextInt(0, 99) < n2;
                    if (this.skipOverheadThisWindow) {
                        this.diag("humanize: miss prayer " + object.name().replace("PROTECT_FROM_", ""));
                    }
                }
                if (!this.skipOverheadThisWindow) {
                    int n3 = n2 = !object.equals((Object)this.activeOverheadPrayer) ? 1 : 0;
                    if (n2 != 0 || !Prayers.isEnabled(object) && n > this.lastOverheadToggleTick) {
                        Prayers.toggle(object);
                        this.activeOverheadPrayer = object;
                        this.lastOverheadToggleTick = n;
                        this.diag("pray " + object.name().replace("PROTECT_FROM_", ""));
                    }
                }
            }
        }
        if (this.config.useOffensivePrayers()) {
            Prayer prayer;
            object = this.offensivePrayerFor(this.currentWeapon);
            Prayer prayer2 = prayer = object != null ? object.prayer : null;
            if (prayer != null) {
                boolean bl;
                boolean bl2 = bl = !prayer.equals((Object)this.activeOffensivePrayer);
                if (bl || !Prayers.isEnabled(prayer) && n > this.lastOffensiveToggleTick) {
                    Prayers.toggle(prayer);
                    this.activeOffensivePrayer = prayer;
                    this.lastOffensiveToggleTick = n;
                }
            }
        }
    }

    private int handleSupplies(Player player) {
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n4 = this.client.getRealSkillLevel(Skill.PRAYER);
        int n5 = n2 > 0 ? n * 100 / n2 : 100;
        int n6 = n4 > 0 ? n3 * 100 / n4 : 100;
        boolean bl = Inventory.contains(item -> this.matches((Item)item, this.config.foodName()));
        boolean bl2 = Inventory.contains(item -> this.matches((Item)item, this.config.prayerPotionName()));
        if (n5 < this.config.eatAtHpPercent() && bl && this.eat(this.config.foodName())) {
            this.lastAteTick = this.client.getTickCount();
            this.status = "Eating (" + n5 + "% HP)";
            this.diag("eat " + n5 + "% hp");
            return this.jitter(300);
        }
        if (n6 < this.config.prayerThresholdPct() && bl2 && this.drink(this.config.prayerPotionName())) {
            this.status = "Restoring prayer (" + n6 + "%)";
            this.diag("restore " + n6 + "% pray");
            return this.jitter(600);
        }
        if (this.config.stopWhenOutOfSupplies() && !bl && n5 < this.config.eatAtHpPercent()) {
            this.status = "No food at low HP \u2014 stopping (check Food Item name)";
            this.diag("STOP: no food '" + this.config.foodName() + "' at " + n5 + "% hp");
            return -1;
        }
        return 0;
    }

    private HunllefWeapon[] weaponCycle() {
        LinkedHashSet<HunllefWeapon> linkedHashSet = new LinkedHashSet<HunllefWeapon>();
        ArrayList<HunllefWeapon> arrayList = new ArrayList<HunllefWeapon>();
        arrayList.add(this.config.weapon1());
        arrayList.add(this.config.weapon2());
        if (this.config.useThirdWeapon()) {
            arrayList.add(this.config.weapon3());
        }
        for (HunllefWeapon hunllefWeapon : arrayList) {
            if (!this.styleEnabled(hunllefWeapon)) continue;
            linkedHashSet.add(hunllefWeapon);
        }
        if (linkedHashSet.isEmpty()) {
            for (HunllefWeapon hunllefWeapon : new HunllefWeapon[]{HunllefWeapon.RANGED, HunllefWeapon.MAGIC, HunllefWeapon.MELEE}) {
                if (!this.styleEnabled(hunllefWeapon)) continue;
                linkedHashSet.add(hunllefWeapon);
            }
        }
        if (linkedHashSet.isEmpty()) {
            linkedHashSet.add(this.config.weapon1());
        }
        return linkedHashSet.toArray(new HunllefWeapon[0]);
    }

    private boolean styleEnabled(HunllefWeapon hunllefWeapon) {
        switch (hunllefWeapon) {
            case RANGED: {
                return this.config.useRanged();
            }
            case MAGIC: {
                return this.config.useMagic();
            }
            case MELEE: {
                return this.config.useMelee();
            }
        }
        return true;
    }

    private String weaponName(HunllefWeapon hunllefWeapon) {
        switch (hunllefWeapon) {
            case RANGED: {
                return "Corrupted bow";
            }
            case MAGIC: {
                return "Corrupted staff";
            }
            case MELEE: {
                return "Corrupted halberd";
            }
        }
        return hunllefWeapon.defaultNameFragment;
    }

    private int weaponId(HunllefWeapon hunllefWeapon) {
        switch (hunllefWeapon) {
            case RANGED: {
                return 23902;
            }
            case MAGIC: {
                return 23899;
            }
            case MELEE: {
                return 23896;
            }
        }
        return 0;
    }

    private boolean itemIsWeapon(Item item, HunllefWeapon hunllefWeapon) {
        if (item == null) {
            return false;
        }
        int n = this.weaponId(hunllefWeapon);
        if (n > 0 && item.getId() == n) {
            return true;
        }
        String string = this.weaponName(hunllefWeapon);
        return string != null && !string.trim().isEmpty() && item.getName() != null && item.getName().toLowerCase().contains(string.trim().toLowerCase());
    }

    private Item findWeaponItem(HunllefWeapon hunllefWeapon) {
        return Inventory.getFirst(item -> this.itemIsWeapon((Item)item, hunllefWeapon));
    }

    private boolean isWeaponEquipped(HunllefWeapon hunllefWeapon) {
        Item item = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
        return this.itemIsWeapon(item, hunllefWeapon);
    }

    private boolean equipWeapon(HunllefWeapon hunllefWeapon) {
        String string;
        Item item;
        block4: {
            item = this.findWeaponItem(hunllefWeapon);
            if (item == null) {
                return false;
            }
            string = "Wield";
            try {
                ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
                if (itemComposition == null || itemComposition.getInventoryActions() == null) break block4;
                for (String string2 : itemComposition.getInventoryActions()) {
                    String string3;
                    if (string2 == null || !(string3 = string2.toLowerCase()).equals("wield") && !string3.equals("wear") && !string3.equals("equip")) continue;
                    string = string2;
                    break;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        item.interact(string);
        return true;
    }

    private OffensivePrayer offensivePrayerFor(HunllefWeapon hunllefWeapon) {
        switch (hunllefWeapon) {
            case RANGED: {
                return this.config.rangedPrayer();
            }
            case MAGIC: {
                return this.config.magicPrayer();
            }
            case MELEE: {
                return this.config.meleePrayer();
            }
        }
        return OffensivePrayer.NONE;
    }

    private void detectEquippedWeapon() {
        HunllefWeapon[] hunllefWeaponArray = this.weaponCycle();
        for (int i = 0; i < hunllefWeaponArray.length; ++i) {
            if (!this.isWeaponEquipped(hunllefWeaponArray[i])) continue;
            this.weaponCycleIndex = i;
            this.currentWeapon = hunllefWeaponArray[i];
            return;
        }
        this.weaponCycleIndex = 0;
        this.currentWeapon = hunllefWeaponArray.length > 0 ? hunllefWeaponArray[0] : HunllefWeapon.RANGED;
    }

    private boolean ensureWeaponWielded() {
        if (this.isWeaponEquipped(this.currentWeapon)) {
            this.equipAttempts = 0;
            return true;
        }
        if (this.equipAttempts < 4) {
            if (this.equipWeapon(this.currentWeapon)) {
                ++this.equipAttempts;
                this.diag("wield " + String.valueOf((Object)this.currentWeapon) + " (id " + this.weaponId(this.currentWeapon) + ") try " + this.equipAttempts);
                return false;
            }
            this.diag("weapon " + String.valueOf((Object)this.currentWeapon) + " (id " + this.weaponId(this.currentWeapon) + " / '" + this.weaponName(this.currentWeapon) + "') not in inventory \u2014 attacking with current gear");
        }
        return true;
    }

    private void captureNearbyNpcs() {
        StringBuilder stringBuilder = new StringBuilder();
        int n = 0;
        for (NPC nPC2 : NPCs.getAll(nPC -> nPC != null && nPC.getName() != null)) {
            if (n++ >= 6) break;
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(nPC2.getName()).append("#").append(nPC2.getId());
        }
        this.nearbyNpcDebug = stringBuilder.toString();
    }

    private void refreshWeaponAvailability() {
        HunllefWeapon[] hunllefWeaponArray = this.weaponCycle();
        int n = 0;
        for (HunllefWeapon hunllefWeapon : hunllefWeaponArray) {
            if (!this.isWeaponEquipped(hunllefWeapon) && this.findWeaponItem(hunllefWeapon) == null) continue;
            ++n;
        }
        this.usableWeaponCount = n;
        this.lowWeaponWarning = n < 2;
    }

    private HunllefWeapon bossProtectedStyle(NPC nPC) {
        HeadIcon headIcon;
        if (nPC == null) {
            return null;
        }
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition == null) {
            nPCComposition = nPC.getComposition();
        }
        if (nPCComposition == null) {
            return null;
        }
        try {
            headIcon = nPCComposition.getOverheadIcon();
        }
        catch (Throwable throwable) {
            return null;
        }
        if (headIcon == null) {
            return null;
        }
        switch (headIcon) {
            case MELEE: {
                return HunllefWeapon.MELEE;
            }
            case RANGED: {
                return HunllefWeapon.RANGED;
            }
            case MAGIC: {
                return HunllefWeapon.MAGIC;
            }
        }
        return null;
    }

    private boolean shouldSwapWeapon() {
        if (System.currentTimeMillis() - this.lastWeaponSwapMs <= 700L) {
            return false;
        }
        if (this.usableWeaponCount < 2) {
            return false;
        }
        HunllefWeapon hunllefWeapon = this.lastProtectedStyle;
        if (hunllefWeapon != null && this.currentWeapon == hunllefWeapon) {
            int n = this.client.getTickCount();
            if (this.swapPendingSinceTick < 0) {
                this.swapPendingSinceTick = n;
                this.swapReactionDelay = this.sampleReactionTicks();
            }
            if (n - this.swapPendingSinceTick >= this.swapReactionDelay) {
                this.lastSwapReason = "overhead";
                return true;
            }
            return false;
        }
        this.swapPendingSinceTick = -1;
        if (this.config.swapOnDamageStall() && this.bossHpKnown && this.stallSinceDropAttacks >= this.config.stallAttacks()) {
            this.lastSwapReason = "stall";
            return true;
        }
        if (hunllefWeapon == null && !this.bossHpKnown && this.playerAttackCount >= this.config.attacksPerSwap()) {
            this.lastSwapReason = "count";
            return true;
        }
        return false;
    }

    private void doWeaponSwap() {
        int n;
        HunllefWeapon[] hunllefWeaponArray = this.weaponCycle();
        if (hunllefWeaponArray.length == 0) {
            return;
        }
        HunllefWeapon hunllefWeapon = this.lastProtectedStyle;
        HunllefWeapon hunllefWeapon2 = this.pickNextWeapon(hunllefWeaponArray, hunllefWeapon);
        if (hunllefWeapon2 == null && hunllefWeapon != null) {
            hunllefWeapon2 = this.pickNextWeapon(hunllefWeaponArray, null);
        }
        int n2 = hunllefWeapon2 == null ? this.weaponCycleIndex : this.indexOfWeapon(hunllefWeaponArray, hunllefWeapon2);
        this.swapTick = n = this.client.getTickCount();
        this.lastBossHpDropTick = n;
        this.playerAttackCount = 0;
        this.stallSinceDropAttacks = 0;
        this.lastWeaponSwapMs = System.currentTimeMillis();
        this.swapPendingSinceTick = -1;
        if (hunllefWeapon2 == null) {
            this.status = "Only 1 weapon \u2014 can't bypass boss prayer!";
            this.diag("swap SKIPPED: no second weapon found (need bow+staff[+halberd] in inventory)");
            return;
        }
        this.weaponCycleIndex = n2;
        this.currentWeapon = hunllefWeapon2;
        this.equipAttempts = 0;
        boolean bl = this.isWeaponEquipped(this.currentWeapon) || this.equipWeapon(this.currentWeapon);
        this.status = "Swapped to " + String.valueOf((Object)this.currentWeapon) + " (" + this.lastSwapReason + ")";
        this.diag("swap -> " + String.valueOf((Object)this.currentWeapon) + " (id " + this.weaponId(this.currentWeapon) + ") " + (bl ? "ok" : "NOT FOUND") + " reason=" + this.lastSwapReason);
    }

    private HunllefWeapon pickNextWeapon(HunllefWeapon[] hunllefWeaponArray, HunllefWeapon hunllefWeapon) {
        for (int i = 1; i <= hunllefWeaponArray.length; ++i) {
            HunllefWeapon hunllefWeapon2 = hunllefWeaponArray[(this.weaponCycleIndex + i) % hunllefWeaponArray.length];
            if (hunllefWeapon2 == this.currentWeapon || hunllefWeapon != null && hunllefWeapon2 == hunllefWeapon || !this.isWeaponEquipped(hunllefWeapon2) && this.findWeaponItem(hunllefWeapon2) == null) continue;
            return hunllefWeapon2;
        }
        return null;
    }

    private int indexOfWeapon(HunllefWeapon[] hunllefWeaponArray, HunllefWeapon hunllefWeapon) {
        for (int i = 0; i < hunllefWeaponArray.length; ++i) {
            if (hunllefWeaponArray[i] != hunllefWeapon) continue;
            return i;
        }
        return this.weaponCycleIndex;
    }

    private int handleLootChest(Player player) {
        if (Dialog.isOpen() && Dialog.canContinue()) {
            Dialog.continueSpace();
            return 600;
        }
        TileObject tileObject = this.findRewardChest();
        if (tileObject != null && this.lootClickLoops < 4) {
            MousePackets.queueClickPacket();
            tileObject.interact("Open");
            ++this.lootClickLoops;
            this.lootedRewardChest = true;
            this.status = "Looting reward chest";
            this.diag("loot: open reward chest " + tileObject.getId() + " (" + this.lootClickLoops + ")");
            return 1200;
        }
        if (this.config.bankAfterRun()) {
            this.phase = HunllefPhase.BANK;
            this.diag(this.lootedRewardChest ? "loot: reward collected \u2192 BANK" : "loot: no reward chest \u2192 BANK");
            return 600;
        }
        this.phase = HunllefPhase.REENTER;
        this.diag("loot: banking off \u2192 REENTER");
        return this.handleReenter(player);
    }

    private int handleBank(Player player) {
        switch (this.bankStep.ordinal()) {
            case 0: {
                TileObject tileObject = this.findBankChest();
                if (tileObject == null) {
                    if (++this.noBankLoops < 4) {
                        this.status = "Looking for bank\u2026";
                        this.diag("bank: chest not found (" + this.noBankLoops + ")");
                        return 700;
                    }
                    this.noBankLoops = 0;
                    this.bankStep = BankStep.PRESET;
                    this.diag("bank: chest (id 29321) not found \u2192 REENTER");
                    this.phase = HunllefPhase.REENTER;
                    return this.handleReenter(player);
                }
                this.noBankLoops = 0;
                if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 3) {
                    this.status = "Walking to bank chest";
                    MovementPackets.sendMovement(tileObject.getWorldLocation());
                    this.diag("bank: walking to chest " + tileObject.getId());
                    return 1200;
                }
                this.status = "Bank \u2014 Last-preset";
                this.presetLoadConfirmed = false;
                this.lockedPresetDetected = false;
                MousePackets.queueClickPacket();
                tileObject.interact("Last-preset");
                this.bankVerifyAt = System.currentTimeMillis();
                this.bankStep = BankStep.VERIFY_PRESET;
                this.diag("bank: Last-preset on chest " + tileObject.getId());
                return 1200;
            }
            case 1: {
                if (this.presetLoadConfirmed) {
                    this.status = "Preset loaded successfully";
                    this.bankStep = BankStep.DONE;
                    this.diag("bank: preset confirmed");
                    return 600;
                }
                if (this.lockedPresetDetected) {
                    this.lockedPresetDetected = false;
                    this.status = "Preset locked \u2014 opening bank to load manually";
                    this.diag("bank: preset locked \u2192 manual load");
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 300;
                }
                if (System.currentTimeMillis() - this.bankVerifyAt > 3000L) {
                    this.status = "Last-preset unconfirmed \u2014 manual load";
                    this.diag("bank: preset unconfirmed \u2192 manual load");
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 300;
                }
                return 400;
            }
            case 2: {
                if (Bank.isOpen()) {
                    this.status = "Bank open \u2014 loading preset";
                    this.bankStep = BankStep.MANUAL_LOAD;
                    return 600;
                }
                TileObject tileObject = this.findBankChest();
                if (tileObject == null) {
                    if (++this.noBankLoops < 4) {
                        this.status = "Looking for bank\u2026";
                        return 700;
                    }
                    this.noBankLoops = 0;
                    this.bankStep = BankStep.PRESET;
                    this.diag("bank: chest not found (manual) \u2192 REENTER");
                    this.phase = HunllefPhase.REENTER;
                    return this.handleReenter(player);
                }
                this.noBankLoops = 0;
                this.status = "Opening bank (Bank)";
                this.presetLoadConfirmed = false;
                tileObject.interact(this.bankChestActionFor(tileObject));
                this.bankOpenAt = System.currentTimeMillis();
                this.bankStep = BankStep.MANUAL_WAIT_OPEN;
                this.diag("bank: open chest " + tileObject.getId());
                return 1000;
            }
            case 3: {
                if (Bank.isOpen()) {
                    this.status = "Bank open \u2014 loading preset";
                    this.bankStep = BankStep.MANUAL_LOAD;
                    return 600;
                }
                if (System.currentTimeMillis() - this.bankOpenAt > 5000L) {
                    this.status = "Bank didn't open \u2014 retrying";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 600;
                }
                this.status = "Waiting for bank to open\u2026";
                return 400;
            }
            case 4: {
                if (!Bank.isOpen()) {
                    this.status = "Bank closed early \u2014 reopening";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 600;
                }
                this.status = "Manual preset: load @ 448,17";
                Mouse.click(448, 17, true);
                this.bankStep = BankStep.MANUAL_CONFIRM;
                return 800;
            }
            case 5: {
                this.status = "Manual preset: confirm @ 68,282";
                Mouse.click(68, 282, true);
                this.bankStep = BankStep.MANUAL_CLOSE;
                return 500;
            }
            case 6: {
                this.status = "Closing bank @ 483,33";
                Mouse.click(483, 33, true);
                this.bankStep = BankStep.DONE;
                return 1000;
            }
        }
        this.bankStep = BankStep.PRESET;
        this.status = "Resupplied \u2014 re-entering";
        this.diag("bank: done \u2192 REENTER");
        this.phase = HunllefPhase.REENTER;
        return this.handleReenter(player);
    }

    private TileObject findRewardChest() {
        return TileObjects.getNearest(tileObject -> {
            if (tileObject == null) {
                return false;
            }
            if (tileObject.getId() == 36087) {
                return true;
            }
            ObjectComposition objectComposition = this.resolveObject(tileObject.getId());
            return objectComposition != null && objectComposition.getId() == 36087;
        });
    }

    private TileObject findBankChest() {
        int n = 29321;
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == n);
        if (tileObject2 != null) {
            return tileObject2;
        }
        String string = "Bank";
        return TileObjects.getNearest(tileObject -> {
            if (tileObject == null) {
                return false;
            }
            ObjectComposition objectComposition = this.resolveObject(tileObject.getId());
            if (objectComposition == null) {
                return false;
            }
            String string2 = objectComposition.getName();
            boolean bl = string2 != null && string2.toLowerCase().contains("bank");
            return bl || !string.isEmpty() && this.objectHasAction(objectComposition, string);
        });
    }

    private String bankChestActionFor(TileObject tileObject) {
        String string = "Bank";
        ObjectComposition objectComposition = this.resolveObject(tileObject.getId());
        if (this.objectHasAction(objectComposition, string)) {
            return string;
        }
        if (objectComposition != null) {
            for (String string2 : new String[]{"Bank", "Use", "Open"}) {
                if (!this.objectHasAction(objectComposition, string2)) continue;
                return string2;
            }
        }
        return string;
    }

    private int handleReenter(Player player) {
        if (this.findRewardChest() != null && this.rewardLootBounces < 3) {
            ++this.rewardLootBounces;
            this.lootClickLoops = 0;
            this.noBankLoops = 0;
            this.lootedRewardChest = false;
            this.bankStep = BankStep.PRESET;
            this.phase = HunllefPhase.LOOT_CHEST;
            this.diag("reenter: reward chest still present \u2192 loot first (bounce " + this.rewardLootBounces + ")");
            return this.handleLootChest(player);
        }
        if (Dialog.isOpen()) {
            if (Dialog.isViewingOptions()) {
                String string = "Corrupted Gauntlet";
                boolean bl = Dialog.hasOption(string) ? Dialog.chooseOption(string) : Dialog.chooseOption(2);
                this.status = "Re-enter: select Corrupted";
                this.diag("reenter dialogue -> " + (String)(bl ? "'" + string + "'" : "option 2"));
                return 1200;
            }
            if (Dialog.canContinue()) {
                Dialog.continueSpace();
                return 600;
            }
            return 600;
        }
        TileObject tileObject = this.findGauntletEntrance();
        if (tileObject != null) {
            String string = this.gauntletEnterActionFor(tileObject);
            tileObject.interact(string);
            this.runResolved = false;
            this.status = "Re-entering Gauntlet";
            this.diag("reenter: clicked Gauntlet object " + tileObject.getId() + " action='" + string + "'");
            return 1800;
        }
        this.status = "Re-enter: waiting for Gauntlet";
        WorldPoint worldPoint = player.getWorldLocation();
        this.diag("reenter: no Gauntlet entrance found near " + String.valueOf(worldPoint != null ? worldPoint : "?") + " (id=37340 name='gauntlet')");
        return 1000;
    }

    private TileObject findGauntletEntrance() {
        int n = 37340;
        String string = "gauntlet".toLowerCase();
        String string2 = "Enter";
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> {
            if (tileObject == null) {
                return false;
            }
            if (n > 0 && tileObject.getId() == n) {
                return true;
            }
            if (string.isEmpty()) {
                return false;
            }
            ObjectComposition objectComposition = this.resolveObject(tileObject.getId());
            String string2 = objectComposition == null ? null : objectComposition.getName();
            return string2 != null && string2.toLowerCase().contains(string);
        });
        if (tileObject2 != null) {
            return tileObject2;
        }
        if (string2.isEmpty()) {
            return null;
        }
        return TileObjects.getNearest(tileObject -> {
            if (tileObject == null) {
                return false;
            }
            ObjectComposition objectComposition = this.resolveObject(tileObject.getId());
            if (objectComposition == null) {
                return false;
            }
            String string2 = objectComposition.getName();
            if (string2 != null && string2.toLowerCase().contains("chest")) {
                return false;
            }
            return this.objectHasAction(objectComposition, string2);
        });
    }

    private String gauntletEnterActionFor(TileObject tileObject) {
        String string = "Enter";
        ObjectComposition objectComposition = this.resolveObject(tileObject.getId());
        if (this.objectHasAction(objectComposition, string)) {
            return string;
        }
        if (objectComposition != null) {
            for (String string2 : new String[]{"Enter", "Enter-corrupted", "Corrupted", "Travel", "Climb", "Use", "Open"}) {
                if (!this.objectHasAction(objectComposition, string2)) continue;
                return string2;
            }
        }
        return string;
    }

    private boolean objectHasAction(ObjectComposition objectComposition, String string) {
        if (objectComposition == null || objectComposition.getActions() == null || string == null || string.isEmpty()) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : objectComposition.getActions()) {
            if (string3 == null || !string3.toLowerCase().contains(string2)) continue;
            return true;
        }
        return false;
    }

    private ObjectComposition resolveObject(int n) {
        ObjectComposition objectComposition = this.client.getObjectDefinition(n);
        if (objectComposition == null) {
            return null;
        }
        try {
            ObjectComposition objectComposition2;
            if (objectComposition.getImpostorIds() != null && (objectComposition2 = objectComposition.getImpostor()) != null) {
                return objectComposition2;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return objectComposition;
    }

    private void refreshTornadoes() {
        WorldPoint worldPoint;
        if (!this.config.dodgeTornadoes()) {
            this.tornadoes = Collections.emptyList();
            return;
        }
        Player player = this.client.getLocalPlayer();
        WorldPoint worldPoint2 = worldPoint = player != null ? player.getWorldLocation() : null;
        if (worldPoint == null) {
            this.tornadoes = Collections.emptyList();
            return;
        }
        int n = 12;
        this.tornadoes = NPCs.getAll(nPC -> this.isTornadoCandidate((NPC)nPC) && nPC.getWorldLocation().distanceTo(worldPoint) <= n);
    }

    private boolean isTornadoCandidate(NPC nPC) {
        if (nPC == null || nPC.getWorldLocation() == null) {
            return false;
        }
        if (this.matchesHunllef(nPC)) {
            return false;
        }
        if (this.ambientNpcIndices.contains(nPC.getIndex())) {
            return false;
        }
        String string = nPC.getName();
        if (string != null && string.toLowerCase().contains("hunllef")) {
            return false;
        }
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition != null) {
            if (nPCComposition.isFollower()) {
                return false;
            }
            if (nPCComposition.getSize() >= 2) {
                return false;
            }
        }
        return true;
    }

    private void snapshotAmbientNpcs() {
        WorldPoint worldPoint;
        this.ambientNpcIndices.clear();
        Player player = this.client.getLocalPlayer();
        WorldPoint worldPoint2 = worldPoint = player != null ? player.getWorldLocation() : null;
        if (worldPoint == null) {
            return;
        }
        for (NPC nPC2 : NPCs.getAll(nPC -> nPC != null && !this.matchesHunllef((NPC)nPC) && nPC.getWorldLocation() != null && nPC.getWorldLocation().distanceTo(worldPoint) <= 20)) {
            this.ambientNpcIndices.add(nPC2.getIndex());
        }
    }

    private int minStandDistance() {
        return this.currentWeapon == HunllefWeapon.MELEE ? Math.max(1, 1) : Math.max(2, 1);
    }

    private Set<WorldPoint> lavaTilesOfIds(int[] nArray) {
        Tile[][][] tileArray;
        WorldPoint worldPoint;
        HashSet<WorldPoint> hashSet = new HashSet<WorldPoint>();
        if (!this.config.avoidLavaFloor() || nArray == null || nArray.length == 0) {
            return hashSet;
        }
        Player player = this.client.getLocalPlayer();
        WorldPoint worldPoint2 = worldPoint = player != null ? player.getWorldLocation() : null;
        if (worldPoint == null) {
            return hashSet;
        }
        int n = worldPoint.getPlane();
        try {
            tileArray = this.client.getScene().getTiles();
        }
        catch (Exception exception) {
            return hashSet;
        }
        if (tileArray == null || n < 0 || n >= tileArray.length) {
            return hashSet;
        }
        int n2 = this.client.getBaseX();
        int n3 = this.client.getBaseY();
        int n4 = 14;
        for (int i = -n4; i <= n4; ++i) {
            for (int j = -n4; j <= n4; ++j) {
                GroundObject groundObject = this.groundObjectAt(tileArray, n, worldPoint.getX() + i - n2, worldPoint.getY() + j - n3);
                if (groundObject == null || !CorruptedGauntletPlugin.containsId(nArray, groundObject.getId())) continue;
                hashSet.add(new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, n));
            }
        }
        return hashSet;
    }

    private GroundObject groundObjectAt(Tile[][][] tileArray, int n, int n2, int n3) {
        if (n2 < 0 || n3 < 0 || n2 >= 104 || n3 >= 104) {
            return null;
        }
        Tile tile = tileArray[n][n2][n3];
        return tile != null ? tile.getGroundObject() : null;
    }

    private Set<WorldPoint> tornadoDangerTiles(WorldPoint worldPoint) {
        HashSet<WorldPoint> hashSet = new HashSet<WorldPoint>();
        int n = 1;
        for (NPC nPC : this.tornadoes) {
            WorldPoint worldPoint2 = nPC.getWorldLocation();
            this.addWithRadius(hashSet, worldPoint2, n);
            this.addWithRadius(hashSet, this.predictTornadoNext(worldPoint2, worldPoint), n);
        }
        return hashSet;
    }

    private boolean tornadoWithin(WorldPoint worldPoint, int n) {
        for (NPC nPC : this.tornadoes) {
            WorldPoint worldPoint2 = nPC.getWorldLocation();
            if (worldPoint2 == null || Math.max(Math.abs(worldPoint2.getX() - worldPoint.getX()), Math.abs(worldPoint2.getY() - worldPoint.getY())) > n) continue;
            return true;
        }
        return false;
    }

    private int tornadoPressure(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return 0;
        }
        int n = this.tornadoKeepDistance() + 1;
        int n2 = 0;
        for (NPC nPC : this.tornadoes) {
            WorldPoint worldPoint2 = nPC.getWorldLocation();
            if (worldPoint2 == null || worldPoint2.getPlane() != worldPoint.getPlane() || Math.max(Math.abs(worldPoint2.getX() - worldPoint.getX()), Math.abs(worldPoint2.getY() - worldPoint.getY())) > n) continue;
            ++n2;
        }
        return n2;
    }

    private int minDistToNpcs(WorldPoint worldPoint, List<NPC> list) {
        int n = 9;
        if (list == null) {
            return n;
        }
        for (NPC nPC : list) {
            WorldPoint worldPoint2 = nPC.getWorldLocation();
            if (worldPoint2 == null || worldPoint2.getPlane() != worldPoint.getPlane()) continue;
            int n2 = Math.max(Math.abs(worldPoint2.getX() - worldPoint.getX()), Math.abs(worldPoint2.getY() - worldPoint.getY()));
            if (n2 < n) {
                n = n2;
            }
            if (n != 0) continue;
            break;
        }
        return n;
    }

    private WorldPoint predictTornadoNext(WorldPoint worldPoint, WorldPoint worldPoint2) {
        if (worldPoint == null) {
            return null;
        }
        if (worldPoint2 == null) {
            return worldPoint;
        }
        int n = Integer.signum(worldPoint2.getX() - worldPoint.getX());
        int n2 = Integer.signum(worldPoint2.getY() - worldPoint.getY());
        return new WorldPoint(worldPoint.getX() + n, worldPoint.getY() + n2, worldPoint.getPlane());
    }

    private Set<WorldPoint> exactTornadoTiles(WorldPoint worldPoint) {
        HashSet<WorldPoint> hashSet = new HashSet<WorldPoint>();
        for (NPC nPC : this.tornadoes) {
            WorldPoint worldPoint2 = nPC.getWorldLocation();
            if (worldPoint2 == null) continue;
            hashSet.add(worldPoint2);
            WorldPoint worldPoint3 = this.predictTornadoNext(worldPoint2, worldPoint);
            if (worldPoint3 == null) continue;
            hashSet.add(worldPoint3);
        }
        return hashSet;
    }

    private Set<WorldPoint> shotDangerTiles(int n) {
        if (!this.config.dodgeBossShots() || this.recentShotLaunches.isEmpty()) {
            return Collections.emptySet();
        }
        int n2 = 4;
        int n3 = 1;
        HashSet<WorldPoint> hashSet = new HashSet<WorldPoint>();
        for (int[] nArray : this.recentShotLaunches) {
            if (n - nArray[3] > n2) continue;
            this.addWithRadius(hashSet, new WorldPoint(nArray[0], nArray[1], nArray[2]), n3);
        }
        return hashSet;
    }

    private void addWithRadius(Set<WorldPoint> set, WorldPoint worldPoint, int n) {
        if (worldPoint == null) {
            return;
        }
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                set.add(new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane()));
            }
        }
    }

    private static boolean containsId(int[] nArray, int n) {
        for (int n2 : nArray) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    private int hazardCost(WorldPoint worldPoint, Set<WorldPoint> set, Set<WorldPoint> set2, Set<WorldPoint> set3, Set<WorldPoint> set4, Set<WorldPoint> set5) {
        int n = 0;
        boolean bl = set2.contains(worldPoint);
        if (set.contains(worldPoint)) {
            n += 10000;
        } else if (bl) {
            n += 6000;
        }
        if (set3.contains(worldPoint)) {
            n += 5500;
        } else if (set4.contains(worldPoint)) {
            n += 1800;
        }
        if (!bl) {
            if (this.touches(worldPoint, set)) {
                n += 1200;
            } else if (this.touches(worldPoint, set2)) {
                n += 400;
            }
        }
        if (set5.contains(worldPoint)) {
            n += 1500;
        }
        return n;
    }

    private boolean touches(WorldPoint worldPoint, Set<WorldPoint> set) {
        if (set.isEmpty()) {
            return false;
        }
        for (int[] nArray : DIRS8) {
            if (!set.contains(new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane()))) continue;
            return true;
        }
        return false;
    }

    private int openness(WorldPoint worldPoint, Set<WorldPoint> set) {
        int n = 0;
        for (int[] nArray : DIRS8) {
            WorldPoint worldPoint2 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (!this.isWalkable(worldPoint2) || this.isWalledBetween(worldPoint, worldPoint2) || set.contains(worldPoint2)) continue;
            ++n;
        }
        return n;
    }

    private double[] bossCenter(NPC nPC) {
        WorldPoint worldPoint = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        return new double[]{(double)worldPoint.getX() + (double)(n - 1) / 2.0, (double)worldPoint.getY() + (double)(n - 1) / 2.0};
    }

    private void stepEvade(Player player, NPC nPC, int n) {
        WorldPoint worldPoint;
        boolean bl;
        int n2;
        boolean bl2;
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (worldPoint2 == null) {
            return;
        }
        Set<WorldPoint> set = this.lavaTilesOfIds(CorruptedGauntletConstants.HOT_LAVA_IDS);
        Set<WorldPoint> set2 = this.lavaTilesOfIds(CorruptedGauntletConstants.LAVA_GROUND_IDS);
        set2.addAll(set);
        Set<WorldPoint> set3 = this.tornadoDangerTiles(worldPoint2);
        Set<WorldPoint> set4 = this.exactTornadoTiles(worldPoint2);
        Set<WorldPoint> set5 = this.shotDangerTiles(n);
        this.hazardTileCount = set2.size();
        boolean bl3 = set2.contains(worldPoint2) || this.isAdjacentToSet(worldPoint2, set2);
        boolean bl4 = this.tornadoWithin(worldPoint2, this.tornadoKeepDistance());
        this.lavaUnderPlayer = bl3;
        this.onHazardTile = bl3 || bl4;
        int n3 = this.chebyshevToNpc(worldPoint2, nPC);
        boolean bl5 = n3 >= this.minStandDistance() && n3 <= this.effectiveAttackRange();
        int n4 = this.tornadoPressure(worldPoint2);
        boolean bl6 = n4 >= 2;
        int n5 = this.hazardCost(worldPoint2, set, set2, set4, set3, set5);
        boolean bl7 = set5.contains(worldPoint2);
        boolean bl8 = this.updateTornadoEncounter(bl4) && !bl6;
        boolean bl9 = bl2 = set.contains(worldPoint2) || set4.contains(worldPoint2) || bl6;
        if (this.config.realismEnabled() && !this.hp().earlyLavaAvoidance) {
            n2 = set2.contains(worldPoint2);
            bl = bl2 || n2 != 0 || bl7 || !bl5 || bl8;
        } else {
            boolean bl10 = bl = n5 > 0 || !bl5 || bl6;
        }
        if (!bl) {
            this.moveIntendedPos = worldPoint2;
            this.lastStepFrom = null;
            return;
        }
        if (!bl2 && !bl7 && this.config.realismEnabled() && (n2 = Math.max(0, this.hp().minStepIntervalTicks)) > 0 && this.lastMoveTick >= 0 && n - this.lastMoveTick < n2) {
            this.moveIntendedPos = worldPoint2;
            this.lastStepFrom = null;
            return;
        }
        if (this.moveIntendedPos == null || worldPoint2.distanceTo(this.moveIntendedPos) > 2) {
            this.moveIntendedPos = worldPoint2;
        }
        WorldPoint worldPoint3 = this.chooseStep(worldPoint2, nPC, set, set2, set4, set3, set5, n4);
        boolean bl11 = set.contains(worldPoint2);
        boolean bl12 = worldPoint3 == null || worldPoint3.equals((Object)worldPoint2) || set2.contains(worldPoint3) || set4.contains(worldPoint3);
        boolean bl13 = false;
        if (n5 > 0 && bl12 && (worldPoint = this.stepTowardLowestHazard(worldPoint2, nPC, set, set2, set4, set3, set5)) != null && !worldPoint.equals((Object)worldPoint2) && (bl11 || !set.contains(worldPoint))) {
            worldPoint3 = worldPoint;
            bl13 = true;
        }
        if (worldPoint3 == null || worldPoint3.equals((Object)worldPoint2)) {
            return;
        }
        boolean bl14 = set2.contains(worldPoint3);
        boolean bl15 = bl3 || bl4 || bl13 || bl7 ? true : Movement.isRunEnabled();
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint3, bl15);
        this.updateOrbitDir(worldPoint2, worldPoint3, nPC);
        this.lastStepFrom = worldPoint2;
        this.moveIntendedPos = worldPoint3;
        this.lastMoveTick = n;
        ++this.movesDone;
        this.status = bl14 ? "Escaping lava!" : (bl13 ? "Escaping corner!" : (bl3 ? "Off the floor!" : (bl4 ? "Kiting tornado!" : (bl7 ? "Dodging shot!" : "Repositioning"))));
    }

    private WorldPoint chooseStep(WorldPoint worldPoint, NPC nPC, Set<WorldPoint> set, Set<WorldPoint> set2, Set<WorldPoint> set3, Set<WorldPoint> set4, Set<WorldPoint> set5, int n) {
        int n2 = worldPoint.getPlane();
        int n3 = this.effectiveAttackRange();
        int n4 = this.minStandDistance();
        double[] dArray = this.bossCenter(nPC);
        int n5 = 20;
        int n6 = 4;
        WorldPoint worldPoint2 = worldPoint;
        double d = Double.MAX_VALUE;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                int n7;
                WorldPoint worldPoint3;
                WorldPoint worldPoint4 = worldPoint3 = i == 0 && j == 0 ? worldPoint : new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, n2);
                if (!worldPoint3.equals((Object)worldPoint) && (!this.isWalkable(worldPoint3) || this.chebyshevToNpc(worldPoint3, nPC) < 1 || !this.reachableStraight(worldPoint, worldPoint3))) continue;
                double d2 = this.hazardCost(worldPoint3, set, set2, set3, set4, set5);
                int n8 = this.chebyshevToNpc(worldPoint3, nPC);
                if (n8 < n4) {
                    d2 += (double)(n4 - n8) * 25.0;
                } else if (n8 > n3) {
                    d2 += (double)(n8 - n3) * 25.0;
                }
                int n9 = this.openness(worldPoint3, set2);
                d2 += (double)((8 - n9) * n5);
                d2 += Math.hypot((double)worldPoint3.getX() - dArray[0], (double)worldPoint3.getY() - dArray[1]) * (double)n6;
                d2 -= (double)this.tangentBonus(worldPoint, worldPoint3, nPC) * 12.0;
                if (n > 0) {
                    n7 = this.minDistToNpcs(worldPoint3, this.tornadoes);
                    d2 += (double)Math.max(0, 4 - n7) * 150.0 * (double)n;
                    if (n9 < 5) {
                        d2 += (double)(5 - n9) * 110.0 * (double)n;
                    }
                    d2 += Math.hypot((double)worldPoint3.getX() - dArray[0], (double)worldPoint3.getY() - dArray[1]) * (double)n6 * 2.0 * (double)n;
                }
                if ((n7 = Math.max(Math.abs(i), Math.abs(j))) == 2) {
                    d2 -= 30.0;
                }
                if (worldPoint3.equals((Object)this.lastStepFrom)) {
                    d2 += 200.0;
                }
                if (!(d2 < d)) continue;
                d = d2;
                worldPoint2 = worldPoint3;
            }
        }
        return worldPoint2;
    }

    private boolean reachableStraight(WorldPoint worldPoint, WorldPoint worldPoint2) {
        WorldPoint worldPoint3 = worldPoint;
        int n = 0;
        while (!worldPoint3.equals((Object)worldPoint2) && n++ < 6) {
            int n2;
            int n3 = worldPoint3.getX() + Integer.signum(worldPoint2.getX() - worldPoint3.getX());
            WorldPoint worldPoint4 = new WorldPoint(n3, n2 = worldPoint3.getY() + Integer.signum(worldPoint2.getY() - worldPoint3.getY()), worldPoint3.getPlane());
            if (!this.isWalkable(worldPoint4) || this.isWalledBetween(worldPoint3, worldPoint4)) {
                return false;
            }
            worldPoint3 = worldPoint4;
        }
        return worldPoint3.equals((Object)worldPoint2);
    }

    private long tangentBonus(WorldPoint worldPoint, WorldPoint worldPoint2, NPC nPC) {
        double d;
        WorldPoint worldPoint3 = nPC.getWorldLocation();
        if (worldPoint3 == null) {
            return 1L;
        }
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        double d2 = (double)worldPoint3.getX() + (double)(n - 1) / 2.0;
        double d3 = (double)worldPoint3.getY() + (double)(n - 1) / 2.0;
        double d4 = (double)worldPoint.getX() - d2;
        double d5 = (double)worldPoint.getY() - d3;
        double d6 = -d5 * (double)this.orbitDir;
        double d7 = d4 * (double)this.orbitDir;
        double d8 = worldPoint2.getX() - worldPoint.getX();
        double d9 = d6 * d8 + d7 * (d = (double)(worldPoint2.getY() - worldPoint.getY()));
        return d9 > 0.0 ? 2L : (long)(d9 == 0.0 ? 1 : 0);
    }

    private void updateOrbitDir(WorldPoint worldPoint, WorldPoint worldPoint2, NPC nPC) {
        if (this.tangentBonus(worldPoint, worldPoint2, nPC) == 0L) {
            this.orbitDir = -this.orbitDir;
        }
    }

    private int minDistToSet(WorldPoint worldPoint, Set<WorldPoint> set) {
        if (set == null || set.isEmpty()) {
            return 9;
        }
        int n = 9;
        for (WorldPoint worldPoint2 : set) {
            if (worldPoint2 == null || worldPoint2.getPlane() != worldPoint.getPlane()) continue;
            int n2 = Math.max(Math.abs(worldPoint2.getX() - worldPoint.getX()), Math.abs(worldPoint2.getY() - worldPoint.getY()));
            if (n2 < n) {
                n = n2;
            }
            if (n != 0) continue;
            break;
        }
        return n;
    }

    private boolean isAdjacentToSet(WorldPoint worldPoint, Set<WorldPoint> set) {
        return this.minDistToSet(worldPoint, set) <= 1;
    }

    private boolean isWalledBetween(WorldPoint worldPoint, WorldPoint worldPoint2) {
        try {
            return Reachable.isWalled(worldPoint, worldPoint2);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private WorldPoint stepTowardLowestHazard(WorldPoint worldPoint, NPC nPC, Set<WorldPoint> set, Set<WorldPoint> set2, Set<WorldPoint> set3, Set<WorldPoint> set4, Set<WorldPoint> set5) {
        WorldPoint worldPoint2;
        ArrayDeque<WorldPoint> arrayDeque = new ArrayDeque<WorldPoint>();
        HashMap<WorldPoint, WorldPoint> hashMap = new HashMap<WorldPoint, WorldPoint>();
        arrayDeque.add(worldPoint);
        hashMap.put(worldPoint, null);
        int n = 16;
        int n2 = 1200;
        WorldPoint worldPoint3 = null;
        WorldPoint worldPoint4 = null;
        int n3 = Integer.MAX_VALUE;
        while (!arrayDeque.isEmpty() && n2-- > 0) {
            worldPoint2 = (WorldPoint)arrayDeque.poll();
            if (!worldPoint2.equals((Object)worldPoint) && this.chebyshevToNpc(worldPoint2, nPC) >= 1) {
                int n4 = this.hazardCost(worldPoint2, set, set2, set3, set4, set5);
                if (n4 == 0) {
                    worldPoint3 = worldPoint2;
                    break;
                }
                if (n4 < n3) {
                    n3 = n4;
                    worldPoint4 = worldPoint2;
                }
            }
            for (int[] nArray : DIRS8) {
                WorldPoint worldPoint5 = new WorldPoint(worldPoint2.getX() + nArray[0], worldPoint2.getY() + nArray[1], worldPoint2.getPlane());
                if (hashMap.containsKey(worldPoint5) || Math.max(Math.abs(worldPoint5.getX() - worldPoint.getX()), Math.abs(worldPoint5.getY() - worldPoint.getY())) > n || !this.isWalkable(worldPoint5) || this.isWalledBetween(worldPoint2, worldPoint5)) continue;
                hashMap.put(worldPoint5, worldPoint2);
                arrayDeque.add(worldPoint5);
            }
        }
        WorldPoint worldPoint6 = worldPoint2 = worldPoint3 != null ? worldPoint3 : worldPoint4;
        if (worldPoint2 == null) {
            return null;
        }
        WorldPoint worldPoint7 = worldPoint2;
        WorldPoint worldPoint8 = (WorldPoint)hashMap.get(worldPoint2);
        while (worldPoint8 != null && !worldPoint8.equals((Object)worldPoint)) {
            worldPoint7 = worldPoint8;
            worldPoint8 = (WorldPoint)hashMap.get(worldPoint8);
        }
        return worldPoint7;
    }

    private int effectiveAttackRange() {
        return this.currentWeapon == HunllefWeapon.MELEE ? 2 : 5;
    }

    private int chebyshevToNpc(WorldPoint worldPoint, NPC nPC) {
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        int n2 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)));
        int n3 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)));
        return Math.max(n2, n3);
    }

    private WorldPoint standoffTile(WorldPoint worldPoint, NPC nPC, int n) {
        int n2;
        double d;
        int n3;
        WorldPoint worldPoint2;
        double d2;
        WorldPoint worldPoint3 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n4 = nPCComposition != null ? nPCComposition.getSize() : 1;
        double d3 = (double)worldPoint3.getX() + (double)(n4 - 1) / 2.0;
        double d4 = (double)worldPoint3.getY() + (double)(n4 - 1) / 2.0;
        double d5 = (double)worldPoint.getX() - d3;
        double d6 = Math.sqrt(d5 * d5 + (d2 = (double)worldPoint.getY() - d4) * d2);
        if (d6 < 0.5) {
            d5 = 0.0;
            d2 = 1.0;
            d6 = 1.0;
        }
        if (this.isWalkable(worldPoint2 = new WorldPoint(n3 = (int)Math.round(d3 + d5 / d6 * (d = (double)n + (double)(n4 - 1) / 2.0)), n2 = (int)Math.round(d4 + d2 / d6 * d), worldPoint3.getPlane()))) {
            return worldPoint2;
        }
        int n5 = Math.max(worldPoint3.getX(), Math.min(worldPoint.getX(), worldPoint3.getX() + n4 - 1));
        int n6 = Math.max(worldPoint3.getY(), Math.min(worldPoint.getY(), worldPoint3.getY() + n4 - 1));
        return new WorldPoint(n5, n6, worldPoint3.getPlane());
    }

    private boolean isWalkable(WorldPoint worldPoint) {
        int n = worldPoint.getX() - this.client.getBaseX();
        int n2 = worldPoint.getY() - this.client.getBaseY();
        if (n < 0 || n2 < 0 || n >= 104 || n2 >= 104) {
            return false;
        }
        CollisionData[] collisionDataArray = this.client.getCollisionMaps();
        if (collisionDataArray == null || collisionDataArray.length <= this.client.getPlane()) {
            return true;
        }
        CollisionData collisionData = collisionDataArray[this.client.getPlane()];
        if (collisionData == null) {
            return true;
        }
        int[][] nArray = collisionData.getFlags();
        return nArray == null || (nArray[n][n2] & 0x240100) == 0;
    }

    private boolean matches(Item item, String string) {
        if (item == null || item.getName() == null) {
            return false;
        }
        String string2 = string.trim().toLowerCase();
        return !string2.isEmpty() && item.getName().toLowerCase().contains(string2);
    }

    private boolean drink(String string) {
        if (string == null || string.trim().isEmpty()) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> this.matches((Item)item, string));
        if (item2 != null) {
            item2.interact("Drink");
            return true;
        }
        return false;
    }

    private boolean eat(String string) {
        if (string == null || string.trim().isEmpty()) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> this.matches((Item)item, string));
        if (item2 != null) {
            item2.interact("Eat");
            return true;
        }
        return false;
    }

    private boolean equipIfNeeded(String string) {
        if (this.isItemEquipped(string)) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> this.matches((Item)item, string));
        if (item2 != null) {
            item2.interact("Wield");
            return true;
        }
        return false;
    }

    private boolean isItemEquipped(String string) {
        if (string == null || string.trim().isEmpty()) {
            return false;
        }
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            Item item = Equipment.fromSlot(equipmentInventorySlot);
            if (item == null || item.getName() == null || !item.getName().toLowerCase().contains(string.trim().toLowerCase())) continue;
            return true;
        }
        return false;
    }

    private void attack(Player player, NPC nPC) {
        boolean bl;
        if (nPC == null || nPC.isDead()) {
            return;
        }
        int n = this.client.getTickCount();
        Actor actor = player.getInteracting();
        boolean bl2 = actor instanceof NPC && this.matchesHunllef((NPC)actor);
        boolean bl3 = bl = bl2 || this.lastBossTargetTick >= 0 && n - this.lastBossTargetTick <= 1;
        if (bl) {
            return;
        }
        if (n == this.lastEngageTick) {
            return;
        }
        nPC.interact("Attack");
        this.lastEngageTick = n;
    }

    private boolean interactingIsHunllef(Player player) {
        Actor actor = player.getInteracting();
        return actor instanceof NPC && this.matchesHunllef((NPC)actor);
    }

    private boolean matchesHunllef(NPC nPC) {
        if (nPC == null || nPC.isDead() || nPC.getName() == null) {
            return false;
        }
        if (nPC.getName().equalsIgnoreCase("Corrupted Hunllef")) {
            return true;
        }
        int n = nPC.getId();
        for (int n2 : CorruptedGauntletConstants.HUNLLEF_IDS) {
            if (n != n2) continue;
            return true;
        }
        return false;
    }

    private void updateBossHp(NPC nPC) {
        int n = nPC.getHealthRatio();
        int n2 = nPC.getHealthScale();
        if (n >= 0 && n2 > 0) {
            this.bossHpPercent = Math.max(0, Math.min(100, n * 100 / n2));
        }
    }

    private void resetFightState() {
        this.currentStyle = HunllefStyle.RANGED;
        this.prayerOverride = null;
        this.prayerOverrideUntilTick = -1;
        this.lastSeenBossStyle = HunllefStyle.RANGED;
        this.bossAttacksInStyle = 0;
        this.predictedNextStyle = HunllefStyle.RANGED;
        this.lastBossProjTick = -100;
        this.lastOverheadToggleTick = -1;
        this.lastOffensiveToggleTick = -1;
        this.activeOverheadPrayer = null;
        this.activeOffensivePrayer = null;
        this.prayerWasDisabled = false;
        this.lastDesiredOverhead = null;
        this.skipOverheadThisWindow = false;
        this.sawBossLowHp = false;
        this.playerAttackCount = 0;
        this.lastPlayerAnim = -1;
        this.lastPlayerAttackTick = -1;
        this.lastBossTargetTick = -1;
        this.lastAteTick = -1;
        this.lastEngageTick = -1;
        this.equipAttempts = 0;
        this.weaponCycleIndex = 0;
        HunllefWeapon[] hunllefWeaponArray = this.weaponCycle();
        this.currentWeapon = hunllefWeaponArray.length > 0 ? hunllefWeaponArray[0] : HunllefWeapon.RANGED;
        this.lastWeaponSwapMs = 0L;
        this.swapTick = -1;
        this.lastSwapReason = "-";
        this.usableWeaponCount = 0;
        this.lowWeaponWarning = false;
        this.lastDodgeTile = null;
        this.ambientNpcIndices.clear();
        this.tornadoEncounterActive = false;
        this.tornadoEncounterFlee = false;
        this.moveIntendedPos = null;
        this.lastStepFrom = null;
        this.lavaUnderPlayer = false;
        this.lastBossAttackTick = -100;
        this.nextExpectedAttackTick = -100;
        this.recentTargetedTiles.clear();
        this.recentShotLaunches.clear();
        this.orbitDir = 1;
        this.lastMoveTick = -1;
        this.movesDone = 0;
        this.hazardTileCount = 0;
        this.onHazardTile = false;
        this.bossMissingTicks = 0;
        this.bossHpPercent = 100;
        this.lastBossHpDropTick = -1;
        this.lastBossHpRatio = -1;
        this.bossHpKnown = false;
        this.stallSinceDropAttacks = 0;
        this.lastProtectedStyle = null;
        this.overheadRaw = null;
        this.overheadStableTicks = 0;
        this.swapPendingSinceTick = -1;
        this.swapReactionDelay = 0;
        this.lootedRewardChest = false;
        this.lootClickLoops = 0;
        this.noBankLoops = 0;
        this.rewardLootBounces = 0;
        this.bankStep = BankStep.PRESET;
        this.presetLoadConfirmed = false;
        this.lockedPresetDetected = false;
        this.bankVerifyAt = 0L;
        this.bankOpenAt = 0L;
    }

    private void resetState() {
        this.phase = HunllefPhase.IDLE;
        this.status = "Idle";
        this.resetFightState();
        this.tornadoes = Collections.emptyList();
        this.loggedInLastTick = false;
    }

    private void recordBossKill() {
        if (this.fightStartMs <= 0L) {
            return;
        }
        long l = System.currentTimeMillis() - this.fightStartMs;
        this.fightStartMs = 0L;
        if (l < 5000L) {
            return;
        }
        this.totalBossMs += l;
        ++this.bossTimedCount;
        if (this.bestBossMs == 0L || l < this.bestBossMs) {
            this.bestBossMs = l;
        }
        if (l > this.worstBossMs) {
            this.worstBossMs = l;
        }
    }

    private boolean runConfirmsKill() {
        long l = this.fightStartMs > 0L ? System.currentTimeMillis() - this.fightStartMs : 0L;
        return l >= 5000L || this.sawBossLowHp;
    }

    private void recordKill(String string) {
        if (this.runResolved) {
            return;
        }
        this.runResolved = true;
        ++this.killCount;
        this.recordBossKill();
        if (Prayers.anyActive()) {
            Prayers.disableAll();
        }
        this.status = "Hunllef defeated! (" + this.killCount + ")";
        this.diag("kill #" + this.killCount + " via " + string);
    }

    private void recordDeath() {
        if (this.runResolved) {
            return;
        }
        this.runResolved = true;
        ++this.deathCount;
        this.fightStartMs = 0L;
        this.diag("death #" + this.deathCount);
    }

    HunllefPhase getPhase() {
        return this.phase;
    }

    String getStatus() {
        return this.status;
    }

    int getKillCount() {
        return this.killCount;
    }

    int getDeathCount() {
        return this.deathCount;
    }

    int getTotalRuns() {
        return this.killCount + this.deathCount;
    }

    long getStartTime() {
        return this.startTime;
    }

    long getAvgBossMs() {
        return this.bossTimedCount > 0 ? this.totalBossMs / (long)this.bossTimedCount : 0L;
    }

    long getBestBossMs() {
        return this.bestBossMs;
    }

    long getWorstBossMs() {
        return this.worstBossMs;
    }

    long getCurrentFightMs() {
        return this.fightStartMs > 0L ? System.currentTimeMillis() - this.fightStartMs : 0L;
    }

    HunllefStyle getCurrentStyle() {
        return this.currentStyle;
    }

    HunllefWeapon getCurrentWeapon() {
        return this.currentWeapon;
    }

    int getPlayerAttackCount() {
        return this.playerAttackCount;
    }

    int getUsableWeaponCount() {
        return this.usableWeaponCount;
    }

    boolean isLowWeaponWarning() {
        return this.lowWeaponWarning;
    }

    String getLastSwapReason() {
        return this.lastSwapReason;
    }

    int getTornadoCount() {
        return this.tornadoes == null ? 0 : this.tornadoes.size();
    }

    int getMovesDone() {
        return this.movesDone;
    }

    int getHazardTileCount() {
        return this.hazardTileCount;
    }

    boolean isOnHazardTile() {
        return this.onHazardTile;
    }

    int getTicksToNextAttack() {
        return this.nextExpectedAttackTick - this.client.getTickCount();
    }

    int getBossHpPercent() {
        return this.bossHpPercent;
    }

    int getLastHunllefId() {
        return this.lastHunllefId;
    }

    int getLastHunllefAnim() {
        return this.lastHunllefAnim;
    }

    int getLastPlayerAnim() {
        return this.lastPlayerAnim;
    }

    String getLastAction() {
        return this.lastAction;
    }

    String getNearbyNpcDebug() {
        return this.nearbyNpcDebug;
    }

    String getCurrentOverheadName() {
        if (!this.config.autoPrayerSwitch()) {
            return "Manual";
        }
        int n = this.client.getTickCount();
        HunllefStyle hunllefStyle = this.config.predictPrayerByCount() ? this.predictedNextStyle : this.currentStyle;
        Prayer prayer = this.prayerOverride != null && n <= this.prayerOverrideUntilTick ? this.prayerOverride : hunllefStyle.protect;
        return prayer == null ? "None" : prayer.name().replace("PROTECT_FROM_", "");
    }

    String getPredictedStyleName() {
        boolean bl;
        int n = this.client.getTickCount();
        boolean bl2 = bl = this.prayerOverride != null && n <= this.prayerOverrideUntilTick;
        if (bl) {
            return this.currentStyle.toString() + " (proj)";
        }
        return this.predictedNextStyle.toString() + " [" + this.bossAttacksInStyle + "/4]";
    }

    String getActivePrayersName() {
        OffensivePrayer offensivePrayer;
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return "-";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (Prayers.isEnabled(Prayer.PROTECT_FROM_MAGIC)) {
            stringBuilder.append("Mag ");
        }
        if (Prayers.isEnabled(Prayer.PROTECT_FROM_MISSILES)) {
            stringBuilder.append("Mis ");
        }
        if (Prayers.isEnabled(Prayer.PROTECT_FROM_MELEE)) {
            stringBuilder.append("Mel ");
        }
        if ((offensivePrayer = this.offensivePrayerFor(this.currentWeapon)) != null && offensivePrayer.prayer != null && Prayers.isEnabled(offensivePrayer.prayer)) {
            stringBuilder.append("+").append(offensivePrayer.toString());
        }
        return stringBuilder.length() == 0 ? "NONE" : stringBuilder.toString().trim();
    }

    private static enum BankStep {
        PRESET,
        VERIFY_PRESET,
        MANUAL_OPEN,
        MANUAL_WAIT_OPEN,
        MANUAL_LOAD,
        MANUAL_CONFIRM,
        MANUAL_CLOSE,
        DONE;

    }
}

