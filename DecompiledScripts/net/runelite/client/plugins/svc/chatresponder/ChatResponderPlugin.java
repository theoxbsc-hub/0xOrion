/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.DrawManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.chatresponder;

import com.google.inject.Binder;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.chatresponder.ChatResponderConfig;
import net.runelite.client.plugins.svc.chatresponder.ChatResponderOverlay;
import net.runelite.client.plugins.svc.chatresponder.ChatScreenshot;
import net.runelite.client.plugins.svc.chatresponder.DiscordWebhook;
import net.runelite.client.plugins.svc.chatresponder.IncomingMessage;
import net.runelite.client.plugins.svc.chatresponder.Intent;
import net.runelite.client.plugins.svc.chatresponder.Persona;
import net.runelite.client.plugins.svc.chatresponder.PmReplyScope;
import net.runelite.client.plugins.svc.chatresponder.ResponseEngine;
import net.runelite.client.plugins.svc.chatresponder.TypoGenerator;
import net.runelite.client.plugins.svc.core.AdminWebhook;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;

@PluginDescriptor(name="<html><!--1--><nobr><font color='#FFB000'>\u25cf x64 Chat Responder</font></nobr></html>", description="Auto-replies to public chat AND private messages with human-like, offline responses (openers, typos, delays, bot-trap deflection) + Discord alerts", tags={"orion", "chat", "responder", "social", "auto", "pm", "private"}, enabledByDefault=false)
public class ChatResponderPlugin
extends OrionLoopedPlugin {
    private static final int WIDGET_GROUP_CHATBOX = 162;
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ChatResponderConfig config;
    @Inject
    private DrawManager drawManager;
    private ResponseEngine engine;
    private DiscordWebhook webhook;
    private ChatResponderOverlay overlay;
    private ScheduledExecutorService scheduler;
    private volatile boolean active;
    private final Deque<String> chatLog = new ArrayDeque<String>();
    private final Object chatLogLock = new Object();
    private static final int CHAT_LOG_MAX = 40;
    private final ConcurrentLinkedQueue<IncomingMessage> queue = new ConcurrentLinkedQueue();
    private final Deque<Outgoing> outgoing = new ArrayDeque<Outgoing>();
    private final ChannelState publicState = new ChannelState();
    private final ChannelState pmState = new ChannelState();
    private final Set<String> seenPmSenders = new HashSet<String>();
    private static final String INCOGNITO_GROUP = "incognito";
    private static final String INCOGNITO_WATCHLIST_KEY = "watchedPlayers";
    private static final long WATCHLIST_CACHE_MS = 5000L;
    private Set<String> watchedNames = Collections.emptySet();
    private long watchedNamesAt = 0L;
    private static final String AUTOLOGIN_GROUP = "autoLogin";
    private static final int AUTOLOGIN_PROFILES = 20;
    private static final long AUTOLOGIN_CACHE_MS = 5000L;
    private Set<String> autoLoginNames = Collections.emptySet();
    private long autoLoginNamesAt = 0L;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(ChatResponderConfig.class).toInstance((Object)((ChatResponderConfig)configManager.getConfig(ChatResponderConfig.class)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void onStart() throws Exception {
        this.engine = new ResponseEngine();
        this.webhook = new DiscordWebhook();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "orion-chat-alert");
            thread.setDaemon(true);
            return thread;
        });
        this.active = true;
        this.queue.clear();
        this.outgoing.clear();
        this.publicState.clear();
        this.pmState.clear();
        this.seenPmSenders.clear();
        Object object = this.chatLogLock;
        synchronized (object) {
            this.chatLog.clear();
        }
        this.publicState.status = this.config.enablePublicReplies() ? "Listening" : "Off";
        this.pmState.status = this.config.enablePmReplies() ? "Listening" : "Off";
        this.overlay = new ChatResponderOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void onStop() throws Exception {
        this.active = false;
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
            this.overlay = null;
        }
        if (this.scheduler != null) {
            this.scheduler.shutdownNow();
            this.scheduler = null;
        }
        if (this.webhook != null) {
            this.webhook.shutdown();
        }
        this.queue.clear();
        this.outgoing.clear();
        Object object = this.chatLogLock;
        synchronized (object) {
            this.chatLog.clear();
        }
        this.publicState.status = "Idle";
        this.pmState.status = "Idle";
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String string;
        boolean bl;
        this.recordChatLine(chatMessage.getType(), chatMessage.getName(), chatMessage.getMessage());
        if (chatMessage.getType() == ChatMessageType.PUBLICCHAT && this.config.enablePublicReplies()) {
            bl = false;
        } else if (chatMessage.getType() == ChatMessageType.PRIVATECHAT && this.config.enablePmReplies()) {
            bl = true;
        } else {
            return;
        }
        String string2 = ChatResponderPlugin.cleanName(chatMessage.getName());
        String string3 = ChatResponderPlugin.stripHtml(chatMessage.getMessage()).trim();
        if (string2.isEmpty() || string3.isEmpty()) {
            return;
        }
        String string4 = this.myName();
        if (string4 != null && string2.equalsIgnoreCase(string4)) {
            return;
        }
        if (this.config.ignoreAutoLoginAccounts() && this.isAutoLoginAccount(string2)) {
            return;
        }
        String string5 = bl ? this.config.pmPlayerBlacklist() : this.config.playerBlacklist();
        String string6 = string = bl ? this.config.pmWordBlacklist() : this.config.wordBlacklist();
        if (ChatResponderPlugin.inListIgnoreCase(ResponseEngine.splitPipe(string5), string2)) {
            return;
        }
        if (ChatResponderPlugin.containsAnyWord(string3, ResponseEngine.splitPipe(string))) {
            return;
        }
        this.queue.add(new IncomingMessage(string2, string3, System.currentTimeMillis(), bl));
        while (this.queue.size() > 20) {
            this.queue.poll();
        }
    }

    @Override
    protected int loop() {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            this.publicState.status = "Logged out";
            this.pmState.status = "Logged out";
            return 1000;
        }
        long l = System.currentTimeMillis();
        Outgoing outgoing = this.outgoing.peekFirst();
        if (outgoing != null) {
            ChannelState channelState;
            ChannelState channelState2 = channelState = outgoing.pm ? this.pmState : this.publicState;
            if (l >= outgoing.sendAt) {
                if (this.canSend()) {
                    boolean bl;
                    if (outgoing.pm) {
                        bl = this.sendPrivateReply(outgoing.sender, outgoing.text);
                    } else {
                        this.sendChat(outgoing.text);
                        bl = true;
                    }
                    this.outgoing.pollFirst();
                    if (!bl) {
                        channelState.status = "PM send failed (not in PM mode)";
                    } else {
                        if (outgoing.primary) {
                            ++channelState.repliesSent;
                            channelState.lastReply = outgoing.text;
                            channelState.recentReplies.addLast(l);
                            channelState.lastReplyPerPlayer.put(outgoing.sender, l);
                            this.logReply(outgoing);
                        }
                        channelState.status = this.outgoing.isEmpty() ? "Listening" : "Sending follow-up";
                    }
                } else {
                    channelState.status = "Waiting (busy) to reply";
                    outgoing.sendAt = l + 600L;
                }
            } else {
                channelState.status = "Replying in " + (outgoing.sendAt - l) + "ms";
            }
            return 150;
        }
        IncomingMessage incomingMessage = this.queue.poll();
        if (incomingMessage == null) {
            if (this.config.enablePublicReplies()) {
                this.publicState.status = "Listening";
            }
            if (this.config.enablePmReplies()) {
                this.pmState.status = "Listening";
            }
            return 300;
        }
        if (incomingMessage.pm) {
            this.handleIncoming(incomingMessage, l, this.pmState, this.pmSettings(), true);
        } else {
            this.handleIncoming(incomingMessage, l, this.publicState, this.publicSettings(), false);
        }
        return 150;
    }

    private void handleIncoming(IncomingMessage incomingMessage, long l, ChannelState channelState, Settings settings, boolean bl) {
        int n;
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        boolean bl2 = false;
        if (bl) {
            if (!this.pmEligible(incomingMessage.sender)) {
                return;
            }
            bl2 = settings.enableOpeners && !this.seenPmSenders.contains(incomingMessage.sender.toLowerCase());
        } else if (!this.publicEligible(incomingMessage, settings)) {
            return;
        }
        boolean bl3 = settings.followUpWindow > 0 && channelState.followUpsRemaining.getOrDefault(incomingMessage.sender, 0) > 0;
        boolean bl4 = bl3 || bl2;
        Intent intent = this.engine.classify(incomingMessage.text, settings.handleInterrogation);
        if (intent == Intent.BOT_ACCUSATION && (n = channelState.interrogationCounts.merge(incomingMessage.sender, 1, Integer::sum).intValue()) >= settings.goQuietAfterInterrogations) {
            channelState.quietUntil.put(incomingMessage.sender, l + (long)settings.quietDurationSec * 1000L);
            channelState.followUpsRemaining.remove(incomingMessage.sender);
        }
        String string = this.decideIgnoreReason(channelState, settings, incomingMessage.sender, l, threadLocalRandom, bl4);
        channelState.lastDetected = incomingMessage.sender + ": " + incomingMessage.text;
        if (string != null) {
            ++channelState.ignored;
            channelState.status = "Ignored (" + string + ")";
            if (settings.webhookEnabled && settings.logDetected) {
                this.webhook.notifyEvent(settings.minWebhookGapMs, false, settings.modeLabel, incomingMessage.sender, incomingMessage.text, intent.name(), null, 0L, string);
            }
            return;
        }
        if (settings.followUpWindow > 0) {
            if (bl3) {
                int n2 = channelState.followUpsRemaining.getOrDefault(incomingMessage.sender, 0) - 1;
                if (n2 > 0) {
                    channelState.followUpsRemaining.put(incomingMessage.sender, n2);
                } else {
                    channelState.followUpsRemaining.remove(incomingMessage.sender);
                }
            } else {
                channelState.followUpsRemaining.put(incomingMessage.sender, settings.followUpWindow);
            }
        }
        if (bl2) {
            this.seenPmSenders.add(incomingMessage.sender.toLowerCase());
        }
        String[] stringArray = bl ? (bl2 ? this.engine.pmOpenerPool(this.config) : this.engine.pmPoolFor(intent, this.config)) : null;
        String string2 = bl ? this.engine.pickResponse(stringArray, threadLocalRandom) : this.engine.pickResponse(intent, this.config, threadLocalRandom);
        String string3 = this.engine.applyPersona(string2, settings.forceLowercase, settings.useAbbreviations, settings.persona, threadLocalRandom);
        TypoGenerator.Result result = TypoGenerator.apply(string3, settings.typoChance, threadLocalRandom);
        String string4 = ChatResponderPlugin.clampLen(result.text, settings.maxResponseLen);
        if (settings.prefixSenderName) {
            string4 = ChatResponderPlugin.clampLen(incomingMessage.sender + " " + string4, settings.maxResponseLen);
        }
        long l2 = this.reactionDelay(settings, threadLocalRandom) + (long)string4.length() * (long)settings.perCharTypingMs;
        Outgoing outgoing = new Outgoing();
        outgoing.text = string4;
        outgoing.sendAt = l + l2;
        outgoing.primary = true;
        outgoing.pm = bl;
        outgoing.sender = incomingMessage.sender;
        outgoing.srcMessage = incomingMessage.text;
        outgoing.intent = bl2 ? "OPENER" : intent.name();
        outgoing.delayMs = l2;
        outgoing.minWebhookGapMs = settings.minWebhookGapMs;
        outgoing.webhookEnabled = settings.webhookEnabled && settings.logResponded;
        outgoing.modeLabel = settings.modeLabel;
        this.outgoing.addLast(outgoing);
        if (result.correctedWord != null && threadLocalRandom.nextInt(100) < settings.correctionChance) {
            Outgoing outgoing2 = new Outgoing();
            String string5 = settings.forceLowercase ? result.correctedWord.toLowerCase() : result.correctedWord;
            outgoing2.text = "*" + string5;
            outgoing2.sendAt = outgoing.sendAt + (long)(1200 + threadLocalRandom.nextInt(1400));
            outgoing2.primary = false;
            outgoing2.pm = bl;
            outgoing2.sender = incomingMessage.sender;
            this.outgoing.addLast(outgoing2);
        }
        channelState.status = "Replying in " + l2 + "ms";
    }

    private boolean publicEligible(IncomingMessage incomingMessage, Settings settings) {
        String string = this.myName();
        boolean bl = settings.followUpWindow > 0 && this.publicState.followUpsRemaining.getOrDefault(incomingMessage.sender, 0) > 0;
        switch (this.config.responseMode()) {
            case NAME_ONLY: {
                if (bl || string != null && this.containsName(incomingMessage.text, string)) break;
                return false;
            }
            case NEARBY: {
                if (this.withinNearby(incomingMessage.sender)) break;
                return false;
            }
        }
        String[] stringArray = ResponseEngine.splitPipe(this.config.playerWhitelist());
        return stringArray.length == 0 || ChatResponderPlugin.inListIgnoreCase(stringArray, incomingMessage.sender);
    }

    private boolean pmEligible(String string) {
        String[] stringArray = ResponseEngine.splitPipe(this.config.pmPlayerWhitelist());
        if (this.config.replyScope() == PmReplyScope.WHITELIST_ONLY) {
            return stringArray.length > 0 && ChatResponderPlugin.inListIgnoreCase(stringArray, string);
        }
        return stringArray.length == 0 || ChatResponderPlugin.inListIgnoreCase(stringArray, string);
    }

    private String decideIgnoreReason(ChannelState channelState, Settings settings, String string, long l, ThreadLocalRandom threadLocalRandom, boolean bl) {
        Long l2 = channelState.quietUntil.get(string);
        if (l2 != null && l < l2) {
            return "quiet";
        }
        if (this.rateLimited(channelState, settings, l)) {
            return "rate limit";
        }
        if (bl) {
            return null;
        }
        Long l3 = channelState.lastReplyPerPlayer.get(string);
        if (l3 != null && l - l3 < (long)settings.perPlayerCooldownSec * 1000L) {
            return "cooldown";
        }
        if (threadLocalRandom.nextInt(100) >= settings.responseProbability) {
            return "probability";
        }
        if (threadLocalRandom.nextInt(100) < settings.randomIgnoreChance) {
            return "random";
        }
        return null;
    }

    private boolean rateLimited(ChannelState channelState, Settings settings, long l) {
        while (!channelState.recentReplies.isEmpty() && l - channelState.recentReplies.peekFirst() > 60000L) {
            channelState.recentReplies.pollFirst();
        }
        return channelState.recentReplies.size() >= settings.maxRepliesPerMinute;
    }

    private long reactionDelay(Settings settings, ThreadLocalRandom threadLocalRandom) {
        int n = settings.minReactionMs;
        int n2 = Math.max(n, settings.maxReactionMs);
        return n2 > n ? (long)(n + threadLocalRandom.nextInt(n2 - n + 1)) : (long)n;
    }

    private void logReply(Outgoing outgoing) {
        boolean bl = this.isWatchedPlayer(outgoing.sender);
        if (this.config.richAlertsEnabled() && (outgoing.pm || bl) && AdminWebhook.isConfigured()) {
            this.fireRichReplyWebhook(outgoing, bl);
            return;
        }
        if (outgoing.webhookEnabled) {
            this.webhook.notifyEvent(outgoing.minWebhookGapMs, true, outgoing.modeLabel, outgoing.sender, outgoing.srcMessage, outgoing.intent, outgoing.text, outgoing.delayMs, null);
        }
    }

    private void fireRichReplyWebhook(Outgoing outgoing, boolean bl) {
        String string = this.snapshotChatLog();
        String string2 = this.myName();
        int n = outgoing.minWebhookGapMs;
        boolean bl2 = outgoing.pm;
        String string3 = outgoing.sender;
        String string4 = outgoing.srcMessage;
        String string5 = outgoing.intent;
        String string6 = outgoing.text;
        String string7 = outgoing.modeLabel;
        long l = outgoing.delayMs;
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Runnable runnable = this.makePostTask(atomicBoolean, n, string7, string3, string4, string5, string6, l, bl2, bl, string2, string, null);
        boolean bl3 = false;
        try {
            if (this.drawManager != null) {
                ChatScreenshot.requestShot(this.drawManager, byArray -> this.makePostTask(atomicBoolean, n, string7, string3, string4, string5, string6, l, bl2, bl, string2, string, (byte[])byArray).run());
                bl3 = true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (!bl3) {
            runnable.run();
            return;
        }
        try {
            if (this.scheduler != null) {
                this.scheduler.schedule(runnable, 2500L, TimeUnit.MILLISECONDS);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private Runnable makePostTask(AtomicBoolean atomicBoolean, int n, String string, String string2, String string3, String string4, String string5, long l, boolean bl, boolean bl2, String string6, String string7, byte[] byArray) {
        return () -> {
            if (this.active && this.webhook != null && atomicBoolean.compareAndSet(false, true)) {
                this.webhook.notifyRichReply(n, string, string2, string3, string4, string5, l, bl, bl2, string6, string7, byArray);
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void recordChatLine(ChatMessageType chatMessageType, String string, String string2) {
        String string3;
        if (!ChatResponderPlugin.isConversational(chatMessageType)) {
            return;
        }
        String string4 = ChatResponderPlugin.stripHtml(string2);
        if (string4.isEmpty()) {
            return;
        }
        String string5 = ChatResponderPlugin.cleanName(string);
        switch (chatMessageType) {
            case PRIVATECHAT: {
                string3 = (string5.isEmpty() ? "?" : string5) + " [PM]: ";
                break;
            }
            case PRIVATECHATOUT: {
                string3 = "[PM->" + (string5.isEmpty() ? "?" : string5) + "]: ";
                break;
            }
            case FRIENDSCHAT: 
            case CLAN_CHAT: 
            case CLAN_GUEST_CHAT: {
                string3 = (string5.isEmpty() ? "?" : string5) + " [cc]: ";
                break;
            }
            default: {
                string3 = (string5.isEmpty() ? "?" : string5) + ": ";
            }
        }
        Object object = this.chatLogLock;
        synchronized (object) {
            this.chatLog.addLast(string3 + string4);
            while (this.chatLog.size() > 40) {
                this.chatLog.pollFirst();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String snapshotChatLog() {
        StringBuilder stringBuilder = new StringBuilder();
        Object object = this.chatLogLock;
        synchronized (object) {
            for (String string : this.chatLog) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append('\n');
                }
                stringBuilder.append(string);
            }
        }
        object = stringBuilder.toString();
        if (((String)object).length() > 3500) {
            object = ((String)object).substring(((String)object).length() - 3500);
        }
        return object;
    }

    private static boolean isConversational(ChatMessageType chatMessageType) {
        if (chatMessageType == null) {
            return false;
        }
        switch (chatMessageType) {
            case PRIVATECHAT: 
            case PRIVATECHATOUT: 
            case FRIENDSCHAT: 
            case CLAN_CHAT: 
            case CLAN_GUEST_CHAT: 
            case PUBLICCHAT: 
            case MODCHAT: 
            case MODPRIVATECHAT: 
            case AUTOTYPER: {
                return true;
            }
        }
        return false;
    }

    private boolean isWatchedPlayer(String string) {
        String string2 = ChatResponderPlugin.normName(string);
        if (string2.isEmpty()) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - this.watchedNamesAt > 5000L) {
            this.watchedNames = this.loadWatchedNames();
            this.watchedNamesAt = l;
        }
        return this.watchedNames.contains(string2);
    }

    private Set<String> loadWatchedNames() {
        String string;
        HashSet<String> hashSet = new HashSet<String>();
        try {
            string = this.configManager.getConfiguration(INCOGNITO_GROUP, INCOGNITO_WATCHLIST_KEY);
        }
        catch (Throwable throwable) {
            string = null;
        }
        if (string != null) {
            for (String string2 : string.split("[,\\r\\n|]+")) {
                String string3 = ChatResponderPlugin.normName(string2);
                if (string3.isEmpty()) continue;
                hashSet.add(string3);
            }
        }
        return hashSet;
    }

    private static String normName(String string) {
        return ChatResponderPlugin.cleanName(string == null ? "" : string).replace('_', ' ').trim().toLowerCase();
    }

    private boolean withinNearby(String string) {
        Player player2 = this.client.getLocalPlayer();
        if (player2 == null) {
            return false;
        }
        WorldPoint worldPoint = player2.getWorldLocation();
        if (worldPoint == null) {
            return false;
        }
        List<Player> list = Players.getAll(player -> player != null && player.getName() != null && ChatResponderPlugin.cleanName(player.getName()).equalsIgnoreCase(string));
        for (Player player3 : list) {
            WorldPoint worldPoint2 = player3.getWorldLocation();
            if (worldPoint2 == null || worldPoint.distanceTo(worldPoint2) > this.config.nearbyRadius()) continue;
            return true;
        }
        return false;
    }

    private boolean canSend() {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return false;
        }
        try {
            if (Bank.isOpen() || Dialog.isOpen() || Dialog.isEnterInputOpen()) {
                return false;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return true;
    }

    private void sendChat(String string) {
        Keyboard.type(string);
        Keyboard.type('\n');
    }

    private boolean sendPrivateReply(String string, String string2) {
        if (!this.enterPmComposeMode(string)) {
            return false;
        }
        if (!Time.sleepUntil(() -> this.isComposingPmTo(string), 600)) {
            return false;
        }
        Keyboard.type(string2);
        Keyboard.type('\n');
        return true;
    }

    private boolean enterPmComposeMode(String string) {
        Object object;
        if (this.isComposingPmTo(string)) {
            return true;
        }
        Keyboard.pressed(9);
        Time.sleep(40L);
        Keyboard.released(9);
        if (Time.sleepUntil(() -> this.isComposingPmTo(string), 600)) {
            return true;
        }
        if (this.isComposingPrivateMessage()) {
            return false;
        }
        try {
            object = Players.getNearest(player -> player != null && player.getName() != null && ChatResponderPlugin.cleanName(player.getName()).equalsIgnoreCase(string));
            if (object != null) {
                object.interact("Message");
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            object = Widgets.getAll(162, widget -> widget != null && ChatResponderPlugin.hasAction(widget, "Message") && widget.getText() != null && ChatResponderPlugin.containsIgnoreCase(widget.getText(), string));
            Iterator iterator = object.iterator();
            if (iterator.hasNext()) {
                Widget widget2 = (Widget)iterator.next();
                MousePackets.queueClickPacket();
                WidgetPackets.widgetAction(widget2, "Message");
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private String pmComposePrompt() {
        try {
            List<Widget> list = Widgets.getAll(162, widget -> {
                if (widget == null || widget.isHidden()) {
                    return false;
                }
                String string = widget.getText();
                if (string == null) {
                    return false;
                }
                String string2 = string.toLowerCase();
                return string2.contains("send to") || string2.contains("message to");
            });
            return list.isEmpty() ? null : list.get(0).getText();
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private boolean isComposingPrivateMessage() {
        return this.pmComposePrompt() != null;
    }

    private boolean isComposingPmTo(String string) {
        String string2 = this.pmComposePrompt();
        if (string2 == null) {
            return false;
        }
        String string3 = ChatResponderPlugin.stripHtml(string2.replace('\u00a0', ' ')).toLowerCase();
        String string4 = ChatResponderPlugin.stripHtml((string == null ? "" : string).replace('\u00a0', ' ')).toLowerCase();
        return !string4.isEmpty() && string3.contains(string4);
    }

    private Settings publicSettings() {
        Settings settings = new Settings();
        settings.responseProbability = this.config.responseProbability();
        settings.randomIgnoreChance = this.config.randomIgnoreChance();
        settings.minReactionMs = this.config.minReactionMs();
        settings.maxReactionMs = this.config.maxReactionMs();
        settings.perCharTypingMs = this.config.perCharTypingMs();
        settings.perPlayerCooldownSec = this.config.perPlayerCooldownSec();
        settings.followUpWindow = this.config.followUpWindow();
        settings.maxRepliesPerMinute = this.config.maxRepliesPerMinute();
        settings.maxResponseLen = this.config.maxResponseLen();
        settings.persona = this.config.persona();
        settings.forceLowercase = this.config.forceLowercase();
        settings.useAbbreviations = this.config.useAbbreviations();
        settings.typoChance = this.config.typoChance();
        settings.correctionChance = this.config.correctionChance();
        settings.handleInterrogation = this.config.handleInterrogation();
        settings.goQuietAfterInterrogations = this.config.goQuietAfterInterrogations();
        settings.quietDurationSec = this.config.quietDurationSec();
        settings.prefixSenderName = this.config.prefixSenderName();
        settings.enableOpeners = false;
        settings.webhookEnabled = this.config.webhookEnabled();
        settings.logResponded = this.config.logResponded();
        settings.logDetected = this.config.logDetected();
        settings.minWebhookGapMs = this.config.minWebhookGapMs();
        settings.modeLabel = this.config.responseMode().toString();
        return settings;
    }

    private Settings pmSettings() {
        Settings settings = new Settings();
        settings.responseProbability = this.config.pmResponseProbability();
        settings.randomIgnoreChance = this.config.pmRandomIgnoreChance();
        settings.minReactionMs = this.config.pmMinReactionMs();
        settings.maxReactionMs = this.config.pmMaxReactionMs();
        settings.perCharTypingMs = this.config.pmPerCharTypingMs();
        settings.perPlayerCooldownSec = this.config.pmPerPlayerCooldownSec();
        settings.followUpWindow = this.config.pmFollowUpWindow();
        settings.maxRepliesPerMinute = this.config.pmMaxRepliesPerMinute();
        settings.maxResponseLen = this.config.pmMaxResponseLen();
        settings.persona = this.config.pmPersona();
        settings.forceLowercase = this.config.pmForceLowercase();
        settings.useAbbreviations = this.config.pmUseAbbreviations();
        settings.typoChance = this.config.pmTypoChance();
        settings.correctionChance = this.config.pmCorrectionChance();
        settings.handleInterrogation = this.config.pmHandleInterrogation();
        settings.goQuietAfterInterrogations = this.config.pmGoQuietAfterInterrogations();
        settings.quietDurationSec = this.config.pmQuietDurationSec();
        settings.prefixSenderName = this.config.pmPrefixSenderName();
        settings.enableOpeners = this.config.enableOpeners();
        settings.webhookEnabled = this.config.pmWebhookEnabled();
        settings.logResponded = this.config.pmLogResponded();
        settings.logDetected = this.config.pmLogDetected();
        settings.minWebhookGapMs = this.config.pmMinWebhookGapMs();
        settings.modeLabel = "PM (" + this.config.replyScope().toString() + ")";
        return settings;
    }

    private String myName() {
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return null;
        }
        String string = player.getName();
        return string == null || string.isEmpty() ? null : ChatResponderPlugin.cleanName(string);
    }

    private boolean isAutoLoginAccount(String string) {
        String string2 = ChatResponderPlugin.normAccount(string);
        if (string2.isEmpty()) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - this.autoLoginNamesAt > 5000L || this.autoLoginNames.isEmpty()) {
            this.autoLoginNames = this.loadAutoLoginNames();
            this.autoLoginNamesAt = l;
        }
        return this.autoLoginNames.contains(string2);
    }

    private Set<String> loadAutoLoginNames() {
        HashSet<String> hashSet = new HashSet<String>();
        for (int i = 1; i <= 20; ++i) {
            String string;
            try {
                string = this.configManager.getConfiguration(AUTOLOGIN_GROUP, "profile" + i + "Username");
            }
            catch (Throwable throwable) {
                string = null;
            }
            String string2 = ChatResponderPlugin.normAccount(string);
            if (string2.isEmpty()) continue;
            hashSet.add(string2);
        }
        return hashSet;
    }

    private static String normAccount(String string) {
        return ChatResponderPlugin.stripHtml(string == null ? "" : string).replace('_', ' ').trim().toLowerCase();
    }

    private boolean containsName(String string, String string2) {
        String string3 = ResponseEngine.normalise(string).replace('_', ' ');
        String string4 = string2.toLowerCase().replace('_', ' ').trim();
        return !string4.isEmpty() && string3.contains(string4);
    }

    private static boolean hasAction(Widget widget, String string) {
        String[] stringArray = widget.getActions();
        if (stringArray == null) {
            return false;
        }
        for (String string2 : stringArray) {
            if (string2 == null || !string2.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    private static boolean containsIgnoreCase(String string, String string2) {
        return string != null && string2 != null && ChatResponderPlugin.stripHtml(string).toLowerCase().contains(string2.toLowerCase());
    }

    private static String stripHtml(String string) {
        return string == null ? "" : string.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private static String cleanName(String string) {
        return ChatResponderPlugin.stripHtml(string).replaceAll("^[^a-zA-Z0-9]+", "").trim();
    }

    private static boolean inListIgnoreCase(String[] stringArray, String string) {
        for (String string2 : stringArray) {
            if (!string2.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    private static boolean containsAnyWord(String string, String[] stringArray) {
        if (stringArray.length == 0) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : stringArray) {
            if (!string2.contains(string3.toLowerCase())) continue;
            return true;
        }
        return false;
    }

    private static String clampLen(String string, int n) {
        if (string.length() <= n) {
            return string;
        }
        String string2 = string.substring(0, n);
        int n2 = string2.lastIndexOf(32);
        return n2 > n / 2 ? string2.substring(0, n2) : string2;
    }

    boolean isPublicEnabled() {
        return this.config.enablePublicReplies();
    }

    boolean isPmEnabled() {
        return this.config.enablePmReplies();
    }

    String getPublicStatus() {
        return this.publicState.status;
    }

    String getPublicLastDetected() {
        return this.publicState.lastDetected;
    }

    String getPublicLastReply() {
        return this.publicState.lastReply;
    }

    int getPublicRepliesSent() {
        return this.publicState.repliesSent;
    }

    int getPublicIgnored() {
        return this.publicState.ignored;
    }

    String getPmStatus() {
        return this.pmState.status;
    }

    String getPmLastDetected() {
        return this.pmState.lastDetected;
    }

    String getPmLastReply() {
        return this.pmState.lastReply;
    }

    int getPmRepliesSent() {
        return this.pmState.repliesSent;
    }

    int getPmIgnored() {
        return this.pmState.ignored;
    }

    int getWebhooksSent() {
        return this.webhook != null ? this.webhook.getSent() : 0;
    }

    private static final class ChannelState {
        final Deque<Long> recentReplies = new ArrayDeque<Long>();
        final Map<String, Long> lastReplyPerPlayer = new HashMap<String, Long>();
        final Map<String, Integer> interrogationCounts = new HashMap<String, Integer>();
        final Map<String, Long> quietUntil = new HashMap<String, Long>();
        final Map<String, Integer> followUpsRemaining = new HashMap<String, Integer>();
        volatile int repliesSent = 0;
        volatile int ignored = 0;
        volatile String status = "Idle";
        volatile String lastDetected = "-";
        volatile String lastReply = "-";

        private ChannelState() {
        }

        void clear() {
            this.recentReplies.clear();
            this.lastReplyPerPlayer.clear();
            this.interrogationCounts.clear();
            this.quietUntil.clear();
            this.followUpsRemaining.clear();
            this.repliesSent = 0;
            this.ignored = 0;
            this.status = "Idle";
            this.lastDetected = "-";
            this.lastReply = "-";
        }
    }

    private static final class Outgoing {
        String text;
        long sendAt;
        boolean primary;
        boolean pm;
        String sender;
        String srcMessage;
        String intent;
        long delayMs;
        boolean webhookEnabled;
        int minWebhookGapMs;
        String modeLabel;

        private Outgoing() {
        }
    }

    private static final class Settings {
        int responseProbability;
        int randomIgnoreChance;
        int minReactionMs;
        int maxReactionMs;
        int perCharTypingMs;
        int perPlayerCooldownSec;
        int followUpWindow;
        int maxRepliesPerMinute;
        int maxResponseLen;
        Persona persona;
        boolean forceLowercase;
        boolean useAbbreviations;
        int typoChance;
        int correctionChance;
        boolean handleInterrogation;
        int goQuietAfterInterrogations;
        int quietDurationSec;
        boolean prefixSenderName;
        boolean enableOpeners;
        boolean webhookEnabled;
        boolean logResponded;
        boolean logDetected;
        int minWebhookGapMs;
        String modeLabel;

        private Settings() {
        }
    }
}

