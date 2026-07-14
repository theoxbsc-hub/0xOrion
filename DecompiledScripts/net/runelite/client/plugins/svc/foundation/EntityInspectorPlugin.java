/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.MenuEntry
 *  net.runelite.api.NPC
 *  net.runelite.api.events.MenuOpened
 *  net.runelite.api.events.MenuOptionClicked
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.foundation;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.foundation.EntityInspectorInventoryOverlay;
import net.runelite.client.plugins.svc.foundation.EntityInspectorOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="<html><!--4--><nobr><font color='#4FA8FF'>\u25cf x64 Entity Inspector</font></nobr></html>", description="Shows IDs and names for nearby entities \u2014 use to discover server-specific IDs", tags={"orion", "inspector", "ids", "debug"})
public class EntityInspectorPlugin
extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    volatile String atCursorInfo = "";
    volatile String atCursorTile = "";
    volatile String lastExamineInfo = "";
    private EntityInspectorOverlay overlay;
    private EntityInspectorInventoryOverlay inventoryOverlay;

    protected void startUp() {
        this.overlay = new EntityInspectorOverlay(this.client, this);
        this.overlayManager.add((Overlay)this.overlay);
        this.inventoryOverlay = new EntityInspectorInventoryOverlay(this.client);
        this.overlayManager.add((Overlay)this.inventoryOverlay);
    }

    protected void shutDown() {
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlayManager.remove((Overlay)this.inventoryOverlay);
        this.overlay = null;
        this.inventoryOverlay = null;
        this.atCursorInfo = "";
        this.atCursorTile = "";
        this.lastExamineInfo = "";
    }

    @Subscribe
    public void onMenuOpened(MenuOpened menuOpened) {
        this.atCursorInfo = "";
        this.atCursorTile = "";
        MenuEntry[] menuEntryArray = menuOpened.getMenuEntries();
        if (menuEntryArray == null) {
            return;
        }
        for (MenuEntry menuEntry : menuEntryArray) {
            String string = menuEntry.getOption();
            if (string == null) continue;
            if (string.equalsIgnoreCase("Walk here")) {
                int n = this.client.getBaseX() + menuEntry.getParam0();
                int n2 = this.client.getBaseY() + menuEntry.getParam1();
                this.atCursorTile = n + ", " + n2 + " P" + this.client.getPlane();
                continue;
            }
            if (string.equalsIgnoreCase("Cancel") || !this.atCursorInfo.isEmpty()) continue;
            String string2 = menuEntry.getType() != null ? menuEntry.getType().name() : "";
            String string3 = EntityInspectorPlugin.stripTags(menuEntry.getTarget());
            int n = menuEntry.getIdentifier();
            if (string2.contains("NPC")) {
                int n3 = this.resolveNpcId(n);
                this.atCursorInfo = "NPC: " + string3 + "  ID:" + n3;
                continue;
            }
            if (string2.contains("OBJECT")) {
                this.atCursorInfo = "Obj: " + string3 + "  ID:" + n;
                continue;
            }
            if (string2.contains("GROUND_ITEM") || string2.contains("ITEM_ON_TILE")) {
                this.atCursorInfo = "Item: " + string3 + "  ID:" + n;
                continue;
            }
            if (string3.isEmpty()) continue;
            this.atCursorInfo = string3 + "  ID:" + n;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
        String string;
        if (!"Examine".equalsIgnoreCase(menuOptionClicked.getMenuOption())) {
            return;
        }
        String string2 = EntityInspectorPlugin.stripTags(menuOptionClicked.getMenuTarget());
        int n = menuOptionClicked.getId();
        String string3 = string = menuOptionClicked.getMenuAction() != null ? menuOptionClicked.getMenuAction().name() : "";
        if (string.contains("NPC")) {
            int n2 = this.resolveNpcId(n);
            this.lastExamineInfo = "NPC: " + string2 + "  ID:" + n2;
        } else if (string.contains("OBJECT")) {
            this.lastExamineInfo = "Obj: " + string2 + "  ID:" + n;
        } else if (string.contains("ITEM")) {
            int n3 = n;
            try {
                Item item;
                ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
                if (itemContainer != null && (item = itemContainer.getItem(menuOptionClicked.getParam0())) != null && item.getId() != -1) {
                    n3 = item.getId();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.lastExamineInfo = "Item: " + string2 + "  ID:" + n3;
        } else {
            this.lastExamineInfo = string2 + "  ID:" + n;
        }
    }

    private int resolveNpcId(int n) {
        NPC[] nPCArray = this.client.getCachedNPCs();
        if (nPCArray == null || n < 0 || n >= nPCArray.length) {
            return n;
        }
        NPC nPC = nPCArray[n];
        return nPC != null ? nPC.getId() : n;
    }

    static String stripTags(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("<[^>]*>", "").trim();
    }
}

