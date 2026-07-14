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
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;

public final class FarmingJob
extends SkillJob {
    private final AfkTrainer afk;
    private boolean started = false;

    public FarmingJob(MasterContext masterContext) {
        super(masterContext);
        this.afk = new AfkTrainer(masterContext);
    }

    @Override
    public SkillId id() {
        return SkillId.FARMING;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.FARMING);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.farmingTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.started = false;
    }

    private AfkTrainer.Spec spec() {
        return new AfkTrainer.Spec(Skill.FARMING, 219, 273, AioSkillerConstants.AFK_FARM_DEST, 16059, "AFK-farm");
    }

    @Override
    public int tick() {
        if (!this.started) {
            this.afk.begin(this.spec());
            this.started = true;
            this.phase = JobPhase.TRAIN;
        }
        int n = this.afk.step(this.ctx.local);
        this.status = this.afk.status();
        return n;
    }
}

