/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.ObjectComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;

public final class AfkTrainer {
    private final Client client;
    private final HumanInput input;
    private Spec spec;
    private Phase phase = Phase.COMMAND;
    private long phaseAt;
    private long lastXpGainMs;
    private int lastXp = -1;
    private int actionAttempts;
    private WorldPoint fromPos;
    private WorldPoint landedPos;
    private long stallThresholdMs = 8000L;
    private final Random rng = new Random();
    private String status = "AFK: starting";

    public AfkTrainer(MasterContext masterContext) {
        this.client = masterContext.client;
        this.input = masterContext.input;
    }

    public void begin(Spec spec) {
        this.spec = spec;
        this.phase = Phase.COMMAND;
        this.phaseAt = System.currentTimeMillis();
        this.lastXp = -1;
        this.lastXpGainMs = this.phaseAt;
        this.actionAttempts = 0;
        this.fromPos = null;
        this.landedPos = null;
        this.status = "AFK: starting";
    }

    public String status() {
        return this.status;
    }

    public int step(Player player) {
        if (this.spec == null || player == null) {
            return 600;
        }
        long l = System.currentTimeMillis();
        WorldPoint worldPoint = player.getWorldLocation();
        boolean bl = worldPoint != null && worldPoint.getPlane() == this.spec.dest.getPlane() && worldPoint.distanceTo(this.spec.dest) <= 10;
        switch (this.phase.ordinal()) {
            case 0: {
                if (bl || this.objectInReach(worldPoint)) {
                    this.landedPos = worldPoint;
                    this.phase = Phase.ACTION;
                    this.phaseAt = l;
                    return 300;
                }
                this.status = "AFK: ::afk";
                this.fromPos = worldPoint;
                this.input.command("::afk");
                this.phase = Phase.CLICK_WINDOW;
                this.phaseAt = l;
                return 3000;
            }
            case 1: {
                this.status = "AFK: selecting method";
                this.input.clickFixedMouse(this.spec.clickX, this.spec.clickY, 8, 6);
                this.phase = Phase.VERIFY;
                this.phaseAt = l;
                return 2500;
            }
            case 2: {
                if (this.teleported(worldPoint) || bl || this.objectInReach(worldPoint)) {
                    this.landedPos = worldPoint;
                    this.phase = Phase.ACTION;
                    this.phaseAt = l;
                    return 600;
                }
                if (l - this.phaseAt > 7000L) {
                    this.status = "AFK: teleport retry";
                    this.phase = Phase.COMMAND;
                    this.phaseAt = l;
                    return 400;
                }
                this.status = "AFK: waiting for teleport";
                return 600;
            }
            case 3: {
                if (this.draggedAway(worldPoint)) {
                    this.phase = Phase.COMMAND;
                    this.phaseAt = l;
                    return 400;
                }
                TileObject tileObject = this.findObject(worldPoint);
                if (tileObject == null) {
                    this.status = "AFK: object not found";
                    return 1200;
                }
                this.status = "AFK: starting " + this.spec.action;
                this.interactObject(tileObject);
                ++this.actionAttempts;
                if (this.lastXp < 0) {
                    this.lastXp = Skills.getExperience(this.spec.skill);
                }
                this.stallThresholdMs = 8000 + this.rng.nextInt(4500) - 1000;
                this.phase = Phase.MONITOR;
                this.phaseAt = l;
                return 2200 + this.rng.nextInt(700);
            }
            case 4: {
                if (this.draggedAway(worldPoint)) {
                    this.phase = Phase.COMMAND;
                    this.phaseAt = l;
                    return 400;
                }
                int n = Skills.getExperience(this.spec.skill);
                if (n > this.lastXp) {
                    this.lastXp = n;
                    this.lastXpGainMs = l;
                    this.actionAttempts = 0;
                    this.status = "AFK: training (" + this.spec.skill.name().toLowerCase() + ")";
                    return 3400 + this.rng.nextInt(1600);
                }
                if (l - this.lastXpGainMs > this.stallThresholdMs) {
                    this.status = "AFK: restarting (xp stalled)";
                    this.phase = Phase.ACTION;
                    this.phaseAt = l;
                    return 400;
                }
                this.status = "AFK: training";
                return 2200 + this.rng.nextInt(700);
            }
        }
        return 600;
    }

