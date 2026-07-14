/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GameObject
 *  net.runelite.api.GameState
 *  net.runelite.api.GraphicsObject
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.MenuAction
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Projectile
 *  net.runelite.api.Skill
 *  net.runelite.api.Tile
 *  net.runelite.api.TileItem
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.MenuOptionClicked
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.DrawManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.diagnostics;

import com.google.inject.Binder;
import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import java.lang.invoke.CallSite;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.diagnostics.DiagnosticsConfig;
import net.runelite.client.plugins.svc.diagnostics.DiagnosticsOverlay;
import net.runelite.client.plugins.svc.diagnostics.DiagnosticsRecorder;
import net.runelite.client.plugins.svc.diagnostics.LogTap;
import net.runelite.client.plugins.svc.diagnostics.PluginInspector;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.items.Inventory;

@PluginDescriptor(name="<html><!--4--><nobr><font color='#4FA8FF'>\u25cf x64 Diagnostics</font></nobr></html>", description="One-switch bug reporter: turn ON, reproduce your issue, turn OFF. Saves a ready-to-send report (what each bot was doing, its config, and any errors) to .orion/x64-logs/", tags={"orion", "diagnostics", "debug", "bug", "report", "log", "error", "support", "scout"}, enabledByDefault=false)
public class DiagnosticsPlugin
extends Plugin {
    private static final long SNAPSHOT_NORMAL_MS = 5000L;
    private static final long SNAPSHOT_VERBOSE_MS = 1200L;
    private static final long SHOT_NORMAL_MS = 20000L;
    private static final long SHOT_VERBOSE_MS = 8000L;
    private static final long EVENT_SHOT_THROTTLE = 4000L;
    private static final long STUCK_MS = 20000L;
    private static final long STUCK_REPEAT_MS = 20000L;
    private static final int ERROR_LEVEL_INT = 40000;
    private static final long ENV_NORMAL_MS = 10000L;
    private static final long ENV_VERBOSE_MS = 2400L;
    private static final long DAMAGE_DUMP_THROTTLE = 600L;
    private static final long IFACE_NORMAL_MS = 2000L;
    private static final long IFACE_VERBOSE_MS = 700L;
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DrawManager drawManager;
    @Inject
    private DiagnosticsConfig config;
    private DiagnosticsOverlay overlay;
    private DiagnosticsRecorder recorder;
    private LogTap logTap;
    private long startMs;
    private final Map<String, String> lastStatus = new HashMap<String, String>();
    private final Map<String, Long> statusSince = new HashMap<String, Long>();
    private final Map<String, Long> lastStuckWarn = new HashMap<String, Long>();
    private Set<String> prevEnabled = new HashSet<String>();
    private long lastSnapshotMs = 0L;
    private long lastPeriodicShotMs = 0L;
    private long lastEventShotMs = 0L;
    private long lastEnvMs = 0L;
    private long lastDamageDumpMs = 0L;
    private int lastHp = -1;
    private boolean loggedPlayerName = false;
    private long lastInterfaceMs = 0L;
    private long lastInvMs = 0L;
    private final Set<String> seenInterfaceSigs = new HashSet<String>();
    private String lastInvSig = "";

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(DiagnosticsConfig.class).toInstance((Object)((DiagnosticsConfig)configManager.getConfig(DiagnosticsConfig.class)));
    }

    protected void startUp() {
        this.startMs = System.currentTimeMillis();
        File file = new File(System.getProperty("user.home"), ".orion/x64-logs");
        file.mkdirs();
        this.recorder = new DiagnosticsRecorder(file, this.startMs);
        this.writeHeader();
        this.logTap = LogTap.attach(this::onCapturedLog, this.config.verbose());
        if (this.logTap == null) {
            this.recorder.line(this.safeTick(), "NOTE", "Log capture could not start \u2014 crashes may not be recorded.");
        }
        this.prevEnabled = this.currentEnabledNames();
        this.overlay = new DiagnosticsOverlay(this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.recorder.line(this.safeTick(), "NOTE", "Recording started. Reproduce your issue, then turn this plugin OFF.");
    }

    protected void shutDown() {
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
            this.overlay = null;
        }
        if (this.logTap != null) {
            this.logTap.detach();
            this.logTap = null;
        }
        if (this.recorder != null) {
            File file;
            long l = Math.max(0L, (System.currentTimeMillis() - this.startMs) / 1000L);
            String string = "\n========================================================================\n  RECORDING STOPPED\n========================================================================\n  Duration : " + l / 60L + "m " + l % 60L + "s\n  Events   : " + this.recorder.events + "\n  Errors   : " + this.recorder.errors + "\n  Shots    : " + this.recorder.shots + "\n  Send the .zip in this folder to the x64 team. Thank you!\n========================================================================";
            File file2 = this.recorder.closeAndZip(string);
            File file3 = file = file2 != null ? file2.getParentFile() : this.recorder.sessionDir();
            if (this.config.openFolder()) {
                this.openFolder(file);
            }
            this.recorder = null;
        }
        this.lastStatus.clear();
        this.statusSince.clear();
        this.lastStuckWarn.clear();
        this.prevEnabled.clear();
        this.seenInterfaceSigs.clear();
        this.lastInvSig = "";
        this.loggedPlayerName = false;
    }

    private void writeHeader() {
        Object object;
        int n = -1;
        GameState gameState = null;
        String string = null;
        try {
            n = this.client.getWorld();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            gameState = this.client.getGameState();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            object = this.client.getLocalPlayer();
            if (object != null) {
                string = object.getName();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        object = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.startMs));
        String string2 = System.getProperty("java.version", "?");
        String string3 = System.getProperty("os.name", "?") + " " + System.getProperty("os.version", "");
        String string4 = System.getProperty("os.arch", "?");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("========================================================================\n");
        stringBuilder.append("  x64 DIAGNOSTIC REPORT\n");
        stringBuilder.append("========================================================================\n");
        stringBuilder.append("  Session  : ").append(this.recorder.sessionName()).append("\n");
        stringBuilder.append("  Started  : ").append((String)object).append("\n");
        stringBuilder.append("  Client   : world ").append(n).append(", game state ").append(gameState).append("\n");
        stringBuilder.append("  Player   : ").append(string != null ? string : "(not logged in yet)").append("\n");
        stringBuilder.append("  Java     : ").append(string2).append("  (").append(string3.trim()).append(", ").append(string4).append(")\n");
        stringBuilder.append("  Detail   : ").append(this.config.verbose() ? "Verbose" : "Normal").append(this.config.screenshots() ? "  (screenshots ON)" : "  (screenshots OFF)").append("\n");
        stringBuilder.append("\n");
        stringBuilder.append("  HOW TO READ: timeline entries are stamped [+MM:SS tTICK] from the start\n");
        stringBuilder.append("  of recording.  STATUS = what a bot is doing;  STUCK = a bot hasn't changed\n");
        stringBuilder.append("  action in a while;  ERROR/WARN = a captured crash/warning (with stack trace);\n");
        stringBuilder.append("  STOPPED = a plugin stopped running;  SNAPSHOT = game state;  SHOT = screenshot.\n");
        stringBuilder.append("  DAMAGE = an HP drop + a dump of nearby ids at that instant;  ENV = a periodic\n");
        stringBuilder.append("  surroundings dump.  In DAMAGE/ENV: under{}/adj{} = ground-object ids on/around\n");
        stringBuilder.append("  your tile (g=GameObject gr=Ground d=Decor w=Wall);  npc[id:name@dist/szN/PET/anN];\n");
        stringBuilder.append("  items[id:name xqty@dist] = ground items on the floor (dropped boxes/loot);\n");
        stringBuilder.append("  gfx[...] = spotanim graphic ids;  proj[...] = in-flight projectile ids.\n");
        stringBuilder.append("  INTERFACE = an open menu's widget tree (group=<id> [child item<id>:name x qty \"text\"\n");
        stringBuilder.append("  {action}]), logged once per distinct content \u2014 this is how we read menu IDs like the\n");
        stringBuilder.append("  furnace/anvil smelt/smith interfaces.  INV = your inventory's item ids+names, on change.\n");
        stringBuilder.append("  CLICK = a menu option you picked: \"action\" -> target  id=<object/item id>  tile=<x,y,p of\n");
        stringBuilder.append("  the clicked object>  from=<x,y,p you stood on>  [MENU_ACTION].  This is the exact obstacle\n");
        stringBuilder.append("  order/ids/actions for an agility lap \u2014 walk a lap by hand with the bot OFF to record it.\n");
        List<Plugin> list = PluginInspector.enabledOrionPlugins(this);
        stringBuilder.append("------------------------------------------------------------------------\n");
        stringBuilder.append("  ENABLED x64 PLUGINS (").append(list.size()).append(")\n");
        stringBuilder.append("------------------------------------------------------------------------\n");
        if (list.isEmpty()) {
            stringBuilder.append("    (none)\n");
            stringBuilder.append("    >> No other x64 plugin is enabled. If you're reporting a bug with a\n");
            stringBuilder.append("       specific bot, ENABLE THAT BOT TOO, then reproduce the issue.\n");
        } else {
            for (Plugin plugin : list) {
                stringBuilder.append("    - ").append(PluginInspector.displayName(plugin)).append("\n");
            }
        }
        for (Plugin plugin : list) {
            stringBuilder.append("------------------------------------------------------------------------\n");
            stringBuilder.append("  CONFIG: ").append(PluginInspector.displayName(plugin)).append("\n");
            stringBuilder.append("------------------------------------------------------------------------\n");
            stringBuilder.append(PluginInspector.dumpConfig(plugin)).append("\n");
        }
        stringBuilder.append("========================================================================\n");
        stringBuilder.append("  TIMELINE\n");
        stringBuilder.append("========================================================================");
        this.recorder.raw(stringBuilder.toString());
    }

    private void onCapturedLog(int n, String string, String string2, String string3, String string4) {
        if (this.recorder == null) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[").append(DiagnosticsPlugin.shortLogger(string2)).append("] ").append(string3);
        if (string4 != null) {
            stringBuilder.append("\n").append(string4);
        }
        this.recorder.line(this.safeTick(), string, stringBuilder.toString());
        if (n >= 40000) {
            ++this.recorder.errors;
            this.maybeEventShot("error");
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        long l;
        if (this.recorder == null) {
            return;
        }
        Player player = this.client.getLocalPlayer();
        if (!this.loggedPlayerName && player != null && player.getName() != null) {
            this.loggedPlayerName = true;
            this.recorder.line(this.safeTick(), "NOTE", "Logged in as " + player.getName());
        }
        this.trackPluginsAndStatus();
        long l2 = System.currentTimeMillis();
        long l3 = l = this.config.verbose() ? 1200L : 5000L;
        if (player != null && l2 - this.lastSnapshotMs >= l) {
            this.lastSnapshotMs = l2;
            this.writeSnapshot(player);
        }
        if (player != null && this.config.captureEnvironment()) {
            this.captureEnvironment(player, l2);
        }
        if (player != null && this.config.captureInterfaces()) {
            this.captureInterfaces(l2);
            this.captureInventory(l2);
        }
        if (this.config.screenshots()) {
            long l4;
            long l5 = l4 = this.config.verbose() ? 8000L : 20000L;
            if (l2 - this.lastPeriodicShotMs >= l4) {
                this.lastPeriodicShotMs = l2;
                this.requestShot("periodic");
            }
        }
    }

    private void trackPluginsAndStatus() {
        List<Plugin> list = PluginInspector.enabledOrionPlugins(this);
        HashSet<String> hashSet = new HashSet<String>();
        long l = System.currentTimeMillis();
        boolean bl = this.isLoggedIn();
        for (Plugin object : list) {
            String string;
            String string2 = PluginInspector.displayName(object);
            hashSet.add(string2);
            if (!this.prevEnabled.contains(string2)) {
                this.recorder.line(this.safeTick(), "STARTED", string2 + " was enabled.");
            }
            if ((string = PluginInspector.readStatus(object)) == null) continue;
            String string3 = this.lastStatus.get(string2);
            if (!string.equals(string3)) {
                this.lastStatus.put(string2, string);
                this.statusSince.put(string2, l);
                this.lastStuckWarn.remove(string2);
                this.recorder.line(this.safeTick(), "STATUS", string2 + ": \"" + string + "\"");
                continue;
            }
            if (!bl) continue;
            long l2 = this.statusSince.getOrDefault(string2, l);
            long l3 = l - l2;
            long l4 = this.lastStuckWarn.getOrDefault(string2, 0L);
            if (l3 < 20000L || l - l4 < 20000L) continue;
            this.lastStuckWarn.put(string2, l);
            this.recorder.line(this.safeTick(), "STUCK", string2 + " has been stuck on \"" + string + "\" for " + l3 / 1000L + "s.");
            this.maybeEventShot("stuck");
        }
        for (String string : this.prevEnabled) {
            if (hashSet.contains(string)) continue;
            this.recorder.line(this.safeTick(), "STOPPED", string + " is no longer running (it was disabled or it crashed \u2014 check ERROR entries above).");
            this.lastStatus.remove(string);
            this.statusSince.remove(string);
            this.lastStuckWarn.remove(string);
        }
        this.prevEnabled = hashSet;
    }

    private void writeSnapshot(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        int n = worldPoint != null ? worldPoint.getX() >> 6 << 8 | worldPoint.getY() >> 6 : -1;
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n4 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n5 = -1;
        try {
            n5 = this.client.getEnergy() / 100;
        }
        catch (Exception exception) {
            // empty catch block
        }
        int n6 = -1;
        try {
            n6 = Inventory.getFreeSlots();
        }
        catch (Exception exception) {
            // empty catch block
        }
        Actor actor = player.getInteracting();
        String string = actor != null ? this.actorTag(actor) : "-";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("pos=").append((String)(worldPoint != null ? worldPoint.getX() + "," + worldPoint.getY() + "," + worldPoint.getPlane() : "?")).append("  region=").append(n).append("  anim=").append(player.getAnimation()).append("  hp=").append(n2).append("/").append(n3).append("  pray=").append(n4).append("  run=").append(n5).append("%").append("  invFree=").append(n6).append("  target=").append(string);
        this.recorder.line(this.safeTick(), "SNAPSHOT", stringBuilder.toString());
    }

    private void captureEnvironment(Player player, long l) {
        long l2;
        int n = this.safeHp();
        if (this.lastHp >= 0 && n >= 0 && n < this.lastHp && this.lastHp - n >= this.config.damageDumpThreshold() && l - this.lastDamageDumpMs >= 600L) {
            this.lastDamageDumpMs = l;
            this.recorder.line(this.safeTick(), "DAMAGE", "-" + (this.lastHp - n) + " hp (" + this.lastHp + "->" + n + ")   " + this.environmentSummary(player));
        }
        if (n >= 0) {
            this.lastHp = n;
        }
        long l3 = l2 = this.config.verbose() ? 2400L : 10000L;
        if (l - this.lastEnvMs >= l2) {
            this.lastEnvMs = l;
            this.recorder.line(this.safeTick(), "ENV", this.environmentSummary(player));
        }
    }

    private int safeHp() {
        try {
            return this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        }
        catch (Exception exception) {
            return -1;
        }
    }

    private String environmentSummary(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        int n = this.config.environmentRange();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("anim=").append(player.getAnimation());
        stringBuilder.append("  under{").append(this.objectsOnTile(worldPoint)).append("}");
        stringBuilder.append("  adj{").append(this.objectsAround(worldPoint, 1)).append("}");
        stringBuilder.append("  npc[").append(this.nearbyNpcs(worldPoint, n)).append("]");
        stringBuilder.append("  items[").append(this.nearbyGroundItems(worldPoint, n)).append("]");
        stringBuilder.append("  gfx").append(this.nearbyGraphics(worldPoint, n));
        stringBuilder.append("  proj").append(this.nearbyProjectiles());
        return stringBuilder.toString();
    }

    private String nearbyGroundItems(WorldPoint worldPoint, int n) {
        if (worldPoint == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int n2 = 0;
        try {
            int n3 = worldPoint.getPlane();
            int n4 = this.client.getBaseX();
            int n5 = this.client.getBaseY();
            Tile[][] tileArray = this.client.getScene().getTiles()[n3];
            for (int i = -n; i <= n && n2 < 16; ++i) {
                block3: for (int j = -n; j <= n && n2 < 16; ++j) {
                    List list;
                    Tile tile;
                    int n6 = worldPoint.getX() + i - n4;
                    int n7 = worldPoint.getY() + j - n5;
                    if (n6 < 0 || n7 < 0 || n6 >= 104 || n7 >= 104 || (tile = tileArray[n6][n7]) == null || (list = tile.getGroundItems()) == null) continue;
                    for (TileItem tileItem : list) {
                        if (tileItem == null || tileItem.getId() < 0) continue;
                        if (n2++ >= 16) {
                            stringBuilder.append("...");
                            continue block3;
                        }
                        int n8 = Math.abs(i) + Math.abs(j);
                        stringBuilder.append(tileItem.getId()).append(":").append(this.itemName(tileItem.getId())).append("x").append(Math.max(1, tileItem.getQuantity())).append("@").append(n8).append(" ");
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return stringBuilder.toString().trim();
    }

    private String objectsOnTile(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return "";
        }
        try {
            int n = worldPoint.getX() - this.client.getBaseX();
            int n2 = worldPoint.getY() - this.client.getBaseY();
            if (n < 0 || n2 < 0 || n >= 104 || n2 >= 104) {
                return "";
            }
            Tile tile = this.client.getScene().getTiles()[worldPoint.getPlane()][n][n2];
            if (tile == null) {
                return "";
            }
            LinkedHashSet<CallSite> linkedHashSet = new LinkedHashSet<CallSite>();
            GameObject[] gameObjectArray = tile.getGameObjects();
            if (gameObjectArray != null) {
                for (GameObject gameObject : gameObjectArray) {
                    if (gameObject == null || gameObject.getId() < 0) continue;
                    linkedHashSet.add((CallSite)((Object)("g" + gameObject.getId())));
                }
            }
            if (tile.getGroundObject() != null) {
                linkedHashSet.add((CallSite)((Object)("gr" + tile.getGroundObject().getId())));
            }
            if (tile.getDecorativeObject() != null) {
                linkedHashSet.add((CallSite)((Object)("d" + tile.getDecorativeObject().getId())));
            }
            if (tile.getWallObject() != null) {
                linkedHashSet.add((CallSite)((Object)("w" + tile.getWallObject().getId())));
            }
            return String.join((CharSequence)",", linkedHashSet);
        }
        catch (Exception exception) {
            return "";
        }
    }

    private String objectsAround(WorldPoint worldPoint, int n) {
        if (worldPoint == null) {
            return "";
        }
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                String string;
                if (i == 0 && j == 0 || (string = this.objectsOnTile(new WorldPoint(worldPoint.getX() + i, worldPoint.getY() + j, worldPoint.getPlane()))).isEmpty()) continue;
                for (String string2 : string.split(",")) {
                    linkedHashSet.add(string2);
                }
            }
        }
        return String.join((CharSequence)",", linkedHashSet);
    }

    private String nearbyNpcs(WorldPoint worldPoint, int n) {
        if (worldPoint == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int n2 = 0;
        try {
            for (NPC nPC : this.client.getNpcs()) {
                int n3;
                int n4;
                if (nPC == null || nPC.getWorldLocation() == null || (n4 = nPC.getWorldLocation().distanceTo(worldPoint)) > n) continue;
                if (n2++ >= 10) {
                    stringBuilder.append("...");
                    break;
                }
                stringBuilder.append(nPC.getId());
                String string = nPC.getName();
                if (string != null && !string.isEmpty()) {
                    stringBuilder.append(":").append(string);
                }
                stringBuilder.append("@").append(n4);
                NPCComposition nPCComposition = nPC.getTransformedComposition();
                if (nPCComposition != null) {
                    stringBuilder.append("/sz").append(nPCComposition.getSize());
                    if (nPCComposition.isFollower()) {
                        stringBuilder.append("/PET");
                    }
                }
                if ((n3 = nPC.getAnimation()) != -1) {
                    stringBuilder.append("/an").append(n3);
                }
                stringBuilder.append(" ");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return stringBuilder.toString().trim();
    }

    private String nearbyGraphics(WorldPoint worldPoint, int n) {
        if (worldPoint == null) {
            return "[]";
        }
        LinkedHashSet<Integer> linkedHashSet = new LinkedHashSet<Integer>();
        try {
            for (GraphicsObject graphicsObject : this.client.getGraphicsObjects()) {
                WorldPoint worldPoint2;
                if (graphicsObject == null || graphicsObject.getLocation() == null || (worldPoint2 = WorldPoint.fromLocal((Client)this.client, (LocalPoint)graphicsObject.getLocation())) == null || worldPoint2.distanceTo(worldPoint) > n) continue;
                linkedHashSet.add(graphicsObject.getId());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ((Object)linkedHashSet).toString();
    }

    private String nearbyProjectiles() {
        LinkedHashSet<Integer> linkedHashSet = new LinkedHashSet<Integer>();
        try {
            for (Projectile projectile : this.client.getProjectiles()) {
                if (projectile == null) continue;
                linkedHashSet.add(projectile.getId());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ((Object)linkedHashSet).toString();
    }

    private void captureInterfaces(long l) {
        Widget[][] widgetArray;
        long l2;
        long l3 = l2 = this.config.verbose() ? 700L : 2000L;
        if (l - this.lastInterfaceMs < l2) {
            return;
        }
        this.lastInterfaceMs = l;
        try {
            widgetArray = this.client.getWidgets();
        }
        catch (Exception exception) {
            return;
        }
        if (widgetArray == null) {
            return;
        }
        for (int i = 0; i < widgetArray.length; ++i) {
            String string;
            Object object2;
            Widget[] widgetArray2 = widgetArray[i];
            if (widgetArray2 == null || widgetArray2.length == 0) continue;
            boolean bl = false;
            for (Object object2 : widgetArray2) {
                if (object2 == null || object2.isHidden()) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            StringBuilder stringBuilder = new StringBuilder();
            int[] nArray = new int[]{0};
            for (int j = 0; j < widgetArray2.length && nArray[0] <= 60; ++j) {
                this.appendInterfaceChild(stringBuilder, j, widgetArray2[j], 0, nArray);
            }
            if (nArray[0] == 0 || !this.seenInterfaceSigs.add((String)(object2 = i + ":" + (string = stringBuilder.toString()).hashCode()))) continue;
            this.recorder.line(this.safeTick(), "INTERFACE", "group=" + i + " [" + string + "]");
        }
        if (this.seenInterfaceSigs.size() > 500) {
            this.seenInterfaceSigs.clear();
        }
    }

    private void appendInterfaceChild(StringBuilder stringBuilder, int n, Widget widget, int n2, int[] nArray) {
        Widget[] widgetArray;
        if (widget == null || n2 > 6 || nArray[0] > 60) {
            return;
        }
        int n3 = -1;
        try {
            n3 = widget.getItemId();
        }
        catch (Exception exception) {
            // empty catch block
        }
        String string = "";
        try {
            string = widget.getText() != null ? widget.getText().replaceAll("<[^>]*>", "").trim() : "";
        }
        catch (Exception exception) {
            // empty catch block
        }
        String string2 = this.firstAction(widget);
        if (n3 > 0 || !string.isEmpty() || !string2.isEmpty()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("; ");
            }
            stringBuilder.append(n);
            if (n3 > 0) {
                int n4 = 1;
                try {
                    n4 = Math.max(1, widget.getItemQuantity());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                stringBuilder.append(" item").append(n3).append(":").append(this.itemName(n3)).append("x").append(n4);
            }
            if (!string.isEmpty()) {
                stringBuilder.append(" \"").append(DiagnosticsPlugin.truncateText(string, 24)).append("\"");
            }
            if (!string2.isEmpty()) {
                stringBuilder.append(" {").append(string2).append("}");
            }
            nArray[0] = nArray[0] + 1;
        }
        try {
            widgetArray = widget.getChildren();
            if (widgetArray != null) {
                for (Widget widget2 : widgetArray) {
                    this.appendInterfaceChild(stringBuilder, n, widget2, n2 + 1, nArray);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            widgetArray = widget.getDynamicChildren();
            if (widgetArray != null) {
                for (Widget widget2 : widgetArray) {
                    this.appendInterfaceChild(stringBuilder, n, widget2, n2 + 1, nArray);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void captureInventory(long l) {
        String string;
        List<Item> list;
        long l2;
        long l3 = l2 = this.config.verbose() ? 700L : 2000L;
        if (l - this.lastInvMs < l2) {
            return;
        }
        this.lastInvMs = l;
        try {
            list = Inventory.getAll();
        }
        catch (Throwable throwable) {
            return;
        }
        if (list == null) {
            return;
        }
        LinkedHashMap<Integer, Integer> linkedHashMap = new LinkedHashMap<Integer, Integer>();
        for (Item object2 : list) {
            if (object2 == null || object2.getId() <= 0) continue;
            linkedHashMap.merge(object2.getId(), Math.max(1, object2.getQuantity()), Integer::sum);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(entry.getKey()).append(":").append(this.itemName((Integer)entry.getKey())).append(" x").append(entry.getValue());
        }
        String string2 = string = stringBuilder.length() == 0 ? "(empty)" : stringBuilder.toString();
        if (string.equals(this.lastInvSig)) {
            return;
        }
        this.lastInvSig = string;
        this.recorder.line(this.safeTick(), "INV", string);
    }

    private String firstAction(Widget widget) {
        try {
            String[] stringArray = widget.getActions();
            if (stringArray != null) {
                for (String string : stringArray) {
                    if (string == null || string.trim().isEmpty()) continue;
                    return string.trim();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "";
    }

    private String itemName(int n) {
        try {
            ItemComposition itemComposition = this.client.getItemDefinition(n);
            String string = itemComposition != null ? itemComposition.getName() : null;
            return string != null && !string.isEmpty() ? string : "?";
        }
        catch (Exception exception) {
            return "?";
        }
    }

    private static String truncateText(String string, int n) {
        if (string == null) {
            return "";
        }
        return string.length() <= n ? string : string.substring(0, n - 1) + "\u2026";
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (this.recorder == null) {
            return;
        }
        GameState gameState = gameStateChanged.getGameState();
        this.recorder.line(this.safeTick(), "STATE", String.valueOf(gameState));
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedPlayerName = false;
        }
        if (gameState != GameState.LOGGED_IN) {
            this.lastHp = -1;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
        WorldPoint worldPoint;
        WorldPoint worldPoint2;
        boolean bl;
        MenuAction menuAction;
        if (this.recorder == null || !this.config.captureClicks()) {
            return;
        }
        String string = menuOptionClicked.getMenuOption();
        if (string == null) {
            string = "";
        }
        string = DiagnosticsPlugin.stripTags(string).trim();
        String string2 = DiagnosticsPlugin.stripTags(menuOptionClicked.getMenuTarget());
        if (string2 != null) {
            string2 = string2.trim();
        }
        String string3 = (menuAction = menuOptionClicked.getMenuAction()) != null ? menuAction.name() : "?";
        int n = menuOptionClicked.getId();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\"').append(string).append('\"');
        if (string2 != null && !string2.isEmpty()) {
            stringBuilder.append(" -> ").append(string2);
        }
        boolean bl2 = bl = string3.contains("GAME_OBJECT") || string3.contains("GROUND_ITEM") || menuAction == MenuAction.WALK;
        if (bl) {
            stringBuilder.append("  id=").append(n);
            worldPoint2 = this.sceneToWorld(menuOptionClicked.getParam0(), menuOptionClicked.getParam1());
            if (worldPoint2 != null) {
                stringBuilder.append("  tile=").append(worldPoint2.getX()).append(",").append(worldPoint2.getY()).append(",").append(worldPoint2.getPlane());
            }
        } else if (string3.contains("NPC")) {
            stringBuilder.append("  npc=").append(this.npcTagByIndex(n));
        } else if (string3.contains("WIDGET") || menuOptionClicked.getWidget() != null) {
            stringBuilder.append("  widget=").append(menuOptionClicked.getWidgetId());
            if (n != -1) {
                stringBuilder.append("/op").append(n);
            }
        } else {
            stringBuilder.append("  id=").append(n);
        }
        worldPoint2 = this.client.getLocalPlayer();
        WorldPoint worldPoint3 = worldPoint = worldPoint2 != null ? worldPoint2.getWorldLocation() : null;
        if (worldPoint != null) {
            stringBuilder.append("  from=").append(worldPoint.getX()).append(",").append(worldPoint.getY()).append(",").append(worldPoint.getPlane());
        }
        stringBuilder.append("  [").append(string3).append("]");
        this.recorder.line(this.safeTick(), "CLICK", stringBuilder.toString());
    }

    private WorldPoint sceneToWorld(int n, int n2) {
        try {
            if (n < 0 || n2 < 0) {
                return null;
            }
            return new WorldPoint(this.client.getBaseX() + n, this.client.getBaseY() + n2, this.client.getPlane());
        }
        catch (Exception exception) {
            return null;
        }
    }

    private String npcTagByIndex(int n) {
        try {
            for (NPC nPC : this.client.getNpcs()) {
                if (nPC == null || nPC.getIndex() != n) continue;
                WorldPoint worldPoint = nPC.getWorldLocation();
                String string = nPC.getId() + (String)(nPC.getName() != null ? ":" + nPC.getName() : "");
                return worldPoint != null ? string + "@" + worldPoint.getX() + "," + worldPoint.getY() : string;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "idx" + n;
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (this.recorder == null) {
            return;
        }
        ChatMessageType chatMessageType = chatMessage.getType();
        if (chatMessageType == null) {
            return;
        }
        switch (chatMessageType) {
            case GAMEMESSAGE: 
            case ENGINE: 
            case SPAM: 
            case BROADCAST: 
            case WELCOME: 
            case TRADE: 
            case CONSOLE: {
                break;
            }
            default: {
                return;
            }
        }
        this.recorder.line(this.safeTick(), "CHAT", DiagnosticsPlugin.stripTags(chatMessage.getMessage()));
    }

    private void maybeEventShot(String string) {
        if (!this.config.screenshots() || this.recorder == null || this.drawManager == null) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastEventShotMs < 4000L) {
            return;
        }
        this.lastEventShotMs = l;
        this.requestShot(string);
    }

    private void requestShot(String string) {
        if (this.recorder == null || this.drawManager == null) {
            return;
        }
        int n = this.safeTick();
        this.drawManager.requestNextFrameListener(image -> {
            DiagnosticsRecorder diagnosticsRecorder = this.recorder;
            if (diagnosticsRecorder != null) {
                diagnosticsRecorder.screenshot((Image)image, n, string);
            }
        });
    }

    private void openFolder(File file) {
        if (file == null) {
            return;
        }
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }, "x64-diag-open").start();
    }

    private Set<String> currentEnabledNames() {
        HashSet<String> hashSet = new HashSet<String>();
        for (Plugin plugin : PluginInspector.enabledOrionPlugins(this)) {
            hashSet.add(PluginInspector.displayName(plugin));
        }
        return hashSet;
    }

    private boolean isLoggedIn() {
        try {
            return this.client.getGameState() == GameState.LOGGED_IN;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private String actorTag(Actor actor) {
        if (actor instanceof NPC) {
            return actor.getName() + "#" + ((NPC)actor).getId();
        }
        if (actor == this.client.getLocalPlayer()) {
            return "you";
        }
        return actor.getName() != null ? actor.getName() : "actor";
    }

    private int safeTick() {
        try {
            return this.client.getTickCount();
        }
        catch (Exception exception) {
            return -1;
        }
    }

    private static String shortLogger(String string) {
        if (string == null) {
            return "?";
        }
        int n = string.lastIndexOf(46);
        return n >= 0 && n < string.length() - 1 ? string.substring(n + 1) : string;
    }

    private static String stripTags(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("<[^>]*>", "");
    }

    String elapsedClock() {
        long l = Math.max(0L, (System.currentTimeMillis() - this.startMs) / 1000L);
        return String.format("%02d:%02d", l / 60L, l % 60L);
    }

    String watchingSummary() {
        int n = this.prevEnabled.size();
        if (n == 0) {
            return "(enable your bot!)";
        }
        if (n == 1) {
            return this.prevEnabled.iterator().next();
        }
        return n + " plugins";
    }

    int recordedEvents() {
        return this.recorder != null ? this.recorder.events : 0;
    }

    int recordedErrors() {
        return this.recorder != null ? this.recorder.errors : 0;
    }
}

