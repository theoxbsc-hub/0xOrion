/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.agility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.agility.AgilityObstacle;

public enum AgilityCourse {
    GNOME_LVL1("Lvl 1 (Gnome)", Arrays.asList(new AgilityObstacle(new WorldPoint(2628, 3491, 0), new WorldPoint(2628, 3489, 0), "Obstacle pipe", 23138, 749), new AgilityObstacle(new WorldPoint(2632, 3500, 0), new WorldPoint(2628, 3497, 0), "Log balance", 3555, new int[0]), new AgilityObstacle(new WorldPoint(2645, 3494, 0), new WorldPoint(2638, 3500, 0), "Ropeswing", 23131, 751), new AgilityObstacle(new WorldPoint(2645, 3490, 0), new WorldPoint(2645, 3495, 0), "Obstacle net", 20211, 828), new AgilityObstacle(new WorldPoint(2640, 3487, 0), new WorldPoint(2645, 3489, 0), "Bridge", 3522, 2586))),
    ICE_LVL50("Lvl 50 (Ice)", Arrays.asList(new AgilityObstacle(new WorldPoint(2952, 3654, 0), new WorldPoint(2945, 3650, 0), "Ice path", 47472, 32557), new AgilityObstacle(new WorldPoint(2949, 3672, 0), new WorldPoint(2953, 3666, 0), "Ice block", 47463, 2585), new AgilityObstacle(new WorldPoint(2943, 3672, 3), new WorldPoint(2946, 3672, 3), "Ice chunks", 47470, 3067), new AgilityObstacle(new WorldPoint(2939, 3672, 3), new WorldPoint(2942, 3672, 3), "Ice block", 47464, 3067), new AgilityObstacle(new WorldPoint(2935, 3672, 3), new WorldPoint(2938, 3672, 3), "Ice block", 47465, 2586), new AgilityObstacle(new WorldPoint(2926, 3672, 0), new WorldPoint(2932, 3672, 0), "Ice chunks", 47471, 3067), new AgilityObstacle(new WorldPoint(2916, 3673, 0), new WorldPoint(2925, 3672, 0), "Floating ice", 47648, 741), new AgilityObstacle(new WorldPoint(2911, 3659, 0), new WorldPoint(2914, 3668, 0), "Floating ice", 47468, 741), new AgilityObstacle(new WorldPoint(2916, 3651, 0), new WorldPoint(2912, 3654, 0), "Floating ice", 47614, 741), new AgilityObstacle(new WorldPoint(2930, 3649, 0), new WorldPoint(2925, 3650, 0), "Cave", 47460, 2796)));

    private final String displayName;
    private final List<AgilityObstacle> obstacles;

    private AgilityCourse(String string2, List<AgilityObstacle> list) {
        this.displayName = string2;
        this.obstacles = Collections.unmodifiableList(list);
    }

    public String displayName() {
        return this.displayName;
    }

    public List<AgilityObstacle> obstacles() {
        return this.obstacles;
    }

    public int size() {
        return this.obstacles.size();
    }
}

