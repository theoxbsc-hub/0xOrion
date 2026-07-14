/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.CollisionData
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Tile
 *  net.runelite.api.coords.Direction
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.ItemContainerChanged
 *  net.runelite.api.events.VarbitChanged
 *  net.runelite.api.events.WidgetLoaded
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.unethicalite.regions.TileFlag
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.CollisionData;
import net.runelite.api.InventoryID;
import net.runelite.api.Tile;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.unethicalite.regions.TileFlag;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.TeleportLoader;
import net.unethicalite.api.movement.pathfinder.TransportLoader;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.movement.pathfinder.model.Teleport;
import net.unethicalite.api.movement.pathfinder.model.Transport;
import net.unethicalite.api.movement.pathfinder.model.poh.HousePortal;
import net.unethicalite.api.movement.pathfinder.model.poh.JewelryBox;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RegionManager {
    private static final Logger log = LoggerFactory.getLogger(RegionManager.class);
    private static final Set<Integer> REFRESH_WIDGET_IDS = Set.of(Integer.valueOf(WidgetInfo.QUEST_COMPLETED_NAME_TEXT.getGroupId()), Integer.valueOf(WidgetInfo.LEVEL_UP_LEVEL.getGroupId()));
    private static final Set<String> pathfinderConfigKeys = Set.of("useTransports", "useTeleports", "avoidWilderness", "usePoh", "hasMountedGlory", "hasMountedDigsitePendant", "hasMountedMythicalCape", "hasMountedXericsTalisman", "hasJewelryBox", "housePortals", "useEquipmentJewellery", "useMinigameTeleports");
    private static final Set<Integer> REFRESH_VARBS = Set.of(Integer.valueOf(4504), Integer.valueOf(4536), Integer.valueOf(4525), Integer.valueOf(10449), Integer.valueOf(10450), Integer.valueOf(3637), Integer.valueOf(4897), Integer.valueOf(8063));
    private static boolean REFRESH_PATH = false;
    private static boolean INITIAL_LOGIN = true;
    @Inject
    private ScheduledExecutorService executorService;

    public static boolean avoidWilderness() {
        return Static.getUnethicaliteConfig().avoidWilderness();
    }

    public static boolean shouldRefreshPath() {
        boolean refreshPath = REFRESH_PATH;
        REFRESH_PATH = false;
        return refreshPath;
    }

    public static boolean useEquipmentJewellery() {
        return Static.getUnethicaliteConfig().useEquipmentJewellery();
    }

    public static boolean usePoh() {
        return Static.getUnethicaliteConfig().usePoh();
    }

    public static boolean hasMountedGlory() {
        return Static.getUnethicaliteConfig().hasMountedGlory();
    }

    public static boolean hasMountedDigsitePendant() {
        return Static.getUnethicaliteConfig().hasMountedDigsitePendant();
    }

    public static boolean hasMountedMythicalCape() {
        return Static.getUnethicaliteConfig().hasMountedMythicalCape();
    }

    public static boolean hasMountedXericsTalisman() {
        return Static.getUnethicaliteConfig().hasMountedXericsTalisman();
    }

    public static JewelryBox hasJewelryBox() {
        return Static.getUnethicaliteConfig().hasJewelryBox();
    }

    public static boolean useMinigameTeleports() {
        return Static.getUnethicaliteConfig().useMinigameTeleports();
    }

    public static Set<HousePortal> getHousePortals() {
        return Static.getUnethicaliteConfig().housePortals();
    }

    @Inject
    public void init() {
        this.executorService.submit(TransportLoader::init);
        Static.getEventBus().register((Object)this);
    }

    @Subscribe(priority=2.14748365E9f)
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case UNKNOWN: 
            case STARTING: 
            case LOGIN_SCREEN: 
            case LOGIN_SCREEN_AUTHENTICATOR: 
            case CONNECTION_LOST: {
                INITIAL_LOGIN = true;
                break;
            }
            case LOGGED_IN: {
                if (!INITIAL_LOGIN) break;
                INITIAL_LOGIN = false;
                this.executorService.schedule(() -> {
                    REFRESH_PATH = true;
                    TeleportLoader.refreshTeleports();
                    TransportLoader.refreshTransports();
                }, 1000L, TimeUnit.MILLISECONDS);
            }
        }
    }

    public boolean isTransport(List<Transport> transports, WorldPoint from, WorldPoint to) {
        if (transports == null) {
            return false;
        }
        return transports.stream().anyMatch(t -> t.getSource().equals((Object)from) && t.getDestination().equals((Object)to));
    }

    @Subscribe(priority=2.14748365E9f)
    public void onWidgetLoaded(WidgetLoaded event) {
        if (REFRESH_WIDGET_IDS.contains(event.getGroupId())) {
            REFRESH_PATH = this.hasChanged();
        }
    }

    @Subscribe(priority=2.14748365E9f)
    public void onVarChanged(VarbitChanged event) {
        if (REFRESH_VARBS.contains(event.getIndex())) {
            REFRESH_PATH = this.hasChanged();
        }
    }

    @Subscribe(priority=2.14748365E9f)
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
            REFRESH_PATH = this.hasChanged();
        }
        if (event.getContainerId() == InventoryID.EQUIPMENT.getId()) {
            REFRESH_PATH = this.hasChanged();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("unethicalite")) {
            return;
        }
        if (pathfinderConfigKeys.contains(event.getKey())) {
            REFRESH_PATH = true;
            if (Game.isLoggedIn()) {
                TransportLoader.refreshTransports();
                TeleportLoader.refreshTeleports();
            }
        }
    }

    private boolean hasChanged() {
        boolean tranChanged = this.transportsChanged();
        boolean teleChanged = this.teleportsChanged();
        return tranChanged || teleChanged;
    }

    private boolean transportsChanged() {
        List<WorldPoint> path = Walker.remainingPath(Walker.buildPath());
        if (path.isEmpty()) {
            TransportLoader.refreshTransports();
            return false;
        }
        Map<WorldPoint, List<Transport>> previousTransports = Walker.buildTransportLinksOnPath(path);
        TransportLoader.refreshTransports();
        Map<WorldPoint, List<Transport>> currentTransports = Walker.buildTransportLinksOnPath(path);
        for (WorldPoint point : path) {
            List currTran;
            List prevTran = previousTransports.getOrDefault(point, new ArrayList());
            if (prevTran.equals(currTran = (List)currentTransports.getOrDefault(point, new ArrayList()))) continue;
            return true;
        }
        return false;
    }

    private boolean teleportsChanged() {
        List<WorldPoint> path = Walker.remainingPath(Walker.buildPath());
        if (path.isEmpty()) {
            TeleportLoader.refreshTeleports();
            return false;
        }
        LinkedHashMap<WorldPoint, Teleport> previousTeleports = Walker.buildTeleportLinksOnPath(path);
        TeleportLoader.refreshTeleports();
        LinkedHashMap<WorldPoint, Teleport> currentTeleports = Walker.buildTeleportLinksOnPath(path);
        for (WorldPoint point : path) {
            Teleport prevTele = previousTeleports.getOrDefault(point, null);
            Teleport currTele = currentTeleports.getOrDefault(point, null);
            if ((prevTele != null || currTele == null) && (prevTele == null || currTele != null)) continue;
            return true;
        }
        return false;
    }

    public List<TileFlag> getTileFlags() {
        CollisionData[] col = Static.getClient().getCollisionMaps();
        if (col == null) {
            return new ArrayList<TileFlag>();
        }
        ArrayList<TileFlag> tileFlags = new ArrayList<TileFlag>();
        Map<WorldPoint, List<Transport>> transportLinks = Walker.buildTransportLinks();
        int plane = Static.getClient().getPlane();
        CollisionData data = col[plane];
        if (data == null) {
            return new ArrayList<TileFlag>();
        }
        int[][] flags = data.getFlags();
        for (int x = 0; x < flags.length; ++x) {
            for (int y = 0; y < flags.length; ++y) {
                WorldPoint easternTile;
                int tileX = x + Static.getClient().getBaseX();
                int tileY = y + Static.getClient().getBaseY();
                int flag = flags[x][y];
                if (flag == 0xFFFFFF) continue;
                int regionId = tileX >> 6 << 8 | tileY >> 6;
                TileFlag tileFlag = new TileFlag(tileX, tileY, plane, 2359552, regionId);
                Tile tile = Tiles.getAt(tileX, tileY, plane);
                if (tile == null) {
                    tileFlags.add(tileFlag);
                    continue;
                }
                tileFlag.setFlag(flag);
                WorldPoint tileCoords = tile.getWorldLocation();
                WorldPoint northernTile = tileCoords.dy(1);
                if (Reachable.getCollisionFlag(northernTile) == 0xFFFFFF) continue;
                if (Reachable.isObstacle(northernTile) && !Reachable.isWalled(Direction.NORTH, tileFlag.getFlag())) {
                    tileFlag.setFlag(tileFlag.getFlag() + 2);
                }
                if (Reachable.getCollisionFlag(easternTile = tileCoords.dx(1)) == 0xFFFFFF) continue;
                if (Reachable.isObstacle(easternTile) && !Reachable.isWalled(Direction.EAST, tileFlag.getFlag())) {
                    tileFlag.setFlag(tileFlag.getFlag() + 8);
                }
                List<Transport> transports = transportLinks.get(tileCoords);
                if (plane == Static.getClient().getPlane()) {
                    block6: for (Direction direction : Direction.values()) {
                        switch (direction) {
                            case NORTH: {
                                if (!Reachable.hasDoor(tile, direction) && !Reachable.hasDoor(northernTile, Direction.SOUTH) || this.isTransport(transports, tileCoords, northernTile)) continue block6;
                                tileFlag.setFlag(tileFlag.getFlag() - 2);
                                continue block6;
                            }
                            case EAST: {
                                if (!Reachable.hasDoor(tile, direction) && !Reachable.hasDoor(easternTile, Direction.WEST) || this.isTransport(transports, tileCoords, easternTile)) continue block6;
                                tileFlag.setFlag(tileFlag.getFlag() - 8);
                            }
                        }
                    }
                }
                tileFlags.add(tileFlag);
            }
        }
        return tileFlags;
    }
}

