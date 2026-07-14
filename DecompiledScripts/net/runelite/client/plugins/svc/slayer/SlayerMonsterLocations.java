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

public class SlayerMonsterLocations {
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
            if (!string2.contains(entry.getKey()) && !entry.getKey().contains(string2)) continue;
            return entry.getValue();
        }
        return null;
    }

    public static boolean hasLocation(String string) {
        return SlayerMonsterLocations.getLocation(string) != null;
    }

    private static void put(String string, int n, int n2, int n3) {
        LOCATIONS.put(string.toLowerCase(), new WorldPoint(n, n2, n3));
    }

    static {
        SlayerMonsterLocations.put("crawling hand", 3429, 3551, 0);
        SlayerMonsterLocations.put("banshee", 3447, 3542, 0);
        SlayerMonsterLocations.put("infernal mage", 3428, 3532, 0);
        SlayerMonsterLocations.put("bloodveld", 3429, 3522, 0);
        SlayerMonsterLocations.put("aberrant spectre", 3428, 3547, 2);
        SlayerMonsterLocations.put("gargoyle", 3440, 3546, 2);
        SlayerMonsterLocations.put("nechryael", 3434, 3538, 3);
        SlayerMonsterLocations.put("abyssal demon", 3416, 3563, 3);
        SlayerMonsterLocations.put("hill giant", 3126, 9833, 0);
        SlayerMonsterLocations.put("moss giant", 3138, 9913, 0);
        SlayerMonsterLocations.put("earth warrior", 3145, 9906, 0);
        SlayerMonsterLocations.put("hobgoblin", 3096, 9867, 0);
        SlayerMonsterLocations.put("cave crawler", 2777, 9997, 0);
        SlayerMonsterLocations.put("rockslug", 2786, 9997, 0);
        SlayerMonsterLocations.put("cockatrice", 2782, 10007, 0);
        SlayerMonsterLocations.put("basilisk", 2775, 10019, 0);
        SlayerMonsterLocations.put("turoth", 2703, 9966, 0);
        SlayerMonsterLocations.put("kurask", 2703, 9982, 0);
        SlayerMonsterLocations.put("blue dragon", 2884, 9799, 0);
        SlayerMonsterLocations.put("black demon", 2889, 9816, 0);
        SlayerMonsterLocations.put("hellhound", 2863, 9839, 0);
        SlayerMonsterLocations.put("chaos dwarf", 2858, 9865, 0);
        SlayerMonsterLocations.put("ice warrior", 3003, 9564, 0);
        SlayerMonsterLocations.put("ice giant", 3007, 9581, 0);
        SlayerMonsterLocations.put("skeletal wyvern", 3003, 9542, 0);
        SlayerMonsterLocations.put("ankou", 2368, 9480, 0);
        SlayerMonsterLocations.put("lesser demon", 2866, 9635, 0);
        SlayerMonsterLocations.put("greater demon", 2884, 9455, 0);
        SlayerMonsterLocations.put("fire giant", 2857, 9460, 0);
        SlayerMonsterLocations.put("iron dragon", 2715, 9466, 0);
        SlayerMonsterLocations.put("steel dragon", 2721, 9492, 0);
        SlayerMonsterLocations.put("ghost", 1666, 10059, 0);
        SlayerMonsterLocations.put("ghoul", 1671, 10066, 0);
        SlayerMonsterLocations.put("shade", 1678, 10082, 0);
        SlayerMonsterLocations.put("twisted banshee", 1673, 10018, 0);
        SlayerMonsterLocations.put("warped jelly", 1680, 10000, 0);
        SlayerMonsterLocations.put("dust devil", 1686, 9992, 0);
        SlayerMonsterLocations.put("smoke devil", 2405, 3846, 0);
        SlayerMonsterLocations.put("black dragon", 1682, 10060, 0);
        SlayerMonsterLocations.put("mutated bloodveld", 1680, 10044, 0);
        SlayerMonsterLocations.put("deviant spectre", 1682, 10028, 0);
        SlayerMonsterLocations.put("brutal black dragon", 1682, 10060, 0);
        SlayerMonsterLocations.put("rock crab", 2671, 3715, 0);
        SlayerMonsterLocations.put("sand crab", 1750, 3462, 0);
        SlayerMonsterLocations.put("green dragon", 3358, 3667, 0);
        SlayerMonsterLocations.put("greater demon", 2887, 3837, 0);
        SlayerMonsterLocations.put("black knight", 3023, 3512, 0);
        SlayerMonsterLocations.put("fossil island wyvern", 3763, 3899, 0);
        SlayerMonsterLocations.put("ancient wyvern", 3763, 3899, 0);
        SlayerMonsterLocations.put("aviansie", 2824, 5298, 0);
        SlayerMonsterLocations.put("spiritual mage", 2919, 5319, 0);
        SlayerMonsterLocations.put("spiritual ranger", 2924, 5305, 0);
        SlayerMonsterLocations.put("spiritual warrior", 2928, 5320, 0);
        SlayerMonsterLocations.put("dagannoth", 2521, 3738, 0);
        SlayerMonsterLocations.put("kalphite", 3226, 3106, 0);
        SlayerMonsterLocations.put("desert lizard", 3399, 3157, 0);
        SlayerMonsterLocations.put("crocodile", 3290, 3140, 0);
        SlayerMonsterLocations.put("wall beast", 3149, 9543, 0);
        SlayerMonsterLocations.put("cave bug", 3149, 9554, 0);
        SlayerMonsterLocations.put("cave slime", 3149, 9543, 0);
        SlayerMonsterLocations.put("vampyre", 3590, 3306, 0);
        SlayerMonsterLocations.put("cave horror", 3724, 9408, 0);
        SlayerMonsterLocations.put("jungle horror", 3724, 9408, 0);
        SlayerMonsterLocations.put("sea snake", 3727, 9420, 0);
        SlayerMonsterLocations.put("suqah", 2125, 3923, 0);
        SlayerMonsterLocations.put("mithril dragon", 1773, 5366, 0);
        SlayerMonsterLocations.put("waterfiend", 1763, 5360, 0);
        SlayerMonsterLocations.put("barbarian", 1771, 5391, 0);
        SlayerMonsterLocations.put("zygomite", 2393, 4451, 0);
        SlayerMonsterLocations.put("mutated zygomite", 2393, 4451, 0);
    }
}

