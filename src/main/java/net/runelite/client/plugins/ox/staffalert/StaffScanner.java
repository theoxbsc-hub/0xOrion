package net.runelite.client.plugins.ox.staffalert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.ox.core.OxConsole;
import net.unethicalite.api.entities.Players;

/**
 * Scans nearby players for any username on the watchlist (or proximity panic).
 * Watchlist is re-read from ConfigManager each parse so UI edits apply immediately.
 */
final class StaffScanner {
    private final StaffAlertConfig config;
    private final ConfigManager configManager;
    private String lastRawList = null;
    private Set<String> watchKeys = Collections.emptySet();

    StaffScanner(StaffAlertConfig config, ConfigManager configManager) {
        this.config = config;
        this.configManager = configManager;
    }

    Detection scan(Client client) {
        Player local = Players.getLocal();
        if (local == null || local.getWorldLocation() == null) {
            return null;
        }
        WorldPoint here = local.getWorldLocation();
        Set<String> watched = parseWatchList();
        boolean hasWatch = !watched.isEmpty();
        boolean panic = this.config.proximityPanic();
        if (!hasWatch && !panic) {
            return null;
        }

        boolean distanceGate = this.config.watchlistDistanceEnabled();
        int watchDist = this.config.watchlistDistance();
        int panicDist = this.config.panicDistance();
        boolean ignoreFriends = this.config.panicIgnoreFriends();
        List<IgnoreZone> zones = parseIgnoreZones();

        Detection closestPanic = null;
        for (Player other : Players.getAll()) {
            if (other == null || other == local || other.getName() == null || other.getWorldLocation() == null) {
                continue;
            }
            WorldPoint their = other.getWorldLocation();
            int dist = their.distanceTo(here);
            String cleaned = cleanName(other.getName());
            String key = nameKey(cleaned);

            if (hasWatch && isWatched(key, cleaned, watched)) {
                if (distanceGate && dist > watchDist) {
                    continue;
                }
                if (inIgnoreZone(their, zones)) {
                    OxConsole.info("StaffAlert", "saw " + cleaned + " @" + dist
                        + "t but ignored (ignore-zone)");
                    continue;
                }
                return new Detection(other.getName(), cleaned, their, dist, Trigger.STAFF_WATCH);
            }
            if (panic
                && dist <= panicDist
                && !(ignoreFriends && isFriendly(other))
                && (closestPanic == null || dist < closestPanic.distance)) {
                closestPanic = new Detection(other.getName(), cleaned, their, dist, Trigger.PROXIMITY);
            }
        }
        return closestPanic;
    }

