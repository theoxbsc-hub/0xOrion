package net.runelite.client.plugins.ox.mule;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("oxMule")
public interface MuleConfig extends Config {
    @ConfigSection(
        name = "Trade",
        description = "Incoming trade acceptance",
        position = 0,
        closedByDefault = false
    )
    String tradeSection = "trade";

    @ConfigSection(
        name = "Banking",
        description = "Bank at home after a successful trade",
        position = 1,
        closedByDefault = false
    )
    String bankSection = "bank";

    @ConfigSection(
        name = "Debug",
        description = "Overlay",
        position = 2,
        closedByDefault = true
    )
    String debugSection = "debug";

    @ConfigItem(
        keyName = "screen1AcceptDelayMs",
        name = "Screen 1 accept delay",
        description = "Wait after trade screen 1 opens so the other account can offer items",
        section = tradeSection,
        position = 0
    )
    @Range(min = 0, max = 15000)
    @Units("ms")
    default int screen1AcceptDelayMs() {
        return 4000;
    }

    @ConfigItem(
        keyName = "autoBankAfterTrade",
        name = "Bank after trade",
        description = "Walk to the home bank and deposit inventory after each completed trade",
        section = bankSection,
        position = 0
    )
    default boolean autoBankAfterTrade() {
        return true;
    }

    @ConfigItem(
        keyName = "bankObjectId",
        name = "Bank object ID",
        description = "Preferred bank object id (0 = use name only). Default Seers booth.",
        section = bankSection,
        position = 1
    )
    default int bankObjectId() {
        return 10083;
    }

    @ConfigItem(
        keyName = "bankObjectName",
        name = "Bank object name",
        description = "Fallback name match (contains, case-insensitive)",
        section = bankSection,
        position = 2
    )
    default String bankObjectName() {
        return "Bank booth";
    }

    @ConfigItem(
        keyName = "bankAction",
        name = "Bank action",
        description = "Object action used to open the bank",
        section = bankSection,
        position = 3
    )
    default String bankAction() {
        return "Use";
    }

    @ConfigItem(
        keyName = "keepItems",
        name = "Keep items",
        description = "Comma-separated name substrings that are never deposited (e.g. coin pouch)",
        section = bankSection,
        position = 4
    )
    default String keepItems() {
        return "";
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show overlay",
        description = "Inventory-bottom mule status card",
        section = debugSection,
        position = 0
    )
    default boolean showOverlay() {
        return true;
    }
}
