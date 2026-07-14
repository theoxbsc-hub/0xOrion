/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.services.GliderTeleporter;
import net.runelite.client.plugins.svc.aioskiller.services.Walk;
import net.runelite.client.plugins.svc.core.HumanInput;

public final class Navigator {
    public static final int ARRIVED = Integer.MIN_VALUE;
    private final HumanInput input;
    private final GliderTeleporter glider;
    private String status = "";
    private boolean homeIssued = false;
    private long homeIssuedAt = 0L;
    private WorldPoint homeFromPos = null;
    private long homeTimeoutMs = 5000L;
    private final Random rng = new Random();

    public Navigator(Client client, HumanInput humanInput) {
        this.input = humanInput;
        this.glider = new GliderTeleporter(humanInput);
    }

    public boolean atHome(WorldPoint worldPoint) {
        return worldPoint != null && worldPoint.getPlane() == 0 && worldPoint.distanceTo(AioSkillerConstants.HOME_CENTER) <= 35;
    }

    public int ensureHome(Player player) {
        WorldPoint worldPoint;
        WorldPoint worldPoint2 = worldPoint = player == null ? null : player.getWorldLocation();
        if (this.atHome(worldPoint)) {
            this.homeIssued = false;
            this.status = "At home";
            return Integer.MIN_VALUE;
        }
        long l = System.currentTimeMillis();
        if (!this.homeIssued) {
            this.homeFromPos = worldPoint;
            this.homeIssuedAt = l;
            this.homeIssued = true;
            this.homeTimeoutMs = 5000 + this.rng.nextInt(3000) - 800;
            this.status = "Teleporting home (::home)";
            this.typeHome();
            return 600;
        }
        if (l - this.homeIssuedAt > this.homeTimeoutMs) {
            boolean bl;
            boolean bl2 = bl = worldPoint != null && this.homeFromPos != null && (worldPoint.getPlane() != this.homeFromPos.getPlane() || worldPoint.distanceTo(this.homeFromPos) > 20);
            if (!bl) {
                this.homeFromPos = worldPoint;
                this.homeIssuedAt = l;
                this.homeTimeoutMs = 5000 + this.rng.nextInt(3000) - 800;
                this.status = "Re-teleporting home (::home)";
                this.typeHome();
                return 600;
            }
            this.homeIssuedAt = l;
            this.status = "Teleport landing\u2026";
            return 600;
        }
        this.status = "Teleporting home\u2026";
        return 600;
    }

    public int walkTo(WorldPoint worldPoint, Player player, int n) {
        WorldPoint worldPoint2 = player.getWorldLocation();
        if (Walk.arrived(worldPoint2, worldPoint, n)) {
            this.status = "Arrived";
            return Integer.MIN_VALUE;
        }
        this.status = "Walking";
        this.input.walkToward(worldPoint2, worldPoint);
        return 1200;
    }

    public void beginGlider() {
        this.glider.begin();
    }

    public int gliderTo(String string, Player player) {
        GliderTeleporter.Result result = this.glider.step(string, player);
        this.status = this.glider.status();
        return result == GliderTeleporter.Result.ARRIVED ? Integer.MIN_VALUE : this.glider.delayMs();
    }

    public void typeHome() {
        this.input.command("::home");
    }

    public String status() {
        return this.status;
    }
}

