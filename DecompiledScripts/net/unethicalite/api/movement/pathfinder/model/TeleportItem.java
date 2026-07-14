/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Quest
 *  net.runelite.api.QuestState
 *  net.runelite.api.Skill
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.movement.pathfinder.model;

import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.MovementConstants;
import net.unethicalite.api.quests.Quests;

public enum TeleportItem {
    HOUSE_TAB(MovementConstants.HOUSE_POINT, "Break", null, 8013),
    WATERBIRTH_TELEPORT_TAB(new WorldPoint(2546, 3757, 0), "Break", null, 24953),
    KHAZARD_TELEPORT_TAB(new WorldPoint(2637, 3166, 0), "Break", null, 24957),
    VARROCK_TELEPORT_TAB(new WorldPoint(3212, 3424, 0), "Break", null, 8007),
    LUMBRIDGE_TELEPORT_TAB(new WorldPoint(3225, 3219, 0), "Break", null, 8008),
    FALADOR_TELEPORT_TAB(new WorldPoint(2966, 3379, 0), "Break", null, 8009),
    CAMELOT_TELEPORT_TAB(new WorldPoint(2757, 3479, 0), "Break", null, 8010),
    ARDOUGNE_TELEPORT_TAB(new WorldPoint(2661, 3300, 0), "Break", null, 8011),
    WEST_ARDOUGNE_TELEPORT_TAB(new WorldPoint(2500, 3290, 0), "Break", null, 19623),
    RIMMINGTON_TELEPORT_TAB(new WorldPoint(2954, 3224, 0), "Break", null, 11741),
    TAVERLEY_TELEPORT_TAB(new WorldPoint(2894, 3465, 0), "Break", null, 11742),
    RELLEKKA_TELEPORT_TAB(new WorldPoint(2668, 3631, 0), "Break", null, 11744),
    BRIMHAVEN_TELEPORT_TAB(new WorldPoint(2758, 3178, 0), "Break", null, 11745),
    POLLNIVNEACH_TELEPORT_TAB(new WorldPoint(3340, 3004, 0), "Break", null, 11743),
    YANILLE_TELEPORT_TAB(new WorldPoint(2544, 3095, 0), "Break", null, 11746),
    HOSIDIUS_TELEPORT_TAB(new WorldPoint(1744, 3517, 0), "Break", null, 19651),
    SALVE_GRAVEYARD_TELEPORT_TAB(new WorldPoint(3432, 3460, 0), "Break", null, 19619),
    DRAYNOR_MANOR_TELEPORT_TAB(new WorldPoint(3109, 3350, 0), "Break", null, 19615),
    FENKENSTRAINS_CASTLE_TELEPORT_TAB(new WorldPoint(3549, 3530, 0), "Break", null, 19621),
    RIMMINGTON_TAB(new WorldPoint(2954, 3223, 0), "Break", null, 11741),
    TAVERLEY_TAB(new WorldPoint(2894, 3466, 0), "Break", null, 11742),
    POLLNIVNEACH_TAB(new WorldPoint(3339, 3004, 0), "Break", null, 11743),
    HOSIDIUS_TAB(new WorldPoint(1744, 3518, 0), "Break", null, 19651),
    RELLEKKA_TAB(new WorldPoint(2671, 3631, 0), "Break", null, 11744),
    BRIMHAVEN_TAB(new WorldPoint(2758, 3177, 0), "Break", null, 11745),
    YANILLE_TAB(new WorldPoint(2545, 3096, 0), "Break", null, 11746),
    TROLLHEIM_TAB(new WorldPoint(2890, 3679, 0), "Break", null, 11747),
    PRIFDDINAS_TAB(new WorldPoint(3239, 6076, 0), "Break", null, 23771),
    FELDIP_HILLS_TELEPORT(new WorldPoint(2541, 2925, 0), "Teleport", null, 12404),
    PISCATORIS_TELEPORT(new WorldPoint(2342, 3647, 0), "Teleport", null, 12408),
    MORTTON_TELEPORT(new WorldPoint(3488, 3288, 0), "Teleport", null, 12406),
    DIGSITE_TELEPORT(new WorldPoint(3324, 3412, 0), "Teleport", null, 12403),
    NARDAH_TELEPORT(new WorldPoint(3420, 2917, 0), "Teleport", null, 12402),
    LUMBERYARD_TELEPORT(new WorldPoint(3302, 3486, 0), "Teleport", null, 12642),
    TAI_BWO_TELEPORT(new WorldPoint(2789, 3066, 0), "Teleport", null, 12409),
    ZULANDRA_TELEPORT(new WorldPoint(2197, 3055, 0), "Teleport", null, 12938),
    IORWERTH_TELEPORT(new WorldPoint(2194, 3258, 0), "Teleport", null, 12410),
    LUNAR_ISLE_TELEPORT(new WorldPoint(2093, 3912, 0), "Teleport", null, 12405),
    MOS_LEHARMLESS_TELEPORT(new WorldPoint(3701, 2996, 0), "Teleport", null, 12411),
    PEST_CONTROL_TELEPORT(new WorldPoint(2657, 2660, 0), "Teleport", null, 12407),
    KEY_MASTER_TELEPORT(new WorldPoint(2686, 9882, 0), "Teleport", null, 13249),
    REVENANT_CAVE_TELEPORT(new WorldPoint(3127, 3833, 0), "Teleport", null, 21802),
    WATSON_TELEPORT(new WorldPoint(1645, 3579, 0), "Teleport", null, 23387),
    WEISS_ICY_BASALT(new WorldPoint(2846, 3940, 0), "Weiss", Quest.MAKING_FRIENDS_WITH_MY_ARM, 22599),
    TROLL_STRONGHOLD(new WorldPoint(2838, 3693, 0), "Troll Stronghold", Quest.MAKING_FRIENDS_WITH_MY_ARM, 22601),
    TROLL_STRONGHOLD_OUTSIDE(new WorldPoint(2844, 3693, 0), "Troll Stronghold", Quest.MAKING_FRIENDS_WITH_MY_ARM, 22601),
    ROYAL_SEED_POD(new WorldPoint(2465, 3495, 0), "Commune", Quest.MONKEY_MADNESS_II, 19564),
    ECTOPHIAL(new WorldPoint(3659, 3523, 0), "Empty", null, 4251),
    TELEPORT_CRYSTAL_LLETYA(new WorldPoint(2330, 3172, 0), "Lletya", null, MovementConstants.TELEPORT_CRYSTAL),
    TELEPORT_CRYSTAL_PRIFDDINAS(new WorldPoint(3264, 6065, 0), "Prifddinas", Quest.SONG_OF_THE_ELVES, MovementConstants.TELEPORT_CRYSTAL),
    RELLEKKKA_LYRE(new WorldPoint(2664, 3643, 0), "Rellekka", Quest.THE_FREMENNIK_TRIALS, MovementConstants.ENCHANTED_LYRE),
    WATERBIRTH_ISLAND_LYRE(new WorldPoint(2550, 3756, 0), "Waterbirth Island", Quest.THE_FREMENNIK_TRIALS, MovementConstants.ENCHANTED_LYRE),
    NEITIZNOT_LYRE(new WorldPoint(2336, 3801, 0), "Neitiznot", Quest.THE_FREMENNIK_TRIALS, MovementConstants.ENCHANTED_LYRE),
    JATIZSO_LYRE(new WorldPoint(2409, 3809, 0), "Jatizso", Quest.THE_FREMENNIK_TRIALS, MovementConstants.ENCHANTED_LYRE);

