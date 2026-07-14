/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller;

import net.runelite.api.coords.WorldPoint;

public final class AioSkillerConstants {
    public static final WorldPoint HOME_CENTER = new WorldPoint(2785, 3382, 0);
    public static final int HOME_RADIUS = 35;
    public static final String HOME_COMMAND = "::home";
    public static final int HOME_POLL_MS = 600;
    public static final int HOME_VERIFY_TIMEOUT_MS = 5000;
    public static final int HOME_MOVED_DIST = 20;
    public static final WorldPoint GLIDER_TILE = new WorldPoint(2795, 3374, 0);
    public static final String GLIDER_OBJECT_NAME = "Gnome glider";
    public static final String GLIDER_TRAVEL_ACTION = "Travel";
    public static final int GLIDER_REACH_DIST = 4;
    public static final int GLIDER_HOME_MAX_ATTEMPTS = 4;
    public static final int SEARCH_BTN_X = 75;
    public static final int SEARCH_BTN_Y = 305;
    public static final int SELECT_BTN_X = 220;
    public static final int SELECT_BTN_Y = 90;
    public static final int MENU_OPEN_DELAY_MS = 6000;
    public static final int SEARCH_PROMPT_DELAY_MS = 3000;
    public static final int TYPE_RESULT_DELAY_MS = 1200;
    public static final int SELECT_RESULT_DELAY_MS = 1500;
    public static final int TELEPORT_VERIFY_TIMEOUT_MS = 6000;
    public static final int TELEPORT_ARRIVE_DIST = 15;
    public static final int BANK_OBJECT_ID = 10083;
    public static final String BANK_ACTION = "Use";
    public static final int BANK_REACH_DIST = 4;
    public static final WorldPoint BANK_TILE = new WorldPoint(2796, 3391, 0);
    public static final int SHOP_NPC_ID = 2813;
    public static final WorldPoint SHOP_TILE = new WorldPoint(2772, 3369, 0);
    public static final String SHOP_OPEN_ACTION = "Open-shop";
    public static final int SHOP_INTERFACE_GROUP = 300;
    public static final int SHOP_REACH_DIST = 3;
    public static final int SHOP_HOME_TELEPORT_DIST = 10;
    public static final int SHOP_CLOSE_X = 482;
    public static final int SHOP_CLOSE_Y = 37;
    public static final int COINS_ID = 995;
    public static final int BIRD_SNARE_ID = 10006;
    public static final int IRON_PICKAXE_ID = 1267;
    public static final int RUNE_PICKAXE_ID = 1275;
    public static final int IRON_AXE_ID = 1349;
    public static final int RUNE_AXE_ID = 1359;
    public static final int HAMMER_ID = 2347;
    public static final int WATER_VIAL_PACK_ID = 11879;
    public static final int VIAL_OF_WATER_ID = 227;
    public static final int CHISEL_ID = 1755;
    public static final String CHISEL_NAME = "Chisel";
    public static final String VIAL_OF_WATER_NAME = "Vial of water";
    public static final int BIRD_SNARE_BUY_QTY = 3;
    public static final int VIAL_PACK_BUY_QTY = 3;
    public static final int VIAL_MIN_BANK = 100;
    public static final int HAMMER_MIN_COINS = 50;
    public static final int[] COIN_POUCH_IDS = new int[]{22535, 22525, 22528, 22527};
    public static final String COIN_POUCH_OPEN_ACTION = "Open-all";
    public static final String[] NEVER_SELL_NAMES = new String[]{"coins", "coin pouch"};
    public static final int[] COMBAT_GEAR_IDS = new int[]{30323, 30365, 30345, 30329, 30326};
    public static final String[] COMBAT_NPC_NAMES = new String[]{"Rock crab", "Crate Crab"};
    public static final int COMBAT_SEARCH_RADIUS = 12;
    public static final int LUNGE_STYLE_INDEX = 2;
    public static final int DEATH_ANIMATION = 836;
    public static final int[] MAGIC_GEAR_IDS = new int[]{30332, 30335, 30362, 30368, 30365, 30371, 30342, 30339, 30374};
    public static final int MAGIC_SEARCH_RADIUS = 12;
    public static final int OLD_WEAPON_ID = 30345;
    public static final int DRAGON_SCIMITAR_ID = 4587;
    public static final String DRAGON_SCIMITAR_NAME = "Dragon scimitar";
    public static final int DRAGON_SCIM_ATTACK_LEVEL = 60;
    public static final int DRAGON_SCIM_COST = 115000;
    public static final int[] SCIM_COMBAT_GEAR_IDS = new int[]{30323, 30365, 4587, 30329, 30326};
    public static final int SCIM_SHOP_NPC_ID = 1158;
    public static final String SCIM_SHOP_NPC_NAME = "Warrior";
    public static final WorldPoint SCIM_SHOP_TILE = new WorldPoint(2771, 3368, 0);
    public static final int SCIM_SHOP_DIALOG_OPTION = 2;
    public static final int SCIM_SHOP_REACH_DIST = 3;
    public static final int SCIM_SHOP_SEARCH_RADIUS = 15;
    public static final String TERM_GEMSTONE = "Gemstone";
    public static final String GEMSTONE_CAVE_NAME = "Cave";
    public static final String GEMSTONE_CRAB_NAME = "Gemstone crab";
    public static final int GEMSTONE_SEARCH_RADIUS = 14;
    public static final int GEMSTONE_CAVE_ID = 57613;
    public static final String GEMSTONE_CAVE_ACTION = "Crawl-through";
    public static final int RUNE_SCIMITAR_ID = 1333;
    public static final int RUNE_FULL_HELM_ID = 1163;
    public static final int RUNE_PLATEBODY_ID = 1127;
    public static final int RUNE_PLATELEGS_ID = 1079;
    public static final int CLIMBING_BOOTS_ID = 3105;
    public static final int RECRUIT_SWORD_ID = 1000001;
    public static final String RECRUIT_SWORD_NAME = "Recruit sword";
    public static final String RUNE_SCIMITAR_NAME = "Rune scimitar";
    public static final String RUNE_FULL_HELM_NAME = "Rune full helm";
    public static final String RUNE_PLATEBODY_NAME = "Rune platebody";
    public static final String RUNE_PLATELEGS_NAME = "Rune platelegs";
    public static final String CLIMBING_BOOTS_NAME = "Climbing boots";
    public static final int GEMSTONE_GP_REQUIRED = 400000;
    public static final int GEMSTONE_RUNE_LEVEL = 40;
    public static final int GEMSTONE_DRAGON_LEVEL = 60;
    public static final int[] GEMSTONE_WEAPON_BUY_IDS = new int[]{1333, 4587};
    public static final int[] GEMSTONE_ARMOUR_BUY_IDS = new int[]{1163, 1127, 1079, 3105};
    public static final int[] GEMSTONE_WITHDRAW_IDS = new int[]{1000001};
    public static final int SCIM_SHOP_WEAPON_OPTION = 2;
    public static final int SCIM_SHOP_ARMOUR_OPTION = 1;
    public static final String SCIM_SHOP_WEAPON_LABEL = "Weaponry";
    public static final String SCIM_SHOP_ARMOUR_LABEL = "Equipment";
    public static final String EVENT_VOTE_BOSS_COMMAND = "::vb";
    public static final String EVENT_WORLD_BOSS_COMMAND = "::wb";
    public static final String[] EVENT_ANNOUNCE_KEYWORDS = new String[]{"spawn", "boss"};
    public static final int[] ROCKCRAB_ARMOUR_IDS = new int[]{30323, 30365, 30329, 30326};
    public static final int[] RUNE_ARMOUR_IDS = new int[]{1163, 1127, 1079, 3105};
    public static final int RUNE_SCIM_ATTACK_LEVEL = 40;
    public static final int RUNE_ARMOUR_DEFENCE_LEVEL = 40;
    public static final int EVENT_TELEPORT_POLL_MS = 600;
    public static final int EVENT_TELEPORT_RETRY_MS = 3000;
    public static final int EVENT_TELEPORT_MAX_ATTEMPTS = 3;
    public static final int EVENT_TELEPORT_MOVED_DIST = 20;
    public static final int EVENT_ARRIVE_SETTLE_MS = 2500;
    public static final int EVENT_NPC_SEARCH_RADIUS = 20;
    public static final long EVENT_NPC_GRACE_MS = 9000L;
    public static final long EVENT_BOSS_GONE_MS = 1500L;
    public static final long EVENT_FIGHT_TIMEOUT_MS = 240000L;
    public static final int EVENT_EQUIP_MAX_ATTEMPTS = 15;
    public static final int EVENT_REJUVENATION_OBJECT_ID = 42822;
    public static final String EVENT_REJUVENATION_ACTION = "Rejuvenate";
    public static final long EVENT_REJUVENATE_TIMEOUT_MS = 12000L;
    public static final long EVENT_DEATH_RETURN_DELAY_MS = 1000L;
    public static final int EVENT_MAX_DEATHS = 3;
    public static final int EVENT_PROTECT_MAGIC_PRAYER_LEVEL = 37;
    public static final WorldPoint WC_TILE = new WorldPoint(2754, 3398, 0);
    public static final int WC_TREE_NORMAL_ID = 1276;
    public static final int WC_LOG_NORMAL_ID = 1511;
    public static final int WC_TREE_WILLOW_ID = 1308;
    public static final int WC_LOG_WILLOW_ID = 1519;
    public static final int WC_TREE_YEW_ID = 1309;
    public static final int WC_LOG_YEW_ID = 1515;
    public static final String WC_CHOP_ACTION = "Chop down";
    public static final int WC_WILLOW_LEVEL = 30;
    public static final int WC_RUNE_AXE_LEVEL = 41;
    public static final int WC_YEW_LEVEL = 60;
    public static final int MINING_RUNE_PICK_LEVEL = 41;
    public static final int ORE_PREP_BRONZE_BARS = 100;
    public static final int ORE_PREP_IRON_BARS = 100;
    public static final int ORE_PREP_STEEL_BARS = 100;
    public static final int ORE_PREP_MITHRIL_BARS = 300;
    public static final int COPPER_ORE_ID = 436;
    public static final int TIN_ORE_ID = 438;
    public static final int IRON_ORE_ID = 440;
    public static final int COAL_ID = 453;
    public static final int MITHRIL_ORE_ID = 447;
    public static final int AGILITY_ICE_LEVEL = 50;
    public static final int HUNTER_WAGTAIL_LEVEL = 19;
    public static final String TERM_AGILITY_LOW = "Skoblin Course";
    public static final String TERM_AGILITY_HIGH = "Arctic";
    public static final String TERM_HUNTER_LOW = "Crimson";
    public static final String TERM_HUNTER_HIGH = "Tropical wag";
    public static final String TERM_MINING = "The ore pit";
    public static final String TERM_COMBAT = "rock crab";
    public static final String HUNTER_BIRDS_LOW = "Crimson swift";
    public static final String HUNTER_BIRDS_HIGH = "Tropical wagtail";
    public static final String AFK_COMMAND = "::afk";
    public static final int AFK_ARRIVE_DIST = 10;
    public static final int AFK_TELEPORT_MOVED_DIST = 20;
    public static final int AFK_OBJECT_SEARCH_DIST = 20;
    public static final int AFK_WINDOW_DELAY_MS = 3000;
    public static final int AFK_TELEPORT_DELAY_MS = 2500;
    public static final int AFK_VERIFY_TIMEOUT_MS = 7000;
    public static final int AFK_STALL_MS = 8000;
    public static final int AFK_RESTART_DIST = 25;
    public static final int AFK_OPTION_JITTER_X = 8;
    public static final int AFK_OPTION_JITTER_Y = 6;
    public static final int AFK_FARM_CLICK_X = 219;
    public static final int AFK_FARM_CLICK_Y = 273;
    public static final WorldPoint AFK_FARM_DEST = new WorldPoint(2563, 3289, 0);
    public static final int AFK_FARM_OBJECT_ID = 16059;
    public static final String AFK_FARM_ACTION = "AFK-farm";
    public static final int AFK_HERB_CLICK_X = 258;
    public static final int AFK_HERB_CLICK_Y = 167;
    public static final WorldPoint AFK_HERB_DEST = new WorldPoint(2534, 3440, 0);
    public static final int AFK_HERB_OBJECT_ID = 47521;
    public static final String AFK_HERB_ACTION = "AFK Herblore";
    public static final int AFK_FISH_CLICK_X = 297;
    public static final int AFK_FISH_CLICK_Y = 165;
    public static final WorldPoint AFK_FISH_DEST = new WorldPoint(2552, 3333, 0);
    public static final int AFK_FISH_OBJECT_ID = 13609;
    public static final String AFK_FISH_ACTION = "AFK-fish";
    public static final int AFK_HUNT_CLICK_X = 215;
    public static final int AFK_HUNT_CLICK_Y = 240;
    public static final WorldPoint AFK_HUNT_DEST = new WorldPoint(2937, 3356, 0);
    public static final int AFK_HUNT_OBJECT_ID = 47534;
    public static final String AFK_HUNT_ACTION = "AFK-hunt";
    public static final int AFK_MINE_CLICK_X = 255;
    public static final int AFK_MINE_CLICK_Y = 133;
    public static final WorldPoint AFK_MINE_DEST = new WorldPoint(2860, 3350, 0);
    public static final int AFK_MINE_OBJECT_ID = 47557;
    public static final String AFK_MINE_ACTION = "AFK-mine";
    public static final int AFK_SMITH_CLICK_X = 295;
    public static final int AFK_SMITH_CLICK_Y = 133;
    public static final WorldPoint AFK_SMITH_DEST = new WorldPoint(2860, 3360, 0);
    public static final int AFK_SMITH_OBJECT_ID = 47558;
    public static final String AFK_SMITH_ACTION = "AFK-smelt";
    public static final int AFK_COMBAT_CLICK_X = 294;
    public static final int AFK_COMBAT_CLICK_Y = 96;
    public static final WorldPoint AFK_DUMMY_DEST = new WorldPoint(2860, 3370, 0);
    public static final int AFK_MAGIC_CLICK_X = 294;
    public static final int AFK_MAGIC_CLICK_Y = 96;
    public static final int AFK_MAGIC_OBJECT_ID = 47563;
    public static final String AFK_MAGIC_ACTION = "AFK-magic";
    public static final int AFK_RANGED_CLICK_X = 294;
    public static final int AFK_RANGED_CLICK_Y = 96;
    public static final int AFK_RANGED_OBJECT_ID = 47564;
    public static final String AFK_RANGED_ACTION = "AFK-ranged";
    public static final int AFK_MELEE_DUMMY_ID = 47562;
    public static final String AFK_ATTACK_ACTION = "AFK-attack";
    public static final String AFK_STRENGTH_ACTION = "AFK-strength";
    public static final String AFK_DEFENCE_ACTION = "AFK-defence";

    private AioSkillerConstants() {
    }

    public static String knownName(int n) {
        if (n == 4587) {
            return DRAGON_SCIMITAR_NAME;
        }
        if (n == 1333) {
            return RUNE_SCIMITAR_NAME;
        }
        if (n == 1163) {
            return RUNE_FULL_HELM_NAME;
        }
        if (n == 1127) {
            return RUNE_PLATEBODY_NAME;
        }
        if (n == 1079) {
            return RUNE_PLATELEGS_NAME;
        }
        if (n == 3105) {
            return CLIMBING_BOOTS_NAME;
        }
        if (n == 1000001) {
            return RECRUIT_SWORD_NAME;
        }
        return null;
    }
}

