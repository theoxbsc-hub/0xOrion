package net.runelite.client.plugins.ox.autologin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("oxAutoLogin")
public interface AutoLoginConfig extends Config {
    @ConfigSection(
        name = "Behaviour",
        description = "When and how to auto-login",
        position = 0
    )
    String behaviourSection = "behaviour";

    @ConfigSection(
        name = "Manual fallback",
        description = "Used only if Launch-0x did not set ox.username / ox.password",
        position = 1,
        closedByDefault = true
    )
    String fallbackSection = "fallback";

    @ConfigItem(
        keyName = "useLaunchAccount",
        name = "Use launch account",
        description = "Log in with the account selected in Launch-0x.bat (-Dox.username / -Dox.password)",
        section = behaviourSection,
        position = 0
    )
    default boolean useLaunchAccount() {
        return true;
    }

    @ConfigItem(
        keyName = "loginDelay",
        name = "Login delay (seconds)",
        description = "Wait this long on the login screen before submitting credentials",
        section = behaviourSection,
        position = 1
    )
    @Range(min = 0, max = 60)
    default int loginDelay() {
        return 3;
    }

    @ConfigItem(
        keyName = "fallbackUsername",
        name = "Fallback username",
        description = "Only used when launch account props are empty",
        section = fallbackSection,
        position = 0
    )
    default String fallbackUsername() {
        return "";
    }

    @ConfigItem(
        keyName = "fallbackPassword",
        name = "Fallback password",
        description = "Only used when launch account props are empty",
        section = fallbackSection,
        position = 1,
        secret = true
    )
    default String fallbackPassword() {
        return "";
    }
}
