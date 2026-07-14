/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.unethicalite.api.movement.pathfinder.model.requirement;

import net.runelite.api.Skill;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirement;

public final class SkillRequirement
implements Requirement {
    private final Skill skill;
    private final int level;

    @Override
    public Boolean get() {
        return Skills.getLevel(this.skill) >= this.level;
    }

    public SkillRequirement(Skill skill, int level) {
        this.skill = skill;
        this.level = level;
    }

    public Skill getSkill() {
        return this.skill;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SkillRequirement)) {
            return false;
        }
        SkillRequirement other = (SkillRequirement)o;
        if (this.getLevel() != other.getLevel()) {
            return false;
        }
        Skill this$skill = this.getSkill();
        Skill other$skill = other.getSkill();
        return !(this$skill == null ? other$skill != null : !this$skill.equals(other$skill));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getLevel();
        Skill $skill = this.getSkill();
        result = result * 59 + ($skill == null ? 43 : $skill.hashCode());
        return result;
    }

    public String toString() {
        return "SkillRequirement(skill=" + String.valueOf(this.getSkill()) + ", level=" + this.getLevel() + ")";
    }
}

