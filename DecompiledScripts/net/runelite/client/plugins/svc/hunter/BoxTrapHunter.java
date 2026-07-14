/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.hunter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.hunter.BoxTrapConstants;
import net.runelite.client.plugins.svc.hunter.ChinchompaType;
import net.runelite.client.plugins.svc.hunter.HunterConfig;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.GroundItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;

class BoxTrapHunter {
    private final Client client;
    private final HunterConfig config;
    private static final Predicate<TileObject> ANY_OBJECT = tileObject -> tileObject != null;
    private static final int LAY_TIMEOUT = 5;
    private static final int COLLECT_TIMEOUT = 6;
    private static final int CHECK_RANGE = 2;
    private static final int PICKUP_SCAN_RANGE = 16;
    private final Map<WorldPoint, Integer> trapTiles = new HashMap<WorldPoint, Integer>();
    private final Set<WorldPoint> blockedTiles = new HashSet<WorldPoint>();
    private WorldPoint clusterAnchor;
    private WorldPoint pendingLayTile;
    private int pendingLayTick;
    private WorldPoint pendingCollectTile;
    private int pendingCollectTick;
    private boolean trapCapReached;
    private int trapCapValue;
    private boolean layBlockedUntilCollect;
    private volatile boolean layHereRejected;
    private volatile boolean trapLimitHit;
    private volatile int parsedTrapCap;
    private volatile int caughtPending;
    private int activeTrapEstimate;
    private int lastHunterLevel = -1;
    private String status = "Starting";
    private int caught;
    private int laid;

    BoxTrapHunter(Client client, HunterConfig hunterConfig) {
        this.client = client;
        this.config = hunterConfig;
    }

    void reset() {
        this.trapTiles.clear();
        this.blockedTiles.clear();
        this.clusterAnchor = null;
        this.pendingLayTile = null;
        this.pendingLayTick = 0;
        this.pendingCollectTile = null;
        this.pendingCollectTick = 0;
        this.trapCapReached = false;
        this.trapCapValue = 0;
        this.layHereRejected = false;
        this.trapLimitHit = false;
        this.parsedTrapCap = 0;
        this.caughtPending = 0;
        this.layBlockedUntilCollect = false;
        this.activeTrapEstimate = 0;
        this.lastHunterLevel = -1;
        this.status = "Starting";
        this.caught = 0;
        this.laid = 0;
    }

    void onSceneLost() {
        this.trapTiles.clear();
        this.blockedTiles.clear();
        this.clusterAnchor = null;
        this.pendingLayTile = null;
        this.pendingCollectTile = null;
        this.layBlockedUntilCollect = false;
        this.activeTrapEstimate = 0;
        this.status = "Logged out";
    }

    void onChatMessage(String string) {
        if (string == null) {
            return;
        }
        if (string.contains("caught a") && string.contains("chinchompa")) {
            ++this.caughtPending;
        }
        if (string.contains("can't lay a trap here") || string.contains("cannot lay a trap here") || string.contains("can't set a trap here") || string.contains("cannot set a trap here") || string.contains("can't put a trap here")) {
            this.layHereRejected = true;
        }
        if (string.contains("trap") && (string.contains("only set") || string.contains("only lay") || string.contains("only setup") || string.contains("only have") || string.contains("only use") || string.contains("only place") || string.contains("at a time") || string.contains("up to") || string.contains("more trap") || string.contains("hunter level") || string.contains("hunting level") || string.contains("higher level"))) {
            this.trapLimitHit = true;
            int n = BoxTrapHunter.parseFirstInt(string);
            if (n > 0 && n <= 10) {
                this.parsedTrapCap = n;
            }
        }
    }

