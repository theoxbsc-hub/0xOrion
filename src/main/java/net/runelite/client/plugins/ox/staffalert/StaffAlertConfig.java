package net.runelite.client.plugins.ox.staffalert;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("oxStaffAlert")
public interface StaffAlertConfig extends Config {
    @ConfigSection(name = "Detection", description = "Who triggers an instant logout", position = 0)
    String detectionSection = "detection";

    @ConfigSection(name = "Logout / cooldown", description = "What happens after a trigger; AutoLogin stays blocked until cooldown ends", position = 1)
    String logoutSection = "logout";

    @ConfigSection(name = "Ignore zones", description = "Skip watchlist logout when staff is in AFK hubs or custom zones", position = 2, closedByDefault = true)
    String zonesSection = "zones";

    @ConfigSection(name = "Overlay", description = "On-screen readout", position = 3, closedByDefault = true)
    String overlaySection = "overlay";

    @ConfigItem(
        keyName = "enabled",
        name = "Enable reactions",
        description = "When ON, a detection instantly logs out and freezes AutoLogin for the cooldown",
        section = detectionSection,
        position = 0
    )
    default boolean enabled() {
        return true;
    }

    @ConfigItem(
        keyName = "staffNames",
        name = "Staff / watched names",
        description = "Comma or newline separated. Pre-filled with known staff; edit anytime. Blank falls back to built-in list.",
        section = detectionSection,
        position = 1
    )
    default String staffNames() {
        return AfkZones.DEFAULT_STAFF;
    }

    @ConfigItem(
        keyName = "watchlistDistanceEnabled",
        name = "Watchlist distance gate",
        description = "Only trigger on watched names within the tile distance below. Off = anywhere in render range",
        section = detectionSection,
        position = 2
    )
    default boolean watchlistDistanceEnabled() {
        return false;
    }

    @ConfigItem(
        keyName = "watchlistDistance",
        name = "Watchlist distance (tiles)",
        description = "Max tiles to a watched name when the distance gate is on",
        section = detectionSection,
        position = 3
    )
    @Range(min = 1, max = 32)
    default int watchlistDistance() {
        return 15;
    }

    @ConfigItem(
        keyName = "proximityPanic",
        name = "Proximity panic (any player)",
        description = "Logout when ANY player (not on friends/clan if ignored) comes within panic distance",
        section = detectionSection,
        position = 4
    )
    default boolean proximityPanic() {
        return false;
    }

    @ConfigItem(
        keyName = "panicDistance",
        name = "Panic distance (tiles)",
        description = "Logout when any non-ignored player is this close",
        section = detectionSection,
        position = 5
    )
    @Range(min = 1, max = 32)
    default int panicDistance() {
        return 5;
    }

    @ConfigItem(
        keyName = "panicIgnoreFriends",
        name = "Panic ignores friends/clan",
        description = "Don't panic-logout on friends, friends chat, or clan members",
        section = detectionSection,
        position = 6
    )
    default boolean panicIgnoreFriends() {
        return true;
    }

    @ConfigItem(
        keyName = "scanIntervalMs",
        name = "Scan interval (ms)",
        description = "Lower = faster detection. 150-300 recommended for staff",
        section = detectionSection,
        position = 7
    )
    @Range(min = 50, max = 2000)
    default int scanIntervalMs() {
        return 200;
    }

    @ConfigItem(
        keyName = "reArmSeconds",
        name = "Re-arm (seconds)",
        description = "Minimum gap before another logout can fire (stops spam if something fails)",
        section = detectionSection,
        position = 8
    )
    @Range(min = 3, max = 600)
    default int reArmSeconds() {
        return 10;
    }

    @ConfigItem(
        keyName = "cooldownMinMinutes",
        name = "Stay offline min (minutes)",
        description = "After logout, AutoLogin is blocked at least this long. Randomised up to the max below so timing is less fingerprinted",
        section = logoutSection,
        position = 0
    )
    @Range(min = 1, max = 720)
    default int cooldownMinMinutes() {
        return 8;
    }

