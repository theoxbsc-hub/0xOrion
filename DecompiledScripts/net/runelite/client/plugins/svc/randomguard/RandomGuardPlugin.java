/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.MenuAction
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.MenuOptionClicked
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.DrawManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.randomguard;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.IdentitySnapshot;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.RandomEventState;
import net.runelite.client.plugins.svc.randomguard.GuardCapture;
import net.runelite.client.plugins.svc.randomguard.GuardScreenshot;
import net.runelite.client.plugins.svc.randomguard.GuardWebhook;
import net.runelite.client.plugins.svc.randomguard.NpcInfo;
import net.runelite.client.plugins.svc.randomguard.RandomEventRecorder;
import net.runelite.client.plugins.svc.randomguard.RandomGuardConfig;
import net.runelite.client.plugins.svc.randomguard.RandomGuardOverlay;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.client.managers.interaction.InteractMethod;
import net.unethicalite.client.managers.interaction.MouseBehavior;

@PluginDescriptor(name="<html><!--1--><nobr><font color='#FFB000'>\u25cf x64 Random Event Guard</font></nobr></html>", description="Failsafe for server random events: on the 'speak to the servant \u2026 complete the random event' line it pauses your bot, screenshots + alerts Discord, records a 1-minute diagnostic of the NPC and its options, and tries to clear the event.", tags={"orion", "random", "event", "servant", "anti-ban", "failsafe", "guard", "shield"}, enabledByDefault=false)
public class RandomGuardPlugin
extends OrionLoopedPlugin {
    private static final String UNETH_GROUP = "unethicalite";
    private static final long CAPTURE_EVERY_MS = 3000L;
    private static final long SHOT_EVERY_MS = 12000L;
    private static final long INTERACT_EVERY_MS = 2500L;
    private static final long SHOT_TIMEOUT_MS = 5000L;
    private static final long PAUSE_BUFFER_MS = 8000L;
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DrawManager drawManager;
    @Inject
    private RandomGuardConfig config;
    private RandomGuardOverlay overlay;
    private GuardWebhook webhook;
    private volatile String pendingLine;
    private volatile boolean handling;
    private volatile String phase = "Watching";
    private long diagnosticEndMs;
    private volatile RandomEventRecorder recorder;
    private volatile NpcInfo eventNpc;
    private volatile String eventNpcTag;
    private volatile List<NpcInfo> nearbyAtStart = new ArrayList<NpcInfo>();
    private long lastCaptureMs;
    private long lastShotMs;
    private long lastInteractMs;
    private volatile boolean awaitingShot;
    private volatile long shotRequestedAt;
    private final AtomicBoolean alertSent = new AtomicBoolean(true);
    private long reArmAt;
    private int eventCount;
    private String lastEventText;
    private String savedInteractMethod;
    private String savedNaturalMouse;
    private String savedMouseBehavior;
    private boolean inputModeApplied;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(RandomGuardConfig.class).toInstance((Object)((RandomGuardConfig)configManager.getConfig(RandomGuardConfig.class)));
    }

    @Override
    protected void onStart() {
        this.webhook = new GuardWebhook();
        this.pendingLine = null;
        this.handling = false;
        this.phase = "Watching";
        this.recorder = null;
        this.eventNpc = null;
        this.eventNpcTag = null;
        this.nearbyAtStart = new ArrayList<NpcInfo>();
        this.awaitingShot = false;
        this.alertSent.set(true);
        this.reArmAt = 0L;
        this.eventCount = 0;
        this.lastEventText = null;
        this.inputModeApplied = false;
        this.overlay = new RandomGuardOverlay(this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
    }

    @Override
    protected void onStop() {
        if (this.handling) {
            this.finalizeHandling(true);
        }
        this.restoreInputMode();
        RandomEventState.clear();
        if (this.webhook != null) {
            this.webhook.shutdown();
            this.webhook = null;
        }
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
            this.overlay = null;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        ChatMessageType chatMessageType = chatMessage.getType();
        if (chatMessageType == null) {
            return;
        }
        switch (chatMessageType) {
            case GAMEMESSAGE: 
            case ENGINE: 
            case SPAM: 
            case BROADCAST: 
            case CONSOLE: 
            case WELCOME: {
                break;
            }
            default: {
                return;
            }
        }
        String string = chatMessage.getMessage();
        if (string == null) {
            return;
        }
        String string2 = string.replaceAll("<[^>]*>", "");
        String string3 = string2.toLowerCase();
        if (this.handling && this.recorder != null) {
            this.recorder.line("CHAT", string2);
        }
        if (this.pendingLine != null || this.handling) {
            return;
        }
        if (System.currentTimeMillis() < this.reArmAt) {
            return;
        }
        if (GuardCapture.matchesAny(string3, GuardCapture.parsePhrases(this.config.triggerPhrases()))) {
            this.pendingLine = string2.trim();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
        if (!this.handling || this.recorder == null) {
            return;
        }
        String string = RandomGuardPlugin.strip(menuOptionClicked.getMenuOption());
        String string2 = RandomGuardPlugin.strip(menuOptionClicked.getMenuTarget());
        MenuAction menuAction = menuOptionClicked.getMenuAction();
        StringBuilder stringBuilder = new StringBuilder("\"" + string + "\"");
        if (!string2.isEmpty()) {
            stringBuilder.append(" -> ").append(string2);
        }
        stringBuilder.append("  id=").append(menuOptionClicked.getId());
        stringBuilder.append("  [").append(menuAction != null ? menuAction.name() : "?").append(']');
        this.recorder.line("CLICK", stringBuilder.toString());
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1200;
        }
        if (this.handling) {
            return this.driveSession();
        }
        if (this.pendingLine != null) {
            String string = this.pendingLine;
            this.pendingLine = null;
            this.startHandling(string);
            return 300;
        }
        return 500;
    }

    private void startHandling(String string) {
        WorldPoint worldPoint;
        long l = System.currentTimeMillis();
        this.handling = true;
        this.phase = "Detecting";
        this.lastCaptureMs = 0L;
        this.lastShotMs = 0L;
        this.lastInteractMs = 0L;
        Set<String> set = GuardCapture.parseKeywords(this.config.eventNpcNames());
        int n = this.config.scanRange();
        this.eventNpc = GuardCapture.findEventNpc(this.client, set, n);
        this.eventNpcTag = this.eventNpc != null ? this.eventNpc.tag() : null;
        this.nearbyAtStart = GuardCapture.nearbyNpcs(this.client, n, 12);
        ++this.eventCount;
        this.lastEventText = (this.eventNpc != null ? this.eventNpc.name : "event") + " @ " + RandomGuardPlugin.timeNow();
        if (this.config.pauseOtherBots()) {
            long l2 = (long)this.config.diagnosticSeconds() * 1000L + 8000L;
            RandomEventState.beginHandling(l2, "random event");
        }
        if (this.config.forceRealInput()) {
            this.applyRealInput();
        }
        if (this.config.runDiagnostic()) {
            this.recorder = new RandomEventRecorder(l);
            this.writeHeader(string);
        }
        this.diagnosticEndMs = l + (long)this.config.diagnosticSeconds() * 1000L;
        this.alertSent.set(false);
        WorldPoint worldPoint2 = worldPoint = this.eventNpc != null ? this.eventNpc.location : null;
        if ((this.config.sendWebhook() || this.config.captureScreenshots()) && this.drawManager != null) {
            this.awaitingShot = true;
            this.shotRequestedAt = l;
            String string2 = string;
            GuardScreenshot.requestShot(this.client, this.drawManager, worldPoint, "EVENT NPC", byArray -> this.onEventShot((byte[])byArray, string2));
        } else {
            this.fireAlert(string, null);
        }
        System.out.println("[RandomGuard] Random event detected: \"" + string + "\"" + (String)(this.eventNpcTag != null ? "  npc=" + this.eventNpcTag : "  (no NPC found)"));
    }

    private int driveSession() {
        long l = System.currentTimeMillis();
        if (this.awaitingShot && l - this.shotRequestedAt > 5000L) {
            this.awaitingShot = false;
            this.fireAlert(this.lastEventText, null);
        }
        if (l >= this.diagnosticEndMs) {
            this.finalizeHandling(false);
            return 500;
        }
        if (this.recorder != null && l - this.lastCaptureMs >= 3000L) {
            this.lastCaptureMs = l;
            this.captureRound();
        }
        if (this.recorder != null && this.config.captureScreenshots() && this.drawManager != null && l - this.lastShotMs >= 12000L) {
            this.lastShotMs = l;
            GuardScreenshot.requestShot(this.client, this.drawManager, this.eventNpc != null ? this.eventNpc.location : null, "during", byArray -> {
                RandomEventRecorder randomEventRecorder = this.recorder;
                if (randomEventRecorder != null) {
                    randomEventRecorder.writePng((byte[])byArray, "during");
                }
            });
        }
        return this.handleInteraction(l);
    }

    private int handleInteraction(long l) {
        try {
            if (this.config.progressDialogue() && Dialog.isOpen()) {
                this.phase = "Dialogue";
                if (Dialog.isViewingOptions()) {
                    Dialog.chooseOption(1);
                    return 900;
                }
                if (Dialog.canContinue()) {
                    Dialog.continueSpace();
                    return 700;
                }
                return 500;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (this.config.interactWithNpc() && l - this.lastInteractMs >= 2500L) {
            this.lastInteractMs = l;
            NPC nPC = this.findLiveEventNpc();
            if (nPC != null) {
                this.phase = "Talking to NPC";
                String string = this.preferredAction(nPC);
                try {
                    nPC.interact(string);
                }
                catch (Throwable throwable) {
                    if (this.recorder != null) {
                        this.recorder.line("NOTE", "interact(\"" + string + "\") threw: " + String.valueOf(throwable));
                    }
                }
            } else {
                this.phase = "NPC not found";
            }
        }
        return 600;
    }

    private void captureRound() {
        int n = this.config.scanRange();
        List<NpcInfo> list = GuardCapture.nearbyNpcs(this.client, n, 16);
        StringBuilder stringBuilder = new StringBuilder();
        for (NpcInfo npcInfo : list) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(npcInfo.tag());
        }
        this.recorder.line("NPCS", list.isEmpty() ? "(none in range)" : stringBuilder.toString());
        Player player = this.client.getLocalPlayer();
        if (player != null) {
            NpcInfo npcInfo;
            npcInfo = player.getWorldLocation();
            int n2 = RandomGuardPlugin.safe(() -> this.client.getBoostedSkillLevel(Skill.HITPOINTS));
            boolean bl = false;
            try {
                bl = Dialog.isOpen();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.recorder.line("SELF", "pos=" + (String)(npcInfo != null ? npcInfo.getX() + "," + npcInfo.getY() + "," + npcInfo.getPlane() : "?") + "  anim=" + RandomGuardPlugin.safe(() -> ((Player)player).getAnimation()) + "  hp=" + n2 + "  dialogOpen=" + bl + "  phase=" + this.phase);
        }
    }

    private void finalizeHandling(boolean bl) {
        long l = System.currentTimeMillis();
        this.handling = false;
        this.phase = "Watching";
        this.awaitingShot = false;
        if (this.recorder != null) {
            long l2 = this.config.diagnosticSeconds();
            String string = "\n========================================================================\n  RECORDING " + (bl ? "ABORTED (plugin disabled)" : "COMPLETE") + "\n  Lines : " + this.recorder.lines + "   Shots : " + this.recorder.shots + "\n  Window: ~" + l2 + "s\n  This is the random event the server threw at you. The NPC's options above are the\n  exact right-click actions available on it \u2014 use them to script a proper solver.\n========================================================================";
            this.recorder.closeAndZip(string);
            if (!bl && this.config.openReport()) {
                this.recorder.openFolder();
            }
            this.recorder = null;
        }
        this.restoreInputMode();
        RandomEventState.clear();
        this.reArmAt = l + (long)this.config.reArmSeconds() * 1000L;
    }

    private void onEventShot(byte[] byArray, String string) {
        this.awaitingShot = false;
        if (byArray != null && this.recorder != null && this.config.captureScreenshots()) {
            this.recorder.writePng(byArray, "event-npc");
        }
        this.fireAlert(string, (byte[])(this.config.sendWebhook() ? byArray : null));
    }

    private void fireAlert(String string, byte[] byArray) {
        Object object;
        if (!this.alertSent.compareAndSet(false, true)) {
            return;
        }
        if (!this.config.sendWebhook() || this.webhook == null) {
            return;
        }
        ArrayList<GuardWebhook.Field> arrayList = new ArrayList<GuardWebhook.Field>();
        arrayList.add(new GuardWebhook.Field("Account", this.localName(), true));
        arrayList.add(new GuardWebhook.Field("World", String.valueOf(this.currentWorld()), true));
        if (this.eventNpc != null) {
            arrayList.add(new GuardWebhook.Field("Event NPC", this.eventNpc.id + ":" + this.eventNpc.name, true));
            arrayList.add(new GuardWebhook.Field("NPC options", this.eventNpc.optionsCsv(), true));
            object = this.eventNpc.location == null ? "?" : this.eventNpc.location.getX() + ", " + this.eventNpc.location.getY() + " P" + this.eventNpc.location.getPlane();
            arrayList.add(new GuardWebhook.Field("NPC location", (String)object, true));
            arrayList.add(new GuardWebhook.Field("Distance", this.eventNpc.distance + " tiles", true));
        } else {
            arrayList.add(new GuardWebhook.Field("Event NPC", "none found within " + this.config.scanRange() + " tiles", false));
        }
        arrayList.add(new GuardWebhook.Field("Nearby NPCs", this.nearbySummary(), false));
        object = "**Random event detected**\n`" + (string == null ? "" : string) + "`\nRecording a " + this.config.diagnosticSeconds() + "s diagnostic to `.orion/x64-logs/`.";
        this.webhook.send("\ud83c\udfb2 Random event detected", (String)object, arrayList, byArray);
    }

    private String nearbySummary() {
        if (this.nearbyAtStart.isEmpty()) {
            return "(none in range)";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int n = 0;
        Object object = this.nearbyAtStart.iterator();
        while (object.hasNext()) {
            NpcInfo npcInfo = object.next();
            if (n++ >= 6) {
                stringBuilder.append("\u2026");
                break;
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(npcInfo.id).append(':').append(npcInfo.name).append(" d=").append(npcInfo.distance).append(" [").append(npcInfo.optionsCsv()).append(']');
        }
        return ((String)(object = stringBuilder.toString())).length() > 1000 ? ((String)object).substring(0, 1000) + "\u2026" : object;
    }

    private void writeHeader(String string) {
        Player player = this.client.getLocalPlayer();
        WorldPoint worldPoint = player != null ? player.getWorldLocation() : null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("========================================================================\n");
        stringBuilder.append("  x64 RANDOM EVENT REPORT\n");
        stringBuilder.append("========================================================================\n");
        stringBuilder.append("  Session : ").append(this.recorder.sessionName()).append("\n");
        stringBuilder.append("  Account : ").append(this.localName()).append("   world ").append(this.currentWorld()).append("\n");
        stringBuilder.append("  Position: ").append((String)(worldPoint != null ? worldPoint.getX() + "," + worldPoint.getY() + "," + worldPoint.getPlane() : "?")).append("\n");
        stringBuilder.append("  Trigger : \"").append(string).append("\"\n");
        stringBuilder.append("\n");
        stringBuilder.append("  IDENTITY AT DETECTION (was the per-account spoof active + distinct?):\n");
        stringBuilder.append(IdentitySnapshot.report()).append("\n");
        stringBuilder.append("\n");
        stringBuilder.append("  EVENT NPC (the thing you must interact with):\n");
        stringBuilder.append("    ").append((String)(this.eventNpcTag != null ? this.eventNpcTag : "(no NPC found within " + this.config.scanRange() + " tiles)")).append("\n");
        stringBuilder.append("\n");
        stringBuilder.append("  ALL NEARBY NPCs AT DETECTION (id:name @x,y d=dist opts=[options]):\n");
        if (this.nearbyAtStart.isEmpty()) {
            stringBuilder.append("    (none in range)\n");
        } else {
            for (NpcInfo npcInfo : this.nearbyAtStart) {
                stringBuilder.append("    ").append(npcInfo.tag()).append("\n");
            }
        }
        stringBuilder.append("\n");
        stringBuilder.append("  HOW TO READ: timeline entries are stamped [+MM:SS] from detection. NPCS = the live NPC\n");
        stringBuilder.append("  table (re-scanned every few seconds); SELF = your state; CHAT = server chat lines; CLICK =\n");
        stringBuilder.append("  a menu option that fired; SHOT = a screenshot. opts=[...] on each NPC are the exact\n");
        stringBuilder.append("  right-click actions available \u2014 that's how you learn what to do with the servant.\n");
        stringBuilder.append("========================================================================\n");
        stringBuilder.append("  TIMELINE\n");
        stringBuilder.append("========================================================================");
        this.recorder.raw(stringBuilder.toString());
    }

    private NPC findLiveEventNpc() {
        Player player = this.client.getLocalPlayer();
        if (player == null || player.getWorldLocation() == null) {
            return null;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        int n = this.eventNpc != null ? this.eventNpc.id : -1;
        String string = this.eventNpc != null ? this.eventNpc.name.toLowerCase() : null;
        Set<String> set = GuardCapture.parseKeywords(this.config.eventNpcNames());
        int n2 = this.config.scanRange();
        NPC nPC = null;
        int n3 = Integer.MAX_VALUE;
        try {
            for (NPC nPC2 : this.client.getNpcs()) {
                int n4;
                if (nPC2 == null || nPC2.getWorldLocation() == null || (n4 = nPC2.getWorldLocation().distanceTo(worldPoint)) > n2) continue;
                boolean bl = false;
                if (n >= 0) {
                    if (RandomGuardPlugin.safe(() -> ((NPC)nPC2).getId()) == n) {
                        bl = true;
                    }
                }
                if (!bl && nPC2.getName() != null) {
                    String string2 = nPC2.getName().replaceAll("<[^>]*>", "").toLowerCase();
                    if (string != null && string2.equals(string)) {
                        bl = true;
                    }
                    if (!bl) {
                        for (String string3 : set) {
                            if (string3.isEmpty() || !string2.contains(string3)) continue;
                            bl = true;
                            break;
                        }
                    }
                }
                if (!bl || n4 >= n3) continue;
                n3 = n4;
                nPC = nPC2;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return nPC;
    }

    private String preferredAction(NPC nPC) {
        String string = this.config.talkAction();
        try {
            NPCComposition nPCComposition = nPC.getTransformedComposition();
            if (nPCComposition != null && nPCComposition.getActions() != null) {
                String string2 = null;
                for (String string3 : nPCComposition.getActions()) {
                    if (string3 == null || string3.trim().isEmpty() || "null".equalsIgnoreCase(string3.trim())) continue;
                    if (string2 == null) {
                        string2 = string3.trim();
                    }
                    if (!string3.trim().equalsIgnoreCase(string)) continue;
                    return string3.trim();
                }
                if (string2 != null) {
                    return string2;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return string;
    }

    private void applyRealInput() {
        try {
            if (!this.inputModeApplied) {
                this.savedInteractMethod = this.configManager.getConfiguration(UNETH_GROUP, "interactMethod");
                this.savedNaturalMouse = this.configManager.getConfiguration(UNETH_GROUP, "naturalMouse");
                this.savedMouseBehavior = this.configManager.getConfiguration(UNETH_GROUP, "mouseBehavior");
                this.inputModeApplied = true;
            }
            this.configManager.setConfiguration(UNETH_GROUP, "interactMethod", (Object)InteractMethod.MOUSE_EVENTS);
            this.configManager.setConfiguration(UNETH_GROUP, "naturalMouse", (Object)true);
            this.configManager.setConfiguration(UNETH_GROUP, "mouseBehavior", (Object)MouseBehavior.CLICKBOXES);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void restoreInputMode() {
        if (!this.inputModeApplied) {
            return;
        }
        this.restoreUneth("interactMethod", this.savedInteractMethod);
        this.restoreUneth("naturalMouse", this.savedNaturalMouse);
        this.restoreUneth("mouseBehavior", this.savedMouseBehavior);
        this.inputModeApplied = false;
    }

    private void restoreUneth(String string, String string2) {
        try {
            if (string2 == null) {
                this.configManager.unsetConfiguration(UNETH_GROUP, string);
            } else {
                this.configManager.setConfiguration(UNETH_GROUP, string, string2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
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

    private static String timeNow() {
        long l = System.currentTimeMillis();
        long l2 = l / 1000L % 60L;
        long l3 = l / 60000L % 60L;
        long l4 = l / 3600000L % 24L;
        return String.format("%02d:%02d:%02d", l4, l3, l2);
    }

    private static String strip(String string) {
        return string == null ? "" : string.replaceAll("<[^>]*>", "").trim();
    }

    private static int safe(IntSup intSup) {
        try {
            return intSup.get();
        }
        catch (Throwable throwable) {
            return -1;
        }
    }

    public boolean isHandling() {
        return this.handling;
    }

    public String getPhase() {
        return this.phase;
    }

    public int getEventCount() {
        return this.eventCount;
    }

    public String getEventNpcTag() {
        return this.eventNpcTag;
    }

    public String getLastEventText() {
        return this.lastEventText;
    }

    public long getDiagnosticSecondsLeft() {
        if (!this.handling) {
            return -1L;
        }
        long l = this.diagnosticEndMs - System.currentTimeMillis();
        return l > 0L ? (l + 999L) / 1000L : 0L;
    }

    public long getCooldownSeconds() {
        long l = this.reArmAt - System.currentTimeMillis();
        return l > 0L ? (l + 999L) / 1000L : 0L;
    }

    private static interface IntSup {
        public int get();
    }
}

