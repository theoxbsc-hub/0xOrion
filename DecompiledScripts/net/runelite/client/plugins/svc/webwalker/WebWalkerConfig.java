/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 */
package net.runelite.client.plugins.svc.webwalker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(value="orionWebWalker")
public interface WebWalkerConfig
extends Config {
    public static final String GROUP = "orionWebWalker";
    @ConfigSection(name="Destination", description="Where to walk", position=0)
    public static final String destSection = "destination";
    @ConfigSection(name="Behaviour", description="How to walk", position=1)
    public static final String behaviourSection = "behaviour";

    @ConfigItem(keyName="destX", name="X", description="Destination world X", section="destination", position=0)
    default public int destX() {
        return 3222;
    }

    @ConfigItem(keyName="destY", name="Y", description="Destination world Y", section="destination", position=1)
    default public int destY() {
        return 3218;
    }

    @ConfigItem(keyName="destPlane", name="Plane", description="Destination plane (0-3)", section="destination", position=2)
    @Range(min=0, max=3)
    default public int destPlane() {
        return 0;
    }

    @ConfigItem(keyName="useTransports", name="Use transports", description="Route through doors/stairs/shortcuts/teleports (transports.json)", section="behaviour", position=0)
    default public boolean useTransports() {
        return true;
    }

    @ConfigItem(keyName="avoidWilderness", name="Avoid Wilderness", description="Never route into the Wilderness (unless the target is inside it)", section="behaviour", position=1)
    default public boolean avoidWilderness() {
        return true;
    }

    @ConfigItem(keyName="arriveRadius", name="Arrive radius", description="Consider the destination reached within this many tiles", section="behaviour", position=2)
    @Range(min=0, max=10)
    default public int arriveRadius() {
        return 1;
    }

    @ConfigItem(keyName="walkStep", name="Hop size (tiles)", description="Tiles per server-pathed hop (lower = more clicks, tighter following)", section="behaviour", position=3)
    @Range(min=4, max=20)
    default public int walkStep() {
        return 12;
    }

    @ConfigItem(keyName="stopOnArrival", name="Stop on arrival", description="Disable walking automatically once the destination is reached", section="behaviour", position=4)
    default public boolean stopOnArrival() {
        return true;
    }
}

