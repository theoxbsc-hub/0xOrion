/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
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
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.inferno;

import com.google.inject.Binder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.Actor;
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
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.inferno.InfernoCaste;
import net.runelite.client.plugins.svc.inferno.InfernoConfig;
import net.runelite.client.plugins.svc.inferno.InfernoConstants;
import net.runelite.client.plugins.svc.inferno.InfernoOverheadPrayer;
import net.runelite.client.plugins.svc.inferno.InfernoOverlay;
import net.runelite.client.plugins.svc.inferno.InfernoPhase;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Inferno</font></nobr></html>", description="RSPS Inferno: wave caste-prayer, Jad animation prayer switching, healers, supplies and best-effort Zuk", tags={"orion", "inferno", "jad", "tzhaar", "zuk", "boss", "combat", "bot"}, enabledByDefault=false)
public class InfernoPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private InfernoConfig config;
    private InfernoOverlay overlay;
    private static final Prayer[] OVERHEADS = new Prayer[]{Prayer.PROTECT_FROM_MAGIC, Prayer.PROTECT_FROM_MISSILES, Prayer.PROTECT_FROM_MELEE};
    private static final Pattern WAVE_PATTERN = Pattern.compile("wave:\\s*(\\d+)");
    private InfernoPhase phase = InfernoPhase.IDLE;
    private String status = "Idle";
    private int currentWave = 0;
    private int highestWave = 0;
    private int bossKills = 0;
    private long startTime = 0L;
    private boolean loggedInLastTick = false;
    private Prayer lastSentOverhead = null;
    private long lastSentOverheadMs = 0L;
    private long lastSentOffensiveMs = 0L;
    private Prayer currentOverhead = null;
    private int lastRangedAnimTick = -100;
    private int lastMagicAnimTick = -100;
    private int magicThreatUntil = -1;
    private Prayer lastBossOverhead = null;
    private boolean sawBossThisPhase = false;
    private int healersUp = 0;
    private InfernoCaste dominantCaste = InfernoCaste.UNKNOWN;
    private int brewDosesSinceRestore = 0;
    private static final Set<Integer> MAGE_ID_SET = InfernoPlugin.toSet(InfernoConstants.MAGE_IDS);
    private static final Set<Integer> RANGE_ID_SET = InfernoPlugin.toSet(InfernoConstants.RANGE_IDS);
    private static final Set<Integer> MELEE_ID_SET = InfernoPlugin.toSet(InfernoConstants.MELEE_IDS);
    private static final Set<Integer> KET_KEH_ID_SET = InfernoPlugin.toSet(InfernoConstants.KET_KEH_IDS);

    private static Set<Integer> toSet(int[] nArray) {
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (int n : nArray) {
            hashSet.add(n);
        }
        return hashSet;
    }

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(InfernoConfig.class).toInstance((Object)((InfernoConfig)configManager.getConfig(InfernoConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new InfernoOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
        this.startTime = System.currentTimeMillis();
        this.status = "Starting";
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        if (this.client.getGameState() == GameState.LOGGED_IN) {
            Prayers.disableAll();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGGED_IN) {
            this.loggedInLastTick = false;
        } else if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
            this.phase = InfernoPhase.IDLE;
        } else if (gameState == GameState.LOADING && this.isCombatPhase()) {
            this.resetState();
            this.status = "Left cave / died \u2014 reset";
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        Integer n;
        ChatMessageType chatMessageType = chatMessage.getType();
        if ((chatMessageType == ChatMessageType.GAMEMESSAGE || chatMessageType == ChatMessageType.SPAM || chatMessageType == ChatMessageType.PUBLICCHAT) && (n = InfernoPlugin.parseWave(InfernoPlugin.clean(chatMessage.getMessage()))) != null) {
            this.currentWave = n;
            if (n > this.highestWave) {
                this.highestWave = n;
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        if (this.client.getBoostedSkillLevel(Skill.HITPOINTS) == 0 && this.isCombatPhase()) {
            this.resetState();
            this.status = "Died \u2014 reset";
            return;
        }
        Player player = this.client.getLocalPlayer();
        if (player != null) {
            this.maintainPrayer(player);
        }
    }

    private static Integer parseWave(String string) {
        if (string == null) {
            return null;
        }
        Matcher matcher = WAVE_PATTERN.matcher(string);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    @Override
    protected int loop() {
        if (this.client.getGameState() == GameState.LOADING) {
            return 1000;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            return 1200;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (this.phase == InfernoPhase.DONE) {
            this.status = "Done \u2014 stop wave reached";
            return -1;
        }
        if (this.config.stopAfterWave() > 0 && this.highestWave >= this.config.stopAfterWave()) {
            this.phase = InfernoPhase.DONE;
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
            this.status = "Reached wave " + this.highestWave + " \u2014 stopping";
            return -1;
        }
        InfernoPhase infernoPhase = this.detectPhase(player);
        if (infernoPhase != this.phase) {
            this.onPhaseChange(this.phase, infernoPhase);
            this.phase = infernoPhase;
        }
        if (this.phase == InfernoPhase.IDLE) {
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
            this.status = "Waiting for arena";
            return 600;
        }
        int n = this.handleSupplies(player);
        if (n != 0) {
            return n;
        }
        int n2 = this.handlePositioning(player);
        if (n2 != 0) {
            return n2;
        }
        return this.handleCombat(player);
    }

    private InfernoPhase detectPhase(Player player) {
        NPC nPC2;
        int n;
        WorldPoint worldPoint = player.getWorldLocation();
        int n2 = n = worldPoint != null ? worldPoint.getRegionID() : -1;
        if (this.config.zukEnabled()) {
            // empty if block
        }
        if ((nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == 7700)) != null) {
            return InfernoPhase.JAD;
        }
        if (n == 10063 || n == 10064) {
            return InfernoPhase.WAVES;
        }
        return InfernoPhase.IDLE;
    }

    private void onPhaseChange(InfernoPhase infernoPhase, InfernoPhase infernoPhase2) {
        this.lastSentOverhead = null;
        this.lastRangedAnimTick = -100;
        this.lastMagicAnimTick = -100;
        this.magicThreatUntil = -1;
        if (infernoPhase2 == InfernoPhase.JAD) {
            this.lastBossOverhead = Prayer.PROTECT_FROM_MAGIC;
            this.sawBossThisPhase = false;
        } else if (infernoPhase2 == InfernoPhase.ZUK) {
            this.lastBossOverhead = InfernoPlugin.prayerOf(this.config.zukDefaultPrayer());
            this.sawBossThisPhase = false;
        } else {
            this.lastBossOverhead = null;
        }
    }

    private void maintainPrayer(Player player) {
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        if (!this.isCombatPhase()) {
            if (Prayers.anyActive()) {
                Prayers.disableAll();
            }
            return;
        }
        long l = System.currentTimeMillis();
        Prayer prayer = null;
        if (this.config.autoPrayerSwitch()) {
            switch (this.phase) {
                case WAVES: {
                    prayer = this.wavesOverhead(player);
                    break;
                }
                case JAD: {
                    prayer = this.reactiveBossOverhead(player, 7700, 7592, 7593, 7591, 448);
                    break;
                }
                case ZUK: {
                    prayer = this.reactiveBossOverhead(player, 0, 0, 0, 0, 0);
                    break;
                }
            }
        }
        this.currentOverhead = prayer;
        this.applyOverhead(prayer, l);
        this.applyOffensive(l);
    }

    private void applyOverhead(Prayer prayer, long l) {
        if (prayer == null || this.client.isPrayerActive(prayer)) {
            return;
        }
        if (!prayer.equals((Object)this.lastSentOverhead) || l - this.lastSentOverheadMs > 700L) {
            for (Prayer prayer2 : OVERHEADS) {
                if (prayer2 == prayer || !this.client.isPrayerActive(prayer2)) continue;
                Prayers.toggle(prayer2);
            }
            Prayers.toggle(prayer);
            this.lastSentOverhead = prayer;
            this.lastSentOverheadMs = l;
        }
    }

    private void applyOffensive(long l) {
        Prayer prayer;
        Prayer prayer2 = prayer = this.config.offensivePrayer() != null ? this.config.offensivePrayer().prayer : null;
        if (prayer != null && !this.client.isPrayerActive(prayer) && l - this.lastSentOffensiveMs > 700L) {
            Prayers.toggle(prayer);
            this.lastSentOffensiveMs = l;
        }
    }

    private Prayer wavesOverhead(Player player) {
        InfernoCaste infernoCaste = InfernoCaste.UNKNOWN;
        WorldPoint worldPoint = player.getWorldLocation();
        for (NPC nPC2 : NPCs.getAll(nPC -> nPC != null && !nPC.isDead())) {
            InfernoCaste infernoCaste2 = this.casteOf(nPC2);
            if (infernoCaste2 == InfernoCaste.UNKNOWN) continue;
            int n = this.chebyshevToNpc(worldPoint, nPC2);
            boolean bl = infernoCaste2 == InfernoCaste.MELEE ? n <= 1 : n <= this.config.attackRange() + 6;
            boolean bl2 = bl;
            if (!bl2) continue;
            if (infernoCaste2.priority < infernoCaste.priority) {
                infernoCaste = infernoCaste2;
            }
            if (infernoCaste != InfernoCaste.MAGIC) continue;
            break;
        }
        this.dominantCaste = infernoCaste;
        if (infernoCaste.protect != null) {
            return infernoCaste.protect;
        }
        return this.currentOverhead;
    }

    private Prayer reactiveBossOverhead(Player player, int n, int n2, int n3, int n4, int n5) {
        boolean bl;
        int n6;
        if (n <= 0) {
            return this.lastBossOverhead;
        }
        int n7 = this.client.getTickCount();
        for (NPC nPC2 : NPCs.getAll(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == n)) {
            n6 = nPC2.getAnimation();
            if (n6 == -1) continue;
            if (n2 > 0 && n6 == n2) {
                this.lastMagicAnimTick = n7;
                continue;
            }
            if ((n3 <= 0 || n6 != n3) && (n4 <= 0 || n6 != n4)) continue;
            this.lastRangedAnimTick = n7;
        }
        if (n5 > 0) {
            for (NPC nPC2 : this.client.getProjectiles()) {
                if (nPC2 == null || nPC2.getId() != n5 || nPC2.getRemainingCycles() <= 0) continue;
                this.magicThreatUntil = n7 + 3;
                break;
            }
        }
        int n8 = 3;
        boolean bl2 = n7 <= this.magicThreatUntil;
        n6 = n7 - this.lastRangedAnimTick <= n8 ? 1 : 0;
        boolean bl3 = bl = n7 - this.lastMagicAnimTick <= n8;
        if (bl2) {
            this.lastBossOverhead = Prayer.PROTECT_FROM_MAGIC;
        } else if (n6 != 0) {
            this.lastBossOverhead = Prayer.PROTECT_FROM_MISSILES;
        } else if (bl) {
            this.lastBossOverhead = Prayer.PROTECT_FROM_MAGIC;
        }
        return this.lastBossOverhead;
    }

    private InfernoCaste casteOf(NPC nPC) {
        String string;
        if (nPC == null) {
            return InfernoCaste.UNKNOWN;
        }
        int n = nPC.getId();
        if (KET_KEH_ID_SET.contains(n)) {
            return this.ketKehCasteResolved();
        }
        if (MAGE_ID_SET.contains(n)) {
            return InfernoCaste.MAGIC;
        }
        if (RANGE_ID_SET.contains(n)) {
            return InfernoCaste.RANGED;
        }
        if (MELEE_ID_SET.contains(n)) {
            return InfernoCaste.MELEE;
        }
        String string2 = string = nPC.getName() == null ? "" : nPC.getName().toLowerCase();
        if (string.isEmpty()) {
            return InfernoCaste.UNKNOWN;
        }
        if (string.contains("ket-keh")) {
            return this.ketKehCasteResolved();
        }
        if (string.contains("mej")) {
            return InfernoCaste.MAGIC;
        }
        if (string.contains("xil")) {
            return InfernoCaste.RANGED;
        }
        if (string.contains("ket")) {
            return InfernoCaste.MELEE;
        }
        return InfernoCaste.UNKNOWN;
    }

    private InfernoCaste ketKehCasteResolved() {
        if (!this.config.ketKehAsThreat()) {
            return InfernoCaste.UNKNOWN;
        }
        Prayer prayer = InfernoPlugin.prayerOf(this.config.ketKehCaste());
        if (prayer == Prayer.PROTECT_FROM_MAGIC) {
            return InfernoCaste.MAGIC;
        }
        if (prayer == Prayer.PROTECT_FROM_MISSILES) {
            return InfernoCaste.RANGED;
        }
        if (prayer == Prayer.PROTECT_FROM_MELEE) {
            return InfernoCaste.MELEE;
        }
        return InfernoCaste.UNKNOWN;
    }

    private int handleSupplies(Player player) {
        int n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        int n4 = this.client.getRealSkillLevel(Skill.PRAYER);
        int n5 = n2 > 0 ? n * 100 / n2 : 100;
        int n6 = n4 > 0 ? n3 * 100 / n4 : 100;
        boolean bl = Inventory.contains(this::isBrew);
        boolean bl2 = Inventory.contains(this::isFood);
        boolean bl3 = Inventory.contains(this::isRestore);
        if (this.config.stopWhenOutOfSupplies() && !bl && !bl2 && !bl3) {
            this.status = "Out of food/brews/restores \u2014 stopping";
            return -1;
        }
        if (n5 < this.config.emergencyHpPct()) {
            boolean bl4 = false;
            if (this.drinkBrew()) {
                ++this.brewDosesSinceRestore;
                bl4 = true;
            }
            if (this.eatFood()) {
                bl4 = true;
            }
            if (bl4) {
                this.status = "Emergency heal (" + n5 + "% HP)";
                return 600;
            }
        }
        if (n5 < this.config.healBelowHpPct()) {
            if (this.drinkBrew()) {
                ++this.brewDosesSinceRestore;
                this.status = "Sipping brew (" + n5 + "% HP)";
                return 600;
            }
            if (this.eatFood()) {
                this.status = "Eating food (" + n5 + "% HP)";
                return 600;
            }
        }
        if (bl3 && (n6 < this.config.restorePrayerPct() || this.brewDosesSinceRestore >= this.config.brewsPerRestore()) && this.drinkRestore()) {
            this.brewDosesSinceRestore = 0;
            this.status = "Sipping restore (" + n6 + "% pray)";
            return 600;
        }
        if (this.config.useStamina() && this.client.getEnergy() < this.config.staminaPct() * 100 && this.drinkByName("stamina")) {
            this.status = "Sipping stamina";
            return 600;
        }
        return 0;
    }

    private int handlePositioning(Player player) {
        if (this.config.standStill()) {
            return 0;
        }
        String string = this.config.safespot();
        if (string == null || string.trim().isEmpty()) {
            return 0;
        }
        WorldPoint worldPoint = this.parseSafespot(string);
        if (worldPoint != null && player.getWorldLocation().distanceTo(worldPoint) > 1) {
            this.status = "Walking to safespot";
            Movement.walkTo(worldPoint);
            return 300;
        }
        return 0;
    }

    private int handleCombat(Player player) {
        if (!this.config.attackEnabled()) {
            this.status = "Prayer-only assist";
            return 300;
        }
        if (this.phase == InfernoPhase.JAD) {
            return this.handleBossCombat(player, 7700, InfernoConstants.JAD_HEALER_IDS);
        }
        if (this.phase == InfernoPhase.ZUK) {
            int[] nArray = new int[]{};
            return this.handleBossCombat(player, 0, nArray);
        }
        NPC nPC = this.pickWaveTarget(player);
        if (nPC == null) {
            this.status = "Waves (wave " + this.currentWave + ") \u2014 no target";
            return 400;
        }
        this.status = "Killing " + InfernoPlugin.safeName(nPC) + " (wave " + this.currentWave + ")";
        this.attack(player, nPC);
        return 400;
    }

    private NPC pickWaveTarget(Player player) {
        NPC nPC2 = null;
        int n = Integer.MAX_VALUE;
        int n2 = Integer.MAX_VALUE;
        WorldPoint worldPoint = player.getWorldLocation();
        for (NPC nPC3 : NPCs.getAll(nPC -> nPC != null && !nPC.isDead())) {
            InfernoCaste infernoCaste = this.casteOf(nPC3);
            if (infernoCaste == InfernoCaste.UNKNOWN) continue;
            int n3 = this.chebyshevToNpc(worldPoint, nPC3);
            if (infernoCaste.priority >= n && (infernoCaste.priority != n || n3 >= n2)) continue;
            nPC2 = nPC3;
            n = infernoCaste.priority;
            n2 = n3;
        }
        return nPC2;
    }

    private int handleBossCombat(Player player, int n, int[] nArray) {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == n);
        if (nPC2 == null) {
            if (this.sawBossThisPhase) {
                this.sawBossThisPhase = false;
                ++this.bossKills;
                this.status = "Boss defeated! (" + this.bossKills + ")";
            } else {
                this.status = String.valueOf((Object)this.phase) + " \u2014 waiting for boss";
            }
            return 600;
        }
        this.sawBossThisPhase = true;
        int n2 = this.chebyshevToNpc(player.getWorldLocation(), nPC2);
        if (this.config.bossSafeDistance() > 0 && n2 < this.config.bossSafeDistance()) {
            this.status = "Backing out of " + InfernoPlugin.safeName(nPC2) + " melee (dist " + n2 + ")";
            this.stepAwayFrom(player, nPC2, this.config.bossSafeDistance() + 1);
            return 300;
        }
        if (this.config.killHealersFirst() && nArray.length > 0) {
            this.healersUp = this.countHealers(nArray);
            NPC nPC3 = this.pickHealingHealer(player, nPC2, nArray);
            if (nPC3 != null) {
                this.status = "Pulling healers off " + InfernoPlugin.safeName(nPC2) + " (" + this.healersUp + " up)";
                this.attack(player, nPC3);
                return 400;
            }
        } else {
            this.healersUp = 0;
        }
        this.status = "Attacking " + InfernoPlugin.safeName(nPC2);
        this.attack(player, nPC2);
        return 400;
    }

    private NPC pickHealingHealer(Player player, NPC nPC2, int[] nArray) {
        WorldPoint worldPoint = player.getWorldLocation();
        NPC nPC3 = null;
        int n = Integer.MAX_VALUE;
        for (NPC nPC4 : NPCs.getAll(nPC -> nPC != null && !nPC.isDead() && InfernoPlugin.idIn(nPC.getId(), nArray))) {
            int n2;
            if (!this.isHealingBoss(nPC4, nPC2) || (n2 = this.chebyshevToNpc(worldPoint, nPC4)) > this.config.attackRange() || n2 >= n) continue;
            n = n2;
            nPC3 = nPC4;
        }
        return nPC3;
    }

    private boolean isHealingBoss(NPC nPC, NPC nPC2) {
        Actor actor = nPC.getInteracting();
        if (actor instanceof Player) {
            return false;
        }
        if (actor instanceof NPC && ((NPC)actor).getId() == nPC2.getId()) {
            return true;
        }
        return this.chebyshevToNpc(nPC.getWorldLocation(), nPC2) <= 6;
    }

    private int countHealers(int[] nArray) {
        return NPCs.getAll(nPC -> nPC != null && !nPC.isDead() && InfernoPlugin.idIn(nPC.getId(), nArray)).size();
    }

    private static boolean idIn(int n, int[] nArray) {
        for (int n2 : nArray) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    private void attack(Player player, NPC nPC) {
        if (nPC == null || nPC.isDead()) {
            return;
        }
        Actor actor = player.getInteracting();
        if (actor instanceof NPC && ((NPC)actor).getIndex() == nPC.getIndex()) {
            return;
        }
        nPC.interact("Attack");
    }

    private int chebyshevToNpc(WorldPoint worldPoint, NPC nPC) {
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 1;
        int n2 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)));
        int n3 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)));
        return Math.max(n2, n3);
    }

    private void stepAwayFrom(Player player, NPC nPC, int n) {
        int n2;
        int n3;
        double d;
        WorldPoint worldPoint = player.getWorldLocation();
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n4 = nPCComposition != null ? nPCComposition.getSize() : 5;
        double d2 = (double)worldPoint2.getX() + (double)(n4 - 1) / 2.0;
        double d3 = (double)worldPoint2.getY() + (double)(n4 - 1) / 2.0;
        double d4 = (double)worldPoint.getX() - d2;
        double d5 = Math.sqrt(d4 * d4 + (d = (double)worldPoint.getY() - d3) * d);
        if (d5 < 0.5) {
            n3 = 0;
            n2 = n;
        } else {
            n3 = (int)Math.round(d4 / d5 * (double)n);
            n2 = (int)Math.round(d / d5 * (double)n);
        }
        WorldPoint worldPoint3 = new WorldPoint(worldPoint.getX() + n3, worldPoint.getY() + n2, worldPoint.getPlane());
        Movement.walkTo(worldPoint3);
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

    private boolean nameContains(Item item, String string) {
        if (item == null || item.getName() == null) {
            return false;
        }
        return item.getName().toLowerCase().contains(string);
    }

    private boolean isFood(Item item) {
        if (item == null) {
            return false;
        }
        ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
        if (itemComposition == null) {
            return false;
        }
        String[] stringArray = itemComposition.getInventoryActions();
        if (stringArray == null) {
            return false;
        }
        for (String string : stringArray) {
            if (string == null || !string.equalsIgnoreCase("Eat")) continue;
            return true;
        }
        return false;
    }

    private boolean isBrew(Item item) {
        return this.nameContains(item, "brew");
    }

    private boolean isRestore(Item item) {
        return this.nameContains(item, "restore") || this.nameContains(item, "prayer potion") || this.nameContains(item, "prayer pot");
    }

    private boolean eatFood() {
        Item item = Inventory.getFirst(this::isFood);
        if (item != null) {
            item.interact("Eat");
            return true;
        }
        return false;
    }

    private boolean drinkBrew() {
        Item item = Inventory.getFirst(this::isBrew);
        if (item != null) {
            item.interact("Drink");
            return true;
        }
        return false;
    }

    private boolean drinkRestore() {
        Item item = Inventory.getFirst(this::isRestore);
        if (item != null) {
            item.interact("Drink");
            return true;
        }
        return false;
    }

    private boolean drinkByName(String string) {
        Item item2 = Inventory.getFirst(item -> this.nameContains((Item)item, string));
        if (item2 != null) {
            item2.interact("Drink");
            return true;
        }
        return false;
    }

    private static Prayer prayerOf(InfernoOverheadPrayer infernoOverheadPrayer) {
        return infernoOverheadPrayer != null ? infernoOverheadPrayer.prayer : null;
    }

    private boolean isCombatPhase() {
        return this.phase == InfernoPhase.WAVES || this.phase == InfernoPhase.JAD || this.phase == InfernoPhase.ZUK;
    }

    private static String safeName(NPC nPC) {
        if (nPC == null) {
            return "?";
        }
        return nPC.getName() != null ? nPC.getName() : "#" + nPC.getId();
    }

    private static String clean(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("<[^>]*>", "").toLowerCase().trim();
    }

    private void resetState() {
        this.phase = InfernoPhase.IDLE;
        this.status = "Idle";
        this.currentWave = 0;
        this.lastSentOverhead = null;
        this.lastSentOverheadMs = 0L;
        this.lastSentOffensiveMs = 0L;
        this.currentOverhead = null;
        this.lastRangedAnimTick = -100;
        this.lastMagicAnimTick = -100;
        this.magicThreatUntil = -1;
        this.lastBossOverhead = null;
        this.sawBossThisPhase = false;
        this.healersUp = 0;
        this.dominantCaste = InfernoCaste.UNKNOWN;
        this.brewDosesSinceRestore = 0;
        this.loggedInLastTick = false;
    }

    InfernoPhase getPhase() {
        return this.phase;
    }

    String getStatus() {
        return this.status;
    }

    int getCurrentWave() {
        return this.currentWave;
    }

    int getHighestWave() {
        return this.highestWave;
    }

    int getBossKills() {
        return this.bossKills;
    }

    int getHealersUp() {
        return this.healersUp;
    }

    long getStartTime() {
        return this.startTime;
    }

    int getJadCount() {
        if (this.phase != InfernoPhase.JAD) {
            return 0;
        }
        return NPCs.getAll(nPC -> nPC != null && !nPC.isDead() && nPC.getId() == 7700).size();
    }

    InfernoCaste getDominantCaste() {
        return this.dominantCaste;
    }

    int getBrewDoses() {
        return this.brewDosesSinceRestore;
    }

    String getCurrentOverheadName() {
        return this.currentOverhead == null ? "None" : this.currentOverhead.name().replace("PROTECT_FROM_", "");
    }

    String getNextHitInfo() {
        int n;
        if (!this.isCombatPhase() || this.lastBossOverhead == null) {
            return "\u2014";
        }
        int n2 = this.client.getTickCount();
        if (n2 - this.lastRangedAnimTick <= (n = 3)) {
            return "Ranged";
        }
        if (n2 - this.lastMagicAnimTick <= n || n2 <= this.magicThreatUntil) {
            return "Magic";
        }
        return "hold " + (this.lastBossOverhead == Prayer.PROTECT_FROM_MISSILES ? "Rng" : "Mag");
    }

    private static /* synthetic */ boolean lambda$detectPhase$0(NPC nPC) {
        return nPC != null && !nPC.isDead() && nPC.getId() == 0;
    }
}

