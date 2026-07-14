package net.runelite.client.plugins.ox.woodcutting;

import java.util.Random;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.widgets.Dialog;

/**
 * Startup path: ::home (if far) → walk Gnome glider → Travel → search destination.
 * Walking matches x64 AIO/Slayer packet-step style (not Walker walkTo).
 */
final class WcGliderTravel {
    static final WorldPoint GLIDER_TILE = new WorldPoint(2795, 3374, 0);
    static final WorldPoint HOME_CENTER = new WorldPoint(2785, 3382, 0);
    static final String GLIDER_NAME = "Gnome glider";
    static final String DEFAULT_DEST = "Foresters Grove";

    private static final int ARRIVE_DIST = 5;
    private static final int HOME_IF_FARTHER_THAN = 60;
    private static final int STEP_TILES = 8;

    private final Random rng = new Random();
    private Phase phase = Phase.WALK;
    private long phaseAtMs = 0L;
    private WorldPoint preClickPos;
    private WorldPoint lastWalkFrom;
    private WorldPoint lastIssuedWaypoint;
    private int homeAttempts = 0;
    private int walkStallCount = 0;
    private String status = "Travel: starting";

    void begin() {
        this.phase = Phase.WALK;
        this.phaseAtMs = System.currentTimeMillis();
        this.preClickPos = null;
        this.lastWalkFrom = null;
        this.lastIssuedWaypoint = null;
        this.homeAttempts = 0;
        this.walkStallCount = 0;
        this.status = "Travel: starting";
    }

    String status() {
        return this.status;
    }

    String phaseName() {
        return this.phase.name();
    }

    /**
     * @return delay ms, or {@link Integer#MIN_VALUE} when arrived
     */
    int step(Player player, String destination) {
        if (player == null || player.getWorldLocation() == null) {
            this.status = "Travel: waiting for player";
            return 600;
        }
        WorldPoint here = player.getWorldLocation();
        long now = System.currentTimeMillis();
        String dest = destination == null || destination.trim().isEmpty() ? DEFAULT_DEST : destination.trim();

        switch (this.phase) {
            case WALK: {
                TileObject visible = findGlider();
                if (here.distanceTo(GLIDER_TILE) <= ARRIVE_DIST
                    || (visible != null && visible.getWorldLocation() != null
                        && here.distanceTo(visible.getWorldLocation()) <= ARRIVE_DIST)) {
                    this.homeAttempts = 0;
                    this.walkStallCount = 0;
                    this.phase = Phase.TRAVEL;
                    this.phaseAtMs = now;
                    this.status = "Travel: at glider";
                    return 300;
                }

                if (here.distanceTo(GLIDER_TILE) > HOME_IF_FARTHER_THAN && this.homeAttempts < 4) {
                    this.status = "Travel: ::home to reach glider (" + (this.homeAttempts + 1) + ")";
                    typeCommand("::home");
                    ++this.homeAttempts;
                    this.lastWalkFrom = null;
                    this.walkStallCount = 0;
                    return 10000;
                }

                maybeEnableRun();
                this.status = "Travel: walking to glider (" + here.distanceTo(GLIDER_TILE) + " tiles)";
                walkToward(here, GLIDER_TILE);
                return 1200;
            }
            case TRAVEL: {
                TileObject glider = findGlider();
                if (glider != null) {
                    this.status = "Travel: clicking Travel";
                    MousePackets.queueClickPacket();
                    if (!safeInteract(glider, "Travel") && !safeInteract(glider, "Glider")) {
                        glider.interact("Travel");
                    }
                    this.phase = Phase.SEARCH;
                    this.phaseAtMs = now;
                    return 6000;
                }
                if (here.distanceTo(GLIDER_TILE) > 2) {
                    this.status = "Travel: walking onto glider tile";
                    walkToward(here, GLIDER_TILE);
                    return 1000;
                }
                this.status = "Travel: looking for Gnome glider";
                return 800;
            }
            case SEARCH: {
                this.status = "Travel: opening search";
                Mouse.click(75, 305, false);
                this.phase = Phase.TYPE;
                this.phaseAtMs = now;
                return 3000;
            }
            case TYPE: {
                this.status = "Travel: typing '" + dest + "'";
                typeCommand(dest);
                this.phase = Phase.SELECT;
                this.phaseAtMs = now;
                return 1200;
            }
            case SELECT: {
                this.preClickPos = here;
                this.status = "Travel: selecting destination";
                Mouse.click(220, 90, false);
                this.phase = Phase.VERIFY;
                this.phaseAtMs = now;
                return 1500;
            }
            case VERIFY: {
                if (Dialog.isOpen()) {
                    this.status = "Travel: confirming dialog";
                    if (!Dialog.chooseOption("Yes, take me there") && !Dialog.chooseOption("Yes")) {
                        Dialog.chooseOption(1);
                    }
                    this.phaseAtMs = now;
                    return 1200;
                }
                if (this.preClickPos != null && here.distanceTo(this.preClickPos) > 15) {
                    this.status = "Travel: arrived at " + dest;
                    this.phase = Phase.WALK;
                    this.preClickPos = null;
                    return Integer.MIN_VALUE;
                }
                if (now - this.phaseAtMs > 6000L) {
                    this.status = "Travel: teleport timeout — retry";
                    this.phase = Phase.TRAVEL;
                    this.phaseAtMs = now;
                    return 300;
                }
                this.status = "Travel: waiting for teleport...";
                return 500;
            }
            default:
                return 600;
        }
    }

