/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 *  net.runelite.api.Quest
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.movement.pathfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Quest;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.pathfinder.model.MovementConstants;
import net.unethicalite.api.movement.pathfinder.model.Teleport;
import net.unethicalite.api.movement.pathfinder.model.TeleportItem;
import net.unethicalite.api.movement.pathfinder.model.TeleportSpell;
import net.unethicalite.api.movement.pathfinder.model.poh.HousePortal;
import net.unethicalite.api.quests.Quests;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Minigames;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import net.unethicalite.client.managers.RegionManager;

public class TeleportLoader {
    private static Pattern WILDY_PATTERN = Pattern.compile("Okay, teleport to level [\\d,]* Wilderness\\.");
    private static List<Teleport> LAST_TELEPORT_LIST = new ArrayList<Teleport>();

    public static List<Teleport> buildTeleports() {
        ArrayList<Teleport> teleports = new ArrayList<Teleport>();
        teleports.addAll(LAST_TELEPORT_LIST);
        teleports.addAll(TeleportLoader.buildTimedTeleports());
        return teleports;
    }

    private static List<Teleport> buildTimedTeleports() {
        return GameThread.invokeLater(() -> {
            ArrayList<Teleport> teleports = new ArrayList<Teleport>();
            if (Worlds.inMembersWorld() && Game.getWildyLevel() == 0 && RegionManager.useMinigameTeleports() && Minigames.canTeleport()) {
                for (Enum enum_ : Minigames.Destination.values()) {
                    if (!((Minigames.Destination)enum_).canUse()) continue;
                    teleports.add(new Teleport(((Minigames.Destination)enum_).getLocation(), 2, () -> TeleportLoader.lambda$buildTimedTeleports$0((Minigames.Destination)enum_)));
                }
            }
            if (Game.getWildyLevel() <= 20) {
                for (Enum enum_ : TeleportSpell.values()) {
                    if (!((TeleportSpell)enum_).canCast() || ((TeleportSpell)enum_).getPoint() == null || ((TeleportSpell)enum_).getPoint().distanceTo(Players.getLocal().getWorldLocation()) <= 50) continue;
                    teleports.add(new Teleport(((TeleportSpell)enum_).getPoint(), 5, () -> TeleportLoader.lambda$buildTimedTeleports$1((TeleportSpell)enum_)));
                }
            }
            return teleports;
        });
    }

