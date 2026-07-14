package net.runelite.client.plugins.ox.woodcutting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("oxWcBot")
public interface WoodcuttingConfig extends Config {
    @ConfigSection(
        name = "Progression",
        description = "Enable tree tiers (lowest→highest). The bot chops the highest enabled tree you can use for your WC level. Enable only one for single-tree mode.",
        position = 0
    )
    String progressionSection = "progression";

    @ConfigSection(name = "Travel", description = "::home → Gnome glider → destination, then start chopping", position = 1)
    String travelSection = "travel";

    @ConfigSection(name = "Overlay", description = "On-screen AIO-style status (chatbox + inventory)", position = 2)
    String overlaySection = "overlay";

    @ConfigSection(name = "Inventory", description = "Bank / drop settings", position = 3)
    String inventorySection = "inventory";

    @ConfigSection(
        name = "ID overrides",
        description = "Leave at 0 to use built-in defaults. Set via 0xDEBUG if Orion uses different IDs.",
        position = 4,
        closedByDefault = true
    )
    String idsSection = "ids";

    // --- Progression ---

    @ConfigItem(keyName = "enableNormal", name = "Normal trees (lvl 1)", description = "Logs 1511 · trees 1276+", section = progressionSection, position = 0)
    default boolean enableNormal() {
        return true;
    }

    @ConfigItem(keyName = "enableOak", name = "Oaks (lvl 15)", description = "Oak logs 1521 · trees 10820/1281", section = progressionSection, position = 1)
    default boolean enableOak() {
        return true;
    }

    @ConfigItem(keyName = "enableWillow", name = "Willows (lvl 30)", description = "Willow logs 1519 · tree 1308", section = progressionSection, position = 2)
    default boolean enableWillow() {
        return true;
    }

    @ConfigItem(keyName = "enableMaple", name = "Maples (lvl 45)", description = "Maple logs 1517 · tree 1307", section = progressionSection, position = 3)
    default boolean enableMaple() {
        return true;
    }

    @ConfigItem(keyName = "enableYew", name = "Yews (lvl 60)", description = "Yew logs 1515 · tree 1309", section = progressionSection, position = 4)
    default boolean enableYew() {
        return true;
    }

    @ConfigItem(keyName = "enableMagic", name = "Magics (lvl 75)", description = "Magic logs 1513 · tree 47421", section = progressionSection, position = 5)
    default boolean enableMagic() {
        return true;
    }

    // --- Travel ---

    @ConfigItem(
        keyName = "gliderTravel",
        name = "Start via glider",
        description = "On login/start: ::home → walk to Gnome glider → teleport to destination, then chop",
        section = travelSection,
        position = 0
    )
    default boolean gliderTravel() {
        return true;
    }

    @ConfigItem(
        keyName = "gliderDestination",
        name = "Glider destination",
        description = "Typed into the glider search box (x64-style)",
        section = travelSection,
        position = 1
    )
    default String gliderDestination() {
        return "Foresters Grove";
    }

    @ConfigItem(
        keyName = "skipTravelIfTreesNearby",
        name = "Skip travel if trees nearby",
        description = "If a matching tree is already in render range, skip ::home/glider and chop immediately",
        section = travelSection,
        position = 2
    )
    default boolean skipTravelIfTreesNearby() {
        return true;
    }

    // --- Inventory ---

    @ConfigItem(
        keyName = "inventoryMode",
        name = "Inventory Action",
        description = "When inventory is full: Bank (deposit box) or Drop",
        section = inventorySection,
        position = 0
    )
    default WoodcuttingMode inventoryMode() {
        return WoodcuttingMode.BANK;
    }

    @ConfigItem(
        keyName = "chopAction",
        name = "Chop Action",
        description = "Right-click action on the tree",
        section = inventorySection,
        position = 1
    )
    default String chopAction() {
        return "Chop down";
    }

    @ConfigItem(
        keyName = "depositObjectId",
        name = "Deposit Object ID",
        description = "Deposit box / barrel object ID",
        section = inventorySection,
        position = 2
    )
    default int depositObjectId() {
        return 29327;
    }

    @ConfigItem(
        keyName = "depositAction",
        name = "Deposit Action",
        description = "Right-click action on the deposit object",
        section = inventorySection,
        position = 3
    )
    default String depositAction() {
        return "Deposit";
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show Overlay",
        description = "AIO-style status over the chatbox and a compact card on the inventory",
        section = overlaySection,
        position = 0
    )
    default boolean showOverlay() {
        return true;
    }

    // --- Inventory ---

    @ConfigItem(keyName = "normalTreeObjectId", name = "Normal tree object ID", description = "0 = default", section = idsSection, position = 0)
    default int normalTreeObjectId() {
        return 0;
    }

    @ConfigItem(keyName = "normalLogItemId", name = "Normal log item ID", description = "0 = default 1511", section = idsSection, position = 1)
    default int normalLogItemId() {
        return 0;
    }

    @ConfigItem(keyName = "oakTreeObjectId", name = "Oak tree object ID", description = "0 = default", section = idsSection, position = 2)
    default int oakTreeObjectId() {
        return 0;
    }

    @ConfigItem(keyName = "oakLogItemId", name = "Oak log item ID", description = "0 = default 1521", section = idsSection, position = 3)
    default int oakLogItemId() {
        return 0;
    }

    @ConfigItem(keyName = "willowTreeObjectId", name = "Willow tree object ID", description = "0 = default", section = idsSection, position = 4)
    default int willowTreeObjectId() {
        return 0;
    }

    @ConfigItem(keyName = "willowLogItemId", name = "Willow log item ID", description = "0 = default 1519", section = idsSection, position = 5)
    default int willowLogItemId() {
        return 0;
    }

    @ConfigItem(keyName = "mapleTreeObjectId", name = "Maple tree object ID", description = "0 = default", section = idsSection, position = 6)
    default int mapleTreeObjectId() {
        return 0;
    }

    @ConfigItem(keyName = "mapleLogItemId", name = "Maple log item ID", description = "0 = default 1517", section = idsSection, position = 7)
    default int mapleLogItemId() {
        return 0;
    }

    @ConfigItem(keyName = "yewTreeObjectId", name = "Yew tree object ID", description = "0 = default", section = idsSection, position = 8)
    default int yewTreeObjectId() {
        return 0;
    }

    @ConfigItem(keyName = "yewLogItemId", name = "Yew log item ID", description = "0 = default 1515", section = idsSection, position = 9)
    default int yewLogItemId() {
        return 0;
    }

    @ConfigItem(keyName = "magicTreeObjectId", name = "Magic tree object ID", description = "0 = default", section = idsSection, position = 10)
    default int magicTreeObjectId() {
        return 0;
    }

    @ConfigItem(keyName = "magicLogItemId", name = "Magic log item ID", description = "0 = default 1513", section = idsSection, position = 11)
    default int magicLogItemId() {
        return 0;
    }
}
