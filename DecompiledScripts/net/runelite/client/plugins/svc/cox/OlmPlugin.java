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
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.GameObject
 *  net.runelite.api.GameState
 *  net.runelite.api.GroundObject
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Projectile
 *  net.runelite.api.Skill
 *  net.runelite.api.Tile
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
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
 */
package net.runelite.client.plugins.svc.cox;

import com.google.inject.Binder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.DecorativeObject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
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
import net.runelite.client.plugins.svc.cox.OlmConfig;
import net.runelite.client.plugins.svc.cox.OlmConstants;
import net.runelite.client.plugins.svc.cox.OlmHeadStrategy;
import net.runelite.client.plugins.svc.cox.OlmOverheadMode;
import net.runelite.client.plugins.svc.cox.OlmOverlay;
import net.runelite.client.plugins.svc.cox.OlmPhase;
import net.runelite.client.plugins.svc.cox.OlmSpell;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 CoX \u2014 Olm</font></nobr></html>", description="Full automation for the Great Olm boss fight: cripple both hands \u2192 kill the head, reactive prayer, gear switching, special-attack avoidance, and chest looting.", tags={"orion", "cox", "olm", "chambers", "boss", "combat", "bot"}, enabledByDefault=false)
public class OlmPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private OlmConfig config;
    private OlmOverlay overlay;
    private OlmPhase phase = OlmPhase.IDLE;
    private String status = "Idle";
    private String currentTarget = "None";
    private int killCount = 0;
    private long startTime = 0L;
    private TargetMode currentTargetMode = null;
    private Tri headDamageable = Tri.UNKNOWN;
    private int headProbeStartTick = -1;
    private int headFracMin = Integer.MAX_VALUE;
    private int headFracLast = -1;
    private int headLastFrac = -1;
    private int cripStartTick = -1;
    private int lastHeadAttackTick = -1;
    private boolean sawHead = false;
    private boolean headPresentLastTick = false;
    private boolean everSawHandThisCombat = false;
    private int noHandUpSinceTick = -1;
    private String targetReason = "";
    private int mageHpPct = -1;
    private int meleeHpPct = -1;
    private int headHpPct = -1;
    private boolean mageHandDisabled = false;
    private boolean meleeHandDisabled = false;
    private int mageDisabledUntilTick = -1;
    private int meleeDisabledUntilTick = -1;
    private int headStuckSinceTick = -1;
    private int headStuckFrac = -1;
    private String desiredGearStyle = "";
    private int equipAttemptId = -1;
    private int equipAttemptCount = 0;
    private boolean gearSwapStuck = false;
    private boolean crystalOnTileLastTick = false;
    private WorldPoint crystalTileLastTick = null;
    private int lastStepTick = -1;
    private int suppressAttackUntilTick = -1;
    private static final int[][] DIRS8 = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] DIRS4 = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    private int lastCastAtHandTick = -1;
    private Prayer desiredOverhead = Prayer.PROTECT_FROM_MAGIC;
    private Prayer lastSentOverhead = null;
    private long lastSentOverheadMs = 0L;
    private int lastReactTick = -1;
    private Prayer sphereOverhead = null;
    private int sphereUntilTick = -1;
    private int prayerDisabledUntilTick = -1;
    private boolean prayerWasDisabled = false;
    private Elemental elemental = Elemental.NONE;
    private int teleportPullUntilTick = -1;
    private WorldPoint lastAcidPos = null;
    private final Deque<WorldPoint> recentTiles = new ArrayDeque<WorldPoint>();
    private String activeMechanic = "None";
    private int lootEntryTick = -1;
    private boolean loggedInLastTick = false;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(OlmConfig.class).toInstance((Object)((OlmConfig)configManager.getConfig(OlmConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new OlmOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
        this.startTime = System.currentTimeMillis();
        this.status = "Starting";
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGGED_IN) {
            this.loggedInLastTick = false;
        } else if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
            this.phase = OlmPhase.IDLE;
        } else if (gameState == GameState.LOADING && this.phase == OlmPhase.COMBAT) {
            this.resetState();
            this.status = "Map loading \u2014 resetting state";
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String string = chatMessage.getMessage();
        if (string == null) {
            return;
        }
        String string2 = string.toLowerCase();
        if (string2.contains("power of acid")) {
            this.elemental = Elemental.ACID;
        } else if (string2.contains("power of crystal")) {
            this.elemental = Elemental.CRYSTAL;
        } else if (string2.contains("power of flame") || string2.contains("power of fire")) {
            this.elemental = Elemental.FLAME;
        }
        if (string2.contains("can't use protection prayers") || string2.contains("cant use protection prayers")) {
            this.prayerDisabledUntilTick = this.client.getTickCount() + 5;
        }
        if (string2.contains("protecting its right claw")) {
            this.mageDisabledUntilTick = this.client.getTickCount() + 17;
        }
        if (string2.contains("protecting its left claw")) {
            this.meleeDisabledUntilTick = this.client.getTickCount() + 17;
        }
        if (this.config.sphereProtect()) {
            Prayer prayer = null;
            if (string2.contains("sphere of aggression")) {
                prayer = Prayer.PROTECT_FROM_MELEE;
            } else if (string2.contains("sphere of accuracy and dexterity")) {
                prayer = Prayer.PROTECT_FROM_MISSILES;
            } else if (string2.contains("sphere of magical power")) {
                prayer = Prayer.PROTECT_FROM_MAGIC;
            }
            if (prayer != null) {
                this.sphereOverhead = prayer;
                this.sphereUntilTick = this.client.getTickCount() + 3;
                this.desiredOverhead = prayer;
                this.lastReactTick = this.client.getTickCount();
            }
        }
        if (this.config.handleTeleportSwap() && string2.contains("no one to pair you with")) {
            this.teleportPullUntilTick = this.client.getTickCount() + 3;
        }
        if (string2.contains("teleport attack had no effect")) {
            this.teleportPullUntilTick = -1;
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved projectileMoved) {
        Player player;
        Projectile projectile = projectileMoved.getProjectile();
        if (projectile == null || projectile.getRemainingCycles() <= 0) {
            return;
        }
        int n = projectile.getId();
        Actor actor = projectile.getInteracting();
        if (n == 1539) {
            if (actor instanceof NPC && this.isOlmNpc(((NPC)actor).getId())) {
                this.lastCastAtHandTick = this.client.getTickCount();
            }
            return;
        }
        if (actor instanceof NPC && this.isOlmNpc(((NPC)actor).getId())) {
            this.lastCastAtHandTick = this.client.getTickCount();
        }
        if (this.client.getTickCount() > this.sphereUntilTick) {
            if (n == 1339) {
                this.desiredOverhead = Prayer.PROTECT_FROM_MAGIC;
                this.lastReactTick = this.client.getTickCount();
            } else if (n == 1340) {
                this.desiredOverhead = Prayer.PROTECT_FROM_MISSILES;
                this.lastReactTick = this.client.getTickCount();
            }
        }
        if (!this.config.sphereProtect() || (player = this.client.getLocalPlayer()) == null || actor == player) {
            // empty if block
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (this.client.getGameState() == GameState.LOGGED_IN && this.client.getBoostedSkillLevel(Skill.HITPOINTS) == 0 && this.phase == OlmPhase.COMBAT) {
            this.resetState();
            this.status = "Died \u2014 state reset";
            return;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return;
        }
        this.maintainPrayer();
        if (this.phase == OlmPhase.COMBAT && !this.desiredGearStyle.isEmpty()) {
            this.equipGearIfNeeded(this.desiredGearStyle);
        }
        this.sampleHeadHp();
        this.detectKillTickAccurate();
        this.updateHeadProbe(player);
        this.maintainSurvival(player);
    }

    private void sampleHeadHp() {
        NPC nPC = this.findHead();
        if (nPC == null) {
            return;
        }
        int n = nPC.getHealthRatio();
        int n2 = nPC.getHealthScale();
        if (n >= 0 && n2 > 0) {
            this.headLastFrac = n * 1000 / n2;
        }
    }

    private void detectKillTickAccurate() {
        boolean bl;
        NPC nPC = this.findHead();
        boolean bl2 = bl = nPC != null;
        if (this.phase == OlmPhase.COMBAT && this.headPresentLastTick && !bl && this.sawHead && this.client.getTickCount() - this.lastHeadAttackTick <= 8 && this.headLastFrac >= 0 && this.headLastFrac <= 500) {
            ++this.killCount;
            this.phase = OlmPhase.LOOTING;
            this.lootEntryTick = -1;
            this.currentTarget = "None";
            this.currentTargetMode = null;
            this.status = "Olm defeated! Looting chest...";
            this.sawHead = false;
            this.headLastFrac = -1;
        }
        this.headPresentLastTick = bl;
    }

    @Override
    protected int loop() {
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
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        this.resolvePhase(player);
        if (this.phase == OlmPhase.DONE) {
            this.status = "Done \u2014 stopped";
            return -1;
        }
        if (this.shouldEscape(player)) {
            return this.doEscape();
        }
        if (this.shouldEat(player)) {
            return this.eatFood();
        }
        if (this.shouldRestorePrayer()) {
            return this.drinkPrayerRestore();
        }
        if (this.shouldDrinkCombatPotion()) {
            return this.drinkCombatPotion();
        }
        if (this.phase == OlmPhase.LOOTING) {
            return this.doLooting();
        }
        if (this.phase == OlmPhase.DONE) {
            this.status = "Done \u2014 stopped";
            return -1;
        }
        if (this.phase == OlmPhase.IDLE) {
            this.status = "Idle \u2014 waiting for Olm";
            return 600;
        }
        if (this.client.getTickCount() <= this.suppressAttackUntilTick) {
            this.status = this.activeMechanic + " \u2014 repositioning";
            return 300;
        }
        NPC nPC = this.decideTarget(player);
        if (nPC == null) {
            this.status = "Waiting for Olm to emerge";
            return 600;
        }
        if (!this.desiredGearStyle.isEmpty() && !this.weaponReadyFor(this.desiredGearStyle)) {
            boolean bl;
            String string = this.gearListFor(this.desiredGearStyle);
            boolean bl2 = bl = string != null && !string.trim().isEmpty();
            this.status = !bl ? "Configure " + this.desiredGearStyle + " gear IDs \u2014 won't attack with wrong style" : (this.gearSwapStuck ? this.desiredGearStyle + " weapon won't equip \u2014 inv full? check gear IDs" : (this.gearPresentFor(this.desiredGearStyle) ? "Swapping to " + this.desiredGearStyle + " gear..." : this.desiredGearStyle + " gear not found \u2014 check gear IDs"));
            return 600;
        }
        int n2 = n = this.currentTargetMode == TargetMode.MELEE_HAND ? 1 : this.config.attackRange();
        if (!this.isInAttackRange(player, nPC, n)) {
            this.step(nPC.getWorldLocation(), true);
            this.status = this.currentTarget + " \u2014 approaching";
            return 600;
        }
        switch (this.currentTargetMode.ordinal()) {
            case 0: {
                this.lastHeadAttackTick = this.client.getTickCount();
                this.status = this.config.headStrategy() == OlmHeadStrategy.AUTO && this.headDamageable == Tri.UNKNOWN ? "Probing head (ranged)" : "Killing head (ranged)";
                this.attackNpc(nPC, player);
                break;
            }
            case 1: {
                this.status = "Crippling Mage Hand";
                this.attackMageHand(nPC, player);
                break;
            }
            default: {
                this.status = "Crippling Melee Hand";
                this.attackNpc(nPC, player);
            }
        }
        return 600;
    }

    private void resolvePhase(Player player) {
        boolean bl;
        if (this.phase == OlmPhase.LOOTING || this.phase == OlmPhase.DONE) {
            return;
        }
        NPC nPC = this.findHead();
        NPC nPC2 = this.findMageHand();
        NPC nPC3 = this.findMeleeHand();
        boolean bl2 = bl = nPC != null || nPC2 != null || nPC3 != null;
        if (this.sawHead && nPC == null && this.client.getTickCount() - this.lastHeadAttackTick <= 4 && this.headLastFrac >= 0 && this.headLastFrac <= 500) {
            ++this.killCount;
            this.phase = OlmPhase.LOOTING;
            this.lootEntryTick = -1;
            this.currentTarget = "None";
            this.currentTargetMode = null;
            this.status = "Olm defeated! Looting chest...";
            this.sawHead = false;
            this.headLastFrac = -1;
            return;
        }
        if (!bl) {
            if (this.phase == OlmPhase.COMBAT) {
                this.clearPhaseTransients();
            }
            this.phase = OlmPhase.IDLE;
            this.sawHead = false;
            return;
        }
        this.phase = OlmPhase.COMBAT;
        this.sawHead = nPC != null;
    }

    private NPC decideTarget(Player player) {
        boolean bl;
        boolean bl2;
        NPC nPC = this.findHead();
        NPC nPC2 = this.findMageHand();
        NPC nPC3 = this.findMeleeHand();
        this.sampleHandHp(nPC2, nPC3, nPC);
        this.updateClawDisabledState(nPC2, nPC3);
        if (this.handPresent(nPC2) || this.handPresent(nPC3)) {
            this.everSawHandThisCombat = true;
        }
        if (nPC == null && nPC2 == null && nPC3 == null) {
            this.currentTargetMode = null;
            this.currentTarget = "None";
            this.noHandUpSinceTick = -1;
            return null;
        }
        if (this.config.headStrategy() == OlmHeadStrategy.HEAD_ONLY && nPC != null) {
            this.targetReason = "head-only mode";
            return this.setHeadTarget(nPC);
        }
        NPC nPC4 = this.decideHandTarget(nPC2, nPC3);
        if (nPC4 != null) {
            this.noHandUpSinceTick = -1;
            if (this.isMageHand(nPC4)) {
                this.currentTargetMode = TargetMode.MAGE_HAND;
                this.desiredGearStyle = "mage";
                this.currentTarget = "Mage Hand";
            } else {
                this.currentTargetMode = TargetMode.MELEE_HAND;
                this.desiredGearStyle = "melee";
                this.currentTarget = "Melee Hand";
            }
            return nPC4;
        }
        if (this.noHandUpSinceTick < 0) {
            this.noHandUpSinceTick = this.client.getTickCount();
        }
        boolean bl3 = bl2 = this.client.getTickCount() - this.noHandUpSinceTick >= 5;
        boolean bl4 = this.config.headStrategy() == OlmHeadStrategy.AUTO ? this.decideAutoTargetHead(nPC, bl2) : (bl = nPC != null && this.everSawHandThisCombat && bl2);
        if (bl && nPC != null) {
            if (this.headSelfHealWatch()) {
                this.mageDisabledUntilTick = -1;
                this.meleeDisabledUntilTick = -1;
                this.mageHandDisabled = false;
                this.meleeHandDisabled = false;
                this.headStuckSinceTick = -1;
                this.currentTargetMode = null;
                this.currentTarget = "None";
                this.targetReason = "head not dropping \u2014 re-checking hands";
                return null;
            }
            this.targetReason = "both hands down";
            return this.setHeadTarget(nPC);
        }
        this.headStuckSinceTick = -1;
        this.currentTargetMode = null;
        this.currentTarget = "None";
        this.targetReason = this.everSawHandThisCombat ? "hands down \u2014 head settling" : "waiting for hands";
        return null;
    }

    private NPC setHeadTarget(NPC nPC) {
        this.currentTargetMode = TargetMode.HEAD;
        this.desiredGearStyle = "ranged";
        this.currentTarget = "Head";
        return nPC;
    }

    private boolean decideAutoTargetHead(NPC nPC, boolean bl) {
        if (nPC == null) {
            return false;
        }
        if (!this.everSawHandThisCombat) {
            return false;
        }
        if (this.headDamageable == Tri.YES) {
            return true;
        }
        if (!bl) {
            return false;
        }
        if (this.headDamageable == Tri.NO) {
            this.startHeadProbe();
        }
        return true;
    }

    private void startHeadProbe() {
        this.headDamageable = Tri.UNKNOWN;
        this.headProbeStartTick = -1;
        this.headFracMin = Integer.MAX_VALUE;
        this.headFracLast = -1;
        this.cripStartTick = -1;
    }

    private void updateHeadProbe(Player player) {
        boolean bl;
        if (this.config.headStrategy() != OlmHeadStrategy.AUTO) {
            return;
        }
        if (this.headDamageable != Tri.UNKNOWN) {
            return;
        }
        if (this.currentTargetMode != TargetMode.HEAD) {
            return;
        }
        if (!this.weaponReadyFor("ranged")) {
            return;
        }
        NPC nPC = this.findHead();
        if (nPC == null) {
            return;
        }
        int n = nPC.getHealthRatio();
        int n2 = nPC.getHealthScale();
        if (n < 0 || n2 <= 0) {
            return;
        }
        int n3 = n * 1000 / n2;
        Actor actor = player.getInteracting();
        boolean bl2 = bl = actor instanceof NPC && ((NPC)actor).getId() == nPC.getId();
        if (this.headProbeStartTick < 0) {
            this.headProbeStartTick = this.client.getTickCount();
            this.headFracMin = n3;
            this.headFracLast = n3;
            return;
        }
        if (bl) {
            this.headFracMin = Math.min(this.headFracMin, n3);
        }
        this.headFracLast = n3;
        if (this.client.getTickCount() - this.headProbeStartTick >= 10) {
            boolean bl3;
            boolean bl4 = this.headFracMin < 980;
            boolean bl5 = bl3 = this.headFracLast - this.headFracMin > 60;
            if (bl4 && !bl3) {
                this.headDamageable = Tri.YES;
            } else {
                this.headDamageable = Tri.NO;
                this.cripStartTick = this.client.getTickCount();
            }
            this.headProbeStartTick = -1;
        }
    }

    private boolean handPresent(NPC nPC) {
        return nPC != null && !nPC.isDead();
    }

    private int handFrac(NPC nPC) {
        int n = nPC.getHealthRatio();
        int n2 = nPC.getHealthScale();
        return n >= 0 && n2 > 0 ? n * 1000 / n2 : 1000;
    }

    private void updateClawDisabledState(NPC nPC, NPC nPC2) {
        boolean bl;
        int n = this.client.getTickCount();
        boolean bl2 = !this.handPresent(nPC);
        boolean bl3 = bl = !this.handPresent(nPC2);
        if (bl2) {
            this.mageDisabledUntilTick = -1;
        }
        if (bl) {
            this.meleeDisabledUntilTick = -1;
        }
        if (bl2) {
            this.meleeDisabledUntilTick = -1;
        }
        if (bl) {
            this.mageDisabledUntilTick = -1;
        }
        this.mageHandDisabled = this.handPresent(nPC) && n <= this.mageDisabledUntilTick;
        this.meleeHandDisabled = this.handPresent(nPC2) && n <= this.meleeDisabledUntilTick;
    }

    private boolean mageAttackable(NPC nPC) {
        return this.handPresent(nPC) && !this.mageHandDisabled;
    }

    private boolean meleeAttackable(NPC nPC) {
        return this.handPresent(nPC) && !this.meleeHandDisabled;
    }

    private NPC decideHandTarget(NPC nPC, NPC nPC2) {
        boolean bl = this.mageAttackable(nPC);
        boolean bl2 = this.meleeAttackable(nPC2);
        if (!bl && !bl2) {
            return null;
        }
        if (bl && !bl2) {
            this.targetReason = "disabling mage hand";
            return nPC;
        }
        if (bl2 && !bl) {
            this.targetReason = "disabling melee hand";
            return nPC2;
        }
        NPC nPC3 = this.config.mageFirst() ? nPC : nPC2;
        NPC nPC4 = this.config.mageFirst() ? nPC2 : nPC;
        int n = 200;
        if (this.handFrac(nPC3) > n) {
            this.targetReason = "softening hand 1";
            return nPC3;
        }
        if (this.handFrac(nPC4) > n) {
            this.targetReason = "softening hand 2";
            return nPC4;
        }
        this.targetReason = "disabling both";
        return nPC4;
    }

    private boolean headSelfHealWatch() {
        int n = this.client.getTickCount();
        if (this.headLastFrac < 0) {
            return false;
        }
        if (this.headStuckSinceTick < 0 || this.headLastFrac < this.headStuckFrac - 30) {
            this.headStuckSinceTick = n;
            this.headStuckFrac = this.headLastFrac;
            return false;
        }
        return n - this.headStuckSinceTick > 12;
    }

    private void sampleHandHp(NPC nPC, NPC nPC2, NPC nPC3) {
        this.mageHpPct = this.handPresent(nPC) ? this.handFrac(nPC) / 10 : -1;
        this.meleeHpPct = this.handPresent(nPC2) ? this.handFrac(nPC2) / 10 : -1;
        this.headHpPct = this.handPresent(nPC3) ? this.handFrac(nPC3) / 10 : -1;
    }

    private void clearPhaseTransients() {
        this.currentTargetMode = null;
        this.cripStartTick = -1;
        this.lastAcidPos = null;
        this.recentTiles.clear();
        this.headDamageable = Tri.UNKNOWN;
        this.headProbeStartTick = -1;
        this.headLastFrac = -1;
        this.suppressAttackUntilTick = -1;
        this.everSawHandThisCombat = false;
        this.noHandUpSinceTick = -1;
        this.targetReason = "";
        this.mageHandDisabled = false;
        this.meleeHandDisabled = false;
        this.mageDisabledUntilTick = -1;
        this.meleeDisabledUntilTick = -1;
        this.equipAttemptId = -1;
        this.equipAttemptCount = 0;
        this.gearSwapStuck = false;
        this.headStuckSinceTick = -1;
        this.headStuckFrac = -1;
    }

    private NPC findMageHand() {
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && (nPC.getId() == 7550 || nPC.getId() == 7553));
    }

    private NPC findMeleeHand() {
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && (nPC.getId() == 7552 || nPC.getId() == 7555));
    }

    private NPC findHead() {
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && (nPC.getId() == 7551 || nPC.getId() == 7554));
    }

    private boolean isMageHand(NPC nPC) {
        return nPC.getId() == 7550 || nPC.getId() == 7553;
    }

    private boolean isOlmNpc(int n) {
        return n == 7550 || n == 7553 || n == 7552 || n == 7555 || n == 7551 || n == 7554;
    }

    private void attackNpc(NPC nPC, Player player) {
        if (nPC.isDead()) {
            return;
        }
        Actor actor = player.getInteracting();
        if (actor instanceof NPC && ((NPC)actor).getId() == nPC.getId()) {
            return;
        }
        nPC.interact("Attack");
    }

    private void attackMageHand(NPC nPC, Player player) {
        OlmSpell olmSpell = this.config.mageSpell();
        if (olmSpell == OlmSpell.AUTOCAST) {
            boolean bl;
            Actor actor = player.getInteracting();
            boolean bl2 = actor instanceof NPC && ((NPC)actor).getId() == nPC.getId();
            boolean bl3 = bl = this.lastCastAtHandTick >= 0 && this.client.getTickCount() - this.lastCastAtHandTick <= 3;
            if (!bl2 || !bl) {
                nPC.interact("Attack");
            }
            return;
        }
        switch (olmSpell) {
            case FIRE_SURGE: {
                Magic.cast((Spell)SpellBook.Standard.FIRE_SURGE, nPC);
                break;
            }
            case FIRE_WAVE: {
                Magic.cast((Spell)SpellBook.Standard.FIRE_WAVE, nPC);
                break;
            }
            case FIRE_BLAST: {
                Magic.cast((Spell)SpellBook.Standard.FIRE_BLAST, nPC);
                break;
            }
            case FIRE_BOLT: {
                Magic.cast((Spell)SpellBook.Standard.FIRE_BOLT, nPC);
                break;
            }
            case IBAN_BLAST: {
                Magic.cast((Spell)SpellBook.Standard.IBAN_BLAST, nPC);
                break;
            }
            case MAGIC_DART: {
                Magic.cast((Spell)SpellBook.Standard.MAGIC_DART, nPC);
                break;
            }
            case SHADOW_BARRAGE: {
                Magic.cast((Spell)SpellBook.Ancient.SHADOW_BARRAGE, nPC);
                break;
            }
            case SHADOW_BURST: {
                Magic.cast((Spell)SpellBook.Ancient.SHADOW_BURST, nPC);
                break;
            }
            case ICE_BARRAGE: {
                Magic.cast((Spell)SpellBook.Ancient.ICE_BARRAGE, nPC);
                break;
            }
            case ICE_BURST: {
                Magic.cast((Spell)SpellBook.Ancient.ICE_BURST, nPC);
                break;
            }
            case BLOOD_BARRAGE: {
                Magic.cast((Spell)SpellBook.Ancient.BLOOD_BARRAGE, nPC);
                break;
            }
            case BLOOD_BURST: {
                Magic.cast((Spell)SpellBook.Ancient.BLOOD_BURST, nPC);
                break;
            }
            case SMOKE_BARRAGE: {
                Magic.cast((Spell)SpellBook.Ancient.SMOKE_BARRAGE, nPC);
                break;
            }
            case SMOKE_BURST: {
                Magic.cast((Spell)SpellBook.Ancient.SMOKE_BURST, nPC);
                break;
            }
            default: {
                nPC.interact("Attack");
            }
        }
    }

    private boolean isInAttackRange(Player player, NPC nPC, int n) {
        WorldPoint worldPoint = player.getWorldLocation();
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n2 = nPCComposition != null ? nPCComposition.getSize() : 1;
        int n3 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n2 - 1)));
        int n4 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n2 - 1)));
        return Math.max(n3, n4) <= n;
    }

    private void equipGearIfNeeded(String string) {
        String string2 = this.gearListFor(string);
        if (string2 == null || string2.trim().isEmpty()) {
            return;
        }
        for (String string3 : string2.split(",")) {
            int n = this.parseId(string3);
            if (n <= 0 || this.isItemEquippedById(n)) continue;
            if (n == this.equipAttemptId && this.equipAttemptCount >= 6) {
                this.gearSwapStuck = true;
                continue;
            }
            Item item2 = Inventory.getFirst(item -> item != null && item.getId() == n);
            if (item2 == null) continue;
            if (n == this.equipAttemptId) {
                ++this.equipAttemptCount;
            } else {
                this.equipAttemptId = n;
                this.equipAttemptCount = 1;
            }
            this.equipItem(item2, this.equipAttemptCount);
            return;
        }
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
        this.equipAttemptId = -1;
        this.equipAttemptCount = 0;
        this.gearSwapStuck = false;
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

    private boolean isItemEquippedById(int n) {
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            Item item = Equipment.fromSlot(equipmentInventorySlot);
            if (item == null || item.getId() != n) continue;
            return true;
        }
        return false;
    }

    private String gearListFor(String string) {
        switch (string) {
            case "mage": {
                return this.config.mageGearIds();
            }
            case "melee": {
                return this.config.meleeGearIds();
            }
        }
        return this.config.rangedGearIds();
    }

    private boolean weaponReadyFor(String string) {
        String string2 = this.gearListFor(string);
        if (string2 != null && !string2.trim().isEmpty()) {
            Item item = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
            return item != null && this.weaponIdInList(item.getId(), string2);
        }
        String string3 = this.currentWeaponStyle();
        return string3.isEmpty() || string3.equals(string);
    }

    private String currentWeaponStyle() {
        Item item = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
        if (item == null) {
            return "";
        }
        int n = item.getId();
        if (this.weaponIdInList(n, this.config.mageGearIds())) {
            return "mage";
        }
        if (this.weaponIdInList(n, this.config.meleeGearIds())) {
            return "melee";
        }
        if (this.weaponIdInList(n, this.config.rangedGearIds())) {
            return "ranged";
        }
        return "";
    }

    private boolean weaponIdInList(int n, String string) {
        if (n <= 0 || string == null) {
            return false;
        }
        for (String string2 : string.split(",")) {
            if (this.parseId(string2) != n) continue;
            return true;
        }
        return false;
    }

    private boolean gearPresentFor(String string) {
        String string2 = this.gearListFor(string);
        if (string2 == null || string2.trim().isEmpty()) {
            return true;
        }
        for (String string3 : string2.split(",")) {
            int n = this.parseId(string3);
            if (n <= 0 || !this.isItemEquippedById(n) && Inventory.getFirst(item -> item != null && item.getId() == n) == null) continue;
            return true;
        }
        return false;
    }

    private int parseId(String string) {
        if (string == null) {
            return -1;
        }
        String string2 = string.trim();
        if (string2.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

    private void maintainPrayer() {
        Prayer prayer;
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        if (this.phase == OlmPhase.IDLE) {
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
            return;
        }
        if (this.client.getTickCount() <= this.prayerDisabledUntilTick) {
            this.prayerWasDisabled = true;
            return;
        }
        if (this.prayerWasDisabled || this.client.getTickCount() - this.prayerDisabledUntilTick <= 1) {
            this.prayerWasDisabled = false;
            this.lastSentOverhead = null;
        }
        Prayer prayer2 = prayer = this.config.overheadMode() == OlmOverheadMode.ALWAYS_MISSILES ? Prayer.PROTECT_FROM_MISSILES : Prayer.PROTECT_FROM_MAGIC;
        if (this.lastReactTick >= 0 && this.client.getTickCount() - this.lastReactTick <= 6) {
            prayer2 = this.desiredOverhead;
        }
        if (this.sphereOverhead != null && this.client.getTickCount() <= this.sphereUntilTick) {
            prayer2 = this.sphereOverhead;
        } else {
            this.sphereOverhead = null;
        }
        this.applyOverhead(prayer2);
        this.maintainOffensivePrayer();
    }

    private void maintainOffensivePrayer() {
        Prayer prayer = this.offensivePrayerForCurrentStyle();
        for (OffensivePrayer offensivePrayer : OffensivePrayer.values()) {
            Prayer prayer2 = offensivePrayer.prayer;
            if (prayer2 == null || prayer2 == prayer || !this.client.isPrayerActive(prayer2)) continue;
            Prayers.toggle(prayer2);
        }
        if (prayer != null && !this.client.isPrayerActive(prayer)) {
            Prayers.toggle(prayer);
        }
    }

    private Prayer offensivePrayerForCurrentStyle() {
        OffensivePrayer offensivePrayer;
        String string;
        switch (string = this.currentWeaponStyle()) {
            case "mage": {
                offensivePrayer = this.config.mageOffensivePrayer();
                break;
            }
            case "melee": {
                offensivePrayer = this.config.meleeOffensivePrayer();
                break;
            }
            case "ranged": {
                offensivePrayer = this.config.rangedOffensivePrayer();
                break;
            }
            default: {
                return null;
            }
        }
        return offensivePrayer != null ? offensivePrayer.prayer : null;
    }

    private void applyOverhead(Prayer prayer) {
        if (prayer == null || this.client.isPrayerActive(prayer)) {
            return;
        }
        long l = System.currentTimeMillis();
        if (!prayer.equals((Object)this.lastSentOverhead) || l - this.lastSentOverheadMs > 700L) {
            Prayer[] prayerArray;
            for (Prayer prayer2 : prayerArray = new Prayer[]{Prayer.PROTECT_FROM_MAGIC, Prayer.PROTECT_FROM_MISSILES, Prayer.PROTECT_FROM_MELEE}) {
                if (prayer2 == prayer || !this.client.isPrayerActive(prayer2)) continue;
                Prayers.toggle(prayer2);
            }
            Prayers.toggle(prayer);
            this.lastSentOverhead = prayer;
            this.lastSentOverheadMs = l;
        }
    }

    private void maintainSurvival(Player player) {
        WorldPoint worldPoint;
        NPC nPC;
        this.activeMechanic = "None";
        WorldPoint worldPoint2 = player.getWorldLocation();
        boolean bl = this.crystalGrowthOnTile(worldPoint2);
        boolean bl2 = bl && !this.crystalOnTileLastTick && worldPoint2.equals((Object)this.crystalTileLastTick);
        this.crystalOnTileLastTick = bl;
        this.crystalTileLastTick = worldPoint2;
        if (this.phase != OlmPhase.COMBAT) {
            this.lastAcidPos = null;
            return;
        }
        if (this.config.handleTeleportSwap() && this.client.getTickCount() <= this.teleportPullUntilTick && (nPC = this.nearestOlmEntity()) != null && worldPoint2.distanceTo(nPC.getWorldLocation()) > 1) {
            this.activeMechanic = "Teleport Swap";
            if (this.step(nPC.getWorldLocation(), true)) {
                this.suppressAttackUntilTick = this.client.getTickCount();
            }
            return;
        }
        if (this.mechanicActive(this.config.dodgeCrystalBurst(), 30034) && bl2) {
            this.activeMechanic = "Crystal Burst";
            nPC = this.findCrystalDodgeTile(player);
            if (nPC != null && this.step((WorldPoint)nPC, true)) {
                this.suppressAttackUntilTick = this.client.getTickCount() + 1;
            }
            return;
        }
        if (this.config.dodgeAcid() && (this.elemental == Elemental.ACID || this.acidPoolsExistNearby())) {
            if (this.maintainAcidEscape(player)) {
                this.activeMechanic = "Acid";
                return;
            }
            if (this.elemental == Elemental.ACID) {
                this.activeMechanic = "Acid (clear)";
            }
        }
        if (this.mechanicActive(this.config.dodgeLightning(), -1) && (nPC = this.nearestLightningInColumn(player)) != null) {
            this.activeMechanic = "Lightning";
            WorldPoint worldPoint3 = this.findLightningDodgeTile(player);
            if (worldPoint3 != null && this.step(worldPoint3, true)) {
                this.suppressAttackUntilTick = this.client.getTickCount() + 1;
            }
            return;
        }
        if (this.mechanicActive(this.config.dodgeCrystalBombs(), -1) && (nPC = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == -1 && tileObject.getWorldLocation().distanceTo(worldPoint2) <= 5)) != null) {
            this.activeMechanic = "Crystal Bomb";
            WorldPoint worldPoint4 = this.findFleeTile(player, nPC.getWorldLocation(), 5);
            if (worldPoint4 != null && this.step(worldPoint4, true)) {
                this.suppressAttackUntilTick = this.client.getTickCount() + 1;
            }
            return;
        }
        if (this.mechanicActive(this.config.dodgeFireWall(), -1) && (nPC = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == -1 && tileObject.getWorldLocation().distanceTo(worldPoint2) <= 3)) != null && (worldPoint = this.findFireWallDodgeTile(player, (TileObject)nPC)) != null) {
            this.activeMechanic = "Fire Wall";
            if (this.step(worldPoint, true)) {
                this.suppressAttackUntilTick = this.client.getTickCount();
            }
            return;
        }
    }

    private boolean mechanicActive(boolean bl, int n) {
        return bl && n > 0;
    }

    boolean hasInertMechanic() {
        return this.config.dodgeLightning() || this.config.dodgeCrystalBombs() || this.config.dodgeFireWall() || this.config.dodgeFallingCrystals() || this.config.useLifeSiphonShield();
    }

    private boolean maintainAcidEscape(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        boolean bl = this.isAcidTile(worldPoint);
        boolean bl2 = false;
        for (int[] nArray : DIRS4) {
            if (!this.isAcidTile(new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane()))) continue;
            bl2 = true;
            break;
        }
        if (!bl && !bl2) {
            this.lastAcidPos = null;
            return false;
        }
        WorldPoint worldPoint2 = this.stepTowardClearTile(worldPoint);
        if (worldPoint2 == null || worldPoint2.equals((Object)worldPoint)) {
            return false;
        }
        if (this.step(worldPoint2, true)) {
            this.pushRecent(worldPoint);
            this.lastAcidPos = worldPoint;
            this.suppressAttackUntilTick = this.client.getTickCount();
            return true;
        }
        return false;
    }

    private void pushRecent(WorldPoint worldPoint) {
        this.recentTiles.addLast(worldPoint);
        while (this.recentTiles.size() > 4) {
            this.recentTiles.removeFirst();
        }
    }

    private boolean acidPoolsExistNearby() {
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return false;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        int n = 8;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                if (!this.isAcidTile(new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane()))) continue;
                return true;
            }
        }
        return false;
    }

    private boolean step(WorldPoint worldPoint, boolean bl) {
        if (worldPoint == null) {
            return false;
        }
        int n = this.client.getTickCount();
        if (this.lastStepTick == n) {
            return false;
        }
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint, bl);
        this.lastStepTick = n;
        return true;
    }

    private WorldPoint stepTowardClearTile(WorldPoint worldPoint) {
        WorldPoint worldPoint2;
        ArrayDeque<WorldPoint> arrayDeque = new ArrayDeque<WorldPoint>();
        HashMap<WorldPoint, WorldPoint> hashMap = new HashMap<WorldPoint, WorldPoint>();
        arrayDeque.add(worldPoint);
        hashMap.put(worldPoint, null);
        int n = 24;
        int n2 = 2500;
        WorldPoint worldPoint3 = null;
        WorldPoint worldPoint4 = null;
        int n3 = Integer.MAX_VALUE;
        while (!arrayDeque.isEmpty() && n2-- > 0) {
            worldPoint2 = (WorldPoint)arrayDeque.poll();
            if (!worldPoint2.equals((Object)worldPoint)) {
                int n4 = this.acidHazardCost(worldPoint2);
                if (n4 == 0) {
                    worldPoint3 = worldPoint2;
                    break;
                }
                if (n4 < n3 && !this.recentTiles.contains(worldPoint2)) {
                    n3 = n4;
                    worldPoint4 = worldPoint2;
                }
            }
            for (int[] nArray : DIRS8) {
                WorldPoint worldPoint5 = new WorldPoint(worldPoint2.getX() + nArray[0], worldPoint2.getY() + nArray[1], worldPoint2.getPlane());
                if (hashMap.containsKey(worldPoint5) || Math.max(Math.abs(worldPoint5.getX() - worldPoint.getX()), Math.abs(worldPoint5.getY() - worldPoint.getY())) > n || !this.isWalkable(worldPoint5)) continue;
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

    private int acidHazardCost(WorldPoint worldPoint) {
        int n = 0;
        if (this.isAcidTile(worldPoint)) {
            n += 100;
        } else {
            for (int[] nArray : DIRS8) {
                if (!this.isAcidTile(new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane()))) continue;
                n += 5;
            }
        }
        if (worldPoint.equals((Object)this.lastAcidPos)) {
            n += 4;
        }
        if (this.recentTiles.contains(worldPoint)) {
            n += 8;
        }
        return n;
    }

    private boolean isAcidTile(WorldPoint worldPoint) {
        int n = worldPoint.getX() - this.client.getBaseX();
        int n2 = worldPoint.getY() - this.client.getBaseY();
        if (n < 0 || n2 < 0 || n >= 104 || n2 >= 104) {
            return false;
        }
        Tile tile = this.client.getScene().getTiles()[this.client.getPlane()][n][n2];
        if (tile == null) {
            return false;
        }
        for (GameObject gameObject : tile.getGameObjects()) {
            if (gameObject == null || !OlmConstants.isAcidId(gameObject.getId())) continue;
            return true;
        }
        GroundObject groundObject = tile.getGroundObject();
        if (groundObject != null && OlmConstants.isAcidId(groundObject.getId())) {
            return true;
        }
        DecorativeObject decorativeObject = tile.getDecorativeObject();
        return decorativeObject != null && OlmConstants.isAcidId(decorativeObject.getId());
    }

    private boolean crystalGrowthOnTile(WorldPoint worldPoint) {
        int n = worldPoint.getX() - this.client.getBaseX();
        int n2 = worldPoint.getY() - this.client.getBaseY();
        if (n < 0 || n2 < 0 || n >= 104 || n2 >= 104) {
            return false;
        }
        Tile tile = this.client.getScene().getTiles()[this.client.getPlane()][n][n2];
        if (tile == null) {
            return false;
        }
        for (GameObject gameObject : tile.getGameObjects()) {
            if (gameObject == null || !OlmConstants.isCrystalGrowthId(gameObject.getId())) continue;
            return true;
        }
        GroundObject groundObject = tile.getGroundObject();
        return groundObject != null && OlmConstants.isCrystalGrowthId(groundObject.getId());
    }

    private WorldPoint findCrystalDodgeTile(Player player) {
        int[][] nArrayArray;
        WorldPoint worldPoint = player.getWorldLocation();
        for (int[] nArray : nArrayArray = new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            WorldPoint worldPoint2 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (!this.isValidDodgeTile(worldPoint2) || this.crystalGrowthOnTile(worldPoint2) || this.isAcidTile(worldPoint2)) continue;
            return worldPoint2;
        }
        return null;
    }

    private WorldPoint findFireWallDodgeTile(Player player, TileObject tileObject) {
        WorldPoint worldPoint;
        WorldPoint worldPoint2;
        WorldPoint worldPoint3 = player.getWorldLocation();
        WorldPoint worldPoint4 = tileObject.getWorldLocation();
        int n = worldPoint3.getX() - worldPoint4.getX();
        int n2 = worldPoint3.getY() - worldPoint4.getY();
        if (Math.abs(n2) <= Math.abs(n)) {
            worldPoint2 = new WorldPoint(worldPoint3.getX(), worldPoint3.getY() + 2, worldPoint3.getPlane());
            worldPoint = new WorldPoint(worldPoint3.getX(), worldPoint3.getY() - 2, worldPoint3.getPlane());
        } else {
            worldPoint2 = new WorldPoint(worldPoint3.getX() + 2, worldPoint3.getY(), worldPoint3.getPlane());
            worldPoint = new WorldPoint(worldPoint3.getX() - 2, worldPoint3.getY(), worldPoint3.getPlane());
        }
        if (this.isValidDodgeTile(worldPoint2)) {
            return worldPoint2;
        }
        if (this.isValidDodgeTile(worldPoint)) {
            return worldPoint;
        }
        return null;
    }

    private boolean isValidDodgeTile(WorldPoint worldPoint) {
        int n = worldPoint.getX() - this.client.getBaseX();
        int n2 = worldPoint.getY() - this.client.getBaseY();
        return n >= 2 && n2 >= 2 && n <= 101 && n2 <= 101 && this.isWalkable(worldPoint);
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

    private TileObject nearestLightningInColumn(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == -1 && tileObject.getWorldLocation().getX() == worldPoint.getX() && Math.abs(tileObject.getWorldLocation().getY() - worldPoint.getY()) <= 10);
    }

    private WorldPoint findLightningDodgeTile(Player player) {
        int[] nArray;
        WorldPoint worldPoint = player.getWorldLocation();
        for (int n : nArray = new int[]{1, -1, 2, -2}) {
            WorldPoint worldPoint2 = new WorldPoint(worldPoint.getX() + n, worldPoint.getY(), worldPoint.getPlane());
            if (!this.isValidDodgeTile(worldPoint2) || this.columnHasLightning(worldPoint2.getX())) continue;
            return worldPoint2;
        }
        return null;
    }

    private boolean columnHasLightning(int n) {
        return false;
    }

    private WorldPoint findFleeTile(Player player, WorldPoint worldPoint, int n) {
        WorldPoint worldPoint2 = player.getWorldLocation();
        int n2 = Integer.signum(worldPoint2.getX() - worldPoint.getX());
        int n3 = Integer.signum(worldPoint2.getY() - worldPoint.getY());
        if (n2 == 0 && n3 == 0) {
            n2 = 1;
        }
        for (int i = n; i >= 1; --i) {
            WorldPoint worldPoint3 = new WorldPoint(worldPoint2.getX() + n2 * i, worldPoint2.getY() + n3 * i, worldPoint2.getPlane());
            if (!this.isValidDodgeTile(worldPoint3)) continue;
            return worldPoint3;
        }
        return null;
    }

    private NPC nearestOlmEntity() {
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return null;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        NPC nPC = null;
        for (NPC nPC2 : new NPC[]{this.findMageHand(), this.findMeleeHand(), this.findHead()}) {
            if (nPC2 == null || nPC != null && worldPoint.distanceTo(nPC2.getWorldLocation()) >= worldPoint.distanceTo(nPC.getWorldLocation())) continue;
            nPC = nPC2;
        }
        return nPC;
    }

    private boolean shouldEscape(Player player) {
        boolean bl;
        if (this.config.escapeItemName().isEmpty()) {
            return false;
        }
        if (this.phase != OlmPhase.COMBAT) {
            return false;
        }
        if (Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.escapeItemName().toLowerCase())) == null) {
            return false;
        }
        boolean bl2 = bl = !this.config.foodName().isEmpty() && Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.foodName().toLowerCase())) != null;
        if (bl) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 && n2 * 100 / n < this.config.escapeBelowHpPercent();
    }

    private int doEscape() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.escapeItemName().toLowerCase()));
        if (item2 != null) {
            this.status = "PANIC \u2014 escaping with " + item2.getName();
            MousePackets.queueClickPacket();
            ItemPackets.itemFirstOption(item2);
            this.phase = OlmPhase.DONE;
            return 3000;
        }
        this.status = "PANIC \u2014 no escape item & out of food!";
        return 600;
    }

    private boolean shouldEat(Player player) {
        if (this.config.foodName().isEmpty()) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.foodName().toLowerCase()));
        if (item2 == null) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 && n2 * 100 / n < this.config.eatAtHpPercent();
    }

    private int eatFood() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.foodName().toLowerCase()));
        if (item2 != null) {
            Item item3;
            this.status = "Eating " + item2.getName();
            MousePackets.queueClickPacket();
            ItemPackets.itemFirstOption(item2);
            this.resetEquipGuard();
            if (!this.config.comboFood().isEmpty() && (item3 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.comboFood().toLowerCase()))) != null) {
                MousePackets.queueClickPacket();
                ItemPackets.itemFirstOption(item3);
            }
            return 1800;
        }
        this.status = "Out of food!";
        return 600;
    }

    private boolean shouldRestorePrayer() {
        if (this.config.prayerPotionName().isEmpty()) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.PRAYER);
        int n2 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        return n > 0 && n2 * 100 / n < this.config.prayerThresholdPct();
    }

    private int drinkPrayerRestore() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.prayerPotionName().toLowerCase()));
        if (item2 != null) {
            this.status = "Drinking prayer restore";
            MousePackets.queueClickPacket();
            ItemPackets.itemFirstOption(item2);
            return 600;
        }
        return 300;
    }

    private boolean shouldDrinkCombatPotion() {
        if (!this.config.drinkAtBaseline() || this.config.combatPotionName().isEmpty()) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.combatPotionName().toLowerCase()));
        if (item2 == null) {
            return false;
        }
        int n = this.client.getBoostedSkillLevel(Skill.RANGED);
        int n2 = this.client.getRealSkillLevel(Skill.RANGED);
        if (n2 > 0 && n <= n2) {
            return true;
        }
        int n3 = this.client.getBoostedSkillLevel(Skill.MAGIC);
        int n4 = this.client.getRealSkillLevel(Skill.MAGIC);
        if (n4 > 0 && n3 <= n4) {
            return true;
        }
        int n5 = this.client.getBoostedSkillLevel(Skill.ATTACK);
        int n6 = this.client.getRealSkillLevel(Skill.ATTACK);
        return n6 > 0 && n5 <= n6;
    }

    private int drinkCombatPotion() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.combatPotionName().toLowerCase()));
        if (item2 != null) {
            this.status = "Drinking " + item2.getName();
            MousePackets.queueClickPacket();
            ItemPackets.itemFirstOption(item2);
            return 600;
        }
        return 300;
    }

    private int doLooting() {
        int n;
        if (this.lootEntryTick < 0) {
            this.lootEntryTick = this.client.getTickCount();
        }
        if ((n = this.client.getTickCount() - this.lootEntryTick) < 5) {
            this.status = "Waiting for chest (" + (5 - n) + " ticks)...";
            return 600;
        }
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 29742);
        if (tileObject2 != null) {
            this.status = "Searching reward chest";
            tileObject2.interact("Search");
            this.phase = OlmPhase.DONE;
            return 3000;
        }
        this.status = "Chest not found \u2014 waiting";
        return 600;
    }

    private void resetState() {
        this.clearPhaseTransients();
        this.phase = OlmPhase.IDLE;
        this.status = "Idle";
        this.currentTarget = "None";
        this.desiredGearStyle = "";
        this.lastStepTick = -1;
        this.lastCastAtHandTick = -1;
        this.desiredOverhead = Prayer.PROTECT_FROM_MAGIC;
        this.lastSentOverhead = null;
        this.lastSentOverheadMs = 0L;
        this.lastReactTick = -1;
        this.sphereOverhead = null;
        this.sphereUntilTick = -1;
        this.prayerDisabledUntilTick = -1;
        this.prayerWasDisabled = false;
        this.elemental = Elemental.NONE;
        this.teleportPullUntilTick = -1;
        this.crystalOnTileLastTick = false;
        this.crystalTileLastTick = null;
        this.activeMechanic = "None";
        this.lootEntryTick = -1;
        this.loggedInLastTick = false;
        this.headDamageable = Tri.UNKNOWN;
        this.headProbeStartTick = -1;
        this.headFracMin = Integer.MAX_VALUE;
        this.headFracLast = -1;
        this.headPresentLastTick = false;
        this.lastHeadAttackTick = -1;
        this.sawHead = false;
    }

    public OlmPhase getPhase() {
        return this.phase;
    }

    public String getStatus() {
        return this.status;
    }

    public String getCurrentTarget() {
        return this.currentTarget;
    }

    public int getKillCount() {
        return this.killCount;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public String getActiveMechanic() {
        return this.activeMechanic;
    }

    public String getHeadDamageable() {
        return this.headDamageable == Tri.YES ? "Yes" : (this.headDamageable == Tri.NO ? "No (cripple hands)" : "Probing");
    }

    public String getElemental() {
        return this.elemental == Elemental.NONE ? "\u2014" : this.elemental.toString();
    }

    public String getTargetReason() {
        return this.targetReason == null || this.targetReason.isEmpty() ? "\u2014" : this.targetReason;
    }

    public String getMageHandHp() {
        return this.hpLabel(this.mageHpPct, this.mageHandDisabled);
    }

    public String getMeleeHandHp() {
        return this.hpLabel(this.meleeHpPct, this.meleeHandDisabled);
    }

    public String getHeadHp() {
        return this.headHpPct < 0 ? "\u2014" : this.headHpPct + "%";
    }

    private String hpLabel(int n, boolean bl) {
        if (bl) {
            return "protected";
        }
        return n < 0 ? "\u2014" : n + "%";
    }

    private static /* synthetic */ boolean lambda$columnHasLightning$0(int n, TileObject tileObject) {
        return tileObject != null && tileObject.getId() == -1 && tileObject.getWorldLocation().getX() == n;
    }

    static enum TargetMode {
        HEAD,
        MAGE_HAND,
        MELEE_HAND;

    }

    static enum Tri {
        UNKNOWN,
        YES,
        NO;

    }

    static enum Elemental {
        NONE,
        ACID,
        CRYSTAL,
        FLAME;

    }
}

