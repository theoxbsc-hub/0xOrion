/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Locatable
 *  net.runelite.api.MenuAction
 *  net.runelite.api.Perspective
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.Tile
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldArea
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.movement;

import java.util.Comparator;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Locatable;
import net.runelite.api.MenuAction;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Movement {
    private static final Logger log = LoggerFactory.getLogger(Movement.class);
    private static final int STAMINA_VARBIT = 25;
    private static final int RUN_VARP = 173;

    public static void setDestination(int sceneX, int sceneY) {
        Static.getClient().setSelectedSceneTileX(sceneX);
        Static.getClient().setSelectedSceneTileY(sceneY);
        Static.getClient().setViewportWalking(true);
    }

    public static WorldPoint getDestination() {
        Client client = Static.getClient();
        if (client.getDestinationX() == 0 && client.getDestinationY() == 0) {
            return null;
        }
        return new WorldPoint(client.getDestinationX() + client.getBaseX(), client.getDestinationY() + client.getBaseY(), client.getPlane());
    }

    public static boolean isWalking() {
        Player local = Players.getLocal();
        WorldPoint destination = Movement.getDestination();
        return local.isMoving() && destination != null && destination.distanceTo((Locatable)local) > 4;
    }

    public static void walk(WorldPoint worldPoint) {
        int sceneY;
        int sceneX;
        Point canv;
        Client client = Static.getClient();
        Player local = client.getLocalPlayer();
        if (local == null) {
            return;
        }
        WorldPoint walkPoint = worldPoint;
        Tile destinationTile = Tiles.getAt(worldPoint);
        if (destinationTile == null) {
            log.debug("Destination {} is not in scene", (Object)worldPoint);
            Tile nearestInScene = Tiles.getAll().stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(local.getWorldLocation()))).orElse(null);
            if (nearestInScene == null) {
                log.debug("Couldn't find nearest walkable tile");
                return;
            }
            walkPoint = nearestInScene.getWorldLocation();
        }
        int x2 = (canv = Perspective.localToCanvas((Client)client, (LocalPoint)LocalPoint.fromScene((int)(sceneX = walkPoint.getX() - client.getBaseX()), (int)(sceneY = walkPoint.getY() - client.getBaseY())), (int)client.getPlane())) != null ? canv.getX() : -1;
        int y = canv != null ? canv.getY() : -1;
        client.interact(0, MenuAction.WALK.getId(), sceneX, sceneY, x2, y);
    }

    public static boolean walkTo(WorldArea worldArea) {
        return Walker.walkTo(worldArea);
    }

    public static void walk(Locatable locatable) {
        Movement.walk(locatable.getWorldLocation());
    }

    public static boolean walkTo(WorldPoint worldPoint) {
        return Walker.walkTo(worldPoint);
    }

    public static boolean walkTo(Locatable locatable) {
        return Movement.walkTo(locatable.getWorldLocation());
    }

    public static boolean walkTo(BankLocation bankLocation) {
        return Movement.walkTo(bankLocation.getArea());
    }

    public static boolean walkTo(int x, int y) {
        return Movement.walkTo(x, y, Static.getClient().getPlane());
    }

    public static boolean walkTo(int x, int y, int plane) {
        return Movement.walkTo(new WorldPoint(x, y, plane));
    }

    public static boolean isRunEnabled() {
        return Vars.getVarp(173) == 1;
    }

    public static void toggleRun() {
        Widget widget = Widgets.get(WidgetInfo.MINIMAP_TOGGLE_RUN_ORB);
        if (widget != null) {
            widget.interact("Toggle Run");
        }
    }

    public static boolean isStaminaBoosted() {
        return Vars.getBit(25) == 1;
    }

    public static int getRunEnergy() {
        return Static.getClient().getEnergy() / 100;
    }

    public static int calculateDistance(WorldArea destination) {
        return Walker.calculatePath(destination).size();
    }

    public static int calculateDistance(WorldPoint start, WorldArea destination) {
        return Movement.calculateDistance(List.of(start), destination);
    }

    public static int calculateDistance(List<WorldPoint> start, WorldArea destination) {
        return Walker.calculatePath(start, destination).size();
    }

    public static int calculateDistance(WorldPoint destination) {
        return Movement.calculateDistance(destination.toWorldArea());
    }

    public static int calculateDistance(WorldPoint start, WorldPoint destination) {
        return Movement.calculateDistance(start, destination.toWorldArea());
    }

    public static int calculateDistance(List<WorldPoint> start, WorldPoint destination) {
        return Movement.calculateDistance(start, destination.toWorldArea());
    }
}

