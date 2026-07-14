/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.CollisionData
 *  net.runelite.api.DecorativeObject
 *  net.runelite.api.GameObject
 *  net.runelite.api.GameState
 *  net.runelite.api.GroundObject
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.Tile
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.moons;

import com.google.inject.Binder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.combat.CombatPotion;
import net.runelite.client.plugins.svc.combat.DefensivePrayer;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.moons.MoonsConfig;
import net.runelite.client.plugins.svc.moons.MoonsOverlay;
import net.runelite.client.plugins.svc.moons.MoonsPhase;
import net.runelite.client.plugins.svc.moons.MoonsResupplyState;
import net.runelite.client.plugins.svc.moons.TargetMoon;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Moons of Peril</font></nobr></html>", description="Boss bot for the Moons of Peril (Blood/Blue/Eclipse) \u2014 fixed prayer, food, potions, special dodging, glider resupply", tags={"orion", "moons", "peril", "perilous", "boss", "combat", "bot"}, enabledByDefault=false)
public class MoonsPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MoonsConfig config;
    private MoonsOverlay overlay;
    private MoonsPhase phase = MoonsPhase.IDLE;
    private String status = "Idle";
    private int killCount = 0;
    private int attackCount = 0;
    private long startTime = 0L;
    private int deathCount = 0;
    private long fightStartMs = 0L;
    private long bestBossMs = 0L;
    private long worstBossMs = 0L;
    private long totalBossMs = 0L;
    private int bossTimedCount = 0;
    private boolean deathCounted = false;
    private NPC currentBoss = null;
    private WorldPoint lastBossLocation = null;
    private boolean bossWasPresent = false;
    private int bossAbsentSince = -1;
    private WorldPoint killLocation = null;
    private volatile boolean pendingKill = false;
    private int respawnWaitStart = -1;
    private MoonsResupplyState resupplyState = null;
    private boolean playerDied = false;
    private int lootingEntryTick = -1;
    private int gliderSearchSentTick = -1;
    private BankStep bankStep = BankStep.FOUNTAIN;
    private volatile boolean presetLoadConfirmed = false;
    private volatile boolean lockedPresetDetected = false;
    private int bankOpenMisses = 0;
    private long bankVerifyAt = 0L;
    private long bankOpenAt = 0L;
    private long bankHomeTeleAt = 0L;
    private volatile int blueFrostStormUntilTick = -1;
    private WorldPoint lastTickPos = null;
    private int stuckTicks = 0;
    private WorldPoint brazierTarget = null;
    private int brazierTargetTicks = 0;
    private final Map<WorldPoint, Integer> brazierCooldownUntil = new HashMap<WorldPoint, Integer>();
    private int cloneRotationIdx = 0;
    private long lastCombatPotMs = 0L;
    private BufferedWriter dbgWriter;
    private boolean dbgInit = false;
    private boolean loggedInLastTick = false;
    private long muralClickAt = 0L;
    private long muralOptionAt = 0L;
    private WorldPoint muralEntryPos = null;
    private volatile boolean enteredChamber = false;
    private volatile int eclipseShieldUntilTick = -1;
    private volatile int eclipseClonesUntilTick = -1;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(MoonsConfig.class).toInstance((Object)((MoonsConfig)configManager.getConfig(MoonsConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new MoonsOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
        this.startTime = System.currentTimeMillis();
        this.status = "Starting";
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        if (this.dbgWriter != null) {
            try {
                this.dbgWriter.flush();
                this.dbgWriter.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            this.dbgWriter = null;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGGED_IN) {
            this.loggedInLastTick = false;
        } else if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
        } else if (gameState == GameState.LOADING && (this.phase == MoonsPhase.FIGHTING || this.phase == MoonsPhase.DODGING)) {
            this.registerDeath();
            if (this.config.enableResupply()) {
                this.phase = MoonsPhase.RESUPPLYING;
                this.resupplyState = MoonsResupplyState.LOOTING;
                this.playerDied = true;
                this.lootingEntryTick = -1;
                this.status = "Died \u2014 returning via resupply";
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (this.client.getGameState() == GameState.LOGGED_IN && this.client.getBoostedSkillLevel(Skill.HITPOINTS) == 0 && (this.phase == MoonsPhase.FIGHTING || this.phase == MoonsPhase.DODGING)) {
            this.registerDeath();
            if (this.config.enableResupply()) {
                this.phase = MoonsPhase.RESUPPLYING;
                this.resupplyState = MoonsResupplyState.LOOTING;
                this.playerDied = true;
                this.lootingEntryTick = -1;
                this.status = "Died \u2014 returning via resupply";
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        int n;
        if (chatMessage.getMessage() == null) {
            return;
        }
        String string = chatMessage.getMessage().replaceAll("<[^>]*>", "").toLowerCase();
        if (string.contains("kill count is") && (this.phase == MoonsPhase.FIGHTING || this.phase == MoonsPhase.DODGING)) {
            this.pendingKill = true;
        }
        if (string.contains("inventory was loaded successfully") || string.contains("equipment was loaded successfully")) {
            this.presetLoadConfirmed = true;
        }
        if (string.contains("may not load locked presets") || string.contains("locked preset")) {
            this.lockedPresetDetected = true;
        }
        if (this.config.targetMoon() == TargetMoon.BLUE && (string.contains("relight the braziers") || string.contains("frost storm") || string.contains("absorbs energy from the extinguished braziers")) && (n = this.safeTickCount()) >= 0) {
            this.blueFrostStormUntilTick = n + 8;
        }
        if (string.contains("you enter the") && string.contains("chamber")) {
            this.enteredChamber = true;
        }
        if (this.config.targetMoon() == TargetMoon.ECLIPSE) {
            n = this.safeTickCount();
            if (string.contains("clones close in") || string.contains("face each one to parry") || string.contains("clone strikes you")) {
                this.eclipseClonesUntilTick = n + 4;
            }
            if (string.contains("moon shield") || string.contains("searing rays") || string.contains("shielded") && string.contains("cannot harm")) {
                this.eclipseShieldUntilTick = n + 5;
            }
        }
    }

    @Override
    protected int loop() {
        boolean bl;
        boolean bl2;
        NPC nPC;
        if (this.client.getGameState() == GameState.LOADING) {
            return 1000;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.status = "Resuming...";
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        this.currentBoss = nPC = this.findBoss();
        this.updatePhase(nPC, player);
        boolean bl3 = bl2 = nPC != null || this.phase == MoonsPhase.FIGHTING || this.phase == MoonsPhase.DODGING || this.phase == MoonsPhase.ENTERING_ROOM;
        if (bl2) {
            this.maintainPrayer();
        } else if (Prayers.anyActive()) {
            Prayers.disableAll();
        }
        if (this.phase == MoonsPhase.DEAD) {
            this.phase = MoonsPhase.LOOTING;
            this.lootingEntryTick = -1;
        }
        if (this.phase == MoonsPhase.LOOTING) {
            return this.handleLooting(player);
        }
        if (this.phase == MoonsPhase.WAITING_RESPAWN) {
            return this.handleWaitRespawn(player, nPC);
        }
        if (this.phase == MoonsPhase.RESUPPLYING) {
            return this.handleResupply(player);
        }
        boolean bl4 = bl = nPC != null || this.phase == MoonsPhase.FIGHTING || this.phase == MoonsPhase.DODGING;
        if (bl) {
            int n;
            int n2;
            WorldPoint worldPoint;
            if (this.shouldEat(player)) {
                return this.eatFood();
            }
            if (this.shouldRestorePrayer()) {
                return this.drinkPrayerRestore();
            }
            if (this.shouldDrinkAntipoison()) {
                return this.drinkAntipoison();
            }
            if (this.shouldDrinkCombatPot()) {
                return this.drinkCombatPot();
            }
            if (this.config.enableBossMechanics() && this.objectOnTile(player.getWorldLocation(), this.bloodPoolId()) && (worldPoint = this.findPoolSafeTile(player.getWorldLocation())) != null && !worldPoint.equals((Object)player.getWorldLocation())) {
                this.phase = MoonsPhase.DODGING;
                this.status = "Stepping off blood pool";
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(worldPoint);
                return 600;
            }
            if (this.config.targetMoon() == TargetMoon.BLUE && this.config.enableBossMechanics() && this.blueEntombActive(nPC)) {
                return this.handleBlueEntomb(player, nPC);
            }
            if (this.config.targetMoon() == TargetMoon.BLUE && this.config.enableBossMechanics() && this.blueFrostStormActive()) {
                return this.handleBlueFrostStorm(player, nPC);
            }
            if (this.config.targetMoon() == TargetMoon.ECLIPSE && this.config.enableBossMechanics() && (n2 = this.handleEclipseSpecials(player, nPC)) >= 0) {
                return n2;
            }
            if (this.config.targetMoon() == TargetMoon.BLOOD && this.config.enableBossMechanics() && this.bloodRainActive(nPC)) {
                return this.handleBloodRain(player, nPC);
            }
            if (this.config.followSafeTiles() && (worldPoint = this.findSafeMarker()) != null) {
                return this.handleMarkerCombat(player, nPC, (NPC)worldPoint);
            }
            if (this.config.enableBossMechanics() && (n = this.handleBossMechanic(player, nPC)) >= 0) {
                return n;
            }
            if (nPC != null) {
                return this.attackBoss(nPC, player);
            }
            this.status = "Waiting for boss to reappear";
            return 600;
        }
        if (this.phase == MoonsPhase.ENTERING_ROOM) {
            return this.confirmMuralEntry();
        }
        if ((this.phase == MoonsPhase.AT_LOBBY || this.phase == MoonsPhase.IDLE) && this.muralPresent()) {
            return this.enterMural();
        }
        this.status = "Idle \u2014 searching for boss / mural";
        return 600;
    }

    private void updatePhase(NPC nPC, Player player) {
        if (this.pendingKill) {
            this.pendingKill = false;
            this.registerKill();
            return;
        }
        if (nPC != null) {
            this.lastBossLocation = nPC.getWorldLocation();
            int n = nPC.getAnimation();
            if (this.phase == MoonsPhase.LOOTING || this.phase == MoonsPhase.DEAD || this.phase == MoonsPhase.RESUPPLYING) {
                return;
            }
            int n2 = this.effectiveDeathAnim();
            if (n2 > 0 && n == n2) {
                this.registerKill();
                this.bossWasPresent = false;
                return;
            }
            this.bossWasPresent = true;
            this.bossAbsentSince = -1;
            this.deathCounted = false;
            if (this.fightStartMs <= 0L) {
                this.fightStartMs = System.currentTimeMillis();
            }
            if (this.phase != MoonsPhase.FIGHTING && this.phase != MoonsPhase.DODGING) {
                this.phase = MoonsPhase.FIGHTING;
                this.attackCount = 0;
            }
            return;
        }
        if (this.bossWasPresent && (this.phase == MoonsPhase.FIGHTING || this.phase == MoonsPhase.DODGING)) {
            boolean bl;
            boolean bl2 = this.config.targetMoon() == TargetMoon.BLUE && this.blueEntombActive(null);
            boolean bl3 = bl = this.config.targetMoon() == TargetMoon.BLOOD && (this.bloodPoolsPresent() || this.bloodRainMorphPresent());
            if (bl || this.jaguarsPresent() || bl2 || this.eclipseSpecialActive()) {
                this.bossAbsentSince = -1;
                return;
            }
            if (this.bossAbsentSince < 0) {
                this.bossAbsentSince = this.client.getTickCount();
            }
            if (this.client.getTickCount() - this.bossAbsentSince >= 25) {
                this.registerKill();
                this.bossWasPresent = false;
                this.bossAbsentSince = -1;
            }
            return;
        }
        this.bossWasPresent = false;
        if (this.phase != MoonsPhase.RESUPPLYING && this.phase != MoonsPhase.ENTERING_ROOM && this.phase != MoonsPhase.LOOTING && this.phase != MoonsPhase.WAITING_RESPAWN && this.phase != MoonsPhase.DEAD) {
            this.phase = this.muralPresent() ? MoonsPhase.AT_LOBBY : MoonsPhase.IDLE;
        }
    }

    private int effectiveDeathAnim() {
        return this.config.targetMoon() == TargetMoon.BLOOD ? 34724 : 0;
    }

    private void registerKill() {
        ++this.killCount;
        this.recordKillTime();
        if (this.lastBossLocation != null) {
            this.killLocation = this.lastBossLocation;
        } else if (this.client.getLocalPlayer() != null) {
            this.killLocation = this.client.getLocalPlayer().getWorldLocation();
        }
        this.phase = MoonsPhase.DEAD;
        this.attackCount = 0;
        this.bossWasPresent = false;
        this.bossAbsentSince = -1;
        this.respawnWaitStart = -1;
        this.lootingEntryTick = -1;
        this.status = "Kill #" + this.killCount + " complete \u2014 looting";
    }

    private void recordKillTime() {
        if (this.fightStartMs <= 0L) {
            return;
        }
        long l = System.currentTimeMillis() - this.fightStartMs;
        this.fightStartMs = 0L;
        if (l < 1000L) {
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

    private void registerDeath() {
        if (this.deathCounted) {
            return;
        }
        this.deathCounted = true;
        ++this.deathCount;
        this.fightStartMs = 0L;
    }

    private NPC findBoss() {
        int n = this.bossIdForSelected();
        if (n > 0) {
            return NPCs.getNearest(nPC -> nPC != null && nPC.getId() == n && !nPC.isDead());
        }
        String string = this.bossNameForSelected().toLowerCase();
        if (string.isEmpty()) {
            return null;
        }
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getName() != null && nPC.getName().toLowerCase().contains(string));
    }

    private int bossIdForSelected() {
        return this.config.targetMoon().bossNpcId;
    }

    private String bossNameForSelected() {
        String string = this.config.bossName().trim();
        return string.isEmpty() ? this.config.targetMoon().defaultBossName : string;
    }

    private int selectedMuralId() {
        return this.config.targetMoon().muralId;
    }

    private boolean muralPresent() {
        int n = this.selectedMuralId();
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == n) != null;
    }

    private int enterMural() {
        int n = this.selectedMuralId();
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == n);
        if (tileObject2 != null) {
            this.status = "Entering " + String.valueOf((Object)this.config.targetMoon()) + " mural (" + n + ")";
            tileObject2.interact("Enter");
            this.phase = MoonsPhase.ENTERING_ROOM;
            Player player = this.client.getLocalPlayer();
            this.muralEntryPos = player != null ? player.getWorldLocation() : null;
            this.muralClickAt = System.currentTimeMillis();
            this.muralOptionAt = 0L;
            this.enteredChamber = false;
            return 900;
        }
        this.status = "Mural " + n + " not found";
        return 1000;
    }

    private int confirmMuralEntry() {
        boolean bl;
        long l = System.currentTimeMillis();
        Player player = this.client.getLocalPlayer();
        WorldPoint worldPoint = player != null ? player.getWorldLocation() : null;
        boolean bl2 = this.muralEntryPos != null && worldPoint != null && worldPoint.distanceTo(this.muralEntryPos) > 20;
        boolean bl3 = bl = this.muralClickAt != 0L && !this.muralPresent() && l - this.muralClickAt > 1200L;
        if (this.enteredChamber || bl2 || bl) {
            this.resetMuralEntry();
            this.status = "Entered room \u2014 waiting for boss";
            return 800;
        }
        if (Dialog.isOpen()) {
            if (Dialog.isViewingOptions()) {
                this.status = "Mural prompt \u2014 choosing 'Yes' (first option)";
                Dialog.chooseOption(1);
                this.muralOptionAt = l;
                return 600;
            }
            if (Dialog.canContinue()) {
                this.status = "Mural prompt \u2014 continue";
                Dialog.continueSpace();
                this.muralOptionAt = l;
                return 600;
            }
            return 600;
        }
        if (this.muralOptionAt != 0L && l - this.muralOptionAt < 2000L) {
            this.status = "Mural \u2014 waiting after 'Yes'";
            return 400;
        }
        if (this.muralPresent()) {
            if (this.muralClickAt == 0L || l - this.muralClickAt >= 2500L) {
                return this.enterMural();
            }
            this.status = "Mural \u2014 waiting for prompt";
            return 400;
        }
        this.status = "Entering room \u2014 waiting for load";
        return 600;
    }

    private void resetMuralEntry() {
        this.muralClickAt = 0L;
        this.muralOptionAt = 0L;
        this.muralEntryPos = null;
        this.enteredChamber = false;
    }

    private int attackBoss(NPC nPC, Player player) {
        Actor actor = player.getInteracting();
        if (actor == nPC && !nPC.isDead()) {
            this.status = "Fighting " + this.safeName(nPC) + " (atk " + this.attackCount + ")";
            return 600;
        }
        this.status = "Attacking " + this.safeName(nPC);
        nPC.interact("Attack");
        ++this.attackCount;
        return 600;
    }

    private String safeName(NPC nPC) {
        String string = nPC.getName();
        return string != null ? string : "npc#" + nPC.getId();
    }

    private NPC findSafeMarker() {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 13015);
        if (nPC2 == null) {
            // empty if block
        }
        return nPC2;
    }

    private int handleMarkerCombat(Player player, NPC nPC2, NPC nPC3) {
        NPC nPC4;
        int n;
        WorldPoint worldPoint = nPC3.getWorldLocation();
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (worldPoint == null || worldPoint2 == null) {
            return nPC2 != null ? this.attackBoss(nPC2, player) : 600;
        }
        int n2 = this.config.meleeReach();
        NPC nPC5 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == this.jaguarId());
        NPC nPC6 = nPC5 != null ? nPC5 : nPC2;
        int n3 = this.config.targetMoon() == TargetMoon.ECLIPSE ? 0 : this.config.safeTileRadius();
        int n4 = this.distToMarkerFootprint(worldPoint2, nPC3);
        boolean bl = nPC6 != null && this.withinReach(worldPoint2, nPC6, n2);
        WorldPoint worldPoint3 = this.bestSafeStandTile(nPC3, nPC6, n3, n2);
        int n5 = n = worldPoint3 != null ? this.distToMarkerFootprint(worldPoint3, nPC3) : Integer.MAX_VALUE;
        WorldPoint worldPoint4 = bl && n4 <= n3 && n4 <= n ? worldPoint2 : (worldPoint3 != null ? worldPoint3 : (n4 <= n3 ? worldPoint2 : worldPoint));
        if (!worldPoint2.equals((Object)worldPoint4)) {
            this.phase = MoonsPhase.DODGING;
            this.status = "Stepping onto safe tile (" + worldPoint4.getX() + "," + worldPoint4.getY() + ")";
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(worldPoint4);
            return 600;
        }
        this.phase = MoonsPhase.FIGHTING;
        NPC nPC7 = nPC5 != null && this.withinReach(worldPoint2, nPC5, n2) ? nPC5 : (nPC4 = nPC2 != null && this.withinReach(worldPoint2, nPC2, n2) ? nPC2 : null);
        if (nPC4 != null) {
            if (player.getInteracting() != nPC4) {
                nPC4.interact("Attack");
                ++this.attackCount;
            }
            this.status = "On safe tile \u2014 attacking " + this.safeName(nPC4);
        } else {
            this.status = "On safe tile \u2014 holding" + (nPC2 == null ? " (boss vanished)" : " (target out of reach)");
        }
        return 600;
    }

    private WorldPoint bestSafeStandTile(NPC nPC, NPC nPC2, int n, int n2) {
        WorldPoint worldPoint = nPC.getWorldLocation();
        if (worldPoint == null) {
            return null;
        }
        int n3 = this.markerSize(nPC);
        if (n <= 0 || nPC2 == null) {
            for (int i = 0; i < n3; ++i) {
                for (int j = 0; j < n3; ++j) {
                    WorldPoint worldPoint2 = new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane());
                    if (!this.isValidStepTile(worldPoint2) || nPC2 != null && !this.withinReach(worldPoint2, nPC2, n2)) continue;
                    return worldPoint2;
                }
            }
            return worldPoint;
        }
        WorldPoint worldPoint3 = null;
        int n4 = Integer.MAX_VALUE;
        for (int i = -n; i < n3 + n; ++i) {
            for (int j = -n; j < n3 + n; ++j) {
                WorldPoint worldPoint4 = new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane());
                int n5 = this.distToMarkerFootprint(worldPoint4, nPC);
                if (n5 > n || n5 >= n4 || !this.isValidStepTile(worldPoint4) || !this.withinReach(worldPoint4, nPC2, n2)) continue;
                worldPoint3 = worldPoint4;
                n4 = n5;
            }
        }
        return worldPoint3;
    }

    private int markerSize(NPC nPC) {
        int n = 1;
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition != null) {
            n = Math.max(1, nPCComposition.getSize());
        }
        return n;
    }

    private int distToMarkerFootprint(WorldPoint worldPoint, NPC nPC) {
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        if (worldPoint2 == null) {
            return Integer.MAX_VALUE;
        }
        int n = this.markerSize(nPC);
        int n2 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)));
        int n3 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)));
        return Math.max(n2, n3);
    }

    private boolean withinReach(WorldPoint worldPoint, NPC nPC, int n) {
        if (nPC == null) {
            return false;
        }
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        if (worldPoint2 == null) {
            return false;
        }
        int n2 = 1;
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition != null) {
            n2 = Math.max(1, nPCComposition.getSize());
        }
        int n3 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n2 - 1)));
        int n4 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n2 - 1)));
        if (n <= 1) {
            return (n3 == 0 || n4 == 0) && n3 + n4 <= n;
        }
        return Math.max(n3, n4) <= n;
    }

    private static int chebyshev(WorldPoint worldPoint, WorldPoint worldPoint2) {
        return Math.max(Math.abs(worldPoint.getX() - worldPoint2.getX()), Math.abs(worldPoint.getY() - worldPoint2.getY()));
    }

    private int handleBossMechanic(Player player, NPC nPC2) {
        switch (this.config.targetMoon()) {
            case BLOOD: {
                NPC nPC3 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == this.jaguarId());
                if (nPC3 != null) {
                    WorldPoint worldPoint;
                    this.phase = MoonsPhase.DODGING;
                    WorldPoint worldPoint2 = player.getWorldLocation();
                    if (this.objectOnTile(worldPoint2, this.bloodPoolId()) && (worldPoint = this.findPoolSafeTile(worldPoint2)) != null && !worldPoint.equals((Object)worldPoint2)) {
                        this.status = "Jaguars: stepping off blood pool";
                        MousePackets.queueClickPacket();
                        MovementPackets.sendMovement(worldPoint);
                        return 600;
                    }
                    this.status = "Jaguars: clearing adds";
                    if (player.getInteracting() != nPC3) {
                        nPC3.interact("Attack");
                        ++this.attackCount;
                    }
                    return 600;
                }
                if (this.bloodPoolsPresent()) {
                    WorldPoint worldPoint;
                    this.phase = MoonsPhase.DODGING;
                    WorldPoint worldPoint3 = player.getWorldLocation();
                    if (this.objectOnTile(worldPoint3, this.bloodPoolId()) && (worldPoint = this.findPoolSafeTile(worldPoint3)) != null && !worldPoint.equals((Object)worldPoint3)) {
                        this.status = "Blood rain: stepping off pool";
                        MousePackets.queueClickPacket();
                        MovementPackets.sendMovement(worldPoint);
                        return 600;
                    }
                    this.status = "Blood rain: holding clear of pools";
                    return 600;
                }
                return -1;
            }
            case BLUE: {
                if (this.blueEntombActive(nPC2)) {
                    return this.handleBlueEntomb(player, nPC2);
                }
                return -1;
            }
            case ECLIPSE: {
                return -1;
            }
        }
        return -1;
    }

    private int handleEclipseSpecials(Player player, NPC nPC) {
        int n = this.safeTickCount();
        if (n < 0) {
            return -1;
        }
        if (n <= this.eclipseClonesUntilTick) {
            this.phase = MoonsPhase.DODGING;
            NPC nPC2 = this.attackingEclipseClone(nPC);
            if (nPC2 != null && player.getInteracting() != nPC2) {
                nPC2.interact("Attack");
                this.status = "Eclipse: clone bind \u2014 parrying clone #" + nPC2.getIndex();
                return 600;
            }
            this.status = "Eclipse: clone bind \u2014 holding (face each clone to parry)";
            return 600;
        }
        NPC nPC3 = this.findEclipseShield();
        if (nPC3 != null || n <= this.eclipseShieldUntilTick) {
            this.phase = MoonsPhase.DODGING;
            if (nPC3 != null && nPC != null) {
                WorldPoint worldPoint = this.behindShieldTile(player, nPC, nPC3);
                if (worldPoint != null && player.getWorldLocation() != null && !worldPoint.equals((Object)player.getWorldLocation())) {
                    this.status = "Eclipse: hiding behind Moon Shield (" + worldPoint.getX() + "," + worldPoint.getY() + ")";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(worldPoint);
                    return 600;
                }
                this.status = "Eclipse: behind shield \u2014 holding";
                return 600;
            }
            this.status = "Eclipse: Moon Shield up \u2014 holding (cannot harm boss)";
            return 600;
        }
        return -1;
    }

    private NPC findEclipseShield() {
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == 13020);
    }

    private NPC attackingEclipseClone(NPC nPC) {
        List<NPC> list = NPCs.getAll(nPC2 -> nPC2 != null && !nPC2.isDead() && nPC2.getId() == 15029 && nPC2 != nPC);
        if (list.isEmpty()) {
            return null;
        }
        for (NPC nPC3 : list) {
            int n = nPC3.getAnimation();
            if (n != 34761 && n != 34795 && n != 34796) continue;
            return nPC3;
        }
        return list.get(Math.abs(this.cloneRotationIdx++) % list.size());
    }

    private WorldPoint behindShieldTile(Player player, NPC nPC, NPC nPC2) {
        double d;
        WorldPoint worldPoint = this.hitboxCenter(nPC);
        WorldPoint worldPoint2 = this.hitboxCenter(nPC2);
        if (worldPoint == null || worldPoint2 == null) {
            return null;
        }
        double d2 = worldPoint2.getX() - worldPoint.getX();
        double d3 = Math.hypot(d2, d = (double)(worldPoint2.getY() - worldPoint.getY()));
        if (d3 < 0.001) {
            return null;
        }
        int n = 2;
        int n2 = (int)Math.round((double)worldPoint2.getX() + d2 / d3 * (double)n);
        int n3 = (int)Math.round((double)worldPoint2.getY() + d / d3 * (double)n);
        WorldPoint worldPoint3 = new WorldPoint(n2, n3, worldPoint2.getPlane());
        return this.nearestValidTileTo(worldPoint3, worldPoint2);
    }

    private WorldPoint hitboxCenter(NPC nPC) {
        WorldPoint worldPoint = nPC.getWorldLocation();
        if (worldPoint == null) {
            return null;
        }
        int n = 1;
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition != null) {
            n = Math.max(1, nPCComposition.getSize());
        }
        return new WorldPoint(worldPoint.getX() + (n - 1) / 2, worldPoint.getY() + (n - 1) / 2, worldPoint.getPlane());
    }

    private WorldPoint nearestValidTileTo(WorldPoint worldPoint, WorldPoint worldPoint2) {
        WorldPoint worldPoint3 = null;
        int n = Integer.MAX_VALUE;
        for (int i = 0; i <= 3; ++i) {
            for (int j = -i; j <= i; ++j) {
                for (int k = -i; k <= i; ++k) {
                    int n2;
                    WorldPoint worldPoint4;
                    if (Math.max(Math.abs(j), Math.abs(k)) != i || !this.isValidStepTile(worldPoint4 = new WorldPoint(worldPoint.getX() + j, worldPoint.getY() + k, worldPoint.getPlane())) || (n2 = MoonsPlugin.chebyshev(worldPoint4, worldPoint2)) >= n) continue;
                    n = n2;
                    worldPoint3 = worldPoint4;
                }
            }
            if (worldPoint3 == null) continue;
            return worldPoint3;
        }
        return worldPoint;
    }

    private WorldPoint tileNextToBoss(NPC nPC, WorldPoint worldPoint) {
        if (nPC == null || nPC.getWorldLocation() == null || worldPoint == null) {
            return worldPoint;
        }
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        int n = 1;
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition != null) {
            n = Math.max(1, nPCComposition.getSize());
        }
        WorldPoint worldPoint3 = null;
        int n2 = Integer.MAX_VALUE;
        for (int i = worldPoint2.getX() - 1; i <= worldPoint2.getX() + n; ++i) {
            for (int j = worldPoint2.getY() - 1; j <= worldPoint2.getY() + n; ++j) {
                int n3;
                WorldPoint worldPoint4;
                boolean bl;
                boolean bl2 = i >= worldPoint2.getX() && i <= worldPoint2.getX() + n - 1;
                boolean bl3 = bl = j >= worldPoint2.getY() && j <= worldPoint2.getY() + n - 1;
                if (bl2 == bl || !this.isValidStepTile(worldPoint4 = new WorldPoint(i, j, worldPoint2.getPlane())) || (n3 = worldPoint.distanceTo(worldPoint4)) >= n2) continue;
                n2 = n3;
                worldPoint3 = worldPoint4;
            }
        }
        return worldPoint3 != null ? worldPoint3 : worldPoint;
    }

    private int blueFrozenId() {
        return 13025;
    }

    private int blueCrackedId() {
        return 13026;
    }

    private int blueEncasedId() {
        return 13009;
    }

    private boolean blueEntombActive(NPC nPC2) {
        boolean bl;
        if (NPCs.getNearest(nPC -> nPC != null && nPC.getId() == this.blueEncasedId()) != null) {
            return true;
        }
        boolean bl2 = bl = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && (nPC.getId() == this.blueFrozenId() || nPC.getId() == this.blueCrackedId())) != null;
        if (bl && nPC2 == null) {
            return true;
        }
        return nPC2 != null && nPC2.getAnimation() == 34789;
    }

    private int handleBlueEntomb(Player player, NPC nPC) {
        this.phase = MoonsPhase.DODGING;
        NPC nPC2 = this.nearestBlueBlock();
        if (nPC2 != null) {
            this.status = "Blue: smashing ice block #" + nPC2.getId();
            if (player.getInteracting() != nPC2) {
                nPC2.interact("Attack");
                ++this.attackCount;
            }
            return 600;
        }
        this.status = "Blue: entombed \u2014 waiting for blocks";
        return 600;
    }

    private NPC nearestBlueBlock() {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == this.blueFrozenId());
        if (nPC2 != null) {
            return nPC2;
        }
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == this.blueCrackedId());
    }

    private boolean blueFrostStormActive() {
        int n = this.safeTickCount();
        if (n >= 0 && n <= this.blueFrostStormUntilTick) {
            return true;
        }
        if (TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 51053) != null) {
            return true;
        }
        return NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 13027) != null;
    }

    private int handleBlueFrostStorm(Player player, NPC nPC) {
        WorldPoint worldPoint;
        this.phase = MoonsPhase.DODGING;
        WorldPoint worldPoint2 = player.getWorldLocation();
        int n = this.safeTickCount();
        TileObject tileObject = this.findUnlitBrazier();
        if (tileObject != null && tileObject.getWorldLocation() != null) {
            WorldPoint worldPoint3 = tileObject.getWorldLocation();
            if (worldPoint3.equals((Object)this.brazierTarget)) {
                ++this.brazierTargetTicks;
            } else {
                this.brazierTarget = worldPoint3;
                this.brazierTargetTicks = 0;
            }
            if (this.brazierTargetTicks >= 6) {
                if (n >= 0) {
                    this.brazierCooldownUntil.put(worldPoint3, n + 8);
                }
                this.brazierTarget = null;
                this.brazierTargetTicks = 0;
                this.status = "Blue: storm \u2014 skipping brazier (" + worldPoint3.getX() + "," + worldPoint3.getY() + "), rotating";
                return 300;
            }
            if (worldPoint2 != null && worldPoint2.distanceTo(worldPoint3) > 1) {
                this.status = "Blue: storm \u2014 walking to brazier (" + worldPoint3.getX() + "," + worldPoint3.getY() + ")";
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(worldPoint3);
                return 600;
            }
            if (this.safeTickCount() % 2 == 0) {
                this.status = "Blue: relighting brazier #" + tileObject.getId() + " (Light)";
                tileObject.interact("Light");
            } else {
                this.status = "Blue: relighting brazier #" + tileObject.getId() + " (first-option)";
                ObjectPackets.tileObjectFirstOption(tileObject, false);
            }
            this.stuckTicks = 0;
            this.lastTickPos = worldPoint2;
            return 900;
        }
        this.brazierTarget = null;
        this.brazierTargetTicks = 0;
        WorldPoint worldPoint4 = this.frostStormRefuge(player, nPC);
        if (worldPoint4 != null && worldPoint2 != null && !worldPoint4.equals((Object)worldPoint2)) {
            this.status = "Blue: storm \u2014 moving to refuge (" + worldPoint4.getX() + "," + worldPoint4.getY() + ")";
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(worldPoint4);
            this.stuckTicks = 0;
            this.lastTickPos = worldPoint2;
            return 600;
        }
        if (worldPoint2 != null && this.bumpStuck(worldPoint2) && (worldPoint = this.findNextSafeTile(worldPoint2)) != null) {
            this.stuckTicks = 0;
            this.status = "Blue: storm \u2014 unsticking";
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(worldPoint);
            return 600;
        }
        this.status = "Blue: storm \u2014 holding (no brazier found)";
        return 600;
    }

    private TileObject findUnlitBrazier() {
        int n = this.safeTickCount();
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 51051 && this.brazierEligible((TileObject)tileObject, n));
        if (tileObject2 != null) {
            return tileObject2;
        }
        tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getName() != null && tileObject.getName().toLowerCase().contains("brazier") && this.brazierEligible((TileObject)tileObject, n));
        if (tileObject2 != null) {
            return tileObject2;
        }
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 51051);
    }

    private boolean brazierEligible(TileObject tileObject, int n) {
        WorldPoint worldPoint = tileObject.getWorldLocation();
        if (worldPoint == null) {
            return true;
        }
        Integer n2 = this.brazierCooldownUntil.get(worldPoint);
        return n2 == null || n < 0 || n >= n2;
    }

    private WorldPoint frostStormRefuge(Player player, NPC nPC) {
        WorldPoint worldPoint;
        NPC nPC2 = this.findSafeMarker();
        if (nPC2 != null && nPC2.getWorldLocation() != null && this.isValidStepTile(nPC2.getWorldLocation())) {
            return nPC2.getWorldLocation();
        }
        if (nPC != null && (worldPoint = this.tileNextToBoss(nPC, player.getWorldLocation())) != null && !worldPoint.equals((Object)player.getWorldLocation())) {
            return worldPoint;
        }
        return player.getWorldLocation() != null ? this.findPoolSafeTile(player.getWorldLocation()) : null;
    }

    private boolean bumpStuck(WorldPoint worldPoint) {
        if (worldPoint != null && worldPoint.equals((Object)this.lastTickPos)) {
            ++this.stuckTicks;
        } else {
            this.stuckTicks = 0;
            this.lastTickPos = worldPoint;
        }
        return this.stuckTicks >= 4;
    }

    private int bloodPoolId() {
        return 64846;
    }

    private int jaguarId() {
        return 13021;
    }

    private boolean bloodPoolsPresent() {
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == this.bloodPoolId()) != null;
    }

    private boolean jaguarsPresent() {
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == this.jaguarId()) != null;
    }

    private boolean bloodRainMorphPresent() {
        return NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 13009) != null;
    }

    private boolean bloodRainActive(NPC nPC) {
        if (this.jaguarsPresent()) {
            return false;
        }
        if (nPC != null && nPC.getAnimation() == 34797) {
            return true;
        }
        if (this.bloodRainMorphPresent()) {
            return true;
        }
        return nPC == null && this.bloodPoolsPresent();
    }

    private int handleBloodRain(Player player, NPC nPC) {
        WorldPoint worldPoint;
        this.phase = MoonsPhase.DODGING;
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (worldPoint2 != null && this.objectOnTile(worldPoint2, this.bloodPoolId()) && (worldPoint = this.findPoolSafeTile(worldPoint2)) != null && !worldPoint.equals((Object)worldPoint2)) {
            this.status = "Blood rain: stepping off pool";
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(worldPoint);
            return 600;
        }
        this.status = "Blood rain: holding clear of pools";
        return 600;
    }

    private WorldPoint findPoolSafeTile(WorldPoint worldPoint) {
        int n = this.bloodPoolId();
        for (int i = 1; i <= 5; ++i) {
            for (int j = -i; j <= i; ++j) {
                for (int k = -i; k <= i; ++k) {
                    WorldPoint worldPoint2;
                    if (Math.max(Math.abs(j), Math.abs(k)) != i || !this.isValidStepTile(worldPoint2 = new WorldPoint(worldPoint.getX() + j, worldPoint.getY() + k, worldPoint.getPlane())) || this.objectOnTile(worldPoint2, n)) continue;
                    return worldPoint2;
                }
            }
        }
        return null;
    }

    private boolean objectOnTile(WorldPoint worldPoint, int n) {
        if (worldPoint == null || n <= 0) {
            return false;
        }
        int n2 = worldPoint.getX() - this.client.getBaseX();
        int n3 = worldPoint.getY() - this.client.getBaseY();
        if (n2 < 0 || n3 < 0 || n2 >= 104 || n3 >= 104) {
            return false;
        }
        Tile tile = this.client.getScene().getTiles()[this.client.getPlane()][n2][n3];
        if (tile == null) {
            return false;
        }
        for (GameObject gameObject : tile.getGameObjects()) {
            if (gameObject == null || gameObject.getId() != n) continue;
            return true;
        }
        GroundObject groundObject = tile.getGroundObject();
        if (groundObject != null && groundObject.getId() == n) {
            return true;
        }
        DecorativeObject decorativeObject = tile.getDecorativeObject();
        return decorativeObject != null && decorativeObject.getId() == n;
    }

    private WorldPoint findNextSafeTile(WorldPoint worldPoint) {
        int[][] nArrayArray;
        for (int[] nArray : nArrayArray = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
            WorldPoint worldPoint2 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (!this.isValidStepTile(worldPoint2)) continue;
            return worldPoint2;
        }
        return null;
    }

    private boolean isValidStepTile(WorldPoint worldPoint) {
        int n = worldPoint.getX() - this.client.getBaseX();
        int n2 = worldPoint.getY() - this.client.getBaseY();
        return n >= 1 && n2 >= 1 && n <= 102 && n2 <= 102 && this.isWalkable(worldPoint);
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

    private void maintainPrayer() {
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        Prayer prayer = this.protectionPrayerForSelected().prayer;
        if (prayer != null && !this.client.isPrayerActive(prayer)) {
            Prayers.toggle(prayer);
        }
        if (this.eclipseSpecialActive()) {
            return;
        }
        Prayer prayer2 = this.config.offensivePrayer().prayer;
        if (prayer2 != null && !this.client.isPrayerActive(prayer2)) {
            Prayers.toggle(prayer2);
        }
    }

    private boolean eclipseSpecialActive() {
        if (this.config.targetMoon() != TargetMoon.ECLIPSE) {
            return false;
        }
        int n = this.safeTickCount();
        return n >= 0 && (n <= this.eclipseShieldUntilTick || n <= this.eclipseClonesUntilTick);
    }

    private DefensivePrayer protectionPrayerForSelected() {
        switch (this.config.targetMoon()) {
            case BLOOD: {
                return this.config.bloodPrayer();
            }
            case BLUE: {
                return this.config.bluePrayer();
            }
            case ECLIPSE: {
                return this.config.eclipsePrayer();
            }
        }
        return DefensivePrayer.NONE;
    }

    private Item findItem(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim().toLowerCase();
        if (string2.isEmpty()) {
            return null;
        }
        return Inventory.getFirst(item -> item != null && item.getName() != null && item.getName().toLowerCase().contains(string2));
    }

    private boolean shouldEat(Player player) {
        if (this.config.foodName().trim().isEmpty()) {
            return false;
        }
        if (this.findItem(this.config.foodName()) == null) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 && n2 * 100 / n < this.effectiveEatPct();
    }

    private int effectiveEatPct() {
        int n = this.config.eatAtHpPercent();
        int n2 = this.safeTickCount();
        if (this.config.targetMoon() == TargetMoon.ECLIPSE && n2 >= 0 && n2 <= this.eclipseClonesUntilTick) {
            return Math.max(n, this.config.eclipseCloneEatPct());
        }
        return n;
    }

    private int eatFood() {
        Item item = this.findItem(this.config.foodName());
        if (item != null) {
            Item item2;
            this.status = "Eating " + item.getName();
            item.interact("Eat");
            if (!this.config.comboFood().trim().isEmpty() && (item2 = this.findItem(this.config.comboFood())) != null) {
                item2.interact("Eat");
            }
            return 1800;
        }
        this.status = "Out of food!";
        return 600;
    }

    private boolean shouldRestorePrayer() {
        if (this.config.prayerPotionName().trim().isEmpty()) {
            return false;
        }
        if (this.findItem(this.config.prayerPotionName()) == null) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.PRAYER);
        int n2 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        return n > 0 && n2 * 100 / n < this.config.prayerThresholdPct();
    }

    private int drinkPrayerRestore() {
        Item item = this.findItem(this.config.prayerPotionName());
        if (item != null) {
            this.status = "Drinking prayer restore";
            item.interact("Drink");
            return 600;
        }
        return 300;
    }

    private boolean shouldDrinkAntipoison() {
        if (this.config.antipoisonName().trim().isEmpty()) {
            return false;
        }
        if (this.client.getVar(VarPlayer.IS_POISONED) <= 0) {
            return false;
        }
        return this.findItem(this.config.antipoisonName()) != null;
    }

    private int drinkAntipoison() {
        Item item = this.findItem(this.config.antipoisonName());
        if (item != null) {
            this.status = "Curing poison";
            item.interact("Drink");
            return 600;
        }
        return 300;
    }

    private boolean shouldDrinkCombatPot() {
        if (this.config.combatPotion() == CombatPotion.NONE) {
            return false;
        }
        if (this.findItem(this.config.combatPotion().toString()) == null) {
            return false;
        }
        long l = System.currentTimeMillis() - this.lastCombatPotMs;
        return this.lastCombatPotMs == 0L || l > (long)this.config.combatPotionRedrinkMins() * 60000L;
    }

    private int drinkCombatPot() {
        Item item = this.findItem(this.config.combatPotion().toString());
        if (item != null) {
            this.status = "Drinking " + String.valueOf((Object)this.config.combatPotion());
            item.interact("Drink");
            this.lastCombatPotMs = System.currentTimeMillis();
            return 600;
        }
        return 300;
    }

    private int handleLooting(Player player) {
        if (this.lootingEntryTick < 0) {
            this.lootingEntryTick = this.client.getTickCount();
        }
        if (this.config.eatWhileLooting() && Inventory.getAll(item -> item != null).size() >= 28) {
            return this.eatFood();
        }
        TileItem tileItem2 = TileItems.getNearest(tileItem -> tileItem != null && this.killLocation != null && tileItem.getWorldLocation().distanceTo(this.killLocation) <= 15 && this.isLootItem((TileItem)tileItem));
        if (tileItem2 != null) {
            this.status = "Looting " + this.client.getItemDefinition(tileItem2.getId()).getName();
            tileItem2.interact("Take");
            return 600;
        }
        int n = this.client.getTickCount() - this.lootingEntryTick;
        if (n < 8) {
            this.status = "Waiting for drops (" + (8 - n) + ")...";
            return 600;
        }
        this.lootingEntryTick = -1;
        if (this.config.continueInRoom() && this.suppliesSufficient()) {
            this.phase = MoonsPhase.WAITING_RESPAWN;
            this.respawnWaitStart = this.client.getTickCount();
            this.status = "Looted \u2014 waiting for respawn";
            return 600;
        }
        if (this.config.enableResupply()) {
            this.status = "Supplies low \u2014 resupplying";
            this.phase = MoonsPhase.RESUPPLYING;
            this.resupplyState = MoonsResupplyState.LOOTING;
            this.playerDied = false;
            this.lootingEntryTick = -1;
            return 600;
        }
        this.status = "Kill complete \u2014 idle (resupply off)";
        this.phase = MoonsPhase.IDLE;
        return 600;
    }

    private int handleWaitRespawn(Player player, NPC nPC) {
        int n;
        if (nPC != null) {
            this.phase = MoonsPhase.FIGHTING;
            this.attackCount = 0;
            this.bossWasPresent = true;
            this.bossAbsentSince = -1;
            this.respawnWaitStart = -1;
            this.status = "Boss respawned \u2014 re-engaging";
            return 600;
        }
        int n2 = this.client.getTickCount() - this.respawnWaitStart;
        if (n2 > (n = Math.max(1, this.config.respawnWaitSecs() * 1000 / 600))) {
            this.respawnWaitStart = -1;
            if (this.muralPresent()) {
                this.status = "No respawn \u2014 re-entering mural";
                this.phase = MoonsPhase.AT_LOBBY;
                return 600;
            }
            if (this.config.enableResupply()) {
                this.status = "No respawn \u2014 resupplying";
                this.phase = MoonsPhase.RESUPPLYING;
                this.resupplyState = MoonsResupplyState.LOOTING;
                this.playerDied = true;
                this.lootingEntryTick = -1;
                return 600;
            }
            this.status = "No respawn \u2014 idle";
            this.phase = MoonsPhase.IDLE;
            return 600;
        }
        this.status = "Waiting for boss to respawn (" + (n - n2) + ")";
        return 1000;
    }

    private boolean suppliesSufficient() {
        if (!this.config.requiredItems().trim().isEmpty()) {
            return this.aboveResupplyMinimums();
        }
        boolean bl = this.config.foodName().trim().isEmpty() || this.findItem(this.config.foodName()) != null;
        boolean bl2 = this.config.prayerPotionName().trim().isEmpty() || this.config.offensivePrayer().prayer == null || this.findItem(this.config.prayerPotionName()) != null;
        return bl && bl2;
    }

    private int handleResupply(Player player) {
        if (this.resupplyState == null) {
            this.resupplyState = MoonsResupplyState.LOOTING;
        }
        switch (this.resupplyState) {
            case LOOTING: {
                if (!this.playerDied) {
                    if (this.lootingEntryTick < 0) {
                        this.lootingEntryTick = this.client.getTickCount();
                    }
                    if (this.config.eatWhileLooting() && Inventory.getAll(item -> item != null).size() >= 28) {
                        return this.eatFood();
                    }
                    TileItem tileItem2 = TileItems.getNearest(tileItem -> tileItem != null && this.killLocation != null && tileItem.getWorldLocation().distanceTo(this.killLocation) <= 15 && this.isLootItem((TileItem)tileItem));
                    if (tileItem2 != null) {
                        this.status = "Resupply: looting " + this.client.getItemDefinition(tileItem2.getId()).getName();
                        tileItem2.interact("Take");
                        return 600;
                    }
                    int n = this.client.getTickCount() - this.lootingEntryTick;
                    if (n < 8) {
                        this.status = "Resupply: waiting for drops (" + (8 - n) + ")...";
                        return 600;
                    }
                    if (this.aboveResupplyMinimums()) {
                        this.status = "Supplies OK \u2014 skipping bank";
                        this.playerDied = false;
                        this.lootingEntryTick = -1;
                        this.resupplyState = MoonsResupplyState.WALKING_TO_GLIDER;
                        return 600;
                    }
                }
                this.playerDied = false;
                this.lootingEntryTick = -1;
                this.status = "Resupply: teleporting home";
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction1Packet(14286854, -1, -1);
                this.resupplyState = MoonsResupplyState.TELEPORTING;
                return 4000;
            }
            case TELEPORTING: {
                this.status = "Resupply: arrived home \u2014 heading to bank";
                this.resupplyState = MoonsResupplyState.WALKING_TO_BANK;
                return 600;
            }
            case WALKING_TO_BANK: {
                TileObject tileObject = this.findBankBooth();
                if (tileObject == null) {
                    if (this.bankHomeTeleAt == 0L || System.currentTimeMillis() - this.bankHomeTeleAt > 9000L) {
                        this.status = "Resupply: teleporting home to bank";
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetAction1Packet(14286854, -1, -1);
                        this.bankHomeTeleAt = System.currentTimeMillis();
                        return 8000;
                    }
                    this.status = "Resupply: waiting to arrive at bank...";
                    return 600;
                }
                this.bankHomeTeleAt = 0L;
                if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 3) {
                    this.status = "Resupply: walking to bank booth";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(tileObject.getWorldLocation());
                    return 1200;
                }
                this.bankStep = BankStep.FOUNTAIN;
                this.resupplyState = MoonsResupplyState.USING_BANK;
                return 300;
            }
            case USING_BANK: {
                return this.handleBankStep(player);
            }
            case WALKING_TO_GLIDER: {
                TileObject tileObject = this.findGlider();
                if (tileObject == null) {
                    this.status = "Resupply: glider not found";
                    return 800;
                }
                if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 4) {
                    this.status = "Resupply: walking to glider";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(tileObject.getWorldLocation());
                    return 1200;
                }
                this.status = "Resupply: glider Previous";
                tileObject.interact("Previous");
                this.gliderSearchSentTick = -1;
                this.resupplyState = MoonsResupplyState.SEARCHING_GLIDER;
                return 1500;
            }
            case SEARCHING_GLIDER: {
                TileObject tileObject = this.findGlider();
                if (this.client.getGameState() == GameState.LOADING || tileObject == null || player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 6) {
                    this.status = "Glider: travelling to Moons";
                    this.gliderSearchSentTick = -1;
                    this.resupplyState = MoonsResupplyState.ARRIVING;
                    return 3000;
                }
                String string = "Previous";
                String string2 = "Moons";
                if (Dialog.isOpen()) {
                    if (!string.isEmpty() && Dialog.hasOption(string)) {
                        this.status = "Glider: choosing '" + string + "'";
                        Dialog.chooseOption(string);
                        this.resupplyState = MoonsResupplyState.ARRIVING;
                        return 3000;
                    }
                    if (!string2.isEmpty() && Dialog.hasOption(string2)) {
                        this.status = "Glider: choosing '" + string2 + "'";
                        Dialog.chooseOption(string2);
                        this.resupplyState = MoonsResupplyState.ARRIVING;
                        return 3000;
                    }
                    if (Dialog.isEnterInputOpen() && !string2.isEmpty()) {
                        this.status = "Glider: typing '" + string2 + "'";
                        Dialog.enterText(string2);
                        this.gliderSearchSentTick = this.client.getTickCount();
                        return 1500;
                    }
                }
                if (this.gliderSearchSentTick < 0) {
                    this.gliderSearchSentTick = this.client.getTickCount();
                }
                if (this.client.getTickCount() - this.gliderSearchSentTick >= 4) {
                    this.status = "Glider: re-opening (Glider)";
                    tileObject.interact("Glider");
                    this.gliderSearchSentTick = this.client.getTickCount();
                    return 1500;
                }
                this.status = "Glider: waiting for menu...";
                return 600;
            }
            case ARRIVING: {
                this.status = "Arriving at Moons lobby";
                this.lastCombatPotMs = 0L;
                this.phase = MoonsPhase.AT_LOBBY;
                this.resupplyState = null;
                return 2000;
            }
        }
        return 600;
    }

    private int handleBankStep(Player player) {
        TileObject tileObject = this.findBankBooth();
        switch (this.bankStep.ordinal()) {
            case 0: {
                TileObject tileObject2 = this.findRejuvenation();
                if (tileObject2 != null) {
                    if (player.getWorldLocation().distanceTo(tileObject2.getWorldLocation()) > 3) {
                        this.status = "Resupply: walking to rejuvenation pool";
                        MousePackets.queueClickPacket();
                        MovementPackets.sendMovement(tileObject2.getWorldLocation());
                        return 1200;
                    }
                    this.status = "Resupply: rejuvenating";
                    tileObject2.interact("Rejuvenate");
                    this.bankStep = BankStep.PRESET;
                    return 4000;
                }
                this.bankStep = BankStep.PRESET;
                return 600;
            }
            case 1: {
                if (tileObject == null) {
                    this.status = "Resupply: bank booth not found";
                    return 600;
                }
                if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 3) {
                    this.status = "Resupply: walking to bank booth";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(tileObject.getWorldLocation());
                    return 1200;
                }
                this.status = "Bank \u2014 Last-preset";
                this.presetLoadConfirmed = false;
                this.lockedPresetDetected = false;
                this.bankOpenMisses = 0;
                tileObject.interact("Last-preset");
                this.bankVerifyAt = System.currentTimeMillis();
                this.bankStep = BankStep.VERIFY_PRESET;
                return 1200;
            }
            case 2: {
                if (this.presetLoadConfirmed || this.suppliesSufficient()) {
                    this.status = "Bank \u2014 preset loaded";
                    this.bankStep = BankStep.DONE;
                    return 600;
                }
                if (this.lockedPresetDetected) {
                    this.lockedPresetDetected = false;
                    this.status = "Preset locked \u2014 opening bank to load manually";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 300;
                }
                if (System.currentTimeMillis() - this.bankVerifyAt > 3000L) {
                    this.status = "Last-preset unconfirmed \u2014 manual load";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 300;
                }
                return 400;
            }
            case 3: {
                if (Bank.isOpen()) {
                    this.status = "Bank open \u2014 loading preset";
                    this.bankStep = BankStep.MANUAL_LOAD;
                    return 600;
                }
                if (tileObject == null) {
                    this.status = "Resupply: bank booth not found";
                    return 600;
                }
                if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 3) {
                    this.status = "Resupply: walking to bank booth";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(tileObject.getWorldLocation());
                    return 1200;
                }
                String string = this.bankOpenAction(this.bankOpenMisses);
                this.status = "Opening bank (" + string + ")";
                tileObject.interact(string);
                this.bankOpenAt = System.currentTimeMillis();
                this.bankStep = BankStep.MANUAL_WAIT_OPEN;
                return 1000;
            }
            case 4: {
                if (Bank.isOpen()) {
                    this.status = "Bank open \u2014 loading preset";
                    this.bankStep = BankStep.MANUAL_LOAD;
                    return 600;
                }
                if (System.currentTimeMillis() - this.bankOpenAt > 5000L) {
                    ++this.bankOpenMisses;
                    if (this.bankOpenMisses >= 4) {
                        this.dlog("BANK_OPEN_GIVEUP misses=" + this.bankOpenMisses);
                        this.status = "Bank won't open \u2014 re-homing to retry";
                        this.bankOpenMisses = 0;
                        this.bankStep = BankStep.FOUNTAIN;
                        this.bankHomeTeleAt = 0L;
                        this.resupplyState = MoonsResupplyState.WALKING_TO_BANK;
                        return 600;
                    }
                    this.status = "Bank didn't open \u2014 retrying (" + this.bankOpenMisses + ")";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 600;
                }
                this.status = "Waiting for bank to open...";
                return 400;
            }
            case 5: {
                if (!Bank.isOpen()) {
                    this.status = "Bank closed early \u2014 reopening";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 600;
                }
                this.status = "Manual preset: load";
                this.presetLoadConfirmed = false;
                Mouse.click(448, 17, true);
                this.bankStep = BankStep.MANUAL_CONFIRM;
                return 500;
            }
            case 6: {
                this.status = "Manual preset: confirm";
                Mouse.click(68, 282, true);
                this.bankVerifyAt = System.currentTimeMillis();
                this.bankStep = BankStep.MANUAL_VERIFY_LOAD;
                return 500;
            }
            case 7: {
                if (this.presetLoadConfirmed || this.suppliesSufficient()) {
                    this.status = "Preset loaded (confirmed) \u2014 closing bank";
                    this.bankStep = BankStep.MANUAL_CLOSE;
                    return 300;
                }
                if (System.currentTimeMillis() - this.bankVerifyAt > 3000L) {
                    this.status = "Preset load unconfirmed \u2014 closing anyway";
                    this.bankStep = BankStep.MANUAL_CLOSE;
                    return 300;
                }
                return 400;
            }
            case 8: {
                this.status = "Closing bank";
                Mouse.click(483, 33, true);
                this.bankStep = BankStep.DONE;
                return 1000;
            }
        }
        this.bankStep = BankStep.FOUNTAIN;
        this.status = "Bank done \u2014 heading to glider";
        this.resupplyState = MoonsResupplyState.WALKING_TO_GLIDER;
        return 600;
    }

    private TileObject findBankBooth() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 10083);
        if (tileObject2 != null) {
            return tileObject2;
        }
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getName() != null && tileObject.getName().toLowerCase().contains("bank"));
    }

    private String bankOpenAction(int n) {
        String[] stringArray = new String[]{"Use", "Bank", "Open"};
        return stringArray[Math.abs(n) % stringArray.length];
    }

    private TileObject findRejuvenation() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 42822);
        if (tileObject2 == null) {
            tileObject2 = TileObjects.getNearest(tileObject -> {
                if (tileObject == null || tileObject.getName() == null) {
                    return false;
                }
                String string = tileObject.getName().toLowerCase();
                return string.contains("pool") || string.contains("rejuven") || string.contains("fountain");
            });
        }
        return tileObject2;
    }

    private TileObject findGlider() {
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getName() != null && tileObject.getName().toLowerCase().contains("glider"));
    }

    private boolean aboveResupplyMinimums() {
        String string = this.config.requiredItems().trim();
        if (string.isEmpty()) {
            return false;
        }
        for (String string2 : string.split(",")) {
            int n;
            String[] stringArray = string2.trim().split(":");
            if (stringArray.length != 2) continue;
            String string3 = stringArray[0].trim().toLowerCase();
            try {
                n = Integer.parseInt(stringArray[1].trim());
            }
            catch (NumberFormatException numberFormatException) {
                continue;
            }
            int n2 = Inventory.getAll(item -> item != null && item.getName() != null && item.getName().toLowerCase().contains(string3)).stream().mapToInt(Item::getQuantity).sum();
            if (n2 >= n) continue;
            return false;
        }
        return true;
    }

    private boolean isLootItem(TileItem tileItem) {
        String string = this.config.lootItems().trim();
        if (string.isEmpty()) {
            return true;
        }
        String string2 = this.client.getItemDefinition(tileItem.getId()).getName().toLowerCase();
        for (String string3 : string.split(",")) {
            if (!string2.contains(string3.trim().toLowerCase())) continue;
            return true;
        }
        return false;
    }

    private void resetState() {
        this.phase = MoonsPhase.IDLE;
        this.status = "Idle";
        this.killCount = 0;
        this.attackCount = 0;
        this.deathCount = 0;
        this.fightStartMs = 0L;
        this.bestBossMs = 0L;
        this.worstBossMs = 0L;
        this.totalBossMs = 0L;
        this.bossTimedCount = 0;
        this.deathCounted = false;
        this.currentBoss = null;
        this.lastBossLocation = null;
        this.bossWasPresent = false;
        this.bossAbsentSince = -1;
        this.killLocation = null;
        this.pendingKill = false;
        this.respawnWaitStart = -1;
        this.resupplyState = null;
        this.playerDied = false;
        this.lootingEntryTick = -1;
        this.gliderSearchSentTick = -1;
        this.bankStep = BankStep.FOUNTAIN;
        this.presetLoadConfirmed = false;
        this.lockedPresetDetected = false;
        this.bankOpenMisses = 0;
        this.bankVerifyAt = 0L;
        this.bankOpenAt = 0L;
        this.bankHomeTeleAt = 0L;
        this.lastCombatPotMs = 0L;
        this.loggedInLastTick = false;
        this.muralClickAt = 0L;
        this.muralOptionAt = 0L;
        this.muralEntryPos = null;
        this.enteredChamber = false;
        this.eclipseShieldUntilTick = -1;
        this.eclipseClonesUntilTick = -1;
        this.blueFrostStormUntilTick = -1;
        this.lastTickPos = null;
        this.stuckTicks = 0;
        this.brazierTarget = null;
        this.brazierTargetTicks = 0;
        this.brazierCooldownUntil.clear();
        this.cloneRotationIdx = 0;
    }

    private void dlog(String string) {
        try {
            if (this.dbgWriter == null && !this.dbgInit) {
                this.dbgInit = true;
                File file = new File(System.getProperty("user.home"), ".orion/moons_debug");
                file.mkdirs();
                String string2 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                this.dbgWriter = new BufferedWriter(new FileWriter(new File(file, "moons_" + string2 + ".log")));
            }
            if (this.dbgWriter != null) {
                this.dbgWriter.write(string);
                this.dbgWriter.write("\n");
                this.dbgWriter.flush();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private int safeTickCount() {
        try {
            return this.client.getTickCount();
        }
        catch (Exception exception) {
            return -1;
        }
    }

    public MoonsPhase getPhase() {
        return this.phase;
    }

    public String getStatus() {
        return this.status;
    }

    public int getKillCount() {
        return this.killCount;
    }

    public int getAttackCount() {
        return this.attackCount;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public MoonsResupplyState getResupplyState() {
        return this.resupplyState;
    }

    public int getDeathCount() {
        return this.deathCount;
    }

    public int getTotalRuns() {
        return this.killCount + this.deathCount;
    }

    public TargetMoon getTargetMoon() {
        return this.config.targetMoon();
    }

    public long getAvgKillMs() {
        return this.bossTimedCount > 0 ? this.totalBossMs / (long)this.bossTimedCount : 0L;
    }

    public long getBestKillMs() {
        return this.bestBossMs;
    }

    public long getWorstKillMs() {
        return this.worstBossMs;
    }

    public long getCurrentFightMs() {
        return this.fightStartMs > 0L ? System.currentTimeMillis() - this.fightStartMs : 0L;
    }

    public String getOffensivePrayerName() {
        return this.config.offensivePrayer() == null ? "\u2014" : this.config.offensivePrayer().toString();
    }

    public String getProtectionPrayerName() {
        DefensivePrayer defensivePrayer = this.protectionPrayerForSelected();
        return defensivePrayer == null ? "\u2014" : defensivePrayer.toString();
    }

    public String getBossHpText() {
        int n = this.getBossHpPercent();
        return n < 0 ? "\u2014" : n + "%";
    }

    public int getBossHpPercent() {
        NPC nPC = this.currentBoss;
        if (nPC == null) {
            return -1;
        }
        int n = nPC.getHealthRatio();
        int n2 = nPC.getHealthScale();
        if (n < 0 || n2 <= 0) {
            return -1;
        }
        return (int)Math.round(100.0 * (double)n / (double)n2);
    }

    private static /* synthetic */ boolean lambda$findSafeMarker$1(NPC nPC) {
        return nPC != null && nPC.getId() == 0;
    }

    private static enum BankStep {
        FOUNTAIN,
        PRESET,
        VERIFY_PRESET,
        MANUAL_OPEN,
        MANUAL_WAIT_OPEN,
        MANUAL_LOAD,
        MANUAL_CONFIRM,
        MANUAL_VERIFY_LOAD,
        MANUAL_CLOSE,
        DONE;

    }
}