    private void walkToward(WorldPoint from, WorldPoint to) {
        if (from == null || to == null) {
            return;
        }
        if (to.equals(this.lastIssuedWaypoint)
            && this.lastWalkFrom != null
            && !from.equals(this.lastWalkFrom)
            && from.getPlane() == this.lastIssuedWaypoint.getPlane()
            && from.distanceTo(this.lastIssuedWaypoint) > 2) {
            this.walkStallCount = 0;
            this.lastWalkFrom = from;
            return;
        }

        if (from.equals(this.lastWalkFrom) && to.equals(GLIDER_TILE)) {
            ++this.walkStallCount;
        } else {
            this.walkStallCount = 0;
        }
        this.lastWalkFrom = from;

        WorldPoint waypoint = nextWaypoint(from, to);
        this.lastIssuedWaypoint = waypoint;

        try {
            MousePackets.queueClickPacket();
            MovementPackets.sendMovement(waypoint);
        } catch (Throwable ignored) {
            // best-effort
        }

        if (this.walkStallCount >= 4) {
            this.walkStallCount = 0;
            try {
                MousePackets.queueClickPacket();
                MovementPackets.sendMovement(to);
            } catch (Throwable ignored) {
                // ignore
            }
        }
    }

    private WorldPoint nextWaypoint(WorldPoint from, WorldPoint to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int chebyshev = Math.max(Math.abs(dx), Math.abs(dy));
        int step = STEP_TILES + this.rng.nextInt(4);
        if (chebyshev <= step) {
            int jx = to.getX() + (this.rng.nextInt(3) - 1);
            int jy = to.getY() + (this.rng.nextInt(3) - 1);
            return new WorldPoint(jx, jy, from.getPlane());
        }
        int nx = from.getX() + Integer.signum(dx) * Math.min(Math.abs(dx), step);
        int ny = from.getY() + Integer.signum(dy) * Math.min(Math.abs(dy), step);
        int drift = this.rng.nextInt(5) - 2;
        if (Math.abs(dx) >= Math.abs(dy)) {
            ny += drift;
        } else {
            nx += drift;
        }
        return new WorldPoint(nx, ny, from.getPlane());
    }

    private static void maybeEnableRun() {
        try {
            if (Movement.getRunEnergy() > 20 && !Movement.isRunEnabled()) {
                Movement.toggleRun();
            }
        } catch (Throwable ignored) {
            // optional
        }
    }

    private static boolean safeInteract(TileObject obj, String action) {
        try {
            String[] actions = obj.getActions();
            if (actions != null) {
                for (String a : actions) {
                    if (a != null && a.equalsIgnoreCase(action)) {
                        obj.interact(action);
                        return true;
                    }
                }
                for (String a : actions) {
                    if (a != null && a.toLowerCase().contains(action.toLowerCase())) {
                        obj.interact(a);
                        return true;
                    }
                }
            }
            obj.interact(action);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static TileObject findGlider() {
        TileObject byName = TileObjects.getNearest(GLIDER_NAME);
        if (byName != null) {
            return byName;
        }
        byName = TileObjects.getNearest(obj ->
            obj != null
                && obj.getName() != null
                && obj.getName().toLowerCase().contains("glider"));
        if (byName != null) {
            return byName;
        }
        return TileObjects.getNearest(
            obj -> obj != null
                && obj.getWorldLocation() != null
                && obj.getWorldLocation().distanceTo(GLIDER_TILE) <= 3
        );
    }

    private static void typeCommand(String text) {
        try {
            Keyboard.type(text, true);
        } catch (Throwable t) {
            try {
                for (int i = 0; i < text.length(); i++) {
                    Keyboard.type(text.charAt(i));
                }
                Keyboard.sendEnter();
            } catch (Throwable ignored) {
                // best-effort
            }
        }
    }

    private enum Phase {
        WALK,
        TRAVEL,
        SEARCH,
        TYPE,
        SELECT,
        VERIFY
    }
}
