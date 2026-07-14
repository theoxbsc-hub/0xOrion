/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConfig;

public final class GearPrefs {
    private GearPrefs() {
    }

    public static int[] eventGear(AioSkillerConfig aioSkillerConfig) {
        if (aioSkillerConfig == null || !aioSkillerConfig.eventGearOverride()) {
            return null;
        }
        int[] nArray = GearPrefs.parse(aioSkillerConfig.eventGearIds());
        return nArray.length == 0 ? null : nArray;
    }

    public static int[] combatGear(AioSkillerConfig aioSkillerConfig) {
        if (aioSkillerConfig == null || !aioSkillerConfig.combatGearOverride()) {
            return null;
        }
        int[] nArray = GearPrefs.parse(aioSkillerConfig.combatGearIds());
        return nArray.length == 0 ? null : nArray;
    }

    public static int[] parse(String string) {
        if (string == null) {
            return new int[0];
        }
        LinkedHashSet<Integer> linkedHashSet = new LinkedHashSet<Integer>();
        for (String string2 : string.split("[,\\s]+")) {
            if (string2.isEmpty()) continue;
            try {
                int n = Integer.parseInt(string2.trim());
                if (n <= 0) continue;
                linkedHashSet.add(n);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        ArrayList arrayList = new ArrayList(linkedHashSet);
        int[] nArray = new int[arrayList.size()];
        for (int i = 0; i < nArray.length; ++i) {
            nArray[i] = (Integer)arrayList.get(i);
        }
        return nArray;
    }
}

