package net.runelite.client.plugins.ox.fishing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("oxFishingBot")
public interface FishingConfig extends Config {
    @ConfigSection(
        name = "Progression",
        description = "Enable fish tiers. Bot uses the highest enabled spot you qualify for.",
        position = 0
    )
    String progressionSection = "progression";

    @ConfigSection(
        name = "Travel",
        description = "When at home: ::home → Gnome glider → Seabright Pier",
        position = 1
    )
    String travelSection = "travel";

    @ConfigSection(name = "Inventory", description = "Bank / drop when full", position = 2)
    String inventorySection = "inventory";

    @ConfigSection(name = "Overlay", description = "Status card", position = 3)
    String overlaySection = "overlay";

    // --- Progression ---

    @ConfigItem(
        keyName = "enableShrimp",
        name = "Shrimps (lvl 1–39)",
        description = "Spot 1514 · Small Net · fish 317 & 321",
        section = progressionSection,
        position = 0
    )
    default boolean enableShrimp() {
        return true;
    }

    @ConfigItem(
        keyName = "enableLobster",
        name = "Lobsters (lvl 40–61)",
        description = "Spot 1510 · Cage · fish 377",
        section = progressionSection,
        position = 1
    )
    default boolean enableLobster() {
        return true;
    }

    @ConfigItem(
        keyName = "enableMonkfish",
        name = "Monkfish (lvl 62–74)",
        description = "Spot 4316 · Net · fish 7944",
        section = progressionSection,
        position = 2
    )
    default boolean enableMonkfish() {
        return true;
    }

    @ConfigItem(
        keyName = "enableShark",
        name = "Sharks (lvl 75–81)",
        description = "Spot 1511 · Harpoon · fish 383",
        section = progressionSection,
        position = 3
    )
    default boolean enableShark() {
        return true;
    }

    @ConfigItem(
        keyName = "enableAngler",
        name = "Anglers (lvl 82–99)",
        description = "Spot 6825 · Bait · fish 13439",
        section = progressionSection,
        position = 4
    )
    default boolean enableAngler() {
        return true;
    }

    // --- Travel ---

    @ConfigItem(
        keyName = "gliderTravel",
        name = "Start via glider",
        description = "If at home (or far from pier): ::home → glider → destination, then fish",
        section = travelSection,
        position = 0
    )
    default boolean gliderTravel() {
        return true;
    }

    @ConfigItem(
        keyName = "gliderDestination",
        name = "Glider destination",
        description = "Typed into the glider search box",
        section = travelSection,
        position = 1
    )
    default String gliderDestination() {
        return "seabright pier";
    }

    @ConfigItem(
        keyName = "skipTravelIfSpotNearby",
        name = "Skip travel if spot nearby",
        description = "If the active fishing spot is already in range, skip glider travel",
        section = travelSection,
        position = 2
    )
    default boolean skipTravelIfSpotNearby() {
        return true;
    }

    // --- Inventory ---

    @ConfigItem(
        keyName = "fishingMode",
        name = "Inventory Action",
        description = "When inventory is full: Bank (deposit barrel) or Drop",
        section = inventorySection,
        position = 0
    )
    default FishingMode fishingMode() {
        return FishingMode.BANK;
    }

    @ConfigItem(
        keyName = "barrelObjectId",
        name = "Deposit barrel ID",
        description = "Deposit object at the pier",
        section = inventorySection,
        position = 1
    )
    default int barrelObjectId() {
        return 29108;
    }

    @ConfigItem(
        keyName = "barrelAction",
        name = "Barrel Action",
        description = "Right-click action on the deposit barrel (Bank mode)",
        section = inventorySection,
        position = 2
    )
    default String barrelAction() {
        return "Deposit";
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show Overlay",
        description = "Inventory-bottom fishing status card",
        section = overlaySection,
        position = 0
    )
    default boolean showOverlay() {
        return true;
    }
}
