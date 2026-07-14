/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 */
package net.runelite.client.plugins.svc.runecrafting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(value="orionRunecrafting")
public interface RunecraftingConfig
extends Config {
    @ConfigSection(name="General", description="Item & object IDs for the runecrafting loop", position=0)
    public static final String generalSection = "general";
    @ConfigSection(name="Banking", description="What to deposit and withdraw", position=1)
    public static final String bankingSection = "banking";
    @ConfigSection(name="Advanced", description="Timing & robustness tuning", position=2, closedByDefault=true)
    public static final String advancedSection = "advanced";

    @ConfigItem(keyName="talismanId", name="Talisman item ID", description="Item ID of the talisman to click to teleport to the rune altar", section="general", position=0)
    default public int talismanId() {
        return 1438;
    }

    @ConfigItem(keyName="talismanAction", name="Talisman action", description="Menu option used on the talisman to teleport (e.g. \"Teleport\")", section="general", position=1)
    default public String talismanAction() {
        return "Teleport";
    }

    @ConfigItem(keyName="altarObjectId", name="Rune altar object ID", description="Object ID of the rune altar to click once teleported. Use the Entity Inspector to find it.", section="general", position=2)
    default public int altarObjectId() {
        return 34760;
    }

    @ConfigItem(keyName="altarAction", name="Altar action", description="Menu option used on the altar to craft runes", section="general", position=3)
    default public String altarAction() {
        return "Craft-rune";
    }

    @ConfigItem(keyName="essenceId", name="Essence item ID", description="Item ID of the (pure/rune) essence consumed at the altar. The bot verifies this is in the inventory before crafting; if it's missing it teleports back to the bank instead of spinning forever on an altar it can't use. Pure essence = 7936, rune essence = 1436.", section="general", position=4)
    default public int essenceId() {
        return 7936;
    }

    @ConfigItem(keyName="tabletId", name="Teleport tablet item ID", description="Item ID of the teleport tablet to click to return to the bank. Tablets are consumed each trip \u2014 remember to withdraw a fresh one.", section="general", position=5)
    default public int tabletId() {
        return 8014;
    }

    @ConfigItem(keyName="tabletAction", name="Tablet action", description="Menu option used on the teleport tablet (e.g. \"Break\")", section="general", position=6)
    default public String tabletAction() {
        return "Break";
    }

    @ConfigItem(keyName="bankBoothId", name="Bank booth object ID", description="Object ID of the bank booth/chest to open at the bank. Use the Entity Inspector to find it.", section="general", position=7)
    default public int bankBoothId() {
        return 10355;
    }

    @ConfigItem(keyName="bankAction", name="Bank action", description="Menu option used to open the bank (e.g. \"Bank\" or \"Use\")", section="general", position=8)
    default public String bankAction() {
        return "Bank";
    }

    @ConfigItem(keyName="depositIds", name="Deposit IDs", description="Comma-separated item IDs to deposit-all (the runes you crafted), e.g. \"556\"", section="banking", position=0)
    default public String depositIds() {
        return "";
    }

    @ConfigItem(keyName="withdrawItems", name="Withdraw (id:qty)", description="Comma-separated id:quantity pairs. Use 'all' to fill the inventory. Fixed quantities are withdrawn first, then 'all' fills the rest. Example: \"8014:1, 7936:all\" (1 tablet + fill with pure essence).", section="banking", position=1)
    default public String withdrawItems() {
        return "";
    }

    @ConfigItem(keyName="arrivalTimeoutTicks", name="Arrival timeout (ticks)", description="Max game ticks to wait for the altar or bank to appear before re-attempting the teleport (1 tick = 600ms)", section="advanced", position=0)
    @Range(min=2, max=30)
    default public int arrivalTimeoutTicks() {
        return 8;
    }

    @ConfigItem(keyName="craftTimeoutTicks", name="Craft timeout (ticks)", description="Max game ticks to wait for a Runecraft XP gain before re-clicking the altar", section="advanced", position=1)
    @Range(min=2, max=20)
    default public int craftTimeoutTicks() {
        return 5;
    }

    @ConfigItem(keyName="bankRetryTimeoutTicks", name="Bank retry timeout (ticks)", description="If the bank booth stays missing this long after teleporting, re-break the teleport tablet. The tablet click can silently fail (registers client-side but no teleport fires), leaving the bot stuck on \"Bank booth not found\". 1 tick = 600ms; ~7 ticks \u2248 4 seconds.", section="advanced", position=2)
    @Range(min=2, max=30)
    default public int bankRetryTimeoutTicks() {
        return 7;
    }
}

