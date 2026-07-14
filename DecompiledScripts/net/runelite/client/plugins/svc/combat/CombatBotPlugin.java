/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
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
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.game.ItemManager
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.combat;

import com.google.inject.Binder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
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
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.combat.AioCombatConfig;
import net.runelite.client.plugins.svc.combat.CombatBotOverlay;
import net.runelite.client.plugins.svc.combat.CombatPotion;
import net.runelite.client.plugins.svc.combat.PrayerFlickMode;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Combat Bot</font></nobr></html>", description="AIO combat bot: fights NPCs, eats food, prays, drinks potions, loots drops", tags={"orion", "combat", "aio", "bot", "automation", "prayer", "potion"}, enabledByDefault=false)
public class CombatBotPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private AioCombatConfig config;
    private CombatBotOverlay overlay;
    private int killCount = 0;
    private String status = "Idle";
    private NPC activeCombatTarget = null;
    private boolean loggedInLastTick = false;
    private long lastStyleSwapMs = 0L;
    private int styleSwapIdx = -1;
    private NoFoodState noFoodState = NoFoodState.IDLE;
    private WorldPoint noFoodOrigin = null;
    private int noFoodCastTick = -1;
    private int boostActivatedOnTick = -1;
    private int lastPlayerAnimation = -1;
    private int lastPlayerAttackTick = -1;
    private boolean offPrayerByAnimation = false;
    private final Map<Integer, Long> busyNpcs = new ConcurrentHashMap<Integer, Long>();
    private static final long BUSY_NPC_COOLDOWN_MS = 5000L;
    private volatile NPC lastAttemptedTarget = null;
    private int lastRunToggleTick = -100;
    private int lastSpecToggleTick = -100;
    private static final int GEMSTONE_CRAB_CAVE_ID = 57613;
    private static final String GEMSTONE_CRAB_NAME = "Gemstone Crab";
    private int lastCrawlTick = -100;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(AioCombatConfig.class).toInstance((Object)((AioCombatConfig)configManager.getConfig(AioCombatConfig.class)));
    }

    private AioCombatConfig cfg() {
        return this.config;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        switch (gameStateChanged.getGameState()) {
            case CONNECTION_LOST: {
                this.status = "Connection lost";
                this.activeCombatTarget = null;
                this.loggedInLastTick = false;
                this.noFoodState = NoFoodState.IDLE;
                this.busyNpcs.clear();
                this.lastAttemptedTarget = null;
                break;
            }
            case LOGIN_SCREEN: {
                this.status = "Logged out";
                this.activeCombatTarget = null;
                this.loggedInLastTick = false;
                this.noFoodState = NoFoodState.IDLE;
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
        ChatMessageType chatMessageType = chatMessage.getType();
        if (chatMessageType != ChatMessageType.GAMEMESSAGE && chatMessageType != ChatMessageType.ENGINE && chatMessageType != ChatMessageType.SPAM) {
            return;
        }
        String string = chatMessage.getMessage().replaceAll("<[^>]*>", "").toLowerCase();
        if (!string.contains("already in combat")) {
            return;
        }
        NPC nPC = this.lastAttemptedTarget;
        if (nPC != null) {
            this.busyNpcs.put(nPC.getIndex(), System.currentTimeMillis() + 5000L);
            if (this.activeCombatTarget == nPC) {
                this.activeCombatTarget = null;
            }
            this.status = "Target busy \u2014 switching";
        }
        this.lastAttemptedTarget = null;
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

    @Override
    protected void onStart() throws Exception {
        this.overlay = new CombatBotOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.killCount = 0;
        this.status = "Starting";
        this.activeCombatTarget = null;
        this.loggedInLastTick = false;
        this.lastStyleSwapMs = 0L;
        this.styleSwapIdx = -1;
        this.boostActivatedOnTick = -1;
        this.lastPlayerAnimation = -1;
        this.lastPlayerAttackTick = -1;
        this.offPrayerByAnimation = false;
        this.noFoodState = NoFoodState.IDLE;
        this.noFoodOrigin = null;
        this.noFoodCastTick = -1;
        this.busyNpcs.clear();
        this.lastAttemptedTarget = null;
        this.lastCrawlTick = -100;
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.status = "Idle";
    }

    @Override
    protected int loop() {
        WorldPoint worldPoint;
        Item item;
        Item item2;
        int n;
        Item item3;
        Object object;
        int n2;
        Item item4;
        boolean bl;
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.status = "Loading...";
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
        AioCombatConfig aioCombatConfig = this.cfg();
        if (aioCombatConfig.gemstoneCrabMode()) {
            return this.handleGemstoneCrab(player, aioCombatConfig);
        }
        if (this.activeCombatTarget != null && this.activeCombatTarget.isDead()) {
            ++this.killCount;
            this.activeCombatTarget = null;
        }
        boolean bl2 = bl = this.activeCombatTarget != null && !this.activeCombatTarget.isDead() || player.getInteracting() instanceof NPC || NPCs.getNearest(nPC -> nPC != null && nPC.getInteracting() == player) != null;
        if (aioCombatConfig.enablePrayer()) {
            this.handlePrayer(aioCombatConfig, player, bl);
        }
        if (this.noFoodState != NoFoodState.IDLE) {
            return this.handleNoFoodLogout(player);
        }
        if (aioCombatConfig.enableStyleSwap()) {
            long l = System.currentTimeMillis();
            if (this.lastStyleSwapMs == 0L) {
                this.lastStyleSwapMs = l;
            } else if (l - this.lastStyleSwapMs >= (long)aioCombatConfig.styleSwapInterval() * 60000L) {
                this.swapCombatStyle(aioCombatConfig);
                this.lastStyleSwapMs = l;
                return 600;
            }
        }
        if (aioCombatConfig.stopAfterKills() > 0 && this.killCount >= aioCombatConfig.stopAfterKills()) {
            this.status = "Done (" + this.killCount + " kills)";
            return -1;
        }
        if (aioCombatConfig.enableRun() && Movement.getRunEnergy() >= 3000 && !Movement.isRunEnabled() && this.client.getTickCount() - this.lastRunToggleTick >= 2) {
            Movement.toggleRun();
            this.lastRunToggleTick = this.client.getTickCount();
        }
        if (aioCombatConfig.eatFood() && this.shouldEat(player, aioCombatConfig)) {
            Item item5 = this.findFood(aioCombatConfig);
            if (item5 != null) {
                Item item6;
                this.status = "Eating " + this.itemName(item5.getId());
                item5.interact("Eat");
                if (!aioCombatConfig.eatComboFood().isEmpty() && (item6 = this.findByName(aioCombatConfig.eatComboFood())) != null) {
                    item6.interact("Eat");
                }
                return 1800;
            }
            if (aioCombatConfig.logoutOnNoFood()) {
                this.noFoodState = NoFoodState.OPENING_MAGIC_TAB;
                return this.handleNoFoodLogout(player);
            }
        }
        if (!aioCombatConfig.drinkAntipoisonName().isEmpty() && this.isPoisoned() && (item4 = this.findByName(aioCombatConfig.drinkAntipoisonName())) != null) {
            this.status = "Curing poison";
            item4.interact("Drink");
            return 600;
        }
        if (aioCombatConfig.enablePrayer() && !aioCombatConfig.prayerPotionName().isEmpty() && (n2 = this.client.getBoostedSkillLevel(Skill.PRAYER)) > 0 && n2 <= aioCombatConfig.drinkPrayerAt() && (object = this.findByName(aioCombatConfig.prayerPotionName())) != null) {
            this.status = "Drinking prayer restore";
            object.interact("Drink");
            return 600;
        }
        if (aioCombatConfig.combatPotion() != CombatPotion.NONE && this.shouldDrinkCombatPot(aioCombatConfig) && (item3 = this.findByName(aioCombatConfig.combatPotion().toString())) != null) {
            this.status = "Drinking " + String.valueOf((Object)aioCombatConfig.combatPotion());
            item3.interact("Drink");
            return 600;
        }
        if (aioCombatConfig.drinkStamina() && this.client.getEnergy() < aioCombatConfig.staminaThreshold() * 100) {
            Item item7 = this.findByName("Stamina potion");
            if (item7 == null) {
                item7 = this.findByName("Stamina mix");
            }
            if (item7 != null) {
                this.status = "Drinking stamina";
                item7.interact("Drink");
                return 600;
            }
        }
        if (aioCombatConfig.enableSpec() && bl && (n = this.client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT)) >= aioCombatConfig.specAt() * 10 && this.client.getTickCount() - this.lastSpecToggleTick >= 3) {
            this.status = "Using special attack";
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(38862884, -1);
            this.lastSpecToggleTick = this.client.getTickCount();
            return 600;
        }
        if (aioCombatConfig.buryBones() && (item2 = this.findBones(aioCombatConfig)) != null) {
            object = this.itemName(item2.getId()).toLowerCase();
            String string = ((String)object).contains("ashes") ? "Scatter" : "Bury";
            this.status = (string.equals("Scatter") ? "Scattering " : "Burying ") + this.itemName(item2.getId());
            item2.interact(string);
            return 600;
        }
        if (aioCombatConfig.eatToLoot() && aioCombatConfig.enableLooting() && (!aioCombatConfig.lootOnlyWhenSafe() || !bl) && Inventory.getFreeSlots() == 0 && this.hasNonBoneLoot(player, aioCombatConfig) && (item = this.findFood(aioCombatConfig)) != null) {
            this.status = "Eating to make room for loot";
            item.interact("Eat");
            return 1800;
        }
        if (aioCombatConfig.enableLooting() && (!aioCombatConfig.lootOnlyWhenSafe() || !bl) && this.lootNearby(player, aioCombatConfig)) {
            return 600;
        }
        if (!aioCombatConfig.alchItems().isEmpty() && !bl && this.alchNextItem(aioCombatConfig)) {
            return 1200;
        }
        if (!aioCombatConfig.safespot().isEmpty() && !bl && (worldPoint = this.parseSafespot(aioCombatConfig.safespot())) != null && player.getWorldLocation().distanceTo(worldPoint) > 1) {
            this.status = "Returning to safespot";
            player.interact("Walk here");
            return 600;
        }
        NPC nPC2 = player.getInteracting() instanceof NPC ? (NPC)player.getInteracting() : null;
        object = NPCs.getNearest(nPC -> nPC != null && nPC.getInteracting() == player && CombatBotPlugin.isAttackable(nPC));
        if (nPC2 != null && !nPC2.isDead() && !this.isBusy(nPC2) && CombatBotPlugin.isAttackable(nPC2)) {
            this.activeCombatTarget = nPC2;
            this.status = "In combat";
            return 600;
        }
        if (nPC2 == null && object != null && !object.isDead()) {
            this.activeCombatTarget = object;
            this.lastAttemptedTarget = null;
            this.status = "Re-engaging " + object.getName();
            object.interact("Attack");
            return 600;
        }
        if (this.activeCombatTarget != null && !this.activeCombatTarget.isDead() && !this.isBusy(this.activeCombatTarget) && CombatBotPlugin.isAttackable(this.activeCombatTarget)) {
            this.status = "Re-engaging " + this.activeCombatTarget.getName();
            this.lastAttemptedTarget = this.activeCombatTarget;
            this.activeCombatTarget.interact("Attack");
            return 600;
        }
        this.activeCombatTarget = null;
        return this.attackNearest(player, aioCombatConfig);
    }

    private void swapCombatStyle(AioCombatConfig aioCombatConfig) {
        List<Integer> list = this.parseIntList(aioCombatConfig.styleSwapCycle());
        if (list.isEmpty()) {
            return;
        }
        this.styleSwapIdx = (this.styleSwapIdx + 1) % list.size();
        int n = Math.max(0, Math.min(3, list.get(this.styleSwapIdx)));
        int n2 = 0x2510000 | (n + 1) * 4;
        this.status = "Swapping style -> " + n;
        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePauseWidgetPacket(n2, -1);
    }

    private List<Integer> parseIntList(String string2) {
        return Arrays.stream(string2.split(",")).map(String::trim).filter(string -> !string.isEmpty()).map(string -> {
            try {
                return Integer.parseInt(string);
            }
            catch (Exception exception) {
                return -1;
            }
        }).filter(n -> n >= 0 && n <= 3).collect(Collectors.toList());
    }

    public int getSecondsUntilStyleSwap(AioCombatConfig aioCombatConfig) {
        if (!aioCombatConfig.enableStyleSwap() || this.lastStyleSwapMs == 0L) {
            return -1;
        }
        long l = (long)aioCombatConfig.styleSwapInterval() * 60000L - (System.currentTimeMillis() - this.lastStyleSwapMs);
        return (int)Math.max(0L, l / 1000L);
    }

    private int handleNoFoodLogout(Player player) {
        switch (this.noFoodState.ordinal()) {
            case 1: {
                this.status = "No food! Opening magic tab...";
                this.noFoodOrigin = player.getWorldLocation();
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePauseWidgetPacket(35913765, -1);
                this.noFoodState = NoFoodState.CASTING_TELE;
                return 600;
            }
            case 2: {
                this.status = "No food! Casting home teleport...";
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePauseWidgetPacket(14286877, -1);
                this.noFoodCastTick = this.client.getTickCount();
                this.noFoodState = NoFoodState.WAITING_TELE;
                return 1000;
            }
            case 3: {
                WorldPoint worldPoint = player.getWorldLocation();
                if (worldPoint != null && this.noFoodOrigin != null && !worldPoint.equals((Object)this.noFoodOrigin)) {
                    this.status = "Teleported! Logging out...";
                    Game.logout();
                    this.noFoodState = NoFoodState.IDLE;
                    return 2000;
                }
                if (this.client.getTickCount() - this.noFoodCastTick > 25) {
                    this.status = "Home tele timeout, retrying...";
                    this.noFoodState = NoFoodState.OPENING_MAGIC_TAB;
                    return 600;
                }
                this.status = "Waiting for home teleport... (" + (25 - (this.client.getTickCount() - this.noFoodCastTick)) + "t)";
                return 600;
            }
        }
        this.noFoodState = NoFoodState.IDLE;
        return 600;
    }

    private int handleGemstoneCrab(Player player, AioCombatConfig aioCombatConfig) {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getName() != null && nPC.getName().equalsIgnoreCase(GEMSTONE_CRAB_NAME) && CombatBotPlugin.isAttackable(nPC));
        if (nPC2 != null) {
            if (player.getInteracting() == nPC2) {
                this.status = "Fighting Gemstone Crab";
            } else {
                this.status = "Attacking Gemstone Crab";
                nPC2.interact("Attack");
            }
            this.activeCombatTarget = nPC2;
            return 1200;
        }
        if (this.activeCombatTarget != null) {
            ++this.killCount;
            this.activeCombatTarget = null;
        }
        if (aioCombatConfig.stopAfterKills() > 0 && this.killCount >= aioCombatConfig.stopAfterKills()) {
            this.status = "Done (" + this.killCount + " crabs)";
            return -1;
        }
        if (this.client.getTickCount() - this.lastCrawlTick < 5) {
            this.status = "Waiting for crab to appear...";
            return 800;
        }
        TileObject tileObject = TileObjects.getNearest(57613);
        if (tileObject != null) {
            this.status = "Crawling through to next crab...";
            tileObject.interact("Crawl-through");
            this.lastCrawlTick = this.client.getTickCount();
            WorldPoint worldPoint = player.getWorldLocation();
            Time.sleepUntil(() -> {
                WorldPoint worldPoint2 = player.getWorldLocation();
                return worldPoint2 != null && !worldPoint2.equals((Object)worldPoint);
            }, 5000);
            return 1200;
        }
        this.status = "No Gemstone Crab or cave found...";
        return 1500;
    }

    private boolean shouldEat(Player player, AioCombatConfig aioCombatConfig) {
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 && n2 * 100 / n < aioCombatConfig.eatAtHpPercent();
    }

    private boolean isPoisoned() {
        return this.client.getVar(VarPlayer.IS_POISONED) > 0;
    }

    private void handlePrayer(AioCombatConfig aioCombatConfig, Player player, boolean bl) {
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        if (aioCombatConfig.quickPrayers()) {
            if (!Prayers.isQuickPrayerEnabled()) {
                Prayers.toggleQuickPrayer(true);
            }
            return;
        }
        Prayer prayer = aioCombatConfig.defensivePrayer().prayer;
        Prayer prayer2 = aioCombatConfig.offensivePrayer().prayer;
        PrayerFlickMode prayerFlickMode = aioCombatConfig.flickMode();
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
            this.boostActivatedOnTick = -1;
            this.lastPlayerAttackTick = -1;
            this.offPrayerByAnimation = false;
            return;
        }
        int n = this.client.getTickCount();
        switch (prayerFlickMode) {
            case DEFENSIVE_FLICK: {
                this.handleDefensiveFlick(prayer);
                if (prayer2 == null || this.client.isPrayerActive(prayer2)) break;
                Prayers.toggle(prayer2);
                break;
            }
            case OFFENSIVE_FLICK: {
                if (prayer != null && !this.client.isPrayerActive(prayer)) {
                    Prayers.toggle(prayer);
                }
                if (aioCombatConfig.offensiveFlickEnabled() && prayer2 != null) {
                    this.handleOffensiveFlick(player, prayer2, n, aioCombatConfig.playerWeaponSpeedTicks());
                    break;
                }
                if (prayer2 == null || this.client.isPrayerActive(prayer2)) break;
                Prayers.toggle(prayer2);
                break;
            }
            case FULL_FLICK: {
                this.handleDefensiveFlick(prayer);
                if (aioCombatConfig.offensiveFlickEnabled() && prayer2 != null) {
                    this.handleOffensiveFlick(player, prayer2, n, aioCombatConfig.playerWeaponSpeedTicks());
                    break;
                }
                if (prayer2 == null || this.client.isPrayerActive(prayer2)) break;
                Prayers.toggle(prayer2);
            }
        }
    }

    private void handleDefensiveFlick(Prayer prayer) {
        if (prayer == null) {
            return;
        }
        if (!this.client.isPrayerActive(prayer)) {
            Prayers.toggle(prayer);
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

    private boolean shouldDrinkCombatPot(AioCombatConfig aioCombatConfig) {
        if (!aioCombatConfig.redrinkBelowBoost()) {
            return this.killCount == 0 && this.client.getBoostedSkillLevel(Skill.ATTACK) <= this.client.getRealSkillLevel(Skill.ATTACK);
        }
        return this.client.getBoostedSkillLevel(Skill.ATTACK) <= this.client.getRealSkillLevel(Skill.ATTACK) && this.client.getBoostedSkillLevel(Skill.STRENGTH) <= this.client.getRealSkillLevel(Skill.STRENGTH);
    }

    private boolean hasNonBoneLoot(Player player, AioCombatConfig aioCombatConfig) {
        int n;
        WorldPoint worldPoint = player.getWorldLocation();
        List<String> list = this.splitList(aioCombatConfig.lootNames());
        List<String> list2 = this.splitList(aioCombatConfig.excludeLootNames());
        return TileItems.getNearest(arg_0 -> this.lambda$hasNonBoneLoot$0(worldPoint, aioCombatConfig, list2, list, n = aioCombatConfig.lootMinValue(), arg_0)) != null;
    }

    private boolean lootNearby(Player player, AioCombatConfig aioCombatConfig) {
        int n;
        List list;
        WorldPoint worldPoint = player.getWorldLocation();
        List<String> list2 = this.splitList(aioCombatConfig.lootNames());
        List<String> list3 = this.splitList(aioCombatConfig.excludeLootNames());
        TileItem tileItem = TileItems.getNearest(arg_0 -> this.lambda$lootNearby$0(worldPoint, aioCombatConfig, list3, list2, list = aioCombatConfig.buryBones() ? this.splitList(aioCombatConfig.boneTypes()) : Collections.emptyList(), n = aioCombatConfig.lootMinValue(), arg_0));
        if (tileItem != null) {
            this.status = "Looting " + this.itemName(tileItem.getId());
            tileItem.interact("Take");
            return true;
        }
        return false;
    }

    private int getItemHaValue(TileItem tileItem) {
        if (tileItem.getId() == 995) {
            return tileItem.getQuantity();
        }
        ItemComposition itemComposition = this.client.getItemDefinition(tileItem.getId());
        return itemComposition != null ? itemComposition.getHaPrice() : 0;
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

    private int attackNearest(Player player, AioCombatConfig aioCombatConfig) {
        WorldPoint worldPoint = player.getWorldLocation();
        List<String> list = this.splitList(aioCombatConfig.npcNames());
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getName() != null && list.stream().anyMatch(string -> string.equalsIgnoreCase(nPC.getName())) && nPC.getWorldLocation().distanceTo(worldPoint) <= aioCombatConfig.searchRadius() && !this.isBusy((NPC)nPC) && CombatBotPlugin.isAttackable(nPC) && (!aioCombatConfig.onlyIdle() || nPC.getInteracting() == null));
        if (nPC2 != null) {
            this.status = "Attacking " + nPC2.getName();
            this.activeCombatTarget = nPC2;
            this.lastAttemptedTarget = nPC2;
            nPC2.interact("Attack");
            return 600;
        }
        this.status = "Searching for NPC...";
        return 1000;
    }

    private boolean alchNextItem(AioCombatConfig aioCombatConfig) {
        List<String> list = this.splitList(aioCombatConfig.alchItems());
        Item item = this.findByName("Nature rune");
        if (item == null) {
            return false;
        }
        for (String string : list) {
            Item item2 = this.findByName(string);
            if (item2 == null) continue;
            this.status = "Alching " + string;
            Magic.cast((Spell)SpellBook.Standard.HIGH_LEVEL_ALCHEMY, item2);
            return true;
        }
        return false;
    }

    private Item findFood(AioCombatConfig aioCombatConfig) {
        for (String string : this.splitList(aioCombatConfig.foodNames())) {
            Item item = this.findByName(string);
            if (item == null) continue;
            return item;
        }
        return null;
    }

    private Item findBones(AioCombatConfig aioCombatConfig) {
        List<String> list = this.splitList(aioCombatConfig.boneTypes());
        if (list.isEmpty()) {
            return Inventory.getFirst(item -> {
                String string = item.getName().toLowerCase();
                return string.contains("bones") || string.contains("ashes");
            });
        }
        return Inventory.getFirst(item -> {
            String string = item.getName().toLowerCase();
            return list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()));
        });
    }

    private Item findByName(String string) {
        return Inventory.getFirst(item -> item.getName().toLowerCase().contains(string.toLowerCase()));
    }

    private String itemName(int n) {
        ItemComposition itemComposition = this.client.getItemDefinition(n);
        return itemComposition != null ? itemComposition.getName() : "";
    }

    private List<String> splitList(String string2) {
        return Arrays.stream(string2.split(",")).map(String::trim).filter(string -> !string.isEmpty()).collect(Collectors.toList());
    }

    private WorldPoint parseSafespot(String string) {
        try {
            String[] stringArray = string.split(",");
            return new WorldPoint(Integer.parseInt(stringArray[0].trim()), Integer.parseInt(stringArray[1].trim()), this.client.getPlane());
        }
        catch (Exception exception) {
            return null;
        }
    }

    public String getStatus() {
        return this.status;
    }

    public int getKillCount() {
        return this.killCount;
    }

    private /* synthetic */ boolean lambda$lootNearby$0(WorldPoint worldPoint, AioCombatConfig aioCombatConfig, List list, List list2, List list3, int n, TileItem tileItem) {
        if (tileItem == null || tileItem.getWorldLocation().distanceTo(worldPoint) > aioCombatConfig.lootRadius()) {
            return false;
        }
        String string = this.itemName(tileItem.getId()).toLowerCase();
        if (!list.isEmpty() && list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        if (list2.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return true;
        }
        if (!list3.isEmpty() && list3.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return true;
        }
        return n > 0 && this.getItemHaValue(tileItem) >= n;
    }

    private /* synthetic */ boolean lambda$hasNonBoneLoot$0(WorldPoint worldPoint, AioCombatConfig aioCombatConfig, List list, List list2, int n, TileItem tileItem) {
        if (tileItem == null || tileItem.getWorldLocation().distanceTo(worldPoint) > aioCombatConfig.lootRadius()) {
            return false;
        }
        String string = this.itemName(tileItem.getId()).toLowerCase();
        if (!list.isEmpty() && list.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return false;
        }
        if (list2.stream().anyMatch(string2 -> string.contains(string2.toLowerCase()))) {
            return true;
        }
        return n > 0 && this.getItemHaValue(tileItem) >= n;
    }

    private static enum NoFoodState {
        IDLE,
        OPENING_MAGIC_TAB,
        CASTING_TELE,
        WAITING_TELE;

    }
}

