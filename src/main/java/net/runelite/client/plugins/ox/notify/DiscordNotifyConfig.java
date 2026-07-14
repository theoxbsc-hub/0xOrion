package net.runelite.client.plugins.ox.notify;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("oxDiscordNotify")
public interface DiscordNotifyConfig extends Config {
    @ConfigSection(
        name = "Webhook",
        description = "Paste your Discord channel webhook URL",
        position = 0
    )
    String webhookSection = "webhook";

    @ConfigSection(
        name = "Events",
        description = "Which events to post",
        position = 1
    )
    String eventsSection = "events";

    @ConfigItem(
        keyName = "webhookUrl",
        name = "Webhook URL",
        description = "https://discord.com/api/webhooks/...",
        section = webhookSection,
        position = 0,
        secret = true
    )
    default String webhookUrl() {
        return "";
    }

    @ConfigItem(
        keyName = "botName",
        name = "Bot name",
        description = "Display name on Discord embeds",
        section = webhookSection,
        position = 1
    )
    default String botName() {
        return "0x Orion";
    }

    @ConfigItem(
        keyName = "minIntervalMs",
        name = "Min interval (ms)",
        description = "Throttle between posts to avoid Discord rate limits",
        section = webhookSection,
        position = 2
    )
    @Range(min = 0, max = 60_000)
    default int minIntervalMs() {
        return 1500;
    }

    @ConfigItem(
        keyName = "sendTestPing",
        name = "Send test ping",
        description = "Toggle ON to send a test embed, then toggle back OFF",
        section = webhookSection,
        position = 3
    )
    default boolean sendTestPing() {
        return false;
    }

    @ConfigItem(
        keyName = "notifyStaffAlert",
        name = "Staff alert logout",
        description = "When 0xStaffAlert triggers a logout",
        section = eventsSection,
        position = 0
    )
    default boolean notifyStaffAlert() {
        return true;
    }

    @ConfigItem(
        keyName = "notifyLogout",
        name = "Logged out",
        description = "When the client returns to the login screen",
        section = eventsSection,
        position = 1
    )
    default boolean notifyLogout() {
        return true;
    }

    @ConfigItem(
        keyName = "notifyLogin",
        name = "Logged in",
        description = "When the client finishes logging in",
        section = eventsSection,
        position = 2
    )
    default boolean notifyLogin() {
        return false;
    }

    @ConfigItem(
        keyName = "notifyScriptBreak",
        name = "Script break",
        description = "When a 0x loop throws (unexpected error)",
        section = eventsSection,
        position = 3
    )
    default boolean notifyScriptBreak() {
        return true;
    }

    @ConfigItem(
        keyName = "notifyScriptStop",
        name = "Script stop",
        description = "When a bot plugin is disabled / session ends",
        section = eventsSection,
        position = 4
    )
    default boolean notifyScriptStop() {
        return false;
    }
}
