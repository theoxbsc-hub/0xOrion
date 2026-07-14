/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.corruptedgauntlet;

import net.runelite.client.plugins.svc.corruptedgauntlet.TornadoBehaviour;

final class HumanizationProfile {
    final int reactionMinTicks;
    final int reactionMaxTicks;
    final int jitterPercent;
    final TornadoBehaviour tornadoBehaviour;
    final int tornadoEvadeChance;
    final int hesitationPercent;
    final boolean earlyLavaAvoidance;
    final int minStepIntervalTicks;
    final int tornadoKeepDistance;
    final int prayerMissPercent;

    private HumanizationProfile(int n, int n2, int n3, TornadoBehaviour tornadoBehaviour, int n4, int n5, boolean bl, int n6, int n7, int n8) {
        this.reactionMinTicks = n;
        this.reactionMaxTicks = n2;
        this.jitterPercent = n3;
        this.tornadoBehaviour = tornadoBehaviour;
        this.tornadoEvadeChance = n4;
        this.hesitationPercent = n5;
        this.earlyLavaAvoidance = bl;
        this.minStepIntervalTicks = n6;
        this.tornadoKeepDistance = n7;
        this.prayerMissPercent = n8;
    }

    static HumanizationProfile of(int n) {
        switch (Math.max(1, Math.min(5, n))) {
            case 1: {
                return new HumanizationProfile(1, 3, 30, TornadoBehaviour.MIX, 50, 12, false, 1, 4, 0);
            }
            case 2: {
                return new HumanizationProfile(0, 2, 24, TornadoBehaviour.MIX, 35, 8, false, 1, 3, 0);
            }
            case 3: {
                return new HumanizationProfile(0, 2, 18, TornadoBehaviour.MIX, 20, 5, false, 1, 3, 0);
            }
            case 4: {
                return new HumanizationProfile(0, 1, 12, TornadoBehaviour.MIX, 10, 3, false, 1, 3, 0);
            }
        }
        return new HumanizationProfile(0, 0, 0, TornadoBehaviour.ATTACK_AND_EVADE, 0, 0, true, 0, 2, 0);
    }
}

