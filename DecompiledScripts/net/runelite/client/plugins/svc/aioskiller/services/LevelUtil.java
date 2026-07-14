/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import net.runelite.api.Skill;
import net.unethicalite.api.game.Skills;

public final class LevelUtil {
    private LevelUtil() {
    }

    public static int level(Skill skill) {
        try {
            return Skills.getLevel(skill);
        }
        catch (Throwable throwable) {
            return 1;
        }
    }

    public static int combatMin() {
        return Math.min(LevelUtil.level(Skill.ATTACK), Math.min(LevelUtil.level(Skill.STRENGTH), LevelUtil.level(Skill.DEFENCE)));
    }

    public static boolean reached(Skill skill, int n) {
        return LevelUtil.level(skill) >= n;
    }
}

