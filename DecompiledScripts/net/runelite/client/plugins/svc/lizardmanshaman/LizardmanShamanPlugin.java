/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.CollisionData
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.plugins.PluginManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.lizardmanshaman;

import com.google.inject.Binder;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.svc.aioskiller.services.GliderTeleporter;
import net.runelite.client.plugins.svc.core.AccountProfile;
import net.runelite.client.plugins.svc.core.AdminWebhook;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.RandomEventState;
import net.runelite.client.plugins.svc.lizardmanshaman.LizardmanShamanConfig;
import net.runelite.client.plugins.svc.lizardmanshaman.LizardmanShamanConstants;
import net.runelite.client.plugins.svc.lizardmanshaman.LizardmanShamanOverlay;
import net.runelite.client.plugins.svc.lizardmanshaman.ShamanPhase;
import net.runelite.client.plugins.svc.lizardmanshaman.ShamanResupplyState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.client.managers.interaction.InteractMethod;
import net.unethicalite.client.managers.interaction.MouseBehavior;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Lizard Shaman</font></nobr></html>", description="Kills Lizardman Shamans \u2014 handles jump attacks (pray melee + run), prayer management, looting, high alching, resupply loop and death walk-back", tags={"orion", "lizardman", "shaman", "boss", "combat", "bot", "slayer"}, enabledByDefault=false)
public class LizardmanShamanPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private LizardmanShamanConfig config;
    private LizardmanShamanOverlay overlay;
    private AccountProfile profile;
    private HumanInput input;
    private GliderTeleporter glider;
    private volatile ShamanPhase phase = ShamanPhase.IDLE;
    private volatile String status = "Idle";
    private volatile int killCount = 0;
    private volatile int deathCount = 0;
    private volatile long alchProfit = 0L;
    private long startTime = 0L;
    private NPC targetShaman = null;
    private long attackStartMs = -1L;
    private long lastAttackClickMs = 0L;
    private final Map<Integer, Long> npcBlacklistUntil = new HashMap<Integer, Long>();
    private final Map<Integer, Integer> shamanAnimMap = new HashMap<Integer, Integer>();
    private NPC jumpingShaman = null;
    private WorldPoint jumpLandingTile = null;
    private WorldPoint dodgeEscapeTile = null;
    private long jumpStartedMs = -1L;
    private Prayer believedOverhead = null;
    private Prayer believedOffensive = null;
    private int lastOverheadToggleTick = -1;
    private int lastOffensiveToggleTick = -1;
    private volatile boolean beliefsInvalid = false;
    private long lastCombatMs = 0L;
    private WorldPoint killLocation = null;
    private String droppedFoodName = null;
    private long droppedFoodAtMs = 0L;
    private volatile ShamanResupplyState resupplyState = null;
    private long resupplyStateStartMs = 0L;
    private int routeAttempts = 0;
    private int lootingEntryTick = -1;
    private int homeAttempts = 0;
    private long homeAttemptAtMs = 0L;
    private WorldPoint homeTeleAnchor = null;
    private boolean rejuvenateClicked = false;
    private int presetAttempts = 0;
    private long presetClickAtMs = 0L;
    private int killsSinceResupply = 0;
    private boolean playerDied = false;
    private volatile boolean lockedPresetSeen = false;
    private volatile long busyNpcAtMs = 0L;
    private long lastBusyHandledMs = 0L;
    private static final Pattern BUSY_NPC_PATTERN = Pattern.compile("already in combat|creature belongs to|can't reach that|cannot reach");
    private static final Pattern LOCKED_PRESET_PATTERN = Pattern.compile("may not load locked presets|locked preset");
    private final Random breakRng = new Random();
    private volatile long nextBreakAtMs = 0L;
    private volatile long breakUntilMs = 0L;
    private volatile int breaksTaken = 0;
    private boolean emergencyActive = false;
    private long idleSinceMs = -1L;
    private boolean idleAlerted = false;
    private boolean loggedInLastTick = false;
    private boolean stopIssued = false;
    private static final String UNETH_GROUP = "unethicalite";
    private String savedInteractMethod;
    private String savedNaturalMouse;
    private String savedMouseBehavior;
    private boolean inputModeApplied = false;
    private ExecutorService alertExec;
    private static final int ALERT_RED = 15158332;
    private static final int ALERT_AMBER = 15105570;
    private static final int ALERT_GREEN = 3066993;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(LizardmanShamanConfig.class).toInstance((Object)((LizardmanShamanConfig)configManager.getConfig(LizardmanShamanConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.profile = new AccountProfile(this.client);
        this.input = new HumanInput(this.client, this.profile);
        this.glider = new GliderTeleporter(this.input);
        this.overlay = new LizardmanShamanOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
        this.startTime = System.currentTimeMillis();
        this.status = "Starting";
        this.applyInputMode();
    }

    @Override
    protected void onStop() throws Exception {
        this.restoreInputMode();
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        try {
            if (this.believedOverhead != null) {
                Prayers.toggle(this.believedOverhead);
            }
            if (this.believedOffensive != null) {
                Prayers.toggle(this.believedOffensive);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.believedOverhead = null;
        this.believedOffensive = null;
        if (this.alertExec != null) {
            this.alertExec.shutdownNow();
            this.alertExec = null;
        }
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

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
            this.beliefsInvalid = true;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String string = chatMessage.getMessage();
        if (string == null) {
            return;
        }
        String string2 = string.replaceAll("<[^>]*>", "").toLowerCase();
        if (LOCKED_PRESET_PATTERN.matcher(string2).find()) {
            this.lockedPresetSeen = true;
        }
        if (BUSY_NPC_PATTERN.matcher(string2).find()) {
            this.busyNpcAtMs = System.currentTimeMillis();
        }
    }

    @Override
    protected int loop() {
        int n;
        int n2;
        int n3;
        Item item2;
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
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
        if (this.beliefsInvalid) {
            this.beliefsInvalid = false;
            this.believedOverhead = null;
            this.believedOffensive = null;
        }
        if (this.phase == ShamanPhase.STUCK) {
            return 5000;
        }
        if (this.phase == ShamanPhase.DEAD) {
            return this.handleDead(player);
        }
        if (this.client.getRealSkillLevel(Skill.HITPOINTS) > 0 && this.client.getBoostedSkillLevel(Skill.HITPOINTS) <= 0) {
            return this.enterDeath(player);
        }
        if (this.phase == ShamanPhase.RESUPPLYING) {
            return this.handleResupply(player);
        }
        if (RandomEventState.isActive()) {
            if (this.shouldEat()) {
                return this.eatFood();
            }
            this.status = "Paused \u2014 " + RandomEventState.label();
            return 1500;
        }
        this.scanShamanAnimations(player);
        if (this.config.stopAfterKills() > 0 && this.killCount >= this.config.stopAfterKills()) {
            return this.handleStopAfterKills();
        }
        if (this.shouldEat(player)) {
            return this.eatFood();
        }
        if (this.shouldDrinkAntipoison()) {
            return this.drinkByName(this.config.antipoisonName(), "Drinking antipoison");
        }
        if (this.shouldRestorePrayer()) {
            return this.drinkByName(this.config.prayerPotionName(), "Drinking prayer restore");
        }
        if (this.phase == ShamanPhase.DODGING_JUMP) {
            return this.handleJumpDodge(player);
        }
        if (this.demonicGorillaTargetingUs(player)) {
            return this.fleeFromGorilla(player);
        }
        if (this.config.fleeFromSpawns() && this.spawnsNearby(player)) {
            return this.fleeFromSpawns(player);
        }
        int n4 = this.emergencyCheck(player);
        if (n4 > 0) {
            return n4;
        }
        int n5 = this.maybeBreak(player);
        if (n5 > 0) {
            return n5;
        }
        this.maintainPrayers(player);
        if (this.busyNpcAtMs > this.lastBusyHandledMs && this.targetShaman != null) {
            this.lastBusyHandledMs = this.busyNpcAtMs;
            this.blacklist(this.targetShaman.getIndex(), 15000L);
            this.targetShaman = null;
            this.attackStartMs = -1L;
            this.status = "Server refused attack \u2014 switching target";
            return 400;
        }
        if (this.shouldDrinkRangingPotion(player)) {
            return this.drinkByName(this.config.rangingPotionName(), "Drinking ranging potion");
        }
        if (this.shouldDrinkStamina()) {
            return this.drinkByName("stamina", "Drinking stamina potion");
        }
        if (this.targetShaman != null && !this.targetShaman.isDead()) {
            return this.attackShaman(this.targetShaman, player);
        }
        boolean bl = this.underAttack(player);
        if (this.config.buryBones() && !bl && (item2 = Inventory.getFirst(item -> item != null && item.getId() != -1 && !this.isNoted((Item)item) && (this.itemName((Item)item).toLowerCase().contains("bones") || this.itemName((Item)item).toLowerCase().contains("ashes")))) != null) {
            this.status = "Burying " + this.itemName(item2);
            this.consume(item2);
            return 900;
        }
        if (this.config.enableHighAlch() && !bl && (n3 = this.handleAlch(player)) > 0) {
            this.idleSinceMs = -1L;
            return n3;
        }
        if (this.config.enableLooting() && (n2 = this.tryLoot(player)) > 0) {
            this.idleSinceMs = -1L;
            return n2;
        }
        if (this.config.enableHighAlch() && !bl && (n = this.alchLootNearby(player)) > 0) {
            this.idleSinceMs = -1L;
            return n;
        }
        if (this.config.enableResupply() && this.shouldResupply()) {
            this.beginResupply(false);
            this.status = "Supplies low \u2014 resupplying";
            return 600;
        }
        this.pruneBlacklist();
        item2 = this.findTarget(player);
        if (item2 != null) {
            this.idleSinceMs = -1L;
            this.idleAlerted = false;
            this.targetShaman = item2;
            this.phase = ShamanPhase.FIGHTING;
            this.status = "Attacking Lizardman Shaman";
            this.lastAttackClickMs = this.attackStartMs = System.currentTimeMillis();
            this.input.attackNpc((NPC)item2);
            return 600;
        }
        return this.handleIdle(player);
    }

    private int enterDeath(Player player) {
        this.phase = ShamanPhase.DEAD;
        ++this.deathCount;
        this.playerDied = true;
        this.targetShaman = null;
        this.attackStartMs = -1L;
        this.jumpingShaman = null;
        this.jumpLandingTile = null;
        this.dodgeEscapeTile = null;
        this.droppedFoodName = null;
        this.emergencyActive = false;
        this.believedOverhead = null;
        this.believedOffensive = null;
        this.status = "Died \u2014 waiting for respawn";
        this.postAlert("Shaman bot died", "Death #" + this.deathCount + " at " + LizardmanShamanPlugin.describe(player.getWorldLocation()) + "\nKills this session: " + this.killCount, 15158332);
        return 1200;
    }

    private int handleDead(Player player) {
        if (this.client.getBoostedSkillLevel(Skill.HITPOINTS) <= 0) {
            this.status = "Dead \u2014 waiting for respawn";
            return 1200;
        }
        this.playerDied = false;
        this.killLocation = null;
        if (!this.config.enableResupply()) {
            return this.hardStuck("Died with the resupply loop disabled \u2014 no route back. Enable Resupply / Banking and restart the plugin.");
        }
        this.beginResupply(true);
        this.status = "Respawned \u2014 walking back";
        return 900;
    }

    private int handleStopAfterKills() {
        if (!this.stopIssued) {
            this.stopIssued = true;
            this.status = "Reached " + this.killCount + " kills \u2014 stopping";
            this.prayersOff();
            this.postAlert("Kill target reached", this.killCount + " kills \u2014 plugin stopping", 3066993);
            this.clientThread.invokeLater(() -> {
                try {
                    this.pluginManager.setPluginEnabled((Plugin)this, false);
                    this.pluginManager.stopPlugin((Plugin)this);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            });
        }
        return 2000;
    }

    private void scanShamanAnimations(Player player) {
        List<NPC> list = NPCs.getAll(nPC -> this.isShamanId(nPC.getId()) && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= 20);
        for (NPC nPC2 : list) {
            int n2;
            int n3 = nPC2.getIndex();
            int n4 = nPC2.getAnimation();
            if (n4 == (n2 = this.shamanAnimMap.getOrDefault(n3, -1).intValue()) || n4 == -1) continue;
            if (n4 == 7152) {
                this.onJumpDetected(nPC2, player);
            } else if (n4 == 7196) {
                this.onShamanDeath(nPC2, player);
            } else if (n4 == 6946 && this.phase == ShamanPhase.DODGING_JUMP && this.jumpingShaman != null && n3 == this.jumpingShaman.getIndex() && (this.jumpLandingTile == null || player.getWorldLocation().distanceTo(this.jumpLandingTile) >= this.config.jumpEscapeDistance())) {
                this.clearJumpDodge();
            }
            this.shamanAnimMap.put(n3, n4);
        }
        this.shamanAnimMap.keySet().removeIf(n -> list.stream().noneMatch(nPC -> nPC.getIndex() == n.intValue()));
    }

    private void onJumpDetected(NPC nPC, Player player) {
        if (this.phase == ShamanPhase.DODGING_JUMP) {
            this.jumpLandingTile = player.getWorldLocation();
            this.dodgeEscapeTile = null;
            return;
        }
        this.phase = ShamanPhase.DODGING_JUMP;
        this.jumpingShaman = nPC;
        this.jumpStartedMs = System.currentTimeMillis();
        this.jumpLandingTile = player.getWorldLocation();
        this.dodgeEscapeTile = this.findEscapeTile(player);
        this.status = "JUMP! Pray melee + run";
        this.assertOverhead(Prayer.PROTECT_FROM_MELEE);
    }

    private void onShamanDeath(NPC nPC, Player player) {
        boolean bl;
        boolean bl2 = bl = this.targetShaman != null && nPC.getIndex() == this.targetShaman.getIndex();
        if (!bl) {
            Actor actor = nPC.getInteracting();
            boolean bl3 = bl = actor != null && actor == player;
        }
        if (!bl) {
            return;
        }
        ++this.killCount;
        ++this.killsSinceResupply;
        this.killLocation = nPC.getWorldLocation();
        if (this.targetShaman != null && nPC.getIndex() == this.targetShaman.getIndex()) {
            this.targetShaman = null;
            this.attackStartMs = -1L;
        }
        if (this.phase != ShamanPhase.DODGING_JUMP) {
            this.phase = ShamanPhase.IDLE;
        }
        this.status = "Kill #" + this.killCount;
    }

    private int handleJumpDodge(Player player) {
        int n;
        this.maintainPrayers(player);
        if (this.jumpStartedMs > 0L && System.currentTimeMillis() - this.jumpStartedMs > 6000L) {
            this.clearJumpDodge();
            return 300;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        int n2 = n = this.jumpLandingTile != null ? worldPoint.distanceTo(this.jumpLandingTile) : 99;
        if (n >= this.config.jumpEscapeDistance()) {
            this.clearJumpDodge();
            this.status = "Jump dodged \u2014 resuming";
            return 300;
        }
        if (this.dodgeEscapeTile == null) {
            this.dodgeEscapeTile = this.findEscapeTile(player);
        }
        if (this.dodgeEscapeTile != null) {
            this.status = "JUMP \u2014 running (" + n + "/" + this.config.jumpEscapeDistance() + " tiles)";
            this.input.walkToward(worldPoint, this.dodgeEscapeTile);
        } else {
            this.status = "JUMP \u2014 no walkable escape tile!";
        }
        return 300;
    }

    private WorldPoint findEscapeTile(Player player) {
        WorldPoint worldPoint = this.jumpingShaman != null ? this.jumpingShaman.getWorldLocation() : this.jumpLandingTile;
        return this.awayFrom(player, worldPoint, this.config.jumpEscapeDistance());
    }

    private void clearJumpDodge() {
        this.phase = this.targetShaman != null ? ShamanPhase.FIGHTING : ShamanPhase.IDLE;
        this.jumpingShaman = null;
        this.jumpLandingTile = null;
        this.dodgeEscapeTile = null;
        this.jumpStartedMs = -1L;
    }

    private boolean isDemonicGorilla(int n) {
        return n == 7995 || n == 7996 || n == 7997 || n == 7998;
    }

    private boolean demonicGorillaTargetingUs(Player player) {
        return NPCs.getNearest(nPC -> this.isDemonicGorilla(nPC.getId()) && !nPC.isDead() && player.equals((Object)nPC.getInteracting())) != null;
    }

    private int fleeFromGorilla(Player player) {
        NPC nPC2 = NPCs.getNearest(nPC -> this.isDemonicGorilla(nPC.getId()) && !nPC.isDead() && player.equals((Object)nPC.getInteracting()));
        if (nPC2 == null) {
            return 300;
        }
        WorldPoint worldPoint = this.awayFrom(player, nPC2.getWorldLocation(), 10);
        this.status = "Fleeing Demonic Gorilla!";
        if (worldPoint != null) {
            this.input.walkToward(player.getWorldLocation(), worldPoint);
        }
        return 600;
    }

    private boolean spawnsNearby(Player player) {
        return NPCs.getNearest(nPC -> this.isSpawnId(nPC.getId()) && !nPC.isDead() && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= 2) != null;
    }

    private int fleeFromSpawns(Player player) {
        List<NPC> list = NPCs.getAll(nPC -> this.isSpawnId(nPC.getId()) && !nPC.isDead() && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= 3);
        if (list.isEmpty()) {
            return 300;
        }
        int n = list.stream().mapToInt(nPC -> nPC.getWorldLocation().getX()).sum() / list.size();
        int n2 = list.stream().mapToInt(nPC -> nPC.getWorldLocation().getY()).sum() / list.size();
        WorldPoint worldPoint = new WorldPoint(n, n2, player.getWorldLocation().getPlane());
        WorldPoint worldPoint2 = this.awayFrom(player, worldPoint, 3);
        this.status = "Avoiding spawn explosion!";
        if (worldPoint2 != null) {
            this.input.walkToward(player.getWorldLocation(), worldPoint2);
        }
        return 300;
    }

    private WorldPoint awayFrom(Player player, WorldPoint worldPoint, int n) {
        int[][] nArrayArray;
        int n2;
        int n3;
        int n4;
        int n5;
        double d;
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (worldPoint == null) {
            worldPoint = worldPoint2;
        }
        if ((d = Math.sqrt((double)(n5 = worldPoint2.getX() - worldPoint.getX()) * (double)n5 + (double)(n4 = worldPoint2.getY() - worldPoint.getY()) * (double)n4)) > 0.5) {
            n3 = (int)Math.round((double)n5 / d * (double)n);
            n2 = (int)Math.round((double)n4 / d * (double)n);
        } else {
            n3 = 0;
            n2 = -n;
        }
        for (int[] nArray : nArrayArray = new int[][]{{n3, n2}, {n3 + 1, n2}, {n3 - 1, n2}, {n3, n2 + 1}, {n3, n2 - 1}, {0, -n}, {0, n}, {-n, 0}, {n, 0}}) {
            WorldPoint worldPoint3 = new WorldPoint(worldPoint2.getX() + nArray[0], worldPoint2.getY() + nArray[1], worldPoint2.getPlane());
            if (!this.isWalkable(worldPoint3)) continue;
            return worldPoint3;
        }
        return null;
    }

    private void maintainPrayers(Player player) {
        Prayer prayer;
        Prayer prayer2;
        boolean bl;
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        long l = System.currentTimeMillis();
        boolean bl2 = bl = this.targetShaman != null || this.underAttack(player);
        if (bl) {
            this.lastCombatMs = l;
        }
        if (this.phase == ShamanPhase.DODGING_JUMP) {
            prayer2 = Prayer.PROTECT_FROM_MELEE;
            prayer = this.configOffensive();
        } else if (bl || l - this.lastCombatMs < 8000L) {
            prayer2 = this.configOverhead();
            prayer = this.configOffensive();
        } else {
            prayer2 = null;
            prayer = null;
        }
        this.assertOverhead(prayer2);
        this.assertOffensive(prayer);
    }

    private Prayer configOverhead() {
        return this.config.overheadPrayer() != null ? this.config.overheadPrayer().prayer : null;
    }

    private Prayer configOffensive() {
        return this.config.offensivePrayer() != null ? this.config.offensivePrayer().prayer : null;
    }

    private void assertOverhead(Prayer prayer) {
        boolean bl;
        int n = this.client.getTickCount();
        if (prayer == null) {
            if (this.believedOverhead != null && n > this.lastOverheadToggleTick) {
                this.togglePrayer(this.believedOverhead);
                this.believedOverhead = null;
                this.lastOverheadToggleTick = n;
            }
            return;
        }
        boolean bl2 = bl = !prayer.equals((Object)this.believedOverhead);
        if (bl || !Prayers.isEnabled(prayer) && n > this.lastOverheadToggleTick) {
            this.togglePrayer(prayer);
            this.believedOverhead = prayer;
            this.lastOverheadToggleTick = n;
        }
    }

    private void assertOffensive(Prayer prayer) {
        boolean bl;
        int n = this.client.getTickCount();
        if (prayer == null) {
            if (this.believedOffensive != null && n > this.lastOffensiveToggleTick) {
                this.togglePrayer(this.believedOffensive);
                this.believedOffensive = null;
                this.lastOffensiveToggleTick = n;
            }
            return;
        }
        boolean bl2 = bl = !prayer.equals((Object)this.believedOffensive);
        if (bl || !Prayers.isEnabled(prayer) && n > this.lastOffensiveToggleTick) {
            this.togglePrayer(prayer);
            this.believedOffensive = prayer;
            this.lastOffensiveToggleTick = n;
        }
    }

    private void togglePrayer(Prayer prayer) {
        this.input.primedDriftOp(() -> Prayers.toggle(prayer));
    }

    private void prayersOff() {
        this.assertOverhead(null);
        this.assertOffensive(null);
    }

    private boolean underAttack(Player player) {
        return NPCs.getNearest(nPC -> (this.isShamanId(nPC.getId()) || this.isDemonicGorilla(nPC.getId())) && !nPC.isDead() && player.equals((Object)nPC.getInteracting()) && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= 15) != null;
    }

    private NPC findTarget(Player player) {
        return NPCs.getNearest(nPC -> this.isShamanId(nPC.getId()) && !nPC.isDead() && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= this.config.searchRadius() && !this.isBlacklisted(nPC.getIndex()) && (!this.config.onlyIdle() || nPC.getInteracting() == null || player.equals((Object)nPC.getInteracting())));
    }

    private int attackShaman(NPC nPC, Player player) {
        Actor actor = player.getInteracting();
        if (actor instanceof NPC && ((NPC)actor).getIndex() == nPC.getIndex() && !nPC.isDead()) {
            this.attackStartMs = -1L;
            this.phase = ShamanPhase.FIGHTING;
            this.status = "Fighting Lizardman Shaman (#" + (this.killCount + 1) + ")";
            return 600;
        }
        long l = System.currentTimeMillis();
        if (this.attackStartMs < 0L) {
            this.attackStartMs = l;
            this.lastAttackClickMs = l;
            this.status = "Attacking Lizardman Shaman";
            this.input.attackNpc(nPC);
            return 600;
        }
        if (l - this.attackStartMs > 5000L) {
            this.blacklist(nPC.getIndex(), 10000L);
            this.targetShaman = null;
            this.attackStartMs = -1L;
            this.phase = ShamanPhase.IDLE;
            this.status = "Target lost \u2014 finding new shaman";
            return 300;
        }
        if (l - this.lastAttackClickMs > 1700L) {
            this.lastAttackClickMs = l;
            this.status = "Attacking Lizardman Shaman";
            this.input.attackNpc(nPC);
        }
        return 600;
    }

    private void blacklist(int n, long l) {
        this.npcBlacklistUntil.put(n, System.currentTimeMillis() + l);
    }

    private boolean isBlacklisted(int n) {
        Long l = this.npcBlacklistUntil.get(n);
        return l != null && l > System.currentTimeMillis();
    }

    private void pruneBlacklist() {
        long l = System.currentTimeMillis();
        this.npcBlacklistUntil.values().removeIf(l2 -> l2 <= l);
    }

    private int handleIdle(Player player) {
        long l = System.currentTimeMillis();
        if (this.idleSinceMs < 0L) {
            this.idleSinceMs = l;
        }
        this.phase = ShamanPhase.IDLE;
        if (player.getWorldLocation().distanceTo(LizardmanShamanConstants.SHAMAN_AREA_TILE) > 15) {
            this.status = "No shamans \u2014 walking to spawn area";
            this.input.walkToward(player.getWorldLocation(), LizardmanShamanConstants.SHAMAN_AREA_TILE);
            return 1200;
        }
        if (this.config.enableResupply() && this.killsSinceResupply > 0 && l - this.idleSinceMs >= 20000L) {
            this.status = "Idle too long \u2014 resupplying";
            this.idleSinceMs = -1L;
            this.beginResupply(false);
            return 600;
        }
        if (l - this.idleSinceMs >= 90000L && !this.idleAlerted) {
            this.idleAlerted = true;
            this.postAlert("Shaman bot idle", "No shamans found for 90s at " + LizardmanShamanPlugin.describe(player.getWorldLocation()) + " \u2014 check the spawn area / glider destination.", 15105570);
        }
        this.status = "Idle \u2014 no shamans nearby";
        return 900;
    }

    private int maybeBreak(Player player) {
        boolean bl;
        if (!this.config.microBreaks()) {
            this.breakUntilMs = 0L;
            if (this.phase == ShamanPhase.BREAK) {
                this.phase = ShamanPhase.IDLE;
            }
            return 0;
        }
        long l = System.currentTimeMillis();
        if (this.breakUntilMs > 0L) {
            if (this.underAttack(player)) {
                this.breakUntilMs = 0L;
                this.scheduleNextBreak(l);
                this.phase = ShamanPhase.IDLE;
                return 0;
            }
            if (l < this.breakUntilMs) {
                this.phase = ShamanPhase.BREAK;
                this.status = "Short break \u2014 " + (this.breakUntilMs - l) / 1000L + "s left";
                return 1000 + this.breakRng.nextInt(600);
            }
            this.breakUntilMs = 0L;
            this.scheduleNextBreak(l);
            this.phase = ShamanPhase.IDLE;
            return 0;
        }
        if (this.nextBreakAtMs == 0L) {
            this.scheduleNextBreak(l);
            return 0;
        }
        boolean bl2 = bl = (this.phase == ShamanPhase.IDLE || this.phase == ShamanPhase.FIGHTING) && this.targetShaman == null && this.resupplyState == null && this.droppedFoodName == null && !this.underAttack(player);
        if (l >= this.nextBreakAtMs && bl) {
            long l2 = this.breakRng.nextDouble() < 0.8 ? 15000L + (long)this.breakRng.nextInt(60000) : 90000L + (long)this.breakRng.nextInt(150000);
            this.breakUntilMs = l + l2;
            ++this.breaksTaken;
            this.phase = ShamanPhase.BREAK;
            this.prayersOff();
            this.status = "Short break \u2014 " + l2 / 1000L + "s";
            return 1200;
        }
        return 0;
    }

    private void scheduleNextBreak(long l) {
        this.nextBreakAtMs = l + 420000L + (long)this.breakRng.nextInt(840000);
    }

    private int emergencyCheck(Player player) {
        boolean bl;
        if (!this.config.retreatWhenNoFood() || this.config.foodName().trim().isEmpty()) {
            return 0;
        }
        boolean bl2 = bl = this.findUnnoted(this.config.foodName()) == null;
        if (!this.emergencyActive) {
            if (bl && this.hpPercent() < 35) {
                if (this.config.enableResupply()) {
                    this.beginResupply(false);
                    this.status = "Out of food \u2014 emergency resupply";
                    return 600;
                }
                this.emergencyActive = true;
                this.targetShaman = null;
                this.attackStartMs = -1L;
                this.prayersOff();
                this.postAlert("Shaman bot retreating", "Out of food at " + this.hpPercent() + "% HP \u2014 backing off to regenerate (resupply loop is disabled).", 15105570);
            }
            return 0;
        }
        if (!bl || this.hpPercent() >= 70) {
            this.emergencyActive = false;
            return 0;
        }
        NPC nPC2 = NPCs.getNearest(nPC -> this.isShamanId(nPC.getId()) && !nPC.isDead() && nPC.getWorldLocation().distanceTo(player.getWorldLocation()) <= 10);
        if (nPC2 != null) {
            WorldPoint worldPoint = this.awayFrom(player, nPC2.getWorldLocation(), 8);
            if (worldPoint != null) {
                this.input.walkToward(player.getWorldLocation(), worldPoint);
            }
            this.status = "Emergency retreat \u2014 moving away";
            return 1200;
        }
        this.status = "Emergency retreat \u2014 regenerating HP (" + this.hpPercent() + "%)";
        return 2000;
    }

    private int tryLoot(Player player) {
        int n;
        if (this.droppedFoodName != null && (n = this.recoverDroppedFood(player)) > 0) {
            return n;
        }
        TileItem tileItem = this.findLoot(player);
        if (tileItem == null) {
            return 0;
        }
        if (Inventory.getAll().size() >= 28) {
            Item item;
            if (this.config.eatToLoot() && (item = this.findUnnoted(this.config.foodName())) != null) {
                this.status = "Eating to free slot for loot";
                this.consume(item);
                return 900;
            }
            return 0;
        }
        this.status = "Looting " + this.defName(tileItem);
        this.input.interactGroundItem(tileItem, "Take");
        return 900;
    }

    private TileItem findLoot(Player player) {
        return TileItems.getNearest(tileItem -> {
            if (tileItem == null) {
                return false;
            }
            if (tileItem.getWorldLocation().distanceTo(player.getWorldLocation()) > this.config.lootRadius()) {
                return false;
            }
            String string = this.defName((TileItem)tileItem).toLowerCase();
            if (LizardmanShamanPlugin.matchesAny(string, this.config.excludeLootNames())) {
                return false;
            }
            if (LizardmanShamanPlugin.matchesAny(string, this.config.lootNames())) {
                return true;
            }
            return this.config.lootMinValue() > 0 && this.haValue(tileItem.getId()) >= this.config.lootMinValue();
        });
    }

    private int recoverDroppedFood(Player player) {
        if (Inventory.getFirst(item -> item != null && item.getId() != -1 && this.itemName((Item)item).toLowerCase().contains(this.droppedFoodName)) != null) {
            this.droppedFoodName = null;
            return 0;
        }
        TileItem tileItem2 = TileItems.getNearest(tileItem -> tileItem != null && tileItem.getWorldLocation().distanceTo(player.getWorldLocation()) <= 5 && this.defName((TileItem)tileItem).toLowerCase().contains(this.droppedFoodName));
        if (tileItem2 != null && Inventory.getAll().size() < 28) {
            this.status = "Recovering dropped food";
            this.input.interactGroundItem(tileItem2, "Take");
            return 900;
        }
        if (System.currentTimeMillis() - this.droppedFoodAtMs > 15000L) {
            this.droppedFoodName = null;
        }
        return 0;
    }

    private int handleAlch(Player player) {
        Item item2 = Inventory.getFirst(item -> item != null && item.getId() != -1 && this.shouldAlchItem(this.itemName((Item)item).toLowerCase(), item.getId()));
        if (item2 == null || !this.canCastHighAlch()) {
            return 0;
        }
        this.status = "High Alching " + this.itemName(item2);
        this.alchProfit += (long)this.haValue(item2.getId());
        this.input.primedItemOp(item2, () -> Magic.cast((Spell)SpellBook.Standard.HIGH_LEVEL_ALCHEMY, item2));
        return 1800;
    }

    private boolean shouldAlchItem(String string, int n) {
        if (string.isEmpty()) {
            return false;
        }
        if (LizardmanShamanPlugin.matchesAny(string, this.config.excludeLootNames())) {
            return false;
        }
        if (LizardmanShamanPlugin.matchesAny(string, this.config.lootNames())) {
            return false;
        }
        if (this.config.lootMinValue() > 0 && this.haValue(n) >= this.config.lootMinValue()) {
            return false;
        }
        if (LizardmanShamanPlugin.matchesAny(string, this.config.alchExcludeNames())) {
            return false;
        }
        if (LizardmanShamanPlugin.matchesAny(string, this.config.alchItemNames())) {
            return true;
        }
        int n2 = this.config.alchMinValue();
        return n2 > 0 && this.haValue(n) >= n2;
    }

    private boolean canCastHighAlch() {
        try {
            if (this.client.getVarbitValue(4070) != 0) {
                return false;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (this.client.getBoostedSkillLevel(Skill.MAGIC) < 55) {
            return false;
        }
        if (!this.config.verifyAlchRunes()) {
            return true;
        }
        return this.countByName("nature rune") >= 1 && this.countByName("fire rune") >= 5;
    }

    private int alchLootNearby(Player player) {
        TileItem tileItem2 = TileItems.getNearest(tileItem -> tileItem != null && tileItem.getWorldLocation().distanceTo(player.getWorldLocation()) <= this.config.lootRadius() && this.shouldAlchItem(this.defName((TileItem)tileItem).toLowerCase(), tileItem.getId()));
        if (tileItem2 == null) {
            return 0;
        }
        if (Inventory.getAll().size() >= 28) {
            Item item;
            if (this.config.eatToAlch() && (item = this.findUnnoted(this.config.foodName())) != null) {
                this.status = "Eating to free slot for alch item";
                this.consume(item);
                return 900;
            }
            if (this.config.dropToAlch() && this.droppedFoodName == null && (item = this.findUnnoted(this.config.foodName())) != null) {
                this.droppedFoodName = this.itemName(item).toLowerCase();
                this.droppedFoodAtMs = System.currentTimeMillis();
                this.status = "Dropping food to free slot for alch item";
                this.input.itemInteractVia(item, "Drop");
                return 900;
            }
            return 0;
        }
        this.status = "Picking up (alch) " + this.defName(tileItem2);
        this.input.interactGroundItem(tileItem2, "Take");
        return 900;
    }

    private void consume(Item item) {
        if (item == null) {
            return;
        }
        this.input.primedItemOp(item, () -> {
            MousePackets.queueClickPacket();
            ItemPackets.itemFirstOption(item);
        });
    }

    private boolean shouldEat() {
        Player player = this.client.getLocalPlayer();
        return player != null && this.shouldEat(player);
    }

    private boolean shouldEat(Player player) {
        if (this.config.foodName().trim().isEmpty()) {
            return false;
        }
        if (this.findUnnoted(this.config.foodName()) == null) {
            return false;
        }
        return this.hpPercent() < this.config.eatAtHpPercent();
    }

    private int eatFood() {
        Item item;
        Item item2 = this.findUnnoted(this.config.foodName());
        if (item2 == null) {
            this.status = "Out of food!";
            return 600;
        }
        this.status = "Eating " + this.itemName(item2);
        this.consume(item2);
        if (!this.config.comboFoodName().trim().isEmpty() && this.hpPercent() < this.config.comboEatAtHpPercent() && (item = this.findUnnoted(this.config.comboFoodName())) != null) {
            this.consume(item);
        }
        return 1800;
    }

    private boolean shouldDrinkAntipoison() {
        if (this.config.antipoisonName().trim().isEmpty()) {
            return false;
        }
        try {
            if (this.client.getVar(VarPlayer.IS_POISONED) <= 0) {
                return false;
            }
        }
        catch (Throwable throwable) {
            return false;
        }
        return this.findUnnoted(this.config.antipoisonName()) != null;
    }

    private boolean shouldRestorePrayer() {
        if (this.config.prayerPotionName().trim().isEmpty()) {
            return false;
        }
        if (this.findUnnoted(this.config.prayerPotionName()) == null) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.PRAYER);
        int n2 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        return n > 0 && n2 * 100 / n < this.config.prayerThresholdPct();
    }

    private boolean shouldDrinkRangingPotion(Player player) {
        if (this.config.rangingPotionName().trim().isEmpty()) {
            return false;
        }
        if (this.targetShaman == null && !this.underAttack(player)) {
            return false;
        }
        if (this.findUnnoted(this.config.rangingPotionName()) == null) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.RANGED);
        int n2 = this.client.getBoostedSkillLevel(Skill.RANGED);
        return n2 <= n;
    }

    private boolean shouldDrinkStamina() {
        if (!this.config.drinkStamina()) {
            return false;
        }
        if (this.findUnnoted("stamina") == null) {
            return false;
        }
        return this.client.getEnergy() / 100 < this.config.staminaThreshold();
    }

    private int drinkByName(String string, String string2) {
        Item item = this.findUnnoted(string);
        if (item == null) {
            return 300;
        }
        this.status = string2;
        this.consume(item);
        return 1800;
    }

    private int hpPercent() {
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 ? n2 * 100 / n : 100;
    }

    private boolean shouldResupply() {
        if (!this.config.foodName().trim().isEmpty() && this.findUnnoted(this.config.foodName()) == null) {
            return true;
        }
        return !this.config.prayerPotionName().trim().isEmpty() && this.findUnnoted(this.config.prayerPotionName()) == null;
    }

    private void beginResupply(boolean bl) {
        this.phase = ShamanPhase.RESUPPLYING;
        this.routeAttempts = 0;
        this.lockedPresetSeen = false;
        this.targetShaman = null;
        this.attackStartMs = -1L;
        this.emergencyActive = false;
        this.enterResupplyState(bl ? ShamanResupplyState.GO_HOME : ShamanResupplyState.LOOTING);
        if (bl) {
            this.prayersOff();
        }
    }

    private void enterResupplyState(ShamanResupplyState shamanResupplyState) {
        this.resupplyState = shamanResupplyState;
        this.resupplyStateStartMs = System.currentTimeMillis();
        switch (shamanResupplyState) {
            case LOOTING: {
                this.lootingEntryTick = -1;
                break;
            }
            case GO_HOME: {
                this.homeAttempts = 0;
                this.homeAttemptAtMs = 0L;
                this.homeTeleAnchor = null;
                break;
            }
            case REJUVENATE: {
                this.rejuvenateClicked = false;
                break;
            }
            case BANK_PRESET: {
                this.presetAttempts = 0;
                this.presetClickAtMs = 0L;
                break;
            }
            case GLIDER: {
                this.glider.begin();
                break;
            }
        }
    }

    private int handleResupply(Player player) {
        if (this.resupplyState == null) {
            this.enterResupplyState(ShamanResupplyState.LOOTING);
        }
        long l = System.currentTimeMillis();
        long l2 = l - this.resupplyStateStartMs;
        WorldPoint worldPoint = player.getWorldLocation();
        switch (this.resupplyState) {
            case LOOTING: {
                Object object;
                if (this.lootingEntryTick < 0) {
                    this.lootingEntryTick = this.client.getTickCount();
                }
                if (Inventory.getAll().size() >= 28 && (object = this.findUnnoted(this.config.foodName())) != null) {
                    this.consume((Item)object);
                    return 900;
                }
                object = this.config.resupplyLootNames().trim().isEmpty() ? this.config.lootNames() : this.config.resupplyLootNames();
                TileItem tileItem = TileItems.getNearest(arg_0 -> this.lambda$handleResupply$0(worldPoint, (String)object, arg_0));
                if (tileItem != null && Inventory.getAll().size() < 28) {
                    this.status = "Resupply: looting " + this.defName(tileItem);
                    this.input.interactGroundItem(tileItem, "Take");
                    return 900;
                }
                int n = this.client.getTickCount() - this.lootingEntryTick;
                if (n < 8) {
                    this.status = "Resupply: waiting for drops (" + (8 - n) + ")...";
                    return 600;
                }
                if (this.hasRequiredItems()) {
                    this.status = "Supplies OK \u2014 skipping bank";
                    this.finishResupply();
                    return 600;
                }
                this.prayersOff();
                this.enterResupplyState(ShamanResupplyState.GO_HOME);
                return 600;
            }
            case GO_HOME: {
                if (l2 > 45000L) {
                    return this.routeFail("home teleport timed out");
                }
                if (this.atHome(player)) {
                    this.enterResupplyState(ShamanResupplyState.REJUVENATE);
                    return 600;
                }
                if (this.homeTeleAnchor != null && worldPoint.distanceTo(this.homeTeleAnchor) > 30) {
                    this.enterResupplyState(ShamanResupplyState.REJUVENATE);
                    return 600;
                }
                if (this.homeAttemptAtMs == 0L || l - this.homeAttemptAtMs > 6000L) {
                    this.homeTeleAnchor = worldPoint;
                    this.homeAttemptAtMs = l;
                    if (this.homeAttempts < 3) {
                        ++this.homeAttempts;
                        this.status = "Resupply: casting home teleport (" + this.homeAttempts + ")";
                        this.input.primedUiOp(218, 6, () -> {
                            MousePackets.queueClickPacket();
                            WidgetPackets.queueWidgetAction1Packet(14286854, -1, -1);
                        });
                        return 4000;
                    }
                    this.status = "Resupply: ::home fallback";
                    this.input.command("::home");
                    return 8000;
                }
                this.status = "Resupply: waiting for teleport...";
                return 600;
            }
            case REJUVENATE: {
                boolean bl;
                if (l2 > 15000L) {
                    this.enterResupplyState(ShamanResupplyState.WALK_TO_BANK);
                    return 300;
                }
                if (this.rejuvenateClicked) {
                    this.enterResupplyState(ShamanResupplyState.WALK_TO_BANK);
                    return 600;
                }
                boolean bl2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS) >= this.client.getRealSkillLevel(Skill.HITPOINTS);
                boolean bl3 = bl = this.client.getBoostedSkillLevel(Skill.PRAYER) >= this.client.getRealSkillLevel(Skill.PRAYER);
                if (bl2 && bl) {
                    this.enterResupplyState(ShamanResupplyState.WALK_TO_BANK);
                    return 300;
                }
                TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 42822);
                if (tileObject2 != null) {
                    this.status = "Resupply: rejuvenating at the geyser";
                    this.input.interactObjectVia(tileObject2, "Rejuvenate");
                    this.rejuvenateClicked = true;
                    return 4000;
                }
                this.status = "Resupply: looking for the Enchanted Geyser...";
                return 600;
            }
            case WALK_TO_BANK: {
                if (l2 > 60000L) {
                    return this.routeFail("couldn't reach the bank");
                }
                if (worldPoint.distanceTo(LizardmanShamanConstants.BANK_TILE) > 4) {
                    this.status = "Resupply: walking to bank";
                    this.input.walkToward(worldPoint, LizardmanShamanConstants.BANK_TILE);
                    return 1200;
                }
                TileObject tileObject3 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 10083);
                if (tileObject3 != null) {
                    this.enterResupplyState(ShamanResupplyState.BANK_PRESET);
                    this.status = "Resupply: loading Last-preset";
                    this.input.interactObjectVia(tileObject3, "Last-preset");
                    this.presetAttempts = 1;
                    this.presetClickAtMs = l;
                    return 3000;
                }
                this.status = "Resupply: bank booth not found";
                return 600;
            }
            case BANK_PRESET: {
                if (this.lockedPresetSeen) {
                    return this.hardStuck("Bank preset is LOCKED (\"You may not load locked presets\") \u2014 unlock the preset in the bank UI, then re-enable the plugin.");
                }
                if (this.presetLoaded()) {
                    this.enterResupplyState(ShamanResupplyState.GLIDER);
                    return 600;
                }
                if (l2 > 30000L) {
                    return this.routeFail("Last-preset never loaded supplies");
                }
                if (l - this.presetClickAtMs > 6000L) {
                    if (this.presetAttempts >= 3) {
                        return this.routeFail("Last-preset failed after 3 attempts");
                    }
                    TileObject tileObject4 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 10083);
                    if (tileObject4 != null) {
                        ++this.presetAttempts;
                        this.presetClickAtMs = l;
                        this.status = "Resupply: retrying Last-preset (" + this.presetAttempts + ")";
                        this.input.interactObjectVia(tileObject4, "Last-preset");
                        return 3000;
                    }
                }
                this.status = "Resupply: waiting for preset...";
                return 600;
            }
            case GLIDER: {
                if (l2 > 90000L) {
                    return this.routeFail("glider travel timed out");
                }
                GliderTeleporter.Result result = this.glider.step(this.gliderTerm(), player);
                this.status = "Resupply: " + this.glider.status();
                if (result == GliderTeleporter.Result.ARRIVED) {
                    this.enterResupplyState(ShamanResupplyState.WALK_TO_AREA);
                    return 600;
                }
                return this.glider.delayMs();
            }
            case WALK_TO_AREA: {
                if (l2 > 150000L) {
                    return this.routeFail("couldn't reach the shaman area");
                }
                if (worldPoint.distanceTo(LizardmanShamanConstants.SHAMAN_AREA_TILE) > 8) {
                    this.status = "Resupply: walking to shaman area";
                    this.input.walkToward(worldPoint, LizardmanShamanConstants.SHAMAN_AREA_TILE);
                    return 1200;
                }
                this.status = "Resupply complete \u2014 hunting shamans";
                this.finishResupply();
                return 600;
            }
        }
        return 600;
    }

    private void finishResupply() {
        this.resupplyState = null;
        this.phase = ShamanPhase.IDLE;
        this.killLocation = null;
        this.targetShaman = null;
        this.attackStartMs = -1L;
        this.killsSinceResupply = 0;
        this.idleSinceMs = -1L;
        this.lootingEntryTick = -1;
    }

    private int routeFail(String string) {
        ++this.routeAttempts;
        if (this.routeAttempts >= 3) {
            return this.hardStuck("Resupply route failed " + this.routeAttempts + "x \u2014 last error: " + string);
        }
        this.status = "Route retry " + this.routeAttempts + "/3: " + string;
        this.enterResupplyState(ShamanResupplyState.GO_HOME);
        return 1200;
    }

    private int hardStuck(String string) {
        this.phase = ShamanPhase.STUCK;
        this.resupplyState = null;
        this.prayersOff();
        this.status = "STUCK \u2014 " + string;
        Player player = this.client.getLocalPlayer();
        this.postAlert("Shaman bot STUCK", string + (String)(player != null ? "\nPosition: " + LizardmanShamanPlugin.describe(player.getWorldLocation()) : "") + "\nKills: " + this.killCount + " \u00b7 Deaths: " + this.deathCount + "\nRe-enable the plugin after fixing the cause.", 15105570);
        return 5000;
    }

    private boolean atHome(Player player) {
        if (player.getWorldLocation().distanceTo(LizardmanShamanConstants.BANK_TILE) <= 30) {
            return true;
        }
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 42822) != null;
    }

    private boolean presetLoaded() {
        if (!this.config.foodName().trim().isEmpty()) {
            return this.findUnnoted(this.config.foodName()) != null;
        }
        if (!this.config.prayerPotionName().trim().isEmpty()) {
            return this.findUnnoted(this.config.prayerPotionName()) != null;
        }
        if (!this.config.requiredItems().trim().isEmpty()) {
            return this.hasRequiredItems();
        }
        return this.presetAttempts > 0;
    }

    private String gliderTerm() {
        String string = this.config.gliderDestination() == null ? "" : this.config.gliderDestination().trim();
        return string.isEmpty() ? "Lizardman" : string;
    }

    private boolean hasRequiredItems() {
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
            int n2 = Inventory.getAll(item -> item != null && item.getId() != -1 && this.itemName((Item)item).toLowerCase().contains(string3)).stream().mapToInt(Item::getQuantity).sum();
            if (n2 >= n) continue;
            return false;
        }
        return true;
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

    private void postAlert(String string, String string2, int n) {
        if (!this.config.discordAlerts() || !AdminWebhook.isConfigured()) {
            return;
        }
        String string3 = this.localName();
        String string4 = string2 + (String)(string3.isEmpty() ? "" : "\nAccount: " + string3);
        String string5 = "{\"username\":\"x64 Lizard Shaman\",\"embeds\":[{\"title\":" + LizardmanShamanPlugin.jsonStr(string) + ",\"color\":" + n + ",\"description\":" + LizardmanShamanPlugin.jsonStr(string4) + "}]}";
        try {
            if (this.alertExec == null) {
                this.alertExec = Executors.newSingleThreadExecutor(runnable -> {
                    Thread thread = new Thread(runnable, "orion-shaman-alert");
                    thread.setDaemon(true);
                    return thread;
                });
            }
            this.alertExec.submit(() -> {
                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection)new URL("https://discord.com/api/webhooks/1519481583453602033/0lI0dIUTo5q-ByNjPg1FJOycZ0TA1YU3M4DOfHuiLdfNCOxJ1oYlWSzX580e-3QSmipL").openConnection();
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setDoOutput(true);
                    try (OutputStream outputStream = httpURLConnection.getOutputStream();){
                        outputStream.write(string5.getBytes(StandardCharsets.UTF_8));
                    }
                    httpURLConnection.getResponseCode();
                    httpURLConnection.disconnect();
                }
                catch (Throwable throwable) {
                    System.err.println("[LizardShaman] webhook failed: " + throwable.getMessage());
                }
            });
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static String jsonStr(String string) {
        StringBuilder stringBuilder = new StringBuilder("\"");
        if (string != null) {
            block7: for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                switch (c) {
                    case '\"': {
                        stringBuilder.append("\\\"");
                        continue block7;
                    }
                    case '\\': {
                        stringBuilder.append("\\\\");
                        continue block7;
                    }
                    case '\n': {
                        stringBuilder.append("\\n");
                        continue block7;
                    }
                    case '\r': {
                        stringBuilder.append("\\r");
                        continue block7;
                    }
                    case '\t': {
                        stringBuilder.append("\\t");
                        continue block7;
                    }
                    default: {
                        if (c < ' ') {
                            stringBuilder.append(String.format("\\u%04x", c));
                            continue block7;
                        }
                        stringBuilder.append(c);
                    }
                }
            }
        }
        return stringBuilder.append('\"').toString();
    }

    private String localName() {
        try {
            Player player = this.client.getLocalPlayer();
            if (player != null && player.getName() != null) {
                return player.getName().replaceAll("<[^>]*>", "").trim();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "";
    }

    private static String describe(WorldPoint worldPoint) {
        return worldPoint == null ? "?" : "(" + worldPoint.getX() + ", " + worldPoint.getY() + ", " + worldPoint.getPlane() + ")";
    }

    private Item findUnnoted(String string) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        String string2 = string.trim().toLowerCase();
        return Inventory.getFirst(item -> item != null && item.getId() != -1 && !this.isNoted((Item)item) && this.itemName((Item)item).toLowerCase().contains(string2));
    }

    private boolean isNoted(Item item) {
        if (item == null || item.getId() == -1) {
            return false;
        }
        try {
            ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
            return itemComposition != null && itemComposition.getNote() > 0;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private int countByName(String string) {
        String string2 = string.toLowerCase();
        return Inventory.getAll(item -> item != null && item.getId() != -1 && !this.isNoted((Item)item) && this.itemName((Item)item).toLowerCase().contains(string2)).stream().mapToInt(Item::getQuantity).sum();
    }

    private String itemName(Item item) {
        try {
            String string = item.getName();
            return string == null ? "" : string;
        }
        catch (Throwable throwable) {
            return "";
        }
    }

    private String defName(TileItem tileItem) {
        try {
            ItemComposition itemComposition = this.client.getItemDefinition(tileItem.getId());
            String string = itemComposition == null ? null : itemComposition.getName();
            return string == null ? "" : string;
        }
        catch (Throwable throwable) {
            return "";
        }
    }

    private int haValue(int n) {
        try {
            ItemComposition itemComposition = this.client.getItemDefinition(n);
            return itemComposition == null ? 0 : itemComposition.getHaPrice();
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private static boolean matchesAny(String string, String string2) {
        if (string2 == null || string2.trim().isEmpty()) {
            return false;
        }
        for (String string3 : string2.split(",")) {
            String string4 = string3.trim().toLowerCase();
            if (string4.isEmpty() || !string.contains(string4)) continue;
            return true;
        }
        return false;
    }

    private boolean isWalkable(WorldPoint worldPoint) {
        int n = worldPoint.getX() - this.client.getBaseX();
        int n2 = worldPoint.getY() - this.client.getBaseY();
        if (n < 1 || n2 < 1 || n >= 103 || n2 >= 103) {
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

    private boolean isShamanId(int n) {
        return n == 6766 || n == 6767 || n == 7744 || n == 7745 || n == 8565;
    }

    private boolean isSpawnId(int n) {
        return n == 6768 || n == 7575;
    }

    private void resetState() {
        this.phase = ShamanPhase.IDLE;
        this.status = "Idle";
        this.killCount = 0;
        this.deathCount = 0;
        this.alchProfit = 0L;
        this.targetShaman = null;
        this.attackStartMs = -1L;
        this.lastAttackClickMs = 0L;
        this.jumpingShaman = null;
        this.jumpLandingTile = null;
        this.dodgeEscapeTile = null;
        this.jumpStartedMs = -1L;
        this.believedOverhead = null;
        this.believedOffensive = null;
        this.lastOverheadToggleTick = -1;
        this.lastOffensiveToggleTick = -1;
        this.beliefsInvalid = false;
        this.lastCombatMs = 0L;
        this.killLocation = null;
        this.droppedFoodName = null;
        this.droppedFoodAtMs = 0L;
        this.resupplyState = null;
        this.routeAttempts = 0;
        this.lootingEntryTick = -1;
        this.homeAttempts = 0;
        this.homeAttemptAtMs = 0L;
        this.homeTeleAnchor = null;
        this.rejuvenateClicked = false;
        this.presetAttempts = 0;
        this.presetClickAtMs = 0L;
        this.killsSinceResupply = 0;
        this.playerDied = false;
        this.lockedPresetSeen = false;
        this.busyNpcAtMs = 0L;
        this.lastBusyHandledMs = 0L;
        this.nextBreakAtMs = 0L;
        this.breakUntilMs = 0L;
        this.breaksTaken = 0;
        this.emergencyActive = false;
        this.idleSinceMs = -1L;
        this.idleAlerted = false;
        this.loggedInLastTick = false;
        this.stopIssued = false;
        this.npcBlacklistUntil.clear();
        this.shamanAnimMap.clear();
    }

    public ShamanPhase getPhase() {
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

    public long getAlchProfit() {
        return this.alchProfit;
    }

    public int getBreaksTaken() {
        return this.breaksTaken;
    }

    public long getBreakUntilMs() {
        return this.breakUntilMs;
    }

    public long getNextBreakAtMs() {
        return this.nextBreakAtMs;
    }

    public ShamanResupplyState getResupplyState() {
        return this.resupplyState;
    }

    public HumanInput getInput() {
        return this.input;
    }

    private /* synthetic */ boolean lambda$handleResupply$0(WorldPoint worldPoint, String string, TileItem tileItem) {
        if (tileItem == null) {
            return false;
        }
        if (this.killLocation != null ? tileItem.getWorldLocation().distanceTo(this.killLocation) > 8 : tileItem.getWorldLocation().distanceTo(worldPoint) > this.config.lootRadius()) {
            return false;
        }
        String string2 = this.defName(tileItem).toLowerCase();
        if (LizardmanShamanPlugin.matchesAny(string2, string)) {
            return true;
        }
        if (this.config.lootMinValue() > 0 && this.haValue(tileItem.getId()) >= this.config.lootMinValue()) {
            return true;
        }
        return string.trim().isEmpty();
    }
}

