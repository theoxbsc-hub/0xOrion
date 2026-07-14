package net.runelite.client.plugins.ox.fishing;

import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Dialog;

/**
 * Startup path when at home: ::home (if needed) → Gnome glider → Travel → search Seabright Pier.
 */
final class FishGliderTravel {
    static final WorldPoint GLIDER_TILE = new WorldPoint(2795, 3374, 0);
    static final WorldPoint HOME_CENTER = new WorldPoint(2785, 3382, 0);
    static final String GLIDER_NAME = "Gnome glider";
    static final String DEFAULT_DEST = "seabright pier";

    private Phase phase = Phase.HOME;
    private long phaseAtMs = 0L;
    private WorldPoint preClickPos;
    private int homeAttempts = 0;
    private String status = "Travel: starting";

    void begin() {
        this.phase = Phase.HOME;
        this.phaseAtMs = System.currentTimeMillis();
        this.preClickPos = null;
        this.homeAttempts = 0;
        this.status = "Travel: ::home";
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
            case HOME: {
                if (here.distanceTo(GLIDER_TILE) <= 8) {
                    this.phase = Phase.WALK;
                    this.phaseAtMs = now;
                    this.status = "Travel: near glider";
                    return 300;
                }
                if (here.distanceTo(HOME_CENTER) <= 25 || here.distanceTo(GLIDER_TILE) <= 60) {
                    this.phase = Phase.WALK;
                    this.phaseAtMs = now;
                    return 300;
                }
                if (this.homeAttempts >= 4) {
                    this.status = "Travel: ::home failed — walking anyway";
                    this.phase = Phase.WALK;
                    return 800;
                }
                ++this.homeAttempts;
                this.status = "Travel: ::home (attempt " + this.homeAttempts + ")";
                typeCommand("::home");
                this.phaseAtMs = now;
                return 10000;
            }
            case WALK: {
                if (here.distanceTo(GLIDER_TILE) > 4) {
                    if (here.distanceTo(GLIDER_TILE) > 60 && this.homeAttempts < 4) {
                        this.phase = Phase.HOME;
                        this.status = "Travel: too far — ::home again";
                        return 300;
                    }
                    this.status = "Travel: walking to glider";
                    Movement.walkTo(GLIDER_TILE);
                    return 1200;
                }
                this.homeAttempts = 0;
                this.phase = Phase.TRAVEL;
                this.phaseAtMs = now;
                this.status = "Travel: at glider";
                return 300;
            }
            case TRAVEL: {
                TileObject glider = findGlider();
                if (glider != null) {
                    this.status = "Travel: clicking Travel";
                    glider.interact("Travel");
                    this.phase = Phase.SEARCH;
                    this.phaseAtMs = now;
                    return 6000;
                }
                this.status = "Travel: looking for Gnome glider";
                return 800;
            }
            case SEARCH: {
                this.status = "Travel: opening search";
                Mouse.click(75, 305, true);
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
                Mouse.click(220, 90, true);
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
                    this.phase = Phase.HOME;
                    this.preClickPos = null;
                    return Integer.MIN_VALUE;
                }
                if (now - this.phaseAtMs > 8000L) {
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

    private static TileObject findGlider() {
        TileObject byName = TileObjects.getNearest(GLIDER_NAME);
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
        HOME,
        WALK,
        TRAVEL,
        SEARCH,
        TYPE,
        SELECT,
        VERIFY
    }
}