    int loop(Player player) {
        if (this.caughtPending > 0) {
            this.caught += this.caughtPending;
            this.caughtPending = 0;
        }
        int n = this.client.getTickCount();
        WorldPoint worldPoint = player.getWorldLocation();
        int n2 = this.hunterLevel();
        if (this.lastHunterLevel >= 0 && n2 > this.lastHunterLevel) {
            this.trapCapReached = false;
            this.trapCapValue = 0;
            this.parsedTrapCap = 0;
            this.layBlockedUntilCollect = false;
        }
        this.lastHunterLevel = n2;
        int n3 = player.getAnimation();
        if (n3 == 5208 || n3 == 5212) {
            this.status = "Working trap...";
            return Rand.nextInt(250, 450);
        }
        Integer n4 = this.handlePendingCollect(n, player);
        if (n4 != null) {
            return n4;
        }
        Integer n5 = this.handlePendingLay(n, player);
        if (n5 != null) {
            return n5;
        }
        this.pruneTraps(n);
        Integer n6 = this.handleFinishedTraps(worldPoint, n);
        if (n6 != null) {
            return n6;
        }
        Integer n7 = this.handleDroppedBoxes(worldPoint);
        if (n7 != null) {
            return n7;
        }
        if (Movement.isWalking()) {
            this.status = "Moving";
            return Rand.nextInt(300, 500);
        }
        return this.manageTraps(worldPoint, n);
    }

    private Integer handlePendingCollect(int n, Player player) {
        boolean bl;
        if (this.pendingCollectTile == null) {
            return null;
        }
        TileObject tileObject = this.findBoxTrapAt(this.pendingCollectTile);
        boolean bl2 = bl = player.getAnimation() == -1;
        if (tileObject == null) {
            this.trapTiles.remove(this.pendingCollectTile);
            this.layBlockedUntilCollect = false;
            this.pendingCollectTile = null;
            if (bl) {
                return null;
            }
            return Rand.nextInt(150, 300);
        }
        if (tileObject.getId() == 9380) {
            this.trapTiles.put(this.pendingCollectTile, n);
            this.pendingCollectTile = null;
            return Rand.nextInt(150, 300);
        }
        if (n - this.pendingCollectTick <= 6) {
            this.status = "Collecting...";
            return Rand.nextInt(300, 500);
        }
        this.pendingCollectTile = null;
        return 300;
    }

    private Integer handlePendingLay(int n, Player player) {
        boolean bl;
        if (this.pendingLayTile == null) {
            return null;
        }
        if (this.layHereRejected) {
            this.layHereRejected = false;
            this.blockedTiles.add(this.pendingLayTile);
            this.pendingLayTile = null;
            this.status = "Tile rejected \u2014 trying next tile";
            return 200;
        }
        if (this.trapLimitHit && this.findBoxTrapAt(this.pendingLayTile) == null) {
            this.trapLimitHit = false;
            this.trapCapReached = true;
            this.trapCapValue = this.parsedTrapCap > 0 ? this.parsedTrapCap : Math.max(1, this.trapTiles.size());
            this.layBlockedUntilCollect = true;
            this.pendingLayTile = null;
            this.status = "At box limit";
            return 300;
        }
        TileObject tileObject = this.findBoxTrapAt(this.pendingLayTile);
        boolean bl2 = tileObject != null;
        boolean bl3 = bl = player.getAnimation() == -1;
        if (bl2 && bl) {
            this.commitLaidTrap(this.pendingLayTile, n);
            this.pendingLayTile = null;
            return null;
        }
        if (n - this.pendingLayTick <= 5) {
            this.status = "Laying box trap...";
            return 300;
        }
        if (bl2) {
            this.commitLaidTrap(this.pendingLayTile, n);
            this.pendingLayTile = null;
            return null;
        }
        this.trapCapReached = true;
        this.trapCapValue = Math.max(1, this.trapTiles.size());
        this.pendingLayTile = null;
        return 400;
    }

    private void commitLaidTrap(WorldPoint worldPoint, int n) {
        if (!this.trapTiles.containsKey(worldPoint)) {
            ++this.laid;
        }
        this.trapTiles.put(worldPoint, n);
        if (this.clusterAnchor == null) {
            this.clusterAnchor = worldPoint;
        }
    }

