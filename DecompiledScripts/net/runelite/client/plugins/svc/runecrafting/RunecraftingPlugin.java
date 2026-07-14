/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.runecrafting;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.runecrafting.RunecraftingConfig;
import net.runelite.client.plugins.svc.runecrafting.RunecraftingOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;

@PluginDescriptor(name="<html><!--3--><nobr><font color='#3FBF3F'>\u25cf x64 Runecrafting</font></nobr></html>", description="Talisman \u2192 altar \u2192 craft \u2192 tablet \u2192 bank runecrafting loop", tags={"orion", "runecrafting", "rc", "skilling"}, enabledByDefault=false)
public class RunecraftingPlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private RunecraftingConfig config;
    private RunecraftingOverlay overlay;
    private RcState state = RcState.TELEPORT_TO_ALTAR;
    private int stateEntryTick = 0;
    private int boothMissingSinceTick = -1;
    private int xpBeforeCraft = 0;
    private int startXp = -1;
    private int runsCompleted = 0;
    private String status = "Idle";

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(RunecraftingConfig.class).toInstance((Object)((RunecraftingConfig)configManager.getConfig(RunecraftingConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new RunecraftingOverlay(this.client, this);
        this.overlayManager.add((Overlay)this.overlay);
        this.state = RcState.TELEPORT_TO_ALTAR;
        this.stateEntryTick = 0;
        this.boothMissingSinceTick = -1;
        this.runsCompleted = 0;
        this.startXp = -1;
        this.status = "Starting";
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.status = "Idle";
    }

    @Override
    protected int loop() {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return 1000;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        if (this.startXp < 0) {
            this.startXp = Skills.getExperience(Skill.RUNECRAFT);
        }
        switch (this.state.ordinal()) {
            case 0: {
                return this.teleportToAltar();
            }
            case 1: {
                return this.waitAtAltar();
            }
            case 2: {
                return this.craft();
            }
            case 3: {
                return this.waitCraft();
            }
            case 4: {
                return this.teleportToBank();
            }
            case 5: {
                return this.waitAtBank();
            }
            case 6: {
                return this.deposit();
            }
            case 7: {
                return this.withdraw();
            }
            case 8: {
                return this.closeBank();
            }
        }
        return 600;
    }

    private int teleportToAltar() {
        if (TileObjects.getNearest(this.config.altarObjectId()) != null) {
            this.xpBeforeCraft = Skills.getExperience(Skill.RUNECRAFT);
            return this.enter(RcState.CRAFTING, 300);
        }
        Item item = Inventory.getFirst(this.config.talismanId());
        if (item == null) {
            this.status = "No talisman (id " + this.config.talismanId() + ") in inventory";
            return 2000;
        }
        this.status = "Teleporting to altar";
        item.interact(this.config.talismanAction());
        return this.enter(RcState.WAIT_AT_ALTAR, 1800);
    }

    private int waitAtAltar() {
        this.status = "Arriving at altar...";
        if (TileObjects.getNearest(this.config.altarObjectId()) != null) {
            this.xpBeforeCraft = Skills.getExperience(Skill.RUNECRAFT);
            return this.enter(RcState.CRAFTING, 300);
        }
        if (this.ticksInState() > this.config.arrivalTimeoutTicks()) {
            this.status = "Altar not found \u2014 retrying teleport";
            return this.enter(RcState.TELEPORT_TO_ALTAR, 600);
        }
        return 600;
    }

    private int craft() {
        if (!Inventory.contains(this.config.essenceId())) {
            this.status = "No essence (id " + this.config.essenceId() + ") \u2014 returning to bank";
            return this.enter(RcState.TELEPORT_TO_BANK, 300);
        }
        TileObject tileObject = TileObjects.getNearest(this.config.altarObjectId());
        if (tileObject == null) {
            return this.enter(RcState.WAIT_AT_ALTAR, 600);
        }
        this.status = "Crafting runes";
        tileObject.interact(this.config.altarAction());
        return this.enter(RcState.WAIT_CRAFT, 1200);
    }

    private int waitCraft() {
        if (Skills.getExperience(Skill.RUNECRAFT) > this.xpBeforeCraft) {
            ++this.runsCompleted;
            this.status = "Crafted (run #" + this.runsCompleted + ")";
            return this.enter(RcState.TELEPORT_TO_BANK, 600);
        }
        if (this.ticksInState() > this.config.craftTimeoutTicks()) {
            this.status = "No XP gain \u2014 re-clicking altar";
            return this.enter(RcState.CRAFTING, 300);
        }
        return 600;
    }

    private int teleportToBank() {
        if (Bank.isOpen() || TileObjects.getNearest(this.config.bankBoothId()) != null) {
            return this.enter(RcState.WAIT_AT_BANK, 300);
        }
        Item item = Inventory.getFirst(this.config.tabletId());
        if (item == null) {
            this.status = "No teleport tablet (id " + this.config.tabletId() + ") in inventory";
            return 2000;
        }
        this.status = "Teleporting to bank";
        item.interact(this.config.tabletAction());
        return this.enter(RcState.WAIT_AT_BANK, 1000);
    }

    private int waitAtBank() {
        if (Bank.isOpen()) {
            this.boothMissingSinceTick = -1;
            return this.enter(RcState.DEPOSIT, 300);
        }
        TileObject tileObject = TileObjects.getNearest(this.config.bankBoothId());
        if (tileObject != null) {
            this.boothMissingSinceTick = -1;
            this.status = "Opening bank";
            tileObject.interact(this.config.bankAction());
            return 1500;
        }
        if (this.boothMissingSinceTick < 0) {
            this.boothMissingSinceTick = this.client.getTickCount();
        }
        if (this.client.getTickCount() - this.boothMissingSinceTick >= this.config.bankRetryTimeoutTicks()) {
            Item item = Inventory.getFirst(this.config.tabletId());
            if (item != null) {
                this.status = "Bank booth not found \u2014 re-teleporting";
                item.interact(this.config.tabletAction());
                this.boothMissingSinceTick = -1;
                return 1000;
            }
            this.status = "Bank booth not found & no tablet (id " + this.config.tabletId() + ")";
            return 1000;
        }
        this.status = "Bank booth (id " + this.config.bankBoothId() + ") not found";
        return 1000;
    }

    private int deposit() {
        if (!Bank.isOpen()) {
            return this.enter(RcState.WAIT_AT_BANK, 600);
        }
        int[] nArray = RunecraftingPlugin.parseIds(this.config.depositIds());
        if (nArray.length > 0) {
            this.status = "Depositing runes";
            for (int n : nArray) {
                Bank.depositAll(n);
            }
        }
        return this.enter(RcState.WITHDRAW, 600);
    }

    private int withdraw() {
        if (!Bank.isOpen()) {
            return this.enter(RcState.WAIT_AT_BANK, 600);
        }
        List<int[]> list = RunecraftingPlugin.parseWithdraw(this.config.withdrawItems());
        for (int[] nArray : list) {
            if (Bank.getCount(nArray[0]) > 0) continue;
            this.status = "Out of bank stock (id " + nArray[0] + ") \u2014 stopping";
            Bank.close();
            return -1;
        }
        this.status = "Withdrawing supplies";
        for (int[] nArray : list) {
            if (nArray[1] < 0) continue;
            Bank.withdraw(nArray[0], nArray[1], Bank.WithdrawMode.ITEM);
        }
        for (int[] nArray : list) {
            if (nArray[1] >= 0) continue;
            Bank.withdrawAll(nArray[0], Bank.WithdrawMode.ITEM);
        }
        return this.enter(RcState.CLOSE_BANK, 800);
    }

    private int closeBank() {
        if (Bank.isOpen()) {
            Bank.close();
        }
        this.status = "Returning to altar";
        return this.enter(RcState.TELEPORT_TO_ALTAR, 600);
    }

    private int enter(RcState rcState, int n) {
        this.state = rcState;
        this.stateEntryTick = this.client.getTickCount();
        return n;
    }

    private int ticksInState() {
        return this.client.getTickCount() - this.stateEntryTick;
    }

    private static int[] parseIds(String string) {
        if (string == null || string.trim().isEmpty()) {
            return new int[0];
        }
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        String[] objectArray = string.split(",");
        int n = objectArray.length;
        for (int i = 0; i < n; ++i) {
            String string2 = objectArray[i];
            String string3 = string2.trim();
            if (string3.isEmpty()) continue;
            try {
                arrayList.add(Integer.parseInt(string3));
                continue;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        int[] nArray = new int[arrayList.size()];
        for (n = 0; n < nArray.length; ++n) {
            nArray[n] = (Integer)arrayList.get(n);
        }
        return nArray;
    }

    private static List<int[]> parseWithdraw(String string) {
        ArrayList<int[]> arrayList = new ArrayList<int[]>();
        if (string == null || string.trim().isEmpty()) {
            return arrayList;
        }
        for (String string2 : string.split(",")) {
            String string3 = string2.trim();
            if (string3.isEmpty()) continue;
            String[] stringArray = string3.split(":");
            try {
                String string4;
                int n = Integer.parseInt(stringArray[0].trim());
                int n2 = -1;
                if (stringArray.length > 1 && !(string4 = stringArray[1].trim()).equalsIgnoreCase("all") && (n2 = Integer.parseInt(string4)) < 0) {
                    n2 = -1;
                }
                arrayList.add(new int[]{n, n2});
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return arrayList;
    }

    public String getStatus() {
        return this.status;
    }

    public String getStateName() {
        return this.state.name();
    }

    public int getRunsCompleted() {
        return this.runsCompleted;
    }

    public int getXpGained() {
        if (this.startXp < 0) {
            return 0;
        }
        return Math.max(0, Skills.getExperience(Skill.RUNECRAFT) - this.startXp);
    }

    static enum RcState {
        TELEPORT_TO_ALTAR,
        WAIT_AT_ALTAR,
        CRAFTING,
        WAIT_CRAFT,
        TELEPORT_TO_BANK,
        WAIT_AT_BANK,
        DEPOSIT,
        WITHDRAW,
        CLOSE_BANK;

    }
}

