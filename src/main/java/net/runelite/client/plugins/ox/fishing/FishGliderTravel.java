package net.runelite.client.plugins.ox.fishing;

import java.awt.Rectangle;
import java.util.Locale;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
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
 * Search/select mirrors x64 Slayer / {@code GliderTeleporter} coords, but prefers widgets and
 * {@link Dialog#enterText(String)} so we never dump the destination into chat (common Mac fail
 * when fixed canvas clicks miss Search and {@code Keyboard.type} hits the chatbox).
 */
final class FishGliderTravel {
    static final WorldPoint GLIDER_TILE = new WorldPoint(2795, 3374, 0);
    static final WorldPoint HOME_CENTER = new WorldPoint(2785, 3382, 0);
    static final String GLIDER_NAME = "Gnome glider";
    static final String DEFAULT_DEST = "seabright pier";

    private static final int ARRIVE_DIST = 5;
    private static final int HOME_IF_FARTHER_THAN = 60;
    private static final int STEP_TILES = 8;
    private static final int SEARCH_BTN_X = 75;
    private static final int SEARCH_BTN_Y = 305;
    private static final int SELECT_BTN_X = 220;
    private static final int SELECT_BTN_Y = 90;

    private final Random rng = new Random();
    private Phase phase = Phase.WALK;
    private long phaseAtMs = 0L;
    private WorldPoint preClickPos;
    private WorldPoint lastWalkFrom;
    private WorldPoint lastIssuedWaypoint;
    private int homeAttempts = 0;
    private int walkStallCount = 0;
    private int searchMisses = 0;
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
        this.searchMisses = 0;
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
                    typeChatCommand("::home");
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
                // ::home leaves chat focused — clear once before interacting with glider UI
                if (!this.clearedChatBeforeTravel) {
                    this.clearedChatBeforeTravel = true;
                    pressEscape();
                    this.status = "Travel: closing chat before glider";
                    return 400;
                }
                TileObject glider = findGlider();
                if (glider != null) {
                    this.status = "Travel: clicking Travel";
                    MousePackets.queueClickPacket();
                    if (!safeInteract(glider, "Travel") && !safeInteract(glider, "Glider")) {
                        glider.interact("Travel");
                    }
                    this.searchMisses = 0;
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
                // Already ready? (Moons / some builds open enter-input after Travel)
                if (Dialog.isEnterInputOpen()) {
                    this.status = "Travel: search input already open";
                    this.phase = Phase.TYPE;
                    this.phaseAtMs = now;
                    return 300;
                }

                Widget search = findVisibleWidget("Search", true);
                if (search != null) {
                    this.status = "Travel: clicking Search widget";
                    clickWidget(search);
                } else {
                    // x64 Slayer fallback coords — Mac canvas / scaling often misses these
                    this.status = "Travel: clicking Search @ " + SEARCH_BTN_X + "," + SEARCH_BTN_Y;
                    Mouse.click(SEARCH_BTN_X, SEARCH_BTN_Y, true);
                }
                this.phase = Phase.TYPE;
                this.phaseAtMs = now;
                return 3000;
            }
            case TYPE: {
                if (!Dialog.isEnterInputOpen()) {
                    ++this.searchMisses;
                    if (this.searchMisses >= 4) {
                        this.searchMisses = 0;
                        this.phase = Phase.TRAVEL;
                        this.clearedChatBeforeTravel = true;
                        this.status = "Travel: search input missing - reopen Travel";
                        return 600;
                    }
                    this.phase = Phase.SEARCH;
                    this.status = "Travel: waiting for search input (not typing into chat)";
                    return 800;
                }

                this.searchMisses = 0;
                this.status = "Travel: typing '" + dest + "' into search";
                // Moons/x64-correct: enter-input packet, NEVER Keyboard.type (that hits chat)
                try {
                    Dialog.enterText(dest);
                } catch (Throwable t) {
                    try {
                        Dialog.enterName(dest);
                    } catch (Throwable ignored) {
                        // last resort only when enter-input is open
                        typeIntoFocusedField(dest);
                    }
                }
                this.phase = Phase.SELECT;
                this.phaseAtMs = now;
                return 1500;
            }
            case SELECT: {
                this.preClickPos = here;
                Widget result = findDestinationResult(dest);
                if (result != null) {
                    this.status = "Travel: clicking result '" + stripTags(result.getText()) + "'";
                    clickWidget(result);
                } else if (Dialog.isViewingOptions()) {
                    this.status = "Travel: choosing dialog option";
                    if (!Dialog.chooseOption(dest)
                        && !Dialog.chooseOption("Seabright Pier")
                        && !Dialog.chooseOption("Yes, take me there")
                        && !Dialog.chooseOption("Yes")) {
                        Dialog.chooseOption(1);
                    }
                } else {
                    this.status = "Travel: selecting result @ " + SELECT_BTN_X + "," + SELECT_BTN_Y;
                    Mouse.click(SELECT_BTN_X, SELECT_BTN_Y, true);
                }
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
                if (now - this.phaseAtMs > 8000L) {
                    this.status = "Travel: teleport timeout - retry";
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

    /** Chat command only (::home). Destination search must NOT use this. */
    private static void typeChatCommand(String text) {
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

    private static void typeIntoFocusedField(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            for (int i = 0; i < text.length(); i++) {
                Keyboard.type(text.charAt(i));
            }
            Keyboard.sendEnter();
        } catch (Throwable ignored) {
            // best-effort
        }
    }

    private static void pressEscape() {
        try {
            Keyboard.typed(27); // VK_ESCAPE — closes chat without Keyboard.type into chat
        } catch (Throwable ignored) {
            // optional
        }
    }

    private static void clickWidget(Widget widget) {
        if (widget == null) {
            return;
        }
        try {
            if (widget.hasAction("Search")) {
                widget.interact("Search");
                return;
            }
            String[] actions = widget.getActions();
            if (actions != null) {
                for (String a : actions) {
                    if (a != null && !a.isEmpty()) {
                        widget.interact(a);
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {
            // fall through to canvas click
        }
        try {
            Rectangle bounds = widget.getBounds();
            if (bounds != null && bounds.width > 0 && bounds.height > 0) {
                Mouse.click(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, true);
                return;
            }
        } catch (Throwable ignored) {
            // ignore
        }
        try {
            widget.interact(0);
        } catch (Throwable ignored) {
            // ignore
        }
    }

    private static Widget findDestinationResult(String dest) {
        String needle = normalize(dest);
        if (needle.isEmpty()) {
            return null;
        }
        Widget exact = findVisibleWidget(dest, false);
        if (exact != null) {
            return exact;
        }
        Client client = clientOrNull();
        if (client == null) {
            return null;
        }
        Widget[][] roots = client.getWidgets();
        if (roots == null) {
            return null;
        }
        Widget best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Widget[] group : roots) {
            if (group == null) {
                continue;
            }
            for (Widget w : group) {
                best = pickBetterMatch(w, needle, best, bestScore);
                if (best != null) {
                    bestScore = normalize(stripTags(best.getText())).length();
                }
                if (w == null) {
                    continue;
                }
                Widget[] kids = w.getChildren();
                if (kids == null) {
                    continue;
                }
                for (Widget kid : kids) {
                    Widget prev = best;
                    best = pickBetterMatch(kid, needle, best, bestScore);
                    if (best != null && best != prev) {
                        bestScore = normalize(stripTags(best.getText())).length();
                    }
                }
            }
        }
        return best;
    }

    private static Widget pickBetterMatch(Widget w, String needle, Widget best, int bestScore) {
        if (w == null || w.isHidden()) {
            return best;
        }
        String text = normalize(stripTags(w.getText()));
        if (text.isEmpty()) {
            return best;
        }
        if (!(text.contains(needle) || needle.contains(text))) {
            return best;
        }
        if (best == null || text.length() < bestScore) {
            return w;
        }
        return best;
    }

    private static Widget findVisibleWidget(String want, boolean alsoAction) {
        Client client = clientOrNull();
        if (client == null || want == null || want.isEmpty()) {
            return null;
        }
        Widget[][] roots = client.getWidgets();
        if (roots == null) {
            return null;
        }
        String wantNorm = normalize(want);
        for (Widget[] group : roots) {
            if (group == null) {
                continue;
            }
            for (Widget w : group) {
                Widget hit = matchWidget(w, want, wantNorm, alsoAction);
                if (hit != null) {
                    return hit;
                }
                if (w == null) {
                    continue;
                }
                Widget[] kids = w.getChildren();
                if (kids == null) {
                    continue;
                }
                for (Widget kid : kids) {
                    hit = matchWidget(kid, want, wantNorm, alsoAction);
                    if (hit != null) {
                        return hit;
                    }
                }
            }
        }
        return null;
    }

    private static Widget matchWidget(Widget w, String want, String wantNorm, boolean alsoAction) {
        if (w == null || w.isHidden()) {
            return null;
        }
        String text = stripTags(w.getText());
        if (text != null && !text.isEmpty()) {
            if (want.equalsIgnoreCase(text) || normalize(text).equals(wantNorm)) {
                return w;
            }
        }
        if (alsoAction) {
            try {
                if (w.hasAction(want)) {
                    return w;
                }
                String[] actions = w.getActions();
                if (actions != null) {
                    for (String a : actions) {
                        if (a != null && a.equalsIgnoreCase(want)) {
                            return w;
                        }
                    }
                }
            } catch (Throwable ignored) {
                // ignore
            }
        }
        return null;
    }

    private static Client clientOrNull() {
        try {
            return RuneLite.getInjector().getInstance(Client.class);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String stripTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "").trim();
    }

    private static String normalize(String text) {
        return stripTags(text).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
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
