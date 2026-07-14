package net.runelite.client.plugins.ox.staffalert;

import com.google.inject.Binder;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ox.core.OxConsole;
import net.runelite.client.plugins.ox.core.OxLoginGate;
import net.runelite.client.plugins.ox.core.OxLoopedPlugin;
import net.runelite.client.plugins.ox.core.OxNotify;
import net.runelite.client.plugins.ox.core.OxPluginRegistry;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>0xStaffAlert</font></nobr></html>",
    description = "Instant logout when any watched username (or proximity panic) is nearby. Add names in config - freezes AutoLogin afterwards.",
    tags = {"0x", "staff", "alert", "logout", "shield", "watch", "panic"},
    enabledByDefault = true
)
public class StaffAlertPlugin extends OxLoopedPlugin {
    private static final Logger log = LoggerFactory.getLogger(StaffAlertPlugin.class);

    @Inject
    private Client client;
    @Inject
    private StaffAlertConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ConfigManager configManager;

    private StaffScanner scanner;
    private StaffAlertOverlay overlay;
    private long reArmAtMs = 0L;
    private volatile String lastEvent = "Armed";
    private volatile String lastTarget = "-";
    private volatile long lastTriggerAtMs = 0L;
    private volatile int lastCooldownMinutes = 0;
    private int logoutClickAttempts = 0;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(StaffAlertConfig.class).toInstance(cm.getConfig(StaffAlertConfig.class));
    }

    @Override
    protected void onStart() {
        seedDefaultStaffIfNeeded();
        migrateIgnoreDefaultsOff();
        this.scanner = new StaffScanner(this.config, this.configManager);
        this.overlay = new StaffAlertOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        this.lastEvent = "Armed";
        this.logoutClickAttempts = 0;
        OxPluginRegistry.register("0xStaffAlert");
        List<String> names = this.scanner.parseWatchEntries();
        OxConsole.info("StaffAlert", "armed  watch=" + names.size()
            + "  names=[" + String.join(", ", names.size() > 8 ? names.subList(0, 8) : names) + (names.size() > 8 ? ", ..." : "") + "]"
            + "  homeIgnore=" + this.config.ignoreAtHome()
            + "  afkIgnore=" + this.config.ignoreAtAfkZones()
            + "  reactions=" + this.config.enabled());
    }

    private void seedDefaultStaffIfNeeded() {
        try {
            String saved = this.configManager.getConfiguration("oxStaffAlert", "staffNames");
            if (saved == null || saved.trim().isEmpty()) {
                this.configManager.setConfiguration("oxStaffAlert", "staffNames", AfkZones.DEFAULT_STAFF);
            }
        } catch (Throwable t) {
            log.debug("Could not seed staff list", t);
        }
    }

    private void migrateIgnoreDefaultsOff() {
        try {
            String migrated = this.configManager.getConfiguration("oxStaffAlert", "oxIgnoreZonesMigrated");
            if (migrated != null && !migrated.isEmpty()) {
                return;
            }
            this.configManager.setConfiguration("oxStaffAlert", "ignoreAtHome", "false");
            this.configManager.setConfiguration("oxStaffAlert", "ignoreAtAfkZones", "false");
            this.configManager.setConfiguration("oxStaffAlert", "oxIgnoreZonesMigrated", "1");
            OxConsole.info("StaffAlert", "reset Ignore-at-Home/AFK to OFF");
        } catch (Throwable t) {
            log.debug("Ignore-zone migrate failed", t);
        }
    }

    @Override
    protected void onStop() {
        this.overlayManager.remove((Overlay) this.overlay);
        this.overlay = null;
        this.scanner = null;
    }

    @Subscribe
    public void onPlayerSpawned(PlayerSpawned event) {
        if (event == null || event.getPlayer() == null || this.scanner == null) {
            return;
        }
        if (!this.config.enabled() || !Game.isLoggedIn()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.reArmAtMs) {
            return;
        }
        // Instant check on spawn - same scan path as the loop
        StaffScanner.Detection detection = this.scanner.scan(this.client);
        if (detection != null) {
            triggerLogout(detection, now, "player spawn");
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        // If we opened logout tab but are still logged in, keep clicking logout
        if (this.logoutClickAttempts > 0 && Game.isLoggedIn()) {
            forceLogoutClick();
        }
    }

    @Override
    protected int loop() {
        long now = System.currentTimeMillis();

        if (OxLoginGate.isBlocked()
            && this.config.extendIfStillNear()
            && OxLoginGate.remainingMs() < 20_000L
            && Game.isLoggedIn()) {
            StaffScanner.Detection lingering = this.scanner.scan(this.client);
            if (lingering != null) {
                this.triggerLogout(lingering, now, "extended - still nearby");
                return this.config.scanIntervalMs();
            }
        }

        if (!Game.isLoggedIn()) {
            this.logoutClickAttempts = 0;
            return Math.max(200, this.config.scanIntervalMs());
        }

        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return this.config.scanIntervalMs();
        }

        if (!this.config.enabled()) {
            this.lastEvent = "Reactions OFF - enable in 0xStaffAlert config";
            return 1000;
        }

        if (this.scanner.uniqueWatchCount() == 0 && !this.config.proximityPanic()) {
            this.lastEvent = "Watchlist empty - add usernames";
            return 1000;
        }

        if (now < this.reArmAtMs) {
            return this.config.scanIntervalMs();
        }

        StaffScanner.Detection detection = this.scanner.scan(this.client);
        if (detection == null) {
            if (!OxLoginGate.isBlocked()) {
                this.lastEvent = "Clear (" + this.scanner.uniqueWatchCount() + " watched)";
            }
            return this.config.scanIntervalMs();
        }

        this.triggerLogout(detection, now, "watchlist hit");
        return this.config.scanIntervalMs();
    }

    private void triggerLogout(StaffScanner.Detection detection, long now, String why) {
        this.reArmAtMs = now + (long) this.config.reArmSeconds() * 1000L;
        int minutes = rollCooldownMinutes();
        this.lastCooldownMinutes = minutes;
        this.lastTriggerAtMs = now;
        this.lastTarget = detection.cleanedName + " @" + detection.distance + "t (" + detection.trigger + ")";
        this.lastEvent = why;

        long blockMs = (long) minutes * 60_000L;
        OxLoginGate.blockFor(blockMs, detection.trigger + ":" + detection.cleanedName);

        log.warn("0xStaffAlert TRIGGER {} -> logout {} min | target={} dist={} | {}",
            detection.trigger, minutes, detection.cleanedName, detection.distance, why);
        OxConsole.alert("StaffAlert", "LOGOUT  " + detection.cleanedName
            + " @" + detection.distance + "t  for " + minutes + "m  (" + why + ")");
        OxNotify.staffAlert(
            detection.cleanedName, detection.distance, minutes, why);

        this.logoutClickAttempts = 8;
        forceLogoutClick();
    }

    /**
     * Game.logout() often only opens the logout tab and never clicks.
     * Keep clicking Logout until we leave the world.
     */
    private void forceLogoutClick() {
        if (!Game.isLoggedIn()) {
            this.logoutClickAttempts = 0;
            return;
        }
        if (this.logoutClickAttempts <= 0) {
            return;
        }
        --this.logoutClickAttempts;
        try {
            Widget hopper = Widgets.get(69, w -> w != null && w.hasAction("Logout"));
            if (hopper != null) {
                hopper.interact("Logout");
                return;
            }
            Widget tabLogout = Widgets.get(182, w -> w != null && w.hasAction("Logout"));
            if (tabLogout != null) {
                tabLogout.interact("Logout");
                return;
            }
            if (!Tabs.isOpen(Tab.LOG_OUT)) {
                Tabs.open(Tab.LOG_OUT);
            }
            // try again next tick after tab opens
            Widget afterOpen = Widgets.get(182, w -> w != null && w.hasAction("Logout"));
            if (afterOpen != null) {
                afterOpen.interact("Logout");
                return;
            }
            Game.logout();
        } catch (Throwable t) {
            log.error("forceLogoutClick failed", t);
            try {
                Game.logout();
            } catch (Throwable ignored) {
                // empty
            }
        }
    }

    private int rollCooldownMinutes() {
        int min = Math.max(1, this.config.cooldownMinMinutes());
        int max = Math.max(min, this.config.cooldownMaxMinutes());
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public String getLastEvent() {
        return this.lastEvent;
    }

    public String getLastTarget() {
        return this.lastTarget;
    }

    public long getLastTriggerAtMs() {
        return this.lastTriggerAtMs;
    }

    public int getLastCooldownMinutes() {
        return this.lastCooldownMinutes;
    }

    public List<StaffScanner.NearbyPlayer> nearbyPlayers() {
        if (this.scanner == null || !Game.isLoggedIn()) {
            return Collections.emptyList();
        }
        return this.scanner.listNearby(this.client, 10);
    }

    public int watchedCount() {
        return this.scanner != null ? this.scanner.uniqueWatchCount() : 0;
    }
}
