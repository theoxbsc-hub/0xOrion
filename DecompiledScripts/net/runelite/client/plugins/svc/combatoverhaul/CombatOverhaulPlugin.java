/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.MenuAction
 *  net.runelite.api.MenuEntry
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.events.MenuOpened
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.config.Keybind
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.input.KeyListener
 *  net.runelite.client.input.KeyManager
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.util.HotkeyListener
 */
package net.runelite.client.plugins.svc.combatoverhaul;

import com.google.inject.Binder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.combatoverhaul.CombatOverhaulConfig;
import net.runelite.client.plugins.svc.combatoverhaul.CombatOverhaulOverlay;
import net.runelite.client.plugins.svc.combatoverhaul.GearPrayer;
import net.runelite.client.plugins.svc.combatoverhaul.GearSpell;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.widgets.Prayers;

@PluginDescriptor(name="<html><!--2--><nobr><font color='#FF4646'>\u25cf x64 Combat Overhaul</font></nobr></html>", description="Combat suite. Feature 1: hotkey gear switcher (equip + prayers + special attack).", tags={"orion", "combat", "gear", "switch", "preset", "prayer", "spec", "overhaul"}, enabledByDefault=false)
public class CombatOverhaulPlugin
extends OrionLoopedPlugin {
    private static final int PRESET_COUNT = 6;
    private static final int SPEC_SETTLE_MS = 650;
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private KeyManager keyManager;
    @Inject
    private CombatOverhaulConfig config;
    private CombatOverhaulOverlay overlay;
    private MouseAdapter mouseAdapter;
    private final List<HotkeyListener> hotkeyListeners = new ArrayList<HotkeyListener>();
    private volatile int pendingPreset = -1;
    private volatile List<SwitchStep> plan = null;
    private volatile int planIdx = 0;
    private volatile int activePreset = -1;
    private volatile String status = "Idle";
    private volatile String lastSwitch = "-";
    private volatile boolean combatHandledThisSwitch = false;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(CombatOverhaulConfig.class).toInstance((Object)((CombatOverhaulConfig)configManager.getConfig(CombatOverhaulConfig.class)));
    }

    @Override
    protected void onStart() {
        this.overlay = new CombatOverhaulOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay)this.overlay);
        this.mouseAdapter = new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (CombatOverhaulPlugin.this.overlay != null && CombatOverhaulPlugin.this.overlay.handleMouseClicked(mouseEvent.getX(), mouseEvent.getY())) {
                    mouseEvent.consume();
                }
            }
        };
        try {
            this.client.getCanvas().addMouseListener(this.mouseAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.hotkeyListeners.clear();
        int n = 1;
        while (n <= 6) {
            final int n2 = n++;
            HotkeyListener hotkeyListener = new HotkeyListener(this, () -> this.presetHotkey(n2)){
                final /* synthetic */ CombatOverhaulPlugin this$0;
                {
                    this.this$0 = combatOverhaulPlugin;
                    super(supplier);
                }

                public void hotkeyPressed() {
                    this.this$0.pendingPreset = n2;
                }
            };
            this.keyManager.registerKeyListener((KeyListener)hotkeyListener);
            this.hotkeyListeners.add(hotkeyListener);
        }
        HotkeyListener hotkeyListener = new HotkeyListener(() -> this.config.snapshotHotkey()){

            public void hotkeyPressed() {
                CombatOverhaulPlugin.this.snapshotCapturePreset();
            }
        };
        this.keyManager.registerKeyListener((KeyListener)hotkeyListener);
        this.hotkeyListeners.add(hotkeyListener);
        this.pendingPreset = -1;
        this.plan = null;
        this.planIdx = 0;
        this.activePreset = -1;
        this.status = "Idle";
    }

    @Override
    protected void onStop() {
        for (HotkeyListener hotkeyListener : this.hotkeyListeners) {
            try {
                this.keyManager.unregisterKeyListener((KeyListener)hotkeyListener);
            }
            catch (Exception exception) {}
        }
        this.hotkeyListeners.clear();
        try {
            this.client.getCanvas().removeMouseListener(this.mouseAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.mouseAdapter = null;
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay)this.overlay);
            this.overlay = null;
        }
        this.plan = null;
        this.pendingPreset = -1;
        this.status = "Idle";
    }

    @Override
    protected int loop() {
        List<SwitchStep> list;
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            this.status = "Not logged in";
            return 600;
        }
        if (!this.config.gearSwitcherEnabled()) {
            this.status = "Disabled";
            this.pendingPreset = -1;
            this.plan = null;
            return 300;
        }
        int n = this.pendingPreset;
        if (n != -1) {
            this.pendingPreset = -1;
            this.buildPlan(n);
        }
        if ((list = this.plan) != null && this.planIdx < list.size()) {
            int n2;
            try {
                n2 = list.get(this.planIdx).run();
            }
            catch (Exception exception) {
                n2 = this.equipDelay();
            }
            ++this.planIdx;
            if (this.planIdx >= list.size()) {
                this.finishPlan();
            }
            return Math.max(20, n2);
        }
        if (this.activePreset == -1) {
            this.status = "Idle";
        }
        return 80;
    }

    private void buildPlan(int n) {
        GearSpell gearSpell;
        Object object;
        Object object2;
        ArrayList<SwitchStep> arrayList = new ArrayList<SwitchStep>();
        this.combatHandledThisSwitch = false;
        List<GearItem> list = this.parseItems(this.presetItems(n));
        if (this.config.fastGearSwap()) {
            if (!list.isEmpty()) {
                arrayList.add(() -> this.equipAllStep(list));
            }
        } else {
            object2 = list.iterator();
            while (object2.hasNext()) {
                object = (GearItem)object2.next();
                arrayList.add(() -> this.lambda$buildPlan$1((GearItem)object));
            }
        }
        if ((object2 = this.presetOffensivePrayer(n)) != GearPrayer.NONE && ((GearPrayer)((Object)object2)).prayer != null) {
            arrayList.add(() -> this.lambda$buildPlan$2((GearPrayer)((Object)object2)));
        }
        if ((object = this.presetProtectionPrayer(n)) != GearPrayer.NONE && ((GearPrayer)((Object)object)).prayer != null) {
            arrayList.add(() -> this.lambda$buildPlan$3((GearPrayer)((Object)object)));
        }
        if (this.presetSpec(n)) {
            arrayList.add(this::specStep);
            if (this.config.specAutoAttack()) {
                arrayList.add(this::specAttackStep);
            }
        }
        if ((gearSpell = this.presetSpell(n)) != GearSpell.NONE && gearSpell.spell != null) {
            arrayList.add(() -> this.spellStep(gearSpell));
        }
        if (this.presetContinueAttack(n)) {
            arrayList.add(this::continueAttackStep);
        }
        if (arrayList.isEmpty()) {
            this.plan = null;
            this.planIdx = 0;
            this.activePreset = -1;
            this.status = "Empty: " + this.presetName(n);
            return;
        }
        this.activePreset = n;
        this.plan = arrayList;
        this.planIdx = 0;
        this.status = "Switching: " + this.presetName(n);
    }

    private void finishPlan() {
        this.lastSwitch = this.presetName(this.activePreset);
        this.plan = null;
        this.planIdx = 0;
        this.activePreset = -1;
        this.status = "Idle";
    }

    private int equipStep(GearItem gearItem) {
        this.status = "Equipping " + (String)(gearItem.name.isEmpty() ? "#" + gearItem.id : gearItem.name);
        try {
            Item item = this.findEquipTarget(gearItem);
            if (item != null) {
                this.equipItem(item);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.equipDelay();
    }

    private int equipAllStep(List<GearItem> list) {
        int n = Math.max(0, this.config.fastSwapItemDelayMs());
        int n2 = 0;
        boolean bl = false;
        this.status = "Fast swap (" + list.size() + ")";
        for (GearItem gearItem : list) {
            try {
                Item item = this.findEquipTarget(gearItem);
                if (item == null) continue;
                if (bl && n > 0) {
                    Time.sleep(n);
                }
                this.equipItem(item);
                bl = true;
                ++n2;
            }
            catch (Exception exception) {}
        }
        this.status = "Equipped " + n2 + (n2 == 1 ? " item" : " items");
        return this.equipDelay();
    }

    private Item findEquipTarget(GearItem gearItem) {
        if (gearItem.id > 0 && this.isItemEquippedById(gearItem.id)) {
            return null;
        }
        Item item2 = null;
        if (gearItem.id > 0) {
            item2 = Inventory.getFirst(gearItem.id);
        }
        if (item2 == null && !gearItem.name.isEmpty()) {
            String string = gearItem.name.toLowerCase();
            item2 = Inventory.getFirst(item -> item != null && item.getName() != null && item.getName().toLowerCase().contains(string));
        }
        return item2;
    }

    private int prayerStep(Prayer prayer, String string) {
        this.status = "Prayer: " + string;
        try {
            if (prayer != null && !Prayers.isEnabled(prayer)) {
                Prayers.toggle(prayer);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.equipDelay();
    }

    private int specStep() {
        this.status = "Enabling spec";
        try {
            int n = this.config.specMinEnergyPercent() * 10;
            if (!Combat.isSpecEnabled() && Combat.getSpecEnergy() >= n) {
                Combat.toggleSpec();
                return Math.max(this.equipDelay(), 650);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.equipDelay();
    }

    private int specAttackStep() {
        this.status = "Spec attack";
        try {
            NPC nPC = this.resolveCombatTarget();
            if (nPC != null) {
                nPC.interact("Attack");
                this.combatHandledThisSwitch = true;
            } else {
                this.status = "Spec on (no target)";
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.equipDelay();
    }

    private int continueAttackStep() {
        if (this.combatHandledThisSwitch) {
            this.status = "Combat already engaged";
            return this.equipDelay();
        }
        this.status = "Resuming combat";
        try {
            NPC nPC = this.resolveCombatTarget();
            if (nPC != null) {
                nPC.interact("Attack");
                this.combatHandledThisSwitch = true;
            } else {
                this.status = "Resume: no target";
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.equipDelay();
    }

    private int spellStep(GearSpell gearSpell) {
        if (gearSpell == null || gearSpell.spell == null) {
            return this.equipDelay();
        }
        try {
            NPC nPC;
            NPC nPC2 = nPC = this.config.spellAutoCast() ? this.resolveCombatTarget() : null;
            if (nPC != null) {
                this.status = "Casting " + String.valueOf((Object)gearSpell) + " on target";
                Magic.cast(gearSpell.spell, nPC);
            } else {
                this.status = "Spell ready: " + String.valueOf((Object)gearSpell);
                Magic.selectSpell(gearSpell.spell);
            }
            this.combatHandledThisSwitch = true;
        }
        catch (Exception exception) {
            // empty catch block
        }
        return this.equipDelay();
    }

    private NPC resolveCombatTarget() {
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return null;
        }
        if (player.getInteracting() instanceof NPC) {
            return (NPC)player.getInteracting();
        }
        return NPCs.getNearest(nPC -> nPC != null && nPC.getInteracting() == player);
    }

    private void equipItem(Item item) {
        String string;
        block3: {
            string = "Wield";
            try {
                ItemComposition itemComposition = this.client.getItemDefinition(item.getId());
                if (itemComposition == null || itemComposition.getInventoryActions() == null) break block3;
                for (String string2 : itemComposition.getInventoryActions()) {
                    String string3;
                    if (string2 == null || !(string3 = string2.toLowerCase()).equals("wield") && !string3.equals("wear") && !string3.equals("equip")) continue;
                    string = string2;
                    break;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        item.interact(string);
    }

    private boolean isItemEquippedById(int n) {
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            Item item = Equipment.fromSlot(equipmentInventorySlot);
            if (item == null || item.getId() != n) continue;
            return true;
        }
        return false;
    }

    @Subscribe
    public void onMenuOpened(MenuOpened menuOpened) {
        if (!this.config.gearSwitcherEnabled()) {
            return;
        }
        if (this.config.requireCtrlForMenu() && !this.isCtrlHeld()) {
            return;
        }
        MenuEntry[] menuEntryArray = menuOpened.getMenuEntries();
        if (menuEntryArray == null || menuEntryArray.length == 0) {
            return;
        }
        Item item = this.findInventoryItem(menuEntryArray);
        if (item == null) {
            return;
        }
        int n = item.getId();
        String string = item.getName() == null ? "" : item.getName();
        int n2 = this.config.capturePreset().index + 1;
        this.client.createMenuEntry(-1).setOption("Add to " + this.presetName(n2)).setTarget("<col=ffff00>" + string + "</col>").setType(MenuAction.RUNELITE).onClick(menuEntry -> this.addItemToPreset(n2, n, string));
    }

    private boolean isCtrlHeld() {
        try {
            return this.client.isKeyPressed(82);
        }
        catch (Exception exception) {
            return false;
        }
    }

    private Item findInventoryItem(MenuEntry[] menuEntryArray) {
        Item item;
        int n;
        int n2;
        for (MenuEntry menuEntry : menuEntryArray) {
            n2 = menuEntry.getItemId();
            if (n2 <= 0 || (n = menuEntry.getParam0()) < 0 || n >= 28 || (item = Inventory.getItem(n)) == null || item.getId() != n2) continue;
            return item;
        }
        for (MenuEntry menuEntry : menuEntryArray) {
            n2 = menuEntry.getParam1() >>> 16;
            if (n2 != 149 || (n = menuEntry.getParam0()) < 0 || n >= 28 || (item = Inventory.getItem(n)) == null) continue;
            return item;
        }
        return null;
    }

    private void addItemToPreset(int n, int n2, String string) {
        String string2 = this.presetItems(n);
        for (GearItem object2 : this.parseItems(string2)) {
            if (n2 <= 0 || object2.id != n2) continue;
            return;
        }
        Object object3 = n2 > 0 ? n2 + ":" + string : string;
        Object object = string2 == null || string2.trim().isEmpty() ? object3 : string2.trim() + ", " + (String)object3;
        this.configManager.setConfiguration("combatOverhaul", "preset" + n + "Items", (String)object);
        this.lastSwitch = "Added to " + this.presetName(n);
    }

    public void snapshotCapturePreset() {
        int n = this.config.capturePreset().index + 1;
        this.snapshotWornGear(n);
    }

    private void snapshotWornGear(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
                String string;
                int n2;
                Item item = Equipment.fromSlot(equipmentInventorySlot);
                if (item == null || (n2 = item.getId()) <= 0) continue;
                String string2 = string = item.getName() == null ? "" : item.getName();
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(n2).append(':').append(string);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.configManager.setConfiguration("combatOverhaul", "preset" + n + "Items", stringBuilder.toString());
        this.lastSwitch = "Snapshot -> " + this.presetName(n);
    }

    private int equipDelay() {
        return Math.max(0, this.config.equipDelayMs());
    }

    private List<GearItem> parseItems(String string) {
        ArrayList<GearItem> arrayList = new ArrayList<GearItem>();
        if (string == null) {
            return arrayList;
        }
        for (String string2 : string.split(",")) {
            int n;
            String string3 = string2.trim();
            if (string3.isEmpty()) continue;
            int n2 = string3.indexOf(58);
            if (n2 >= 0) {
                n = this.parseId(string3.substring(0, n2));
                String string4 = string3.substring(n2 + 1).trim();
                arrayList.add(new GearItem(n, string4));
                continue;
            }
            n = this.parseId(string3);
            if (n > 0) {
                arrayList.add(new GearItem(n, ""));
                continue;
            }
            arrayList.add(new GearItem(-1, string3));
        }
        return arrayList;
    }

    private int parseId(String string) {
        if (string == null) {
            return -1;
        }
        String string2 = string.trim();
        if (string2.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

    private Keybind presetHotkey(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1Hotkey();
            }
            case 2: {
                return this.config.preset2Hotkey();
            }
            case 3: {
                return this.config.preset3Hotkey();
            }
            case 4: {
                return this.config.preset4Hotkey();
            }
            case 5: {
                return this.config.preset5Hotkey();
            }
            case 6: {
                return this.config.preset6Hotkey();
            }
        }
        return Keybind.NOT_SET;
    }

    private String presetItems(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1Items();
            }
            case 2: {
                return this.config.preset2Items();
            }
            case 3: {
                return this.config.preset3Items();
            }
            case 4: {
                return this.config.preset4Items();
            }
            case 5: {
                return this.config.preset5Items();
            }
            case 6: {
                return this.config.preset6Items();
            }
        }
        return "";
    }

    private String presetNameRaw(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1Name();
            }
            case 2: {
                return this.config.preset2Name();
            }
            case 3: {
                return this.config.preset3Name();
            }
            case 4: {
                return this.config.preset4Name();
            }
            case 5: {
                return this.config.preset5Name();
            }
            case 6: {
                return this.config.preset6Name();
            }
        }
        return "";
    }

    private GearPrayer presetOffensivePrayer(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1OffensivePrayer();
            }
            case 2: {
                return this.config.preset2OffensivePrayer();
            }
            case 3: {
                return this.config.preset3OffensivePrayer();
            }
            case 4: {
                return this.config.preset4OffensivePrayer();
            }
            case 5: {
                return this.config.preset5OffensivePrayer();
            }
            case 6: {
                return this.config.preset6OffensivePrayer();
            }
        }
        return GearPrayer.NONE;
    }

    private GearPrayer presetProtectionPrayer(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1ProtectionPrayer();
            }
            case 2: {
                return this.config.preset2ProtectionPrayer();
            }
            case 3: {
                return this.config.preset3ProtectionPrayer();
            }
            case 4: {
                return this.config.preset4ProtectionPrayer();
            }
            case 5: {
                return this.config.preset5ProtectionPrayer();
            }
            case 6: {
                return this.config.preset6ProtectionPrayer();
            }
        }
        return GearPrayer.NONE;
    }

    private boolean presetSpec(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1Spec();
            }
            case 2: {
                return this.config.preset2Spec();
            }
            case 3: {
                return this.config.preset3Spec();
            }
            case 4: {
                return this.config.preset4Spec();
            }
            case 5: {
                return this.config.preset5Spec();
            }
            case 6: {
                return this.config.preset6Spec();
            }
        }
        return false;
    }

    private GearSpell presetSpell(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1Spell();
            }
            case 2: {
                return this.config.preset2Spell();
            }
            case 3: {
                return this.config.preset3Spell();
            }
            case 4: {
                return this.config.preset4Spell();
            }
            case 5: {
                return this.config.preset5Spell();
            }
            case 6: {
                return this.config.preset6Spell();
            }
        }
        return GearSpell.NONE;
    }

    private boolean presetContinueAttack(int n) {
        switch (n) {
            case 1: {
                return this.config.preset1ContinueAttack();
            }
            case 2: {
                return this.config.preset2ContinueAttack();
            }
            case 3: {
                return this.config.preset3ContinueAttack();
            }
            case 4: {
                return this.config.preset4ContinueAttack();
            }
            case 5: {
                return this.config.preset5ContinueAttack();
            }
            case 6: {
                return this.config.preset6ContinueAttack();
            }
        }
        return false;
    }

    int presetCount() {
        return 6;
    }

    String presetName(int n) {
        String string = this.presetNameRaw(n);
        return string == null || string.trim().isEmpty() ? "Preset " + n : string.trim();
    }

    int presetItemCount(int n) {
        return this.parseItems(this.presetItems(n)).size();
    }

    String presetHotkeyText(int n) {
        Keybind keybind = this.presetHotkey(n);
        if (keybind == null) {
            return "-";
        }
        String string = keybind.toString();
        if (string == null || string.isEmpty() || string.equalsIgnoreCase("Not set")) {
            return "-";
        }
        return string;
    }

    String getStatus() {
        return this.status;
    }

    String getLastSwitch() {
        return this.lastSwitch;
    }

    String getCapturePresetName() {
        return this.presetName(this.config.capturePreset().index + 1);
    }

    String getProgress() {
        List<SwitchStep> list = this.plan;
        if (list == null || list.isEmpty()) {
            return "";
        }
        return Math.min(this.planIdx + 1, list.size()) + "/" + list.size();
    }

    int getSpecEnergyPercent() {
        try {
            return Combat.getSpecEnergy() / 10;
        }
        catch (Exception exception) {
            return -1;
        }
    }

    private /* synthetic */ int lambda$buildPlan$3(GearPrayer gearPrayer) {
        return this.prayerStep(gearPrayer.prayer, gearPrayer.toString());
    }

    private /* synthetic */ int lambda$buildPlan$2(GearPrayer gearPrayer) {
        return this.prayerStep(gearPrayer.prayer, gearPrayer.toString());
    }

    private /* synthetic */ int lambda$buildPlan$1(GearItem gearItem) {
        return this.equipStep(gearItem);
    }

    private static interface SwitchStep {
        public int run();
    }

    private static final class GearItem {
        final int id;
        final String name;

        GearItem(int n, String string) {
            this.id = n;
            this.name = string == null ? "" : string;
        }
    }
}

