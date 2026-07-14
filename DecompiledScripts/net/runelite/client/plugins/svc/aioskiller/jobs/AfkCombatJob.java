/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.AfkTrainer;
import net.runelite.client.plugins.svc.aioskiller.services.CombatFocus;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;

public final class AfkCombatJob
extends SkillJob {
    private final AfkTrainer afk;
    private final CombatFocus focus = new CombatFocus();
    private boolean started = false;
    private Skill currentSub = null;

    public AfkCombatJob(MasterContext masterContext) {
        super(masterContext);
        this.afk = new AfkTrainer(masterContext);
    }

    @Override
    public SkillId id() {
        return SkillId.COMBAT;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.combatMin();
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.combatTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.started = false;
        this.currentSub = null;
        this.focus.reset();
    }

    @Override
    public int tick() {
        Skill skill;
        if (!this.started) {
            this.started = true;
            this.phase = JobPhase.TRAIN;
        }
        if ((skill = this.focus.select(this.ctx.config.combatTarget())) == null) {
            this.status = "AFK combat: all sub-skills at target";
            return 600;
        }
        if (skill != this.currentSub) {
            this.currentSub = skill;
            this.afk.begin(new AfkTrainer.Spec(skill, 294, 96, AioSkillerConstants.AFK_DUMMY_DEST, 47562, AfkCombatJob.actionFor(skill)));
        }
        int n = this.afk.step(this.ctx.local);
        this.status = "AFK " + this.currentSub.name().toLowerCase() + ": " + this.afk.status();
        return n;
    }

    private static String actionFor(Skill skill) {
        if (skill == Skill.ATTACK) {
            return "AFK-attack";
        }
        if (skill == Skill.STRENGTH) {
            return "AFK-strength";
        }
        return "AFK-defence";
    }
}

