/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Player
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.widgets.Dialog;

public final class GliderTeleporter {
    private final HumanInput input;
    private Phase phase = Phase.WALK;
    private long phaseAt = System.currentTimeMillis();
    private WorldPoint preClickPos;
    private int homeTeleAttempts;
    private int delayMs = 600;
    private String status = "Glider";

    public GliderTeleporter(HumanInput humanInput) {
        this.input = humanInput;
    }

    public void begin() {
        this.phase = Phase.WALK;
        this.phaseAt = System.currentTimeMillis();
        this.preClickPos = null;
        this.homeTeleAttempts = 0;
        this.status = "Glider: starting";
    }

    public int delayMs() {
        return this.delayMs;
    }

    public String status() {
        return this.status;
    }

    public Result step(String string, Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        long l = System.currentTimeMillis();
        switch (this.phase.ordinal()) {
            case 0: {
                if (worldPoint.distanceTo(AioSkillerConstants.GLIDER_TILE) > 4) {
                    if (worldPoint.distanceTo(AioSkillerConstants.GLIDER_TILE) > 60 && this.homeTeleAttempts < 4) {
                        this.status = "Glider: ::home to reach glider";
                        this.input.command("::home");
                        ++this.homeTeleAttempts;
                        this.delayMs = 10000;
                        return Result.RUNNING;
                    }
                    this.status = "Glider: walking to glider";
                    this.input.walkToward(worldPoint, AioSkillerConstants.GLIDER_TILE);
                    this.delayMs = 1200;
                    return Result.RUNNING;
                }
                this.homeTeleAttempts = 0;
                this.phase = Phase.TRAVEL;
                this.phaseAt = l;
                this.delayMs = 300;
                return Result.RUNNING;
            }
            case 1: {
                TileObject tileObject = this.findGlider();
                if (tileObject != null) {
                    this.status = "Glider: Travel";
                    this.input.interactObjectVia(tileObject, "Travel");
                    this.phase = Phase.SEARCH;
                    this.phaseAt = l;
                    this.delayMs = 6000;
                    return Result.RUNNING;
                }
                this.status = "Glider: locating object";
                this.delayMs = 800;
                return Result.RUNNING;
            }
            case 2: {
                this.status = "Glider: search box";
                this.input.clickFixedMouse(75, 305);
                this.phase = Phase.TYPE;
                this.phaseAt = l;
                this.delayMs = 3000;
                return Result.RUNNING;
            }
            case 3: {
                this.status = "Glider: typing '" + string + "'";
                this.input.command(string);
                this.phase = Phase.SELECT;
                this.phaseAt = l;
                this.delayMs = 1200;
                return Result.RUNNING;
            }
            case 4: {
                this.preClickPos = worldPoint;
                this.status = "Glider: select result";
                this.input.clickFixedMouse(220, 90);
                this.phase = Phase.VERIFY;
                this.phaseAt = l;
                this.delayMs = 1500;
                return Result.RUNNING;
            }
            case 5: {
                if (Dialog.isOpen() && Dialog.isViewingOptions()) {
                    this.status = "Glider: confirming";
                    if (!Dialog.chooseOption("Yes, take me there") && !Dialog.chooseOption("Yes")) {
                        Dialog.chooseOption(1);
                    }
                    this.phaseAt = l;
                    this.delayMs = 1200;
                    return Result.RUNNING;
                }
                if (this.preClickPos != null && worldPoint.distanceTo(this.preClickPos) > 15) {
                    this.status = "Glider: arrived";
                    this.phase = Phase.WALK;
                    this.preClickPos = null;
                    this.delayMs = 600;
                    return Result.ARRIVED;
                }
                if (l - this.phaseAt > 6000L) {
                    this.status = "Glider: retry";
                    this.phase = Phase.TRAVEL;
                    this.phaseAt = l;
                    this.delayMs = 300;
                    return Result.RUNNING;
                }
                this.status = "Glider: waiting teleport";
                this.delayMs = 500;
                return Result.RUNNING;
            }
        }
        this.delayMs = 600;
        return Result.RUNNING;
    }

    private TileObject findGlider() {
        TileObject tileObject2 = TileObjects.getNearest("Gnome glider");
        if (tileObject2 != null) {
            return tileObject2;
        }
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getWorldLocation().distanceTo(AioSkillerConstants.GLIDER_TILE) <= 3);
    }

    private static enum Phase {
        WALK,
        TRAVEL,
        SEARCH,
        TYPE,
        SELECT,
        VERIFY;

    }

    public static enum Result {
        RUNNING,
        ARRIVED;

    }
}

