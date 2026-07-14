/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameObject
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldArea
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.prayer;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.prayer.BonePrayerConfig;
import net.runelite.client.plugins.svc.prayer.BonePrayerOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 AIO Prayer</font></nobr></html>", description="Withdraws all bones from the home bank and offers them on the altar, then repeats.", tags={"orion", "prayer", "bones", "altar", "bot", "aio", "skilling"}, enabledByDefault=false)
public class BonePrayerPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BonePrayerConfig config;
    private BonePrayerOverlay overlay;
    private State state = State.OFFER;
    private String status = "Starting";
    private boolean loggedInLastTick = false;
    private long sessionStartMs = 0L;
    private int startXp = -1;
    private int bonesOffered = 0;
    private int trips = 0;
    private WorldPoint altarAnchor = null;
    private WorldPoint bankAnchor = null;
    private int lastOfferTick = -100;
    private int bankClickTick = -100;
    private long bankOpenAt = 0L;
    private long progressAt = 0L;
    private int prevBoneCount = -1;
    private boolean preferMenuOffer = true;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(BonePrayerConfig.class).toInstance((Object)((BonePrayerConfig)configManager.getConfig(BonePrayerConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new BonePrayerOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.state = State.OFFER;
        this.status = "Starting";
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
        this.startXp = -1;
        this.bonesOffered = 0;
        this.trips = 0;
        this.altarAnchor = null;
        this.bankAnchor = null;
        this.lastOfferTick = -100;
        this.bankClickTick = -100;
        this.bankOpenAt = 0L;
        this.progressAt = 0L;
        this.prevBoneCount = -1;
        this.preferMenuOffer = true;
    }

    @Override
    protected void onStop() throws Exception {
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
        }
        this.overlay = null;
        this.status = "Idle";
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.status = "Logged out";
            this.loggedInLastTick = false;
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.status = "Loading...";
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
        if (this.startXp < 0) {
            this.startXp = Skills.getExperience(Skill.PRAYER);
        }
        int n = this.boneCount();
        if (this.prevBoneCount >= 0 && n < this.prevBoneCount) {
            this.bonesOffered += this.prevBoneCount - n;
            this.progressAt = System.currentTimeMillis();
        }
        this.prevBoneCount = n;
        switch (this.state.ordinal()) {
            case 0: {
                return this.doOffer(player, n);
            }
            case 1: {
                return this.doBank(player, n);
            }
            case 2: {
                return this.doIdle();
            }
        }
        return 600;
    }

    private int doOffer(Player player, int n) {
        if (n == 0) {
            ++this.trips;
            this.status = "Out of bones \u2014 banking";
            this.state = State.BANK;
            return 400;
        }
        TileObject tileObject = this.findAltar();
        if (tileObject == null) {
            if (this.altarAnchor != null) {
                this.status = "Walking to altar";
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(this.altarAnchor);
                return 1200;
            }
            this.status = "Altar (409) not found";
            return 1000;
        }
        this.altarAnchor = tileObject.getWorldLocation();
        int n2 = this.objectDistance(player, tileObject);
        if (player.getAnimation() != -1) {
            this.progressAt = System.currentTimeMillis();
            this.status = "Offering bones (" + n + " left)";
            return this.jitter(1000);
        }
        if (this.client.getTickCount() - this.lastOfferTick < 2) {
            return 300;
        }
        Item item = Inventory.getFirst(this.bonePredicate());
        if (item == null) {
            return 300;
        }
        if (this.progressAt > 0L && System.currentTimeMillis() - this.progressAt > 5000L) {
            this.preferMenuOffer = !this.preferMenuOffer;
            this.progressAt = System.currentTimeMillis();
            if (n2 > 2) {
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(tileObject.getWorldLocation());
            }
            this.status = "Retrying offer (" + (this.preferMenuOffer ? "menu" : "packet") + ")";
        } else {
            this.status = n2 > 2 ? "Walking to altar" : "Offering bones (" + n + " left)";
        }
        this.offerBone(item, tileObject);
        this.lastOfferTick = this.client.getTickCount();
        if (this.progressAt == 0L) {
            this.progressAt = System.currentTimeMillis();
        }
        return this.jitter(1000);
    }

    private void offerBone(Item item, TileObject tileObject) {
        if (this.preferMenuOffer) {
            item.useOn(tileObject);
        } else {
            MousePackets.queueClickPacket();
            ObjectPackets.useItemOnTileObject(item, tileObject);
        }
    }

    private int doBank(Player player, int n) {
        if (n > 0 && Inventory.getFreeSlots() == 0) {
            if (Bank.isOpen()) {
                Bank.close();
            }
            this.state = State.OFFER;
            return 400;
        }
        if (!Bank.isOpen()) {
            TileObject tileObject = this.findBank();
            if (tileObject == null) {
                if (this.bankAnchor != null) {
                    this.status = "Walking to bank";
                    MousePackets.queueClickPacket();
                    MovementPackets.sendMovement(this.bankAnchor);
                    return 1200;
                }
                this.status = "Bank booth not found";
                return 1000;
            }
            this.bankAnchor = tileObject.getWorldLocation();
            int n2 = this.objectDistance(player, tileObject);
            if (n2 > 3) {
                this.status = "Walking to bank";
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(tileObject.getWorldLocation());
                return 1000;
            }
            if (this.client.getTickCount() - this.bankClickTick < 5) {
                this.status = "Opening bank...";
                if (System.currentTimeMillis() - this.bankOpenAt > 4000L) {
                    this.bankClickTick = -100;
                }
                return 400;
            }
            this.status = "Opening bank";
            tileObject.interact("Use");
            this.bankClickTick = this.client.getTickCount();
            this.bankOpenAt = System.currentTimeMillis();
            return 1200;
        }
        if (this.hasJunk()) {
            this.status = "Depositing";
            Bank.depositAll(item -> item != null && !this.isOfferable(item.getName()) && !this.isKept(item.getName()));
            return 500;
        }
        if (Inventory.getFreeSlots() == 0) {
            Bank.close();
            this.status = "Withdrew bones \u2014 offering";
            this.state = State.OFFER;
            return 500;
        }
        Item item2 = Bank.getFirst(this.bonePredicate());
        if (item2 == null) {
            if (n > 0) {
                Bank.close();
                this.state = State.OFFER;
                return 500;
            }
            Bank.close();
            if (this.config.stopWhenOut()) {
                this.status = "Out of bones \u2014 stopped";
                return -1;
            }
            this.status = "Out of bones \u2014 re-checking";
            this.state = State.IDLE;
            return 10000;
        }
        this.status = "Withdrawing " + item2.getName();
        Bank.withdrawAll(item2.getId(), Bank.WithdrawMode.ITEM);
        return 600;
    }

    private int doIdle() {
        this.status = "Idle \u2014 re-checking bank";
        this.state = State.BANK;
        return 600;
    }

    private int objectDistance(Player player, TileObject tileObject) {
        WorldArea worldArea;
        WorldPoint worldPoint = player.getWorldLocation();
        if (worldPoint == null || tileObject == null) {
            return Integer.MAX_VALUE;
        }
        if (tileObject instanceof GameObject && (worldArea = ((GameObject)tileObject).getWorldArea()) != null) {
            return worldArea.distanceTo(worldPoint);
        }
        return worldPoint.distanceTo(tileObject.getWorldLocation());
    }

    private TileObject findAltar() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 409);
        if (tileObject2 != null) {
            return tileObject2;
        }
        return TileObjects.getNearest("Altar");
    }

    private TileObject findBank() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 10083);
        if (tileObject2 != null) {
            return tileObject2;
        }
        TileObject tileObject3 = TileObjects.getNearest("Bank booth");
        if (tileObject3 != null) {
            return tileObject3;
        }
        return TileObjects.getNearest("Bank chest");
    }

    private Predicate<Item> bonePredicate() {
        return item -> item != null && this.isOfferable(item.getName());
    }

    private boolean isOfferable(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        if (this.config.offerAllBones()) {
            return string2.contains("bones");
        }
        for (String string3 : this.specificBoneList()) {
            if (string3.isEmpty() || !string2.contains(string3)) continue;
            return true;
        }
        return false;
    }

    private List<String> specificBoneList() {
        ArrayList<String> arrayList = new ArrayList<String>();
        String string = this.config.specificBones();
        if (string == null) {
            return arrayList;
        }
        for (String string2 : string.split(",")) {
            String string3 = string2.trim().toLowerCase();
            if (string3.isEmpty()) continue;
            arrayList.add(string3);
        }
        return arrayList;
    }

    private int boneCount() {
        List<Item> list = Inventory.getAll(this.bonePredicate());
        return list == null ? 0 : list.size();
    }

    private boolean hasJunk() {
        return Inventory.getFirst(item -> item != null && !this.isOfferable(item.getName()) && !this.isKept(item.getName())) != null;
    }

    private boolean isKept(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : this.keepNames()) {
            if (string3.isEmpty() || !string2.contains(string3)) continue;
            return true;
        }
        return false;
    }

    private List<String> keepNames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        String string = this.config.keepItems();
        if (string == null) {
            return arrayList;
        }
        for (String string2 : string.split(",")) {
            String string3 = string2.trim().toLowerCase();
            if (string3.isEmpty()) continue;
            arrayList.add(string3);
        }
        return arrayList;
    }

    private int jitter(int n) {
        int n2 = 150;
        return n2 > 0 ? n + Rand.nextInt(0, n2) : n;
    }

    public String getStatus() {
        return this.status;
    }

    public String getStateName() {
        return this.state.name();
    }

    public int getBonesInInventory() {
        return this.boneCount();
    }

    public int getBonesOffered() {
        return this.bonesOffered;
    }

    public int getTrips() {
        return this.trips;
    }

    public int getPrayerLevel() {
        return this.client.getBoostedSkillLevel(Skill.PRAYER);
    }

    public int getXpGained() {
        return this.startXp < 0 ? 0 : Math.max(0, Skills.getExperience(Skill.PRAYER) - this.startXp);
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    private static enum State {
        OFFER,
        BANK,
        IDLE;

    }
}

