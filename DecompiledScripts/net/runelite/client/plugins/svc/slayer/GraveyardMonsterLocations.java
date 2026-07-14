/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.slayer;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.slayer.SlayerNames;

public class GraveyardMonsterLocations {
    private static final Map<String, WorldPoint> LOCATIONS = new HashMap<String, WorldPoint>();

    public static WorldPoint getLocation(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        String string2 = string.toLowerCase();
        if (LOCATIONS.containsKey(string2)) {
            return LOCATIONS.get(string2);
        }
        for (Map.Entry<String, WorldPoint> entry : LOCATIONS.entrySet()) {
            if (!SlayerNames.matches(string, entry.getKey())) continue;
            return entry.getValue();
        }
        return null;
    }

    public static boolean hasLocation(String string) {
        return GraveyardMonsterLocations.getLocation(string) != null;
    }

    private static void put(String string, int n, int n2, int n3) {
        LOCATIONS.put(string.toLowerCase(), new WorldPoint(n, n2, n3));
    }

    static {
        GraveyardMonsterLocations.put("zogre", 2605, 3561, 0);
        GraveyardMonsterLocations.put("crypt beast", 2620, 3561, 0);
        GraveyardMonsterLocations.put("zombie", 2619, 3579, 0);
        GraveyardMonsterLocations.put("grave horror", 2619, 3603, 0);
        GraveyardMonsterLocations.put("ghost", 2615, 3534, 0);
        GraveyardMonsterLocations.put("skogre", 2599, 3603, 0);
        GraveyardMonsterLocations.put("crawling hand", 2582, 3602, 0);
        GraveyardMonsterLocations.put("banshee", 2570, 3616, 0);
        GraveyardMonsterLocations.put("wailing wraith", 2559, 3578, 0);
        GraveyardMonsterLocations.put("infernal mage", 2519, 3525, 0);
        GraveyardMonsterLocations.put("otherworldly being", 2548, 3519, 0);
        GraveyardMonsterLocations.put("aberrant spectre", 3739, 4181, 0);
        GraveyardMonsterLocations.put("snail", 2640, 3466, 0);
    }
}

