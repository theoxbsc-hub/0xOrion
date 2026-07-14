/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller;

import net.runelite.api.Skill;

public enum SkillId {
    AGILITY("Agility", Skill.AGILITY, "agility"),
    HUNTER("Hunter", Skill.HUNTER, "hunter"),
    MINING("Mining", Skill.MINING, "mining"),
    SMITHING("Smithing", Skill.SMITHING, "smithing"),
    THIEVING("Thieving", Skill.THIEVING, "thieving"),
    WOODCUTTING("Woodcutting", Skill.WOODCUTTING, "woodcutting"),
    CRAFTING("Crafting", Skill.CRAFTING, "crafting"),
    HERBLORE("Herblore", Skill.HERBLORE, "herblore"),
    FARMING("Farming", Skill.FARMING, "farming"),
    COMBAT("Combat", null, "combat"),
    MAGIC("Magic", Skill.MAGIC, "magic"),
    FISHING("Fishing", Skill.FISHING, "fishing"),
    RANGED("Ranged", Skill.RANGED, "ranged");

    private final String displayName;
    private final Skill rlSkill;
    private final String key;

    private SkillId(String string2, Skill skill, String string3) {
        this.displayName = string2;
        this.rlSkill = skill;
        this.key = string3;
    }

    public String displayName() {
        return this.displayName;
    }

    public Skill rlSkill() {
        return this.rlSkill;
    }

    public String key() {
        return this.key;
    }

    public static SkillId fromKey(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim().toLowerCase();
        if (string2.isEmpty()) {
            return null;
        }
        for (SkillId skillId : SkillId.values()) {
            if (!skillId.key.equals(string2) && !skillId.displayName.toLowerCase().equals(string2)) continue;
            return skillId;
        }
        return null;
    }
}

