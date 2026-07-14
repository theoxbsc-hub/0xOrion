/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.randomguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.randomguard.NpcInfo;

final class GuardCapture {
    private GuardCapture() {
    }

    static NpcInfo findEventNpc(Client client, Set<String> set, int n) {
        Player player = client.getLocalPlayer();
        if (player == null || player.getWorldLocation() == null) {
            return null;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        NpcInfo npcInfo = null;
        NpcInfo npcInfo2 = null;
        NpcInfo npcInfo3 = null;
        int n2 = Integer.MAX_VALUE;
        int n3 = Integer.MAX_VALUE;
        int n4 = Integer.MAX_VALUE;
        try {
            for (NPC nPC : client.getNpcs()) {
                int n5;
                if (nPC == null || nPC.getWorldLocation() == null || (n5 = nPC.getWorldLocation().distanceTo(worldPoint)) > n) continue;
                NpcInfo npcInfo4 = GuardCapture.describe(client, nPC, n5);
                if (n5 < n4) {
                    n4 = n5;
                    npcInfo3 = npcInfo4;
                }
                String string = npcInfo4.name.toLowerCase();
                if (!set.isEmpty()) {
                    for (String string2 : set) {
                        if (string2.isEmpty() || !string.contains(string2)) continue;
                        if (n5 >= n2) break;
                        n2 = n5;
                        npcInfo = npcInfo4;
                        break;
                    }
                }
                if (!GuardCapture.hasVerb(npcInfo4, "talk-to") && !GuardCapture.hasVerb(npcInfo4, "dismiss") && !GuardCapture.hasVerb(npcInfo4, "talk to") || n5 >= n3) continue;
                n3 = n5;
                npcInfo2 = npcInfo4;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (npcInfo != null) {
            return npcInfo;
        }
        if (npcInfo2 != null) {
            return npcInfo2;
        }
        return npcInfo3;
    }

    static List<NpcInfo> nearbyNpcs(Client client, int n, int n2) {
        ArrayList<NpcInfo> arrayList = new ArrayList<NpcInfo>();
        Player player = client.getLocalPlayer();
        if (player == null || player.getWorldLocation() == null) {
            return arrayList;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        try {
            for (NPC nPC : client.getNpcs()) {
                int n3;
                if (nPC == null || nPC.getWorldLocation() == null || (n3 = nPC.getWorldLocation().distanceTo(worldPoint)) > n) continue;
                arrayList.add(GuardCapture.describe(client, nPC, n3));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        arrayList.sort((npcInfo, npcInfo2) -> Integer.compare(npcInfo.distance, npcInfo2.distance));
        return arrayList.size() > n2 ? arrayList.subList(0, n2) : arrayList;
    }

    static NpcInfo describe(Client client, NPC nPC, int n) {
        int n2 = -1;
        int n3 = -1;
        String string = "?";
        WorldPoint worldPoint = null;
        ArrayList<String> arrayList = new ArrayList<String>();
        try {
            n2 = nPC.getId();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            n3 = nPC.getAnimation();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            string = nPC.getName() != null ? nPC.getName().replaceAll("<[^>]*>", "") : "?";
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            worldPoint = nPC.getWorldLocation();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            NPCComposition nPCComposition = nPC.getTransformedComposition();
            if (nPCComposition != null && nPCComposition.getActions() != null) {
                for (String string2 : nPCComposition.getActions()) {
                    if (string2 == null || string2.trim().isEmpty() || "null".equalsIgnoreCase(string2.trim())) continue;
                    arrayList.add(string2.trim());
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new NpcInfo(n2, string, arrayList, worldPoint, n, n3);
    }

    private static boolean hasVerb(NpcInfo npcInfo, String string) {
        for (String string2 : npcInfo.actions) {
            if (!string2.toLowerCase().contains(string)) continue;
            return true;
        }
        return false;
    }

    static Set<String> parseKeywords(String string) {
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        if (string == null) {
            return linkedHashSet;
        }
        for (String string2 : string.split("[,\\n]")) {
            String string3 = string2.trim().toLowerCase();
            if (string3.isEmpty()) continue;
            linkedHashSet.add(string3);
        }
        return linkedHashSet;
    }

    static List<String> parsePhrases(String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (string == null) {
            return arrayList;
        }
        for (String string2 : string.split("[,\\n]")) {
            String string3 = string2.trim().toLowerCase();
            if (string3.isEmpty()) continue;
            arrayList.add(string3);
        }
        return arrayList;
    }

    static boolean matchesAny(String string, List<String> list) {
        if (string == null) {
            return false;
        }
        for (String string2 : list) {
            if (!string.contains(string2)) continue;
            return true;
        }
        return false;
    }

    private static List<String> safeActions(String[] stringArray) {
        return stringArray == null ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(stringArray));
    }
}

