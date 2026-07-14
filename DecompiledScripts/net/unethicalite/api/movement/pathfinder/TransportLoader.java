/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.runelite.api.Item
 *  net.runelite.api.MenuAction
 *  net.runelite.api.NPC
 *  net.runelite.api.Point
 *  net.runelite.api.Quest
 *  net.runelite.api.QuestState
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  org.apache.commons.lang3.tuple.Pair
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.movement.pathfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.movement.pathfinder.model.FairyRingLocation;
import net.unethicalite.api.movement.pathfinder.model.MovementConstants;
import net.unethicalite.api.movement.pathfinder.model.Transport;
import net.unethicalite.api.movement.pathfinder.model.dto.TransportDto;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirements;
import net.unethicalite.api.quests.Quests;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportLoader {
    private static final Logger log = LoggerFactory.getLogger(TransportLoader.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<Transport> ALL_STATIC_TRANSPORTS = new ArrayList<Transport>();
    private static List<Transport> LAST_TRANSPORT_LIST = new ArrayList<Transport>();

    public static void init() {
        log.info("Loading transports");
        try (InputStream stream = Walker.class.getResourceAsStream("/transports.json");){
            if (stream == null) {
                log.error("Failed to load transports.");
                return;
            }
            TransportDto[] json = (TransportDto[])GSON.fromJson(new String(stream.readAllBytes()), TransportDto[].class);
            List list = Arrays.stream(json).map(TransportDto::toTransport).collect(Collectors.toList());
            ALL_STATIC_TRANSPORTS.addAll(list);
        }
        catch (IOException e) {
            log.error("Failed to load transports.", (Throwable)e);
        }
        log.info("Loaded {} transports", (Object)ALL_STATIC_TRANSPORTS.size());
    }

    public static List<Transport> buildTransports() {
        return LAST_TRANSPORT_LIST;
    }

    public static void refreshTransports() {
        GameThread.invoke(() -> {
            int gold;
            List filteredStatic = ALL_STATIC_TRANSPORTS.stream().filter(it -> it.getRequirements().fulfilled()).collect(Collectors.toList());
            ArrayList<Transport> transports = new ArrayList<Transport>();
            int n = gold = Inventory.getFirst(995) != null ? Inventory.getFirst(995).getQuantity() : 0;
            if (gold >= 30) {
                if (Quests.isFinished(Quest.PIRATES_TREASURE)) {
                    transports.add(TransportLoader.npcTransport(new WorldPoint(3027, 3218, 0), new WorldPoint(2956, 3143, 1), 3644, "Pay-fare"));
                    transports.add(TransportLoader.npcTransport(new WorldPoint(2954, 3147, 0), new WorldPoint(3032, 3217, 1), 3648, "Pay-Fare"));
                } else {
                    transports.add(TransportLoader.npcDialogTransport(new WorldPoint(3027, 3218, 0), new WorldPoint(2956, 3143, 1), 3644, "Yes please."));
                    transports.add(TransportLoader.npcDialogTransport(new WorldPoint(2954, 3147, 0), new WorldPoint(3032, 3217, 1), 3648, "Can I journey on this ship?", "Search away, I have nothing to hide.", "Ok"));
                }
            }
            if (Worlds.inMembersWorld()) {
                transports.add(TransportLoader.objectTransport(new WorldPoint(1312, 3685, 0), new WorldPoint(1312, 10086, 0), 34405, "Enter"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1293, 10090, 0), new WorldPoint(1293, 10093, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1293, 10093, 0), new WorldPoint(1293, 10091, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1296, 10096, 0), new WorldPoint(1298, 10096, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1298, 10096, 0), new WorldPoint(1296, 10096, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1307, 10096, 0), new WorldPoint(1309, 10096, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1309, 10096, 0), new WorldPoint(1307, 10096, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1316, 10096, 0), new WorldPoint(1318, 10096, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1318, 10096, 0), new WorldPoint(1316, 10096, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1324, 10096, 0), new WorldPoint(1326, 10096, 0), 34642, "Pass"));
                transports.add(TransportLoader.objectTransport(new WorldPoint(1326, 10096, 0), new WorldPoint(1324, 10096, 0), 34642, "Pass"));
                if (gold >= 10000) {
                    transports.add(TransportLoader.npcTransport(new WorldPoint(1782, 3458, 0), new WorldPoint(1778, 3417, 0), 7483, "Travel"));
                }
                transports.add(TransportLoader.npcTransport(new WorldPoint(1779, 3418, 0), new WorldPoint(1784, 3458, 0), 7484, "Travel"));
                if (Vars.getBit(4897) == 0) {
                    if (Vars.getBit(8063) >= 7) {
                        transports.add(TransportLoader.npcDialogTransport(new WorldPoint(3054, 3245, 0), new WorldPoint(1824, 3691, 0), 8484, "Can you take me to Great Kourend?"));
                    } else {
                        transports.add(TransportLoader.npcDialogTransport(new WorldPoint(3054, 3245, 0), new WorldPoint(1824, 3691, 0), 8484, "That's great, can you take me there please?"));
                    }
                } else {
                    transports.add(TransportLoader.npcTransport(new WorldPoint(3054, 3245, 0), new WorldPoint(1824, 3695, 1), "Veos", "Port Piscarilius"));
                }
                if (Quests.getState(Quest.LUNAR_DIPLOMACY) != QuestState.NOT_STARTED) {
                    transports.add(TransportLoader.npcTransport(new WorldPoint(2222, 3796, 2), new WorldPoint(2130, 3899, 2), 6650, "Travel"));
                    transports.add(TransportLoader.npcTransport(new WorldPoint(2130, 3899, 2), new WorldPoint(2222, 3796, 2), 6650, "Travel"));
                }
                if (Quests.isFinished(Quest.TREE_GNOME_VILLAGE)) {
                    for (SpiritTree spiritTree : MovementConstants.SPIRIT_TREES) {
                        if (spiritTree.location.equals("Gnome Stronghold") && !Quests.isFinished(Quest.THE_GRAND_TREE)) continue;
                        for (SpiritTree spiritTree2 : MovementConstants.SPIRIT_TREES) {
                            if (spiritTree == spiritTree2) continue;
                            transports.add(TransportLoader.spritTreeTransport(spiritTree.position, spiritTree2.position, spiritTree2.location));
                        }
                    }
                }
                if (Quests.isFinished(Quest.THE_LOST_TRIBE)) {
                    transports.add(TransportLoader.npcTransport(new WorldPoint(3229, 9610, 0), new WorldPoint(3316, 9613, 0), 7301, "Mines"));
                    transports.add(TransportLoader.npcTransport(new WorldPoint(3316, 9613, 0), new WorldPoint(3229, 9610, 0), 7299, "Cellar"));
                }
                if (Quests.getState(Quest.TREE_GNOME_VILLAGE) != QuestState.NOT_STARTED) {
                    transports.add(TransportLoader.npcTransport(new WorldPoint(2504, 3192, 0), new WorldPoint(2515, 3159, 0), 4968, "Follow"));
                    transports.add(TransportLoader.npcTransport(new WorldPoint(2515, 3159, 0), new WorldPoint(2504, 3192, 0), 4968, "Follow"));
                }
                if (Vars.getVarp(934) >= 15) {
                    transports.add(TransportLoader.objectTransport(new WorldPoint(2328, 3496, 0), new WorldPoint(1994, 4983, 3), 19790, "Enter"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(1994, 4983, 3), new WorldPoint(2328, 3496, 0), 19891, "Exit"));
                }
                if (Quests.isFinished(Quest.THE_FREMENNIK_TRIALS) || gold >= 1000) {
                    transports.add(TransportLoader.npcTransport(new WorldPoint(2544, 3760, 0), new WorldPoint(2620, 3682, 0), 10407, "Rellekka"));
                    transports.add(TransportLoader.npcTransport(new WorldPoint(2620, 3682, 0), new WorldPoint(2547, 3759, 0), 5937, "Waterbirth Island"));
                }
                transports.add(TransportLoader.npcTransport(new WorldPoint(2620, 3692, 0), new WorldPoint(2213, 3794, 0), 3855, "Pirate's Cove"));
                transports.add(TransportLoader.npcTransport(new WorldPoint(2213, 3794, 0), new WorldPoint(2620, 3692, 0), 9306, "Rellekka"));
                if (Skills.getBoostedLevel(Skill.AGILITY) >= 10) {
                    transports.add(TransportLoader.objectTransport(new WorldPoint(2546, 2871, 0), new WorldPoint(2546, 2873, 0), 31757, "Climb"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(2546, 2873, 0), new WorldPoint(2546, 2871, 0), 31757, "Climb"));
                }
                if (Quests.getState(Quest.RECIPE_FOR_DISASTER) != QuestState.IN_PROGRESS) {
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3213, 3221, 0), new WorldPoint(3212, 3221, 0), 12349, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3212, 3221, 0), new WorldPoint(3213, 3221, 0), 12349, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3213, 3222, 0), new WorldPoint(3212, 3222, 0), 12350, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3212, 3222, 0), new WorldPoint(3213, 3222, 0), 12350, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3207, 3218, 0), new WorldPoint(3207, 3217, 0), 12348, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3207, 3217, 0), new WorldPoint(3207, 3218, 0), 12348, "Open"));
                }
                if (Vars.getBit(3637) >= 153) {
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3295, 3429, 0), new WorldPoint(3296, 3429, 0), 24561, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3296, 3429, 0), new WorldPoint(3295, 3429, 0), 24561, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3295, 3428, 0), new WorldPoint(3296, 3428, 0), 24561, "Open"));
                    transports.add(TransportLoader.objectTransport(new WorldPoint(3296, 3428, 0), new WorldPoint(3295, 3428, 0), 24561, "Open"));
                }
                if (Equipment.contains(772, 9084) && Quests.getState(Quest.FAIRYTALE_II__CURE_A_QUEEN) != QuestState.NOT_STARTED) {
                    for (Iterator<Object> iterator : FairyRingLocation.values()) {
                        for (FairyRingLocation destRing : FairyRingLocation.values()) {
                            if (iterator == destRing) continue;
                            transports.add(TransportLoader.fairyRingTransport(iterator, destRing));
                        }
                    }
                }
            }
            transports.add(TransportLoader.npcTransport(new WorldPoint(3041, 3237, 0), new WorldPoint(2834, 3331, 1), 1166, "Take-boat"));
            transports.add(TransportLoader.npcTransport(new WorldPoint(2834, 3335, 0), new WorldPoint(3048, 3231, 1), 1170, "Take-boat"));
            transports.add(TransportLoader.npcDialogTransport(new WorldPoint(2821, 3374, 0), new WorldPoint(2822, 9774, 0), 1164, "Well that is a risk I will have to take."));
            transports.add(TransportLoader.npcTransport(new WorldPoint(3362, 3445, 0), new WorldPoint(3724, 3808, 0), 8012, "Quick-Travel"));
            transports.add(TransportLoader.objectDialogTransport(new WorldPoint(3724, 3808, 0), new WorldPoint(3362, 3445, 0), 30914, new String[]{"Travel"}, "Row to the barge and travel to the Digsite."));
            for (MagicMushtree magicMushtree : MovementConstants.MUSHTREES) {
                for (MagicMushtree magicMushtree2 : MovementConstants.MUSHTREES) {
                    if (magicMushtree.position == magicMushtree2.position) continue;
                    transports.add(TransportLoader.mushtreeTransport(magicMushtree.position, magicMushtree2.position, magicMushtree2.widget));
                }
            }
            transports.add(TransportLoader.objectDialogTransport(new WorldPoint(2461, 3382, 0), new WorldPoint(2461, 3385, 0), 190, new String[]{"Open"}, "Sorry, I'm a bit busy."));
            transports.add(TransportLoader.trapDoorTransport(new WorldPoint(3405, 3506, 0), new WorldPoint(3405, 9906, 0), 1579, 1581));
            transports.add(TransportLoader.trapDoorTransport(new WorldPoint(3423, 3485, 0), new WorldPoint(3440, 9887, 0), 3432, 3433));
            transports.add(TransportLoader.trapDoorTransport(new WorldPoint(3422, 3484, 0), new WorldPoint(3440, 9887, 0), 3432, 3433));
            transports.add(TransportLoader.npcTransport(new WorldPoint(1824, 3691, 0), new WorldPoint(3055, 3242, 1), 10727, "Port Sarim"));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2557, 3444, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2557, 3445, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2558, 3443, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2559, 3443, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2560, 3444, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2560, 3445, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2558, 3446, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2559, 3446, 0), new WorldPoint(2555, 9844, 0), 294, 1992));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2512, 3476, 0), new WorldPoint(2513, 3468, 0), 954, 1996));
            transports.add(TransportLoader.itemUseTransport(new WorldPoint(2512, 3466, 0), new WorldPoint(2511, 3463, 0), 954, 2020));
            transports.add(TransportLoader.trapDoorTransport(new WorldPoint(3096, 3468, 0), new WorldPoint(3096, 9867, 0), 1579, 1581));
            if (Inventory.contains(MovementConstants.SLASH_ITEMS) || Equipment.contains(MovementConstants.SLASH_ITEMS)) {
                for (Pair pair : MovementConstants.SLASH_WEB_POINTS) {
                    transports.add(TransportLoader.slashWebTransport((WorldPoint)pair.getLeft(), (WorldPoint)pair.getRight()));
                    transports.add(TransportLoader.slashWebTransport((WorldPoint)pair.getRight(), (WorldPoint)pair.getLeft()));
                }
            }
            LAST_TRANSPORT_LIST.clear();
            LAST_TRANSPORT_LIST.addAll(filteredStatic);
            LAST_TRANSPORT_LIST.addAll(transports);
        });
    }

    public static Transport trapDoorTransport(WorldPoint source, WorldPoint destination, int closedId, int openedId) {
        return new Transport(source, destination, Integer.MAX_VALUE, 0, () -> {
            TileObject openedTrapdoor = TileObjects.getFirstSurrounding(source, 5, openedId);
            if (openedTrapdoor != null) {
                openedTrapdoor.interact(0);
                return;
            }
            TileObject closedTrapDoor = TileObjects.getFirstSurrounding(source, 5, closedId);
            if (closedTrapDoor != null) {
                closedTrapDoor.interact(0);
            }
        });
    }

    public static Transport fairyRingTransport(FairyRingLocation source, FairyRingLocation destination) {
        return new Transport(source.getLocation(), destination.getLocation(), Integer.MAX_VALUE, 0, () -> {
            log.debug("Looking for fairy ring at {} to {}", (Object)source.getLocation(), (Object)destination.getLocation());
            TileObject ring = TileObjects.getFirstSurrounding(source.getLocation(), 5, "Fairy ring");
            if (ring == null) {
                log.debug("Fairy ring at {} is null", (Object)source.getLocation());
                return;
            }
            if (destination == FairyRingLocation.ZANARIS) {
                ring.interact("Zanaris");
                return;
            }
            if (ring.hasAction(a -> a != null && a.contains(destination.getCode()))) {
                ring.interact(a -> a != null && a.contains(destination.getCode()));
                return;
            }
            if (Widgets.isVisible(Widgets.get(WidgetInfo.FAIRY_RING))) {
                destination.travel();
                return;
            }
            ring.interact("Configure");
        });
    }

    public static Transport itemUseTransport(WorldPoint source, WorldPoint destination, int itemId, int objId) {
        return new Transport(source, destination, Integer.MAX_VALUE, 0, () -> {
            Item item = Inventory.getFirst(itemId);
            if (item == null) {
                return;
            }
            TileObject transport = TileObjects.getFirstSurrounding(source, 5, objId);
            if (transport != null) {
                item.useOn(transport);
            }
        });
    }

    public static Transport npcTransport(WorldPoint source, WorldPoint destination, int npcId, String ... actions) {
        return new Transport(source, destination, 10, 0, () -> {
            NPC npc = NPCs.getNearest(x -> x.getWorldLocation().distanceTo(source) <= 10 && x.getId() == npcId);
            if (npc != null) {
                npc.interact(actions);
            }
        });
    }

    public static Transport npcTransport(WorldPoint source, WorldPoint destination, String npcName, String ... actions) {
        return new Transport(source, destination, 10, 0, () -> {
            NPC npc = NPCs.getNearest(x -> x.getWorldLocation().distanceTo(source) <= 10 && x.getName().equalsIgnoreCase(npcName));
            if (npc != null) {
                npc.interact(actions);
            }
        });
    }

    public static Transport npcDialogTransport(WorldPoint source, WorldPoint destination, int npcId, String ... chatOptions) {
        return new Transport(source, destination, 10, 0, () -> {
            if (Dialog.canContinue()) {
                Dialog.continueSpace();
                return;
            }
            if (Dialog.isViewingOptions()) {
                if (Dialog.chooseOption(chatOptions)) {
                    return;
                }
                return;
            }
            NPC npc = NPCs.getNearest(x -> x.getWorldLocation().distanceTo(source) <= 10 && x.getId() == npcId);
            if (npc != null) {
                npc.interact(0);
            }
        });
    }

    public static Transport objectTransport(WorldPoint source, WorldPoint destination, int objId, String actions) {
        return new Transport(source, destination, Integer.MAX_VALUE, 0, () -> {
            TileObject first = TileObjects.getFirstAt(source, objId);
            if (first != null) {
                first.interact(actions);
                return;
            }
            TileObjects.getSurrounding(source, 5, x -> x.getId() == objId).stream().min(Comparator.comparingInt(o -> o.distanceTo(source))).ifPresent(obj -> obj.interact(actions));
        });
    }

    public static Transport objectTransport(WorldPoint source, WorldPoint destination, int objId, String actions, Requirements requirements) {
        return new Transport(source, destination, Integer.MAX_VALUE, 0, () -> {
            TileObject first = TileObjects.getFirstAt(source, objId);
            if (first != null) {
                log.debug("Transport found {}", (Object)first.getWorldLocation());
                first.interact(actions);
                return;
            }
            log.debug("Transport not found {}, {}", (Object)source, (Object)objId);
            TileObjects.getSurrounding(source, 5, x -> x.getId() == objId).stream().min(Comparator.comparingInt(o -> o.distanceTo(source))).ifPresent(obj -> obj.interact(actions));
        }, requirements);
    }

    public static Transport objectTransport(WorldPoint source, WorldPoint destination, TileObject tileObject, int actionIndex) {
        return new Transport(source, destination, Integer.MAX_VALUE, 0, () -> {
            if (tileObject == null) {
                return;
            }
            tileObject.interact(actionIndex);
        });
    }

    public static Transport objectDialogTransport(WorldPoint source, WorldPoint destination, int objId, String[] actions, String ... chatOptions) {
        return new Transport(source, destination, Integer.MAX_VALUE, 0, () -> {
            if (Dialog.isOpen()) {
                if (Dialog.canContinue()) {
                    Dialog.continueSpace();
                    return;
                }
                if (Dialog.chooseOption(chatOptions)) {
                    return;
                }
                return;
            }
            TileObject transport = TileObjects.getFirstSurrounding(source, 5, objId);
            if (transport != null) {
                transport.interact(actions);
            }
        });
    }

    public static Transport slashWebTransport(WorldPoint source, WorldPoint destination) {
        return new Transport(source, destination, Integer.MAX_VALUE, 0, () -> {
            TileObject transport = TileObjects.getFirstSurrounding(source, 5, it -> it.getName() != null && it.getName().contains("Web") && it.hasAction(new String[]{"Slash"}));
            if (transport != null) {
                transport.interact("Slash");
            } else {
                Movement.walk(destination);
            }
        });
    }

    private static Transport spritTreeTransport(WorldPoint source, WorldPoint target, String location) {
        return new Transport(source, target, 5, 0, () -> {
            Widget treeWidget = Widgets.get(187, 3);
            if (Widgets.isVisible(treeWidget)) {
                Arrays.stream(treeWidget.getDynamicChildren()).filter(child -> child.getText().toLowerCase().contains(location.toLowerCase())).findFirst().ifPresent(child -> child.interact(0, MenuAction.WIDGET_CONTINUE.getId(), child.getIndex(), child.getId()));
                return;
            }
            TileObject tree = TileObjects.getFirstSurrounding(source, 5, 1293, 1294, 1295);
            if (tree != null) {
                Point point = tree.menuPoint();
                tree.interact(tree.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), point.getX(), point.getY());
            }
        });
    }

    private static Transport mushtreeTransport(WorldPoint source, WorldPoint target, WidgetInfo widget) {
        return new Transport(source, target, 5, 0, () -> {
            Widget treeWidget = Widgets.get(widget);
            if (Widgets.isVisible(treeWidget)) {
                treeWidget.interact(0, MenuAction.WIDGET_CONTINUE.getId(), treeWidget.getIndex(), treeWidget.getId());
                return;
            }
            TileObject tree = TileObjects.getFirstSurrounding(source, 5, "Magic Mushtree");
            if (tree != null) {
                tree.interact("Use");
            }
        });
    }

    public static class SpiritTree {
        private final WorldPoint position;
        private final String location;

        public SpiritTree(WorldPoint position, String location) {
            this.position = position;
            this.location = location;
        }
    }

    public static class MagicMushtree {
        private final WorldPoint position;
        private final WidgetInfo widget;

        public MagicMushtree(WorldPoint position, WidgetInfo widget) {
            this.position = position;
            this.widget = widget;
        }
    }
}

