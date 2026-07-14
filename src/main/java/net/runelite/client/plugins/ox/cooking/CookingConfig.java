package net.runelite.client.plugins.ox.cooking;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("oxCookingBot")
public interface CookingConfig extends Config {
    @ConfigSection(
        name = "Fish",
        description = "Enable raw fish types to withdraw/cook (IDs match 0xFish pier catches)",
        position = 0
    )
    String fishSection = "fish";

    @ConfigSection(
        name = "Objects",
        description = "Bank booth + cooking range at the pier",
        position = 1
    )
    String objectsSection = "objects";

    @ConfigSection(name = "Overlay", description = "Status card", position = 2)
    String overlaySection = "overlay";

    // --- Fish ---

    @ConfigItem(
        keyName = "enableShrimp",
        name = "Shrimps / Anchovies",
        description = "Raw 317 / 321",
        section = fishSection,
        position = 0
    )
    default boolean enableShrimp() {
        return true;
    }

    @ConfigItem(
        keyName = "enableLobster",
        name = "Lobsters",
        description = "Raw 377",
        section = fishSection,
        position = 1
    )
    default boolean enableLobster() {
        return true;
    }

    @ConfigItem(
        keyName = "enableMonkfish",
        name = "Monkfish",
        description = "Raw 7944",
        section = fishSection,
        position = 2
    )
    default boolean enableMonkfish() {
        return true;
    }

    @ConfigItem(
        keyName = "enableShark",
        name = "Sharks",
        description = "Raw 383",
        section = fishSection,
        position = 3
    )
    default boolean enableShark() {
        return true;
    }

    @ConfigItem(
        keyName = "enableAngler",
        name = "Anglers",
        description = "Raw 13439",
        section = fishSection,
        position = 4
    )
    default boolean enableAngler() {
        return true;
    }

    // --- Objects ---

    @ConfigItem(
        keyName = "bankBoothId",
        name = "Bank booth ID",
        description = "Pier bank booth object id",
        section = objectsSection,
        position = 0
    )
    default int bankBoothId() {
        return 10517;
    }

    @ConfigItem(
        keyName = "bankAction",
        name = "Bank action",
        description = "Object action on the booth",
        section = objectsSection,
        position = 1
    )
    default String bankAction() {
        return "Bank";
    }

    @ConfigItem(
        keyName = "rangeObjectId",
        name = "Cooking range ID",
        description = "Range / fire object to use raw fish on",
        section = objectsSection,
        position = 2
    )
    default int rangeObjectId() {
        return 114;
    }

    @ConfigItem(
        keyName = "cookClickX",
        name = "Cook button X",
        description = "Canvas X for cook-all / interface click (classic 765-wide)",
        section = objectsSection,
        position = 3
    )
    default int cookClickX() {
        return 260;
    }

    @ConfigItem(
        keyName = "cookClickY",
        name = "Cook button Y",
        description = "Canvas Y for cook-all / interface click (classic 503-tall)",
        section = objectsSection,
        position = 4
    )
    default int cookClickY() {
        return 412;
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show overlay",
        description = "Compact status card on the inventory",
        section = overlaySection,
        position = 0
    )
    default boolean showOverlay() {
        return true;
    }
}
