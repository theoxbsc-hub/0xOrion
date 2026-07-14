/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;

public final class CombatFocus {
    public static final int INTERVAL = 20;
    private Skill current;
    private int bandCeil;

    public void reset() {
        this.current = null;
        this.bandCeil = 0;
    }

    public Skill select(int n) {
        if (this.current != null && LevelUtil.level(this.current) < this.bandCeil && LevelUtil.level(this.current) < n) {
            return this.current;
        }
        return this.repick(n);
    }

    public Skill current() {
        return this.current;
    }

    private Skill repick(int n) {
        this.current = CombatFocus.lowestBelowTarget(n);
        if (this.current != null) {
            this.bandCeil = Math.min(LevelUtil.level(this.current) + 20, n);
        }
        return this.current;
    }

    private static Skill lowestBelowTarget(int n) {
        int n2 = LevelUtil.level(Skill.ATTACK);
        int n3 = LevelUtil.level(Skill.STRENGTH);
        int n4 = LevelUtil.level(Skill.DEFENCE);
        Skill skill = null;
        int n5 = Integer.MAX_VALUE;
        if (n2 < n && n2 < n5) {
            skill = Skill.ATTACK;
            n5 = n2;
        }
        if (n3 < n && n3 < n5) {
            skill = Skill.STRENGTH;
            n5 = n3;
        }
        if (n4 < n && n4 < n5) {
            skill = Skill.DEFENCE;
            n5 = n4;
        }
        return skill;
    }

    public static int styleIndexFor(Skill skill) {
        if (skill == Skill.ATTACK) {
            return 0;
        }
        if (skill == Skill.STRENGTH) {
            return 1;
        }
        return 3;
    }
}

