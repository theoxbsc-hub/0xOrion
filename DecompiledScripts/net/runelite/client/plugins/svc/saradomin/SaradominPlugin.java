/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.saradomin;

import com.google.inject.Binder;
import java.util.ArrayList;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.saradomin.SaradominConfig;
import net.runelite.client.plugins.svc.saradomin.SaradominConstants;
import net.runelite.client.plugins.svc.saradomin.SaradominOverlay;
import net.runelite.client.plugins.svc.saradomin.SaradominPhase;
import net.runelite.client.plugins.svc.saradomin.SaradominResupplyState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Saradomin</font></nobr></html>", description="GWD Commander Zilyana boss fight bot \u2014 ranged kiting, prayer, loot, altar, and resupply", tags={"orion", "saradomin", "zilyana", "gwd", "boss", "combat", "bot"}, enabledByDefault=false, hidden=true)
public class SaradominPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SaradominConfig config;
    private SaradominOverlay overlay;
    private SaradominPhase phase = SaradominPhase.IDLE;
    private SaradominResupplyState resupplyState = null;
    private String status = "Idle";
    private int killCount = 0;
    private long startTime = 0L;
    private WorldPoint killLocation = null;
    private int lootingEntryTick = -1;
    private boolean altarInteracted = false;
    private boolean loggedInLastTick = false;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(SaradominConfig.class).toInstance((Object)((SaradominConfig)configManager.getConfig(SaradominConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new SaradominOverlay(this.client, this, this.config);
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
            this.phase = SaradominPhase.IDLE;
        } else if (gameState == GameState.LOADING && this.phase == SaradominPhase.FIGHTING) {
            this.phase = SaradominPhase.RESUPPLYING;
            this.resupplyState = SaradominResupplyState.TELEPORTING;
            this.status = "Died \u2014 returning to Saradomin";
        }
    }

    @Override
    protected int loop() {
        int n;
        int n2;
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return 1000;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            return 600;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (this.phase == SaradominPhase.RESUPPLYING) {
            return this.handleResupply(player);
        }
        if (this.phase == SaradominPhase.USING_ALTAR) {
            return this.handleAltar();
        }
        if (this.phase == SaradominPhase.LOOTING) {
            return this.handleLoot(player);
        }
        this.maintainPrayer();
        int n3 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n4 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        if (n3 * 100 / n4 < this.config.eatAtHpPercent() && (n2 = this.eatFood()) > 0) {
            return n2;
        }
        n2 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n5 = this.client.getRealSkillLevel(Skill.PRAYER);
        if (n5 > 0 && n2 * 100 / n5 < this.config.prayerThresholdPct() && (n = this.drinkPrayerPotion()) > 0) {
            return n;
        }
        NPC nPC = NPCs.getNearest(596);
        if (nPC == null || nPC.isDead()) {
            if (this.phase == SaradominPhase.FIGHTING) {
                ++this.killCount;
                this.killLocation = player.getWorldLocation();
                this.lootingEntryTick = this.client.getTickCount();
                this.phase = SaradominPhase.LOOTING;
                this.status = "Zilyana dead \u2014 looting";
                return 300;
            }
            this.status = "Waiting for Zilyana to spawn";
            return 600;
        }
        this.phase = SaradominPhase.FIGHTING;
        return this.attackAndKite(nPC, player);
    }

    private int attackAndKite(NPC nPC, Player player) {
        WorldPoint worldPoint;
        WorldPoint worldPoint2 = player.getWorldLocation();
        int n = SaradominPlugin.chebyshev(worldPoint2, worldPoint = nPC.getWorldLocation());
        if (n <= this.config.attackRange()) {
            this.status = "Attacking Zilyana (kiting)";
            nPC.interact("Attack");
            WorldPoint worldPoint3 = this.getFarKiteTile(worldPoint2);
            Movement.walkTo(worldPoint3);
            return 600;
        }
        this.status = "Kiting \u2014 dist " + n;
        if (!player.isMoving()) {
            Movement.walkTo(worldPoint);
        }
        return 300;
    }

    private WorldPoint getFarKiteTile(WorldPoint worldPoint) {
        int n;
        int n2 = SaradominPlugin.chebyshev(worldPoint, SaradominConstants.KITE_TILE_NE);
        return n2 >= (n = SaradominPlugin.chebyshev(worldPoint, SaradominConstants.KITE_TILE_SW)) ? SaradominConstants.KITE_TILE_NE : SaradominConstants.KITE_TILE_SW;
    }

    private static int chebyshev(WorldPoint worldPoint, WorldPoint worldPoint2) {
        return Math.max(Math.abs(worldPoint.getX() - worldPoint2.getX()), Math.abs(worldPoint.getY() - worldPoint2.getY()));
    }

    private int handleLoot(Player player) {
        int n;
        int n2 = this.client.getTickCount();
        int n3 = n2 - this.lootingEntryTick;
        if (n3 < 10) {
            this.status = "Looting: waiting for drops (" + (10 - n3) + " ticks)";
            return 600;
        }
        if (Inventory.isFull() && this.config.eatWhileLooting() && (n = this.eatFood()) > 0) {
            this.status = "Looting: eating to free slot";
            return n;
        }
        TileItem tileItem = this.findLootItem();
        if (tileItem != null && !Inventory.isFull()) {
            this.status = "Looting: " + tileItem.getName();
            tileItem.interact("Take");
            return 600;
        }
        if (this.config.enableResupply() && !this.hasRequiredItems()) {
            this.status = "Resupply needed \u2014 teleporting home";
            this.sendHomeTele();
            this.phase = SaradominPhase.RESUPPLYING;
            this.resupplyState = SaradominResupplyState.TELEPORTING;
            return 4000;
        }
        this.altarInteracted = false;
        this.phase = SaradominPhase.USING_ALTAR;
        this.status = "Looting done \u2014 using prayer altar";
        return 300;
    }

    private TileItem findLootItem() {
        String string = this.config.lootItems().trim();
        WorldPoint worldPoint = this.killLocation;
        if (string.isEmpty()) {
            return TileItems.getNearest(tileItem -> {
                if (tileItem == null) {
                    return false;
                }
                return worldPoint == null || tileItem.getWorldLocation().distanceTo(worldPoint) <= 15;
            });
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : string.split(",")) {
            String string3 = string2.trim().toLowerCase();
            if (string3.isEmpty()) continue;
            arrayList.add(string3);
        }
        return TileItems.getNearest(tileItem -> {
            if (tileItem == null || tileItem.getName() == null) {
                return false;
            }
            if (worldPoint != null && tileItem.getWorldLocation().distanceTo(worldPoint) > 15) {
                return false;
            }
            String string = tileItem.getName().toLowerCase();
            for (String string2 : arrayList) {
                if (!string.contains(string2)) continue;
                return true;
            }
            return false;
        });
    }

    private int handleAltar() {
        int n = this.client.getRealSkillLevel(Skill.PRAYER);
        int n2 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        if (n2 >= n) {
            this.altarInteracted = false;
            this.phase = SaradominPhase.IDLE;
            this.status = "Prayer restored \u2014 waiting for Zilyana";
            return 300;
        }
        if (this.altarInteracted) {
            this.status = "Using altar (" + n2 + "/" + n + ")";
            return 600;
        }
        TileObject tileObject = TileObjects.getNearest(17010);
        if (tileObject == null) {
            this.status = "Altar not found (ID 17010) \u2014 check Entity Inspector";
            this.phase = SaradominPhase.IDLE;
            return 600;
        }
        this.status = "Using prayer altar";
        tileObject.interact("Pray");
        this.altarInteracted = true;
        return 2000;
    }

    private int handleResupply(Player player) {
        if (this.resupplyState == null) {
            this.resupplyState = SaradominResupplyState.TELEPORTING;
        }
        switch (this.resupplyState) {
            case TELEPORTING: {
                this.status = "Resupply: waiting for home teleport";
                this.resupplyState = SaradominResupplyState.REJUVENATING;
                return 4000;
            }
            case REJUVENATING: {
                TileObject tileObject = TileObjects.getNearest(42822);
                if (tileObject != null) {
                    this.status = "Resupply: rejuvenating";
                    tileObject.interact("Rejuvenate");
                    this.resupplyState = SaradominResupplyState.WALKING_TO_BANK;
                    return 4000;
                }
                this.status = "Resupply: looking for Enchanted Geyser";
                return 600;
            }
            case WALKING_TO_BANK: {
                if (player.getWorldLocation().distanceTo(SaradominConstants.BANK_TILE) > 3) {
                    this.status = "Resupply: walking to bank";
                    Movement.walkTo(SaradominConstants.BANK_TILE);
                    return 1200;
                }
                TileObject tileObject = TileObjects.getNearest(10083);
                if (tileObject != null) {
                    this.status = "Resupply: using bank preset";
                    tileObject.interact("Last-preset");
                    this.resupplyState = SaradominResupplyState.USING_BANK;
                    return 3000;
                }
                this.status = "Resupply: bank booth not found";
                return 600;
            }
            case USING_BANK: {
                this.status = "Resupply: waiting for preset equip";
                this.resupplyState = SaradominResupplyState.WALKING_TO_GLIDER;
                return 1000;
            }
            case WALKING_TO_GLIDER: {
                if (player.getWorldLocation().distanceTo(SaradominConstants.GLIDER_TILE) > 4) {
                    this.status = "Resupply: walking to gnome glider";
                    Movement.walkTo(SaradominConstants.GLIDER_TILE);
                    return 1200;
                }
                TileObject tileObject = TileObjects.getNearest(187);
                if (tileObject != null) {
                    this.status = "Resupply: glider \u2014 Previous";
                    tileObject.interact("Previous");
                    this.resupplyState = SaradominResupplyState.USING_GLIDER;
                    return 3000;
                }
                this.status = "Resupply: gnome glider not found";
                return 600;
            }
            case USING_GLIDER: {
                this.status = "Resupply: waiting for glider travel";
                this.resupplyState = SaradominResupplyState.WALKING_TO_DOOR;
                return 3000;
            }
            case WALKING_TO_DOOR: {
                TileObject tileObject = TileObjects.getNearest(47450);
                if (tileObject == null) {
                    this.status = "Resupply: searching for Saradomin door (ID 47450)";
                    return 600;
                }
                if (player.getWorldLocation().distanceTo(tileObject.getWorldLocation()) > 5) {
                    this.status = "Resupply: walking to Saradomin door";
                    Movement.walkTo(tileObject.getWorldLocation());
                    return 1200;
                }
                this.status = "Resupply: entering boss room";
                tileObject.interact("Enter");
                this.resupplyState = SaradominResupplyState.ENTERING_ROOM;
                return 3000;
            }
            case ENTERING_ROOM: {
                this.status = "Resupply: loading boss room";
                this.phase = SaradominPhase.IDLE;
                this.resupplyState = null;
                return 3000;
            }
        }
        return 600;
    }

    private void maintainPrayer() {
        Prayer prayer;
        Prayer prayer2 = this.config.overheadPrayer().prayer;
        if (prayer2 != null && !this.client.isPrayerActive(prayer2)) {
            Prayers.toggle(prayer2);
        }
        if ((prayer = this.config.offensivePrayer().prayer) != null && !this.client.isPrayerActive(prayer)) {
            Prayers.toggle(prayer);
        }
    }

    private int eatFood() {
        String string = this.config.foodName().trim().toLowerCase();
        if (string.isEmpty()) {
            return 0;
        }
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(string));
        if (item2 != null) {
            item2.interact("Eat");
            return 600;
        }
        return 0;
    }

    private int drinkPrayerPotion() {
        String string = this.config.prayerPotionName().trim().toLowerCase();
        if (string.isEmpty()) {
            return 0;
        }
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(string));
        if (item2 != null) {
            item2.interact("Drink");
            return 600;
        }
        return 0;
    }

    private void sendHomeTele() {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction1Packet(14286854, -1, -1);
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
            int n2 = Inventory.getAll(item -> item != null && item.getName() != null && item.getName().toLowerCase().contains(string3)).stream().mapToInt(Item::getQuantity).sum();
            if (n2 >= n) continue;
            return false;
        }
        return true;
    }

    private void resetState() {
        this.phase = SaradominPhase.IDLE;
        this.resupplyState = null;
        this.status = "Idle";
        this.killLocation = null;
        this.lootingEntryTick = -1;
        this.altarInteracted = false;
        this.loggedInLastTick = false;
    }

    SaradominPhase getPhase() {
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
}

