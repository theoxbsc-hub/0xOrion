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
package net.runelite.client.plugins.svc.incognito;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.svc.incognito.ShieldReaction;

@ConfigGroup(value="incognito")
public interface IncognitoConfig
extends Config {
    @ConfigSection(name="Presence / Anti-Idle", description="Keep the values the client reports to the server in a human range: focus state, mouse/keyboard idle counters, the camera, and the mouse-movement recorder. Closes the 'minimized + still gaining XP' gap. The two passive options are safe to leave on for every account; the two input-injecting options (fidget / camera) are for AFK/skilling accounts only \u2014 they can fight a PvM bot's own mouse.", position=5, closedByDefault=true)
    public static final String presence = "presence";
    @ConfigSection(name="Player Watch / Logout Shield", description="Discord-alert the instant a watched player appears (or any player gets too close), and optionally log out / teleport home in response", position=10, closedByDefault=true)
    public static final String playerWatch = "playerWatch";

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", description="Display the incognito shield overlay with protection status", position=0)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="monitorInterval", name="Monitor Interval (ms)", description="How often to re-check and re-apply spoofed fields if the client resets them", position=1)
    @Range(min=500, max=10000)
    default public int monitorInterval() {
        return 2000;
    }

    @ConfigItem(keyName="keepFocused", name="Force focus state = true", description="Each tick, write isFocused()=true. WARNING: forcing focus=true while the window is actually minimized/backgrounded is impossible for a real client (a real OS client reports unfocused when backgrounded), so combined with injected mouse motion it is a bot tell. OFF by default (2026-07-02): let focus reflect the real OS state, exactly like stock Orion.", section="presence", position=0)
    default public boolean keepFocused() {
        return false;
    }

    @ConfigItem(keyName="clampIdle", name="Clamp idle counters", description="Reset the mouse/keyboard idle counters when they climb past the ceiling. OFF by default (2026-07-02): this RSPS has no AFK auto-logout, so clamping buys nothing, and it makes the client NEVER emit the idle beacon a real client sends \u2014 an anomaly. Let the counters climb naturally, like stock Orion.", section="presence", position=1)
    default public boolean clampIdle() {
        return false;
    }

    @ConfigItem(keyName="idleCeilingTicks", name="Idle ceiling (cycles)", description="Reset the idle counter once it exceeds this many cycles. Lower = looks busier but corrects more often. A real player rarely sits perfectly still for long; ~80-150 is plausibly human.", section="presence", position=2)
    @Range(min=20, max=600)
    default public int idleCeilingTicks() {
        return 120;
    }

    @ConfigItem(keyName="fidget", name="Humanize mouse when idle", description="Inject a synthetic eased mouse path into the game canvas while idle. OFF by default (2026-07-02): this feeds the outbound mouse-recording packet with a fixed-cadence (~every fidgetSeconds), fixed-easing, robotic signature that a genuinely idle real client NEVER produces \u2014 identified as the most defensible after-hours jail tell (ran on the no-bot AFK control too). Leaving it OFF makes an idle account emit the same empty recorder stream as a stock AFK player.", section="presence", position=3)
    default public boolean fidget() {
        return false;
    }

    @ConfigItem(keyName="fidgetSeconds", name="Move mouse every ~ (sec)", description="Roughly how often to inject an idle mouse move (randomised around this value). Real players rarely leave the cursor dead-still longer than this.", section="presence", position=4)
    @Range(min=5, max=180)
    default public int fidgetSeconds() {
        return 22;
    }

    @ConfigItem(keyName="normalizeLastClick", name="Normalize last-click time", description="Rewrite the recorded last-click time to a recent value when it goes stale. OFF by default (2026-07-02): a synthetic always-recent last-click is itself fabricated telemetry a real AFK player never produces. Let it reflect the real last physical click, like stock Orion.", section="presence", position=5)
    default public boolean normalizeLastClick() {
        return false;
    }

    @ConfigItem(keyName="moveCamera", name="Nudge camera when idle", description="Auto-rotate the camera a little while idle. OFF by default (2026-07-02): the ~20s fixed-cadence synthetic camera nudge emits EVENT_CAMERA_POSITION on a robotic beat a genuinely idle real client never produces \u2014 part of the same manufactured-presence tell as fidget. Leave OFF so an idle camera stays still like a real AFK player's.", section="presence", position=6)
    default public boolean moveCamera() {
        return false;
    }

    @ConfigItem(keyName="showPresenceStats", name="Show presence readout", description="Show the live values the server would see (focus, mouse idle, keyboard idle, recorder spread) on the overlay, so you can confirm the spoof is holding and looks human.", section="presence", position=7)
    default public boolean showPresenceStats() {
        return true;
    }

    @ConfigItem(keyName="playerWatchEnabled", name="Enable Player Watch reaction", description="Gates the REACTION only (logout / teleport home). Discord alerts are sent whenever a watched player is detected (or proximity panic trips) regardless of this toggle, as long as a watch list or proximity panic is configured. Turn this on to also log out / ::home on a trigger", section="playerWatch", position=0)
    default public boolean playerWatchEnabled() {
        return false;
    }

    @ConfigItem(keyName="watchedPlayers", name="Watched Players", description="Names to log out for, separated by commas or new lines. Matched on the cleaned name (tags/symbols stripped), case-insensitive. Detects players even in invisible mode IF the server still transmits them", section="playerWatch", position=1)
    default public String watchedPlayers() {
        return "";
    }

    @ConfigItem(keyName="watchlistDistanceEnabled", name="Watchlist: distance gate", description="Only trigger on a watched player when they are within the distance below. Off = trigger anywhere in render range", section="playerWatch", position=2)
    default public boolean watchlistDistanceEnabled() {
        return false;
    }

    @ConfigItem(keyName="watchlistDistance", name="Watchlist distance (tiles)", description="Max distance to a watched player that still triggers a logout (only used when the distance gate is on)", section="playerWatch", position=3)
    @Range(min=1, max=32)
    default public int watchlistDistance() {
        return 15;
    }

    @ConfigItem(keyName="ignoreZonesEnabled", name="Watchlist: ignore zones", description="When ON, a watched player standing inside any of the coordinate+radius zones below will NOT trigger a reaction (no logout / no ::home). Only affects the watch list \u2014 proximity panic still fires", section="playerWatch", position=20)
    default public boolean ignoreZonesEnabled() {
        return false;
    }

    @ConfigItem(keyName="ignoreZone1", name="Zone 1 \u2014 coords (x, y)", description="Center tile of ignore zone 1, as \"x, y\" (e.g. 3200, 3200). Leave blank to disable this zone", section="playerWatch", position=21)
    default public String ignoreZone1() {
        return "";
    }

    @ConfigItem(keyName="ignoreZone1Radius", name="Zone 1 \u2014 radius", description="A watched player within this many tiles of zone 1's center is ignored", section="playerWatch", position=22)
    @Range(min=1, max=64)
    default public int ignoreZone1Radius() {
        return 10;
    }

    @ConfigItem(keyName="ignoreZone2", name="Zone 2 \u2014 coords (x, y)", description="Center tile of ignore zone 2, as \"x, y\". Leave blank to disable this zone", section="playerWatch", position=23)
    default public String ignoreZone2() {
        return "";
    }

    @ConfigItem(keyName="ignoreZone2Radius", name="Zone 2 \u2014 radius", description="A watched player within this many tiles of zone 2's center is ignored", section="playerWatch", position=24)
    @Range(min=1, max=64)
    default public int ignoreZone2Radius() {
        return 10;
    }

    @ConfigItem(keyName="ignoreZone3", name="Zone 3 \u2014 coords (x, y)", description="Center tile of ignore zone 3, as \"x, y\". Leave blank to disable this zone", section="playerWatch", position=25)
    default public String ignoreZone3() {
        return "";
    }

    @ConfigItem(keyName="ignoreZone3Radius", name="Zone 3 \u2014 radius", description="A watched player within this many tiles of zone 3's center is ignored", section="playerWatch", position=26)
    @Range(min=1, max=64)
    default public int ignoreZone3Radius() {
        return 10;
    }

    @ConfigItem(keyName="ignoreZone4", name="Zone 4 \u2014 coords (x, y)", description="Center tile of ignore zone 4, as \"x, y\". Leave blank to disable this zone", section="playerWatch", position=27)
    default public String ignoreZone4() {
        return "";
    }

    @ConfigItem(keyName="ignoreZone4Radius", name="Zone 4 \u2014 radius", description="A watched player within this many tiles of zone 4's center is ignored", section="playerWatch", position=28)
    @Range(min=1, max=64)
    default public int ignoreZone4Radius() {
        return 10;
    }

    @ConfigItem(keyName="ignoreZone5", name="Zone 5 \u2014 coords (x, y)", description="Center tile of ignore zone 5, as \"x, y\". Leave blank to disable this zone", section="playerWatch", position=29)
    default public String ignoreZone5() {
        return "";
    }

    @ConfigItem(keyName="ignoreZone5Radius", name="Zone 5 \u2014 radius", description="A watched player within this many tiles of zone 5's center is ignored", section="playerWatch", position=30)
    @Range(min=1, max=64)
    default public int ignoreZone5Radius() {
        return 10;
    }

    @ConfigItem(keyName="panicEnabled", name="Proximity panic (any player)", description="Independent of the watch list: log out when ANY player comes within the panic distance below", section="playerWatch", position=4)
    default public boolean panicEnabled() {
        return false;
    }

    @ConfigItem(keyName="panicDistance", name="Panic distance (tiles)", description="Log out when any player is within this many tiles", section="playerWatch", position=5)
    @Range(min=1, max=32)
    default public int panicDistance() {
        return 5;
    }

    @ConfigItem(keyName="panicIgnoreFriends", name="Panic ignores friends/clan", description="Don't trip the proximity panic on friends, friends-chat or clan members", section="playerWatch", position=6)
    default public boolean panicIgnoreFriends() {
        return true;
    }

    @ConfigItem(keyName="scanIntervalMs", name="Scan Interval (ms)", description="How often to scan nearby players while the watch is enabled. Lower = faster reaction, more CPU", section="playerWatch", position=7)
    @Range(min=100, max=2000)
    default public int scanIntervalMs() {
        return 500;
    }

    @ConfigItem(keyName="reArmSeconds", name="Re-arm cooldown (s)", description="After a trigger, wait this long before the watcher can fire (and alert) again. Stops alert/teleport spam", section="playerWatch", position=8)
    @Range(min=5, max=600)
    default public int reArmSeconds() {
        return 30;
    }

    @ConfigItem(keyName="showWatchOverlay", name="Show nearby-player list", description="List nearby player names + distances on the overlay. Use this to verify whether invisible-mode players still show up on your server", section="playerWatch", position=9)
    default public boolean showWatchOverlay() {
        return true;
    }

    @ConfigItem(keyName="reactionMode", name="On detection", description="What to do when a watched / too-close player is spotted: teleport home with ::home (stay logged in) or fully log out", section="playerWatch", position=10)
    default public ShieldReaction reactionMode() {
        return ShieldReaction.TELEPORT_HOME;
    }

    @ConfigItem(keyName="logoutCooldownMinutes", name="Stay logged out (min)", description="After a LOG OUT reaction, stay offline for this many minutes. This overrides Auto Login \u2014 it will not log back in until the cooldown expires. Ignored when the reaction is Teleport home", section="playerWatch", position=11)
    @Range(min=1, max=720)
    default public int logoutCooldownMinutes() {
        return 10;
    }

    @ConfigItem(keyName="includeScreenshot", name="Attach screenshot", description="Capture the game screen (with the target tile marked) and attach it to the Discord alert", section="playerWatch", position=12)
    default public boolean includeScreenshot() {
        return true;
    }

    @ConfigItem(keyName="screenshotTimeoutMs", name="Screenshot timeout (ms)", description="Max time to wait for the game frame before reacting anyway. Logout/teleport safety always wins over the screenshot", section="playerWatch", position=13)
    @Range(min=300, max=3000)
    default public int screenshotTimeoutMs() {
        return 1000;
    }
}