    List<NearbyPlayer> listNearby(Client client, int limit) {
        Player local = Players.getLocal();
        if (local == null || local.getWorldLocation() == null) {
            return Collections.emptyList();
        }
        WorldPoint here = local.getWorldLocation();
        Set<String> watched = parseWatchList();
        ArrayList<NearbyPlayer> list = new ArrayList<>();
        for (Player other : Players.getAll()) {
            if (other == null || other == local || other.getName() == null || other.getWorldLocation() == null) {
                continue;
            }
            String cleaned = cleanName(other.getName());
            int dist = other.getWorldLocation().distanceTo(here);
            list.add(new NearbyPlayer(
                cleaned,
                dist,
                isWatched(nameKey(cleaned), cleaned, watched),
                isFriendly(other)
            ));
        }
        list.sort(Comparator.comparingInt(p -> p.distance));
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    int watchedCount() {
        return parseWatchList().size() / 2; // we store key + spaced for each; count unique keys
    }

    /** Unique name-keys only (for overlay). */
    int uniqueWatchCount() {
        Set<String> keys = new HashSet<>();
        for (String entry : parseWatchList()) {
            if (entry != null && entry.equals(nameKey(entry))) {
                keys.add(entry);
            }
        }
        // fallback: all entries that look like keys (no spaces)
        if (keys.isEmpty()) {
            for (String entry : parseWatchList()) {
                if (entry != null && !entry.contains(" ")) {
                    keys.add(entry);
                }
            }
        }
        return Math.max(keys.size(), parseWatchEntries().size());
    }

    List<String> parseWatchEntries() {
        ArrayList<String> entries = new ArrayList<>();
        String raw = liveWatchListRaw();
        for (String piece : raw.split("[,\\n;]")) {
            String cleaned = cleanName(piece);
            if (!cleaned.isEmpty()) {
                entries.add(cleaned);
            }
        }
        return entries;
    }

    private List<IgnoreZone> parseIgnoreZones() {
        ArrayList<IgnoreZone> zones = new ArrayList<>();
        if (this.config.ignoreAtAfkZones()) {
            int radius = Math.max(1, this.config.afkZoneRadius());
            for (int[] centre : AfkZones.AFK_CENTRES) {
                zones.add(new IgnoreZone(centre[0], centre[1], radius));
            }
        }
        if (this.config.ignoreAtHome()) {
            int radius = Math.max(1, this.config.homeRadius());
            for (int[] centre : AfkZones.HOME_CENTRES) {
                zones.add(new IgnoreZone(centre[0], centre[1], radius));
            }
        }
        if (this.config.ignoreZonesEnabled()) {
            addZone(zones, this.config.ignoreZone1(), this.config.ignoreZone1Radius());
            addZone(zones, this.config.ignoreZone2(), this.config.ignoreZone2Radius());
            addZone(zones, this.config.ignoreZone3(), this.config.ignoreZone3Radius());
        }
        return zones;
    }

    private static void addZone(List<IgnoreZone> zones, String coords, int radius) {
        if (coords == null || radius < 1) {
            return;
        }
        String[] parts = coords.trim().split("[^-0-9]+");
        int x = 0;
        int y = 0;
        int n = 0;
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            try {
                int value = Integer.parseInt(part);
                if (n == 0) {
                    x = value;
                } else if (n == 1) {
                    y = value;
                }
                ++n;
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        if (n >= 2) {
            zones.add(new IgnoreZone(x, y, radius));
        }
    }

    private static boolean inIgnoreZone(WorldPoint point, List<IgnoreZone> zones) {
        for (IgnoreZone zone : zones) {
            if (zone.contains(point)) {
                return true;
            }
        }
        return false;
    }

    private String liveWatchListRaw() {
        try {
            String fromCm = this.configManager.getConfiguration("oxStaffAlert", "staffNames");
            if (fromCm != null && !fromCm.trim().isEmpty()) {
                return fromCm;
            }
        } catch (Throwable ignored) {
            // fall through
        }
        String fromConfig = this.config.staffNames();
        if (fromConfig != null && !fromConfig.trim().isEmpty()) {
            return fromConfig;
        }
        return AfkZones.DEFAULT_STAFF;
    }

    private Set<String> parseWatchList() {
        String raw = liveWatchListRaw();
        if (raw.equals(this.lastRawList)) {
            return this.watchKeys;
        }
        HashSet<String> set = new HashSet<>();
        for (String piece : raw.split("[,\\n;]")) {
            String cleaned = cleanName(piece);
            if (cleaned.isEmpty()) {
                continue;
            }
            set.add(nameKey(cleaned));
            set.add(cleaned.toLowerCase(Locale.ROOT));
        }
        this.lastRawList = raw;
        this.watchKeys = set;
        return set;
    }

    private static boolean isWatched(String playerKey, String cleanedDisplay, Set<String> watched) {
        if (playerKey == null || playerKey.isEmpty() || watched.isEmpty()) {
            return false;
        }
        if (watched.contains(playerKey)) {
            return true;
        }
        String spaced = cleanedDisplay == null ? "" : cleanedDisplay.toLowerCase(Locale.ROOT);
        if (!spaced.isEmpty() && watched.contains(spaced)) {
            return true;
        }
        for (String entry : watched) {
            if (entry == null || entry.length() < 2) {
                continue;
            }
            String entryKey = nameKey(entry);
            if (entryKey.isEmpty()) {
                continue;
            }
            if (playerKey.equals(entryKey)
                || playerKey.contains(entryKey)
                || entryKey.contains(playerKey)) {
                return true;
            }
        }
        return false;
    }

    static String cleanName(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9 ]", "").trim();
    }

    static String nameKey(String cleaned) {
        if (cleaned == null) {
            return "";
        }
        return cleaned.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static boolean isFriendly(Player player) {
        try {
            return player.isFriend() || player.isFriendsChatMember() || player.isClanMember();
        } catch (Throwable t) {
            return false;
        }
    }

    enum Trigger {
        STAFF_WATCH,
        PROXIMITY
    }

    static final class Detection {
        final String rawName;
        final String cleanedName;
        final WorldPoint location;
        final int distance;
        final Trigger trigger;

        Detection(String rawName, String cleanedName, WorldPoint location, int distance, Trigger trigger) {
            this.rawName = rawName;
            this.cleanedName = cleanedName;
            this.location = location;
            this.distance = distance;
            this.trigger = trigger;
        }
    }

    static final class NearbyPlayer {
        final String name;
        final int distance;
        final boolean watched;
        final boolean friendly;

        NearbyPlayer(String name, int distance, boolean watched, boolean friendly) {
            this.name = name;
            this.distance = distance;
            this.watched = watched;
            this.friendly = friendly;
        }
    }

    static final class IgnoreZone {
        final int x;
        final int y;
        final int radius;

        IgnoreZone(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        boolean contains(WorldPoint point) {
            return Math.max(Math.abs(point.getX() - this.x), Math.abs(point.getY() - this.y)) <= this.radius;
        }
    }
}
