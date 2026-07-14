/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.tob;

import java.util.HashMap;
import java.util.Map;
import net.runelite.client.plugins.svc.tob.TobProjectileLearner;

final class TobConstants {
    static final int VERZIK_P1 = 8369;
    static final int VERZIK_P2 = 8371;
    static final int VERZIK_P3 = 8372;
    static final int VERZIK_P3_FINAL = 8374;
    static final int[] VERZIK_IDS = new int[]{8369, 8371, 8372, 8374};
    static final int WEB = 8376;
    static final int PILLAR = 8379;
    static final int WEB_WINDOW_TICKS = 9;
    static final int BOUND_EAT_FLOOR_PCT = 60;
    static final int BOUND_HOLD_TICKS = 4;
    static final int ENRAGE_HP_PCT = 20;
    static final int TORNADO_NPC = 8386;
    static final int TORNADO_FLEE_DIST = 2;
    static final int FINAL_ASSUMED_HIT = 64;
    static final int PROJ_SAFESPOT = 1596;
    static final int ANIM_BOMB = 8114;
    static final int ANIM_STUN = 8116;
    static final int ANIM_WEB = 8127;
    static final int ATTACK_CADENCE_TICKS = 4;
    static final int BOMB_FLIGHT_TICKS = 3;
    static final int ANIM_PLAYER_STUNNED = 1157;
    static final int STUN_TICKS = 5;
    static final int PROJ_BOMB = 1583;
    static final int PROJ_STUN = 1585;
    static final int PROJ_FINAL_MAGIC_A = 1591;
    static final int PROJ_FINAL_MAGIC_B = 1594;
    static final int PROJ_FINAL_RANGED = 1593;
    static final int PROJ_WEB = 1601;
    static final int HAZARD_GAME_OBJECT = 32734;
    static final int[] GROUND_TRUTH_MAGIC = new int[0];
    static final int[] GROUND_TRUTH_MECHANIC = new int[]{1583, 1585};
    static final int[] TILE_BOMB_PROJECTILES = new int[]{1583};
    static final int INSTANCE_MIN_X = 6400;
    static final int OPENING_WALK_SOUTH = 6;
    static final int OPENING_WALK_TIMEOUT_TICKS = 8;
    static final int ENTRANCE_OBJECT = 32653;
    static final int NOTICE_BOARD_OBJECT = 32655;
    static final int BOARD_GROUP = 364;
    static final int BOARD_MAKE_PARTY_CHILD = 66;
    static final int PARTY_PANEL_GROUP = 50;
    static final int PARTY_PANEL_CLOSE_CHILD = 1;
    static final int PARTY_FRAME_GROUP = 28;
    static final int PARTY_FRAME_NAMES_CHILD = 12;
    static final int PRAYER_TAB_GROUP = 541;
    static final int PRAYER_CHILD_PROT_MAGIC = 17;
    static final int PRAYER_CHILD_PROT_MISSILES = 18;
    static final int PRAYER_CHILD_PROT_MELEE = 19;
    static final int SPEC_WIDGET = 10485795;
    static final int SPEC_ORB_WIDGET = 38862884;
    static final int ANIM_BGS_SPEC = 7642;
    static final int SPEC_FIRED_ENERGY_DROP = 100;
    static final int SPEC_TIMEOUT_TICKS = 8;
    static final int SPEC_COOLDOWN_TICKS = 4;
    static final String CHAT_SAP = "saps your health";
    static final String CHAT_STUNNED = "you're stunned";
    static final String CHAT_FINAL_CHALLENGE = "the final challenge";
    static final String CHAT_COMPLETION = "theatre of blood completion";
    static final String CHAT_WEB_STUCK = "cannot free yourself from the web";
    static final String DHAROK_PIECE_HINT = "dharok's";
    static final int ASSUMED_MAX_HIT = 47;
    static final int PREDICTIVE_EAT_BUFFER = 12;
    static final int PREDICTIVE_EAT_ARM_TICKS = 10;
    static final int PRAYER_DRAIN_WATCHDOG_TICKS = 25;
    static final int WATCHDOG_TRIPS_TO_WIDGET_PRAYERS = 2;
    static final int LEARNER_ATTRIBUTION_WINDOW_TICKS = 4;
    static final int DODGE_FUTILE_STRIKES = 3;
    static final int MELEE_REACH = 1;
    static final int RANGED_MAX_RANGE = 8;
    static final int STUN_SAFE_EDGE = 2;
    static final int PINGPONG_GAP = 2;

    private TobConstants() {
    }

    static Map<Integer, TobProjectileLearner.Style> defaultProjectileStyles() {
        HashMap<Integer, TobProjectileLearner.Style> hashMap = new HashMap<Integer, TobProjectileLearner.Style>();
        hashMap.put(1583, TobProjectileLearner.Style.NONE);
        hashMap.put(1593, TobProjectileLearner.Style.RANGED);
        hashMap.put(1591, TobProjectileLearner.Style.MAGIC);
        hashMap.put(1594, TobProjectileLearner.Style.MAGIC);
        hashMap.put(1585, TobProjectileLearner.Style.NONE);
        hashMap.put(1596, TobProjectileLearner.Style.NONE);
        hashMap.put(1598, TobProjectileLearner.Style.NONE);
        hashMap.put(1601, TobProjectileLearner.Style.NONE);
        return hashMap;
    }
}

