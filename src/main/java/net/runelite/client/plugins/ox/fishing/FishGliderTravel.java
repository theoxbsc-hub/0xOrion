package net.runelite.client.plugins.ox.fishing;

import java.util.Locale;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.widgets.Dialog;

/**
 * Startup path when at home: ::home (if far) -> walk Gnome glider -> Travel -> search dest.
 * <p>
 * Search/select match x64 Slayer / {@code GliderTeleporter}:
 * left-click Search @ 75,305 -> type query (Keyboard, like {@code HumanInput.command})
 * -> left-click result @ 220,90 -> confirm dialog.
 * <p>
 * Coordinates are scaled from the classic 765x503 canvas so Mac / stretched clients work.
 * Do NOT gate on {@link Dialog#isEnterInputOpen()} — Orion glider search is not that dialog
 * (that gate caused an infinite "clicking Search widget" loop).
 */
final class FishGliderTravel {
    static final WorldPoint GLIDER_TILE = new WorldPoint(2795, 3374, 0);
    static final WorldPoint HOME_CENTER = new WorldPoint(2785, 3382, 0);
    static final String GLIDER_NAME = "Gnome glider";
    static final String DEFAULT_DEST = "seabright pier";

    /** Classic fixed-mode canvas used by x64 hardcoded click coords. */
    private static final int BASE_CANVAS_W = 765;
    private static final int BASE_CANVAS_H = 503;
    private static final int SEARCH_BTN_X = 75;
    private static final int SEARCH_BTN_Y = 305;
    private static final int SELECT_BTN_X = 220;
    private static final int SELECT_BTN_Y = 90;

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
    private int cycleAttempts = 0;
    private boolean clearedChatBeforeTravel = false;
    private String status = "Travel: starting";

    void begin() {
        this.phase = Phase.WALK;
        this.phaseAtMs = System.currentTimeMillis();
        this.preClickPos = null;
        this.lastWalkFrom = null;
        this.lastIssuedWaypoint = null;
        this.homeAttempts = 0;
        this.walkStallCount = 0;
        this.cycleAttempts = 0;
        this.clearedChatBeforeTravel = false;
        this.status = "Travel: starting";
    }

    String status() {
        return this.status;
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
                    this.clearedChatBeforeTravel = false;
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
                if (!this.clearedChatBeforeTravel) {
                    this.clearedChatBeforeTravel = true;
                    pressEscape();
                    this.status = "Travel: clearing chat focus";
                    return 400;
                }
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
                // x64 Slayer: fixed left-click Search — no widget scan (false positives loop forever)
                int[] xy = scaleClick(SEARCH_BTN_X, SEARCH_BTN_Y);
                this.status = "Travel: Search click @ " + xy[0] + "," + xy[1];
                Mouse.click(xy[0], xy[1], true);
                this.phase = Phase.TYPE;
                this.phaseAtMs = now;
                return 3000;
            }
            case TYPE: {
                // x64 HumanInput.command: type into whatever Search focused (NOT Dialog.enterText)
                this.status = "Travel: typing '" + dest + "'";
                typeCommand(dest);
                this.phase = Phase.SELECT;
                this.phaseAtMs = now;
                return 1200;
            }
            case SELECT: {
                this.preClickPos = here;
                int[] xy = scaleClick(SELECT_BTN_X, SELECT_BTN_Y);
                this.status = "Travel: select result @ " + xy[0] + "," + xy[1];
                Mouse.click(xy[0], xy[1], true);
                this.phase = Phase.VERIFY;
                this.phaseAtMs = now;
                return 1500;
            }
            case VERIFY: {
                if (Dialog.isOpen() && Dialog.isViewingOptions()) {
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
                    ++this.cycleAttempts;
                    this.status = "Travel: teleport timeout - retry (" + this.cycleAttempts + ")";
                    this.phase = Phase.TRAVEL;
                    this.clearedChatBeforeTravel = true;
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

    /**
     * Scale x64 fixed-mode coords to the live canvas (Mac / stretched clients).
     */
    private static int[] scaleClick(int baseX, int baseY) {
        Client client = clientOrNull();
        int canvasW = BASE_CANVAS_W;
        int canvasH = BASE_CANVAS_H;
        if (client != null) {
            try {
                int w = client.getCanvasWidth();
                int h = client.getCanvasHeight();
                if (w > 0 && h > 0) {
                    canvasW = w;
                    canvasH = h;
                }
            } catch (Throwable ignored) {
                // keep defaults
            }
        }
        int x = (int) Math.round(baseX * (canvasW / (double) BASE_CANVAS_W));
        int y = (int) Math.round(baseY * (canvasH / (double) BASE_CANVAS_H));
        return new int[] {x, y};
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
                    if (a != null && a.toLowerCase(Locale.ROOT).contains(action.toLowerCase(Locale.ROOT))) {
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
                && obj.getName().toLowerCase(Locale.ROOT).contains("glider"));
        if (byName != null) {
            return byName;
        }
        return TileObjects.getNearest(
            obj -> obj != null
                && obj.getWorldLocation() != null
                && obj.getWorldLocation().distanceTo(GLIDER_TILE) <= 3
        );
    }

    /** Same as x64 HumanInput.command: char-by-char then Enter. */
    private static void typeCommand(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            for (int i = 0; i < text.length(); i++) {
                Keyboard.type(text.charAt(i));
            }
            Keyboard.sendEnter();
        } catch (Throwable t) {
            try {
                Keyboard.type(text, true);
            } catch (Throwable ignored) {
                // best-effort
            }
        }
    }

    private static void pressEscape() {
        try {
            Keyboard.typed(27);
        } catch (Throwable ignored) {
            // optional
        }
    }

    private static Client clientOrNull() {
        try {
            return RuneLite.getInjector().getInstance(Client.class);
        } catch (Throwable t) {
            return null;
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
