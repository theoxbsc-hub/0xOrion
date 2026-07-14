package net.runelite.client.plugins.svc.woodcutting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("oxWcBot")
public interface WoodcuttingConfig extends Config {
    @ConfigSection(name = "Woodcutting", description = "Woodcutting settings", position = 0)
    String woodcuttingSection = "woodcutting";

    @ConfigItem(
        keyName = "treeObjectId",
        name = "Tree Object ID",
        description = "Object ID of the tree to chop (use Entity Inspector to find it)",
        section = woodcuttingSection,
        position = 0
    )
    default int treeObjectId() {
        return 1276;
    }

    @ConfigItem(
        keyName = "logItemId1",
        name = "Log Item ID 1",
        description = "Primary log item ID to bank/drop",
        section = woodcuttingSection,
        position = 1
    )
    default int logItemId1() {
        return 1511;
    }

    @ConfigItem(
        keyName = "logItemId2",
        name = "Log Item ID 2",
        description = "Optional second log/item ID (nests, etc). Set 0 to ignore.",
        section = woodcuttingSection,
        position = 2
    )
    default int logItemId2() {
        return 0;
    }

    @ConfigItem(
        keyName = "inventoryMode",
        name = "Inventory Action",
        description = "When inventory is full: Bank (deposit box) or Drop logs",
        section = woodcuttingSection,
        position = 3
    )
    default WoodcuttingMode inventoryMode() {
        return WoodcuttingMode.BANK;
    }

    @ConfigItem(
        keyName = "chopAction",
        name = "Chop Action",
        description = "Right-click action on the tree (usually 'Chop down')",
        section = woodcuttingSection,
        position = 4
    )
    default String chopAction() {
        return "Chop down";
    }

    @ConfigItem(
        keyName = "depositObjectId",
        name = "Deposit Object ID",
        description = "Object ID of the deposit box/barrel (default 29327)",
        section = woodcuttingSection,
        position = 5
    )
    default int depositObjectId() {
        return 29327;
    }

    @ConfigItem(
        keyName = "depositAction",
        name = "Deposit Action",
        description = "Right-click action on the deposit object (only used in Bank mode)",
        section = woodcuttingSection,
        position = 6
    )
    default String depositAction() {
        return "Deposit";
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show Overlay",
        description = "Show the status overlay",
        section = woodcuttingSection,
        position = 7
    )
    default boolean showOverlay() {
        return true;
    }
}
