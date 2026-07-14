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
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.hunter.HunterConstants;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.ObjectPackets;

public final class BirdSnareHunterEngine {
    private final Client client;
    private final HumanInput input;
    private volatile Settings settings;
    private static final Predicate<TileObject> ANY_OBJECT = tileObject -> tileObject != null;
    private static final int LAY_TIMEOUT = 5;
    private final Map<WorldPoint, Integer> trapTiles = new HashMap<WorldPoint, Integer>();
    private final Set<WorldPoint> blockedTiles = new HashSet<WorldPoint>();
    private WorldPoint clusterAnchor;
    private int knownSetId;
    private WorldPoint pendingLayTile;
    private int pendingLayTick;
    private boolean trapCapReached;
    private int trapCapValue;
    private volatile boolean layHereRejected;
    private volatile boolean trapLimitHit;
    private volatile int parsedTrapCap;
    private boolean layBlockedUntilCollect;
    private int activeTrapEstimate;
    private int lastHunterLevel = -1;
    private String status = "Starting";
    private int collected;
    private int dropped;

    public BirdSnareHunterEngine(Client client) {
        this(client, null);
    }

    public BirdSnareHunterEngine(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public void reset() {
        this.trapTiles.clear();
        this.blockedTiles.clear();
        this.clusterAnchor = null;
        this.knownSetId = 0;
        this.pendingLayTile = null;
        this.pendingLayTick = 0;
        this.trapCapReached = false;
        this.trapCapValue = 0;
        this.layHereRejected = false;
        this.trapLimitHit = false;
        this.parsedTrapCap = 0;
        this.layBlockedUntilCollect = false;
        this.activeTrapEstimate = 0;
        this.lastHunterLevel = -1;
        this.status = "Starting";
        this.collected = 0;
        this.dropped = 0;
    }

    public void onSceneLost() {
        this.trapTiles.clear();
        this.blockedTiles.clear();
        this.clusterAnchor = null;
        this.pendingLayTile = null;
        this.layBlockedUntilCollect = false;
        this.activeTrapEstimate = 0;
        this.status = "Logged out";
    }

    public void onChatMessage(String string) {
        if (string == null) {
            return;
        }
        if (string.contains("can't lay a trap here") || string.contains("cannot lay a trap here") || string.contains("can't set a trap here") || string.contains("cannot set a trap here") || string.contains("can't put a trap here")) {
            this.layHereRejected = true;
        }
        if (string.contains("trap") && (string.contains("only set") || string.contains("only lay") || string.contains("only have") || string.contains("only use") || string.contains("only place") || string.contains("at a time") || string.contains("up to") || string.contains("more trap") || string.contains("hunter level") || string.contains("hunting level") || string.contains("higher level"))) {
            this.trapLimitHit = true;
            int n = BirdSnareHunterEngine.parseFirstInt(string);
            if (n > 0 && n <= 10) {
                this.parsedTrapCap = n;
            }
        }
    }

    public int tick(Player player, Settings settings) {
        Integer n;
        this.settings = settings;
        int n2 = this.client.getTickCount();
        WorldPoint worldPoint = player.getWorldLocation();
        int n3 = this.hunterLevel();
        if (this.lastHunterLevel >= 0 && n3 > this.lastHunterLevel) {
            this.trapCapReached = false;
            this.trapCapValue = 0;
            this.parsedTrapCap = 0;
            this.layBlockedUntilCollect = false;
        }
        this.lastHunterLevel = n3;
        if (this.knownSetId == 0) {
            this.knownSetId = 9345;
        }
        if ((n = this.handlePendingLay(n2)) != null) {
            return n;
        }
        this.pruneTraps(n2);
        Integer n4 = this.handleTraps(worldPoint, n2);
        if (n4 != null) {
            return n4;
        }
        if (Movement.isWalking()) {
            this.status = "Moving";
            return 400;
        }
        Integer n5 = this.pickUpFallenSnare(worldPoint);
        if (n5 != null) {
            return n5;
        }
        List<Item> list = Inventory.getAll(this::shouldDrop);
        if (!list.isEmpty()) {
            this.status = "Dropping loot (" + list.size() + ")";
            for (Item item : list) {
                if (this.input != null) {
                    this.input.itemInteractVia(item, "Drop");
                } else {
                    item.interact("Drop");
                }
                ++this.dropped;
                if (Time.sleep(Rand.nextInt(70, 130))) continue;
                break;
            }
            return 400;
        }
        return this.manageCluster(worldPoint, n2);
    }

    private Integer handlePendingLay(int n) {
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
        if (this.trapLimitHit && this.findSnareObjectAt(this.pendingLayTile) == null) {
            this.trapLimitHit = false;
            this.trapCapReached = true;
            this.trapCapValue = this.parsedTrapCap > 0 ? this.parsedTrapCap : Math.max(1, this.trapTiles.size());
            this.layBlockedUntilCollect = true;
            this.pendingLayTile = null;
            this.status = "At trap limit";
            return 300;
        }
        TileObject tileObject = this.findSnareObjectAt(this.pendingLayTile);
        boolean bl2 = bl = tileObject != null;
        if (bl && this.isIdle()) {
            this.trapTiles.put(this.pendingLayTile, this.pendingLayTick);
            if (this.clusterAnchor == null) {
                this.clusterAnchor = this.pendingLayTile;
            }
            if (this.knownSetId == 0 && !this.isCaughtId(tileObject.getId())) {
                this.knownSetId = tileObject.getId();
            }
            this.pendingLayTile = null;
            return null;
        }
        if (n - this.pendingLayTick <= 5) {
            this.status = "Laying snare...";
            return 300;
        }
        if (bl) {
            this.trapTiles.put(this.pendingLayTile, this.pendingLayTick);
            if (this.clusterAnchor == null) {
                this.clusterAnchor = this.pendingLayTile;
            }
            if (this.knownSetId == 0 && !this.isCaughtId(tileObject.getId())) {
                this.knownSetId = tileObject.getId();
            }
            this.pendingLayTile = null;
            return null;
        }
        this.trapCapReached = true;
        this.trapCapValue = this.trapTiles.size();
        this.pendingLayTile = null;
        return 400;
    }

    private void pruneTraps(int n) {
        int n2 = this.trapTiles.size();
        this.trapTiles.entrySet().removeIf(entry -> n - (Integer)entry.getValue() > 3 && this.findSnareObjectAt((WorldPoint)entry.getKey()) == null);
        if (this.trapTiles.size() < n2) {
            this.layBlockedUntilCollect = false;
        }
        if (this.trapTiles.isEmpty()) {
            this.clusterAnchor = null;
            this.trapCapReached = false;
        }
    }

    private Integer handleTraps(WorldPoint worldPoint, int n) {
        for (Map.Entry<WorldPoint, Integer> entry : new ArrayList<Map.Entry<WorldPoint, Integer>>(this.trapTiles.entrySet())) {
            WorldPoint worldPoint2 = entry.getKey();
            TileObject tileObject = this.findSnareObjectAt(worldPoint2);
            if (tileObject == null) continue;
            if (this.knownSetId == 0 && !this.isCaughtId(tileObject.getId()) && n - entry.getValue() <= 3) {
                this.knownSetId = tileObject.getId();
            }
            if (!this.isSprung(tileObject)) continue;
            if (worldPoint2.distanceTo(worldPoint) > 2) {
                this.status = "Walking to trap";
                this.walkTo(worldPoint2);
                return 1000;
            }
            this.status = "Checking trap";
            if (this.input != null) {
                this.input.interactObjectFirst(tileObject);
            } else {
                MousePackets.queueClickPacket();
                ObjectPackets.tileObjectFirstOption(tileObject, false);
            }
            ++this.collected;
            this.layBlockedUntilCollect = false;
            return Rand.nextInt(1000, 1400);
        }
        return null;
    }

    private Integer pickUpFallenSnare(WorldPoint worldPoint) {
        TileItem tileItem = TileItems.getNearest("Bird snare");
        if (tileItem == null) {
            return null;
        }
        WorldPoint worldPoint2 = tileItem.getWorldLocation();
        if (worldPoint2 == null || worldPoint == null) {
            return null;
        }
        WorldPoint worldPoint3 = this.clusterAnchor != null ? this.clusterAnchor : worldPoint;
        int n = Math.max(2, this.settings.clusterRadius + this.settings.birdSearchRadius);
        if (worldPoint3 != null && worldPoint2.distanceTo(worldPoint3) > n) {
            return null;
        }
        if (worldPoint2.distanceTo(worldPoint) > 1) {
            this.status = "Walking to fallen snare";
            this.walkTo(worldPoint2);
            return 1000;
        }
        this.status = "Picking up snare";
        if (this.input != null) {
            this.input.interactGroundItem(tileItem, "Take");
        } else {
            tileItem.interact("Take");
        }
        return Rand.nextInt(700, 1100);
    }

    private int manageCluster(WorldPoint worldPoint, int n) {
        WorldPoint worldPoint2;
        int n2;
        this.activeTrapEstimate = n2 = this.countPhysicalTraps(worldPoint);
        boolean bl = Inventory.contains("Bird snare");
        int n3 = this.effectiveMaxTraps();
        if (n2 == 0) {
            WorldPoint worldPoint3;
            this.layBlockedUntilCollect = false;
            if (!bl) {
                this.status = "Out of bird snares";
                return 2000;
            }
            WorldPoint worldPoint4 = worldPoint3 = this.clusterAnchor != null ? this.clusterAnchor : worldPoint;
            if (!this.birdsNear(worldPoint3)) {
                NPC nPC;
                if (this.settings.walkToBirds && (nPC = this.nearestBird()) != null && nPC.getWorldLocation() != null) {
                    this.status = "Walking to birds";
                    this.clusterAnchor = null;
                    this.blockedTiles.clear();
                    this.walkTo(nPC.getWorldLocation());
                    return 1200;
                }
                this.status = "No birds nearby";
                return 1500;
            }
            return this.doLay(worldPoint, n);
        }
        if (this.layBlockedUntilCollect) {
            this.status = "At trap limit (" + n2 + "/" + n3 + ") \u2014 waiting for a catch";
            return Rand.nextInt(900, 1400);
        }
        WorldPoint worldPoint5 = worldPoint2 = this.clusterAnchor != null ? this.clusterAnchor : worldPoint;
        if (n2 < n3 && bl && this.birdsNear(worldPoint2)) {
            return this.doLay(worldPoint, n);
        }
        this.status = "Waiting for catch (" + n2 + "/" + n3 + " traps)";
        return Rand.nextInt(900, 1400);
    }

    private int countPhysicalTraps(WorldPoint worldPoint) {
        WorldPoint worldPoint2;
        if (this.knownSetId == 0 && this.caughtIds().length == 0) {
            return this.trapTiles.size();
        }
        WorldPoint worldPoint3 = worldPoint2 = this.clusterAnchor != null ? this.clusterAnchor : worldPoint;
        if (worldPoint2 == null) {
            return this.trapTiles.size();
        }
        int n = Math.max(1, this.settings.clusterRadius);
        int n2 = worldPoint2.getPlane();
        HashSet<WorldPoint> hashSet = new HashSet<WorldPoint>(this.trapTiles.keySet());
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                WorldPoint worldPoint4 = new WorldPoint(worldPoint2.getX() + i, worldPoint2.getY() + j, n2);
                if (this.findTrapStateObjectAt(worldPoint4) == null) continue;
                hashSet.add(worldPoint4);
            }
        }
        return hashSet.size();
    }

    private TileObject findTrapStateObjectAt(WorldPoint worldPoint) {
        List<TileObject> list = TileObjects.getAt(worldPoint, ANY_OBJECT);
        if (list == null) {
            return null;
        }
        for (TileObject tileObject : list) {
            if (tileObject == null) continue;
            if (this.knownSetId != 0 && tileObject.getId() == this.knownSetId) {
                return tileObject;
            }
            if (!this.isCaughtId(tileObject.getId())) continue;
            return tileObject;
        }
        return null;
    }

    private int doLay(WorldPoint worldPoint, int n) {
        WorldPoint worldPoint2 = this.pickLayTile(worldPoint);
        if (worldPoint2 == null) {
            return this.relocateForLay(worldPoint);
        }
        if (!worldPoint.equals((Object)worldPoint2)) {
            this.status = "Moving to lay spot";
            this.walkTo(worldPoint2);
            return 600;
        }
        Item item = Inventory.getFirst("Bird snare");
        if (item == null) {
            this.status = "Out of bird snares";
            return 1500;
        }
        this.layHereRejected = false;
        this.trapLimitHit = false;
        this.pendingLayTile = worldPoint2;
        this.pendingLayTick = n;
        this.status = "Laying snare";
        if (this.input != null) {
            this.input.itemInteractVia(item, "Lay");
        } else {
            item.interact("Lay");
        }
        return Rand.nextInt(900, 1300);
    }

    private int relocateForLay(WorldPoint worldPoint) {
        if (this.clusterAnchor != null || !this.trapTiles.isEmpty()) {
            this.status = "Waiting for catch (no open tile)";
            return Rand.nextInt(900, 1400);
        }
        WorldPoint worldPoint2 = null;
        NPC nPC = this.nearestBird();
        worldPoint2 = nPC != null && nPC.getWorldLocation() != null && nPC.getWorldLocation().distanceTo(worldPoint) > 1 ? nPC.getWorldLocation() : this.nearestOpenTile(worldPoint);
        if (worldPoint2 != null && !worldPoint2.equals((Object)worldPoint)) {
            this.clusterAnchor = null;
            this.blockedTiles.clear();
            this.status = "Relocating to open ground";
            this.walkTo(worldPoint2);
            return Rand.nextInt(900, 1300);
        }
        this.status = "No free tile to lay";
        return 1000;
    }

    private WorldPoint nearestOpenTile(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return null;
        }
        int n = worldPoint.getPlane();
        for (int i = 2; i <= 6; ++i) {
            WorldPoint worldPoint2 = null;
            int n2 = Integer.MAX_VALUE;
            for (int j = -i; j <= i; ++j) {
                for (int k = -i; k <= i; ++k) {
                    int n3;
                    WorldPoint worldPoint3;
                    if (Math.max(Math.abs(j), Math.abs(k)) != i || TileObjects.getFirstAt(worldPoint3 = new WorldPoint(worldPoint.getX() + j, worldPoint.getY() + k, n), ANY_OBJECT) != null || (n3 = worldPoint3.distanceTo(worldPoint)) >= n2) continue;
                    n2 = n3;
                    worldPoint2 = worldPoint3;
                }
            }
            if (worldPoint2 == null) continue;
            return worldPoint2;
        }
        return null;
    }

    private WorldPoint pickLayTile(WorldPoint worldPoint) {
        if (worldPoint != null && this.isValidLayTile(worldPoint, false)) {
            return worldPoint;
        }
        WorldPoint worldPoint2 = this.bestClusterTile(worldPoint, true);
        if (worldPoint2 == null) {
            worldPoint2 = this.bestClusterTile(worldPoint, false);
        }
        return worldPoint2;
    }

    private WorldPoint bestClusterTile(WorldPoint worldPoint, boolean bl) {
        WorldPoint worldPoint2 = this.clusterAnchor != null ? this.clusterAnchor : worldPoint;
        int n = Math.max(1, this.settings.clusterRadius);
        int n2 = worldPoint.getPlane();
        WorldPoint worldPoint3 = null;
        int n3 = Integer.MAX_VALUE;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                int n4;
                WorldPoint worldPoint4;
                if (i == 0 && j == 0 || !this.isValidLayTile(worldPoint4 = new WorldPoint(worldPoint2.getX() + i, worldPoint2.getY() + j, n2), bl) || (n4 = worldPoint4.distanceTo(worldPoint)) >= n3) continue;
                n3 = n4;
                worldPoint3 = worldPoint4;
            }
        }
        return worldPoint3;
    }

    private boolean isValidLayTile(WorldPoint worldPoint, boolean bl) {
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
        if (bl && !this.isWalkableSafe(worldPoint)) {
            return false;
        }
        return TileObjects.getFirstAt(worldPoint, ANY_OBJECT) == null;
    }

    private boolean isWalkableSafe(WorldPoint worldPoint) {
        try {
            return Reachable.isWalkable(worldPoint);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private TileObject findSnareObjectAt(WorldPoint worldPoint) {
        List<TileObject> list = TileObjects.getAt(worldPoint, ANY_OBJECT);
        if (list == null || list.isEmpty()) {
            return null;
        }
        TileObject tileObject = null;
        for (TileObject tileObject2 : list) {
            if (tileObject2 == null) continue;
            tileObject = tileObject2;
            if (tileObject2.getId() == 9345) {
                return tileObject2;
            }
            if (!this.isCaughtId(tileObject2.getId())) continue;
            return tileObject2;
        }
        return tileObject;
    }

    private boolean isSprung(TileObject tileObject) {
        if (this.isCaughtId(tileObject.getId())) {
            return true;
        }
        return this.knownSetId != 0 && tileObject.getId() != this.knownSetId;
    }

    private boolean isCaughtId(int n) {
        for (int n2 : this.caughtIds()) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    private boolean birdsNear(WorldPoint worldPoint) {
        WorldPoint worldPoint2;
        if (worldPoint == null) {
            worldPoint2 = this.client.getLocalPlayer();
            if (worldPoint2 == null) {
                return false;
            }
            worldPoint = worldPoint2.getWorldLocation();
        }
        worldPoint2 = worldPoint;
        int n = this.settings.birdSearchRadius;
        String[] stringArray = BirdSnareHunterEngine.parseNames(this.settings.birdNames);
        if (stringArray.length == 0) {
            return true;
        }
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && BirdSnareHunterEngine.matchesAny(nPC.getName(), stringArray) && nPC.getWorldLocation() != null && nPC.getWorldLocation().distanceTo(worldPoint2) <= n);
        return nPC2 != null;
    }

    private NPC nearestBird() {
        String[] stringArray = BirdSnareHunterEngine.parseNames(this.settings.birdNames);
        if (stringArray.length == 0) {
            return null;
        }
        return NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && BirdSnareHunterEngine.matchesAny(nPC.getName(), stringArray));
    }

    private boolean shouldDrop(Item item) {
        if (item == null) {
            return false;
        }
        if (BirdSnareHunterEngine.isJunkId(item.getId())) {
            return true;
        }
        String string = item.getName();
        if (string == null) {
            return false;
        }
        if (this.isProtected(string)) {
            return false;
        }
        return BirdSnareHunterEngine.matchesAny(string, BirdSnareHunterEngine.parseNames(this.settings.dropItems));
    }

    private static boolean isJunkId(int n) {
        for (int n2 : HunterConstants.JUNK_ITEM_IDS) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    private boolean isProtected(String string) {
        String string2 = string.toLowerCase();
        String string3 = "Bird snare".trim().toLowerCase();
        if (!string3.isEmpty() && string2.contains(string3)) {
            return true;
        }
        for (String string4 : BirdSnareHunterEngine.parseNames(this.settings.keepItems)) {
            if (string4.isEmpty() || !string2.contains(string4)) continue;
            return true;
        }
        return false;
    }

    public boolean hasTrapsOut() {
        if (!this.trapTiles.isEmpty()) {
            return true;
        }
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && (tileObject.getId() == 9345 || this.isCaughtId(tileObject.getId())));
        return tileObject2 != null;
    }

    public int recallStep(Player player) {
        if (player == null) {
            return 600;
        }
        int n = this.client.getTickCount();
        WorldPoint worldPoint = player.getWorldLocation();
        if (player.getAnimation() != -1) {
            this.status = "Recalling snares...";
            return Rand.nextInt(300, 500);
        }
        if (Movement.isWalking()) {
            this.status = "Recalling snares...";
            return 400;
        }
        this.pruneTraps(n);
        WorldPoint worldPoint2 = null;
        TileObject tileObject2 = null;
        TileObject tileObject3 = new ArrayList<WorldPoint>(this.trapTiles.keySet()).iterator();
        while (tileObject3.hasNext()) {
            WorldPoint worldPoint3 = tileObject3.next();
            TileObject tileObject4 = this.findSnareObjectAt(worldPoint3);
            if (tileObject4 != null) {
                worldPoint2 = worldPoint3;
                tileObject2 = tileObject4;
                break;
            }
            this.trapTiles.remove(worldPoint3);
        }
        if (tileObject2 == null && (tileObject3 = TileObjects.getNearest(tileObject -> tileObject != null && (tileObject.getId() == 9345 || this.isCaughtId(tileObject.getId())))) != null) {
            tileObject2 = tileObject3;
            worldPoint2 = tileObject3.getWorldLocation();
        }
        if (tileObject2 == null) {
            this.status = "All snares recalled";
            return -1;
        }
        if (worldPoint2 != null && worldPoint != null && worldPoint2.distanceTo(worldPoint) > 1) {
            this.status = "Walking to snare";
            this.walkTo(worldPoint2);
            return 1000;
        }
        this.status = "Recalling snare";
        if (this.input != null) {
            this.input.interactObjectFirst(tileObject2);
        } else {
            MousePackets.queueClickPacket();
            ObjectPackets.tileObjectFirstOption(tileObject2, false);
        }
        if (worldPoint2 != null) {
            this.trapTiles.remove(worldPoint2);
        }
        return Rand.nextInt(900, 1300);
    }

    private void walkTo(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return;
        }
        if (this.input != null) {
            this.input.walkToward(this.client.getLocalPlayer().getWorldLocation(), worldPoint);
        } else {
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(worldPoint);
        }
    }

    private boolean isIdle() {
        Player player = this.client.getLocalPlayer();
        return player != null && player.getAnimation() == -1;
    }

    private int effectiveMaxTraps() {
        int n;
        int n2 = n = this.settings != null ? this.settings.maxTraps : 0;
        if (this.trapCapReached) {
            return Math.min(n, Math.max(1, this.trapCapValue));
        }
        return n;
    }

    private int hunterLevel() {
        try {
            return this.client.getRealSkillLevel(Skill.HUNTER);
        }
        catch (Throwable throwable) {
            return this.lastHunterLevel;
        }
    }

    private int[] caughtIds() {
        return HunterConstants.CAUGHT_OBJECT_IDS;
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

    public String getStatus() {
        return this.status;
    }

    public int getActiveTraps() {
        return Math.max(this.activeTrapEstimate, this.trapTiles.size());
    }

    public int getEffectiveMaxTraps() {
        return this.effectiveMaxTraps();
    }

    public int getCollected() {
        return this.collected;
    }

    public int getDropped() {
        return this.dropped;
    }

    public static final class Settings {
        public final String birdNames;
        public final int maxTraps;
        public final int clusterRadius;
        public final int birdSearchRadius;
        public final boolean walkToBirds;
        public final String dropItems;
        public final String keepItems;

        public Settings(String string, int n, int n2, int n3, boolean bl, String string2, String string3) {
            this.birdNames = string;
            this.maxTraps = n;
            this.clusterRadius = n2;
            this.birdSearchRadius = n3;
            this.walkToBirds = bl;
            this.dropItems = string2;
            this.keepItems = string3;
        }
    }
}