    private final WorldPoint destination;
    private final Quest requirement;
    private final int[] itemIds;
    private final String action;

    private TeleportItem(WorldPoint destination, String action, Quest requirement, int ... itemIds) {
        this.destination = destination;
        this.requirement = requirement;
        this.itemIds = itemIds;
        this.action = action;
    }

    public WorldPoint getDestination() {
        return this.destination;
    }

    public int[] getItemId() {
        return this.itemIds;
    }

    public String getAction() {
        return this.action;
    }

    public boolean canUse() {
        return this.hasRequirements() && Inventory.getFirst(this.itemIds) != null;
    }

    public boolean hasRequirements() {
        boolean hasQuestReqs = this.requirement == null || Quests.getState(this.requirement) == QuestState.FINISHED;
        switch (this) {
            case JATIZSO_LYRE: 
            case NEITIZNOT_LYRE: {
                return hasQuestReqs && Vars.getBit(4494) > 0;
            }
            case WATERBIRTH_ISLAND_LYRE: {
                return hasQuestReqs && Vars.getBit(4493) > 0;
            }
            case TROLL_STRONGHOLD: {
                return hasQuestReqs && Skills.getLevel(Skill.AGILITY) >= 73 && Vars.getBit(4493) > 0;
            }
            case ARDOUGNE_TELEPORT_TAB: {
                return hasQuestReqs && Vars.getVarp(165) >= 30;
            }
            case SALVE_GRAVEYARD_TELEPORT_TAB: {
                return hasQuestReqs && Vars.getVarp(302) >= 61;
            }
        }
        return hasQuestReqs;
    }
}

