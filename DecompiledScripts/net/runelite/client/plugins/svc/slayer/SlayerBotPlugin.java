/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.GameState
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.Locatable
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.game.ItemManager
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.slayer;

import com.google.inject.Binder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Locatable;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.combat.CombatPotion;
import net.runelite.client.plugins.svc.combat.PrayerFlickMode;
import net.runelite.client.plugins.svc.core.AccountProfile;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.RandomEventState;
import net.runelite.client.plugins.svc.slayer.GraveyardMonsterLocations;
import net.runelite.client.plugins.svc.slayer.SlayerBotConfig;
import net.runelite.client.plugins.svc.slayer.SlayerBotOverlay;
import net.runelite.client.plugins.svc.slayer.SlayerNames;
import net.runelite.client.plugins.svc.slayer.SlayerState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.client.managers.interaction.InteractMethod;
import net.unethicalite.client.managers.interaction.MouseBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Slayer Bot</font></nobr></html>", description="Full slayer automation: gets tasks from Vannaka, teleports to monsters, kills, banks, loops", tags={"orion", "slayer", "bot", "automation", "task"}, enabledByDefault=false)
public class SlayerBotPlugin
extends OrionLoopedPlugin {
    private static final Logger log = LoggerFactory.getLogger(SlayerBotPlugin.class);
    private static final int RETRY_ESCALATE = 5;
    private static final int RETRY_GIVE_UP = 12;
    private static final WorldPoint VANNAKA = new WorldPoint(2792, 3394, 0);
    private static final WorldPoint GLIDER = new WorldPoint(2795, 3374, 0);
    private static final int ENCHANTED_GEM_ID = 4155;
    private static final int SEARCH_RADIUS = 30;
    private static final int NPC_ATTACK_SPEED_TICKS = 4;
    private static final String HOME_COMMAND = "::home";
    private static final String GLIDER_OBJECT_NAME = "Gnome glider";
    private static final String GLIDER_TRAVEL_ACTION = "Travel";
    private static final int SEARCH_BTN_X = 75;
    private static final int SEARCH_BTN_Y = 305;
    private static final int SELECT_BTN_X = 220;
    private static final int SELECT_BTN_Y = 90;
    private static final int BANK_OBJECT_ID = 10083;
    private static final String BANK_ACTION = "Use";
    private static final int PRESET_LOAD_X = 448;
    private static final int PRESET_LOAD_Y = 17;
    private static final int PRESET_CONFIRM_X = 68;
    private static final int PRESET_CONFIRM_Y = 282;
    private static final int BANK_CLOSE_X = 483;
    private static final int BANK_CLOSE_Y = 33;
    private static final int REJUVENATION_OBJECT_ID = 42822;
    private static final int GRAVEYARD_MASTER_ID = 9579;
    private static final String GRAVEYARD_MASTER_ACTION = "Assignment";
    private static final String GRAVEYARD_TELEPORT_CMD = "::graveyard";
    private static final int GRAVEYARD_COINS_ITEM_ID = 995;
    private static final int GRAVEYARD_ARRIVE = 8;
    private static final int MENU_OPEN_DELAY_MS = 6000;
    private static final int SEARCH_PROMPT_DELAY_MS = 3000;
    private static final int TYPE_RESULT_DELAY_MS = 1200;
    private static final int SELECT_RESULT_DELAY_MS = 1500;
    private static final int BANK_PRESET_CLICK_DELAY_MS = 800;
    private static final int BANK_PRESET_CONFIRM_DELAY_MS = 500;
    private static final int LAST_PRESET_VERIFY_MS = 3000;
    private static final int BANK_OPEN_TIMEOUT_MS = 5000;
    private static final int BANK_OPEN_SETTLE_MS = 600;
    private static final List<String> FOOD_NAMES = Arrays.asList("Shark", "Manta ray", "Anglerfish", "Dark crab", "Sea turtle", "Monkfish", "Swordfish", "Cooked karambwan", "Lobster", "Tuna", "Bass", "Salmon", "Trout");
    private static final List<String> PRAYER_RESTORE_NAMES = Arrays.asList("Super restore", "Prayer potion", "Sanfew serum", "Prayer mix");
    private static final String[] SPEC_WEAPONS = new String[]{"dragon dagger", "dragon longsword", "dragon mace", "dragon scimitar", "dragon battleaxe", "dragon halberd", "dragon spear", "dragon 2h", "dragon warhammer", "dragon claws", "dragon thrownaxe", "dragon knife", "dragon crossbow", "dragon sword", "abyssal dagger", "abyssal bludgeon", "godsword", "granite maul", "granite hammer", "barrelchest anchor", "sarachnis cudgel", "crystal halberd", "zamorakian spear", "ancient mace", "saradomin's blessed sword", "excalibur", "bone dagger", "dorgeshuun", "arclight", "darklight", "emberlight", "burning claws", "magic shortbow", "magic longbow", "dark bow", "seercull", "blowpipe", "armadyl crossbow", "zaryte crossbow", "thrownaxe", "chainmace", "voidwaker", "korasi", "soulreaper axe", "elder maul", "accursed sceptre", "webweaver", "craw's bow", "viggora", "ursine", "nightmare staff", "eldritch", "harmonised", "volatile", "statius", "vesta", "morrigan", "zuriel", "keris partisan", "scorching bow", "tonalztics"};
    private static final Map<String, String> TELEPORT_SEARCH_OVERRIDES = new HashMap<String, String>(){
        {
            this.put("Jellies", "Jelly");
            this.put("Spiritual Creatures", "Spiritual mage");
        }
    };
    private static final Pattern TASK_PATTERN = Pattern.compile("(?:kill|slay|assigned to kill|task is to kill)\\s+(\\d+)\\s+([a-z ']+?)(?:\\s*[.,!]|$)", 2);
    private static final Pattern DONE_PATTERN = Pattern.compile("(?:you.?ve completed|assignment is complete|task is complete)", 2);
    private static final Pattern GEM_TASK_PATTERN = Pattern.compile("assigned to kill\\s+([a-z ']+?)\\s*[;,].*?(\\d+)\\s+more to go", 2);
    private static final Pattern GEM_NO_TASK_PATTERN = Pattern.compile("(?:not (?:currently )?assigned|no (?:slayer )?task|haven.t been assigned|need something new to hunt|need a new (?:slayer )?(?:task|assignment))", 2);
    private static final Pattern BUSY_NPC_PATTERN = Pattern.compile("already in combat|creature belongs to", 2);
    private static final Pattern LOCKED_PRESET_PATTERN = Pattern.compile("may not load locked presets|locked preset", 2);
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private SlayerBotConfig config;
    private SlayerBotOverlay overlay;
    private AccountProfile profile;
    private HumanInput input;
    private static final String UNETH_GROUP = "unethicalite";
    private String savedInteractMethod;
    private String savedNaturalMouse;
    private String savedMouseBehavior;
    private boolean inputModeApplied = false;
    private final Random breakRng = new Random();
    private long nextBreakAtMs = 0L;
    private long breakUntilMs = 0L;
    private volatile String pauseStatus = null;
    private volatile boolean inDangerousCombat = false;
    private final Map<String, List<String>> listCache = new HashMap<String, List<String>>();
    private SlayerState state = SlayerState.GET_TASK;
    private String currentTask = "";
    private int killsRemaining = 0;
    private int killsThisTask = 0;
    private int totalKills = 0;
    private int tasksCompleted = 0;
    private long startTime = 0L;
    private String status = "Starting";
    private boolean taskComplete = false;
    private long alchProfit = 0L;
    private boolean loggedInLastTick = false;
    private boolean wasInCombat = false;
    private NPC activeCombatTarget = null;
    private long outOfCombatSince = 0L;
    private NPC engagedTarget = null;
    private long engageStartedAt = 0L;
    private final Map<Integer, Long> busyNpcs = new ConcurrentHashMap<Integer, Long>();
    private static final long BUSY_NPC_COOLDOWN_MS = 8000L;
    private volatile NPC lastAttemptedTarget = null;
    private TeleportPhase teleportPhase = TeleportPhase.WALK_TO_GLIDER;
    private long teleportPhaseAt = 0L;
    private WorldPoint preClickPos = null;
    private int homeTeleportAttempts = 0;
    private long stateEnteredAt = 0L;
    private long walkingToVannakaSince = 0L;
    private long rejuvenatingHomeSince = 0L;
    private long noTargetSince = 0L;
    private int bankBoothMisses = 0;
    private int graveyardMasterMisses = 0;
    private int gemCheckMisses = 0;
    private int dialogMisses = 0;
    private int reNavCycles = 0;
    private int noTargetBankBails = 0;
    private long lastProgressAt = 0L;
    private int watchdogTrips = 0;
    private long lastWatchdogAt = 0L;
    private boolean gemCheckPending = false;
    private long gemCheckAt = 0L;
    private boolean presetLoadConfirmed = false;
    private volatile boolean lockedPresetDetected = false;
    private long bankVerifyAt = 0L;
    private long bankOpenAt = 0L;
    private boolean startupBankDone = false;
    private long graveyardTeleAt = 0L;
    private long bankHomeTeleAt = 0L;
    private long graveyardTravelSince = 0L;
    private int graveyardTravelTeleAttempts = 0;
    private boolean graveyardDialogSeen = false;
    private int prayerActivatedOnTick = -1;
    private int boostActivatedOnTick = -1;
    private int lastNpcAnimation = -1;
    private int lastPlayerAnimation = -1;
    private int lastNpcAttackTick = -1;
    private int lastPlayerAttackTick = -1;
    private NPC lastFlickTarget = null;
    private boolean defPrayerByAnimation = false;
    private boolean offPrayerByAnimation = false;
    private final int[] npcIntervalSamples = new int[5];
    private int npcSampleIdx = 0;
    private int npcSampleCount = 0;
    private int detectedAttackSpeed = 0;
    private BankStep bankStep = BankStep.FOUNTAIN;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(SlayerBotConfig.class).toInstance((Object)((SlayerBotConfig)configManager.getConfig(SlayerBotConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.startTime = System.currentTimeMillis();
        this.profile = new AccountProfile(this.client);
        this.input = new HumanInput(this.client, this.profile);
        this.overlay = new SlayerBotOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.nextBreakAtMs = 0L;
        this.breakUntilMs = 0L;
        this.pauseStatus = null;
        this.inDangerousCombat = false;
        this.resetState();
        this.applyInputMode();
    }

    @Override
    protected void onStop() throws Exception {
        this.restoreInputMode();
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.status = "Idle";
    }

    @Override
    protected long jitterDelay(long l) {
        if (this.profile == null) {
            return l;
        }
        long l2 = this.profile.jitterDelay(l);
        if (this.inDangerousCombat) {
            l2 = Math.min(l2, Math.max(l, 1600L));
        }
        long l3 = this.input == null ? 0L : this.input.takeLastMoveMs();
        return Math.max(1L, l2 - l3);
    }

    private void applyInputMode() {
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

    private void walkTo(WorldPoint worldPoint) {
        WorldPoint worldPoint2;
        if (worldPoint == null) {
            return;
        }
        Player player = this.client.getLocalPlayer();
        WorldPoint worldPoint3 = worldPoint2 = player != null ? player.getWorldLocation() : null;
        if (this.input != null && worldPoint2 != null) {
            this.input.walkToward(worldPoint2, worldPoint);
        } else {
            MovementPackets.sendMovement(worldPoint);
        }
    }

    private void attack(NPC nPC) {
        if (nPC == null) {
            return;
        }
        if (this.input != null) {
            this.input.attackNpc(nPC);
        } else {
            nPC.interact("Attack");
        }
    }

    private void npcAct(NPC nPC, String string) {
        if (nPC == null) {
            return;
        }
        if (this.input != null) {
            this.input.npcActionVia(nPC, string);
        } else {
            nPC.interact(string);
        }
    }

    private void objAct(TileObject tileObject, String string) {
        if (tileObject == null) {
            return;
        }
        if (this.input != null) {
            this.input.interactObjectVia(tileObject, string);
        } else {
            tileObject.interact(string);
        }
    }

    private void groundTake(TileItem tileItem) {
        if (tileItem == null) {
            return;
        }
        if (this.input != null) {
            this.input.interactGroundItem(tileItem, "Take");
        } else {
            tileItem.interact("Take");
        }
    }

    private void itemAct(Item item, String string) {
        if (item == null) {
            return;
        }
        if (this.input != null) {
            this.input.itemInteractVia(item, string);
        } else {
            item.interact(string);
        }
    }

    private void fixedClick(int n, int n2) {
        if (this.input != null) {
            this.input.clickFixedMouse(n, n2);
        } else {
            Mouse.click(n, n2, true);
        }
    }

    private void chatCommand(String string) {
        if (string == null || string.isEmpty()) {
            return;
        }
        if (this.input != null) {
            this.input.command(string);
        } else {
            Keyboard.type(string);
            Keyboard.type('\n');
        }
    }

    private int maybeBreak(Player player) {
        if (!this.config.microBreaks()) {
            this.breakUntilMs = 0L;
            return 0;
        }
        long l = System.currentTimeMillis();
        boolean bl = this.breakSafeNow(player);
        if (this.breakUntilMs > 0L) {
            if (!bl) {
                this.breakUntilMs = 0L;
                this.scheduleNextBreak(l);
                return 0;
            }
            if (l < this.breakUntilMs) {
                this.pauseStatus = "Short break \u2014 " + (this.breakUntilMs - l) / 1000L + "s left";
                this.markProgress();
                return 600 + this.breakRng.nextInt(400);
            }
            this.breakUntilMs = 0L;
            this.scheduleNextBreak(l);
            return 0;
        }
        if (this.nextBreakAtMs == 0L) {
            this.scheduleNextBreak(l);
            return 0;
        }
        if (l >= this.nextBreakAtMs && bl) {
            long l2 = this.breakRng.nextDouble() < 0.8 ? 15000L + (long)this.breakRng.nextInt(35000) : 50000L + (long)this.breakRng.nextInt(40000);
            this.breakUntilMs = l + l2;
            this.pauseStatus = "Short break \u2014 " + l2 / 1000L + "s";
            return 800;
        }
        return 0;
    }

    private void scheduleNextBreak(long l) {
        this.nextBreakAtMs = l + 480000L + (long)this.breakRng.nextInt(960000);
    }

    private boolean breakSafeNow(Player player) {
        switch (this.state) {
            case IN_COMBAT: 
            case GET_TASK: 
            case CHECKING_GEM: 
            case REJUVENATING_HOME: 
            case GRAVEYARD_TRAVEL: {
                break;
            }
            default: {
                return false;
            }
        }
        if (player.getAnimation() == 836) {
            return false;
        }
        if (player.getInteracting() instanceof NPC && this.isLiveTarget((NPC)player.getInteracting())) {
            return false;
        }
        if (NPCs.getNearest(nPC -> nPC != null && nPC.getInteracting() == player && this.isLiveTarget((NPC)nPC)) != null) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n <= 0 || n2 * 100 / n >= 70;
    }

    private List<String> splitCached(String string) {
        return this.listCache.computeIfAbsent(string == null ? "" : string, this::splitList);
    }

    private void resetState() {
        this.state = SlayerState.CHECKING_GEM;
        this.status = "Starting";
        this.loggedInLastTick = false;
        this.wasInCombat = false;
        this.activeCombatTarget = null;
        this.outOfCombatSince = 0L;
        this.busyNpcs.clear();
        this.lastAttemptedTarget = null;
        this.gemCheckPending = false;
        this.walkingToVannakaSince = 0L;
        this.rejuvenatingHomeSince = 0L;
        this.noTargetSince = 0L;
        this.teleportPhase = TeleportPhase.WALK_TO_GLIDER;
        this.bankStep = BankStep.FOUNTAIN;
        this.presetLoadConfirmed = false;
        this.lockedPresetDetected = false;
        this.bankVerifyAt = 0L;
        this.startupBankDone = false;
        this.graveyardTeleAt = 0L;
        this.bankHomeTeleAt = 0L;
        this.graveyardTravelSince = 0L;
        this.graveyardTravelTeleAttempts = 0;
        this.graveyardDialogSeen = false;
        this.bankBoothMisses = 0;
        this.graveyardMasterMisses = 0;
        this.gemCheckMisses = 0;
        this.dialogMisses = 0;
        this.reNavCycles = 0;
        this.noTargetBankBails = 0;
        this.lastProgressAt = System.currentTimeMillis();
        this.watchdogTrips = 0;
        this.lastWatchdogAt = 0L;
        this.prayerActivatedOnTick = -1;
        this.boostActivatedOnTick = -1;
        this.lastNpcAnimation = -1;
        this.lastPlayerAnimation = -1;
        this.lastNpcAttackTick = -1;
        this.lastPlayerAttackTick = -1;
        this.lastFlickTarget = null;
        this.defPrayerByAnimation = false;
        this.offPrayerByAnimation = false;
        Arrays.fill(this.npcIntervalSamples, 0);
        this.npcSampleIdx = 0;
        this.npcSampleCount = 0;
        this.detectedAttackSpeed = 0;
    }

    private void enterTeleporting() {
        this.state = SlayerState.TELEPORTING;
        this.teleportPhase = TeleportPhase.WALK_TO_GLIDER;
        this.teleportPhaseAt = System.currentTimeMillis();
        this.noTargetSince = 0L;
        this.homeTeleportAttempts = 0;
    }

    private void transitionTo(SlayerState slayerState) {
        if (slayerState != this.state) {
            this.markProgress();
        }
        this.state = slayerState;
        this.stateEnteredAt = System.currentTimeMillis();
        this.bankBoothMisses = 0;
        this.graveyardMasterMisses = 0;
    }

    private void markProgress() {
        this.lastProgressAt = System.currentTimeMillis();
        this.watchdogTrips = 0;
    }

    private void resetPrayerFlickState() {
        this.outOfCombatSince = 0L;
        this.lastFlickTarget = null;
        this.prayerActivatedOnTick = -1;
        this.lastNpcAttackTick = -1;
        this.lastNpcAnimation = -1;
        Arrays.fill(this.npcIntervalSamples, 0);
        this.npcSampleIdx = 0;
        this.npcSampleCount = 0;
        this.detectedAttackSpeed = 0;
        this.defPrayerByAnimation = false;
        this.boostActivatedOnTick = -1;
        this.lastPlayerAttackTick = -1;
        this.offPrayerByAnimation = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        switch (gameStateChanged.getGameState()) {
            case CONNECTION_LOST: {
                this.status = "Connection lost";
                this.wasInCombat = false;
                this.activeCombatTarget = null;
                this.loggedInLastTick = false;
                this.busyNpcs.clear();
                this.lastAttemptedTarget = null;
                break;
            }
            case LOGIN_SCREEN: {
                this.status = "Logged out";
                this.wasInCombat = false;
                this.activeCombatTarget = null;
                this.loggedInLastTick = false;
                this.busyNpcs.clear();
                this.lastAttemptedTarget = null;
                break;
            }
            case LOGGED_IN: {
                this.loggedInLastTick = false;
                break;
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        Matcher matcher;
        String string = chatMessage.getMessage().replaceAll("<[^>]*>", "");
        String string2 = string.toLowerCase();
        ChatMessageType chatMessageType = chatMessage.getType();
        if ((chatMessageType == ChatMessageType.GAMEMESSAGE || chatMessageType == ChatMessageType.ENGINE || chatMessageType == ChatMessageType.SPAM) && BUSY_NPC_PATTERN.matcher(string2).find()) {
            NPC nPC = this.lastAttemptedTarget;
            if (nPC != null) {
                this.busyNpcs.put(nPC.getIndex(), System.currentTimeMillis() + 8000L);
                if (this.activeCombatTarget == nPC) {
                    this.activeCombatTarget = null;
                }
                if (this.engagedTarget == nPC) {
                    this.engagedTarget = null;
                }
                this.status = "NPC busy/owned \u2014 switching target";
            }
            this.lastAttemptedTarget = null;
            return;
        }
        if (string2.contains("inventory was loaded successfully") || string2.contains("equipment was loaded successfully")) {
            this.presetLoadConfirmed = true;
            this.markProgress();
        }
        if (LOCKED_PRESET_PATTERN.matcher(string2).find()) {
            this.lockedPresetDetected = true;
        }
        if (this.gemCheckPending) {
            matcher = GEM_TASK_PATTERN.matcher(string);
            if (matcher.find()) {
                this.gemCheckPending = false;
                this.gemCheckMisses = 0;
                this.reNavCycles = 0;
                this.noTargetBankBails = 0;
                this.markProgress();
                try {
                    this.currentTask = SlayerBotPlugin.capitalise(matcher.group(1).trim());
                    this.killsRemaining = Integer.parseInt(matcher.group(2));
                    this.killsThisTask = 0;
                    this.taskComplete = false;
                    this.status = "Gem verified: " + this.currentTask + " x" + this.killsRemaining;
                    this.state = SlayerState.GET_TASK;
                }
                catch (NumberFormatException numberFormatException) {
                    this.state = SlayerState.GET_TASK;
                }
                return;
            }
            if (GEM_NO_TASK_PATTERN.matcher(string).find()) {
                this.gemCheckPending = false;
                this.gemCheckMisses = 0;
                this.markProgress();
                this.status = "No task \u2014 getting new assignment";
                this.currentTask = "";
                this.killsRemaining = 0;
                this.killsThisTask = 0;
                this.taskComplete = false;
                this.state = SlayerState.GET_TASK;
                return;
            }
        }
        if (DONE_PATTERN.matcher(string).find()) {
            this.taskComplete = true;
            ++this.tasksCompleted;
            this.killsRemaining = 0;
            this.status = "Task complete! (" + this.tasksCompleted + " done)";
            return;
        }
        matcher = TASK_PATTERN.matcher(string);
        if (matcher.find()) {
            try {
                int n = Integer.parseInt(matcher.group(1));
                String string3 = SlayerBotPlugin.capitalise(matcher.group(2).trim());
                this.killsRemaining = n;
                this.killsThisTask = 0;
                this.currentTask = string3;
                this.taskComplete = false;
                this.dialogMisses = 0;
                this.reNavCycles = 0;
                this.noTargetBankBails = 0;
                this.markProgress();
                this.status = "Got task: " + string3 + " x" + n + " \u2014 verifying with gem";
                this.gemCheckPending = false;
                if (this.state != SlayerState.AWAITING_DIALOG && this.state != SlayerState.GRAVEYARD_DIALOG) {
                    this.state = SlayerState.CHECKING_GEM;
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.status = "Loading...";
            this.wasInCombat = false;
            this.activeCombatTarget = null;
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.status = "Resuming...";
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (RandomEventState.isActive()) {
            this.pauseStatus = "Paused \u2014 " + RandomEventState.label();
            return 1500;
        }
        int n = this.maybeBreak(player);
        if (n > 0) {
            return n;
        }
        this.pauseStatus = null;
        if (this.state != SlayerState.STOPPED) {
            if (this.lastProgressAt == 0L) {
                this.lastProgressAt = System.currentTimeMillis();
            }
            long l = System.currentTimeMillis() - this.lastProgressAt;
            long l2 = (long)this.config.watchdogStallSeconds() * 1000L;
            if (l2 > 0L && l > l2 && System.currentTimeMillis() - this.lastWatchdogAt > l2) {
                this.lastWatchdogAt = System.currentTimeMillis();
                ++this.watchdogTrips;
                if (this.watchdogTrips >= this.config.watchdogMaxTrips() && this.config.watchdogStopOnGiveUp()) {
                    this.status = "WATCHDOG: no progress " + l / 1000L + "s after " + this.watchdogTrips + " recoveries \u2014 STOPPING";
                    log.warn("[SlayerBot] watchdog give-up: state={} task='{}' stalled={}s", new Object[]{this.state, this.currentTask, l / 1000L});
                    this.transitionTo(SlayerState.STOPPED);
                    return -1;
                }
                log.warn("[SlayerBot] watchdog trip #{} state={} task='{}' stalled={}s \u2014 re-homing", new Object[]{this.watchdogTrips, this.state, this.currentTask, l / 1000L});
                this.status = "WATCHDOG: stalled " + l / 1000L + "s in " + String.valueOf((Object)this.state) + " \u2014 recovering";
                this.activeCombatTarget = null;
                this.wasInCombat = false;
                this.engagedTarget = null;
                this.resetPrayerFlickState();
                this.teleportHome();
                this.bankStep = BankStep.FOUNTAIN;
                int n2 = this.watchdogTrips;
                this.transitionTo(SlayerState.BANKING);
                this.watchdogTrips = n2;
                return 10000;
            }
        }
        if (player.getAnimation() == 836 && this.state != SlayerState.DEATH_RECOVERY) {
            this.status = "Death detected";
            this.activeCombatTarget = null;
            this.wasInCombat = false;
            this.resetPrayerFlickState();
            this.state = SlayerState.DEATH_RECOVERY;
            return 3000;
        }
        if (this.state != SlayerState.IN_COMBAT && this.config.enablePrayer() && Prayers.anyActive()) {
            Prayers.disableAll();
        }
        if (this.state != SlayerState.IN_COMBAT) {
            this.inDangerousCombat = false;
        }
        switch (this.state) {
            case CHECKING_GEM: {
                return this.loopCheckingGem();
            }
            case GET_TASK: {
                return this.loopGetTask(player);
            }
            case REJUVENATING_HOME: {
                return this.loopRejuvenatingHome(player);
            }
            case AWAITING_DIALOG: {
                return this.loopAwaitingDialog();
            }
            case TELEPORTING: {
                return this.loopTeleporting(player);
            }
            case IN_COMBAT: {
                return this.loopCombat(player);
            }
            case DEATH_RECOVERY: {
                return this.loopDeathRecovery(player);
            }
            case BANKING: {
                return this.loopBanking(player);
            }
            case BANKING_FOR_GEM: {
                return this.loopBankingForGem(player);
            }
            case GRAVEYARD_GET_ASSIGNMENT: {
                return this.loopGraveyardGetAssignment(player);
            }
            case GRAVEYARD_DIALOG: {
                return this.loopGraveyardDialog(player);
            }
            case GRAVEYARD_TRAVEL: {
                return this.loopGraveyardTravel(player);
            }
            case STOPPED: {
                return -1;
            }
        }
        return 600;
    }

    private int loopCheckingGem() {
        Item item = Inventory.getFirst(4155);
        if (item == null) {
            this.status = "No gem \u2014 going to Vannaka";
            this.state = SlayerState.GET_TASK;
            return 300;
        }
        if (!this.gemCheckPending) {
            this.status = "Checking enchanted gem";
            this.itemAct(item, "Check");
            this.gemCheckPending = true;
            this.gemCheckAt = System.currentTimeMillis();
            return 1500;
        }
        if (System.currentTimeMillis() - this.gemCheckAt > 5000L) {
            this.gemCheckPending = false;
            ++this.gemCheckMisses;
            if (this.gemCheckMisses >= 12) {
                log.warn("[SlayerBot] gem check give-up after {} silent checks", (Object)this.gemCheckMisses);
                this.gemCheckMisses = 0;
                if (this.config.watchdogStopOnGiveUp()) {
                    this.status = "Gem check no-response x12 \u2014 stopping";
                    this.transitionTo(SlayerState.STOPPED);
                    return -1;
                }
                this.status = "Gem check no-response \u2014 proceeding to Vannaka";
            } else {
                this.status = "Gem timeout (" + this.gemCheckMisses + ") \u2014 going to Vannaka";
            }
            this.transitionTo(SlayerState.GET_TASK);
        }
        return 600;
    }

    private int loopGetTask(Player player) {
        if (this.config.graveyardMode()) {
            return this.loopGraveyardGetTask(player);
        }
        if (!this.currentTask.isEmpty() && this.killsRemaining > 0 && !this.taskComplete) {
            boolean bl;
            boolean bl2 = bl = this.countFood() <= this.config.bankWhenFoodLeft();
            if (!this.startupBankDone && bl) {
                this.status = "Startup banking before first task";
                this.startupBankDone = true;
                this.bankStep = BankStep.FOUNTAIN;
                this.state = SlayerState.BANKING;
                return 300;
            }
            this.startupBankDone = true;
            if (bl) {
                this.status = "Low food \u2014 banking before teleport";
                this.bankStep = BankStep.FOUNTAIN;
                this.state = SlayerState.BANKING;
                return 300;
            }
            this.enterTeleporting();
            return 300;
        }
        if (this.config.stopAfterTasks() > 0 && this.tasksCompleted >= this.config.stopAfterTasks()) {
            this.status = "Done (" + this.tasksCompleted + " tasks)";
            this.state = SlayerState.STOPPED;
            return -1;
        }
        this.taskComplete = false;
        WorldPoint worldPoint = player.getWorldLocation();
        if (worldPoint.distanceTo(VANNAKA) > 20) {
            this.status = "Home teleporting \u2014 will rejuvenate on arrival";
            this.walkingToVannakaSince = 0L;
            this.teleportHome();
            this.state = SlayerState.REJUVENATING_HOME;
            return 10000;
        }
        if (worldPoint.distanceTo(VANNAKA) > 5) {
            if (this.walkingToVannakaSince == 0L) {
                this.walkingToVannakaSince = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - this.walkingToVannakaSince > 30000L) {
                this.status = "Stuck walking \u2014 home teleporting";
                this.walkingToVannakaSince = 0L;
                this.teleportHome();
                this.state = SlayerState.REJUVENATING_HOME;
                return 10000;
            }
            this.status = "Walking to Vannaka";
            this.walkTo(VANNAKA);
            return 1200;
        }
        this.walkingToVannakaSince = 0L;
        int n = this.config.slayerMaster().getNpcId();
        NPC nPC2 = NPCs.getNearest(nPC -> nPC.getId() == n);
        if (nPC2 != null) {
            this.status = "Getting assignment";
            this.npcAct(nPC2, GRAVEYARD_MASTER_ACTION);
            this.state = SlayerState.AWAITING_DIALOG;
            this.stateEnteredAt = System.currentTimeMillis();
            return 2500;
        }
        this.status = "Looking for Vannaka";
        return 1000;
    }

    private int loopRejuvenatingHome(Player player) {
        int n;
        TileObject tileObject2;
        if (this.rejuvenatingHomeSince == 0L) {
            this.rejuvenatingHomeSince = System.currentTimeMillis();
        }
        TileObject tileObject3 = tileObject2 = (n = 42822) != 0 ? TileObjects.getNearest(tileObject -> tileObject.getId() == n) : null;
        if (tileObject2 != null) {
            this.status = "Rejuvenating at pool";
            this.objAct(tileObject2, "Rejuvenate");
            this.rejuvenatingHomeSince = 0L;
            this.bankStep = BankStep.PRESET;
            this.state = SlayerState.BANKING;
            return 4000;
        }
        if (n == 0) {
            this.rejuvenatingHomeSince = 0L;
            this.bankStep = BankStep.PRESET;
            this.state = SlayerState.BANKING;
            return 300;
        }
        if (player.getWorldLocation().distanceTo(VANNAKA) > 15) {
            this.status = "Walking to rejuvenation pool";
            this.walkTo(VANNAKA);
            return 1200;
        }
        if (System.currentTimeMillis() - this.rejuvenatingHomeSince > 20000L) {
            this.status = "Pool not found \u2014 banking anyway";
            this.rejuvenatingHomeSince = 0L;
            this.bankStep = BankStep.PRESET;
            this.state = SlayerState.BANKING;
            return 300;
        }
        this.status = "Looking for rejuvenation pool...";
        return 600;
    }

    private int loopAwaitingDialog() {
        long l = System.currentTimeMillis() - this.stateEnteredAt;
        if (l < 3000L) {
            this.status = "Waiting for assignment...";
            return 600;
        }
        if (!this.currentTask.isEmpty() && this.killsRemaining > 0) {
            Item item = Inventory.getFirst(4155);
            if (item != null) {
                this.gemCheckPending = false;
                this.state = SlayerState.CHECKING_GEM;
                return 300;
            }
            this.status = "No gem \u2014 going to bank";
            this.state = SlayerState.BANKING_FOR_GEM;
            return 300;
        }
        if (l > 8000L) {
            ++this.dialogMisses;
            if (this.dialogMisses >= 12) {
                log.warn("[SlayerBot] assignment-dialog give-up after {} timeouts (master silent or TASK_PATTERN mismatch)", (Object)this.dialogMisses);
                this.dialogMisses = 0;
                if (this.config.watchdogStopOnGiveUp()) {
                    this.status = "Assignment never parsed x12 \u2014 stopping";
                    this.transitionTo(SlayerState.STOPPED);
                    return -1;
                }
                this.status = "Assignment never parsed \u2014 retrying";
            } else {
                this.status = "Assignment dialog timeout (" + this.dialogMisses + ") \u2014 retrying";
            }
            this.transitionTo(SlayerState.GET_TASK);
            return 300;
        }
        Item item = Inventory.getFirst(4155);
        if (item != null) {
            this.gemCheckPending = false;
            this.state = SlayerState.CHECKING_GEM;
            return 300;
        }
        this.status = "No gem \u2014 going to bank";
        this.state = SlayerState.BANKING_FOR_GEM;
        return 300;
    }

    private int loopTeleporting(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        long l = System.currentTimeMillis();
        switch (this.teleportPhase.ordinal()) {
            case 0: {
                if (worldPoint.distanceTo(GLIDER) > 4) {
                    if (worldPoint.distanceTo(GLIDER) > 60) {
                        if (this.homeTeleportAttempts >= 3) {
                            this.status = "Glider unreachable after " + this.homeTeleportAttempts + " home teles \u2014 stopping";
                            this.state = SlayerState.STOPPED;
                            return -1;
                        }
                        ++this.homeTeleportAttempts;
                        this.status = "Home teleporting to reach glider (attempt " + this.homeTeleportAttempts + ")";
                        this.teleportHome();
                        return 10000;
                    }
                    this.status = "Walking to Gnome Glider";
                    this.walkTo(GLIDER);
                    return 1200;
                }
                this.homeTeleportAttempts = 0;
                this.teleportPhase = TeleportPhase.CLICK_TRAVEL;
                this.teleportPhaseAt = l;
                return 300;
            }
            case 1: {
                TileObject tileObject = this.findGlider();
                if (tileObject != null) {
                    this.status = "Clicking Travel on Gnome Glider";
                    this.objAct(tileObject, GLIDER_TRAVEL_ACTION);
                    this.teleportPhase = TeleportPhase.CLICK_SEARCH;
                    this.teleportPhaseAt = l;
                    return 6000;
                }
                this.status = "Looking for Gnome Glider";
                return 800;
            }
            case 2: {
                this.status = "Clicking Search @ 75,305";
                this.fixedClick(75, 305);
                this.teleportPhase = TeleportPhase.TYPE_QUERY;
                this.teleportPhaseAt = l;
                return 3000;
            }
            case 3: {
                String string = this.currentTask.toLowerCase().contains("wyvern") ? "wyvern" : (TELEPORT_SEARCH_OVERRIDES.containsKey(this.currentTask) ? TELEPORT_SEARCH_OVERRIDES.get(this.currentTask) : SlayerNames.core(this.currentTask));
                this.status = "Typing: " + string;
                this.chatCommand(string);
                this.teleportPhase = TeleportPhase.CLICK_SELECT;
                this.teleportPhaseAt = l;
                return 1200;
            }
            case 4: {
                this.preClickPos = worldPoint;
                this.status = "Clicking result @ 220,90";
                this.fixedClick(220, 90);
                this.teleportPhase = TeleportPhase.VERIFY_TELEPORT;
                this.teleportPhaseAt = l;
                return 1500;
            }
            case 5: {
                if (Dialog.isOpen() && Dialog.isViewingOptions()) {
                    this.status = "Confirming wilderness teleport (Yes)";
                    if (!Dialog.chooseOption("Yes, take me there") && !Dialog.chooseOption("Yes")) {
                        Dialog.chooseOption(1);
                    }
                    this.teleportPhaseAt = l;
                    return 1200;
                }
                if (this.preClickPos != null && worldPoint.distanceTo(this.preClickPos) > 15) {
                    this.status = "Teleported to " + this.currentTask + " area";
                    this.state = SlayerState.IN_COMBAT;
                    this.teleportPhase = TeleportPhase.WALK_TO_GLIDER;
                    this.wasInCombat = false;
                    this.activeCombatTarget = null;
                    this.preClickPos = null;
                    return 600;
                }
                long l2 = l - this.teleportPhaseAt;
                if (l2 > 6000L) {
                    this.status = "Teleport failed \u2014 retrying";
                    this.teleportPhase = TeleportPhase.CLICK_TRAVEL;
                    this.teleportPhaseAt = l;
                    return 300;
                }
                this.status = "Waiting for teleport (" + (6000L - l2) / 1000L + "s)...";
                return 500;
            }
        }
        return 600;
    }

    private int loopCombat(Player player) {
        Item item;
        int n;
        Object object;
        int n2;
        Item item2;
        boolean bl;
        boolean bl2;
        if (player.getAnimation() == 836) {
            this.status = "Death detected \u2014 waiting for respawn";
            this.activeCombatTarget = null;
            this.wasInCombat = false;
            this.resetPrayerFlickState();
            this.state = SlayerState.DEATH_RECOVERY;
            return 3000;
        }
        if (this.activeCombatTarget != null) {
            if (this.activeCombatTarget.isDead() || this.activeCombatTarget.getHealthRatio() == 0) {
                this.killsRemaining = Math.max(0, this.killsRemaining - 1);
                ++this.killsThisTask;
                ++this.totalKills;
                this.reNavCycles = 0;
                this.noTargetBankBails = 0;
                this.markProgress();
                this.activeCombatTarget = null;
            } else if (!this.isPresent(this.activeCombatTarget)) {
                this.activeCombatTarget = null;
            }
        }
        NPC nPC2 = player.getInteracting() instanceof NPC ? (NPC)player.getInteracting() : null;
        NPC nPC3 = NPCs.getNearest(nPC -> nPC != null && nPC.getInteracting() == player && this.isLiveTarget((NPC)nPC));
        boolean bl3 = this.isLiveTarget(nPC2) || nPC3 != null;
        this.inDangerousCombat = bl2 = this.isLiveTarget(this.activeCombatTarget) || bl3;
        if (bl2) {
            this.markProgress();
        }
        if (bl3) {
            this.noTargetSince = 0L;
            this.outOfCombatSince = 0L;
        } else if (this.outOfCombatSince == 0L) {
            this.outOfCombatSince = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - this.outOfCombatSince > 2000L) {
            this.status = "Out of combat \u2014 finding new target";
            this.activeCombatTarget = null;
            this.outOfCombatSince = 0L;
        }
        boolean bl4 = bl = this.isLiveTarget(nPC2) || this.isLiveTarget(this.activeCombatTarget) && this.taskMatches(this.activeCombatTarget.getName()) && !this.isBusy(this.activeCombatTarget);
        if (this.config.enablePrayer()) {
            this.handlePrayer(player, bl2);
        }
        if (this.taskComplete || this.killsRemaining <= 0) {
            this.status = "Task done \u2014 going for new task";
            this.currentTask = "";
            this.killsRemaining = 0;
            this.taskComplete = false;
            this.activeCombatTarget = null;
            this.wasInCombat = false;
            this.inDangerousCombat = false;
            this.resetPrayerFlickState();
            this.state = SlayerState.GET_TASK;
            return 300;
        }
        if (this.countFood() <= this.config.bankWhenFoodLeft()) {
            this.status = "Low food \u2014 teleporting home to bank";
            this.state = SlayerState.BANKING;
            this.bankStep = BankStep.FOUNTAIN;
            this.activeCombatTarget = null;
            this.wasInCombat = false;
            this.inDangerousCombat = false;
            this.resetPrayerFlickState();
            this.teleportHome();
            return 10000;
        }
        if (this.client.getEnergy() >= 3000 && this.client.getVarbitValue(173) == 0) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(10485782, -1);
        }
        if (this.config.eatFood() && this.shouldEat(player) && (item2 = this.findFood()) != null) {
            this.status = "Eating " + this.itemName(item2.getId());
            this.eatFood(item2);
            return 1800;
        }
        if (this.config.enablePrayer() && (n2 = this.client.getBoostedSkillLevel(Skill.PRAYER)) > 0 && n2 <= this.config.drinkPrayerAt() && (object = this.findPrayerPotion()) != null) {
            this.status = "Drinking prayer pot";
            this.drinkPotion((Item)object);
            return 600;
        }
        if (this.config.combatPotion() != CombatPotion.NONE && this.shouldDrinkCombatPot() && (item2 = this.findByName(this.config.combatPotion().toString())) != null) {
            this.status = "Drinking " + String.valueOf((Object)this.config.combatPotion());
            this.drinkPotion(item2);
            return 600;
        }
        if (this.config.enableSpec() && bl && this.weaponHasSpecial() && (n = this.client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT)) >= this.config.specAt() * 10) {
            this.status = "Using special attack";
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(38862884, -1);
            return 600;
        }
        if (this.config.buryBones() && (item = this.findBones()) != null) {
            object = this.itemName(item.getId()).toLowerCase();
            String string = ((String)object).contains("ashes") ? "Scatter" : "Bury";
            this.status = (string.equals("Scatter") ? "Scattering " : "Burying ") + this.itemName(item.getId());
            this.itemAct(item, string);
            return 600;
        }
        if (bl) {
            int n3;
            if (!(this.config.lootOnlyWhenSafe() && this.config.alchOnlyWhenSafe() || (n3 = this.handleLoot(player, true)) < 0)) {
                return n3;
            }
            if (this.isLiveTarget(nPC2)) {
                this.activeCombatTarget = nPC2;
                this.wasInCombat = true;
                this.status = "Fighting " + this.currentTask + " (" + this.killsRemaining + " left)";
                return 300;
            }
            this.wasInCombat = true;
            return this.attackOrApproach(player, this.activeCombatTarget, "Re-engaging");
        }
        int n4 = this.handleLoot(player, false);
        if (n4 >= 0) {
            return n4;
        }
        if (nPC3 != null) {
            this.wasInCombat = true;
            return this.attackOrApproach(player, nPC3, "Re-engaging");
        }
        this.wasInCombat = false;
        this.activeCombatTarget = null;
        this.engagedTarget = null;
        return this.attackTask(player);
    }

    private int handleLoot(Player player, boolean bl) {
        Item item2;
        boolean bl2;
        Item item3;
        boolean bl3;
        boolean bl4 = !bl || !this.config.lootOnlyWhenSafe();
        boolean bl5 = bl3 = !bl || !this.config.alchOnlyWhenSafe();
        if (bl3 && this.config.enableHighAlch() && (item3 = Inventory.getFirst(item -> item != null && item.getId() != -1 && this.shouldAlchInventoryItem((Item)item))) != null && this.canCastHighAlch()) {
            this.status = "High Alching " + this.itemName(item3.getId());
            this.alchProfit += (long)this.highAlchValue(item3.getId());
            Magic.cast((Spell)SpellBook.Standard.HIGH_LEVEL_ALCHEMY, item3);
            return 1800;
        }
        boolean bl6 = bl4 && this.config.enableLooting() && this.hasLootNearby(player);
        boolean bl7 = bl2 = bl3 && this.config.enableHighAlch() && this.hasAlchLootNearby(player);
        if (this.config.eatFoodForLoot() && this.isInventoryFull() && (bl6 || bl2) && (item2 = this.findFood()) != null) {
            this.status = "Eating to make loot space";
            this.eatFood(item2);
            return 1800;
        }
        if (this.isInventoryFull()) {
            return -1;
        }
        if (bl4 && this.config.enableLooting() && this.lootNearby(player)) {
            return 600;
        }
        if (bl3 && this.config.enableHighAlch() && this.alchLootNearby(player)) {
            return 600;
        }
        if (bl4 && this.config.buryBones() && this.pickUpBonesNearby(player)) {
            return 600;
        }
        return -1;
    }

    private int loopDeathRecovery(Player player) {
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        if (n <= 0) {
            this.status = "Waiting for respawn...";
            return 1000;
        }
        this.status = "Respawned \u2014 teleporting home to restock";
        this.teleportHome();
        this.bankStep = BankStep.FOUNTAIN;
        this.state = SlayerState.BANKING;
        return 10000;
    }

    private TileObject findBankBooth() {
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject.getId() == 10083);
        return tileObject2 != null ? tileObject2 : TileObjects.getNearest("Bank booth");
    }

    private int bankBoothMissing(Player player) {
        ++this.bankBoothMisses;
        if (this.bankBoothMisses >= 12) {
            log.warn("[SlayerBot] bank booth give-up after {} misses at {}", (Object)this.bankBoothMisses, (Object)player.getWorldLocation());
            if (this.config.watchdogStopOnGiveUp()) {
                this.status = "Bank booth never found (ID 10083) \u2014 stopping";
                this.transitionTo(SlayerState.STOPPED);
                return -1;
            }
            this.status = "Bank booth never found (ID 10083) \u2014 re-homing";
            this.bankBoothMisses = 0;
            this.teleportHome();
            return 10000;
        }
        if (this.bankBoothMisses % 5 == 0) {
            this.status = "Bank booth not found \u2014 re-homing (try " + this.bankBoothMisses + ")";
            this.teleportHome();
            return 10000;
        }
        this.status = "Bank booth not found (ID 10083) \u2014 retry " + this.bankBoothMisses;
        return 600;
    }

    private int loopBanking(Player player) {
        TileObject tileObject2;
        int n;
        if (this.config.graveyardMode()) {
            TileObject tileObject3;
            n = 42822;
            tileObject2 = this.findBankBooth();
            TileObject tileObject4 = tileObject3 = n != 0 ? TileObjects.getNearest(tileObject -> tileObject.getId() == n) : null;
            if (tileObject2 == null && tileObject3 == null) {
                if (this.bankHomeTeleAt == 0L || System.currentTimeMillis() - this.bankHomeTeleAt > 9000L) {
                    this.status = "Teleporting home to bank (::home)";
                    this.teleportHome();
                    this.bankHomeTeleAt = System.currentTimeMillis();
                    return 10000;
                }
                this.status = "Waiting to arrive at bank...";
                return 600;
            }
            this.bankHomeTeleAt = 0L;
        } else {
            if (player.getWorldLocation().distanceTo(VANNAKA) > 80) {
                if (this.bankHomeTeleAt == 0L || System.currentTimeMillis() - this.bankHomeTeleAt > 9000L) {
                    this.status = "Too far from bank \u2014 teleporting home (::home)";
                    this.teleportHome();
                    this.bankHomeTeleAt = System.currentTimeMillis();
                    return 10000;
                }
                this.status = "Waiting for home teleport...";
                return 600;
            }
            this.bankHomeTeleAt = 0L;
            if (player.getWorldLocation().distanceTo(VANNAKA) > 20) {
                this.status = "Walking to bank area";
                this.walkTo(VANNAKA);
                return 1200;
            }
        }
        switch (this.bankStep.ordinal()) {
            case 0: {
                n = 42822;
                TileObject tileObject5 = tileObject2 = n != 0 ? TileObjects.getNearest(tileObject -> tileObject.getId() == n) : null;
                if (tileObject2 != null) {
                    this.status = "Rejuvenating at fountain";
                    this.objAct(tileObject2, "Rejuvenate");
                    this.bankStep = BankStep.PRESET;
                    return 4000;
                }
                this.status = "Fountain not found \u2014 going to bank";
                this.bankStep = BankStep.PRESET;
                return 600;
            }
            case 1: {
                TileObject tileObject6 = this.findBankBooth();
                if (tileObject6 == null) {
                    return this.bankBoothMissing(player);
                }
                this.bankBoothMisses = 0;
                if (player.getWorldLocation().distanceTo(tileObject6.getWorldLocation()) > 3) {
                    this.status = "Walking to bank booth";
                    this.walkTo(tileObject6.getWorldLocation());
                    return 1200;
                }
                this.status = "Bank \u2014 Last-preset";
                this.presetLoadConfirmed = false;
                this.lockedPresetDetected = false;
                this.objAct(tileObject6, "Last-preset");
                this.bankVerifyAt = System.currentTimeMillis();
                this.bankStep = BankStep.VERIFY_PRESET;
                return 1200;
            }
            case 2: {
                if (this.presetLoadConfirmed) {
                    this.status = "Preset loaded successfully";
                    this.bankStep = BankStep.DONE;
                    return 600;
                }
                if (this.lockedPresetDetected) {
                    this.lockedPresetDetected = false;
                    this.status = "Preset locked \u2014 opening bank to load manually";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 300;
                }
                if (System.currentTimeMillis() - this.bankVerifyAt > 3000L) {
                    this.status = "Last-preset unconfirmed \u2014 manual preset load";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 300;
                }
                return 400;
            }
            case 3: {
                if (Bank.isOpen()) {
                    this.status = "Bank open \u2014 loading preset";
                    this.bankStep = BankStep.MANUAL_LOAD;
                    return 600;
                }
                TileObject tileObject7 = this.findBankBooth();
                if (tileObject7 == null) {
                    return this.bankBoothMissing(player);
                }
                this.bankBoothMisses = 0;
                if (player.getWorldLocation().distanceTo(tileObject7.getWorldLocation()) > 3) {
                    this.status = "Walking to bank booth";
                    this.walkTo(tileObject7.getWorldLocation());
                    return 1200;
                }
                this.status = "Opening bank (Use)";
                this.presetLoadConfirmed = false;
                this.objAct(tileObject7, BANK_ACTION);
                this.bankOpenAt = System.currentTimeMillis();
                this.bankStep = BankStep.MANUAL_WAIT_OPEN;
                return 1000;
            }
            case 4: {
                if (Bank.isOpen()) {
                    this.status = "Bank open \u2014 loading preset";
                    this.bankStep = BankStep.MANUAL_LOAD;
                    return 600;
                }
                if (System.currentTimeMillis() - this.bankOpenAt > 5000L) {
                    this.status = "Bank didn't open \u2014 retrying";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 600;
                }
                this.status = "Waiting for bank to open...";
                return 400;
            }
            case 5: {
                if (!Bank.isOpen()) {
                    this.status = "Bank closed early \u2014 reopening";
                    this.bankStep = BankStep.MANUAL_OPEN;
                    return 600;
                }
                this.status = "Manual preset: load @ 448,17";
                this.fixedClick(448, 17);
                this.bankStep = BankStep.MANUAL_CONFIRM;
                return 800;
            }
            case 6: {
                this.status = "Manual preset: confirm @ 68,282";
                this.fixedClick(68, 282);
                this.bankStep = BankStep.MANUAL_CLOSE;
                return 500;
            }
            case 7: {
                this.status = "Closing bank @ 483,33";
                this.fixedClick(483, 33);
                this.bankStep = BankStep.DONE;
                return 1000;
            }
            case 8: {
                this.bankStep = BankStep.FOUNTAIN;
                this.status = !this.presetLoadConfirmed ? "Bank done (preset load unconfirmed)" : (this.taskComplete || this.killsRemaining <= 0 ? "Bank done \u2014 getting new task" : "Bank done \u2014 verifying task");
                this.state = this.taskComplete || this.killsRemaining <= 0 ? SlayerState.GET_TASK : SlayerState.CHECKING_GEM;
                return 600;
            }
        }
        return 600;
    }

    private int loopBankingForGem(Player player) {
        Item item = Inventory.getFirst(4155);
        if (item != null) {
            this.status = "Have gem \u2014 checking task";
            this.state = SlayerState.CHECKING_GEM;
            return 300;
        }
        this.bankStep = BankStep.FOUNTAIN;
        this.state = SlayerState.BANKING;
        return 300;
    }

    private int loopGraveyardGetTask(Player player) {
        boolean bl;
        if (!this.currentTask.isEmpty() && this.killsRemaining > 0 && !this.taskComplete) {
            boolean bl2;
            boolean bl3 = this.countFood() <= this.config.bankWhenFoodLeft();
            boolean bl4 = bl2 = Inventory.getFirst(4155) == null;
            if (!this.startupBankDone && (bl3 || bl2)) {
                this.status = "Startup banking before first task";
                this.startupBankDone = true;
                this.bankStep = BankStep.FOUNTAIN;
                this.state = SlayerState.BANKING;
                return 300;
            }
            this.startupBankDone = true;
            if (bl3) {
                this.status = "Low food \u2014 banking before travel";
                this.bankStep = BankStep.FOUNTAIN;
                this.state = SlayerState.BANKING;
                return 300;
            }
            if (bl2) {
                this.status = "No gem \u2014 banking";
                this.state = SlayerState.BANKING_FOR_GEM;
                return 300;
            }
            this.state = SlayerState.GRAVEYARD_TRAVEL;
            return 300;
        }
        if (this.config.stopAfterTasks() > 0 && this.tasksCompleted >= this.config.stopAfterTasks()) {
            this.status = "Done (" + this.tasksCompleted + " tasks)";
            this.state = SlayerState.STOPPED;
            return -1;
        }
        this.taskComplete = false;
        boolean bl5 = !this.hasTaskGp();
        boolean bl6 = Inventory.getFirst(4155) == null;
        boolean bl7 = bl = this.countFood() <= this.config.bankWhenFoodLeft();
        if (bl5 || bl6 || bl || !this.startupBankDone) {
            this.status = !this.startupBankDone ? "Startup banking" : (bl5 ? "Need " + this.config.graveyardTaskCost() + " GP \u2014 resupplying" : (bl6 ? "No gem \u2014 resupplying" : "Low food \u2014 resupplying"));
            this.startupBankDone = true;
            this.bankStep = BankStep.FOUNTAIN;
            this.state = SlayerState.BANKING;
            return 300;
        }
        this.graveyardTeleAt = 0L;
        this.state = SlayerState.GRAVEYARD_GET_ASSIGNMENT;
        return 300;
    }

    private int loopGraveyardGetAssignment(Player player) {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC.getId() == 9579);
        if (nPC2 != null) {
            this.graveyardMasterMisses = 0;
            if (player.getWorldLocation().distanceTo(nPC2.getWorldLocation()) > 2) {
                this.status = "Walking to graveyard master";
                this.walkTo(nPC2.getWorldLocation());
                return 1000;
            }
            this.status = "Getting graveyard assignment";
            this.npcAct(nPC2, GRAVEYARD_MASTER_ACTION);
            this.state = SlayerState.GRAVEYARD_DIALOG;
            this.stateEnteredAt = System.currentTimeMillis();
            this.graveyardDialogSeen = false;
            this.graveyardTeleAt = 0L;
            return 2000;
        }
        if (this.graveyardTeleAt == 0L || System.currentTimeMillis() - this.graveyardTeleAt > 8000L) {
            ++this.graveyardMasterMisses;
            if (this.graveyardMasterMisses >= 12) {
                log.warn("[SlayerBot] graveyard master give-up after {} ::graveyard attempts", (Object)this.graveyardMasterMisses);
                if (this.config.watchdogStopOnGiveUp()) {
                    this.status = "Graveyard master never appeared x" + this.graveyardMasterMisses + " \u2014 stopping";
                    this.transitionTo(SlayerState.STOPPED);
                    return -1;
                }
                this.status = "Graveyard master never appeared x" + this.graveyardMasterMisses + " \u2014 resupplying";
                this.bankStep = BankStep.FOUNTAIN;
                this.transitionTo(SlayerState.BANKING);
                return 300;
            }
            this.status = "Teleporting to graveyard (::graveyard) \u2014 try " + this.graveyardMasterMisses;
            this.typeChatCommand(GRAVEYARD_TELEPORT_CMD);
            this.graveyardTeleAt = System.currentTimeMillis();
            return 5000;
        }
        this.status = "Waiting for graveyard teleport...";
        return 800;
    }

    private int loopGraveyardDialog(Player player) {
        long l = System.currentTimeMillis() - this.stateEnteredAt;
        if (Dialog.isOpen()) {
            this.graveyardDialogSeen = true;
            if (Dialog.isViewingOptions()) {
                this.status = "Confirming graveyard task (Yes)";
                if (!Dialog.chooseOption("Yes")) {
                    Dialog.chooseOption(1);
                }
                return 1200;
            }
            if (Dialog.canContinue()) {
                this.status = "Advancing master dialogue";
                Dialog.continueSpace();
                return 1000;
            }
            this.status = "Reading master dialogue...";
            return 600;
        }
        if (this.graveyardDialogSeen) {
            this.status = "Task obtained \u2014 checking gem";
            this.graveyardDialogSeen = false;
            this.gemCheckPending = false;
            this.state = SlayerState.CHECKING_GEM;
            return 600;
        }
        if (l > 6000L) {
            this.status = "No master dialogue \u2014 retrying assignment";
            this.graveyardTeleAt = 0L;
            this.state = SlayerState.GRAVEYARD_GET_ASSIGNMENT;
            return 600;
        }
        this.status = "Waiting for master dialogue...";
        return 600;
    }

    private int loopGraveyardTravel(Player player) {
        NPC nPC2;
        boolean bl;
        if (this.taskComplete || this.killsRemaining <= 0 || this.currentTask.isEmpty()) {
            this.graveyardTravelSince = 0L;
            this.state = SlayerState.GET_TASK;
            return 300;
        }
        WorldPoint worldPoint = GraveyardMonsterLocations.getLocation(this.currentTask);
        boolean bl2 = bl = SlayerBotPlugin.inGraveyardZone(player.getWorldLocation()) || worldPoint != null && player.getWorldLocation().distanceTo(worldPoint) <= 30;
        if (bl && (nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getHealthRatio() != 0 && nPC.getName() != null && this.taskMatches(nPC.getName()))) != null && player.getWorldLocation().distanceTo(nPC2.getWorldLocation()) <= 30) {
            this.status = "Found " + this.currentTask + " \u2014 engaging";
            this.graveyardTravelSince = 0L;
            this.wasInCombat = false;
            this.activeCombatTarget = null;
            this.state = SlayerState.IN_COMBAT;
            return 300;
        }
        if (!SlayerBotPlugin.inGraveyardZone(player.getWorldLocation()) && this.graveyardTravelTeleAttempts < 3) {
            if (this.graveyardTeleAt == 0L || System.currentTimeMillis() - this.graveyardTeleAt > 8000L) {
                this.status = "Teleporting to graveyard (::graveyard)";
                this.typeChatCommand(GRAVEYARD_TELEPORT_CMD);
                this.graveyardTeleAt = System.currentTimeMillis();
                ++this.graveyardTravelTeleAttempts;
                this.graveyardTravelSince = 0L;
                return 5000;
            }
            this.status = "Waiting for graveyard teleport...";
            return 800;
        }
        this.graveyardTeleAt = 0L;
        this.graveyardTravelTeleAttempts = 0;
        if (worldPoint == null) {
            this.status = "No graveyard spot for " + this.currentTask + " \u2014 searching here";
            this.graveyardTravelSince = 0L;
            this.state = SlayerState.IN_COMBAT;
            return 600;
        }
        int n = player.getWorldLocation().distanceTo(worldPoint);
        if (n > 8) {
            if (this.graveyardTravelSince == 0L) {
                this.graveyardTravelSince = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - this.graveyardTravelSince > 60000L) {
                this.status = "Can't reach " + this.currentTask + " spot \u2014 searching in place";
                this.graveyardTravelSince = 0L;
                this.state = SlayerState.IN_COMBAT;
                return 600;
            }
            this.status = "Walking to " + this.currentTask + " (" + n + " tiles)";
            this.walkTo(worldPoint);
            return 1500;
        }
        this.status = "At " + this.currentTask + " spot \u2014 looking for monster";
        this.graveyardTravelSince = 0L;
        this.state = SlayerState.IN_COMBAT;
        return 600;
    }

    private boolean hasTaskGp() {
        return Inventory.getCount(true, 995) >= this.config.graveyardTaskCost();
    }

    private static boolean inGraveyardZone(WorldPoint worldPoint) {
        return worldPoint != null && worldPoint.getPlane() == 0 && worldPoint.getX() >= 2470 && worldPoint.getX() <= 2690 && worldPoint.getY() >= 3420 && worldPoint.getY() <= 3660;
    }

    private void typeChatCommand(String string) {
        this.chatCommand(string);
    }

    private void teleportHome() {
        this.typeChatCommand(HOME_COMMAND);
    }

    private void handlePrayer(Player player, boolean bl) {
        NPC nPC;
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        if (this.config.quickPrayers()) {
            boolean bl2;
            boolean bl3 = bl2 = this.client.isPrayerActive(Prayer.PROTECT_FROM_MELEE) || this.client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES) || this.client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC);
            if (!bl2) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePauseWidgetPacket(0xA0000F, -1);
            }
            return;
        }
        Prayer prayer = this.config.defensivePrayer().prayer;
        Prayer prayer2 = this.config.offensivePrayer().prayer;
        PrayerFlickMode prayerFlickMode = this.config.flickMode();
        if (prayerFlickMode == PrayerFlickMode.ALWAYS_ON) {
            if (prayer != null && !this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            if (prayer2 != null && !this.client.isPrayerActive(prayer2)) {
                Prayers.toggle(prayer2);
            }
            return;
        }
        if (!bl) {
            if (prayer != null && this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            if (prayer2 != null && this.client.isPrayerActive(prayer2)) {
                Prayers.toggle(prayer2);
            }
            if (this.lastFlickTarget != null) {
                this.resetPrayerFlickState();
            }
            this.boostActivatedOnTick = -1;
            this.lastPlayerAttackTick = -1;
            this.offPrayerByAnimation = false;
            return;
        }
        Actor actor = player.getInteracting();
        NPC nPC2 = nPC = actor instanceof NPC ? (NPC)actor : null;
        if (nPC == null && this.activeCombatTarget != null && !this.activeCombatTarget.isDead() && this.taskMatches(this.activeCombatTarget.getName())) {
            nPC = this.activeCombatTarget;
        }
        if (nPC != this.lastFlickTarget) {
            this.lastNpcAnimation = -1;
            this.prayerActivatedOnTick = -1;
            this.lastNpcAttackTick = -1;
            this.lastFlickTarget = nPC;
            Arrays.fill(this.npcIntervalSamples, 0);
            this.npcSampleIdx = 0;
            this.npcSampleCount = 0;
            this.detectedAttackSpeed = 0;
            this.defPrayerByAnimation = false;
        }
        int n = this.client.getTickCount();
        switch (prayerFlickMode) {
            case DEFENSIVE_FLICK: {
                this.handleDefensiveFlick(nPC, prayer, n, 4, 0);
                if (prayer2 == null || this.client.isPrayerActive(prayer2)) break;
                Prayers.toggle(prayer2);
                break;
            }
            case OFFENSIVE_FLICK: {
                if (prayer != null && !this.client.isPrayerActive(prayer)) {
                    Prayers.toggle(prayer);
                }
                if (prayer2 == null) break;
                this.handleOffensiveFlick(player, prayer2, n, this.config.playerWeaponSpeedTicks());
                break;
            }
            case FULL_FLICK: {
                this.handleDefensiveFlick(nPC, prayer, n, 4, 0);
                if (prayer2 == null) break;
                this.handleOffensiveFlick(player, prayer2, n, this.config.playerWeaponSpeedTicks());
            }
        }
    }

    private void handleDefensiveFlick(NPC nPC, Prayer prayer, int n, int n2, int n3) {
        int n4;
        int n5;
        if (nPC == null || prayer == null) {
            return;
        }
        if (n3 == 0 && n2 == 0) {
            if (!this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            return;
        }
        int n6 = n5 = this.detectedAttackSpeed > 0 ? this.detectedAttackSpeed : n2;
        if (this.lastNpcAttackTick < 0 && n5 > 0) {
            this.lastNpcAttackTick = n;
        }
        if (n3 != 0) {
            n4 = nPC.getAnimation();
            if (n4 == n3 && n4 != this.lastNpcAnimation) {
                int n7;
                if (this.lastNpcAttackTick >= 0 && n > this.lastNpcAttackTick && (n7 = n - this.lastNpcAttackTick) >= 2 && n7 <= 12) {
                    this.npcIntervalSamples[this.npcSampleIdx % 5] = n7;
                    ++this.npcSampleIdx;
                    this.npcSampleCount = Math.min(this.npcSampleCount + 1, 5);
                    this.detectedAttackSpeed = this.computeDetectedNpcSpeed();
                }
                this.lastNpcAttackTick = n;
                if (!this.client.isPrayerActive(prayer)) {
                    Prayers.toggle(prayer);
                }
                this.prayerActivatedOnTick = n;
                this.defPrayerByAnimation = true;
            }
            this.lastNpcAnimation = n4;
        }
        if (n5 > 0 && this.lastNpcAttackTick >= 0 && this.prayerActivatedOnTick < 0 && (n4 = n - this.lastNpcAttackTick) >= n5 - 1) {
            if (!this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            this.prayerActivatedOnTick = n;
            this.defPrayerByAnimation = false;
            this.lastNpcAttackTick = Math.max(this.lastNpcAttackTick + n5, n);
        }
        int n8 = n4 = this.defPrayerByAnimation ? 0 : 1;
        if (this.prayerActivatedOnTick >= 0 && n > this.prayerActivatedOnTick + n4) {
            if (this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            this.prayerActivatedOnTick = -1;
            this.defPrayerByAnimation = false;
        }
    }

    private void handleOffensiveFlick(Player player, Prayer prayer, int n, int n2) {
        int n3;
        if (prayer == null) {
            return;
        }
        int n4 = player.getAnimation();
        boolean bl = n4 != -1 && n4 != this.lastPlayerAnimation;
        this.lastPlayerAnimation = n4;
        if (bl) {
            if (!this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            this.boostActivatedOnTick = n;
            this.lastPlayerAttackTick = n;
            this.offPrayerByAnimation = true;
        } else if (n2 > 0 && this.lastPlayerAttackTick >= 0 && this.boostActivatedOnTick < 0 && (n3 = n - this.lastPlayerAttackTick) >= n2 - 1 && n3 <= n2 + 1) {
            if (!this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            this.boostActivatedOnTick = n;
            this.offPrayerByAnimation = false;
            this.lastPlayerAttackTick += n2;
        }
        int n5 = n3 = this.offPrayerByAnimation ? 0 : 1;
        if (this.boostActivatedOnTick >= 0 && n > this.boostActivatedOnTick + n3) {
            if (this.client.isPrayerActive(prayer)) {
                Prayers.toggle(prayer);
            }
            this.boostActivatedOnTick = -1;
            this.offPrayerByAnimation = false;
        }
    }

    private int computeDetectedNpcSpeed() {
        if (this.npcSampleCount < 2) {
            return 0;
        }
        int[] nArray = Arrays.copyOf(this.npcIntervalSamples, this.npcSampleCount);
        Arrays.sort(nArray);
        return nArray[this.npcSampleCount / 2];
    }

    private TileObject findGlider() {
        TileObject tileObject2 = TileObjects.getNearest(GLIDER_OBJECT_NAME);
        if (tileObject2 != null) {
            return tileObject2;
        }
        return TileObjects.getNearest(tileObject -> tileObject.getWorldLocation().distanceTo(GLIDER) <= 3);
    }

    private void canvasClick(int n, int n2) {
        Mouse.click(n, n2, false);
    }

    private Widget findTeleportsPanelButton(String string) {
        Widget[][] widgetArray = this.client.getWidgets();
        if (widgetArray == null) {
            return null;
        }
        for (Widget[] widgetArray2 : widgetArray) {
            if (widgetArray2 == null) continue;
            for (Widget widget : widgetArray2) {
                if (widget == null || widget.isHidden()) continue;
                if (string.equalsIgnoreCase(widget.getText())) {
                    return widget;
                }
                Widget[] widgetArray3 = widget.getChildren();
                if (widgetArray3 == null) continue;
                for (Widget widget2 : widgetArray3) {
                    if (widget2 == null || widget2.isHidden() || !string.equalsIgnoreCase(widget2.getText())) continue;
                    return widget2;
                }
            }
        }
        return null;
    }

    private boolean isPresent(NPC nPC) {
        return nPC != null && NPCs.getNearest(nPC2 -> nPC2 == nPC) != null;
    }

    private boolean isBusy(NPC nPC) {
        if (nPC == null) {
            return false;
        }
        Long l = this.busyNpcs.get(nPC.getIndex());
        if (l == null) {
            return false;
        }
        if (System.currentTimeMillis() >= l) {
            this.busyNpcs.remove(nPC.getIndex());
            return false;
        }
        return true;
    }

    private boolean isIdleNpc(NPC nPC) {
        if (nPC == null) {
            return false;
        }
        if (nPC.getInteracting() != null) {
            return false;
        }
        if (nPC.getHealthRatio() >= 0) {
            return false;
        }
        Player player = this.client.getLocalPlayer();
        return Players.getNearest(player2 -> player2 != null && player2 != player && player2.getInteracting() == nPC) == null;
    }

    private boolean isLiveTarget(NPC nPC) {
        return nPC != null && !nPC.isDead() && nPC.getHealthRatio() != 0 && nPC.getName() != null && SlayerBotPlugin.isAttackable(nPC) && this.isPresent(nPC);
    }

    private static boolean isAttackable(NPC nPC) {
        if (nPC == null) {
            return false;
        }
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition == null) {
            nPCComposition = nPC.getComposition();
        }
        if (nPCComposition == null) {
            return true;
        }
        String[] stringArray = nPCComposition.getActions();
        if (stringArray == null) {
            return true;
        }
        boolean bl = false;
        for (String string : stringArray) {
            if (string == null) continue;
            if (string.toLowerCase().contains("spawn-instanced")) {
                return false;
            }
            if (!string.equalsIgnoreCase("Attack")) continue;
            bl = true;
        }
        return bl;
    }

    private String sampleNearbyNpcNames(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        for (NPC nPC2 : NPCs.getAll(nPC -> nPC != null && nPC.getName() != null && nPC.getWorldLocation().distanceTo(worldPoint) <= 30)) {
            linkedHashSet.add(nPC2.getName());
            if (linkedHashSet.size() < 8) continue;
            break;
        }
        return linkedHashSet.isEmpty() ? "(none)" : String.join((CharSequence)", ", linkedHashSet);
    }

    private int attackTask(Player player) {
        long l;
        WorldPoint worldPoint = player.getWorldLocation();
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getHealthRatio() != 0 && nPC.getName() != null && this.taskMatches(nPC.getName()) && SlayerBotPlugin.isAttackable(nPC) && !this.isBusy((NPC)nPC) && nPC.getWorldLocation().distanceTo(worldPoint) <= 30 && (!this.config.onlyAttackIdle() || this.isIdleNpc((NPC)nPC)));
        if (nPC2 != null) {
            this.noTargetSince = 0L;
            this.reNavCycles = 0;
            this.noTargetBankBails = 0;
            return this.attackOrApproach(player, nPC2, "Attacking");
        }
        if (this.noTargetSince == 0L) {
            this.noTargetSince = System.currentTimeMillis();
        }
        if ((l = System.currentTimeMillis() - this.noTargetSince) > 30000L) {
            ++this.reNavCycles;
            this.noTargetSince = 0L;
            log.warn("[SlayerBot] no '{}' after 30s (re-nav #{}) nearby={}", new Object[]{this.currentTask, this.reNavCycles, this.sampleNearbyNpcNames(player)});
            if (this.reNavCycles >= 3) {
                this.reNavCycles = 0;
                ++this.noTargetBankBails;
                if (this.noTargetBankBails >= 3) {
                    log.warn("[SlayerBot] no-target give-up: '{}' unkillable after {} bank-and-retry cycles (likely task<->NPC name mismatch)", (Object)this.currentTask, (Object)this.noTargetBankBails);
                    if (this.config.watchdogStopOnGiveUp()) {
                        this.status = "Can't find '" + this.currentTask + "' after " + this.noTargetBankBails + " resupply cycles \u2014 stopping";
                        this.transitionTo(SlayerState.STOPPED);
                        return -1;
                    }
                    this.noTargetBankBails = 0;
                }
                this.status = "No " + this.currentTask + " after re-navigations \u2014 banking/re-validating task";
                this.bankStep = BankStep.FOUNTAIN;
                this.transitionTo(SlayerState.BANKING);
                return 300;
            }
            this.status = "No " + this.currentTask + " found for 30s \u2014 re-navigating (" + this.reNavCycles + ")";
            if (this.config.graveyardMode()) {
                this.state = SlayerState.GRAVEYARD_TRAVEL;
            } else {
                this.enterTeleporting();
            }
            return 300;
        }
        this.status = "Searching for " + this.currentTask + " (" + l / 1000L + "s)...";
        return 1000;
    }

    private int attackOrApproach(Player player, NPC nPC, String string) {
        long l;
        if (nPC == null) {
            this.activeCombatTarget = null;
            this.engagedTarget = null;
            return 300;
        }
        this.activeCombatTarget = nPC;
        if (nPC != this.engagedTarget) {
            this.engagedTarget = nPC;
            this.engageStartedAt = System.currentTimeMillis();
        }
        if (player.getInteracting() == nPC && !nPC.isDead()) {
            this.engageStartedAt = System.currentTimeMillis();
        }
        if ((l = System.currentTimeMillis() - this.engageStartedAt) > 8000L) {
            this.status = "Can't engage " + nPC.getName() + " \u2014 re-acquiring";
            this.activeCombatTarget = null;
            this.engagedTarget = null;
            return 600;
        }
        if (Reachable.isInteractable((Locatable)nPC)) {
            boolean bl;
            boolean bl2 = bl = !(player.getInteracting() instanceof NPC) && !Movement.isWalking();
            if (bl && l > 2500L) {
                this.status = "Approaching " + nPC.getName();
                this.walkTo(nPC.getWorldLocation());
                return 1000;
            }
            this.status = string + " " + nPC.getName();
            this.lastAttemptedTarget = nPC;
            this.attack(nPC);
            return 600;
        }
        this.walkTo(nPC.getWorldLocation());
        this.status = "Approaching " + nPC.getName();
        return 1000;
    }

    private boolean lootNearby(Player player) {
        int n;
        WorldPoint worldPoint = player.getWorldLocation();
        List<String> list = this.splitCached(this.config.lootNames());
        List<String> list2 = this.splitCached(this.config.excludeLootNames());
        TileItem tileItem = TileItems.getNearest(arg_0 -> this.lambda$lootNearby$0(worldPoint, list2, list, n = this.config.lootMinValue(), arg_0));
        if (tileItem != null) {
            this.status = "Looting " + this.itemName(tileItem.getId());
            this.groundTake(tileItem);
            return true;
        }
        return false;
    }

    private long groundItemStackValue(TileItem tileItem) {
        if (tileItem == null) {
            return 0L;
        }
        long l = Math.max(1, tileItem.getQuantity());
        if (tileItem.getId() == 995 || tileItem.getId() == 995) {
            return l;
        }
        return (long)this.highAlchValue(tileItem.getId()) * l;
    }

    private boolean isInventoryFull() {
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return false;
        }
        for (Item item : itemContainer.getItems()) {
            if (item.getId() != -1) continue;
            return false;
        }
        return true;
    }

    private boolean hasLootNearby(Player player) {
        int n;
        WorldPoint worldPoint = player.getWorldLocation();
        List<String> list = this.splitCached(this.config.lootNames());
        List<String> list2 = this.splitCached(this.config.excludeLootNames());
        return TileItems.getNearest(arg_0 -> this.lambda$hasLootNearby$0(worldPoint, list2, list, n = this.config.lootMinValue(), arg_0)) != null;
    }

    private boolean shouldEat(Player player) {
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 && n2 * 100 / n < this.config.eatAtHpPercent();
    }

    private boolean shouldDrinkCombatPot() {
        if (!this.config.redrinkBelowBoost()) {
            return this.killsThisTask == 0 && this.client.getBoostedSkillLevel(Skill.ATTACK) <= this.client.getRealSkillLevel(Skill.ATTACK);
        }
        return this.client.getBoostedSkillLevel(Skill.ATTACK) <= this.client.getRealSkillLevel(Skill.ATTACK) && this.client.getBoostedSkillLevel(Skill.STRENGTH) <= this.client.getRealSkillLevel(Skill.STRENGTH);
    }

    private void eatFood(Item item) {
        MousePackets.queueClickPacket();
        ItemPackets.itemFirstOption(item);
    }

    private void drinkPotion(Item item) {
        if (item == null) {
            return;
        }
        MousePackets.queueClickPacket();
        ItemPackets.itemFirstOption(item);
    }

    private Item findFood() {
        for (String string : FOOD_NAMES) {
            Item item = this.findByName(string);
            if (item == null) continue;
            return item;
        }
        return null;
    }

    private Item findPrayerPotion() {
        for (String string : PRAYER_RESTORE_NAMES) {
            Item item = this.findByName(string);
            if (item == null) continue;
            return item;
        }
        return null;
    }

    private Item findBones() {
        List<String> list = this.splitCached(this.config.boneTypes());
        return Inventory.getFirst(item -> {
            if (item == null || item.getId() == -1) {
                return false;
            }
            String string = this.itemName(item.getId()).toLowerCase();
            if (string.isEmpty()) {
                return false;
            }
            if (list.isEmpty()) {
                return string.contains("bones") || string.contains("ashes");
            }
            return list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()));
        });
    }

    private Item findByName(String string) {
        return Inventory.getFirst(item -> item != null && item.getId() != -1 && !this.isNoted((Item)item) && this.itemName(item.getId()).toLowerCase().contains(string.toLowerCase()));
    }

    private boolean isNoted(Item item) {
        if (item == null || item.getId() == -1) {
            return false;
        }
        ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
        return itemComposition != null && itemComposition.getNote() > 0;
    }

    private int countFood() {
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return 0;
        }
        int n = 0;
        for (Item item : itemContainer.getItems()) {
            if (item.getId() == -1 || this.isNoted(item)) continue;
            String string = this.itemName(item.getId()).toLowerCase();
            if (!FOOD_NAMES.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) continue;
            ++n;
        }
        return n;
    }

    private boolean weaponHasSpecial() {
        Item item = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
        if (item == null || item.getId() == -1) {
            return false;
        }
        String string = this.itemName(item.getId()).toLowerCase();
        if (string.isEmpty() && item.getName() != null) {
            string = item.getName().toLowerCase();
        }
        for (String string2 : SPEC_WEAPONS) {
            if (!string.contains(string2)) continue;
            return true;
        }
        return false;
    }

    private boolean isBuryableBone(String string) {
        if (!this.config.buryBones()) {
            return false;
        }
        List<String> list = this.splitCached(this.config.boneTypes());
        if (list.isEmpty()) {
            return string.contains("bones") || string.contains("ashes");
        }
        return list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()));
    }

    private boolean pickUpBonesNearby(Player player) {
        List<String> list;
        WorldPoint worldPoint = player.getWorldLocation();
        TileItem tileItem = TileItems.getNearest(arg_0 -> this.lambda$pickUpBonesNearby$0(worldPoint, list = this.splitCached(this.config.excludeLootNames()), arg_0));
        if (tileItem != null) {
            this.status = "Looting " + this.itemName(tileItem.getId()) + " to bury";
            this.groundTake(tileItem);
            return true;
        }
        return false;
    }

    private String itemName(int n) {
        ItemComposition itemComposition = this.client.getItemDefinition(n);
        return itemComposition != null ? itemComposition.getName() : "";
    }

    private List<String> splitList(String string2) {
        return Arrays.stream(string2.split(",")).map(String::trim).filter(string -> !string.isEmpty()).collect(Collectors.toList());
    }

    private boolean taskMatches(String string) {
        if (string == null || this.currentTask.isEmpty()) {
            return false;
        }
        if (this.currentTask.toLowerCase().contains("spiritual creature")) {
            return string.toLowerCase().contains(this.spiritualCreatureTarget());
        }
        return SlayerNames.matches(this.currentTask, string);
    }

    private String spiritualCreatureTarget() {
        return this.client.getRealSkillLevel(Skill.SLAYER) >= 83 ? "spiritual mage" : "spiritual ranger";
    }

    private static String capitalise(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String string2 : string.split("\\s+")) {
            if (string2.isEmpty()) continue;
            stringBuilder.append(Character.toUpperCase(string2.charAt(0))).append(string2.substring(1).toLowerCase()).append(' ');
        }
        return stringBuilder.toString().trim();
    }

    private int highAlchValue(int n) {
        ItemComposition itemComposition = this.client.getItemDefinition(n);
        return itemComposition != null ? itemComposition.getHaPrice() : 0;
    }

    private boolean shouldAlchGroundItem(TileItem tileItem) {
        if (!this.config.enableHighAlch()) {
            return false;
        }
        if (tileItem == null) {
            return false;
        }
        String string = this.itemName(tileItem.getId()).toLowerCase();
        if (string.isEmpty()) {
            return false;
        }
        List<String> list = this.splitCached(this.config.excludeLootNames());
        if (!list.isEmpty() && list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        List<String> list2 = this.splitCached(this.config.lootNames());
        if (!list2.isEmpty() && list2.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        List<String> list3 = this.splitCached(this.config.alchExcludeNames());
        if (!list3.isEmpty() && list3.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        List<String> list4 = this.splitCached(this.config.alchItemNames());
        if (!list4.isEmpty() && list4.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return true;
        }
        int n = this.config.alchMinValue();
        if (n <= 0) {
            return false;
        }
        return this.highAlchValue(tileItem.getId()) >= n;
    }

    private boolean shouldAlchInventoryItem(Item item) {
        if (!this.config.enableHighAlch()) {
            return false;
        }
        if (item == null || item.getId() == -1) {
            return false;
        }
        String string = this.itemName(item.getId()).toLowerCase();
        if (string.isEmpty()) {
            return false;
        }
        List<String> list = this.splitCached(this.config.excludeLootNames());
        if (!list.isEmpty() && list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        List<String> list2 = this.splitCached(this.config.lootNames());
        if (!list2.isEmpty() && list2.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        List<String> list3 = this.splitCached(this.config.alchExcludeNames());
        if (!list3.isEmpty() && list3.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        List<String> list4 = this.splitCached(this.config.alchItemNames());
        if (!list4.isEmpty() && list4.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return true;
        }
        int n = this.config.alchMinValue();
        if (n <= 0) {
            return false;
        }
        return this.highAlchValue(item.getId()) >= n;
    }

    private boolean canCastHighAlch() {
        if (this.client.getVarbitValue(4070) != 0) {
            return false;
        }
        if (this.client.getBoostedSkillLevel(Skill.MAGIC) < 55) {
            return false;
        }
        if (!this.config.verifyAlchRunes()) {
            return true;
        }
        if (this.countInventoryItemById(561) < 1) {
            return false;
        }
        return this.countInventoryItemById(554) >= 5;
    }

    private int countInventoryItemById(int n) {
        return Inventory.getAll(item -> item != null && item.getId() == n).stream().mapToInt(Item::getQuantity).sum();
    }

    private boolean hasAlchLootNearby(Player player) {
        if (!this.config.enableHighAlch()) {
            return false;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        return TileItems.getNearest(tileItem -> tileItem != null && tileItem.getWorldLocation().distanceTo(worldPoint) <= this.config.lootRadius() && this.shouldAlchGroundItem((TileItem)tileItem)) != null;
    }

    private boolean alchLootNearby(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        TileItem tileItem2 = TileItems.getNearest(tileItem -> tileItem != null && tileItem.getWorldLocation().distanceTo(worldPoint) <= this.config.lootRadius() && this.shouldAlchGroundItem((TileItem)tileItem));
        if (tileItem2 != null) {
            this.status = "Picking up (alch) " + this.itemName(tileItem2.getId());
            this.groundTake(tileItem2);
            return true;
        }
        return false;
    }

    public String getStatus() {
        String string = this.pauseStatus;
        return string != null ? string : this.status;
    }

    public SlayerState getState() {
        return this.state;
    }

    public String getCurrentTask() {
        return this.currentTask;
    }

    public int getKillsRemaining() {
        return this.killsRemaining;
    }

    public int getKillsThisTask() {
        return this.killsThisTask;
    }

    public int getTotalKills() {
        return this.totalKills;
    }

    public int getTasksCompleted() {
        return this.tasksCompleted;
    }

    public long getAlchProfit() {
        return this.alchProfit;
    }

    public int getDetectedNpcSpeed() {
        return this.detectedAttackSpeed;
    }

    public int getNpcSampleCount() {
        return this.npcSampleCount;
    }

    public long getRuntimeMs() {
        return this.startTime == 0L ? 0L : System.currentTimeMillis() - this.startTime;
    }

    public int getWatchdogTrips() {
        return this.watchdogTrips;
    }

    public long getStalledSeconds() {
        return this.lastProgressAt == 0L ? 0L : (System.currentTimeMillis() - this.lastProgressAt) / 1000L;
    }

    public boolean isOnBreak() {
        return this.breakUntilMs > 0L;
    }

    public long getHumanizedActions() {
        return this.input == null ? 0L : this.input.humanizedCount();
    }

    public long getInputFallbacks() {
        return this.input == null ? 0L : this.input.fallbackCount();
    }

    private /* synthetic */ boolean lambda$pickUpBonesNearby$0(WorldPoint worldPoint, List list, TileItem tileItem) {
        if (tileItem == null || tileItem.getWorldLocation().distanceTo(worldPoint) > this.config.lootRadius()) {
            return false;
        }
        String string = this.itemName(tileItem.getId()).toLowerCase();
        if (string.isEmpty()) {
            return false;
        }
        if (!list.isEmpty() && list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        return this.isBuryableBone(string);
    }

    private /* synthetic */ boolean lambda$hasLootNearby$0(WorldPoint worldPoint, List list, List list2, int n, TileItem tileItem) {
        if (tileItem == null || tileItem.getWorldLocation().distanceTo(worldPoint) > this.config.lootRadius()) {
            return false;
        }
        String string = this.itemName(tileItem.getId()).toLowerCase();
        if (!list.isEmpty() && list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        if (list2.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return true;
        }
        return n > 0 && this.groundItemStackValue(tileItem) >= (long)n;
    }

    private /* synthetic */ boolean lambda$lootNearby$0(WorldPoint worldPoint, List list, List list2, int n, TileItem tileItem) {
        if (tileItem == null || tileItem.getWorldLocation().distanceTo(worldPoint) > this.config.lootRadius()) {
            return false;
        }
        String string = this.itemName(tileItem.getId()).toLowerCase();
        if (!list.isEmpty() && list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        if (list2.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return true;
        }
        return n > 0 && this.groundItemStackValue(tileItem) >= (long)n;
    }

    private static enum TeleportPhase {
        WALK_TO_GLIDER,
        CLICK_TRAVEL,
        CLICK_SEARCH,
        TYPE_QUERY,
        CLICK_SELECT,
        VERIFY_TELEPORT;

    }

    private static enum BankStep {
        FOUNTAIN,
        PRESET,
        VERIFY_PRESET,
        MANUAL_OPEN,
        MANUAL_WAIT_OPEN,
        MANUAL_LOAD,
        MANUAL_CONFIRM,
        MANUAL_CLOSE,
        DONE;

    }
}

