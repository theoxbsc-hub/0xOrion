package net.runelite.client.plugins.ox.debug;

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
import net.runelite.client.plugins.ox.core.OxConsole;
import net.runelite.client.plugins.ox.core.OxPluginRegistry;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>\u25cf 0xDEBUG</font></nobr></html>",
    description = "Shows IDs and names for nearby entities — use to discover server-specific IDs",
    tags = {"0x", "inspector", "ids", "debug", "entity"},
    enabledByDefault = false
)
public class DebugPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;

    volatile String atCursorInfo = "";
    volatile String atCursorTile = "";
    volatile String lastExamineInfo = "";

    private DebugOverlay overlay;
    private DebugInventoryOverlay inventoryOverlay;

    @Override
    protected void startUp() {
        this.overlay = new DebugOverlay(this.client, this);
        this.overlayManager.add((Overlay) this.overlay);
        this.inventoryOverlay = new DebugInventoryOverlay(this.client);
        this.overlayManager.add((Overlay) this.inventoryOverlay);
        OxPluginRegistry.register("0xDEBUG");
        OxConsole.info("DEBUG", "entity inspector on");
    }

    @Override
    protected void shutDown() {
        this.overlayManager.remove((Overlay) this.overlay);
        this.overlayManager.remove((Overlay) this.inventoryOverlay);
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
        MenuEntry[] entries = menuOpened.getMenuEntries();
        if (entries == null) {
            return;
        }
        for (MenuEntry entry : entries) {
            String option = entry.getOption();
            if (option == null) {
                continue;
            }
            if (option.equalsIgnoreCase("Walk here")) {
                int x = this.client.getBaseX() + entry.getParam0();
                int y = this.client.getBaseY() + entry.getParam1();
                this.atCursorTile = x + ", " + y + " P" + this.client.getPlane();
                continue;
            }
            if (option.equalsIgnoreCase("Cancel") || !this.atCursorInfo.isEmpty()) {
                continue;
            }
            String type = entry.getType() != null ? entry.getType().name() : "";
            String target = stripTags(entry.getTarget());
            int id = entry.getIdentifier();
            if (type.contains("NPC")) {
                this.atCursorInfo = "NPC: " + target + "  ID:" + this.resolveNpcId(id);
            } else if (type.contains("OBJECT")) {
                this.atCursorInfo = "Obj: " + target + "  ID:" + id;
            } else if (type.contains("GROUND_ITEM") || type.contains("ITEM_ON_TILE")) {
                this.atCursorInfo = "Item: " + target + "  ID:" + id;
            } else if (!target.isEmpty()) {
                this.atCursorInfo = target + "  ID:" + id;
            }
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked click) {
        if (!"Examine".equalsIgnoreCase(click.getMenuOption())) {
            return;
        }
        String target = stripTags(click.getMenuTarget());
        int id = click.getId();
        String action = click.getMenuAction() != null ? click.getMenuAction().name() : "";
        if (action.contains("NPC")) {
            this.lastExamineInfo = "NPC: " + target + "  ID:" + this.resolveNpcId(id);
        } else if (action.contains("OBJECT")) {
            this.lastExamineInfo = "Obj: " + target + "  ID:" + id;
        } else if (action.contains("ITEM")) {
            int itemId = id;
            try {
                ItemContainer inv = this.client.getItemContainer(InventoryID.INVENTORY);
                if (inv != null) {
                    Item item = inv.getItem(click.getParam0());
                    if (item != null && item.getId() != -1) {
                        itemId = item.getId();
                    }
                }
            } catch (Exception ignored) {
                // keep menu id
            }
            this.lastExamineInfo = "Item: " + target + "  ID:" + itemId;
        } else {
            this.lastExamineInfo = target + "  ID:" + id;
        }
    }

    private int resolveNpcId(int indexOrId) {
        NPC[] cached = this.client.getCachedNPCs();
        if (cached == null || indexOrId < 0 || indexOrId >= cached.length) {
            return indexOrId;
        }
        NPC npc = cached[indexOrId];
        return npc != null ? npc.getId() : indexOrId;
    }

    static String stripTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "").trim();
    }
}
