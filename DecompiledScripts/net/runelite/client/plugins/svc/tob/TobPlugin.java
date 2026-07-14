/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Projectile
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.AnimationChanged
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.ProjectileMoved
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.tob;

import com.google.inject.Binder;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.combat.OffensivePrayer;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.RandomEventState;
import net.runelite.client.plugins.svc.tob.TobCombatMethod;
import net.runelite.client.plugins.svc.tob.TobConfig;
import net.runelite.client.plugins.svc.tob.TobConstants;
import net.runelite.client.plugins.svc.tob.TobOverlay;
import net.runelite.client.plugins.svc.tob.TobPhase;
import net.runelite.client.plugins.svc.tob.TobProjectileLearner;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Prayers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Theatre of Blood</font></nobr></html>", description="Boss fight bot for Verzik Vitur (Theatre of Blood) \u2014 reactive prayers, methods (melee/Dharok's/ranged/magic), brewing, spec", tags={"orion", "tob", "theatre", "verzik", "boss", "combat", "bot"}, enabledByDefault=false)
public class TobPlugin
extends OrionLoopedPlugin {
    private static final Logger log = LoggerFactory.getLogger(TobPlugin.class);
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TobConfig config;
    private TobOverlay overlay;
    private volatile TobPhase phase = TobPhase.IDLE;
    private volatile String status = "Idle";
    private int killCount = 0;
    private int deathCount = 0;
    private long startTime = 0L;
    private volatile boolean started = false;
    private volatile boolean complete = false;
    private boolean wasFighting = false;
    private boolean wasInside = false;
    private int lastVerzikSeenTick = -1;
    private volatile boolean openingWalkDone = true;
    private volatile WorldPoint openingTarget = null;
    private int openingWalkStartTick = -100;
    private TobCombatMethod activeMethod = TobCombatMethod.MELEE;
    private boolean methodResolved = false;
    private TobProjectileLearner learner;
    private Prayer activeOverhead = null;
    private Prayer activeOffensive = null;
    private int lastOverheadToggleTick = -1;
    private int lastOffensiveToggleTick = -1;
    private int lastThreatProjId = -1;
    private int lastThreatTick = -100;
    private static final int THREAT_HOLD_TICKS = 6;
    private int lastPrayerPoints = -1;
    private int lastPrayerDrainTick = -100;
    private final Deque<int[]> pendingImpacts = new ConcurrentLinkedDeque<int[]>();
    private int lastTickHp = -1;
    private int worstHitSeen = 47;
    private int lastBigHitTick = -100;
    private boolean deathLatched = false;
    private int lastBossLaunchTick = -100;
    private final Map<Integer, Integer> lastLaunchTickById = new ConcurrentHashMap<Integer, Integer>();
    private final Deque<int[]> dangerTiles = new ConcurrentLinkedDeque<int[]>();
    private volatile int lastMoveTick = -100;
    private int dodgesDone = 0;
    private boolean dodgeFutile = false;
    private final Deque<WorldPoint> recentTiles = new ConcurrentLinkedDeque<WorldPoint>();
    private static final int DANCE_HISTORY = 3;
    private volatile int orbitDir = 1;
    private volatile WorldPoint dodgeTileA = null;
    private volatile WorldPoint dodgeTileB = null;
    private int stunnedUntilTick = -100;
    private volatile int webWindowUntilTick = -100;
    private volatile int boundUntilTick = -100;
    private int watchdogTrips = 0;
    private boolean prayerViaWidgets = false;
    private Map<Integer, TobProjectileLearner.Style> styleMap;
    private boolean overloadDrunk = false;
    private boolean sapPending = false;
    private int lastEatTick = -100;
    private int lastOverloadTick = -100;
    private boolean dharokTopping = false;
    private int lastTalkTick = -100;
    private int lastBoardTick = -100;
    private int lastMakePartyTick = -100;
    private int lastEnterTick = -100;
    private final Map<String, Integer> equipAttempts = new ConcurrentHashMap<String, Integer>();
    private int lastEquipTick = -100;
    private static final int MAX_EQUIP_ATTEMPTS = 4;
    private volatile boolean specActive = false;
    private int lastSpecTick = -100;
    private int specStartEnergy = -1;
    private int specStartTick = -100;
    private boolean specEnableSent = false;
    private String preSpecWeapon = null;
    private boolean prayersOffAfterKill = false;
    private int specEnableAttempts = 0;
    private static final int MAX_SPEC_ENABLE_ATTEMPTS = 3;
    private int lastEngageTick = -1;
    private boolean loggedInLastTick = false;
    private static final int[][] DODGE_OFFSETS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(TobConfig.class).toInstance((Object)((TobConfig)configManager.getConfig(TobConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.styleMap = this.buildStyleMap();
        this.learner = new TobProjectileLearner(this.styleMap);
        this.overlay = new TobOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
        this.startTime = System.currentTimeMillis();
        this.status = "Starting";
    }

    @Override
    protected void onStop() throws Exception {
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
        }
        this.overlay = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGGED_IN || gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
            this.clearPrayerBeliefs();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        int n;
        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM) {
            return;
        }
        String string = chatMessage.getMessage().replaceAll("<[^>]*>", "").toLowerCase();
        if (string.contains("saps your health")) {
            this.sapPending = true;
        }
        if (string.contains("you're stunned") && (n = this.client.getTickCount()) >= this.stunnedUntilTick) {
            this.stunnedUntilTick = n + 5;
        }
        if (string.contains("cannot free yourself from the web")) {
            n = this.client.getTickCount();
            this.boundUntilTick = n + 4;
            this.webWindowUntilTick = Math.max(this.webWindowUntilTick, n + 9);
        }
        if ((string.contains("theatre of blood completion") || string.contains("the final challenge") && string.contains("complete")) && !this.complete) {
            this.complete = true;
            ++this.killCount;
            this.phase = TobPhase.COMPLETE;
            this.status = "Theatre complete!";
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved projectileMoved) {
        Integer n;
        boolean bl;
        Projectile projectile = projectileMoved.getProjectile();
        if (projectile == null || projectile.getRemainingCycles() <= 0) {
            return;
        }
        int n2 = projectile.getId();
        if (!this.isKnownProjectile(n2)) {
            return;
        }
        Actor actor = projectile.getInteracting();
        if (actor != null && actor != this.client.getLocalPlayer()) {
            return;
        }
        int n3 = this.client.getTickCount();
        if (n2 == 1601) {
            this.webWindowUntilTick = n3 + 9;
        }
        boolean bl2 = bl = (n = this.lastLaunchTickById.get(n2)) == null || n3 - n >= 4;
        if (bl) {
            this.lastLaunchTickById.put(n2, n3);
            int n4 = n3 + 3;
            this.pendingImpacts.addLast(new int[]{n2, n4, 0});
            while (this.pendingImpacts.size() > 8 && this.pendingImpacts.pollFirst() != null) {
            }
            this.lastBossLaunchTick = n3;
            if (this.isTileBomb(n2)) {
                WorldPoint worldPoint;
                Player player = this.client.getLocalPlayer();
                WorldPoint worldPoint2 = worldPoint = player != null ? player.getWorldLocation() : null;
                if (worldPoint != null) {
                    this.dangerTiles.addLast(new int[]{worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane(), n4});
                    while (this.dangerTiles.size() > 8 && this.dangerTiles.pollFirst() != null) {
                    }
                }
            }
        }
        if (this.learner.styleOf(n2) != TobProjectileLearner.Style.NONE) {
            this.lastThreatProjId = n2;
            this.lastThreatTick = n3;
        }
    }

    private boolean isTileBomb(int n) {
        for (int n2 : TobConstants.TILE_BOMB_PROJECTILES) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged animationChanged) {
        Actor actor = animationChanged.getActor();
        if (!(actor instanceof NPC) || !this.isVerzik((NPC)actor)) {
            return;
        }
        int n = actor.getAnimation();
        if (n == 8114 || n == 8116) {
            this.lastBossLaunchTick = this.client.getTickCount();
        }
        if (n == 8127) {
            this.webWindowUntilTick = this.client.getTickCount() + 9;
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        int[] nArray;
        boolean bl;
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return;
        }
        int n = this.client.getTickCount();
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        if (this.lastTickHp > 0 && n2 < this.lastTickHp) {
            this.handleDamage(this.lastTickHp - n2, n);
        }
        this.lastTickHp = n2;
        boolean bl2 = bl = player.getWorldLocation() != null && player.getWorldLocation().getX() >= 6400;
        if (n2 <= 0) {
            if (!this.deathLatched && bl && this.wasFighting && !this.complete) {
                this.deathLatched = true;
                ++this.deathCount;
                this.clearPrayerBeliefs();
                this.overloadDrunk = false;
                this.sapPending = false;
                this.dharokTopping = false;
                this.specActive = false;
                this.resetEquipGuard();
                this.status = "Died \u2014 waiting for respawn";
            }
            return;
        }
        if (this.deathLatched) {
            this.deathLatched = false;
            this.lastBigHitTick = n;
        }
        while ((nArray = this.pendingImpacts.peekFirst()) != null && nArray[1] < n - 4) {
            this.pendingImpacts.pollFirst();
        }
        while ((nArray = this.dangerTiles.peekFirst()) != null && nArray[3] < n) {
            this.dangerTiles.pollFirst();
        }
        if (bl && this.started && !this.complete) {
            if (this.openingWalkDone) {
                this.combatMove(n, player);
            } else {
                this.openingWalk(n, player);
            }
            this.maintainPrayers(n);
        }
    }

    private void openingWalk(int n, Player player) {
        WorldPoint worldPoint = this.openingTarget;
        if (worldPoint == null) {
            return;
        }
        if (this.inBombPhase() || this.inFinalPhase() || n - this.openingWalkStartTick > 8) {
            this.openingWalkDone = true;
            return;
        }
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (worldPoint2 == null) {
            return;
        }
        if (worldPoint2.distanceTo(worldPoint) <= 1) {
            this.openingWalkDone = true;
            return;
        }
        if (this.lastMoveTick == n) {
            return;
        }
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint, true);
        this.lastMoveTick = n;
        this.status = "Moving into position";
    }

    private void combatMove(int n, Player player) {
        if (n < this.stunnedUntilTick) {
            return;
        }
        if (this.lastMoveTick == n) {
            return;
        }
        NPC nPC2 = NPCs.getNearest(nPC -> this.isVerzik((NPC)nPC) && !nPC.isDead());
        if (nPC2 == null) {
            return;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        if (worldPoint == null) {
            return;
        }
        if (this.specActive && this.inBombPhase() && !this.isDangerTile(worldPoint)) {
            return;
        }
        if (this.inFinalPhase()) {
            this.finalPhaseMove(n, player, nPC2, worldPoint);
        } else {
            this.bombDodge(n, player, nPC2, worldPoint);
        }
    }

    private void bombDodge(int n, Player player, NPC nPC, WorldPoint worldPoint) {
        WorldPoint worldPoint2;
        boolean bl;
        if (!this.config.dodgeBombs()) {
            return;
        }
        boolean bl2 = bl = this.isFightingVerzik(player) || this.lastBossLaunchTick > 0 && n - this.lastBossLaunchTick <= 6;
        if (!bl) {
            return;
        }
        if (!this.meleeMethod()) {
            this.legacyTimedStep(n, player, nPC, worldPoint);
            return;
        }
        int n2 = Math.max(this.attackRange(), 2);
        if (!this.pingPairValid(nPC, n2)) {
            this.pickPingPongTiles(nPC, worldPoint);
        }
        if (this.dodgeTileA == null || this.dodgeTileB == null) {
            this.legacyTimedStep(n, player, nPC, worldPoint);
            return;
        }
        boolean bl3 = worldPoint.equals((Object)this.dodgeTileA);
        boolean bl4 = worldPoint.equals((Object)this.dodgeTileB);
        boolean bl5 = this.isDangerTile(worldPoint);
        if (bl5) {
            worldPoint2 = bl3 ? this.dodgeTileB : this.dodgeTileA;
        } else if (!bl3 && !bl4) {
            worldPoint2 = worldPoint.distanceTo(this.dodgeTileA) <= worldPoint.distanceTo(this.dodgeTileB) ? this.dodgeTileA : this.dodgeTileB;
        } else {
            return;
        }
        if (worldPoint2 == null || worldPoint2.equals((Object)worldPoint) || this.isDangerTile(worldPoint2)) {
            this.legacyTimedStep(n, player, nPC, worldPoint);
            return;
        }
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint2, true);
        this.lastMoveTick = n;
        this.pushRecentTile(worldPoint);
        if (bl5) {
            ++this.dodgesDone;
        }
        this.status = bl5 ? "Dodging bomb" : "Repositioning";
    }

    private void pickPingPongTiles(NPC nPC, WorldPoint worldPoint) {
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        if (worldPoint2 == null) {
            this.dodgeTileA = null;
            this.dodgeTileB = null;
            return;
        }
        int n = this.verzikSize(nPC);
        int n2 = Math.max(this.attackRange(), 2);
        int n3 = 2;
        int n4 = worldPoint2.getX();
        int n5 = worldPoint2.getY();
        int n6 = worldPoint.getPlane();
        int n7 = n - 1;
        int[][] nArrayArray = new int[][]{{n4, n5 - n2, 1, 0, 5}, {n4 - n2, n5, 0, 1, 0}, {n4 + n7 + n2, n5, 0, 1, 0}, {n4, n5 + n7 + n2, 1, 0, 0}};
        WorldPoint worldPoint3 = null;
        WorldPoint worldPoint4 = null;
        long l = Long.MIN_VALUE;
        for (int[] nArray : nArrayArray) {
            long l2;
            WorldPoint worldPoint5 = new WorldPoint(nArray[0], nArray[1], n6);
            WorldPoint worldPoint6 = new WorldPoint(nArray[0] + nArray[2] * n3, nArray[1] + nArray[3] * n3, n6);
            if (!this.ringTileGeomOk(worldPoint5, nPC, n2) || !this.ringTileGeomOk(worldPoint6, nPC, n2) || (l2 = (long)nArray[4] * 1000L - (long)(worldPoint.distanceTo(worldPoint5) + worldPoint.distanceTo(worldPoint6)) * 100L) <= l) continue;
            l = l2;
            worldPoint3 = worldPoint5;
            worldPoint4 = worldPoint6;
        }
        this.dodgeTileA = worldPoint3;
        this.dodgeTileB = worldPoint4;
    }

    private boolean ringTileGeomOk(WorldPoint worldPoint, NPC nPC, int n) {
        if (worldPoint == null) {
            return false;
        }
        if (this.distToVerzikEdge(worldPoint, nPC) != n) {
            return false;
        }
        WorldPoint worldPoint2 = this.hazardLocation();
        if (worldPoint2 != null && worldPoint.distanceTo(worldPoint2) == 0) {
            return false;
        }
        try {
            return Reachable.isWalkable(worldPoint);
        }
        catch (Exception exception) {
            return true;
        }
    }

    private boolean pingPairValid(NPC nPC, int n) {
        if (this.dodgeTileA == null || this.dodgeTileB == null) {
            return false;
        }
        if (!this.ringTileGeomOk(this.dodgeTileA, nPC, n) || !this.ringTileGeomOk(this.dodgeTileB, nPC, n)) {
            return false;
        }
        int n2 = Math.max(Math.abs(this.dodgeTileA.getX() - this.dodgeTileB.getX()), Math.abs(this.dodgeTileA.getY() - this.dodgeTileB.getY()));
        return n2 == 2;
    }

    private void legacyTimedStep(int n, Player player, NPC nPC, WorldPoint worldPoint) {
        boolean bl;
        int n2 = this.attackRange();
        int n3 = Math.max(n2, 2);
        int n4 = this.distToVerzikEdge(worldPoint, nPC);
        boolean bl2 = this.meleeMethod();
        boolean bl3 = this.isDangerTile(worldPoint);
        boolean bl4 = bl2 ? n4 != n3 : (bl = n4 < 2 || n4 > n2);
        if (!bl3 && !bl) {
            return;
        }
        WorldPoint worldPoint2 = this.pickTimedStep(worldPoint, nPC, bl3);
        if (worldPoint2 == null || worldPoint2.equals((Object)worldPoint)) {
            return;
        }
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint2, true);
        this.lastMoveTick = n;
        this.pushRecentTile(worldPoint);
        if (bl3) {
            ++this.dodgesDone;
        }
        this.status = bl3 ? "Dodging bomb" : "Repositioning";
    }

    private boolean meleeMethod() {
        return this.activeMethod == TobCombatMethod.MELEE || this.activeMethod == TobCombatMethod.DHAROKS;
    }

    private WorldPoint pickTimedStep(WorldPoint worldPoint, NPC nPC, boolean bl) {
        int n = this.attackRange();
        int n2 = Math.max(n, 2);
        boolean bl2 = this.meleeMethod();
        int n3 = bl ? 2 : 1;
        WorldPoint worldPoint2 = null;
        long l = Long.MIN_VALUE;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                long l2;
                long l3;
                long l4;
                int n4;
                boolean bl3;
                WorldPoint worldPoint3;
                int n5 = Math.max(Math.abs(i), Math.abs(j));
                if (n5 < n3 || n5 == 0 || this.isDangerTile(worldPoint3 = new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane()))) continue;
                try {
                    bl3 = Reachable.isWalkable(worldPoint3);
                }
                catch (Exception exception) {
                    bl3 = true;
                }
                if (!bl3 || (n4 = this.distToVerzikEdge(worldPoint3, nPC)) < 2 || n4 > (bl2 ? n2 : n) || (l4 = (l3 = bl2 ? this.orbitAlignment(worldPoint, worldPoint3, nPC) : 0L) * 100000L + (l2 = bl2 ? (long)(-Math.abs(n4 - n2)) * 50000L : 0L) + (long)this.minDistToDanger(worldPoint3) * 1000L + (this.visitedRecently(worldPoint3) ? 0L : 300L)) <= l) continue;
                l = l4;
                worldPoint2 = worldPoint3;
            }
        }
        return worldPoint2 != null ? worldPoint2 : this.pickDanceTile(worldPoint, nPC);
    }

    private void finalPhaseMove(int n, Player player, NPC nPC, WorldPoint worldPoint) {
        WorldPoint worldPoint2;
        boolean bl;
        NPC nPC2;
        if (this.config.handleWebs() && this.isWebActive() && !this.isBound() && (nPC2 = this.pickWebRunTile(worldPoint, nPC)) != null && !nPC2.equals((Object)worldPoint)) {
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement((WorldPoint)nPC2, true);
            this.lastMoveTick = n;
            this.pushRecentTile(worldPoint);
            this.status = "Running webs";
            return;
        }
        if (this.isEnraged(nPC)) {
            nPC2 = this.nearestTornado(worldPoint);
            boolean bl2 = bl = nPC2 != null && nPC2.getWorldLocation() != null && worldPoint.distanceTo(nPC2.getWorldLocation()) <= 2;
            if (bl && (worldPoint2 = this.fleeStep(worldPoint, nPC, nPC2.getWorldLocation())) != null && !worldPoint2.equals((Object)worldPoint)) {
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(worldPoint2, true);
                this.lastMoveTick = n;
                this.pushRecentTile(worldPoint);
                this.status = "Enrage: kiting tornado";
                return;
            }
        }
        boolean bl3 = this.onHazard(worldPoint);
        boolean bl4 = bl = this.config.dodgeBombs() && this.isDangerTile(worldPoint);
        if (!bl3 && !bl) {
            return;
        }
        worldPoint2 = this.pickDanceTile(worldPoint, nPC);
        if (worldPoint2 == null || worldPoint2.equals((Object)worldPoint)) {
            return;
        }
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint2, true);
        this.lastMoveTick = n;
        this.pushRecentTile(worldPoint);
        if (bl) {
            ++this.dodgesDone;
        }
        this.status = bl3 ? "Fleeing hazard" : "Dodging bomb";
    }

    private boolean isEnraged(NPC nPC) {
        try {
            int n = nPC.getHealthScale();
            int n2 = nPC.getHealthRatio();
            if (n > 0 && n2 >= 0) {
                return n2 * 100 / n <= 20;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.nearestTornado(this.client.getLocalPlayer() != null && this.client.getLocalPlayer().getWorldLocation() != null ? this.client.getLocalPlayer().getWorldLocation() : null) != null;
    }

    private NPC nearestTornado(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return null;
        }
        try {
            return NPCs.getNearest(nPC -> {
                if (nPC == null || nPC.isDead() || nPC.getWorldLocation() == null) {
                    return false;
                }
                if (nPC.getId() == 8386) {
                    return true;
                }
                if (this.isVerzik((NPC)nPC) || nPC.getId() == 8376 || nPC.getId() == 8379) {
                    return false;
                }
                NPCComposition nPCComposition = nPC.getTransformedComposition();
                int n = nPCComposition != null ? nPCComposition.getSize() : 1;
                return n <= 1 && worldPoint.distanceTo(nPC.getWorldLocation()) <= 5;
            });
        }
        catch (Exception exception) {
            return null;
        }
    }

    private WorldPoint fleeStep(WorldPoint worldPoint, NPC nPC, WorldPoint worldPoint2) {
        WorldPoint worldPoint3 = this.hazardLocation();
        WorldPoint worldPoint4 = null;
        long l = Long.MIN_VALUE;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                int n;
                long l2;
                boolean bl;
                WorldPoint worldPoint5;
                if (i == 0 && j == 0 || this.isDangerTile(worldPoint5 = new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane())) || worldPoint3 != null && worldPoint5.distanceTo(worldPoint3) == 0) continue;
                try {
                    bl = Reachable.isWalkable(worldPoint5);
                }
                catch (Exception exception) {
                    bl = true;
                }
                if (!bl) continue;
                long l3 = l2 = worldPoint2 != null ? (long)worldPoint5.distanceTo(worldPoint2) * 1000L : 0L;
                if (this.visitedRecently(worldPoint5)) {
                    l2 -= 200L;
                }
                if ((n = this.distToVerzikEdge(worldPoint5, nPC)) > 8) {
                    l2 -= 5000L;
                }
                if (l2 <= l) continue;
                l = l2;
                worldPoint4 = worldPoint5;
            }
        }
        return worldPoint4;
    }

    private WorldPoint hazardLocation() {
        try {
            TileObject tileObject = TileObjects.getNearest(32734);
            return tileObject != null ? tileObject.getWorldLocation() : null;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private boolean onHazard(WorldPoint worldPoint) {
        WorldPoint worldPoint2 = this.hazardLocation();
        return worldPoint2 != null && worldPoint.distanceTo(worldPoint2) == 0;
    }

    private boolean isWebActive() {
        if (this.client.getTickCount() < this.webWindowUntilTick) {
            return true;
        }
        try {
            WorldPoint worldPoint;
            Player player = this.client.getLocalPlayer();
            WorldPoint worldPoint2 = worldPoint = player != null ? player.getWorldLocation() : null;
            if (worldPoint == null) {
                return false;
            }
            NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 8376 && !nPC.isDead());
            return nPC2 != null && nPC2.getWorldLocation() != null && worldPoint.distanceTo(nPC2.getWorldLocation()) <= 2;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private boolean isBound() {
        return this.client.getTickCount() < this.boundUntilTick;
    }

    private WorldPoint pickWebRunTile(WorldPoint worldPoint, NPC nPC) {
        WorldPoint worldPoint2 = this.hazardLocation();
        WorldPoint worldPoint3 = null;
        long l = Long.MIN_VALUE;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                int n;
                boolean bl;
                WorldPoint worldPoint4;
                int n2 = Math.max(Math.abs(i), Math.abs(j));
                if (n2 == 0 || this.isDangerTile(worldPoint4 = new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane())) || worldPoint2 != null && worldPoint4.distanceTo(worldPoint2) == 0 || this.onWeb(worldPoint4)) continue;
                try {
                    bl = Reachable.isWalkable(worldPoint4);
                }
                catch (Exception exception) {
                    bl = true;
                }
                if (!bl || (n = this.distToVerzikEdge(worldPoint4, nPC)) < 2 || n > 8) continue;
                long l2 = this.orbitAlignment(worldPoint, worldPoint4, nPC);
                long l3 = l2 * 100000L + (long)n2 * 10000L + (this.visitedRecently(worldPoint4) ? 0L : 500L);
                if (l3 <= l) continue;
                l = l3;
                worldPoint3 = worldPoint4;
            }
        }
        if (worldPoint3 != null && this.orbitAlignment(worldPoint, worldPoint3, nPC) < 0L) {
            this.orbitDir = -this.orbitDir;
        }
        return worldPoint3;
    }

    private boolean onWeb(WorldPoint worldPoint) {
        try {
            NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 8376 && !nPC.isDead());
            return nPC2 != null && nPC2.getWorldLocation() != null && nPC2.getWorldLocation().distanceTo(worldPoint) == 0;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private void pushRecentTile(WorldPoint worldPoint) {
        this.recentTiles.addLast(worldPoint);
        while (this.recentTiles.size() > 3 && this.recentTiles.pollFirst() != null) {
        }
    }

    private boolean visitedRecently(WorldPoint worldPoint) {
        for (WorldPoint worldPoint2 : this.recentTiles) {
            if (!worldPoint2.equals((Object)worldPoint)) continue;
            return true;
        }
        return false;
    }

    private WorldPoint pickDanceTile(WorldPoint worldPoint, NPC nPC) {
        int n = this.attackRange();
        WorldPoint worldPoint2 = this.hazardLocation();
        boolean bl = this.inBombPhase() && worldPoint2 == null && this.activeMethod != TobCombatMethod.RANGED && this.activeMethod != TobCombatMethod.MAGIC;
        WorldPoint worldPoint3 = null;
        long l = Long.MIN_VALUE;
        long l2 = 0L;
        for (int[] nArray : DODGE_OFFSETS) {
            boolean bl2;
            WorldPoint worldPoint4 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (this.isDangerTile(worldPoint4) || worldPoint2 != null && worldPoint4.distanceTo(worldPoint2) == 0) continue;
            try {
                bl2 = Reachable.isWalkable(worldPoint4);
            }
            catch (Exception exception) {
                bl2 = true;
            }
            if (!bl2) continue;
            int n2 = this.distToVerzikEdge(worldPoint4, nPC);
            long l3 = bl ? this.orbitAlignment(worldPoint, worldPoint4, nPC) : 0L;
            long l4 = this.scoreDanceTile(worldPoint4, n2, n, l3);
            if (worldPoint2 != null) {
                l4 += (long)worldPoint4.distanceTo(worldPoint2) * 500L;
            }
            if (l4 <= l) continue;
            l = l4;
            worldPoint3 = worldPoint4;
            l2 = l3;
        }
        if (bl && worldPoint3 != null && l2 < 0L) {
            this.orbitDir = -this.orbitDir;
        }
        return worldPoint3;
    }

    private long orbitAlignment(WorldPoint worldPoint, WorldPoint worldPoint2, NPC nPC) {
        WorldPoint worldPoint3 = nPC.getWorldLocation();
        if (worldPoint3 == null) {
            return 0L;
        }
        int n = this.verzikSize(nPC);
        long l = 2L * (long)worldPoint.getX() - (2L * (long)worldPoint3.getX() + (long)(n - 1));
        long l2 = 2L * (long)worldPoint.getY() - (2L * (long)worldPoint3.getY() + (long)(n - 1));
        long l3 = worldPoint2.getX() - worldPoint.getX();
        long l4 = worldPoint2.getY() - worldPoint.getY();
        long l5 = l * l4 - l2 * l3;
        return (long)this.orbitDir * (long)Long.signum(l5);
    }

    private long scoreDanceTile(WorldPoint worldPoint, int n, int n2, long l) {
        long l2 = (long)this.minDistToDanger(worldPoint) * 1000L + (this.visitedRecently(worldPoint) ? 0L : 300L);
        long l3 = l * 100000L;
        if (n < 2) {
            return 1000000L + (long)n * 1000L + l2;
        }
        if (this.activeMethod == TobCombatMethod.RANGED || this.activeMethod == TobCombatMethod.MAGIC) {
            if (n <= n2) {
                return 3000000L + l2;
            }
            return 2000000L - (long)n * 1000L + l2;
        }
        int n3 = Math.max(n2, 2);
        if (n == n3) {
            return 3000000L + l3 + l2;
        }
        return 2000000L - (long)n * 1000L + l2;
    }

    private int minDistToDanger(WorldPoint worldPoint) {
        int n = 99;
        for (int[] nArray : this.dangerTiles) {
            int n2 = Math.max(Math.abs(nArray[0] - worldPoint.getX()), Math.abs(nArray[1] - worldPoint.getY()));
            if (n2 >= n) continue;
            n = n2;
        }
        return n;
    }

    private int attackRange() {
        String string;
        if (this.activeMethod == TobCombatMethod.RANGED || this.activeMethod == TobCombatMethod.MAGIC) {
            return 8;
        }
        Item item = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
        String string2 = string = item != null && item.getName() != null ? item.getName().toLowerCase() : "";
        if (string.contains("halberd")) {
            return 2;
        }
        return 1;
    }

    private boolean isDangerTile(WorldPoint worldPoint) {
        for (int[] nArray : this.dangerTiles) {
            if (nArray[0] != worldPoint.getX() || nArray[1] != worldPoint.getY() || nArray[2] != worldPoint.getPlane()) continue;
            return true;
        }
        return false;
    }

    private int distToVerzikEdge(WorldPoint worldPoint, NPC nPC) {
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        if (worldPoint2 == null) {
            return Integer.MAX_VALUE;
        }
        int n = this.verzikSize(nPC);
        int n2 = Math.max(Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)), 0);
        int n3 = Math.max(Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)), 0);
        return Math.max(n2, n3);
    }

    private int verzikSize(NPC nPC) {
        try {
            NPCComposition nPCComposition = nPC.getTransformedComposition();
            if (nPCComposition == null) {
                nPCComposition = nPC.getComposition();
            }
            if (nPCComposition != null && nPCComposition.getSize() > 0) {
                return nPCComposition.getSize();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return 3;
    }

    private void handleDamage(int n, int n2) {
        Object object3;
        if (n2 - this.lastOverloadTick < 5) {
            return;
        }
        if (n >= 20) {
            this.worstHitSeen = Math.max(this.worstHitSeen, n);
        }
        if (n >= 25) {
            this.lastBigHitTick = n2;
        }
        Object object2 = null;
        int n3 = Integer.MAX_VALUE;
        for (Object object3 : this.pendingImpacts) {
            int n4 = Math.abs(object3[1] - n2);
            if (n4 > 4 || n4 >= n3) continue;
            object2 = object3;
            n3 = n4;
        }
        if (object2 == null) {
            return;
        }
        this.pendingImpacts.remove(object2);
        void var5_6 = object2[0];
        object3 = (Object)this.learner.styleOf((int)var5_6);
        if (object3 == TobProjectileLearner.Style.NONE) {
            return;
        }
        if (this.activeOverhead != null && this.activeOverhead == object3.protect && this.learner.recordPrayedHit((int)var5_6, n)) {
            this.status = "Projectile " + (int)var5_6 + " reclassified \u2192 " + String.valueOf((Object)this.learner.styleOf((int)var5_6));
            log.info("[ToB] learner flipped projectile {} to {} after {} damage through {}", new Object[]{(int)var5_6, this.learner.styleOf((int)var5_6), n, this.activeOverhead});
        }
    }

    @Override
    protected int loop() {
        try {
            return this.loopBody();
        }
        catch (Exception exception) {
            log.warn("[ToB] loop error", (Throwable)exception);
            this.status = "Error: " + exception.getClass().getSimpleName();
            return 900;
        }
    }

    private int loopBody() {
        boolean bl;
        int n;
        if (this.client.getGameState() == GameState.LOADING) {
            return 1000;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.status = "Resuming...";
            return 1000;
        }
        if (RandomEventState.isActive()) {
            this.status = "Paused \u2014 random event";
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (Dialog.isViewingOptions() && (n = this.handleOptionDialog()) > 0) {
            return n;
        }
        if (Dialog.canContinue()) {
            this.status = "Continuing dialog";
            Dialog.continueSpace();
            return 600;
        }
        int n2 = n = player.getWorldLocation().getX() >= 6400 ? 1 : 0;
        if (n == 0) {
            this.handleExitEdge();
            if (!this.config.autoEnter()) {
                this.status = "Outside \u2014 auto-enter is off";
                return 1500;
            }
            return this.handleEntry(player);
        }
        this.wasInside = true;
        if (this.client.getBoostedSkillLevel(Skill.HITPOINTS) <= 0) {
            this.status = "Dead \u2014 waiting for respawn";
            return 900;
        }
        NPC nPC2 = NPCs.getNearest(nPC -> this.isVerzik((NPC)nPC) && !nPC.isDead());
        this.updatePhase(nPC2);
        if (this.complete) {
            if (!this.prayersOffAfterKill) {
                try {
                    Prayers.disableAll();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                this.clearPrayerBeliefs();
                this.prayersOffAfterKill = true;
            }
            this.status = "Complete \u2014 KC " + this.killCount + " (leave to re-enter)";
            return 2000;
        }
        if (nPC2 == null) {
            this.status = this.wasFighting ? "Verzik down \u2014 waiting" : "Waiting for Verzik...";
            return 800;
        }
        if (nPC2.getId() == 8369 && !this.started && this.config.startEncounter()) {
            this.resolveMethod(player);
            if (this.client.getTickCount() - this.lastTalkTick >= 8) {
                this.status = "Talking to Verzik to start";
                nPC2.interact("Talk-to");
                this.lastTalkTick = this.client.getTickCount();
            } else {
                this.status = "Starting encounter...";
            }
            return 900;
        }
        if (!this.methodResolved) {
            this.resolveMethod(player);
        }
        if (this.started && !this.openingWalkDone) {
            this.status = "Moving into position";
            return 300;
        }
        if (this.hpPercent() < this.emergencyThresholdPct() && this.findFood() != null) {
            if (this.specActive) {
                this.specResolved();
            }
            return this.eatFood();
        }
        if (this.specActive) {
            return this.runSpec(nPC2);
        }
        if (this.shouldEat()) {
            return this.eatFood();
        }
        if (this.shouldRestore()) {
            return this.drinkRestore();
        }
        if (this.config.drinkOverload() && !this.overloadDrunk && this.started && this.drinkOverload()) {
            return 1200;
        }
        boolean bl2 = bl = !this.config.specWeapon().trim().isEmpty() && (this.byName(this.config.specWeapon()) != null || this.isInWeaponSlot(this.config.specWeapon()));
        if (this.config.enableSpec() && bl && this.specSafe() && this.isFightingVerzik(player) && this.client.getTickCount() >= this.stunnedUntilTick && this.specEnergy() >= this.config.specAtPct() * 10 && this.client.getTickCount() - this.lastSpecTick >= 4 && this.safeSpecWindow()) {
            this.beginSpec();
            return this.runSpec(nPC2);
        }
        if (this.client.getTickCount() < this.stunnedUntilTick) {
            this.status = "Stunned";
            return 600;
        }
        if (this.lastMoveTick == this.client.getTickCount() && this.isFightingVerzik(player)) {
            this.status = "Dodging bomb";
            return 300;
        }
        return this.attackVerzik(nPC2, player);
    }

    private void resolveMethod(Player player) {
        TobCombatMethod tobCombatMethod = this.config.combatMethod();
        if (tobCombatMethod != TobCombatMethod.AUTO) {
            this.activeMethod = tobCombatMethod;
        } else {
            Item item = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
            this.activeMethod = TobCombatMethod.detect(item, Inventory.getAll(), this.dharokSetWorn());
        }
        this.methodResolved = true;
    }

    private boolean dharokSetWorn() {
        int n = 0;
        for (EquipmentInventorySlot equipmentInventorySlot : new EquipmentInventorySlot[]{EquipmentInventorySlot.HEAD, EquipmentInventorySlot.BODY, EquipmentInventorySlot.LEGS}) {
            Item item = Equipment.fromSlot(equipmentInventorySlot);
            if (item == null || item.getName() == null || !item.getName().toLowerCase().contains("dharok's")) continue;
            ++n;
        }
        return n >= 3;
    }

    private String phaseWeapon() {
        boolean bl;
        if (this.activeMethod == TobCombatMethod.DHAROKS) {
            return "Dharok's greataxe";
        }
        boolean bl2 = bl = this.activeMethod == TobCombatMethod.MELEE;
        if (bl && this.inBombPhase() && !this.config.flyingWeapon().trim().isEmpty()) {
            return this.config.flyingWeapon();
        }
        String string = this.config.groundedWeapon();
        if ((string == null || string.trim().isEmpty()) && this.preSpecWeapon != null) {
            if (this.isInWeaponSlot(this.preSpecWeapon)) {
                this.preSpecWeapon = null;
                return string;
            }
            return this.preSpecWeapon;
        }
        return string;
    }

    private String phaseOffhand() {
        if (this.activeMethod == TobCombatMethod.DHAROKS) {
            return "";
        }
        if (this.activeMethod == TobCombatMethod.MELEE && this.inBombPhase() && !this.config.flyingWeapon().trim().isEmpty()) {
            return "";
        }
        if ((this.inBombPhase() || this.inFinalPhase()) && this.config.groundedWeapon().trim().isEmpty()) {
            return "";
        }
        return this.config.offHand();
    }

    private void maintainPrayers(int n) {
        Prayer prayer;
        int n2 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        if (n2 <= 0) {
            this.clearPrayerBeliefs();
            this.lastPrayerPoints = n2;
            return;
        }
        if (n2 < this.lastPrayerPoints) {
            this.lastPrayerDrainTick = n;
        }
        this.lastPrayerPoints = n2;
        if (this.activeOverhead != null && n - this.lastPrayerDrainTick > 25) {
            ++this.watchdogTrips;
            if (!this.prayerViaWidgets && this.watchdogTrips >= 2) {
                this.prayerViaWidgets = true;
                log.info("[ToB] escalating prayer toggles to the widget-541 path after {} watchdog trips", (Object)this.watchdogTrips);
            }
            log.info("[ToB] prayer-drain watchdog: no drain for {} ticks \u2014 re-asserting prayers", (Object)(n - this.lastPrayerDrainTick));
            this.clearPrayerBeliefs();
            this.lastPrayerDrainTick = n;
        }
        if ((prayer = this.desiredOverhead(n)) != null) {
            boolean bl;
            boolean bl2 = bl = prayer != this.activeOverhead;
            if (bl || !Prayers.isEnabled(prayer) && n > this.lastOverheadToggleTick) {
                this.togglePrayer(prayer);
                this.activeOverhead = prayer;
                this.lastOverheadToggleTick = n;
                if (bl) {
                    this.lastPrayerDrainTick = n;
                }
            }
        }
        OffensivePrayer offensivePrayer = this.config.offensivePrayer();
        if (prayer == null && offensivePrayer != null && offensivePrayer.prayer != null) {
            boolean bl;
            boolean bl3 = bl = offensivePrayer.prayer != this.activeOffensive;
            if (bl || !Prayers.isEnabled(offensivePrayer.prayer) && n > this.lastOffensiveToggleTick) {
                this.togglePrayer(offensivePrayer.prayer);
                this.activeOffensive = offensivePrayer.prayer;
                this.lastOffensiveToggleTick = n;
            }
        }
    }

    private void togglePrayer(Prayer prayer) {
        int n;
        if (this.prayerViaWidgets && (n = this.prayerWidgetChild(prayer)) >= 0) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction1Packet(0x21D0000 | n, -1, -1);
            return;
        }
        Prayers.toggle(prayer);
    }

    private int prayerWidgetChild(Prayer prayer) {
        switch (prayer) {
            case PROTECT_FROM_MAGIC: {
                return 17;
            }
            case PROTECT_FROM_MISSILES: {
                return 18;
            }
            case PROTECT_FROM_MELEE: {
                return 19;
            }
        }
        return -1;
    }

    private Prayer desiredOverhead(int n) {
        if (this.config.reactivePrayer() && this.lastThreatProjId >= 0 && n - this.lastThreatTick <= 6) {
            TobProjectileLearner.Style style = this.learner.styleOf(this.lastThreatProjId);
            if (style.protect != null) {
                return style.protect;
            }
        }
        return this.config.restingOverhead().prayer;
    }

    private void clearPrayerBeliefs() {
        this.activeOverhead = null;
        this.activeOffensive = null;
    }

    private boolean prayersProven() {
        return this.client.getTickCount() - this.lastBigHitTick > 25;
    }

    private int hpPercent() {
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        if (n <= 0) {
            return 100;
        }
        return this.client.getBoostedSkillLevel(Skill.HITPOINTS) * 100 / n;
    }

    private int prayerPercent() {
        int n = this.client.getRealSkillLevel(Skill.PRAYER);
        if (n <= 0) {
            return 100;
        }
        return this.client.getBoostedSkillLevel(Skill.PRAYER) * 100 / n;
    }

    private int emergencyThresholdPct() {
        if (this.dharokRideActive()) {
            return Math.min(this.config.emergencyHpPct(), this.config.dharokFloorPct());
        }
        return this.config.emergencyHpPct();
    }

    private boolean dharokRideActive() {
        return this.activeMethod == TobCombatMethod.DHAROKS && this.started && this.prayersProven() && !this.inBombPhase() && !this.inFinalPhase();
    }

    private boolean shouldEat() {
        int n;
        if (this.findFood() == null) {
            return false;
        }
        int n2 = this.hpPercent();
        int n3 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        if (this.sapPending && this.config.panicBrewOnSap() && n2 < 85) {
            return true;
        }
        if (this.config.handleWebs() && this.isBound() && n2 < 60) {
            return true;
        }
        if (this.dharokRideActive()) {
            if (this.dharokTopping) {
                if (n2 >= this.config.dharokCeilPct()) {
                    this.dharokTopping = false;
                    return false;
                }
                return this.eatThrottleOk();
            }
            if (n2 < this.config.dharokFloorPct()) {
                this.dharokTopping = true;
                return this.eatThrottleOk();
            }
            return false;
        }
        boolean bl = this.started && this.client.getTickCount() - this.lastBigHitTick <= 10;
        int n4 = n = this.inFinalPhase() ? Math.max(this.worstHitSeen, 64) : this.worstHitSeen;
        if ((bl || this.inFinalPhase()) && n3 <= n + 12) {
            return true;
        }
        if (n2 >= this.config.eatBelowHpPct()) {
            return false;
        }
        return this.eatThrottleOk();
    }

    private boolean eatThrottleOk() {
        return this.client.getTickCount() - this.lastEatTick >= 2;
    }

    private Item findFood() {
        Item item2 = this.byName(this.config.foodName());
        if (item2 == null && !this.config.backupFood().trim().isEmpty()) {
            item2 = this.byName(this.config.backupFood());
        }
        if (item2 == null) {
            item2 = Inventory.getFirst(this::hasEatAction);
        }
        if (item2 == null) {
            item2 = Inventory.getFirst(item -> {
                if (item == null || item.getName() == null) {
                    return false;
                }
                String string = item.getName().toLowerCase();
                return string.contains("saradomin brew") || string.contains("ancient brew") || string.contains("xeric");
            });
        }
        return item2;
    }

    private boolean hasEatAction(Item item) {
        if (item == null) {
            return false;
        }
        try {
            ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
            if (itemComposition == null || itemComposition.getInventoryActions() == null) {
                return false;
            }
            for (String string : itemComposition.getInventoryActions()) {
                if (string == null || !string.equalsIgnoreCase("Eat")) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private int eatFood() {
        Item item = this.findFood();
        if (item == null) {
            this.status = "Out of food!";
            return 600;
        }
        this.status = "Eating " + item.getName();
        MousePackets.queueClickPacket();
        ItemPackets.itemFirstOption(item);
        this.sapPending = false;
        this.lastEatTick = this.client.getTickCount();
        return this.jitter(600);
    }

    private boolean shouldRestore() {
        if (this.config.restoreItem().trim().isEmpty()) {
            return false;
        }
        if (this.byName(this.config.restoreItem()) == null) {
            return false;
        }
        return this.prayerPercent() < this.config.restoreBelowPct();
    }

    private int drinkRestore() {
        Item item = this.byName(this.config.restoreItem());
        if (item == null) {
            return 300;
        }
        this.status = "Drinking " + item.getName();
        MousePackets.queueClickPacket();
        ItemPackets.itemFirstOption(item);
        return this.jitter(900);
    }

    private boolean drinkOverload() {
        Item item = this.byName(this.config.overloadName());
        if (item == null) {
            this.overloadDrunk = true;
            return false;
        }
        if (this.hpPercent() < this.config.overloadMinHpPct()) {
            return false;
        }
        if (!this.noBombPending(2)) {
            return false;
        }
        this.status = "Drinking " + item.getName();
        MousePackets.queueClickPacket();
        ItemPackets.itemFirstOption(item);
        this.overloadDrunk = true;
        this.lastOverloadTick = this.client.getTickCount();
        return true;
    }

    private boolean specSafe() {
        return this.hpPercent() >= this.emergencyThresholdPct();
    }

    private int specEnergy() {
        return this.client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
    }

    private int runSpec(NPC nPC) {
        boolean bl;
        String string = this.config.specWeapon();
        if (!this.isInWeaponSlot(string)) {
            Item item = this.byName(string);
            if (item == null) {
                this.specResolved();
                return 300;
            }
            String string2 = string.toLowerCase();
            int n = this.equipAttempts.getOrDefault(string2, 0);
            if (n >= 4) {
                this.specResolved();
                return 300;
            }
            int n2 = this.client.getTickCount();
            if (n2 - this.lastEquipTick < 2) {
                return 300;
            }
            this.equipAttempts.put(string2, ++n);
            this.lastEquipTick = n2;
            this.equipItem(item, n);
            this.status = "Spec: equipping " + string;
            return 600;
        }
        this.equipAttempts.remove(string.toLowerCase());
        Player player = this.client.getLocalPlayer();
        boolean bl2 = bl = this.specStartEnergy >= 0 && this.specEnergy() <= this.specStartEnergy - 100 || player != null && player.getAnimation() == 7642;
        if (bl) {
            this.specResolved();
            return 600;
        }
        if (this.specEnergy() < this.config.specAtPct() * 10) {
            this.specResolved();
            return 300;
        }
        if (this.client.getTickCount() - this.specStartTick > 8) {
            this.specResolved();
            return 300;
        }
        if (!this.specEnableSent) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(38862884, -1);
            this.specEnableSent = true;
        }
        if (player == null || !this.isFightingVerzik(player)) {
            nPC.interact("Attack");
        }
        this.status = "Spec: firing " + string;
        return 300;
    }

    private void beginSpec() {
        Item item = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
        String string = item != null ? item.getName() : null;
        String string2 = this.config.specWeapon().trim().toLowerCase();
        if (string != null && !string2.isEmpty() && !string.toLowerCase().contains(string2)) {
            this.preSpecWeapon = string;
        }
        this.specActive = true;
        this.specEnableAttempts = 0;
        this.specStartEnergy = this.specEnergy();
        this.specStartTick = this.client.getTickCount();
        this.specEnableSent = false;
    }

    private void specResolved() {
        this.specActive = false;
        this.lastSpecTick = this.client.getTickCount();
        this.specStartEnergy = -1;
        this.specStartTick = -100;
        this.specEnableSent = false;
    }

    private boolean safeSpecWindow() {
        NPC nPC2;
        if (this.client.getTickCount() < this.boundUntilTick) {
            return false;
        }
        if (this.isWebActive()) {
            return false;
        }
        if (!this.noBombPending(2)) {
            return false;
        }
        return !this.inFinalPhase() || (nPC2 = NPCs.getNearest(nPC -> this.isVerzik((NPC)nPC) && !nPC.isDead())) == null || !this.isEnraged(nPC2);
    }

    private boolean noBombPending(int n) {
        int n2 = this.client.getTickCount();
        for (int[] nArray : this.pendingImpacts) {
            if (nArray[1] < n2 || nArray[1] - n2 > n) continue;
            return false;
        }
        return true;
    }

    private int attackVerzik(NPC nPC, Player player) {
        int n = this.trySwapWeapon();
        if (n > 0) {
            return n;
        }
        int n2 = this.client.getTickCount();
        if (this.isFightingVerzik(player) && !nPC.isDead()) {
            this.status = "Fighting Verzik (" + String.valueOf((Object)this.phase) + ", " + String.valueOf((Object)this.activeMethod) + ")";
            return this.jitter(600);
        }
        if (n2 == this.lastEngageTick) {
            return 300;
        }
        this.status = "Attacking Verzik";
        nPC.interact("Attack");
        this.lastEngageTick = n2;
        return this.jitter(600);
    }

    private int trySwapWeapon() {
        Item item;
        int n = this.tryEquip(this.phaseWeapon());
        if (n > 0) {
            return n;
        }
        String string = this.phaseOffhand();
        if (string != null && !string.trim().isEmpty() && !this.isInShieldSlot(string) && (item = this.byName(string)) != null) {
            return this.tryEquip(string);
        }
        return 0;
    }

    private int tryEquip(String string) {
        if (string == null || string.trim().isEmpty()) {
            return 0;
        }
        if (this.isInWeaponSlot(string) || this.isInShieldSlot(string)) {
            this.equipAttempts.remove(string.toLowerCase());
            return 0;
        }
        Item item = this.byName(string);
        if (item == null) {
            return 0;
        }
        String string2 = string.toLowerCase();
        int n = this.equipAttempts.getOrDefault(string2, 0);
        if (n >= 4) {
            return 0;
        }
        int n2 = this.client.getTickCount();
        if (n2 - this.lastEquipTick < 2) {
            return 0;
        }
        this.equipAttempts.put(string2, ++n);
        this.lastEquipTick = n2;
        this.equipItem(item, n);
        this.status = "Equipping " + item.getName() + " (" + n + "/4)";
        return 600;
    }

    private void equipItem(Item item, int n) {
        if (n % 2 == 1) {
            MousePackets.queueClickPacket();
            ItemPackets.itemFirstOption(item);
        } else {
            item.interact(this.resolveEquipAction(item));
        }
    }

    private void resetEquipGuard() {
        this.equipAttempts.clear();
    }

    private String resolveEquipAction(Item item) {
        try {
            ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
            if (itemComposition != null && itemComposition.getInventoryActions() != null) {
                for (String string : itemComposition.getInventoryActions()) {
                    String string2;
                    if (string == null || !(string2 = string.toLowerCase()).equals("wield") && !string2.equals("wear") && !string2.equals("equip")) continue;
                    return string;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "Wield";
    }

    private boolean isInWeaponSlot(String string) {
        return this.slotMatches(EquipmentInventorySlot.WEAPON, string);
    }

    private boolean isInShieldSlot(String string) {
        return this.slotMatches(EquipmentInventorySlot.SHIELD, string);
    }

    private boolean slotMatches(EquipmentInventorySlot equipmentInventorySlot, String string) {
        if (string == null || string.trim().isEmpty()) {
            return true;
        }
        Item item = Equipment.fromSlot(equipmentInventorySlot);
        return item != null && item.getName() != null && item.getName().toLowerCase().contains(string.toLowerCase());
    }

    private void handleExitEdge() {
        if (!this.wasInside) {
            return;
        }
        this.wasInside = false;
        this.status = this.complete ? "Run over \u2014 resetting" : "Outside \u2014 resetting run";
        this.resetRunState();
    }

    private int handleEntry(Player player) {
        TileObject tileObject = TileObjects.getNearest(32653);
        if (tileObject == null) {
            this.status = "Outside \u2014 walk to the ToB entrance";
            return 2000;
        }
        int n = this.client.getTickCount();
        if (!this.inParty(player)) {
            Widget widget = this.client.getWidget(364, 66);
            if (widget != null && !widget.isHidden()) {
                if (n - this.lastMakePartyTick >= 3) {
                    this.status = "Making a solo party";
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction1Packet(widget.getId(), -1, -1);
                    this.lastMakePartyTick = n;
                }
                return 900;
            }
            TileObject tileObject2 = TileObjects.getNearest(32655);
            if (tileObject2 != null && n - this.lastBoardTick >= 5) {
                this.status = "Reading the notice board";
                tileObject2.interact("Read");
                this.lastBoardTick = n;
                return 1200;
            }
            this.status = "Waiting for the notice board...";
            return 900;
        }
        Widget widget = this.client.getWidget(50, 1);
        if (widget != null && !widget.isHidden()) {
            this.status = "Closing the party panel";
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction1Packet(widget.getId(), -1, -1);
            return 900;
        }
        if (n - this.lastEnterTick >= 5) {
            this.status = "Entering the Theatre";
            tileObject.interact("Enter");
            this.lastEnterTick = n;
            return 1500;
        }
        this.status = "Entering...";
        return 900;
    }

    private boolean inParty(Player player) {
        Widget widget = this.client.getWidget(28, 12);
        if (widget == null || widget.getText() == null) {
            return false;
        }
        String string = player.getName();
        if (string == null || string.isEmpty()) {
            return false;
        }
        String string2 = TobPlugin.stripNbsp(widget.getText()).toLowerCase();
        return string2.contains(TobPlugin.stripNbsp(string).toLowerCase());
    }

    private static String stripNbsp(String string) {
        return string.replace('\u00a0', ' ');
    }

    private int handleOptionDialog() {
        if (this.config.startEncounter() && !this.started && !this.config.startDialogOption().trim().isEmpty() && Dialog.chooseOption(this.config.startDialogOption().trim())) {
            this.status = "Confirming start";
            this.lastTalkTick = this.client.getTickCount();
            return 900;
        }
        if (Dialog.hasOption("Do not resign") && Dialog.chooseOption("Do not resign")) {
            this.status = "Declining resign";
            return 600;
        }
        this.status = "Unhandled dialog option";
        return 0;
    }

    private boolean isVerzik(NPC nPC) {
        if (nPC == null) {
            return false;
        }
        int n = nPC.getId();
        for (int n2 : TobConstants.VERZIK_IDS) {
            if (n != n2) continue;
            return true;
        }
        return false;
    }

    private boolean inBombPhase() {
        return this.phase == TobPhase.P3_FLYING;
    }

    private boolean inFinalPhase() {
        return this.phase == TobPhase.P3_ENRAGED;
    }

    private void updatePhase(NPC nPC) {
        if (nPC != null) {
            this.wasFighting = true;
            this.lastVerzikSeenTick = this.client.getTickCount();
            boolean bl = this.started;
            switch (nPC.getId()) {
                case 8369: {
                    this.phase = TobPhase.P1_THRONE;
                    break;
                }
                case 8371: {
                    this.phase = TobPhase.P2_SPIDER;
                    this.started = true;
                    break;
                }
                case 8372: {
                    this.phase = TobPhase.P3_FLYING;
                    this.started = true;
                    break;
                }
                case 8374: {
                    this.phase = TobPhase.P3_ENRAGED;
                    this.started = true;
                    break;
                }
            }
            if (!bl && this.started) {
                this.lastBigHitTick = this.client.getTickCount();
                int n = this.config.openingWalkTiles();
                Player player = this.client.getLocalPlayer();
                if (n > 0 && !this.openingWalkDone && player != null && player.getWorldLocation() != null) {
                    WorldPoint worldPoint = player.getWorldLocation();
                    this.openingTarget = new WorldPoint(worldPoint.getX(), worldPoint.getY() - n, worldPoint.getPlane());
                    this.openingWalkStartTick = this.client.getTickCount();
                } else {
                    this.openingWalkDone = true;
                }
            }
            return;
        }
        if (this.wasFighting && this.started && !this.complete && this.phase != TobPhase.DEAD && this.client.getBoostedSkillLevel(Skill.HITPOINTS) > 0 && this.lastVerzikSeenTick >= 0 && this.client.getTickCount() - this.lastVerzikSeenTick >= 4) {
            this.phase = TobPhase.DEAD;
            this.status = "Verzik defeated";
        }
    }

    private boolean isFightingVerzik(Player player) {
        Actor actor = player.getInteracting();
        return actor instanceof NPC && this.isVerzik((NPC)actor);
    }

    private Item byName(String string) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        String string2 = string.toLowerCase();
        return Inventory.getFirst(item -> item != null && item.getName() != null && item.getName().toLowerCase().contains(string2));
    }

    private boolean isKnownProjectile(int n) {
        return this.styleMap != null && this.styleMap.containsKey(n);
    }

    private Map<Integer, TobProjectileLearner.Style> buildStyleMap() {
        int n;
        Map<Integer, TobProjectileLearner.Style> map = TobConstants.defaultProjectileStyles();
        Object object = this.parseIds(this.config.rangedProjIds()).iterator();
        while (object.hasNext()) {
            n = object.next();
            map.put(n, TobProjectileLearner.Style.RANGED);
        }
        object = this.parseIds(this.config.magicProjIds()).iterator();
        while (object.hasNext()) {
            n = object.next();
            map.put(n, TobProjectileLearner.Style.MAGIC);
        }
        object = this.parseIds(this.config.ignoredProjIds()).iterator();
        while (object.hasNext()) {
            n = object.next();
            map.put(n, TobProjectileLearner.Style.NONE);
        }
        for (Object object2 : (Object)TobConstants.GROUND_TRUTH_MAGIC) {
            map.put((int)object2, TobProjectileLearner.Style.MAGIC);
        }
        for (Object object2 : (Object)TobConstants.GROUND_TRUTH_MECHANIC) {
            map.put((int)object2, TobProjectileLearner.Style.NONE);
        }
        return map;
    }

    private Set<Integer> parseIds(String string) {
        HashSet<Integer> hashSet = new HashSet<Integer>();
        if (string == null) {
            return hashSet;
        }
        for (String string2 : string.split(",")) {
            if ((string2 = string2.trim()).isEmpty()) continue;
            try {
                hashSet.add(Integer.parseInt(string2));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return hashSet;
    }

    private int jitter(int n) {
        int n2 = n * 12 / 100;
        return n2 <= 0 ? n : n + Rand.nextInt(-n2, n2);
    }

    private void resetRunState() {
        this.phase = TobPhase.IDLE;
        this.started = false;
        this.complete = false;
        this.wasFighting = false;
        this.methodResolved = false;
        this.lastVerzikSeenTick = -1;
        this.overloadDrunk = false;
        this.sapPending = false;
        this.dharokTopping = false;
        this.specActive = false;
        this.specEnableAttempts = 0;
        this.lastSpecTick = -100;
        this.preSpecWeapon = null;
        this.prayersOffAfterKill = false;
        this.openingWalkDone = false;
        this.openingTarget = null;
        this.openingWalkStartTick = -100;
        this.dodgeTileA = null;
        this.dodgeTileB = null;
        this.webWindowUntilTick = -100;
        this.boundUntilTick = -100;
        this.lastTalkTick = -100;
        this.lastEatTick = -100;
        this.lastOverloadTick = -100;
        this.clearPrayerBeliefs();
        this.lastOverheadToggleTick = -1;
        this.lastOffensiveToggleTick = -1;
        this.lastThreatProjId = -1;
        this.lastThreatTick = -100;
        this.pendingImpacts.clear();
        this.lastLaunchTickById.clear();
        this.dangerTiles.clear();
        this.recentTiles.clear();
        this.stunnedUntilTick = -100;
        this.lastMoveTick = -100;
        this.orbitDir = 1;
        this.lastBigHitTick = -100;
        this.worstHitSeen = 47;
        this.deathLatched = false;
        this.resetEquipGuard();
        this.lastEquipTick = -100;
        this.lastEngageTick = -1;
    }

    private void resetState() {
        this.resetRunState();
        this.status = "Idle";
        this.wasInside = false;
        this.lastBoardTick = -100;
        this.lastMakePartyTick = -100;
        this.lastEnterTick = -100;
        this.loggedInLastTick = false;
        this.lastTickHp = -1;
        this.lastPrayerPoints = -1;
        this.lastPrayerDrainTick = -100;
        this.lastBossLaunchTick = -100;
    }

    public TobPhase getPhase() {
        return this.phase;
    }

    public String getStatus() {
        return this.status;
    }

    public int getKillCount() {
        return this.killCount;
    }

    public int getDeathCount() {
        return this.deathCount;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public TobCombatMethod getActiveMethod() {
        return this.activeMethod;
    }

    public int getWorstHitSeen() {
        return this.worstHitSeen;
    }

    public boolean arePrayersProven() {
        return this.prayersProven();
    }

    public String getLearnerReadout() {
        return this.learner == null ? "-" : this.learner.describeFlips();
    }

    public int getDodgesDone() {
        return this.dodgesDone;
    }

    public boolean isDodgeFutile() {
        return this.dodgeFutile;
    }

    public boolean isPrayerViaWidgets() {
        return this.prayerViaWidgets;
    }

    public boolean isStunned() {
        return this.client.getTickCount() < this.stunnedUntilTick;
    }

    public int getSpecEnergyPercent() {
        try {
            return this.specEnergy() / 10;
        }
        catch (Exception exception) {
            return -1;
        }
    }

    public String getOverheadReadout() {
        Prayer prayer = this.activeOverhead;
        if (prayer == null) {
            return "none";
        }
        boolean bl = false;
        try {
            bl = Prayers.isEnabled(prayer);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return prayer.name().replace("PROTECT_FROM_", "") + (bl ? " (on)" : " (unverified)");
    }
}

