/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.ox.smithing;

import net.runelite.api.coords.WorldPoint;

final class SmithingConstants {
    static final int BANK_OBJECT_ID = 10083;
    static final String BANK_ACTION = "Use";
    static final String BANK_OBJECT_NAME = "Bank booth";
    static final int FURNACE_ID = 165;
    static final String FURNACE_ACTION = "Smelt-bars";
    static final String FURNACE_NAME = "Furnace";
    static final int ANVIL_ID = 28563;
    static final String ANVIL_ACTION = "Smith";
    static final String ANVIL_NAME = "Anvil";
    static final int SMITH_IFACE_GROUP = 312;
    static final int SMITH_QTY_ALL_CHILD = 7;
    static final int SMITH_OPTION_FIRST_CHILD = 9;
    static final int SMITH_OPTION_LAST_CHILD = 50;
    static final int SMELT_IFACE_GROUP = 270;
    static final int SMELT_QTY_ALL_CHILD = 12;
    static final int SMELT_OPTION_FIRST_CHILD = 14;
    static final int SMELT_OPTION_LAST_CHILD = 30;
    static final int HAMMER_ID = 2347;
    static final String HAMMER_NAME = "Hammer";
    static final WorldPoint STATION_TILE = new WorldPoint(2784, 3374, 0);
    static final WorldPoint BANK_TILE = new WorldPoint(2792, 3394, 0);
    static final int ARRIVE_DIST = 4;
    static final int STATION_DIST = 5;
    static final int INV_SIZE = 28;
    static final int MIN_SMITH_BARS = 20;
    static final int COPPER_ORE = 436;
    static final int TIN_ORE = 438;
    static final int IRON_ORE = 440;
    static final int SILVER_ORE = 442;
    static final int COAL = 453;
    static final int GOLD_ORE = 444;
    static final int MITHRIL_ORE = 447;
    static final int ADAMANTITE_ORE = 449;
    static final int RUNITE_ORE = 451;
    static final int BRONZE_BAR = 2349;
    static final int IRON_BAR = 2351;
    static final int SILVER_BAR = 2355;
    static final int STEEL_BAR = 2353;
    static final int GOLD_BAR = 2357;
    static final int MITHRIL_BAR = 2359;
    static final int ADAMANTITE_BAR = 2361;
    static final int RUNITE_BAR = 2363;
    static final int WALK_POLL_MS = 1200;
    static final int STUCK_TIMEOUT_MS = 30000;
    static final int BANK_OPEN_TIMEOUT_MS = 5000;
    static final int BANK_OPEN_SETTLE_MS = 600;
    static final int MAKE_INTERFACE_WAIT_MS = 1200;
    static final int STATION_WORK_GRACE_MS = 3600;
    static final int SMELT_CONFIRM_MS = 5000;
    static final int WITHDRAW_POLL_MS = 500;
    static final int WITHDRAW_SETTLE_MS = 1000;
    static final int PROGRESS_WATCHDOG_MS = 60000;
    static final int STATION_METHOD_SWITCH_ATTEMPTS = 3;
    static final int SMITH_SELECT_PROBE_MS = 1200;

    private SmithingConstants() {
    }
}

