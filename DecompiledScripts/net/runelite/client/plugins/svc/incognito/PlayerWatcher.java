/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.incognito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.incognito.IncognitoConfig;

final class PlayerWatcher {
    private final IncognitoConfig config;
    private String lastRawList = null;
    private Set<String> watchSet = Collections.emptySet();

    PlayerWatcher(IncognitoConfig incognitoConfig) {
        this.config = incognitoConfig;
    }

    Detection scan(Client client) {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return null;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        if (worldPoint == null) {
            return null;
        }
        Set<String> set = this.parseWatchList(this.config.watchedPlayers());
        boolean bl = !set.isEmpty();
        boolean bl2 = this.config.panicEnabled();
        if (!bl && !bl2) {
            return null;
        }
        boolean bl3 = this.config.watchlistDistanceEnabled();
        int n = this.config.watchlistDistance();
        int n2 = this.config.panicDistance();
        boolean bl4 = this.config.panicIgnoreFriends();
        List<IgnoreZone> list = this.parseIgnoreZones();
        Detection detection = null;
        for (Player player2 : client.getPlayers()) {
            WorldPoint worldPoint2;
            if (player2 == null || player2 == player || player2.getName() == null || (worldPoint2 = player2.getWorldLocation()) == null) continue;
            int n3 = worldPoint2.distanceTo(worldPoint);
            String string = PlayerWatcher.cleanName(player2.getName());
            if (bl && set.contains(string.toLowerCase()) && (!bl3 || n3 <= n) && !PlayerWatcher.inIgnoreZone(worldPoint2, list)) {
                return new Detection(player2.getName(), string, worldPoint2, n3, Trigger.WATCHLIST);
            }
            if (!bl2 || n3 > n2 || bl4 && PlayerWatcher.isFriendly(player2) || detection != null && n3 >= detection.distance) continue;
            detection = new Detection(player2.getName(), string, worldPoint2, n3, Trigger.PROXIMITY);
        }
        return detection;
    }

    List<NearbyPlayer> listNearby(Client client, int n) {
        Player player = client.getLocalPlayer();
        if (player == null || player.getWorldLocation() == null) {
            return Collections.emptyList();
        }
        WorldPoint worldPoint = player.getWorldLocation();
        Set<String> set = this.parseWatchList(this.config.watchedPlayers());
        ArrayList<NearbyPlayer> arrayList = new ArrayList<NearbyPlayer>();
        for (Player player2 : client.getPlayers()) {
            if (player2 == null || player2 == player || player2.getName() == null || player2.getWorldLocation() == null) continue;
            String string = PlayerWatcher.cleanName(player2.getName());
            int n2 = player2.getWorldLocation().distanceTo(worldPoint);
            arrayList.add(new NearbyPlayer(string, n2, set.contains(string.toLowerCase())));
        }
        arrayList.sort(Comparator.comparingInt(nearbyPlayer -> nearbyPlayer.distance));
        return arrayList.size() > n ? arrayList.subList(0, n) : arrayList;
    }

    int watchedCount() {
        return this.parseWatchList(this.config.watchedPlayers()).size();
    }

    int ignoreZoneCount() {
        return this.config.ignoreZonesEnabled() ? this.parseIgnoreZones().size() : 0;
    }

    private static boolean inIgnoreZone(WorldPoint worldPoint, List<IgnoreZone> list) {
        for (IgnoreZone ignoreZone : list) {
            if (!ignoreZone.contains(worldPoint)) continue;
            return true;
        }
        return false;
    }

    private List<IgnoreZone> parseIgnoreZones() {
        if (!this.config.ignoreZonesEnabled()) {
            return Collections.emptyList();
        }
        ArrayList<IgnoreZone> arrayList = new ArrayList<IgnoreZone>();
        PlayerWatcher.addZone(arrayList, this.config.ignoreZone1(), this.config.ignoreZone1Radius());
        PlayerWatcher.addZone(arrayList, this.config.ignoreZone2(), this.config.ignoreZone2Radius());
        PlayerWatcher.addZone(arrayList, this.config.ignoreZone3(), this.config.ignoreZone3Radius());
        PlayerWatcher.addZone(arrayList, this.config.ignoreZone4(), this.config.ignoreZone4Radius());
        PlayerWatcher.addZone(arrayList, this.config.ignoreZone5(), this.config.ignoreZone5Radius());
        return arrayList;
    }

    private static void addZone(List<IgnoreZone> list, String string, int n) {
        if (string == null || n < 1) {
            return;
        }
        String[] stringArray = string.trim().split("[^-0-9]+");
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (String string2 : stringArray) {
            if (string2.isEmpty()) continue;
            try {
                int n5 = Integer.parseInt(string2);
                if (n4 == 0) {
                    n2 = n5;
                } else if (n4 == 1) {
                    n3 = n5;
                }
                ++n4;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (n4 >= 2) {
            list.add(new IgnoreZone(n2, n3, n));
        }
    }

    private Set<String> parseWatchList(String string) {
        if (string == null) {
            string = "";
        }
        if (string.equals(this.lastRawList)) {
            return this.watchSet;
        }
        HashSet<String> hashSet = new HashSet<String>();
        for (String string2 : string.split("[,\\n]")) {
            String string3 = PlayerWatcher.cleanName(string2);
            if (string3.isEmpty()) continue;
            hashSet.add(string3.toLowerCase());
        }
        this.lastRawList = string;
        this.watchSet = hashSet;
        return hashSet;
    }

    private static String cleanName(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim();
    }

    private static boolean isFriendly(Player player) {
        try {
            return player.isFriend() || player.isFriendsChatMember() || player.isClanMember();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    static final class Detection {
        final String rawName;
        final String cleanedName;
        final WorldPoint location;
        final int distance;
        final Trigger trigger;

        Detection(String string, String string2, WorldPoint worldPoint, int n, Trigger trigger) {
            this.rawName = string;
            this.cleanedName = string2;
            this.location = worldPoint;
            this.distance = n;
            this.trigger = trigger;
        }
    }

    static enum Trigger {
        WATCHLIST,
        PROXIMITY;

    }

    static final class NearbyPlayer {
        final String name;
        final int distance;
        final boolean watched;

        NearbyPlayer(String string, int n, boolean bl) {
            this.name = string;
            this.distance = n;
            this.watched = bl;
        }
    }

    static final class IgnoreZone {
        final int x;
        final int y;
        final int radius;

        IgnoreZone(int n, int n2, int n3) {
            this.x = n;
            this.y = n2;
            this.radius = n3;
        }

        boolean contains(WorldPoint worldPoint) {
            return Math.max(Math.abs(worldPoint.getX() - this.x), Math.abs(worldPoint.getY() - this.y)) <= this.radius;
        }
    }
}

