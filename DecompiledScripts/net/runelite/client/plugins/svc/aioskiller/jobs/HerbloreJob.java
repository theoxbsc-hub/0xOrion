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

public final class HerbloreJob
extends SkillJob {
    private final AfkTrainer afk;
    private boolean started = false;

    public HerbloreJob(MasterContext masterContext) {
        super(masterContext);
        this.afk = new AfkTrainer(masterContext);
    }

    @Override
    public SkillId id() {
        return SkillId.HERBLORE;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.HERBLORE);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.herbloreTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.started = false;
    }

    private AfkTrainer.Spec spec() {
        return new AfkTrainer.Spec(Skill.HERBLORE, 258, 167, AioSkillerConstants.AFK_HERB_DEST, 47521, "AFK Herblore");
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

