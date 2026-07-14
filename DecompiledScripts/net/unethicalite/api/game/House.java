/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.openosrs.client.game.WorldLocation
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.game;

import com.openosrs.client.game.WorldLocation;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Vars;

public class House {
    public static final int HOUSE_LOCATION_VARBIT = 2187;
    public static final WorldLocation[] HOUSE_LOCATIONS = new WorldLocation[]{WorldLocation.RIMMINGTON_PORTAL, WorldLocation.TAVERLY_POH_PORTAL, WorldLocation.POLLNIVNEACH_POH_PORTAL, WorldLocation.RELLEKKA_POH_PORTAL, WorldLocation.BRIMHAVEN_POH_PORTAL, WorldLocation.YANILLE_POH_PORTAL, WorldLocation.PRIFDDINAS_POH_PORTAL, WorldLocation.HOSIDIUS_POH_PORTAL};

    public static WorldPoint getOutsideLocation() {
        if (!Game.isLoggedIn()) {
            return null;
        }
        int idx = Vars.getBit(2187);
        if (idx >= HOUSE_LOCATIONS.length || idx < 0) {
            return null;
        }
        return HOUSE_LOCATIONS[idx - 1].getWorldArea().getCenter();
    }
}