    private void pruneTraps(int n) {
        int n2 = this.trapTiles.size();
        this.trapTiles.entrySet().removeIf(entry -> n - (Integer)entry.getValue() > 2 && this.findBoxTrapAt((WorldPoint)entry.getKey()) == null);
        if (this.trapTiles.size() < n2) {
            this.layBlockedUntilCollect = false;
        }
        if (this.trapTiles.isEmpty()) {
            this.clusterAnchor = null;
            this.trapCapReached = false;
        }
    }

    private Integer handleFinishedTraps(WorldPoint worldPoint, int n) {
        for (Map.Entry<WorldPoint, Integer> entry : new ArrayList<Map.Entry<WorldPoint, Integer>>(this.trapTiles.entrySet())) {
            WorldPoint worldPoint2 = entry.getKey();
            TileObject tileObject = this.findBoxTrapAt(worldPoint2);
            if (tileObject == null || !this.isFinished(tileObject)) continue;
            if (worldPoint2.distanceTo(worldPoint) > 2) {
                this.status = "Walking to trap";
                this.walkTo(worldPoint2);
                return 1000;
            }
            this.status = this.isCaughtId(tileObject.getId()) ? "Checking catch" : "Resetting failed trap";
            MousePackets.queueClickPacket();
            ObjectPackets.tileObjectFirstOption(tileObject, false);
            this.pendingCollectTile = worldPoint2;
            this.pendingCollectTick = n;
            return Rand.nextInt(900, 1300);
        }
        return null;
    }

    private Integer handleDroppedBoxes(WorldPoint worldPoint) {
        TileItem tileItem = this.nearestDroppedBox(worldPoint);
        if (tileItem == null) {
            return null;
        }
        WorldPoint worldPoint2 = tileItem.getWorldLocation();
        if (worldPoint2 == null) {
            return null;
        }
        if (worldPoint2.distanceTo(worldPoint) > 0) {
            this.status = "Recovering dropped box";
            this.walkTo(worldPoint2);
            return 1000;
        }
        this.status = "Picking up box trap";
        MousePackets.queueClickPacket();
        GroundItemPackets.groundItemFirstOption(tileItem, false);
        return Rand.nextInt(700, 1100);
    }

    private TileItem nearestDroppedBox(WorldPoint worldPoint) {
        return TileItems.getNearest(tileItem -> tileItem != null && tileItem.getId() == 10008 && tileItem.getWorldLocation() != null && tileItem.getWorldLocation().distanceTo(worldPoint) <= 16);
    }

    private int manageTraps(WorldPoint worldPoint, int n) {
        int n2;
        this.activeTrapEstimate = n2 = this.countActiveTraps();
        boolean bl = Inventory.contains("Box trap");
        int n3 = this.effectiveMaxBoxes();
        WorldPoint worldPoint2 = this.clusterAnchor != null ? this.clusterAnchor : worldPoint;
        boolean bl2 = this.chinsNear(worldPoint2);
        if (n2 == 0) {
            this.layBlockedUntilCollect = false;
            if (!bl) {
                this.status = "Out of box traps";
                return 2000;
            }
            if (!bl2) {
                NPC nPC;
                if (this.config.walkToChins() && (nPC = this.nearestChin()) != null && nPC.getWorldLocation() != null) {
                    this.status = "Walking to chinchompas";
                    this.clusterAnchor = null;
                    this.blockedTiles.clear();
                    this.walkTo(nPC.getWorldLocation());
                    return 1200;
                }
                this.status = "No chinchompas nearby";
                return 1500;
            }
            return this.doLay(worldPoint, n);
        }
        if (this.layBlockedUntilCollect) {
            this.status = "At box limit (" + n2 + "/" + n3 + ") \u2014 waiting for a catch";
            return Rand.nextInt(900, 1400);
        }
        if (n2 < n3 && bl && bl2) {
            return this.doLay(worldPoint, n);
        }
        this.status = "Waiting for catch (" + n2 + "/" + n3 + " boxes)";
        return Rand.nextInt(900, 1400);
    }

