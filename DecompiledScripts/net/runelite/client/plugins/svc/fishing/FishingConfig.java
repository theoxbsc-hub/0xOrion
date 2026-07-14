/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 */
package net.runelite.client.plugins.svc.fishing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.svc.fishing.FishingMode;

@ConfigGroup(value="orionFishingBot")
public interface FishingConfig
extends Config {
    @ConfigSection(name="Fishing", description="Fishing settings", position=0)
    public static final String fishingSection = "fishing";

    @ConfigItem(keyName="fishingSpotId", name="Fishing Spot NPC ID", description="NPC ID of the fishing spot to click (use Entity Inspector to find it)", section="fishing", position=0)
    default public int fishingSpotId() {
        return 1530;
    }

    @ConfigItem(keyName="fishItemId1", name="Fish Item ID 1", description="Item ID of the primary fish to bank/drop (use Entity Inspector to find it)", section="fishing", position=1)
    default public int fishItemId1() {
        return 317;
    }

    @ConfigItem(keyName="fishItemId2", name="Fish Item ID 2", description="Item ID of a second fish type (e.g. some spots give two kinds). Set to 0 to ignore.", section="fishing", position=2)
    default public int fishItemId2() {
        return 0;
    }

    @ConfigItem(keyName="fishingMode", name="Inventory Action", description="What to do when inventory is full: Bank (deposit barrel) or Drop (drop fish on ground)", section="fishing", position=3)
    default public FishingMode fishingMode() {
        return FishingMode.BANK;
    }

    @ConfigItem(keyName="fishingAction", name="Fishing Action", description="Right-click action on the fishing spot (Fish, Bait, Lure, Cage, Harpoon)", section="fishing", position=4)
    default public String fishingAction() {
        return "Fish";
    }

    @ConfigItem(keyName="barrelAction", name="Barrel Action", description="Right-click action on the deposit barrel (only used in Bank mode)", section="fishing", position=5)
    default public String barrelAction() {
        return "Deposit";
    }

    @ConfigItem(keyName="showOverlay", name="Show Overlay", description="Show the status overlay", section="fishing", position=6)
    default public boolean showOverlay() {
        return true;
    }
}

