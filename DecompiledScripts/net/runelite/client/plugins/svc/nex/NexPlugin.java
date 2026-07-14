/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.CollisionData
 *  net.runelite.api.EquipmentInventorySlot
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
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.OverheadTextChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.nex;

import com.google.inject.Binder;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.EquipmentInventorySlot;
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
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.nex.NexConfig;
import net.runelite.client.plugins.svc.nex.NexOverheadPrayer;
import net.runelite.client.plugins.svc.nex.NexOverlay;
import net.runelite.client.plugins.svc.nex.NexPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Nex</font></nobr></html>", description="Full Nex boss fight: phase detection, prayer switching, minions, mechanics, brews/restores and looting", tags={"orion", "nex", "gwd", "ancient prison", "boss", "combat", "bot"}, enabledByDefault=false)
public class NexPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private NexConfig config;
    private NexOverlay overlay;
    private NexPhase phase = NexPhase.IDLE;
    private String status = "Idle";
    private int killCount = 0;
    private long startTime = 0L;
    private boolean loggedInLastTick = false;
    private boolean mustKillMinion = false;
    private int activeMinionId = -1;
    private int phaseStartTick = -1;
    private Prayer lastSentOverhead = null;
    private long lastSentOverheadMs = 0L;
    private long lastSentOffensiveMs = 0L;
    private int siphonUntilTick = -1;
    private int embraceUntilTick = -1;
    private int sacrificeUntilTick = -1;
    private int icePrisonUntilTick = -1;
    private int containmentUntilTick = -1;
    private int smokeDashUntilTick = -1;
    private int wrathUntilTick = -1;
    private int brewDosesSinceRestore = 0;
    private long lastDivineMs = 0L;
    private WorldPoint killLocation = null;
    private int lootEntryTick = -1;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(NexConfig.class).toInstance((Object)((NexConfig)configManager.getConfig(NexConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new NexOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
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
            this.phase = NexPhase.IDLE;
        } else if (gameState == GameState.LOADING && this.isCombatPhase()) {
            this.resetState();
            this.status = "Left lair / died \u2014 reset";
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.PUBLICCHAT || chatMessage.getType() == ChatMessageType.GAMEMESSAGE || chatMessage.getType() == ChatMessageType.SPAM) {
            this.handleShout(NexPlugin.clean(chatMessage.getMessage()));
        }
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged overheadTextChanged) {
        Actor actor = overheadTextChanged.getActor();
        if (actor != null && "Nex".equalsIgnoreCase(actor.getName())) {
            this.handleShout(NexPlugin.clean(overheadTextChanged.getOverheadText()));
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (this.client.getGameState() == GameState.LOGGED_IN && this.client.getBoostedSkillLevel(Skill.HITPOINTS) == 0 && this.isCombatPhase()) {
            this.resetState();
            this.status = "Died \u2014 reset";
        }
    }

    private void handleShout(String string) {
        if (string == null || string.isEmpty()) {
            return;
        }
        int n = this.client.getTickCount();
        if (string.contains("soul with smoke")) {
            this.setPhase(NexPhase.SMOKE, n);
            return;
        }
        if (string.contains("darken my shadow")) {
            this.setPhase(NexPhase.SHADOW, n);
            return;
        }
        if (string.contains("lungs with blood")) {
            this.setPhase(NexPhase.BLOOD, n);
            return;
        }
        if (string.contains("power of ice")) {
            this.setPhase(NexPhase.ICE, n);
            return;
        }
        if (string.contains("power of zaros")) {
            this.setPhase(NexPhase.ZAROS, n);
            return;
        }
        if (string.contains("fail me")) {
            this.activeMinionId = NexPlugin.minionForPhase(this.phase);
            if (this.activeMinionId > 0) {
                this.mustKillMinion = true;
            }
            return;
        }
        if (this.config.doBloodSacrificeRun() && (string.contains("marked you") || string.contains("blood sacrifice"))) {
            this.sacrificeUntilTick = n + 7;
            return;
        }
        if (this.config.stopDpsOnSiphon() && string.contains("siphon")) {
            this.siphonUntilTick = n + 9;
            return;
        }
        if (this.config.handleEmbraceDarkness() && string.contains("embrace darkness")) {
            this.embraceUntilTick = n + 22;
            return;
        }
        if (this.config.dodgeContainment() && string.contains("contain this")) {
            this.containmentUntilTick = n + 6;
            return;
        }
        if (this.config.breakIcePrison() && string.contains("prison of ice")) {
            this.icePrisonUntilTick = n + 7;
            return;
        }
        if (this.config.dodgeSmokeDash() && string.contains("no escape")) {
            this.smokeDashUntilTick = n + 3;
            return;
        }
    }

    private void setPhase(NexPhase nexPhase, int n) {
        if (this.phase != nexPhase) {
            this.phase = nexPhase;
            this.phaseStartTick = n;
            this.mustKillMinion = false;
            this.activeMinionId = -1;
            this.clearMechanicWindows();
        }
    }

    private void clearMechanicWindows() {
        this.siphonUntilTick = -1;
        this.embraceUntilTick = -1;
        this.sacrificeUntilTick = -1;
        this.icePrisonUntilTick = -1;
        this.containmentUntilTick = -1;
        this.smokeDashUntilTick = -1;
    }

    private static int minionForPhase(NexPhase nexPhase) {
        switch (nexPhase) {
            case SMOKE: {
                return 11283;
            }
            case SHADOW: {
                return 11284;
            }
            case BLOOD: {
                return 11285;
            }
            case ICE: {
                return 11286;
            }
        }
        return -1;
    }

    @Override
    protected int loop() {
        if (this.client.getGameState() == GameState.LOADING) {
            return 1000;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
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
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && "Nex".equalsIgnoreCase(nPC.getName()) && !nPC.isDead());
        if (this.phase == NexPhase.LOOTING) {
            return this.handleLoot(player);
        }
        if (this.phase == NexPhase.DONE) {
            this.status = "Done \u2014 target kills reached";
            return -1;
        }
        if (nPC2 == null) {
            if (this.phase == NexPhase.ZAROS) {
                ++this.killCount;
                this.killLocation = player.getWorldLocation();
                this.lootEntryTick = -1;
                if (this.config.dodgeWrath()) {
                    this.wrathUntilTick = this.client.getTickCount() + 5;
                }
                this.phase = NexPhase.LOOTING;
                this.status = "Nex defeated! (" + this.killCount + ")";
                return 100;
            }
            if (this.isCombatPhase()) {
                this.status = "Nex not visible \u2014 holding";
                return 600;
            }
            this.phase = NexPhase.IDLE;
            this.status = "Waiting for Nex";
            return 600;
        }
        if (this.phase == NexPhase.IDLE) {
            this.phase = NexPhase.SMOKE;
            this.phaseStartTick = this.client.getTickCount();
            this.status = "In fight \u2014 phase unknown, camping Magic";
        }
        this.maintainPrayer();
        int n = this.handleMechanics(player, nPC2);
        if (n > 0) {
            return n;
        }
        int n2 = this.handleSupplies(player);
        if (n2 != 0) {
            return n2;
        }
        return this.handleCombat(player, nPC2);
    }

    private void maintainPrayer() {
        Prayer[] prayerArray;
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        if (!this.isCombatPhase()) {
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
            return;
        }
        long l = System.currentTimeMillis();
        Prayer prayer = null;
        if (this.config.autoPrayerSwitch()) {
            prayer = this.phaseOverhead();
        }
        if (!(prayer == null || this.client.isPrayerActive(prayer) || prayer.equals((Object)this.lastSentOverhead) && l - this.lastSentOverheadMs <= 700L)) {
            prayerArray = new Prayer[]{Prayer.PROTECT_FROM_MAGIC, Prayer.PROTECT_FROM_MISSILES, Prayer.PROTECT_FROM_MELEE};
            int n = prayerArray.length;
            for (int i = 0; i < n; ++i) {
                Prayer prayer2 = prayerArray[i];
                if (prayer2 == prayer || !this.client.isPrayerActive(prayer2)) continue;
                Prayers.toggle(prayer2);
            }
            Prayers.toggle(prayer);
            this.lastSentOverhead = prayer;
            this.lastSentOverheadMs = l;
        }
        Prayer[] prayerArray2 = prayerArray = this.config.offensivePrayer() != null ? this.config.offensivePrayer().prayer : null;
        if (prayerArray != null && !this.client.isPrayerActive((Prayer)prayerArray) && l - this.lastSentOffensiveMs > 700L) {
            Prayers.toggle((Prayer)prayerArray);
            this.lastSentOffensiveMs = l;
        }
    }

    private Prayer phaseOverhead() {
        NexOverheadPrayer nexOverheadPrayer;
        switch (this.phase) {
            case SHADOW: {
                nexOverheadPrayer = this.config.shadowPrayer();
                break;
            }
            case BLOOD: {
                nexOverheadPrayer = this.config.bloodPrayer();
                break;
            }
            case ICE: {
                nexOverheadPrayer = this.config.icePrayer();
                break;
            }
            case ZAROS: {
                nexOverheadPrayer = this.config.zarosPrayer();
                break;
            }
            default: {
                nexOverheadPrayer = this.config.smokePrayer();
            }
        }
        return nexOverheadPrayer != null ? nexOverheadPrayer.prayer : null;
    }

    private int handleMechanics(Player player, NPC nPC) {
        WorldPoint worldPoint;
        WorldPoint worldPoint2;
        int n;
        int n2 = this.client.getTickCount();
        WorldPoint worldPoint3 = player.getWorldLocation();
        WorldPoint worldPoint4 = nPC.getWorldLocation();
        if (n2 <= this.sacrificeUntilTick) {
            this.status = "Blood sacrifice \u2014 running!";
            this.runFrom(worldPoint3, worldPoint4, this.config.sacrificeRunTiles());
            return 300;
        }
        if (this.config.breakIcePrison() && n2 <= this.icePrisonUntilTick && this.stepOffOrAwayFromObject(player, 42944, worldPoint4)) {
            this.status = "Ice prison \u2014 breaking out!";
            return 300;
        }
        if (this.config.dodgeContainment() && n2 <= this.containmentUntilTick && (n = this.chebyshevToNpc(worldPoint3, nPC)) <= 2) {
            this.status = "Containment \u2014 clearing icicle field";
            this.runFrom(worldPoint3, worldPoint4, 4);
            return 300;
        }
        if (this.config.dodgeShadowSmash() && this.phase == NexPhase.SHADOW && this.standingOnObject(worldPoint3, 42942) && (worldPoint2 = this.findSafeAdjacent(worldPoint3, 42942)) != null) {
            this.status = "Shadow smash \u2014 dodging";
            Movement.walkTo(worldPoint2);
            return 300;
        }
        if (this.config.dodgeSmokeDash() && n2 <= this.smokeDashUntilTick && this.phase == NexPhase.SMOKE && (worldPoint = this.sidestep(worldPoint3, worldPoint4)) != null) {
            this.status = "Smoke dash \u2014 sidestepping";
            Movement.walkTo(worldPoint);
            return 300;
        }
        if (this.config.handleEmbraceDarkness() && n2 <= this.embraceUntilTick && (n = this.chebyshevToNpc(worldPoint3, nPC)) < this.config.embraceDistance()) {
            this.status = "Embrace darkness \u2014 keeping distance";
            this.runFrom(worldPoint3, worldPoint4, this.config.embraceDistance());
            return 300;
        }
        return 0;
    }

    private int handleSupplies(Player player) {
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n4 = this.client.getRealSkillLevel(Skill.PRAYER);
        int n5 = n2 > 0 ? n * 100 / n2 : 100;
        int n6 = n4 > 0 ? n3 * 100 / n4 : 100;
        boolean bl = Inventory.contains(item -> this.matches((Item)item, this.config.brewName()));
        boolean bl2 = Inventory.contains(item -> this.matches((Item)item, this.config.restorePotion()));
        if (this.config.stopWhenOutOfSupplies() && !bl && !bl2) {
            this.status = "Out of brews & restores \u2014 stopping";
            return -1;
        }
        if (n5 < this.config.emergencyHpPct()) {
            boolean bl3 = false;
            if (this.drink(this.config.brewName())) {
                ++this.brewDosesSinceRestore;
                bl3 = true;
            }
            if (!this.config.hardFood().trim().isEmpty() && this.eat(this.config.hardFood())) {
                bl3 = true;
            }
            if (bl3) {
                this.status = "Emergency heal";
                return 1000;
            }
        }
        if (n5 < this.config.brewHpPct() && bl && this.drink(this.config.brewName())) {
            ++this.brewDosesSinceRestore;
            this.status = "Sipping brew (" + n5 + "% HP)";
            return 900;
        }
        if (bl2 && (n6 < this.config.prayerPct() || this.brewDosesSinceRestore >= this.config.brewsPerRestore()) && this.drink(this.config.restorePotion())) {
            this.brewDosesSinceRestore = 0;
            this.status = "Sipping restore (" + n6 + "% pray)";
            return 900;
        }
        if (this.config.useStamina() && this.client.getEnergy() < this.config.staminaPct() * 100 && this.drink(this.config.staminaName())) {
            this.status = "Sipping stamina";
            return 900;
        }
        if (this.config.useDivineRanging()) {
            long l = System.currentTimeMillis();
            long l2 = (long)this.config.divineRedoseMins() * 60000L;
            if (this.lastDivineMs == 0L || l - this.lastDivineMs > l2) {
                if (this.drink(this.config.divineName())) {
                    this.lastDivineMs = l;
                    this.status = "Sipping divine potion";
                    return 900;
                }
                if (this.lastDivineMs == 0L) {
                    this.lastDivineMs = l;
                }
            }
        }
        return 0;
    }

    private int handleCombat(Player player, NPC nPC2) {
        int n;
        NPC nPC3;
        int n2 = this.client.getTickCount();
        if (this.mustKillMinion && this.activeMinionId > 0) {
            nPC3 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == this.activeMinionId && !nPC.isDead());
            if (nPC3 == null) {
                this.mustKillMinion = false;
            } else {
                if (!this.config.sameWeaponForMinions() && !this.config.minionWeapon().trim().isEmpty() && this.equipIfNeeded(this.config.minionWeapon())) {
                    return 600;
                }
                this.status = "Killing minion (" + nPC3.getName() + ")";
                this.attack(player, nPC3);
                return 300;
            }
        }
        if (n2 <= this.siphonUntilTick) {
            if (this.config.attackReavers() && (nPC3 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && (nPC.getId() == 11293 || nPC.getId() == 11294))) != null) {
                this.status = "Siphon \u2014 killing reaver";
                this.attack(player, nPC3);
                return 300;
            }
            this.status = "Blood siphon \u2014 holding DPS";
            return 300;
        }
        if (this.phase == NexPhase.SHADOW && (n = this.chebyshevToNpc(player.getWorldLocation(), nPC2)) < this.config.shadowPhaseDistance()) {
            this.status = "Shadow \u2014 keeping distance";
            this.runFrom(player.getWorldLocation(), nPC2.getWorldLocation(), this.config.shadowPhaseDistance());
            return 300;
        }
        if (!this.inAttackRange(player, nPC2)) {
            this.status = "Repositioning to attack";
            int n3 = Math.max(2, this.config.attackRange() - 2);
            Movement.walkTo(this.standoffTile(player.getWorldLocation(), nPC2, n3));
            return 300;
        }
        this.status = "Attacking Nex [" + String.valueOf((Object)this.phase) + "]";
        this.attack(player, nPC2);
        return 300;
    }

    private void attack(Player player, NPC nPC) {
        if (nPC == null || nPC.isDead()) {
            return;
        }
        Actor actor = player.getInteracting();
        if (actor instanceof NPC && ((NPC)actor).getIndex() == nPC.getIndex()) {
            return;
        }
        nPC.interact("Attack");
    }

    private int handleLoot(Player player) {
        int n = this.client.getTickCount();
        if (n <= this.wrathUntilTick && this.killLocation != null) {
            this.status = "Death wrath \u2014 running clear";
            this.runFrom(player.getWorldLocation(), this.killLocation, this.config.wrathTiles());
            return 300;
        }
        if (this.config.skipLooting()) {
            return this.finishLoot();
        }
        if (this.lootEntryTick < 0) {
            this.lootEntryTick = n;
        }
        if (n - this.lootEntryTick < 3) {
            this.status = "Waiting for drops...";
            return 600;
        }
        if (Inventory.isFull()) {
            if (!this.config.hardFood().trim().isEmpty() && this.eat(this.config.hardFood())) {
                this.status = "Looting: ate to free a slot";
                return 1000;
            }
            return this.finishLoot();
        }
        TileItem tileItem = this.findLootItem();
        if (tileItem != null) {
            this.status = "Looting: " + tileItem.getName();
            tileItem.interact("Take");
            return 600;
        }
        return this.finishLoot();
    }

    private int finishLoot() {
        this.killLocation = null;
        this.lootEntryTick = -1;
        this.mustKillMinion = false;
        this.activeMinionId = -1;
        this.clearMechanicWindows();
        if (this.config.targetKillCount() > 0 && this.killCount >= this.config.targetKillCount()) {
            this.phase = NexPhase.DONE;
            this.status = "Target kills reached \u2014 stopping";
            return -1;
        }
        this.phase = NexPhase.IDLE;
        this.status = "Looted \u2014 waiting for Nex";
        return 600;
    }

    private TileItem findLootItem() {
        String string = this.config.lootItems().trim().toLowerCase();
        WorldPoint worldPoint = this.killLocation;
        int n = this.config.lootRadius();
        if (string.isEmpty()) {
            return TileItems.getNearest(tileItem -> tileItem != null && (worldPoint == null || tileItem.getWorldLocation().distanceTo(worldPoint) <= n));
        }
        String[] stringArray = string.split(",");
        return TileItems.getNearest(tileItem -> {
            if (tileItem == null || tileItem.getName() == null) {
                return false;
            }
            if (worldPoint != null && tileItem.getWorldLocation().distanceTo(worldPoint) > n) {
                return false;
            }
            String string = tileItem.getName().toLowerCase();
            for (String string2 : stringArray) {
                String string3 = string2.trim();
                if (string3.isEmpty() || !string.contains(string3)) continue;
                return true;
            }
            return false;
        });
    }

    private void runFrom(WorldPoint worldPoint, WorldPoint worldPoint2, int n) {
        int n2;
        int n3;
        int n4;
        int n5 = worldPoint.getX() - worldPoint2.getX();
        double d = Math.sqrt((double)n5 * (double)n5 + (double)(n4 = worldPoint.getY() - worldPoint2.getY()) * (double)n4);
        if (d < 0.5) {
            n3 = 0;
            n2 = n;
        } else {
            n3 = (int)Math.round((double)n5 / d * (double)n);
            n2 = (int)Math.round((double)n4 / d * (double)n);
        }
        WorldPoint worldPoint3 = new WorldPoint(worldPoint.getX() + n3, worldPoint.getY() + n2, worldPoint.getPlane());
        if (!this.isWalkable(worldPoint3)) {
            int[][] nArrayArray;
            for (int[] nArray : nArrayArray = new int[][]{{n, 0}, {-n, 0}, {0, n}, {0, -n}, {n, n}, {-n, -n}}) {
                WorldPoint worldPoint4 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
                if (!this.isWalkable(worldPoint4)) continue;
                worldPoint3 = worldPoint4;
                break;
            }
        }
        Movement.walkTo(worldPoint3);
    }

    private WorldPoint sidestep(WorldPoint worldPoint, WorldPoint worldPoint2) {
        int[][] nArrayArray;
        int n = worldPoint.getX() - worldPoint2.getX();
        int n2 = worldPoint.getY() - worldPoint2.getY();
        for (int[] nArray : nArrayArray = new int[][]{{-NexPlugin.sign(n2) * 2, NexPlugin.sign(n) * 2}, {NexPlugin.sign(n2) * 2, -NexPlugin.sign(n) * 2}, {2, 0}, {-2, 0}}) {
            WorldPoint worldPoint3 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (nArray[0] == 0 && nArray[1] == 0 || !this.isWalkable(worldPoint3)) continue;
            return worldPoint3;
        }
        return null;
    }

    private static int sign(int n) {
        return Integer.compare(n, 0);
    }

    private boolean standingOnObject(WorldPoint worldPoint, int n) {
        if (n <= 0) {
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
        return groundObject != null && groundObject.getId() == n;
    }

    private WorldPoint findSafeAdjacent(WorldPoint worldPoint, int n) {
        int[][] nArrayArray;
        for (int[] nArray : nArrayArray = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
            WorldPoint worldPoint2 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (!this.isWalkable(worldPoint2) || this.standingOnObject(worldPoint2, n)) continue;
            return worldPoint2;
        }
        return null;
    }

    private boolean stepOffOrAwayFromObject(Player player, int n, WorldPoint worldPoint) {
        if (n <= 0) {
            return false;
        }
        WorldPoint worldPoint2 = player.getWorldLocation();
        boolean bl = false;
        block0: for (int i = -1; i <= 1 && !bl; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (!this.standingOnObject(new WorldPoint(worldPoint2.getX() + i, worldPoint2.getY() + j, worldPoint2.getPlane()), n)) continue;
                bl = true;
                continue block0;
            }
        }
        if (!bl) {
            return false;
        }
        WorldPoint worldPoint3 = this.findSafeAdjacent(worldPoint2, n);
        if (worldPoint3 != null) {
            Movement.walkTo(worldPoint3);
            return true;
        }
        this.runFrom(worldPoint2, worldPoint, 2);
        return true;
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

    private int chebyshevToNpc(WorldPoint worldPoint, NPC nPC) {
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        int n2 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)));
        int n3 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)));
        return Math.max(n2, n3);
    }

    private boolean inAttackRange(Player player, NPC nPC) {
        return this.chebyshevToNpc(player.getWorldLocation(), nPC) <= this.config.attackRange();
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
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            Item item = Equipment.fromSlot(equipmentInventorySlot);
            if (item == null || item.getName() == null || !item.getName().toLowerCase().contains(string.trim().toLowerCase())) continue;
            return true;
        }
        return false;
    }

    private boolean isCombatPhase() {
        return this.phase == NexPhase.SMOKE || this.phase == NexPhase.SHADOW || this.phase == NexPhase.BLOOD || this.phase == NexPhase.ICE || this.phase == NexPhase.ZAROS;
    }

    private static String clean(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("<[^>]*>", "").toLowerCase().trim();
    }

    private void resetState() {
        this.phase = NexPhase.IDLE;
        this.status = "Idle";
        this.mustKillMinion = false;
        this.activeMinionId = -1;
        this.phaseStartTick = -1;
        this.lastSentOverhead = null;
        this.lastSentOverheadMs = 0L;
        this.lastSentOffensiveMs = 0L;
        this.siphonUntilTick = -1;
        this.embraceUntilTick = -1;
        this.sacrificeUntilTick = -1;
        this.icePrisonUntilTick = -1;
        this.containmentUntilTick = -1;
        this.smokeDashUntilTick = -1;
        this.wrathUntilTick = -1;
        this.brewDosesSinceRestore = 0;
        this.killLocation = null;
        this.lootEntryTick = -1;
        this.loggedInLastTick = false;
    }

    NexPhase getPhase() {
        return this.phase;
    }

    String getStatus() {
        return this.status;
    }

    int getKillCount() {
        return this.killCount;
    }

    long getStartTime() {
        return this.startTime;
    }

    boolean isMustKillMinion() {
        return this.mustKillMinion;
    }

    int getBrewDoses() {
        return this.brewDosesSinceRestore;
    }

    String getCurrentOverheadName() {
        Prayer prayer = this.isCombatPhase() && this.config.autoPrayerSwitch() ? this.phaseOverhead() : null;
        return prayer == null ? "None" : prayer.name().replace("PROTECT_FROM_", "");
    }
}