    private int doLay(WorldPoint worldPoint, int n) {
        WorldPoint worldPoint2 = this.pickLayTile(worldPoint);
        if (worldPoint2 == null) {
            this.status = "No free tile to lay";
            return 1000;
        }
        if (!worldPoint.equals((Object)worldPoint2)) {
            this.status = "Moving to lay spot";
            this.walkTo(worldPoint2);
            return 600;
        }
        if (!Inventory.contains("Box trap")) {
            this.status = "Out of box traps";
            return 2000;
        }
        this.layHereRejected = false;
        this.trapLimitHit = false;
        this.pendingLayTile = worldPoint2;
        this.pendingLayTick = n;
        this.status = "Laying box trap";
        Item item = Inventory.getFirst("Box trap");
        if (item != null) {
            item.interact("Lay");
        }
        return Rand.nextInt(900, 1300);
    }

    private WorldPoint pickLayTile(WorldPoint worldPoint) {
        WorldPoint worldPoint2 = this.clusterAnchor != null ? this.clusterAnchor : worldPoint;
        int n = Math.max(1, this.config.clusterRadius());
        int n2 = worldPoint.getPlane();
        WorldPoint worldPoint3 = null;
        int n3 = -1;
        int n4 = Integer.MAX_VALUE;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                WorldPoint worldPoint4 = new WorldPoint(worldPoint2.getX() + i, worldPoint2.getY() + j, n2);
                if (!this.isValidLayTile(worldPoint4)) continue;
                int n5 = this.chinsWithin(worldPoint4, 2);
                int n6 = worldPoint4.distanceTo(worldPoint);
                if (n5 <= n3 && (n5 != n3 || n6 >= n4)) continue;
                n3 = n5;
                n4 = n6;
                worldPoint3 = worldPoint4;
            }
        }
        return worldPoint3;
    }

    private boolean isValidLayTile(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return false;
        }
        if (this.trapTiles.containsKey(worldPoint)) {
            return false;
        }
        if (this.blockedTiles.contains(worldPoint)) {
            return false;
        }
        if (this.pendingLayTile != null && this.pendingLayTile.equals((Object)worldPoint)) {
            return false;
        }
        if (!Reachable.isWalkable(worldPoint)) {
            return false;
        }
        if (this.findBoxTrapAt(worldPoint) != null) {
            return false;
        }
        return !this.config.avoidOtherPlayers() || !this.tileHasOtherPlayer(worldPoint);
    }

    private int countActiveTraps() {
        int n = 0;
        for (WorldPoint worldPoint : this.trapTiles.keySet()) {
            if (this.findBoxTrapAt(worldPoint) == null) continue;
            ++n;
        }
        return n;
    }

    private TileObject findBoxTrapAt(WorldPoint worldPoint) {
        List<TileObject> list = TileObjects.getAt(worldPoint, ANY_OBJECT);
        if (list == null) {
            return null;
        }
        for (TileObject tileObject : list) {
            if (tileObject == null || !this.isBoxTrapId(tileObject.getId())) continue;
            return tileObject;
        }
        return null;
    }

    private boolean isFinished(TileObject tileObject) {
        return tileObject.getId() != 9380 && this.isBoxTrapId(tileObject.getId());
    }

    private boolean isBoxTrapId(int n) {
        for (int n2 : BoxTrapConstants.ALL_TRAP_OBJECT_IDS) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    private boolean isCaughtId(int n) {
        for (int n2 : BoxTrapConstants.CAUGHT_OBJECT_IDS) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    private boolean isTargetChin(NPC nPC) {
        if (nPC == null || nPC.isDead()) {
            return false;
        }
        ChinchompaType chinchompaType = this.config.type();
        String[] stringArray = BoxTrapHunter.parseNames(this.config.npcNameOverride());
        if (stringArray.length > 0) {
            return BoxTrapHunter.matchesAny(nPC.getName(), stringArray);
        }
        if (chinchompaType.npcId() > 0 && nPC.getId() == chinchompaType.npcId()) {
            return true;
        }
        return BoxTrapHunter.matchesAny(nPC.getName(), new String[]{chinchompaType.nameKeyword()});
    }

    private boolean chinsNear(WorldPoint worldPoint) {
        int n;
        NPC nPC;
        WorldPoint worldPoint2;
        if (worldPoint == null) {
            worldPoint2 = this.client.getLocalPlayer();
            if (worldPoint2 == null) {
                return false;
            }
            worldPoint = worldPoint2.getWorldLocation();
        }
        return (nPC = NPCs.getNearest(arg_0 -> this.lambda$chinsNear$0(worldPoint2 = worldPoint, n = this.config.chinSearchRadius(), arg_0))) != null;
    }

    private NPC nearestChin() {
        return NPCs.getNearest(this::isTargetChin);
    }

    private int chinsWithin(WorldPoint worldPoint, int n) {
        List<NPC> list = NPCs.getAll(this::isTargetChin);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int n2 = 0;
        for (NPC nPC : list) {
            WorldPoint worldPoint2 = nPC.getWorldLocation();
            if (worldPoint2 == null || worldPoint2.distanceTo(worldPoint) > n) continue;
            ++n2;
        }
        return n2;
    }

    private void walkTo(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return;
        }
        MousePackets.queueClickPacket();
        MovementPackets.sendMovement(worldPoint);
    }

    private boolean tileHasOtherPlayer(WorldPoint worldPoint) {
        Player player = this.client.getLocalPlayer();
        List<Player> list = Players.getAll(player2 -> player2 != null && player2 != player && player2.getWorldLocation() != null && player2.getWorldLocation().equals((Object)worldPoint));
        return list != null && !list.isEmpty();
    }

    private int effectiveMaxBoxes() {
        if (this.trapCapReached) {
            return Math.min(this.config.maxTraps(), Math.max(1, this.trapCapValue));
        }
        return this.config.maxTraps();
    }

    private int hunterLevel() {
        try {
            return this.client.getRealSkillLevel(Skill.HUNTER);
        }
        catch (Throwable throwable) {
            return this.lastHunterLevel;
        }
    }

    private static String[] parseNames(String string) {
        if (string == null || string.trim().isEmpty()) {
            return new String[0];
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : string.split(",")) {
            String string3 = string2.trim().toLowerCase();
            if (string3.isEmpty()) continue;
            arrayList.add(string3);
        }
        return arrayList.toArray(new String[0]);
    }

    private static boolean matchesAny(String string, String[] stringArray) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : stringArray) {
            if (string3.isEmpty() || !string2.contains(string3)) continue;
            return true;
        }
        return false;
    }

    private static int parseFirstInt(String string) {
        if (string == null) {
            return 0;
        }
        int n = string.length();
        for (int i = 0; i < n; ++i) {
            char c = string.charAt(i);
            if (c < '0' || c > '9') continue;
            int n2 = 0;
            while (i < n && string.charAt(i) >= '0' && string.charAt(i) <= '9') {
                n2 = n2 * 10 + (string.charAt(i) - 48);
                ++i;
            }
            return n2;
        }
        return 0;
    }

    String getStatus() {
        return this.status;
    }

    int getActiveTraps() {
        return Math.max(this.activeTrapEstimate, this.trapTiles.size());
    }

    int getEffectiveMaxBoxes() {
        return this.effectiveMaxBoxes();
    }

    int getCaught() {
        return this.caught;
    }

    int getLaid() {
        return this.laid;
    }

    private /* synthetic */ boolean lambda$chinsNear$0(WorldPoint worldPoint, int n, NPC nPC) {
        return this.isTargetChin(nPC) && nPC.getWorldLocation() != null && nPC.getWorldLocation().distanceTo(worldPoint) <= n;
    }
}

