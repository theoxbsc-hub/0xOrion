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

public final class AfkSkillJob
extends SkillJob {
    private final SkillId id;
    private final Skill skill;
    private final AfkTrainer afk;
    private boolean started = false;

    public AfkSkillJob(MasterContext masterContext, SkillId skillId) {
        super(masterContext);
        this.id = skillId;
        this.skill = skillId.rlSkill();
        this.afk = new AfkTrainer(masterContext);
    }

    @Override
    public SkillId id() {
        return this.id;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(this.skill);
    }

    @Override
    public int targetLevel() {
        return this.target();
    }

    @Override
    public void reset() {
        super.reset();
        this.started = false;
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

    private int target() {
        switch (this.id) {
            case FISHING: {
                return this.ctx.config.fishingTarget();
            }
            case HUNTER: {
                return this.ctx.config.hunterTarget();
            }
            case MINING: {
                return this.ctx.config.miningTarget();
            }
            case SMITHING: {
                return this.ctx.config.smithingTarget();
            }
            case MAGIC: {
                return this.ctx.config.magicTarget();
            }
            case RANGED: {
                return this.ctx.config.rangedTarget();
            }
        }
        return 99;
    }

    private AfkTrainer.Spec spec() {
        switch (this.id) {
            case FISHING: {
                return new AfkTrainer.Spec(Skill.FISHING, 297, 165, AioSkillerConstants.AFK_FISH_DEST, 13609, "AFK-fish");
            }
            case HUNTER: {
                return new AfkTrainer.Spec(Skill.HUNTER, 215, 240, AioSkillerConstants.AFK_HUNT_DEST, 47534, "AFK-hunt");
            }
            case MINING: {
                return new AfkTrainer.Spec(Skill.MINING, 255, 133, AioSkillerConstants.AFK_MINE_DEST, 47557, "AFK-mine");
            }
            case SMITHING: {
                return new AfkTrainer.Spec(Skill.SMITHING, 295, 133, AioSkillerConstants.AFK_SMITH_DEST, 47558, "AFK-smelt");
            }
            case MAGIC: {
                return new AfkTrainer.Spec(Skill.MAGIC, 294, 96, AioSkillerConstants.AFK_DUMMY_DEST, 47563, "AFK-magic");
            }
            case RANGED: {
                return new AfkTrainer.Spec(Skill.RANGED, 294, 96, AioSkillerConstants.AFK_DUMMY_DEST, 47564, "AFK-ranged");
            }
        }
        throw new IllegalStateException("AfkSkillJob has no AFK spec for " + String.valueOf((Object)this.id));
    }
}

