/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Button
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 */
package net.unethicalite.client.config;

import java.util.Set;
import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.unethicalite.api.movement.pathfinder.model.poh.HousePortal;
import net.unethicalite.api.movement.pathfinder.model.poh.JewelryBox;
import net.unethicalite.client.managers.interaction.InteractMethod;
import net.unethicalite.client.managers.interaction.MouseBehavior;

@ConfigGroup(value="unethicalite")
public interface UnethicaliteConfig
extends Config {
    public static final String CONFIG_GROUP = "unethicalite";
    @ConfigSection(name="Pathfinder/Regions", position=1, description="")
    public static final String pathfinderSection = "Pathfinder/Regions";
    @ConfigSection(name="Minimal Mode", position=2, description="")
    public static final String minimalSection = "Minimal Mode";
    @ConfigSection(name="Data collection", position=3, description="")
    public static final String dataCollection = "Data collection";
    @ConfigSection(name="Other settings", position=3, description="")
    public static final String otherSettings = "Other settings";

    @ConfigItem(keyName="interactMethod", name="Interact method", description="Interaction method", hidden=true, position=0)
    default public InteractMethod interactMethod() {
        return InteractMethod.MOUSE_EVENTS;
    }

    @ConfigItem(keyName="naturalMouse", name="Natural mouse", description="Uses the 'natural mouse' algorithm to move and click", hidden=true)
    default public boolean naturalMouse() {
        return false;
    }

    @ConfigItem(keyName="disableMouse", name="Disable manual input", description="Disables manual input if a script/looped plugin is running", hidden=true)
    default public boolean disableMouse() {
        return false;
    }

    @ConfigItem(keyName="mouseBehavior", name="Mouse behavior", description="Type of clicks to send to the server", hidden=true)
    default public MouseBehavior mouseBehavior() {
        return MouseBehavior.CLICKBOXES;
    }

    @ConfigItem(position=22, keyName="neverLog", name="Neverlog", description="Skips idle checks", hidden=true)
    default public boolean neverLog() {
        return false;
    }

    @ConfigItem(keyName="useTransports", name="Use transports", description="Include transport nodes when calculating paths", position=2, section="Pathfinder/Regions")
    default public boolean useTransports() {
        return true;
    }

    @ConfigItem(keyName="useTeleports", name="Use teleports", description="Include teleportation when calculating paths", position=2, section="Pathfinder/Regions")
    default public boolean useTeleports() {
        return true;
    }

    @ConfigItem(keyName="avoidWilderness", name="Avoid Wilderness", description="Avoids walking in the wilderness if the destination is not in the wildy", position=3, section="Pathfinder/Regions")
    default public boolean avoidWilderness() {
        return false;
    }

    @ConfigItem(keyName="downloadCollisionData", name="Download collision data", description="Downloads new collision data from the server and updates the currently used collision map", position=5, section="Pathfinder/Regions")
    default public Button download() {
        return new Button();
    }

    @ConfigItem(keyName="localCollisionData", name="Load cached collision data", description="Loads the locally stored collision data", position=6, section="Pathfinder/Regions")
    default public Button reload() {
        return new Button();
    }

    @ConfigItem(keyName="usePoh", name="Use POH", description="", position=3, section="Pathfinder/Regions")
    default public boolean usePoh() {
        return false;
    }

    @ConfigItem(keyName="hasMountedGlory", name="Mounted Glory", description="", position=4, section="Pathfinder/Regions")
    default public boolean hasMountedGlory() {
        return false;
    }

    @ConfigItem(keyName="hasMountedDigsitePendant", name="Mounted Digsite Pendant", description="", position=4, section="Pathfinder/Regions")
    default public boolean hasMountedDigsitePendant() {
        return false;
    }

    @ConfigItem(keyName="hasMountedMythicalCape", name="Mounted Mythical Cape", description="", position=4, section="Pathfinder/Regions")
    default public boolean hasMountedMythicalCape() {
        return false;
    }

    @ConfigItem(keyName="hasMountedXericsTalisman", name="Mounted Xerics Talisman", description="", position=4, section="Pathfinder/Regions")
    default public boolean hasMountedXericsTalisman() {
        return false;
    }

    @ConfigItem(keyName="hasJewelryBox", name="Jewelry Box", description="", position=4, section="Pathfinder/Regions")
    default public JewelryBox hasJewelryBox() {
        return JewelryBox.NONE;
    }

    @ConfigItem(keyName="useEquipmentJewellery", name="Use jewellery from equipment", description="", position=4, section="Pathfinder/Regions")
    default public boolean useEquipmentJewellery() {
        return true;
    }

    @ConfigItem(keyName="useMinigameTeleports", name="Use minigames teleports", description="", position=4, section="Pathfinder/Regions")
    default public boolean useMinigameTeleports() {
        return true;
    }

    @ConfigItem(keyName="housePortals", name="House Portals", description="", position=4, section="Pathfinder/Regions")
    default public Set<HousePortal> housePortals() {
        return Set.of();
    }

    @Range(min=1, max=50)
    @ConfigItem(position=0, keyName="fpsLimit", name="FPS limit", description="FPS limit", section="Minimal Mode")
    default public int fpsLimit() {
        return 15;
    }

    @ConfigItem(position=1, keyName="renderOff", name="Render off", description="Render off", section="Minimal Mode")
    default public boolean renderOff() {
        return false;
    }

    @ConfigItem(position=2, keyName="minimized", name="Start minimized", description="Start minimized", section="Minimal Mode")
    default public boolean minimized() {
        return false;
    }

    @ConfigItem(position=0, keyName="session", name="Send session (online users)", description="Sends your online status & your client mode to the backend.", section="Data collection")
    default public boolean session() {
        return true;
    }

    @ConfigItem(position=0, keyName="regions", name="Send region", description="Sends your local region data to the backend.", section="Data collection")
    default public boolean regions() {
        return true;
    }

    @ConfigItem(position=0, keyName="disableGroups", name="Disable Groups/Party", description="Disables the Party functionality (needs a restart).", section="Other settings")
    default public boolean disableGroups() {
        return false;
    }
}

