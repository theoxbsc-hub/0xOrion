/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.movement.pathfinder.model.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.unethicalite.api.movement.pathfinder.model.requirement.ItemRequirement;
import net.unethicalite.api.movement.pathfinder.model.requirement.QuestRequirement;
import net.unethicalite.api.movement.pathfinder.model.requirement.Requirement;
import net.unethicalite.api.movement.pathfinder.model.requirement.SkillRequirement;
import net.unethicalite.api.movement.pathfinder.model.requirement.VarRequirement;
import net.unethicalite.api.movement.pathfinder.model.requirement.WorldRequirement;

public final class Requirements {
    private final List<ItemRequirement> itemRequirements = new ArrayList<ItemRequirement>();
    private final List<SkillRequirement> skillRequirements = new ArrayList<SkillRequirement>();
    private final List<VarRequirement> varRequirements = new ArrayList<VarRequirement>();
    private final List<QuestRequirement> questRequirements = new ArrayList<QuestRequirement>();
    private final List<WorldRequirement> worldRequirements = new ArrayList<WorldRequirement>();

    public boolean fulfilled() {
        ArrayList<Requirement> all = new ArrayList<Requirement>();
        all.addAll(this.itemRequirements);
        all.addAll(this.skillRequirements);
        all.addAll(this.varRequirements);
        all.addAll(this.questRequirements);
        all.addAll(this.worldRequirements);
        return all.stream().allMatch(Supplier::get);
    }

    public List<ItemRequirement> getItemRequirements() {
        return this.itemRequirements;
    }

    public List<SkillRequirement> getSkillRequirements() {
        return this.skillRequirements;
    }

    public List<VarRequirement> getVarRequirements() {
        return this.varRequirements;
    }

    public List<QuestRequirement> getQuestRequirements() {
        return this.questRequirements;
    }

    public List<WorldRequirement> getWorldRequirements() {
        return this.worldRequirements;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Requirements)) {
            return false;
        }
        Requirements other = (Requirements)o;
        List<ItemRequirement> this$itemRequirements = this.getItemRequirements();
        List<ItemRequirement> other$itemRequirements = other.getItemRequirements();
        if (this$itemRequirements == null ? other$itemRequirements != null : !((Object)this$itemRequirements).equals(other$itemRequirements)) {
            return false;
        }
        List<SkillRequirement> this$skillRequirements = this.getSkillRequirements();
        List<SkillRequirement> other$skillRequirements = other.getSkillRequirements();
        if (this$skillRequirements == null ? other$skillRequirements != null : !((Object)this$skillRequirements).equals(other$skillRequirements)) {
            return false;
        }
        List<VarRequirement> this$varRequirements = this.getVarRequirements();
        List<VarRequirement> other$varRequirements = other.getVarRequirements();
        if (this$varRequirements == null ? other$varRequirements != null : !((Object)this$varRequirements).equals(other$varRequirements)) {
            return false;
        }
        List<QuestRequirement> this$questRequirements = this.getQuestRequirements();
        List<QuestRequirement> other$questRequirements = other.getQuestRequirements();
        if (this$questRequirements == null ? other$questRequirements != null : !((Object)this$questRequirements).equals(other$questRequirements)) {
            return false;
        }
        List<WorldRequirement> this$worldRequirements = this.getWorldRequirements();
        List<WorldRequirement> other$worldRequirements = other.getWorldRequirements();
        return !(this$worldRequirements == null ? other$worldRequirements != null : !((Object)this$worldRequirements).equals(other$worldRequirements));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        List<ItemRequirement> $itemRequirements = this.getItemRequirements();
        result = result * 59 + ($itemRequirements == null ? 43 : ((Object)$itemRequirements).hashCode());
        List<SkillRequirement> $skillRequirements = this.getSkillRequirements();
        result = result * 59 + ($skillRequirements == null ? 43 : ((Object)$skillRequirements).hashCode());
        List<VarRequirement> $varRequirements = this.getVarRequirements();
        result = result * 59 + ($varRequirements == null ? 43 : ((Object)$varRequirements).hashCode());
        List<QuestRequirement> $questRequirements = this.getQuestRequirements();
        result = result * 59 + ($questRequirements == null ? 43 : ((Object)$questRequirements).hashCode());
        List<WorldRequirement> $worldRequirements = this.getWorldRequirements();
        result = result * 59 + ($worldRequirements == null ? 43 : ((Object)$worldRequirements).hashCode());
        return result;
    }

    public String toString() {
        return "Requirements(itemRequirements=" + String.valueOf(this.getItemRequirements()) + ", skillRequirements=" + String.valueOf(this.getSkillRequirements()) + ", varRequirements=" + String.valueOf(this.getVarRequirements()) + ", questRequirements=" + String.valueOf(this.getQuestRequirements()) + ", worldRequirements=" + String.valueOf(this.getWorldRequirements()) + ")";
    }
}

