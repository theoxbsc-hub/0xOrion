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
package net.runelite.client.plugins.svc.hunter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.svc.hunter.ChinchompaType;
import net.runelite.client.plugins.svc.hunter.HunterMethod;

@ConfigGroup(value="orionHunterBot")
public interface HunterConfig
extends Config {
    @ConfigSection(name="General", description="Core hunter settings", position=0)
    public static final String generalSection = "general";
    @ConfigSection(name="Birds", description="Bird targeting & walking", position=1)
    public static final String birdsSection = "birds";
    @ConfigSection(name="Chinchompas", description="Box trap method \u2014 chinchompa targeting", position=2)
    public static final String chinchompaSection = "chinchompa";
    @ConfigSection(name="Loot", description="Bird snare \u2014 what to drop / keep", position=3)
    public static final String lootSection = "loot";
    @ConfigSection(name="Overlay", description="Status display", position=4)
    public static final String overlaySection = "overlay";

    @ConfigItem(keyName="method", name="Method", description="Which hunter method to run. Bird snare trains on birds; Box trap hunts chinchompas (configure the chinchompa in the Chinchompas section below).", section="general", position=0)
    default public HunterMethod method() {
        return HunterMethod.BIRD_SNARE;
    }

    @Range(min=1, max=5)
    @ConfigItem(keyName="maxTraps", name="Max Traps / Boxes", description="How many traps (or box traps) to keep laid at once. The game also caps this by Hunter level (e.g. 3 boxes at lvl 40, 4 at 60, 5 at 80) \u2014 the bot auto-detects that cap and won't fight it.", section="general", position=1)
    default public int maxTraps() {
        return 3;
    }

    @Range(min=1, max=3)
    @ConfigItem(keyName="clusterRadius", name="Cluster Radius", description="Traps are laid in a square cluster of this radius around the first trap so they can all be watched without running around. 1 = a 3x3 area, 2 = 5x5, 3 = 7x7.", section="general", position=2)
    default public int clusterRadius() {
        return 1;
    }

    @ConfigItem(keyName="birdNames", name="Bird Names", description="Comma-separated, case-insensitive substrings of the bird NPC names to hunt (e.g. \"Crimson swift, Golden warbler\"). Used to detect birds nearby and to walk to them.", section="birds", position=0)
    default public String birdNames() {
        return "Crimson swift, Golden warbler, Copper longtail, Cerulean twitch, Tropical wagtail";
    }

    @Range(min=2, max=20)
    @ConfigItem(keyName="birdSearchRadius", name="Nearby Radius", description="A bird must be within this many tiles of the trap cluster to count as \"nearby\". If none are nearby (and no traps are out) the bot walks to the closest bird.", section="birds", position=1)
    default public int birdSearchRadius() {
        return 10;
    }

    @ConfigItem(keyName="walkToBirds", name="Walk To Birds", description="When there are no traps out and no birds nearby, walk to the nearest bird. Disable to stay put and only hunt birds that come to you.", section="birds", position=2)
    default public boolean walkToBirds() {
        return true;
    }

    @ConfigItem(keyName="type", name="Chinchompa", description="Which chinchompa to hunt with the Box trap method. All three use box traps \u2014 just stand in the relevant area. Grey is fully confirmed; Red/Black are matched by NPC name (set 'NPC Name Override' if your server names them differently).", section="chinchompa", position=0)
    default public ChinchompaType type() {
        return ChinchompaType.GREY;
    }

    @ConfigItem(keyName="npcNameOverride", name="NPC Name Override", description="Optional. Comma-separated, case-insensitive substrings of the chinchompa NPC name to scan for. Leave blank to use the built-in name for the selected chinchompa. Use the Entity Inspector to read the exact NPC name if Red/Black aren't being found.", section="chinchompa", position=1)
    default public String npcNameOverride() {
        return "";
    }

    @Range(min=2, max=20)
    @ConfigItem(keyName="chinSearchRadius", name="Nearby Radius", description="A chinchompa must be within this many tiles of the trap cluster to count as \"nearby\". If none are nearby (and no traps are out) the bot walks to the closest one.", section="chinchompa", position=2)
    default public int chinSearchRadius() {
        return 12;
    }

    @ConfigItem(keyName="walkToChins", name="Walk To Chinchompas", description="When there are no box traps out and no chinchompas nearby, walk to the nearest one. Disable to stay put and only trap chinchompas that wander to you.", section="chinchompa", position=3)
    default public boolean walkToChins() {
        return true;
    }

    @ConfigItem(keyName="avoidOtherPlayers", name="Avoid Other Players", description="Never lay a box trap on a tile another player is standing on. (The bot already only ever collects traps it laid itself, so it never touches another player's boxes.)", section="chinchompa", position=4)
    default public boolean avoidOtherPlayers() {
        return true;
    }

    @ConfigItem(keyName="dropItems", name="Drop Items", description="Comma-separated, case-insensitive substrings of items to DROP after checking a trap (e.g. \"Raw bird meat, Bones\"). Leave blank to drop nothing. The bird snare itself is never dropped.", section="loot", position=0)
    default public String dropItems() {
        return "";
    }

    @ConfigItem(keyName="keepItems", name="Keep Items", description="Comma-separated substrings of items to never drop, even if they match Drop Items.", section="loot", position=1)
    default public String keepItems() {
        return "";
    }

    @ConfigItem(keyName="showOverlay", name="Show Overlay", description="Show the live status overlay.", section="overlay", position=0)
    default public boolean showOverlay() {
        return true;
    }
}

