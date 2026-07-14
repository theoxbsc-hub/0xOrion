/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.widgets.Widget
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.jad;

import com.google.inject.Binder;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.jad.JadConfig;
import net.runelite.client.plugins.svc.jad.JadOverlay;
import net.runelite.client.plugins.svc.jad.JadPhase;
import net.runelite.client.plugins.svc.jad.JadResupplyState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Jad Bot</font></nobr></html>", description="Handles the TzTok-Jad fight: auto prayer switching, healer aggro, distance management, prayer restore, and cave re-entry", tags={"orion", "jad", "fight caves", "fire cape", "combat", "boss"}, enabledByDefault=false)
public class JadPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private JadConfig config;
    private JadOverlay overlay;
    private JadPhase phase = JadPhase.ENTERING;
    private String status = "Starting";
    private static final Prayer[] PROTECTION_PRAYERS = new Prayer[]{Prayer.PROTECT_FROM_MAGIC, Prayer.PROTECT_FROM_MISSILES, Prayer.PROTECT_FROM_MELEE};
    private int lastJadAnim = -1;
    private Prayer desiredProtection = Prayer.PROTECT_FROM_MISSILES;
    private Prayer lastSentProtection = null;
    private long lastSentProtectionMs = 0L;
    private long lastSentOffensiveMs = 0L;
    private int healersAlive = 0;
    private int healersOnJad = 0;
    private boolean invSpaceConfirmed = false;
    private boolean postFightScan = false;
    private JadResupplyState resupplyState = null;
    private long bankOpenAt = 0L;
    private boolean resuppliedThisTrip = false;
    private boolean loggedInLastTick = false;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(JadConfig.class).toInstance((Object)((JadConfig)configManager.getConfig(JadConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new JadOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.resetState();
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        switch (gameState) {
            case LOGGED_IN: {
                this.loggedInLastTick = false;
                break;
            }
            case LOGIN_SCREEN: 
            case CONNECTION_LOST: {
                this.loggedInLastTick = false;
                this.phase = JadPhase.ENTERING;
                break;
            }
            case LOADING: {
                if (this.phase != JadPhase.FIGHTING) break;
                this.healersAlive = 0;
                this.healersOnJad = 0;
                this.invSpaceConfirmed = false;
                this.lastJadAnim = -1;
                this.desiredProtection = Prayer.PROTECT_FROM_MISSILES;
                this.lastSentProtection = null;
                this.lastSentProtectionMs = 0L;
                this.lastSentOffensiveMs = 0L;
                this.resupplyState = null;
                this.resuppliedThisTrip = false;
                this.postFightScan = true;
                this.phase = JadPhase.ENTERING;
                this.status = this.config.enableResupply() ? "Died \u2014 will check supplies before re-entering" : "Died \u2014 re-entering caves";
                break;
            }
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
        switch (this.phase) {
            case ENTERING: {
                return this.handleEntering();
            }
            case PREPARING: {
                return this.handlePreparing();
            }
            case COMPLETE: {
                return this.handleComplete();
            }
            case RESUPPLYING: {
                return this.handleResupply(player);
            }
        }
        return this.handleFighting(player);
    }

    private int handleEntering() {
        if (this.dismissDialogue()) {
            this.status = "Dismissing entry dialogue";
            return 1000;
        }
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 3127 && !nPC.isDead());
        if (nPC2 != null) {
            this.phase = JadPhase.FIGHTING;
            this.status = "Jad spotted \u2014 fighting!";
            return 100;
        }
        if (Inventory.getFreeSlots() < 1) {
            Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.foodName().toLowerCase()));
            if (item2 != null) {
                this.status = "Eating " + item2.getName() + " to free slot before entry";
                item2.interact("Eat");
                return 1800;
            }
            this.status = "WARNING: inventory full, no " + this.config.foodName() + " found!";
            return 600;
        }
        TileObject tileObject = TileObjects.getNearest(11833);
        if (tileObject != null) {
            if (this.config.enableResupply() && !this.resuppliedThisTrip && !this.hasRequiredItems()) {
                this.status = "Low on supplies \u2014 banking before entry";
                this.phase = JadPhase.RESUPPLYING;
                this.resupplyState = JadResupplyState.OPEN_CHEST;
                return 600;
            }
            this.postFightScan = false;
            this.status = "Clicking cave entrance";
            tileObject.interact("Enter");
            return 3000;
        }
        if (this.postFightScan) {
            this.status = "Searching for cave entrance...";
            return 600;
        }
        this.phase = JadPhase.PREPARING;
        this.status = "Inside caves \u2014 preparing";
        return 600;
    }

    private int handlePreparing() {
        if (this.dismissDialogue()) {
            this.status = "Dismissing dialogue";
            return 1000;
        }
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 3127 && !nPC.isDead());
        if (nPC2 != null) {
            this.phase = JadPhase.FIGHTING;
            this.status = "Jad spawned \u2014 fighting!";
            return 100;
        }
        if (!this.invSpaceConfirmed) {
            if (Inventory.getFreeSlots() >= 1) {
                this.invSpaceConfirmed = true;
                this.status = "Inventory OK \u2014 waiting for Jad";
            } else {
                Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.foodName().toLowerCase()));
                if (item2 != null) {
                    this.status = "Eating " + item2.getName() + " to free a slot";
                    item2.interact("Eat");
                    return 1800;
                }
                this.status = "WARNING: inventory full, no food found!";
            }
        } else {
            this.status = "Waiting for Jad...";
        }
        return 600;
    }

    private int handleFighting(Player player) {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 3127 && !nPC.isDead());
        if (nPC2 == null) {
            this.phase = JadPhase.COMPLETE;
            this.healersAlive = 0;
            this.healersOnJad = 0;
            this.status = "Jad defeated \u2014 claim Fire Cape!";
            return 600;
        }
        this.updatePrayer(nPC2);
        this.maintainPrayer();
        if (this.shouldRestorePrayer()) {
            return this.drinkPrayerRestore();
        }
        if (this.shouldEat(player)) {
            return this.eatFood();
        }
        if (this.isTooClose(player, nPC2)) {
            return this.stepBack(player, nPC2);
        }
        List<NPC> list = NPCs.getAll(3128);
        this.healersAlive = 0;
        this.healersOnJad = 0;
        NPC nPC3 = null;
        int n = Integer.MAX_VALUE;
        NPC nPC4 = null;
        int n2 = Integer.MAX_VALUE;
        Actor actor = list.iterator();
        while (actor.hasNext()) {
            int n3;
            NPC nPC5 = actor.next();
            if (nPC5 == null || nPC5.isDead()) continue;
            ++this.healersAlive;
            if (this.isHealingJad(nPC5, nPC2)) {
                ++this.healersOnJad;
                n3 = this.chebyshevToJad(nPC5.getWorldLocation(), nPC2);
                if (n3 < n) {
                    n = n3;
                    nPC3 = nPC5;
                }
            }
            if ((n3 = nPC5.getWorldLocation().distanceTo(player.getWorldLocation())) >= n2) continue;
            n2 = n3;
            nPC4 = nPC5;
        }
        if (this.config.smartHealerAggro()) {
            if (nPC3 != null) {
                this.status = "Pulling healer off Jad (" + this.healersOnJad + " healing, dist=" + n + ")";
                nPC3.interact("Attack");
                return 600;
            }
        } else if (nPC4 != null) {
            boolean bl;
            actor = player.getInteracting();
            boolean bl2 = bl = actor instanceof NPC && ((NPC)actor).getId() == 3128 && !((NPC)actor).isDead();
            if (!bl) {
                this.status = "Attacking nearest healer (" + this.healersAlive + " left)";
                nPC4.interact("Attack");
                return 600;
            }
            this.status = "Killing healers (" + this.healersAlive + " left) \u2014 Jad paused";
            return 600;
        }
        if (!((actor = player.getInteracting()) instanceof NPC) || ((NPC)actor).getId() != 3127) {
            this.status = "Attacking Jad";
            nPC2.interact("Attack");
            return 600;
        }
        this.status = "Fighting Jad [" + this.desiredProtection.name().replace("PROTECT_FROM_", "") + "]";
        return 100;
    }

    private int handleComplete() {
        if (this.dismissDialogue()) {
            this.status = "Dismissing post-fight dialogue";
            return 1000;
        }
        this.healersAlive = 0;
        this.healersOnJad = 0;
        this.invSpaceConfirmed = false;
        this.lastJadAnim = -1;
        this.desiredProtection = Prayer.PROTECT_FROM_MISSILES;
        this.lastSentProtection = null;
        this.lastSentProtectionMs = 0L;
        this.lastSentOffensiveMs = 0L;
        this.resupplyState = null;
        this.resuppliedThisTrip = false;
        this.postFightScan = true;
        this.phase = JadPhase.ENTERING;
        this.status = "Jad defeated \u2014 waiting for teleport to entrance";
        return 4000;
    }

    private boolean dismissDialogue() {
        Widget widget = this.client.getWidget(231, 5);
        if (widget != null && !widget.isHidden()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(15138821, -1);
            return true;
        }
        widget = this.client.getWidget(229, 2);
        if (widget != null && !widget.isHidden()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(15007746, -1);
            return true;
        }
        widget = this.client.getWidget(219, 2);
        if (widget != null && !widget.isHidden()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePauseWidgetPacket(14352386, -1);
            return true;
        }
        return false;
    }

    private void updatePrayer(NPC nPC) {
        int n = nPC.getAnimation();
        if (n == -1 || n == this.lastJadAnim) {
            return;
        }
        this.lastJadAnim = n;
        if (n == 2656) {
            this.desiredProtection = Prayer.PROTECT_FROM_MAGIC;
        } else if (n == 2652 || n == 2653) {
            this.desiredProtection = Prayer.PROTECT_FROM_MISSILES;
        }
    }

    private void maintainPrayer() {
        Prayer prayer;
        if (this.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
            return;
        }
        long l = System.currentTimeMillis();
        if (!(this.client.isPrayerActive(this.desiredProtection) || this.desiredProtection == this.lastSentProtection && l - this.lastSentProtectionMs <= 700L)) {
            prayer = PROTECTION_PRAYERS;
            int n = ((Prayer)prayer).length;
            for (int i = 0; i < n; ++i) {
                Prayer prayer2 = prayer[i];
                if (prayer2 == this.desiredProtection || !this.client.isPrayerActive(prayer2)) continue;
                Prayers.toggle(prayer2);
            }
            Prayers.toggle(this.desiredProtection);
            this.lastSentProtection = this.desiredProtection;
            this.lastSentProtectionMs = l;
        }
        Prayer prayer3 = prayer = this.config.offensivePrayer() != null ? this.config.offensivePrayer().prayer : null;
        if (prayer != null && !this.client.isPrayerActive(prayer) && l - this.lastSentOffensiveMs > 700L) {
            Prayers.toggle(prayer);
            this.lastSentOffensiveMs = l;
        }
    }

    private boolean shouldRestorePrayer() {
        if (this.config.prayerPotionName().isEmpty()) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.PRAYER);
        int n2 = this.client.getBoostedSkillLevel(Skill.PRAYER);
        return n > 0 && n2 * 100 / n < this.config.prayerThresholdPct();
    }

    private int drinkPrayerRestore() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.prayerPotionName().toLowerCase()));
        if (item2 != null) {
            this.status = "Drinking " + item2.getName();
            item2.interact("Drink");
            return 600;
        }
        this.status = "OUT OF PRAYER POTIONS!";
        return 300;
    }

    private boolean shouldEat(Player player) {
        if (this.config.foodName().isEmpty()) {
            return false;
        }
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.foodName().toLowerCase()));
        if (item2 == null) {
            return false;
        }
        int n = this.client.getRealSkillLevel(Skill.HITPOINTS);
        int n2 = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
        return n > 0 && n2 * 100 / n < this.config.eatBelowHpPct();
    }

    private int eatFood() {
        Item item2 = Inventory.getFirst(item -> item != null && item.getName().toLowerCase().contains(this.config.foodName().toLowerCase()));
        if (item2 != null) {
            this.status = "Eating " + item2.getName();
            item2.interact("Eat");
            return 1800;
        }
        this.status = "OUT OF FOOD!";
        return 300;
    }

    private boolean isHealingJad(NPC nPC, NPC nPC2) {
        Actor actor = nPC.getInteracting();
        if (actor instanceof Player) {
            return false;
        }
        if (actor instanceof NPC && ((NPC)actor).getId() == 3127) {
            return true;
        }
        return this.chebyshevToJad(nPC.getWorldLocation(), nPC2) <= this.config.healerJadRadius();
    }

    private int chebyshevToJad(WorldPoint worldPoint, NPC nPC) {
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        int n = nPCComposition != null ? nPCComposition.getSize() : 5;
        int n2 = Math.max(0, Math.max(worldPoint2.getX() - worldPoint.getX(), worldPoint.getX() - (worldPoint2.getX() + n - 1)));
        int n3 = Math.max(0, Math.max(worldPoint2.getY() - worldPoint.getY(), worldPoint.getY() - (worldPoint2.getY() + n - 1)));
        return Math.max(n2, n3);
    }

    private boolean isTooClose(Player player, NPC nPC) {
        return this.chebyshevToJad(player.getWorldLocation(), nPC) < this.config.safeDistance();
    }

    private int stepBack(Player player, NPC nPC) {
        int n;
        WorldPoint worldPoint = player.getWorldLocation();
        WorldPoint worldPoint2 = nPC.getWorldLocation();
        int n2 = worldPoint2.getX() + 2;
        int n3 = worldPoint2.getY() + 2;
        int n4 = worldPoint.getX() - n2;
        double d = Math.sqrt((double)n4 * (double)n4 + (double)(n = worldPoint.getY() - n3) * (double)n);
        int n5 = d > 0.0 ? (int)Math.round((double)n4 / d * 4.0) : 0;
        int n6 = d > 0.0 ? (int)Math.round((double)n / d * 4.0) : -4;
        WorldPoint worldPoint3 = new WorldPoint(worldPoint.getX() + n5, worldPoint.getY() + n6, worldPoint.getPlane());
        Movement.walkTo(worldPoint3);
        this.status = "Too close \u2014 backing away from Jad";
        return 600;
    }

    private int handleResupply(Player player) {
        if (this.resupplyState == null) {
            this.phase = JadPhase.ENTERING;
            return 600;
        }
        switch (this.resupplyState) {
            case OPEN_CHEST: {
                if (Bank.isOpen()) {
                    this.resupplyState = JadResupplyState.PRESET_LOAD;
                    return 600;
                }
                TileObject tileObject = TileObjects.getNearest(30267);
                if (tileObject != null) {
                    this.status = "Resupply: opening bank chest";
                    tileObject.interact("Use");
                    this.bankOpenAt = System.currentTimeMillis();
                    this.resupplyState = JadResupplyState.WAIT_OPEN;
                    return 1000;
                }
                this.status = "Resupply: bank chest not found";
                return 600;
            }
            case WAIT_OPEN: {
                if (Bank.isOpen()) {
                    this.status = "Resupply: bank open \u2014 loading preset";
                    this.resupplyState = JadResupplyState.PRESET_LOAD;
                    return 600;
                }
                if (System.currentTimeMillis() - this.bankOpenAt > 5000L) {
                    this.status = "Resupply: bank didn't open \u2014 retrying";
                    this.resupplyState = JadResupplyState.OPEN_CHEST;
                    return 600;
                }
                this.status = "Resupply: waiting for bank to open...";
                return 400;
            }
            case PRESET_LOAD: {
                if (!Bank.isOpen()) {
                    this.status = "Resupply: bank closed early \u2014 reopening";
                    this.resupplyState = JadResupplyState.OPEN_CHEST;
                    return 600;
                }
                this.status = "Resupply: loading preset";
                Mouse.click(452, 22, true);
                this.resupplyState = JadResupplyState.PRESET_SELECT;
                return 500;
            }
            case PRESET_SELECT: {
                this.status = "Resupply: selecting preset";
                Mouse.click(72, 288, true);
                this.resupplyState = JadResupplyState.BANK_CLOSE;
                return 750;
            }
            case BANK_CLOSE: {
                this.status = "Resupply: closing bank";
                Mouse.click(487, 38, true);
                this.resupplyState = JadResupplyState.DONE;
                return 1000;
            }
            case DONE: {
                this.status = "Resupply complete \u2014 re-entering caves";
                this.resuppliedThisTrip = true;
                this.resupplyState = null;
                this.postFightScan = true;
                this.phase = JadPhase.ENTERING;
                return 600;
            }
        }
        return 600;
    }

    private boolean hasRequiredItems() {
        String string = this.config.requiredItems().trim();
        if (string.isEmpty()) {
            return false;
        }
        for (String string2 : string.split(",")) {
            int n;
            String[] stringArray = string2.trim().split(":");
            if (stringArray.length != 2) continue;
            String string3 = stringArray[0].trim().toLowerCase();
            try {
                n = Integer.parseInt(stringArray[1].trim());
            }
            catch (NumberFormatException numberFormatException) {
                continue;
            }
            int n2 = Inventory.getAll(item -> item != null && item.getName().toLowerCase().contains(string3)).stream().mapToInt(Item::getQuantity).sum();
            if (n2 >= n) continue;
            return false;
        }
        return true;
    }

    private void resetState() {
        this.phase = JadPhase.ENTERING;
        this.status = "Starting";
        this.lastJadAnim = -1;
        this.desiredProtection = Prayer.PROTECT_FROM_MISSILES;
        this.lastSentProtection = null;
        this.lastSentProtectionMs = 0L;
        this.lastSentOffensiveMs = 0L;
        this.healersAlive = 0;
        this.healersOnJad = 0;
        this.invSpaceConfirmed = false;
        this.postFightScan = false;
        this.resupplyState = null;
        this.resuppliedThisTrip = false;
        this.loggedInLastTick = false;
    }

    public JadPhase getPhase() {
        return this.phase;
    }

    public String getStatus() {
        return this.status;
    }

    public String getActivePrayerName() {
        return this.desiredProtection.name().replace("PROTECT_FROM_", "");
    }

    public int getHealersAlive() {
        return this.healersAlive;
    }

    public int getHealersOnJad() {
        return this.healersOnJad;
    }

    public int getJadHealthRatio() {
        NPC nPC2 = NPCs.getNearest(nPC -> nPC != null && nPC.getId() == 3127 && !nPC.isDead());
        if (nPC2 == null) {
            return -1;
        }
        int n = nPC2.getHealthRatio();
        int n2 = nPC2.getHealthScale();
        if (n < 0 || n2 <= 0) {
            return -1;
        }
        return n * 100 / n2;
    }
}