    @ConfigItem(
        keyName = "cooldownMaxMinutes",
        name = "Stay offline max (minutes)",
        description = "Upper bound for randomised offline time. Set equal to min for a fixed delay",
        section = logoutSection,
        position = 1
    )
    @Range(min = 1, max = 720)
    default int cooldownMaxMinutes() {
        return 18;
    }

    @ConfigItem(
        keyName = "extendIfStillNear",
        name = "Extend gate if still nearby on login",
        description = "If a watched/panic player is still in range when the freeze would lift, extend the offline gate instead of logging in into them",
        section = logoutSection,
        position = 2
    )
    default boolean extendIfStillNear() {
        return true;
    }

    @ConfigItem(
        keyName = "ignoreAtAfkZones",
        name = "Ignore at AFK zones",
        description = "When ON, watched staff in ::afk hubs (farm/herb/fish/hunt/mine/smith/dummy) will NOT trigger logout. Off by default (x64-style: always alert unless you opt in).",
        section = zonesSection,
        position = 0
    )
    default boolean ignoreAtAfkZones() {
        return false;
    }

    @ConfigItem(
        keyName = "afkZoneRadius",
        name = "AFK zone radius",
        description = "Tiles around each AFK hub centre that count as ignored for the watchlist",
        section = zonesSection,
        position = 1
    )
    @Range(min = 5, max = 64)
    default int afkZoneRadius() {
        return AfkZones.DEFAULT_RADIUS;
    }

    @ConfigItem(
        keyName = "ignoreAtHome",
        name = "Ignore at Home",
        description = "When ON, watched staff around ::home will NOT trigger logout. Off by default - turn on only if you want staff to be ignored at home.",
        section = zonesSection,
        position = 2
    )
    default boolean ignoreAtHome() {
        return false;
    }

    @ConfigItem(
        keyName = "homeRadius",
        name = "Home radius",
        description = "Tiles around home hub centres that count as ignored for the watchlist",
        section = zonesSection,
        position = 3
    )
    @Range(min = 5, max = 64)
    default int homeRadius() {
        return AfkZones.DEFAULT_HOME_RADIUS;
    }

    @ConfigItem(
        keyName = "ignoreZonesEnabled",
        name = "Enable custom ignore zones",
        description = "Extra manual zones below (in addition to AFK / Home if those toggles are on)",
        section = zonesSection,
        position = 4
    )
    default boolean ignoreZonesEnabled() {
        return false;
    }

    @ConfigItem(keyName = "ignoreZone1", name = "Zone 1 coords (x, y)", description = "e.g. 3200, 3200", section = zonesSection, position = 5)
    default String ignoreZone1() {
        return "";
    }

    @ConfigItem(keyName = "ignoreZone1Radius", name = "Zone 1 radius", description = "Tiles around zone 1 centre", section = zonesSection, position = 6)
    @Range(min = 1, max = 64)
    default int ignoreZone1Radius() {
        return 10;
    }

    @ConfigItem(keyName = "ignoreZone2", name = "Zone 2 coords (x, y)", description = "e.g. 3200, 3200", section = zonesSection, position = 7)
    default String ignoreZone2() {
        return "";
    }

    @ConfigItem(keyName = "ignoreZone2Radius", name = "Zone 2 radius", description = "Tiles around zone 2 centre", section = zonesSection, position = 8)
    @Range(min = 1, max = 64)
    default int ignoreZone2Radius() {
        return 10;
    }

    @ConfigItem(keyName = "ignoreZone3", name = "Zone 3 coords (x, y)", description = "e.g. 3200, 3200", section = zonesSection, position = 9)
    default String ignoreZone3() {
        return "";
    }

    @ConfigItem(keyName = "ignoreZone3Radius", name = "Zone 3 radius", description = "Tiles around zone 3 centre", section = zonesSection, position = 10)
    @Range(min = 1, max = 64)
    default int ignoreZone3Radius() {
        return 10;
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show overlay",
        description = "Status + nearby players",
        section = overlaySection,
        position = 0
    )
    default boolean showOverlay() {
        return true;
    }

    @ConfigItem(
        keyName = "showNearby",
        name = "List nearby players",
        description = "Show names/distances on the overlay",
        section = overlaySection,
        position = 1
    )
    default boolean showNearby() {
        return true;
    }
}
