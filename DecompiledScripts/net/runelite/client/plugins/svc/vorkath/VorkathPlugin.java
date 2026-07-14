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
 *  net.runelite.api.Projectile
 *  net.runelite.api.Skill
 *  net.runelite.api.Tile
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
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
package net.runelite.client.plugins.svc.vorkath;

import com.google.inject.Binder;
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
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.vorkath.ResupplyState;
import net.runelite.client.plugins.svc.vorkath.VorkathConfig;
import net.runelite.client.plugins.svc.vorkath.VorkathOverlay;
import net.runelite.client.plugins.svc.vorkath.VorkathPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Vorkath</font></nobr></html>", description="Boss fight bot for Vorkath \u2014 handles all phases, prayers, potions, and zombie spawn", tags={"orion", "vorkath", "boss", "combat", "bot"}, enabledByDefault=false)
public class VorkathPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private VorkathConfig config;
    private VorkathOverlay overlay;
    private VorkathPhase phase = VorkathPhase.IDLE;
    private String status = "Idle";
    private int killCount = 0;
    private long startTime = 0L;
    private int attackCount = 0;
    private int lastVorkathAnim = -1;
    private WorldPoint lastAcidPos = null;
    private WorldPoint acidIntendedPos = null;
    private boolean runWasEnabled = false;
    private int acidPhaseStartTick = -1;
    private String debugLastResult = "\u2014";
    private boolean needsFireballDodge = false;
    private int fireballDodgedOnTick = -1;
    private WorldPoint fireballLandingTile = null;
    private WorldPoint fireballDodgeTile = null;
    private WorldPoint killLocation = null;
    private WorldPoint vorkathSpawnPoint = null;
    private long lastAntifireDrinkTime = 0L;
    private long lastAntivenomDrinkTime = 0L;
    private boolean loggedInLastTick = false;
    private ResupplyState resupplyState = null;
    private boolean playerDied = false;
    private int lootingEntryTick = -1;
    private int lastAttackAnimTick = -1;
    private static final WorldPoint BANK_TILE = new WorldPoint(2796, 3391, 0);
    private static final WorldPoint GLIDER_TILE = new WorldPoint(2796, 3375, 0);
    private static final WorldPoint ENTRANCE_TILE = new WorldPoint(2272, 4051, 0);

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(VorkathConfig.class).toInstance((Object)((VorkathConfig)configManager.getConfig(VorkathConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new VorkathOverlay(this.client, this, this.config);
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
            this.phase = VorkathPhase.IDLE;
        } else if (gameState == GameState.LOADING && (this.phase == VorkathPhase.FIGHTING || this.phase == VorkathPhase.ACID_PHASE || this.phase == VorkathPhase.ZOMBIE_SPAWN || this.phase == VorkathPhase.WAKING || this.phase == VorkathPhase.DEAD)) {
            this.phase = VorkathPhase.RESUPPLYING;
            this.resupplyState = ResupplyState.LOOTING;
            this.playerDied = true;
            this.status = "Died \u2014 returning to Vorkath";
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved projectileMoved) {
        int n = 1481;
        if (n <= 0) {
            return;
        }
        if (projectileMoved.getProjectile().getId() != n) {
            return;
        }
        int n2 = this.client.getTickCount();
        if (this.fireballDodgedOnTick < 0 || n2 - this.fireballDodgedOnTick >= 6) {
            this.needsFireballDodge = true;
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        WorldPoint worldPoint;
        if (this.client.getGameState() == GameState.LOGGED_IN && this.client.getBoostedSkillLevel(Skill.HITPOINTS) == 0 && (this.phase == VorkathPhase.FIGHTING || this.phase == VorkathPhase.ACID_PHASE || this.phase == VorkathPhase.ZOMBIE_SPAWN || this.phase == VorkathPhase.WAKING || this.phase == VorkathPhase.DEAD)) {
            this.phase = VorkathPhase.RESUPPLYING;
            this.resupplyState = ResupplyState.LOOTING;
            this.playerDied = true;
            this.status = "Died \u2014 returning to Vorkath";
            return;
        }
        if (this.phase != VorkathPhase.ACID_PHASE) {
            return;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return;
        }
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (this.acidIntendedPos == null || worldPoint2.distanceTo(this.acidIntendedPos) > 3) {
            this.acidIntendedPos = worldPoint2;
            this.lastAcidPos = null;
        }
        if ((worldPoint = this.findNextSafeAcidTile(this.acidIntendedPos)) == null) {
            this.acidIntendedPos = worldPoint2;
            worldPoint = this.findNextSafeAcidTile(worldPoint2);
        }
        if (worldPoint != null) {
            Movement.walkTo(worldPoint);
            this.lastAcidPos = this.acidIntendedPos;
            this.acidIntendedPos = worldPoint;
        }
    }

    @Override
    protected int loop() {
        int n;
        int n2;
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
        NPC nPC2 = NPCs.getNearest(nPC -> nPC.getId() == 8061 && !nPC.isDead());
        NPC nPC3 = NPCs.getNearest(nPC -> nPC.getId() == 8062 && !nPC.isDead());
        this.updatePhase(nPC2, nPC3, player);
        if (nPC3 != null) {
            return this.handleZombieSpawn(nPC3);
        }
        if (this.needsFireballDodge) {
            n2 = 1481;
            n = 0;
            if (n2 > 0) {
                for (Projectile projectile : this.client.getProjectiles()) {
                    if (projectile.getId() != n2) continue;
                    n = 1;
                    break;
                }
            }
            if (n != 0) {
                return this.dodgeFireball(player, nPC2);
            }
            this.needsFireballDodge = false;
        }
        int n3 = n2 = this.fireballDodgedOnTick >= 0 ? this.client.getTickCount() - this.fireballDodgedOnTick : Integer.MAX_VALUE;
        if (n2 < 6) {
            boolean bl;
            n = 6 - n2;
            this.maintainPrayer();
            boolean bl2 = bl = this.fireballLandingTile != null && player.getWorldLocation().equals((Object)this.fireballLandingTile);
            if (bl && this.fireballDodgeTile != null) {
                this.status = "Dodging (resend) (" + n + " ticks)";
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(this.fireballDodgeTile);
            } else if (nPC2 != null && this.isInAttackRange(player, nPC2)) {
                this.status = "Dodged \u2014 attacking in place (" + n + " ticks)";
                nPC2.interact("Attack");
            } else {
                this.status = "Dodged \u2014 holding (" + n + " ticks)";
            }
            return 300;
        }
        if (this.phase == VorkathPhase.RESUPPLYING || this.phase == VorkathPhase.DEAD) {
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
        } else {
            this.maintainPrayer();
        }
        if (this.shouldEat(player)) {
            return this.eatFood();
        }
        if (this.shouldDrinkAntifire()) {
            return this.drinkAntifire();
        }
        if (this.shouldDrinkAntivenom()) {
            return this.drinkAntivenom();
        }
        if (this.shouldRestorePrayer()) {
            return this.drinkPrayerRestore();
        }
        if (this.phase == VorkathPhase.ACID_PHASE) {
            return this.handleAcidPhase(player, nPC2);
        }
        if (this.phase == VorkathPhase.WAKING && this.vorkathSpawnPoint != null) {
            NPC nPC4 = NPCs.getNearest(nPC -> nPC.getId() == 8059);
            if (nPC4 != null) {
                this.status = "Poking Vorkath (retry)...";
                nPC4.interact("Poke");
                return 600;
            }
            this.status = "Stepping back from Vorkath...";
            Movement.walkTo(this.calcDodgeTile(player.getWorldLocation(), this.vorkathSpawnPoint));
            return 600;
        }
        if (nPC2 != null && this.isInMeleeRange(player, nPC2)) {
            return this.stepBackFromMelee(player, nPC2);
        }
        if (nPC2 != null) {
            return this.attackVorkath(nPC2, player);
        }
        if (this.phase == VorkathPhase.DEAD && this.config.enableResupply()) {
            this.phase = VorkathPhase.RESUPPLYING;
            this.resupplyState = ResupplyState.LOOTING;
        }
        if (this.phase == VorkathPhase.RESUPPLYING) {
            return this.handleResupply(player);
        }
        NPC nPC5 = NPCs.getNearest(nPC -> nPC.getId() == 8059);
        if (nPC5 != null) {
            return this.wakeVorkath(nPC5);
        }
        this.status = "Idle \u2014 no Vorkath found";
        return 600;
    }

    private void updatePhase(NPC nPC, NPC nPC2, Player player) {
        if (nPC2 != null && !nPC2.isDead()) {
            if (this.phase != VorkathPhase.ZOMBIE_SPAWN) {
                this.phase = VorkathPhase.ZOMBIE_SPAWN;
            }
            return;
        }
        if (this.phase == VorkathPhase.ZOMBIE_SPAWN) {
            this.phase = VorkathPhase.FIGHTING;
            this.attackCount = 0;
        }
        if (nPC == null) {
            if (this.phase == VorkathPhase.FIGHTING || this.phase == VorkathPhase.ACID_PHASE) {
                ++this.killCount;
                this.killLocation = player.getWorldLocation();
                this.phase = VorkathPhase.DEAD;
                this.status = "Kill #" + this.killCount + " complete";
            }
            return;
        }
        boolean bl = this.acidPoolsExistNearby();
        if (bl && this.phase != VorkathPhase.ACID_PHASE) {
            this.phase = VorkathPhase.ACID_PHASE;
            this.acidPhaseStartTick = this.client.getTickCount();
            this.attackCount = 0;
            this.setupAcidWalkTiles(player);
        } else if (!bl && this.phase == VorkathPhase.ACID_PHASE) {
            this.phase = VorkathPhase.FIGHTING;
            this.attackCount = 0;
            if (this.runWasEnabled) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePauseWidgetPacket(10485782, -1);
                this.runWasEnabled = false;
            }
        }
        int n = nPC.getAnimation();
        if (n != -1 && n != this.lastVorkathAnim) {
            switch (n) {
                case 7950: {
                    this.phase = VorkathPhase.FIGHTING;
                    this.attackCount = 0;
                    break;
                }
                case 7952: {
                    this.lastAttackAnimTick = this.client.getTickCount();
                    if (this.phase == VorkathPhase.FIGHTING) {
                        ++this.attackCount;
                        break;
                    }
                    if (this.phase == VorkathPhase.ACID_PHASE) break;
                    this.phase = VorkathPhase.FIGHTING;
                    break;
                }
                case 7960: {
                    this.phase = VorkathPhase.ZOMBIE_SPAWN;
                    this.attackCount = 0;
                    break;
                }
                case 7957: {
                    if (this.phase == VorkathPhase.ACID_PHASE) break;
                    this.phase = VorkathPhase.ACID_PHASE;
                    this.acidPhaseStartTick = this.client.getTickCount();
                    this.attackCount = 0;
                    this.setupAcidWalkTiles(player);
                    break;
                }
                case 7949: {
                    ++this.killCount;
                    this.killLocation = nPC.getWorldLocation();
                    this.phase = VorkathPhase.DEAD;
                    this.status = "Kill #" + this.killCount + " complete";
                }
            }
        }
        if (this.phase == VorkathPhase.ACID_PHASE && this.acidPhaseStartTick >= 0 && this.client.getTickCount() - this.acidPhaseStartTick > 28) {
            this.phase = VorkathPhase.FIGHTING;
            this.attackCount = 0;
            if (this.runWasEnabled) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePauseWidgetPacket(10485782, -1);
                this.runWasEnabled = false;
            }
        }
        if ((this.phase == VorkathPhase.DEAD || this.phase == VorkathPhase.IDLE || this.phase == VorkathPhase.WAKING) && nPC != null) {
            this.phase = VorkathPhase.FIGHTING;
        }
        this.lastVorkathAnim = n;
    }

    private boolean acidPoolsExistNearby() {
        return TileObjects.getNearest(tileObject -> tileObject.getId() == 32000) != null;
    }

    private int dodgeFireball(Player player, NPC nPC) {
        WorldPoint worldPoint;
        this.needsFireballDodge = false;
        this.fireballDodgedOnTick = this.client.getTickCount();
        this.fireballLandingTile = player.getWorldLocation();
        this.status = "Dodging fireball!";
        this.fireballDodgeTile = worldPoint = this.calcDodgeTile(player.getWorldLocation(), nPC);
        for (int i = 0; i < 3; ++i) {
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(worldPoint);
        }
        return 300;
    }

    private WorldPoint calcDodgeTile(WorldPoint worldPoint, WorldPoint worldPoint2) {
        int[][] nArrayArray;
        int n;
        int n2;
        double d;
        int n3 = 0;
        int n4 = -2;
        if (worldPoint2 != null && (d = Math.sqrt((double)(n2 = worldPoint.getX() - worldPoint2.getX()) * (double)n2 + (double)(n = worldPoint.getY() - worldPoint2.getY()) * (double)n)) > 0.0) {
            n3 = (int)Math.round((double)n2 / d * 2.0);
            n4 = (int)Math.round((double)n / d * 2.0);
            if (Math.abs(n3) < 2 && Math.abs(n4) < 2) {
                if (Math.abs(n2) >= Math.abs(n)) {
                    n3 = n2 >= 0 ? 2 : -2;
                } else {
                    n4 = n >= 0 ? 2 : -2;
                }
            }
        }
        for (int[] nArray : nArrayArray = new int[][]{{n3, n4}, {0, -2}, {0, 2}, {-2, 0}, {2, 0}, {-n3, -n4}}) {
            WorldPoint worldPoint3 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (!this.isValidDodgeTile(worldPoint3)) continue;
            return worldPoint3;
        }
        return worldPoint;
    }

    private WorldPoint calcDodgeTile(WorldPoint worldPoint, NPC nPC) {
        return this.calcDodgeTile(worldPoint, nPC != null ? nPC.getWorldLocation() : null);
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

    private int handleZombieSpawn(NPC nPC) {
        if (this.config.useCrumbleUndead() && this.canCastCrumbleUndead()) {
            this.status = "Zombie spawn! Casting Crumble Undead...";
            Magic.cast((Spell)SpellBook.Standard.CRUMBLE_UNDEAD, nPC);
        } else {
            this.status = "Zombie spawn! Attacking" + (this.config.useCrumbleUndead() ? " (no runes/wrong spellbook)" : "...");
            nPC.interact("Attack");
        }
        return 600;
    }

    private boolean canCastCrumbleUndead() {
        if (this.client.getVarbitValue(4070) != 0) {
            return false;
        }
        if (this.client.getBoostedSkillLevel(Skill.MAGIC) < 39) {
            return false;
        }
        if (this.countInventoryItem(556) < 2) {
            return false;
        }
        if (this.countInventoryItem(557) < 2) {
            return false;
        }
        return this.countInventoryItem(562) >= 1;
    }

    private int countInventoryItem(int n) {
        return Inventory.getAll(item -> item != null && item.getId() == n).stream().mapToInt(Item::getQuantity).sum();
    }

    private int handleAcidPhase(Player player, NPC nPC) {
        this.status = this.acidIntendedPos != null ? "Acid \u2014 walking to " + this.acidIntendedPos.getX() + "," + this.acidIntendedPos.getY() : "Acid phase \u2014 initialising";
        if (this.config.wooxWalk() && nPC != null) {
            nPC.interact("Attack");
        }
        return 600;
    }

    private void setupAcidWalkTiles(Player player) {
        WorldPoint worldPoint;
        this.lastAcidPos = null;
        this.acidIntendedPos = player.getWorldLocation();
        boolean bl = this.runWasEnabled = this.client.getVarbitValue(173) != 0;
        if (this.runWasEnabled) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(10485782, -1);
        }
        if ((worldPoint = this.findNextSafeAcidTile(this.acidIntendedPos)) != null) {
            Movement.walkTo(worldPoint);
            this.lastAcidPos = this.acidIntendedPos;
            this.acidIntendedPos = worldPoint;
        }
    }

    private WorldPoint findNextSafeAcidTile(WorldPoint worldPoint) {
        int[][] nArrayArray = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        WorldPoint worldPoint2 = null;
        WorldPoint worldPoint3 = null;
        for (int[] nArray : nArrayArray) {
            WorldPoint worldPoint4 = new WorldPoint(worldPoint.getX() + nArray[0], worldPoint.getY() + nArray[1], worldPoint.getPlane());
            if (this.isAcidTile(worldPoint4) || !this.isValidDodgeTile(worldPoint4)) continue;
            if (worldPoint4.equals((Object)this.lastAcidPos)) {
                if (worldPoint3 != null) continue;
                worldPoint3 = worldPoint4;
                continue;
            }
            if (worldPoint2 != null) continue;
            worldPoint2 = worldPoint4;
        }
        return worldPoint2 != null ? worldPoint2 : worldPoint3;
    }

    private boolean isAcidTile(WorldPoint worldPoint) {
        Tile tile;
        int n = worldPoint.getX() - this.client.getBaseX();
        int n2 = worldPoint.getY() - this.client.getBaseY();
        if (n >= 0 && n2 >= 0 && n < 104 && n2 < 104 && (tile = this.client.getScene().getTiles()[this.client.getPlane()][n][n2]) != null) {
            for (GameObject gameObject : tile.getGameObjects()) {
                if (gameObject == null || gameObject.getId() != 32000) continue;
                return true;
            }
            GroundObject groundObject = tile.getGroundObject();
            if (groundObject != null && groundObject.getId() == 32000) {
                return true;
            }
            DecorativeObject decorativeObject = tile.getDecorativeObject();
            if (decorativeObject != null && decorativeObject.getId() == 32000) {
                return true;
            }
        }
        return false;
    }

    private boolean isInMeleeRange(Player player, NPC nPC) {
        WorldPoint worldPoint = player.getWorldLocation();
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        int n2 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)));
        int n3 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)));
        return Math.max(n2, n3) <= 1;
    }

    private boolean isInAttackRange(Player player, NPC nPC) {
        WorldPoint worldPoint = player.getWorldLocation();
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        int n2 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)));
        int n3 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)));
        return Math.max(n2, n3) <= this.config.attackRange();
    }

    private WorldPoint findSafeApproachTile(Player player, NPC nPC) {
        WorldPoint worldPoint = player.getWorldLocation();
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        int n2 = this.config.attackRange();
        int n3 = this.fireballDodgedOnTick >= 0 ? this.client.getTickCount() - this.fireballDodgedOnTick : Integer.MAX_VALUE;
        boolean bl = this.fireballLandingTile != null && n3 < 9;
        WorldPoint worldPoint3 = null;
        int n4 = Integer.MAX_VALUE;
        for (int i = -n2; i <= n2; ++i) {
            for (int j = -n2; j <= n2; ++j) {
                int n5;
                int n6;
                int n7;
                WorldPoint worldPoint4 = new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane());
                if (!this.isWalkable(worldPoint4) || bl && worldPoint4.equals((Object)this.fireballLandingTile) || Math.max(n7 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint4.getX(), worldPoint4.getX() - (worldPoint2.getX() + n - 1))), n6 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint4.getY(), worldPoint4.getY() - (worldPoint2.getY() + n - 1)))) > n2 || (n5 = Math.abs(i) + Math.abs(j)) >= n4) continue;
                n4 = n5;
                worldPoint3 = worldPoint4;
            }
        }
        return worldPoint3;
    }

    private int stepBackFromMelee(Player player, NPC nPC) {
        this.status = "Too close \u2014 backing away from melee range";
        Movement.walkTo(this.calcDodgeTile(player.getWorldLocation(), nPC));
        return 600;
    }

    private int attackVorkath(NPC nPC, Player player) {
        WorldPoint worldPoint;
        Actor actor = player.getInteracting();
        if (actor instanceof NPC && ((NPC)actor).getId() == 8061 && !((NPC)actor).isDead()) {
            this.status = "Fighting Vorkath (" + this.attackCount + "/6)";
            return 300;
        }
        if (!this.isInAttackRange(player, nPC) && (worldPoint = this.findSafeApproachTile(player, nPC)) != null) {
            this.status = "Repositioning for attack";
            Movement.walkTo(worldPoint);
            return 600;
        }
        this.status = "Attacking Vorkath";
        nPC.interact("Attack");
        return 600;
    }

    private int wakeVorkath(NPC nPC) {
        this.phase = VorkathPhase.WAKING;
        this.status = "Poking Vorkath...";
        this.attackCount = 0;
        this.lastVorkathAnim = -1;
        this.vorkathSpawnPoint = nPC.getWorldLocation();
        nPC.interact("Poke");
        return 500;
    }

    private void maintainPrayer() {
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        Prayer prayer = this.config.overheadPrayer().prayer;
        if (this.config.prayFlick() && prayer != null) {
            boolean bl;
            int n = this.client.getTickCount();
            int n2 = this.lastAttackAnimTick >= 0 ? n - this.lastAttackAnimTick : Integer.MAX_VALUE;
            boolean bl2 = bl = n2 <= 2;
            if (bl && !this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            } else if (!bl && this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
        } else if (prayer != null && !this.client.isPrayerActive(prayer)) {
            Prayers.toggle(prayer);
        }
        Prayer prayer2 = this.config.offensivePrayer().prayer;
        if (prayer2 != null && !this.client.isPrayerActive(prayer2)) {
            Prayers.toggle(prayer2);
        }
    }

    private boolean shouldEat(Player player) {
        if (this.phase == VorkathPhase.ACID_PHASE) {
            return false;
        }
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
            this.status = "Eating " + item2.getName();
            item2.interact("Eat");
            return 1800;
        }
        this.status = "Out of food!";
        return 600;
    }

    private boolean shouldDrinkAntifire() {
        if (this.config.antifireName().isEmpty()) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.antifireName().toLowerCase()));
        if (item2 == null) {
            return false;
        }
        long l = System.currentTimeMillis() - this.lastAntifireDrinkTime;
        long l2 = (long)this.config.antifireDurationSeconds() * 1000L;
        return this.lastAntifireDrinkTime == 0L || l > l2 - 30000L;
    }

    private int drinkAntifire() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.antifireName().toLowerCase()));
        if (item2 != null) {
            this.status = "Drinking " + item2.getName();
            item2.interact("Drink");
            this.lastAntifireDrinkTime = System.currentTimeMillis();
            return 600;
        }
        return 300;
    }

    private boolean shouldDrinkAntivenom() {
        if (this.config.antivenomName().isEmpty()) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.antivenomName().toLowerCase()));
        if (item2 == null) {
            return false;
        }
        if (this.lastAntifireDrinkTime > 0L && System.currentTimeMillis() - this.lastAntifireDrinkTime < 1500L) {
            return false;
        }
        long l = System.currentTimeMillis() - this.lastAntivenomDrinkTime;
        long l2 = (long)this.config.antivenomDurationSeconds() * 1000L;
        return this.lastAntivenomDrinkTime == 0L || l > l2 - 30000L;
    }

    private int drinkAntivenom() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.antivenomName().toLowerCase()));
        if (item2 != null) {
            this.status = "Drinking " + item2.getName();
            item2.interact("Drink");
            this.lastAntivenomDrinkTime = System.currentTimeMillis();
            return 600;
        }
        return 300;
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
            item2.interact("Drink");
            return 600;
        }
        return 300;
    }

    private void resetState() {
        this.phase = VorkathPhase.IDLE;
        this.status = "Idle";
        this.killCount = 0;
        this.attackCount = 0;
        this.lastVorkathAnim = -1;
        this.lastAcidPos = null;
        this.acidIntendedPos = null;
        this.runWasEnabled = false;
        this.acidPhaseStartTick = -1;
        this.needsFireballDodge = false;
        this.fireballDodgedOnTick = -1;
        this.fireballLandingTile = null;
        this.fireballDodgeTile = null;
        this.killLocation = null;
        this.vorkathSpawnPoint = null;
        this.lastAcidPos = null;
        this.runWasEnabled = false;
        this.lastAntifireDrinkTime = 0L;
        this.lastAntivenomDrinkTime = 0L;
        this.loggedInLastTick = false;
        this.resupplyState = null;
        this.playerDied = false;
        this.lootingEntryTick = -1;
        this.lastAttackAnimTick = -1;
    }

    private int handleResupply(Player player) {
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
                    if (this.hasRequiredItems()) {
                        this.status = "Supplies OK \u2014 skipping bank";
                        this.phase = VorkathPhase.IDLE;
                        this.resupplyState = null;
                        this.lootingEntryTick = -1;
                        return 600;
                    }
                }
                this.playerDied = false;
                this.lootingEntryTick = -1;
                this.status = "Resupply: teleporting home";
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction1Packet(14286854, -1, -1);
                this.resupplyState = ResupplyState.TELEPORTING;
                return 4000;
            }
            case TELEPORTING: {
                this.status = "Resupply: arrived home \u2014 finding Enchanted Geyser";
                this.resupplyState = ResupplyState.REJUVENATING;
                return 600;
            }
            case REJUVENATING: {
                TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject.getId() == 42822);
                if (tileObject2 != null) {
                    this.status = "Resupply: rejuvenating at Enchanted Geyser";
                    tileObject2.interact("Rejuvenate");
                    this.resupplyState = ResupplyState.WALKING_TO_BANK;
                    return 4000;
                }
                this.status = "Resupply: looking for Enchanted Geyser...";
                return 600;
            }
            case WALKING_TO_BANK: {
                if (player.getWorldLocation().distanceTo(BANK_TILE) > 3) {
                    this.status = "Resupply: walking to bank";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(BANK_TILE);
                    return 1200;
                }
                TileObject tileObject3 = TileObjects.getNearest(tileObject -> tileObject.getId() == 10083);
                if (tileObject3 != null) {
                    this.status = "Resupply: bank Last-preset";
                    tileObject3.interact("Last-preset");
                    this.resupplyState = ResupplyState.USING_BANK;
                    return 3000;
                }
                this.status = "Resupply: bank booth not found";
                return 600;
            }
            case USING_BANK: {
                this.status = "Resupply: bank done \u2014 walking to glider";
                this.resupplyState = ResupplyState.WALKING_TO_GLIDER;
                return 600;
            }
            case WALKING_TO_GLIDER: {
                if (player.getWorldLocation().distanceTo(GLIDER_TILE) > 4) {
                    this.status = "Resupply: walking to Gnome Glider";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(GLIDER_TILE);
                    return 1200;
                }
                TileObject tileObject4 = TileObjects.getNearest(tileObject -> tileObject.getId() == 187);
                if (tileObject4 != null) {
                    this.status = "Resupply: Gnome Glider \u2014 Previous";
                    tileObject4.interact("Previous");
                    this.resupplyState = ResupplyState.BOARDING_BOAT;
                    return 3000;
                }
                this.status = "Resupply: Gnome Glider not found";
                return 600;
            }
            case BOARDING_BOAT: {
                TileObject tileObject5 = TileObjects.getNearest(tileObject -> tileObject.getId() == 8926);
                if (tileObject5 != null) {
                    this.status = "Resupply: Fremennik Boat \u2014 Travel";
                    tileObject5.interact("Travel");
                    this.resupplyState = ResupplyState.WALKING_TO_ENTRANCE;
                    return 2000;
                }
                this.status = "Resupply: Fremennik Boat not found";
                return 600;
            }
            case WALKING_TO_ENTRANCE: {
                if (player.getWorldLocation().distanceTo(ENTRANCE_TILE) > 5) {
                    this.status = "Resupply: walking to Vorkath entrance";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(ENTRANCE_TILE);
                    return 1200;
                }
                TileObject tileObject6 = TileObjects.getNearest(tileObject -> tileObject.getId() == 31990);
                if (tileObject6 != null) {
                    this.status = "Resupply: climbing Ice Chunks";
                    tileObject6.interact("Climb-over");
                    this.resupplyState = ResupplyState.CLIMBING_ENTRANCE;
                    return 2000;
                }
                this.status = "Resupply: Ice Chunks not found";
                return 600;
            }
            case CLIMBING_ENTRANCE: {
                this.status = "Resupply complete \u2014 ready to fight";
                this.phase = VorkathPhase.IDLE;
                this.resupplyState = null;
                return 3000;
            }
        }
        return 600;
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
            int n2 = Inventory.getAll(item -> item != null && item.getName().toLowerCase().contains(string3)).stream().mapToInt(Item::getQuantity).sum();
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

    public VorkathPhase getPhase() {
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
}

