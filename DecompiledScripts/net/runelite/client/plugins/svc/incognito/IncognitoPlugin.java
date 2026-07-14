/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.DrawManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.incognito;

import com.google.inject.Binder;
import java.io.File;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.IdentitySnapshot;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.ShieldLoginGate;
import net.runelite.client.plugins.svc.incognito.IncognitoConfig;
import net.runelite.client.plugins.svc.incognito.IncognitoOverlay;
import net.runelite.client.plugins.svc.incognito.PlayerWatcher;
import net.runelite.client.plugins.svc.incognito.PresenceGuard;
import net.runelite.client.plugins.svc.incognito.ShieldReaction;
import net.runelite.client.plugins.svc.incognito.ShieldScreenshot;
import net.runelite.client.plugins.svc.incognito.ShieldWebhook;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.input.Keyboard;

@PluginDescriptor(name="<html><!--1--><nobr><font color='#FFB000'>\u25cf x64 Incognito Shield</font></nobr></html>", description="Runtime privacy layer \u2014 verifies the agent's game-login identity spoof (MAC/uid/java), keeps presence signals human, and runs the Player-Watch logout shield", tags={"orion", "incognito", "privacy", "spoof", "hwid", "stealth", "proxy"}, enabledByDefault=true)
public class IncognitoPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private IncognitoConfig config;
    @Inject
    private DrawManager drawManager;
    private IncognitoOverlay overlay;
    private long startTime;
    private PresenceGuard presence;
    private PlayerWatcher watcher;
    private ShieldWebhook webhook;
    private long reArmAt;
    private volatile boolean awaitingShot;
    private volatile long shotRequestedAt;
    private volatile PlayerWatcher.Detection pendingDetection;
    private final AtomicBoolean triggerHandled = new AtomicBoolean(true);
    private PlayerWatcher.Detection lastDetection;
    private long lastDetectionAt;
    private String fakeMachineId;
    private String fakeMac;
    private String fakeHostname;
    private boolean prelaunchActive;
    private String liveJavaVendor = "?";
    private String liveJavaVersion = "?";
    private String liveCacheDir = "(default)";
    private String gameUid = "?";
    private String gameRevision = "?";
    private String liveMac = "?";
    private boolean macSpoofOk;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(IncognitoConfig.class).toInstance((Object)((IncognitoConfig)configManager.getConfig(IncognitoConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.startTime = System.currentTimeMillis();
        this.prelaunchActive = "true".equals(System.getProperty("incognito.active", "false"));
        this.fakeMachineId = System.getProperty("incognito.machineId", "");
        this.fakeMac = System.getProperty("incognito.mac", "");
        this.fakeHostname = System.getProperty("incognito.hostname", "");
        if (!this.prelaunchActive) {
            System.out.println("[Incognito] WARNING: Pre-launcher did not run \u2014 the agent may not have spoofed identity!");
            System.out.println("[Incognito] Launch via run.bat or LaunchOrionDev.bat for full protection.");
        }
        this.presence = new PresenceGuard(this.client, this.config);
        this.watcher = new PlayerWatcher(this.config);
        this.webhook = new ShieldWebhook();
        this.reArmAt = 0L;
        this.awaitingShot = false;
        this.pendingDetection = null;
        this.triggerHandled.set(true);
        this.lastDetection = null;
        this.overlay = new IncognitoOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.captureGameLoginIdentity();
        System.out.println("[Incognito] ========================================");
        System.out.println("[Incognito] SHIELD ACTIVATED (identity spoof is done by the agent at premain)");
        System.out.println("[Incognito] Pre-launch:      " + (this.prelaunchActive ? "ACTIVE" : "NOT RUNNING"));
        System.out.println("[Incognito] Username:         " + System.getProperty("user.name"));
        System.out.println("[Incognito] Hostname:         " + this.fakeHostname);
        System.out.println("[Incognito] Machine ID (cfg): " + this.fakeMachineId);
        System.out.println("[Incognito] --- GAME LOGIN (what the server actually sees) ---");
        System.out.println("[Incognito] MAC (live):       " + this.liveMac + (this.fakeMac.isEmpty() ? "" : (this.macSpoofOk ? "  [spoof OK]" : "  *** REAL MAC LEAKING ***")));
        System.out.println("[Incognito] Java (live):      " + this.liveJavaVendor + " " + this.liveJavaVersion);
        System.out.println("[Incognito] Cache dir:        " + this.liveCacheDir);
        System.out.println("[Incognito] Game UID:         " + this.gameUid);
        System.out.println("[Incognito] Client revision:  " + this.gameRevision + " (SHARED by all Orion users \u2014 not spoofable)");
        System.out.println("[Incognito] ========================================");
    }

    @Override
    protected void onStop() throws Exception {
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
            this.overlay = null;
        }
        if (this.webhook != null) {
            this.webhook.shutdown();
            this.webhook = null;
        }
        this.watcher = null;
        this.presence = null;
        this.awaitingShot = false;
        this.pendingDetection = null;
        ShieldLoginGate.clear();
        System.out.println("[Incognito] Shield stopped (agent identity spoofs remain active until JVM exit)");
    }

    @Override
    protected int loop() {
        long l = System.currentTimeMillis();
        if (this.watcher != null && (this.watcher.watchedCount() > 0 || this.config.panicEnabled())) {
            this.runPlayerWatch(l);
            return this.config.scanIntervalMs();
        }
        return this.config.monitorInterval();
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (this.presence != null) {
            this.presence.tick();
        }
    }

    private void runPlayerWatch(long l) {
        if (this.awaitingShot && l - this.shotRequestedAt > (long)this.config.screenshotTimeoutMs()) {
            this.fireTrigger(this.pendingDetection, null);
        }
        if (!Game.isLoggedIn()) {
            return;
        }
        if (l < this.reArmAt || this.awaitingShot) {
            return;
        }
        PlayerWatcher.Detection detection = this.watcher.scan(this.client);
        if (detection == null) {
            return;
        }
        this.reArmAt = l + (long)this.config.reArmSeconds() * 1000L;
        this.lastDetection = detection;
        this.lastDetectionAt = l;
        this.triggerHandled.set(false);
        if (this.config.includeScreenshot() && this.drawManager != null) {
            this.pendingDetection = detection;
            this.shotRequestedAt = l;
            this.awaitingShot = true;
            ShieldScreenshot.requestShot(this.client, this.drawManager, detection.location, detection.rawName, byArray -> this.fireTrigger(detection, (byte[])byArray));
        } else {
            this.fireTrigger(detection, null);
        }
    }

    private void fireTrigger(PlayerWatcher.Detection detection, byte[] byArray) {
        if (detection == null || !this.triggerHandled.compareAndSet(false, true)) {
            return;
        }
        this.awaitingShot = false;
        this.pendingDetection = null;
        boolean bl = this.config.playerWatchEnabled();
        ShieldReaction shieldReaction = this.config.reactionMode();
        String string = this.describeAction(shieldReaction, bl);
        try {
            this.webhook.notifyDetection(detection, this.localName(), this.currentWorld(), string, byArray);
        }
        catch (Throwable throwable) {
            System.err.println("[Incognito] alert dispatch failed: " + throwable.getMessage());
        }
        if (bl) {
            try {
                if (shieldReaction == ShieldReaction.TELEPORT_HOME) {
                    Keyboard.type("::home");
                    Keyboard.type('\n');
                } else {
                    ShieldLoginGate.blockFor((long)this.config.logoutCooldownMinutes() * 60000L);
                    Game.logout();
                }
            }
            catch (Throwable throwable) {
                System.err.println("[Incognito] reaction (" + String.valueOf((Object)shieldReaction) + ") failed: " + throwable.getMessage());
            }
        }
        System.out.println("[Incognito] PLAYER WATCH " + String.valueOf((Object)detection.trigger) + " -> " + string + ": [" + detection.rawName + "] @ " + detection.distance + " tiles");
    }

    private String describeAction(ShieldReaction shieldReaction, boolean bl) {
        int n;
        if (!bl) {
            return "Alert only (Player Watch reaction disabled)";
        }
        if (shieldReaction == ShieldReaction.TELEPORT_HOME) {
            return "Teleported home (::home)";
        }
        return "Logged out \u2014 staying offline " + n + ((n = this.config.logoutCooldownMinutes()) == 1 ? " min" : " mins");
    }

    private int currentWorld() {
        try {
            return this.client.getWorld();
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private String localName() {
        try {
            Player player = this.client.getLocalPlayer();
            if (player != null && player.getName() != null) {
                return player.getName().replaceAll("<[^>]*>", "").trim();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "?";
    }

    public boolean isPrelaunchActive() {
        return this.prelaunchActive;
    }

    public String getFakeHostname() {
        return this.fakeHostname;
    }

    public String getLiveJavaVendor() {
        return this.liveJavaVendor;
    }

    public String getLiveJavaVersion() {
        return this.liveJavaVersion;
    }

    public String getLiveCacheDir() {
        return this.liveCacheDir;
    }

    public String getGameUid() {
        return this.gameUid;
    }

    public String getGameRevision() {
        return this.gameRevision;
    }

    public String getLiveMac() {
        return this.liveMac;
    }

    public boolean isMacSpoofOk() {
        return this.macSpoofOk;
    }

    private void captureGameLoginIdentity() {
        this.liveJavaVendor = System.getProperty("java.vendor", "?");
        this.liveJavaVersion = System.getProperty("java.version", "?");
        this.liveCacheDir = System.getProperty("unethicalite.cache-dir", "(default)");
        this.gameUid = this.computeGameUid();
        this.gameRevision = this.readClientRevision();
        this.liveMac = this.computeLiveMac();
        boolean bl = this.macSpoofOk = !this.fakeMac.isEmpty() && !this.liveMac.equals("?") && (this.liveMac.equalsIgnoreCase(this.fakeMac) || IncognitoPlugin.sameOui(this.liveMac, this.fakeMac));
        if (!this.fakeMac.isEmpty() && !this.macSpoofOk) {
            System.err.println("[Incognito] *** MAC SPOOF FAILED *** live NIC MAC=" + this.liveMac + " != fake=" + this.fakeMac + " \u2014 the REAL hardware MAC is leaking in the login block!");
        }
        IdentitySnapshot.set(this.liveMac, this.macSpoofOk, this.gameUid, this.liveCacheDir, this.liveJavaVendor, this.liveJavaVersion, this.gameRevision);
    }

    private String computeLiveMac() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration != null && enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                try {
                    byte[] byArray;
                    if (networkInterface.isLoopback() || !networkInterface.isUp() || (byArray = networkInterface.getHardwareAddress()) == null || byArray.length != 6) continue;
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < byArray.length; ++i) {
                        if (i > 0) {
                            stringBuilder.append(':');
                        }
                        stringBuilder.append(String.format("%02X", byArray[i]));
                    }
                    return stringBuilder.toString();
                }
                catch (Throwable throwable) {
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "?";
    }

    private static boolean sameOui(String string, String string2) {
        if (string == null || string2 == null || string.length() < 8 || string2.length() < 8) {
            return false;
        }
        return string.substring(0, 8).equalsIgnoreCase(string2.substring(0, 8));
    }

    private String computeGameUid() {
        try {
            Class<?> clazz = Class.forName("net.unethicalite.client.Orion");
            File file = (File)clazz.getMethod("getCacheDirectory", new Class[0]).invoke(null, new Object[0]);
            File file2 = new File(file, "random.dat");
            if (!file2.isFile()) {
                return "none@" + file.getName();
            }
            byte[] byArray = Files.readAllBytes(file2.toPath());
            byte[] byArray2 = MessageDigest.getInstance("SHA-256").digest(byArray);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 4; ++i) {
                stringBuilder.append(String.format("%02x", byArray2[i]));
            }
            return String.valueOf(stringBuilder) + " (" + byArray.length + "b)";
        }
        catch (Throwable throwable) {
            return "?";
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String readClientRevision() {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/net/runelite/client/runelite.properties");){
            if (inputStream == null) return "?";
            Properties properties = new Properties();
            properties.load(inputStream);
            String string = properties.getProperty("runescape.version", "?");
            return string;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return "?";
    }

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - this.startTime) / 1000L;
    }

    public boolean isPresenceActive() {
        return this.presence != null;
    }

    public boolean getPresenceFocused() {
        return this.presence != null && this.presence.lastFocused();
    }

    public int getPresenceMouseIdle() {
        return this.presence == null ? -1 : this.presence.lastMouseIdle();
    }

    public int getPresenceKbIdle() {
        return this.presence == null ? -1 : this.presence.lastKbIdle();
    }

    public long getPresenceCorrections() {
        return this.presence == null ? 0L : this.presence.corrections();
    }

    public int getPresenceRecorderSpread() {
        return this.presence == null ? -1 : this.presence.recorderSpread();
    }

    public long getPresenceFidgets() {
        return this.presence == null ? 0L : this.presence.fidgets();
    }

    public int getWatchedCount() {
        return this.watcher == null ? 0 : this.watcher.watchedCount();
    }

    public int getIgnoreZoneCount() {
        return this.watcher == null ? 0 : this.watcher.ignoreZoneCount();
    }

    public boolean isAwaitingShot() {
        return this.awaitingShot;
    }

    public long getCooldownSeconds() {
        long l = this.reArmAt - System.currentTimeMillis();
        return l > 0L ? (l + 999L) / 1000L : 0L;
    }

    public String getLastDetectionText() {
        if (this.lastDetection == null) {
            return null;
        }
        return this.lastDetection.rawName + " (" + String.valueOf((Object)this.lastDetection.trigger) + ", " + this.lastDetection.distance + "t)";
    }

    public List<PlayerWatcher.NearbyPlayer> getNearbyPlayers(int n) {
        return this.watcher == null ? Collections.emptyList() : this.watcher.listNearby(this.client, n);
    }

    public ShieldReaction getReactionMode() {
        return this.config.reactionMode();
    }

    public boolean isWebhookConfigured() {
        return ShieldWebhook.isConfigured();
    }

    public long getStayLoggedOutSeconds() {
        return ShieldLoginGate.remainingSeconds();
    }
}

