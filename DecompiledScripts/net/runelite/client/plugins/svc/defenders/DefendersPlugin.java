/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.Locatable
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileItem
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.ChatMessage
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.defenders;

import com.google.inject.Binder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Locatable;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.defenders.DefendersConfig;
import net.runelite.client.plugins.svc.defenders.DefendersOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.WidgetPackets;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Auto Defenders</font></nobr></html>", description="Kills Cyclopes and loots every Defender; can stop after looting a Dragon defender", tags={"orion", "defenders", "cyclops", "warriors guild", "combat", "bot"}, enabledByDefault=false)
public class DefendersPlugin
extends OrionLoopedPlugin {
    private static final int CYCLOPS_ID = 11231;
    private static final String DEFENDER_KEYWORD = "defender";
    private static final String DRAGON_DEFENDER = "dragon defender";
    private static final int SEARCH_RADIUS = 20;
    private static final int LOOT_RADIUS = 12;
    private static final int DEATH_ANIMATION = 836;
    private static final List<String> FOOD_NAMES = Arrays.asList("Shark", "Manta ray", "Anglerfish", "Dark crab", "Sea turtle", "Monkfish", "Swordfish", "Cooked karambwan", "Lobster", "Tuna", "Bass", "Salmon", "Trout", "Cake", "Pie", "Bread");
    private static final Pattern BUSY_NPC_PATTERN = Pattern.compile("already in combat|creature belongs to", 2);
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DefendersConfig config;
    private DefendersOverlay overlay;
    private long startTime = 0L;
    private String status = "Starting";
    private int kills = 0;
    private int defendersLooted = 0;
    private int bestDefenderRank = 0;
    private String bestDefenderName = "\u2014";
    private boolean dragonLooted = false;
    private boolean finished = false;
    private boolean fighting = false;
    private boolean loggedInLastTick = false;
    private int prevDefenderCount = 0;
    private boolean hadDragonAtStart = false;
    private NPC activeCombatTarget = null;
    private long outOfCombatSince = 0L;
    private NPC engagedTarget = null;
    private long engageStartedAt = 0L;
    private final Map<Integer, Long> busyNpcs = new ConcurrentHashMap<Integer, Long>();
    private static final long BUSY_NPC_COOLDOWN_MS = 8000L;
    private volatile NPC lastAttemptedTarget = null;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(DefendersConfig.class).toInstance((Object)((DefendersConfig)configManager.getConfig(DefendersConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.startTime = System.currentTimeMillis();
        this.overlay = new DefendersOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
    }

    @Override
    protected void onStop() throws Exception {
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
            this.overlay = null;
        }
        this.status = "Idle";
    }

    private void resetState() {
        this.status = "Starting";
        this.kills = 0;
        this.defendersLooted = 0;
        this.bestDefenderRank = 0;
        this.bestDefenderName = "\u2014";
        this.dragonLooted = false;
        this.finished = false;
        this.fighting = false;
        this.loggedInLastTick = false;
        this.activeCombatTarget = null;
        this.outOfCombatSince = 0L;
        this.engagedTarget = null;
        this.engageStartedAt = 0L;
        this.busyNpcs.clear();
        this.lastAttemptedTarget = null;
        this.prevDefenderCount = this.countInventoryDefenders();
        this.hadDragonAtStart = this.inventoryHasDragon();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        switch (gameStateChanged.getGameState()) {
            case CONNECTION_LOST: 
            case LOGIN_SCREEN: {
                this.status = "Logged out";
                this.activeCombatTarget = null;
                this.engagedTarget = null;
                this.fighting = false;
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
        String string = chatMessage.getMessage().replaceAll("<[^>]*>", "").toLowerCase();
        ChatMessageType chatMessageType = chatMessage.getType();
        if ((chatMessageType == ChatMessageType.GAMEMESSAGE || chatMessageType == ChatMessageType.ENGINE || chatMessageType == ChatMessageType.SPAM) && BUSY_NPC_PATTERN.matcher(string).find()) {
            NPC nPC = this.lastAttemptedTarget;
            if (nPC != null) {
                this.busyNpcs.put(nPC.getIndex(), System.currentTimeMillis() + 8000L);
                if (this.activeCombatTarget == nPC) {
                    this.activeCombatTarget = null;
                }
                if (this.engagedTarget == nPC) {
                    this.engagedTarget = null;
                }
                this.status = "Cyclops busy/owned \u2014 switching target";
            }
            this.lastAttemptedTarget = null;
        }
    }

    @Override
    protected int loop() {
        Item item;
        if (this.finished) {
            return -1;
        }
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.status = "Loading...";
            this.fighting = false;
            return 800;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1200;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            this.status = "Resuming...";
            return 800;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (player.getAnimation() == 836) {
            this.status = "Died \u2014 waiting to respawn";
            this.activeCombatTarget = null;
            this.engagedTarget = null;
            this.fighting = false;
            return 3000;
        }
        if (this.activeCombatTarget != null) {
            if (this.activeCombatTarget.isDead() || this.activeCombatTarget.getHealthRatio() == 0) {
                ++this.kills;
                this.activeCombatTarget = null;
            } else if (!this.isPresent(this.activeCombatTarget)) {
                this.activeCombatTarget = null;
            }
        }
        this.updateDefenderStats();
        if (this.config.eatFood() && this.shouldEat() && (item = this.findFood()) != null) {
            this.status = "Eating " + this.itemName(item.getId());
            this.eatFood(item);
            return 1800;
        }
        if (this.config.keepRunEnabled() && this.client.getEnergy() >= 3000 && this.client.getVarbitValue(173) == 0) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(10485782, -1);
        }
        if (this.lootDefenders(player)) {
            return 600;
        }
        if (this.config.stopAfterDragon() && this.dragonLooted) {
            this.status = "Dragon defender obtained \u2014 stopping";
            this.finished = true;
            this.fighting = false;
            return -1;
        }
        return this.doCombat(player);
    }

    private int doCombat(Player player) {
        boolean bl;
        NPC nPC2 = player.getInteracting() instanceof NPC ? (NPC)player.getInteracting() : null;
        boolean bl2 = bl = this.isLiveTarget(nPC2) && nPC2.getId() == 11231 || NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 11231 && nPC.getInteracting() == player && this.isLiveTarget((NPC)nPC)) != null;
        if (bl) {
            this.outOfCombatSince = 0L;
        } else if (this.outOfCombatSince == 0L) {
            this.outOfCombatSince = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - this.outOfCombatSince > 2000L) {
            this.activeCombatTarget = null;
            this.outOfCombatSince = 0L;
        }
        NPC nPC3 = player.getInteracting() instanceof NPC ? (NPC)player.getInteracting() : null;
        NPC nPC4 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 11231 && nPC.getInteracting() == player && this.isLiveTarget((NPC)nPC));
        boolean bl3 = this.fighting = this.isLiveTarget(nPC3) && nPC3.getId() == 11231 || this.isLiveTarget(this.activeCombatTarget) || nPC4 != null;
        if (this.isLiveTarget(nPC3) && nPC3.getId() == 11231) {
            this.activeCombatTarget = nPC3;
            this.status = "Fighting Cyclops";
            return 300;
        }
        if (nPC4 != null) {
            return this.attackOrApproach(player, nPC4, "Re-engaging");
        }
        if (this.isLiveTarget(this.activeCombatTarget) && this.activeCombatTarget.getId() == 11231 && !this.isBusy(this.activeCombatTarget)) {
            return this.attackOrApproach(player, this.activeCombatTarget, "Re-engaging");
        }
        this.activeCombatTarget = null;
        this.engagedTarget = null;
        return this.attackCyclops(player);
    }

    private int attackCyclops(Player player) {
        WorldPoint worldPoint = player.getWorldLocation();
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 11231 && !nPC.isDead() && nPC.getHealthRatio() != 0 && nPC.getName() != null && DefendersPlugin.isAttackable(nPC) && !this.isBusy((NPC)nPC) && nPC.getWorldLocation().distanceTo(worldPoint) <= 20 && (!this.config.onlyAttackIdle() || this.isIdleNpc((NPC)nPC)));
        if (nPC2 != null) {
            return this.attackOrApproach(player, nPC2, "Attacking");
        }
        this.fighting = false;
        this.status = "No attackable Cyclops nearby";
        return 1000;
    }

    private int attackOrApproach(Player player, NPC nPC, String string) {
        long l;
        this.activeCombatTarget = nPC;
        this.fighting = true;
        if (nPC != this.engagedTarget) {
            this.engagedTarget = nPC;
            this.engageStartedAt = System.currentTimeMillis();
        }
        if (player.getInteracting() == nPC && !nPC.isDead()) {
            this.engageStartedAt = System.currentTimeMillis();
        }
        if ((l = System.currentTimeMillis() - this.engageStartedAt) > 8000L) {
            this.status = "Can't engage Cyclops \u2014 re-acquiring";
            this.activeCombatTarget = null;
            this.engagedTarget = null;
            return 600;
        }
        if (Reachable.isInteractable((Locatable)nPC)) {
            boolean bl;
            boolean bl2 = bl = !(player.getInteracting() instanceof NPC) && !Movement.isWalking();
            if (bl && l > 2500L) {
                this.status = "Approaching Cyclops";
                MovementPackets.sendMovement(nPC.getWorldLocation());
                return 1000;
            }
            this.status = string + " Cyclops";
            this.lastAttemptedTarget = nPC;
            nPC.interact("Attack");
            return 600;
        }
        MovementPackets.sendMovement(nPC.getWorldLocation());
        this.status = "Approaching Cyclops";
        return 1000;
    }

    private boolean lootDefenders(Player player) {
        Item item;
        TileItem tileItem2;
        WorldPoint worldPoint = player.getWorldLocation();
        TileItem tileItem3 = TileItems.getNearest(tileItem -> tileItem != null && tileItem.getWorldLocation().distanceTo(worldPoint) <= 12 && this.itemName(tileItem.getId()).toLowerCase().contains(DRAGON_DEFENDER));
        TileItem tileItem4 = tileItem2 = tileItem3 != null ? tileItem3 : TileItems.getNearest(tileItem -> tileItem != null && tileItem.getWorldLocation().distanceTo(worldPoint) <= 12 && this.itemName(tileItem.getId()).toLowerCase().contains(DEFENDER_KEYWORD));
        if (tileItem2 == null) {
            return false;
        }
        if (this.isInventoryFull() && (item = this.findFood()) != null) {
            this.status = "Eating to make loot space";
            this.eatFood(item);
            return true;
        }
        this.status = "Looting " + this.itemName(tileItem2.getId());
        tileItem2.interact("Take");
        return true;
    }

    private void updateDefenderStats() {
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        int n = 0;
        boolean bl = false;
        if (itemContainer != null) {
            for (Item item : itemContainer.getItems()) {
                String string;
                if (item == null || item.getId() == -1 || !(string = this.itemName(item.getId()).toLowerCase()).contains(DEFENDER_KEYWORD)) continue;
                ++n;
                int n2 = DefendersPlugin.defenderRank(string);
                if (n2 > this.bestDefenderRank) {
                    this.bestDefenderRank = n2;
                    this.bestDefenderName = DefendersPlugin.defenderTierName(string);
                }
                if (!string.contains(DRAGON_DEFENDER)) continue;
                bl = true;
            }
        }
        if (n > this.prevDefenderCount) {
            this.defendersLooted += n - this.prevDefenderCount;
        }
        this.prevDefenderCount = n;
        if (bl && !this.hadDragonAtStart) {
            this.dragonLooted = true;
        }
    }

    private int countInventoryDefenders() {
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return 0;
        }
        int n = 0;
        for (Item item : itemContainer.getItems()) {
            if (item == null || item.getId() == -1 || !this.itemName(item.getId()).toLowerCase().contains(DEFENDER_KEYWORD)) continue;
            ++n;
        }
        return n;
    }

    private boolean inventoryHasDragon() {
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer == null) {
            return false;
        }
        for (Item item : itemContainer.getItems()) {
            if (item == null || item.getId() == -1 || !this.itemName(item.getId()).toLowerCase().contains(DRAGON_DEFENDER)) continue;
            return true;
        }
        return false;
    }

    private static int defenderRank(String string) {
        if (string.contains("avernic")) {
            return 9;
        }
        if (string.contains("dragon")) {
            return 8;
        }
        if (string.contains("rune")) {
            return 7;
        }
        if (string.contains("adamant")) {
            return 6;
        }
        if (string.contains("mithril")) {
            return 5;
        }
        if (string.contains("black")) {
            return 4;
        }
        if (string.contains("steel")) {
            return 3;
        }
        if (string.contains("iron")) {
            return 2;
        }
        if (string.contains("bronze")) {
            return 1;
        }
        return 0;
    }

    private static String defenderTierName(String string) {
        if (string.contains("avernic")) {
            return "Avernic";
        }
        if (string.contains("dragon")) {
            return "Dragon";
        }
        if (string.contains("rune")) {
            return "Rune";
        }
        if (string.contains("adamant")) {
            return "Adamant";
        }
        if (string.contains("mithril")) {
            return "Mithril";
        }
        if (string.contains("black")) {
            return "Black";
        }
        if (string.contains("steel")) {
            return "Steel";
        }
        if (string.contains("iron")) {
            return "Iron";
        }
        if (string.contains("bronze")) {
            return "Bronze";
        }
        return "Defender";
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
        return nPC != null && !nPC.isDead() && nPC.getHealthRatio() != 0 && nPC.getName() != null && DefendersPlugin.isAttackable(nPC) && this.isPresent(nPC);
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

    private boolean shouldEat() {
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 && n2 * 100 / n < this.config.eatAtHpPercent();
    }

    private void eatFood(Item item) {
        MousePackets.queueClickPacket();
        ItemPackets.itemFirstOption(item);
    }

    private Item findFood() {
        for (String string : FOOD_NAMES) {
            Item item2 = Inventory.getFirst(item -> item != null && item.getId() != -1 && this.itemName(item.getId()).toLowerCase().contains(string.toLowerCase()));
            if (item2 == null) continue;
            return item2;
        }
        return null;
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

    private String itemName(int n) {
        ItemComposition itemComposition = this.client.getItemDefinition(n);
        return itemComposition != null ? itemComposition.getName() : "";
    }

    long getStartTime() {
        return this.startTime;
    }

    String getStatus() {
        return this.status;
    }

    int getKills() {
        return this.kills;
    }

    int getDefendersLooted() {
        return this.defendersLooted;
    }

    String getBestDefenderName() {
        return this.bestDefenderName;
    }

    boolean isDragonLooted() {
        return this.dragonLooted;
    }

    boolean isFinished() {
        return this.finished;
    }

    boolean isFighting() {
        return this.fighting;
    }

    int getTargetHpPercent() {
        NPC nPC = this.activeCombatTarget;
        if (nPC == null || nPC.isDead()) {
            return -1;
        }
        int n = nPC.getHealthRatio();
        int n2 = nPC.getHealthScale();
        if (n < 0 || n2 <= 0) {
            return -1;
        }
        return Math.max(0, Math.min(100, n * 100 / n2));
    }
}