    private boolean teleported(WorldPoint worldPoint) {
        if (worldPoint == null || this.fromPos == null) {
            return false;
        }
        return worldPoint.getPlane() != this.fromPos.getPlane() || worldPoint.distanceTo(this.fromPos) > 20;
    }

    private boolean draggedAway(WorldPoint worldPoint) {
        if (worldPoint == null || this.landedPos == null) {
            return false;
        }
        return worldPoint.getPlane() != this.landedPos.getPlane() || worldPoint.distanceTo(this.landedPos) > 25;
    }

    private void interactObject(TileObject tileObject) {
        if (this.actionAttempts < 3) {
            this.input.interactObject(tileObject, this.spec.action);
        } else {
            this.input.interactObjectFirst(tileObject);
        }
    }

    private boolean objectInReach(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return false;
        }
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> this.matchesId((TileObject)tileObject, this.spec.objectId));
        if (tileObject2 == null) {
            tileObject2 = TileObjects.getNearest(tileObject -> this.hasAction((TileObject)tileObject, this.spec.action));
        }
        return tileObject2 != null && tileObject2.getWorldLocation() != null && tileObject2.getWorldLocation().distanceTo(worldPoint) <= 20;
    }

    private TileObject findObject(WorldPoint worldPoint) {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> this.matchesId((TileObject)tileObject, this.spec.objectId));
        if (tileObject2 != null) {
            return tileObject2;
        }
        tileObject2 = TileObjects.getNearest(tileObject -> this.hasAction((TileObject)tileObject, this.spec.action));
        if (tileObject2 != null) {
            return tileObject2;
        }
        WorldPoint worldPoint2 = this.landedPos != null ? this.landedPos : this.spec.dest;
        return TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getWorldLocation() != null && tileObject.getWorldLocation().distanceTo(worldPoint2) <= 20);
    }

    private boolean matchesId(TileObject tileObject, int n) {
        if (tileObject == null) {
            return false;
        }
        if (tileObject.getId() == n) {
            return true;
        }
        ObjectComposition objectComposition = this.resolve(tileObject.getId());
        return objectComposition != null && objectComposition.getId() == n;
    }

    private boolean hasAction(TileObject tileObject, String string) {
        if (tileObject == null || string == null || string.isEmpty()) {
            return false;
        }
        ObjectComposition objectComposition = this.resolve(tileObject.getId());
        if (objectComposition == null || objectComposition.getActions() == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : objectComposition.getActions()) {
            if (string3 == null || !string3.toLowerCase().contains(string2)) continue;
            return true;
        }
        return false;
    }

    private ObjectComposition resolve(int n) {
        ObjectComposition objectComposition = this.client.getObjectDefinition(n);
        if (objectComposition == null) {
            return null;
        }
        try {
            ObjectComposition objectComposition2;
            if (objectComposition.getImpostorIds() != null && (objectComposition2 = objectComposition.getImpostor()) != null) {
                return objectComposition2;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return objectComposition;
    }

    private static enum Phase {
        COMMAND,
        CLICK_WINDOW,
        VERIFY,
        ACTION,
        MONITOR;

    }

    public static final class Spec {
        public final Skill skill;
        public final int clickX;
        public final int clickY;
        public final WorldPoint dest;
        public final int objectId;
        public final String action;

        public Spec(Skill skill, int n, int n2, WorldPoint worldPoint, int n3, String string) {
            this.skill = skill;
            this.clickX = n;
            this.clickY = n2;
            this.dest = worldPoint;
            this.objectId = n3;
            this.action = string;
        }
    }
}