    public static void refreshTeleports() {
        GameThread.invoke(() -> {
            ArrayList<Teleport> teleports = new ArrayList<Teleport>();
            if (Worlds.inMembersWorld()) {
                for (TeleportItem tele : TeleportItem.values()) {
                    if (!tele.canUse() || tele.getDestination().distanceTo(Players.getLocal().getWorldLocation()) <= 20) continue;
                    switch (tele) {
                        case ROYAL_SEED_POD: {
                            if (Game.getWildyLevel() > 30) break;
                            teleports.add(TeleportLoader.itemTeleport(tele));
                        }
                    }
                    if (Game.getWildyLevel() > 20) continue;
                    teleports.add(TeleportLoader.itemTeleport(tele));
                }
                if (Game.getWildyLevel() <= 20) {
                    if (TeleportLoader.ringOfDueling()) {
                        teleports.add(new Teleport(new WorldPoint(3315, 3235, 0), 6, () -> TeleportLoader.jewelryTeleport("PvP Arena", MovementConstants.RING_OF_DUELING)));
                        teleports.add(new Teleport(new WorldPoint(2440, 3090, 0), 2, () -> TeleportLoader.jewelryTeleport("Castle Wars", MovementConstants.RING_OF_DUELING)));
                        teleports.add(new Teleport(new WorldPoint(3151, 3635, 0), 2, () -> TeleportLoader.jewelryTeleport("Ferox Enclave", MovementConstants.RING_OF_DUELING)));
                    }
                    if (TeleportLoader.gamesNecklace()) {
                        teleports.add(new Teleport(new WorldPoint(2898, 3553, 0), 2, () -> TeleportLoader.jewelryTeleport("Burthorpe", MovementConstants.GAMES_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(2520, 3571, 0), 6, () -> TeleportLoader.jewelryTeleport("Barbarian Outpost", MovementConstants.GAMES_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(2964, 4382, 2), 2, () -> TeleportLoader.jewelryTeleport("Corporeal Beast", MovementConstants.GAMES_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(3244, 9501, 2), 2, () -> TeleportLoader.jewelryTeleport("Tears of Guthix", MovementConstants.GAMES_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(1624, 3938, 0), 1, () -> TeleportLoader.jewelryTeleport("Wintertodt Camp", MovementConstants.GAMES_NECKLACE)));
                    }
                    if (TeleportLoader.necklaceOfPassage()) {
                        teleports.add(new Teleport(new WorldPoint(3114, 3179, 0), 2, () -> TeleportLoader.jewelryTeleport("Wizards' Tower", MovementConstants.NECKLACE_OF_PASSAGE)));
                        teleports.add(new Teleport(new WorldPoint(2430, 3348, 0), 2, () -> TeleportLoader.jewelryTeleport("The Outpost", MovementConstants.NECKLACE_OF_PASSAGE)));
                        teleports.add(new Teleport(new WorldPoint(3405, 3157, 0), 2, () -> TeleportLoader.jewelryTeleport("Eagle's Eyrie", MovementConstants.NECKLACE_OF_PASSAGE)));
                    }
                    if (TeleportLoader.xericsTalisman()) {
                        teleports.add(new Teleport(new WorldPoint(1576, 3530, 0), 6, () -> TeleportLoader.jewelryTeleport("Xeric's Lookout", MovementConstants.XERICS_TALISMAN)));
                        teleports.add(new Teleport(new WorldPoint(1752, 3566, 0), 6, () -> TeleportLoader.jewelryTeleport("Xeric's Glade", MovementConstants.XERICS_TALISMAN)));
                        teleports.add(new Teleport(new WorldPoint(1504, 3817, 0), 6, () -> TeleportLoader.jewelryTeleport("Xeric's Inferno", MovementConstants.XERICS_TALISMAN)));
                        if (Quests.isFinished(Quest.ARCHITECTURAL_ALLIANCE)) {
                            teleports.add(new Teleport(new WorldPoint(1640, 3674, 0), 6, () -> TeleportLoader.jewelryTeleport("Xeric's Heart", MovementConstants.XERICS_TALISMAN)));
                        }
                    }
                    if (TeleportLoader.digsitePendant()) {
                        teleports.add(new Teleport(new WorldPoint(3341, 3445, 0), 6, () -> TeleportLoader.jewelryTeleport("Digsite", MovementConstants.DIGSITE_PENDANT)));
                        teleports.add(new Teleport(new WorldPoint(3764, 3869, 1), 6, () -> TeleportLoader.jewelryTeleport("Fossil Island", MovementConstants.DIGSITE_PENDANT)));
                        if (Quests.isFinished(Quest.DRAGON_SLAYER_II)) {
                            teleports.add(new Teleport(new WorldPoint(3549, 10456, 0), 6, () -> TeleportLoader.jewelryTeleport("Lithkren", MovementConstants.DIGSITE_PENDANT)));
                        }
                    }
                }
                if (Game.getWildyLevel() <= 30) {
                    if (TeleportLoader.combatBracelet()) {
                        teleports.add(new Teleport(new WorldPoint(2882, 3548, 0), 2, () -> TeleportLoader.jewelryTeleport("Warriors' Guild", MovementConstants.COMBAT_BRACELET)));
                        teleports.add(new Teleport(new WorldPoint(3191, 3367, 0), 2, () -> TeleportLoader.jewelryTeleport("Champions' Guild", MovementConstants.COMBAT_BRACELET)));
                        teleports.add(new Teleport(new WorldPoint(3052, 3488, 0), 2, () -> TeleportLoader.jewelryTeleport("Monastery", MovementConstants.COMBAT_BRACELET)));
                        teleports.add(new Teleport(new WorldPoint(2655, 3441, 0), 2, () -> TeleportLoader.jewelryTeleport("Ranging Guild", MovementConstants.COMBAT_BRACELET)));
                    }
                    if (TeleportLoader.skillsNecklace()) {
                        teleports.add(new Teleport(new WorldPoint(2611, 3390, 0), 6, () -> TeleportLoader.jewelryPopupTeleport("Fishing Guild", MovementConstants.SKILLS_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(3050, 9763, 0), 6, () -> TeleportLoader.jewelryPopupTeleport("Mining Guild", MovementConstants.SKILLS_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(2933, 3295, 0), 6, () -> TeleportLoader.jewelryPopupTeleport("Crafting Guild", MovementConstants.SKILLS_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(3143, 3440, 0), 6, () -> TeleportLoader.jewelryPopupTeleport("Cooking Guild", MovementConstants.SKILLS_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(1662, 3505, 0), 6, () -> TeleportLoader.jewelryPopupTeleport("Woodcutting Guild", MovementConstants.SKILLS_NECKLACE)));
                        teleports.add(new Teleport(new WorldPoint(1249, 3718, 0), 6, () -> TeleportLoader.jewelryPopupTeleport("Farming Guild", MovementConstants.SKILLS_NECKLACE)));
                    }
                    if (TeleportLoader.ringOfWealth()) {
                        teleports.add(new Teleport(new WorldPoint(3163, 3478, 0), 2, () -> TeleportLoader.jewelryTeleport("Grand Exchange", MovementConstants.RING_OF_WEALTH)));
                        teleports.add(new Teleport(new WorldPoint(2996, 3375, 0), 2, () -> TeleportLoader.jewelryTeleport("Falador", MovementConstants.RING_OF_WEALTH)));
                        if (Quests.isFinished(Quest.THRONE_OF_MISCELLANIA)) {
                            teleports.add(new Teleport(new WorldPoint(2538, 3863, 0), 2, () -> TeleportLoader.jewelryTeleport("Miscellania", MovementConstants.RING_OF_WEALTH)));
                        }
                        if (Quests.isFinished(Quest.BETWEEN_A_ROCK)) {
                            teleports.add(new Teleport(new WorldPoint(2828, 10166, 0), 2, () -> TeleportLoader.jewelryTeleport("Miscellania", MovementConstants.RING_OF_WEALTH)));
                        }
                    }
                    if (TeleportLoader.amuletOfGlory()) {
                        teleports.add(new Teleport(new WorldPoint(3087, 3496, 0), 0, () -> TeleportLoader.jewelryTeleport("Edgeville", MovementConstants.AMULET_OF_GLORY)));
                        teleports.add(new Teleport(new WorldPoint(2918, 3176, 0), 0, () -> TeleportLoader.jewelryTeleport("Karamja", MovementConstants.AMULET_OF_GLORY)));
                        teleports.add(new Teleport(new WorldPoint(3105, 3251, 0), 0, () -> TeleportLoader.jewelryTeleport("Draynor Village", MovementConstants.AMULET_OF_GLORY)));
                        teleports.add(new Teleport(new WorldPoint(3293, 3163, 0), 0, () -> TeleportLoader.jewelryTeleport("Al Kharid", MovementConstants.AMULET_OF_GLORY)));
                    }
                    if (TeleportLoader.burningAmulet()) {
                        teleports.add(new Teleport(new WorldPoint(3235, 3636, 0), 5, () -> TeleportLoader.jewelryWildernessTeleport("Chaos Temple", MovementConstants.BURNING_AMULET)));
                        teleports.add(new Teleport(new WorldPoint(3038, 3651, 0), 5, () -> TeleportLoader.jewelryWildernessTeleport("Bandit Camp", MovementConstants.BURNING_AMULET)));
                        teleports.add(new Teleport(new WorldPoint(3028, 3842, 0), 5, () -> TeleportLoader.jewelryWildernessTeleport("Lava Maze", MovementConstants.BURNING_AMULET)));
                    }
                    if (TeleportLoader.slayerRing()) {
                        teleports.add(new Teleport(new WorldPoint(2432, 3423, 0), 2, () -> TeleportLoader.slayerRingTeleport("Stronghold Slayer Cave", MovementConstants.SLAYER_RING)));
                        teleports.add(new Teleport(new WorldPoint(3422, 3537, 0), 2, () -> TeleportLoader.slayerRingTeleport("Slayer Tower", MovementConstants.SLAYER_RING)));
                        teleports.add(new Teleport(new WorldPoint(2802, 10000, 0), 2, () -> TeleportLoader.slayerRingTeleport("Fremennik Slayer Dungeon", MovementConstants.SLAYER_RING)));
                        teleports.add(new Teleport(new WorldPoint(3185, 4601, 0), 2, () -> TeleportLoader.slayerRingTeleport("Tarn's Lair", MovementConstants.SLAYER_RING)));
                        if (Quests.isFinished(Quest.MOURNINGS_END_PART_II)) {
                            teleports.add(new Teleport(new WorldPoint(2028, 4636, 0), 2, () -> TeleportLoader.slayerRingTeleport("Dark Beasts", MovementConstants.SLAYER_RING)));
                        }
                    }
                }
                if (RegionManager.usePoh() && (TeleportLoader.canEnterHouse() || TileObjects.getNearest(4525) != null)) {
                    if (RegionManager.hasMountedGlory()) {
                        teleports.add(TeleportLoader.mountedPohTeleport(new WorldPoint(3087, 3496, 0), 13523, "Edgeville"));
                        teleports.add(TeleportLoader.mountedPohTeleport(new WorldPoint(2918, 3176, 0), 13523, "Karamja"));
                        teleports.add(TeleportLoader.mountedPohTeleport(new WorldPoint(3105, 3251, 0), 13523, "Draynor Village"));
                        teleports.add(TeleportLoader.mountedPohTeleport(new WorldPoint(3293, 3163, 0), 13523, "Al Kharid"));
                    }
                    if (RegionManager.hasMountedDigsitePendant()) {
                        teleports.add(TeleportLoader.pohDigsitePendantTeleport(new WorldPoint(3341, 3445, 0), 1));
                        teleports.add(TeleportLoader.pohDigsitePendantTeleport(new WorldPoint(3766, 3870, 1), 2));
                        if (Quests.isFinished(Quest.DRAGON_SLAYER_II)) {
                            teleports.add(TeleportLoader.pohDigsitePendantTeleport(new WorldPoint(3549, 10456, 0), 3));
                        }
                    }
                    switch (RegionManager.hasJewelryBox()) {
                        case ORNATE: {
                            if (Quests.isFinished(Quest.THRONE_OF_MISCELLANIA)) {
                                teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2538, 3863, 0), 'j'));
                            }
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3163, 3478, 0), 'k'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2996, 3375, 0), 'l'));
                            if (Quests.isFinished(Quest.BETWEEN_A_ROCK)) {
                                teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2828, 10166, 0), 'm'));
                            }
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3087, 3496, 0), 'n'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2918, 3176, 0), 'o'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3105, 3251, 0), 'p'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3293, 3163, 0), 'q'));
                        }
                        case FANCY: {
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2882, 3548, 0), '9'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3191, 3367, 0), 'a'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3052, 3488, 0), 'b'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2655, 3441, 0), 'c'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2611, 3390, 0), 'd'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3050, 9763, 0), 'e'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2933, 3295, 0), 'f'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3143, 3440, 0), 'g'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(1662, 3505, 0), 'h'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(1249, 3718, 0), 'i'));
                        }
                        case BASIC: {
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3315, 3235, 0), '1'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2440, 3090, 0), '2'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3151, 3635, 0), '3'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2898, 3553, 0), '4'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2520, 3571, 0), '5'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(2964, 4382, 2), '6'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(3244, 9501, 2), '7'));
                            teleports.add(TeleportLoader.pohWidgetTeleport(new WorldPoint(1624, 3938, 0), '8'));
                            break;
                        }
                    }
                    List<Teleport> nexusTeleports = TeleportLoader.getNexusTeleports();
                    teleports.addAll(nexusTeleports);
                    RegionManager.getHousePortals().stream().filter(housePortal -> nexusTeleports.stream().noneMatch(teleport -> teleport.getDestination().equals((Object)housePortal.getDestination()))).forEach(housePortal -> teleports.add(TeleportLoader.pohPortalTeleport(housePortal)));
                }
            }
            LAST_TELEPORT_LIST.clear();
            LAST_TELEPORT_LIST.addAll(teleports);
        });
    }

    public static boolean canEnterHouse() {
        return Inventory.contains(8013) || TeleportSpell.TELEPORT_TO_HOUSE.canCast();
    }

    public static void enterHouse() {
        if (TeleportSpell.TELEPORT_TO_HOUSE.canCast()) {
            SpellBook.Standard.TELEPORT_TO_HOUSE.cast();
            return;
        }
        Item teleTab = Inventory.getFirst(8013);
        if (teleTab != null) {
            teleTab.interact("Break");
        }
    }

    public static void jewelryTeleport(String target, int ... ids) {
        Item inv = Inventory.getFirst(ids);
        if (inv != null) {
            if (!Dialog.isViewingOptions()) {
                inv.interact("Rub");
                Time.sleepTicksUntil(Dialog::isViewingOptions, 2);
                return;
            }
            Dialog.chooseOption(target);
            return;
        }
        if (!RegionManager.useEquipmentJewellery()) {
            return;
        }
        Item equipped = Equipment.getFirst(ids);
        if (equipped != null) {
            equipped.interact(target);
        }
    }

    public static Teleport pohPortalTeleport(HousePortal housePortal) {
        return new Teleport(housePortal.getDestination(), 10, () -> {
            if (!Players.getLocal().isIdle() || Static.getClient().getGameState() == GameState.LOADING) {
                return;
            }
            TileObject portal = TileObjects.getNearest(housePortal.getPortalName());
            if (portal != null) {
                portal.interact(new String[]{"Enter", "Varrock", "Seers' Village", "Watchtower"});
                return;
            }
            TeleportLoader.enterHouse();
        });
    }

    public static List<Teleport> getNexusTeleports() {
        int[] varbitArray;
        ArrayList<Teleport> result = new ArrayList<Teleport>();
        block33: for (int varbit : varbitArray = new int[]{6672, 6673, 6674, 6675, 6676, 6677, 6678, 6679, 6680, 6681, 6682, 6683, 6684, 6685, 6686, 6568, 6569, 6582, 10092, 10093, 10094, 10095, 10096, 10097, 10098, 10099, 10100, 10101, 10102, 10103}) {
            int id = Vars.getBit(varbit);
            switch (id) {
                case 0: {
                    continue block33;
                }
                case 1: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.VARROCK));
                    continue block33;
                }
                case 2: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.LUMBRIDGE));
                    continue block33;
                }
                case 3: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.FALADOR));
                    continue block33;
                }
                case 4: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.CAMELOT));
                    continue block33;
                }
                case 5: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.EAST_ARDOUGNE));
                    continue block33;
                }
                case 6: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.WATCHTOWER));
                    continue block33;
                }
                case 7: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.SENNTISTEN));
                    continue block33;
                }
                case 8: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.MARIM));
                    continue block33;
                }
                case 9: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.KHARYRLL));
                    continue block33;
                }
                case 10: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.LUNAR_ISLE));
                    continue block33;
                }
                case 11: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.KOUREND));
                    continue block33;
                }
                case 12: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.WATERBIRTH_ISLAND));
                    continue block33;
                }
                case 13: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.FISHING_GUILD));
                    continue block33;
                }
                case 14: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.ANNAKARL));
                    continue block33;
                }
                case 15: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.TROLL_STRONGHOLD));
                    continue block33;
                }
                case 16: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.CATHERBY));
                    continue block33;
                }
                case 17: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.GHORROCK));
                    continue block33;
                }
                case 18: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.CARRALLANGAR));
                    continue block33;
                }
                case 19: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.WEISS));
                    continue block33;
                }
                case 20: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.ARCEUUS_LIBRARY));
                    continue block33;
                }
                case 21: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.DRAYNOR_MANOR));
                    continue block33;
                }
                case 22: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.BATTLEFRONT));
                    continue block33;
                }
                case 23: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.MIND_ALTAR));
                    continue block33;
                }
                case 24: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.SALVE_GRAVEYARD));
                    continue block33;
                }
                case 25: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.FENKENSTRAINS_CASTLE));
                    continue block33;
                }
                case 26: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.WEST_ARDOUGNE));
                    continue block33;
                }
                case 27: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.HARMONY_ISLAND));
                    continue block33;
                }
                case 28: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.CEMETERY));
                    continue block33;
                }
                case 29: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.BARROWS));
                    continue block33;
                }
                case 30: {
                    result.add(TeleportLoader.pohNexusTeleport(HousePortal.APE_ATOLL_DUNGEON));
                }
            }
        }
        return result;
    }

    public static Teleport pohNexusTeleport(HousePortal housePortal) {
        WorldPoint destination = housePortal.getDestination();
        return new Teleport(destination, 10, () -> {
            if (!Players.getLocal().isIdle() || Static.getClient().getGameState() == GameState.LOADING) {
                return;
            }
            TileObject nexusPortal = TileObjects.getNearest("Portal Nexus");
            if (nexusPortal == null) {
                TeleportLoader.enterHouse();
                return;
            }
            Widget teleportInterface = Widgets.get(17, 12);
            if (teleportInterface == null || teleportInterface.isHidden()) {
                nexusPortal.interact("Teleport Menu");
                return;
            }
            Widget[] teleportChildren = teleportInterface.getDynamicChildren();
            if (teleportChildren == null || teleportChildren.length == 0) {
                return;
            }
            Optional<Widget> optionalTeleportWidget = Arrays.stream(teleportChildren).filter(Objects::nonNull).filter(widget -> widget.getText() != null).filter(widget -> widget.getText().contains(housePortal.getNexusTarget())).findFirst();
            if (optionalTeleportWidget.isEmpty()) {
                return;
            }
            Widget teleportWidget = optionalTeleportWidget.get();
            String teleportChar = teleportWidget.getText().substring(12, 13);
            Keyboard.type(teleportChar);
        });
    }

    public static void jewelryPopupTeleport(String target, int ... ids) {
        Item inv = Inventory.getFirst(ids);
        if (inv != null) {
            Widget baseWidget = Widgets.get(187, 3);
            if (Widgets.isVisible(baseWidget)) {
                Widget[] children = baseWidget.getChildren();
                if (children == null) {
                    return;
                }
                for (int i = 0; i < children.length; ++i) {
                    Widget teleportItem = children[i];
                    if (!teleportItem.getText().contains(target)) continue;
                    Keyboard.type(i + 1);
                    return;
                }
            }
            inv.interact("Rub");
            return;
        }
        if (!RegionManager.useEquipmentJewellery()) {
            return;
        }
        Item equipped = Equipment.getFirst(ids);
        if (equipped != null) {
            equipped.interact(target);
        }
    }

    public static Teleport pohDigsitePendantTeleport(WorldPoint destination, int action) {
        return new Teleport(destination, 10, () -> {
            if (!Players.getLocal().isIdle() || Static.getClient().getGameState() == GameState.LOADING) {
                return;
            }
            if (Widgets.isVisible(Widgets.get(WidgetInfo.ADVENTURE_LOG))) {
                Keyboard.type(action);
                return;
            }
            TileObject digsitePendant = TileObjects.getNearest("Digsite Pendant");
            if (digsitePendant != null) {
                digsitePendant.interact("Teleport menu");
                return;
            }
            TeleportLoader.enterHouse();
        });
    }

    public static Teleport itemTeleport(TeleportItem teleportItem) {
        return new Teleport(teleportItem.getDestination(), 5, () -> {
            Item item = Inventory.getFirst(teleportItem.getItemId());
            if (item != null) {
                item.interact(teleportItem.getAction());
            }
        });
    }

    public static Teleport pohWidgetTeleport(WorldPoint destination, char action) {
        return new Teleport(destination, 10, () -> {
            if (!Players.getLocal().isIdle() || Static.getClient().getGameState() == GameState.LOADING) {
                return;
            }
            if (Widgets.isVisible(Widgets.get(590, 0))) {
                Keyboard.type(action);
                return;
            }
            TileObject box = TileObjects.getNearest(to -> to.getName() != null && to.getName().contains("Jewellery Box"));
            if (box != null) {
                box.interact("Teleport Menu");
                return;
            }
            TeleportLoader.enterHouse();
        });
    }

    public static Teleport mountedPohTeleport(WorldPoint destination, int objId, String action) {
        return new Teleport(destination, 10, () -> {
            if (!Players.getLocal().isIdle() || Static.getClient().getGameState() == GameState.LOADING) {
                return;
            }
            TileObject first = TileObjects.getNearest(objId);
            if (first != null) {
                first.interact(action);
                return;
            }
            TeleportLoader.enterHouse();
        });
    }

    public static void jewelryWildernessTeleport(String target, int ... ids) {
        TeleportLoader.jewelryTeleport(target, ids);
        Time.sleepTick();
        if (Dialog.isViewingOptions() && Dialog.getOptions().stream().anyMatch(it -> it.getText() != null && WILDY_PATTERN.matcher(it.getText()).matches())) {
            Dialog.chooseOption(1);
        }
    }

    public static void slayerRingTeleport(String target, int ... ids) {
        Item ring = Inventory.getFirst(ids);
        if (ring == null && RegionManager.useEquipmentJewellery()) {
            ring = Equipment.getFirst(ids);
        }
        if (ring != null) {
            if (!Dialog.isViewingOptions()) {
                ring.interact("Teleport");
                Time.sleepTicksUntil(Dialog::isViewingOptions, 2);
                return;
            }
            if (Dialog.hasOption("Teleport")) {
                Dialog.chooseOption("Teleport");
                return;
            }
            Dialog.chooseOption(target);
        }
    }

    public static boolean ringOfDueling() {
        return Inventory.getFirst(MovementConstants.RING_OF_DUELING) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.RING_OF_DUELING) != null;
    }

    public static boolean gamesNecklace() {
        return Inventory.getFirst(MovementConstants.GAMES_NECKLACE) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.GAMES_NECKLACE) != null;
    }

    public static boolean combatBracelet() {
        return Inventory.getFirst(MovementConstants.COMBAT_BRACELET) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.COMBAT_BRACELET) != null;
    }

    public static boolean skillsNecklace() {
        return Inventory.getFirst(MovementConstants.SKILLS_NECKLACE) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.SKILLS_NECKLACE) != null;
    }

    public static boolean ringOfWealth() {
        return Inventory.getFirst(MovementConstants.RING_OF_WEALTH) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.RING_OF_WEALTH) != null;
    }

    public static boolean amuletOfGlory() {
        return Inventory.getFirst(MovementConstants.AMULET_OF_GLORY) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.AMULET_OF_GLORY) != null;
    }

    public static boolean necklaceOfPassage() {
        return Inventory.getFirst(MovementConstants.NECKLACE_OF_PASSAGE) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.NECKLACE_OF_PASSAGE) != null;
    }

    public static boolean xericsTalisman() {
        return Inventory.getFirst(MovementConstants.XERICS_TALISMAN) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.XERICS_TALISMAN) != null;
    }

    public static boolean slayerRing() {
        return Inventory.getFirst(MovementConstants.SLAYER_RING) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.SLAYER_RING) != null;
    }

    public static boolean digsitePendant() {
        return Inventory.getFirst(MovementConstants.DIGSITE_PENDANT) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.DIGSITE_PENDANT) != null;
    }

    public static boolean burningAmulet() {
        return Inventory.getFirst(MovementConstants.BURNING_AMULET) != null || RegionManager.useEquipmentJewellery() && Equipment.getFirst(MovementConstants.BURNING_AMULET) != null;
    }

    private static /* synthetic */ void lambda$buildTimedTeleports$1(TeleportSpell teleportSpell) {
        Spell spell = teleportSpell.getSpell();
        if (teleportSpell == TeleportSpell.TELEPORT_TO_HOUSE) {
            Widget widget = Widgets.get(spell.getWidget());
            if (widget == null) {
                return;
            }
            widget.interact(1);
        } else {
            Magic.cast(spell);
        }
    }

    private static /* synthetic */ void lambda$buildTimedTeleports$0(Minigames.Destination tp) {
        Minigames.teleport(tp);
    }
}

