/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.cox;

class OlmConstants {
    static final int OLM_HEAD_NORMAL = 7551;
    static final int OLM_HEAD_CM = 7554;
    static final int OLM_MAGE_HAND_NORMAL = 7550;
    static final int OLM_MAGE_HAND_CM = 7553;
    static final int OLM_MELEE_HAND_NORMAL = 7552;
    static final int OLM_MELEE_HAND_CM = 7555;
    static final int ANIM_HEAD_MAGIC = -1;
    static final int ANIM_HEAD_RANGE = -1;
    static final int ANIM_HEAD_CRYSTAL_BURST = -1;
    static final int ANIM_HEAD_LIGHTNING = -1;
    static final int ANIM_HEAD_HEAL_POOL = -1;
    static final int ANIM_HEAD_DEATH = -1;
    static final int ANIM_OLM_RETREAT = -1;
    static final int ANIM_OLM_EMERGE = -1;
    static final int ANIM_HAND_CLAW = -1;
    static final int ANIM_HAND_SLAM = -1;
    static final int ANIM_HAND_MAGIC = -1;
    static final int ANIM_HAND_ACID = -1;
    static final int ANIM_HAND_FIRE_WALL = -1;
    static final int ANIM_HAND_CRIPPLE = -1;
    static final int ANIM_HAND_RISE = -1;
    static final int PROJ_OLM_MAGIC = 1339;
    static final int PROJ_OLM_RANGE = 1340;
    static final int PLAYER_PROJ_SANG = 1539;
    static final int PROJ_SPHERE_RED = -1;
    static final int PROJ_SPHERE_GREEN = -1;
    static final int PROJ_SPHERE_BLUE = -1;
    static final int PROJ_CRYSTAL_BURST = -1;
    static final int PROJ_ACID_SPLAT = -1;
    static final int PROJ_LIGHTNING = -1;
    static final int PROJ_FALLING_CRYSTAL = -1;
    static final int PROJ_LIFE_SIPHON = -1;
    static final int GFX_FALLING_CRYSTAL = -1;
    static final int GFX_TELEPORT_SWAP = -1;
    static final int GFX_DEEP_BURN = -1;
    static final int OBJ_CRYSTAL_BURST = 30034;
    static final int[] OBJ_CRYSTAL_GROWTH = new int[]{30032, 30033, 30034};
    static final int OBJ_CRYSTAL_BOMB = -1;
    static final int OBJ_LIGHTNING = -1;
    static final int OBJ_LIFE_SIPHON_SHIELD = -1;
    static final int OBJ_FIRE_WALL = -1;
    static final int OBJ_REWARD_CHEST = 29742;
    static final int PHASE_TRANSITION_MAX_TICKS = 20;
    static final int CRYSTAL_BURST_TICKS = 4;
    static final int CRYSTAL_HOLD_TICKS = 4;
    static final int FALLING_CRYSTAL_TICKS = 3;
    static final int CRYSTAL_BOMB_FLEE_TILES = 5;
    static final int LIGHTNING_HOLD_TICKS = 3;
    static final int ACID_PHASE_MAX_TICKS = 35;
    static final int MELEE_HAND_CRIPPLE_HP_FRAC = 20;
    static final int CHEST_WAIT_TICKS = 5;
    static final int PRAYER_REACT_HOLD_TICKS = 6;
    static final int SPHERE_REACT_HOLD_TICKS = 3;
    static final int EQUIP_MAX_ATTEMPTS = 6;
    static final int HEAD_PROBE_TICKS = 10;
    static final int HEAD_FRAC_DROP_MIN = 980;
    static final int HEAD_FRAC_HEAL_BACK = 60;
    static final int HAND_SOFTEN_FRAC = 200;
    static final int HEAD_SETTLE_TICKS = 5;
    static final int CLENCH_STICKY_TICKS = 17;
    static final int HEAD_SELFHEAL_WATCHDOG_TICKS = 12;
    static final int HEAD_DROP_EPS = 30;
    static final int HEAD_KILL_FRAC_MAX = 500;
    static final int HEAD_REPROBE_WATCHDOG_TICKS = 50;
    static final int PRAYER_DISABLE_TICKS = 5;
    static final int TELEPORT_PULL_TICKS = 3;
    static final int[] ACID_POOL_IDS = new int[]{30038, 30039, 30040, 30041, 30042, 30043, 30044, 30045};
    static final int ACID_SCAN_RADIUS = 8;

    OlmConstants() {
    }

    static boolean isCrystalGrowthId(int n) {
        for (int n2 : OBJ_CRYSTAL_GROWTH) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    static boolean isAcidId(int n) {
        for (int n2 : ACID_POOL_IDS) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }
}

